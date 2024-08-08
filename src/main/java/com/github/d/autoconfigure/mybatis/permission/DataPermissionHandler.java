package com.github.d.autoconfigure.mybatis.permission;

import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Setter
public class DataPermissionHandler<T> implements MultiDataPermissionHandler, BeanFactoryAware {

    private final DataPermissionExchange dataScopeExchange;
    private final Supplier<T> principalSupplier;
    private final Function<T, Serializable> roleFn;
    private Map<DataPermissionEnum, String> rule;
    Map<String, DataPermission> dataAuthMap = new ConcurrentHashMap<>();

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private Function<DataPermissionScope, DataPermissionScope> scopeTransfer = Function.identity();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        this.evaluationContext.registerFunction("sqlParam", ReflectionUtils.findMethod(StringUtils.class, "sqlParam", Object.class));
    }

    @Override
    public Expression getSqlSegment(final Table table, final Expression where, final String mappedStatementId) {


        boolean select = Optional.of(table.getASTNode()).filter(e -> e.jjtGetParent() instanceof SimpleNode)
                .map(e -> (SimpleNode) e.jjtGetParent())
                .filter(e -> "SELECT".equalsIgnoreCase(e.jjtGetFirstToken().toString()))
                .filter(e -> Objects.equals(0, e.jjtGetParent().jjtGetParent().getId()))
                .isPresent();
        if (!select) {
            log.trace("{} {} AS {} : 非主表忽略", mappedStatementId, table.getName(), table.getAlias());
            return null;
        }

        T principal = principalSupplier.get();
        if (Objects.isNull(principal)) {
            return null;
        }

        try {
            DataPermission dataAuthAnnotation = findDataAuthAnnotation(mappedStatementId);
            DataPermissionScope prevScope = Optional.ofNullable(dataAuthAnnotation).map(e -> {
                DataPermissionScope dataScope = new DataPermissionScope();
                dataScope.setScopeType(e.type().getType());
                dataScope.setScopeValue(e.value());
                return dataScope;
            }).orElseGet(() -> {
                Serializable role = roleFn.apply(principal);
                return dataScopeExchange.getDataScope(mappedStatementId, role);
            });

            DataPermissionScope scope = scopeTransfer.apply(prevScope);

            if (Objects.isNull(scope)) {
                return null;
            }
            DataPermissionEnum scopeEnum = DataPermissionEnum.of(scope.getScopeType());
            if (Objects.equals(DataPermissionEnum.ALL, scopeEnum)) return null;
            String sqlSegment = Optional.ofNullable(scope.getScopeValue()).filter(StringUtils::isNotBlank).orElse(rule.get(scopeEnum));
            if (sqlSegment == null) {
                log.info("{} {} AS {} : NOT FOUND", mappedStatementId, table.getName(), table.getAlias());
                return null;
            }
            org.springframework.expression.Expression parseExpression = PARSER.parseExpression(sqlSegment, ParserContext.TEMPLATE_EXPRESSION);
            String value = parseExpression.getValue(evaluationContext, principal, String.class);
            Expression sqlSegmentExpression = CCJSqlParserUtil.parseCondExpression(value);
            if (Objects.isNull(sqlSegmentExpression)) {
                log.warn("表达式解析错误: {}", sqlSegment);
                return null;
            }
            sqlSegmentExpression.accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(Column value) {
                    super.visit(value);
                    value.setTable(table);
                }
            });

            log.info("{} {} AS {} : {}", mappedStatementId, table.getName(), table.getAlias(), sqlSegmentExpression);
            return sqlSegmentExpression;
        } catch (JSQLParserException e) {
            log.error("数据权限解析错误", e);
        }
        return null;
    }


    /**
     * 获取数据权限注解信息
     *
     * @return DataAuth
     */
    private DataPermission findDataAuthAnnotation(String id) {
        return dataAuthMap.computeIfAbsent(id, (key) -> {
            String className = key.substring(0, key.lastIndexOf(StringPool.DOT));
            String methodName = key.substring(key.lastIndexOf(StringPool.DOT) + 1);
            Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(ClassUtils.toClassConfident(className), method -> Objects.equals(method.getName(), methodName));
            return Arrays.stream(methods).map(e -> e.getAnnotation(DataPermission.class)).findFirst().orElse(null);
        });
    }


}

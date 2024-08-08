package com.github.d.autoconfigure.mybatis;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.d.autoconfigure.mybatis.permission.ComposeDataPermissionExchange;
import com.github.d.autoconfigure.mybatis.permission.DataPermissionEnum;
import com.github.d.autoconfigure.mybatis.permission.DataPermissionExchange;
import com.github.d.autoconfigure.mybatis.permission.DataPermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({DataInterceptProperties.class})
@ConditionalOnProperty(prefix = "mp.permission", name = "enabled", matchIfMissing = true)
@ConditionalOnBean({MybatisPlusAutoConfiguration.class})
public class MybatisPlusPermissionAutoConfiguration {


    /**
     * 默认使用 spring security里面的对象作为拦截数据源供给者
     *
     * @param dataScopeExchanges
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({SecurityContextHolder.class})
    public DataPermissionHandler<Authentication> defaultDataInterceptor(@Autowired(required = false) List<DataPermissionExchange> dataScopeExchanges) {
        DataPermissionHandler<Authentication> interceptor = new DataPermissionHandler<>(new ComposeDataPermissionExchange(dataScopeExchanges),
                // 凭证
                () -> SecurityContextHolder.getContext().getAuthentication(),
                // 角色提供, 检索拼接片段
                (Authentication e) -> e.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(t -> t.startsWith("ROLE_")).collect(Collectors.joining(",")));
        Map<DataPermissionEnum, String> rule = new LinkedHashMap<>();
        // 规则映射
        rule.put(DataPermissionEnum.OWN, "create_by = #{#sqlParam(principal.username)}");

        interceptor.setRule(rule);
        return interceptor;
    }

}

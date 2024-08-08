## 使用方式

```xml
    <dependency>
        <groupId>com.github</groupId>
        <artifactId>permission-spring-boot-starter</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
```



## mybatis plus 行级数据拦截

```java
@Configuration
@MapperScan("com.github.*.mapper")
public class MybatisPlusConfigure {

    /**
     * 添加插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<DataPermissionHandler<Authentication>> dataInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        dataInterceptor.ifAvailable(e -> interceptor.addInnerInterceptor(new DataPermissionInterceptor(e)));
        return interceptor;
    }

    /**
     * 自行实现
     * @param dataScopeExchanges
     * @return
     */
    @Bean
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

```

## spring security 动态权限

参照`DynamicSecurityAutoConfiguration`实现对应的web环境所需的权限加载器即可

webflux：ReactiveSecurityMappingLoader

servlet：SecurityMappingLoader

默认配置security.dynamic.route,会被解析为对应环境中权限加载器

```java
@Configuration
public class SecurityConfigure {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityMappingLoader securityMappingLoader() {
        // 自行实现 jdbc,redis等获取方式
        return () -> RequestMatchers.antMatchers("/user/**").stream().map(matcher->new RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>(matcher, AuthorityAuthorizationManager.hasRole("user"))).toList();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public ReactiveSecurityMappingLoader reactiveSecurityMappingLoader() {
        // 自行实现 jdbc,redis等获取方式
        return () -> List.of(new ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>(ServerWebExchangeMatchers.pathMatchers("/user/**"), AuthorityReactiveAuthorizationManager.hasRole("user")));
    }

}
```

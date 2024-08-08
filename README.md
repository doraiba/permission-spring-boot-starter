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
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<DataPermissionHandler<Authentication>> dataInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        dataInterceptor.ifAvailable(e -> interceptor.addInnerInterceptor(new DataPermissionInterceptor(e)));
        return interceptor;
    }

    // 自定义实现即可
    @Bean
    public DataPermissionExchange dataPermissionExchange() {
        // 注册拦截
        Map<String, DataPermissionScope> map = Map.of("com.github.pdfinvoice.mapper.CompanyManagerMapper.selectJoinList-com-github-pdfinvoice-dto-CompanyDTO", DataPermissionScope.of(DataPermissionEnum.OWN.getType(), null));
        return (mapperId, role) -> map.get(mapperId);
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
public class SecurityConfigure{

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityMappingLoader jdbcSecurityMappingLoader(){
        // 自行实现
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public ReactiveSecurityMappingLoader reactiveSecurityMappingLoader(){
        // 自行实现
    }
    
}
```

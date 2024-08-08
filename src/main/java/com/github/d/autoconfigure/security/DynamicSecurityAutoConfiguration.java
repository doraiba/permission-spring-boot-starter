package com.github.d.autoconfigure.security;

import com.github.d.autoconfigure.security.reactive.ReactiveSecurityMappingLoader;
import com.github.d.autoconfigure.security.reactive.SecurityWebFilterChainPostProcessor;
import com.github.d.autoconfigure.security.servlet.SecurityFilterChainPostProcessor;
import com.github.d.autoconfigure.security.servlet.SecurityMappingLoader;
import com.github.d.autoconfigure.security.util.RequestMatchers;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;

import java.util.List;

@Configuration
@EnableConfigurationProperties(DynamicSecurityProperties.class)
@ConditionalOnProperty(prefix = "security.dynamic", name = "enabled", matchIfMissing = true)
@AutoConfigureBefore({ReactiveSecurityAutoConfiguration.class, SecurityAutoConfiguration.class})
public class DynamicSecurityAutoConfiguration {


    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class InternalReactiveSecurityAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public SecurityWebFilterChainPostProcessor securityWebFilterChainPostProcessor() {
            return new SecurityWebFilterChainPostProcessor();
        }


        @Bean
        public ReactiveSecurityMappingLoader reactiveSecurityMappingLoader(DynamicSecurityProperties properties) {
            List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> list = properties.getRoute().entrySet().stream().map(e -> {
                String key = e.getKey();
                String[] split = key.split(" ");
                ServerWebExchangeMatcher matcher = split.length == 2 ? ServerWebExchangeMatchers.pathMatchers(HttpMethod.valueOf(split[0]), split[1]) : ServerWebExchangeMatchers.pathMatchers(key);
                return new ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>(matcher, AuthorityReactiveAuthorizationManager.hasRole(e.getValue()));
            }).toList();
            return () -> list;
        }
    }


    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class InternalServletSecurityAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public SecurityFilterChainPostProcessor securityFilterChainPostProcessor() {
            return new SecurityFilterChainPostProcessor();
        }


        @Bean
        public SecurityMappingLoader securityMappingLoader(DynamicSecurityProperties properties) throws ClassNotFoundException {


            List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> list = properties.getRoute().entrySet().stream().flatMap(e -> {
                String key = e.getKey();
                String[] split = key.split(" ");
                Object f = split.length == 2 ? RequestMatchers.antMatchers(HttpMethod.valueOf(split[0]), split[1]) : RequestMatchers.antMatchers(key);
                List<RequestMatcher> matchers = (List<RequestMatcher>) f;
                return matchers.stream().map(matcher -> new RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>(matcher, AuthorityAuthorizationManager.hasAuthority(e.getValue())));

            }).toList();
            return () -> list;
        }
    }
}

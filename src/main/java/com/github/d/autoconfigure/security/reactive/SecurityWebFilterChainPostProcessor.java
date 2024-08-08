package com.github.d.autoconfigure.security.reactive;

import com.github.d.autoconfigure.security.util.SecureUtil;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authorization.ObservationReactiveAuthorizationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.authorization.AuthorizationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class SecurityWebFilterChainPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Setter
    private Predicate<String> filterChainPredicate = (s) -> true;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SecurityWebFilterChain && filterChainPredicate.test(beanName)) {
            ((SecurityWebFilterChain) bean).getWebFilters().filter(e -> e instanceof AuthorizationWebFilter).map(e -> (AuthorizationWebFilter) e)
                    .next().subscribe(e -> {
                        SecureUtil.
                                <AuthorizationWebFilter, ReactiveAuthorizationManager<ServerWebExchange>, List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>>>substitute(e, ObservationReactiveAuthorizationManager.class, mappings -> {
                            List<ReactiveSecurityMappingLoader> mappingLoaders = new ArrayList<>(applicationContext.getBeansOfType(ReactiveSecurityMappingLoader.class).values());
                            return new ReplaceDelegatingReactiveAuthorizationManager(mappings, mappingLoaders);
                        });

                    });
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

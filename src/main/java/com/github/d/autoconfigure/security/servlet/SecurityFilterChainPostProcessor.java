package com.github.d.autoconfigure.security.servlet;

import com.github.d.autoconfigure.security.util.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.ObservationAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class SecurityFilterChainPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Setter
    private Predicate<String> filterChainPredicate = (s) -> true;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SecurityFilterChain && filterChainPredicate.test(beanName)) {
            ((SecurityFilterChain) bean).getFilters().stream().filter(e -> e instanceof AuthorizationFilter).map(e -> (AuthorizationFilter) e)
                    .findFirst().ifPresent(object -> {
                        SecureUtil.
                                <AuthorizationFilter, AuthorizationManager<HttpServletRequest>, List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>>>substitute(object, ObservationAuthorizationManager.class, mappings -> {
                            List<SecurityMappingLoader> mappingLoaders = new ArrayList<>(applicationContext.getBeansOfType(SecurityMappingLoader.class).values());
                            return new ReplaceRequestMatcherDelegatingAuthorizationManager(mappings, mappingLoaders);
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

package com.github.d.autoconfigure.security.servlet;

import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;

import java.util.List;

@FunctionalInterface
public interface SecurityMappingLoader {
    List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> load();

    static SecurityMappingLoader wrap(List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> mappings) {
        return () -> mappings;
    }
}

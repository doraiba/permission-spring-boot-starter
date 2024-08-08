package com.github.d.autoconfigure.security.reactive;

import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;

import java.util.List;

@FunctionalInterface
public interface ReactiveSecurityMappingLoader {
    List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> load();

    static ReactiveSecurityMappingLoader wrap(List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> mappings) {
        return () -> mappings;
    }
}

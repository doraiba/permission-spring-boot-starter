package com.github.d.autoconfigure.security.reactive;

import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class ReplaceDelegatingReactiveAuthorizationManager implements ReactiveAuthorizationManager<ServerWebExchange> {

    private static final Log logger = LogFactory.getLog(ReplaceDelegatingReactiveAuthorizationManager.class);

    private final List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> delegate;
    private final List<ReactiveSecurityMappingLoader> mappings;


    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, ServerWebExchange exchange) {
        return Flux.fromIterable(this.mappings).concatWithValues(ReactiveSecurityMappingLoader.wrap(delegate))
                .concatMapIterable(ReactiveSecurityMappingLoader::load)
                .concatMap((mapping) -> mapping.getMatcher()
                        .matches(exchange)
                        .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                        .map(ServerWebExchangeMatcher.MatchResult::getVariables)
                        .flatMap((variables) -> {
                            logger.debug(LogMessage.of(() -> "Checking authorization on '"
                                    + exchange.getRequest().getPath().pathWithinApplication() + "' using "
                                    + mapping.getEntry()));
                            return mapping.getEntry().check(authentication, new AuthorizationContext(exchange, variables));
                        }))
                .next()
                .defaultIfEmpty(new AuthorizationDecision(false));
    }


}

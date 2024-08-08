
package com.github.d.autoconfigure.security.servlet;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.log.LogMessage;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher.MatchResult;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;
import org.springframework.util.Assert;

/**
 * An {@link AuthorizationManager} which delegates to a specific
 * {@link AuthorizationManager} based on a {@link RequestMatcher} evaluation.
 *
 * @author Evgeniy Cheban
 * @author Parikshit Dutta
 * @since 5.5
 */
public class ReplaceRequestMatcherDelegatingAuthorizationManager implements AuthorizationManager<HttpServletRequest> {

    private static final AuthorizationDecision DENY = new AuthorizationDecision(false);

    private final Log logger = LogFactory.getLog(getClass());


    private final List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> delegate;
    private final List<SecurityMappingLoader> mappings;

    public ReplaceRequestMatcherDelegatingAuthorizationManager(
            List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> delegate, List<SecurityMappingLoader> mappings) {
        Assert.notEmpty(delegate, "mappings cannot be empty");
        this.delegate = delegate;
        this.mappings = mappings;
    }

    /**
     * Delegates to a specific {@link AuthorizationManager} based on a
     * {@link RequestMatcher} evaluation.
     *
     * @param authentication the {@link Supplier} of the {@link Authentication} to check
     * @param request        the {@link HttpServletRequest} to check
     * @return an {@link AuthorizationDecision}. If there is no {@link RequestMatcher}
     * matching the request, or the {@link AuthorizationManager} could not decide, then
     * null is returned
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, HttpServletRequest request) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(LogMessage.format("Authorizing %s", requestLine(request)));
        }

        return Stream.concat(mappings.stream().map(SecurityMappingLoader::load), Stream.of(delegate)).flatMap(l -> {
            return l.stream().map(mapping -> {

                RequestMatcher matcher = mapping.getRequestMatcher();
                MatchResult matchResult = matcher.matcher(request);
                if (matchResult.isMatch()) {
                    AuthorizationManager<RequestAuthorizationContext> manager = mapping.getEntry();
                    if (this.logger.isTraceEnabled()) {
                        this.logger.trace(
                                LogMessage.format("Checking authorization on %s using %s", requestLine(request), manager));
                    }
                    return manager.check(authentication,
                            new RequestAuthorizationContext(request, matchResult.getVariables()));
                }
                return null;
            });
        }).filter(Objects::nonNull).findFirst().orElseGet(()->{
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.of(() -> "Denying request since did not find matching RequestMatcher"));
            }
            return DENY;
        });

    }

    private static String requestLine(HttpServletRequest request) {
        return request.getMethod() + " " + UrlUtils.buildRequestUrl(request);
    }

}

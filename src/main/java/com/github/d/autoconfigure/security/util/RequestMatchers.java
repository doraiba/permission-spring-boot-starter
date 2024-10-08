package com.github.d.autoconfigure.security.util;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * copy from {@link org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry}
 */
public class RequestMatchers {

    private RequestMatchers() {
    }

    /**
     * Create a {@link List} of {@link AntPathRequestMatcher} instances.
     *
     * @param httpMethod  the {@link HttpMethod} to use or {@code null} for any
     *                    {@link HttpMethod}.
     * @param antPatterns the ant patterns to create {@link AntPathRequestMatcher}
     *                    from
     * @return a {@link List} of {@link AntPathRequestMatcher} instances
     */
    public static List<RequestMatcher> antMatchers(HttpMethod httpMethod, String... antPatterns) {
        return Arrays.asList(antMatchersAsArray(httpMethod, antPatterns));
    }

    /**
     * Create a {@link List} of {@link AntPathRequestMatcher} instances that do not
     * specify an {@link HttpMethod}.
     *
     * @param antPatterns the ant patterns to create {@link AntPathRequestMatcher}
     *                    from
     * @return a {@link List} of {@link AntPathRequestMatcher} instances
     */
    public static List<RequestMatcher> antMatchers(String... antPatterns) {
        return antMatchers(null, antPatterns);
    }

    public static RequestMatcher[] antMatchersAsArray(HttpMethod httpMethod, String... antPatterns) {
        String method = (httpMethod != null) ? httpMethod.toString() : null;
        RequestMatcher[] matchers = new RequestMatcher[antPatterns.length];
        for (int index = 0; index < antPatterns.length; index++) {
            matchers[index] = new AntPathRequestMatcher(antPatterns[index], method);
        }
        return matchers;
    }

    /**
     * Create a {@link List} of {@link RegexRequestMatcher} instances.
     *
     * @param httpMethod    the {@link HttpMethod} to use or {@code null} for any
     *                      {@link HttpMethod}.
     * @param regexPatterns the regular expressions to create
     *                      {@link RegexRequestMatcher} from
     * @return a {@link List} of {@link RegexRequestMatcher} instances
     */
    public static List<RequestMatcher> regexMatchers(HttpMethod httpMethod, String... regexPatterns) {
        String method = (httpMethod != null) ? httpMethod.toString() : null;
        List<RequestMatcher> matchers = new ArrayList<>();
        for (String pattern : regexPatterns) {
            matchers.add(new RegexRequestMatcher(pattern, method));
        }
        return matchers;
    }

    /**
     * Create a {@link List} of {@link RegexRequestMatcher} instances that do not
     * specify an {@link HttpMethod}.
     *
     * @param regexPatterns the regular expressions to create
     *                      {@link RegexRequestMatcher} from
     * @return a {@link List} of {@link RegexRequestMatcher} instances
     */
    public static List<RequestMatcher> regexMatchers(String... regexPatterns) {
        return regexMatchers(null, regexPatterns);
    }

}

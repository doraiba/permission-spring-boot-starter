package com.github.d.autoconfigure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "security.dynamic")
@Getter
@Setter
public class DynamicSecurityProperties {
    private Boolean enabled = true;

    private Map<String, String> route = new LinkedHashMap<>();
}

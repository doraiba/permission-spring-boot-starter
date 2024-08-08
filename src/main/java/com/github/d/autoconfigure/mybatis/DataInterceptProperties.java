package com.github.d.autoconfigure.mybatis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mp.permission")
@Getter
@Setter
public class DataInterceptProperties {

    private Boolean enabled = true;

}

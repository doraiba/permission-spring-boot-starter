package com.github.d.autoconfigure.mybatis.permission;


import org.springframework.core.Ordered;

import java.io.Serializable;

public interface DataPermissionExchange extends Ordered {

    DataPermissionScope getDataScope(String mapperId, Serializable role);

    @Override
    default int getOrder() {
        return 0;
    }
}

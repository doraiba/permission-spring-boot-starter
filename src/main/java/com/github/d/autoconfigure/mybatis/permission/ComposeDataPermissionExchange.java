package com.github.d.autoconfigure.mybatis.permission;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class ComposeDataPermissionExchange implements DataPermissionExchange {
    private List<DataPermissionExchange> exchanges;

    @Override
    public DataPermissionScope getDataScope(String mapperId, Serializable role) {
        if (Objects.isNull(exchanges)) {
            return null;
        }
        return exchanges.stream().map(exchange -> exchange.getDataScope(mapperId, role)).filter(Objects::nonNull).findFirst().orElse(null);

    }

}

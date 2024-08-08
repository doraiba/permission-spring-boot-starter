package com.github.d.autoconfigure.mybatis.permission;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DataPermissionScope {

    /**
     * 数据权限规则
     * {@link DataPermissionEnum}
     */
    private Integer scopeType;
    /**
     * 数据权限规则值
     */
    private String scopeValue;

    public static DataPermissionScope of(DataPermissionEnum scopeType) {
        return new DataPermissionScope(scopeType.getType(), null);
    }
}

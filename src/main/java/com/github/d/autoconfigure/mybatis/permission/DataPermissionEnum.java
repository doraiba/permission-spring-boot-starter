package com.github.d.autoconfigure.mybatis.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum DataPermissionEnum {
    /**
     * 全部数据
     */
    ALL(1, "全部"),

    /**
     * 本人可见
     */
    OWN(2, "本人可见"),

    /**
     * 所在机构可见
     */
    OWN_DEPT(3, "所在机构可见"),

    /**
     * 所在机构及子级可见
     */
    OWN_DEPT_CHILD(4, "所在机构及子级可见"),

    /**
     * 自定义
     */
    CUSTOM(5, "自定义");

    /**
     * 类型
     */
    private final int type;
    /**
     * 描述
     */
    private final String description;

    public static DataPermissionEnum of(Integer dataPermission) {
        return Optional.ofNullable(dataPermission).flatMap(e -> Arrays.stream(values()).filter(t -> t.type == dataPermission).findFirst()).orElse(null);
    }
}

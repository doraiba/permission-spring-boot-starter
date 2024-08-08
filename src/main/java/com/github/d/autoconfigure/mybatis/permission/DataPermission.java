
package com.github.d.autoconfigure.mybatis.permission;


import java.lang.annotation.*;

/**
 * 数据权限定义
 *
 * @author Chill
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DataPermission {
	/**
	 * 数据权限规则
	 */
	DataPermissionEnum type() default DataPermissionEnum.ALL;

	/**
	 * 数据权限规则值域
	 */
	String value() default "";

}


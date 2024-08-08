package com.github.d.autoconfigure.security.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Function;

public class SecureUtil {


    public static <E, T, R> E substitute(E object, Class<?> delegate, Function<R, T> function) {

        Field manager = ReflectionUtils.findField(object.getClass(), "authorizationManager");
        manager.setAccessible(true);
        T authorizationManager = (T) ReflectionUtils.getField(manager, object);

        boolean obs = delegate.isInstance(authorizationManager);
        T target = authorizationManager;
        Field delegatingAuthorizationManager = null;
        if (obs) {
            delegatingAuthorizationManager = ReflectionUtils.findField(authorizationManager.getClass(), "delegate");
            delegatingAuthorizationManager.setAccessible(true);
            target = (T) ReflectionUtils.getField(delegatingAuthorizationManager, authorizationManager);
        }
        Field mappingsField = ReflectionUtils.findField(target.getClass(), "mappings");
        mappingsField.setAccessible(true);
        R mappings = (R) ReflectionUtils.getField(mappingsField, target);

        T requestAuthorizationManager = function.apply(mappings);

        if (obs) {
            ReflectionUtils.setField(delegatingAuthorizationManager, authorizationManager, requestAuthorizationManager);
        }
        if (!obs) {
            ReflectionUtils.setField(manager, object, requestAuthorizationManager);
        }
        return object;
    }


}

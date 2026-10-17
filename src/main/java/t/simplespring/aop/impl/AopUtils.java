package t.simplespring.aop.impl;

import t.simplespring.aop.Advisor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AopUtils {

    private AopUtils() {
    }

    public static boolean canApply(Advisor advisor, Class<?> targetClass) {
        for (Class<?> proxiedInterface : getAllInterfaces(targetClass)) {
            for (Method method : proxiedInterface.getMethods()) {
                if (!Modifier.isStatic(method.getModifiers())
                        && advisor.getPointcut().matches(getMostSpecificMethod(method, targetClass), targetClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        try {
            return targetClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return method;
        }
    }

    public static Set<Class<?>> getAllInterfaces(Class<?> targetClass) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        for (Class<?> type = targetClass; type != null; type = type.getSuperclass()) {
            interfaces.addAll(List.of(type.getInterfaces()));
        }
        return interfaces;
    }
}

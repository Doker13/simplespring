package t.simplespring.aop;

import java.lang.reflect.Method;

@FunctionalInterface
public interface Pointcut {

    boolean matches(Method method, Class<?> targetClass);
}

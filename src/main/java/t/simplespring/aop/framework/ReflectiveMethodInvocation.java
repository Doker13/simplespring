package t.simplespring.aop.framework;

import t.simplespring.aop.MethodInterceptor;
import t.simplespring.aop.MethodInvocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectiveMethodInvocation implements MethodInvocation {

    private final Object target;
    private final Method method;
    private final Object[] arguments;
    private final List<MethodInterceptor> interceptors;
    private int currentInterceptorIndex;

    public ReflectiveMethodInvocation(Object target, Method method, Object[] arguments,
                                      List<MethodInterceptor> interceptors) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.interceptors = interceptors;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public Object proceed() throws Throwable {
        if (currentInterceptorIndex < interceptors.size()) {
            return interceptors.get(currentInterceptorIndex++).invoke(this);
        }
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}

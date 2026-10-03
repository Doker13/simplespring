package t.simplespring.aop.framework;

import t.simplespring.aop.Advisor;
import t.simplespring.aop.MethodInterceptor;
import t.simplespring.aop.support.AopUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JdkDynamicAopProxy implements InvocationHandler {

    private static final Object[] NO_ARGUMENTS = new Object[0];

    private final Object target;
    private final List<Advisor> advisors;
    private final Map<Method, List<MethodInterceptor>> interceptorsCache = new ConcurrentHashMap<>();

    public JdkDynamicAopProxy(Object target, List<Advisor> advisors) {
        this.target = target;
        this.advisors = List.copyOf(advisors);
    }

    public Object getProxy() {
        Class<?> targetClass = target.getClass();
        return Proxy.newProxyInstance(targetClass.getClassLoader(),
                AopUtils.getAllInterfaces(targetClass).toArray(Class<?>[]::new), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class && method.getName().equals("equals")) {
            return proxy == args[0];
        }
        List<MethodInterceptor> interceptors = method.getDeclaringClass() == Object.class
                ? List.of()
                : interceptorsCache.computeIfAbsent(method, this::getInterceptors);
        return new ReflectiveMethodInvocation(target, method, args == null ? NO_ARGUMENTS : args, interceptors)
                .proceed();
    }

    private List<MethodInterceptor> getInterceptors(Method method) {
        Class<?> targetClass = target.getClass();
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return advisors.stream()
                .filter(advisor -> advisor.getPointcut().matches(specificMethod, targetClass))
                .map(Advisor::getAdvice)
                .toList();
    }
}

package t.simplespring.aop;

@FunctionalInterface
public interface MethodInterceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}

package t.simplespring.aop;

public interface Advisor {

    Pointcut getPointcut();

    MethodInterceptor getAdvice();
}

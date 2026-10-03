package t.simplespring.aop.support;

import t.simplespring.aop.Advisor;
import t.simplespring.aop.MethodInterceptor;
import t.simplespring.aop.Pointcut;

import java.util.Objects;

public class DefaultPointcutAdvisor implements Advisor {

    private final Pointcut pointcut;
    private final MethodInterceptor advice;

    public DefaultPointcutAdvisor(Pointcut pointcut, MethodInterceptor advice) {
        this.pointcut = Objects.requireNonNull(pointcut, "pointcut");
        this.advice = Objects.requireNonNull(advice, "advice");
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public MethodInterceptor getAdvice() {
        return advice;
    }
}

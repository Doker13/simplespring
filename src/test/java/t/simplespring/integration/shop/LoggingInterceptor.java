package t.simplespring.integration.shop;

import t.simplespring.aop.MethodInterceptor;
import t.simplespring.aop.MethodInvocation;
import t.simplespring.stereotype.Component;

@Component
public class LoggingInterceptor implements MethodInterceptor {

    private final Journal journal;

    public LoggingInterceptor(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        journal.record("before " + methodName);
        Object result = invocation.proceed();
        journal.record("after " + methodName);
        return result;
    }
}

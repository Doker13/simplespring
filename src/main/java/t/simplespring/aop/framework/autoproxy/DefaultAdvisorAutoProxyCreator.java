package t.simplespring.aop.framework.autoproxy;

import t.simplespring.aop.Advisor;
import t.simplespring.aop.MethodInterceptor;
import t.simplespring.aop.Pointcut;
import t.simplespring.aop.framework.JdkDynamicAopProxy;
import t.simplespring.aop.support.AopUtils;
import t.simplespring.beans.factory.ListableBeanFactory;
import t.simplespring.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAdvisorAutoProxyCreator implements BeanPostProcessor {

    private final ListableBeanFactory beanFactory;
    private final Map<Class<?>, List<Advisor>> eligibleAdvisorsCache = new HashMap<>();
    private List<Advisor> candidateAdvisors;
    private boolean resolvingAdvisors;

    public DefaultAdvisorAutoProxyCreator(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (isInfrastructureBean(bean) || resolvingAdvisors) {
            return bean;
        }
        List<Advisor> advisors = findEligibleAdvisors(bean.getClass());
        return advisors.isEmpty() ? bean : new JdkDynamicAopProxy(bean, advisors).getProxy();
    }

    private static boolean isInfrastructureBean(Object bean) {
        return bean instanceof Advisor || bean instanceof Pointcut || bean instanceof MethodInterceptor
                || bean instanceof BeanPostProcessor;
    }

    private List<Advisor> findEligibleAdvisors(Class<?> beanClass) {
        List<Advisor> advisors = eligibleAdvisorsCache.get(beanClass);
        if (advisors == null) {
            advisors = findCandidateAdvisors().stream()
                    .filter(advisor -> AopUtils.canApply(advisor, beanClass))
                    .toList();
            eligibleAdvisorsCache.put(beanClass, advisors);
        }
        return advisors;
    }

    private List<Advisor> findCandidateAdvisors() {
        if (candidateAdvisors == null) {
            resolvingAdvisors = true;
            try {
                candidateAdvisors = beanFactory.getBeanNamesForType(Advisor.class).stream()
                        .map(advisorName -> beanFactory.getBean(advisorName, Advisor.class))
                        .toList();
            } finally {
                resolvingAdvisors = false;
            }
        }
        return candidateAdvisors;
    }
}

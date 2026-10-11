package t.simplespring.integration.shop;

import t.simplespring.beans.factory.config.BeanPostProcessor;
import t.simplespring.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class BeanTracker implements BeanPostProcessor {

    private final Set<String> beanNames = new LinkedHashSet<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        beanNames.add(beanName);
        return bean;
    }

    public Set<String> beanNames() {
        return Set.copyOf(beanNames);
    }
}

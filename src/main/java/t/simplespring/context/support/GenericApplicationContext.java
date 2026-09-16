package t.simplespring.context.support;

import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.support.DefaultListableBeanFactory;
import t.simplespring.context.ApplicationContext;

public class GenericApplicationContext implements ApplicationContext {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    public void registerBean(String beanName, Class<?> beanClass) {
        beanFactory.registerBeanDefinition(beanName, new BeanDefinition(beanClass));
    }

    public void refresh() {
        beanFactory.preInstantiateSingletons();
    }

    @Override
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return beanFactory.getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }
}

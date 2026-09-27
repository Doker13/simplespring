package t.simplespring.context.support;

import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.config.BeanPostProcessor;
import t.simplespring.beans.factory.support.BeanDefinitionRegistry;
import t.simplespring.beans.factory.support.DefaultListableBeanFactory;
import t.simplespring.context.ApplicationContext;

import java.util.List;

public class GenericApplicationContext implements ApplicationContext, BeanDefinitionRegistry, AutoCloseable {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    public void registerBean(String beanName, Class<?> beanClass) {
        registerBeanDefinition(beanName, new BeanDefinition(beanClass));
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public List<String> getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }

    public void refresh() {
        registerBeanPostProcessors();
        beanFactory.preInstantiateSingletons();
    }

    private void registerBeanPostProcessors() {
        for (String beanName : beanFactory.getBeanNamesForType(BeanPostProcessor.class)) {
            beanFactory.addBeanPostProcessor(beanFactory.getBean(beanName, BeanPostProcessor.class));
        }
    }

    @Override
    public void close() {
        beanFactory.destroySingletons();
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

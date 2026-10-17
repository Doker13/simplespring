package t.simplespring.beans.factory;

import t.simplespring.beans.factory.config.BeanDefinition;

import java.util.List;

public interface BeanDefinitionRegistry {

    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    BeanDefinition getBeanDefinition(String beanName);

    boolean containsBeanDefinition(String beanName);

    List<String> getBeanDefinitionNames();
}

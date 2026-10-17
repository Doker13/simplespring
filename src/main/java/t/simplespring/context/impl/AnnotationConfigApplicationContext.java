package t.simplespring.context.impl;

import t.simplespring.aop.impl.DefaultAdvisorAutoProxyCreator;
import t.simplespring.beans.factory.impl.InitDestroyAnnotationBeanPostProcessor;
import t.simplespring.context.annotation.Bean;
import t.simplespring.context.annotation.Configuration;


public class AnnotationConfigApplicationContext extends GenericApplicationContext {

    public AnnotationConfigApplicationContext(String... basePackages) {
        getBeanFactory().addBeanPostProcessor(new DefaultAdvisorAutoProxyCreator(getBeanFactory()));
        registerBean(InitDestroyAnnotationBeanPostProcessor.class.getName(), InitDestroyAnnotationBeanPostProcessor.class);
        new ClassPathBeanDefinitionScanner(this).scan(basePackages);
        new ConfigurationClassBeanDefinitionReader(this).loadBeanDefinitions();
        refresh();
    }
}

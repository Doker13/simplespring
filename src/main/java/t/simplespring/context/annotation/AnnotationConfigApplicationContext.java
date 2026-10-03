package t.simplespring.context.annotation;

import t.simplespring.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor;
import t.simplespring.context.support.GenericApplicationContext;

public class AnnotationConfigApplicationContext extends GenericApplicationContext {

    public AnnotationConfigApplicationContext(String... basePackages) {
        registerBean(InitDestroyAnnotationBeanPostProcessor.class.getName(), InitDestroyAnnotationBeanPostProcessor.class);
        new ClassPathBeanDefinitionScanner(this).scan(basePackages);
        new ConfigurationClassBeanDefinitionReader(this).loadBeanDefinitions();
        refresh();
    }
}

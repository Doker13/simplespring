package t.simplespring.context.annotation;

import t.simplespring.context.support.GenericApplicationContext;

public class AnnotationConfigApplicationContext extends GenericApplicationContext {

    public AnnotationConfigApplicationContext(String... basePackages) {
        new ClassPathBeanDefinitionScanner(this).scan(basePackages);
        refresh();
    }
}

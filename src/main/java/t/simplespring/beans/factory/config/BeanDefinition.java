package t.simplespring.beans.factory.config;

import java.util.Objects;

public record BeanDefinition(Class<?> beanClass, String scope) {

    public static final String SCOPE_SINGLETON = "singleton";
    public static final String SCOPE_PROTOTYPE = "prototype";

    public BeanDefinition {
        Objects.requireNonNull(beanClass, "beanClass");
        Objects.requireNonNull(scope, "scope");
    }

    public BeanDefinition(Class<?> beanClass) {
        this(beanClass, SCOPE_SINGLETON);
    }

    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope);
    }

    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(scope);
    }
}

package t.simplespring.beans.factory.config;

import java.lang.reflect.Method;
import java.util.Objects;

public record BeanDefinition(Class<?> beanClass, String scope, boolean primary,
                             String factoryBeanName, Method factoryMethod) {

    public static final String SCOPE_SINGLETON = "singleton";
    public static final String SCOPE_PROTOTYPE = "prototype";

    public BeanDefinition {
        Objects.requireNonNull(beanClass, "beanClass");
        Objects.requireNonNull(scope, "scope");
        if ((factoryBeanName == null) != (factoryMethod == null)) {
            throw new IllegalArgumentException("factoryBeanName and factoryMethod must be set together");
        }
    }

    public BeanDefinition(Class<?> beanClass, String scope, boolean primary) {
        this(beanClass, scope, primary, null, null);
    }

    public BeanDefinition(Class<?> beanClass, String scope) {
        this(beanClass, scope, false);
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

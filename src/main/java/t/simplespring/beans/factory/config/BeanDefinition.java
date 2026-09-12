package t.simplespring.beans.factory.config;

import java.util.Objects;

public record BeanDefinition(Class<?> beanClass) {

    public BeanDefinition {
        Objects.requireNonNull(beanClass, "beanClass");
    }
}

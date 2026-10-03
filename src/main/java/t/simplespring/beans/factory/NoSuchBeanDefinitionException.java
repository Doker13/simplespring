package t.simplespring.beans.factory;

import t.simplespring.beans.BeansException;

public class NoSuchBeanDefinitionException extends BeansException {

    public NoSuchBeanDefinitionException(String beanName) {
        super("No bean named '%s' is defined".formatted(beanName));
    }

    public NoSuchBeanDefinitionException(Class<?> type) {
        super("No bean of type '%s' is defined".formatted(type.getName()));
    }

    public NoSuchBeanDefinitionException(Class<?> type, String reason) {
        super("No qualifying bean of type '%s': %s".formatted(type.getName(), reason));
    }
}

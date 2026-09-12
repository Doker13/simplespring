package t.simplespring.beans.factory;

import t.simplespring.beans.BeansException;

public class BeanCreationException extends BeansException {

    public BeanCreationException(String beanName, String reason, Throwable cause) {
        super("Error creating bean with name '%s': %s".formatted(beanName, reason), cause);
    }
}

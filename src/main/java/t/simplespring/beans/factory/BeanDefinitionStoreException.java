package t.simplespring.beans.factory;

import t.simplespring.beans.BeansException;

public class BeanDefinitionStoreException extends BeansException {

    public BeanDefinitionStoreException(String beanName, String reason) {
        super("Invalid bean definition with name '%s': %s".formatted(beanName, reason));
    }
}

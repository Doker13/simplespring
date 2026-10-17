package t.simplespring.beans.exception;


public class BeanDefinitionStoreException extends BeansException {

    public BeanDefinitionStoreException(String beanName, String reason) {
        super("Invalid bean definition with name '%s': %s".formatted(beanName, reason));
    }
}

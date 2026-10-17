package t.simplespring.beans.exception;


public class BeanCreationException extends BeansException {

    public BeanCreationException(String beanName, String reason) {
        super(message(beanName, reason));
    }

    public BeanCreationException(String beanName, String reason, Throwable cause) {
        super(message(beanName, reason), cause);
    }

    private static String message(String beanName, String reason) {
        return "Error creating bean with name '%s': %s".formatted(beanName, reason);
    }
}

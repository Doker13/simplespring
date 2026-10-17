package t.simplespring.beans.exception;


public class BeanNotOfRequiredTypeException extends BeansException {

    public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
        super("Bean named '%s' is expected to be of type '%s' but was actually of type '%s'"
                .formatted(beanName, requiredType.getName(), actualType.getName()));
    }
}

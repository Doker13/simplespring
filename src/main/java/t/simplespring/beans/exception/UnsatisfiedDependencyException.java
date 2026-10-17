package t.simplespring.beans.exception;


public class UnsatisfiedDependencyException extends BeanCreationException {

    public UnsatisfiedDependencyException(String beanName, String injectionPoint, BeansException cause) {
        super(beanName, "unsatisfied dependency expressed through %s: %s"
                .formatted(injectionPoint, cause.getMessage()), cause);
    }
}

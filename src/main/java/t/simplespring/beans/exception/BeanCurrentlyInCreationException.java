package t.simplespring.beans.exception;

import java.util.Collection;

public class BeanCurrentlyInCreationException extends BeanCreationException {

    public BeanCurrentlyInCreationException(String beanName, Collection<String> creationPath) {
        super(beanName, "circular dependency %s -> %s"
                .formatted(String.join(" -> ", creationPath), beanName));
    }

    public BeanCurrentlyInCreationException(String beanName, String reason) {
        super(beanName, reason);
    }
}

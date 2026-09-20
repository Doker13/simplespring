package t.simplespring.beans.factory;

import java.util.Collection;

public class BeanCurrentlyInCreationException extends BeanCreationException {

    public BeanCurrentlyInCreationException(String beanName, Collection<String> creationPath) {
        super(beanName, "circular dependency %s -> %s"
                .formatted(String.join(" -> ", creationPath), beanName));
    }
}

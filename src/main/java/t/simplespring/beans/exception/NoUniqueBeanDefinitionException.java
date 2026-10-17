package t.simplespring.beans.exception;

import java.util.Collection;

public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

    public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNames) {
        super(type, "expected single matching bean but found %d: %s"
                .formatted(beanNames.size(), String.join(", ", beanNames)));
    }

    public NoUniqueBeanDefinitionException(Class<?> type, String reason) {
        super(type, reason);
    }
}

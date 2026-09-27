package t.simplespring.beans.factory.config;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

    void postProcessBeforeDestruction(Object bean, String beanName);
}

package t.simplespring.context.annotation;

import t.simplespring.beans.factory.BeanDefinitionStoreException;
import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ConfigurationClassBeanDefinitionReader {

    private final BeanDefinitionRegistry registry;

    public ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public int loadBeanDefinitions() {
        int registered = 0;
        for (String configurationBeanName : registry.getBeanDefinitionNames()) {
            Class<?> configurationClass = registry.getBeanDefinition(configurationBeanName).beanClass();
            if (configurationClass.isAnnotationPresent(Configuration.class)) {
                for (Method beanMethod : beanMethods(configurationClass)) {
                    registry.registerBeanDefinition(beanMethod.getName(),
                            beanDefinition(configurationBeanName, beanMethod));
                    registered++;
                }
            }
        }
        return registered;
    }

    private static List<Method> beanMethods(Class<?> configurationClass) {
        return Arrays.stream(configurationClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

    private static BeanDefinition beanDefinition(String configurationBeanName, Method beanMethod) {
        if (beanMethod.getReturnType() == void.class) {
            throw new BeanDefinitionStoreException(beanMethod.getName(),
                    "@Bean method of '%s' must return a bean but is declared void".formatted(configurationBeanName));
        }
        return new BeanDefinition(beanMethod.getReturnType(), BeanDefinition.SCOPE_SINGLETON, false,
                configurationBeanName, beanMethod);
    }
}

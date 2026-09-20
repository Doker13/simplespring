package t.simplespring.beans.factory.support;

import t.simplespring.beans.BeansException;
import t.simplespring.beans.factory.BeanCreationException;
import t.simplespring.beans.factory.BeanDefinitionStoreException;
import t.simplespring.beans.factory.BeanFactory;
import t.simplespring.beans.factory.NoSuchBeanDefinitionException;
import t.simplespring.beans.factory.NoUniqueBeanDefinitionException;
import t.simplespring.beans.factory.UnsatisfiedDependencyException;
import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.beans.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultListableBeanFactory implements BeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        if (!beanDefinition.isSingleton() && !beanDefinition.isPrototype()) {
            throw new BeanDefinitionStoreException(beanName,
                    "unknown scope '%s'".formatted(beanDefinition.scope()));
        }
        BeanDefinition existing = beanDefinitions.putIfAbsent(beanName, beanDefinition);
        if (existing != null) {
            throw new BeanDefinitionStoreException(beanName,
                    "name is already bound to " + existing.beanClass().getName());
        }
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition beanDefinition = beanDefinitions.get(beanName);
        if (beanDefinition == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return beanDefinition;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitions.containsKey(beanName);
    }

    @Override
    public List<String> getBeanDefinitionNames() {
        return List.copyOf(beanDefinitions.keySet());
    }

    @Override
    public Object getBean(String name) {
        Object singleton = singletons.get(name);
        if (singleton != null) {
            return singleton;
        }
        BeanDefinition beanDefinition = getBeanDefinition(name);
        Object bean = createBean(name, beanDefinition);
        if (beanDefinition.isSingleton()) {
            singletons.put(name, bean);
        }
        return bean;
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        Object bean = getBean(name);
        if (!requiredType.isInstance(bean)) {
            throw new BeansException("Bean named '%s' is expected to be of type '%s' but was actually of type '%s'"
                    .formatted(name, requiredType.getName(), bean.getClass().getName()));
        }
        return requiredType.cast(bean);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        List<String> candidates = getBeanNamesForType(requiredType);
        return switch (candidates.size()) {
            case 0 -> throw new NoSuchBeanDefinitionException(requiredType);
            case 1 -> requiredType.cast(getBean(candidates.getFirst()));
            default -> throw new NoUniqueBeanDefinitionException(requiredType, candidates);
        };
    }

    @Override
    public boolean containsBean(String name) {
        return containsBeanDefinition(name);
    }

    public List<String> getBeanNamesForType(Class<?> type) {
        return beanDefinitions.entrySet().stream()
                .filter(entry -> type.isAssignableFrom(entry.getValue().beanClass()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public void preInstantiateSingletons() {
        beanDefinitions.forEach((beanName, beanDefinition) -> {
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        });
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.beanClass();
        Constructor<?> constructor = resolveConstructor(beanName, beanClass);
        Object[] arguments = resolveArguments(beanName, constructor);
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(beanName,
                    "constructor of " + beanClass.getName() + " threw an exception", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException(beanName,
                    "instantiation of " + beanClass.getName() + " failed", e);
        }
    }

    private Constructor<?> resolveConstructor(String beanName, Class<?> beanClass) {
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        List<Constructor<?>> autowired = Arrays.stream(constructors)
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .toList();
        if (autowired.size() > 1) {
            throw new BeanCreationException(beanName,
                    "more than one @Autowired constructor found in " + beanClass.getName());
        }
        if (autowired.size() == 1) {
            return autowired.getFirst();
        }
        try {
            return beanClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new BeanCreationException(beanName,
                    "no @Autowired or default constructor found in " + beanClass.getName(), e);
        }
    }

    private Object[] resolveArguments(String beanName, Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arguments[i] = resolveDependency(beanName, parameterTypes[i], "constructor parameter " + i);
        }
        return arguments;
    }

    private Object resolveDependency(String beanName, Class<?> dependencyType, String injectionPoint) {
        try {
            return getBean(dependencyType);
        } catch (NoSuchBeanDefinitionException e) {
            throw new UnsatisfiedDependencyException(beanName, injectionPoint, e);
        }
    }
}

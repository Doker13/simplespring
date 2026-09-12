package t.simplespring.beans.factory.support;

import t.simplespring.beans.BeansException;
import t.simplespring.beans.factory.BeanCreationException;
import t.simplespring.beans.factory.BeanDefinitionStoreException;
import t.simplespring.beans.factory.BeanFactory;
import t.simplespring.beans.factory.NoSuchBeanDefinitionException;
import t.simplespring.beans.factory.NoUniqueBeanDefinitionException;
import t.simplespring.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultListableBeanFactory implements BeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
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
        if (singleton == null) {
            singleton = createBean(name, getBeanDefinition(name));
            singletons.put(name, singleton);
        }
        return singleton;
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
        getBeanDefinitionNames().forEach(this::getBean);
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.beanClass();
        try {
            return beanClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new BeanCreationException(beanName,
                    "no default constructor found in " + beanClass.getName(), e);
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException(beanName,
                    "instantiation of " + beanClass.getName() + " failed", e);
        }
    }
}

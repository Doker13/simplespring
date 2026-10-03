package t.simplespring.beans.factory.support;

import t.simplespring.beans.BeansException;
import t.simplespring.beans.factory.BeanCreationException;
import t.simplespring.beans.factory.BeanCurrentlyInCreationException;
import t.simplespring.beans.factory.BeanDefinitionStoreException;
import t.simplespring.beans.factory.BeanFactory;
import t.simplespring.beans.factory.NoSuchBeanDefinitionException;
import t.simplespring.beans.factory.NoUniqueBeanDefinitionException;
import t.simplespring.beans.factory.UnsatisfiedDependencyException;
import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.config.BeanPostProcessor;
import t.simplespring.beans.factory.config.DependencyDescriptor;
import t.simplespring.beans.factory.config.DestructionAwareBeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.BiFunction;

public class DefaultListableBeanFactory implements BeanFactory, BeanDefinitionRegistry {

    private static final System.Logger logger = System.getLogger(DefaultListableBeanFactory.class.getName());

    private final Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();
    private final SequencedMap<String, Object> disposableBeans = new LinkedHashMap<>();
    private final Set<String> beansInCreation = new LinkedHashSet<>();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

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
        if (!beansInCreation.add(name)) {
            throw new BeanCurrentlyInCreationException(name, beansInCreation);
        }
        try {
            Object bean = createBeanInstance(name, beanDefinition);
            if (beanDefinition.isSingleton()) {
                singletons.put(name, bean);
            }
            populateBean(name, bean);
            Object exposedBean = initializeBean(name, bean);
            if (beanDefinition.isSingleton()) {
                singletons.put(name, exposedBean);
                disposableBeans.put(name, bean);
            }
            return exposedBean;
        } catch (RuntimeException e) {
            singletons.remove(name);
            throw e;
        } finally {
            beansInCreation.remove(name);
        }
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
        return requiredType.cast(getBean(determineAutowireCandidate(requiredType, null)));
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

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.remove(beanPostProcessor);
        beanPostProcessors.add(beanPostProcessor);
    }

    public void preInstantiateSingletons() {
        beanDefinitions.forEach((beanName, beanDefinition) -> {
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        });
    }

    public void destroySingletons() {
        disposableBeans.reversed().forEach(this::destroyBean);
        disposableBeans.clear();
        singletons.clear();
    }

    private void destroyBean(String beanName, Object bean) {
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (processor instanceof DestructionAwareBeanPostProcessor destructionAware) {
                try {
                    destructionAware.postProcessBeforeDestruction(bean, beanName);
                } catch (RuntimeException e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Destruction of bean with name '%s' threw an exception".formatted(beanName), e);
                }
            }
        }
    }

    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition) {
        return beanDefinition.factoryMethod() != null
                ? instantiateUsingFactoryMethod(beanName, beanDefinition)
                : instantiateBean(beanName, beanDefinition.beanClass());
    }

    private Object instantiateUsingFactoryMethod(String beanName, BeanDefinition beanDefinition) {
        Method factoryMethod = beanDefinition.factoryMethod();
        Object factoryBean = getBean(beanDefinition.factoryBeanName());
        Object[] arguments = resolveArguments(beanName, factoryMethod);
        Object bean;
        try {
            factoryMethod.setAccessible(true);
            bean = factoryMethod.invoke(factoryBean, arguments);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(beanName,
                    "factory method '%s' of bean '%s' threw an exception"
                            .formatted(factoryMethod.getName(), beanDefinition.factoryBeanName()), e.getCause());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new BeanCreationException(beanName,
                    "invocation of factory method '%s' of bean '%s' failed"
                            .formatted(factoryMethod.getName(), beanDefinition.factoryBeanName()), e);
        }
        if (bean == null) {
            throw new BeanCreationException(beanName,
                    "factory method '%s' of bean '%s' returned null"
                            .formatted(factoryMethod.getName(), beanDefinition.factoryBeanName()));
        }
        return bean;
    }

    private Object instantiateBean(String beanName, Class<?> beanClass) {
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

    private void populateBean(String beanName, Object bean) {
        for (Class<?> type = bean.getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    injectField(beanName, bean, field);
                }
            }
        }
    }

    private Object initializeBean(String beanName, Object bean) {
        try {
            Object processed = applyBeanPostProcessors(bean,
                    (processor, current) -> processor.postProcessBeforeInitialization(current, beanName));
            return applyBeanPostProcessors(processed,
                    (processor, current) -> processor.postProcessAfterInitialization(current, beanName));
        } catch (BeansException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BeanCreationException(beanName, "initialization of bean failed", e);
        }
    }

    private Object applyBeanPostProcessors(Object bean, BiFunction<BeanPostProcessor, Object, Object> hook) {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            Object current = hook.apply(processor, result);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    private void injectField(String beanName, Object bean, Field field) {
        Object dependency = resolveDependency(beanName, DependencyDescriptor.forField(field));
        try {
            field.setAccessible(true);
            field.set(bean, dependency);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new BeanCreationException(beanName,
                    "injection into field '" + field.getName() + "' failed", e);
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

    private Object[] resolveArguments(String beanName, Executable executable) {
        Parameter[] parameters = executable.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            arguments[i] = resolveDependency(beanName, DependencyDescriptor.forParameter(parameters[i], i));
        }
        return arguments;
    }

    private Object resolveDependency(String beanName, DependencyDescriptor descriptor) {
        String candidate;
        try {
            candidate = determineAutowireCandidate(descriptor.dependencyType(), descriptor.qualifier());
        } catch (NoSuchBeanDefinitionException e) {
            throw new UnsatisfiedDependencyException(beanName, descriptor.description(), e);
        }
        return getBean(candidate);
    }

    private String determineAutowireCandidate(Class<?> type, String qualifier) {
        List<String> candidates = getBeanNamesForType(type);
        if (qualifier != null) {
            if (!candidates.contains(qualifier)) {
                throw new NoSuchBeanDefinitionException(type,
                        "no bean named '%s' among candidates: %s".formatted(qualifier, candidates));
            }
            return qualifier;
        }
        return switch (candidates.size()) {
            case 0 -> throw new NoSuchBeanDefinitionException(type);
            case 1 -> candidates.getFirst();
            default -> determinePrimaryCandidate(type, candidates);
        };
    }

    private String determinePrimaryCandidate(Class<?> type, List<String> candidates) {
        List<String> primaryCandidates = candidates.stream()
                .filter(candidate -> beanDefinitions.get(candidate).primary())
                .toList();
        return switch (primaryCandidates.size()) {
            case 0 -> throw new NoUniqueBeanDefinitionException(type, candidates);
            case 1 -> primaryCandidates.getFirst();
            default -> throw new NoUniqueBeanDefinitionException(type,
                    "more than one 'primary' bean found among candidates: " + primaryCandidates);
        };
    }
}

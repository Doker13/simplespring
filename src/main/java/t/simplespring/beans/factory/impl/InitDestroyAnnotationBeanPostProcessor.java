package t.simplespring.beans.factory.impl;

import t.simplespring.beans.exception.BeanCreationException;
import t.simplespring.beans.exception.BeansException;
import t.simplespring.beans.factory.annotation.PostConstruct;
import t.simplespring.beans.factory.annotation.PreDestroy;
import t.simplespring.beans.factory.config.DestructionAwareBeanPostProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class InitDestroyAnnotationBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    private final Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        invoke(findLifecycleMetadata(bean, beanName).initMethods(), bean, (method, cause) ->
                new BeanCreationException(beanName,
                        "invocation of init method '%s' failed".formatted(method.getName()), cause));
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        invoke(findLifecycleMetadata(bean, beanName).destroyMethods(), bean, (method, cause) ->
                new BeansException("Invocation of destroy method '%s' on bean with name '%s' failed"
                        .formatted(method.getName(), beanName), cause));
    }

    private LifecycleMetadata findLifecycleMetadata(Object bean, String beanName) {
        return lifecycleMetadataCache.computeIfAbsent(bean.getClass(),
                beanClass -> buildLifecycleMetadata(beanClass, beanName));
    }

    private static LifecycleMetadata buildLifecycleMetadata(Class<?> beanClass, String beanName) {
        List<Method> initMethods = new ArrayList<>();
        List<Method> destroyMethods = new ArrayList<>();
        Set<String> overridable = new HashSet<>();
        for (Class<?> type = beanClass; type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                boolean init = method.isAnnotationPresent(PostConstruct.class);
                boolean destroy = method.isAnnotationPresent(PreDestroy.class);
                if (!init && !destroy) {
                    continue;
                }
                if (method.getParameterCount() != 0) {
                    throw new BeanCreationException(beanName,
                            "lifecycle method '%s' must have no parameters".formatted(method.getName()));
                }
                if (!Modifier.isPrivate(method.getModifiers()) && !overridable.add(method.getName())) {
                    continue;
                }
                method.setAccessible(true);
                if (init) {
                    initMethods.addFirst(method);
                }
                if (destroy) {
                    destroyMethods.add(method);
                }
            }
        }
        return new LifecycleMetadata(initMethods, destroyMethods);
    }

    private static void invoke(List<Method> methods, Object bean,
                               BiFunction<Method, Throwable, BeansException> failure) {
        for (Method method : methods) {
            try {
                method.invoke(bean);
            } catch (InvocationTargetException e) {
                throw failure.apply(method, e.getCause());
            } catch (IllegalAccessException e) {
                throw failure.apply(method, e);
            }
        }
    }

    private record LifecycleMetadata(List<Method> initMethods, List<Method> destroyMethods) {
    }
}

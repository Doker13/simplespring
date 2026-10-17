package t.simplespring.context.impl;

import t.simplespring.context.event.ApplicationEvent;
import t.simplespring.context.event.ApplicationListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SimpleApplicationEventMulticaster {

    private final List<ListenerRegistration> registrations = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public void addApplicationListener(ApplicationListener<?> listener, Class<?> listenerClass) {
        Class<?> eventType = resolveEventType(listenerClass);
        registrations.add(new ListenerRegistration((ApplicationListener<ApplicationEvent>) listener,
                eventType == null ? ApplicationEvent.class : eventType));
    }

    public void multicastEvent(ApplicationEvent event) {
        for (ListenerRegistration registration : registrations) {
            if (registration.eventType().isInstance(event)) {
                registration.listener().onApplicationEvent(event);
            }
        }
    }

    private static Class<?> resolveEventType(Type type) {
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType() == ApplicationListener.class) {
            return parameterized.getActualTypeArguments()[0] instanceof Class<?> eventType ? eventType : null;
        }
        Class<?> rawType = type instanceof ParameterizedType parameterized
                ? (Class<?>) parameterized.getRawType()
                : (Class<?>) type;
        if (rawType == null) {
            return null;
        }
        for (Type genericInterface : rawType.getGenericInterfaces()) {
            Class<?> eventType = resolveEventType(genericInterface);
            if (eventType != null) {
                return eventType;
            }
        }
        return resolveEventType(rawType.getGenericSuperclass());
    }

    private record ListenerRegistration(ApplicationListener<ApplicationEvent> listener, Class<?> eventType) {
    }
}

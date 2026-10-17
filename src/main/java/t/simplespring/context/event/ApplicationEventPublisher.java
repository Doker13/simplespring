package t.simplespring.context.event;

@FunctionalInterface
public interface ApplicationEventPublisher {

    void publishEvent(ApplicationEvent event);
}

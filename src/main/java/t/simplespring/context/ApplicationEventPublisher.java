package t.simplespring.context;

@FunctionalInterface
public interface ApplicationEventPublisher {

    void publishEvent(ApplicationEvent event);
}

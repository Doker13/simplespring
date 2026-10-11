package t.simplespring.context;

@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> {

    void onApplicationEvent(E event);
}

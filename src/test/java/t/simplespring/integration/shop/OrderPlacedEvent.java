package t.simplespring.integration.shop;

import t.simplespring.context.ApplicationEvent;

public class OrderPlacedEvent extends ApplicationEvent {

    private final Order order;

    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order order() {
        return order;
    }
}

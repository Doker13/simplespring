package t.simplespring.integration.shop.event;

import t.simplespring.context.event.ApplicationEvent;
import t.simplespring.integration.shop.order.Order;


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

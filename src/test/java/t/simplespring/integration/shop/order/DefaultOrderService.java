package t.simplespring.integration.shop.order;

import t.simplespring.context.event.ApplicationEventPublisher;
import t.simplespring.integration.shop.event.OrderPlacedEvent;
import t.simplespring.integration.shop.logging.Logged;
import t.simplespring.integration.shop.notification.Notifier;
import t.simplespring.stereotype.Component;


@Component
public class DefaultOrderService implements OrderService {

    private final OrderRepository repository;
    private final Notifier notifier;
    private final ApplicationEventPublisher publisher;

    public DefaultOrderService(OrderRepository repository, Notifier notifier, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.notifier = notifier;
        this.publisher = publisher;
    }

    @Logged
    @Override
    public Order placeOrder(String item) {
        Order order = repository.save(item);
        notifier.send("order %d placed".formatted(order.id()));
        publisher.publishEvent(new OrderPlacedEvent(this, order));
        return order;
    }
}

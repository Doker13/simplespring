package t.simplespring.integration.shop;

import t.simplespring.context.ApplicationEventPublisher;
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

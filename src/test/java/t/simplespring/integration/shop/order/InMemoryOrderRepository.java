package t.simplespring.integration.shop.order;

import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.beans.factory.annotation.PostConstruct;
import t.simplespring.beans.factory.annotation.PreDestroy;
import t.simplespring.integration.shop.support.Journal;
import t.simplespring.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> orders = new HashMap<>();

    @Autowired
    private Journal journal;

    @PostConstruct
    void open() {
        journal.record("repository opened");
    }

    @PreDestroy
    void close() {
        journal.record("repository closed");
    }

    @Override
    public Order save(String item) {
        Order order = new Order(orders.size() + 1, item);
        orders.put(order.id(), order);
        return order;
    }

    @Override
    public Order find(long id) {
        return orders.get(id);
    }
}

package t.simplespring.sample;

import t.simplespring.stereotype.Component;

@Component
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderRepository repository() {
        return repository;
    }

    public String describe(long id) {
        return "found " + repository.findOrder(id);
    }
}

package t.simplespring.sample;

import t.simplespring.stereotype.Component;

@Component
public class JdbcOrderRepository implements OrderRepository {

    @Override
    public String findOrder(long id) {
        return "order-" + id;
    }
}

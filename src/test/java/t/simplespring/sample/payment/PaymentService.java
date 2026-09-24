package t.simplespring.sample.payment;

import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.sample.OrderService;
import t.simplespring.sample.ShoppingCart;
import t.simplespring.stereotype.Component;

@Component
public class PaymentService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCart cart;

    public OrderService orderService() {
        return orderService;
    }

    public ShoppingCart cart() {
        return cart;
    }
}

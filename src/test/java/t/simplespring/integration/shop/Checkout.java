package t.simplespring.integration.shop;

import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.stereotype.Component;

@Component
public class Checkout {

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

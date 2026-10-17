package t.simplespring.integration.shop.order;

public interface OrderRepository {

    Order save(String item);

    Order find(long id);
}

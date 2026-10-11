package t.simplespring.integration.shop;

public interface OrderRepository {

    Order save(String item);

    Order find(long id);
}

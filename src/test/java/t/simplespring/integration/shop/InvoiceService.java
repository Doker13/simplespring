package t.simplespring.integration.shop;

public class InvoiceService {

    private final OrderRepository repository;
    private final Notifier notifier;

    public InvoiceService(OrderRepository repository, Notifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    public OrderRepository repository() {
        return repository;
    }

    public void sendInvoice(long orderId) {
        notifier.send("invoice for " + repository.find(orderId).item());
    }
}

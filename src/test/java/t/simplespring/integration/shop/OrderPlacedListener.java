package t.simplespring.integration.shop;

import t.simplespring.context.ApplicationListener;
import t.simplespring.stereotype.Component;

@Component
public class OrderPlacedListener implements ApplicationListener<OrderPlacedEvent> {

    private final Journal journal;

    public OrderPlacedListener(Journal journal) {
        this.journal = journal;
    }

    @Override
    public void onApplicationEvent(OrderPlacedEvent event) {
        journal.record("order placed: " + event.order().item());
    }
}

package t.simplespring.integration.shop.event;

import t.simplespring.context.event.ApplicationListener;
import t.simplespring.integration.shop.support.Journal;
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

package t.simplespring.context.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import t.simplespring.beans.factory.BeanCreationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericApplicationContextTest {

    private final GenericApplicationContext context = new GenericApplicationContext();

    @BeforeEach
    void resetCounter() {
        OrderService.instances = 0;
    }

    @Test
    void refreshCreatesSingletonsEagerly() {
        context.registerBean("orderService", OrderService.class);
        assertEquals(0, OrderService.instances);

        context.refresh();

        assertEquals(1, OrderService.instances);
    }

    @Test
    void returnsBeansCreatedOnRefresh() {
        context.registerBean("orderService", OrderService.class);
        context.refresh();

        OrderService byType = context.getBean(OrderService.class);

        assertSame(byType, context.getBean("orderService"));
        assertSame(byType, context.getBean("orderService", OrderService.class));
        assertTrue(context.containsBean("orderService"));
        assertEquals(1, OrderService.instances);
    }

    @Test
    void refreshReportsBrokenBean() {
        context.registerBean("orderService", OrderService.class);
        context.registerBean("mailSender", MailSender.class);

        BeanCreationException e = assertThrows(BeanCreationException.class, context::refresh);

        assertTrue(e.getMessage().contains("mailSender"));
    }

    public static class OrderService {
        static int instances;

        public OrderService() {
            instances++;
        }
    }

    public static class MailSender {
        public MailSender(String host) {
        }
    }
}

package t.simplespring.context.annotation;

import org.junit.jupiter.api.Test;
import t.simplespring.beans.factory.NoSuchBeanDefinitionException;
import t.simplespring.beans.factory.UnsatisfiedDependencyException;
import t.simplespring.sample.JdbcOrderRepository;
import t.simplespring.sample.OrderRepository;
import t.simplespring.sample.OrderService;
import t.simplespring.sample.PriceFormatter;
import t.simplespring.sample.ShoppingCart;
import t.simplespring.sample.SmtpNotifier;
import t.simplespring.sample.payment.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationConfigApplicationContextTest {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext("t.simplespring.sample");

    @Test
    void wiresScannedComponentsThroughConstructor() {
        OrderService orderService = context.getBean(OrderService.class);

        assertInstanceOf(JdbcOrderRepository.class, orderService.repository());
        assertSame(context.getBean(OrderRepository.class), orderService.repository());
        assertEquals("found order-7", orderService.describe(7));
    }

    @Test
    void wiresScannedComponentsThroughFields() {
        PaymentService paymentService = context.getBean(PaymentService.class);

        assertSame(context.getBean(OrderService.class), paymentService.orderService());
    }

    @Test
    void sharesSingletons() {
        assertSame(context.getBean("orderService"), context.getBean(OrderService.class));
    }

    @Test
    void createsNewPrototypeForEveryRequestAndInjection() {
        ShoppingCart requested = context.getBean(ShoppingCart.class);

        assertNotSame(requested, context.getBean(ShoppingCart.class));
        assertNotSame(requested, context.getBean(PaymentService.class).cart());
    }

    @Test
    void exposesComponentUnderExplicitName() {
        assertInstanceOf(SmtpNotifier.class, context.getBean("mailer"));
        assertTrue(context.containsBean("mailer"));
    }

    @Test
    void ignoresClassesWithoutComponentAnnotation() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(PriceFormatter.class));
    }

    @Test
    void failsOnStartupWhenDependencyIsOutsideScannedPackages() {
        UnsatisfiedDependencyException e = assertThrows(UnsatisfiedDependencyException.class,
                () -> new AnnotationConfigApplicationContext("t.simplespring.sample.payment"));

        assertTrue(e.getMessage().contains("paymentService"));
        assertTrue(e.getMessage().contains("field 'orderService'"));
    }
}

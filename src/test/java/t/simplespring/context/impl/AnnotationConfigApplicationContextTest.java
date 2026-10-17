package t.simplespring.context.impl;

import t.simplespring.beans.exception.NoSuchBeanDefinitionException;
import t.simplespring.beans.exception.UnsatisfiedDependencyException;
import t.simplespring.fixture.scan.JdbcOrderRepository;
import t.simplespring.fixture.scan.OrderRepository;
import t.simplespring.fixture.scan.OrderService;
import t.simplespring.fixture.scan.PriceFormatter;
import t.simplespring.fixture.scan.ShoppingCart;
import t.simplespring.fixture.scan.SmtpNotifier;
import t.simplespring.fixture.scan.payment.PaymentService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationConfigApplicationContextTest {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext("t.simplespring.fixture.scan");

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
                () -> new AnnotationConfigApplicationContext("t.simplespring.fixture.scan.payment"));

        assertTrue(e.getMessage().contains("paymentService"));
        assertTrue(e.getMessage().contains("field 'orderService'"));
    }
}

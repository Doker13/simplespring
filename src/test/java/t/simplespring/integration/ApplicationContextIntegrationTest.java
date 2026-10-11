package t.simplespring.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import t.simplespring.beans.factory.BeanNotOfRequiredTypeException;
import t.simplespring.beans.factory.config.BeanPostProcessor;
import t.simplespring.context.annotation.AnnotationConfigApplicationContext;
import t.simplespring.integration.shop.BeanTracker;
import t.simplespring.integration.shop.Checkout;
import t.simplespring.integration.shop.DefaultOrderService;
import t.simplespring.integration.shop.EmailNotifier;
import t.simplespring.integration.shop.InvoiceService;
import t.simplespring.integration.shop.Journal;
import t.simplespring.integration.shop.Order;
import t.simplespring.integration.shop.OrderRepository;
import t.simplespring.integration.shop.OrderService;
import t.simplespring.integration.shop.ShoppingCart;
import t.simplespring.integration.shop.SmsNotifier;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationContextIntegrationTest {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext("t.simplespring.integration.shop");

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void scansComponentsAndCreatesSingletonsOnStartup() {
        assertTrue(context.getBeanDefinitionNames().containsAll(List.of(
                "journal", "inMemoryOrderRepository", "emailNotifier", "smsNotifier", "defaultOrderService",
                "orderPlacedListener", "loggingInterceptor", "shopConfiguration", "shoppingCart", "checkout",
                "beanTracker")));
        assertEquals(List.of("repository opened"), context.getBean(Journal.class).entries());
        assertSame(context.getBean("journal"), context.getBean(Journal.class));
    }

    @Test
    void injectsThroughConstructorsAndFieldsAndCreatesNewPrototypes() {
        Checkout checkout = context.getBean(Checkout.class);
        ShoppingCart requested = context.getBean(ShoppingCart.class);

        assertSame(context.getBean(OrderService.class), checkout.orderService());
        assertNotNull(checkout.cart());
        assertNotSame(requested, checkout.cart());
        assertNotSame(requested, context.getBean(ShoppingCart.class));
        assertEquals(new Order(1, "book"), checkout.orderService().placeOrder("book"));
    }

    @Test
    void appliesCustomBeanPostProcessorToEveryBean() {
        Set<String> expected = context.getBeanDefinitionNames().stream()
                .filter(beanName -> !(context.getBean(beanName) instanceof BeanPostProcessor))
                .collect(Collectors.toSet());

        assertEquals(expected, context.getBean(BeanTracker.class).beanNames());
    }

    @Test
    void runsLifecycleCallbacksAfterInjectionAndOnClose() {
        Journal journal = context.getBean(Journal.class);

        assertEquals(List.of("repository opened"), journal.entries());

        context.close();

        assertEquals(List.of("repository opened", "repository closed"), journal.entries());
    }

    @Test
    void choosesNotifierByPrimaryAndByQualifier() {
        Order order = context.getBean(OrderService.class).placeOrder("book");
        context.getBean(InvoiceService.class).sendInvoice(order.id());

        assertEquals(List.of("order 1 placed"), context.getBean(EmailNotifier.class).sent());
        assertEquals(List.of("invoice for book"), context.getBean(SmsNotifier.class).sent());
    }

    @Test
    void createsBeanFromFactoryMethodWithItsDependencies() {
        InvoiceService invoiceService = assertInstanceOf(InvoiceService.class, context.getBean("invoiceService"));

        assertSame(context.getBean(OrderRepository.class), invoiceService.repository());
    }

    @Test
    void exposesAdvisedServiceAsProxy() {
        OrderService orderService = context.getBean(OrderService.class);

        assertTrue(Proxy.isProxyClass(orderService.getClass()));
        assertSame(orderService, context.getBean("defaultOrderService"));
        assertThrows(BeanNotOfRequiredTypeException.class, () -> context.getBean(DefaultOrderService.class));
    }

    @Test
    void interceptsCallAndDeliversEventToListener() {
        context.getBean(OrderService.class).placeOrder("book");

        assertEquals(List.of("repository opened", "before placeOrder", "order placed: book", "after placeOrder"),
                context.getBean(Journal.class).entries());
    }
}

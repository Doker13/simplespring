package t.simplespring.beans.factory.impl;

import t.simplespring.beans.exception.BeanCreationException;
import t.simplespring.beans.exception.BeanCurrentlyInCreationException;
import t.simplespring.beans.exception.NoSuchBeanDefinitionException;
import t.simplespring.beans.exception.NoUniqueBeanDefinitionException;
import t.simplespring.beans.exception.UnsatisfiedDependencyException;
import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.beans.factory.config.BeanDefinition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyInjectionTest {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @Test
    void injectsDependenciesThroughSingleConstructor() {
        register("orderService", OrderService.class);
        register("repository", JdbcOrderRepository.class);

        OrderService orderService = beanFactory.getBean(OrderService.class);

        assertSame(beanFactory.getBean("repository"), orderService.repository);
    }

    @Test
    void prefersAutowiredConstructor() {
        register("notifier", Notifier.class);
        register("repository", JdbcOrderRepository.class);

        Notifier notifier = beanFactory.getBean(Notifier.class);

        assertNotNull(notifier.repository);
    }

    @Test
    void usesDefaultConstructorWhenNoneIsAutowired() {
        register("reportService", ReportService.class);
        register("repository", JdbcOrderRepository.class);

        assertNull(beanFactory.getBean(ReportService.class).repository);
    }

    @Test
    void injectsAutowiredFieldsIncludingInherited() {
        register("auditedService", AuditedService.class);
        register("repository", JdbcOrderRepository.class);
        register("clock", Clock.class);

        AuditedService service = beanFactory.getBean(AuditedService.class);

        assertSame(beanFactory.getBean("repository"), service.repository());
        assertSame(beanFactory.getBean("clock"), service.clock);
        assertNull(service.notInjected);
    }

    @Test
    void injectsFreshPrototypeIntoEveryDependent() {
        register("orderService", OrderService.class);
        register("notifier", Notifier.class);
        beanFactory.registerBeanDefinition("repository",
                new BeanDefinition(JdbcOrderRepository.class, BeanDefinition.SCOPE_PROTOTYPE));

        assertNotSame(beanFactory.getBean(OrderService.class).repository,
                beanFactory.getBean(Notifier.class).repository);
    }

    @Test
    void failsOnMissingDependency() {
        register("orderService", OrderService.class);

        UnsatisfiedDependencyException e = assertThrows(UnsatisfiedDependencyException.class,
                () -> beanFactory.getBean("orderService"));

        assertTrue(e.getMessage().contains("orderService"));
        assertTrue(e.getMessage().contains("constructor parameter 0"));
        assertTrue(e.getMessage().contains(OrderRepository.class.getName()));
        assertInstanceOf(NoSuchBeanDefinitionException.class, e.getCause());
    }

    @Test
    void failsOnMissingFieldDependency() {
        register("auditedService", AuditedService.class);
        register("repository", JdbcOrderRepository.class);

        UnsatisfiedDependencyException e = assertThrows(UnsatisfiedDependencyException.class,
                () -> beanFactory.getBean("auditedService"));

        assertTrue(e.getMessage().contains("field 'clock'"));
    }

    @Test
    void failsOnAmbiguousDependency() {
        register("orderService", OrderService.class);
        register("jdbc", JdbcOrderRepository.class);
        register("memory", InMemoryOrderRepository.class);

        UnsatisfiedDependencyException e = assertThrows(UnsatisfiedDependencyException.class,
                () -> beanFactory.getBean("orderService"));

        assertInstanceOf(NoUniqueBeanDefinitionException.class, e.getCause());
        assertTrue(e.getMessage().contains("jdbc, memory"));
    }

    @Test
    void failsOnSeveralAutowiredConstructors() {
        register("ambiguous", AmbiguousConstructors.class);

        BeanCreationException e = assertThrows(BeanCreationException.class,
                () -> beanFactory.getBean("ambiguous"));

        assertTrue(e.getMessage().contains("more than one @Autowired constructor"));
    }

    @Test
    void failsOnConstructorCycle() {
        register("chicken", Chicken.class);
        register("egg", Egg.class);

        BeanCurrentlyInCreationException e = assertThrows(BeanCurrentlyInCreationException.class,
                () -> beanFactory.getBean("chicken"));

        assertTrue(e.getMessage().contains("chicken -> egg -> chicken"));
    }

    @Test
    void recoversAfterFailedCreation() {
        register("chicken", Chicken.class);
        register("egg", Egg.class);
        assertThrows(BeanCurrentlyInCreationException.class, () -> beanFactory.getBean("chicken"));

        BeanCurrentlyInCreationException e = assertThrows(BeanCurrentlyInCreationException.class,
                () -> beanFactory.getBean("egg"));

        assertTrue(e.getMessage().contains("egg -> chicken -> egg"));
    }

    @Test
    void resolvesFieldCycleBetweenSingletons() {
        register("husband", Husband.class);
        register("wife", Wife.class);

        Husband husband = beanFactory.getBean(Husband.class);

        assertSame(husband, husband.wife.husband);
    }

    @Test
    void failsOnFieldCycleBetweenPrototypes() {
        beanFactory.registerBeanDefinition("husband",
                new BeanDefinition(Husband.class, BeanDefinition.SCOPE_PROTOTYPE));
        beanFactory.registerBeanDefinition("wife",
                new BeanDefinition(Wife.class, BeanDefinition.SCOPE_PROTOTYPE));

        assertThrows(BeanCurrentlyInCreationException.class, () -> beanFactory.getBean("husband"));
    }

    @Test
    void doesNotKeepSingletonWhoseFieldInjectionFailed() {
        register("auditedService", AuditedService.class);
        register("repository", JdbcOrderRepository.class);
        assertThrows(UnsatisfiedDependencyException.class, () -> beanFactory.getBean("auditedService"));

        register("clock", Clock.class);

        assertNotNull(beanFactory.getBean(AuditedService.class).clock);
    }

    private void register(String beanName, Class<?> beanClass) {
        beanFactory.registerBeanDefinition(beanName, new BeanDefinition(beanClass));
    }

    interface OrderRepository {
    }

    static class JdbcOrderRepository implements OrderRepository {
    }

    static class InMemoryOrderRepository implements OrderRepository {
    }

    static class Clock {
    }

    static class OrderService {
        final OrderRepository repository;

        OrderService(OrderRepository repository) {
            this.repository = repository;
        }
    }

    static class Notifier {
        final OrderRepository repository;

        Notifier() {
            this.repository = null;
        }

        @Autowired
        Notifier(OrderRepository repository) {
            this.repository = repository;
        }
    }

    static class ReportService {
        final OrderRepository repository;

        ReportService() {
            this.repository = null;
        }

        ReportService(OrderRepository repository) {
            this.repository = repository;
        }
    }

    static class BaseService {
        @Autowired
        private OrderRepository repository;

        OrderRepository repository() {
            return repository;
        }
    }

    static class AuditedService extends BaseService {
        @Autowired
        Clock clock;

        Clock notInjected;
    }

    static class AmbiguousConstructors {
        @Autowired
        AmbiguousConstructors() {
        }

        @Autowired
        AmbiguousConstructors(Clock clock) {
        }
    }

    static class Chicken {
        Chicken(Egg egg) {
        }
    }

    static class Egg {
        Egg(Chicken chicken) {
        }
    }

    static class Husband {
        @Autowired
        Wife wife;
    }

    static class Wife {
        @Autowired
        Husband husband;
    }
}

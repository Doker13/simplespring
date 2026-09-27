package t.simplespring.beans.factory.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import t.simplespring.beans.factory.BeanCreationException;
import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.support.DefaultListableBeanFactory;
import t.simplespring.context.annotation.AnnotationConfigApplicationContext;
import t.simplespring.context.support.GenericApplicationContext;
import t.simplespring.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanLifecycleTest {

    private static final List<String> log = new ArrayList<>();

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @BeforeEach
    void setUp() {
        log.clear();
        beanFactory.addBeanPostProcessor(new InitDestroyAnnotationBeanPostProcessor());
    }

    @Test
    void callsPostConstructAfterFieldInjection() {
        register("orderService", OrderService.class);
        register("repository", OrderRepository.class);

        beanFactory.getBean("orderService");

        assertEquals(List.of("repository.open", "orderService.start:true"), log);
    }

    @Test
    void callsPreDestroyOnClose() {
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.registerBean("lifecycleProcessor", InitDestroyAnnotationBeanPostProcessor.class);
            context.registerBean("repository", OrderRepository.class);
            context.refresh();

            assertEquals(List.of("repository.open"), log);
        }

        assertEquals(List.of("repository.open", "repository.close"), log);
    }

    @Test
    void destroysSingletonsInReverseCreationOrder() {
        register("orderService", OrderService.class);
        register("repository", OrderRepository.class);
        beanFactory.preInstantiateSingletons();
        log.clear();

        beanFactory.destroySingletons();

        assertEquals(List.of("orderService.stop", "repository.close"), log);
    }

    @Test
    void destroysSingletonsOnlyOnce() {
        register("repository", OrderRepository.class);
        beanFactory.getBean("repository");
        beanFactory.destroySingletons();
        log.clear();

        beanFactory.destroySingletons();

        assertTrue(log.isEmpty());
    }

    @Test
    void doesNotCallPreDestroyForPrototype() {
        beanFactory.registerBeanDefinition("repository",
                new BeanDefinition(OrderRepository.class, BeanDefinition.SCOPE_PROTOTYPE));
        beanFactory.getBean("repository");

        beanFactory.destroySingletons();

        assertEquals(List.of("repository.open"), log);
    }

    @Test
    void reportsFailingPostConstruct() {
        register("brokenService", BrokenService.class);

        BeanCreationException e = assertThrows(BeanCreationException.class,
                () -> beanFactory.getBean("brokenService"));

        assertTrue(e.getMessage().contains("brokenService"));
        assertTrue(e.getMessage().contains("init method 'start'"));
        assertInstanceOf(IllegalStateException.class, e.getCause());
    }

    @Test
    void rejectsLifecycleMethodWithParameters() {
        register("misconfigured", MisconfiguredService.class);

        BeanCreationException e = assertThrows(BeanCreationException.class,
                () -> beanFactory.getBean("misconfigured"));

        assertTrue(e.getMessage().contains("'start' must have no parameters"));
    }

    @Test
    void callsInheritedLifecycleMethodsOnce() {
        register("worker", Worker.class);

        beanFactory.getBean("worker");

        assertEquals(3, log.size());
        assertTrue(log.indexOf("base.init") < log.indexOf("worker.init"));
        assertEquals(1, Collections.frequency(log, "worker.configure"));
        assertFalse(log.contains("base.configure"));

        log.clear();
        beanFactory.destroySingletons();

        assertEquals(List.of("worker.release", "base.release"), log);
    }

    @Test
    void keepsDestroyingAfterFailingPreDestroy() {
        register("repository", OrderRepository.class);
        register("leakyCache", LeakyCache.class);
        beanFactory.preInstantiateSingletons();

        beanFactory.destroySingletons();

        assertEquals(List.of("repository.open", "leakyCache.evict", "repository.close"), log);
    }

    @Test
    void annotationContextRegistersProcessorItself() {
        try (var context = new AnnotationConfigApplicationContext(getClass().getPackageName())) {
            assertEquals(List.of("priceCache.warm"), log);
        }

        assertEquals(List.of("priceCache.warm", "priceCache.evict"), log);
    }

    private void register(String beanName, Class<?> beanClass) {
        beanFactory.registerBeanDefinition(beanName, new BeanDefinition(beanClass));
    }

    static class OrderRepository {
        @PostConstruct
        void open() {
            log.add("repository.open");
        }

        @PreDestroy
        void close() {
            log.add("repository.close");
        }
    }

    static class OrderService {
        @Autowired
        OrderRepository repository;

        @PostConstruct
        void start() {
            log.add("orderService.start:" + (repository != null));
        }

        @PreDestroy
        void stop() {
            log.add("orderService.stop");
        }
    }

    static class BrokenService {
        @PostConstruct
        void start() {
            throw new IllegalStateException("boom");
        }
    }

    static class MisconfiguredService {
        @PostConstruct
        void start(String mode) {
        }
    }

    static class BaseWorker {
        @PostConstruct
        private void init() {
            log.add("base.init");
        }

        @PostConstruct
        void configure() {
            log.add("base.configure");
        }

        @PreDestroy
        private void release() {
            log.add("base.release");
        }
    }

    static class Worker extends BaseWorker {
        @PostConstruct
        private void init() {
            log.add("worker.init");
        }

        @Override
        @PostConstruct
        void configure() {
            log.add("worker.configure");
        }

        @PreDestroy
        private void release() {
            log.add("worker.release");
        }
    }

    static class LeakyCache {
        @PreDestroy
        void evict() {
            log.add("leakyCache.evict");
            throw new IllegalStateException("boom");
        }
    }

    @Component
    static class PriceCache {
        @PostConstruct
        void warm() {
            log.add("priceCache.warm");
        }

        @PreDestroy
        void evict() {
            log.add("priceCache.evict");
        }
    }
}

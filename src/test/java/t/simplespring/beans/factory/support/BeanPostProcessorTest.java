package t.simplespring.beans.factory.support;

import org.junit.jupiter.api.Test;
import t.simplespring.beans.factory.BeanCreationException;
import t.simplespring.beans.factory.annotation.Autowired;
import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.config.BeanPostProcessor;
import t.simplespring.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanPostProcessorTest {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    private final List<String> log = new ArrayList<>();

    @Test
    void callsBothHooksInOrderAfterInjection() {
        register("orderService", OrderService.class);
        register("repository", OrderRepository.class);
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof OrderService orderService) {
                    log.add("before:" + beanName + ":" + (orderService.repository != null));
                }
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof OrderService) {
                    log.add("after:" + beanName);
                }
                return bean;
            }
        });

        beanFactory.getBean("orderService");

        assertEquals(List.of("before:orderService:true", "after:orderService"), log);
    }

    @Test
    void appliesProcessorsInRegistrationOrder() {
        register("repository", OrderRepository.class);
        beanFactory.addBeanPostProcessor(new LoggingProcessor("first"));
        beanFactory.addBeanPostProcessor(new LoggingProcessor("second"));

        beanFactory.getBean("repository");

        assertEquals(List.of("first:before", "second:before", "first:after", "second:after"), log);
    }

    @Test
    void doesNotRegisterSameProcessorTwice() {
        register("repository", OrderRepository.class);
        LoggingProcessor processor = new LoggingProcessor("only");
        beanFactory.addBeanPostProcessor(processor);
        beanFactory.addBeanPostProcessor(processor);

        beanFactory.getBean("repository");

        assertEquals(List.of("only:before", "only:after"), log);
    }

    @Test
    void processorCanReplaceBean() {
        register("greeter", PoliteGreeter.class);
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                return bean instanceof Greeter greeter ? new LoudGreeter(greeter) : bean;
            }
        });

        Greeter greeter = beanFactory.getBean(Greeter.class);

        assertEquals("HELLO", greeter.greet());
        assertSame(greeter, beanFactory.getBean("greeter"));
    }

    @Test
    void keepsBeanWhenProcessorReturnsNull() {
        register("repository", OrderRepository.class);
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return null;
            }
        });

        assertInstanceOf(OrderRepository.class, beanFactory.getBean("repository"));
    }

    @Test
    void appliesProcessorsToEveryPrototypeInstance() {
        beanFactory.registerBeanDefinition("repository",
                new BeanDefinition(OrderRepository.class, BeanDefinition.SCOPE_PROTOTYPE));
        beanFactory.addBeanPostProcessor(new LoggingProcessor("processor"));

        beanFactory.getBean("repository");
        beanFactory.getBean("repository");

        assertEquals(4, log.size());
    }

    @Test
    void wrapsProcessorFailure() {
        register("repository", OrderRepository.class);
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                throw new IllegalStateException("boom");
            }
        });

        BeanCreationException e = assertThrows(BeanCreationException.class,
                () -> beanFactory.getBean("repository"));

        assertTrue(e.getMessage().contains("repository"));
        assertInstanceOf(IllegalStateException.class, e.getCause());
    }

    @Test
    void contextDetectsProcessorBeans() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("repository", OrderRepository.class);
        context.registerBean("orderService", OrderService.class);
        context.registerBean("recordingProcessor", RecordingProcessor.class);

        context.refresh();

        assertEquals(List.of("repository", "orderService"),
                context.getBean(RecordingProcessor.class).processed);
    }

    private void register(String beanName, Class<?> beanClass) {
        beanFactory.registerBeanDefinition(beanName, new BeanDefinition(beanClass));
    }

    private class LoggingProcessor implements BeanPostProcessor {
        private final String name;

        LoggingProcessor(String name) {
            this.name = name;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            log.add(name + ":before");
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            log.add(name + ":after");
            return bean;
        }
    }

    static class RecordingProcessor implements BeanPostProcessor {
        final List<String> processed = new ArrayList<>();

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            processed.add(beanName);
            return bean;
        }
    }

    static class OrderRepository {
    }

    static class OrderService {
        @Autowired
        OrderRepository repository;
    }

    interface Greeter {
        String greet();
    }

    static class PoliteGreeter implements Greeter {
        @Override
        public String greet() {
            return "hello";
        }
    }

    record LoudGreeter(Greeter delegate) implements Greeter {
        @Override
        public String greet() {
            return delegate.greet().toUpperCase();
        }
    }
}

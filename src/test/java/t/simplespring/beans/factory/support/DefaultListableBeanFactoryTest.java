package t.simplespring.beans.factory.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import t.simplespring.beans.BeansException;
import t.simplespring.beans.factory.BeanCreationException;
import t.simplespring.beans.factory.BeanDefinitionStoreException;
import t.simplespring.beans.factory.NoSuchBeanDefinitionException;
import t.simplespring.beans.factory.NoUniqueBeanDefinitionException;
import t.simplespring.beans.factory.config.BeanDefinition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultListableBeanFactoryTest {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @BeforeEach
    void resetCounter() {
        CountingService.instances = 0;
    }

    @Test
    void returnsBeanByName() {
        beanFactory.registerBeanDefinition("greeter", new BeanDefinition(EnglishGreeter.class));

        assertInstanceOf(EnglishGreeter.class, beanFactory.getBean("greeter"));
        assertTrue(beanFactory.containsBean("greeter"));
        assertFalse(beanFactory.containsBean("unknown"));
    }

    @Test
    void returnsBeanByNameAndType() {
        beanFactory.registerBeanDefinition("greeter", new BeanDefinition(EnglishGreeter.class));

        Greeter greeter = beanFactory.getBean("greeter", Greeter.class);

        assertEquals("hello", greeter.greet());
    }

    @Test
    void returnsBeanByType() {
        beanFactory.registerBeanDefinition("greeter", new BeanDefinition(EnglishGreeter.class));
        beanFactory.registerBeanDefinition("counting", new BeanDefinition(CountingService.class));

        assertSame(beanFactory.getBean("greeter"), beanFactory.getBean(Greeter.class));
    }

    @Test
    void returnsSameInstanceOnEveryCall() {
        beanFactory.registerBeanDefinition("counting", new BeanDefinition(CountingService.class));

        assertSame(beanFactory.getBean("counting"), beanFactory.getBean("counting"));
        assertEquals(1, CountingService.instances);
    }

    @Test
    void createsBeanOnlyOnFirstRequest() {
        beanFactory.registerBeanDefinition("counting", new BeanDefinition(CountingService.class));
        assertEquals(0, CountingService.instances);

        beanFactory.getBean("counting");

        assertEquals(1, CountingService.instances);
    }

    @Test
    void preInstantiatesAllSingletons() {
        beanFactory.registerBeanDefinition("counting", new BeanDefinition(CountingService.class));

        beanFactory.preInstantiateSingletons();

        assertEquals(1, CountingService.instances);
    }

    @Test
    void createsNewPrototypeOnEveryCall() {
        beanFactory.registerBeanDefinition("counting",
                new BeanDefinition(CountingService.class, BeanDefinition.SCOPE_PROTOTYPE));

        assertNotSame(beanFactory.getBean("counting"), beanFactory.getBean(CountingService.class));
        assertEquals(2, CountingService.instances);
    }

    @Test
    void doesNotPreInstantiatePrototypes() {
        beanFactory.registerBeanDefinition("counting",
                new BeanDefinition(CountingService.class, BeanDefinition.SCOPE_PROTOTYPE));

        beanFactory.preInstantiateSingletons();

        assertEquals(0, CountingService.instances);
    }

    @Test
    void failsOnUnknownScope() {
        BeanDefinitionStoreException e = assertThrows(BeanDefinitionStoreException.class,
                () -> beanFactory.registerBeanDefinition("counting",
                        new BeanDefinition(CountingService.class, "request")));

        assertTrue(e.getMessage().contains("request"));
    }

    @Test
    void keepsDefinitionNamesInRegistrationOrder() {
        beanFactory.registerBeanDefinition("second", new BeanDefinition(RussianGreeter.class));
        beanFactory.registerBeanDefinition("first", new BeanDefinition(EnglishGreeter.class));

        assertEquals(List.of("second", "first"), beanFactory.getBeanDefinitionNames());
    }

    @Test
    void failsOnUnknownName() {
        NoSuchBeanDefinitionException e = assertThrows(NoSuchBeanDefinitionException.class,
                () -> beanFactory.getBean("missing"));

        assertTrue(e.getMessage().contains("missing"));
    }

    @Test
    void failsOnUnknownType() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> beanFactory.getBean(Greeter.class));
    }

    @Test
    void failsOnAmbiguousType() {
        beanFactory.registerBeanDefinition("english", new BeanDefinition(EnglishGreeter.class));
        beanFactory.registerBeanDefinition("russian", new BeanDefinition(RussianGreeter.class));

        NoUniqueBeanDefinitionException e = assertThrows(NoUniqueBeanDefinitionException.class,
                () -> beanFactory.getBean(Greeter.class));

        assertTrue(e.getMessage().contains("english, russian"));
    }

    @Test
    void failsOnTypeMismatch() {
        beanFactory.registerBeanDefinition("counting", new BeanDefinition(CountingService.class));

        assertThrows(BeansException.class, () -> beanFactory.getBean("counting", Greeter.class));
    }

    @Test
    void failsOnDuplicateName() {
        beanFactory.registerBeanDefinition("greeter", new BeanDefinition(EnglishGreeter.class));

        assertThrows(BeanDefinitionStoreException.class,
                () -> beanFactory.registerBeanDefinition("greeter", new BeanDefinition(RussianGreeter.class)));
    }

    @Test
    void failsWhenConstructorCannotBeChosen() {
        beanFactory.registerBeanDefinition("configured", new BeanDefinition(ConfiguredService.class));

        BeanCreationException e = assertThrows(BeanCreationException.class,
                () -> beanFactory.getBean("configured"));

        assertTrue(e.getMessage().contains("configured"));
        assertInstanceOf(NoSuchMethodException.class, e.getCause());
    }

    @Test
    void failsWhenConstructorThrows() {
        beanFactory.registerBeanDefinition("broken", new BeanDefinition(BrokenService.class));

        BeanCreationException e = assertThrows(BeanCreationException.class,
                () -> beanFactory.getBean("broken"));

        assertTrue(e.getMessage().contains("broken"));
        assertInstanceOf(IllegalStateException.class, e.getCause());
    }

    interface Greeter {
        String greet();
    }

    static class EnglishGreeter implements Greeter {
        @Override
        public String greet() {
            return "hello";
        }
    }

    static class RussianGreeter implements Greeter {
        @Override
        public String greet() {
            return "привет";
        }
    }

    static class CountingService {
        static int instances;

        CountingService() {
            instances++;
        }
    }

    static class ConfiguredService {
        ConfiguredService(String url) {
        }

        ConfiguredService(String url, int timeout) {
        }
    }

    static class BrokenService {
        BrokenService() {
            throw new IllegalStateException("no connection");
        }
    }
}

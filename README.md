# simplespring

Spring Framework подобный IoC-контейнер на Java 25.

## Возможности

- **Определения бинов и области видимости** — `BeanDefinition`, `BeanDefinitionRegistry`,
  `DefaultListableBeanFactory`; `singleton` и `prototype` через `@Scope`.
- **Внедрение зависимостей по типу** — через конструктор и в поля с `@Autowired`; выбор из нескольких
  кандидатов по `@Qualifier` и `@Primary`; обнаружение циклических зависимостей.
- **Сканирование classpath** — классы с `@Component` в каталогах и jar-файлах.
- **Java-конфигурация** — `@Configuration` и фабричные методы `@Bean`.
- **Точки расширения** — `BeanPostProcessor` и `DestructionAwareBeanPostProcessor`.
- **Жизненный цикл** — `@PostConstruct`, `@PreDestroy`, закрытие контекста через `close()`.
- **AOP** — `MethodInterceptor`, `Pointcut`, `Advisor`; бины, к которым подходит советник,
  автоматически оборачиваются в JDK-прокси.
- **События** — `ApplicationEvent`, `ApplicationListener`, `ApplicationEventPublisher`.

## Пример

```java
package com.example.shop;

public interface OrderRepository {

    void save(String item);
}

@Component
public class InMemoryOrderRepository implements OrderRepository {

    private final List<String> items = new ArrayList<>();

    @Override
    public void save(String item) {
        items.add(item);
    }
}

@Component
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void start() {
        System.out.println("order service is ready");
    }

    public void placeOrder(String item) {
        repository.save(item);
    }
}
```

```java
try (var context = new AnnotationConfigApplicationContext("com.example.shop")) {
    context.getBean(OrderService.class).placeOrder("book");
}
```

Так же контекст можно собрать вручную:

```java
var context = new GenericApplicationContext();
context.registerBean("orderRepository", InMemoryOrderRepository.class);
context.registerBean("orderService", OrderService.class);
context.refresh();
```

## Ограничения

- Прокси создаются только через `java.lang.reflect.Proxy`: у проксируемого бина должен быть интерфейс,
  и внедрять его нужно по интерфейсу. Проксирование через CGLIB не предусмотрено.
- `@Qualifier` сопоставляется с именем бина.
- `@Scope` и `@Primary` на методах `@Bean` не учитываются.
- События доставляются синхронно, в потоке издателя.

## Сборка

Нужен JDK 25.

```bash
./gradlew build      # сборка и тесты
```

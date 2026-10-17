package t.simplespring.integration.shop.config;

import t.simplespring.aop.Advisor;
import t.simplespring.aop.impl.DefaultPointcutAdvisor;
import t.simplespring.beans.factory.annotation.Qualifier;
import t.simplespring.context.annotation.Bean;
import t.simplespring.context.annotation.Configuration;
import t.simplespring.integration.shop.logging.Logged;
import t.simplespring.integration.shop.logging.LoggingInterceptor;
import t.simplespring.integration.shop.notification.Notifier;
import t.simplespring.integration.shop.order.OrderRepository;


@Configuration
public class ShopConfiguration {

    @Bean
    InvoiceService invoiceService(OrderRepository repository, @Qualifier("smsNotifier") Notifier notifier) {
        return new InvoiceService(repository, notifier);
    }

    @Bean
    Advisor loggingAdvisor(LoggingInterceptor interceptor) {
        return new DefaultPointcutAdvisor(
                (method, targetClass) -> method.isAnnotationPresent(Logged.class), interceptor);
    }
}

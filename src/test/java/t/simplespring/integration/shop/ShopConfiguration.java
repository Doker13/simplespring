package t.simplespring.integration.shop;

import t.simplespring.aop.Advisor;
import t.simplespring.aop.support.DefaultPointcutAdvisor;
import t.simplespring.beans.factory.annotation.Qualifier;
import t.simplespring.context.annotation.Bean;
import t.simplespring.context.annotation.Configuration;

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

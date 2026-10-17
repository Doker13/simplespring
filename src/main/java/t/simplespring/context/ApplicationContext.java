package t.simplespring.context;

import t.simplespring.beans.factory.BeanFactory;
import t.simplespring.context.event.ApplicationEventPublisher;


public interface ApplicationContext extends BeanFactory, ApplicationEventPublisher {
}

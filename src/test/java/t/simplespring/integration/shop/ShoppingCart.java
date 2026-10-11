package t.simplespring.integration.shop;

import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.context.annotation.Scope;
import t.simplespring.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ShoppingCart {
}

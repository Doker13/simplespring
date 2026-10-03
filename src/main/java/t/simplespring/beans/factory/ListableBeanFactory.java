package t.simplespring.beans.factory;

import java.util.List;

public interface ListableBeanFactory extends BeanFactory {

    List<String> getBeanNamesForType(Class<?> type);
}

package t.simplespring.context;

import java.util.EventObject;

public abstract class ApplicationEvent extends EventObject {

    protected ApplicationEvent(Object source) {
        super(source);
    }
}

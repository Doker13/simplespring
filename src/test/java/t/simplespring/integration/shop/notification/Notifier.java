package t.simplespring.integration.shop.notification;

import java.util.List;

public interface Notifier {

    void send(String message);

    List<String> sent();
}

package t.simplespring.integration.shop;

import java.util.List;

public interface Notifier {

    void send(String message);

    List<String> sent();
}

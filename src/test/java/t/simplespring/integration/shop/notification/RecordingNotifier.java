package t.simplespring.integration.shop.notification;

import java.util.ArrayList;
import java.util.List;

public abstract class RecordingNotifier implements Notifier {

    private final List<String> sent = new ArrayList<>();

    @Override
    public void send(String message) {
        sent.add(message);
    }

    @Override
    public List<String> sent() {
        return List.copyOf(sent);
    }
}

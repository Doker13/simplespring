package t.simplespring.integration.shop.notification;

import t.simplespring.context.annotation.Primary;
import t.simplespring.stereotype.Component;


@Component
@Primary
public class EmailNotifier extends RecordingNotifier {
}

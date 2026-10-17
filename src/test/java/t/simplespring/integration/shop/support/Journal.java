package t.simplespring.integration.shop.support;

import t.simplespring.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Journal {

    private final List<String> entries = new ArrayList<>();

    public void record(String entry) {
        entries.add(entry);
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}

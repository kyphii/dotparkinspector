package model;

import java.util.ArrayList;
import java.util.List;

public class ObjectGroup {
    final ObjectCategory type;
    private int count;

    private List<ObjectEntry> entries;

    public ObjectGroup(ObjectCategory type) {
        this.type = type;
        entries = new ArrayList<>();
    }

    public ObjectCategory getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ObjectEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ObjectEntry> entries) {
        this.entries = entries;
    }
}

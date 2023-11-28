package model;

public class ObjectGroup {
    final ObjectCategory type;
    private int count;

    public ObjectGroup(ObjectCategory type) {
        this.type = type;
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
}

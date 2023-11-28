package model;

public class ObjectGroup {
    final ObjectType type;
    private int count;

    public ObjectGroup(ObjectType type) {
        this.type = type;
    }

    public ObjectType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

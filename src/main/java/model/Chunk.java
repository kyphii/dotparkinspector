package model;

public record Chunk(int id, long offset, long length) {

    public int getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }
}

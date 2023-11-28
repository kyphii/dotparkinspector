package model;

public enum ObjectFileType {
    DESCRIPTOR_NONE(0), DESCRIPTOR_DAT(1), DESCRIPTOR_JSON(2);

    private final int typeId;

    ObjectFileType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public static ObjectFileType byId(int typeId) {
        return ObjectFileType.values()[typeId];
    }
}

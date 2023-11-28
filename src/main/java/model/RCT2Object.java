package model;

public class RCT2Object {
    private final String id;
    private final String enGbName;

    private final ObjectType objectType;

    private final boolean isBaseGame;
    private final boolean flaggedAsIssue;

    public RCT2Object(ObjectType objectType, String id, String enGbName, boolean isBaseGame, boolean flaggedAsIssue) {
        this.objectType = objectType;
        this.id = id;
        this.enGbName = enGbName;
        this.isBaseGame = isBaseGame;
        this.flaggedAsIssue = flaggedAsIssue;
    }

    public String getId() {
        return id;
    }

    public String getEnGbName() {
        return enGbName;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public boolean isBaseGame() {
        return isBaseGame;
    }

    public boolean isFlaggedAsIssue() {
        return flaggedAsIssue;
    }
}

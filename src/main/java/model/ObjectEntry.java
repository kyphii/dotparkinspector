package model;

public class ObjectEntry {
    private String id;
    private String enGbName;

    private ObjectCategory objectCategory;

    private boolean isBaseGame;
    private boolean flaggedAsIssue;

    public ObjectEntry() {

    }

    public ObjectEntry(ObjectCategory objectCategory, String id, String enGbName, boolean isBaseGame, boolean flaggedAsIssue) {
        this.objectCategory = objectCategory;
        this.id = id;
        this.enGbName = enGbName;
        this.isBaseGame = isBaseGame;
        this.flaggedAsIssue = flaggedAsIssue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnGbName() {
        return enGbName;
    }

    public void setEnGbName(String enGbName) {
        this.enGbName = enGbName;
    }

    public ObjectCategory getObjectCategory() {
        return objectCategory;
    }

    public void setObjectCategory(ObjectCategory objectCategory) {
        this.objectCategory = objectCategory;
    }

    public boolean isBaseGame() {
        return isBaseGame;
    }

    public void setIsBaseGame(boolean baseGame) {
        isBaseGame = baseGame;
    }

    public boolean isFlaggedAsIssue() {
        return flaggedAsIssue;
    }

    public void setFlaggedAsIssue(boolean flaggedAsIssue) {
        this.flaggedAsIssue = flaggedAsIssue;
    }

    @Override
    public String toString() {
        return "ObjectEntry{" +
                "id='" + id + '\'' +
                ", enGbName='" + enGbName + '\'' +
                ", objectCategory=" + objectCategory +
                '}';
    }
}

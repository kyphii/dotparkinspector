package model;

public class ParkObjectEntry extends ObjectEntry {
    private String author;

    private String version;

    private String originalID;

    public ParkObjectEntry() {

    }

    public ParkObjectEntry(ObjectCategory objectType, String id, String enGbName, boolean isBaseGame, boolean flaggedAsIssue,
                           String author, String version, String originalID) {
        super(objectType, id, enGbName, isBaseGame, flaggedAsIssue);
        this.author = author;
        this.version = version;
        this.originalID = originalID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOriginalID() {
        return originalID;
    }

    public void setOriginalID(String originalID) {
        this.originalID = originalID;
    }
}

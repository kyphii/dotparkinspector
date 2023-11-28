package model;

public class ParkObject extends RCT2Object {
    private final String author;

    private final String originalID;

    public ParkObject(ObjectType objectType, String id, String enGbName, boolean isBaseGame, boolean flaggedAsIssue,
                      String author, String originalID) {
        super(objectType, id, enGbName, isBaseGame, flaggedAsIssue);
        this.author = author;
        this.originalID = originalID;
    }

    public String getAuthor() {
        return author;
    }

    public String getOriginalID() {
        return originalID;
    }
}

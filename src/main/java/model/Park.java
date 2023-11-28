package model;

public class Park {
    private final String parkFileName;
    private final String parkFilePath;

    private String parkName;
    private final ObjectGroup[] objectGroupList;

    public Park(String parkFileName, String parkFilePath) {
        this.parkFileName = parkFileName;
        this.parkFilePath = parkFilePath;

        objectGroupList = new ObjectGroup[ObjectCategory.count()];
        for (int i = 0; i < objectGroupList.length; ++i) {
            objectGroupList[i] = new ObjectGroup(ObjectCategory.byId(i));
        }
    }

    public String getParkFileName() {
        return parkFileName;
    }

    public String getParkFilePath() {
        return parkFilePath;
    }

    public String getParkName() {
        return parkName;
    }

    public ObjectGroup[] getObjectGroupList() {
        return objectGroupList;
    }
}

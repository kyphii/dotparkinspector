package model;

public enum ObjectCategory {
    RIDE("ride"),
    SMALL_SCENERY("small_scenery"),
    LARGE_SCENERY("large_scenery"),
    WALL("walls"),
    BANNER("banners"),
    PATH("paths"),
    PATH_ADDITION("path_additions"),
    SCENERY_GROUP("scenery_group"),
    PARK_ENTRANCE("park_entrance"),
    PALETTE("water"),
    SCENARIO_TEXT("scenario_text"),
    TERRAIN_SURFACE("terrain_surface"),
    TERRAIN_EDGE("terrain_edge"),
    STATION("station"),
    MUSIC("music"),
    FOOTPATH_SURFACE("footpath_surface"),
    FOOTPATH_RAILING("footpath_railings"),
    AUDIO("audio");

    private final String typeID;

    ObjectCategory(String typeID) {
        this.typeID = typeID;
    }

    @Override
    public String toString() {
        return typeID;
    }

    public static ObjectCategory byId(int id) {
        return ObjectCategory.values()[id];
    }

    public static int count() {
        return ObjectCategory.values().length;
    }
}

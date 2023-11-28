public class Options {
    private static String pathParkFile = "";

    private static String pathBaseGameObjectRepo = "";
    private static String pathCustomObjectRepo = "";

    private static boolean scanBaseGameObjects = true;
    private static boolean scanCustomObjects = true;

    public static String getPathParkFile() {
        return pathParkFile;
    }

    public static void setPathParkFile(String pathParkFile) {
        Options.pathParkFile = pathParkFile;
    }

    public static String getPathBaseGameObjectRepo() {
        return pathBaseGameObjectRepo;
    }

    public static void setPathBaseGameObjectRepo(String pathBaseGameObjectRepo) {
        Options.pathBaseGameObjectRepo = pathBaseGameObjectRepo;
    }

    public static String getPathCustomObjectRepo() {
        return pathCustomObjectRepo;
    }

    public static void setPathCustomObjectRepo(String pathCustomObjectRepo) {
        Options.pathCustomObjectRepo = pathCustomObjectRepo;
    }

    public static boolean getScanBaseGameObjects() {
        return scanBaseGameObjects;
    }

    public static void setScanBaseGameObjects(boolean scanBaseGameObjects) {
        Options.scanBaseGameObjects = scanBaseGameObjects;
    }

    public static boolean getScanCustomObjects() {
        return scanCustomObjects;
    }

    public static void setScanCustomObjects(boolean scanCustomObjects) {
        Options.scanCustomObjects = scanCustomObjects;
    }
}

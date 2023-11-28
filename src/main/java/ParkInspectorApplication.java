import java.io.File;
public class ParkInspectorApplication {
    public static void main(String[] args) {
        //TODO snazzy ui

        //Testing
        String path = ""; //Full park file path goes here
        new DotParkProcessor().scanParkFile(new File(path));

    }
}

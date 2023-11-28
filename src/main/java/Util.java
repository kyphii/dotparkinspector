import java.io.File;
import java.util.Optional;

public class Util {
    // Source: https://www.baeldung.com/java-file-extension
    public static String getFileExtension(String filename) {
        Optional<String> extension = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
        return (extension.isPresent()) ? extension.get() : null;
    }

    public static String getFilePathWithoutName(File file) {
        String path = file.getPath();
        return path.substring(0, path.length() - file.getName().length());
    }
}

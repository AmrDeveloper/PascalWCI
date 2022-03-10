package ide.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ImageUtil {
    public static String FOLDER_CLOSE = "images/folder-close.gif";
    public static String FOLDER_OPEN = "images/folder-open.gif";
    public static String FILE = "images/file.gif";

    public static Image getImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static ImageIcon getImageIcon(String path) throws IOException {
        return new ImageIcon(getImage(path));
    }
}

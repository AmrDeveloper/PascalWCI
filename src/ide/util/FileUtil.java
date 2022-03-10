package ide.util;

import java.io.*;

public class FileUtil {
    public static String readFile(File file) {
        StringBuilder result = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            String content = null;
            byte[] arr = new byte[1024];
            int readLength;
            while ((readLength = fis.read(arr)) > 0) {
                content = new String(arr, 0, readLength);
                result.append(content);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();

    }

    public static void writeFile(File file, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            ps.print(content);
            ps.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

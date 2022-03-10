package ide.util.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunProcess {

    private static final String COMMAND = "java -classpath ./out/production/PascalWCI Pascal execute GUI -ixlafcr %s";

    public static String run(String source) {
        String command = String.format(COMMAND, source);
        StringBuilder builder = new StringBuilder();
        try {
            Process process = new ProcessBuilder(command.split(" ")).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            reader.close();
            process.destroy();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "FAIL";
    }

}

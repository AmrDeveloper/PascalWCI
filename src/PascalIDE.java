import ide.IDEFrame;
import ide.SpaceFrame;

import java.awt.*;

public class PascalIDE {

    public static void main(String[] args) {
        EventQueue.invokeLater(()->{
            IDEFrame ideFrame = new IDEFrame("PascalIDE");
            SpaceFrame spaceFrame = new SpaceFrame(ideFrame);
            spaceFrame.setVisible(true);
        });
    }
}

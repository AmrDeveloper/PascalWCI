package ide.pane;

import javax.swing.*;

public class ConsolePane extends JScrollPane {
    private JTextArea info;

    public ConsolePane(JTextArea info) {
        super(info);
        this.info = info;
        this.info.setEditable(false);
    }

    public void setInfo(String info) {
        this.info.setText(info);
    }
}

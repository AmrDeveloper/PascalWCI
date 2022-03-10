package ide.pane;

import javax.swing.*;

public class OutputPane extends JScrollPane {
    private JTextArea output;

    public OutputPane(JTextArea output) {
        super(output);
        this.output = output;
        this.output.setEditable(false);
    }

    public void setOutput(String output) {
        this.output.setText(output);
    }
}

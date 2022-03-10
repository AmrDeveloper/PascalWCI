package ide.pane;

import javax.swing.*;

public class ICodePane extends JScrollPane {
    private JTextArea iCode;

    public ICodePane(JTextArea iCode) {
        super(iCode);
        this.iCode = iCode;
        this.iCode.setEditable(false);
    }

    public void setICode(String iCode) {
        this.iCode.setText(iCode);
    }
}

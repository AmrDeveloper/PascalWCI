package ide.util.edit.listener;

import ide.pane.EditPane;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EditDocumentListener implements DocumentListener {
    private EditPane editPane;

    public EditDocumentListener(EditPane editPane) {
        this.editPane = editPane;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        editPane.getCurrentFile().setSaved(false);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
}

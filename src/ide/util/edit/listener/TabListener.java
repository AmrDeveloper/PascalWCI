package ide.util.edit.listener;


import ide.pane.EditPane;
import ide.util.edit.EditFile;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TabListener implements ChangeListener {
    private EditPane editPane;

    public TabListener(EditPane editPane) {
        this.editPane = editPane;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        int index = tabbedPane.getSelectedIndex();

        if (index == -1) return;

        JInternalFrame currentFrame = editPane.getIFrame(tabbedPane.getToolTipTextAt(index));
        editPane.showIFrame(currentFrame);
        EditFile currentFile = editPane.getEditFile(currentFrame);
        editPane.setCurrentFile(currentFile);
    }
}

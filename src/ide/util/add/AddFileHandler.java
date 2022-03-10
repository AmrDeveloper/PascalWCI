package ide.util.add;

import ide.IDEFrame;
import ide.tree.ProjectTreeNode;

import javax.swing.*;
import java.io.File;

public class AddFileHandler implements AddHandler {
    @Override
    public void afterAdd(IDEFrame ideFrame, AddFrame addFrame, Object data) {
        // run the code here after entering the file name
        try {
            ProjectTreeNode selectNode = ideFrame.getSelectNode();
            File folder = selectNode.getFile();
            if (!folder.isDirectory()) {
                ProjectTreeNode parent = (ProjectTreeNode) selectNode.getParent();
                selectNode = parent;
                folder = parent.getFile();
            }
            File newFile = new File(folder.getAbsoluteFile() + File.separator + data);
            if (newFile.exists()) {
                JOptionPane.showMessageDialog(addFrame, "This file is already exists");
                return;
            }
            newFile.createNewFile();
            ideFrame.reloadNode(selectNode);
            ideFrame.setEnabled(true);
            addFrame.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

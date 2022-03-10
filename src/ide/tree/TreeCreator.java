package ide.tree;

import ide.IDEFrame;

import javax.swing.*;
import java.io.File;

public interface TreeCreator {
    JTree createTree(IDEFrame ideFrame);

    ProjectTreeNode createNode(File folder);
}

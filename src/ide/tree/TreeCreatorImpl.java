package ide.tree;


import ide.IDEFrame;
import ide.pane.FileBrowserPane;
import ide.util.ImageUtil;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TreeCreatorImpl implements TreeCreator {
    @Override
    public FileBrowserPane createTree(IDEFrame ideFrame) {
        File spaceFolder = ideFrame.getWorkSpace().getFolder();
        ProjectTreeNode root = new ProjectTreeNode(spaceFolder, true);
        ProjectTreeModel treeModel = new ProjectTreeModel(root);
        FileBrowserPane tree = new FileBrowserPane(treeModel);
        root.add(createNode(spaceFolder));
        try {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setOpenIcon(ImageUtil.getImageIcon(ImageUtil.FOLDER_OPEN));
            renderer.setLeafIcon(ImageUtil.getImageIcon(ImageUtil.FILE));
            renderer.setClosedIcon(ImageUtil.getImageIcon(ImageUtil.FOLDER_CLOSE));
            tree.setCellRenderer(renderer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TreePath treePath = new TreePath(root);
        tree.expandPath(treePath);
        expandAllNodes(tree, 0, tree.getRowCount());
        tree.setRootVisible(false); // avoid repeat
        tree.addMouseListener(new ProjectTreeSelectionListener(ideFrame));
        return tree;
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        // only first layer
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
    }

    @Override
    public ProjectTreeNode createNode(File folder) {
        ProjectTreeNode parent = null;
        if (!folder.isDirectory()) {
            return new ProjectTreeNode(folder, false);
        } else {
            parent = new ProjectTreeNode(folder, true);
        }
        List<ProjectTreeNode> nodes = createNodes(parent.getFile());
        for (ProjectTreeNode node : nodes) {
            parent.add(createNode(node.getFile()));
        }
        return parent;
    }

    private List<ProjectTreeNode> createNodes(File folder) {
        File[] files = folder.listFiles();// all files in this directory
        List<ProjectTreeNode> result = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(new ProjectTreeNode(file, true));
            }
        }
        for (File file : files) {
            if (!file.isDirectory()) {
                result.add(new ProjectTreeNode(file, false));
            }
        }
        return result;
    }

    private List<File> getFolders(File spaceFolder) {
        List<File> result = new ArrayList<>();
        File[] files = spaceFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(file);
            }
        }
        return result;
    }
}

package ide.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectTreeNode extends DefaultMutableTreeNode {
    private File file;
    private List<ProjectTreeNode> children;

    public ProjectTreeNode(File file, boolean allowsChildren) {
        super(file.getName(), allowsChildren);
        this.file = file;
        children = new ArrayList<>();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<ProjectTreeNode> getChildren() {
        // clear and re-get
        children.removeAll(children);
        for (int i = 0; i < getChildCount(); ++i) {
            children.add((ProjectTreeNode) getChildAt(i));
        }
        return this.children;
    }
}

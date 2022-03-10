package ide.tree;

import javax.swing.tree.DefaultTreeModel;

public class ProjectTreeModel extends DefaultTreeModel {
    public ProjectTreeModel(ProjectTreeNode node) {
        super(node);
    }

    public void reload(ProjectTreeNode node, TreeCreator creator) {
        ProjectTreeNode parent = (ProjectTreeNode) node.getParent();

        if (parent == null) return;

        int index = parent.getIndex(node);
        parent.remove(index);
        node = creator.createNode(node.getFile());
        parent.insert(node, index);

        /*Invoke this method if you've modified the TreeNodes upon which this model depends.*/
        super.reload(node);
    }

}

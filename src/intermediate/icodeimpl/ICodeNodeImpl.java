package intermediate.icodeimpl;

import intermediate.ICodeFactory;
import intermediate.ICodeKey;
import intermediate.ICodeNode;
import intermediate.ICodeNodeType;

import java.util.*;

public class ICodeNodeImpl
        extends HashMap<ICodeKey, Object>
        implements ICodeNode {

    private ICodeNodeType type;
    private ICodeNode parent;
    private List<ICodeNode> children;

    public ICodeNodeImpl(ICodeNodeType type) {
        this.type = type;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    @Override
    public ICodeNodeType getType() {
        return type;
    }

    @Override
    public ICodeNode getParent() {
        return parent;
    }

    @Override
    public ICodeNode addChild(ICodeNode node) {
        if(node != null) {
            children.add(node);
            ((ICodeNodeImpl) node).parent = this;
        }
        return node;
    }

    @Override
    public List<ICodeNode> getChildren() {
        return children;
    }

    @Override
    public void setAttribute(ICodeKey key, Object value) {
        put(key, value);
    }

    @Override
    public Object getAttribute(ICodeKey key) {
        return get(key);
    }

    @Override
    public ICodeNode copy() {
        ICodeNodeImpl copy = (ICodeNodeImpl) ICodeFactory.createICodeNode(type);

        //Copy attributes from current Instance to new Instance
        Set<Entry<ICodeKey, Object>> attributes = entrySet();
        for (Entry<ICodeKey, Object> attribute : attributes) {
            copy.put(attribute.getKey(), attribute.getValue());
        }

        return copy;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}

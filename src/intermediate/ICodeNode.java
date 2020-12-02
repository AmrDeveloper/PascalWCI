package intermediate;

import java.util.List;

public interface ICodeNode {

    public ICodeNodeType getType();

    public ICodeNode getParent();

    public ICodeNode addChild(ICodeNode node);

    public List<ICodeNode> getChildren();

    public void setAttribute(ICodeKey key, Object value);

    public Object getAttribute(ICodeKey key);

    public void setTypeSpec(TypeSpec typeSpec);

    public TypeSpec getTypeSpec();

    public ICodeNode copy();
}

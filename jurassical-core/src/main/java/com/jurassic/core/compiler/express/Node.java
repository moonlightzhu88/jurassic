package com.jurassic.core.compiler.express;

/**
 * ���ʽ�������ĳ���ڵ�
 * ʹ���������ֵܵ����ݽṹ
 *
 * @author yzhu
 */
public abstract class Node {

    protected Node _parent;// ���ڵ�
    protected Node _firstChild;// ��һ���ӽڵ�
    protected Node _nextBrother;// ��һ���ֵܽڵ�

    public abstract String toText();

    public Node getFirstChild() {
        return this._firstChild;
    }

    public Node getBrother() {
        return this._nextBrother;
    }

    /**
     * �����ӽڵ�
     * @param childs
     */
    public void setChilds(Node[] childs) {
        for (Node child : childs)
            child._parent = this;
        this._firstChild = childs[0];
        for (int i = 0; i < childs.length - 1; i++)
            childs[i]._nextBrother = childs[i + 1];
    }
}

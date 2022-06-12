package com.jurassic.core.compiler.express;

/**
 * 表达式分析树的抽象节点
 * 使用左孩子右兄弟的数据结构
 *
 * @author yzhu
 */
public abstract class Node {

    protected Node _parent;// 父节点
    protected Node _firstChild;// 第一个子节点
    protected Node _nextBrother;// 下一个兄弟节点

    public abstract String toText();

    public Node getFirstChild() {
        return this._firstChild;
    }

    public Node getBrother() {
        return this._nextBrother;
    }

    /**
     * 设置子节点
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

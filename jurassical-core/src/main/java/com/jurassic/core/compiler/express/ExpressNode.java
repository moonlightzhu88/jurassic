package com.jurassic.core.compiler.express;

/**
 * ���ʽ�������Ľڵ�
 * �����Ա�ʾһ�����������ݣ��������߱�����
 * Ҳ���Ա�ʾһ�����ӵı��ʽ���������ݺ����������ϣ�
 *
 * @author yzhu
 */
public class ExpressNode extends Node{
    private Operator _operator;// �����

    public void setOperator(Operator operator) {
        this._operator = operator;
    }

    public Operator getOperator() {
        return this._operator;
    }

    public String toText() {
        if (this._operator == Operator.NEG) {
            return "(-" + this._firstChild.toText() + ")";
        } else if (this._operator == Operator.NOT) {
            return "!" + this._firstChild.toText();
        } else if (this._operator == Operator.MUL) {
            return "(" + this._firstChild.toText() + ")*(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.DIV) {
            return "(" + this._firstChild.toText() + ")/(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.MOD) {
            return "(" + this._firstChild.toText() + ")%(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.ADD) {
            return "(" + this._firstChild.toText() + ")+(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.SUB) {
            return "(" + this._firstChild.toText() + ")-(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.GREAT) {
            return "(" + this._firstChild.toText() + ")>(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.GREATEQUAL) {
            return "(" + this._firstChild.toText() + ")>=(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.LESS) {
            return "(" + this._firstChild.toText() + ")<(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.LESSEQUAL) {
            return "(" + this._firstChild.toText() + ")<=(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.EQUAL) {
            return "(" + this._firstChild.toText() + ")==(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.NOTEQUAL) {
            return "(" + this._firstChild.toText() + ")!=(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.AND) {
            return "(" + this._firstChild.toText() + ")&&(" + this._firstChild._nextBrother.toText() + ")";
        } else if (this._operator == Operator.OR) {
            return "(" + this._firstChild.toText() + ")||(" + this._firstChild._nextBrother.toText() + ")";
        } else {
            return "";
        }
    }
}

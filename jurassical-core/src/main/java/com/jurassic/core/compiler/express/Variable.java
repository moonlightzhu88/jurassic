package com.jurassic.core.compiler.express;

/**
 * ���ʽ�еı�������
 * ���ʽ�еı���ָ�������ݹܽŵĶ���
 * ����������ֱ�ӵ����ݹܽ�
 * Ҳ��������ĳ��task��������ݹܽ�
 * �������嶼��$��ͷ
 * �����Ҫ����task��������ݹܽţ���ʹ��[index]�ķ�ʽ
 *
 * @author yzhu
 */
public class Variable {

    private String _refName;// ���õ����ݹܽ�����
    private int _index = -1;// ����task������ݹܽŵ�����λ�ã������õ�ʱ��Ϊ-1

    public Variable(String refName) {
        this._refName = refName;
    }

    public Variable(String refName, int index) {
        this._refName = refName;
        this._index = index;
    }

    public String toString() {
        if (this._index == -1) {
            return "$" + this._refName;
        } else {
            return "$" + this._refName + "[" + this._index + "]";
        }
    }


    public String getRefName() {
        return this._refName;
    }

    public int getIndex() {
        return this._index;
    }
}

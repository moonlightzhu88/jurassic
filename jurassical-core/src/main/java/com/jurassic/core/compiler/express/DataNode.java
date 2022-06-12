package com.jurassic.core.compiler.express;

/**
 * ���ݽڵ�
 * ���Ա�ʾ�������ַ���������ֵ����ֵ��
 * Ҳ���Ա�ʾ���������ݹܽţ�����$��ͷ
 *
 * @author yzhu
 */
public class DataNode extends Node{

    private Object _data;// ���ݣ�ΪString��Number��Boolean��Variable4��

    public DataNode(Object data) {
        this._data = data;
    }

    public Object getData() {
        return this._data;
    }

    public String toText() {
        if (this._data instanceof String)
            return "'" + (String) this._data + "'";
        else
            return this._data.toString();
    }
}

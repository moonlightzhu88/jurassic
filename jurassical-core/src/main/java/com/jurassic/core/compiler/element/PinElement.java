package com.jurassic.core.compiler.element;

import java.util.List;

/**
 * ���ݹܽű�ǩ����������Ϣ����õ�һ��Ԫ��
 * ��������һ�����ݵķ�װ
 * ���̵ĸ�����ɲ���֮�����Ϣ���ݶ���Ҫ�ƶ���Ӧ�����ݹܽ�
 * ����9�ֲ�ͬ�Ĺܽ�
 * <data/>
 * <static-object/>
 * <class/>
 * <param/>
 * <ref/>
 * <list/>
 * <express/>
 * <composite/>
 * <empty/>
 * 
 * @author yzhu
 * 
 */
public abstract class PinElement extends Element {

	/**
	 * �ܽŵ����Ͷ���
	 */
	public static final int T_DATA = 0;// ���ݹܽţ���ʾһЩ�������͵�����
	public static final int T_STATIC = 1;// ��̬ʵ���ܽţ���ʾ��Щstatic����ʵ������
	public static final int T_CLASS = 2;// ��ܽţ���ʾClass����
	public static final int T_PARAM = 3;// �����ܽţ���ʾ��Ӧλ�õ������������
	public static final int T_REF = 4;// ���ùܽţ����������ܽ�
	public static final int T_LIST = 5;// �б�ܽţ��ɶ���ܽ���ɵ��б�
	public static final int T_EXPRESS = 6;// ���ʽ�ܽţ��ɶ���ܽź���Ӧ�ı��ʽ���
	public static final int T_COMPOSITE = 7;// ���Ϲܽţ��ɶ���ܽ���ɵĸ������ݹܽ�
	public static final int T_EMPTY = 8;// �չܽţ����������ݣ���Ӧ��null

	/**
	 * ��ùܽŵ�����
	 */
	public abstract int getPinType();

	/**
	 * ��ѯ���������ݹܽ�
	 */
	public abstract List<String> getRefPins();

}

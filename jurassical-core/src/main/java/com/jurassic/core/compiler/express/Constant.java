package com.jurassic.core.compiler.express;

import com.jurassic.core.compiler.ParserException;

/**
 * ��������
 * ����3�ࣺ��ֵ���ַ�����ʹ�õ����ţ�������ֵ
 *
 * @author yzhu
 */
public class Constant {

    public static Object generateData(String text) throws ParserException {
        if ("true".equalsIgnoreCase(text)) {
            return true;
        } else if ("false".equalsIgnoreCase(text)) {
            return false;
        } else if (text.charAt(0) == '\'') {
            return text.substring(1, text.length() - 1);
        } else {
            // �Ƚ����Ƿ���integer
            try {
                return Integer.parseInt(text);
            } catch (Throwable ignored) {}
            // �ټ���Ƿ��ǳ�����
            if (text.charAt(text.length() - 1) == 'L') {
                // �����ǳ�����
                try {
                    return Long.parseLong(text.substring(0, text.length() - 1));
                } catch (Throwable ex) {
                    return text;
                }
            } else {
                // һ�������ַ��������������ַ���������ʾ���ı�
                // ���Ǳ�ʾһЩ�������Ƶ�
                return text;
            }
        }
    }
}

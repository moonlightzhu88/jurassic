package com.jurassic.core.compiler.express;

import com.jurassic.core.compiler.ParserException;

/**
 * 常量定义
 * 包括3类：数值，字符串（使用单引号），布尔值
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
            // 先解析是否是integer
            try {
                return Integer.parseInt(text);
            } catch (Throwable ignored) {}
            // 再检查是否是长整形
            if (text.charAt(text.length() - 1) == 'L') {
                // 可能是长整形
                try {
                    return Long.parseLong(text.substring(0, text.length() - 1));
                } catch (Throwable ex) {
                    return text;
                }
            } else {
                // 一律视作字符串，但是这种字符串并不表示纯文本
                // 而是表示一些属性名称等
                return text;
            }
        }
    }
}

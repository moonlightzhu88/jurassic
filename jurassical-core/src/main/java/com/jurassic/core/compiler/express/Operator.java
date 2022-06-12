package com.jurassic.core.compiler.express;

/**
 * 操作符
 *
 * @author yzhu
 */
public enum Operator {
    NEG("-", 2, 1),
    NOT("!", 2, 1),
    MUL("*", 3, 2),
    DIV("/", 3, 2),
    MOD("%", 3, 2),
    ADD("+", 4, 2),
    SUB("-", 4, 2),
    GREAT(">", 6, 2),
    GREATEQUAL(">=", 6, 2),
    LESS("<", 6, 2),
    LESSEQUAL("<=", 6, 2),
    EQUAL("==", 7, 2),
    NOTEQUAL("!=", 7, 2),
    AND("&&", 11, 2),
    OR("||", 12, 2),
    SIZE("size", 2, 1),
    SUBLIST("sublist", 2, 3),
    FIELD(".", 1, 2),
    ELEMENT("[", 1, 2),
    DATEFORMAT("dateformat", 2, 2),
    ;

    public static final Operator[] operators = new Operator[] {
            NEG, MUL, DIV, MOD, ADD, SUB,
            GREAT, GREATEQUAL, LESS, LESSEQUAL, EQUAL, NOTEQUAL,
            AND, OR, NOT,
            SIZE, SUBLIST,
            FIELD, ELEMENT,
            DATEFORMAT,
    };

    private final String _text;// 操作符文本
    private final int _priority;// 运算优先级,越低优先级越高
    private final int _numOfParam;// 运算数的数量

    Operator(String text, int priority, int num) {
        this._text = text;
        this._priority = priority;
        this._numOfParam = num;
    }

    public String getText() {
        return this._text;
    }

    public int getPriority() {
        return this._priority;
    }

    public int getNumOfParam() {
        return this._numOfParam;
    }

}

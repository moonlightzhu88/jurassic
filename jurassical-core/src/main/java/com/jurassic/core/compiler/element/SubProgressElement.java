package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 子流程元素
 *
 * @author yzhu
 */
public class SubProgressElement extends Element{

    private String _packageKey;// 流程所处的包
    private String _progressKey;// 流程的key
    private final List<PinElement> _inputs = new ArrayList<>();// 子流程的输入数据管脚

    public void setPackageKey(String packageKey) {
        this._packageKey = packageKey;
    }

    public String getPackageKey() {
        return this._packageKey;
    }

    public void setProgressKey(String progressKey) {
        this._progressKey = progressKey;
    }

    public String getProgressKey() {
        return this._progressKey;
    }

    public void addInput(PinElement input) {
        this._inputs.add(input);
    }

    public List<PinElement> getInputs() {
        return this._inputs;
    }

    public String toXml() {
        StringBuilder buf = new StringBuilder();

        buf.append("<sub-progress");
        if (this._name != null)
            buf.append(" name=\"").append(this._name).append("\"");
        buf.append(" package=\"").append(this._packageKey).append("\"");
        buf.append(" progress=\"").append(this._progressKey).append("\">\r\n");
        for (PinElement input : this._inputs) buf.append(input.toXml());
        buf.append("</sub-progress>\r\n");

        return buf.toString();
    }
}

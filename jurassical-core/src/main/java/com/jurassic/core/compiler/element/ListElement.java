package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表标签
 * <list>
 *     <pin .../>
 *     <pin .../>
 * </list>
 * 
 * @author yzhu
 * 
 */
public class ListElement extends PinElement {

	private final List<PinElement> _pins = new ArrayList<>();// 存储数据管脚的列表

	public void addPin(PinElement pin) {
		this._pins.add(pin);
	}

	public List<PinElement> getPins() {
		return this._pins;
	}

	public int getPinType() {
		return PinElement.T_LIST;
	}

	public List<String> getRefPins() {
		List<String> refPins = new ArrayList<>();
		for (PinElement pin : this._pins) {
			List<String> pins = pin.getRefPins();
			if (pins != null)
				refPins.addAll(pins);
		}
		return refPins.isEmpty() ? null : refPins;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<list");
		if (this._name != null)
			buf.append(" name=\"").append(this._name).append("\"");
		buf.append(">\r\n");
		for (PinElement pin : this._pins) {
			buf.append(pin.toXml());
		}
		buf.append("</list>\r\n");

		return buf.toString();
	}

}

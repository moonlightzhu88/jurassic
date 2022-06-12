package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 复合类的管脚标签
 * <composite class="...">
 *     <data .../>
 *     <list .../>
 *     ...
 * </composite>
 * 
 * @author yzhu
 * 
 */
public class CompositeElement extends PinElement {

	private String _className;// 类名
	private final List<PinElement> _pins = new ArrayList<>();

	public void setClassName(String className) {
		this._className = className;
	}

	public String getClassName() {
		return this._className;
	}

	public void addPin(PinElement pin) {
		this._pins.add(pin);
	}

	public List<PinElement> getPins() {
		return this._pins;
	}

	public int getPinType() {
		return PinElement.T_COMPOSITE;
	}

	public List<String> getRefPins() {
		List<String> refNames = new ArrayList<>();
		for (PinElement pin : this._pins) {
			List<String> refPinNames = pin.getRefPins();
			if (refPinNames != null)
				refNames.addAll(refPinNames);
		}
		return refNames.isEmpty() ? null : refNames;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<composite");
		if (this._name != null)
			buf.append(" name=\"").append(this._name).append("\"");
		buf.append(" class=\"").append(this._className).append("\">\r\n");

		for (PinElement pin : this._pins) {
			buf.append(pin.toXml());
		}

		buf.append("</composite>\r\n");

		return buf.toString();
	}

}

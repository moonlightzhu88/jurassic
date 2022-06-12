package com.jurassic.core.compiler.element;

import java.util.List;

/**
 * ø’±Í«©
 * <empty/>
 * 
 * @author yzhu
 * 
 */
public class EmptyElement extends PinElement {

	public int getPinType() {
		return PinElement.T_EMPTY;
	}

	public List<String> getRefPins() {
		return null;
	}

	public String toXml() {
		return "<empty/>\r\n";
	}

}

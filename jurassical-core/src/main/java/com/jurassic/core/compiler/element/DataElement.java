package com.jurassic.core.compiler.element;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * �������ݱ�ǩ
 * <data type="..." value="..."/>
 * 
 * @author yzhu
 * 
 */
public class DataElement extends PinElement {

	// ��������,֧�����ͣ�
	// boolean/int/long/decimal/string/date
	private String _type;
	private String _value;// ���ݵ��ı���ʾ
	private SimpleDateFormat _format;// ����date/time���͵����ݣ���ʾ����ʱ��ĸ�ʽ
	private Object _data;// ʵ�ʵ�����

	public void setType(String type) {
		this._type = type;
	}

	public String getType() {
		return this._type;
	}

	public void setValue(String value) {
		this._value = value;
	}

	public void setFormat(String format) {
		this._format = new SimpleDateFormat(format);
	}

	public Object getData() {
		if (this._data != null)
			return this._data;
		try {
			if ("boolean".equals(this._type)) {
				this._data = Boolean.valueOf(this._value);
			} else if ("int".equals(this._type)) {
				this._data = Integer.valueOf(this._value);
			} else if ("long".equals(this._type)) {
				this._data = Long.parseLong(this._value);
			} else if ("decimal".equals(this._type)) {
				this._data = new BigDecimal(this._value);
			} else if ("string".equals(this._type)) {
				this._data = this._value;
			} else if ("date".equals(this._type)) {
				this._data = this._format.parse(this._value);
			}
			return this._data;
		} catch (Throwable ex) {
			return null;
		}
	}

	public int getPinType() {
		return PinElement.T_DATA;
	}

	public List<String> getRefPins() {
		return null;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<data");
		if (this._name != null) {
			buf.append(" name=\"").append(this._name).append("\"");
		}
		buf.append(" type=\"").append(this._type).append("\"");
		if (this._format != null) {
			buf.append(" format=\"").append(this._format).append("\"");
		}
		buf.append(" value=\"").append(this._value).append("\"/>\r\n");

		return buf.toString();
	}
}

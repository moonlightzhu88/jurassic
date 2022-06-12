package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * except-table标签
 * <except-table>
 *     <entry .../>
 *     <entry .../>
 * </except-table>
 * 
 * @author yzhu
 * 
 */
public class ExceptionTableElement extends Element {

	private final List<EntryElement> _entrys = new ArrayList<>();// 所有的异常入口配置

	public void addEntry(EntryElement entry) {
		this._entrys.add(entry);
	}

	public List<EntryElement> getEntrys() {
		return this._entrys;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("<except-table>\r\n");
		for (EntryElement entry : this._entrys) {
			buf.append(entry.toXml());
		}
		buf.append("</except-table>\r\n");
		
		return buf.toString();
	}

}

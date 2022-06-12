package com.jurassic.core.progress.handler.pin.express;

import com.jurassic.core.compiler.express.Operator;
import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.handler.pin.express.date.DateFormat;
import com.jurassic.core.progress.handler.pin.express.list.Element;
import com.jurassic.core.progress.handler.pin.express.list.Size;
import com.jurassic.core.progress.handler.pin.express.list.SubList;
import com.jurassic.core.progress.handler.pin.express.logic.And;
import com.jurassic.core.progress.handler.pin.express.logic.Not;
import com.jurassic.core.progress.handler.pin.express.logic.Or;
import com.jurassic.core.progress.handler.pin.express.math.*;
import com.jurassic.core.progress.handler.pin.express.object.Field;
import com.jurassic.core.progress.handler.pin.express.relation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 表达式形式的数据管脚定义
 *
 * @author yzhu
 */
public abstract class Express extends Pin {

	public static final Map<Operator, Class<?>> expressTbl = new HashMap<>();

	static {
		expressTbl.put(Operator.NEG, Neg.class);
		expressTbl.put(Operator.NOT, Not.class);
		expressTbl.put(Operator.MUL, Mul.class);
		expressTbl.put(Operator.DIV, Div.class);
		expressTbl.put(Operator.MOD, Mod.class);
		expressTbl.put(Operator.ADD, Add.class);
		expressTbl.put(Operator.SUB, Sub.class);
		expressTbl.put(Operator.GREAT, Great.class);
		expressTbl.put(Operator.GREATEQUAL, GreateEqual.class);
		expressTbl.put(Operator.LESS, Less.class);
		expressTbl.put(Operator.LESSEQUAL, LessEqual.class);
		expressTbl.put(Operator.EQUAL, Equal.class);
		expressTbl.put(Operator.NOTEQUAL, NotEqual.class);
		expressTbl.put(Operator.AND, And.class);
		expressTbl.put(Operator.OR, Or.class);
		expressTbl.put(Operator.SIZE, Size.class);
		expressTbl.put(Operator.SUBLIST, SubList.class);
		expressTbl.put(Operator.FIELD, Field.class);
		expressTbl.put(Operator.DATEFORMAT, DateFormat.class);
		expressTbl.put(Operator.ELEMENT, Element.class);
	}

	protected Object _data;// 计算结果数据
	protected Pin[] _pins;// 数据管脚

	public void setPins(Pin...pins) {
		this._pins = pins;
	}

	public Object getData() {
		// 如果_data上缓存了真实数据
		if (this._data != null)
			return this._data;

		// 通过表达式计算获得真实数据并缓存在_data
		this._data = this.doExpress();
		return this._data;
	}

	protected abstract Object doExpress();
}

package com.jurassic.core.compiler.element;

import com.jurassic.core.compiler.ParserException;
import com.jurassic.core.compiler.express.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 表达式标签
 * <express text="...">
 *     <pin .../>
 *     <pin .../>
 * </express>
 * 
 * @author yzhu
 * 
 */
public class ExpressElement extends PinElement {
	
	private String _text;// 表达式文本
	private Node _root;// 表达式解析的根节点
	private final List<PinElement> _pins = new ArrayList<>();// 参数管脚
	
	public void setText(String text){
		this._text = text;
	}

	/**
	 * 搜索操作符
	 */
	private Operator getOperator(String operator, Stack<Node> dataStack) {
		if ("-".equals(operator)) {
			// -操作符单独处理，这里根据dataStack的数量区分NEG操作还是SUB操作
			if (dataStack.isEmpty())
				return Operator.NEG;
			else
				return Operator.SUB;
		}
		// 其他的操作符在列表中查找
		for (Operator oper : Operator.operators) {
			if (oper.getText().equals(operator))
				return oper;
		}
		return null;
	}

	/**
	 * 生成表达式节点
	 */
	private ExpressNode createExpressNode(Operator operator, Node... children) {
		ExpressNode node = new ExpressNode();
		node.setOperator(operator);
		node.setChilds(children);
		return node;
	}

	/**
	 * 将新的操作符压栈
	 */
	private boolean pushOperator(
			Operator operator, Stack<Node> dataStack,
			Stack<Operator> operatorStack) throws ParserException{
		while (!operatorStack.isEmpty()) {
			if (operator != null &&
					operator.getPriority() < operatorStack.peek().getPriority()) {
				// 操作符的优先级比栈顶的操作符优先级高
				// 则将操作符压栈
				operatorStack.push(operator);
				return true;
			} else {
				// 弹出栈顶的操作符和对应的操作数构成表达式，放入操作数栈
				try {
					Operator topOperator = operatorStack.pop();
					Node[] childs = new Node[topOperator.getNumOfParam()];
					for (int i = childs.length - 1; i >= 0; i--) {
						childs[i] = dataStack.pop();
					}
					if (topOperator.getText().equals(".")) {
						// .操作符需要做特殊处理，判断应为操作符还是浮点数
						if (childs[0] instanceof DataNode
								&& ((DataNode) childs[0]).getData() instanceof Integer
								&& childs[1] instanceof DataNode
								&& ((DataNode) childs[1]).getData() instanceof Integer) {
							int num1 = (Integer)((DataNode) childs[0]).getData();
							int num2 = (Integer)((DataNode) childs[0]).getData();
							BigDecimal decimal = new BigDecimal(num1 + "." + num2);
							dataStack.push(new DataNode(decimal));
						} else {
							ExpressNode newNode = this.createExpressNode(topOperator, childs);
							dataStack.push(newNode);
						}
					} else {
						ExpressNode newNode = this.createExpressNode(topOperator, childs);
						dataStack.push(newNode);
					}
				} catch (Throwable ex) {
					throw new ParserException("invalid <express/>");
				}
			}
		}
		// 将操作符栈内的操作符全部pop
		return false;
	}

	/**
	 * 根据分词解析表达式
	 */
	private Node[] parseNode(List<String> words) throws ParserException{
		Stack<Node> dataStack = new Stack<>();// 操作数栈
		Stack<Operator> operatorStack = new Stack<>();// 操作符栈
		Iterator<String> it = words.iterator();
		while (it.hasNext()) {
			String word = it.next();
			// 每一个单词有3类：操作数，操作符，分隔符：(),
			if ("(".equals(word)) {
				// 找到与其相匹配的）对两者之间的words进行再次解析
				// 对应的右括号将不被后续操作处理
				int numOfKuoHao = 1;
				List<String> subWords = new ArrayList<>();
				while (it.hasNext()) {
					String subWord = it.next();
					if ("(".equals(subWord)) {
						subWords.add(subWord);
						numOfKuoHao++;
					} else if (")".equals(subWord)) {
						numOfKuoHao--;
						if (numOfKuoHao == 0) {
							break;
						} else {
							subWords.add(subWord);
						}
					} else {
						subWords.add(subWord);
					}
				}
				// 对subWords进行解析，生成若干个（数据）节点
				// 因为括号内可能含有逗号，因此可能产生若干个节点
				Node[] subNodes = this.parseNode(subWords);
				for (Node subNode : subNodes)
					dataStack.push(subNode);
			} else if (",".equals(word)) {
				// 清空当前的操作符栈，组成一个表达式节点，并重新压入操作数栈
				this.pushOperator(null, dataStack, operatorStack);
			} else if ("[".equals(word)) {
				// 列表取元素操作符
				if (!this.pushOperator(Operator.ELEMENT, dataStack, operatorStack)){
					operatorStack.push(Operator.ELEMENT);
				}
				// 找到与其相匹配的]对两者之间的words进行再次解析
				// 对应的右括号将不被后续操作处理
				int numOfKuoHao = 1;
				List<String> subWords = new ArrayList<>();
				while (it.hasNext()) {
					String subWord = it.next();
					if ("[".equals(subWord)) {
						subWords.add(subWord);
						numOfKuoHao++;
					} else if ("]".equals(subWord)) {
						numOfKuoHao--;
						if (numOfKuoHao == 0) {
							break;
						} else {
							subWords.add(subWord);
						}
					} else {
						subWords.add(subWord);
					}
				}
				// 对subWords进行解析，生成若干个（数据）节点
				Node[] subNodes = this.parseNode(subWords);
				for (Node subNode : subNodes)
					dataStack.push(subNode);
			} else {
				// 区分操作符合操作数
				Operator operator = this.getOperator(word, dataStack);
				if (operator != null) {
					// 操作符
					if (operatorStack.isEmpty()) {
						// 操作符栈空的话压栈
						operatorStack.push(operator);
					} else {
						// 将当前操作符压栈
						if (!pushOperator(operator, dataStack, operatorStack)) {
							operatorStack.push(operator);
						}
					}
				} else {
					// 操作数
					if (word.charAt(0) == '$') {
						String variableName = word.substring(1);
						// 检查变量是否使用了task的输出管脚
						int index1 = variableName.indexOf("{");
						int index2 = variableName.indexOf("}");
						if (index1 == -1) {
							// 直接引用了数据管脚
							dataStack.push(new DataNode(new Variable(variableName)));
						} else {
							String taskName = variableName.substring(0, index1);
							int index = Integer.parseInt(variableName.substring(index1 + 1, index2));
							dataStack.push(new DataNode(new Variable(taskName, index)));
						}
					} else {
						// 常量定义，数值，字符串，布尔值
						dataStack.push(new DataNode(Constant.generateData(word)));
					}
				}
			}
		}
		if (!operatorStack.isEmpty()) {
			this.pushOperator(null, dataStack, operatorStack);
		}
		return dataStack.toArray(new Node[0]);
	}

	/**
	 * 分解单词
	 */
	private List<String> splitText(String text) {
		//拆分词
		String delim = "+-*/%()[].,&|!= ><'";
		StringTokenizer token = new StringTokenizer(text, delim, true);
		boolean quota = false;// 标记是否解析字符串
		List<String> words = new ArrayList<>();
		String buf = null;// 临时缓存字符，用于解析类似>=这种组合字符
		while (token.hasMoreElements()) {
			String s = token.nextToken(delim);
			switch (s) {
				case "'":
					if (buf != null) {
						words.add(buf);
						buf = null;
					}
					quota = !quota;// 标记正在使用单引号，或者关闭单引号的解析

					if (quota) {
						// 单引号中的内容全部作为字符串常量
						words.add("'" + token.nextToken("'") + "'");
					}
					break;
				case " ":
					// 空格字符省略
					break;
				case "&":
					// &字符需要考虑解析逻辑与（&&）操作符
					if (buf == null) {
						buf = "&";// 等待解析第二个&
					} else {
						if ("&".equals(buf)) {
							words.add("&&");
							buf = null;
						} else {
							words.add(buf);
							buf = "&";
						}
					}
					break;
				case "|":
					// |字符需要考虑解析逻辑或（||）操作符
					if (buf == null) {
						buf = "|";// 等待解析第二个|
					} else {
						if ("|".equals(buf)) {
							words.add("||");
							buf = null;
						} else {
							words.add(buf);
							buf = "|";
						}
					}
					break;
				case "=":
					// =字符需要考虑解析>=,<=,==,!=4种组合操作符
					if (buf == null) {
						buf = "=";// 等待解析==操作符
					} else {
						switch (buf) {
							case "=":
								words.add("==");
								buf = null;
								break;
							case "!":
								words.add("!=");
								buf = null;
								break;
							case ">":
								words.add(">=");
								buf = null;
								break;
							case "<":
								words.add("<=");
								buf = null;
								break;
							default:
								words.add(buf);
								buf = "=";// 等待解析==操作符

								break;
						}
					}
					break;
				case "!":
					// =字符需要考虑解析!=
					if (buf != null) {
						words.add(buf);
					}
					buf = "!";
					break;
				case ">":
					// >字符需要考虑解析>=
					if (buf != null) {
						words.add(buf);
					}
					buf = ">";
					break;
				case "<":
					// <字符需要考虑解析<=
					if (buf != null) {
						words.add(buf);
					}
					buf = "<";
					break;
				default:
					if (buf != null) {
						words.add(buf);
						buf = null;
					}
					words.add(s);
					break;
			}
		}
		return words;
	}

	/**
	 * 解析表达式文本
	 */
	public void parseExpress() throws Throwable {
		List<String> words = this.splitText(this._text);
		Node[] nodes = this.parseNode(words);
		if (nodes.length != 1)
			throw new ParserException("invalid express text");
		this._root = nodes[0];
	}

	public Node getRoot() {
		return this._root;
	}

	public void addPin(PinElement pin) {
		this._pins.add(pin);
	}

	public List<PinElement> getPins() {
		return this._pins;
	}

	public int getPinType() {
		return PinElement.T_EXPRESS;
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
		
		buf.append("<express");
		if (this._name != null)
			buf.append(" name=\"").append(this._name).append("\"");
		buf.append(" text=\"").append(this._text).append("\">\r\n");
		for (PinElement pin : this._pins) buf.append(pin.toXml());
		buf.append("</express>\r\n");
		
		return buf.toString();
	}

}

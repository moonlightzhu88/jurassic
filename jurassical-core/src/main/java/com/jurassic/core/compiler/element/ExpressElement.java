package com.jurassic.core.compiler.element;

import com.jurassic.core.compiler.ParserException;
import com.jurassic.core.compiler.express.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * ���ʽ��ǩ
 * <express text="...">
 *     <pin .../>
 *     <pin .../>
 * </express>
 * 
 * @author yzhu
 * 
 */
public class ExpressElement extends PinElement {
	
	private String _text;// ���ʽ�ı�
	private Node _root;// ���ʽ�����ĸ��ڵ�
	private final List<PinElement> _pins = new ArrayList<>();// �����ܽ�
	
	public void setText(String text){
		this._text = text;
	}

	/**
	 * ����������
	 */
	private Operator getOperator(String operator, Stack<Node> dataStack) {
		if ("-".equals(operator)) {
			// -���������������������dataStack����������NEG��������SUB����
			if (dataStack.isEmpty())
				return Operator.NEG;
			else
				return Operator.SUB;
		}
		// �����Ĳ��������б��в���
		for (Operator oper : Operator.operators) {
			if (oper.getText().equals(operator))
				return oper;
		}
		return null;
	}

	/**
	 * ���ɱ��ʽ�ڵ�
	 */
	private ExpressNode createExpressNode(Operator operator, Node... children) {
		ExpressNode node = new ExpressNode();
		node.setOperator(operator);
		node.setChilds(children);
		return node;
	}

	/**
	 * ���µĲ�����ѹջ
	 */
	private boolean pushOperator(
			Operator operator, Stack<Node> dataStack,
			Stack<Operator> operatorStack) throws ParserException{
		while (!operatorStack.isEmpty()) {
			if (operator != null &&
					operator.getPriority() < operatorStack.peek().getPriority()) {
				// �����������ȼ���ջ���Ĳ��������ȼ���
				// �򽫲�����ѹջ
				operatorStack.push(operator);
				return true;
			} else {
				// ����ջ���Ĳ������Ͷ�Ӧ�Ĳ��������ɱ��ʽ�����������ջ
				try {
					Operator topOperator = operatorStack.pop();
					Node[] childs = new Node[topOperator.getNumOfParam()];
					for (int i = childs.length - 1; i >= 0; i--) {
						childs[i] = dataStack.pop();
					}
					if (topOperator.getText().equals(".")) {
						// .��������Ҫ�����⴦���ж�ӦΪ���������Ǹ�����
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
		// ��������ջ�ڵĲ�����ȫ��pop
		return false;
	}

	/**
	 * ���ݷִʽ������ʽ
	 */
	private Node[] parseNode(List<String> words) throws ParserException{
		Stack<Node> dataStack = new Stack<>();// ������ջ
		Stack<Operator> operatorStack = new Stack<>();// ������ջ
		Iterator<String> it = words.iterator();
		while (it.hasNext()) {
			String word = it.next();
			// ÿһ��������3�ࣺ�����������������ָ�����(),
			if ("(".equals(word)) {
				// �ҵ�������ƥ��ģ�������֮���words�����ٴν���
				// ��Ӧ�������Ž�����������������
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
				// ��subWords���н������������ɸ������ݣ��ڵ�
				// ��Ϊ�����ڿ��ܺ��ж��ţ���˿��ܲ������ɸ��ڵ�
				Node[] subNodes = this.parseNode(subWords);
				for (Node subNode : subNodes)
					dataStack.push(subNode);
			} else if (",".equals(word)) {
				// ��յ�ǰ�Ĳ�����ջ�����һ�����ʽ�ڵ㣬������ѹ�������ջ
				this.pushOperator(null, dataStack, operatorStack);
			} else if ("[".equals(word)) {
				// �б�ȡԪ�ز�����
				if (!this.pushOperator(Operator.ELEMENT, dataStack, operatorStack)){
					operatorStack.push(Operator.ELEMENT);
				}
				// �ҵ�������ƥ���]������֮���words�����ٴν���
				// ��Ӧ�������Ž�����������������
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
				// ��subWords���н������������ɸ������ݣ��ڵ�
				Node[] subNodes = this.parseNode(subWords);
				for (Node subNode : subNodes)
					dataStack.push(subNode);
			} else {
				// ���ֲ������ϲ�����
				Operator operator = this.getOperator(word, dataStack);
				if (operator != null) {
					// ������
					if (operatorStack.isEmpty()) {
						// ������ջ�յĻ�ѹջ
						operatorStack.push(operator);
					} else {
						// ����ǰ������ѹջ
						if (!pushOperator(operator, dataStack, operatorStack)) {
							operatorStack.push(operator);
						}
					}
				} else {
					// ������
					if (word.charAt(0) == '$') {
						String variableName = word.substring(1);
						// �������Ƿ�ʹ����task������ܽ�
						int index1 = variableName.indexOf("{");
						int index2 = variableName.indexOf("}");
						if (index1 == -1) {
							// ֱ�����������ݹܽ�
							dataStack.push(new DataNode(new Variable(variableName)));
						} else {
							String taskName = variableName.substring(0, index1);
							int index = Integer.parseInt(variableName.substring(index1 + 1, index2));
							dataStack.push(new DataNode(new Variable(taskName, index)));
						}
					} else {
						// �������壬��ֵ���ַ���������ֵ
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
	 * �ֽⵥ��
	 */
	private List<String> splitText(String text) {
		//��ִ�
		String delim = "+-*/%()[].,&|!= ><'";
		StringTokenizer token = new StringTokenizer(text, delim, true);
		boolean quota = false;// ����Ƿ�����ַ���
		List<String> words = new ArrayList<>();
		String buf = null;// ��ʱ�����ַ������ڽ�������>=��������ַ�
		while (token.hasMoreElements()) {
			String s = token.nextToken(delim);
			switch (s) {
				case "'":
					if (buf != null) {
						words.add(buf);
						buf = null;
					}
					quota = !quota;// �������ʹ�õ����ţ����߹رյ����ŵĽ���

					if (quota) {
						// �������е�����ȫ����Ϊ�ַ�������
						words.add("'" + token.nextToken("'") + "'");
					}
					break;
				case " ":
					// �ո��ַ�ʡ��
					break;
				case "&":
					// &�ַ���Ҫ���ǽ����߼��루&&��������
					if (buf == null) {
						buf = "&";// �ȴ������ڶ���&
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
					// |�ַ���Ҫ���ǽ����߼���||��������
					if (buf == null) {
						buf = "|";// �ȴ������ڶ���|
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
					// =�ַ���Ҫ���ǽ���>=,<=,==,!=4����ϲ�����
					if (buf == null) {
						buf = "=";// �ȴ�����==������
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
								buf = "=";// �ȴ�����==������

								break;
						}
					}
					break;
				case "!":
					// =�ַ���Ҫ���ǽ���!=
					if (buf != null) {
						words.add(buf);
					}
					buf = "!";
					break;
				case ">":
					// >�ַ���Ҫ���ǽ���>=
					if (buf != null) {
						words.add(buf);
					}
					buf = ">";
					break;
				case "<":
					// <�ַ���Ҫ���ǽ���<=
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
	 * �������ʽ�ı�
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

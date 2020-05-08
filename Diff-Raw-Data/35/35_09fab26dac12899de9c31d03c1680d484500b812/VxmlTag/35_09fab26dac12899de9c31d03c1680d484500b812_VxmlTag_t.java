 package cudl;
 
 import static cudl.utils.Utils.getNodeAttributeValue;
 import static cudl.utils.VxmlElementType.isFormItem;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.mozilla.javascript.EcmaError;
 import org.mozilla.javascript.Undefined;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import cudl.script.InterpreterVariableDeclaration;
 import cudl.utils.Utils;
 import cudl.utils.VxmlElementType;
 
 abstract class VxmlTag {
 	protected final Node node;
 
 	VxmlTag(Node node) {
 		this.node = node;
 	}
 
 	protected boolean checkCond(Node node, InterpreterContext context) throws IOException {
 		String cond = getNodeAttributeValue(node, "cond");
 		return cond == null || Boolean.valueOf(context.getDeclaration().evaluateScript(cond, InterpreterVariableDeclaration.ANONYME_SCOPE) + "");
 	}
 
 	public abstract Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException;
 }
 
 abstract class NonTerminalTag extends VxmlTag {
 	protected List<VxmlTag> childs;
 
 	NonTerminalTag(Node node) {
 		super(node);
 		createChilds(node.getChildNodes());
 	}
 
 	private void createChilds(NodeList nodelist) {
 		childs = new ArrayList<VxmlTag>();
 		for (int i = 0; i < nodelist.getLength(); i++) {
 			childs.add(TagInterpreterFactory.getTagInterpreter(nodelist.item(i)));
 		}
 	}
 
 }
 
 class FormTag extends VxmlTag {
 	private static final String initChild = "var script";
 	private int form_item_generated = 0;
 	private String formItems = "block transfer subdialog field";
 	private boolean initVar = true;
 
 	public FormTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		// Phase initialization
 		if (initVar) {
 			NodeList childs = node.getChildNodes();
 			for (int i = 0; i < childs.getLength(); i++) {
 				Node child = childs.item(i);
 				String nodeName = child.getNodeName();
 				if (initChild.contains(nodeName)) {
 					TagInterpreterFactory.getTagInterpreter(child).interpret(context);
 				} else if (isFormItem(child)) {
 					String name = getNodeAttributeValue(child, "name");
 					String expr = getNodeAttributeValue(child, "expr");
 					if (name == null) {
 						name = "form_item_generated_name_by_cudl_" + form_item_generated++;
 					}
 					context.getDeclaration().declareVariable(name, expr == null ? "undefined" : expr, 60);
 					// if (isInputItem(child) || "initial".equals(getName(child))) {
 					// promptCounter.put(name, 1);
 					// }
 					context.addFormItemName(child, name);
 				}
 			}
 		}
 
 		while (true) {
 			Node formItem = selectNextFormItem(context);
 			if (formItem == null) {
 				context.setHangup(true);
 				System.err.println("SORTIE");
 				break;
 			}
 			context.setSelectedFormItem(formItem);
 			activateGrammar(context);
 
 			if (formItems.contains(formItem.getNodeName())) {
 				try {
 					TagInterpreterFactory.getTagInterpreter(formItem).interpret(context);
 					context.getDeclaration().resetScopeBinding(InterpreterVariableDeclaration.ANONYME_SCOPE);
 				} catch (ExitException e) {
 					context.setHangup(true);
 					return null;
 				} catch (GotoException e) {
 					context.setNextItemToVisit(e.nextItem);
 					if (e.next != null) {
 						context.getDeclaration().resetScopeBinding(InterpreterVariableDeclaration.DIALOG_SCOPE);
 						throw new GotoException(e.next, null);
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private void activateGrammar(InterpreterContext context) {
 		List<Node> grammarActive = new ArrayList<Node>();
 		if (VxmlElementType.isInputItem(context.getSelectedFormItem()))
 			if (VxmlElementType.isAModalItem(context.getSelectedFormItem())) {
 				grammarActive.add(Utils.serachItem(context.getSelectedFormItem(), "grammar"));
 			} else {
 				Node parent = context.getSelectedFormItem();
 				while (null != parent) {
 					Node serachItem = Utils.serachItem(parent, "grammar");
 					if (null != serachItem) {
 						grammarActive.add(serachItem);
 					}
 					parent = parent.getParentNode();
 				}
 			}
 		context.setGrammarActive(grammarActive);
 	}
 
 	Node selectNextFormItem(InterpreterContext context) {
 		Node node = null;
 		if (context.getNextItemToVisit() != null) {
 			node = Utils.searchItemByName(context.getCurrentDialog(), context.getNextItemToVisit());
 			context.setNextItemToVisit(null);
 		} else {
 			for (Iterator<Entry<Node, String>> iterator = context.getFormItemNames().entrySet().iterator(); iterator.hasNext();) {
 				Entry<Node, String> next = iterator.next();
 				Node nextToVisit = next.getKey();
 				String name = next.getValue();
 				String cond = Utils.getNodeAttributeValue(nextToVisit, "cond");
 				if (cond == null || (Boolean) context.getDeclaration().evaluateScript(cond, 50))
 					if (context.getDeclaration().getValue(name).equals(Undefined.instance)) {
 						return nextToVisit;
 					}
 			}
 		}
 		return node;
 	}
 
 	public void setInitVar(boolean initVar) {
 		this.initVar = initVar;
 	}
 }
 
 class MenuTag extends VxmlTag {
 	private String execute = "prompt";
 
 	public MenuTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		context.setSelectedFormItem(node);
 		NodeList childs = node.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node item = childs.item(i);
 			if (execute.contains(item.getNodeName())) // FIXME: remove
 				TagInterpreterFactory.getTagInterpreter(item).interpret(context);
 		}
 		return null;
 	}
 }
 
 class VarTag extends VxmlTag {
 	private String parentTag = "block catch error filled form help if nomatch vxml noinput vxml";
 
 	public VarTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws EventException {
 		if (parentTag.contains(node.getParentNode().getNodeName())) {
 			String name = Utils.getNodeAttributeValue(node, "name");
 			String[] split = name.split("\\.");
 			if (Utils.scopeNames().contains(split[0]) && split.length > 1) {
 				throw new EventException("error.semantic");
 			}
 			if (!context.getParams().contains(name)) {
 				String expr = Utils.getNodeAttributeValue(node, "expr");
 				context.getDeclaration().declareVariable(name, expr == null ? "undefined" : expr, InterpreterVariableDeclaration.ANONYME_SCOPE);
 			}
 		}
 		return null;
 	}
 }
 
 class AssignTag extends VxmlTag {
 	// private String parentTag =
 	// "block catch error filled form help if nomatch noinput";
 
 	public AssignTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws EventException {
 		String name = getNodeAttributeValue(node, "name");
 		String expr = getNodeAttributeValue(node, "expr");
 		try {
 			context.getDeclaration().getValue(name); // Dont'n remove this, he
 			// check variable declaration
 			context.getDeclaration().setValue(name, expr);
 		} catch (EcmaError error) {
 			throw new EventException("error.semantic");
 		}
 		return null;
 	}
 }
 
 class LogTag extends VxmlTag {
 	public LogTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		Log log = new Log();
 
 		String label = getNodeAttributeValue(node, "label");
 		String expr = getNodeAttributeValue(node, "expr");
 		System.err.print("LOG:");
 		if (label != null) {
 			log.label = label;
 			System.err.print(" " + label + " ");
 		}
 
 		log.value += expr == null ? "" : context.getDeclaration().evaluateScript(expr, 50) + " ";
 
 		NodeList childs = node.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node child = childs.item(i);
 			log.value += TagInterpreterFactory.getTagInterpreter(child).interpret(context);
 		}
 		log.value = log.value.trim();
 		System.err.println(log.value);
 
 		context.getLogs().add(log);
 		return null;
 	}
 }
 
 class PromptTag extends VxmlTag {
 	String prompt = "audio";
 
 	public PromptTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		if (!checkCond(node, context)) {
 			return null;
 		}
 
 		Prompt p = new Prompt();
 		String timeout = getNodeAttributeValue(node, "timeout");
 		p.timeout = timeout != null ? timeout : "";
 
 		String bargein = getNodeAttributeValue(node, "bargein");
 		p.bargein = bargein != null ? bargein : "";
 
 		String bargeinType = getNodeAttributeValue(node, "bargeintype");
 		p.bargeinType = bargeinType != null ? bargeinType : "";
 
 		NodeList childs = node.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node child = childs.item(i);
 			if (prompt.contains(child.getNodeName())) {
 				Prompt pc = (Prompt) TagInterpreterFactory.getTagInterpreter(child).interpret(context);
 				p.audio += pc.audio + " ";
 				p.tts += pc.tts + " ";
 			} else {
 				p.tts += TagInterpreterFactory.getTagInterpreter(child).interpret(context);
 			}
 		}
 
 		p.tts = p.tts.trim();
 		p.audio = p.audio.trim();
 		context.addPrompt(p);
 		return null;
 	}
 }
 
 class ExitTag extends VxmlTag {
 	public ExitTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws ExitException, EventException {
 		if (node.getAttributes().getLength() > 1)
 			throw new EventException("error.badfetch");
 		context.setHangup(true);
 		throw new ExitException();
 	}
 }
 
 class ClearTag extends VxmlTag {
 	public ClearTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws EventException {
 		String namelist = getNodeAttributeValue(node, "namelist");
 		if (namelist != null) {
 			StringTokenizer tokenizer = new StringTokenizer(namelist);
 			while (tokenizer.hasMoreElements()) {
 				String name = (String) tokenizer.nextElement();
 				try {
 					context.getDeclaration().getValue(name);
 				} catch (Exception e) {
 					throw new EventException("error.semantic");
 				}
 				context.getDeclaration().setValue(name, "undefined");
 			}
 		} else {
 			for (Iterator<Map.Entry<Node, String>> iterator = context.getFormItemNames().entrySet().iterator(); iterator.hasNext();) {
 				Map.Entry<Node, String> FormItem = (Map.Entry<Node, String>) iterator.next();
 				context.getDeclaration().setValue(FormItem.getValue(), "undefined");
 			}
 		}
 
 		return null;
 	}
 }
 
 class IfTag extends VxmlTag {
 	public IfTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws IOException, InterpreterException, SAXException, ParserConfigurationException {
 		boolean conditionChecked = checkCond(node, context);
 		NodeList childs = node.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node child = childs.item(i);
 			if (VxmlElementType.isAnExecutableItem(child)) {
 				if (conditionChecked) {
 					TagInterpreterFactory.getTagInterpreter(child).interpret(context);
 				}
 			} else if (VxmlElementType.isConditionalItem(child)) {
 				if (conditionChecked) {
 					break;
 				} else {
 					conditionChecked = this.checkCond(child, context);
 				}
 			}
 		}
 		return null;
 	}
 }
 
 class TextTag extends VxmlTag {
 	public TextTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) {
 		String textContent = node.getTextContent().replaceAll("[\\s][\\s]+", " ");
 		if (VxmlElementType.isFormItem(node.getParentNode()) && !textContent.trim().equals("")) {
 			Prompt p = new Prompt();
 			p.tts += textContent.trim();
 			context.addPrompt(p);
 		}
 		return textContent;
 	}
 }
 
 class CommentTag extends VxmlTag {
 	public CommentTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) {
 		// TODO: modifie prompt and log to return null
 		return "";
 	}
 }
 
 class GotoTag extends VxmlTag {
 	public GotoTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException {
 		String nextItemTmp = getNodeAttributeValue(node, "nextitem");
 		String exprItemTmp = getNodeAttributeValue(node, "expritem");
 
 		String nextItem;
 		if (nextItemTmp != null && exprItemTmp != null) {
 			System.err.println("BADFETCH ERROR DURING GOTO : goto must define once of nextitem or expritem");
 			throw new EventException("error.badfetch");
 		} else {
 			nextItem = nextItemTmp != null ? nextItemTmp : exprItemTmp != null ? context.getDeclaration().getValue(exprItemTmp) + "" : null;
 		}
 
 		if (nextItem != null)
 			throw new GotoException(null, nextItem);
 
 		String nextTmp = getNodeAttributeValue(node, "next");
 		String exprTmp = getNodeAttributeValue(node, "expr");
 
 		String next;
 		if (nextTmp != null && exprTmp != null) {
 			System.err.println("BADFETCH ERROR DURING GOTO : goto must define once of next or expr");
 			throw new EventException("error.badfetch");
 		} else {
 			next = nextTmp != null ? nextTmp : context.getDeclaration().getValue(exprTmp) + "";
 		}
 		throw new GotoException(next, null);
 	}
 }
 
 class SubmitTag extends VxmlTag {
 	public SubmitTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException {
 		String next = getNodeAttributeValue(node, "next");
 		String expr = getNodeAttributeValue(node, "expr");
 		if (next != null && expr != null) {
 			System.err.println("Badfecth during submit");
 			throw new EventException("error.badfetch");
 		}
 
 		next = next != null ? next : context.getDeclaration().getValue(expr) + "";
 
 		String nameList = getNodeAttributeValue(node, "namelist");
 		if (nameList != null) {
 			StringTokenizer tokenizer = new StringTokenizer(nameList);
 			String urlSuite = "?";
 			while (tokenizer.hasMoreElements()) {
 				String data = tokenizer.nextToken();
 				urlSuite += data + "=" + context.getDeclaration().getValue(data) + "&";
 			}
 			next += urlSuite;
 		}
 		throw new SubmitException(next);
 	}
 }
 
 class ReturnTag extends VxmlTag {
 	public ReturnTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException {
 		String namelist = getNodeAttributeValue(node, "namelist");
 		String event = getNodeAttributeValue(node, "event");
 		String eventexpr = getNodeAttributeValue(node, "eventexpr");
 		if ((namelist != null && (event != null || eventexpr != null)) || (event != null && eventexpr != null))
 			throw new EventException("error.badfetch");
 		// TODO: check parent is an subdialog ==> throw semantic error
 		throw new ReturnException(event, eventexpr, namelist);
 	}
 }
 
 class DisconnectTag extends VxmlTag {
 	public DisconnectTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException {
 		throw new EventException("connection.disconnect.hangup");
 	}
 }
 
 class ValueTag extends VxmlTag {
 	String prompt = "block catch";
 
 	public ValueTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) {
 		Object value = context.getDeclaration().getValue(getNodeAttributeValue(node, "expr"));
 		if (prompt.contains(node.getParentNode().getNodeName())) {
 			Prompt p = new Prompt();
 			p.tts = value + "";
 			context.getPrompts().add(p);
 		}
 		return value;
 	}
 }
 
 class ThrowTag extends VxmlTag {
 	public ThrowTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException {
 		NamedNodeMap attributes = node.getAttributes();
 		if (attributes.getNamedItem("event") != null && attributes.getNamedItem("eventexpr") != null) {
 			throw new EventException("error.badfetch");
 		}
 		throw new EventException(getNodeAttributeValue(node, "event"));
 	}
 }
 
 class ScriptTag extends VxmlTag {
 	public ScriptTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws IOException, InterpreterException {
 		String src = getNodeAttributeValue(node, "src");
 		if (src != null && node.hasChildNodes())
 			throw new EventException("error.badfetch");
 
 		if (src == null) {
 			context.getDeclaration().evaluateScript(node.getTextContent(), InterpreterVariableDeclaration.DIALOG_SCOPE);
 		} else {
 			try {
 				context.getDeclaration().evaluateFileScript(src, InterpreterVariableDeclaration.DIALOG_SCOPE);
 			} catch (FileNotFoundException e) {
 				throw new EventException("error.badfetch");
 			}
 
 		}
 		return null;
 	}
 }
 
 class SubdialogTag extends VxmlTag {
 	public SubdialogTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		InterpreterVariableDeclaration declaration = context.getDeclaration();
         String formItemName = context.getFormItemNames().get(node);
         declaration.setValue(formItemName, "true");
 		String src = getNodeAttributeValue(node, "src");
 		InternalInterpreter internalInterpreter = null;
 		if (src != null) {
 			String url = Utils.tackWeelFormedUrl(context.getCurrentFileName(), src);
 			
             //if remote url, put namelist values in GET parameters
 			if (! url.startsWith("#")) {
 			    String nameList = getNodeAttributeValue(node, "namelist");
 			    if (nameList != null) {
 			        StringTokenizer tokenizer = new StringTokenizer(nameList);
 			        String urlSuite = "?";
 			        while (tokenizer.hasMoreElements()) {
 			            String data = tokenizer.nextToken();
 			            urlSuite += data + "=" + context.getDeclaration().getValue(data) + "&";
 			        }
 			        url += urlSuite;
 			    }
 			}
 
 			
             //TODO put namelist values in child context
 
			internalInterpreter = new InternalInterpreter(new InterpreterContext(url, context.cookies));
 
 			declareParams(internalInterpreter, node.getChildNodes(), context);
 			internalInterpreter.interpret(1, null);
 			context.getLogs().addAll(internalInterpreter.getContext().getLogs());
 			context.getPrompts().addAll(internalInterpreter.getContext().getPrompts());
 		}
 		context.getDeclaration().evaluateScript(formItemName + "=new Object();", InterpreterVariableDeclaration.DIALOG_SCOPE);
 		
 		if (internalInterpreter != null) {
 			String[] returnValue = internalInterpreter.getContext().getReturnValue();
 
 			System.err.println("return value *" + returnValue[0] + "* *" + returnValue[1] + "*  *" + returnValue[2] + "*");
 			String namelist = returnValue[2];
 			if (namelist != null) {
 				StringTokenizer tokenizer = new StringTokenizer(namelist);
 				while (tokenizer.hasMoreElements()) {
 					String variable = tokenizer.nextToken();
 					InterpreterVariableDeclaration declaration2 = internalInterpreter.getContext().getDeclaration();
 					context.getDeclaration().evaluateScript(
 							formItemName + "." + variable + "='" + declaration2.getValue(variable) + "'",
 							InterpreterVariableDeclaration.ANONYME_SCOPE);
 				}
 			} else if (returnValue[0] != null) {
 				throw new EventException(returnValue[0]);
 			}
 
 			Node filled = Utils.serachItem(node, "filled");
 			if (filled != null) {
 				FilledTag tagInterpreter = (FilledTag) TagInterpreterFactory.getTagInterpreter(filled);
 				tagInterpreter.setExecute(true);
 				tagInterpreter.interpret(context);
 			}
 		}
 
 		return null;
 	}
 
 	private void declareParams(InternalInterpreter internalInterpreter, NodeList childNodes, InterpreterContext context) {
 		for (int i = 0; i < childNodes.getLength(); i++) {
 			Node item = childNodes.item(i);
 			if (item.getNodeName().equals("param")) {
 				String name = getNodeAttributeValue(item, "name");
 				String value = getNodeAttributeValue(item, "expr");
 				internalInterpreter.getContext().addParam(name);
 				Object evaluateScript = context.getDeclaration().evaluateScript(value, 50);
 				internalInterpreter.getContext().getDeclaration().declareVariable(name, "'" + evaluateScript + "'", 50);
 			}
 		}
 	}
 }
 
 class TransferTag extends VxmlTag {
 	public TransferTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException {
 		String dest = getNodeAttributeValue(node, "dest");
 		String destExpr = getNodeAttributeValue(node, "destexpr");
 		context.setTransfertDestination((dest != null) ? dest : context.getDeclaration().evaluateScript(destExpr, 50) + "");
 		throw new TransferException();
 	}
 }
 
 class FieldTag extends VxmlTag {
 	private String tags = "prompt var assign if goto submit filled";
 
 	public FieldTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		NodeList childs = node.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node child = childs.item(i);
 			if (tags.contains((child.getNodeName())))
 				TagInterpreterFactory.getTagInterpreter(child).interpret(context);
 		}
 		return null;
 	}
 }
 
 class AudioTag extends VxmlTag {
 	public AudioTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException {
 		Prompt p = new Prompt();
 		String src = getNodeAttributeValue(node, "src");
 		String expr = getNodeAttributeValue(node, "expr");
 
 		if ((src == null && expr == null) || (src != null && expr != null)) {
 			throw new EventException("error.badfetch");
 		}
 
 		p.audio = src == null ? context.getDeclaration().evaluateScript(expr, 50) + "" : src;
 		p.tts = node.getTextContent();
 
 		if (node.getParentNode().getNodeName().equals("block")) {
 			context.addPrompt(p);
 		}
 		return p;
 	}
 }
 
 class EnumerateTag extends VxmlTag {
 	public EnumerateTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException {
 		String enumearation = "";
 		NodeList childNodes = context.getSelectedFormItem().getChildNodes();
 		for (int i = 0; i < childNodes.getLength(); i++) {
 			Node item = childNodes.item(i);
 			if (item.getNodeName().equals("choice")) {
 				enumearation += item.getTextContent().trim() + "; ";
 			}
 		}
 		return enumearation;
 	}
 
 }
 
 class RepromptTag extends VxmlTag {
 
 	public RepromptTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		return null;
 	}
 }
 
 class ProceduralsTag extends NonTerminalTag {
 
 	ProceduralsTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		for (VxmlTag tag : childs) {
 			tag.interpret(context);
 		}
 		return null;
 	}
 }
 
 class BlockTag extends ProceduralsTag {
 	public BlockTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		context.getDeclaration().setValue(context.getFormItemNames().get(node), "true");
 		return super.interpret(context);
 	}
 }
 
 class FilledTag extends ProceduralsTag {
 	private boolean execute = false;
 
 	public FilledTag(Node node) {
 		super(node);
 	}
 
 	@Override
 	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException, ParserConfigurationException {
 		if (!execute)
 			throw new FilledException(node.getParentNode());
 		return super.interpret(context);
 	}
 
 	public void setExecute(boolean execute) {
 		this.execute = execute;
 	}
 }
 
 class NoinputTag extends ProceduralsTag {
 	public NoinputTag(Node node) {
 		super(node);
 	}
 }
 
 class CatchTag extends ProceduralsTag {
 	public CatchTag(Node node) {
 		super(node);
 	}
 }
 
 class NomatchTag extends ProceduralsTag {
 	public NomatchTag(Node node) {
 		super(node);
 	}
 }

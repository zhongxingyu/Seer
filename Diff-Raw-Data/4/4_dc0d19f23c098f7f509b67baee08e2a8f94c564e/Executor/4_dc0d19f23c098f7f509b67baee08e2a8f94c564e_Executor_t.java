 package cudl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import org.mozilla.javascript.EcmaError;
 
 import cudl.node.Assign;
 import cudl.node.Audio;
 import cudl.node.Block;
 import cudl.node.Catch;
 import cudl.node.Choice;
 import cudl.node.Clear;
 import cudl.node.Disconnect;
 import cudl.node.Else;
 import cudl.node.Elseif;
 import cudl.node.Exit;
 import cudl.node.Filled;
 import cudl.node.Form;
 import cudl.node.Goto;
 import cudl.node.If;
 import cudl.node.Log;
 import cudl.node.Prompt;
 import cudl.node.Return;
 import cudl.node.Script;
 import cudl.node.Submit;
 import cudl.node.Text;
 import cudl.node.Throw;
 import cudl.node.Value;
 import cudl.node.Var;
 import cudl.node.Voice;
 import cudl.node.VoiceXmlNode;
 import cudl.script.Scripting;
 
 public class Executor {
 	private final Scripting scripting;
 	private final SystemOutput voiceXTTOutPut;
 	private DocumentAcces documentAcces;
 	private Logger logger = Logger.getLogger("Executor");
 	
 	public Executor(Scripting scripting, SystemOutput voiceXTTOutPut, DocumentAcces documentAcces) {
 		this.scripting = scripting;
 		this.voiceXTTOutPut = voiceXTTOutPut;
 		this.documentAcces = documentAcces;
 	}
 
 	public Object execute(VoiceXmlNode child) throws InterpreterException {
 		try {
 			return Executor.class.getMethod("execute", child.getClass()).invoke(this, child);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			if (e.getCause() instanceof InterpreterException) {
 				throw (InterpreterException) e.getCause();
 			}
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (NoSuchMethodException e) {
 			if (!(child instanceof Else || child instanceof Elseif)) {
 				throw new RuntimeException("No implementation for Executor.execute(" + child.getClass().getSimpleName() + " "
 						+ child.getNodeName() + ")");
 			}
 		}
		return null;
 	}
 
 	public Object execute(Goto goto1) throws InterpreterException {
 		if (goto1.getNextItem() != null) {
 			logger.info("goto " + goto1.getNextItem());
 			throw new FormItemChangeException(goto1.getNextItem());
 		} else if (goto1.getExpritem() != null) {
 			String nextItem = scripting.eval(goto1.getExpritem()).toString();
 			logger.info("goto " + nextItem);
 			throw new FormItemChangeException(nextItem);
 		}
 		if (goto1.getNext() != null) {
 			if (goto1.getNext().startsWith("#")) {
 				logger.info("goto " + goto1.getNext().split("#")[1]);
 				throw new DialogChangeException(goto1.getNext().split("#")[1]);
 			} else {
 				logger.info("goto " + goto1.getNext());
 				throw new DocumentChangeException(goto1.getNext(), null);
 			}
 		} else if (goto1.getExpr() != null) {
 			String next = scripting.eval(goto1.getExpr()).toString();
 			if (next.startsWith("#")) {
 				logger.info("goto " + next.split("#")[1]);
 				throw new DialogChangeException(next.split("#")[1]);
 			} else {
 				logger.info("goto " + next);
 				throw new DocumentChangeException(next, null);
 			}
 		}
 		throw new RuntimeException("badfetch error");
 	}
 
 	public Object execute(Exit Exit) throws InterpreterException {
 		throw new ExitException();
 	}
 
 	public Object execute(Log log) throws InterpreterException {
 		String debug = "";
 		String label = log.getLabel();
 		String expr = log.getExpr();
 
 		if (label != null) {
 			debug += "[" + label + "] ";
 		}
 
 		if (expr != null) {
 			debug += scripting.eval(expr);
 		}
 
 		for (VoiceXmlNode node : log.getChilds()) {
 			debug += execute(node);
 		}
 		voiceXTTOutPut.addLog(debug.trim());
 		return null;
 	}
 
 	public Object execute(Return return1) throws InterpreterException {
 		throw new ReturnException(return1);
 	}
 
 	public Object execute(Choice choice) throws InterpreterException {
 		if (choice.getAttribute("event") != null) {
 			InterpreterEventHandler.doEvent(choice, this, choice.getAttribute("event"), 1);
 			return null;
 		}
 		throw new DocumentChangeException(choice.getAttribute("next"), null);
 	}
 
 	public Object execute(Disconnect disconnect) throws InterpreterException {
 		throw new RuntimeException("implement Disconnect executor");
 	}
 
 	public Object execute(Submit submit) throws InterpreterException {
 		throw new DocumentChangeException(submit.getAttribute("next"), submit.getAttribute("method"));
 	}
 
 	public Object execute(Audio audio) {
 		cudl.Prompt p = new cudl.Prompt();
 		String src = audio.getSrc();
 		String expr = audio.getExpr();
 
 		p.audio = audio.getSrc() == null ? scripting.eval(expr) + "" : src;
 		p.tts = audio.getTextContent();
 
 		if (!(audio.getParent() instanceof Prompt)) {
 			voiceXTTOutPut.addPrompt(p);
 		}
 
 		return p;
 	}
 
 	public Object execute(Voice voice) {
 		cudl.Prompt p = new cudl.Prompt();
 
 		p.tts = voice.getTextContent();
 
 		if (!(voice.getParent() instanceof Prompt)) {
 			voiceXTTOutPut.addPrompt(p);
 		}
 
 		return p;
 	}
 
 	public Object execute(Var var) throws InterpreterException {
 		String name = var.getAttribute("name");
 		if (!validateName(name)) {
 			throw new SemanticException(var, "ne doi pas ");
 		}
 		String expr = var.getAttribute("expr");
 		String value = expr == null ? "undefined" : expr;
 		scripting.put(name, value);
 		logger.info("declaration " + name + " = " + value);
 		return null;
 	}
 
 	private boolean validateName(String name) {
 		return !name.startsWith("dialog.");
 	}
 
 	public Object execute(Assign assign) {
 		String name = assign.getAttribute("name");
 		String expr = assign.getAttribute("expr");
 		scripting.set(name, expr);
 		return null;
 	}
 
 	public Object execute(Script script) throws InterpreterException {
 		try {
 			if (script.getAttribute("src") != null) {
 				String scriptFileName = script.getAttribute("src");
 				return scripting.eval(getFileTextContent(scriptFileName));
 			} else {
 				return scripting.eval(script.getTextContent());
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new SemanticException(script, e.getMessage());
 		}
 	}
 
 	protected String getFileTextContent(String fileName) throws MalformedURLException, IOException {
 		URL url = new URL(documentAcces.getLastBaseUrl(), fileName);
 		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 		StringBuffer stringBuffer = new StringBuffer();
 
 		String inputLine;
 		while ((inputLine = in.readLine()) != null) {
 			stringBuffer.append(inputLine).append("\n");
 		}
 		in.close();
 
 		return stringBuffer.toString();
 	}
 
 	public Object execute(Clear clear) throws SemanticException {
 		String nameList = clear.getAttribute("namelist");
 		try {
 			if (nameList == null) {
 				for (VoiceXmlNode voiceXmlNode : getEnclosingDialog(clear).getChilds()) {
 					if (voiceXmlNode instanceof FormItem) {
 						scripting.set(((FormItem) voiceXmlNode).getName(), "undefined");
 					}
 				}
 			} else {
 				StringTokenizer tokenizer = new StringTokenizer(nameList, " ");
 				while (tokenizer.hasMoreElements()) {
 					String name = (String) tokenizer.nextElement();
 					scripting.set(name, "undefined");
 				}
 			}
 		} catch (EcmaError e) {
 			e.printStackTrace();
 			throw new SemanticException(clear, e.getMessage());
 		}
 		return null;
 	}
 
 	private VoiceXmlNode getEnclosingDialog(VoiceXmlNode node) {
 		VoiceXmlNode parent = node.getParent();
 
 		while (!(parent instanceof Form)) {
 			parent = parent.getParent();
 		}
 		return parent;
 	}
 
 	public Object execute(Text text) {
 		String value = text.getValue().replaceAll("[\\s][\\s]+", " ");
 		if (value.trim().isEmpty()) {
 			return "";
 		}
 
 		if (text.getParent() instanceof Block) {
 			cudl.Prompt p = new cudl.Prompt();
 			p.tts += value.trim();
 			voiceXTTOutPut.addPrompt(p);
 		}
 		return value;
 	}
 
 	public Object execute(Catch catch1) throws InterpreterException {
 		execute(catch1.getChilds());
 		return null;
 	}
 
 	public Object execute(Throw throw1) throws InterpreterException {
 		if (throw1.getAttribute("event") != null && throw1.getAttribute("eventexpr") != null) {
 			InterpreterEventHandler.doEvent(throw1, this, "error.badfetch", 1);
 		}
 		return null;
 	}
 
 	public Object execute(Value value) {
 		Object eval = null;
 		eval = scripting.eval(value.getExpr());
 		if (value.getParent() instanceof Block /* catch */) {
 			cudl.Prompt p = new cudl.Prompt();
 			p.tts += eval;
 			voiceXTTOutPut.addPrompt(p);
 		}
 
 		return eval;
 	}
 
 	public Object execute(Prompt prompt) throws InterpreterException {
 		cudl.Prompt p = new cudl.Prompt();
 
 		String timeout = prompt.getTimeout();
 		p.timeout = timeout != null ? timeout : "";
 
 		String bargein = prompt.getBargein();
 		p.bargein = bargein != null ? bargein : "";
 
 		String bargeinType = prompt.getBargeinType();
 		p.bargeinType = bargeinType != null ? bargeinType : "";
 
 		for (VoiceXmlNode voiceXmlNode : prompt.getChilds()) {
 			if (voiceXmlNode instanceof Audio) {
 				cudl.Prompt pa = (cudl.Prompt) execute(voiceXmlNode);
 				p.tts += pa.tts + " ";
 				p.audio += pa.audio + " ";
 			} else {
 				p.tts += execute(voiceXmlNode) + "";
 			}
 		}
 
 		p.tts = p.tts.trim();
 		p.audio = p.audio.trim();
 		voiceXTTOutPut.addPrompt(p);
 		return null;
 	}
 
 	public Object execute(Filled filled) throws InterpreterException {
 		for (VoiceXmlNode voiceXmlNode : filled.getChilds()) {
 			execute(voiceXmlNode);
 		}
 		return null;
 	}
 
 	public void execute(If if1) throws InterpreterException {
 		String cond = if1.getCond();
 
 		String string = scripting.eval(cond).toString();
 
 		if (Boolean.parseBoolean(string)) {
 			execute(getInTrueChilds(if1));
 		} else {
 			execute(getInFalseChilds(if1));
 		}
 	}
 
 	private List<VoiceXmlNode> getInFalseChilds(If if1) {
 		List<VoiceXmlNode> childsFalse = new ArrayList<VoiceXmlNode>();
 		boolean inElse = false;
 		String cond = "";
 		for (VoiceXmlNode voiceXmlNode : if1.getChilds()) {
 			if (voiceXmlNode instanceof Else || voiceXmlNode instanceof Elseif) {
 				inElse = !inElse;
 				cond = voiceXmlNode instanceof Elseif ? voiceXmlNode.getAttribute("cond") : null;
 				if (!inElse) {
 					return childsFalse;
 				}
 			}
 			if (inElse) {
 				if (cond == null || Boolean.parseBoolean(scripting.eval(cond).toString())) {
 					childsFalse.add(voiceXmlNode);
 				}
 			}
 		}
 
 		return childsFalse;
 	}
 
 	private List<VoiceXmlNode> getInTrueChilds(If if1) {
 		List<VoiceXmlNode> childsTrue = new ArrayList<VoiceXmlNode>();
 
 		for (VoiceXmlNode voiceXmlNode : if1.getChilds()) {
 			boolean el_se = voiceXmlNode instanceof Else;
 			boolean elseif = voiceXmlNode instanceof Elseif;
 			if (el_se || elseif)
 				break;
 
 			childsTrue.add(voiceXmlNode);
 		}
 		return childsTrue;
 	}
 
 	private void execute(List<VoiceXmlNode> voiceXmlNodes) throws InterpreterException {
 		for (VoiceXmlNode voiceXmlNode : voiceXmlNodes) {
 			execute(voiceXmlNode);
 		}
 	}
 }

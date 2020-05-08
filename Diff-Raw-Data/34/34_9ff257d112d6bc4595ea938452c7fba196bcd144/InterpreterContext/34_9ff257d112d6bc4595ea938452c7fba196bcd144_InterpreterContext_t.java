 package cudl;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.script.ScriptException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import cudl.event.InterpreterEvent;
 import cudl.event.InterpreterEventHandler;
 import cudl.event.InterpreterListener;
 import cudl.script.DefaultInterpreterScriptContext;
 import cudl.utils.Utils;
 
 public class InterpreterContext {
 	private Document currentdDocument;
 	private Node currentDialog;
 	private NodeList dialogs;
 	private InterpreterListener interpreterListener;
 	private String currentFileName;
 	private String currentRootFileName;
 	private String location;
 	public Interpreter interpreter;
 	public Node field;
 	public Document rootDocument;
 
 	private DocumentBuilder documentBuilder;
 	private URLConnection connection;
 
 	private String cookies;
 
 	public InterpreterContext(String fileName) throws IOException,
 			ScriptException, ParserConfigurationException, SAXException {
 		location = fileName;
 		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
 				.newInstance();
 		documentBuilder = builderFactory.newDocumentBuilder();
 		interpreter = new Interpreter(fileName);
 		connection = new URL(fileName).openConnection();
 		cookies = connection.getHeaderField("Set-Cookie");
		buildDocument(fileName);
		interpreterListener = new InterpreterEventHandler();
 	}
	
 	public void launchInterpreter() throws IOException, ScriptException,
 			SAXException {
 		try {
 			interpreter.interpretDialog(currentDialog);
 			field = interpreter.selectedItem;
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void executionHandler(InterpreterException e) throws IOException,
 			ScriptException, SAXException {
 		if (e instanceof GotoException) {
 			GotoException gotoException = (GotoException) e;
 			String next = gotoException.next;
 			interpreter.resetDialogScope();
 			if (next.startsWith("#")) {
 				currentDialog = Utils.searchDialogByName(dialogs, next.replace(
 						"#", ""));
 			} else {
 				interpreter.resetDocumentScope();
 				interpreter.selectedItem = null;
 				buildDocument(next);
 			}
 			launchInterpreter();
 		} else if (e instanceof SubmitException) {
 			interpreter.resetDocumentScope();
 			buildDocument(((SubmitException) e).next);
 			interpreter.selectedItem = null;
 			launchInterpreter();
 		} else if (e instanceof EventException) {
 			event(((EventException) e).type);
 		}
 	}
 
 	public void event(String eventType) throws ScriptException, IOException,
 			SAXException {
 		field = interpreter.selectedItem;
 		interpreterListener.doEvent(new InterpreterEvent(this, eventType));
 	}
 
 	private void buildDocument(String fileName) throws ScriptException,
 			IOException, SAXException {
 		String url = Utils.tackWeelFormedUrl(location, fileName).replaceAll(
 				"#", "%23");
 		System.err.println(url);
 
 		connection = new URL(url).openConnection();
 		connection.setRequestProperty("Cookie", cookies);
 
 		currentdDocument = documentBuilder.parse(connection.getInputStream());
 		dialogs = currentdDocument.getElementsByTagName("form");
 		currentDialog = dialogs.item(0);
 		Node appplicationRoot = currentdDocument.getElementsByTagName("vxml")
 				.item(0).getAttributes().getNamedItem("application");
 		if (null != appplicationRoot) {
 			String rootUrl = Utils.tackWeelFormedUrl(location, appplicationRoot
 					.getTextContent());
 			rootDocument = documentBuilder.parse(rootUrl);
 			declareRootScopeVariableIfNeed(rootUrl);
 		} else if (!url.equals(currentRootFileName)) {
 			interpreter.resetApplicationScope();
 		}
 		declareDocumentScopeVariableIfNeed(fileName);
 
 	}
 
 	private void declareRootScopeVariableIfNeed(String textContent)
 			throws ScriptException, IOException {
 		if (!textContent.equals(currentRootFileName)) {
 			interpreter.resetApplicationScope();
 			interpreter.declareVariable(rootDocument.getElementsByTagName(
 					"vxml").item(0).getChildNodes(),
 					DefaultInterpreterScriptContext.APPLICATION_SCOPE);
 			currentRootFileName = Utils
 					.tackWeelFormedUrl(location, textContent);
 		}
 	}
 
 	private void declareDocumentScopeVariableIfNeed(String fileName)
 			throws ScriptException, IOException {
 		if (!fileName.equals(currentFileName)) {
 			interpreter.declareVariable(currentdDocument.getElementsByTagName(
 					"vxml").item(0).getChildNodes(),
 					DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
 			currentFileName = Utils.tackWeelFormedUrl(location, fileName);
 		}
 	}
 
 	// we assume that the user is in a context where the word he utters is
 	// recognized if it is not the case we use the function nomatch
 	public void talk(String sentence) throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.utterance(sentence, "'voice'");
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void push(String dtmf) throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.utterance(dtmf, "'dtmf'");
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void destinationHangup() throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.destinationHangup();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void callerHangDestination() throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.callerHangDestination();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void blindTransferSuccess() throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.blindTransferSuccess();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void callerHangup(int i) throws IOException, ScriptException,
 			SAXException {
 		try {
 			interpreter.callerHangup(i);
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void noAnswer() throws ScriptException, IOException, SAXException {
 		try {
 
 			interpreter.noAnswer();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void maxTimeDisconnect() throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.maxTimeDisconnect();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void destinationBusy() throws ScriptException, IOException,
 			SAXException {
 		try {
 			interpreter.destinationBusy();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 
 	public void networkBusy() throws ScriptException, IOException, SAXException {
 		try {
 			interpreter.networkBusy();
 		} catch (InterpreterException e) {
 			executionHandler(e);
 		}
 	}
 }

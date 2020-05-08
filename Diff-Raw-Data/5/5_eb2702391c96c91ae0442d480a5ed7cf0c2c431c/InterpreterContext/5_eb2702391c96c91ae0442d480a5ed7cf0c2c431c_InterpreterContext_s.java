 package cudl;
 
 import static cudl.utils.Utils.tackWeelFormedUrl;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import cudl.script.InterpreterVariableDeclaration;
 import cudl.utils.Utils;
 
 class InterpreterContext {
 	private String location;
 	private final InterpreterVariableDeclaration declaration;
 	private DocumentBuilder documentBuilder;
 	private Document currentdDocument;
 	private Document rootDocument;
 	private URLConnection connection;
 	private String cookies;
 	private String currentRootFileName;
 	private String currentFileName;
 	private Node currentDialog;
 	private Node selectedFormItem;
 	private String transferDestination;
 	private boolean hangup = false;
 	private List<Node> grammarActive = new ArrayList<Node>();
 	private String[] returnValue = new String[3];
 	private List<String> params = new ArrayList<String>();
 	private List<Log> logs = new ArrayList<Log>();
 	private String nextItemToVisit;
 	private Map<Node, String> formItemNames = new LinkedHashMap<Node, String>();
 	private List<Prompt> prompts = new ArrayList<Prompt>();
 
 	InterpreterContext(String location) throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
 		this.location = location;
 		this.declaration = new InterpreterVariableDeclaration();
 		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		connection = new URL(location).openConnection();
 		cookies = connection.getHeaderField("Set-Cookie");
 		buildDocument(location);
 	}
 
 	void buildDocument(String fileName) throws IOException, SAXException {
 		String url = Utils.tackWeelFormedUrl(location, fileName);
 		System.err.println("build " + location + " + " + fileName + " url" + url);
 		connection = new URL(url).openConnection();
 		if (cookies != null)
 			connection.setRequestProperty("Cookie", cookies);
 
		try {
			currentdDocument = documentBuilder.parse(connection.getInputStream());
		} catch (Exception e) {
		}
 
 		Node vxmlTag = currentdDocument.getDocumentElement();
 		NodeList dialogs = vxmlTag.getChildNodes();
 		int lastIndexOf = url.lastIndexOf("#");
 		if (lastIndexOf > 0) {
 			currentDialog = Utils.searchDialogByName(dialogs, url.substring(lastIndexOf + 1));
 		} else {
 			for (int i = 0; i < dialogs.getLength(); i++) {
 				String nodeName = dialogs.item(i).getNodeName();
 				if (nodeName.equals("menu") || nodeName.equals("form")) {
 					currentDialog = dialogs.item(i);
 					break;
 				}
 			}
 		}
 
 		Node appplicationRoot = vxmlTag.getAttributes().getNamedItem("application");
 		if (null != appplicationRoot) {
 			String rootUrl = tackWeelFormedUrl(location, appplicationRoot.getTextContent());
 			rootDocument = documentBuilder.parse(rootUrl);
 			declareRootScopeVariableIfNeeded(rootUrl);
 		} else if (!url.equals(currentRootFileName) && currentRootFileName != null) {
 			declaration.resetScopeBinding(InterpreterVariableDeclaration.APPLICATION_SCOPE);
 		}
 
 		declareDocumentScopeVariableIfNeeded(url.split("#")[0]);
 	}
 
 	private void declareRootScopeVariableIfNeeded(String textContent) throws IOException {
 		if (!textContent.equals(currentRootFileName)) {
 			NodeList childNodes = rootDocument.getDocumentElement().getChildNodes();
 			declaration.resetScopeBinding(InterpreterVariableDeclaration.APPLICATION_SCOPE);
 			declareVariable(childNodes, InterpreterVariableDeclaration.APPLICATION_SCOPE);
 			currentRootFileName = tackWeelFormedUrl(location, textContent);
 		}
 	}
 
 	private void declareDocumentScopeVariableIfNeeded(String fileName) throws IOException {
 		if (!fileName.equals(getCurrentFileName())) {
 			System.err.println(fileName + "   sdfsdfsd");
 			NodeList childNodes = currentdDocument.getElementsByTagName("vxml").item(0).getChildNodes();
 			declaration.resetScopeBinding(InterpreterVariableDeclaration.DOCUMENT_SCOPE);
 			declareVariable(childNodes, InterpreterVariableDeclaration.DOCUMENT_SCOPE);
 			setCurrentFileName(fileName);
 			location = currentFileName;
 		}
 	}
 
 	private void declareVariable(NodeList childNodes, int scope) throws MalformedURLException, IOException {
 		for (int i = 0; i < childNodes.getLength(); i++) {
 			Node child = childNodes.item(i);
 			if (child.getNodeName().equals("var")) {
 				String name = Utils.getNodeAttributeValue(child, "name");
 				String value = Utils.getNodeAttributeValue(child, "expr");
 				System.err.println(name + "=" + value + "   documentscope");
 				declaration.declareVariable(name, value == null ? "undefined" : value, scope);
 			} else if (child.getNodeName().equals("script")) {
 				String src = Utils.getNodeAttributeValue(child, "src");
 				if (src != null) {
 					declaration.evaluateFileScript(tackWeelFormedUrl(location, src), scope);
 				} else
 					declaration.evaluateScript(child.getTextContent(), scope);
 			}
 		}
 	}
 
 	public Node getCurrentDialog() {
 		return currentDialog;
 	}
 
 	void setCurrentDialog(Node dialog) {
 		currentDialog = dialog;
 	}
 
 	Node getSelectedFormItem() {
 		return selectedFormItem;
 	}
 
 	void setSelectedFormItem(Node selectedFormItem) {
 		this.selectedFormItem = selectedFormItem;
 	}
 
 	String getLocation() {
 		return location;
 	}
 
 	InterpreterVariableDeclaration getDeclaration() {
 		return declaration;
 	}
 
 	public void setTransfertDestination(String object) {
 		this.transferDestination = object;
 	}
 
 	public String getTransferDestination() {
 		return transferDestination;
 
 	}
 
 	public void setHangup(boolean hangup) {
 		this.hangup = hangup;
 	}
 
 	public boolean isHangup() {
 		return hangup;
 	}
 
 	public Document getRootDocument() {
 		return rootDocument;
 	}
 
 	public void setGrammarActive(List<Node> grammarActive) {
 		this.grammarActive = grammarActive;
 	}
 
 	public List<Node> getGrammarActive() {
 		return grammarActive;
 	}
 
 	public void setCurrentFileName(String currentFileName) {
 		this.currentFileName = currentFileName;
 	}
 
 	public String getCurrentFileName() {
 		return currentFileName;
 	}
 
 	public void setReturnValue(String... returnValue) {
 		this.returnValue = returnValue;
 	}
 
 	public String[] getReturnValue() {
 		return returnValue;
 	}
 
 	public void addParam(String name) {
 		params.add(name);
 	}
 
 	public List<String> getParams() {
 		return params;
 	}
 
 	public List<Log> getLogs() {
 		return logs;
 	}
 
 	public void setNextItemToVisit(String nextItemAtt) {
 		nextItemToVisit = nextItemAtt;
 	}
 
 	public String getNextItemToVisit() {
 		return nextItemToVisit;
 	}
 
 	public void addFormItemName(Node node, String name) {
 		formItemNames.put(node, name);
 	}
 
 	public Map<Node, String> getFormItemNames() {
 		return formItemNames;
 	}
 
 	public void addPrompt(Prompt p) {
 		prompts.add(p);
 	}
 
 	public List<Prompt> getPrompts() {
 		return prompts;
 	}
 }

 package com.kdcloud.lib.client;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import weka.core.Instances;
 
 import com.kdcloud.lib.domain.Modality;
 import com.kdcloud.lib.domain.ServerAction;
 import com.kdcloud.lib.domain.ServerParameter;
 import com.kdcloud.lib.rest.api.ModalitiesResource;
 
 public abstract class BaseClient implements Runnable {
 
 	String baseUri;
 	Modality modality;
 	ClientResource resource;
 	Document executionLog;
 	DocumentBuilder documentBuilder;
 	XPath xpath;
 	Queue<ServerAction> queue;
 	private boolean canRun;
 	private boolean repeatAllowed;
 	private ServerAction currentAction;
 
 	public abstract void log(String message, Throwable thrown);
 
 	public abstract void log(String message);
 
 	public abstract Instances getData();
 
 	/**
 	 * returns a selected string between the ones in choices
 	 * 
 	 * @param parameterName
 	 *            a string specifing the semantic meaning of the choices
 	 * @param choices
 	 *            the possibles choice alternatives
 	 * @return the choosen alternative
 	 */
 	public abstract String handleChoice(String parameterName, String[] choices);
 
 	public abstract void report(Document view);
 
 	
 	/**
 	 * checks if a single 
 	 * @return
 	 */
 	public synchronized boolean isRepeatAllowed() {
 		return repeatAllowed;
 	}
 
 	public synchronized void setRepeatAllowed(boolean repeatAllowed) {
 		this.repeatAllowed = repeatAllowed;
 	}
 	
 
 	public BaseClient(String url) throws ParserConfigurationException {
 		this(url, null);
 	}
 
 	public BaseClient(String url, Modality modality) throws ParserConfigurationException {
 		super();
 		this.baseUri = url;
 		this.modality = modality;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		this.documentBuilder = dbf.newDocumentBuilder();
 		this.executionLog = this.documentBuilder.newDocument();
 		Element rootElement = this.executionLog.createElement("execution");
 		this.executionLog.appendChild(rootElement);
 		this.resource = new ClientResource(url);
 		XPathFactory xPathfactory = XPathFactory.newInstance();
 		this.xpath = xPathfactory.newXPath();
 		this.canRun = true;
 		this.repeatAllowed = true;
 	}
 
 	public synchronized void stopModalityExecution() {
 		canRun = false;
 	}
 
 	public synchronized void startModalityExecution() {
 		canRun = true;
 	}
 
 	public synchronized boolean canRun() {
 		return canRun;
 	}
 	
 	public synchronized Modality getModality() {
 		return modality;
 	}
 	
 	/**
 	 * changes client modality, stops the execution of the previous if any
 	 * @param modality
 	 */
 	public synchronized void setModality(Modality modality) {
 		this.canRun = false;
 		this.modality = modality;
 	}
 
 	public void setAccessToken(String token) {
 		log("setting access token");
 		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "login",
 				token);
 	}
 
 	public static List<Modality> getModalities(String url) {
 		return getModalities(url, null);
 	}
 	
 	public static List<Modality> getModalities(String url, String accessToken) {
 		ClientResource cr = new ClientResource(url + ModalitiesResource.URI);
 		if (accessToken != null)
 			cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "login",
 					accessToken);
 		return cr.wrap(ModalitiesResource.class).listModalities().asList();
 	}
 	
 	@Override
 	public void run() {
 		try {
 			executeModality();
 		} catch (Exception e) {
 			log(e.getMessage(), e);
 		}
 	}
 
 	public void executeModality() throws IOException,
 			InterruptedException {
 		startModalityExecution();
 		log("executing " + modality.getName());
 		queue = new LinkedList<ServerAction>(
 				modality.getServerCommands());
 		while (canRun() && !queue.isEmpty()) {
 			currentAction = queue.poll();
 			if (repeatAllowed && currentAction.isRepeat())
 				queue.add(new ServerAction(currentAction));
 			while (currentAction.hasParameters())
 				setActionParameter();
 			try {
 				executeAction();
 			} catch (ResourceException e) {
 				handleResourceException(resource.getStatus(), e);
 				throw new IOException(e);
 			}
 			Thread.sleep(currentAction.getSleepTimeInMillis());
 		}
 	}
 
 	public abstract void handleResourceException(Status status, ResourceException e);
 
 	protected void setActionParameter() throws IOException {
 		ServerParameter parameter = currentAction.getParams().get(0);
 		String value = null;
 		try {
 			log("executing xpath expression: " + parameter.getReference());
 			XPathExpression expr = xpath.compile(parameter.getReference());
 			NodeList result = (NodeList) expr.evaluate(executionLog,
 					XPathConstants.NODESET);
 			log("expression result length: " + result.getLength());
 			if (result.getLength() == 0)
 				throw new IOException(
 						"cannot execute request: missing parameter");
 			if (result.getLength() == 1)
 				value = result.item(0).getTextContent();
 			else
 				value = handleChoice(parameter.getName(), result);
 		} catch (XPathExpressionException e) {
 			throw new IOException(e);
 		}
 		log("setting parameter: " + parameter.getName() + ":" + value);
 		currentAction.setParameter(parameter, value);
 	}
 
 	public String handleChoice(String parameterName, NodeList result) {
 		String[] choices = new String[result.getLength()];
 		for (int i = 0; i < choices.length; i++) {
 			choices[i] = result.item(i).getTextContent();
 		}
 		return handleChoice(parameterName, choices);
 	}
 
 	protected void setResourceReference(String uri) {
 		String reference = baseUri + uri;
 		log("fetching " + reference);
 		resource.setReference(reference);
 	}
 	
 	public void retryRequest() throws ResourceException, IOException {
 		executeAction();
 	}
 
 	public void executeAction() throws IOException,
 			ResourceException {
 		setResourceReference(currentAction.getUri());
 		beforeRequest();
 		Representation entity = null;
 		switch (currentAction.getMethod()) {
 		case GET:
 			log("executing GET");
 			entity = resource.get(MediaType.APPLICATION_ALL_XML);
 			break;
 		case PUT:
 			log("executing PUT");
 			Instances data = getData();
 			Representation putRep = currentAction.getPutRepresentation(data);
 			entity = resource.put(putRep);
 			break;
 		case DELETE:
 			log("executing DELETE");
 			entity = resource.delete();
 			break;
 		case POST:
 			log("executing POST");
 			Representation postRep = currentAction.getPostRepresentation();
 			entity = resource.post(postRep);
 		default:
 			break;
 		}
 		if (!entity.isEmpty()) {
 			handleEntity(entity);
 		} else {
 			log("received entity is empty, doing nothing");
 		}
 	}
 
 	public void beforeRequest() {
 	}
 
 	protected void handleEntity(Representation entity) throws IOException {
 		log("handling entity");
 		Document lastOutput = new DomRepresentation(entity).getDocument();
 		String elementName = lastOutput.getDocumentElement().getNodeName();
 		if (elementName.contains("report")) {
 			Document view = documentBuilder.newDocument();
 			Element toImport = lastOutput.getDocumentElement();
 			view.appendChild(view.importNode(toImport, true));
 			report(view);
 		} else {
 			log("storing last output");
			Node child = executionLog.importNode(
					lastOutput.getDocumentElement(), true);
 			executionLog.getDocumentElement().appendChild(child);
 		}
 	}
 }

 package utils;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.traversal.DocumentTraversal;
 import org.w3c.dom.traversal.NodeFilter;
 import org.w3c.dom.traversal.NodeIterator;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.eviware.soapui.SoapUI;
 import com.eviware.soapui.impl.WsdlInterfaceFactory;
 import com.eviware.soapui.impl.wsdl.WsdlInterface;
 import com.eviware.soapui.impl.wsdl.WsdlOperation;
 import com.eviware.soapui.impl.wsdl.WsdlProject;
 import com.eviware.soapui.impl.wsdl.WsdlRequest;
 import com.eviware.soapui.impl.wsdl.WsdlSubmit;
 import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
 import com.eviware.soapui.model.iface.Operation;
 import com.eviware.soapui.model.iface.Response;
 
 public class Soap {
 
 
 	/**
 	 * Sends a request to the webservice specified and returns the response as
 	 * XML string
 	 * 
 	 * @param serviceUrl
 	 * @param method
 	 * @param xmlRequest
 	 * @return
 	 * @throws ServiceException
 	 */
 	public static String sendRequest(String serviceUrl, String method, String xmlRequest) throws ServiceException {
 		String result = null;
 		try {
 
 			SoapUI.setStandalone(true);
 			WsdlProject project = new WsdlProject();
 			project.setTimeout(1500);
 			
 			if (!Soap.isAvailable(serviceUrl)) {
 				throw new ServiceException("Server not reachable"); 
 			}
 			
 			WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project, serviceUrl, true)[0];
 			WsdlOperation operation = (WsdlOperation) iface.getOperationByName(method);
 			WsdlRequest request = operation.addNewRequest("Request"); 
 			request.setRequestContent(xmlRequest);
 			
 			WsdlSubmit<?> submit = (WsdlSubmit<?>) request.submit(new WsdlSubmitContext(request), false);
 			
 			Response response = submit.getResponse();
 			result = response.getContentAsString();
 			SoapUI.shutdown(); 
 			
 			
 		} catch (Exception e) {
 			throw new ServiceException(e);
 		}
 
		if (result == null) {
			throw new ServiceException("Webservice did not response to call.");
		}
		
 		return result.toString();
 	}
 	
 	/**
 	 * Returns a Map containing methods names as key
 	 * and request templates as values.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	public static Map<String, String> getRequests(String serviceUrl) throws Exception {
 		SoapUI.setStandalone(true);
 
 		if (!Soap.isAvailable(serviceUrl)) {
 			throw new IOException("Server not reachable");
 		}
 
 		HashMap<String, String> result = new HashMap<>();
 		WsdlProject project = new WsdlProject();
 		project.setTimeout(15);
 		WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, serviceUrl);
 		WsdlInterface wsdl = wsdls[0];
 		wsdl.getEndpoints();
 		for (Operation operation : wsdl.getOperationList()) {
 			WsdlOperation op = (WsdlOperation) operation;
 			result.put(op.getName(), op.createRequest(true));
 		}
 	
 		SoapUI.shutdown();
 
 		return result;
 	}
 	
 	/**
 	 * Returns a Map containing the method name as key, with
 	 * the list of parameters as value.
 	 * 
 	 * @param serviceUrl
 	 * @return
 	 * @throws Exception
 	 */
 	public static Map<String, ArrayList<String>> getMethods(String serviceUrl) throws Exception {
 		Map <String, String> requests = getRequests(serviceUrl);
 		Map<String, ArrayList<String>> result = new HashMap<>();
 		for (String method : requests.keySet()) {
 			ArrayList<String> tmp = (ArrayList<String>) getParams(requests.get(method));
 			result.put(method, tmp);
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns a list of parameters for a particular method
 	 * 
 	 * @param methodRequest
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private static List<String> getParams(String methodRequest) throws ParserConfigurationException, SAXException, IOException {
 		ArrayList<String> result = new ArrayList<>(); 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder loader = factory.newDocumentBuilder();
 		Document document = loader.parse(new InputSource(new StringReader(methodRequest)));
 
 		DocumentTraversal traversal = (DocumentTraversal) document;
 
 		NodeIterator iterator = traversal.createNodeIterator(document.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
 
 		for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
 			Element element = ((Element) n);
 			if (element.getTextContent().equals("?")) {
 				result.add(element.getTagName());
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Checks whether the given URL is available
 	 * @param url
 	 * @return
 	 */
 	public static boolean isAvailable(String url) {
 		HttpURLConnection connection;
 		try {
 			connection = (HttpURLConnection) new URL(url).openConnection();
 			connection.setRequestMethod("HEAD");
 			int responseCode = connection.getResponseCode();
 			if (responseCode != 200) {
 				return true;
 			}
 		} catch (ProtocolException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 }

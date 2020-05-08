 package eu.choreos.vv.interceptor;
 
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.xmlbeans.XmlException;

 import com.eviware.soapui.impl.wsdl.WsdlOperation;
 import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
 import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
 
 import eu.choreos.vv.common.MockProject;
import eu.choreos.vv.exceptions.WSDLException;
 
 /**
  * This class represents the WSProxy objects which are used in the message interception
  * 
  * @author Felipe Besson
  *
  */
 public class WSProxy extends MockProject {
 	
 	private String realWsdl;
 	
 	/**
 	 * Creates a WSProxy object which will be deployed in the url provided, also, the 
 	 * proxy interface is the same of the provided WSDL URI 
 	 * 
 	 * @param proxyURI
 	 * @param realWsdl
	 * @throws IOException 
	 * @throws XmlException 
	 * @throws WSDLException 
 	 * @throws Exception
 	 */
	public WSProxy(String proxyURI, String realWsdl) throws WSDLException, XmlException, IOException  {
 		super(proxyURI+ "Proxy", realWsdl);
 		this.realWsdl = realWsdl;
 		addOperationsToProxy();
 	}
 	
 	/**
 	 * Builds the proxy operations by using the SoapUI features
 	 * 
 	 */
 	private void addOperationsToProxy(){
 		for (int i=0; i < iface.getOperationCount(); i++){
 			WsdlMockOperation mockOperation = service.addNewMockOperation(iface.getOperationAt(i));
 			mockOperation.setDispatchStyle("SCRIPT");
 			
 			String responseContent = "${message}";
 			String script = getScript(mockOperation.getName());
 			
 			WsdlMockResponse response = mockOperation.addNewMockResponse( "Response 1", true );
 			response.setScript(script);
 			response.setResponseContent(responseContent);
 		}
 	}
 	
 	/**
 	 * Returns the operation names
 	 * 
 	 * @return a list containing all operation names
 	 */
 	public List<String> getOperationNames() {
 		List<String> operationNames =new ArrayList<String>();
 		
 		for (WsdlOperation operation : service.getMockedOperations()) 
 			operationNames.add(operation.getName());
 		
 		return operationNames;
 	}
 
 	/**
 	 * Returns the script that collect and forward the messages to the real services
 	 * 
 	 * @param operationName
 	 * @return
 	 */
 	private String getScript(String operationName) {
 		String script = "context.message =  eu.choreos.vv.interceptor.RequestDispatcher.getResponse ('" + realWsdl + "','" +
 										operationName + "', mockRequest.requestContent)";
 		
 		return script;
 	}
 
 	/**
 	 * Returns the real service WSDL URI
 	 * 
 	 * @return 
 	 */
 	public String getRealWsdl() {
 		return realWsdl;
 	}
 	
 	/**
 	 * Returns the proxy WSDL URI
 	 * 
 	 * @return
 	 */
 	public String getProxyWsdl(){
 		return super.getWsdl();
 	}
 	
 }

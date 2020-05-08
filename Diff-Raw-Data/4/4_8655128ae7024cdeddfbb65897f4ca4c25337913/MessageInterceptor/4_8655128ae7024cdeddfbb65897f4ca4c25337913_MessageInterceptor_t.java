 package eu.choreos.vv.interceptor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.xmlbeans.XmlException;
 
 import eu.choreos.vv.clientgenerator.Item;
 import eu.choreos.vv.clientgenerator.ItemParser;
 import eu.choreos.vv.common.WsdlUtils;
 import eu.choreos.vv.exceptions.MockDeploymentException;
 import eu.choreos.vv.exceptions.ParserException;
 import eu.choreos.vv.exceptions.WSDLException;
 
 /**
  * This class provides the Message Interceptor feature
  * 
  * @author Felipe Besson
  *
  */
 public class MessageInterceptor {
 
 	private WSProxy proxy;
 	private InterceptedMessagesRegistry registry; 
 	private String port;
 	
 	/**
 	 * Creates a message interceptor instance which will intercept the messages
 	 * by using a proxy deployed on the provided port
 	 * 
 	 * @param port
 	 */
 	public MessageInterceptor(String port){
 		registry = InterceptedMessagesRegistry.getInstance();
 		this.port = port;
 	}
 	
 	/**
 	 * Intercepts all messages sent to the provided WSDL
 	 * 
 	 * @param realWsdl
 	 * @throws IOException 
 	 * @throws XmlException 
 	 * @throws WSDLException 
 	 * @throws MockDeploymentException 
 	 * @throws Exception
 	 */
 	public void interceptTo(String realWsdl) throws WSDLException, XmlException, IOException, MockDeploymentException  {
 		proxy = new WSProxy(WsdlUtils.getBaseName(realWsdl), realWsdl);
 		proxy.setPort(port);
 		proxy.start();
 		
 		registry.registerWsdl(realWsdl);
 	}
 
 	/**
 	 * Retrieves all intercepted messages
 	 * 
 	 * @return a list of Messages in the Item format
 	 */
 	public List<Item> getMessages() {
 		List<Item> itemMessages = new ArrayList<Item>();
		List<String> xmlMessages =  new CopyOnWriteArrayList<String>(registry.getMessages(proxy.getRealWsdl()));
 		ItemParser parser = new ItemParser();
 
 		try {
 			for (String xmlMessage : xmlMessages)
 					itemMessages.add(parser.parse(xmlMessage));
 		} catch (ParserException e) {e.printStackTrace();}
 		
 		return itemMessages;
 	}
 
 	public String getRealWsdl() {
 		return proxy.getRealWsdl();
 	}
 
 	public String getProxyWsdl() {
 		return proxy.getProxyWsdl();
 	}
 
 	public String getPort() {
 		return proxy.getPort();
 	}
 	
 	public void stop(){
 		proxy.stop();
 	}
 
 }

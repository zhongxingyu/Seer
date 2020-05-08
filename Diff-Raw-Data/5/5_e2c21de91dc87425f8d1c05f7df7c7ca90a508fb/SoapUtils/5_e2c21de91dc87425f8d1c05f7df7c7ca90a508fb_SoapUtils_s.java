 package bieberfever.compositeservice;
 
 import javax.xml.namespace.QName;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPBodyElement;
 import javax.xml.soap.SOAPConnection;
 import javax.xml.soap.SOAPConnectionFactory;
 import javax.xml.soap.SOAPConstants;
 import javax.xml.soap.SOAPElement;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPHeader;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.ws.Service;
 import javax.xml.ws.soap.SOAPBinding;
 
 public class SoapUtils {
   private String serviceNs = "http://itu.dk/smds-e2012/lab/week-04/";
   private String serviceUri = "http://trustcare.itu.dk/task-manager-soap/TaskManagerService.svc?wsdl=wsdl0";
   private Service webService;
   private QName port;
 
  public static String doRestCall(String soapMethod, String[] params) {
     SoapUtils utils = new SoapUtils();
     // Initialise SOAP
     utils.initialize();
 
     try {
       // Create the request to send to server
       MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
       
       SOAPMessage message = factory.createMessage();
       SOAPHeader header = message.getSOAPHeader();
       header.detachNode();
       SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
       envelope.setAttribute("namspace",utils.serviceNs);
       SOAPBody body = message.getSOAPBody();
       QName bodyName = new QName(utils.serviceNs, "getResponse");
       SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
       SOAPElement symbol = bodyElement.addChildElement("taskId");
       symbol.addTextNode("tch-01");
       SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
       SOAPMessage response = connection.call(message, utils.serviceUri);
       connection.close();
       SOAPBody responseBody = response.getSOAPBody();
       SOAPBodyElement responseElement = (SOAPBodyElement)responseBody.getChildElements().next();
       SOAPElement returnElement = (SOAPElement)responseElement.getChildElements().next();
       if(responseBody.getFault() != null) { //-- If response has any fault.
           System.out.println(returnElement.getValue()+" "+responseBody.getFault().getFaultString());
       }  else  {
           System.out.println(returnElement.getValue());
       }
 
 
       
       
 //      MimeHeaders mimeHeader = request.getMimeHeaders(); 
 //      mimeHeader.setHeader("SOAPAction", utils.serviceUri);
 //      SOAPPart soap = request.getSOAPPart();
 //      SOAPEnvelope envelope = soap.getEnvelope();
 //      SOAPBody body = envelope.getBody();
 //      SOAPElement content = body.addBodyElement(new QName(utils.serviceNs,soapMethod));
 //
 //      SOAPElement name;
 //      int argNum = 0;
 //      for (String param : params) {
 //        name = content.addChildElement("arg" + argNum++);
 //        name.setTextContent(param);
 //      }
 
       Utils.print(message);
 
       // Send request to server
 //      Dispatch<SOAPMessage> dispatch = utils.webService.createDispatch(utils.port,SOAPMessage.class, Service.Mode.MESSAGE);
 //      SOAPMessage response = dispatch.invoke(message);
 //      String text = response.getSOAPBody().getTextContent();
 //      
 //      System.out.println(text);
     } catch (SOAPException e) {
       e.printStackTrace();
     }
 
     return "";
   }
 
   private void initialize() {
     QName service = new QName(serviceNs, "helloService");
     port = new QName(serviceNs, "helloServicePort");
     webService = Service.create(service);
     webService.addPort(port, SOAPBinding.SOAP11HTTP_BINDING, serviceUri);
   }
 
   public static void main(String[] args) {
     String[] params = { "tch-01" };
    doRestCall("GetTask", params);
   }
 }

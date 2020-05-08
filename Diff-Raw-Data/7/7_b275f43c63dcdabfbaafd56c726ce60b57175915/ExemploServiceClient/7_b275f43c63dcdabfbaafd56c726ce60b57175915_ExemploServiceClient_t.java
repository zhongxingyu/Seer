 package br.com.ggdio.ws.service.client;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.Service;
 import javax.xml.ws.WebServiceClient;
 
 import br.com.ggdio.ws.service.structure.ExemploService;
 
 /**
 * Esta classe realiza uma conex�o com o webservice
 * e a partir do wsdl realiza chamadas aos m�todos.
  * @author Guilherme Dio
  *
  */
 @WebServiceClient(name = "ExemploService", targetNamespace = "http://exemplo.ggdio.com.br/ws", wsdlLocation = "")
 public class ExemploServiceClient extends Service {
 
 	public ExemploServiceClient(String url) throws MalformedURLException {
		super(new URL(url), new QName("http://exemplo.ggdio.com.br/ws",
 				"ExemploService"));
 	}
 
 	public ExemploServiceClient(URL wsdlDocumentLocation, QName serviceName) {
 		super(wsdlDocumentLocation, serviceName);
 	}
 
 	public ExemploService getPort() {
 		return (ExemploService) super.getPort(ExemploService.class);
 	}
 
 }

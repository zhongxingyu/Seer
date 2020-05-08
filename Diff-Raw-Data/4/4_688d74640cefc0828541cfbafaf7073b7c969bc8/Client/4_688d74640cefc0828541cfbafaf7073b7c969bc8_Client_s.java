 package org.codehaus.xfire.jibx;
 
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.xfire.client.XFireProxyFactory;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 
 /**
  * <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * 
  */
 public class Client
 {
 
     /**
      * @param args
      * @throws MalformedURLException
      */
     public static void main(String[] args)
         throws MalformedURLException
     {
         Map props = new HashMap();
         props.put(ObjectServiceFactory.STYLE, "document");
         props.put(ObjectServiceFactory.USE, "literal");
         Service serviceModel = new JibxObjectFactory().create(AccountService.class,
                                                               "JibxService",
                                                               "http://jibx.xfire.codehaus.org",
                                                               props);
        
         AccountService service = (AccountService) new XFireProxyFactory()
 
         .create(serviceModel, "http://localhost:8080/jibxws/services/JibxService");
         Account param = new Account();
         param.setAccountNo("33040770037003665");
         AccountInfo result = service.getAccountStatus(param);
     }
 
 }

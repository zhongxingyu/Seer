 package org.codehaus.xfire.client;
 
 import java.net.MalformedURLException;
 import java.util.Properties;
 
 import org.apache.ws.security.handler.WSHandlerConstants;
 
 /**
  * <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  *  Signature Sample
  * 
  */
 public class BookClientSign
     extends BookClient
 {
 
     protected void configureProperties(Properties properties)
     {
         properties.setProperty(WSHandlerConstants.ACTION,WSHandlerConstants.SIGNATURE);
         properties.setProperty(WSHandlerConstants.USER, "alias");
         properties.setProperty(WSHandlerConstants.PW_CALLBACK_CLASS, org.codehaus.xfire.demo.PasswordHandler.class.getName());
         properties.setProperty(WSHandlerConstants.SIG_PROP_FILE,"org/codehaus/xfire/client/outsecurity_sign.properties");
        properties.setProperty(WSHandlerConstants.SIG_KEY_ID,"IssuerSerial");
 
     }
 
     /**
      * @param args
      * @throws MalformedURLException
      */
     public static void main(String[] args)
         throws MalformedURLException
     {
         new BookClientSign().executeClient("BookServiceSign");
 
     }
 
     protected String getName()
     {
         
         return "Syignature Client";
     }
 
 }

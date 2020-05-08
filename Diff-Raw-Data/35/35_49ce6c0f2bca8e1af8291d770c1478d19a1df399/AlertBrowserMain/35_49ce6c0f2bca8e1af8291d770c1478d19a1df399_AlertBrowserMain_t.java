 /*
  * Created on Mar 15, 2006
  */
 package com.alertscape.browser.ui.swing;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * @author josh
  * @version $Version: $
  */
 public class AlertBrowserMain {
   /**
    * @param args
    */
   public static void main(String[] args) {
    if(args.length < 2) {
      System.err.println("Usage: AlertBrowserMain jmsurl remoteserviceurl");
    }

     System.setProperty("jms.url", args[0]);
     System.setProperty("remote.service.url", args[1]);
     
 
     String[] contextPaths = new String[] { "browser-beans.xml" };
     new ClassPathXmlApplicationContext(contextPaths);
   }
 }

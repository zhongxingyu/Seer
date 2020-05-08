 package com.oopsconsultancy.xmltask.jdk15;
 
 import org.w3c.dom.*;
 import org.w3c.dom.traversal.*;
 import com.oopsconsultancy.xmltask.*;
 import javax.xml.xpath.*;
 
 /**
  * uses the JDK 1.5 XPath API
  * to analyse XML docs
  *
  * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
  * @version $Id: XPathAnalyser15.java,v 1.2 2004/05/18 08:42:17 bagnew Exp $
  */
 public class XPathAnalyser15 implements XPathAnalyser {
 
   private XPathAnalyserClient client;
   private Object callback;
   private XPathFactory g_xpathFactory;
   private XPath m_xpath;
 
   public XPathAnalyser15()
   {
     if (g_xpathFactory == null)
     {
         try
         {
             g_xpathFactory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
         }
         catch (Exception e)
         {
             System.out.println("Error: Could not initialize XPath api");
             e.printStackTrace(System.out);
         }
     }
     if (m_xpath == null && g_xpathFactory != null)
     {
         m_xpath = g_xpathFactory.newXPath();
     }
   }
 
   public void registerClient(XPathAnalyserClient client, Object callback) {
     this.client = client;
     this.callback = callback;
   }
 
   public int analyse(Node node, String xpath) throws Exception {
     int count = 0;
     Object result = null;
     try
     {
         result = m_xpath.evaluate(xpath, node, XPathConstants.NODESET);
     }
     catch (Exception e)
     {
 
     }
     if (result instanceof NodeList) {
       NodeList nl = (NodeList) result;
       Node n;
       for (int i = 0; i < nl.getLength(); i++) {
         n = nl.item(i);
        client.applyNode(n, callback);
         count++;
       }
     }
     else 
     {      
       result = m_xpath.evaluate(xpath, node, XPathConstants.STRING);
       String str = (String) result;
       client.applyNode(str, callback);
       count++;
     }
     
     return count;
   }
 }
 

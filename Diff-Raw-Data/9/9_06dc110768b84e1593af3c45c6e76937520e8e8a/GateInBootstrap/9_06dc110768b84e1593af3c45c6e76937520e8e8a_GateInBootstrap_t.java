 package org.gatein.portletbox;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Set;
 
 import javax.servlet.ServletContainerInitializer;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRegistration;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.gatein.pc.embed.EmbedServlet;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * Implement the {@link ServletContainerInitializer} interface for embedding GateIn Portlet Container in a war file
  * containing portlets.
  *
  * @author Julien Viet
  */
 public class GateInBootstrap implements ServletContainerInitializer {
 
     @Override
     public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
         ArrayList<String> portletNames = null;
         try {
             URL url = ctx.getResource("WEB-INF/portlet.xml");
 
             //
             if (url != null) {
                 // XPath to get the portlet names
                 portletNames = new ArrayList<String>();
                 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder builder = factory.newDocumentBuilder();
                 Document doc = builder.parse(url.openStream());
                 XPath xpath = XPathFactory.newInstance().newXPath();
                 XPathExpression expr = xpath.compile("//portlet-name/text()");
                 NodeList portletNameNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                 for (int i = 0;i < portletNameNodes.getLength();i++) {
                     Text portletNameNode = (Text) portletNameNodes.item(i);
                     portletNames.add(portletNameNode.getWholeText().trim());
                 }
             } else {
                 ctx.log("No portlets inside this web application");
             }
 
         } catch (Exception e) {
             ctx.log("Could not obtain a valid portlet.xml file", e);
         }
 
         //
         if (portletNames != null) {
             ctx.log("Detected portlet XML file with portlets " + portletNames + " -> Bootstrapping GateIn Portlet Container");
 
             //
             ServletRegistration.Dynamic embedRegistration = ctx.addServlet("EmbedServlet", EmbedServlet.class);
             embedRegistration.addMapping("/embed/*");
 
             //
             ServletRegistration.Dynamic redirectRegistration = ctx.addServlet("RedirectServlet", new RedirectServlet(portletNames));
             redirectRegistration.addMapping("/");
         }
     }
 }

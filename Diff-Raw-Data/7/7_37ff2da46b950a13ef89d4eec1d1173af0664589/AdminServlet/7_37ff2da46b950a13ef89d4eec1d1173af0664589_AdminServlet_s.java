 /**
  * 
  */
 package com.wyona.tomcat.cluster;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.wyona.tomcat.cluster.worker.Worker;
 import com.wyona.tomcat.util.TemplateUtil;
 
 /**
  * @author greg
  *
  */
 public class AdminServlet implements Servlet {
 
     private ServletContext ctx;
     private Logger log;
     
     private PropertyFile propertyFile;
     private ProtocolMananger protocolManager;
     private Worker[] workers;
     
     private final static String ADMIN_STYLESHEET = "admin.xsl";
     
     /* (non-Javadoc)
      * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
      */
     public void init(ServletConfig arg0) throws ServletException {
         
         log = Logger.getLogger(BalanceServlet.class);  
         
         this.ctx = arg0.getServletContext();     
         ServletContext balancerContext = this.ctx.getContext("/");
         if (balancerContext == null) {
             throw new ServletException("unable to get servlet context for url: /");
         }
         
         this.propertyFile = (PropertyFile) balancerContext.getAttribute(BalanceServlet.PROPERTY_FILE_ATTR_NAME);
         this.protocolManager = (ProtocolMananger) balancerContext.getAttribute(BalanceServlet.PROTOCOL_MANAGER_ATTR_NAME);
         this.workers = (Worker[]) balancerContext.getAttribute(BalanceServlet.WORKERS_ATTR_NAME);
         
         if (this.propertyFile == null || this.protocolManager == null || this.workers == null) {
             throw new ServletException("servlet context does not contain all bindings");
         }
     }
 
     /* (non-Javadoc)
      * @see javax.servlet.Servlet#getServletConfig()
      */
     public ServletConfig getServletConfig() {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
      */
     public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {                      
         
         handleToggleStatus(req);
         
         OutputStream out = res.getOutputStream();
         
         try {                        
             res.setContentType("text/xml; charset=UTF-8");
             
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();            
             Document doc = builder.newDocument();
             
             createStatusDocument(doc);                                 
             
             InputStream xslInputStream = TemplateUtil.getTemplateInputStream(this.ctx, ADMIN_STYLESHEET);
             if (xslInputStream == null) {
                 throw new ServletException("Stylesheet not found: " + ADMIN_STYLESHEET);
             }            
             
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Source xslSource = new StreamSource(xslInputStream);
             Transformer transformer = transformerFactory.newTransformer(xslSource); 
             transformer.transform(new DOMSource(doc), new StreamResult(out));                        
             
         } catch (ParserConfigurationException e) {
             throw new ServletException(e);
         } catch (TransformerConfigurationException e) {
             log.error(e.fillInStackTrace());
             throw new ServletException(e);
         } catch (TransformerException e) {
             throw new ServletException(e);
         }
         
         out.close();
     }
     
     private void handleToggleStatus(ServletRequest req) {
         for (int i=0; i<this.workers.length; i++) {
             String togglePar = req.getParameter(this.workers[i].getName());
             if (togglePar != null) {
                 //if (togglePar.equalsIgnoreCase("off")) {
                if (togglePar.equals("Turn+Off")) {
                     this.workers[i].setState(Worker.DEACTIVATED);
                 //} else if (togglePar.equalsIgnoreCase("on")) {
                } else if (togglePar.equals("Turn+On")) {
                     this.workers[i].setState(Worker.UNUSED);
                 }
             }
         }
     }
     
     private void createStatusDocument(Document doc) {        
         
         Node status = doc.appendChild(doc.createElement("status"));
         
         for (int i=0; i<this.workers.length; i++) {
             Element worker = doc.createElement("worker");
             worker.setAttribute("name", this.workers[i].getName());
             worker.setAttribute("type", this.workers[i].getType());
             worker.setAttribute("count", String.valueOf(this.workers[i].getRequestCount()));
             worker.setAttribute("state", String.valueOf(this.workers[i].getState()));
             worker.setAttribute("uri", this.workers[i].getUri());           
             worker.setAttribute("rttavg", Double.toString(this.workers[i].getAvgRoundTripTime()));
             worker.setAttribute("rtt", Double.toString(this.workers[i].getLastRoundTripTime()));            
             status.appendChild(worker);
         }
         
         Element connections = doc.createElement("connections");
         connections.setAttribute("count", String.valueOf(this.protocolManager.getActiveConnections()));
         status.appendChild(connections);
 
         Element refusedConnections = doc.createElement("refused-connections");
         refusedConnections.setAttribute("count", String.valueOf(this.protocolManager.getRefusedConnections()));
         status.appendChild(refusedConnections);
         
         Element failover = doc.createElement("failover");
         failover.setAttribute("enabled", Boolean.toString(this.propertyFile.getFailover()));
         status.appendChild(failover);
         
         Element balanceType = doc.createElement("balance-type");
         balanceType.setAttribute("type", this.propertyFile.getBalanceType());
         status.appendChild(balanceType);
         
         Element maxConnections = doc.createElement("max-connections");
         maxConnections.setAttribute("count", Integer.toString(this.propertyFile.getMaxConnections()));
         status.appendChild(maxConnections);
         
         Element maintainIntervall = doc.createElement("maintain-intervall");
         maintainIntervall.setAttribute("count", Integer.toString(this.propertyFile.getMaintainIntervall()));
         status.appendChild(maintainIntervall);
         
         Element osVersion = doc.createElement("os-version");
         osVersion.setAttribute("type",
                 System.getProperty("os.name") + " "  + 
                 System.getProperty("os.version") + " " +
                 System.getProperty("os.arch"));
         status.appendChild(osVersion);
         
     }        
     
     /* (non-Javadoc)
      * @see javax.servlet.Servlet#getServletInfo()
      */
     public String getServletInfo() {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see javax.servlet.Servlet#destroy()
      */
     public void destroy() {
         // TODO Auto-generated method stub
 
     }
 
 }

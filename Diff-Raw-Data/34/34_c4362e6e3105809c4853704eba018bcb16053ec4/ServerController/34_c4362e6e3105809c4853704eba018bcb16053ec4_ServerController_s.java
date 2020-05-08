 package fedora.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Properties;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
 import fedora.common.HttpClient;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
 import fedora.common.Constants;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.NotAuthorizedException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.utilities.ServerUtility;
 import fedora.server.Server;
 
 /**
  *
  * <p><b>Title:</b> ServerController.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2005 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class ServerController
         extends HttpServlet {
 
     private static Server s_server;
 
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         String action=request.getParameter("action");
         String requestInfo="Got controller '" + action + "' request from " + request.getRemoteAddr();
         if (fedora.server.Debug.DEBUG) System.out.println(requestInfo);
         PrintWriter out = response.getWriter();
         response.setContentType("text/plain");
     	String lineResponse = "UNKNOWN";
         if (action==null) {
             System.err.println("Error in controller request: action was not specified.");
             lineResponse = "ERROR";
             //fixup for xacml
         } else if (action.equals("startup")) {
             //the trouble with startup -- fixup for xacml
             if (Server.hasInstance(new File(System.getProperty("fedora.home")))) {
                 lineResponse = "ERROR";
             } else {
                 try {
                     s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
                     lineResponse = "OK";
                 } catch (Exception e) {
                     System.err.println("Error starting Fedora server: " + e.getClass().getName() + ": " + e.getMessage());
                     lineResponse = "ERROR";
                 }
             }            
         } else if (action.equals("shutdown")) {
         	System.err.println("shutdown 1");
             if (Server.hasInstance(new File(System.getProperty("fedora.home")))) {
             	System.err.println("shutdown 2");            	
                 try {
                 	System.err.println("shutdown 3");                	
                     s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
                 	System.err.println("shutdown 4");                    
                     s_server.logInfo(requestInfo);
                 	System.err.println("shutdown 5");                    
                 	Context context 
 					= ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, request, false);
                 	System.err.println("shutdown 6");                	
                     s_server.shutdown(context);
                 	System.err.println("shutdown 7");                    
                     lineResponse = "OK";
                 	System.err.println("shutdown 8");                    
                 } catch (Throwable t) {
                 	System.err.println("shutdown 9");                	
                     lineResponse = "ERROR";
                 	System.err.println("shutdown 10");                    
                     System.err.println("Error shutting down Fedora server: " + t.getClass().getName() + ": " + t.getMessage());
                 }
             	System.err.println("shutdown 11");
             }
         } else if (action.equals("status")) {
         	Context context 
 			= ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, request, false);
         	File fedoraHome = new File(System.getProperty("fedora.home"));
             if (! Server.hasInstance(fedoraHome)) {
             	lineResponse = "STOPPED";
             } else {
             	Server server;
 				try {
 					server = Server.getInstance(fedoraHome, false);
 					lineResponse = server.status(context);
 				} catch (ServerInitializationException e) {
 					//we can do no more
 				} catch (ModuleInitializationException e) {
 					//since 2nd parm above is "false", this is unexpected
 				} catch (NotAuthorizedException e) {
 					//don't tell
 				}
             }
         } else {
             System.err.println("Error in controller request: action '" + action + "' was not recognized.");
             lineResponse = "ERROR";
         }
         out.write(lineResponse);
     }
 
     public void init() {
     }
 
     public void destroy() {
     }
 
     /*
     public static final String FEDORA_SERVER_HOST = "fedoraServerHost";
     public static final String FEDORA_SERVER_PORT = "fedoraServerPort";
     public static final String FEDORA_REDIRECT_PORT = "fedoraRedirectPort";
     public static final String ADMIN_USER = "adminUser";
     public static final String ADMIN_PASSWORD = "adminPassword";
     public static final Properties getServerProperties(String protocol) 
     	throws Exception {
        	Properties properties = new Properties();    	
         String fedoraHome=System.getProperty("fedora.home");
         if (fedoraHome==null) {
         	throw new Exception("ERROR: fedora.home system property not set.");            	
         }        	
         File fedoraHomeDir=new File(fedoraHome);
         File fcfgFile=new File(fedoraHomeDir, "server/config/fedora.fcfg");
         DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder=factory.newDocumentBuilder();
         Element rootElement=builder.parse(fcfgFile).getDocumentElement();
         NodeList params=rootElement.getElementsByTagName("param");
         for (int i=0; i<params.getLength(); i++) {
             Node nameNode=params.item(i).getAttributes().getNamedItem("name");
             Node valueNode=params.item(i).getAttributes().getNamedItem("value");
     		if (("http".equals(protocol) && FEDORA_SERVER_PORT.equals(nameNode.getNodeValue()))
             ||  ("https".equals(protocol) && FEDORA_REDIRECT_PORT.equals(nameNode.getNodeValue()))
             ||  FEDORA_SERVER_HOST.equals(nameNode.getNodeValue())
             ||  ADMIN_USER.equals(nameNode.getNodeValue())
             ||  ADMIN_PASSWORD.equals(nameNode.getNodeValue())) {
         		properties.put(nameNode.getNodeValue(),valueNode.getNodeValue());            			
     		}
         }
         if ((! properties.containsKey(FEDORA_SERVER_HOST))) {
         	throw new Exception("fedora.fcfg missing " + "http host");            	
         }                
         if ((! properties.containsKey(FEDORA_SERVER_PORT)) 
         &&  (! properties.containsKey(FEDORA_REDIRECT_PORT))) {
         	throw new Exception("fedora.fcfg missing " + "http port");            	
         }
         if ((! properties.containsKey(ADMIN_USER))) {
         	throw new Exception("fedora.fcfg missing " + "admin user");            	
         }
         if ((! properties.containsKey(ADMIN_PASSWORD))) {
         	throw new Exception("fedora.fcfg missing " + "admin passwd");            	
         }            
     	return properties;
     }
     */
 
     //private static final String USAGE = "ERROR: Need one argument: 'startup', 'shutdown', or 'status'";
     private static final String USAGE = "USAGE for ServerController.main(): startup|shutdown|status [http|https] [username] [passwd]";
     
     public static void main(String[] args) {
     	try {
             if (args.length < 1) {
             	throw new Exception(USAGE);
             }
             String action = args[0];
             if (! "startup".equals(action)
             &&  ! "shutdown".equals(action)
             &&  ! "status".equals(action)) {
             	throw new Exception(USAGE);
             }
             String protocol = args.length > 1 ? args[1] : "http";        
             if (! "http".equals(protocol) && ! "https".equals(protocol)) {
             	throw new Exception(USAGE);
             }
             String optionalUsername = null;
             String optionalPassword = null;            
             if (args.length > 2) {
             	if (args.length == 3) {
                 	throw new Exception(USAGE);
             	}
             	optionalUsername = args[2];
             	optionalPassword = args[3];
             }
            Properties serverProperties = ServerUtility.getServerProperties(protocol);            
        	String response = HttpClient.getLineResponse(
        			protocol,
          			serverProperties.getProperty(ServerUtility.FEDORA_SERVER_HOST),
          			serverProperties.getProperty( "http".equals(protocol) ? ServerUtility.FEDORA_SERVER_PORT : ServerUtility.FEDORA_REDIRECT_PORT),								
          			(optionalUsername == null) ? serverProperties.getProperty(ServerUtility.ADMIN_USER) : optionalUsername,
          			(optionalPassword == null) ? serverProperties.getProperty(ServerUtility.ADMIN_PASSWORD) : optionalPassword,
                  	"/management/control?action=" + action);        	
             System.out.println(response);        	
     	} catch (Exception e) {
     	    System.err.println(e.getMessage());
             System.exit(1);
     	}
     }
 }

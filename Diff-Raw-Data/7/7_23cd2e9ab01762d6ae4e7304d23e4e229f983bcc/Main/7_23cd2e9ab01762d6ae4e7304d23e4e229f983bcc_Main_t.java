 package org.tai.cops.server;
 
 
 import java.util.ArrayList;
 import java.util.EnumSet;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.DispatcherType;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.tai.cops.occi.ERenderingStructures;
 
 import com.google.inject.servlet.GuiceFilter;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import occi.lexpar.OcciParser;
 
 /**
  * Starts an embedded Jetty server.
  */
 public class Main {
 	public static final String PUBLISHER_URL = "http://127.0.0.1:8086";
 	
 	private static String rebuildOcciHeaders(Map<String, List<String>> headers) {
 		StringBuilder buffer = new StringBuilder();
 		for (Entry<String, List<String>> i_elt : headers.entrySet()) {
 			ERenderingStructures ers = ERenderingStructures.fromHttpHeader(i_elt.getKey());
 			if (ers != null)
 				for (String v : i_elt.getValue()) {
 					buffer.append(i_elt.getKey()).append(": ").append(v).append('\n');
 				}
 		}
 		return buffer.toString();
 	}
 	
 	final static Logger logger = LoggerFactory.getLogger(Main.class);
 	
     public static void main(String[] args) throws Exception {
     	
         Server server = new Server(8080);
         ServletContextHandler root = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
 
         root.addEventListener(new SampleConfig());
         root.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
         root.addServlet(EmptyServlet.class, "/*");
 
         Client client = Client.create();
         
 		WebResource webResource = client.resource(PUBLISHER_URL + "/-/");
 		
 		logger.debug("connecting to publisher");
 		ClientResponse response = webResource.accept("text/occi").type("text/occi")
                    .get(ClientResponse.class);
 		
 		logger.debug("rebuilding occi headers with the response");		
         String occiHeaders = rebuildOcciHeaders(response.getHeaders());
         logger.debug("rebuild result:\n" + occiHeaders);
         
         OcciParser op = OcciParser.getParser(occiHeaders);
        Map<String, ArrayList<String>> hs = op.headers();
		logger.debug("parsed {} catogories: {}", hs.get(OcciParser.occi_categories).size(),
				hs.get(OcciParser.occi_categories).toString());
 		
         server.start();
     }
 }

 package eu.fusepool.datalifecycle;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.AllPermission;
 import java.security.Permission;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import org.apache.clerezza.jaxrs.utils.TrailingSlash;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.Triple;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
 import org.apache.clerezza.rdf.core.access.TcManager;
 import org.apache.clerezza.rdf.core.access.security.TcAccessController;
 import org.apache.clerezza.rdf.core.access.security.TcPermission;
 import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
 import org.apache.clerezza.rdf.core.serializedform.Parser;
 import org.apache.clerezza.rdf.ontologies.RDFS;
 import org.apache.clerezza.rdf.utils.GraphNode;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.stanbol.commons.indexedgraph.IndexedMGraph;
 import org.apache.stanbol.commons.web.viewable.RdfViewable;
 import org.apache.stanbol.entityhub.servicesapi.site.SiteManager;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Uses the SiteManager to resolve entities. Every requested is recorded to
  * a graph. The client gets information and meta-information about the resource
  * and sees all previous requests for that resource.
  */
 @Component
 @Service(Object.class)
 @Property(name="javax.ws.rs", boolValue=true)
 @Path("sourcing")
 public class SourcingAdmin {
     
     /**
      * Using slf4j for normal logging
      */
     private static final Logger log = LoggerFactory.getLogger(SourcingAdmin.class);
     
     @Reference
     private Parser parser;
     
     /**
      * This service allows accessing and creating persistent triple collections
      */
     @Reference
     private TcManager tcManager;
     
     /**
      * This is the name of the graph in which we "log" the requests
      */
     private UriRef REQUEST_LOG_GRAPH_NAME = new UriRef("http://example.org/resource-resolver-log.graph");
     
     /**
      * Name of the data life cycle graph. It is used as a register of other graphs to manage their life cycle 
      */
    private UriRef DATA_LIFECYCLE_GRAPH_REFERENCE = new UriRef("urn:x-localinstance:/datalifecycle.graph");
     
     /**
      * Register graph referencing graphs for life cycle monitoring;
      */
     private MGraph dlcRegisterGraph = null;
     
     @Activate
     protected void activate(ComponentContext context) {
         
     	log.info("The Data Life Cycle service is being activated");
         try {
             tcManager.createMGraph(REQUEST_LOG_GRAPH_NAME);
             //now make sure everybody can read from the graph
             //or more precisly, anybody who can read the content-graph
             TcAccessController tca = new TcAccessController(tcManager);
             tca.setRequiredReadPermissions(REQUEST_LOG_GRAPH_NAME, 
                     Collections.singleton((Permission)new TcPermission(
                     "urn:x-localinstance:/content.graph", "read")));
            
             // creates the data lifecycle register graph if it doesn't exists
             if( ! graphExists(DATA_LIFECYCLE_GRAPH_REFERENCE) ) {            	
             	dlcRegisterGraph = createDlcGraph();
             	log.info("Created Data Lifecycle Register Graph. This graph will reference all graphs during their lifecycle");
             }
             
         } catch (EntityAlreadyExistsException ex) {
             log.debug("The graph for the request log already exists");
         }
         
     }
     
     @Deactivate
     protected void deactivate(ComponentContext context) {
         log.info("The Data Life Cycle service is being deactivated");
     }
     
     /**
      * This method return an RdfViewable, this is an RDF serviceUri with associated
      * presentational information.
      */
     @GET
     public RdfViewable serviceEntry(@Context final UriInfo uriInfo, 
             @QueryParam("url") final UriRef url, 
             @HeaderParam("user-agent") String userAgent) throws Exception {
         //this maks sure we are nt invoked with a trailing slash which would affect
         //relative resolution of links (e.g. css)
         TrailingSlash.enforcePresent(uriInfo);
         
         final String resourcePath = uriInfo.getAbsolutePath().toString();
         if(url != null) {
         	String query = url.toString();
         	log.info(query);
         }
         
         //The URI at which this service was accessed, this will be the 
         //central serviceUri in the response
         final UriRef serviceUri = new UriRef(resourcePath);
         //the in memory graph to which the triples for the response are added
         //final MGraph responseGraph = new IndexedMGraph();
         //This GraphNode represents the service within our result graph
         //final GraphNode node = new GraphNode(serviceUri, responseGraph);
         //node.addProperty(Ontology.graph, new UriRef("http://fusepool.com/graphs/patentdata"));
         //node.addPropertyValue(RDFS.label, "A graph of patent data");
         //What we return is the GraphNode we created with a template path
         final GraphNode node = new GraphNode(DATA_LIFECYCLE_GRAPH_REFERENCE, getDlcGraph());
         
         return new RdfViewable("SourcingAdmin", node, SourcingAdmin.class);
     }
     
     
     @GET
     @Path("graphexists")
     @Produces("text/plain")
     public String graphExistsCommand(@Context final UriInfo uriInfo,
             @QueryParam("graph") final String graphName) throws Exception {
         AccessController.checkPermission(new AllPermission());
         String uriInfoStr = uriInfo.getRequestUri().toString();
         String response = "";
         if(graphExists( new UriRef(graphName) )) {
         	response = "Graph: " + graphName + " already exists.";
         }
         else {
         	response = "Graph " + graphName + " doesn't exist.";
         }
         
         return "Datalifecycle service. " + response;
     }
     
     
     @GET
     @Path("listgraphs")
     @Produces("text/plain")
     public String getGraphsCommand(@Context final UriInfo uriInfo,
             @QueryParam("graph") final String query) throws Exception {
         AccessController.checkPermission(new AllPermission());
         String uriInfoStr = uriInfo.getRequestUri().toString();
         
         ArrayList<UriRef> graphs = getGraphs();
         String graphListTxt = "";
         if(graphs.size() > 0) {
 	        Iterator<UriRef> i = graphs.iterator();
 	        while(i.hasNext()) {
 	        	UriRef graph = i.next();
 	        	graphListTxt += graph.toString() + "\n";
 	        	log.info(graph.toString());
 	        }
         }
         else {
         	graphListTxt += "No Dlc graphs";
         }
         
         return "Datalifecycle service. Graphs: " + graphListTxt;
     }
     
    /**
     * Returns a list of graphs referred in the data life cycle graph 
     * @return
     */
    
     private ArrayList<UriRef> getGraphs() {
         MGraph dlcRegister = getDlcGraph();
         ArrayList<UriRef> dlcGraphsList = new ArrayList<UriRef>();
         Iterator<Triple> idlcGraphs = dlcRegister.filter(DATA_LIFECYCLE_GRAPH_REFERENCE, Ontology.graph, null);
         while(idlcGraphs.hasNext()) {
         	Triple graphTriple = idlcGraphs.next();
         	UriRef dlcGraphRef = (UriRef) graphTriple.getObject(); 
         	dlcGraphsList.add(dlcGraphRef);
         }
         return dlcGraphsList;
     }
     
     
     @GET
     @Path("addgraph")
     @Produces("text/plain")
     public String createGraphCommand(@Context final UriInfo uriInfo,
             @QueryParam("graph") final String graphName) throws Exception {
         AccessController.checkPermission(new AllPermission());
         String uriInfoStr = uriInfo.getRequestUri().toString();
         String response = "";
         if(createGraph(graphName)) {
         	response += "Graph " + graphName + " already exists.";
         }
         else {
         	response += "Created new graph " + graphName;
         }
         
         return "Datalifecycle service. " + response;
     }
     
     /**
      * Creates a new graph and adds its uri to the data life cycle graph
      * @param dlc_graph_name
      * @return
      */
     private boolean createGraph(String graph_name) {
     	boolean graphExists = false;
     	UriRef graphRef = new UriRef(graph_name); 
     	try {
 	    	if( graphExists(new UriRef(graph_name)) ) {
 	    		graphExists = true;
 	    	}
 	    	else { 
 	    		MGraph graph = tcManager.createMGraph(graphRef);
 	    		GraphNode dlcGraphNode = new GraphNode(DATA_LIFECYCLE_GRAPH_REFERENCE, getDlcGraph());
 	    		dlcGraphNode.addProperty(Ontology.graph, graphRef);
 	    	}
     	}
     	catch(UnsupportedOperationException uoe) {
     		log.error("Error while creating a graph");
     	}
     	
     	return graphExists;
     }
     
     /**
      * Load RDF data from a URI (schemes: "file://" or "http://")
      *
      */
     @GET
     @Path("upload")
     @Produces("text/plain")
     public String uploadRdfDataCommand(@Context final UriInfo uriInfo,
             @QueryParam("url") final URL url,
             @QueryParam("graph") final String graphName) throws Exception {
         AccessController.checkPermission(new AllPermission());
        
         HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
         urlConnection.addRequestProperty("Accept", "application/rdf+xml; q=.9, text/turte;q=1");
         final String mediaType = urlConnection.getContentType();
         final InputStream data = urlConnection.getInputStream();
         return uploadRdfData(uriInfo, mediaType, graphName, data);
     }
     
     /**
      * Load RDF data
      */
     private String uploadRdfData(@Context final UriInfo uriInfo,
             @HeaderParam("Content-Type") final String mediaType,
             String graphName,
             final InputStream data) throws Exception {
     	
         AccessController.checkPermission(new AllPermission());
         MGraph graph = tcManager.getMGraph(new UriRef(graphName));
         parser.parse(graph, data, mediaType);
 
         return "The graph " + graphName + " now contains " + graph.size() + " triples";
     }
     
     /**
      * Returns the data life cycle graph containing all the monitored graphs. It creates it if doesn't exit yet.
      * @return
      */
     private MGraph getDlcGraph() {
     	
     	return tcManager.getMGraph(DATA_LIFECYCLE_GRAPH_REFERENCE);
     }
     
     private MGraph getRequestGraph() {
         return tcManager.getMGraph(REQUEST_LOG_GRAPH_NAME);
     }
     
     /**
      * Checks if a graph exists and returns a boolean value.
      * @param graph_ref
      * @return
      */
     private boolean graphExists(UriRef graph_ref) {
     	boolean registerGraphExists = false;
     	Set<UriRef> graphs = tcManager.listMGraphs();
     	Iterator<UriRef> igraphs = graphs.iterator();
     	while(igraphs.hasNext()){
     		UriRef graphRef = igraphs.next();
     		if(graph_ref.toString().equals(graphRef.toString())) {
     			registerGraphExists = true;
     		}
     	}
     	
     	return registerGraphExists;
     	
     }
     
     /**
      * Creates the data lifecycle register graph. Must be called at the bundle activation if the graph doesn't exists yet.
      */
     private MGraph createDlcGraph() {
         return tcManager.createMGraph(DATA_LIFECYCLE_GRAPH_REFERENCE);
     }
     
 }

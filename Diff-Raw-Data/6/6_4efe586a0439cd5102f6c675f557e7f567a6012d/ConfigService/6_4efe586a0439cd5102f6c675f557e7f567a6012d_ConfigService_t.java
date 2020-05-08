 package eu.dm2e.ws.services.config;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 import eu.dm2e.logback.LogbackMarkers;
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.ConfigProp;
 import eu.dm2e.ws.DM2E_MediaType;
 import eu.dm2e.ws.ErrorMsg;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.api.AbstractConfigPojo;
 import eu.dm2e.ws.api.SerializablePojo;
 import eu.dm2e.ws.api.WebserviceConfigPojo;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.api.WorkflowConfigPojo;
 import eu.dm2e.ws.api.json.OmnomJsonSerializer;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
 import eu.dm2e.ws.services.AbstractRDFService;
 
 /**
  * Service for storing and retrieving configurations for workflows and webservices.
  *
  * FIXME refactor this into workflowconfig/webservice config service so we can delegate 
  * de/serialization to MessageBodyWriter
  * @author Konstantin Baierer
  *
  */
 @Path("/config")
 public class ConfigService extends AbstractRDFService {
 	
 	@Override
 	public WebservicePojo getWebServicePojo() {
 		WebservicePojo ws = super.getWebServicePojo();
 		ws.setLabel("Configuration service");
 		return ws;
 	}
 	
     /**
      * GET /{id}		Accept: RDF, JSON
      * @param uriInfo
      * @param id
      * @return
      */
     @GET
     @Path("{id}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 		// , DM2E_MediaType.TEXT_PLAIN,
 		 , MediaType.APPLICATION_JSON
 	})
     public Response getConfig() {
         log.info("Configuration requested: " + getRequestUriWithoutQuery());
         Grafeo g = new GrafeoImpl();
         URI configURI = getRequestUriWithoutQuery();
         try {
 	        g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), configURI);
         } catch (RuntimeException e) {
         	return throwServiceError(getRequestUriWithoutQuery().toString(), ErrorMsg.NOT_FOUND, 404);
         }
         
         ResponseBuilder resp;
         if (g.containsTriple(configURI, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
         	WebserviceConfigPojo configPojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, configURI);
         	resp = Response.ok(configPojo);
         } else if (g.containsTriple(configURI, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WORKFLOW_CONFIG)) {
         	WorkflowConfigPojo configPojo = g.getObjectMapper().getObject(WorkflowConfigPojo.class, configURI);
         	resp = Response.ok(configPojo);
         } else {
         	return throwServiceError(configURI + " has an unknown rdf:type.", ErrorMsg.NOT_FOUND, 404);
         }
         return resp.build();
     }
     
     /**
      * GET /list		Accept: RDF, JSON
      * @param uriInfo
      * @return
      */
     @GET
     @Path("list")
     public Response getConfigList(@Context UriInfo uriInfo) {
     	
     	List<Class<? extends AbstractConfigPojo<?>>> pojoClasses = Arrays.asList(
 			WebserviceConfigPojo.class,
 			WorkflowConfigPojo.class
 		);
         List<SerializablePojo> configList = new ArrayList<>();
     	for (Class<? extends AbstractConfigPojo<?>> pojoClass  : pojoClasses) {
     		GrafeoImpl g = new GrafeoImpl();
     		String pojoRdfClass = pojoClass.getAnnotation(RDFClass.class).value();
     		g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(pojoRdfClass));
     		for (GResource gres : g.listSubjects()) {
     			if (gres.isAnon()) continue;
     			AbstractConfigPojo<?> pojo = null;
 				try {
 					pojo = pojoClass.newInstance();
 				} catch (InstantiationException | IllegalAccessException e) {
 					log.error("Could not instantiate {} for URI {}", pojoClass, gres);
 					return throwServiceError("INTERNAL ERROR ");
 				}
     			pojo.setId(gres.getUri());
 				pojo.loadFromURI(pojo.getId());
     			configList.add(pojo);
     		}
     	}
     	return Response.ok(configList).build();
     }
 
     /**
      * PUT /{id}		Accept: RDF
      * @param input
      * @return
      */
     @PUT
     @Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
     })
     @Path("{id}")
     public Response putConfig(File input) {
     	URI uri = getRequestUriWithoutQuery();
         log.info("PUT config to " + uri);
         Grafeo g;
         try {
         	g = new GrafeoImpl(input);
         } catch (Throwable t) {
         	log.error(ErrorMsg.BAD_RDF.toString());
         	return throwServiceError(ErrorMsg.BAD_RDF, t);
         }
         log.info("Looking for top blank node.");
         GResource res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
         if (null == res) {
         	res = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_CONFIG);
         }
         if (null == res)  {
         	res = g.resource(uri);
         } else {
         	res.rename(uri.toString());
         }
         String actualRdfType;
 		try {
 			actualRdfType = g.firstMatchingObject(uri.toString(), NS.RDF.PROP_TYPE).resource().getUri();
 		} catch (NullPointerException e) {
 			return throwServiceError(ErrorMsg.UNTYPED_RDF);
 		}
         log.warn("Skolemnizing assignments ...");
         g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
         log.warn("DONE Skolemnizing ...");
         
         SerializablePojo pojo = null;
         if (actualRdfType.equals(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
         	pojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, res);
         }
         else if (actualRdfType.equals(NS.OMNOM.CLASS_WORKFLOW_CONFIG)) {
         	pojo = g.getObjectMapper().getObject(WorkflowConfigPojo.class, res);
         } else {
         	return throwServiceError(ErrorMsg.WRONG_RDF_TYPE);
         }
 
         g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
         
         return Response.created(uri).entity(pojo).build();
         
 //        return Response.created(uri).entity(getResponseEntity(g)).build();
 //        return Response.seeOther(uri).build();
     }
 
 	/**
 	 * POST /			Accept: RDF
 	 * @param input
 	 * @return
 	 */
 	@POST
     @Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
     })
     public Response postConfig(File input) {
         log.info("POST config.");
 //        // TODO BUG this fails if newlines aren't correctly transmitted due to line-wide comments in turtle
         
     	URI uri = appendPath(createUniqueStr());
     	log.debug("Posting the config to " + uri.toString());
         Grafeo g;
         try {
         	g = new GrafeoImpl(input);
         } catch (Throwable t) {
         	log.warn(ErrorMsg.BAD_RDF.toString());
         	return throwServiceError(ErrorMsg.BAD_RDF, t);
         }
         log.debug("Parsed config RDF.");
 
         boolean isWorkflow = true;
         GResource res = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_CONFIG);
         if (null == res)  {
         	isWorkflow = false;
         	res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
         	if (null == res)
 	        	return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE + ": " + g.getTurtle());
         }
         res.rename(uri);
         log.debug("Renamed top blank node.");
         g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
         log.debug("Skolemnized assignments");
         
         SerializablePojo pojo;
         if (isWorkflow) {
         	pojo = g.getObjectMapper().getObject(WorkflowConfigPojo.class, res);
        	log.warn("Workflow: " + ((WorkflowConfigPojo) pojo).getWorkflow());
         } else {
         	pojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, res);
        	log.warn("Webservice: " + ((WebserviceConfigPojo) pojo).getWebservice());
         }
         
         g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
         log.info("Written data to endpoint.");
         log.debug(LogbackMarkers.DATA_DUMP, g.getTerseTurtle());
 
         return Response.created(uri).entity(pojo).build();
     }
 	
 	/**
 	 * POST /		Accept: JSON
 	 * @param input
 	 * @return
 	 */
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response postConfigJSON(String input) {
 		log.info("POST config (JSON)");
 
 		JsonParser jsonParser = new JsonParser();
 		JsonElement json = jsonParser.parse(input);
 		if (! json.getAsJsonObject().has(NS.RDF.PROP_TYPE)) {
 			return throwServiceError(ErrorMsg.UNTYPED_RDF);
 		}
 
 		String rdfType = json.getAsJsonObject().get(NS.RDF.PROP_TYPE).getAsString();
 		SerializablePojo pojo;
 		boolean isWorkflow = false;
 		if (rdfType.equals(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
 			pojo = OmnomJsonSerializer.deserializeFromJSON(input, WebserviceConfigPojo.class);
 		} else if (rdfType.equals(NS.OMNOM.CLASS_WORKFLOW_CONFIG)) { 
 			isWorkflow = true;
 			pojo = OmnomJsonSerializer.deserializeFromJSON(input, WorkflowConfigPojo.class);
 		} else {
 			return throwServiceError(ErrorMsg.WRONG_RDF_TYPE);
 		}
 		log.debug(LogbackMarkers.DATA_DUMP, pojo.getTerseTurtle());
 		
 		// set uri
     	URI uri = appendPath(createUniqueStr());
     	pojo.setId(uri.toString());
 		
     	// Skolemize assignments
 		Grafeo g = pojo.getGrafeo();
 		g.skolemizeUUID(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
 
 		SerializablePojo pojoSkolemized = g.getObjectMapper()
 				.getObject(isWorkflow ? WorkflowConfigPojo.class : WebserviceConfigPojo.class, uri);
     	
 		log.debug(LogbackMarkers.DATA_DUMP, pojoSkolemized.getTerseTurtle());
     	pojoSkolemized.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
     	
     	return Response.created(uri).entity(pojoSkolemized).build();
 		
 	}
 
 	/**
 	 * PUT /{id}		Accept: JSON
 	 * @param input
 	 * @return
 	 */
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Path("{id}")
 	public Response putConfigJSON(String input) {
 		log.info("PUT config (JSON)");
 
 		JsonParser jsonParser = new JsonParser();
 		JsonElement json = jsonParser.parse(input);
 		if (! json.getAsJsonObject().has(NS.RDF.PROP_TYPE)) {
 			return throwServiceError(ErrorMsg.UNTYPED_RDF);
 		}
 
 		String rdfType = json.getAsJsonObject().get(NS.RDF.PROP_TYPE).getAsString();
 		SerializablePojo pojo;
 		boolean isWorkflow = false;
 		if (rdfType.equals(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
 			pojo = OmnomJsonSerializer.deserializeFromJSON(input, WebserviceConfigPojo.class);
 		} else if (rdfType.equals(NS.OMNOM.CLASS_WORKFLOW_CONFIG)) { 
 			isWorkflow = true;
 			pojo = OmnomJsonSerializer.deserializeFromJSON(input, WorkflowConfigPojo.class);
 		} else {
 			return throwServiceError(ErrorMsg.WRONG_RDF_TYPE);
 		}
 		log.debug(LogbackMarkers.DATA_DUMP, pojo.getTerseTurtle());
 		
 		// DON'T change the ID
 		URI uri = getRequestUriWithoutQuery();
 		
     	// Skolemize assignments
 		Grafeo g = pojo.getGrafeo();
 		g.skolemizeUUID(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
 
 		SerializablePojo pojoSkolemized = g.getObjectMapper()
 				.getObject(isWorkflow ? WorkflowConfigPojo.class : WebserviceConfigPojo.class, uri);
     	
 		log.debug(LogbackMarkers.DATA_DUMP, pojoSkolemized.getTerseTurtle());
     	pojoSkolemized.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
     	
     	return Response.created(uri).entity(pojoSkolemized).build();
 		
 	}
 
 	/**
 	 * GET /{id}/validate
 	 * @param g
 	 * @param res
 	 */
 	@GET
 	@Path("{id}/validate")
 	public Response getValidation() {
 		log.info("Validating");
 		URI confPojoUri = popPath();
         WebserviceConfigPojo confPojo = new WebserviceConfigPojo();
 		try {
 			confPojo.loadFromURI(confPojoUri);
 		} catch (Exception e1) {
 			return throwServiceError(e1);
 		}
         try {
 			confPojo.validate();
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
         return Response.ok().build();
 	}
 
 	/**
 	 * POST {id}/assignment
 	 * @param configId
 	 * @param rdfString
 	 * @return
 	 */
 	@POST
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{configId}/assignment/")
 	public Response postAssignment(@PathParam("configId") String configId, String rdfString) { Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER_ASSIGNMENT);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI conifgUri = popPath();
 		blank.rename(newUri);
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), conifgUri);
 		return Response.created(newUri).build();
 	}
 	
 	/**
 	 * PUT {configId}/assignment/{assId}
 	 * @param configId
 	 * @param assId
 	 * @param rdfString
 	 * @return
 	 */
 	@PUT
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{configId}/assignment/{assId}")
 	public Response putAssignment(@PathParam("configId") String configId, @PathParam("assId") String assId, String rdfString) {
 		URI assUri = getRequestUriWithoutQuery();
 		URI configUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(assUri + "?s ?o")
 			.insert(g.toString())
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(configUri).build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	
 	@GET
 	@Path("{configId}/assignment/{assId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowConnector( @PathParam("configId") String configId, @PathParam("assId") String assId) {
         log.info("Assignment " + assId + " of configuration requested: " + uriInfo.getRequestUri());	
 		URI configUri = popPath(popPath());
 		return Response.seeOther(configUri).build();
 	}
 
 }

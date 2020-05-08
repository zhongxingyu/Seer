 package eu.dm2e.ws.services.workflow;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.ConfigProp;
 import eu.dm2e.ws.DM2E_MediaType;
 import eu.dm2e.ws.ErrorMsg;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.api.AbstractJobPojo;
 import eu.dm2e.ws.api.JobPojo;
 import eu.dm2e.ws.api.LogEntryPojo;
 import eu.dm2e.ws.api.ParameterAssignmentPojo;
 import eu.dm2e.ws.api.ParameterConnectorPojo;
 import eu.dm2e.ws.api.ParameterPojo;
 import eu.dm2e.ws.api.WebserviceConfigPojo;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.api.WorkflowConfigPojo;
 import eu.dm2e.ws.api.WorkflowJobPojo;
 import eu.dm2e.ws.api.WorkflowPojo;
 import eu.dm2e.ws.api.WorkflowPositionPojo;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
 import eu.dm2e.ws.services.AbstractAsynchronousRDFService;
 import eu.dm2e.ws.services.WorkerExecutorSingleton;
 
 /**
  * Service for the creation and execution of workflows
  */
 @Path("/workflow")
 public class WorkflowService extends AbstractAsynchronousRDFService {
 
 	public static String PARAM_POLL_INTERVAL = "pollInterval";
 	public static String PARAM_JOB_TIMEOUT = "jobTimeout";
 	public static String PARAM_COMPLETE_LOG = "completeLog";
 
 	/**
 	 * The WorkflowJob object for the worker part of the service (to be used in
 	 * the run() method)
 	 */
 	private WorkflowJobPojo workflowJobPojo;
 	public WorkflowJobPojo getWorkflowJobPojo() { return this.workflowJobPojo; };
 	public void setWorkflowJobPojo(WorkflowJobPojo wfJobPojo) { this.workflowJobPojo = wfJobPojo; };
 
 	@Override
 	public WebservicePojo getWebServicePojo() {
 		WebservicePojo ws = new WebservicePojo();
 		ws.setLabel("Workflow Service");
 		return ws;
 	}
 
 
 	/**
 	 * GET /list
 	 * @return
 	 */
 	@GET
 	@Path("list")
 	@Consumes({
 		MediaType.APPLICATION_JSON
 	})
 	public Response getWorkflowList() {
 		List<WorkflowPojo> wfList = new ArrayList<WorkflowPojo>();
         Grafeo g = new GrafeoImpl();
         g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WORKFLOW));
         for (GResource wfUri : g.findByClass(NS.OMNOM.CLASS_WORKFLOW)) {
         	log.info("Workflow Resource: " + wfUri.getUri());
         	if (null == wfUri.getUri()) {
         		log.warn("There is a blank node workflow without an ID in the triplestore.");
         	}
         	// FIXME possibly very inefficient
         	g.load(wfUri.getUri());
         	WorkflowPojo wf = new WorkflowPojo();
         	wf.loadFromURI(wfUri.getUri());
         	wfList.add(wf);
         }
 
 		return Response.ok(wfList).build();
 	}
 
 
 
 	/**
 	 * PUT /{id}
 	 *
 	 *  (non-Javadoc)
 	 * @see eu.dm2e.ws.services.AbstractAsynchronousRDFService#putConfigToService(java.lang.String)
 	 */
 	@PUT
 	@Path("/{id}")
 	@Consumes({DM2E_MediaType.TEXT_PLAIN})
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 //		 DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE,
 		MediaType.APPLICATION_JSON
 	})
 	public Response putConfigToService(String workflowConfigURI) {
 
 		URI workflowUri = getRequestUriWithoutQuery();
 		/*
 		 * Build workflow
 		 */
 		WorkflowPojo workflowPojo = new WorkflowPojo();
 		try {
 			workflowPojo.loadFromURI(workflowUri);
 		} catch (Exception e2) {
 			return throwServiceError(e2);
 		}
 
 
 		/*
 		 * Resolve configURI to WebserviceConfigPojo
 		 */
 		log.warn("Loading workflow config wfConfig " + workflowConfigURI);
 		WorkflowConfigPojo wfConf = new WorkflowConfigPojo();
 		try {
 			wfConf.loadFromURI(workflowConfigURI, 1);
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 
 		/*
 		 * Validate the configuration
 		 */
 		log.warn("Validating workflow config");
 		try {
 			wfConf.validate();
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 
 		/*
 		 * Build WorkflowJobPojo
 		 */
 		WorkflowJobPojo jobPojo = new WorkflowJobPojo();
 		jobPojo.setWorkflow(workflowPojo);
 		jobPojo.setWorkflowConfig(wfConf);
 		log.info("WorkflowJobPojo constructed by WorkflowService: {}", jobPojo);
 		jobPojo.addLogEntry("WorkflowJobPojo constructed by WorkflowService", "TRACE");
 		try {
 			jobPojo.publishToService(client.getJobWebTarget());
 		} catch (Exception e1) {
 			return throwServiceError(e1);
 		}
 
 		/*
 		 * Let the asynchronous worker handle the job
 		 */
 		log.info("WorkflowJob is before instantiation :" + jobPojo);
 		try {
 			WorkflowService instance = this.getClass().newInstance();
 			Method method = getClass().getMethod("setWorkflowJobPojo", WorkflowJobPojo.class);
 			method.invoke(instance, jobPojo);
 			WorkerExecutorSingleton.INSTANCE.handleJob(instance);
 		} catch ( NoSuchMethodException |
 					InvocationTargetException |
 					InstantiationException |
 					IllegalAccessException
 					e) {
 			log.error("Could not initialize worker WorkflowService: " + e + ExceptionUtils.getFullStackTrace(e));
 			return throwServiceError(e);
 		} catch (Exception e) {
 			log.error("Could not initialize worker WorkflowService: " + e + ExceptionUtils.getFullStackTrace(e));
 			return throwServiceError(e);
 		}
 
 		/*
 		 * Return JobPojo
 		 */
 		return Response.status(202).location(jobPojo.getIdAsURI()).entity(jobPojo).build();
 	}
 
 
 	/**
 	 * GET /{id}
 	 * @return
 	 */
 	@GET
 	@Path("{id}")
 	public Response getWorkflow() {
 		URI wfUri = getRequestUriWithoutQuery();
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
 		if (g.isEmpty()) {
 			return Response.status(404).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		return Response.ok(wf).build();
 	}
 
 
     /**
      * GET {id}/blankConfig
      * @return
      */
     @GET
     @Path("{id}/blankConfig")
     public Response getEmptyConfigForWorkflow() {
 		URI wfUri = popPath(getRequestUriWithoutQuery());
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
 		if (g.isEmpty()) {
 			return Response.status(404).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
 		wfconf.setWorkflow(wf);
 		for (ParameterPojo inputParam : wf.getInputParams()) {
 			ParameterAssignmentPojo ass = wfconf.addParameterAssignment(inputParam.getId(), inputParam.getDefaultValue());
 			ass.setLabel(inputParam.getLabel());
 		}
 //		for (ParameterPojo inputParam : wf.getOutputParams()) {
 //			wfconf.addParameterAssignment(inputParam.getId(), "BLANK");
 //		}
 		return Response.ok().entity(wfconf).build();
     }
 
 	/**
 	 * PUT {workflowID} 	Accept: json
 	 * @param wf
 	 * @return
 	 */
 	@PUT
 	@Path("{workflowID}")
 	@Consumes({
 		MediaType.APPLICATION_JSON
 	})
 	public Response putWorkflowAsJSON(WorkflowPojo wf) {
 		return putGrafeo(wf.getGrafeo());
 	}
 
 	/**
 	 * PUT {workflowID}		Accept: rdf
 	 * TODO refactor
 	 * @param rdfString
 	 * @return
 	 */
 	@PUT
 	@Path("{workflowID}")
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response putRDF(String rdfString) {
 		Grafeo g = null;
 		try {
 			g = new GrafeoImpl();
 			g.readHeuristically(rdfString);
 			assert(g != null);
 		}
 		catch (Exception e) {
 			throwServiceError(e);
 		}
 		return this.putGrafeo(g);
 	}
 
 	public Response putGrafeo(Grafeo g) {
 		final URI wfUri = getRequestUriWithoutQuery();
 		final String wfUriStr = getRequestUriWithoutQuery().toString();
 
 		// TODO FIXME What if the user changed the default parameters defined in post?
 
 		log.info("Skolemnizing parameters.");
 		{
 			g.skolemizeByLabel(wfUriStr, NS.OMNOM.PROP_INPUT_PARAM, "param");
 			g.skolemizeByLabel(wfUriStr, NS.OMNOM.PROP_OUTPUT_PARAM, "param");
 		}
 
 		log.info("Skolemizing Positions.");
 		{
 			g.skolemizeSequential(wfUriStr, NS.OMNOM.PROP_WORKFLOW_POSITION, "position");
 		}
 
 		log.info("Skolemnizing Connectors.");
 		{
 			g.skolemizeSequential(wfUriStr, NS.OMNOM.PROP_PARAMETER_CONNECTOR, "connector");
 		}
 //		log.info("Skolemnizing WebserviceConfigs");
 //		{
 //			for (GResource subj : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
 //				g.skolemizeUUID(subj.toString(), NS.OMNOM.PROP_WEBSERVICE_CONFIG, "webserviceconfig");
 //			}
 //		}
 
 		log.info("Writing updated workflow to endpoint.");
 		try {
 			g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUriStr);
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 		log.info("DONE Writing updating workflow to endpoint: " + wfUriStr);
 
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUriStr);
 		return Response.ok(wf).location(wfUri).build();
 	}
 
 	/**
 	 * POST /				Accept: json
 	 * @param wf
 	 * @return
 	 */
 	@POST
 	@Consumes({
 		MediaType.APPLICATION_JSON
 	})
 	public Response postWorkflowAsJSON(WorkflowPojo wf) {
 //		WorkflowPojo wf = OmnomJsonSerializer.deserializeFromJSON(s, WorkflowPojo.class);
 		log.error(wf.getTerseTurtle());
 		return postGrafeo(wf.getGrafeo());
 	}
 
 	@Override
 	public Response postGrafeo(Grafeo g) {
 		GResource wfRes = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW);
 		log.trace("Workflow before Posting: " + g.getTerseTurtle());
 		log.info("Number of positions: " + g.listStatements(null, "omnom:hasPosition", null).size());
 
 		if (null == wfRes) {
 			return throwServiceError(NS.OMNOM.CLASS_WORKFLOW, ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		String wfUri = getRequestUriWithoutQuery() + "/" + createUniqueStr();
 		wfRes.rename(wfUri);
 
 		/*
 		 * Add global workflow parameters
 		 */
 		log.info("Adding global workflow parameters");
 		{
 			{
 				ParameterPojo pollTimeParam = new ParameterPojo();
 				pollTimeParam.setLabel(PARAM_POLL_INTERVAL);
 				pollTimeParam.setComment("Time to wait between polls for job status, in milliseconds. [Default: 2000ms]");
 				pollTimeParam.setDefaultValue("" + 2 * 1000);
 				GResource pollTimeParamRes = g.getObjectMapper().addObject(pollTimeParam);
 				g.addTriple(wfRes, NS.OMNOM.PROP_INPUT_PARAM, pollTimeParamRes);
 			}
 			{
 				ParameterPojo maxJobWaitParam = new ParameterPojo();
 				maxJobWaitParam.setLabel(PARAM_JOB_TIMEOUT);
 				maxJobWaitParam.setComment("Maximum time to wait for a job to finish, in seconds. [Default: 120s]");
 				maxJobWaitParam.setDefaultValue("" + 120);
 				GResource maxJobWaitParamRes = g.getObjectMapper().addObject(maxJobWaitParam);
 				g.addTriple(wfRes, NS.OMNOM.PROP_INPUT_PARAM, maxJobWaitParamRes);
 			}
 			{
 				ParameterPojo completeLogParam = new ParameterPojo();
 				completeLogParam.setLabel(PARAM_COMPLETE_LOG);
 				completeLogParam.setComment("The complete log file of the workflow job and its webservice jobs.");
 				GResource completeLogParamRes = g.getObjectMapper().addObject(completeLogParam);
 				g.addTriple(wfRes, NS.OMNOM.PROP_OUTPUT_PARAM, completeLogParamRes);
 			}
 		}
 
 		log.info("Skolemnizing parameters.");
 		{
 			g.skolemizeByLabel(wfUri, NS.OMNOM.PROP_INPUT_PARAM, "param");
 			g.skolemizeByLabel(wfUri, NS.OMNOM.PROP_OUTPUT_PARAM, "param");
 		}
 
 		log.info("Skolemizing Positions.");
 		{
 			g.skolemizeSequential(wfUri, NS.OMNOM.PROP_WORKFLOW_POSITION, "position");
 		}
 
 		log.info("Skolemnizing Connectors.");
 		{
 			g.skolemizeSequential(wfUri, NS.OMNOM.PROP_PARAMETER_CONNECTOR, "connector");
 		}
 //		log.info("Skolemnizing WebserviceConfigs");
 //		{
 //			for (GResource subj : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
 //				g.skolemizeUUID(subj.toString(), NS.OMNOM.PROP_WEBSERVICE_CONFIG, "webserviceconfig");
 //			}
 //		}
 
 		log.info("Writing workflow to config.");
 		try {
 			g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUri);
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 		log.info("Done Writing workflow to config: " + wfUri);
 
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 
 		try {
 			wf.validate();
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 
 		return Response.ok().entity(wf).location(URI.create(wfUri)).build();
 	}
 	@POST
 	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML,
 			DM2E_MediaType.APPLICATION_X_TURTLE, DM2E_MediaType.TEXT_PLAIN,
 			DM2E_MediaType.TEXT_RDF_N3, DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/position")
 	public Response postWorkflowPosition(@PathParam("workflowId") String workflowId, String rdfString) {
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_POSITION);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI workflowUri = popPath("position");
 		blank.rename(newUri);
 		// TODO rename blank sub-resources
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
 		return Response.created(newUri).build();
 	}
 	@PUT
 	@Path("{workflowId}/position/{posId}")
 	public Response putWorkflowPosition(@PathParam("workflowId") String workflowId, @PathParam("posId") String posId, String rdfString) {
 		URI posUri = getRequestUriWithoutQuery();
 		URI workflowUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(posUri + "?s ?o")
 			.insert(g)
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(workflowUri)
 			.build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	@GET
 	@Path("{workflowId}/position/{posId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowPosition(@PathParam("workflowId") String workflowId, @PathParam("posId") String posId) {
 		URI workflowUri = popPath(popPath());
 		return Response.seeOther(workflowUri).build();
 	}
 	@POST
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/connector")
 	public Response postWorkflowConnector(@PathParam("workflowId") String workflowId, String rdfString) { Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER_CONNECTOR);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI workflowUri = popPath("connector");
 		blank.rename(newUri);
 		// TODO rename blank sub-resources
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
 		return Response.created(newUri).build();
 	}
 	@PUT
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/connector/{conId}")
 	public Response putWorkflowConnector(@PathParam("workflowId") String workflowId, @PathParam("conId") String conId, String rdfString) {
 		URI conUri = getRequestUriWithoutQuery();
 		URI workflowUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(conUri + "?s ?o")
 			.insert(g.toString())
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(workflowUri).build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	@GET
 	@Path("{workflowId}/connector/{conId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowConnector( @PathParam("workflowId") String workflowId, @PathParam("conId") String conId) {
 		URI workflowUri = popPath(popPath());
 		return Response.seeOther(workflowUri).build();
 	}
 	@POST
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/param")
 	public Response postWorkflowParameter(@PathParam("workflowId") String workflowId, String rdfString) {
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI workflowUri = popPath();
 		blank.rename(newUri);
 		// TODO rename blank sub-resources
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
 		return Response.created(newUri).build();
 	}
 	@PUT
 	@Path("{workflowId}/param/{paramId}")
 	public Response putWorkflowParamaeter(@PathParam("workflowId") String workflowId, @PathParam("paramId") String paramId, String rdfString) {
 		URI conUri = getRequestUriWithoutQuery();
 		URI workflowUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(conUri + "?s ?o")
 			.insert(g.toString())
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(workflowUri).build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	@GET
 	@Path("{workflowId}/param/{paramId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowParameter( @PathParam("workflowId") String workflowId, @PathParam("paramId") String paramId) {
 		URI workflowUri = popPath(popPath());
 		return Response.seeOther(workflowUri).build();
 	}
 
 	/**
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		WorkflowJobPojo workflowJob = this.getWorkflowJobPojo();
 		WorkflowConfigPojo workflowConfig = workflowJob.getWorkflowConfig();
 		WorkflowPojo workflow = workflowJob.getWorkflowConfig().getWorkflow();
 		Map<String,JobPojo> finishedJobs = new HashMap<>();
 		try {
 			try {
 				workflowJob.getId().toString();
 				workflow.getId().toString();
 				workflowConfig.getId().toString();
 				log.info("Workflow in run before validation: " + workflow.getTerseTurtle());
 			} catch (NullPointerException e) {
 				throw e;
 			}
 
 			log.info("Job used in run(): " + workflowJob);
 
 			/*
 			 * Validate
 			 */
 			try {
 				workflowConfig.validate();
 			} catch (Throwable t) {
 				throw(t);
 			}
 
 			/*
 			 * Get global meta parameters (pollinterval, jobtimeout ...)
 			 */
 			long pollInterval = Long.parseLong(workflow.getParamByName(PARAM_POLL_INTERVAL).getDefaultValue());
 			if (null != workflowConfig.getParameterAssignmentForParam(PARAM_POLL_INTERVAL)) {
 				pollInterval = Long.parseLong(workflowConfig.getParameterAssignmentForParam(PARAM_POLL_INTERVAL).getParameterValue());
 			}
 			long jobTimeoutInterval = Long.parseLong(workflow.getParamByName(PARAM_JOB_TIMEOUT).getDefaultValue());
 			if (null != workflowConfig.getParameterAssignmentForParam(PARAM_JOB_TIMEOUT)) {
 				jobTimeoutInterval = Long.parseLong(workflowConfig.getParameterAssignmentForParam(PARAM_JOB_TIMEOUT).getParameterValue());
 			}
 
 			/*
 			 *
 			 */
 			workflowJob.setStarted();
 
 			/*
 			 * Iterate Positions
 			 */
 			for (WorkflowPositionPojo pos : workflow.getPositions()) {
 				WebservicePojo ws = pos.getWebservice();
 				workflowJob.addLogEntry("Re-loading webservice description", "TRACE");
 				ws.loadFromURI(ws.getId());
 				WebserviceConfigPojo wsconf = new WebserviceConfigPojo();
 				wsconf.setWebservice(ws);
 				wsconf.setWasGeneratedBy(workflowJob);
 
 				/*
 				 * Iterate Input Parameters of the Webservice at this position
 				 */
 				workflowJob.addLogEntry("About to iterate parameters", "TRACE");
 				nextParam:
 				for (ParameterPojo param : ws.getInputParams()) {
 					workflowJob.trace("Generating assignment for param " + param);
 //					workflowJob.addLogEntry("Current param: " + param, "TRACE");
 					workflowJob.publishToService();
 					log.trace("Current param: " + param);
 
 					// if there is a connector to this parameter at this position
 					ParameterConnectorPojo conn = workflow.getConnectorToPositionAndParam(pos, param);
 					if (null == conn) continue nextParam;
 
 					final ParameterAssignmentPojo ass;
 					if (conn.hasFromWorkflow()) {
 						// FIXME this is not working for whatever reason
 						// if the connector is from the workflow, take the value assigned to the workflow parameter
 						ass = workflowConfig.getParameterAssignmentForParam(conn.getFromParam());
 					} else {
 						// if the connector is from a previous position, take the value from the corresponding previous job
 						ass = finishedJobs.get(conn.getFromPosition().getId()).getOutputParameterAssignmentForParam(conn.getFromParam());
 						workflowJob.debug("Finished Jobs: " + finishedJobs.keySet());
 						workflowJob.debug("This connector fromPosition: " + conn.getFromPosition());
 					}
 					if (ass == null) {
 						workflowJob.debug(workflowConfig.getTerseTurtle());
 						throw new RuntimeException("Couldn't get the assignment for param " + param);
 					}
 					wsconf.addParameterAssignment(param.getId(), ass.getParameterValue());
 				}
 
 				/*
 				 * Publish the WebserviceConfig, so it becomes stable
 				 */
 				wsconf.resetId();
 				wsconf.setExecutesPosition(pos);
 				wsconf.publishToService(client.getConfigWebTarget());
 				if (null == wsconf.getId()) {
 					throw new RuntimeException("Could not publish webservice config " + wsconf);
 				}
 
 				/*
 				 * Run the webservice
 				 */
 				Response resp = client.target(ws.getId())
 						.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
 						.put(Entity.text(wsconf.getId()));
 				if (202 != resp.getStatus() || null == resp.getLocation()) {
 //					workflowJob.debug(wsconf.getTerseTurtle());
 					throw new RuntimeException("Request to start web service " + ws + " with config " + wsconf + "failed: " + resp);
 				}
 
 
 				/*
 				 * start the job
 				 */
 				long timePassed = 0;
 				JobPojo webserviceJob = new JobPojo(resp.getLocation());
 
 				// persist change of the workflow job
 				workflowJob.getRunningJobs().add(webserviceJob);
 				workflowJob.publishToService();
 				do {
 					webserviceJob.loadFromURI(webserviceJob.getId());
 					workflowJob.trace("Sleeping for " + pollInterval + "ms, waiting for job " + webserviceJob + " to finish.");
 					try {
 						Thread.sleep(pollInterval);
 					} catch (Exception e) {
 						throw new RuntimeException(e);
 					}
 					timePassed += pollInterval;
 					if (timePassed > jobTimeoutInterval*1000) {
 						throw new RuntimeException("Job " + webserviceJob + " took more than " + jobTimeoutInterval + "s too long to finish :(");
 					}
 					log.info("JOB STATUS: " +webserviceJob.getTerseTurtle());
 					
 				} while (webserviceJob.isStillRunning());
 
 				// just clearing the runningjobs set will be a problem for parallel jobs
 				workflowJob.getRunningJobs().remove(webserviceJob);
 
 				finishedJobs.put(pos.getId(), webserviceJob);
 				workflowJob.getFinishedJobs().add(webserviceJob);
 				workflowJob.getFinishedPositions().add(pos);
 
 				workflowJob.publishToService();
 				if (webserviceJob.isFailed()) {
 					throw new RuntimeException("Job " + webserviceJob + " of Webservice " + ws + "failed, hence workflow " + workflow + "failed. :(");
 				}
 				else if (webserviceJob.isFinished()) {
 					workflowJob.info("Job " + webserviceJob + " of Webservice " + ws + "finished successfully, moving on to next position.");
 				}
 				workflowJob.publishToService();
 
 			} // end position loop
 
 			// TODO
 			workflowJob.setFinished();
 		} catch (Throwable t) {
 			log.error("Workflow " + workflowJob +  " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
 			workflowJob.fatal("Workflow " + workflowJob +  " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
 			workflowJob.setFailed();
 			// TODO why can't I throw this here but in AbstractTransformationService??
 			// throw t
 		} finally {
 			JobPojo dummyJob = new JobPojo();
 			Set<AbstractJobPojo> allLoggingJobs = new HashSet<>();
 			allLoggingJobs.addAll(finishedJobs.values());
 			allLoggingJobs.add(workflowJob);
 			for (AbstractJobPojo job : allLoggingJobs) {
 				for (LogEntryPojo logEntry : job.getLogEntries()) {
 					LogEntryPojo newLogEntry = new LogEntryPojo();
 					newLogEntry.setTimestamp(logEntry.getTimestamp());
 					newLogEntry.setLevel(logEntry.getLevel());
 					newLogEntry.setMessage( job + ": " + logEntry.getMessage());
 					dummyJob.getLogEntries().add(newLogEntry);
 				}
 			}
 			workflowJob.addOutputParameterAssignment(PARAM_COMPLETE_LOG, dummyJob.toLogString());
 			workflowJob.publishToService();
 		}
 	}
 }

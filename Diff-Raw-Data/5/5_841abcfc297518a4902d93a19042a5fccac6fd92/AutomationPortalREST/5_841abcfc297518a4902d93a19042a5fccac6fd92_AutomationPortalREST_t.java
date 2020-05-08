 package com.redhat.automationportal.rest;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.redhat.automationportal.scripts.BugzillaReportGenerator;
 import com.redhat.automationportal.scripts.ParseToc;
 import com.redhat.automationportal.scripts.RegenSplash;
 import com.redhat.automationportal.scripts.svnstats.ConfigXMLData;
 import com.redhat.automationportal.scripts.svnstats.SvnStats;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 
 @Path("/")
 public class AutomationPortalREST
 {
 	@GET
 	@Consumes("text/plain")
 	@Produces("application/json")
 	@Path("/BugzillaReportGenerator/get/json/Execute")
 	public Response BugzillaReportGeneratorGetJson(@QueryParam("bugzillaUsername") final String bugzillaUsername, @QueryParam("bugzillaPassword") final String bugzillaPassword, @HeaderParam("Referer") final String refererHeader, @HeaderParam("Origin") final String originHeader)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.BugzillaReportGeneratorGetJson()");
 
 			final BugzillaReportGenerator script = new BugzillaReportGenerator();
 			script.setBugzillaPassword(bugzillaPassword);
 			script.setBugzillaUsername(bugzillaUsername);
 			final boolean result = script.run();
 
 			final String message = script.getMessage();
 			final String output = script.getOutput();
 
 			logger.info("AutomationPortalREST.BugzillaReportGeneratorGetJson() message: " + message);
 			logger.info("AutomationPortalREST.BugzillaReportGeneratorGetJson() output: " + output);
 
 			return Response.status(result ? 200 : 500)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(new AutomationPortalResponseData(message, output)).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.BugzillaReportGeneratorGetJson()");
 		}
 
 	}
 
 	@GET
 	@Consumes("text/plain")
 	@Produces("application/json")
 	@Path("/ParseTOC/get/json/Execute")
 	public Response ParseTOCGetJson(@HeaderParam("Referer") final String refererHeader, @HeaderParam("Origin") final String originHeader)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.ParseTOCGetJson()");
 
 			final ParseToc script = new ParseToc();
 			final boolean result = script.run();
 
 			final String message = script.getMessage();
 			final String output = script.getOutput();
 
 			logger.info("AutomationPortalREST.ParseTOCGetJson() message: " + message);
 			logger.info("AutomationPortalREST.ParseTOCGetJson() output: " + output);
 
 			return Response.status(result ? 200 : 500)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(new AutomationPortalResponseData(message, output)).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.ParseTOCGetJson()");
 		}
 
 	}
 
 	@GET
 	@Consumes("text/plain")
 	@Produces("application/json")
 	@Path("/RegenSplash/get/json/Execute")
 	public Response RegenSplashGetJson(@QueryParam("tocUrl") final String tocUrl, @QueryParam("productName") final String productName, @QueryParam("username") final String username, @QueryParam("password") final String password, @HeaderParam("Referer") final String refererHeader,
 			@HeaderParam("Origin") final String originHeader)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.RegenSplashGetJson()");
 
 			final RegenSplash script = new RegenSplash();
 			script.setUsername(username);
 			script.setPassword(password);
 			script.setProduct(productName);
 			script.setSelectedSite(tocUrl);
 
 			final boolean result = script.run();
 
 			final String message = script.getMessage();
 			final String output = script.getOutput();
 
 			logger.info("AutomationPortalREST.RegenSplashGetJson() message: " + message);
 			logger.info("AutomationPortalREST.RegenSplashGetJson() output: " + output);
 
 			return Response.status(result ? 200 : 500)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(new AutomationPortalResponseData(message, output)).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.RegenSplashGetJson()");
 		}
 
 	}
 
 	@GET
 	@Consumes("text/plain")
 	@Produces("application/json")
 	@Path("/RegenSplash/get/json/Sites")
 	public Response RegenSplashGetJsonSites(@HeaderParam("Referer") final String refererHeader, @HeaderParam("Origin") final String originHeader)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.RegenSplashGetJsonSites()");
 
 			final RegenSplash script = new RegenSplash();
 			final List<StringPair> sites = script.getSites();
 
 			return Response.status(200)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(sites).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.RegenSplashGetJsonSites()");
 		}
 
 	}
 
 	@GET
 	@Consumes("text/plain")
 	@Produces("application/json")
 	@Path("/RegenSplash/get/json/Products")
 	public Response RegenSplashGetJsonProducts(@QueryParam("tocUrl") final String tocUrl, @HeaderParam("Referer") final String refererHeader, @HeaderParam("Origin") final String originHeader)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.RegenSplashGetJsonProducts()");
 
 			final RegenSplash script = new RegenSplash();
 			final List<String> products = script.getProducts(tocUrl);
 
 			return Response.status(200)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(products).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.RegenSplashGetJsonProducts()");
 		}
 
 	}
 
 	@GET
 	@Consumes("text/plain")
 	@Produces("application/json")
 	@Path("/SVNStats/get/json/Execute")
 	public Response SVNStatsGetJson(@QueryParam("entries") final String entriesJson, @HeaderParam("Referer") final String refererHeader, @HeaderParam("Origin") final String originHeader)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.SVNStatsGetJson()");
 
 			final ObjectMapper mapper = new ObjectMapper();
 			final Object raw = mapper.readValue(entriesJson, Object.class);
 			
 			final ArrayList<ConfigXMLData> entries = new ArrayList<ConfigXMLData>();
 			if (raw instanceof List<?>)
			{
 				CollectionUtilities.addAll(mapper.convertValue(raw, ConfigXMLData[].class), entries);
 			}
 			else
			{
 				entries.add(mapper.convertValue(raw, ConfigXMLData.class));
 			}
 
 			final SvnStats script = new SvnStats();
 
 			final boolean result = script.run(entries);
 
 			final String message = script.getMessage();
 			final String output = script.getOutput();
 
 			logger.info("AutomationPortalREST.SVNStatsGetJson() message: " + message);
 			logger.info("AutomationPortalREST.SVNStatsGetJson() output: " + output);
 
 			return Response.status(result ? 200 : 500)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(new AutomationPortalResponseData(message, output)).build();
 		}
 		catch (final Exception ex)
 		{
 			return Response.status(500)
 			/* CORS header allowing cross-site requests */
 			.header("Access-Control-Allow-Origin", originHeader).entity(new AutomationPortalResponseData(ex.getMessage(), ex.toString())).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.SVNStatsGetJson()");
 		}
 
 	}
 
 }

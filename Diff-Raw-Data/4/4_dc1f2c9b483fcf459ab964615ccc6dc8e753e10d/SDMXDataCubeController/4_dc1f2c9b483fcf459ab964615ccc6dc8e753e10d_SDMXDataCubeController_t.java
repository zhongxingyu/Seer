 /*
  * Copyright / LIRMM 2013
  * Contributor(s) : T. Colas, T. Marmin
  *
  * Contact: thibaut.marmin@etud.univ-montp2.fr
  * Contact: thibaud.colas@etud.univ-montp2.fr
  *
  * This software is governed by the CeCILL license under French law and
  * abiding by the rules of distribution of free software. You can use,
  * modify and/or redistribute the software under the terms of the CeCILL
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info".
  *
  * As a counterpart to the access to the source code and rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty and the software's author, the holder of the
  * economic rights, and the successive licensors have only limited
  * liability.
  *
  * In this respect, the user's attention is drawn to the risks associated
  * with loading, using, modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean that it is complicated to manipulate, and that also
  * therefore means that it is reserved for developers and experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or
  * data to be ensured and, more generally, to use and operate it in the
  * same conditions as regards security.
  *
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL license and that you accept its terms.
  */
 
 package org.datalift.sdmxdatacube;
 
 import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.datalift.fwk.MediaTypes;
 import org.datalift.fwk.project.Project;
 import org.datalift.fwk.project.Source;
 import org.datalift.fwk.project.TransformedRdfSource;
 import org.datalift.fwk.project.XmlSource;
 import org.datalift.fwk.view.TemplateModel;
 import org.datalift.sdmxdatacube.jsontransporter.MessageTransporter;
 import org.datalift.sdmxdatacube.utils.ControllerHelper;
 import org.openrdf.rio.RDFFormat;
 import org.sdmxsource.rdf.model.RDFStructureOutputFormat;
 import org.sdmxsource.sdmx.api.model.StructureFormat;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.io.DefaultResourceLoader;
 import static org.datalift.fwk.util.StringUtils.*;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.sun.jersey.api.core.DefaultResourceConfig;
 
 /**
  * The SDMX DataCube module's main class which exposes the SDMXRDFParser engine
  * to the Datalift architecture.
  *
  * @author T. Colas, T. Marmin
  * @version 090213
  */
 @Path(SDMXDataCubeController.MODULE_NAME)
 public class SDMXDataCubeController extends ModuleController {
 	// -------------------------------------------------------------------------
 	// Constants
 	// -------------------------------------------------------------------------
 
 	/** The module's name. */
 	public static final String MODULE_NAME = "sdmxdatacube";
 	public final static int MODULE_POSITION = 6000;
 
 	public final static boolean VIEW_RESULTS_DEFAULT = true;
 
 	// -------------------------------------------------------------------------
 	// Instance members
 	// -------------------------------------------------------------------------
 
 	protected SDMXDataCubeModel model;
 
 	StructureFormat structureFormat;
 
 	// SDMXDataCubeTransformer rdfDataTransformer;
 
 	// -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
 
 	/**
 	 * Creates a new SDMXDataCubeController instance, sets its button position.
 	 */
 	public SDMXDataCubeController() {
 		// TODO Switch to the right position.
 		super(MODULE_NAME, MODULE_POSITION);
 		model = new SDMXDataCubeModel(MODULE_NAME);
 
 		LOG.debug("Current classpath: {}",
 				System.getProperties().getProperty("java.class.path", null));
 
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
 		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
		//ctx.setClassLoader(this.getClass().getClassLoader());
 		ctx.setConfigLocation("spring/spring-beans.xml");
 		ctx.refresh();
 
 		// rdfDataTransformer = ctx.getBean(SDMXDataCubeTransformer.class);
 
 		structureFormat = new RDFStructureOutputFormat(RDFFormat.TURTLE);
 	}
 
 	// -------------------------------------------------------------------------
 	// Project management
 	// -------------------------------------------------------------------------
 
 	/**
 	 * Tells the project manager to add a new button to projects with at least
 	 * two sources.
 	 *
 	 * @param p
 	 *            Our current project.
 	 * @return The URI to our project's main page.
 	 */
 	@Override
 	public UriDesc canHandle(Project p) {
 		UriDesc projectPage = null;
 		try {
 			// The project can be handled if it has at least one SDMX source.
 			// TODO enlever pseudo condition here
 			if (true || model.hasMultipleValidSources(p, 1)) {
 				// link URL, link label
 				projectPage = new UriDesc(this.getName() + "?project="
 						+ p.getUri(), HttpMethod.GET,
 						getTranslatedResource(MODULE_NAME + ".button"));
 				projectPage.setPosition(this.MODULE_POSITION);
 
 				LOG.debug("Project {} can use SDMXToDataCube", p.getTitle());
 			} else {
 				LOG.debug("Project {} cannot use SDMXToDataCube", p.getTitle());
 			}
 		} catch (URISyntaxException e) {
 			LOG.fatal("Failed to check status of project {}: {}", e,
 					p.getUri(), e.getMessage());
 		}
 		return projectPage;
 	}
 
 	// -------------------------------------------------------------------------
 	// Web services
 	// -------------------------------------------------------------------------
 
 	/**
 	 * Index page handler of the SDMXToDataCube module.
 	 *
 	 * @param projectId
 	 *            the project using SDMXToDataCube
 	 * @return Our module's interface.
 	 */
 	@GET
 	@Produces({ MediaTypes.TEXT_HTML_UTF8, MediaTypes.APPLICATION_XHTML_XML })
 	public Response getIndexPage(@QueryParam("project") java.net.URI projectId) {
 		Response response = null;
 		try {
 			// Retrieve project.
 			Project p = this.getProject(projectId);
 			// Display conversion configuration page.
 			TemplateModel view = this.newView("convert-form.vm", p);
 			view.put("helper", new ControllerHelper(model));
 			view.put("viewResults", VIEW_RESULTS_DEFAULT);
 
 			response = Response.ok(view, MediaTypes.TEXT_HTML_UTF8).build();
 		} catch (IllegalArgumentException e) {
 			TechnicalException error = new TechnicalException(
 					"ws.invalid.param.error", "project", projectId);
 			this.sendError(Status.BAD_REQUEST, error.getLocalizedMessage());
 		}
 		return response;
 	}
 
 	/**
 	 * Form submit handler : launching SDMXDataCube.
 	 *
 	 * @param project
 	 *            the project using SDMXDataCube.
 	 * @param inputSourceURI
 	 *            context of our source (reference) data.
 	 * @param dest_title
 	 *            name of the source which will be created.
 	 * @param dest_graph_uri
 	 *            URI of the source (graph) which will be created to store the
 	 *            result.
 	 * @param vizualisation
 	 * @return An empty HTTP response (code 201) if OK else an error.
 	 * @throws WebApplicationException
 	 */
 	@POST
 	@Path("/")
 	@Consumes(MediaTypes.APPLICATION_FORM_URLENCODED)
 	@Produces(MediaTypes.TEXT_PLAIN)
 	public Response doSubmit(@FormParam("project") String project,
 			@FormParam("source") String source,
 			@FormParam("dest_title") String dest_title,
 			@FormParam("dest_graph_uri") String dest_graph_uri,
 			@FormParam("vizualisation") boolean vizualisation)
 			throws WebApplicationException {
 
 		LOG.debug("soSubmit : validate");
 		MessageTransporter transporter = this.validate(project, source,
 				dest_graph_uri, dest_title, vizualisation);
 
 		if (!transporter.isValid()) {
 			this.sendError(BAD_REQUEST, transporter.getGlobal());
 		}
 
 		URI destUri = null;
 
 		LOG.debug("soSubmit : validation ok");
 		// Lauch SDMX to Datacube process
 		try {
 			Project p = this.getProject(new java.net.URI(project));
 			LOG.debug("doSubmit : get project ok");
 			XmlSource s = (XmlSource) p.getSource(source);
 			LOG.debug("doSubmit : get source ok");
 			URI projectUri = new URI(p.getUri());
 			destUri = new URI(projectUri.getScheme(), null,
 					projectUri.getHost(), projectUri.getPort(),
 					this.getSourceId(projectUri, dest_title), null, null);
 
 			TransformedRdfSource destSource = this.projectManager
 					.newTransformedRdfSource(p, destUri, dest_title, null,
 							new URI(dest_graph_uri), s);
 
 			LOG.debug("Destination source URI {}", destSource.getUri());
 
 			try {
 				model.lauchSdmxToDatacubeProcess(p, s, destSource);
 			} catch (Exception e) {
 				p.remove(destSource);
 				throw e;
 			}
 
 			this.projectManager.saveProject(p);
 		} catch (URISyntaxException e) {
 			LOG.fatal(e);
 			this.sendError(Status.BAD_REQUEST, e.getMessage());
 		} catch (Exception e) {
 			LOG.fatal(e);
 			this.sendError(Status.INTERNAL_SERVER_ERROR, e.getMessage());
 		}
 
 		return Response.created(destUri).build();
 	}
 
 	private String getSourceId(URI projectUri, String sourceName) {
 		return projectUri.getPath() + "/source/" + urlify(sourceName);
 	}
 
 	/**
 	 * Form validation handler : validate de form.
 	 *
 	 * @param project
 	 *            the project using SDMXDataCube.
 	 * @param inputSourceURI
 	 *            context of our source (reference) data.
 	 * @param dest_title
 	 *            name of the source which will be created.
 	 * @param dest_graph_uri
 	 *            URI of the source (graph) which will be created to store the
 	 *            result.
 	 * @param vizualisation
 	 * @return A json containing error messages.
 	 * @throws WebApplicationException
 	 */
 	@POST
 	@Path("/validate")
 	@Consumes(MediaTypes.APPLICATION_FORM_URLENCODED)
 	@Produces(MediaTypes.APPLICATION_JSON)
 	public Response doValidate(@FormParam("project") String project,
 			@FormParam("source") String source,
 			@FormParam("dest_title") String dest_title,
 			@FormParam("dest_graph_uri") String dest_graph_uri,
 			@FormParam("vizualisation") boolean vizualisation)
 			throws WebApplicationException {
 
 		Gson gson = new GsonBuilder().create();
 
 		MessageTransporter transporter = this.validate(project, source,
 				dest_graph_uri, dest_title, vizualisation);
 
 		int statusCode = 200;
 		if (!transporter.isValid())
 			statusCode = 400;
 
 		return Response.status(statusCode).entity(gson.toJson(transporter))
 				.build();
 	}
 
 	private MessageTransporter validate(String project, String source,
 			String dest_graph_uri, String dest_title, boolean vizualisation) {
 
 		MessageTransporter transporter = new MessageTransporter();
 
 		if (project != null) {
 			Project p = null;
 			try {
 				p = this.getProject(new java.net.URI(project));
 			} catch (Exception e) {
 				transporter.setError("project",
 						getTranslatedResource("error.projectId.unidentifiable")
 								+ " (" + project + ")");
 			}
 
 			if (p != null) {
 
 				// Check inputSourceURI
 				if (!(source == null) && !source.isEmpty()) {
 					Source s = null;
 					try {
 						s = p.getSource(source);
 					} catch (Exception e) {
 						transporter
 								.setError(
 										"source",
 										getTranslatedResource("error.inputSource.unknown")
 												+ " (" + source + ")");
 					}
 
 					if (s == null)
 						transporter
 								.setError(
 										"source",
 										getTranslatedResource("error.inputSource.unknown")
 												+ " (" + source + ")");
 					else if (!model.isValidSource(p.getSource(source)))
 						transporter
 								.setError(
 										"source",
 										getTranslatedResource("error.inputSource.notsdmx")
 												+ " (" + source + ")");
 
 				} else {
 					transporter.setError("source",
 							getTranslatedResource("error.inputSource.empty"));
 				}
 
 				// Check outputSourceURI
 				if (dest_graph_uri == null || dest_title.isEmpty())
 					transporter
 							.setError(
 									"dest_graph_uri",
 									getTranslatedResource("error.outputSourceURI.empty"));
 
 				try {
 					if (p.getSource(dest_graph_uri) != null)
 						transporter
 								.setError(
 										"dest_graph_uri",
 										getTranslatedResource("error.outputSourceURI.alreadyexists")
 												+ " (" + dest_graph_uri + ")");
 				} catch (Exception e) {
 					transporter
 							.setError(
 									"dest_graph_uri",
 									getTranslatedResource("error.outputSourceURI.malformed")
 											+ " (" + dest_graph_uri + ")");
 				}
 
 				// Check outputSourceName
 
 				if (dest_title == null || dest_title.isEmpty())
 					transporter
 							.setError(
 									"dest_graph_title",
 									getTranslatedResource("error.outputSourceName.empty"));
 				else {
 
 					Collection<Source> sources = p.getSources();
 					boolean already_exists = false;
 
 					for (Source s : sources) {
 						if (s.getTitle().equals(dest_title)) {
 							already_exists = true;
 							break;
 						}
 					}
 
 					if (already_exists)
 						transporter
 								.setError(
 										"dest_graph_title",
 										getTranslatedResource("error.outputSourceName.alreadyexists"));
 				}
 			}
 		} else {
 			transporter.setError("project",
 					getTranslatedResource("error.projectId.empty"));
 		}
 
 		return transporter;
 	}
 
 	// private final static class RequestValidatorJsonStreamingOutput implements
 	// StreamingOutput {
 	// private final MessageTransporter v;
 	//
 	// public RequestValidatorJsonStreamingOutput(MessageTransporter v) {
 	// this.v = v;
 	// }
 	//
 	// /** {@inheritDoc} */
 	// @Override
 	// public void write(OutputStream output) throws IOException,
 	// WebApplicationException {
 	// Gson gson = new GsonBuilder().create();
 	//
 	// Writer w = new OutputStreamWriter(output, Charsets.UTF_8);
 	// gson.toJson(this.v, w);
 	// w.flush();
 	// }
 	// }
 }

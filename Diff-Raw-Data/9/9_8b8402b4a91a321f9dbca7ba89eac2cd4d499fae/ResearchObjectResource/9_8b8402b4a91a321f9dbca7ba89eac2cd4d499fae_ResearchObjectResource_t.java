 package pl.psnc.dl.wf4ever.rosrsfull;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Set;
 
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.transform.TransformerException;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.ForbiddenException;
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
 import pl.psnc.dl.wf4ever.dlibra.ConflictException;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 import pl.psnc.dlibra.service.DLibraException;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * 
  * @author nowakm
  * 
  */
 @Path(("workspaces" + "/{W_ID}" + "/ROs" + "/{RO_ID}/"))
 public class ResearchObjectResource
 {
 
 	@SuppressWarnings("unused")
 	private final static Logger logger = Logger
 			.getLogger(ResearchObjectResource.class);
 
 	@Context
 	HttpServletRequest request;
 
 	@Context
 	UriInfo uriInfo;
 
 
 	/**
 	 * Returns list of versions of this research object.
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @return 200 (OK) response code with a rdf file in response body
 	 *         containing OAI-ORE aggreagates tags.
 	 * @throws RemoteException
 	 * @throws DLibraException
 	 * @throws TransformerException
 	 * @throws UnknownHostException
 	 * @throws MalformedURLException
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 */
 	@GET
 	@Produces("text/plain")
 	public Response getListOfVersions(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId)
 		throws RemoteException, TransformerException, MalformedURLException,
 		UnknownHostException, DigitalLibraryException, NotFoundException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 		List<String> list = dl.getVersionIds(workspaceId, researchObjectId);
 
 		StringBuilder sb = new StringBuilder();
 		for (String id : list) {
 			sb.append(uriInfo.getAbsolutePathBuilder().path("/").build()
 					.resolve(id).toString());
 			sb.append("\r\n");
 		}
 
 		ContentDisposition cd = ContentDisposition.type("text/plain")
 				.fileName(researchObjectId + ".rdf").build();
 
 		return Response.ok().entity(sb.toString())
 				.header("Content-disposition", cd).build();
 	}
 
 
 	/**
 	 * Creates new version. Input is RO_VERSION_ID and optional URI of the base
 	 * version that should be used to create a new version.
 	 * 
 	 * @param workspaceId
 	 * @param researchObjectId
 	 * @param data
 	 *            Input format is text/plain with RO_VERSION_ID in first line
 	 *            and base version URI in second (optional).
 	 * @return 201 (Created) if the version was created, 409 (Conflict) if
 	 *         version with given RO_VERSION_ID already exists
 	 * @throws IOException
 	 * @throws TransformerException
 	 * @throws URISyntaxException
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 * @throws SQLException 
 	 * @throws NamingException 
 	 * @throws ClassNotFoundException 
 	 * @throws ConflictException 
 	 * @throws SAXException
 	 */
 	@POST
 	@Consumes("text/plain")
 	public Response createVersion(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId, String data)
 		throws IOException, TransformerException, URISyntaxException,
 		DigitalLibraryException, NotFoundException, ClassNotFoundException,
 		NamingException, SQLException, ConflictException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		if (user.getRole() == UserProfile.Role.PUBLIC) {
 			throw new ForbiddenException(
 					"Only authenticated users can do that.");
 		}
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		String lines[] = data.split("[\\r\\n]+");
 		if (lines.length < 1) {
 			return Response.status(Status.BAD_REQUEST)
 					.entity("Content is shorter than 2 lines")
 					.header("Content-type", "text/plain").build();
 		}
 		String version = lines[0];
 		String baseVersion = null;
 		URI baseVersionURI = null;
 		if (lines.length > 1) {
 			baseVersionURI = new URI(lines[1]);
 			URI roUri = baseVersionURI.resolve(".");
 			baseVersion = roUri.relativize(baseVersionURI).toString();
 		}
 
 		URI roURI = uriInfo.getAbsolutePathBuilder().path(version).path("/")
 				.build();
 
 		SemanticMetadataService sms = SemanticMetadataServiceFactory
 				.getService(user);
 		try {
 			if (baseVersion == null) {
 				sms.createResearchObject(roURI);
				dl.createVersion(workspaceId, researchObjectId, version);
 			}
 			else {
 				dl.createVersion(workspaceId, researchObjectId, version,
 					baseVersion);
 				//				sms.createResearchObjectAsCopy(resourceURI, baseVersionURI);
 			}
 		}
		catch (IllegalArgumentException e) {
			// RO already existed in sms, maybe created by someone else
			throw new ConflictException("The RO with identifier "
					+ researchObjectId + " already exists");
		}
 		finally {
 			sms.close();
 		}
 		return Response.created(roURI).build();
 	}
 
 
 	/**
 	 * Deletes the research object.
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 * @throws SQLException 
 	 * @throws NamingException 
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 */
 	@DELETE
 	public void deleteResearchObject(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId)
 		throws DigitalLibraryException, NotFoundException,
 		ClassNotFoundException, IOException, NamingException, SQLException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		if (user.getRole() == UserProfile.Role.PUBLIC) {
 			throw new ForbiddenException(
 					"Only authenticated users can do that.");
 		}
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		dl.deleteResearchObject(workspaceId, researchObjectId);
 
 		SemanticMetadataService sms = SemanticMetadataServiceFactory
 				.getService(user);
 		try {
 			Set<URI> versions = sms.findResearchObjects(uriInfo
 					.getAbsolutePath());
 			for (URI uri : versions) {
 				sms.removeResearchObject(uri);
 			}
 		}
 		finally {
 			sms.close();
 		}
 	}
 }

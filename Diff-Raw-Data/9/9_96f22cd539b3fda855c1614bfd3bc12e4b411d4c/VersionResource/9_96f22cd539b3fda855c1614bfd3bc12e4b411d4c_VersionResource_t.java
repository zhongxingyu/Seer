 package pl.psnc.dl.wf4ever.rosrsfull;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.Set;
 
 import javax.naming.NamingException;
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.transform.TransformerException;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.AuthenticationException;
 import pl.psnc.dl.wf4ever.auth.SecurityFilter;
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.Snapshot;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 import pl.psnc.dlibra.service.IdNotFoundException;
 
 import com.hp.hpl.jena.shared.JenaException;
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * 
  * @author nowakm
  * 
  */
 @Path(("workspaces" + "/{W_ID}" + "/ROs" + "/{RO_ID}" + "/{RO_VERSION_ID}"))
 public class VersionResource
 {
 
 	private final static Logger logger = Logger
 			.getLogger(VersionResource.class);
 
 	@Context
 	private HttpServletRequest request;
 
 	@Context
 	private UriInfo uriInfo;
 
 
 	/**
 	 * Returns zip archive with contents of RO version.
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @param versionId
 	 *            identifier of version of RO - defined by the user
 	 * @return
 	 * @throws IOException
 	 * @throws DigitalLibraryException
 	 * @throws IdNotFoundException
 	 * @throws OperationNotSupportedException
 	 * @throws NotFoundException
 	 */
 	@GET
 	@Produces("application/zip")
 	public Response getZippedRO(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId, @PathParam("RO_VERSION_ID")
 	String versionId, @QueryParam("edition_id")
 	@DefaultValue(Constants.EDITION_QUERY_PARAM_DEFAULT_STRING)
 	long editionId, @QueryParam("edition_list")
 	String isEditionListRequested)
 		throws IOException, DigitalLibraryException,
 		OperationNotSupportedException, NotFoundException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		if (isEditionListRequested != null) {
 			return getEditionList(workspaceId, researchObjectId, versionId, dl);
 		}
 		else {
 			return getZippedPublication(workspaceId, researchObjectId,
 				versionId, dl, editionId);
 		}
 	}
 
 
 	/**
 	 * @param researchObjectId
 	 * @param versionId
 	 * @param dLibraDataSource
 	 * @param editionId
 	 * @return
 	 * @throws RemoteException
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 */
 	private Response getZippedPublication(String workspaceId,
 			String researchObjectId, String versionId,
 			DigitalLibrary dLibraDataSource, long editionId)
 		throws RemoteException, DigitalLibraryException, NotFoundException
 	{
 		InputStream body;
 		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
 			body = dLibraDataSource.getZippedVersion(workspaceId,
 				researchObjectId, versionId);
 		}
 		else {
 			body = dLibraDataSource.getZippedVersion(workspaceId,
 				researchObjectId, versionId, editionId);
 		}
 		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(researchObjectId + "-" + versionId + ".zip").build();
 		return Response.ok(body).header("Content-disposition", cd).build();
 	}
 
 
 	/**
 	 * @param researchObjectId
 	 * @param versionId
 	 * @param dLibraDataSource
 	 * @return
 	 * @throws RemoteException
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 * @throws IdNotFoundException
 	 */
 	private Response getEditionList(String workspaceId,
 			String researchObjectId, String versionId,
 			DigitalLibrary dLibraDataSource)
 		throws RemoteException, DigitalLibraryException, NotFoundException
 	{
 		logger.debug("Getting edition list");
 		Set<Snapshot> snapshots = dLibraDataSource.getEditionList(workspaceId,
 			researchObjectId, versionId);
 		StringBuilder sb = new StringBuilder();
 		for (Snapshot snapshot : snapshots) {
 			sb.append((snapshot.isPublished() ? "*" : "") + snapshot.getId()
 					+ "=" + snapshot.getCreationDate() + "\n");
 		}
 		return Response.ok(sb.toString()).build();
 	}
 
 
 	/**
 	 * Used for updating metadata of version of RO (manifest.rdf file).
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @param versionId
 	 *            identifier of version of RO - defined by the user
 	 * @param rdfAsString
 	 *            manifest.rdf
 	 * @return 200 (OK) response code if the descriptive metadata was
 	 *         successfully updated, 400 (Bad Request) if manifest.rdf is not
 	 *         well-formed, 409 (Conflict) if manifest.rdf contains incorrect
 	 *         data (for example, one of required tags is missing).
 	 * @throws IOException
 	 * @throws TransformerException
 	 * @throws JenaException
 	 *             if the manifest is malformed
 	 * @throws IncorrectManifestException
 	 *             if the manifest is missing a property
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 * @throws IdNotFoundException
 	 */
 	@PUT
 	@Consumes("application/rdf+xml")
 	public Response publish(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId, @PathParam("RO_VERSION_ID")
 	String versionId, @QueryParam("unpublish")
 	String unpublish)
 		throws IOException, TransformerException, JenaException,
 		DigitalLibraryException, NotFoundException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		if (user.getRole() == UserProfile.Role.PUBLIC) {
 			throw new AuthenticationException(
 					"Only authenticated users can do that.",
 					SecurityFilter.REALM);
 		}
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		if (unpublish == null) {
 			dl.publishVersion(workspaceId, researchObjectId, versionId);
 		}
 		else {
 			dl.unpublishVersion(workspaceId, researchObjectId, versionId);
 		}
 
 		return Response.ok().build();
 	}
 
 
 	@POST
 	public Response createEdition(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId, @PathParam("RO_VERSION_ID")
 	String versionId)
 		throws RemoteException, DigitalLibraryException, MalformedURLException,
 		UnknownHostException, NotFoundException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		if (user.getRole() == UserProfile.Role.PUBLIC) {
 			throw new AuthenticationException(
 					"Only authenticated users can do that.",
 					SecurityFilter.REALM);
 		}
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		long editionId = dl.createEdition(workspaceId, versionId,
 			researchObjectId, versionId).getId();
 
 		String uri = uriInfo.getAbsolutePath().toString() + "?edition_id="
 				+ editionId;
 
 		return Response.created(URI.create(uri)).build();
 	}
 
 
 	/**
 	 * Deletes this version of research object.
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @param versionId
 	 *            identifier of version of RO - defined by the user
 	 * @throws DigitalLibraryException
 	 * @throws NotFoundException
 	 * @throws SQLException 
 	 * @throws NamingException 
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 * @throws IdNotFoundException
 	 */
 	@DELETE
 	public void deleteVersion(@PathParam("W_ID")
 	String workspaceId, @PathParam("RO_ID")
 	String researchObjectId, @PathParam("RO_VERSION_ID")
 	String versionId)
 		throws DigitalLibraryException, NotFoundException,
 		ClassNotFoundException, IOException, NamingException, SQLException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		if (user.getRole() == UserProfile.Role.PUBLIC) {
 			throw new AuthenticationException(
 					"Only authenticated users can do that.",
 					SecurityFilter.REALM);
 		}
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		dl.deleteVersion(workspaceId, researchObjectId, versionId);
 
 		URI researchObjectURI = uriInfo.getAbsolutePathBuilder().path("/")
 				.build();
 		SemanticMetadataService sms = SemanticMetadataServiceFactory
 				.getService(user);
 		try {
 			sms.removeResearchObject(researchObjectURI);
 		}
 		finally {
 			sms.close();
 		}
 	}
 }

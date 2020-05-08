 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.AuthenticationException;
 import pl.psnc.dl.wf4ever.auth.SecurityFilter;
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 import pl.psnc.dlibra.service.IdNotFoundException;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("ROs/{ro_id}/")
 public class ResearchObjectResource
 {
 
 	@SuppressWarnings("unused")
 	private final static Logger logger = Logger
 			.getLogger(ResearchObjectResource.class);
 
 	@Context
 	HttpServletRequest request;
 
 	@Context
 	UriInfo uriInfo;
 
 	private static final String workspaceId = "default";
 
 	private static final String versionId = "v1";
 
 
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
 	 * @throws UnknownHostException 
 	 * @throws MalformedURLException 
 	 * @throws RemoteException 
 	 * @throws IOException
 	 * @throws DigitalLibraryException
 	 * @throws IdNotFoundException
 	 * @throws OperationNotSupportedException
 	 * @throws NotFoundException
 	 */
 	@GET
 	@Produces("application/zip")
 	public Response getZippedRO(@PathParam("ro_id")
 	String researchObjectId)
 		throws RemoteException, MalformedURLException, UnknownHostException,
 		DigitalLibraryException, NotFoundException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
 			user.getLogin(), user.getPassword());
 
 		InputStream body = dl.getZippedVersion(workspaceId, researchObjectId,
 			versionId);
 		//TODO add all named graphs from SMS that start with the base URI
 		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(versionId + ".zip").build();
 		return Response.ok(body).header("Content-disposition", cd).build();
 	}
 
 
 	@DELETE
 	public void deleteVersion(@PathParam("ro_id")
 	String researchObjectId)
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
 
 		try {
 			dl.deleteVersion(workspaceId, researchObjectId, versionId);
 			if (dl.getVersionIds(workspaceId, researchObjectId).isEmpty()) {
 				dl.deleteResearchObject(workspaceId, researchObjectId);
 				if (dl.getResearchObjectIds(workspaceId).isEmpty()) {
 					dl.deleteWorkspace(workspaceId);
 				}
 			}
 		}
 		finally {
 
 			SemanticMetadataService sms = SemanticMetadataServiceFactory
 					.getService(user);
 			try {
 				sms.removeResearchObject(uriInfo.getAbsolutePath());
 			}
 			finally {
 				sms.close();
 			}
 		}
 	}
 }

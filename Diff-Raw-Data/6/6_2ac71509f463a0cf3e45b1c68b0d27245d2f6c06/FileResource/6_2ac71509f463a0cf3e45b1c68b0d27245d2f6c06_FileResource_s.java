 package pl.psnc.dl.wf4ever;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.transform.TransformerException;
 
 import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
 import pl.psnc.dlibra.service.DLibraException;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * 
  * @author nowakm
  *
  */
@Path(Constants.WORKSPACES_URL_PART + "/{W_ID}/"
 		+ Constants.RESEARCH_OBJECTS_URL_PART
		+ "/{RO_ID}/{RO_VERSION_ID}/{FILE_PATH : [a-zA-Z0-9.%/]+}")
 public class FileResource
 {
 
 	//	private final static Logger logger = Logger.getLogger(FileResource.class);
 
 	@Context
 	private HttpServletRequest request;
 
 	@Context
 	private UriInfo uriInfo;
 
 
 	@GET
 	public Response getFile(@PathParam("W_ID") String workspaceId,
 			@PathParam("RO_ID") String researchObjectId,
 			@PathParam("RO_VERSION_ID") String versionId,
 			@PathParam("FILE_PATH") String filePath,
 			@QueryParam("content") String isContentRequested)
 		throws IOException, DLibraException, TransformerException
 	{
 
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 
 		if (isContentRequested != null) { // file
 			InputStream body = dLibraDataSource.getFileContents(
 				researchObjectId, versionId, filePath);
 			String mimeType = dLibraDataSource.getFileMimeType(
 				researchObjectId, versionId, filePath);
 
 			String fileName = uriInfo.getPath().substring(
 				1 + uriInfo.getPath().lastIndexOf("/"));
 			ContentDisposition cd = ContentDisposition.type(mimeType)
 					.fileName(fileName).build();
 			return Response.ok(body)
 					.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd)
 					.header(Constants.CONTENT_TYPE_HEADER_NAME, mimeType)
 					.build();
 		}
 		else { // metadata
 			String metadata = dLibraDataSource.getFileMetadata(
 				researchObjectId, versionId, filePath, uriInfo
 						.getAbsolutePath().toString());
 			ContentDisposition cd = ContentDisposition.type(
 				Constants.RDF_XML_MIME_TYPE).build();
 			return Response
 					.ok(metadata)
 					.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd)
 					.header(Constants.CONTENT_TYPE_HEADER_NAME,
 						Constants.RDF_XML_MIME_TYPE).build();
 		}
 
 	}
 
 
 	@POST
 	public Response createOrUpdateFile(@PathParam("W_ID") String workspaceId,
 			@PathParam("RO_ID") String researchObjectId,
 			@PathParam("RO_VERSION_ID") String versionId,
 			@PathParam("FILE_PATH") String filePath,
 			@HeaderParam(Constants.CONTENT_TYPE_HEADER_NAME) String type,
 			InputStream inputStream)
 		throws IOException, DLibraException, TransformerException
 	{
 
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 
 		String versionUri = uriInfo
 				.getAbsolutePath()
 				.toString()
 				.substring(
 					0,
 					uriInfo.getAbsolutePath().toString().lastIndexOf(filePath) - 1);
 
 		dLibraDataSource.createOrUpdateFile(versionUri, researchObjectId,
 			versionId, filePath, inputStream, type);
 
 		return Response.ok().build();
 	}
 
 
 	@DELETE
 	public void deleteFile(@PathParam("W_ID") String workspaceId,
 			@PathParam("RO_ID") String researchObjectId,
 			@PathParam("RO_VERSION_ID") String versionId,
 			@PathParam("FILE_PATH") String filePath)
 		throws DLibraException, IOException, TransformerException
 	{
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 
 		String versionUri = uriInfo
 				.getAbsolutePath()
 				.toString()
 				.substring(
 					0,
 					uriInfo.getAbsolutePath().toString().lastIndexOf(filePath) - 1);
 
 		dLibraDataSource.deleteFile(versionUri, researchObjectId, versionId,
 			filePath);
 	}
 }

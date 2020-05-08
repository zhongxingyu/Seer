 package org.bioinfo.gcsa.ws;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.gcsa.lib.GcsaUtils;
 import org.bioinfo.gcsa.lib.account.beans.Bucket;
 import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
 import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
 import org.bioinfo.gcsa.lib.account.io.IOManagementException;
 
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
 
 @Path("/account/{accountId}/storage")
 public class StorageWSServer extends GenericWSServer {
 	private String accountId;
 
 	public StorageWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
 			@DefaultValue("") @PathParam("accountId") String accountId) throws IOException, AccountManagementException {
 		super(uriInfo, httpServletRequest);
 		this.accountId = accountId;
 
 		logger.info("HOST: " + uriInfo.getRequestUri().getHost());
 		logger.info("----------------------------------->");
 	}
 
 	/********************
 	 * 
 	 * BUCKET METHODS
 	 * 
 	 ********************/
 	@GET
 	@Path("/buckets")
 	public Response getAccountBuckets() {
 		try {
 			String res = cloudSessionManager.getAccountBuckets(accountId, sessionId);
 			return createOkResponse(res);
 		} catch (AccountManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not get buckets");
 		}
 	}
 
 	@GET
 	@Path("/{bucketId}/create")
 	public Response createBucket(@DefaultValue("") @PathParam("bucketId") String bucketId,
 			@DefaultValue("") @QueryParam("description") String description) {
 		Bucket bucket = new Bucket(bucketId);
 		bucket.setId(bucketId.toLowerCase());
 		bucket.setDescripcion(description);
 		try {
 			cloudSessionManager.createBucket(accountId, bucket, sessionId);
 			return createOkResponse("OK");
 		} catch (AccountManagementException | IOManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not create bucket");
 		}
 	}
 
 	// TODO
 	// @GET
 	// @Path("/{bucketname}/rename/{newName}")
 	// public Response renameBucket(@DefaultValue("") @PathParam("bucket_name")
 	// String bucketId,
 	// @DefaultValue("") @PathParam("newName") String newName) {
 	// try {
 	// cloudSessionManager.renameBucket(accountId, bucketId, newName,
 	// sessionId);
 	// return createOkResponse("OK");
 	// } catch (AccountManagementException | IOManagementException e) {
 	// logger.error(e.toString());
 	// return createErrorResponse("could not rename bucket");
 	// }
 	// }
 
 	// TODO
 	// @GET
 	// @Path("/{bucketname}/delete")
 	// public Response deleteBucket(@DefaultValue("") @PathParam("bucketname")
 	// String bucketId) {
 	// try {
 	// cloudSessionManager.deleteBucket(accountId, bucketId, sessionId);
 	// return createOkResponse("OK");
 	// } catch (AccountManagementException | IOManagementException e) {
 	// logger.error(e.toString());
 	// return createErrorResponse("could not delete the bucket");
 	// }
 	// }
 
 	// TODO
 	// @GET
 	// @Path("/{bucketname}/share/{accountList}")
 	// public Response shareBucket(@DefaultValue("") @PathParam("bucketname")
 	// String bucketId,
 	// @DefaultValue("") @PathParam("accountList") String accountList) {
 	// try {
 	// cloudSessionManager.shareBucket(accountId, bucketId,
 	// StringUtils.toList(accountList, ","), sessionId);
 	// return createOkResponse("OK");
 	// } catch (AccountManagementException | IOManagementException e) {
 	// logger.error(e.toString());
 	// return createErrorResponse("could not share the bucket");
 	// }
 	// }
 
 	@POST
 	@Path("/{bucketId}/upload")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Response uploadObject(@DefaultValue("") @PathParam("bucketId") String bucketId,
 			@DefaultValue("") @FormDataParam("objectid") String objectIdFromURL,
 			@FormDataParam("file") InputStream fileIs, @FormDataParam("file") FormDataContentDisposition fileInfo,
			@DefaultValue("undefined") @FormDataParam("name") String name, @FormDataParam("tags") String tags,
 			@DefaultValue("r") @QueryParam("filetype") String filetype,
			@DefaultValue("-") @FormDataParam("responsible")  String responsible,
			@DefaultValue("-")@FormDataParam("organization")  String organization,
			@DefaultValue("-") @FormDataParam("date")  String date,
			@DefaultValue("-") @FormDataParam("description") String description,
			@DefaultValue("-1") @FormDataParam("jobid") String jobid,
 			@DefaultValue("false") @QueryParam("parents") boolean parents) {
 
 		java.nio.file.Path objectId = parseObjectId(objectIdFromURL);
		System.out.println(bucketId);
 		System.out.println(objectId);
		System.out.println(parents);
 
 		ObjectItem objectItem = new ObjectItem(null, null, null);// TODO PAKO
 		// COMPROBAR
 		// CONSTRUCTOR
 		objectItem.setFileFormat(tags);
 		objectItem.setFileType(filetype);
 		objectItem.setResponsible(responsible);
 		objectItem.setOrganization(organization);
 		objectItem.setDate(GcsaUtils.getTime());
 		objectItem.setDescription(description);
 
 		try {
 			String res = cloudSessionManager.createObjectToBucket(accountId, bucketId, objectId, objectItem, fileIs,
 					parents, sessionId);
 			return createOkResponse(res);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	/********************
 	 * 
 	 * OBJECT METHODS
 	 * 
 	 ********************/
 
 	@GET
 	@Path("/{bucketId}/create_directory")
 	public Response createDirectory(@DefaultValue("") @PathParam("bucketId") String bucketId,
 			@DefaultValue("") @QueryParam("objectid") String objectIdFromURL,
 			@DefaultValue("false") @QueryParam("parents") boolean parents) {
 
 		java.nio.file.Path objectId = parseObjectId(objectIdFromURL);
 
 		ObjectItem objectItem = new ObjectItem(null, null, null);
 		objectItem.setFileType("dir");
 		objectItem.setDate(GcsaUtils.getTime());
 		try {
 			String res = cloudSessionManager.createFolderToBucket(accountId, bucketId, objectId, objectItem, parents,
 					sessionId);
 			return createOkResponse(res);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	@GET
 	@Path("/{bucketId}/{objectId}/delete")
 	public Response deleteData(@DefaultValue("") @PathParam("bucketId") String bucketId,
 			@DefaultValue("") @PathParam("objectId") String objectIdFromURL) {
 
 		java.nio.file.Path objectId = parseObjectId(objectIdFromURL);
 		try {
 			cloudSessionManager.deleteDataFromBucket(accountId, bucketId, objectId, sessionId);
 			return createOkResponse("OK");
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	// TODO for now, only region filter allowed
 	@GET
 	@Path("/{bucketId}/{objectId}/fetch/")
 	public Response region(@DefaultValue("") @PathParam("bucketId") String bucketId,
 			@DefaultValue("") @PathParam("objectId") String objectIdFromURL,
 			@DefaultValue("") @QueryParam("region") String regionStr) {
 		java.nio.file.Path objectId = parseObjectId(objectIdFromURL);
 		try {
 			String res = cloudSessionManager.region(accountId, bucketId, objectId, regionStr, params, sessionId);
 			return createOkResponse(res);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	/********************
 	 * 
 	 * JOB METHODS
 	 * 
 	 ********************/
 
 	@GET
 	@Path("/job/{jobid}/result.{format}")
 	public Response getResultFile(@DefaultValue("") @PathParam("jobid") String jobId, @PathParam("format") String format) {
 		try {
 			String res = cloudSessionManager.getJobResult(accountId, jobId);
 			return createOkResponse(res);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	@GET
 	@Path("/job/{jobid}/table")
 	public Response table(@DefaultValue("") @PathParam("jobid") String jobId,
 			@DefaultValue("") @QueryParam("filename") String filename,
 			@DefaultValue("") @QueryParam("start") String start, @DefaultValue("") @QueryParam("limit") String limit,
 			@DefaultValue("") @QueryParam("colNames") String colNames,
 			@DefaultValue("") @QueryParam("colVisibility") String colVisibility,
 			@DefaultValue("") @QueryParam("callback") String callback,
 			@QueryParam("sort") @DefaultValue("false") String sort) {
 
 		try {
 			String res = cloudSessionManager.getFileTableFromJob(accountId, jobId, filename, start, limit, colNames,
 					colVisibility, callback, sort);
 			return createOkResponse(res);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	// @GET
 	// @Path("{jobId}/poll")
 	// public Response pollJobFile(@PathParam("jobId") String jobId,
 	// @QueryParam("filename") String filename, @DefaultValue("true")
 	// @QueryParam("zip") String zip) {
 	// logger.debug("POLLING "+ filename + "...");
 	//
 	@GET
 	@Path("/job/{jobid}/poll")
 	public Response pollJobFile(@DefaultValue("") @PathParam("jobid") String jobId,
 			@DefaultValue("") @QueryParam("filename") String filename,
 			@DefaultValue("true") @QueryParam("zip") String zip) {
 
 		try {
 			DataInputStream is = cloudSessionManager.getFileFromJob(accountId, jobId, filename, zip);
 			String name = null;
 			if (zip.compareTo("true") != 0) {// PAKO zip != true
 				name = filename;
 			} else {
 				name = filename + ".zip";
 			}
 			return createOkResponse(is, MediaType.APPLICATION_OCTET_STREAM_TYPE, name);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	@GET
 	@Path("/job/{jobid}/status")
 	public Response getJobStatus(@DefaultValue("") @PathParam("jobid") String jobId) {
 		try {
 			String res = cloudSessionManager.checkJobStatus(accountId, jobId, sessionId);
 			return createOkResponse(res);
 		} catch (Exception e) {
 			logger.error(e.toString());
 			return createErrorResponse(e.getMessage());
 		}
 	}
 
 	/*******************/
 	@POST
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	@Path("/subir")
 	public Response subir(@FormDataParam("content") InputStream contentIs,
 			@FormDataParam("content") FormDataContentDisposition contentDisposition,
 			@FormDataParam("num") @DefaultValue("") String num, @FormDataParam("total") @DefaultValue("") String total,
 			@FormDataParam("filename") @DefaultValue("") String filename) {
 		System.out.println("---------->  subir!!!");
 		System.out.println("num " + num);
 		System.out.println("total " + total);
 		System.out.println("getFileName " + contentDisposition.getFileName());
 		System.out.println("getType " + contentDisposition.getType());
 		System.out.println("getSize " + contentDisposition.getSize());
 
 		try {
 			Files.copy(contentIs, Paths.get("tmp", filename + ".part" + num));
 			if (num.equals(total)) {
 				int tot = Integer.parseInt(total);
 				for (int i = 0; i < tot; i++) {
 
 				}
 			}
 		} catch (IOException e) {
 
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return createOkResponse("ok");
 	}
 }

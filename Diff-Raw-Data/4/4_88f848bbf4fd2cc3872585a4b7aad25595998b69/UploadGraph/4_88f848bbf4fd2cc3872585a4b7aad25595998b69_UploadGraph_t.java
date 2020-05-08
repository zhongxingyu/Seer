 package de.anycook.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.log4j.Logger;
 
 import de.anycook.session.Session;
 import de.anycook.upload.RecipeUploader;
 import de.anycook.upload.UploadHandler;
 import de.anycook.upload.UserUploader;
 
 
 @Path("upload")
 public class UploadGraph {
 	private final Logger logger;
 	
 	public UploadGraph() {
 		logger = Logger.getLogger(getClass());
 	}
 	
 	
 	@POST
 	@Path("image/{type}")
 	public Response uploadRecipeImage(@Context HttpServletRequest request,
 			@Context HttpHeaders hh,
 			@PathParam("type") String type){
 		
 		UploadHandler upload;
 		Session session = Session.init(request.getSession());
 		
 		switch (type) {
 		case "recipe":
 			upload = new RecipeUploader();
 			break;
 		case "user":
             session.checkLogin(hh.getCookies());
             upload = new UserUploader();
 			break;
 		default:
 			throw new WebApplicationException(400);
 		}
         File tempFile;
         try {
             tempFile = upload.uploadFile(request);
         } catch (IOException | FileUploadException e) {
             logger.error(e,e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         if(tempFile!=null){
             try{
                 String newFilename = upload.saveFile(tempFile);
                 if(type.equals("user"))
                     session.getUser().setImage(newFilename);
 
                 return  Response.ok("{success:\""+newFilename+"\"}").build();
            } catch (SQLException | IOException e) {
                logger.error(e, e);
                 throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
             }
 
         }
 		else{
 			logger.warn("upload failed");
 			return Response.status(400).entity("{error:\"upload failed\"}").build();
 		}
 			
 		
 	}
 	
 	
 }

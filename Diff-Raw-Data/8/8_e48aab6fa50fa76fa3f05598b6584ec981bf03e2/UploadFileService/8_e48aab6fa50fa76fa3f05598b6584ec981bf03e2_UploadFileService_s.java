 package com.monkey.fileupload;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.monkey.FilePathConstants;
 import com.monkey.image.resize.ImageFileConverter;
 
 /**
  * Created with IntelliJ IDEA. User: Ric Date: 06.05.13 Time: 11:26 To change
  * this template use File | Settings | File Templates.
  */
 @Component
 @Path("/file")
 public class UploadFileService {
 
     @Autowired
     ImageFileConverter imageFileConverter;
 
     /**
      * @param request
      * @param res
      * @throws Exception
      */
     @POST
     @Path("/multipleFiles")
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Produces(MediaType.TEXT_PLAIN)
     public void uploadFile(@Context HttpServletRequest request, @Context HttpServletResponse res) throws Exception {
 
 	if (ServletFileUpload.isMultipartContent(request)) {
 	    final FileItemFactory factory = new DiskFileItemFactory();
 	    final ServletFileUpload fileUpload = new ServletFileUpload(factory);
 
 	    try {
 
 		final List items = fileUpload.parseRequest(request);
 
 		if (items != null) {
 		    final Iterator iterator = items.iterator();
 
 		    while (iterator.hasNext()) {
 			final FileItem item = (FileItem) iterator.next();
 			final String itemName = item.getName();
 
 			createUploadFolderStructure();
 
 			final File savedFile = new File(FilePathConstants.FILE_UPLOAD_PATH + File.separator + itemName);
 			System.out.println("Saving the file: " + savedFile.getName());
 			item.write(savedFile);
 
 			imageFileConverter.resize(savedFile, itemName);
 		    }
 		}
 	    } catch (FileUploadException fue) {
 		fue.printStackTrace();
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	}
     }
 
     private void createUploadFolderStructure() {
 
 	ImageUtil.createFolderStructure(FilePathConstants.FILE_UPLOAD_PATH);
 	ImageUtil.createFolderStructure(FilePathConstants.FILE_RESIZE_PATH);
     }
 }

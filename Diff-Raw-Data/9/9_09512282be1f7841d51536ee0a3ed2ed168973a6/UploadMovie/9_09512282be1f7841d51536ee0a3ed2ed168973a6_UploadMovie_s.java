 package com.osgo.plugin.portfolio.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 
 import java.nio.ByteBuffer;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileServiceFactory;
 import com.google.appengine.api.files.FileWriteChannel;
 import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
 import com.osgo.plugin.portfolio.api.PortfolioService;
 import com.osgo.plugin.portfolio.api.PortfolioServiceFactory;
 import com.osgo.plugin.portfolio.model.objectify.Picture;
 import com.osgo.plugin.portfolio.model.objectify.Project;
 
 public class UploadMovie extends HttpServlet {
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 	private boolean cloudStorage = true;
 	private boolean blobStore = false;
 	public static final String BUCKETNAME = "osgo/dealImages";
 	public static final String DIR = "dealImages/";
 	private static final Logger log =
 		      Logger.getLogger(UploadMovie.class.getName());
 	
     public void doPost(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException{
 
 	/**
 	 * Set up properties of your new object
 	 * After finalizing objects, they are accessible
 	 * through Cloud Storage with the URL:
 	 * http://commondatastorage.googleapis.com/my_bucket/my_object
 	 */
 	
 	
     	ServletFileUpload upload = new ServletFileUpload();
     
         res.setContentType("text/plain");
         PortfolioService portfolioService = PortfolioServiceFactory.getPortfolioService();
         Project project = null;
         List<String> info = new ArrayList<String>();
         List<String> links = new ArrayList<String>();
         List<String> linkTexts = new ArrayList<String>();
         String movieUrl = "";
         Map<String, BlobKey> images = new HashMap<String, BlobKey>();
 
         FileItemIterator iterator = null;
 		try {
 			iterator = upload.getItemIterator(req);
 		} catch (FileUploadException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         try {
         	
         	// get Project to add image to
         	
         	
 			while (iterator.hasNext()) {
 			  FileItemStream item = iterator.next();
 			  InputStream stream = item.openStream();
 
 			  if (item.isFormField()) {
 			    log.warning("Got a form field: " + item.getFieldName());
 			    StringWriter writer = new StringWriter();
 			    IOUtils.copy(stream, writer);
 		    	String theString = writer.toString();
 			    if(item.getFieldName().equals("project_id")){			    	
 				    String projId = URLDecoder.decode(theString);
 				    Long id = Long.parseLong(projId);
 				    project = portfolioService.getProject(id);
 			    } else {
 			    	if(item.getFieldName().contains("display_text")){
 			    		info.add(theString);
			    	} else if(item.getFieldName().contains("link")){
 			    		links.add(theString);
 			    	} else if(item.getFieldName().contains("link_text")){
 			    		linkTexts.add(theString);
 			    	} else if(item.getFieldName().contains("movie")){
 			    		movieUrl = theString;
 			    	}
 			    }
 			  } else {
 				  if(project==null){
 					  res.getWriter().println("Error: Project not saved in DB.");
 					  res.setStatus(500);
 				  } else {
 				    log.warning("Got an uploaded file: " + item.getFieldName() +
 				                ", name = " + item.getName());
 				    
 				    byte[] data = IOUtils.toByteArray(item.openStream());
 				    
 				    BlobKey blobKey = createBlob(data, res);
 				    images.put(item.getFieldName(), blobKey);
 					
 				  }
 			  }
 			}
 		} catch (IllegalStateException e) {
 			res.getWriter().println("Error: Occurred during upload: "+e.getMessage());
 			res.setStatus(500);
 		} catch (FileUploadException e) {
 			res.getWriter().println("Error: Occurred during upload: "+e.getMessage());
 			res.setStatus(500);
 		}	
         
 		Picture picture = new Picture();
 		BlobKey thumbKey = images.get("thumb");
 		picture.setThumbKey(thumbKey);
 		picture.intUrl();
 		picture.setMovieUrl(movieUrl);
 		picture.setInfo(info);
 		picture.setLinks(links);
 		picture.setLinksText(linkTexts);
 		
         portfolioService.addImage(picture, project);
     
         res.sendRedirect("/forms/admin.php");
     }
     
     private BlobKey createBlob(byte[] data, HttpServletResponse response) throws IOException{
 		  // Get a file service
 		  FileService fileService = FileServiceFactory.getFileService();
 
 		  // Create a new Blob file with mime-type "text/plain"
 		  AppEngineFile file = fileService.createNewBlobFile("image/jpeg");
 
 		  // Open a channel to write to it
 		  boolean lock = true;
 		  FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);
 		  String path = file.getFullPath();
 
 		  // Write more to the file in a separate request:
 		  file = new AppEngineFile(path);
 
 		  // This time we write to the channel directly
 		  writeChannel.write(ByteBuffer.wrap(data));
 
 		  // Now finalize
 		  writeChannel.closeFinally();
 
 		  // Now read from the file using the Blobstore API
 		  BlobKey blobKey = fileService.getBlobKey(file);
 		  
 		  return blobKey;
 
 	}
 }

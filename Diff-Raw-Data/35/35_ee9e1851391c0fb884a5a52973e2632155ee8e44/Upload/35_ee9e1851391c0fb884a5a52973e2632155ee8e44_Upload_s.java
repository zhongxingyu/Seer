 package com.sri.straylight.webapp;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.io.File;
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.PageContext;
 
 import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 
 
 
 
 public class Upload {
 	
 	//private String _fileUploadPath; 
	private ProgressListener _progressListener;
 	private String _fmuFolder;
 	
 	
     public Upload() {
     		
     	
 		
  	   //ServletContext context = pageContext.getServletContext();
  	  // String filePath = context.getInitParameter("fmu-folder");
  	   
     	//_fileUploadPath = context.getInitParameter("file-upload");
     	//Create a progress listener
     	ProgressListener _progressListener = new ProgressListener(){
     	   public void update(long pBytesRead, long pContentLength, int pItems) {
     	       System.out.println("We are currently reading item " + pItems);
     	       if (pContentLength == -1) {
     	           System.out.println("So far, " + pBytesRead + " bytes have been read.");
     	       } else {
     	           System.out.println("So far, " + pBytesRead + " of " + pContentLength
     	                              + " bytes have been read.");
     	       }
     	   }
     	};
     	
     }
     
 	
 	//public ArrayList<String> fmuList;
 	
 	
 	public ArrayList<String> getFMUList() {
 		
 
 		   
 	    File fmuFolder = new File(this._fmuFolder);
 	    
 	    File[] allFiles = fmuFolder.listFiles();
 	   
 	    ArrayList<File> fmuFiles =  new ArrayList<File>();
 	    
 	    for( File f : allFiles) {
 	       	
 	    	String fileName = f.getName();
 	       	int idx = fileName.lastIndexOf(".") + 1;
 	       	
 	       	String extension = fileName.substring(idx, fileName.length()) ;
 	       	
 	       	if (extension.compareTo("fmu") == 0) {
 	       		fmuFiles.add(f);
 	       	}
 	    }
 	    
 	    
 		FileComparator comparator = new FileComparator();
 		Collections.sort(fmuFiles, comparator);
 	        
 		ArrayList<String> fmuFileNames =  new ArrayList<String>();
 		
 	    for( File fmuFile : fmuFiles) {
 	    	fmuFileNames.add(fmuFile.getName());
 	    }
 		
 	    
 	    
 		return fmuFileNames;
 	
 	}
 	
 	
 	
 	public void checkForUpload(PageContext pageContext, HttpServletRequest request) {
 		
 		ServletContext context = pageContext.getServletContext();
 		this._fmuFolder = context.getInitParameter("fmu-folder");
 		   
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 		
 		if (isMultipart) {
 			this.process(pageContext, request);
 		}
 	
 	}
 	
 	
 	
 	public boolean process(PageContext pageContext, HttpServletRequest request) {
 
 		   
 		   File file ;
 		   int maxFileSize = 5000 * 1024 * 1024;
 		   int maxMemSize = 5000 * 1024;
 		   
 		   
 		   // Verify the content type
 		   String contentType = request.getContentType();
 		   
 		   
 		   if ((contentType.indexOf("multipart/form-data") >= 0)) {
 
 		      DiskFileItemFactory factory = new DiskFileItemFactory();
 		      // maximum size that will be stored in memory
 		      factory.setSizeThreshold(maxMemSize);
 		      // Location to save data that is larger than maxMemSize.
 		      factory.setRepository(new File("c:\\temp"));
 
 		      // Create a new file upload handler
 		      ServletFileUpload upload = new ServletFileUpload(factory);
 		      // maximum file size to be uploaded.
 		      upload.setSizeMax( maxFileSize );
		      upload.setProgressListener(this._progressListener);
 		    	
 		      try{ 
 		         // Parse the request to get file items.
		         List fileItems = upload.parseRequest(request);
 
 		         // Process the uploaded file items
		         Iterator i = fileItems.iterator();
 
 
 		         while ( i.hasNext () ) 
 		         {
 		            FileItem fi = (FileItem)i.next();
 		            if ( !fi.isFormField () )	
 		            {
 		            // Get the uploaded file parameters
		            String fieldName = fi.getFieldName();
 		            String fileName = fi.getName();
 		           // boolean isInMemory = fi.isInMemory();
 		          //  long sizeInBytes = fi.getSize();
 		            // Write the file
 		            if( fileName.lastIndexOf("\\") >= 0 ){
 		            file = new File( this._fmuFolder + 
 		            fileName.substring( fileName.lastIndexOf("\\"))) ;
 		            }else{
 		            file = new File( this._fmuFolder + 
 		            fileName.substring(fileName.lastIndexOf("\\")+1)) ;
 		            }
 		            fi.write( file ) ;
 		            
 		            }
 		         }
 
 		      }catch(Exception ex) {
 		         System.out.println(ex);
 		      }
 		      
 		      return true;
 		   }else {
 			   return false;
 		   }
 		   
 		   
 		   
 	}
 	
 	
 }

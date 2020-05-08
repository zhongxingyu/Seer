 //
 // Created       : 2008 May 26 (Mon) 13:01:58 by Harold Carr.
// Last Modified : 2008 May 28 (Wed) 12:51:23 by Harold Carr.
 // 
 // from http://home.izforge.com/index.php/2006/10/29/295-handling-file-uploads-with-the-google-web-toolkit
 //
 
 package org.openhc.trowser.gwt.server;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Iterator;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import org.openhc.trowser.gwt.common.Constants;
 
 public class FileUploaderServlet
     extends 
 	HttpServlet
 {
     protected void doPost(HttpServletRequest request,
 			  HttpServletResponse response) 
 	throws ServletException, 
 	       IOException
     {
 	response.setContentType("text/plain");
 		
 	FileItem uploadItem = getFileItem(request);
 	if (uploadItem == null) {
 	    response.getWriter().write("NO-SCRIPT-DATA");
 	    return;
 	}
 		
 	//response.getWriter().write(new String(uploadItem.get()));
 	try {
	    // ***** - FIX
 	    ServiceImpl.serviceImplDelegate.loadData(
 	        new String(uploadItem.get()));
 	    response.getWriter().write("OK");
 	} catch (Throwable t) {
 	    response.getWriter().write(t.toString());
 	}
     }
 	
     private FileItem getFileItem(HttpServletRequest request)
     {
 	FileItemFactory factory = new DiskFileItemFactory();
 	ServletFileUpload upload = new ServletFileUpload(factory);
 		
 	try {
 	    List items = upload.parseRequest(request);
 	    Iterator it = items.iterator();
 	    while (it.hasNext()) {
 		FileItem item = (FileItem) it.next();
 		if (!item.isFormField() 
 		    && 
 		    Constants.trowserUploadedFile.equals(item.getFieldName())) 
                 {
 		    return item;
 		}
 	    }
 	} catch (FileUploadException e) {
 	    return null;
 	}
 	return null;
     }
 }
 
 // End of file.

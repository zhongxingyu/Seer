 package otanga.Controllers;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.io.*;
 import org.apache.commons.io.*;
 import org.apache.commons.fileupload.*;
 import org.apache.commons.fileupload.disk.*;
 import org.apache.commons.fileupload.servlet.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Polys
  * Date: 10/05/13
  * Time: 15:16
  */
 public class FileUploadServlet extends HttpServlet {
 
     @Override
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         try {
             ServletFileUpload fileUpload = new ServletFileUpload();
             FileItemIterator items = fileUpload.getItemIterator(req);
             while (items.hasNext()) {
                 FileItemStream item = items.next();
 

                 // List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
             //for (FileItem item : items) {
 
                 if (item.isFormField())
                     continue;
 
                 String fileName = FilenameUtils.getName(item.getName());
                 InputStream fileContent = item.openStream(); // item.getInputStream();
 
                 UUID fileKey = null;
 
                 int fileSize = fileContent.available();


                System.err.print("FileName: " + fileName);
                System.err.print("FileSize: " + fileSize);

                 if (fileSize > 0)
                 {
                     try{
                         byte[] data = new byte[fileSize];
                         if (fileContent.read(data) != fileSize)
                             throw new IOException();
 
                         fileKey = FileStorage.storeImage(data, item.getContentType());
                     }
                     finally {
                         fileContent.close();
                     }
                 }
                else
                {
                    System.err.print("The file is empty!");
                }
 
                 if (fileKey != null)
                 {
                    System.err.print("Key: " + fileKey.toString());

                    //TODO: Respond somehow using the fileKey
                     resp.setStatus(200); // Status: OK
                     resp.setContentType("text/plain");
                     resp.getWriter().println("Uploaded file '" + fileName + "' with key: " + fileKey.toString());
 

                     //test reading back the file
                    String input = FileStorage.retrieveImage(fileKey);
                     resp.getWriter().println();
                    resp.getWriter().println(input);

                 }
                 else
                 {
                    System.err.print("The key is null!");
                    resp.setStatus(400); // Status: Bad Request
                 }
             }
         } catch (FileUploadException e) {
             throw new IOException("Cannot parse multipart request.", e);
         }
     }
 }

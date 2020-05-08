 package gutenberg.collect;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 /**
  * @link http://javaee-spec.java.net/nonav/javadocs/index.html?javax/servlet/http/class-use/Part.html 
  * 
  * When overriding this method, read the request data, write the response headers, get the 
  * response's writer or output stream object, and finally, write the response data. It's best 
  * to include content type and encoding. When using a PrintWriter object to return the response, 
  * set the content type before accessing the PrintWriter object.
  *  
  * The servlet container must write the headers before committing the response, because in 
  * HTTP the headers must be sent before the response body.
  * 
  * Where possible, set the Content-Length header (with the ServletResponse.setContentLength(int)
  * method), to allow the servlet container to use a persistent connection to return its response 
  * to the client, improving performance. The content length is automatically set if the entire 
  * response fits inside the response buffer.
  *  
  * @author adamarla
  *
  */
 
 @WebServlet(name = "Upload", urlPatterns = {"/scan","/suggestion"})
 @MultipartConfig(location = "/tmp")
 public class Upload extends HttpServlet { 
     
     @Override
     protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         resp.setStatus(HttpServletResponse.SC_OK);
         if (req.getHeader("Origin").startsWith("http://localhost:3000") ||
             req.getHeader("Origin").startsWith("http://10.") ||
             req.getHeader("Origin").startsWith("http://www.gradians.com")) {
             resp.addHeader("Access-Control-Allow-Origin", 
                 req.getHeader("Origin"));
             resp.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
             resp.addHeader("Access-Control-Allow-Headers",
                 req.getHeader("Access-Control-Request-Headers"));
             resp.addHeader("Access-Control-Max-Age", "1728000");
         } else {
             resp.addHeader("Access-Control-Allow-Origin", "null");
         }        
     }
         
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
         String response = "{\"msg\": \"It works\"}";
         resp.setStatus(HttpServletResponse.SC_OK);
         resp.setContentType(CONTENT_TYPE_JSON);
         resp.setContentLength(response.length());
         resp.getWriter().print(response);
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {        
         MessageDigest md = null;
         try {
             md = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) { 
             throw new ServletException(e.getMessage());
         }                
         
         String filename = null;
         for (Part part : req.getParts()) {            
             if (part.getContentType() != null) {
                 java.io.InputStream is = part.getInputStream();
                 int bytesRead = 0;
                 byte[] byteBuf = new byte[1024];
                 while ((bytesRead = is.read(byteBuf)) != -1) {
                     md.update(byteBuf, 0, bytesRead);
                 }
                 byte[] SHA1digest = md.digest();
                 StringBuffer sb = new StringBuffer();
                 for (byte b : SHA1digest){
                     sb.append(String.format("%02x", b));
                 }                
                 filename = sb.toString().substring(0, 12);
                 part.write(filename);                
             }                        
         }
 
         String response = null;
         if (filename == null) {
             response = "not-ok";
         } else if (req.getServletPath().equals("/scan")) {
             Path source = FileSystems.getDefault().getPath("/tmp").resolve(filename);
             String contentType = Files.probeContentType(source);
             if (contentType.contains(CONTENT_TYPE_PDF) ||
                 contentType.contains(CONTENT_TYPE_IMG)) {
                 Path target = FileSystems.getDefault().getPath("webapps").
                     resolve("scantray").
                     resolve(String.format("%s.%s", filename, EXTNSN));
                 if (Files.exists(target)) {
                     response = "file-already-exists";
                     Files.delete(source);
                 } else {
                     response = "true";
                     Files.move(source, target);
                 }
             } else {
                 Files.delete(source);
                 response = "file-type-not-ok";                
             }
         } else if (req.getServletPath().equals("/suggestion")) {
             String teacherId = req.getParameter("id");
             Path source = FileSystems.getDefault().getPath("/tmp").resolve(filename);
             String contentType = Files.probeContentType(source);
             if (contentType.contains(CONTENT_TYPE_PDF) ||
                 contentType.contains(CONTENT_TYPE_IMG)) {
                 Path target = FileSystems.getDefault().getPath("webapps").
                     resolve("suggestiontray").
                    resolve(String.format("%s.%s.%s", filename, teacherId,"02"));
                 if (Files.exists(target)) {
                     response = "file-already-exists";
                     Files.delete(source);
                 } else {
                     response = "true";
                     Files.move(source, target);
                 }
             } else if (contentType.contains(CONTENT_TYPE_DOC) ||
                 contentType.contains(CONTENT_TYPE_TXT)) {            
                 Path target = FileSystems.getDefault().getPath("webapps").
                     resolve("suggestiontray").
                    resolve(String.format("%s.%s.%s", filename, teacherId, "01"));
                 if (Files.exists(target)) {
                     response = "file-already-exists";
                     Files.delete(source);
                 } else {
                     response = "true";
                     Files.move(source, target);
                 }
             } else {
                 Files.delete(source);
                 response = "file-type-not-ok";                
             }
         }
         
         sendResponse(req, resp, response);
     }    
     
     private void sendResponse(HttpServletRequest req, HttpServletResponse resp, String response) 
         throws IOException {
         resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
         resp.setContentType(CONTENT_TYPE_JSON);
         resp.getWriter().println(String.format("{\"success\": %s}", response));        
     }
     
     private final String EXTNSN = "ue";
     private final String CONTENT_TYPE_DOC = "document";
     private final String CONTENT_TYPE_TXT = "text";
     private final String CONTENT_TYPE_JSON = "application/json";
     private final String CONTENT_TYPE_PDF = "application/pdf";
     private final String CONTENT_TYPE_IMG = "image/";
 
     private static final long serialVersionUID = 8282143940079610653L;
 
 }

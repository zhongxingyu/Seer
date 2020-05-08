 package gov.usgs.cida.watersmart.util;
 
 import gov.usgs.cida.config.DynamicReadOnlyProperties;
 import gov.usgs.cida.watersmart.common.ContextConstants;
 import gov.usgs.cida.watersmart.common.JNDISingleton;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import java.io.*;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.LoggerFactory;
 
 public class Upload extends HttpServlet {
 
     private static DynamicReadOnlyProperties props = JNDISingleton.getInstance();
     static final org.slf4j.Logger log = LoggerFactory.getLogger(Upload.class);
 
     @Override
     public void init() throws ServletException {
         super.init();
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
         doPost(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
         log.info("A file is being uploaded");
        int maxFileSize = Integer.parseInt(props.getProperty(ContextConstants.UPLOAD_MAXSIZE));
         int fileSize = Integer.parseInt(request.getHeader("Content-Length"));
         if (fileSize > maxFileSize) {
             sendErrorResponse(response, "Upload exceeds max file size of " + maxFileSize + " bytes");
             return;
         }
 
         // filename is parameter passed by our javascript uploader
         //String filename = request.getParameter("filename");
         String tempDir = props.getProperty(ContextConstants.UPLOAD_LOCATION);
         File dirFile = new File(tempDir);
         if (!dirFile.exists()) {
             dirFile.mkdirs();
         }
 
         File destinationFile;
         String wpsresponse = null;
 
         // Handle form-based upload (from IE)
         if (ServletFileUpload.isMultipartContent(request)) {
             FileItemFactory factory = new DiskFileItemFactory();
             ServletFileUpload upload = new ServletFileUpload(factory);
 
             log.debug("Parsing form request");
             try {
                 List<FileItem> itemList = upload.parseRequest(request);
                 InputStream fileIn = null;
                 RunMetadata meta = new RunMetadata();
                 for (FileItem item : itemList) {
                     // filename must come first
                     if (item.isFormField()) {
                         meta.set(item);
                     } else {
                         fileIn = item.getInputStream();
                     }
                 }
 
                 if (meta.isFilledIn() && fileIn != null) {
                     destinationFile = meta.getFile(tempDir);
                     saveFileFromRequest(fileIn, destinationFile);
                     log.debug("Upload file saved to " + destinationFile.getPath());
                     
                     log.debug("Beginning WPS process");
                     WPSImpl impl = new WPSImpl();
                     wpsresponse = impl.executeProcess(destinationFile, meta);
                 } else {
                     throw new Exception("Must provide all required parameters");
                 }
 
             } catch (Exception ex) {
                 // pass exception text along?
                 sendErrorResponse(response, "Unable to upload file: " + ex.getMessage());
                 return;
             }
         }
 
         String responseText = "{success: true, message: '" + wpsresponse + "'}";
         // can do post processing stuff here
         sendResponse(response, responseText);
     }
 
     public static void sendErrorResponse(HttpServletResponse response, String text) {
         sendResponse(response, "{success: false, message: '" + text + "'}");
     }
 
     public static void sendResponse(HttpServletResponse response, String text) {
 
         response.setContentType("text/html");
         //response.setCharacterEncoding("utf-8");
 
         try {
             Writer writer = response.getWriter();
             writer.write(text);
             writer.close();
         } catch (IOException ex) {
             // LOG
         }
     }
 
     private void saveFileFromRequest(InputStream is, File destinationFile) throws IOException {
         FileOutputStream os = null;
         try {
             os = new FileOutputStream(destinationFile);
             IOUtils.copy(is, os);
         } finally {
             IOUtils.closeQuietly(os);
         }
     }
 }

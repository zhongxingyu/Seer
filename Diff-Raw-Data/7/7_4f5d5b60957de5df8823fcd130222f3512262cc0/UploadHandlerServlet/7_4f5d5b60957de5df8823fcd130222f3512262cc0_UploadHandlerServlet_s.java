 package gov.usgs.cida.utilities.communication;
 
 import com.google.gson.Gson;
 import gov.usgs.cida.config.DynamicReadOnlyProperties;
 import gov.usgs.cida.utilities.properties.JNDISingleton;
 import java.io.File;
 //import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 //import javax.mail.MessagingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 //import org.apache.commons.codec.binary.Base64OutputStream;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 //import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 //import org.apache.http.HttpResponse;
 //import org.apache.http.client.HttpClient;
 //import org.apache.http.client.methods.HttpPost;
 //import org.apache.http.entity.AbstractHttpEntity;
 //import org.apache.http.entity.InputStreamEntity;
 //import org.apache.http.impl.client.DefaultHttpClient;
 //import org.apache.http.util.EntityUtils;
 import org.slf4j.LoggerFactory;
 
 public class UploadHandlerServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UploadHandlerServlet.class);
     private static DynamicReadOnlyProperties props = null;
     private static String uploadDirectory = null;
     // Config param string constants
     private static String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "coastal-hazards.files.upload.max-size";
     private static String FILE_UPLOAD_FILENAME_PARAM_CONFIG_KEY = "coastal-hazards.files.upload.filename-param";
     private static String DIRECTORY_BASE_PARAM_CONFIG_KEY = "coastal-hazards.files.directory.base";
     private static String DIRECTORY_UPLOAD_PARAM_CONFIG_KEY = "coastal-hazards.files.directory.upload";
     // Config defaults
     private static int FILE_UPLOAD_MAX_SIZE_DEFAULT = 15728640;
     private static String FILE_UPLOAD_FILENAME_PARAM_DEFAULT = "qqfile";
 
     @Override
     public void init() throws ServletException {
         super.init();
         props = JNDISingleton.getInstance();
         uploadDirectory = props.getProperty(DIRECTORY_BASE_PARAM_CONFIG_KEY) + props.getProperty(DIRECTORY_UPLOAD_PARAM_CONFIG_KEY);
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doPost(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         int maxFileSize = FILE_UPLOAD_MAX_SIZE_DEFAULT;
         int fileSize = Integer.parseInt(request.getHeader("Content-Length"));
         String fileParamKey = props.getProperty(FILE_UPLOAD_FILENAME_PARAM_CONFIG_KEY, FILE_UPLOAD_FILENAME_PARAM_DEFAULT);
         String fileName = request.getParameter(fileParamKey);
         String destinationDirectoryChild = UUID.randomUUID().toString();
         File uploadDestinationDirectory = new File(new File(uploadDirectory), destinationDirectoryChild);
         File uploadDestinationFile = new File(uploadDestinationDirectory, fileName);
         Map<String, String> responseMap = new HashMap<String, String>();
 
         try {
             maxFileSize = Integer.parseInt(props.get(FILE_UPLOAD_MAX_SIZE_CONFIG_KEY));
         } catch (NumberFormatException nfe) {
             LOG.info("JNDI property '" + FILE_UPLOAD_MAX_SIZE_CONFIG_KEY + "' not set or is an invalid value. Using: " + maxFileSize);
         }
 
         // Check that the incoming file is not larger than our limit
         if (maxFileSize > 0 && fileSize > maxFileSize) {
             responseMap.put("error", "Upload exceeds max file size of " + maxFileSize + " bytes");
             sendErrorResponse(response, responseMap);
             return;
         }
 
         // Create destination directory
         try {
             FileUtils.forceMkdir(uploadDestinationDirectory);
         } catch (IOException ioe) {
             responseMap.put("error", "Could not save file.");
             responseMap.put("exception", ioe.getMessage());
             sendErrorResponse(response, responseMap);
         }
 
         // Save the file to the upload directory
         try {
             saveFileFromRequest(request, fileParamKey, uploadDestinationFile);
         } catch (FileUploadException ex) {
             responseMap.put("error", "Could not save file.");
             responseMap.put("exception", ex.getMessage());
             sendErrorResponse(response, responseMap);
         } catch (IOException ex) {
             responseMap.put("error", "Could not save file.");
             responseMap.put("exception", ex.getMessage());
             sendErrorResponse(response, responseMap);
         }
 
         responseMap.put("file-token", destinationDirectoryChild);
         responseMap.put("file-checksum", Long.toString(FileUtils.checksumCRC32(uploadDestinationFile)));
         responseMap.put("file-size", Long.toString(FileUtils.sizeOf(uploadDestinationFile)));
         sendResponse(response, responseMap);
     }
 
     File saveFileFromRequest(HttpServletRequest request, String filenameParameter, File destinationFile) throws FileUploadException, IOException {
         if (StringUtils.isBlank(filenameParameter)) {
             throw new IllegalArgumentException();
         }
 
         if (ServletFileUpload.isMultipartContent(request)) {
             FileItemFactory factory = new DiskFileItemFactory();
             ServletFileUpload upload = new ServletFileUpload(factory);
 
             // Parse the request
             FileItemIterator iter;
             iter = upload.getItemIterator(request);
             while (iter.hasNext()) {
                 FileItemStream item = iter.next();
                 String name = item.getFieldName();
                 if (filenameParameter.toLowerCase().equals(name.toLowerCase())) {
                     saveFileFromInputStream(item.openStream(), destinationFile);
                     break;
                 }
             }
         } else {
             saveFileFromInputStream(request.getInputStream(), destinationFile);
         }
 
         return destinationFile;
     }
 //    @Override
 //    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 //
 //        int maxFileSize = Integer.parseInt(request.getParameter("maxfilesize"));
 //        int fileSize = Integer.parseInt(request.getHeader("Content-Length"));
 //        if (fileSize > maxFileSize) {
 //            sendErrorResponse(response, "Upload exceeds max file size of " + maxFileSize + " bytes");
 //            return;
 //        }
 //
 //        // qqfile is parameter passed by our javascript uploader
 //        String filename = request.getParameter("qqfile");
 //        String utilityWpsUrl = request.getParameter("utilitywps");
 //        String wfsEndpoint = request.getParameter("wfs-url");
 //        String tempDir = System.getProperty("java.io.tmpdir");
 //
 //        File destinationFile = new File(tempDir + File.separator + filename);
 //
 //        // Handle form-based upload (from IE)
 //        if (ServletFileUpload.isMultipartContent(request)) {
 //            FileItemFactory factory = new DiskFileItemFactory();
 //            ServletFileUpload upload = new ServletFileUpload(factory);
 //
 //            // Parse the request
 //            FileItemIterator iter;
 //            try {
 //                iter = upload.getItemIterator(request);
 //                while (iter.hasNext()) {
 //                    FileItemStream item = iter.next();
 //                    String name = item.getFieldName();
 //                    if ("qqfile".equals(name)) {
 //                        saveFileFromRequest(item.openStream(), destinationFile);
 //                        break;
 //                    }
 //                }
 //            } catch (Exception ex) {
 //                sendErrorResponse(response, "Unable to upload file");
 //                return;
 //            }
 //        } else {
 //            // Handle octet streams (from standards browsers)
 //            try {
 //                saveFileFromRequest(request.getInputStream(), destinationFile);
 //            } catch (IOException ex) {
 //                Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
 //            }
 //        }
 //        
 //        String responseText = null;
 //        try {
 //            String wpsResponse = postToWPS(utilityWpsUrl, wfsEndpoint, destinationFile);
 //
 //            responseText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 //                         + "<wpsResponse><![CDATA[" + wpsResponse + "]]></wpsResponse>";
 //
 //        } catch (Exception ex) {
 //            Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
 //            sendErrorResponse(response, "Unable to upload file");
 //            return;
 //        } finally {
 //            FileUtils.deleteQuietly(destinationFile);
 //        }
 //        
 //        sendResponse(response, responseText);
 //    }//    @Override
 //    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 //
 //        int maxFileSize = Integer.parseInt(request.getParameter("maxfilesize"));
 //        int fileSize = Integer.parseInt(request.getHeader("Content-Length"));
 //        if (fileSize > maxFileSize) {
 //            sendErrorResponse(response, "Upload exceeds max file size of " + maxFileSize + " bytes");
 //            return;
 //        }
 //
 //        // qqfile is parameter passed by our javascript uploader
 //        String filename = request.getParameter("qqfile");
 //        String utilityWpsUrl = request.getParameter("utilitywps");
 //        String wfsEndpoint = request.getParameter("wfs-url");
 //        String tempDir = System.getProperty("java.io.tmpdir");
 //
 //        File destinationFile = new File(tempDir + File.separator + filename);
 //
 //        // Handle form-based upload (from IE)
 //        if (ServletFileUpload.isMultipartContent(request)) {
 //            FileItemFactory factory = new DiskFileItemFactory();
 //            ServletFileUpload upload = new ServletFileUpload(factory);
 //
 //            // Parse the request
 //            FileItemIterator iter;
 //            try {
 //                iter = upload.getItemIterator(request);
 //                while (iter.hasNext()) {
 //                    FileItemStream item = iter.next();
 //                    String name = item.getFieldName();
 //                    if ("qqfile".equals(name)) {
 //                        saveFileFromRequest(item.openStream(), destinationFile);
 //                        break;
 //                    }
 //                }
 //            } catch (Exception ex) {
 //                sendErrorResponse(response, "Unable to upload file");
 //                return;
 //            }
 //        } else {
 //            // Handle octet streams (from standards browsers)
 //            try {
 //                saveFileFromRequest(request.getInputStream(), destinationFile);
 //            } catch (IOException ex) {
 //                Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
 //            }
 //        }
 //        
 //        String responseText = null;
 //        try {
 //            String wpsResponse = postToWPS(utilityWpsUrl, wfsEndpoint, destinationFile);
 //
 //            responseText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 //                         + "<wpsResponse><![CDATA[" + wpsResponse + "]]></wpsResponse>";
 //
 //        } catch (Exception ex) {
 //            Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
 //            sendErrorResponse(response, "Unable to upload file");
 //            return;
 //        } finally {
 //            FileUtils.deleteQuietly(destinationFile);
 //        }
 //        
 //        sendResponse(response, responseText);
 //    }
 
     static void sendResponse(HttpServletResponse response, Map<String, String> responseMap) {
         responseMap.put("success", "true");
         sendJSONResponse(response, responseMap);
     }
 
     static void sendErrorResponse(HttpServletResponse response, Map<String, String> responseMap) {
         responseMap.put("success", "false");
         sendJSONResponse(response, responseMap);
     }
 
     static void sendJSONResponse(HttpServletResponse response, Map<String, String> responseMap) {
         String responseContent = new Gson().toJson(responseMap);
         response.setContentType("application/json");
         response.setCharacterEncoding("utf-8");
         response.setHeader("Content-Length", Integer.toString(responseContent.length()));
 
         try {
             Writer writer = response.getWriter();
             writer.write(responseContent);
             writer.close();
         } catch (IOException ex) {
             Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void saveFileFromInputStream(InputStream is, File destinationFile) throws IOException {
         FileOutputStream os = null;
         try {
             os = new FileOutputStream(destinationFile);
             IOUtils.copy(is, os);
         } finally {
             IOUtils.closeQuietly(os);
         }
     }
 //
 //    private String postToWPS(String url, String wfsEndpoint, File uploadedFile) throws IOException, MessagingException {
 //        HttpPost post = null;
 //        HttpClient httpClient = new DefaultHttpClient();
 //
 //        post = new HttpPost(url);
 //
 //        File wpsRequestFile = createWPSReceiveFilesXML(uploadedFile, wfsEndpoint);
 //        FileInputStream wpsRequestInputStream = null;
 //
 //      try {
 //            wpsRequestInputStream = new FileInputStream(wpsRequestFile);
 //
 //            AbstractHttpEntity entity = new InputStreamEntity(wpsRequestInputStream, wpsRequestFile.length());
 //
 //            post.setEntity(entity);
 //
 //            HttpResponse response = httpClient.execute(post);
 //
 //            return EntityUtils.toString(response.getEntity());
 //
 //        } finally {
 //            IOUtils.closeQuietly(wpsRequestInputStream);
 //            FileUtils.deleteQuietly(wpsRequestFile);
 //        }
 //    }
 //
 //    private static File createWPSReceiveFilesXML(final File uploadedFile, final String wfsEndpoint) throws IOException, MessagingException {
 //
 //        File wpsRequestFile = null;
 //        FileOutputStream wpsRequestOutputStream = null;
 //        FileInputStream uploadedInputStream = null;
 //
 //        try {
 //            wpsRequestFile = File.createTempFile("wps.upload.", ".xml");
 //            wpsRequestOutputStream = new FileOutputStream(wpsRequestFile);
 //            uploadedInputStream = new FileInputStream(uploadedFile);
 //
 //            wpsRequestOutputStream.write(new String(
 //                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 //                    + "<wps:Execute service=\"WPS\" version=\"1.0.0\" "
 //                    + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
 //                    + "xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
 //                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
 //                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 //                    + "xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
 //                    + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
 //                    + "<ows:Identifier>gov.usgs.cida.gdp.wps.algorithm.filemanagement.ReceiveFiles</ows:Identifier>"
 //                    + "<wps:DataInputs>"
 //                    + "<wps:Input>"
 //                    + "<ows:Identifier>filename</ows:Identifier>"
 //                    + "<wps:Data>"
 //                    + "<wps:LiteralData>"
 //                    + StringEscapeUtils.escapeXml(uploadedFile.getName().replace(".zip", ""))
 //                    + "</wps:LiteralData>"
 //                    + "</wps:Data>"
 //                    + "</wps:Input>"
 //                    + "<wps:Input>"
 //                    + "<ows:Identifier>wfs-url</ows:Identifier>"
 //                    + "<wps:Data>"
 //                    + "<wps:LiteralData>"
 //                    + StringEscapeUtils.escapeXml(wfsEndpoint)
 //                    + "</wps:LiteralData>"
 //                    + "</wps:Data>"
 //                    + "</wps:Input>"
 //                    + "<wps:Input>"
 //                    + "<ows:Identifier>file</ows:Identifier>"
 //                    + "<wps:Data>"
 //                    + "<wps:ComplexData mimeType=\"application/x-zipped-shp\" encoding=\"Base64\">").getBytes());
 //            IOUtils.copy(uploadedInputStream, new Base64OutputStream(wpsRequestOutputStream, true, 0, null));
 //            wpsRequestOutputStream.write(new String(
 //                    "</wps:ComplexData>"
 //                    + "</wps:Data>"
 //                    + "</wps:Input>"
 //                    + "</wps:DataInputs>"
 //                    + "<wps:ResponseForm>"
 //                    + "<wps:ResponseDocument>"
 //                    + "<wps:Output>"
 //                    + "<ows:Identifier>result</ows:Identifier>"
 //                    + "</wps:Output>"
 //                    + "<wps:Output>"
 //                    + "<ows:Identifier>wfs-url</ows:Identifier>"
 //                    + "</wps:Output>"
 //                    + "<wps:Output>"
 //                    + "<ows:Identifier>featuretype</ows:Identifier>"
 //                    + "</wps:Output>"
 //                    + "</wps:ResponseDocument>"
 //                    + "</wps:ResponseForm>"
 //                    + "</wps:Execute>").getBytes());
 //        } finally {
 //            IOUtils.closeQuietly(wpsRequestOutputStream);
 //            IOUtils.closeQuietly(uploadedInputStream);
 //        }
 //        return wpsRequestFile;
 //    }
 }

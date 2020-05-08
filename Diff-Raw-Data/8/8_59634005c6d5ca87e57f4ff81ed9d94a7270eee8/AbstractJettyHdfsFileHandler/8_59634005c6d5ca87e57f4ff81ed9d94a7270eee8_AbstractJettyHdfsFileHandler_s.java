 package net.joshdevins.hadoop.utils.io.http;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.joshdevins.hadoop.utils.io.IOUtils;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 /**
  * Base class for all Jetty HDFS file {@link Handler}s.
  * 
  * @author Josh Devins
  */
 public abstract class AbstractJettyHdfsFileHandler extends AbstractHandler implements Handler {
 
     private static MimetypesFileTypeMap MIME_TYPES_MAP = new MimetypesFileTypeMap();
 
     static {
         MIME_TYPES_MAP.addMimeTypes("image/png png PNG");
     }
 
     private final Map<String, byte[]> errorImages = new HashMap<String, byte[]>();
 
     private final String rootPathInFileSystem;
 
     private final Configuration conf;
 
     private final FileSystem fileSystem;
 
     public AbstractJettyHdfsFileHandler(final String rootPathInFileSystem) throws IOException {
 
         Validate.notEmpty(rootPathInFileSystem, "Root path in filesystem is required");
 
         conf = new Configuration();
 
         Path rootPath = new Path(rootPathInFileSystem);
         fileSystem = rootPath.getFileSystem(conf);
 
         // check for root path, must be a dir
         Validate.isTrue(fileSystem.exists(rootPath), "Root path in filesystem does not exist: " + rootPathInFileSystem);
 
         FileStatus fileStatus = fileSystem.getFileStatus(rootPath);
         Validate.isTrue(fileStatus.isDir(), "Root path in filesystem is not a directory: " + rootPathInFileSystem);
 
         this.rootPathInFileSystem = rootPathInFileSystem;
 
         // load error images
         addErrorImage("black");
         addErrorImage("white");
         addErrorImage("transparent");
     }
 
     public Configuration getConfiguration() {
         return conf;
     }
 
     public FileSystem getFileSystem() {
         return fileSystem;
     }
 
     public String getRootPathInFileSystem() {
         return rootPathInFileSystem;
     }
 
     public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
             final HttpServletResponse response) throws IOException, ServletException {
 
         try {
             handleWithExceptionTranslation(target, baseRequest, request, response);
         } catch (HttpErrorException hee) {
 
             response.setStatus(hee.getStatusCode());
 
             // test to see if we just want to return a known image for this error
             String errorParam = request.getParameter(String.valueOf(hee.getStatusCode()));
 
             if (!StringUtils.isBlank(errorParam) && errorImages.containsKey(errorParam.toLowerCase(Locale.UK))) {
                 handleImageHttpErrorException(errorParam, hee, request, response);
 
             } else {
                 handleStandardHttpErrorException(hee, request, response);
             }
 
             ((Request) request).setHandled(true);
         }
     }
 
     protected abstract void handleWithExceptionTranslation(final String target, final Request baseRequest,
             final HttpServletRequest request, final HttpServletResponse response);
 
     private void addErrorImage(final String imageName) throws IOException {
         errorImages.put(imageName, IOUtils.getBytesFromResource("/images/" + imageName + ".png"));
     }
 
     private void handleImageHttpErrorException(final String errorParam, final HttpErrorException hee,
             final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 
         response.setContentType("image/png");
         ServletOutputStream os = response.getOutputStream();
 
         try {
             os.write(errorImages.get(errorParam));
             os.flush();
         } finally {
             os.close();
         }
     }
 
     private void handleStandardHttpErrorException(final HttpErrorException hee, final HttpServletRequest request,
             final HttpServletResponse response) throws IOException {
 
         response.setContentType("text/html");
 
         String errorString = "Error " + hee.getStatusCode();
 
         PrintWriter writer = response.getWriter();
 
         try {
             writer.println("<html><head><title>" + errorString + "</title></head><body>");
             writer.println("<h2>" + errorString + "</h2>");
 
             if (!StringUtils.isBlank(hee.getMessage())) {
                 writer.println("<p><b>");
                 writer.println(hee.getMessage());
                 writer.println("</b></p>");
             }
 
             if (hee.getCause() != null) {
                 writer.println("<p>");
                 writer.println(hee.getCause().getMessage() + "<br />");
 
                 StackTraceElement[] stackTrace = hee.getCause().getStackTrace();
                 for (StackTraceElement e : stackTrace) {
                     writer.println(e.toString() + "<br />");
                 }
 
                 writer.println("</p>");
             }
 
             writer.println("</body></html>");
 
             writer.flush();
         } finally {
             writer.close();
         }
     }
 
     /**
      * A very simple way to check mime-type. If this is not good enough, use something like mime-utils or JMimeMagic.
      */
     public static String getMimeType(final String filename) {
         return MIME_TYPES_MAP.getContentType(filename);
     }
 }

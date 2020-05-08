 /**
  * Copyright
  */
 package scldclng;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class UploadServlet extends HttpServlet {
 
     private static final long serialVersionUID = 2462714031432357354L;
     private static final Pattern UPLOAD_ID_PATTERN = Pattern.compile("^/([^/]+)/?$");
 
     private final Map<String, Integer> progressMap = new ConcurrentHashMap<String, Integer>();
 
     @Override
     protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
             IOException {
 
         final String uploadId = fetchUploadId(req);
 
         if (uploadId == null) {
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
         } else {
             final Integer progress = progressMap.get(uploadId);
             if (progress != null) {
                 resp.getWriter().print(progress);
             } else {
                 resp.sendError(HttpServletResponse.SC_NOT_FOUND);
             }
         }
     }
 
     @Override
     protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
             IOException {
         final String uploadId = fetchUploadId(req);
         if (uploadId == null || !req.getHeader("Content-Type").toLowerCase().startsWith("multipart/form-data")) {
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
         } else {
             doUpload(uploadId, req, resp);
         }
     }
 
     private void doUpload(final String uploadId, final HttpServletRequest req, final HttpServletResponse resp)
             throws ServletException, IOException {
 
         final FileUploader uploader = new FileUploader(req, "file_input");
 
         try {
             final long contentLength = Long.parseLong(req.getHeader("Content-Length"));
 
             progressMap.put(uploadId, 0);
 
             if (contentLength > 0) {
                 final ProgressUpdater progressListener = new ProgressUpdater();
                 progressListener.setUploadId(uploadId);
                 progressListener.setContentLength(contentLength);
                 uploader.setProgressListener(progressListener);
             }
 
             final String filename = uploader.upload(); // extract original filename
 
             req.setAttribute("file_url", toUploadedFileUrl(uploadId, filename, req));
             req.getRequestDispatcher("/META-INF/upload-complete.jsp").forward(req, resp);
         } finally {
             // cleanup progress data
             progressMap.remove(uploadId);
         }
     }
 
     String toUploadedFileUrl(final String uploadId, final String filename, final HttpServletRequest req) {
         final StringBuffer requestURL = req.getRequestURL();
        final String pathInfo = req.getPathInfo();
        final String servletPath = req.getServletPath();
         final StringBuilder sb = new StringBuilder()
                .append(requestURL.substring(0, requestURL.length() - pathInfo.length() - servletPath.length()))
                .append("/uploaded/").append(uploadId);
         if (filename != null) {
             sb.append("-").append(filename);
         }
         return sb.toString();
     }
 
     /**
      * Fetch upload id from request. Valid upload id must be expressed as a single item in path, e.g. /sdfs/.
      * Forms as /john/does/blues are not allowed.
      *
      * @param req request object
      * @return upload id if path is valid, otherwise {@code null}
      */
     private String fetchUploadId(final HttpServletRequest req) {
         final String pathInfo = req.getPathInfo();
         return pathInfo == null ? null : fetchUploadId(pathInfo);
     }
 
     private String fetchUploadId(final String pathInfo) {
         final Matcher matcher = UPLOAD_ID_PATTERN.matcher(pathInfo);
         return matcher.matches() ? matcher.group(1) : null;
     }
 
     private class ProgressUpdater implements ProgressListener {
 
         private long contentLength;
         private String uploadId;
 
         public void setContentLength(final long contentLength) {
             this.contentLength = contentLength;
         }
 
         public void setUploadId(final String uploadId) {
             this.uploadId = uploadId;
         }
 
         @Override
         public void onBytesRead(final long bytesRead) {
             progressMap.put(uploadId, (int) Math.ceil((double) bytesRead / contentLength * 100));
         }
     }
 
 }

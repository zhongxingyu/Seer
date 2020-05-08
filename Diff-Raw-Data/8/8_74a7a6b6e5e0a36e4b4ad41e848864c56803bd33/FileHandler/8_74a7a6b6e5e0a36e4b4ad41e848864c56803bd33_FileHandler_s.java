 package ch.rollis.emma.contenthandler;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 
 import ch.rollis.emma.Emma;
 import ch.rollis.emma.context.ServerContext;
 import ch.rollis.emma.request.Request;
 import ch.rollis.emma.response.Response;
 import ch.rollis.emma.response.ResponseFactory;
 import ch.rollis.emma.response.ResponseStatus;
 import ch.rollis.emma.util.DateConverter;
 import ch.rollis.emma.util.MimeTypes;
 
 public class FileHandler implements ContentHandler {
     @Override
     public Response process(Request request, ServerContext context) {
         File docRoot, file;
 
         ResponseFactory responseFacotry = new ResponseFactory();
 
         try {
             docRoot = context.getDocumentRoot().getCanonicalFile();
 
             String uri = request.getRequestURI().getPath();
 
            // Prohibit getting out of current directory
            if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 ) {
                return responseFacotry.getResponse(request, ResponseStatus.FORBIDDEN);
            }

             file = new File(docRoot, uri);
             if (!file.exists()) {
                 return responseFacotry.getResponse(request, ResponseStatus.NOT_FOUND);
             }
 
             if (file.isDirectory()) {
                 if (!uri.endsWith("/")) {
                     Response response = responseFacotry.getResponse(request,
                             ResponseStatus.SEE_OTHER);
                     response.setHeader("Location", uri + "/");
                     return response;
                 }
             }
 
             // check default index files
             for (String defaultFile : Emma.DEFAULT_FILES) {
                 if (new File(file, defaultFile).exists()) {
                     file = new File(file, "/" + defaultFile);
                     break;
                 }
             }
 
             if (!file.canRead()) {
                 return responseFacotry.getResponse(request, ResponseStatus.FORBIDDEN);
             }
 
             if (file.isDirectory()) {
                 // still a directory
                 if (context.allowsIndexes()) {
                     String listing = getDirectoryListing(uri, file);
                     Response response = responseFacotry.getResponse(request, ResponseStatus.OK);
                     response.setEntity(listing);
                     return response;
                 } else {
                     return responseFacotry.getResponse(request, ResponseStatus.FORBIDDEN);
                 }
             }
 
             String contentType = MimeTypes.evaluate(MimeTypes.getExtension(file));
             if (contentType == null) {
                 contentType = "application/octet-stream";
             }
 
             Response response = responseFacotry.getResponse(request);
             response.setStatus(ResponseStatus.OK);
             response.setContentType(contentType);
             response.setContentLength(String.valueOf(file.length()));
             response.setLastModified(DateConverter.formatRfc1123(new Date(file.lastModified())));
 
             byte fileContent[] = new byte[(int) file.length()];
             new FileInputStream(file).read(fileContent);
             response.setEntity(fileContent);
 
             return response;
 
         } catch (IOException e) {
             return responseFacotry.getResponse(request, ResponseStatus.INTERNAL_SERVER_ERROR);
         }
     }
 
     public String getDirectoryListing(String uri, File targetDir) {
         String[] files = targetDir.list();
         String msg = "<html><head><title>Index of " + uri + "</title></head><body><h1>Index of "
                 + uri + "</h1><pre><hr />";
 
         if (uri.length() > 1) {
             String u = uri.substring(0, uri.length() - 1);
             int slash = u.lastIndexOf('/');
             if (slash >= 0 && slash < u.length()) {
                 msg += "<img src=\"/images/icons/back.gif\" alt=\"[DIR]\" width=\"20\" height=\"22\"> "
                         + "<a href=\""
                         + uri.substring(0, slash + 1)
                         + "\">Parent Directory</a>\r\n";
             }
         }
 
         if (files != null) {
             for (int i = 0; i < files.length; ++i) {
                 File curFile = new File(targetDir, files[i]);
 
                 if (curFile.isDirectory()) {
                     files[i] += "/";
                    msg += "<img src=\"/images/folder.gif\" alt=\"[DIR]\" width=\"20\" height=\"22\"> ";
                 } else {
                     String mimetype = MimeTypes.evaluate(MimeTypes.getExtension(files[i]));
                     if (mimetype.startsWith("image")) {
                         msg += "<img src=\"/images/icons/image.gif\" alt=\"[DIR]\" width=\"20\" height=\"22\"> ";
                     } else {
                         msg += "<img src=\"/images/icons/text.gif\" alt=\"[DIR]\" width=\"20\" height=\"22\"> ";
                     }
                 }
                 msg += "<a href=\"" + files[i] + "\">" + files[i] + "</a>";
 
                 if (curFile.isFile()) {
                     long len = curFile.length();
                     msg += " &nbsp;";
                     if (len < 1024) {
                         msg += len;
                     } else if (len < 1024 * 1024) {
                         msg += len / 1024 + "." + (len % 1024 / 10 % 100) + "K";
                     } else {
                         msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + "M";
                     }
                 }
                 msg += "\r\n";
             }
         }
         msg += "</pre><hr />" + Emma.VERSION;
         msg += "</body></html>";
 
         return msg;
     }
 }

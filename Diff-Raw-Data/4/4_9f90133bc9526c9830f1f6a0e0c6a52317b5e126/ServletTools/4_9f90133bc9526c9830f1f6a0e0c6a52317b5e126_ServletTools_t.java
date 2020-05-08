 package nmd.rss.collector.rest;
 
 import com.google.gson.Gson;
 import nmd.rss.collector.exporter.FeedExporter;
 import nmd.rss.collector.exporter.FeedExporterException;
 import nmd.rss.collector.feed.FeedHeader;
 import nmd.rss.collector.feed.FeedItem;
 import nmd.rss.collector.updater.FeedService;
 import nmd.rss.collector.updater.FeedServiceException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static nmd.rss.collector.util.Assert.assertNotNull;
 import static nmd.rss.collector.util.CloseableTools.close;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 18.05.13
  */
 public final class ServletTools {
 
     private static final Logger LOGGER = Logger.getLogger(ServletTools.class.getName());
 
    private static final String UTF_8 = "UTF-8";

     public static UUID parseFeedId(final String pathInfo) {
 
         if (pathInfoIsEmpty(pathInfo)) {
             return null;
         }
 
         try {
             final String data = pathInfo.substring(1);
 
             return UUID.fromString(data);
         } catch (Exception exception) {
             LOGGER.log(Level.SEVERE, String.format("Error parse feedId from [ %s ]", pathInfo), exception);
 
             return null;
         }
     }
 
     public static boolean pathInfoIsEmpty(final String pathInfo) {
         return pathInfo == null || pathInfo.length() < 2;
     }
 
     //TODO not from here
     public static String exportFeed(final UUID feedId, final FeedService feedService) throws FeedServiceException, FeedExporterException {
         assertNotNull(feedId);
         assertNotNull(feedService);
 
         final FeedHeader header = feedService.loadHeader(feedId);
 
         if (header == null) {
             LOGGER.severe(String.format("Can not find feed header for feed id [ %s ]", feedId));
 
             return "";
         }
 
         final List<FeedItem> items = feedService.loadItems(feedId);
 
         if (items == null) {
             LOGGER.severe(String.format("Can not find feed items for feed id [ %s ]", feedId));
 
             return "";
         }
 
         final String generated = FeedExporter.export(header, items);
 
         LOGGER.info(String.format("Feed for feed id [ %s ] generated successfully", feedId));
 
         return generated;
     }
 
     public static String readRequestBody(final HttpServletRequest request) throws IOException {
         assertNotNull(request);
 
         BufferedReader reader = null;
 
         try {
             reader = request.getReader();
             final StringBuilder result = new StringBuilder();
 
             String line;
 
             while ((line = reader.readLine()) != null) {
                 result.append(line);
             }
 
             return result.toString();
         } finally {
             close(reader);
         }
     }
 
     public static void writeResponseBody(final ResponseBody responseBody, final HttpServletResponse response) throws IOException {
         assertNotNull(responseBody);
         assertNotNull(response);
 
         final String contentType = responseBody.contentType == ContentType.JSON ? "application/json" : "application/rss+xml";
         response.setContentType(contentType);
        response.setCharacterEncoding(UTF_8);
 
         response.getWriter().print(responseBody.content);
     }
 
     public static void writeException(final Exception exception, final HttpServletResponse response) {
         assertNotNull(exception);
         assertNotNull(response);
 
         String message = exception.getMessage();
         message = message == null || message.isEmpty() ? exception.getClass().getSimpleName() : message;
 
         final ErrorResponse errorResponse = new ErrorResponse(0, message, "Please try again later");
         final String content = new Gson().toJson(errorResponse);
         final ResponseBody responseBody = new ResponseBody(ContentType.JSON, content);
 
         try {
             writeResponseBody(responseBody, response);
         } catch (IOException e) {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
     }
 
     private ServletTools() {
         // empty
     }
 
 }

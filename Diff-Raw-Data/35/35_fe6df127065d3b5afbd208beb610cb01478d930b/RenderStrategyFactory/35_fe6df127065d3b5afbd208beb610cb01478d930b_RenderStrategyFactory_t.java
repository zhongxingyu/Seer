 package gr.dsigned.springcrudutils.strategies;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 /**
  * @author nk
  */
 public class RenderStrategyFactory {
 
     private static List<ContentType> types = newArrayList(ContentType.JSON, ContentType.APPLICATION_XML, ContentType.TEXT_HTML, ContentType.TEXT_PLAIN);
 
     public static RenderStrategy getRenderStrategy(HttpServletRequest request) {
         RenderStrategy strategy = null;
         String accept = request.getParameter("accept");
         String callBack = request.getParameter("callback");
 
         if (accept != null) {
             if (accept.equals("json")) {
                 if (callBack == null) {
                     strategy = new JsonStrategy();
                 } else {
                     strategy = new JsonpStrategy(callBack);
                 }
             } else if (accept.equals("xml")) {
                 strategy = new XMLStrategy();
             } else if (accept.equals("xfs")) {
                 strategy = new CrossFrameStrategy();
             } else if (accept.equals("text")) {
                 strategy = new TextPlainStrategy();
             } else if (accept.equals("csv")) {
                 strategy = new CsvStrategy();
             }
         } else {
             accept = request.getHeader("Accept");
             ContentType type = ContentType.getBestContentType(accept, types);
             if (type != null) {
                 if (type.getSubType().equals("xml")) {
                     strategy = new XMLStrategy();
                 } else if (type.getSubType().equals("json")) {
                     if (callBack == null) {
                         strategy = new JsonStrategy();
                     } else {
                         strategy = new JsonpStrategy(callBack);
                     }
                 } else if (type.getSubType().equals("html")) {
                     strategy = new CrossFrameStrategy();
                 } else if (type.getType().equals("text") && type.getSubType().equals("plain")) {
                     strategy = new TextPlainStrategy();
                 }
             }
         }
        return strategy == null ? new JsonStrategy() : strategy;
     }
 }

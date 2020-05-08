 package me.vgv.requestlog.handler;
 
 import me.vgv.requestlog.database.DBManager;
 import me.vgv.requestlog.database.entity.Response;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author Vasily Vasilkov (vgv@vgv.me)
  */
 public class WatchHandler implements Handler {
 
     private static final String ACTION_PARAM_NAME = "request-log___action";
     private static final String CLEAR_ALL_ACTION = "clearall";
 
     private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>()  {
         @Override
         protected DateFormat initialValue() {
             return new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
         }
     };
 
     private final DBManager dbManager;
 
     public WatchHandler(DBManager dbManager) {
         this.dbManager = dbManager;
     }
 
     private String getHeadHtml() {
         StringBuilder head = new StringBuilder();
         head.append("<head>");
         head.append(HandlerUtils.getFavicon());
         head.append(HandlerUtils.getMeta());
         head.append("<style type='text/css'>");
         head.append(".header {font-size: 120%;}");
         head.append(".body {}");
         head.append("</style>");
         head.append("</head>");
         return head.toString();
     }
 
     private String getItemHtml(Response response, DateFormat dateFormat) {
         StringBuilder item = new StringBuilder();
 
         item.append("<div class='header'>");
         item.append(response.getId()).append("&nbsp;&nbsp;&nbsp;&nbsp;");
         item.append(dateFormat.format(response.getDate())).append("&nbsp;&nbsp;&nbsp;&nbsp;");
         item.append(response.getMethod()).append("&nbsp;&nbsp;&nbsp;&nbsp;");
         item.append(response.getSchema()).append("&nbsp;&nbsp;&nbsp;&nbsp;");
         item.append("</div>");
 
         item.append("<div class='body'>");
        item.append(response.getParameters());
         item.append("</div>");
 
         return item.toString();
     }
 
     private String getItemsHtml(List<Response> responses) {
         StringBuilder items = new StringBuilder(responses.size() * 200);
 
         DateFormat dateFormat = DATE_FORMAT_THREAD_LOCAL.get();
 
         Iterator<Response> iterator =  responses.iterator();
         if (iterator.hasNext()) {
             items.append(getItemHtml(iterator.next(), dateFormat));
 
             while (iterator.hasNext()) {
                 items.append("<hr/>");
 
                 items.append(getItemHtml(iterator.next(), dateFormat));
             }
         }
 
         return items.toString();
     }
 
     private String getClearAllButtonHtml(String key) {
         StringBuilder form = new StringBuilder();
         form.append("<div style='position:fixed; top:10px; right:10px;'>");
         form.append("<form action='/").append(key).append("?watch' method='post'>");
         form.append("<input type='hidden' name='" + ACTION_PARAM_NAME + "' value='" + CLEAR_ALL_ACTION + "' />");
         form.append("<input type='submit' value='Clear All' />");
         form.append("</form>");
         form.append("</div>");
         return form.toString();
     }
 
     @Override
     public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
         String key = request.getRequestURI();
         if (key.startsWith("/")) {
             key = key.substring(1);
         }
 
         // check possible action before display page
         if (CLEAR_ALL_ACTION.equals(request.getParameter(ACTION_PARAM_NAME))) {
             dbManager.deleteAll(key);
         }
 
         response.getWriter().write("<html>");
 
         response.getWriter().write(getHeadHtml());
 
         response.getWriter().write("<body>");
 
         // "Clear all" button
         response.getWriter().write(getClearAllButtonHtml(key));
 
         // items
         response.getWriter().write(getItemsHtml(dbManager.find(key)));
 
         response.getWriter().write("</body>");
         response.getWriter().write("</html>");
     }
 }

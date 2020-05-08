 package vivo.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 import org.springframework.web.portlet.bind.annotation.ResourceMapping;
 import vivo.shared.dto.VivoQueryDTO;
 import vivo.shared.services.VivoQueryService;
 import vivo.util.MultiportalUserInfoUtilities;
 
 import javax.portlet.*;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 @Controller
 @RequestMapping("VIEW")
 public class VivoController {
     private static final Logger LOG = LoggerFactory.getLogger(VivoController.class);
 
     private String vivoUrl;
 
     @Autowired
     private VivoQueryService vivoQueryService;
 
     @RenderMapping
     public String doView(RenderRequest request, RenderResponse response, ModelMap modelMap) {
         if (vivoUrl == null || "".equals(vivoUrl)) {
             LOG.warn("vivoUrl must be set in configuration");
         }
 
         modelMap.put("vivoUrl", vivoUrl);
         return "view";
     }
 
     // very simplistic POJO to JSON (oesn't convert arrays/lists/sets properly, etc.)
     private String mapToJson(Map map) {
         StringBuffer sb = new StringBuffer("{");
         if (map != null) {
             Set keySet = map.keySet();
             Iterator iterator = keySet.iterator();
             boolean delimit = false;
             while (iterator.hasNext()) {
                 if (delimit) {
                     sb.append(", ");
                 }
                 String key = (String) iterator.next();
                 sb.append("\"");
                 appendIfNotNull(key, sb);
                 sb.append("\": \"");
                 Object o = map.get(key);
                 if (o instanceof Map) {
                     appendIfNotNull(mapToJson((Map) o), sb);
                 }
                 appendIfNotNull(map.get(key), sb);
                 sb.append("\"");
                 delimit = true;
             }
         }
         sb.append("}");
         return sb.toString();
     }
 
     private void appendIfNotNull(Object o, StringBuffer sb) {
         if (o != null) {
             sb.append(o.toString());
         }
     }
 
     @ResourceMapping(value = "info")
     public void info(ResourceRequest request, ResourceResponse response)
             throws Exception {
         PrintWriter writer = response.getWriter();
         writer.print("{" +
                 "\"vivoUrl\": \"" + vivoUrl + "\"" +
                 ", \"totalMemory\": \"" + Runtime.getRuntime().totalMemory() + "\"" +
                 ", \"maxMemory\": \"" + Runtime.getRuntime().maxMemory() + "\"" +
                 ", " + mapToJson((Map) request.getAttribute(PortletRequest.USER_INFO)) +
                 "}");
     }
 
     @ResourceMapping(value = "addHistory")
     public void addHistory(ResourceRequest request, ResourceResponse response)
             throws Exception {
         PrintWriter writer = null;
         try {
             writer = response.getWriter();
             String singleQuery = request.getParameter("history");
             if (singleQuery==null) {
                 LOG.info("Must supply request parameter 'history' to addHistory in vivo portlet resource request");
                 writer.print("{\"error\": \"missing request param called: history\"}");
                 return;
             }
 
             String userId = getUserId(request);
             
             // TODO: we shouldn't supply the object id here, only the userId. fix.
             String previousHistory = ""; 
             VivoQueryDTO queryResult = vivoQueryService.findVivoQuery(99);
             if (queryResult!=null) {
                 LOG.debug("Got non-null DB query result for getting existing history in vivo portlet addHistory");
                 String historyResult = queryResult.getHistory();
                 LOG.debug("Query result's existing history in vivo portlet addHistory was " + historyResult);
                 if (historyResult!=null) {
                     previousHistory = historyResult;
                 }
             }
 
            String history = minifyingAppendToPipeDelimitedString(singleQuery, previousHistory);
 
             // TODO: we shouldn't supply the object id here, only the userId. fix.
             vivoQueryService.saveOrUpdateVivoQuery(99, userId, history);
             writer.print("{\"history\": \"" + history + "\"}");
         }
         catch (Throwable t) {
             LOG.error("addHistory in vivo portlet resource request failed", t);
             if (writer != null) {
                 writer.print("{\"error\": \"addHistory in vivo portlet resource request failed: " + t + "\"}");
             }
         }
     }
 
    private String minifyingAppendToPipeDelimitedString(String singleQuery, String previousHistory) {
 	    if (previousHistory==null || "".equals(previousHistory)) {
 		    return singleQuery;
 	    }
 	    else if (singleQuery==null || "".equals(singleQuery)) {
 		    return previousHistory;
 		}
 	
 	    StringBuffer sb = new StringBuffer(singleQuery);
	    String[] sArray = previousHistory.split("\\|");
 	    for (int i=0; i<sArray.length; i++) {
 		    String s = sArray[i];
 		    // only keep history that doesn't contain the new query
 		    if (s!=null && !"".equals(s)) {
 			    if (!s.equals(singleQuery)) {
 				    sb.append('|');
 				    sb.append(s);
 			    }
 		    }
 	    }
 	    return sb.toString();
     }
 
     @ResourceMapping(value = "clearHistory")
     public void clearHistory(ResourceRequest request, ResourceResponse response)
             throws Exception {
         PrintWriter writer = null;
         try {
             writer = response.getWriter();
 
             String userId = getUserId(request);
             // TODO: we shouldn't supply the object id here, only the userId. fix.
             vivoQueryService.saveOrUpdateVivoQuery(99, userId, "");
             writer.print("{\"history\": \"\"}");
         }
         catch (Throwable t) {
             LOG.error("clearHistory in vivo portlet resource request failed", t);
             if (writer != null) {
                 writer.print("{\"error\": \"clearHistory in vivo portlet resource request failed: " + t + "\"}");
             }
         }
     }
 
     @ResourceMapping(value = "updateHistory")
     public void updateHistory(ResourceRequest request, ResourceResponse response)
             throws Exception {
         PrintWriter writer = null;
         try {
             writer = response.getWriter();
             String history = request.getParameter("history");
             if (history==null) {
                 LOG.info("Must supply request parameter 'history' to updateHistory in vivo portlet resource request");
                 writer.print("{\"error\": \"missing request param called: history\"}");
                 return;
             }
 
             String userId = getUserId(request);
             // TODO: we shouldn't supply the object id here, only the userId. fix.
             vivoQueryService.saveOrUpdateVivoQuery(99, userId, history);
             writer.print("{\"history\": \"" + history + "\"}");
         }
         catch (Throwable t) {
             LOG.error("updateHistory in vivo portlet resource request failed", t);
             if (writer != null) {
                 writer.print("{\"error\": \"updateHistory in vivo portlet resource request failed: " + t + "\"}");
             }
         }
     }
 
     private String getUserId(ResourceRequest request) {
         Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
         String userId = MultiportalUserInfoUtilities.getUserId(userInfo);
         return userId;
     }
 
     @ResourceMapping(value = "getHistory")
     public void getHistory(ResourceRequest request, ResourceResponse response)
             throws Exception {
         PrintWriter writer = null;
         try {
             writer = response.getWriter();
             String history = "";
 
             // TODO: we shouldn't supply the object id here, only the userId. fix.
             //String userId = getUserId(request);
             VivoQueryDTO queryResult = vivoQueryService.findVivoQuery(99);
             if (queryResult!=null) {
                 LOG.debug("Got non-null DB query result for vivo portlet getHistory");
                 String historyResult = queryResult.getHistory();
                 LOG.debug("Query result's history for vivo portlet getHistory was " + historyResult);
                 if (historyResult!=null) {
                     history = historyResult;
                 }
             }
             else {
                 LOG.debug("Got null DB query result for vivo portlet getHistory");
             }
             writer.print("{\"history\": \"" + history + "\"}");
         }
         catch (Throwable t) {
             LOG.error("updateHistory in vivo portlet resource request failed", t);
             if (writer != null) {
                 writer.print("{\"error\": \"updateHistory in vivo portlet resource request failed: " + t + "\"}");
             }
         }
     }
 
     public void setVivoUrl(String vivoUrl) {
         this.vivoUrl = vivoUrl;
     }
 }

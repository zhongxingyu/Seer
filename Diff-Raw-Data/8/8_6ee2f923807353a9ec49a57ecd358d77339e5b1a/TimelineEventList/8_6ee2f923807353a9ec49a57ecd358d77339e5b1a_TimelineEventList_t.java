 package org.koshuke.stapler.simile.timeline;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JsonConfig;
 import net.sf.json.processors.JsonValueProcessor;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
import java.util.Locale;
 
 /**
  * List of {@link Event} that the timeline component will display.
  */
 public class TimelineEventList extends ArrayList<Event> implements HttpResponse {
    private static SimpleDateFormat SDF = new SimpleDateFormat("MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
     /**
      * Renders HTTP response.
      */
     public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
         // Date needs to be converted into iso-8601 date format.
         JsonConfig config = new JsonConfig();
         config.registerJsonValueProcessor(Date.class,new JsonValueProcessor() {
             public Object processArrayValue(Object value, JsonConfig jsonConfig) {
                return value==null ? null : SDF.format(value);
             }
 
             public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) {
                 return processArrayValue(value,jsonConfig);
             }
         });
 
         JSONObject o = new JSONObject();
         o.put("events", JSONArray.fromObject(this,config));
         rsp.setContentType("application/javascript;charset=UTF-8");
         o.write(rsp.getWriter());
     }
 }

 package de.minestar.theterminal.http.pages;
 
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.bukkit.gemo.BukkitHTTP.HTTPEvent;
 import com.bukkit.gemo.BukkitHTTP.Page;
 
 import de.minestar.theterminal.core.Core;
 import de.minestar.theterminal.utils.ChatEvent;
 
 public class tplGetChat extends TemplatePage {
 
     private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
     private static int initialMinutes = 5;
 
     @SuppressWarnings("unchecked")
     public static void execTemplate(Page page, HTTPEvent event) {
         long now = System.currentTimeMillis();
 
         // GET TIMESTAMP FROM PARAMETERS (GET-METHOD)
         long timestamp = System.currentTimeMillis() - (initialMinutes * 60 * 1000);
         if (event.getParameter != null) {
             if (event.getParameter.containsKey("timestamp")) {
                 timestamp = Long.valueOf(event.getParameter.get("timestamp"));
             }
         }
 
         // GET CHATHISTORY
         ArrayList<ChatEvent> chatList = Core.getInstance().getChatManager().getMessagesAfter(timestamp);
 
         // CREATE JSON-OBJECTS
         JSONArray jsonArray = new JSONArray();
 
         // CREATE JSON-TIMESTAMP
         JSONObject timeObject = new JSONObject();
         timeObject.put("timestamp", now);
         jsonArray.add(timeObject);
 
         // BUILD THE HISTORY
         ChatEvent thisEvent;
         for (int i = chatList.size() - 1; i >= 0; i--) {
             thisEvent = chatList.get(i);
             // CREATE JSON-OBJECT
             JSONObject thisObj = new JSONObject();
             thisObj.put("time", getTime(thisEvent.getTimestamp()));
            thisObj.put("player", thisEvent.getDisplayName());
            try {
                thisObj.put("message", URLEncoder.encode(thisEvent.getMessage(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
             jsonArray.add(thisObj);
         }
 
         // FINALLY REPLACE THE TEXT
         page.replaceText("%DATA%", jsonArray.toJSONString());
     }
 
     private static String getTime(long time) {
         return timeFormat.format(new Date(time));
     }
 }

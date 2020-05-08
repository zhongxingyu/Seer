 package ru.yandex.semantic_geo;
 
 import org.apache.commons.lang3.StringUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import ru.yandex.semantic_geo.freebase.FreebaseAPI;
 import ru.yandex.semantic_geo.freebase.FreebaseObject;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: rasifiel
  * Date: 06.10.12
  * Time: 14:31
  */
 public class EntryServlet extends HttpServlet {
 
     private FreebaseAPI api = new FreebaseAPI("v1");
 
     Map<String, FreebaseObject> m2i = new HashMap<String, FreebaseObject>();
 
     @Override
     public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
         InputStream is = req.getInputStream();
         JSONTokener tokener = null;
         try {
             tokener = new JSONTokener(is);
             JSONArray ar = new JSONArray(tokener);
             List<String> texts = new ArrayList<String>(ar.length());
             int cl = 4;
             int[] lens = new int[ar.length() + 1];
             for (int i = 0; i < ar.length(); i++) {
                 lens[i] = cl;
                 cl += ar.getString(i).length() + 4;
                 texts.add(ar.getString(i));
             }
             lens[ar.length()] = cl;
             long startTime = System.currentTimeMillis();
             final String toDetect = "</p>" + StringUtils.join(texts, "</p>");
             URL url = new URL("http://77.37.152.188/WebHighlighterService/WebHighlighterService.svc/Highlight");
             URLConnection connection = url.openConnection();
             connection.setDoInput(true);
             connection.setDoOutput(true);
             connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             OutputStream os = connection.getOutputStream();
             os.write(toDetect.getBytes(Charset.forName("UTF-8")));
             os.close();
             long time = System.currentTimeMillis();
             log("Extracted entities in "+(time-startTime)+" ms");
             startTime=time;
            JSONTokener tokener1 = new JSONTokener(connection.getInputStream());
            JSONArray trans = new JSONArray(tokener1);
             ArrayList<JSONArray> results = new ArrayList<JSONArray>(ar.length());
             for (int i = 0; i < ar.length(); i++) {
                 results.add(new JSONArray());
             }
             Set<String> mids = new HashSet<String>();
             for (int i = 0; i < trans.length(); i++) {
                 JSONObject rs = (JSONObject) trans.get(i);
                 String mid = rs.getString("MID");
                 if (!m2i.containsKey(mid)) {
                     mids.add(mid);
                 }
             }
             m2i.putAll(api.getInfo(mids));
             time = System.currentTimeMillis();
             log("Fetched freebase info in "+(time-startTime)+" ms for "+mids.size()+" entities");
             startTime=time;
             for (int i = 0; i < trans.length(); i++) {
                 JSONObject rs = (JSONObject) trans.get(i);
                 int start = rs.getInt("Start");
                 int length = rs.getInt("Length");
                 String mid = rs.getString("MID");
                 int c = 0;
                 while (lens[c] <= start) {
                     c++;
                 }
                 c--;
                 start -= lens[c];
                 results.get(c).put(
                         new JSONObject().put("start", start).put("length", length).put("data", m2i.get(mid).toJson()));
             }
             JSONArray rr = new JSONArray(results);
             resp.setCharacterEncoding("utf-8");
             resp.setContentType("application/json");
             PrintWriter pw = resp.getWriter();
             pw.print(rr.toString());
             pw.close();
         } catch (JSONException e) {
         }
     }
 }

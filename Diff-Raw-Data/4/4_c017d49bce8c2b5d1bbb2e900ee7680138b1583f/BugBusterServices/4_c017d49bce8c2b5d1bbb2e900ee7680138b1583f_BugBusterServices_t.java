 package org.jenkinsci.plugins.bugbuster;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.net.ssl.HttpsURLConnection;
 import net.sf.json.JSON;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 
 /**
  *
  * @author <a href="mailto:daniel.tralamazza@bugbuster.com">Daniel Tralamazza</a>
  */
 public class BugBusterServices {
 
     public static BugBusterServices newInstance(final String host, final String key) throws MalformedURLException, IOException {
         BugBusterServices serv = new BugBusterServices(host, key);
         if (!serv.ping()) {
             // XXX this check will change once we use HTTP error codes
             throw new IllegalArgumentException("Invalid API key.");
         }
         return serv;
     }
     
     private static StringBuilder stringBuilderFromInputStream(InputStream is) throws IOException {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder builder = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
             builder.append(line);
         }
         return builder;
     }
     
     private HttpsURLConnection createConnection(final String path) throws MalformedURLException, IOException {
         URL url = new URL("https://" + getHost() + path);
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
         String encoding = javax.xml.bind.DatatypeConverter.printBase64Binary((getKey() + ":x").getBytes());
         conn.setRequestProperty("Authorization", "Basic " + encoding);
         conn.setRequestProperty("Accept", "application/json");
         conn.setRequestProperty("Content-Type", "application/json");
         // XXX Please don't ignore certs in production
         try {
             HTTPSConnectionRelaxer.relax(conn);
         } catch (Exception ex) {
             Logger.getLogger(BugBusterServices.class.getName()).log(Level.SEVERE, "Ignore certificate", ex);
         }
         return conn;
     }
 
     public JSON GET(final String path) throws MalformedURLException, IOException {
         HttpsURLConnection conn = createConnection(path);
         conn.setRequestMethod("GET");
         conn.setDoInput(true);
         try {
             return JSONSerializer.toJSON(stringBuilderFromInputStream(conn.getInputStream()).toString());
         } finally {
             conn.disconnect();
         }
     }
 
     public JSON POST(final String path, final String data) throws MalformedURLException, IOException {
         HttpsURLConnection conn = createConnection(path);
         conn.setRequestMethod("POST");
         conn.setDoInput(true);
         conn.setDoOutput(true);
         conn.connect();
         try {
             OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
             osw.write(data);
             osw.close();
             return JSONSerializer.toJSON(stringBuilderFromInputStream(conn.getInputStream()).toString());
         } finally {
             conn.disconnect();
         }
     }
 
     private String host;
     private String key;
 
     public String getHost() {
         return host;
     }
 
     public String getKey() {
         return key;
     }
     
     protected BugBusterServices(final String host, final String key) {
         this.host = host;
         this.key = key;
     }
 
     public boolean ping() throws IOException {
         JSONObject jobj = (JSONObject) GET("/api/ping");
         return jobj.getBoolean("ok");
     }
 
     public JSONArray getProjects() throws IOException {
         JSONObject jobj = (JSONObject) GET("/api/projects");
         return jobj.getJSONArray("project");
     }
 
     public JSONArray getScenarios(final String project) throws IOException {
         // TODO implement page fetching
         JSONObject jobj = (JSONObject) GET("/api/project/" + project + "/scenarios");
         return jobj.getJSONArray("scenario");
     }
 
     public BugBusterSession newSession(final String project, final String scenario) throws IOException {
        JSONObject jobj = (JSONObject) POST("/api/session", "{ \"session\": { \"scenarioID\": " + scenario + "} }");
        return new BugBusterSession(this, jobj.getJSONObject("session").getInt("id"));
     }
 }

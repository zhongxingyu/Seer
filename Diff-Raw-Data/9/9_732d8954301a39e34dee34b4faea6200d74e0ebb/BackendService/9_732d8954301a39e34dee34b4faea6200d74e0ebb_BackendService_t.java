 package org.neo4j.training.backend;
 
 import org.slf4j.Logger;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Scanner;
 
 import static org.neo4j.helpers.collection.MapUtil.map;
 
 /**
 * @author mh
 * @since 30.05.12
 */
 public class BackendService {
 
     private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BackendService.class);
 
     public BackendService() {
     }
 
     private void log(String msg) {
         LOG.warn(msg);
     }
 
     public Map<String, Object> execute(Neo4jService service, String init, String query, String version) {
         if (version!=null) service.setVersion(version);
         boolean initial = init != null;
         if (dontInitialize(service) || init==null || init.equalsIgnoreCase("none")) init=null;
        if ("none".equalsIgnoreCase(query)) query=null;
         final Map<String, Object> data = map("init", init, "query", query,"version",service.getVersion());
         long start = System.currentTimeMillis(), time = start;
         try {
             time = trace("service", time);
             if (init != null && service.isMutatingQuery(init)) {
                 final CypherQueryExecutor.CypherResult result = service.initCypherQuery(init);
                 if (result.getQuery() != null) data.put("init", result.getQuery());
             }
             if (initial) {
                 service.setInitialized();
             }
             time = trace("graph", time);
             CypherQueryExecutor.CypherResult result = null;
             if (query!=null) {
                 result = service.cypherQuery(query);
                 data.put("json", result.getJson());
                 data.put("columns", result.getColumns());
                 data.put("stats", result.getQueryStatistics());
                 if (result.getQuery()!=null) data.put("query",result.getQuery());
             }
             time = trace("cypher", time);
             data.put("visualization", service.cypherQueryViz(result));
             trace("viz", time);
         } catch (Exception e) {
             e.printStackTrace();
             data.put("error", e.toString());
         }
         time = trace("all", start);
         data.put("time", time-start);
         return data;
     }
 
     private boolean dontInitialize(Neo4jService service) {
         return !service.doesOwnDatabase() || service.isInitialized();
     }
 
     protected long trace(String msg, long time) {
         long now = System.currentTimeMillis();
         log("## " + msg + " took: " + (now - time) + " ms.");
         return now;
     }
 
 
     public String shortenUrl(String uri) {
         try {
             final InputStream stream = (InputStream) new URL("http://tinyurl.com/api-create.php?url=" + URLEncoder.encode(uri, "UTF-8")).getContent();
             final String shortUrl = new Scanner(stream).useDelimiter("\\z").next();
             stream.close();
             return shortUrl;
         } catch (IOException ioe) {
             return null;
         }
     }
 
     public Map<String, Object> execute(Neo4jService service, GraphInfo info) {
         if (!info.hasRoot()) {
             service.deleteReferenceNode();
         }
         final Map<String, Object> result = this.execute(service, info.getInit(), info.getQuery(), info.getVersion());
         result.put("message",info.getMessage());
         return result;
     }
 
     protected String param(Map input, String param, String defaultValue) {
         if (input==null) return defaultValue;
         String data = (String) input.get(param);
         if (data == null || data.isEmpty()) {
             data = defaultValue;
         } else {
             log(param+": "+data);
         }
         return data;
     }
 
     public Map<String, Object> init(Neo4jService service, Map<String,Object> input) {
         input.put("init",param(input,"init",null));
         input.put("query",param(input,"query",null));
         return execute(service, GraphInfo.from(input));
     }
 }

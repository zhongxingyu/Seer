 package org.atlasapi.remotesite.space;
 
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import java.util.Iterator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  */
 public class TheSpaceItemsProcessor {
 
     private final Log logger = LogFactory.getLog(getClass());
     private final String url;
     private final SimpleHttpClient client;
     private final AdapterLog log;
     private final ContentResolver contentResolver;
     private final ContentWriter contentWriter;
 
     public TheSpaceItemsProcessor(String url, SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
         this.url = url;
         this.client = client;
         this.log = log;
         this.contentResolver = contentResolver;
         this.contentWriter = contentWriter;
     }
 
     public void process(JsonNode items) throws Exception {
         ObjectMapper mapper = new ObjectMapper();
         //
         TheSpaceItemProcessor processor = new TheSpaceItemProcessor(url, client, log, contentResolver, contentWriter);
         //
         Iterator<JsonNode> results = items.get("results").getElements();
         while (results.hasNext()) {
             JsonNode item = null;
             try {
                 item = results.next();
                 if (item.has("type") && item.has("pid")) {
                     String pid = item.get("pid").asText();
                     JsonNode node = client.get(new SimpleHttpRequest<JsonNode>(url + "/items/" + pid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
                    if (node.has("programme")) {
                        processor.process(node.get("programme"));
                    } else {
                        logger.warn("Unknown item type " + item.get("type").asText() + " with pid " + pid);
                    }
                 }
             } catch (Exception ex) {
                 if (item != null) {
                     String pid = item.has("pid") ? item.get("pid").asText() : "";
                     String key = item.has("key") ? Long.toString(item.get("key").asLong()) : "";
                     String title = item.has("title") ? item.get("title").asText() : "";
                     logger.error("Failed to ingest item with pid " + pid + " and key " + key + " and title " + title, ex);
                 }
                 log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(ex));
                 throw ex;
             }
         }
     }
 }

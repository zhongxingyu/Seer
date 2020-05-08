 package org.atlasapi.remotesite.space;
 
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import java.util.Iterator;
 import org.atlasapi.persistence.content.ContentGroupResolver;
 import org.atlasapi.persistence.content.ContentGroupWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  */
 public class TheSpacePlaylistsProcessor {
 
     private final String url;
     private final SimpleHttpClient client;
     private final AdapterLog log;
     private final ContentResolver contentResolver;
     private final ContentWriter contentWriter;
     private final ContentGroupResolver groupResolver;
     private final ContentGroupWriter groupWriter;
 
     public TheSpacePlaylistsProcessor(String url, SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter, ContentGroupResolver groupResolver, ContentGroupWriter groupWriter) {
         this.url = url;
         this.client = client;
         this.log = log;
         this.contentResolver = contentResolver;
         this.contentWriter = contentWriter;
         this.groupResolver = groupResolver;
         this.groupWriter = groupWriter;
     }
 
     public void process(JsonNode items) throws Exception {
         ObjectMapper mapper = new ObjectMapper();
         //
         TheSpacePlaylistProcessor processor = new TheSpacePlaylistProcessor(client, log, contentResolver, contentWriter, groupResolver, groupWriter);
         //
         Iterator<JsonNode> results = items.get("results").getElements();
         while (results.hasNext()) {
             JsonNode item = results.next();
            String pid = item.get("pid").asText();
            try {
                JsonNode node = client.get(new SimpleHttpRequest<JsonNode>(url + "/items/" + pid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
                processor.process(node.get("programme"));
            } catch (Exception ex) {
                log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withCause(ex));
             }
         }
     }
 }

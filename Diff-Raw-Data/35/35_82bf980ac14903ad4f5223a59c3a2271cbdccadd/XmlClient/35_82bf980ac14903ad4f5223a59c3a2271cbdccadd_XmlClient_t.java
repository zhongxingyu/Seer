 package org.atlasapi.remotesite.channel4;
 
 import static com.metabroadcast.common.http.SimpleHttpRequest.httpRequestFrom;
 
import java.io.InputStreamReader;
import java.util.Set;
 
 import nu.xom.Builder;
 import nu.xom.Document;
 
import org.atlasapi.http.AbstractHttpResponseTransformer;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 
import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class XmlClient implements RemoteSiteClient<Document> {
     
     private final SimpleHttpClient client;
     private final Builder builder;
     
     public XmlClient(SimpleHttpClient client) {
         this(client, new Builder());
     }
 
     public XmlClient(SimpleHttpClient client, Builder builder) {
         this.client = client;
         this.builder = builder;
     }
 
     @Override
     public Document get(String uri) throws Exception {
        return client.get(httpRequestFrom(uri, new AbstractHttpResponseTransformer<Document>() {

            @Override
            protected Document transform(InputStreamReader bodyReader) throws Exception {
                return builder.build(bodyReader);
            }
            
             @Override
            protected Set<Integer> acceptableResponseCodes() {
                return ImmutableSet.of(200);
             }
         }));
     }
 
 }

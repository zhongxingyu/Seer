 package org.atlasapi.remotesite.channel4;
 
 import static com.metabroadcast.common.http.SimpleHttpRequest.httpRequestFrom;
 
import java.io.InputStream;
 
 import nu.xom.Builder;
 import nu.xom.Document;
 
 import org.atlasapi.persistence.system.RemoteSiteClient;
 
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
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
        return client.get(httpRequestFrom(uri, new HttpResponseTransformer<Document>() {
             @Override
            public Document transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                return builder.build(body);
             }
         }));
     }
 
 }

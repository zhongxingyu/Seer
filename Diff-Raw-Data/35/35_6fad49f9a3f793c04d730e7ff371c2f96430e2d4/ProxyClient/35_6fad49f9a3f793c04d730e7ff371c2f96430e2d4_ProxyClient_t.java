 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package se.wallenius.domain.proxyclient;
 
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.JsonObjectParser;
 import com.google.api.client.json.jackson.JacksonFactory;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.List;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author fredrik
  */
 @Component
 public class ProxyClient {
    
    private static final String APIURL = "http://localhost:3000/harvest?term=";
 
     private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
     private static final JsonFactory JSON_FACTORY = new JacksonFactory();
 
     public List<Provider> requestForTerm(final String term) {
 
        // This way it should be thread safe.
         final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
             @Override
             public void initialize(HttpRequest request) {
                 request.setParser(new JsonObjectParser(JSON_FACTORY));
             }
         });
 
 
        try {         
            final GenericUrl url = new GenericUrl(APIURL + URLEncoder.encode(term, "UTF-8"));
         
             HttpRequest request = requestFactory.buildGetRequest(url);
             Response response = request.execute().parseAs(Response.class);
             
             return response.getProviders();
             
             
         } catch (IOException ex) {
            // No logger configured so just sending to stderr
            System.err.println(ex);
         } 
 
         return null;
     }
 }

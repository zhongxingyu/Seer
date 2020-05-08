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
import java.util.logging.Level;
import java.util.logging.Logger;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author fredrik
  */
 @Component
 public class ProxyClient {
 
     private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
     private static final JsonFactory JSON_FACTORY = new JacksonFactory();
 
     public List<Provider> requestForTerm(final String term) {
 
         final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
             @Override
             public void initialize(HttpRequest request) {
                 request.setParser(new JsonObjectParser(JSON_FACTORY));
             }
         });
 
 
        try {
         
            final GenericUrl url = new GenericUrl("http://localhost:3000/harvest?term=" + URLEncoder.encode(term, "UTF-8"));
         
             HttpRequest request = requestFactory.buildGetRequest(url);
             Response response = request.execute().parseAs(Response.class);
             
             return response.getProviders();
             
             
         } catch (IOException ex) {
            Logger.getLogger(ProxyClient.class.getName()).log(Level.SEVERE, null, ex);
         } 
 
         return null;
     }
 }

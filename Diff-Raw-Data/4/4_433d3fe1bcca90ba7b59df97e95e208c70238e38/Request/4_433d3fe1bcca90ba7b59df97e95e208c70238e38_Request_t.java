 package com.asquera.hmac;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 
 import org.apache.http.NameValuePair;
 
 public class Request {
     
     private final static String EOL = System.getProperty("line.separator");
     
     private final RequestParams options;
     private final URI uri;
     private final Query query;
     private final String urlWithoutQuery;
     
     public Request(final String url, final RequestParams options) throws URISyntaxException {
         if (url == null)
             throw new IllegalArgumentException();
         if (options == null)
             throw new IllegalArgumentException();
         
         this.uri = new URI(url);
         this.options = options;
         this.urlWithoutQuery = url.split("\\?")[0];
         
         query = new Query(uri);
     }
     
     public String canonicalRepresentation() {
         StringBuilder builder = new StringBuilder();
         builder.append(options.method()).append(EOL);
         builder.append("date:").append(dateAsString()).append(EOL);
         builder.append("nonce:").append(options.nonce()).append(EOL);
         for (NameValuePair pair : options.headers()) {
             builder.append(pair.getName().toLowerCase()).append(':').append(pair.getValue()).append(EOL);
         }
         builder.append(path());
         if (!query.query().isEmpty()) {
             builder.append('?').append(query.query());
         }
         return builder.toString();
     }
     
     public String path() {
         return uri.getPath().isEmpty() ? "/" : uri.getPath();
     }
     
     public Query query() {
         return this.query;
     }
     
     public boolean isQueryBased() {
         return options.isQueryBased();
     }
     
     public String url() {
        String url = new String(urlWithoutQuery);
         String query = this.query.encodedQuery();
         if (query != null && !query.isEmpty()) {
             url += "?" + query;
         }
         if (uri.getFragment() != null && !uri.getFragment().isEmpty()) {
             url += "#" + uri.getFragment();
         }
         return url;
     }
     
     public String dateAsString() {
         SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
         return (dateFormat.format(options.date()) + " GMT");
     }
 }

 package ecologylab.bigsemantics.httpclient;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.message.BasicNameValuePair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 
 /**
  * Utility methods for the HTTP client.
  * 
  * @author quyin
  */
 public class ModifiedHttpClientUtils
 {
 
   static Logger logger;
 
   static
   {
     logger = LoggerFactory.getLogger(ModifiedHttpClientUtils.class);
   }
 
   public static URIBuilder getUriBuilder(String url)
       throws UnsupportedEncodingException, URISyntaxException
   {
     URIBuilder ub = null;
     try
     {
       ub = new URIBuilder(url);
     }
     catch (URISyntaxException e)
     {
       ub = parseAndGetUriBuilder(url);
     }
     return ub;
   }
 
   static URIBuilder parseAndGetUriBuilder(String url)
       throws URISyntaxException, UnsupportedEncodingException
   {
     String path = null;
     String queries = null;
     int qpos = url.indexOf('?');
     if (qpos < 0)
     {
       path = url;
     }
     else
     {
       path = url.substring(0, qpos);
       queries = url.substring(qpos + 1);
     }
 
     // TODO more escaping
     path = path.replace(" ", "+");
 
     URIBuilder ub = new URIBuilder(path);
 
     if (queries != null)
     {
       String[] params = queries.split("&");
       for (int i = 0; i < params.length; ++i)
       {
         int epos = params[i].indexOf('=');
         String name = params[i].substring(0, epos);
         String value = params[i].substring(epos + 1);
        // URIBuilder.addParameter() handles URL encoding automatically, no need to do it here.
        // ub.addParameter(name, URLEncoder.encode(value, "UTF-8"));
        ub.addParameter(name, value);
       }
     }
     return ub;
   }
 
   public static HttpGet generateGetRequest(String url,
                                            Map<String, String> additionalParams)
   {
     try
     {
       URIBuilder ub = getUriBuilder(url);
 
       // handle special characters in path
       String path = ub.getPath();
 
       if (additionalParams != null)
       {
         for (String key : additionalParams.keySet())
         {
           String value = additionalParams.get(key);
           ub.addParameter(key, value);
         }
       }
       URI uri = ub.build();
 
       String hostName = uri.getHost();
       int port = uri.getPort();
       String host = hostName + ((port > 0) ? (":" + port) : "");
 
       HttpGet get = new HttpGet(uri);
       get.addHeader("HOST", host); // this header is required by HTTP 1.1
 
       return get;
     }
     catch (Exception e)
     {
       logger.error("Exception when generating a HttpGet object for " + url, e);
     }
     return null;
   }
 
   public static HttpPost generatePostRequest(String url, Map<String, String> formParams)
   {
     HttpPost post = new HttpPost(url);
     URI uri = post.getURI();
     String hostName = uri.getHost();
     int port = uri.getPort();
     String host = hostName + ((port > 0) ? (":" + port) : "");
     post.addHeader("HOST", host); // this header is required by HTTP 1.1
 
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     for (String key : formParams.keySet())
     {
       String value = formParams.get(key);
       if (value != null)
       {
         params.add(new BasicNameValuePair(key, value));
       }
     }
     HttpEntity entity = new UrlEncodedFormEntity(params, Charsets.UTF_8);
     post.setEntity(entity);
 
     return post;
   }
 
 }

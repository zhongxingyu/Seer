 package org.tsinghua.omedia.tool;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.http.HttpStatus;
 
 /**
  * 封装了HTTP的GET操作和POST操作
  * sample :
  * 
         try {
             String json = HttpExecutor.httpGet("http://166.111.137.72:10086/omedis/login.do")
                     .addParam("username", "xuhongfeng")
                     .addParam("password", "123456")
                     .exec();
         } catch (IOException e) {
             //处理异常
         }
  *
  * @author xuhongfeng
  */
 public class HttpExecutor {
     private static Logger logger = Logger.getLogger(HttpExecutor.class);
     
     private static enum METHOD_TYPE {POST, GET};
     
     private String url;
     private METHOD_TYPE type;
     private Map<String,String> params = new HashMap<String, String>();
     
     private HttpExecutor(){};
     
     public static HttpExecutor httpGet(String url) {
         HttpExecutor executor = new HttpExecutor();
         executor.url = url;
         executor.type = METHOD_TYPE.GET;
         return executor;
     }
     
     public static HttpExecutor httpPost(String url) {
         HttpExecutor executor = new HttpExecutor();
         executor.url = URLEncoder.encode(url);
         executor.type = METHOD_TYPE.POST;
         return executor;
     }
     
     public HttpExecutor addParam(String key, String value) {
         params.put(URLEncoder.encode(key), URLEncoder.encode(value));
         return this;
     }
     public HttpExecutor addParam(String key, long value) {
         return addParam(key, String.valueOf(value));
     }
     
     public String exec() throws IOException {
         switch(type) {
         case GET :
            return new String(innerHttpGet().getBytes("ISO-8859-1"), "utf8");
         case POST :
            return new String(innerHttpPost().getBytes("ISO-8859-1"), "utf8");
         default :  throw new IOException("unknow http type");
         }
     }
     
     private String innerHttpGet() throws IOException {
         StringBuilder sb = new StringBuilder();
         sb.append(url);
         Set<String> keys = params.keySet();
         boolean isFirst = true;
         for(String key:keys) {
             if(isFirst) {
                 sb.append("?");
                 isFirst = false;
             } else {
                 sb.append("&");
             }
             sb.append(key);
             sb.append("=");
             sb.append(params.get(key));
         }
         String uri = sb.toString();
         logger.info("http get,uri = " + uri);
         GetMethod method = new GetMethod(uri);
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler());
         try {
             HttpClient client = new HttpClient();
             int statusCode = client.executeMethod(method);
             if(statusCode != HttpStatus.SC_OK) {
                 throw new IOException("http status code = " + statusCode);
             }
             return method.getResponseBodyAsString();
         } finally {
             method.releaseConnection();
         }
     }
 
    private String innerHttpPost() throws IOException {
         PostMethod postMethod = new PostMethod(url);
         List<NameValuePair> datas = new ArrayList<NameValuePair>();
         Set<String> keys = params.keySet();
         for(String key:keys) {
             String value = params.get(key);
             datas.add(new NameValuePair(key,value));
         }
         postMethod.setRequestBody(datas.toArray(new NameValuePair[0]));
         HttpClient client = new HttpClient();
         try {
             int statusCode = client.executeMethod(postMethod);
             if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                     || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                 Header locationHeader = postMethod.getResponseHeader("location");
                 String location = null;
                 if (locationHeader != null) {
                     location = locationHeader.getValue();
                     logger.error("The page was redirected to:" + location);
                 } else {
                     logger.error("Location field value is null.");
                 }
             }
             return String.valueOf(statusCode);
         } finally {
             postMethod.releaseConnection();
         }
     }
 }

 /**
  * 
  */
 package fm.audiobox;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector;
 
 
 /**
  * @author fatshotty
  *
  */
 public class Report {
   
   private static Logger log = LoggerFactory.getLogger(Report.class);
   
   
   public static enum ENDPOINT {
     desktop,
     any
   }
   
   private ENDPOINT endpoint = ENDPOINT.any;
   private IConfiguration configuration;
   private String url;
   
   public Report(IConfiguration config, ENDPOINT endpoint) {
     this.endpoint = endpoint;
     this.configuration = config;
     
     String protocol = this.configuration.getProtocol(IConfiguration.Connectors.RAILS);
     String host = this.configuration.getHost(IConfiguration.Connectors.RAILS);
     String port = "" + this.configuration.getPort(IConfiguration.Connectors.RAILS);
    log.info("URL found: " + protocol + "://" + host + ":" + port);
     this.url = protocol + "://" + host + ":" + port + IConnector.URI_SEPARATOR + "webhooks/crashes/" + this.endpoint.toString();
     
   }
   
   
   
   public void report(Throwable t, String extra) {
     
     
     String message = t.getMessage();
     String klass = t.getClass().getName();
     String backtrace = "";
     
     {
       StringBuffer sb = new StringBuffer();
       for( StackTraceElement ste : t.getStackTrace() ) {
         sb.append(   ste.getClassName() + ":" + ste.getLineNumber() + "\r\n" ); 
       }
       backtrace = sb.toString();
     }
     
     
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     params.add( new BasicNameValuePair("message", message) );
     params.add( new BasicNameValuePair("klass", klass) );
     params.add( new BasicNameValuePair("backtrace", backtrace) );
     if ( extra != null ) {
       params.add( new BasicNameValuePair("extra", extra) );
     }
     
     try {
       this._report( new UrlEncodedFormEntity(params, HTTP.UTF_8) );
     } catch (UnsupportedEncodingException e) {
       log.error("No encoding supported", e);
     }
     
   }
   
   
   public void report( String message, String extra ) { 
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     params.add( new BasicNameValuePair("message", message) );
     if ( extra != null ) {
       params.add( new BasicNameValuePair("extra", extra) );
     }
     try {
       this._report( new UrlEncodedFormEntity(params, HTTP.UTF_8) );
     } catch (UnsupportedEncodingException e) {
       log.error("No encoding supported", e);
     }
   }
   
   
   
   private void _report(HttpEntity entity) {
     
     final HttpPost post = new HttpPost( this.url );
     
     post.setEntity(entity);
     
     final HttpClient client = new DefaultHttpClient();
     
     
     (new Thread() {
       public void run() {
         try {
           client.execute(post, new BasicHttpContext());
         } catch (ClientProtocolException e) {
           log.error("Protocol exception while sending report");
         } catch (IOException e) {
           log.error("Network error while sending report");
         }
       }
     }).start();
   }
   
 }

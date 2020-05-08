 package edu.rpi.cmt.timezones;
 
 import edu.rpi.cmt.timezones.Timezones.TaggedTimeZone;
 import edu.rpi.cmt.timezones.model.CapabilitiesType;
 import edu.rpi.cmt.timezones.model.TimezoneListType;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 import javax.servlet.http.HttpServletResponse;
 
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /** CLass to allow us to call the server
  */
 public class TzServer {
   private transient Logger log;
 
   protected boolean debug;
 
   private static String tzserverUri;
 
   private CapabilitiesType capabilities;
 
   private ObjectMapper om;
 
   private HttpClient client;
   private HttpGet getter;
   private int status;
   private HttpResponse response;
 
   /**
    * @param uri
    * @throws TimezonesException
    */
   public TzServer(final String uri) throws TimezonesException {
     debug = getLogger().isDebugEnabled();
     om = new ObjectMapper();
 
     /* Don't use dates in json - still issues with timezones ironically */
     //DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
 
     //om.setDateFormat(df);
 
     om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
     tzserverUri = discover(uri);
   }
 
   /**
    * @param id
    * @param etag
    * @return fetch timezone if etag is old
    * @throws TimezonesException
    */
   public TaggedTimeZone getTz(final String id,
                               final String etag) throws TimezonesException {
     try {
//    doCall("action=get&tzid=" +
//        URLEncoder.encode(id, "UTF-8"), etag);
        doCall("action=get&tzid=" + id, etag);
 
       int status = response.getStatusLine().getStatusCode();
 
       if (status == HttpServletResponse.SC_NO_CONTENT) {
         return new TaggedTimeZone(etag);
       }
 
       if (status != HttpServletResponse.SC_OK) {
         return null;
       }
 
       return new TaggedTimeZone(response.getFirstHeader("Etag").getValue(),
                                 EntityUtils.toString(response.getEntity()));
     } catch (TimezonesException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new TimezonesException(t);
     }
   }
 
   /* Not used - remove from server
   public String getNames() throws TimezonesException {
     return call("names");
   }*/
 
   /**
    * @param changedSince
    * @return List of timezone information
    * @throws TimezonesException
    */
   public TimezoneListType getList(final String changedSince) throws TimezonesException {
     String req = "action=list";
 
     if (changedSince != null) {
       req = req + "&changedsince=" + changedSince;
     }
 
     return getJson(req, TimezoneListType.class);
   }
 
   /**
    * @return Input stream of alias data
    * @throws TimezonesException
    */
   public InputStream getAliases() throws TimezonesException {
     return callForStream("aliases");
   }
 
   /**
    * @return capabilities obtained at discovery phase
    */
   public CapabilitiesType getCapabilities() {
     return capabilities;
   }
 
   /**
    * @throws TimezonesException
    */
   public void close() throws TimezonesException {
     try {
       if (response == null) {
         return;
       }
 
       HttpEntity ent = response.getEntity();
 
       if (ent != null) {
         InputStream is = ent.getContent();
         is.close();
       }
 
       getter = null;
       response = null;
 
       if (client != null) {
         client.getConnectionManager().shutdown();
         client = null;
       }
     } catch (Throwable t) {
       throw new TimezonesException(t);
     }
   }
 
   /* ====================================================================
    *                   protected methods
    * ==================================================================== */
 
   protected <T> T getJson(final String req,
                        final Class<T> valueType) throws TimezonesException {
     InputStream is = null;
     try {
       is = callForStream(req);
 
       if ((is == null) || (status != HttpServletResponse.SC_OK)) {
         return null;
       }
 
       synchronized (this) {
         return om.readValue(is, valueType);
       }
     } catch (TimezonesException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new TimezonesException(t);
     } finally {
       if (is != null) {
         try {
           is.close();
         } catch (Throwable t) {}
       }
     }
   }
 
   /**
    * @param req
    * @return input stream from response
    * @throws TimezonesException
    */
   protected InputStream callForStream(final String req) throws TimezonesException {
     try {
       doCall(req, null);
 
       if (status != HttpServletResponse.SC_OK) {
         return null;
       }
 
       HttpEntity ent = response.getEntity();
 
       return ent.getContent();
     } catch (TimezonesException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new TimezonesException(t);
     }
   }
 
   /* ====================================================================
    *                   private methods
    * ==================================================================== */
 
   /** See if we have a url for the service. If not discover the real one.
    *
    * <p>If the uri is parseable we won't even attempt the /.well-known approach.
    * It implies we have a scheme etc.
    *
    * <p>Otherwise we will assume it's a host attempt to discover it through
    * /.well-known
    *
    * @param url
    * @return discovered url
    * @throws CalFacadeException
    */
   private String discover(final String url) throws TimezonesException {
     /* For the moment we'll try to find it via .well-known. We may have to
      * use DNS SRV lookups
      */
 //    String domain = hi.getHostname();
 
   //  int lpos = domain.lastIndexOf(".");
     //int lpos2 = domain.lastIndexOf(".", lpos - 1);
 
 //    if (lpos2 > 0) {
   //    domain = domain.substring(lpos2 + 1);
     //}
 
     String realUrl;
 
     try {
       /* See if it's a real url */
       new URL(url);
       realUrl = url;
     } catch (Throwable t) {
       realUrl = "https://" + url + "/.well-known/timezone";
     }
 
     try {
       for (int redirects = 0; redirects < 10; redirects++) {
         client = new DefaultHttpClient();
 
         getter = new HttpGet(realUrl + "?action=capabilities");
         response = client.execute(getter);
         status = response.getStatusLine().getStatusCode();
 
         if ((status == HttpServletResponse.SC_MOVED_PERMANENTLY) ||
             (status == HttpServletResponse.SC_MOVED_TEMPORARILY) ||
             (status == HttpServletResponse.SC_TEMPORARY_REDIRECT)) {
           //boolean permanent = rcode == HttpServletResponse.SC_MOVED_PERMANENTLY;
 
           Header locationHeader = response.getFirstHeader("location");
           if (locationHeader != null) {
             if (debug) {
               debug("Got redirected to " + locationHeader.getValue() +
                     " from " + url);
             }
 
             String newLoc = locationHeader.getValue();
             int qpos = newLoc.indexOf("?");
 
             if (qpos < 0) {
               realUrl = newLoc;
             } else {
               realUrl = newLoc.substring(0, qpos);
             }
 
             close();
 
             // Try again
             continue;
           }
         }
 
         if (status != HttpServletResponse.SC_OK) {
             // The response is invalid and did not provide the new location for
             // the resource.  Report an error or possibly handle the response
             // like a 404 Not Found error.
           if (debug) {
             error("Got response " + status +
                   ", from " + realUrl);
           }
 
           throw new TimezonesException("Got response " + status +
                                        ", from " + realUrl);
         }
 
         /* Should have a capabilities record. */
         try {
           capabilities = om.readValue(response.getEntity().getContent(),
                                       CapabilitiesType.class);
         } catch (Throwable t) {
           // Bad data - we'll just go with the url for the moment?
           error(t);
         }
 
         return realUrl;
       }
 
       if (debug) {
         error("Too many redirects: Got response " + status +
               ", from " + realUrl);
       }
 
       throw new TimezonesException("Too many redirects on " + realUrl);
     } catch (TimezonesException tze) {
       throw tze;
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
 
       throw new TimezonesException(t);
     } finally {
       close();
     }
   }
 
   private void doCall(final String req,
                       final String etag) throws TimezonesException {
     try {
       if (tzserverUri == null) {
         throw new TimezonesException("No timezones server URI defined");
       }
 
       client = new DefaultHttpClient();
 
       getter = new HttpGet(tzserverUri + "?" + req);
 
       if (etag != null) {
         getter.addHeader(new BasicHeader("If-None-Match", etag));
       }
 
       response = client.execute(getter);
       status = response.getStatusLine().getStatusCode();
     } catch (TimezonesException cfe) {
       throw cfe;
     } catch (UnknownHostException uhe) {
       throw new TzUnknownHostException(tzserverUri);
     } catch (Throwable t) {
       throw new TimezonesException(t);
     }
   }
 
   /* Get a logger for messages
    */
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
   private void error(final String msg) {
     getLogger().error(msg);
   }
 
   private void debug(final String msg) {
     getLogger().debug(msg);
   }
 }

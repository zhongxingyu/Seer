 package com.google.code.geocoder;
 
 import com.google.code.geocoder.model.GeocodeResponse;
 import com.google.code.geocoder.model.GeocoderRequest;
 import com.google.code.geocoder.model.LatLng;
 import com.google.code.geocoder.model.LatLngBounds;
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * @author <a href="mailto:panchmp@gmail.com">Michael Panchenko</a>
  */
 public class Geocoder {
     private static final Log log = LogFactory.getLog(Geocoder.class);
 
     private static final String GEOCODE_REQUEST_SERVER_HTTP = "http://maps.googleapis.com";
     private static final String GEOCODE_REQUEST_SERVER_HTTPS = "https://maps.googleapis.com";
     private static final String GEOCODE_REQUEST_QUERY_BASIC = "/maps/api/geocode/json?sensor=false";
 
     private static HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
 
     private final String clientId;
     private final Mac mac;
 
     public Geocoder() {
         clientId = null;
         mac = null;
     }
 
     public Geocoder(final String clientId, final String clientKey) throws InvalidKeyException {
         if (StringUtils.isBlank(clientId)) {
             throw new IllegalArgumentException("ClientId is not defined");
         }
         if (StringUtils.isBlank(clientKey)) {
             throw new IllegalArgumentException("ClientKey is not defined");
         }
 
         this.clientId = clientId;
         this.mac = getMAC(clientKey);
     }
 
     @Deprecated
     public static synchronized void setConnectionManager(HttpConnectionManager manager) {
         httpClient = new HttpClient(manager);
     }
 
     public synchronized HttpClient getHttpClient() {
         return httpClient;
     }
 
     public GeocodeResponse geocode(final GeocoderRequest geocoderRequest) {
         try {
             final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
 
             final String urlString = getURL(geocoderRequest);
 
             final GetMethod getMethod = new GetMethod(urlString);
             try {
                 httpClient.executeMethod(getMethod);
                 final Reader reader = new InputStreamReader(getMethod.getResponseBodyAsStream(), getMethod.getResponseCharSet());
 
                 return gson.fromJson(reader, GeocodeResponse.class);
             } finally {
                 getMethod.releaseConnection();
             }
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             return null;
         }
     }
 
     protected String getURL(final GeocoderRequest geocoderRequest) throws UnsupportedEncodingException {
         if (log.isTraceEnabled()) {
             log.trace(geocoderRequest);
         }
         final StringBuilder url = getURLQuery(geocoderRequest);
 
         if (mac == null) {
             // add server name to URL
             url.insert(0, GEOCODE_REQUEST_SERVER_HTTP);
         } else {
             addClientIdAndSignURL(url);
 
             // add server name to URL
             url.insert(0, GEOCODE_REQUEST_SERVER_HTTPS);
         }
 
         if (log.isTraceEnabled()) {
             log.trace("FULL Request URL: " + url);
         }
         return url.toString();
     }
 
     protected StringBuilder getURLQuery(GeocoderRequest geocoderRequest) throws UnsupportedEncodingException {
         final String address = geocoderRequest.getAddress();
         final LatLngBounds bounds = geocoderRequest.getBounds();
         final String language = geocoderRequest.getLanguage();
         final String region = geocoderRequest.getRegion();
         final LatLng location = geocoderRequest.getLocation();
 
         final StringBuilder url = new StringBuilder(GEOCODE_REQUEST_QUERY_BASIC);
         if (StringUtils.isNotBlank(address)) {
             url.append("&address=").append(URLEncoder.encode(address, "UTF-8"));
         } else if (location != null) {
             url.append("&latlng=").append(URLEncoder.encode(location.toUrlValue(), "UTF-8"));
         } else {
             throw new IllegalArgumentException("Address or location must be defined");
         }
         if (StringUtils.isNotBlank(language)) {
             url.append("&language=").append(URLEncoder.encode(language, "UTF-8"));
         }
         if (StringUtils.isNotBlank(region)) {
             url.append("&region=").append(URLEncoder.encode(region, "UTF-8"));
         }
         if (bounds != null) {
             url.append("&bounds=").append(URLEncoder.encode(bounds.getSouthwest().toUrlValue() + "|" + bounds.getNortheast().toUrlValue(), "UTF-8"));
         }
         if (log.isTraceEnabled()) {
             log.trace("URL query: " + url);
         }
         return url;
     }
 
     protected void addClientIdAndSignURL(StringBuilder url) {
         url.append("&client=").append(clientId);
 
         if (log.isTraceEnabled()) {
             log.trace("URL query to Sign: " + url);
         }
 
         byte[] sigBytes;
         synchronized (mac) {
             mac.update(url.toString().getBytes());
             sigBytes = mac.doFinal();
         }
 
         String signature = new String(Base64.encodeBase64(sigBytes));
         signature = signature.replace('+', '-');
         signature = signature.replace('/', '_');
 
         if (log.isTraceEnabled()) {
             log.trace("Signature: [" + url + "] for URL query " + url);
         }
         url.append("&signature=").append(signature);
     }
 
     protected Mac getMAC(String clientKey) throws InvalidKeyException {
         try {
             byte[] key = Base64.decodeBase64(
                     clientKey.replace('-', '+')
                             .replace('_', '/').getBytes()
             );
 
 
             // Get an HMAC-SHA1 signing key from the raw key bytes
             final SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1");
 
             // Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1 key
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(sha1Key);
 
            return mac;
         } catch (NoSuchAlgorithmException e) {
             throw new IllegalStateException(e);
         }
     }
 }

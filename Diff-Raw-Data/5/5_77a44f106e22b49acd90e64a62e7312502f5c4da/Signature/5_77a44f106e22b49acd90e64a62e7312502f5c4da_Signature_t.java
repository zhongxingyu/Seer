 /**
  *
  * Copyright 2008-2009 Elements. All Rights Reserved.
  *
  * License version: CPAL 1.0
  *
  * The Original Code is mysimpledb.com code. Please visit mysimpledb.com to see how
  * you can contribute and improve this software.
  *
  * The contents of this file are licensed under the Common Public Attribution
  * License Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  *
  *    http://mysimpledb.com/license.
  *
  * The License is based on the Mozilla Public License Version 1.1.
  *
  * Sections 14 and 15 have been added to cover use of software over a computer
  * network and provide for attribution determined by Elements.
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  *
  * Elements is the Initial Developer and the Original Developer of the Original
  * Code.
  *
  * Based on commercial needs the contents of this file may be used under the
  * terms of the Elements End-User License Agreement (the Elements License), in
  * which case the provisions of the Elements License are applicable instead of
  * those above.
  *
  * You may wish to allow use of your version of this file under the terms of
  * the Elements License please visit http://mysimpledb.com/license for details.
  *
  */
 package ac.elements.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.InterruptedIOException;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.NoHttpResponseException;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 
 import org.apache.log4j.Logger;
 
 /**
  * This class defines common routines for generating authentication signatures
  * for AWS requests.
  */
 public class Signature {
 
     /** The Constant "Message Authentication Code" ALGORITHM. */
     private static final String ALGORITHM = "HmacSHA256";
 
     /** The Constant DEFAULT_ENCODING. */
     private static final String DEFAULT_ENCODING = "UTF-8";
 
     /** The http client. */
     public static HttpClient httpClient = configureHttpClient();
 
     /** The Constant MAX_CONNECTIONS. */
     private static final int MAX_CONNECTIONS = 5000;
 
     /** The Constant MAX_RETRY_ERROR. */
     private static final int MAX_RETRY_ERROR = 50;
 
     /** The Constant SERVICE_URL. */
     private static final String SERVICE_URL = "https://sdb.amazonaws.com/";
 
     /** The Constant SIGNATURE_VERSION. */
     private static final String SIGNATURE_VERSION = "2";
 
     /** The Constant USER_AGENT. */
     private static final String USER_AGENT = "";
 
     /** The Constant VERSION. */
     private static final String VERSION = "2007-11-07";
 
     /** The Constant log. */
     private final static Logger log = Logger.getLogger(Signature.class);
 
     /**
      * Add authentication related and version parameter and set request body
      * with all of the parameters.
      * 
      * @param parameters
      *            the parameters
      * @param id
      *            the id
      * @param key
      *            the key
      * 
      * @return the map< string, string>
      * @throws SignatureException
      */
     private static Map<String, String> addRequiredParametersToRequest(
             Map<String, String> parameters, String id, String key)
             throws SignatureException {
         parameters.put("Version", VERSION);
         parameters.put("SignatureVersion", SIGNATURE_VERSION);
         parameters.put("Timestamp", getTimeStamp());
         parameters.put("AWSAccessKeyId", id);
         parameters.put("Signature", signParameters(parameters, key));
         return (parameters);
     }
 
     /**
      * Calculate String to Sign for SignatureVersion 0.
      * 
      * @param parameters
      *            request parameters
      * 
      * @return String to Sign
      * 
      * @throws java.security.SignatureException
      */
     private static String calculateStringToSignV0(Map<String, String> parameters) {
         StringBuilder data = new StringBuilder();
         data.append(parameters.get("Action")).append(
                 parameters.get("Timestamp"));
         return data.toString();
     }
 
     /**
      * Calculate String to Sign for SignatureVersion 1.
      * 
      * @param parameters
      *            request parameters
      * 
      * @return String to Sign
      * 
      * @throws java.security.SignatureException
      */
     private static String calculateStringToSignV1(Map<String, String> parameters) {
         StringBuilder data = new StringBuilder();
         Map<String, String> sorted =
                 new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
         sorted.putAll(parameters);
         Iterator<Entry<String, String>> pairs = sorted.entrySet().iterator();
         while (pairs.hasNext()) {
             Map.Entry<String, String> pair = pairs.next();
             data.append(pair.getKey());
             data.append(pair.getValue());
         }
         return data.toString();
     }
 
     /**
      * Calculate String to Sign for SignatureVersion 2.
      * 
      * @param parameters
      *            request parameters
      * 
      * @return String to Sign
      * 
      * @throws java.security.SignatureException
      */
     private static String calculateStringToSignV2(Map<String, String> parameters) {
         StringBuilder data = new StringBuilder();
         data.append("POST");
         data.append("\n");
         URI endpoint = null;
         try {
             endpoint = new URI(SERVICE_URL);
         } catch (URISyntaxException ex) {
             // log.error("URI Syntax Exception", ex);
             throw new RuntimeException("URI Syntax Exception thrown "
                     + "while constructing string to sign", ex);
         }
         data.append(endpoint.getHost());
         data.append("\n");
         String uri = endpoint.getPath();
         if (uri == null || uri.length() == 0) {
             uri = "/";
         }
         data.append(urlEncode(uri, true));
         data.append("\n");
         Map<String, String> sorted = new TreeMap<String, String>();
         sorted.putAll(parameters);
         Iterator<Map.Entry<String, String>> pairs =
                 sorted.entrySet().iterator();
         while (pairs.hasNext()) {
             Map.Entry<String, String> pair = pairs.next();
             String key = pair.getKey();
             data.append(urlEncode(key, false));
             data.append("=");
             String value = pair.getValue();
             data.append(urlEncode(value, false));
             if (pairs.hasNext()) {
                 data.append("&");
             }
         }
 
         return data.toString();
     }
 
     /**
      * Configure HttpClient with set of defaults as well as configuration from
      * AmazonEC2Config instance.
      * 
      * @return the http client
      */
     private static HttpClient configureHttpClient() {
 
         /* Set http client parameters */
         HttpClientParams httpClientParams = new HttpClientParams();
         httpClientParams.setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
         httpClientParams.setParameter(HttpMethodParams.RETRY_HANDLER,
                 new HttpMethodRetryHandler() {
 
                     public boolean retryMethod(HttpMethod method,
                             IOException exception, int executionCount) {
                         if (executionCount > MAX_RETRY_ERROR) {
                             log.warn("Maximum Number of Retry attempts "
                                     + "reached, will not retry");
                             return false;
                         }
                         log.warn("Retrying request. Attempt " + executionCount);
                         if (exception instanceof NoHttpResponseException) {
                             log.warn("Retrying on NoHttpResponseException");
                             return true;
                         }
                         if (exception instanceof InterruptedIOException) {
                             log.warn(
                                     "Will not retry on InterruptedIOException",
                                     exception);
                             return false;
                         }
                         if (exception instanceof UnknownHostException) {
                             log.warn("Will not retry on UnknownHostException",
                                     exception);
                             return false;
                         }
                         if (!method.isRequestSent()) {
                             log.warn("Retrying on failed sent request");
                             return true;
                         }
                         return false;
                     }
                 });
 
         /* Set host configuration */
         HostConfiguration hostConfiguration = new HostConfiguration();
 
         /* Set connection manager parameters */
         HttpConnectionManagerParams connectionManagerParams =
                 new HttpConnectionManagerParams();
         connectionManagerParams.setConnectionTimeout(50000);
         connectionManagerParams.setSoTimeout(50000);
         connectionManagerParams.setStaleCheckingEnabled(true);
         connectionManagerParams.setTcpNoDelay(true);
         connectionManagerParams.setMaxTotalConnections(MAX_CONNECTIONS);
         connectionManagerParams.setMaxConnectionsPerHost(hostConfiguration,
                 MAX_CONNECTIONS);
 
         /* Set connection manager */
         MultiThreadedHttpConnectionManager connectionManager =
                 new MultiThreadedHttpConnectionManager();
         connectionManager.setParams(connectionManagerParams);
 
         /* Set http client */
         httpClient = new HttpClient(httpClientParams, connectionManager);
 
         /* Set proxy if configured */
         // if (config.isSetProxyHost() && config.isSetProxyPort()) {
         // log.info("Configuring Proxy. Proxy Host: " + config.getProxyHost() +
         // "Proxy Port: " + config.getProxyPort() );
         // hostConfiguration.setProxy(config.getProxyHost(),
         // config.getProxyPort());
         // if (config.isSetProxyUsername() && config.isSetProxyPassword()) {
         // httpClient.getState().setProxyCredentials (new AuthScope(
         // config.getProxyHost(),
         // config.getProxyPort()),
         // new UsernamePasswordCredentials(
         // config.getProxyUsername(),
         // config.getProxyPassword()));
         //        
         // }
         // }
         httpClient.setHostConfiguration(hostConfiguration);
         return httpClient;
     }
 
     /**
      * Gets the parameters.
      * 
      * @param keyValuePairs
      *            the key value pairs
      * @param id
      *            the id
      * @param key
      *            the key
      * 
      * @return the parameters
      * @throws SignatureException
      */
     private static Map<String, String> getParameters(
             Map<String, String> keyValuePairs, String id, String key)
             throws SignatureException {
 
         Map<String, String> parameters =
                 new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
 
         parameters.putAll(keyValuePairs);
 
         addRequiredParametersToRequest(parameters, id, key);
 
         return parameters;
     }
 
     /**
      * Read stream into string.
      * 
      * @param input
      *            stream to read
      * 
      * @return the respons body as string
      * 
      * @throws IOException
      *             Signals that an I/O exception has occurred.
      */
     private static String getResponsBodyAsString(InputStream input)
             throws IOException {
         String responsBodyString = null;
         try {
             Reader reader = new InputStreamReader(input, DEFAULT_ENCODING);
             StringBuilder b = new StringBuilder();
             char[] c = new char[1024];
             int len;
             while (0 < (len = reader.read(c))) {
                 b.append(c, 0, len);
             }
             responsBodyString = b.toString();
         } finally {
             input.close();
         }
         return responsBodyString;
     }
 
     /**
      * The time stamp used in the AWS request. It must be in a specific format
      * (eg. 2007-01-31T23:59:59Z)
      * 
      * <p>
      * It must be a date time object, with the complete date plus hours,
      * minutes, and seconds, for more information, go to
      * {@link "http://www.w3.org/TR/xmlschema-2/#dateTime"}).
      * 
      * <p>
      * For example: 2007-01-31T23:59:59Z. Although it is not required, we
      * recommend you provide the time stamp in the Coordinated Universal Time
      * (Greenwich Mean Time) time zone.
      * 
      * <p>
      * The request automatically expires 15 minutes after the time stamp (in
      * other words, AWS does not process a request if the request time stamp is
      * more than 15 minutes earlier than the current time on AWS servers). Make
      * sure your server's time is set correctly.
      * 
      * @return the correctly formatted time stamp
      */
     private static String getTimeStamp() {
         final String dateFormat = "yyyy-MM-dd\'T\'HH:mm:ss\'Z\'";
         final SimpleDateFormat format =
                 new SimpleDateFormat(dateFormat, Locale.US);
         format.setTimeZone(TimeZone.getTimeZone("UTC"));
         return format.format(new Date());
     }
 
     /**
      * Gets the XML response as a string.
      * 
      * @param keyValues
      *            the keyValues pairs
      * @param id
      *            the id
      * @param key
      *            the key
      * 
      * @return the XML response as a string
      */
     public static String getXMLResponse(final Map<String, String> keyValues,
             String id, String key) {
 
         Map<String, String> parameters;
         try {
             parameters = Signature.getParameters(keyValues, id, key);
         } catch (SignatureException se) {
             se.printStackTrace();
             throw new RuntimeException("CredentialsNotFound: Please make sure "
                     + "that the file "
                     + "'aws.properties' is located in the classpath "
                     + "(usually "
                     + "$TOMCAT_HOME/webapps/mysimpledb/WEB-INF/classes"
                     + ") of the java virtual machine. " + "This file should "
                     + "define your AWSAccessKeyId and SecretAccessKey.");
         }
         int status = -1;
         String response = null;
 
         PostMethod method = new PostMethod(SERVICE_URL);
 
         for (Entry<String, String> entry : parameters.entrySet()) {
             method.addParameter(entry.getKey(), entry.getValue());
         }
 
         String en = null;
         try {
 
             /* Set content type and encoding */
             method.addRequestHeader("Content-Type",
                     "application/x-www-form-urlencoded; charset="
                             + DEFAULT_ENCODING.toLowerCase());
             boolean shouldRetry = true;
             int retries = 0;
             do {
                 // log.debug("Sending Request to host: " + SERVICE_URL);
 
                 try {
 
                     /* Submit request */
                     status = Signature.httpClient.executeMethod(method);
 
                     /* Consume response stream */
                     response =
                             getResponsBodyAsString(method
                                     .getResponseBodyAsStream());
 
                     if (status == HttpStatus.SC_OK) {
                         shouldRetry = false;
                         // log.debug("Received Response. Status: " + status + ".
                         // " +
                         // "Response Body: " + responseBodyString);
 
                     } else {
                         // log.debug("Received Response. Status: " + status + ".
                         // " +
                         // "Response Body: " + responseBodyString);
 
                         if ((status == HttpStatus.SC_INTERNAL_SERVER_ERROR || status == HttpStatus.SC_SERVICE_UNAVAILABLE)
                                 && pauseIfRetryNeeded(++retries)) {
                             shouldRetry = true;
                         } else {
                             shouldRetry = false;
                         }
                     }
                 } catch (ConnectException ce) {
                     shouldRetry = false;
                     en =
 
                             "ConnectException: This webapp is not able to "
                                     + "connect, a likely cause is your "
                                     + "firewall settings, please double check.";
 
                 } catch (UnknownHostException uhe) {
                     shouldRetry = false;
                     en =
 
                             "UnknownHostException: This webapp is not able to "
                                     + "connect, to sdb.amazonaws.com "
                                     + "please check connection and your "
                                     + "firewall settings.";
 
                 } catch (IOException ioe) {
 
                     ++retries;
                     log.error("Caught IOException: ", ioe);
                     ioe.printStackTrace();
 
                 } catch (Exception e) {
                     ++retries;
                     log.error("Caught Exception: ", e);
                     e.printStackTrace();
                 } finally {
                     method.releaseConnection();
                 }
                 // if (shouldRetry && retries == 1) {
                 // concurrentRetries++;
                 // log.warn("concurrentRetries: " + concurrentRetries);
                 // }
                 // if (concurrentRetries >= 1) {
                 // StatementAsync.throttleDown();
                 // } else {
                 // StatementAsync.throttleUp();
                 // }
             } while (shouldRetry);
             // if (retries > 0 && concurrentRetries > 0)
             // concurrentRetries--;
         } catch (Exception e) {
             log.error("Caught Exception: ", e);
             e.printStackTrace();
         }
         if (en != null) {
             throw new RuntimeException(en);
         }
         if (response.indexOf("<Code>InvalidClientTokenId</Code>") != -1) {
 
             throw new RuntimeException(
                     "InvalidClientTokenId: The AWS Access Key Id you provided "
                             + "does not exist in Amazons records. The file "
                             + "aws.properties should define your "
                             + "AWSAccessKeyId and SecretAccessKey.");
 
         } else if (response.indexOf("<Code>SignatureDoesNotMatch</Code>") != -1) {
 
             throw new RuntimeException(
                     "SignatureDoesNotMatch: The request signature we "
                             + "calculated does not match the signature you "
                             + "provided. Check your Secret Access Key "
                             + "and signing method. Consult the service "
                             + "documentation for details.");
         } else if (response.indexOf("<Code>AuthFailure</Code>") != -1) {
 
             throw new RuntimeException(
                     "AuthFailure: AWS was not able to validate the provided "
                             + "access credentials. This usually means you do "
                             + "not have a simple db account. "
                             + "Go to <a href=\""
                             + "http://aws.amazon.com/simpledb/\">Amazon's "
                             + "Simple DB</a> page and create an account, if "
                             + "you do not have one at this moment.");
        } else if (response.indexOf("<Errors>") != -1) {

            log.error("Found keyword error in response:\n" + response);
            log.error("Key value pairs sent:\n" + keyValues);

         }
         return response;
 
     }
 
     /**
      * Exponential sleep on failed request. Sleeps and returns true if retry
      * needed
      * 
      * @param retries
      *            current retry
      * 
      * @return true, if pause if retry needed
      * 
      * @throws InterruptedException
      *             the interrupted exception if the thread fails to sleep
      */
     private static boolean pauseIfRetryNeeded(int retries)
             throws InterruptedException {
         if (retries <= MAX_RETRY_ERROR) {
             long delay = (long) (Math.pow(2, retries) * 100L);
             log.warn("Retriable error detected, will retry in " + delay
                     + "ms, attempt numer: " + retries);
             Thread.sleep(delay);
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Computes RFC 2104-compliant HMAC signature.
      * 
      * @param data
      *            the data
      * @param key
      *            the key
      * @param algorithm
      *            the algorithm
      * 
      * @return the string
      * @throws SignatureException
      */
     private static String sign(String data, String key, String algorithm)
             throws SignatureException {
         if (key == null)
             throw new SignatureException("Encoding key is null.");
         byte[] signature = null;
         try {
             Mac mac = Mac.getInstance(algorithm);
             mac.init(new SecretKeySpec(key.getBytes(), algorithm));
             signature =
                     Base64.encodeBase64(mac.doFinal(data
                             .getBytes(DEFAULT_ENCODING)));
         } catch (Exception e) {
             log.error("Failed to generate signature: " + e.getMessage(), e);
         }
 
         return new String(signature);
     }
 
     /**
      * <p>
      * Computes RFC 2104-compliant HMAC signature for request parameters
      * Implements AWS Signature, as per following spec:
      * 
      * <p>
      * If Signature Version is 0, it signs concatenated Action and Timestamp
      * 
      * <p>
      * If Signature Version is 1, it performs the following:
      * 
      * <p>
      * Sorts all parameters (including SignatureVersion and excluding Signature,
      * the value of which is being created), ignoring case.
      * 
      * <p>
      * Iterate over the sorted list and append the parameter name (in original
      * case) and then its value. It will not URL-encode the parameter values
      * before constructing this string. There are no separators.
      * 
      * <p>
      * If Signature Version is 2, string to sign is based on following:
      * <ol>
      * <li>The HTTP Request Method followed by an ASCII newline (%0A)
      * <li>The HTTP Host header in the form of lowercase host, followed by an
      * ASCII newline.
      * <li>The URL encoded HTTP absolute path component of the URI (up to but
      * not including the query string parameters); if this is empty use a
      * forward '/'. This parameter is followed by an ASCII newline.
      * <li>The concatenation of all query string components (names and values)
      * as UTF-8 characters which are URL encoded as per RFC 3986 (hex characters
      * MUST be uppercase), sorted using lexicographic byte ordering. Parameter
      * names are separated from their values by the '=' character (ASCII
      * character 61), even if the value is empty. Pairs of parameter and values
      * are separated by the '&' character (ASCII code 38).
      * </ol>
      * 
      * @param parameters
      *            the parameters
      * @param key
      *            the key
      * 
      * @return the string
      * @throws SignatureException
      */
     private static String signParameters(Map<String, String> parameters,
             String key) throws SignatureException {
 
         String signatureVersion = parameters.get("SignatureVersion");
         String algorithm = "HmacSHA1";
         String stringToSign = null;
         if ("0".equals(signatureVersion)) {
             stringToSign = calculateStringToSignV0(parameters);
         } else if ("1".equals(signatureVersion)) {
             stringToSign = calculateStringToSignV1(parameters);
         } else if ("2".equals(signatureVersion)) {
             algorithm = ALGORITHM;
             parameters.put("SignatureMethod", algorithm);
             stringToSign = calculateStringToSignV2(parameters);
         } else {
             throw new RuntimeException("Invalid Signature Version specified");
         }
         log.debug("Calculated string to sign: " + stringToSign);
         // System.out.println(stringToSign);
         return sign(stringToSign, key, algorithm);
     }
 
     /**
      * Url encode.
      * 
      * @param value
      *            the value
      * @param path
      *            the path
      * 
      * @return the string
      */
     private static String urlEncode(String value, boolean path) {
         if (value == null)
             return null;
         String encoded = null;
         try {
             encoded =
                     URLEncoder.encode(value, DEFAULT_ENCODING).replace("+",
                             "%20").replace("*", "%2A").replace("%7E", "~");
             if (path) {
                 encoded = encoded.replace("%2F", "/");
             }
         } catch (UnsupportedEncodingException ex) {
             log.error("Unsupported Encoding Exception", ex);
             throw new RuntimeException(ex);
         }
         return encoded;
     }
 
 }

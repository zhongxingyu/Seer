 /*
  *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  *  SOFTWARE.
  */
 
 package com.dmdirc.addons.parser_twitter.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.parsers.ParserConfigurationException;
 import oauth.signpost.OAuth;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.basic.DefaultOAuthConsumer;
 import oauth.signpost.basic.DefaultOAuthProvider;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.exception.OAuthNotAuthorizedException;
 import oauth.signpost.signature.SignatureMethod;
 
 import java.io.ByteArrayInputStream;
 
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.NoRouteToHostException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * Implementation of the twitter API for DMDirc.
  * 
  * @author shane
  */
 public class TwitterAPI {
     /** OAuth Consumer */
     private OAuthConsumer consumer;
 
     /** OAuth Provider */
     private OAuthProvider provider;
 
     /** Have we signed anything yet? */
     private boolean hasSigned = false;
 
     /** Login Username for this twitter API. */
     private final String myLoginUsername;
 
     /**
      * Display Username for this twitter API.
      * (The same as myLoginUsername unless autoAt is enabled)
      */
     private final String myDisplayUsername;
 
     /** Password for this twitter API if oauth isn't available. */
     private String myPassword;
 
     /** Cache of users. */
     final Map<String, TwitterUser> userCache = new HashMap<String, TwitterUser>();
 
     /** Cache of user IDs to screen names. */
     final Map<Long, String> userIDMap = new HashMap<Long, String>();
 
     /** Cache of statuses. */
     final Map<Long, TwitterStatus> statusCache = new HashMap<Long, TwitterStatus>();
 
     /** API Allowed status */
     private APIAllowed allowed = APIAllowed.UNKNOWN;
     
     /** How many API calls have we made since the last reset? */
     private int usedCalls = 0;
     
     /** API reset time. */
     private long resetTime = 0;
 
     /** Twitter Token */
     private String token = "";
 
     /** Twitter Token Secret */
     private String tokenSecret = "";
 
     /** Last input to the API. */
     private String apiInput = "";
 
     /** Last output from the API. */
     private String apiOutput = "";
 
     /** List of TwitterErrorHandlers */
     private final List<TwitterErrorHandler> errorHandlers = new LinkedList<TwitterErrorHandler>();
 
     /** List of TwitterRawHandlers */
     private final List<TwitterRawHandler> rawHandlers = new LinkedList<TwitterRawHandler>();
 
     /** What address should API calls be made to? */
     private final String apiAddress;
 
     /** Should we use OAuth? */
     private boolean useOAuth = true;
 
     /** Should we use ssl? */
     private boolean useSSL = false;
     
     /** Should we enable debug? */
     private boolean debug = false;
 
     /** Should we use the versioned API? */
     private boolean useAPIVersion = false;
 
     /** 
      * What version of the API should we try to use?
      * If useAPIVersion is false this is irrelevent, otherwise method calls will
      * try to use this version of the API, otherwise falling back to any lower
      * version they know how to use.
      */
     private int apiVersion = 1;
 
     /** Source to try and use for non-oauth status updates. */
     private String mySource = "web";
 
     /** Should we prepend an @ to all user names? */
     private boolean autoAt;
 
     /**
      * Create a non-OAuth using TwitterAPI.
      *
      * @param username Username to use.
      * @param password Password to use.
      * @param apiAddress 
      * @param useAPIVersion
      * @param apiVersion
      */
     public TwitterAPI(final String username, final String password, final String apiAddress, final boolean useAPIVersion, final int apiVersion, final boolean autoAt) {
         this(username, password, apiAddress, "", "", "", "", "", useAPIVersion, apiVersion, autoAt);
         useOAuth = false;
     }
 
     /**
      * Create a new Twitter API for the given user.
      *
      * @param username Username to use.
      * @param password Password to use. (if using OAuth then "" is sufficient, if specified then it will be used as a fallback if OAuth is unavailable)
      * @param apiAddress Address to send API Requests to. (No protocol, eg "api.twitter.com")
      * @param oauthAddress Path to OAuth. (default: apiAddress+"/oauth");
      * @param consumerKey If using OAuth, the consumerKey to use.
      * @param consumerSecret If using OAuth, the consumerSecret to use.
      * @param token If using OAuth, the token to use.
      * @param tokenSecret If using OAuth, the tokenSecret to use.
      * @param useAPIVersion Should we use the versioned api?
      * @param apiVersion What version of the api should we use? (0 == Unversioned, -1 == maximum version known)
      * @param autoAt Should '@' signs be prepended to user names?
      */
     public TwitterAPI(final String username, final String password, final String apiAddress, final String oauthAddress, final String consumerKey, final String consumerSecret, final String token, final String tokenSecret, final boolean useAPIVersion, final int apiVersion, final boolean autoAt) {
         this.myLoginUsername = username;
         this.apiAddress = apiAddress.replaceAll("/+$", "");
         this.myPassword = password;
         this.autoAt = autoAt;
         this.myDisplayUsername = (autoAt ? "@" : "") + myLoginUsername;
 
         if (this.apiAddress.isEmpty() && myLoginUsername.isEmpty()) { return; }
 
         if (!consumerKey.isEmpty() && !consumerSecret.isEmpty()) {
             consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret, SignatureMethod.HMAC_SHA1);
             final String thisOauthAddress = ((useSSL) ? "https" : "http") + "://" + ((oauthAddress == null || oauthAddress.isEmpty()) ? apiAddress.replaceAll("/+$", "") + "/oauth" : oauthAddress.replaceAll("/+$", ""));
             
             provider = new DefaultOAuthProvider(consumer, thisOauthAddress+"/request_token", thisOauthAddress+"/access_token", thisOauthAddress+"/authorize");
 
             this.token = token;
             this.tokenSecret = tokenSecret;
 
             try {
                 useOAuth = !(getOAuthURL().isEmpty());
             } catch (TwitterRuntimeException tre) {
                 useOAuth = false;
             }
         } else {
             useOAuth = false;
         }
 
         this.useAPIVersion = useAPIVersion && (apiVersion != 0);
         if (apiVersion > 0) {
             this.apiVersion = apiVersion;
         }
 
         // if we are allowed, isAllowed will automatically call getUser() to
         // update the cache with our own user object.
         if (!isAllowed(true)) {
             // If not, add a temporary one.
             // It will be replaced as soon as the allowed status is changed to
             // true by isAlowed().
             updateUser(new TwitterUser(this, myLoginUsername));
         }
     }
 
     /**
      * Get the source used in status updates when not using OAuth.
      * 
      * @return The source used in status updates when not using OAuth.
      */
     public String getSource() {
         return mySource;
     }
 
     /**
      * Set the source used in status updates when using OAuth.
      *
      * @param source The source to use in status updates when not using OAuth.
      */
     public void setSource(final String source) {
         this.mySource = source;
     }
 
     /**
      * Get the address to send API Calls to, assuming version 1 of the API.
      *
      * @param apiCall call we want to make
      * @return URL To use!
      */
     private String getURL(final String apiCall) {
         return getURL(apiCall, 1);
     }
 
     /**
      * Get the address to send API Calls to.
      *
      * @param apiCall call we want to make
      * @param version API Version to use. (or 0 to make an unversioned call)
      * @return URL To use!
      */
     private String getURL(final String apiCall, final int version) {
         if (!useAPIVersion || version == 0) {
             return (useSSL ? "https" : "http") + "://" + apiAddress + "/" + apiCall + ".xml";
         } else {
             return (useSSL ? "https" : "http") + "://" + apiAddress + "/" + version + "/" + apiCall + ".xml";
         }
     }
 
     /**
      * Add a new error handler.
      *
      * @param handler handler to add.
      */
     public void addErrorHandler(final TwitterErrorHandler handler) {
         synchronized (errorHandlers) {
             errorHandlers.add(handler);
         }
     }
     
     /**
      * Remove an error handler.
      *
      * @param handler handler to remove.
      */
     public void delErrorHandler(final TwitterErrorHandler handler) {
         synchronized (errorHandlers) {
             errorHandlers.remove(handler);
         }
     }
     
     /**
      * Clear error handlers.
      */
     public void clearErrorHandlers() {
         synchronized (errorHandlers) {
             errorHandlers.clear();
         }
     }
 
     /**
      * Handle an error from twitter.
      *
      * @param t The throwable that caused the error.
      * @param source Source of exception.
      * @param twitterInput The input to the API that caused this error.
      * @param twitterOutput The output from the API that caused this error.
      */
     private void handleError(final Throwable t, final String source, final String twitterInput, final String twitterOutput) {
         handleError(t, source, twitterInput, twitterOutput, "");
     }
 
     /**
      * Handle an error from twitter.
      *
      * @param t The throwable that caused the error.
      * @param source Source of exception.
      * @param twitterInput The input to the API that caused this error.
      * @param twitterOutput The output from the API that caused this error.
      * @param message If more information should be relayed to the user, it comes here
      */
     private void handleError(final Throwable t, final String source, final String twitterInput, final String twitterOutput, final String message) {
         synchronized (errorHandlers) {
             for (TwitterErrorHandler eh : errorHandlers) {
                 eh.handleTwitterError(this, t, source, twitterInput, twitterOutput, message);
             }
         }
     }
 
     /**
      * Add a new raw handler.
      *
      * @param handler handler to add.
      */
     public void addRawHandler(final TwitterRawHandler handler) {
         synchronized (rawHandlers) {
             rawHandlers.add(handler);
         }
     }
 
     /**
      * Remove a raw handler.
      *
      * @param handler handler to remove.
      */
     public void delRawHandler(final TwitterRawHandler handler) {
         synchronized (rawHandlers) {
             rawHandlers.remove(handler);
         }
     }
 
     /**
      * Clear raw handlers.
      */
     public void clearRawHandlers() {
         synchronized (rawHandlers) {
             rawHandlers.clear();
         }
     }
 
     /**
      * Handle input from twitter.
      *
      * @param raw The raw input from twitter.
      */
     private void handleRawInput(final String raw) {
         synchronized (rawHandlers) {
             for (TwitterRawHandler rh : rawHandlers) {
                 rh.handleRawTwitterInput(this, raw);
             }
         }
     }
 
     /**
      * Handle output to twitter.
      *
      * @param raw The raw output to twitter
      */
     private void handleRawOutput(final String raw) {
         synchronized (rawHandlers) {
             for (TwitterRawHandler rh : rawHandlers) {
                 rh.handleRawTwitterOutput(this, raw);
             }
         }
     }
 
     /**
      * Are we using autoAt?
      *
      * @return are we using autoAt?
      */
     public boolean autoAt() {
         return autoAt;
     }
 
     /**
      * Set if we should use autoAt or not.
      *
      * @param autoAt Should we use autoAt?
      */
     /* public void setAutoAt(final boolean autoAt) {
         this.autoAt = autoAt;
     } */
 
     /**
      * Is debugging enabled?
      *
      * @return Is debugging enabled?
      */
     public boolean isDebug() {
         return debug;
     }
 
     /**
      * Set if debugging is enabled.
      *
      * @param debug if debugging is enabled.
      */
     public void setDebug(final boolean debug) {
         this.debug = debug;
     }
 
     /** 
      * Are we using oauth?
      * 
      * @return are we usin oauth?
      */
     public boolean useOAuth() {
         return useOAuth;
     }
 
     /**
      * Set if we should use oauth or not.
      *
      * @param useOAuth Should we use oauth?
      */
     public void setUseOAuth(final boolean useOAuth) {
         this.useOAuth = useOAuth;
     }
 
     /**
      * Are we using the versioned API?
      *
      * @return are we using the versioned API?
      */
     public boolean useAPIVersion() {
         return useAPIVersion;
     }
 
     /**
      * Set if we should use the versioned API or not.
      *
      * This should be called after setApiVersion
      *
      * @param useAPIVersion Should we use the versioned API or not?
      */
     public void setUseAPIVersion(final boolean useAPIVersion) {
         this.useAPIVersion = useAPIVersion;
         setApiVersion(apiVersion()); // Reset the API Version
     }
 
     /**
      * What version of the API are we using?
      *
      * @return The version of the API we are using (0 for none)
      */
     public int apiVersion() {
         return apiVersion;
     }
 
     /**
      * Set the api version to try and use.
      * Methods will use the given api if support has been added, or the latest
      * version below that they know.
      * If this method is not called, then the latest version is assumed
      *
      * Changing this causes us to force a check to isAllowed() if apiversioning
      * is enabled
      *
      * @param apiVersion What API Version to use.
      */
     public void setApiVersion(final int apiVersion) {
         this.apiVersion = apiVersion;
 
         if (useAPIVersion) {
             // if we are allowed, isAllowed will automatically call getUser() to
             // update the cache with our own user object.
             if (!isAllowed(true)) {
                 // If not, add a temporary one.
                 // It will be replaced as soon as the allowed status is changed to
                 // true by isAlowed().
                 updateUser(new TwitterUser(this, myLoginUsername));
             }
         }
     }
 
     /**
      * Set the account password.
      *
      * @param password new account password
      */
     public void setPassword(final String password) {
         this.myPassword = password;
     }
 
     /**
      * Gets the twitter access token if known.
      *
      * @return Access Token
      */
     public String getToken() {
         return token;
     }
 
     /**
      * Gets the twitter access token secret if known.
      *
      * @return Access Token Secret
      */
     public String getTokenSecret() {
         return tokenSecret;
     }
 
     /**
      * Attempt to sign the given connection.
      * If not using OAuth, we just authenticate the connection instead.
      *
      * @param connection Connection to sign.
      */
     private void signURL(final HttpURLConnection connection) {
         if (useOAuth) {
             if (!hasSigned) {
                 if (getToken().isEmpty() || getTokenSecret().isEmpty()) {
                     return;
                 }
                 consumer.setTokenWithSecret(getToken(), getTokenSecret());
                 hasSigned = true;
             }
             try {
                 consumer.sign(connection);
             } catch (OAuthMessageSignerException ex) {
                 handleError(ex, "(1) signURL", apiInput, apiOutput, "Unable to sign URL, are we authorised to use this account?");
             } catch (OAuthExpectationFailedException ex) {
                 handleError(ex, "(2) signURL", apiInput, apiOutput, "Unable to sign URL, are we authorised to use this account?");
             }
         } else {
           // final String userpassword = myUsername + ":" + myPassword;
           // sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
           // String encodedAuthorization = enc.encode(userpassword.getBytes());
 
           final String encodedAuthorization = b64encode(myLoginUsername + ":" + myPassword);
           connection.setRequestProperty("Authorization", "Basic "+ encodedAuthorization);
         }
     }
 
     /**
      * Encode a string to base64,
      * Based on code from http://www.wikihow.com/Encode-a-String-to-Base64-With-Java
      *
      * @param string String to encode
      * @return Encoded output
      */
     private String b64encode(final String string) {
         final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
         final StringBuilder encoded = new StringBuilder();
         byte[] stringArray;
         try {
             stringArray = string.getBytes("UTF-8");
         } catch (UnsupportedEncodingException ex) {
             stringArray = string.getBytes();
         }
 
         // determine how many padding bytes to add to the output
         final int paddingCount = (3 - (stringArray.length % 3)) % 3;
 
         // add any necessary padding to the input
         final byte[] padded = new byte[stringArray.length + paddingCount];
         System.arraycopy(stringArray, 0, padded, 0, stringArray.length);
 
         // process 3 bytes at a time, churning out 4 output bytes
         for (int i = 0; i < padded.length; i += 3) {
             int j = (padded[i] << 16) + (padded[i + 1] << 8) + padded[i + 2];
             encoded.append(base64code.charAt((j >> 18) & 0x3f));
             encoded.append(base64code.charAt((j >> 12) & 0x3f));
             encoded.append(base64code.charAt((j >> 6) & 0x3f));
             encoded.append(base64code.charAt(j & 0x3f));
         }
 
         // replace encoded padding nulls with "="
         return encoded.substring(0, encoded.length() - paddingCount) + "==".substring(0, paddingCount);
     }
 
     /**
      * Parse the given string to a long without throwing an exception.
      * If an exception was raised, default will be used.
      *
      * @param string String to parse.
      * @param fallback Default on failure.
      * @return Long from string
      */
     public static Long parseLong(final String string, final long fallback) {
         try {
             return Long.parseLong(string);
         } catch (NumberFormatException nfe) {
             return fallback;
         }
     }
 
     /**
      * Parse the given string as a twitter time to a long.
      * If parsing fails, then the fallback will be used.
      *
      * @param string String to parse.
      * @param fallback Default on failure.
      * @return Long from string
      */
     public static Long timeStringToLong(final String string, final long fallback) {
         try {
             return (new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy").parse(string)).getTime();
         } catch (ParseException ex) {
             return fallback;
         }
     }
 
     /**
      * Parse the given string to a boolean, returns true for "true", "yes" or "1"
      *
      * @param string String to parse.
      * @return Boolean from string
      */
     public static boolean parseBoolean(final String string) {
         return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("1");
     }
 
     /**
      * Get the contents of the given node from an element.
      * If node doesn't exist, fallbcak will be returned.
      *
      * @param element Element to look at
      * @param string Node to get content from.
      * @param fallback Default on failure.
      * @return Long from string
      */
     public static String getElementContents(final Element element, final String string, final String fallback) {
         if (element != null) {
             final NodeList nl = element.getElementsByTagName(string);
             if (nl != null && nl.getLength() > 0) {
                 return nl.item(0).getTextContent();
             }
         }
         return fallback;
     }
 
     /**
      * Get the XML for the given address.
      *
      * @param address Address to get XML for.
      * @return Document object for this xml.
      */
     private XMLResponse getXML(final String address) {
         try {
             final URL url = new URL(address);
             return getXML((HttpURLConnection) url.openConnection());
         } catch (MalformedURLException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) getXML: "+address, apiInput, apiOutput);
             }
         } catch (IOException ex) {
             if (isDebug()) {
                 handleError(ex, "* (2) getXML: "+address, apiInput, apiOutput);
             }
         }
 
         return new XMLResponse(null, null);
     }
 
     /**
      * Get the XML for the given address, using a POST request.
      *
      * @param address Address to get XML for.
      * @return Document object for this xml.
      */
     private XMLResponse postXML(final String address) {
         return postXML(address, "");
     }
 
     /**
      * Get the XML for the given address, using a POST request.
      *
      * @param address Address to get XML for.
      * @param params Params to post.
      * @return Document object for this xml.
      */
     private XMLResponse postXML(final String address, final String params) {
         try {
             final URL url = new URL(address + (params.isEmpty() ? "" : "?" + params));
             return postXML((HttpURLConnection) url.openConnection());
         } catch (MalformedURLException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) postXML: "+address+" | "+params, apiInput, apiOutput);
             }
         } catch (IOException ex) {
             if (isDebug()) {
                 handleError(ex, "* (2) postXML: "+address+" | "+params, apiInput, apiOutput);
             }
         }
 
         return new XMLResponse(null, null);
     }
 
     /**
      * Get the XML for the given UNSIGNED HttpURLConnection object, using a
      * POST request.
      *
      * @param request HttpURLConnection to get XML for.
      * @return Document object for this xml.
      */
     private XMLResponse postXML(final HttpURLConnection request) {
         try {
             request.setRequestMethod("POST");
             request.setRequestProperty("Content-Length", "0");
             request.setUseCaches(false);
         } catch (ProtocolException ex) {
             if (isDebug()) {
                 handleError(ex, "* (3) postXML: "+request.getURL(), apiInput, apiOutput);
             }
         }
         return getXML(request);
     }
 
     /**
      * Check if a connection can be made to the api.
      *
      * MalformedURLException or NoRouteToHostException cause this to return
      * false, otherwise true is sent.
      *
      * If any other IOException is thrown this is consided a success and true is
      * returned.
      *
      * If an IOException is thrown this will be sent via handleError when
      * debugigng is enabled.
      * 
      * @return True if connecting to the api works, else false.
      */
     public boolean checkConnection() {
         try {
             final URL url = new URL(getURL("account/verify_credentials"));
             final HttpURLConnection request = (HttpURLConnection) url.openConnection();
             signURL(request);
             request.connect();
             return true;
         } catch (MalformedURLException ex) {
             return false;
         } catch (NoRouteToHostException ex) {
             return false;
         } catch (IOException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) checkConnection", "", "");
             }
             return true;
         }
     }
 
     /**
      * Get the XML for the given UNSIGNED HttpURLConnection object.
      *
      * @param request HttpURLConnection to get XML for.
      * @return Document object for this xml.
      */
     private XMLResponse getXML(final HttpURLConnection request) {
         if (request.getURL().getHost().isEmpty()) {
             return new XMLResponse(request, null);
         }
         if (resetTime > 0 && resetTime <= System.currentTimeMillis()) {
             usedCalls = 0;
             resetTime = System.currentTimeMillis() + 3600000;
         }
         usedCalls++;
         
         apiInput = request.getURL().toString();
         handleRawOutput(apiInput);
         BufferedReader in = null;
         try {
             signURL(request);

            request.setConnectTimeout(5000);
            request.setReadTimeout(5000);
             request.connect();
             in = new BufferedReader(new InputStreamReader(request.getInputStream()));
         } catch (IOException ex) {
             if (isDebug()) {
                 handleError(ex, "* (4) getXML: "+request.getURL(), apiInput, apiOutput);
             }
             if (request.getErrorStream() != null) {
                 in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
             } else {
                 return new XMLResponse(request, null);
             }
         }
 
         final StringBuilder xml = new StringBuilder();
         boolean incomplete = false;
         String line;
 
         synchronized (this) {
             try {
                 do {
                     line = in.readLine();
                     if (line != null) {
                         xml.append("\n");
                         xml.append(line);
                     }
                 } while (line != null);
 
                 apiOutput = xml.toString().trim();
             } catch (IOException ex) {
                 apiOutput = xml.toString().trim() + "\n ... Incomplete!";
                 incomplete = true;
                 if (isDebug()) {
                     handleError(ex, "* (5) getXML", apiInput, apiOutput);
                 }
             } finally {
                 try { in.close(); } catch (IOException ex) { }
             }
         }
 
         handleRawInput(xml.toString() + (incomplete ? "\n ... Incomplete!" : ""));
 
         int responseCode;
 
         try {
             responseCode = request.getResponseCode();
             if (responseCode != 200) {
                 if (isDebug()) {
                     handleError(null, "* (6) getXML", apiInput, apiOutput, "("+request.getResponseCode()+") "+request.getResponseMessage());
                 } else if (responseCode >= 500 && responseCode < 600) {
                     handleError(null, "(6) getXML", apiInput, apiOutput, "("+request.getResponseCode()+") "+request.getResponseMessage());
                 }
             }
         } catch (IOException ioe) {
             responseCode = 0;
             if (isDebug()) {
                 handleError(ioe, "* (7) getXML", apiInput, apiOutput, "Unable to get response code.");
             }
         }
 
         try {
             final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             
             final Document doc = db.parse(new ByteArrayInputStream(apiOutput.getBytes()));
 
             final XMLResponse response = new XMLResponse(request, doc);
             if (response.isError()) {
                 handleError(null, "(8) getXML", apiInput, apiOutput, response.getError());
             }
 
             return response;
         } catch (SAXException ex) {
             if (isDebug()) {
                 handleError(ex, "* (9) getXML", apiInput, apiOutput);
             }
         } catch (ParserConfigurationException ex) {
             if (isDebug()) {
                 handleError(ex, "* (10) getXML", apiInput, apiOutput);
             }
         } catch (IOException ex) {
             if (isDebug()) {
                 handleError(ex, "* (11) getXML", apiInput, apiOutput);
             }
         }
 
         return new XMLResponse(request, null);
     }
 
     /**
      * Remove the cache of the status object for the given status.
      *
      * @param status
      */
     protected void uncacheStatus(final TwitterStatus status) {
         if (status == null) { return; }
         synchronized (statusCache) {
             statusCache.remove(status.getID());
         }
     }
 
     /**
      * Remove the cache of the user object for the given user.
      *
      * @param user
      */
     protected void uncacheUser(final TwitterUser user) {
         if (user == null) { return; }
         synchronized (userCache) {
             userCache.remove(user.getScreenName().toLowerCase());
             userIDMap.remove(user.getID());
         }
     }
 
     /**
      * Update the user object for the given user, if the user ins't know already
      * this will add them to the cache.
      *
      * @param user
      */
     protected void updateUser(final TwitterUser user) {
         updateUser(user.getScreenName(), user);
     }
 
     /**
      * Update the user object for the given user, if the user ins't know already
      * this will add them to the cache.
      *
      * If the username doesn't match the screen name for the user, then the
      * cached object for "username" will be deleted and a new cached object
      * will be added from the users screen name.
      *
      * @param username
      * @param user
      */
     protected void updateUser(final String username, final TwitterUser user) {
         if (user == null) { return; }
         synchronized (userCache) {
             if (!username.equalsIgnoreCase(user.getScreenName())) {
                 userCache.remove(username.toLowerCase());
             }
 
             userCache.put(user.getScreenName().toLowerCase(), user);
             userIDMap.put(user.getID(), user.getScreenName().toLowerCase());
         }
 
         // TODO: TwitterStatus and TwitterMessage objects should be informed
         // about updates.
     }
 
     /**
      * Get a user object for the given user.
      *
      * @param username
      * @return User object for the requested user.
      */
     public TwitterUser getUser(final String username) {
         return getUser(username, false);
     }
 
     /**
      * Get a cached user object for the given user.
      *
      * @param username
      * @return User object for the requested user.
      */
     public TwitterUser getCachedUser(final String username) {
         synchronized (userCache) {
             if (userCache.containsKey(username.toLowerCase())) {
                 return userCache.get(username.toLowerCase());
             } else {
                 return null;
             }
         }
     }
 
     /**
      * Get a user object for the given user.
      *
      * @param username
      * @param force Force an update of the cache?
      * @return User object for the requested user.
      */
     public TwitterUser getUser(final String username, final boolean force) {
         TwitterUser user = getCachedUser(username);
         if (user == null || force) {
             if (username.equalsIgnoreCase(myDisplayUsername) && !isAllowed()) {
                  user = new TwitterUser(this, myLoginUsername, -1, "", true);
             } else {
                 final XMLResponse doc = getXML(getURL("users/show")+"?screen_name="+username);
 
                 if (doc.isGood()) {
                     user = new TwitterUser(this, doc.getDocumentElement());
                 } else {
                     user = null;
                 }
             }
 
             if (user != null) { updateUser(user); }
         }
 
         return user;
     }
 
     /**
      * Update the status object for the given status, if the status isn't known
      * already this will add them to the cache.
      *
      * @param status
      */
     protected void updateStatus(final TwitterStatus status) {
         if (status == null) { return; }
         synchronized (statusCache) {
             statusCache.put(status.getID(), status);
         }
     }
 
     /**
      * Get a status object for the given id.
      *
      * @param id
      * @return Status object for the requested id.
      */
     public TwitterStatus getStatus(final long id) {
         return getStatus(id, false);
     }
 
     /**
      * Get a cached status object for the given id.
      *
      * @param id
      * @return status object for the requested id.
      */
     public TwitterStatus getCachedStatus(final long id) {
         synchronized (statusCache) {
             if (statusCache.containsKey(id)) {
                 return statusCache.get(id);
             } else {
                 return null;
             }
         }
     }
 
     /**
      * Get a status object for the given id.
      *
      * @param id
      * @param force Force an update of the cache?
      * @return status object for the requested id.
      */
     public TwitterStatus getStatus(final long id, final boolean force) {
         TwitterStatus status = getCachedStatus(id);
         if (status == null || force) {
             final XMLResponse doc = getXML(getURL("statuses/show/"+id));
 
             if (doc.isGood()) {
                 status = new TwitterStatus(this, doc.getDocumentElement());
             } else {
                 status = null;
             }
 
             updateStatus(status);
         }
 
         return status;
     }
 
     /**
      * Prune the status cache of statuses older than the given time.
      * This should be done periodically depending on how many statuses you see.
      *
      * @param time
      */
     public void pruneStatusCache(final long time) {
         synchronized (statusCache) {
             final Map<Long, TwitterStatus> current = new HashMap<Long, TwitterStatus>(statusCache);
 
             for (Long item : current.keySet()) {
                 if (current.get(item).getTime() < time) {
                     statusCache.remove(item);
                 }
             }
         }
     }
 
     /**
      * Send a direct message to the given user
      *
      * @param target Target user.
      * @param message Message to send.
      * @return true if the message was sent, else false.
      */
     public boolean newDirectMessage(final String target, final String message) {
         try {
             final XMLResponse doc = postXML(getURL("direct_messages/new"), "screen_name=" + target + "&text=" + URLEncoder.encode(message, "utf-8"));
 
             if (doc.isGood()) {
                 new TwitterMessage(this, doc.getDocumentElement());
                 return true;
             }
         } catch (UnsupportedEncodingException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) newDirectMessage: "+target+" | "+message, apiInput, apiOutput);
             }
         }
         
         return false;
     }
 
     /**
      * What port are we using?
      *
      * @return port for api connections.
      */
     public int getPort() {
         return 80;
     }
 
     /**
      * Get a List of TwitterStatus Objects for this user.
      *
      * @param lastUserTimelineId
      * @return a List of TwitterStatus Objects for this user.
      */
     public List<TwitterStatus> getUserTimeline(final long lastUserTimelineId) {
         final List<TwitterStatus> result = new ArrayList<TwitterStatus>();
 
         final XMLResponse doc = getXML(getURL("statuses/user_timeline")+"?since_id="+lastUserTimelineId+"&count=20");
 
         if (doc.isGood()) {
             final NodeList nodes = doc.getElementsByTagName("status");
             for (int i = 0; i < nodes.getLength(); i++) {
                 result.add(new TwitterStatus(this, nodes.item(i)));
             }
         }
 
         return result;
     }
 
     /**
      * Get a list of TwitterUsers who we are following.
      *
      * @return A list of TwitterUsers who we are following.
      */
     public List<TwitterUser> getFriends() {
         final List<TwitterUser> result = new ArrayList<TwitterUser>();
 
         long cursor = -1;
         int count = 0;
         while (cursor != 0) {
             final XMLResponse doc = getXML(getURL("statuses/friends") + "?cursor=" + cursor);
 
             if (doc.isGood()) {
                 final NodeList nodes = doc.getElementsByTagName("user");
                 for (int i = 0; i < nodes.getLength(); i++) {
                     final TwitterUser user = new TwitterUser(this, nodes.item(i));
                     updateUser(user);
                     result.add(user);
                 }
 
                 cursor = parseLong(getElementContents(doc.getDocumentElement(), "next_cursor", "0"), 0);
                 count = 0;
             } else if (count++ > 1) {
                 break; // If we get an error twice in a row, abort.
             }
         }
 
         return result;
     }
 
     /**
      * Get a list of people we have blocked..
      *
      * @return A list of people we have blocked.
      */
     public List<TwitterUser> getBlocked() {
         final List<TwitterUser> result = new ArrayList<TwitterUser>();
 
 
         final XMLResponse doc = getXML(getURL("blocks/blocking"));
 
         if (doc.isGood()) {
             final NodeList nodes = doc.getElementsByTagName("user");
             for (int i = 0; i < nodes.getLength(); i++) {
                 final TwitterUser user = new TwitterUser(this, nodes.item(i));
                 uncacheUser(user);
                 result.add(user);
             }
         }
 
         return result;
     }
 
     /**
      * Get a list of IDs of people who are following us.
      *
      * @return A list of IDs of people who are following us.
      */
     public List<Long> getFollowers() {
         final List<Long> result = new ArrayList<Long>();
 
         long cursor = -1;
         int count = 0;
         while (cursor != 0) {
             final XMLResponse doc = getXML(getURL("followers/ids") + "?cursor=" + cursor);
             if (doc.isGood()) {
                 final NodeList nodes = doc.getElementsByTagName("id");
                 for (int i = 0; i < nodes.getLength(); i++) {
                     final Element element = (Element)nodes.item(i);
                     final Long id = parseLong(element.getTextContent(), -1);
                     result.add(id);
 
                     if (userIDMap.containsKey(id)) {
                         final TwitterUser user = getCachedUser(userIDMap.get(id));
                         user.setFollowingUs(true);
                     }
                 }
 
                 cursor = parseLong(getElementContents(doc.getDocumentElement(), "next_cursor", "0"), 0);
                 count = 0;
             } else if (count++ > 1) {
                 break; // If we get an error twice in a row, abort.
             }
         }
 
         return result;
     }
 
     /**
      * Get the messages sent for us that are later than the given ID.
      *
      * @param lastReplyId Last reply we know of.
      * @return The messages sent for us that are later than the given ID.
      */
     public List<TwitterStatus> getReplies(final long lastReplyId) {
         return getReplies(lastReplyId, 20);
     }
 
     /**
      * Get the messages sent for us that are later than the given ID.
      *
      * @param lastReplyId Last reply we know of.
      * @param count How many replies to get
      * @return The messages sent for us that are later than the given ID.
      */
     public List<TwitterStatus> getReplies(final long lastReplyId, final int count) {
         final List<TwitterStatus> result = new ArrayList<TwitterStatus>();
 
         final XMLResponse doc = getXML(getURL("statuses/mentions")+"?since_id="+lastReplyId+"&count="+count);
         if (doc.isGood()) {
             final NodeList nodes = doc.getElementsByTagName("status");
 
             for (int i = 0; i < nodes.getLength(); i++) {
                 result.add(new TwitterStatus(this, nodes.item(i)));
             }
         }
 
         return result;
     }
 
     /**
      * Get the messages sent by friends that are later than the given ID.
      *
      * @param lastTimelineId Last reply we know of.
      * @return The messages sent by friends that are later than the given ID.
      */
     public List<TwitterStatus> getFriendsTimeline(final long lastTimelineId) {
         return getFriendsTimeline(lastTimelineId, 20);
     }
     
     /**
      * Get the messages sent by friends that are later than the given ID.
      *
      * @param lastTimelineId Last reply we know of.
      * @param count How many statuses to get
      * @return The messages sent by friends that are later than the given ID.
      */
     public List<TwitterStatus> getFriendsTimeline(final long lastTimelineId, final int count) {
         final List<TwitterStatus> result = new ArrayList<TwitterStatus>();
 
         final XMLResponse doc = getXML(getURL("statuses/home_timeline")+"?since_id="+lastTimelineId+"&count="+count);
         if (doc.isGood()) {
             final NodeList nodes = doc.getElementsByTagName("status");
             for (int i = 0; i < nodes.getLength(); i++) {
                 result.add(new TwitterStatus(this, nodes.item(i)));
             }
         }
 
         return result;
     }
 
     /**
      * Get the direct messages sent to us that are later than the given ID.
      *
      * @param lastDirectMessageId Last reply we know of.
      * @return The direct messages sent to us that are later than the given ID.
      */
     public List<TwitterMessage> getDirectMessages(final long lastDirectMessageId) {
         return getDirectMessages(lastDirectMessageId, 20);
     }
 
     /**
      * Get the direct messages sent to us that are later than the given ID.
      *
      * @param lastDirectMessageId Last reply we know of.
      * @param count How many messages to request at a time
      * @return The direct messages sent to us that are later than the given ID.
      */
     public List<TwitterMessage> getDirectMessages(final long lastDirectMessageId, final int count) {
         final List<TwitterMessage> result = new ArrayList<TwitterMessage>();
 
         final XMLResponse doc = getXML(getURL("direct_messages")+"?since_id="+lastDirectMessageId+"&count="+count);
         if (doc.isGood()) {
             final NodeList nodes = doc.getElementsByTagName("direct_message");
             for (int i = 0; i < nodes.getLength(); i++) {
                 result.add(new TwitterMessage(this, nodes.item(i)));
             }
         }
 
         return result;
     }
 
     /**
      * Get the direct messages sent by us that are later than the given ID.
      *
      * @param lastDirectMessageId Last reply we know of.
      * @return The direct messages sent by us that are later than the given ID.
      */
     public List<TwitterMessage> getSentDirectMessages(final long lastDirectMessageId) {
         return getSentDirectMessages(lastDirectMessageId, 20);
     }
 
     /**
      * Get the direct messages sent by us that are later than the given ID.
      *
      * @param lastDirectMessageId Last reply we know of.
      * @param count How many messages to request at a time
      * @return The direct messages sent by us that are later than the given ID.
      */
     public List<TwitterMessage> getSentDirectMessages(final long lastDirectMessageId, final int count) {
         final List<TwitterMessage> result = new ArrayList<TwitterMessage>();
 
         final XMLResponse doc = getXML(getURL("direct_messages/sent")+"?since_id="+lastDirectMessageId+"&count="+count);
         if (doc.isGood()) {
             final NodeList nodes = doc.getElementsByTagName("direct_message");
             for (int i = 0; i < nodes.getLength(); i++) {
                 result.add(new TwitterMessage(this, nodes.item(i)));
             }
         }
 
         return result;
     }
 
     /**
      * Set your status to the given TwitterStatus
      *
      * @param status Status to send
      * @param id id to reply to or -1
      * @return True if status was updated ok.
      */
     public boolean setStatus(final String status, final Long id) {
         try {
             final StringBuilder params = new StringBuilder("status=");
             params.append(URLEncoder.encode(status, "utf-8"));
             if (id >= 0) {
                 params.append("&in_reply_to_status_id="+Long.toString(id));
             }
             if (!useOAuth) {
                 params.append("&source="+URLEncoder.encode(mySource, "utf-8"));
             }
 
             final XMLResponse doc = postXML(getURL("statuses/update"), params.toString());
             if (doc.getResponseCode() == 200) {
                 if (doc.isGood()) {
                     new TwitterStatus(this, doc.getDocumentElement());
                 }
                 return true;
             }
         } catch (UnsupportedEncodingException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) setStatus: "+status+" | "+id, apiInput, apiOutput);
             }
         } catch (IOException ex) {
             if (isDebug()) {
                 handleError(ex, "* (2) setStatus: "+status+" | "+id, apiInput, apiOutput);
             }
         }
 
         return false;
     }
 
     /**
      * Retweet the given status
      *
      * @param status Status to retweet
      * @return True if status was retweeted ok.
      */
     public boolean retweetStatus(final TwitterStatus status) {
         final XMLResponse doc = postXML(getURL("statuses/retweet/"+status.getID()));
         if (doc.getResponseCode() == 200) {
             if (doc.isGood()) {
                 new TwitterStatus(this, doc.getDocumentElement());
             }
             return true;
         }
 
         return false;
     }
 
     /**
      * Delete the given status
      *
      * @param status Status to delete
      * @return True if status was deleted ok.
      */
     public boolean deleteStatus(final TwitterStatus status) {
         final XMLResponse doc = postXML(getURL("statuses/destroy/"+status.getID()));
         if (doc.getResponseCode() == 200) {
             if (doc.isGood()) {
                 final TwitterStatus deletedStatus = new TwitterStatus(this, doc.getDocumentElement());
                 uncacheStatus(deletedStatus);
 
                 // Get our previous tweet.
                 getUser(myDisplayUsername, true);
             }
             return true;
         }
 
         return false;
     }
 
     /**
      * Get the number of api calls remaining.
      *
      * @return Long[4] containting API calls limit information.
      *          - 0 is remaning calls
      *          - 1 is total calls per hour
      *          - 2 is the time (in milliseconds) that the limit is reset.
      *          - 3 is the estimated number of api calls we have made since
      *            the last reset.
      */
     public Long[] getRemainingApiCalls() {
         final XMLResponse doc = getXML(getURL("account/rate_limit_status"));
         // The call we just made doesn't count, so remove it from the count.
         usedCalls--;
         if (doc.isGood()) {
             final Element element = doc.getDocumentElement();
 
             final long remaining = parseLong(getElementContents(element, "remaining-hits", ""), -1);
             final long total = parseLong(getElementContents(element, "hourly-limit", ""), -1);
             
             // laconica does this wrong :( so support both.
             final String resetTimeString = getElementContents(element, "reset-time-in-seconds", getElementContents(element, "reset_time_in_seconds", "0"));
             resetTime = 1000 * parseLong(resetTimeString, -1);
 
             return new Long[]{remaining, total, resetTime, (long)usedCalls};
         } else {
             return new Long[]{0L, 0L, System.currentTimeMillis(), (long)usedCalls};
         }
     }
 
     /**
      * How many calls have we used since reset?
      *
      * @return calls used since reset.
      */
     public int getUsedCalls() {
         return usedCalls;
     }
 
     /**
      * Get the URL the user must visit in order to authorize DMDirc.
      * 
      * @return the URL the user must visit in order to authorize DMDirc.
      * @throws TwitterRuntimeException  if there is a problem with OAuth.*
      */
     public String getOAuthURL() throws TwitterRuntimeException {
         try {
             return provider.retrieveRequestToken(OAuth.OUT_OF_BAND);
         } catch (OAuthMessageSignerException ex) {
             if (myPassword.isEmpty()) {
                 if (isDebug()) {
                     handleError(ex, "* (1) getOAuthURL", apiInput, apiOutput);
                 }
                 throw new TwitterRuntimeException(ex.getMessage(), ex);
             }
         } catch (OAuthNotAuthorizedException ex) {
             if (myPassword.isEmpty()) {
                 if (isDebug()) {
                     handleError(ex, "* (2) getOAuthURL", apiInput, apiOutput);
                 }
                 throw new TwitterRuntimeException(ex.getMessage(), ex);
             }
         } catch (OAuthExpectationFailedException ex) {
             if (myPassword.isEmpty()) {
                 if (isDebug()) {
                     handleError(ex, "* (3) getOAuthURL", apiInput, apiOutput);
                 }
                 throw new TwitterRuntimeException(ex.getMessage(), ex);
             }
         } catch (OAuthCommunicationException ex) {
             if (myPassword.isEmpty()) {
                 if (isDebug()) {
                     handleError(ex, "* (4) getOAuthURL", apiInput, apiOutput);
                 }
                 throw new TwitterRuntimeException(ex.getMessage(), ex);
             }
         }
 
         useOAuth = false;
         return "";
     }
 
     /**
      * Get the URL the user must visit in order to authorize DMDirc.
      *
      * @param pin Pin for OAuth
      * @throws TwitterException  if there is a problem with OAuth.
      */
     public void setAccessPin(final String pin) throws TwitterException {
         if (!useOAuth) { return; }
         try {
             provider.retrieveAccessToken(pin);
             token = consumer.getToken();
             tokenSecret = consumer.getTokenSecret();
         } catch (OAuthMessageSignerException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) setAccessPin: "+pin, apiInput, apiOutput);
             }
             throw new TwitterException(ex.getMessage(), ex);
         } catch (OAuthNotAuthorizedException ex) {
             if (isDebug()) {
                 handleError(ex, "* (2) setAccessPin: "+pin, apiInput, apiOutput);
             }
             throw new TwitterException(ex.getMessage(), ex);
         } catch (OAuthExpectationFailedException ex) {
             if (isDebug()) {
                 handleError(ex, "* (3) setAccessPin: "+pin, apiInput, apiOutput);
             }
             throw new TwitterException(ex.getMessage(), ex);
         } catch (OAuthCommunicationException ex) {
             if (isDebug()) {
                 handleError(ex, "* (4) setAccessPin: "+pin, apiInput, apiOutput);
             }
             throw new TwitterException(ex.getMessage(), ex);
         }
     }
 
     /**
      * Get the login username for this Twitter API
      *
      * @return login Username for this twitter API
      */
     public String getLoginUsername() {
         return myLoginUsername;
     }
 
     /**
      * Get the display username for this Twitter API
      *
      * @return display Username for this twitter API
      */
     public String getDisplayUsername() {
         return myDisplayUsername;
     }
 
     /**
      * Have we been authorised to use this account?
      *
      * @return true if we have been authorised, else false.
      */
     public boolean isAllowed() {
         return isAllowed(false);
     }
 
     /**
      * Have we been authorised to use this account?
      * Forcing a recheck may use up an API call.
      *
      * @param forceRecheck force a recheck to see if we are allowed.
      * @return true if we have been authorised, else false.
      */
     public boolean isAllowed(final boolean forceRecheck) {
         if (myLoginUsername.isEmpty()) { return false; }
 
         if ((useOAuth && (getToken().isEmpty() || getTokenSecret().isEmpty())) || (!useOAuth && myPassword.isEmpty())) {
             return false;
         }
         if (allowed == allowed.UNKNOWN || forceRecheck) {
             try {
                 final URL url = new URL(getURL("account/verify_credentials"));
                 final HttpURLConnection request = (HttpURLConnection) url.openConnection();
                 final XMLResponse doc = getXML(request);
                 allowed = (request.getResponseCode() == 200) ? allowed.TRUE : allowed.FALSE;
 
                 if (doc.isGood() && allowed.getBooleanValue()) {
                     final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                     updateUser(user);
                     getRemainingApiCalls();
                 }
             } catch (IOException ex) {
                 if (isDebug()) {
                     handleError(ex, "* (1) isAllowed", apiInput, apiOutput);
                 }
                 allowed = allowed.FALSE;
             }
         }
 
         return allowed.getBooleanValue();
     }
 
     /**
      * Add the user with the given screen name as a friend.
      *
      * @param name name to add
      * @return The user just added
      */
     public TwitterUser addFriend(final String name) {
         try {
             final XMLResponse doc = postXML(getURL("friendships/create"), "screen_name=" + URLEncoder.encode(name, "utf-8"));
             if (doc.isGood()) {
                 final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                 updateUser(user);
                 return user;
             }
         } catch (UnsupportedEncodingException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) addFriend: "+name, apiInput, apiOutput);
             }
         }
 
         return null;
     }
 
     /**
      * Remove the user with the given screen name as a friend.
      *
      * @param name name to remove
      * @return The user just deleted
      */
     public TwitterUser delFriend(final String name) {
         try {
             final XMLResponse doc = postXML(getURL("friendships/destroy"), "screen_name=" + URLEncoder.encode(name, "utf-8"));
             if (doc.isGood()) {
                 final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                 uncacheUser(user);
 
                 return user;
             }
         } catch (UnsupportedEncodingException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) delFriend: "+name, apiInput, apiOutput);
             }
         }
 
         return null;
     }
 
     /**
      * Block a user on twitter.
      * 
      * @param name Username to block.
      * @return The user just blocked.
      */
     public TwitterUser blockUser(final String name) {
         try {
             final XMLResponse doc = postXML(getURL("blocks/create"), "screen_name=" + URLEncoder.encode(name, "utf-8"));
             if (doc.isGood()) {
                 final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                 uncacheUser(user);
 
                 return user;
             }
         } catch (UnsupportedEncodingException ex) {
             if (isDebug()) {
                 handleError(ex, "*(1) blockUser: "+name, apiInput, apiOutput);
             }
         }
 
         return null;
     }
 
     /**
      * Unblock a user on twitter.
      * 
      * @param name Username to unblock.
      * @return The user just unblocked.
      */
     public TwitterUser unblockUser(final String name) {
         try {
             final XMLResponse doc = postXML(getURL("blocks/destroy"), "screen_name=" + URLEncoder.encode(name, "utf-8"));
             if (doc.isGood()) {
                 final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                 uncacheUser(user);
 
                 return user;
             }
         } catch (UnsupportedEncodingException ex) {
             if (isDebug()) {
                 handleError(ex, "* (1) unblockUser: "+name, apiInput, apiOutput);
             }
         }
 
         return null;
     }
 }

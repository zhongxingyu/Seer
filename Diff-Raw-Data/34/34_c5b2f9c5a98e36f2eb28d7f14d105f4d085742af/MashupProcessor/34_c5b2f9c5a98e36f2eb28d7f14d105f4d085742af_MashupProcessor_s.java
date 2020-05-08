 package org.apache.camel.processor.mashup.core;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.Processor;
 import org.apache.camel.processor.mashup.api.IExtractor;
 import org.apache.camel.processor.mashup.model.*;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.NTCredentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.impl.auth.BasicSchemeFactory;
 import org.apache.http.impl.auth.NTLMSchemeFactory;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Camel processor loading the navigation file and extract data
  */
 public class MashupProcessor implements Processor {
 
     private final static transient Logger LOGGER = LoggerFactory.getLogger(MashupProcessor.class);
 
     public final static String DEFAULT_STORE = "data/mashup";
     public final static String MASHUP_ID_HEADER = "MASHUP_ID";
     public final static String MASHUP_STORE_HEADER = "MASHUP_STORE";
 
     public void process(Exchange exchange) throws Exception {
         LOGGER.trace("Get the Camel in message");
         Message in = exchange.getIn();
 
         LOGGER.trace("Get the Camel out message");
         Message out = exchange.getOut();
 
         LOGGER.trace("Get the {} header", MASHUP_ID_HEADER);
         String mashupId = (String) in.getHeader(MASHUP_ID_HEADER);
         LOGGER.debug("Mashup ID: {}", mashupId);
 
         LOGGER.trace("Get the {} header", MASHUP_STORE_HEADER);
         String store = (String) in.getHeader(MASHUP_STORE_HEADER);
         LOGGER.debug("Mashup Store: {}", store);
 
         LOGGER.debug("Digesting the navigation file {}/{}.xml", store, mashupId);
         FileInputStream fileInputStream = new FileInputStream(store + "/" + mashupId + ".xml");
         Mashup mashup = new Mashup();
         mashup.digeste(fileInputStream);
 
         LOGGER.trace("Create the HTTP client");
         DefaultHttpClient httpClient = new DefaultHttpClient();
         // ClientConnectionManager mgr = httpClient.getConnectionManager();
         // HttpParams params = httpClient.getParams();
         // httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
 
         if (mashup.getProxy() != null) {
             LOGGER.trace("Registering the HTTP client proxy");
 
             if (mashup.getProxy().getScheme().equalsIgnoreCase("NTLM")) {
                 LOGGER.trace("Registering a NTLM authenticated proxy");
                 httpClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
                 NTCredentials credentials = new NTCredentials(mashup.getProxy().getUsername(),
                         mashup.getProxy().getPassword(),
                         mashup.getProxy().getWorkstation(),
                         mashup.getProxy().getDomain());
                 httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
                 HttpHost proxy = new HttpHost(mashup.getProxy().getHost(),
                         Integer.parseInt(mashup.getProxy().getPort()));
                 httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
             } else if (mashup.getProxy().getScheme().equalsIgnoreCase("Basic")) {
                 LOGGER.trace("Registering a Basic authenticated proxy");
                 httpClient.getAuthSchemes().register("basic", new BasicSchemeFactory());
                 UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(mashup.getProxy().getUsername(),
                         mashup.getProxy().getPassword());
                 httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
                 HttpHost proxy = new HttpHost(mashup.getProxy().getHost(),
                         Integer.parseInt(mashup.getProxy().getPort()));
                 httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
             } else {
                 throw new IllegalArgumentException("Proxy authentication " + mashup.getProxy().getScheme() + " not supported");
             }
         }
 
         CookieStore cookieStore = CookieStore.getInstance();
 
         LOGGER.trace("Iterate in the pages");
         for (Page page : mashup.getPages()) {
 
             LOGGER.trace("Replacing the headers in the URL");
             String url = page.getUrl();
             for (String header : in.getHeaders().keySet()) {
                 LOGGER.trace("Replace %{}% with {}", header, in.getHeader(header).toString());
                 url = url.replace("%" + header + "%", in.getHeader(header).toString());
             }
 
             LOGGER.trace("Replace params with param tags if exist");
             List<NameValuePair> nvps = new ArrayList<NameValuePair>();
             if (page.getParams() != null && page.getParams().size() > 0) {
                 for (Param param : page.getParams()) {
                     String value = param.getValue();
                     for (String header : in.getHeaders().keySet()) {
                         value = value.replace("%" + header + "%", in.getHeader(header).toString());
                     }
                     nvps.add(new BasicNameValuePair(param.getName(), value));
                 }
             }
 
             LOGGER.trace("Constructing the HTTP request");
             HttpUriRequest request = null;
             if (page.getMethod() != null && page.getMethod().equalsIgnoreCase("POST")) {
                 HttpPost postRequest = new HttpPost(url);
                 if (nvps.size() > 0) {
                     postRequest.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
                 }
                 request = postRequest;
             } else {
                 if ((nvps.size() > 0)) {
                     if (!url.contains("?")) {
                         url = url + "?";
                     }
                     for (NameValuePair nvp : nvps) {
                         if (!url.endsWith("&") && (!url.endsWith("?"))) {
                             url = url + "&";
                         }
                         url = url + nvp.getName() + "=" + nvp.getValue();
                     }
                 }
                 request = new HttpGet(url);
             }
 
             if (mashup.getCookieStore() != null) {
                 LOGGER.trace("Looking cookies in the cookie store");
                 String cookieKey = mashup.getCookieStore().getKey();
                 if (cookieKey == null) {
                     throw new IllegalArgumentException("CookieStore key is mandatory");
                 }
                 for (String header : in.getHeaders().keySet()) {
                     cookieKey = cookieKey.replace("%" + header + "%", in.getHeader(header).toString());
                 }
                 List<org.apache.http.cookie.Cookie> storedCookies = cookieStore.getCookies(cookieKey);
                for (org.apache.http.cookie.Cookie cookie : storedCookies) {
                    LOGGER.trace("Adding cookie " + cookie.getName() + " in the HTTP client");
                    BasicCookieStore basicCookieStore = new BasicCookieStore();
                    basicCookieStore.addCookie(cookie);
                    httpClient.setCookieStore(basicCookieStore);
                 }
             } else {
                 LOGGER.warn("No cookie configuration defined");
             }
 
             HttpResponse response = httpClient.execute(request);
             HttpEntity entity = response.getEntity();
 
             if (mashup.getCookieStore() != null) {
                 String cookieKey = mashup.getCookieStore().getKey();
                 if (cookieKey == null) {
                     throw new IllegalArgumentException("CookieStore key is mandatory");
                 }
                 for (String header : in.getHeaders().keySet()) {
                     cookieKey = cookieKey.replace("%" + header + "%", in.getHeader(header).toString());
                 }
                 LOGGER.trace("Populating the cookie store");
                 List<org.apache.http.cookie.Cookie> cookies = httpClient.getCookieStore().getCookies();
 
                 for (org.apache.http.cookie.Cookie cookie : cookies) {
                     LOGGER.trace("Storing cookie " + cookie.getName());
                     cookieStore.addCookie(cookieKey, cookie);
                 }
             }
 
             if (page.getExtractors() != null && page.getExtractors().size() > 0) {
                 LOGGER.trace("Populate content to be used by extractors");
                 String content = EntityUtils.toString(entity);
                 try {
                     for (Extractor extractor : page.getExtractors()) {
                         IExtractor extractorBean = this.instantiateExtractor(extractor);
                         String extractedData = extractorBean.extract(content);
                         if (extractor.isMandatory() && (extractedData == null || extractedData.isEmpty())) {
                             throw new IllegalStateException("Extracted data is empty");
                         }
                         if (extractor.isAppend()) {
                             if (out.getBody() == null) {
                                 out.setBody("<extract id=\"" + extractor.getId() + "\"><![CDATA[" + extractedData + "]]></extract>");
                             } else {
                                 out.setBody(out.getBody() + "<extract id=\"" + extractor.getId() + "\"><![CDATA[" + extractedData + "]]></extract>");
                             }
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                     LOGGER.warn("An exception occurs during the extraction", e);
                     LOGGER.warn("Calling the error handler");
                     exchange.setException(e);
                     out.setFault(true);
                     out.setBody(null);
                     if (page.getErrorHandler() != null && page.getErrorHandler().getExtractors() != null
                             && page.getErrorHandler().getExtractors().size() > 0) {
                         LOGGER.trace("Processing the error handler extractor");
                         for (Extractor extractor : page.getErrorHandler().getExtractors()) {
                             IExtractor extractorBean = this.instantiateExtractor(extractor);
                             String extractedData = extractorBean.extract(content);
                            if (extractedData != null) {
                                out.setBody(out.getBody() + extractedData);
                             }
                         }
                     }
                 }
 
             } else {
                 EntityUtils.toString(entity);
             }
 
             if (page.getWait() > 0) {
                 Thread.sleep(page.getWait() * 1000);
             }
 
         }
 
     }
 
     /**
      * Create a new instance of a extractor
      *
      * @param extractor the extractor model object.
      * @return the IExtractor object.
      */
     protected IExtractor instantiateExtractor(Extractor extractor) throws Exception {
         LOGGER.trace("Create new instance of " + extractor.getClazz() + "extractor");
         Class extractorClass = Class.forName(extractor.getClazz());
         IExtractor extractorBean = (IExtractor) extractorClass.newInstance();
         if (extractor.getProperties() != null) {
             for (Property property : extractor.getProperties()) {
                 LOGGER.trace("Setting property " + property.getName() + " with value " + property.getValue());
                 PropertyUtils.setProperty(extractorBean, property.getName(), property.getValue());
             }
         }
         return extractorBean;
     }
 
 }

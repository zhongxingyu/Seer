 package com.googlecode.lighthttp.impl;
 
 import com.googlecode.lighthttp.Cookie;
 import com.googlecode.lighthttp.WebBrowser;
 import com.googlecode.lighthttp.WebRequest;
 import com.googlecode.lighthttp.WebResponse;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Thread safe implementation of {@link com.googlecode.lighthttp.WebBrowser}
 * Difference from ThreadLocalWebBrowser is that this implementation share cookies and other params
  *
  * @author Sergey Pilukin
  * @since 09.02.2009 20:57:29
  */
public final class MultiThreadWebBrowser implements WebBrowser {
     private Map<String, WebBrowser> webBrowsersList = new HashMap<String, WebBrowser>(); 
 
     protected final Map<String, String> defaultHeaders = new HashMap<String, String>();
     protected int retryCount = 3;
     protected int socketTimeout = 60000;
     protected int connectionTimeout = 30000;
 
     private final Object setRetryCountMonitor = new Object();
     private final Object setSocketTimeoutMonitor = new Object();
     private final Object setConnectionTimeoutMonitor = new Object();
 
     private String getThreadName() {
         return Thread.currentThread().getName();
     }
 
     private void initNewBrowser(WebBrowser webBrowser) {
         synchronized (defaultHeaders) {
                 Map<String, String> headersProperties = new HashMap<String, String>(defaultHeaders);
                 webBrowser.setDefaultHeaders(headersProperties);
         }
         synchronized (setRetryCountMonitor) {
             webBrowser.setRetryCount(retryCount);
         }
         synchronized (setSocketTimeoutMonitor) {
             webBrowser.setSocketTimeout(socketTimeout);
         }
         synchronized (setConnectionTimeoutMonitor) {
             webBrowser.setConnectionTimeout(connectionTimeout);
         }
     }
 
     private final Object getBrowserForCurrentThreadMonitor = new Object();
 
     private WebBrowser getBrowserForCurrentThread() {
         synchronized (getBrowserForCurrentThreadMonitor) {
             String threadName = getThreadName();
             if (webBrowsersList.get(threadName) == null) {
                 DefaultWebBrowser defaultWebBrowser = new DefaultWebBrowser(false);
                 initNewBrowser(defaultWebBrowser);
                 webBrowsersList.put(threadName, defaultWebBrowser);
             }
 
             return webBrowsersList.get(threadName);
         }
     }
 
     public WebResponse getResponse(String url) throws IOException {
         return getBrowserForCurrentThread().getResponse(url);
     }
 
     public WebResponse getResponse(String url, String expectedResponseCharset) throws IOException {
         return getBrowserForCurrentThread().getResponse(url, expectedResponseCharset);
     }
 
     public WebResponse getResponse(WebRequest webRequest) throws IOException {
         return getBrowserForCurrentThread().getResponse(webRequest);
     }
 
     public WebResponse getResponse(WebRequest webRequest, String expectedResponseCharset) throws IOException {
         return getBrowserForCurrentThread().getResponse(webRequest, expectedResponseCharset);
     }
 
     public Map<String, String> getHeaders() {
         return getBrowserForCurrentThread().getHeaders();
     }
 
     public String getHeader(String headerName) {
         return getBrowserForCurrentThread().getHeader(headerName);
     }
 
     public WebBrowser addHeaders(Map<String, String> headers) {
         synchronized (defaultHeaders) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.addHeaders(headers);
             }
 
             return this;
         }
     }
 
     public WebBrowser addHeader(String name, String value) {
         synchronized (defaultHeaders) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.addHeader(name, value);
             }
 
             return this;
         }
     }
 
     public WebBrowser setDefaultHeaders(Map defaultHeaders) {
         synchronized (this.defaultHeaders) {
             this.defaultHeaders.clear();
             for (Object entryObject : defaultHeaders.entrySet()) {
                 Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>)entryObject;
                 String headerName = String.valueOf(entry.getKey());
                 String headerValue = String.valueOf(entry.getValue());
                 this.defaultHeaders.put(headerName, headerValue);
             }
 
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.setDefaultHeaders(defaultHeaders);
             }
 
             return this;
         }
     }
 
     public Integer getRetryCount() {
         return getBrowserForCurrentThread().getRetryCount();
     }
 
     public WebBrowser setRetryCount(Integer retryCount) {
         synchronized (setRetryCountMonitor) {
             this.retryCount = retryCount;
 
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.setRetryCount(retryCount);
             }
 
             return this;
         }
     }
 
     public Integer getSocketTimeout() {
         return getBrowserForCurrentThread().getSocketTimeout();
     }
 
     public WebBrowser setSocketTimeout(Integer socketTimeout) {
         synchronized (setSocketTimeoutMonitor) {
             this.socketTimeout = socketTimeout;
 
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.setSocketTimeout(socketTimeout);
             }
 
             return this;
         }
     }
 
     public Integer getConnectionTimeout() {
         return getBrowserForCurrentThread().getConnectionTimeout();
     }
 
     public WebBrowser setConnectionTimeout(Integer connectionTimeout) {
         synchronized (setConnectionTimeoutMonitor) {
             this.connectionTimeout = connectionTimeout;
 
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.setConnectionTimeout(connectionTimeout);
             }
 
             return this;
         }
     }
 
     public List<Cookie> getCookies() {
         return getBrowserForCurrentThread().getCookies();
     }
 
     private final Object cookieMonitor = new Object();
 
     public WebBrowser addCookie(Cookie cookie) {
         synchronized (cookieMonitor) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.addCookie(cookie);
             }
 
             return this;
         }
     }
 
     public WebBrowser addCookies(List<Cookie> cookies) {
         synchronized (cookieMonitor) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.addCookies(cookies);
             }
 
             return this;
         }
     }
 
     public WebBrowser clearAllCookies() {
         synchronized (cookieMonitor) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.clearAllCookies();
             }
 
             return this;
         }
     }
 
     private final Object setProxyMonitor = new Object();
 
     public WebBrowser setProxy(String url, int port) {
         synchronized (setProxyMonitor) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.setProxy(url, port);
             }
 
             return this;
         }
     }
 
     public WebBrowser clearProxy() {
         synchronized (setProxyMonitor) {
             for (WebBrowser webBrowser: webBrowsersList.values()) {
                 webBrowser.clearProxy();
             }
 
             return this;
         }
     }
 
     public WebBrowser abort() {
         throw new RuntimeException("Not implemented");
     }
 }

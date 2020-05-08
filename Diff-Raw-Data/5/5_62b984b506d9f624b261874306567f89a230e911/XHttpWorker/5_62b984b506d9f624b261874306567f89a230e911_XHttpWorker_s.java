 
 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.polyvi.xface.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.ref.WeakReference;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.SyncBasicHttpContext;
 
 import com.polyvi.xface.ssl.XSSLSocketFactory;
 import com.polyvi.xface.util.XStringUtils;
 /**
  * 实现异步Http请求的封装
  *
  */
 public class XHttpWorker {
 
     /** 检查网络连接的间隔时间 */
     private static final int SERVER_CONNECT_TIMEOUT = 10000;
     private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
     private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
     private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
     private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
     private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
     private static final String ENCODING_GZIP = "gzip";
 
     /**url检测的正则表达式及错误信息*/
     private static final String TAG_URL_REGEX = "https?://([\\w]+\\.)+[\\w-]+(:\\d*)?(/[\\w-./?%&=]*)?$";
     private static final String TAG_URL_INVALID_MSG = "url is invalid";
 
     private AbstractHttpClient mHttpClient;
     private HttpContext mHttpContext;
     private Map<String, String> mClientHeaderMap;
     private ThreadPoolExecutor mThreadPool;
     /**防止异步请求次数太多 导致内存紧张 通过弱引用存储任务对象*/
     private List<WeakReference<Future<?>>> mRequestList = new LinkedList<WeakReference<Future<?>>>();
 
     public XHttpWorker() {
         mThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
         mClientHeaderMap = new HashMap<String, String>();
         initHttpClient();
     }
 
     /**
      * 初始化httpclient
      */
     private void initHttpClient() {
         BasicHttpParams httpParams = new BasicHttpParams();
         ConnManagerParams.setTimeout(httpParams, socketTimeout);
         ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
                 new ConnPerRouteBean(maxConnections));
         ConnManagerParams.setMaxTotalConnections(httpParams,
                 DEFAULT_MAX_CONNECTIONS);
 
         HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
         HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
         HttpConnectionParams.setTcpNoDelay(httpParams, true);
         HttpConnectionParams.setSocketBufferSize(httpParams,
                 DEFAULT_SOCKET_BUFFER_SIZE);
 
         HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setUserAgent(httpParams, "xface");
 
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory
                 .getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", XSSLSocketFactory
                 .getSocketFactory(), 443));
         schemeRegistry.register(new Scheme("https", XSSLSocketFactory
                 .getSocketFactory(), 8443));
         ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
                 httpParams, schemeRegistry);
         mHttpClient = new DefaultHttpClient(cm, httpParams);
         mHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
             @Override
             public void process(HttpRequest request, HttpContext context) {
                 if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                     request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                 }
                 for (String header : mClientHeaderMap.keySet()) {
                     request.addHeader(header, mClientHeaderMap.get(header));
                 }
             }
         });
 
         mHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {
             @Override
             public void process(HttpResponse response, HttpContext context) {
                 final HttpEntity entity = response.getEntity();
                 if (entity == null) {
                     return;
                 }
                 final Header encoding = entity.getContentEncoding();
                 if (encoding != null) {
                     for (HeaderElement element : encoding.getElements()) {
                         if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                             response.setEntity(new InflatingEntity(response
                                     .getEntity()));
                             break;
                         }
                     }
                 }
             }
         });
         mHttpContext = new SyncBasicHttpContext(new BasicHttpContext());
     }
 
     /**
      * http post请求(异步)
      *
      * @param url
      *            请求的url地址
      * @param headers
      *            请求的头部
      * @param postData
      *            post给服务器的数据
      * @param contentType
      *            数据类型
      * @param handler
      *            http响应的处理器
      */
     public void post(String url, Header[] headers, String postData,
             String contentType, XAsyncHttpResponseHandler handler)
             throws IllegalArgumentException, IOException {
         checkUrlValid(url);
         HttpEntity entity = null;
         if (postData != null) {
             try {
                entity = new StringEntity(postData);
             } catch (UnsupportedEncodingException e) {
                 throw e;
             }
         }
         this.post(url, headers, entity, contentType, handler);
     }
 
     /**
      * http post请求(异步)
      *
      * @param url
      *            请求的url地址
      * @param headers
      *            请求的头部
      * @param postData
      *            post给服务器的数据
      * @param contentType
      *            数据类型
      * @param handler
      *            http响应的处理器
      */
     public void post(String url, Header[] headers, HttpEntity postData,
             String contentType, XAsyncHttpResponseHandler handler)
             throws IllegalArgumentException, IOException {
         checkUrlValid(url);
         HttpPost post = new HttpPost(url);
         this.setHeader(post, headers);
         doHttpRequest(addEntityToRequestBase(post, postData), contentType,
                 handler);
 
     }
 
     /**
      * http post请求(同步)
      *
      * @param url
      *            请求的url地址
      * @param headers
      *            请求的头部
      * @param postData
      *            post给服务器的数据
      * @param contentType
      *            数据类型
      * @param handler
      *            http响应的处理器
      */
     public XSynHttpResponse post(String url, Header[] headers, String postData,
             String contentType) throws IllegalArgumentException, IOException {
         checkUrlValid(url);
         XSyncHttpResponseHandler handler = new XSyncHttpResponseHandler();
         this.post(url, headers, postData, contentType, handler);
         return handler.getHttpResponse();
     }
 
     /**
      * http post请求(同步)
      *
      * @param url
      *            请求的url地址
      * @param headers
      *            请求的头部
      * @param postData
      *            post给服务器的数据
      * @param contentType
      *            数据类型
      * @param handler
      *            http响应的处理器
      */
     public XSynHttpResponse post(String url, Header[] headers,
             HttpEntity postData, String contentType) throws IllegalArgumentException, IOException {
         checkUrlValid(url);
         XSyncHttpResponseHandler handler = new XSyncHttpResponseHandler();
         this.post(url, headers, postData, contentType, handler);
         return handler.getHttpResponse();
     }
 
     /**
      * http get请求(同步)
      *
      * @param url
      *            请求的url地址
      * @param headers
      *            请求的头部
      * @param handler
      *            http响应的处理器
      */
     public XSynHttpResponse get(String url, Header[] headers) throws IllegalArgumentException {
         checkUrlValid(url);
         XSyncHttpResponseHandler handler = new XSyncHttpResponseHandler();
         this.get(url, headers, handler);
         return handler.getHttpResponse();
     }
 
     /**
      * http get请求(异步)
      *
      * @param url
      *            请求的url地址
      * @param headers
      *            请求的头部
      * @param handler
      *            http响应的处理器
      */
     public void get(String url, Header[] headers,
             XAsyncHttpResponseHandler handler) throws IllegalArgumentException{
         checkUrlValid(url);
         HttpGet get = new HttpGet(url);
         this.setHeader(get, headers);
         doHttpRequest(get, null, handler);
     }
 
     /**
      * 取消请求
      *
      * @param mayInterruptIfRunning
      */
     public void cancelRequest(boolean mayInterruptIfRunning) {
         for (WeakReference<Future<?>> requestRef : mRequestList) {
             Future<?> request = requestRef.get();
             if (request != null) {
                 request.cancel(mayInterruptIfRunning);
             }
         }
     }
 
     /**
      * 工具方法 设置头部
      *
      * @param request
      * @param headers
      */
     private void setHeader(HttpUriRequest request, Header[] headers) {
         if (headers != null)
             request.setHeaders(headers);
     }
 
     /**
      * 提交一个异步http请求
      *
      * @param request
      * @param contentType
      * @param handler
      */
     private void doHttpRequest(HttpUriRequest request, String contentType,
             XAsyncHttpResponseHandler handler) {
         if (contentType != null) {
             request.addHeader("Content-Type", contentType);
         }
         if (handler.isAsync()) {
             Future<?> task = mThreadPool.submit(new XAsyncHttpRequest(
                     mHttpClient, mHttpContext, request, handler));
             mRequestList.add(new WeakReference<Future<?>>(task));
         } else {
             new XAsyncHttpRequest(mHttpClient, mHttpContext, request, handler)
                     .run();
         }
 
     }
 
     /**
      * 增加http实体到post请求中
      *
      * @param requestBase
      * @param entity
      * @return
      */
     private HttpEntityEnclosingRequestBase addEntityToRequestBase(
             HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
         if (entity != null) {
             requestBase.setEntity(entity);
         }
 
         return requestBase;
     }
 
     /**
      * 设置头部 这些头部会被增加所有的http请求中
      *
      * @param header
      * @param value
      */
     public void addHeader(String header, String value) {
         mClientHeaderMap.put(header, value);
     }
 
     /**
      * 检查指定url的服务器是否可用
      *
      * @param url
      *            [in]
      * @return
      */
     public static boolean isServerAccessable(String url) {
         boolean usable = false;
         try {
             URL urlCon = new URL(url);
             HttpURLConnection httpUrl = (HttpURLConnection) urlCon
                     .openConnection();
             httpUrl.setConnectTimeout(SERVER_CONNECT_TIMEOUT);
             httpUrl.setReadTimeout(SERVER_CONNECT_TIMEOUT);
             if (httpUrl.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 usable = true;
                 return usable;
             }
         } catch (IOException e) {
             usable = false;
             e.printStackTrace();
         }
         return usable;
     }
 
     /**
      * 对于gzip数据客户端需要解压
      */
     private static class InflatingEntity extends HttpEntityWrapper {
         public InflatingEntity(HttpEntity wrapped) {
             super(wrapped);
         }
 
         @Override
         public InputStream getContent() throws IOException {
             return new GZIPInputStream(wrappedEntity.getContent());
         }
 
         @Override
         public long getContentLength() {
             return -1;
         }
     }
 
     /**
      * 检查url的合法性
      * @param url:要检测的url
      * @throws IllegalArgumentException
      */
     private void checkUrlValid(String url) throws IllegalArgumentException {
         if(XStringUtils.isEmptyString(url) || !Pattern.matches(TAG_URL_REGEX, url)){
             throw new IllegalArgumentException(TAG_URL_INVALID_MSG);
         }
     }
 }

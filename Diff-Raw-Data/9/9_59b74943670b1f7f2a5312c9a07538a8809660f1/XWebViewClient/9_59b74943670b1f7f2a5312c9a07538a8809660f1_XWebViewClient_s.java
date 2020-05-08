 
 /*
  This file was modified from or inspired by Apache Cordova.
 
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */
 
 package com.polyvi.xface.view;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.Enumeration;
 
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.net.http.SslError;
 import android.os.Build;
 import android.webkit.ClientCertRequestHandler;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebResourceResponse;
 import android.webkit.WebView;
 import android.webkit.WebViewClientClassicExt;
 
 import com.polyvi.xface.app.XWhiteList;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.core.XNativeToJsMessageQueue;
 import com.polyvi.xface.event.XIWebAppEventListener;
 import com.polyvi.xface.ssl.XSSLManager;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XLog;
 
 /**
  * 主要实现WebView提供的回调函数
  */
 public class XWebViewClient extends WebViewClientClassicExt {
 
     private static final String CLASS_NAME = XWebViewClient.class
             .getSimpleName();
     private static final String SMS_BODY = "body=";
     private XAppWebView mWebAppView;
     private String ABOUT_BLANK = "about:blank";
     private XInputScaleHandler mInputScaleHandler;
     /** < 用来处理input框获得焦点自动变大的问题 */
     private XISystemContext mSystemContext;
     private String[] URL_PARAMS_TAG = { "#", "?" };
     private static final String XFACE_EXEC_URL_PREFIX = "http://xface_exec/";
 
     public XWebViewClient(XISystemContext systemContext, XAppWebView appWebView) {
         mWebAppView = appWebView;
         mInputScaleHandler = new XInputScaleHandler();
         mSystemContext = systemContext;
     }
 
     @Override
     public void onPageFinished(WebView view, String url) {
         super.onPageFinished(view, url);
         XIWebAppEventListener evtListener = mWebAppView.getAppEventListener();
         if (null == evtListener) {
             XLog.w(CLASS_NAME, "onPageFinished: XIWebAppEventListener is null!");
             return;
         }
         XAppWebView appView = (XAppWebView) view;
         evtListener.onPageLoadingFinished(appView);
     }
 
     @Override
     public void onPageStarted(WebView view, String url, Bitmap favicon) {
         XIWebAppEventListener evtListener = mWebAppView.getAppEventListener();
         if (null == evtListener) {
             XLog.w(CLASS_NAME, "onPageStarted: XIWebAppEventListener is null!");
             return;
         }
         // 在每次开始加载页面时重新设置WideViewPort为true来支持适配
         view.getSettings().setUseWideViewPort(true);
         // 在每次开始加载页面时恢复，因多点触控事件中使缩放失效的设置
         view.getSettings().setSupportZoom(true);
         // 当页面为指定的about_blank空页面时，不需要处理pageStart事件
         if (!ABOUT_BLANK.equals(url)) {
             evtListener.onPageStarted((XAppWebView) view);
         }
         XAppWebView appView = (XAppWebView) view;
         // TODO:4.0的手机 如果缩放因子不变 则不会再次适配,如果一个应用不同的页面的适配因子不一样 则需要特殊处理
         // 如果是4.0以下的手机（2.2 2.3）[3.1等没测过] 则不管缩放因子是否变化,每次进入一个页面都会适配
         if (Build.VERSION.SDK_INT < XInputScaleHandler.APILEVEL_11) {
             appView.setAdapated(false);
         }
     }
 
     @Override
     public void onReceivedError(WebView view, int errorCode,
             String description, String failingUrl) {
         // TODO 对错误进行处理
         super.onReceivedError(view, errorCode, description, failingUrl);
     }
 
     @Override
     public boolean shouldOverrideUrlLoading(WebView view, String url) {
         try {
             // 检查是否是一个exec()桥指令信息.
             if (XNativeToJsMessageQueue.ENABLE_LOCATION_CHANGE_EXEC_MODE
                     && url.startsWith(XFACE_EXEC_URL_PREFIX)) {
                 handleExecUrl(url);
             }
 
             XWhiteList whiteList = mWebAppView.getOwnerApp().getAppInfo()
                     .getWhiteList();
             if (url.startsWith("tel")
                     && !XConfiguration.getInstance().isTelLinkEnabled()) {
                 return true;
             }
             // TODO:以后参照phonegap管理url和白名单处理。
             else if (url.startsWith(XConstant.FILE_SCHEME)
                     || url.startsWith("data:")
                    || url.startsWith(XConstant.HTTP_SCHEME)
                    || url.startsWith(XConstant.HTTPS_SCHEME)
                    || (null != whiteList && whiteList.isUrlWhiteListed(url))) {
                 /**
                  * 由于三星I9003如果页面不存在会崩溃，所以这里对url进行预处理 如果url不存在给出错误提示
                  */
                 return handleUrl(url);
             } else if (url.startsWith(XConstant.SCHEME_SMS)) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 // 获取地址
                 String address = null;
                 int parmIndex = url.indexOf('?');
                 if (-1 == parmIndex) {
                     address = url.substring(XConstant.SCHEME_SMS.length());
                 } else {
                     address = url.substring(XConstant.SCHEME_SMS.length(),
                             parmIndex);
                     Uri uri = Uri.parse(url);
                     String query = uri.getQuery();
                     if (null != query) {
                         if (query.startsWith(SMS_BODY)) {
                             intent.putExtra("sms_body",
                                     query.substring(SMS_BODY.length()));
                         }
                     }
                 }
                 intent.setData(Uri.parse(XConstant.SCHEME_SMS + address));
                 intent.putExtra("address", address);
                 intent.setType("vnd.android-dir/mms-sms");
                 mSystemContext.getContext().startActivity(intent);
                 return true;
             } else {
                 // 如果前面的加载条件都不满足，则用默认方式加载
                 return startSysApplication(url);
             }
         } catch (ActivityNotFoundException e) {
             XLog.e(CLASS_NAME, e.toString());
         }
         return false;
     }
 
     /**
      * 解析设置webView的url地址，并执行
      *
      * @param url
      */
     private void handleExecUrl(String url) {
         try {
             int idx1 = XFACE_EXEC_URL_PREFIX.length();
             int idx2 = url.indexOf('#', idx1 + 1);
             int idx3 = url.indexOf('#', idx2 + 1);
             int idx4 = url.indexOf('#', idx3 + 1);
             if (idx1 == -1 || idx2 == -1 || idx3 == -1 || idx4 == -1) {
                 XLog.e(CLASS_NAME, "Could not decode URL command: " + url);
                 return;
             }
             String service = url.substring(idx1, idx2);
             String action = url.substring(idx2 + 1, idx3);
             String callbackId = url.substring(idx3 + 1, idx4);
             String jsonArgs = url.substring(idx4 + 1);
             mWebAppView.getOwnerApp().getJSNativeBridge()
                     .exec(service, action, callbackId, jsonArgs);
         } catch (JSONException e) {
             XLog.e(CLASS_NAME, "handleExecUrl: JSONException");
             e.printStackTrace();
             return;
         }
     }
 
     @Override
     public void onScaleChanged(WebView view, float oldScale, float newScale) {
         XLog.d("xface", "old " + oldScale + " new " + newScale);
         mInputScaleHandler.changeDefaultScale(view, oldScale, newScale);
     }
 
     /**
      * 根据url启动系统程序 url 启动系统程序的url
      */
     private boolean startSysApplication(String url) {
         Intent intent = null;
         if (url.contains("content://media/external")) {
             intent = new Intent(Intent.ACTION_PICK);
         } else {
             intent = new Intent(Intent.ACTION_VIEW);
         }
         intent.setData(Uri.parse(url));
         try {
             mSystemContext.getContext().startActivity(intent);
         } catch (ActivityNotFoundException e) {
             XLog.e(CLASS_NAME, e.toString());
         }
         return true;
     }
 
     /**
      * 对url进行处理，并返回处理结果。
     * 
      * @param url
      *            要处理的url
      * @param return true:url已经被处理，false：未被处理。
      */
     private boolean handleUrl(String url) {
         // TODO: I9003有一款机子 如果页面不存在会崩溃 需要处理
         return false;
     }
 
     @SuppressLint("Override")
     @TargetApi(11)
     public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
         /** 如果url不在白名单中，则返回一个空白页面 */
         XWhiteList whiteList = mWebAppView.getOwnerApp().getAppInfo()
                 .getWhiteList();
         if ((url.startsWith("http://") || url.startsWith("https://"))
                 && (null != whiteList && !whiteList.isUrlWhiteListed(url))) {
             return getWhitelistResponse();
         }
         if (url.indexOf(XConstant.ASSERT_PROTACAL) == 0) {
             for (int i = 0; i < URL_PARAMS_TAG.length; i++) {
                 if (url.contains(URL_PARAMS_TAG[i])) {
                     String filePath = url.substring(
                             XConstant.ASSERT_PROTACAL.length(), url.length());
                     filePath = filePath.substring(0,
                             filePath.indexOf(URL_PARAMS_TAG[i]));
                     try {
                         InputStream is = mSystemContext.getContext()
                                 .getAssets().open(filePath);
                         WebResourceResponse wr = new WebResourceResponse(
                                 "text/javascript", "utf-8", is);
                         return wr;
                     } catch (IOException e) {
                         return null;
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * 由于服务器的证书可能没有经过CA机构的认证，会出现SslError，通过调用 proceed()来继续SSL连接
      */
     @TargetApi(8)
     @Override
     public void onReceivedSslError(WebView view, SslErrorHandler handler,
             SslError error) {
         handler.proceed();
     }
 
     /**
      * 在android4.0以下版本直接通过反射方法，将客户端证书设置在SSLContext中 为了在Android 4.x
      * WebView上支持客户端证书，这个方法需要用到WebKit的私有类ClientCertRequestHandler
      * 需要引入jar/cer.jar来解决编译问题
      */
     @TargetApi(14)
     public void onReceivedClientCertRequest(WebView view,
             ClientCertRequestHandler handler, String host_and_port) {
         try {
             KeyStore store = XSSLManager.getInstace().getKeyStore();
             // 未内置客户端证书
             if (store == null) {
                 return;
             }
             PrivateKey privateKey = null;
             X509Certificate[] certificates = null;
             Enumeration<String> e = store.aliases();
             while (e.hasMoreElements()) {
                 String alias = e.nextElement();
                 if (store.isKeyEntry(alias)) {
                     KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) store
                             .getEntry(alias, null);
                     privateKey = entry.getPrivateKey();
                     certificates = (X509Certificate[]) entry
                             .getCertificateChain();
                     break;
                 }
             }
             handler.proceed(privateKey, certificates);
         } catch (Exception e) {
             e.printStackTrace();
             XLog.e(CLASS_NAME, e.getMessage());
         }
     }
 
     /** 返回白名单响应页面 */
     @SuppressLint("NewApi")
     private WebResourceResponse getWhitelistResponse() {
         String empty = "";
         ByteArrayInputStream data = new ByteArrayInputStream(empty.getBytes());
         return new WebResourceResponse("text/plain", "UTF-8", data);
     }
 }

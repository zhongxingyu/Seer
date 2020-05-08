 /*
  *
  *  * Copyright (C) 2013 Aleksandr Beshkenadze <behskenadze@gmail.com>
  *  *
  *  * Licensed under the Apache License, Version 2.0 (the "License");
  *  * you may not use this file except in compliance with the License.
  *  * You may obtain a copy of the License at
  *  *
  *  *    http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  *
  */
 
 package net.beshkenadze.anyoauth.oauth;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.text.TextUtils;
 import android.util.Log;
 import net.beshkenadze.anyoauth.utils.MyPreference;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.Api;
 import org.scribe.builder.api.TwitterApi;
 import org.scribe.model.Token;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 public abstract class BaseOAuth {
     private static String consumerKey = "";
     private static String consumerSecret = "";
     private static String accessTokenKey = null;
     private static String accessSecretKey = null;
     private boolean isNeedToken = false;
 
     private static String prefix = "";
 
     private static String accessTokenKeyName = null;
     private static String accessSecretKeyName = null;
 
     private Class<? extends Api> classApi;
 
     private String scopes = "";
 
     private OAuthService service;
     private Token requestToken = null;
     private Context context;
     private MyPreference prefs;
     public static String callback = null;
 
     public interface ReturnCallback {
         void onSuccess();
 
         void onError();
     }
 
     private ReturnCallback returnCallback = new ReturnCallback() {
         public void onSuccess() {
         }
 
         public void onError() {
         }
     };
 
     public BaseOAuth(Context context, Class<? extends Api> classApi, String prefix, boolean isNeedToken) {
         settingsVariable(context, classApi, prefix, isNeedToken);
     }
 
     public BaseOAuth(Context context, Class<? extends Api> classApi, String prefix) {
         settingsVariable(context, classApi, prefix, false);
     }
 
     private void settingsVariable(Context context, Class<? extends Api> classApi, String prefix, boolean isNeedToken) {
         try {
             ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                     PackageManager.GET_META_DATA);
 
            setConsumerKey(ai.metaData.get(prefix + "Key").toString());
            setConsumerSecret(ai.metaData.get(prefix + "Secret").toString());
             setCallback(ai.metaData.getString(prefix + "Callback"));
 
             String scopes = ai.metaData.getString(prefix + "Scopes");
             if (!TextUtils.isEmpty(scopes))
                 setScopes(scopes);
 
 
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
 
         setContext(context);
         setClassApi(classApi);
 
         setAccessTokenKeyName(prefix);
         setAccessSecretKeyName(prefix);
         setNeedToken(isNeedToken);
         setPrefs(new MyPreference(context));
 
         init();
     }
 
     public void init() {
     	
     	ServiceBuilder provider = new ServiceBuilder()
         .provider(getClassApi())
         .apiKey(getConsumerKey())
         .apiSecret(getConsumerSecret())
         .callback(getCallback());
     	
     	if (!TextUtils.isEmpty(getScopes()))
         	provider.scope(getScopes());
     	
     	setService(provider.build());
     }
 
     public static OAuthService getService(Context context, Class<? extends Api> classApi, String prefix) {
 
         ServiceBuilder provider = new ServiceBuilder().provider(classApi);
         try {
             ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                     PackageManager.GET_META_DATA);
             provider.apiKey(ai.metaData.get(prefix + "Key").toString())
                     .apiSecret(ai.metaData.get(prefix + "Secret").toString());
             provider.callback(ai.metaData.getString(prefix + "Callback"));
 
             String scopes = ai.metaData.getString(prefix + "Scopes");
             if (!TextUtils.isEmpty(scopes))
                 provider.scope(scopes);
 
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
 
         return provider.build();
     }
 
     public interface OnApiAuthUrlReturn {
         void onReturn(String authorizationUrl);
     }
 
     public interface OnTokenReturn {
         void onReturn(Token token);
     }
 
     public interface OnApiRequest {
         void onError(String error);
 
         void onSuccess(String data);
     }
 
     public void getAuthUrl(final OnApiAuthUrlReturn callback) {
         if (isNeedToken()) {
             requestToken(new OnTokenReturn() {
                 public void onReturn(Token token) {
                     requestAuthorizationUrl(callback);
                 }
             });
         } else {
             requestAuthorizationUrl(callback);
         }
     }
 
     private void requestAuthorizationUrl(final OnApiAuthUrlReturn callback) {
         new Thread() {
             @Override
             public void run() {
                 callback.onReturn(getService().getAuthorizationUrl(getRequestToken()));
             }
         }.start();
     }
 
     public static boolean isAuth(Context c, String prefix) {
         MyPreference prefs = new MyPreference(c);
 
         setAccessTokenKeyName(prefix);
         setAccessSecretKeyName(prefix);
 
         if (prefs.getString(getAccessTokenKeyName()) != null
                 && prefs.getString(getAccessSecretKeyName()) != null) {
             return true;
         }
         return false;
     }
 
     public void requestToken(final OnTokenReturn callback) {
         new Thread() {
             @Override
             public void run() {
                 setRequestToken(getService().getRequestToken());
                 callback.onReturn(getRequestToken());
             }
         }.start();
     }
 
     public static Token getAccessToken(Context c, String prefix) {
         MyPreference prefs = new MyPreference(c);
         if (isAuth(c, prefix)) {
             return new Token(prefs.getString(getAccessTokenKeyName()),
                     prefs.getString(getAccessSecretKeyName()));
         }
         return null;
     }
 
     public void requestAccessToken(final Verifier verifier) {
         new Thread() {
             @Override
             public void run() {
                 try {
                     Token accessToken = getService().getAccessToken(getRequestToken(),
                             verifier);
                     setAccessToken(accessToken);
                 } catch (Exception e) {
                     getReturnCallback().onError();
                 }
             }
         }.start();
     }
 
     public void requestAccessToken(String oauthToken, String oauthVerifier) {
         Verifier verifier = new Verifier(oauthVerifier);
         requestAccessToken(verifier);
     }
 
     public void setAccessToken(Token accessToken) {
         if (accessToken == null) {
             getReturnCallback().onError();
             return;
         }
 
         setAccessTokenKey(accessToken.getToken());
         setAccessSecretKey(accessToken.getSecret());
         getPrefs().set(getAccessTokenKeyName(), getAccessTokenKey());
         getPrefs().set(getAccessSecretKeyName(), getAccessSecretKey());
         getReturnCallback().onSuccess();
     }
 
     public static String getConsumerKey() {
         return consumerKey;
     }
 
     public void setConsumerKey(String consumerKey) {
         this.consumerKey = consumerKey;
     }
 
     public static String getConsumerSecret() {
         return consumerSecret;
     }
 
     public void setConsumerSecret(String consumerSecret) {
         this.consumerSecret = consumerSecret;
     }
 
     public static String getAccessTokenKey() {
         return accessTokenKey;
     }
 
     public void setAccessTokenKey(String accessTokenKey) {
         this.accessTokenKey = accessTokenKey;
     }
 
     public static String getAccessSecretKey() {
         return accessSecretKey;
     }
 
     public void setAccessSecretKey(String accessSecretKey) {
         this.accessSecretKey = accessSecretKey;
     }
 
     public OAuthService getService() {
         return service;
     }
 
 
     public void setService(OAuthService service) {
         this.service = service;
     }
 
     public Context getContext() {
         return context;
     }
 
     public void setContext(Context context) {
         this.context = context;
     }
 
     public MyPreference getPrefs() {
         return prefs;
     }
 
     public void setPrefs(MyPreference prefs) {
         this.prefs = prefs;
     }
 
     public static String getCallback() {
         return callback;
     }
 
     public static void setCallback(String callback) {
         BaseOAuth.callback = callback;
     }
 
     public Token getRequestToken() {
         return requestToken;
     }
 
     public void setRequestToken(Token requestToken) {
         this.requestToken = requestToken;
     }
 
     public String getScopes() {
         return scopes;
     }
 
     public void setScopes(String scopes) {
         this.scopes = scopes;
     }
 
     public Class<? extends Api> getClassApi() {
         return classApi;
     }
 
     public void setClassApi(Class<? extends Api> classApi) {
         this.classApi = classApi;
     }
 
     public static String getAccessTokenKeyName() {
         return accessTokenKeyName;
     }
 
     public static void setAccessTokenKeyName(String prefix) {
         BaseOAuth.accessTokenKeyName = prefix + "_" + "access_token";
     }
 
     public static String getAccessSecretKeyName() {
         return accessSecretKeyName;
     }
 
     public static void setAccessSecretKeyName(String prefix) {
         BaseOAuth.accessSecretKeyName = prefix + "_" + "access_secret";
     }
 
     public ReturnCallback getReturnCallback() {
         return returnCallback;
     }
 
     public void setReturnCallback(ReturnCallback returnCallback) {
         this.returnCallback = returnCallback;
     }
 
     public boolean isNeedToken() {
         return isNeedToken;
     }
 
     public void setNeedToken(boolean needToken) {
         isNeedToken = needToken;
     }
 }

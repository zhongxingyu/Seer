 package fr.ybo.ybotv.android.service;
 
 
 import android.util.Log;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import fr.ybo.ybotv.android.exception.YboTvErreurReseau;
 import fr.ybo.ybotv.android.exception.YboTvException;
 import fr.ybo.ybotv.android.util.HttpUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 import java.util.List;
 
 public abstract class HttpService {
 
 
     private static final int MAX_RETRY = 5;
 
     public <T> T getObjects(String url, TypeToken<T> typeToken) throws YboTvErreurReseau {
         int countRetry = 0;
 
         while (countRetry < MAX_RETRY - 1) {
             countRetry++;
             try {
                 return getObjectsWithoutRetry(url, typeToken);
             } catch (YboTvErreurReseau erreurReseau) {
                 Log.e("YboTv", "Erreur réseau (" + countRetry + ") en accédant à l'url : " + url);
                 Log.e("YboTv", Log.getStackTraceString(erreurReseau));
             }
         }
         return getObjectsWithoutRetry(url, typeToken);
 
     }
 
     protected <T> T getObjectsWithoutRetry(String url, TypeToken<T> typeToken) throws YboTvErreurReseau {
 
         Reader reader = null;
         try {
             Log.d("YboTv", "Url demandee : " + url);
             HttpClient client = HttpUtils.getHttpClient();
             HttpUriRequest request = new HttpGet(url.replaceAll(" ", "%20"));
             request.addHeader("Accept", "application/json");
             HttpResponse reponse = client.execute(request);
             reader = new InputStreamReader(reponse.getEntity().getContent());
             GsonBuilder gsonBuilder = new GsonBuilder();
             Gson gson = gsonBuilder.create();
             T retour = gson.fromJson(reader, typeToken.getType());
             if (retour == null) {
                 throw new YboTvErreurReseau("Null object in respose");
             }
             return retour;
         } catch (MalformedURLException e) {
             throw new YboTvException(e);
         } catch (IOException e) {
             throw new YboTvErreurReseau(e);
         } catch (JsonSyntaxException e) {
             throw new YboTvErreurReseau(e);
        } catch (JsonIOException e) {
            throw new YboTvErreurReseau(e);
         } finally {
             if (reader != null) {
                 try { reader.close(); } catch (Exception ignore) {}
             }
         }
     }
 
 
 }

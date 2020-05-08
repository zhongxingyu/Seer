 package cz.cvut.fel.via.zboziforandroid.client;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 
 import android.net.Uri;
 
 
 public class ViaClientHttp {
 
 	DefaultHttpClient httpClient;
    public final static String url = "http://api.zbozi.sbeta.cz/1/";
 
     public ViaClientHttp() {
         httpClient = new DefaultHttpClient();
         httpClient.getCredentialsProvider().setCredentials(
                 new AuthScope("api.zbozi.sbeta.cz", 80),
                 new UsernamePasswordCredentials("cvut", "pqvDd-wek2D"));
     }
     
     public ProductsResponse getProducts(String query, int offset, int limit, String criterion, String direction,
             double minPrice, double maxPrice) {
         ProductsResponse productsResponse = null;
         try {
         	
             Uri.Builder ub = Uri.parse(url).buildUpon();
            ub.path("products");
 
             
             //add parameters
             ub.appendQueryParameter("query", query);
             ub.appendQueryParameter("offset", Integer.toString(offset));
             if (limit != -1) {
                 ub.appendQueryParameter("limit", Integer.toString(limit));
             }
             if (!criterion.equals("")) {
                 ub.appendQueryParameter("criterion", criterion);
             }
             ub.appendQueryParameter("direction", direction);
             ub.appendQueryParameter("minPrice", Double.toString(minPrice));
             ub.appendQueryParameter("maxPrice", Double.toString(maxPrice));
             
 //            URI uri = ub.build();
             String url = ub.build().toString();
             System.out.println("Actual request:\n " + url + "\n");
             HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
             HttpEntity entity = response.getEntity();
 //            System.out.println(EntityUtils.toString(entity));
             Gson gson = new Gson();
             productsResponse = gson.fromJson(EntityUtils.toString(entity), ProductsResponse.class);
 
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             System.out.println(e.toString());
         } catch (IOException e) {
             // TODO Auto-generated catch block
             System.out.println(e.toString());
         }
         return productsResponse;
     }
 
 }

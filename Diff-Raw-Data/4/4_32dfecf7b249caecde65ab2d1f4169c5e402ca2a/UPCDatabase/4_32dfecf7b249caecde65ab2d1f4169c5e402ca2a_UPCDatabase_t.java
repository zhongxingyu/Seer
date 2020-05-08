 package Shopaholix.database;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 
 import android.util.Log;
 
 public class UPCDatabase {
     
 
     public static Item lookupByUPC(String upc) {
         try {
         java.net.URL url = new java.net.URL("http://www.searchupc.com/handlers/upcsearch.ashx?request_type=1&access_token=567CFFB1-26F8-4BD6-8C29-935D9324B425&upc="+upc);
          HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
          Item item = readStream(in,upc);
          urlConnection.disconnect();
          return item;
         }
         catch (IOException e) {
             return null;
         }
     }
     
     private static Item readStream(InputStream in, String upc) {
         InputStreamReader is = new InputStreamReader(in);
         BufferedReader br = new BufferedReader(is);
         String data = "";
         try {
             data = br.readLine();
             data = br.readLine();
         } catch (IOException e) {
             return null;
         }
         return toItem(data, upc);
     }
     
     private static Item toItem(String data, String upc) {
 //      String pattern = "\"(.+)\",\"(.+)\",\"(.+)\",\"(.+)\",\"(.+)\",\"(.+)\"";
         if (data == null) {
             return null;
         }
         String pattern = "\",\"";
         int index = data.indexOf(pattern);
         String name = data.substring(1,index);
         String rest = data.substring(index+3,data.length());
         String url = data.substring(index+3,rest.indexOf(pattern)+index+2);
         //Log.d("aaki",url);
         //Log.d("aaki",name);
         return new Item(upc, name, url, new ItemRatings());
     }
}

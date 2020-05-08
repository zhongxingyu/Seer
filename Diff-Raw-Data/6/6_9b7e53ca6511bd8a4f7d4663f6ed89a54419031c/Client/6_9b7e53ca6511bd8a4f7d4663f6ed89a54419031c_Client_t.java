 package org.ckan;
 
 
 import com.google.gson.Gson;
 
 
 /**
  * The primary interface to this package the Client class is responsible
  * for managing all interactions with a given connection.
  *
  * @author      Ross Jones <ross.jones@okfn.org>
  * @version     1.7
  * @since       2012-05-01
  */
 public final class Client {
 
     public class Response {
         public String success;
     }
 
     private Connection _connection = null;
 
     public Client( Connection c, String apikey ) {
         this._connection = c;
         this._connection.setApiKey(apikey);
     }
 
     protected <T> T LoadClass( Class<T> cls, String data ) {
         Gson gson = new Gson();
         return gson.fromJson(data, cls);
     }
 
     public Dataset getDatasetByName(String name) {
         String returned_json = this._connection.Post("/api/action/package_show",
                                                      "{\"id\":\"" + name + "\"}" );
 
         Client.Response r = LoadClass( Client.Response.class, returned_json);
        if ( r.success == "false" ) {

        }
 
         returned_json = returned_json.substring( returned_json.indexOf("result") + 6 );
         returned_json = returned_json.substring( returned_json.indexOf("{") );
         returned_json = returned_json.substring( 0, returned_json.length() -1  );
         return LoadClass( Dataset.class, returned_json);
     }
 
 }
 
 
 
 
 
 

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dev.xyzcraft.net.bkbw;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author Joey
  */
 public class TempbanDatabase extends MacDatabase{
 
     @Override
     JSONObject defaultDb() {
 	JSONObject defaultDb = new JSONObject();
         Collection<String> bans = new HashSet();
         try {
            defaultDb.put("whitelisted", bans);
         } catch (JSONException ex) {
             Logger.getLogger(WhitelistDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
 	return defaultDb;
     }
     public void addNewTempban(String user, String reason, Integer length) {
         
     }
     public Integer lengthRemaining(String user) {
         return 0;
     }
     public String reason(String user) {
         return "";
     }
     public boolean tempbanned(String user) {
         return false;
     }
     @Override
     String name() {
         return "tempbanDB";
     }
     
 }

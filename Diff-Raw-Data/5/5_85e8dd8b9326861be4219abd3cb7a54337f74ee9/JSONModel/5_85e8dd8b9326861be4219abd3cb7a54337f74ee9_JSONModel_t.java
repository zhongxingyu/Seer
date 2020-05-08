 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twitchapplication;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  *
  * @author Toby
  */
 public class JSONModel {
 
     private TwitchController twc;
 
     public JSONModel(TwitchController twc) {
         this.twc = twc;
     }
 
     private String readUrl(String urlString) {
         BufferedReader reader = null;
         try {
             URL url = new URL(urlString);
             URLConnection ucn = url.openConnection();
             ucn.setRequestProperty("Accept", twc.getTwitchAPIVersion());
             ucn.setRequestProperty("Client-ID", twc.getClientID());
             reader = new BufferedReader(new InputStreamReader(ucn.getInputStream()));
             StringBuilder builder = new StringBuilder();
             int read;
             char[] chars = new char[1024];
             while ((read = reader.read(chars)) != -1) {
                 builder.append(chars, 0, read);
             }
 
             return builder.toString();
         } catch (Exception ex) {
             twc.showMessage(3, ex.getClass().getName() + ": Unable to open Twitch TV!");
             return null;
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException ex) {
                     twc.showMessage(3, ex.getClass().getName() + ": could not close HTTP stream!");
                 }
             }
         }
     }
 
     /**
      * Retrieve a list of followed channels from a user from the Twitch API
      *
      * @param user The username to get followers from.
      * @return An arraylist containing the followed users.
      */
     public ArrayList<String> getFollowers(String user) {
         String jsonstring = "";
          JSONObject jsonobj = null;
          JSONArray follows = null;
         try {
             jsonstring = readUrl("https://api.twitch.tv/kraken/users/" + user + "/follows/channels");
             jsonobj = new JSONObject(jsonstring);
             follows = jsonobj.getJSONArray("follows");
             ArrayList<String> al = new ArrayList<>();
             for (int i = 0; i < follows.length(); i++) {
                 JSONObject streamer = follows.getJSONObject(i);
                 JSONObject channel = streamer.getJSONObject("channel");
                 al.add((String) channel.get("name"));
             }
             return al;
         } catch (Exception ex) {
             System.out.println("getFollowers threw");
             ex.printStackTrace();
             System.out.println("--User was: " + user);
            System.out.println("JSON String was:" + jsonstring);
             if(jsonobj == null)
                 System.out.println("JSON object was null");
             if(follows == null)
                 System.out.println("JSON Array was null");
             if (ex.getClass().getName().contains("NullPointerException")) {
                 twc.showMessage(0, "Username not found!");
             } else {
                 twc.showMessage(3, ex.getClass().getName() + " was thrown");
             }
             twc.setLoginButton(true);
         }
         return null;
     }
 
     /**
      * Takes a list of users and generates a new arraylist with those that are
      * online.
      *
      * @param list The list of users to be checked.
      * @return Returns a filtered list (online people only).
      */
     public ArrayList<Streamer> getOnline(ArrayList<String> list) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < list.size(); i++) {
             sb.append(list.get(i));
             if (i != list.size() - 1) {
                 sb.append(",");
             }
         }
         try {
             String jsonString = readUrl("https://api.twitch.tv/kraken/streams?channel=" + sb);
             JSONObject jbo = new JSONObject(jsonString);
             JSONArray array = jbo.getJSONArray("streams");
             //int viewers = ;
             //System.out.println(viewers + "");
             
             ArrayList<Streamer> al = new ArrayList<>();
             for (int i = 0; i < array.length(); i++) {
                 JSONObject streamer = array.getJSONObject(i);
                 JSONObject channel = streamer.getJSONObject("channel");
                 Streamer addStreamer = new Streamer((String) channel.get("display_name"), true, streamer.getInt("viewers"));
                 al.add(addStreamer);
                 //al.add((String) channel.get("display_name"));
             }
             return al;
         } catch (Exception ex) {
             ex.printStackTrace();
             twc.showMessage(3, ex.getClass().getName() + ": Could not generate channel information!");
            twc.setContentPanel(0);
         }
         return null;
     }
 }

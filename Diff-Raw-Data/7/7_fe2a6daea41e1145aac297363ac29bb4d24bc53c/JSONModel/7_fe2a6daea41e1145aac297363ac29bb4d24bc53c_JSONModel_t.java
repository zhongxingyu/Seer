 package twitchapplication;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  *
  * @author Toby
  */
 public class JSONModel {
 
     private enum request {
 
         READ_FOLLOWERS, READ_ONLINE;
     }
 
     private enum result {
         WAITING,
         USERNAME_NOT_FOUND,
         TWITCH_OFFLINE,
         INTERNAL_ERROR,
         CHANNEL_OVERFLOW,
         SUCESSFUL;
     }
     
     private TwitchController twc;
     private result res;
     private final String baseURL = "https://api.twitch.tv/kraken";
     
     
     public JSONModel(TwitchController twc) {
         this.twc = twc;
         res = result.WAITING;
     }
 
     private String readUrl(String urlString) {
         BufferedReader reader = null;
         String notFound = "{\"error\":\"Not Found\",\"status\":404,\"message\":\"User does not exist\"}";
         try {
             URL url = new URL(urlString);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestProperty("Accept", twc.getTwitchAPIVersion());
             conn.setRequestProperty("Client-ID", twc.getClientID());
             if (conn.getResponseCode() == 404) {
                 res = result.USERNAME_NOT_FOUND;
                 return notFound;
             }
             reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder sb = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) {
                 sb.append(line);
             }
             reader.close();
 
             conn.disconnect();
             return sb.toString();
         } catch (Exception ex) {
             res = result.TWITCH_OFFLINE;
             return null;
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException ex) {
                     twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + ": could not close HTTP stream!");
                 }
             }
         }
     }
     
     private String decodeString(String d) {
        //Fixme: Has to decode properly, trouble with included source
        //return new StringEscapeUtils().unescapeHtml4(d);
        return d;
     }
 
     public ArrayList<Streamer> generateList(String username) {
         res = result.INTERNAL_ERROR;
         if (username == null) {
             throw new IllegalArgumentException("Username was null.");
         }
 
         res = result.WAITING;
 
         
         String followedURL =
                 baseURL + "/users/" + username + "/follows/channels?limit=" + 100;
         String followedJSONString = "";
         JSONObject followedJSONObject;
         JSONArray followedJSONArray;
         
         String streamersURL = "";
         String streamersJSONString;
         JSONObject streamersJSONObject;
         JSONArray streamersJSONArray;
         
         ArrayList<Streamer> newResultList;
         try {
  
             followedJSONString = readUrl(followedURL);
             followedJSONObject = new JSONObject(followedJSONString);
             if (followedJSONObject.has("error")) {
                 res = result.USERNAME_NOT_FOUND;
                 throw new IOException();
             }
             followedJSONArray = followedJSONObject.getJSONArray("follows");
 
 
             newResultList = new ArrayList<>();
             
             //First we generate a list of streamers, all offline
             for(int i = 0; i < followedJSONArray.length(); i++) {
                 JSONObject streamer = followedJSONArray.getJSONObject(i);
                 JSONObject channel = streamer.getJSONObject("channel");
                 
                 String channelName = (String) channel.getString("name");
                 String displayName = (String) channel.getString("display_name");
                 String gameTitle = "";
                 String streamTitle = "";
                 if (channel.get("game") != org.json.JSONObject.NULL) {
                     gameTitle = decodeString((String) channel.get("game"));
                 }
                 if (channel.get("status") != org.json.JSONObject.NULL) {
                     streamTitle = decodeString((String) channel.get("status"));
                 }
                 
                 Streamer addStreamer = new Streamer(displayName, channelName, 
                                             false, gameTitle, streamTitle, 0);
                 newResultList.add(addStreamer);
             }
             
             StringBuilder streamersQuery = new StringBuilder();
             for(int i = 0; i < newResultList.size(); i++) {
                 String appendName = newResultList.get(i).getChannelName();
                 streamersQuery.append(appendName);
                 if(i != newResultList.size() - 1){
                     streamersQuery.append(",");
                 }
             }
 
             
             streamersURL = baseURL + "/streams?channel=" + streamersQuery + "?limit=" + 100;
             streamersJSONString = readUrl(streamersURL);
             streamersJSONObject = new JSONObject(streamersJSONString);
             streamersJSONArray = streamersJSONObject.getJSONArray("streams");
             
             // now we have an array with online channels, time to mark online ones...
 
             // onlineChannelIndex => j
             // "offline"ChannelIndex => i
             for(int j = 0; j < streamersJSONArray.length(); j++){
                 JSONObject streamer = streamersJSONArray.getJSONObject(j);
                 int viewers = streamer.getInt("viewers");
                 JSONObject channel = streamer.getJSONObject("channel");
                 String channelName = channel.getString("name");
                 for(int i = 0; i < newResultList.size(); i++) {
                     if(newResultList.get(i).getChannelName().equals(channelName)){
                         newResultList.get(i).setStatus(true);
                         newResultList.get(i).setViewers(viewers);
                     }
                 }
             }
   
 
             
             
 
 /*            The following code determines how many objects there are in the array.
  *            This is going to be used to get more than just 100 streamers in a single request
  *              (if there are that many objects to be processed)
  * 
  *              Proposed is to loop until the total amount has been reached, 
  *              increasing the offset each loop.
  *                  Step 1 though: find someone with over 100 followed channels.
 */            
 //            switch (req) {
 //                case READ_FOLLOWERS:
 //                    totalElements = jsonObj.getInt("_total");
 //                    break;
 //                case READ_ONLINE:
 //                    array = jsonObj.getJSONArray("streams");
 //                    totalElements = array.length();
 //                    break;
 //            }
 //            if (totalElements > 24) { //Without a specified range the API will respond with a default number of 25 channels. Some users might have more than 25 followed channels.
 //                if (totalElements > 99) {
 //                    res = result.CHANNEL_OVERFLOW;  // Insert Fix!
 //                    throw new Exception();
 //                }
 //            }
 
             return newResultList;
         } catch (Exception ex) {
             switch (res) {
                 case TWITCH_OFFLINE:
                     twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + ": Unable to open Twitch TV!");
                     break;
 
                 case USERNAME_NOT_FOUND:
                     twc.showMessage(TwitchController.MessageState.ERROR, "Username was not found!");
                     break;
 
                 case CHANNEL_OVERFLOW:
                     twc.showMessage(TwitchController.MessageState.ERROR, "Current version only supports up to 100 online channels.");
                     break;
 
                 case INTERNAL_ERROR:
                     twc.showMessage(TwitchController.MessageState.WARNING, "Application error, please contact developer!");
                     break;
 
                 default:
                     twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + " was thrown!");
                     System.out.println("jsonString was:" + followedJSONString);
                     ex.printStackTrace();
                     break;
             }
             twc.setLoginButton(true);
             return null;
         }
     }
 }

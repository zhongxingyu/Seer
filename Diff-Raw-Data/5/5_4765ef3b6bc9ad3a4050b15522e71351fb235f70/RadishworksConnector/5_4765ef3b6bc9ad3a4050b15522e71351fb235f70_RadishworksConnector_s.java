 /*
  Copyright (C) 2012 Corey Edwards
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package org.zmonkey.beacon;
 
 import android.content.Context;
 import android.net.NetworkInfo;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import org.zmonkey.beacon.data.DataManager;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * User: corey
  * Date: 3/6/12
  * Time: 3:48 PM
  */
 public class RadishworksConnector {
 
     //public static final int APIKEY_LENGTH = 33;
     public static final String FIELD_DELIMITER = "\n";
     public static final int REQUEST_LIST_MISSIONS = 0;
     public static final int REQUEST_TEAM_NUMER = 1;
     public static final int REQUEST_TEAM_MEMBERS = 2;
     public static final int REQUEST_TEAM_TYPE = 3;
     public static final int REQUEST_TEAM_OBJECTIVES = 4;
     public static final int REQUEST_TEAM_NOTES = 5;
     public static final int REQUEST_POST_LOCATION = 6;
     public static final int REQUEST_POST_CLUE = 7;
     public static final int REQUEST_MISSION_DESC = 8;
     public static final int REQUEST_CMD_NAME = 9;
     public static final int REQUEST_CMD_LOCATION = 10;
     public static final int REQUEST_CMD_GPS = 11;
     public static final int REQUEST_RADIO_COMMAND = 12;
     public static final int REQUEST_RADIO_TACTICAL = 13;
     public static final int REQUEST_SUBJECT_LIST = 14;
     public static final String API_BASE = "https://www.radishworks.com/SearchManager/api.php?";
     public static final String API_APIKEY = "APIKey=";
     public static final String API_MISSIONID = "MissionID=";
     public static final String API_LATITUDE = "Latitude=";
     public static final String API_LONGITUDE = "Longitude=";
     public static final String API_CLUE_NAME = "ClueName=";
     public static final String API_CLUE_DESCRIPTION = "ClueDescription=";
     public static final String API_CLUE_FOUNDBY = "ClueFoundBy=";
     public static final String API_CLUE_LOCATION = "ClueLocation=";
     public static final String API_TIME = "Time=";
     public static final String API_DATE = "Date=";
     public static final String[] API_REQUESTS = {"Get=MissionList", "Get=TeamNumber", "Get=TeamMembers", "Get=TeamType",
             "Get=TeamObjectives", "Get=TeamNotes", "Post=Location", "Post=Clue",
             "Get=MissionDescription", "Get=CommandPostName", "Get=CommandPostLocation", "Get=CommandPostGPSLocation",
             "Get=RadioCommandChannel", "Get=RadioTacticalChannel", "Get=SubjectsList"
     };
 
     public static boolean apiCall(int requestId, Context context, Handler h){
         return apiCall(requestId, context, h, null);
     }
 
     public static boolean apiCall(int requestId, Context context, Handler h, String parameters){
         NetworkInfo network = MainActivity.connectivity.getActiveNetworkInfo();
         if (!network.isConnected()){
             return false;
         }
 
         try	{
             String uri = API_BASE + API_APIKEY +
                     PreferenceManager.getDefaultSharedPreferences(context).getString("apikey", "") +
                     "&" + API_REQUESTS[requestId];
             if (parameters != null){
                 uri = uri + "&" + parameters;
             }
             switch(requestId){
                 case REQUEST_TEAM_NUMER:
                 case REQUEST_TEAM_MEMBERS:
                 case REQUEST_TEAM_OBJECTIVES:
                 case REQUEST_TEAM_NOTES:
                 case REQUEST_TEAM_TYPE:
                 case REQUEST_POST_LOCATION:
                 case REQUEST_POST_CLUE:
                 case REQUEST_MISSION_DESC:
                 case REQUEST_CMD_NAME:
                 case REQUEST_CMD_LOCATION:
                 case REQUEST_CMD_GPS:
                 case REQUEST_RADIO_COMMAND:
                 case REQUEST_RADIO_TACTICAL:
                 case REQUEST_SUBJECT_LIST:
                     uri = uri + "&" + API_MISSIONID + Integer.toString(DataManager.data.activeMission.number);
                     break;
             }
             //Toast.makeText(getApplicationContext(), uri, Toast.LENGTH_SHORT).show();
             URL url = new URL(uri);
             URLConnection conn = url.openConnection();
             // Get the response
             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder s = new StringBuilder();
             String line;
             Message message = new Message();
             message.what = requestId;
             boolean first = true;
            while ((line = rd.readLine()) != null) {
                 if (first){
                     first = false;
                 }
                 else{
                     s.append("\n");
                 }
                 s.append(line);
             }
             line = s.toString();
             message.obj = line;
             h.sendMessage(message);
         }
         catch (Exception e)	{
             return false;
         }
 
         return true;
     }
 
     public static String apiFailure(String s){
         if (s == null || s.equals("")){
             return "No data returned";
         }
         if (s.equals("<300>")){
             return "Missing API Key";
         }
         if (s.equals("<301>")){
             return "Bad API Key";
         }
         if (s.equals("<302>")){
             return "No command";
         }
         if (s.equals("<303>")){
             return "No email address";
         }
         if (s.equals("<304>")){
             return "No permissions";
         }
         if (s.equals("<305>")){
             return "No mission selected";
         }
         if (s.equals("<306>")){
             return "No permissions to that mission";
         }
         if (s.equals("<309>")){
             return "Sorry, no missions";
         }
         if (s.equals("<310>")){
             return "Invalid Latitude Longitude";
         }
         if (s.equals("<311>")){
             return "No team assignment";
         }
         if (s.equals("<312>")){
             return "Invalid Post add value";
         }
         if (s.equals("<313>")){
             return "Invalid Get request";
         }
         if (s.equals("<314>")){
             return "Missing clue name";
         }
         if (s.equals("<315>")){
             return "Posted image is too large";
         }
 
         return null;
     }
 
 }

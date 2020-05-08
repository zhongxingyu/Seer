 package com.thoughtworks.orteroid.utilities;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class SharedData {
     public static final String PREFS_NAME = "file";
     private static JSONArray jsonArray = new JSONArray();
 
     public static JSONArray getJsonArrayOfRecentBoard() {
         return jsonArray;
     }
 
 
     private static JSONArray fixRecentBoardSize(JSONArray recentBoards) {
         if (recentBoards.length() <= 3) return recentBoards;
         JSONArray jsonArrayOfFixedSize = new JSONArray();
         try {
             jsonArrayOfFixedSize.put(recentBoards.get(0));
             jsonArrayOfFixedSize.put(recentBoards.get(1));
             jsonArrayOfFixedSize.put(recentBoards.get(2));
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return jsonArrayOfFixedSize;
     }
 
     public static void add(String boardId, String boardKey, Activity activity) {
         if (boardId.equals("2") || boardId.equals("1")) return;
 
         SharedPreferences sharedPreferences = activity.getSharedPreferences(SharedData.PREFS_NAME, 0);
         String previusRecentBoardList = sharedPreferences.getString("boards", null);
        if (previusRecentBoardList != null)
            if (previusRecentBoardList.length() != 0)
                if (isBoardAlreadyInList(boardId, previusRecentBoardList)) return;
         final JSONObject jsonObject = new JSONObject();
         try {
             jsonObject.put("board_name", boardKey);
             jsonObject.put("board_id", boardId);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         jsonArray = putObjectOnFirst(jsonObject);
         jsonArray = fixRecentBoardSize(jsonArray);
     }
 
     private static JSONArray putObjectOnFirst(JSONObject jsonObject) {
         JSONArray jsonArray1 = new JSONArray();
         jsonArray1.put(jsonObject);
        if(jsonArray != null)
        for (int index = 0; index < jsonArray.length(); index++) {
            try {
                jsonArray1.put(jsonArray.getJSONObject(index));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
         return jsonArray1;
     }
 
     private static boolean isBoardAlreadyInList(String boardId, String previusRecentBoardList) {
         final String[] recentBoardId = JSONParser.parseStringToRecentBoardsId(previusRecentBoardList);
         for (String id : recentBoardId) {
             if (boardId.equals(id)) return true;
         }
         return false;
     }
 }

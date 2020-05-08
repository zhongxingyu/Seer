 package com.codeminders.inotes.imap;
 
 import android.util.Log;
 import com.codeminders.inotes.Constants;
 import com.codeminders.inotes.model.Note;
 import org.json.JSONObject;
 
 import javax.mail.Message;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class HeaderUtils {
 
     public static String INOTES_ID_HEADER = "X-Inotes-Unique-Identifier";
 
     enum AppleHeaders {
         IDENTIFIER("X-Universally-Unique-Identifier"),
         NOTE_TYPE("X-Uniform-Type-Identifier"),
         CREATED_DATE("X-Mail-Created-Date"),
        DEFAULT_NOTE_TYPE("com.apple.mail-note");
 
         private String name;
 
         private AppleHeaders(String name) {
             this.name = name;
         }
 
         public String toString() {
             return name;
         }
     }
 
     public static HashMap<String, String> getHeaders(Message message) throws Exception {
         HashMap<String, String> headers = new HashMap<String, String>();
         String[] identifier = message.getHeader(AppleHeaders.IDENTIFIER.toString());
         if (identifier != null) {
             headers.put(AppleHeaders.IDENTIFIER.toString(), identifier[0]);
         }
         String[] createdDate = message.getHeader(AppleHeaders.CREATED_DATE.toString());
         if (createdDate != null) {
             headers.put(AppleHeaders.CREATED_DATE.toString(), createdDate[0]);
         }
         headers.put(AppleHeaders.NOTE_TYPE.toString(), AppleHeaders.DEFAULT_NOTE_TYPE.toString());
 
         String[] inoteId = message.getHeader(INOTES_ID_HEADER);
         if (inoteId != null) {
             headers.put(INOTES_ID_HEADER, inoteId[0]);
         }
 
         return headers;
     }
 
     public static void addDefaultHeader(Note note) {
         note.getHeaders().put(AppleHeaders.NOTE_TYPE.toString(), AppleHeaders.DEFAULT_NOTE_TYPE.toString());
     }
 
     public static Map<String, String> getHeaders(String string) {
         Map<String, String> headers = new HashMap<String, String>();
         try {
             JSONObject jsonObject = new JSONObject(string);
             Iterator iterator = jsonObject.keys();
             while (iterator.hasNext()) {
                 String key = (String) iterator.next();
                 headers.put(key, jsonObject.getString(key));
             }
         } catch (Exception e) {
             Log.e(Constants.TAG, e.getMessage());
         }
 
         return headers;
     }
 
 }

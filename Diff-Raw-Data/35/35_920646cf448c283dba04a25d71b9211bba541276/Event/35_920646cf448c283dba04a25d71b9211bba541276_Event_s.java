 package event;
 
 import java.util.HashMap;
 import exception.TivooEventKeywordNotFound;
 
 
 public class Event
 {
 
     private HashMap<String, String> myFields = new HashMap<String, String>();
 
     public static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
 
 
     public Event (HashMap<String, String> map)
     {
         myFields = map;
     }
 
 
     public String get (String key)
     {
         String toReturn = myFields.get(key);
         if (toReturn == null)
         {
            throw new TivooEventKeywordNotFound(key);
         }
         else
         {
             return toReturn;
         }
     }
 
 
     public boolean containsKeyword (String key, String keyword)
     {
         return get(key).contains(keyword);
     }
 
 
     public boolean containsKeywordInAllFields (String keyword)
     {
         for (String s : myFields.values())
         {
             if (s != null && s.contains(keyword))
             {
                 return true;
             }
         }
         return false;
     }
 
 }

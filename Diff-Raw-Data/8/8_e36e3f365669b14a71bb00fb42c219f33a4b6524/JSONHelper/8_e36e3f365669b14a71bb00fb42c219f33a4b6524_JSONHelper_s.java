 package edu.thu.cslab.footwith.server;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: roselone
  * Date: 3/16/13
  * Time: 1:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class JSONHelper {
 
     public static final JSONHelper JSONHelperInstance=new JSONHelper();
 
     public static JSONHelper getJSONHelperInstance(){
         return JSONHelperInstance;
     }
 
     public String convertToString(Vector<Integer> parts){
         String result=null;
         JSONArray array=new JSONArray();
         for (int i=0;i<parts.size();i++){
             array.put(parts.get(i));
         }
         return array.toString();
     }
 
     public Vector<Integer> convertToArray(String s) throws JSONException {
         if (s ==null || s.length()==0 || s.equals("null")) return null;
         JSONArray array=new JSONArray(s);
         Vector<Integer> result=new Vector<Integer>();
         for (int i=0;i<array.length();i++) result.add(array.getInt(i));
         return result;
     }
 
     public String addToArray(String s, int one) throws JSONException {
         Vector<Integer> tmp;
         if (s == null || s.length()==0 || s.equals("null")) {
             tmp=new Vector<Integer>();
         }else{
             tmp=convertToArray(s);
         }
         tmp.add(one);
         return convertToString(tmp);
     }
 
     public String deleteFromArray(String s, int one) throws JSONException {
         if (s == null || s.length()==0 || s.equals("null")) return null;
         Vector<Integer> tmp=convertToArray(s);
         tmp.remove(tmp.indexOf(one));
         return convertToString(tmp);
     }
 
 }

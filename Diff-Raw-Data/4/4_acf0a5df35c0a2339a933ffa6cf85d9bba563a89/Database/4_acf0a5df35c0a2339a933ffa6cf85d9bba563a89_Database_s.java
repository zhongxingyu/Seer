 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CSV.main;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 /**
  *
  * @author kdsweenx
  */
 public class Database {
     public static final int COMMA=0;
     public static final int TAB=1;
     public static final int SPACE=2;
     public static final int SEMICOLON=3;
     
     HashMap<Integer, HashMap<Integer, Object>> data;
     
     public Database(){
         data= new HashMap<Integer, HashMap<Integer, Object>>();
     }
     
     public void put(Object v, int row, int col){
         try{
             data.get(row).put(col, v);
         }catch(NullPointerException npe){
            Hashtable<Integer, Object> r=new Hashtable<Integer, Object>();
             r.put(col, v);
         }
     }
     
     public Object get(int row, int col){
         try{
             return data.get(row).get(col);
         }catch(Exception e){
             e.printStackTrace();
             return null;
         }
     }
 
     public HashMap<Integer, HashMap<Integer, Object>> getData(){
         return data;
     }
 }

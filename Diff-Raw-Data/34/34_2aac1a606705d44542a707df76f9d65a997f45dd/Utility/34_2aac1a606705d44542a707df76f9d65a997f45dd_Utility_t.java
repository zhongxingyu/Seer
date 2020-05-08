 package oscar.util;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import org.joda.time.Days;
import org.joda.time.LocalDate;
 
 /**
  *
  * @author sujan
  */
 public class Utility {
 
     /**
      * 
      * @param ob
      * @return
      */
     public static HashMap<String, String> convertToHashMap(Object ob) {
         Class currentClass = ob.getClass();
         return hashMap(currentClass, ob);
     }
 
     private static HashMap<String, String> hashMap(Class currentClass, Object ob) {
         Method method = null;
         Field[] fields = currentClass.getDeclaredFields();
         HashMap<String, String> objHashMap = new HashMap<String, String>();
         for (Field f : fields) {
 
             if (!Modifier.isStatic(f.getModifiers())) {
                 // switch(g.get)
                 String type = f.getType().getSimpleName();
                 //System.out.println("get"+ucFirst(f.getName()));
                 try {
                     // System.out.println(type);
                     if (type.equals("String")
                             || type.equals("boolean")
                             || type.equals("float")
                             || type.equals("int")
                             /*|| f.getClass().getSimpleName().equals("CarStatus")*/) {
 
                         method = currentClass.getMethod("get" + ucFirst(f.getName()), new Class[]{});
                         Object obj = method.invoke(ob, new Object[]{});
 
                         if (type.equals("boolean")) {
                             String value = "0";
                             if ((Boolean) obj == true) {
                                 value = "1";
                             }
                             objHashMap.put(f.getName(), value);
                         /*} else if (f.getClass().getSimpleName().equals("CarStatus")) {
                             objHashMap.put(f.getName(), Enum.valueOf(CarStatus)
                             obj.toString()
                           
                             );*/
                         } else {
                             if (obj != null) {
                                 objHashMap.put(f.getName(), obj.toString());
                             }
                         }
                     }
                 } catch (Exception ex) {
                     Logger.getLogger(currentClass.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         }
         return objHashMap;
     }
 
     /**
      * 
      * @param ob
      * @return
      */
     public static HashMap<String, HashMap<String, String>> convertToHashMapWithParent(Object ob) {
         Class currentClass = ob.getClass();
         Class parentClass = currentClass.getSuperclass();
         HashMap<String, HashMap<String, String>> allHashMaps = new HashMap<String, HashMap<String, String>>();
         allHashMaps.put(currentClass.getName(), hashMap(currentClass, ob));
         allHashMaps.put(parentClass.getName(), hashMap(parentClass, ob));
         return allHashMaps;
     }
 
     /**
      * 
      * @param hashMap
      * @param ob
      */
     public static void fillDependent(HashMap<String, HashMap<String, String>> hashMap, Object ob) {
 
         Class currentClass = ob.getClass();
         Method method = null;
         for (String key : hashMap.keySet()) {
 
             HashMap<String, String> record = hashMap.get(key);
             for (String recordKey : record.keySet()) {
 
                 try {
                     method = currentClass.getMethod("set" + ucFirst(recordKey), new Class[]{String.class});
                     method.invoke(ob, new Object[]{hashMap.get(recordKey)});
 
                 } catch (Exception ex) {
                     Logger.getLogger(currentClass.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         }
     }
 
     /**
      * 
      * @param hashMap
      * @param ob
      */
     public static void fill(HashMap<String, String> hashMap, Object ob) {
         if(hashMap==null)
             return;
         Class currentClass = ob.getClass();
         Method method = null;
         for (String key : hashMap.keySet()) {
             try {
                 method = currentClass.getMethod("set" + ucFirst(key), new Class[]{String.class});
                 method.invoke(ob, new Object[]{hashMap.get(key)});
 
             } catch (Exception ex) {
                 ex.getMessage();
                 //Logger.getLogger(currentClass.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * 
      * @param str
      * @return
      */
     public static String ucFirst(String str) {
         char[] stringArray = str.toCharArray();
         stringArray[0] = Character.toUpperCase(stringArray[0]);
         return new String(stringArray);
     }
 
     /**
      * 
      * @param password
      * @return
      * @throws NoSuchAlgorithmException
      */
     public static String encodeSHA256(String password) throws NoSuchAlgorithmException {
 
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(password.getBytes());
         byte byteData[] = md.digest();
         //convert the byte to hex
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < byteData.length; i++) {
             sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
     }
     
     /**
      * 
      * @return
      */
     public static String generateReferenceKey(){
         String referenceKey = "";
         try {
             String time = encodeSHA256(System.currentTimeMillis()+"");
             referenceKey = time.substring(0, 10);
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
         }
         return referenceKey;
     }
     
     /**
      * 
      * @param d2
      * @param d1
      * @return
      */
     public static int dateDifference(Date d2,Date d1){
         
        LocalDate start = new LocalDate(d1);
        LocalDate end = new LocalDate(d2);
        Days days = Days.daysBetween(end, start);
        return days.getDays();
     }
 }

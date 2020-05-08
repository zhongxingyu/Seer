 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Models;
 
import java.sql.Timestamp;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 /**
  *
  * @author Shehan Tis
  */
 public class UniqueKeyGenerator {
    
    public long generateNewKey(){
         Date date;
         date = new Date();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmssSSS");
         String formattedDate = sdf.format(date);
         return Long.parseLong(formattedDate);
     }
 }

 package dk.frv.eavdam.utils;
 
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.io.XMLExporter;
 import dk.frv.eavdam.io.XMLImporter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.util.Calendar;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 
 public class DBHandler {
         
     public static EAVDAMData getData() {        
         try {
            ArrayList<EAVDAMData> data = new DerbyDBInterface().retrieveAllEAVDAMData();
             if (data != null && !data.isEmpty()) {
                 return data.get(0);
             } else {
                 return null;
             }
         } catch (Exception e) {
             System.out.println(e.getMessage());
             return null;
         }
     }
 
     public static void saveData(EAVDAMData data) {
        new DerbyDBInterface().insertEAVDAMData(data);
     }
     
 }
         

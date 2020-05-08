 package com.crimezone.sd.server.logic;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import com.crimezone.sd.server.domain.AverageIncidentNumber;
 import com.crimezone.sd.server.domain.Incident;
 import com.crimezone.sd.server.persistence.CrimeDataStore;
 
 /**
  * Class to populate the datastore with crime data and averages
  * 
  * @author Amy
  * 
  */
 public class CrimeDataLoader {
   private static final int DATE_INDEX = 0;
   private static final int TIME_INDEX = 1;
   private static final int BCC_INDEX = 2;
   private static final int ADDRESS_INDEX = 3;
   private static final int LATITUDE_INDEX = 4;
   private static final int LONGITUDE_INDEX = 5;
   private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
 
   private static final Logger log = Logger.getLogger(CrimeDataReader.class.getName());
 
   private CrimeDataStore crimeDao;
 
   // Private constructor prevents instantiation from other classes
   private CrimeDataLoader() {
     crimeDao = new CrimeDataStore();
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance()
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder {
     public static final CrimeDataLoader instance = new CrimeDataLoader();
   }
 
   public static CrimeDataLoader getInstance() {
     return SingletonHolder.instance;
   }
 
   public void insertIncidents(BufferedReader incidentFile) {
     // parse byte array and for every row, create a new Incident and persist
     String line;
     try {
       line = incidentFile.readLine();
       SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
       SimpleDateFormat yearformat = new SimpleDateFormat("yyyy");
       while (line != null) {
         try {
           Incident incident = new Incident();
           String[] fields = line.split(",");
           String dateString = fields[DATE_INDEX] + " " + fields[TIME_INDEX];
           Date date = dateFormat.parse(dateString);
           incident.setYear(Integer.valueOf(yearformat.format(date)));
 
           incident.setAddress(fields[ADDRESS_INDEX]);
           incident.setBccCode(fields[BCC_INDEX]);
          incident.setLatitude(new Double(fields[LATITUDE_INDEX]));
          incident.setLongitude(new Double(fields[LONGITUDE_INDEX]));
           crimeDao.updateIncident(incident);
         } catch (ParseException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
           continue;
         } catch (Exception e) {
           e.printStackTrace();
           continue;
         }
       }
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   public void deleteAllIncidents() {
     crimeDao.deleteAllIncidents();
   }
 
   public Incident updateIncident(Incident incident) {
     return crimeDao.updateIncident(incident);
   }
 
   public void deleteIncident(Incident incident) {
     crimeDao.deleteIncident(incident.getId());
   }
 
   /**
    * Populate the database with incident averages by radius and year for the
    * entire city
    */
   public void calculateIncidentAverages() {
     // TODO: logic for calculating averages goes here
     AverageIncidentNumber average = new AverageIncidentNumber();
   }
 }

 package com.sf.checkinactivity;
 
 import com.perforce.p4java.core.IChangelist;
 import com.perforce.p4java.core.IChangelistSummary;
 import com.perforce.p4java.core.file.IFileSpec;
 import com.sun.org.apache.xerces.internal.parsers.DOMParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Hello world!
  */
 public class Checkins {
     private static final Logger LOGGER = Logger.getLogger(Checkins.class.getName());
 
     public enum DAYS_OF_WEEK {
         SUNDAY("Sunday"), MONDAY("Monday"), TUESDAY("Tuesday"), WEDNESDAY("Wednesday"), THURSDAY("Thursday"), FRIDAY("Friday"), SATURDAY("Saturday");
 
         private String day;
 
         private DAYS_OF_WEEK(final String day) {
             this.day = day;
         }
         @Override
         public String toString() {
             return day;
         }
     }
     private Map<Calendar, Map<String, List<String>>> dateMap = new HashMap<Calendar, Map<String, List<String>>>();
     private Map<DAYS_OF_WEEK, Map<String, List<List<String>>>> detailedCheckinPerDevPerDayOfWeek = new HashMap<DAYS_OF_WEEK, Map<String, List<List<String>>>>();
     private Map<DAYS_OF_WEEK, Integer> numberOfCheckinsPerDayOfWeekPerDev = new HashMap<DAYS_OF_WEEK, Integer>();
     private Set<String> developers = new TreeSet<String>();
 
     public void parse(String fileName) throws IOException, SAXException, ParseException {
         parse(fileName, null, null);
     }
 
     /**
      * Parse an xml file that has the checkins for the given date range.  Null for start date is a date in 1995, and null for end date is *now*.
      * @param fileName code_swarm generated file to analyze.
      * @param startDate on or after this midnight of the startDate PT.
      * @param endDate specification of an end date will be everything before midnight of the end date in PT.
      * @throws IOException
      * @throws SAXException
      * @throws ParseException
      */
     public void parse(String fileName, String startDate, String endDate) throws IOException, SAXException, ParseException {
         File file = new File(fileName);
         if (file.exists()) {
             SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
             // 1995
             Date start = startDate != null ? formatter.parse(startDate) : new Date(814180700000L);
             System.err.println(start);
             Date end = endDate != null ? formatter.parse(endDate) : new Date();
             System.err.println(end);
 
             DOMParser parser = new DOMParser();
             parser.parse(file.toString());
             Document doc = parser.getDocument();
             NodeList root = doc.getChildNodes();
             if (root != null && root.getLength() > 0) {
                 NodeList children = root.item(0).getChildNodes();
                 if (children != null) {
                     for (int i = 0; i < children.getLength(); i++) {
                         NamedNodeMap attributes = children.item(i).getAttributes();
                         if (attributes != null) {
 
                             Node date = attributes.getNamedItem("date");
                             if (date != null) {
                                 Long timeInMillis = Long.valueOf(date.getNodeValue());
                                 if (timeInMillis >= start.getTime() && timeInMillis <= end.getTime()) {
                                     Calendar cal = Calendar.getInstance();
                                     cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                                     cal.setTimeInMillis(timeInMillis);
                                     LOGGER.log(Level.INFO, "Day: " + cal.get(Calendar.DAY_OF_WEEK));
                                     Map<String, List<String>> devs = dateMap.get(cal);
                                     if (devs == null) {
                                         devs = new HashMap<String, List<String>>();
                                         dateMap.put(cal, devs);
                                     }
                                     String developer = attributes.getNamedItem("author").getNodeValue();
                                     developers.add(developer);
                                     List<String> filesForDev = devs.get(developer);
                                     if (filesForDev == null) {
                                         filesForDev = new ArrayList<String>();
                                         devs.put(developer, filesForDev);
                                     }
                                     filesForDev.add(attributes.getNamedItem("filename").getNodeValue());
                                 }
                             }
                         }
                     }
                     bucketToDayOfWeekPerDev();
                     bucketTotalPerDay();
                 }
             }
         } else {
             throw new FileNotFoundException("File does not exist. :" + file.getAbsolutePath());
         }
     }
 
 
    public void p4(List<String> devsToAnalyze, String startDate, String endDate) {
         try {
             final P4Checkins p4Checkins = new P4Checkins();
             p4Checkins.connect();
             SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
             final Date start = startDate != null ? formatter.parse(startDate) : new Date(814180700000L);
             final Date end = endDate != null ? formatter.parse(endDate) : new Date();
             long startTick = System.currentTimeMillis();
             ExecutorService es = Executors.newCachedThreadPool();
             for (final String dev : devsToAnalyze) {
                 es.execute(new Runnable() {
                     @Override
                     public void run() {
                         try {
 
                            List<IChangelistSummary> changelistSummaries = p4Checkins.getChangelistsForUser(dev);
                             for (IChangelistSummary changelistSummary : changelistSummaries) {
                                 Date date = changelistSummary.getDate();
                                 if (date != null) {
                                     IChangelist changelist = p4Checkins.describeChangelist(changelistSummary);
                                     Long timeInMillis = date.getTime();
                                     if (timeInMillis >= start.getTime() && timeInMillis <= end.getTime()) {
                                         Calendar cal = Calendar.getInstance();
                                         cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                                         cal.setTimeInMillis(timeInMillis);
                                         LOGGER.log(Level.INFO, String.format("%s Day: %s", dev, dayOfWeekForCalDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)).toString()));
                                         Map<String, List<String>> devs = dateMap.get(cal);
                                         if (devs == null) {
                                             devs = new HashMap<String, List<String>>();
                                             dateMap.put(cal, devs);
                                         }
                                         String developer = changelistSummary.getUsername();
                                         developers.add(developer);
                                         List<String> filesForDev = devs.get(developer);
                                         if (filesForDev == null) {
                                             filesForDev = new ArrayList<String>();
                                             devs.put(developer, filesForDev);
                                         }
                                         for (IFileSpec iFileSpec : changelist.getFiles(true)) {
                                             filesForDev.add(iFileSpec.getDepotPath().getPathString());
                                         }
                                     }
                                 }
                             }
                         } catch (Throwable t) {
                             throw new RuntimeException(t);
                         }
                     }
                 });
             }
             es.shutdown();
             es.awaitTermination(5, TimeUnit.MINUTES);
             bucketToDayOfWeekPerDev();
             bucketTotalPerDay();
             LOGGER.info("Time to get and analyze all changelists for all developers: " + (System.currentTimeMillis() - startTick));
         } catch (Throwable t) {
             throw new RuntimeException(t);
         }
     }
 
 
 
     private DAYS_OF_WEEK dayOfWeekForCalDayOfWeek(int calDayOfWeek) {
         switch (calDayOfWeek) {
             case Calendar.SUNDAY:
                 return DAYS_OF_WEEK.SUNDAY;
             case Calendar.MONDAY:
                 return DAYS_OF_WEEK.MONDAY;
             case Calendar.TUESDAY:
                 return DAYS_OF_WEEK.TUESDAY;
             case Calendar.WEDNESDAY:
                 return DAYS_OF_WEEK.WEDNESDAY;
             case Calendar.THURSDAY:
                 return DAYS_OF_WEEK.THURSDAY;
             case Calendar.FRIDAY:
                 return DAYS_OF_WEEK.FRIDAY;
             case Calendar.SATURDAY:
             default:
                 return DAYS_OF_WEEK.SATURDAY;
         }
     }
 
 
     protected void bucketToDayOfWeekPerDev() {
         for (Calendar cal : dateMap.keySet()) {
             DAYS_OF_WEEK day_of_week = dayOfWeekForCalDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
             // convert to our enum
             Map<String, List<List<String>>> dayMap = detailedCheckinPerDevPerDayOfWeek.get(day_of_week);
             if (dayMap == null) {
                 dayMap = new HashMap<String, List<List<String>>>();
                 detailedCheckinPerDevPerDayOfWeek.put(day_of_week, dayMap);
             }
 
             Map<String, List<String>> devAtomicCheckin = dateMap.get(cal);
             for (String devName : developers) {
                 List<List<String>> checkinsForDev = dayMap.get(devName);
                 if (checkinsForDev == null) {
                     checkinsForDev = new ArrayList<List<String>>();
                     dayMap.put(devName, checkinsForDev);
                 }
                 List<String> checkins = devAtomicCheckin.get(devName);
                 if (checkins != null) {
                     checkinsForDev.add(checkins);
                 }
             }
         }
     }
 
     protected void bucketTotalPerDay() {
         for (DAYS_OF_WEEK day_of_week : detailedCheckinPerDevPerDayOfWeek.keySet()) {
             int total = 0;
             Map<String, List<List<String>>> checkinsForDev = detailedCheckinPerDevPerDayOfWeek.get(day_of_week);
             for (String dev : developers) {
                 List<List<String>> atomicCheckins = checkinsForDev.get(dev);
                 int atomicCheckinsSize = atomicCheckins != null ? atomicCheckins.size() : 0;
                 LOGGER.log(Level.INFO, day_of_week + " " + dev + ": " + atomicCheckinsSize);
                 total = total + atomicCheckinsSize;
             }
             numberOfCheckinsPerDayOfWeekPerDev.put(day_of_week, total);
         }
 
         if (LOGGER.isLoggable(Level.INFO)) {
             for (DAYS_OF_WEEK days_of_week : DAYS_OF_WEEK.values()) {
                 LOGGER.log(Level.INFO, days_of_week + ": " + totalForDay(days_of_week));
             }
         }
     }
 
     public int totalForDayForDev(DAYS_OF_WEEK day_of_week, String dev) {
         int total = 0;
         Map<String, List<List<String>>> checkinsForDev = detailedCheckinPerDevPerDayOfWeek.get(day_of_week);
         if (checkinsForDev != null) {
             List<List<String>> atomicCheckins = checkinsForDev.get(dev);
             total = atomicCheckins != null ? atomicCheckins.size() : 0;
         }
         return total;
     }
 
     public int totalForDay(DAYS_OF_WEEK day_of_week) {
         Integer total = numberOfCheckinsPerDayOfWeekPerDev.get(day_of_week);
         return (total != null ? total : 0);
     }
 
     public int totalCheckins() {
         int total = 0;
         for (DAYS_OF_WEEK day_of_week : DAYS_OF_WEEK.values()) {
             total += totalForDay(day_of_week);
         }
         return total;
     }
 
 
     public List<String> devWithFewestForDay(DAYS_OF_WEEK day_of_week) {
         Integer min =  Integer.MAX_VALUE;
         List<String> devsWithFewest = new ArrayList<String>();
         Map<String, List<List<String>>> map = detailedCheckinPerDevPerDayOfWeek.get(day_of_week);
         if (map != null) {
             for (String dev : map.keySet()) {
                 int len = map.get(dev).size();
                 if (len < min) {
                     devsWithFewest.clear();
                     devsWithFewest.add(dev);
                     min = len;
                 } else if (len == min) {
                     devsWithFewest.add(dev);
                 }
 
             }
         }
         return devsWithFewest;
     }
 
     public List<String> devWithMostForDay(DAYS_OF_WEEK day_of_week) {
         Integer max =  Integer.MIN_VALUE;
         List<String> devsWithMost = new ArrayList<String>();
         Map<String, List<List<String>>> map = detailedCheckinPerDevPerDayOfWeek.get(day_of_week);
         if (map != null) {
             for (String dev : map.keySet()) {
                 int len = map.get(dev).size();
                 if (len > max) {
                     devsWithMost.clear();
                     devsWithMost.add(dev);
                     max = len;
                 } else if (len == max) {
                     devsWithMost.add(dev);
                 }
 
             }
         }
         return devsWithMost;
     }
 
 
     public String[] getDevelopers() {
         return developers.toArray(new String[developers.size()]);
     }
 
     public Map<DAYS_OF_WEEK, Map<String, List<List<String>>>> getDetailedCheckinPerDevPerDayOfWeek() {
         return detailedCheckinPerDevPerDayOfWeek;
     }
 
 }

 package org.yaoha;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.util.Log;
 
 public class OsmNode {
     public enum shopStatus {OPEN, CLOSED, UNSET, MAYBE};
     private int ID;
     private int latitudeE6;
     private int longitudeE6;
     private Date lastUpdated;
     private HashMap<String, String> attributes;
     private HashMap<Integer, ArrayList<HourRange>> weekDayMap = new HashMap<Integer, ArrayList<HourRange>>();
     private static HashMap<String, Integer> weekDayToInt = new HashMap<String, Integer>();
     private static Pattern openingHoursPattern = Pattern.compile("[0-9]{1,2}:[0-9]{2}[-|+][0-9]{0,2}[:]{0,1}[0-9]{0,2}");
     
     static {
         weekDayToInt.put("mo", Calendar.MONDAY);
         weekDayToInt.put("tu", Calendar.TUESDAY);
         weekDayToInt.put("we", Calendar.WEDNESDAY);
         weekDayToInt.put("th", Calendar.THURSDAY);
         weekDayToInt.put("fr", Calendar.FRIDAY);
         weekDayToInt.put("sa", Calendar.SATURDAY);
         weekDayToInt.put("su", Calendar.SUNDAY);
     }
     
     public OsmNode(String ID, String latitude, String longitude) {
         this.ID = Integer.parseInt(ID);
         this.latitudeE6 = new Double(Double.parseDouble(latitude)*1e6).intValue();
         this.longitudeE6 = new Double(Double.parseDouble(longitude)*1e6).intValue();
         this.attributes = new HashMap<String, String>();
     }
     
     public OsmNode(int ID, int latitudeE6, int longitudeE6) {
         this.ID = ID;
         this.latitudeE6 = latitudeE6;
         this.longitudeE6 = longitudeE6;
         this.attributes = new HashMap<String, String>();
     }
     
     public void putAttribute(String key, String value) {
         attributes.put(key, value);
     }
     
     public String getAttribute(String key) {
         return attributes.get(key);
     }
     
     public Set<String> getKeys() {
         return attributes.keySet();
     }
     
     public int getID() {
         return ID;
     }
     
     public int getLatitudeE6() {
         return latitudeE6;
     }
     
     public int getLongitudeE6() {
         return longitudeE6;
     }
 
     public String getName() {
         return getAttribute("name");
     }
 
     public void setName(String name) {
         putAttribute("name", name);
     }
 
     public String getAmenity() {
         return getAttribute("amenity");
     }
 
     public void setAmenity(String amenity) {
         putAttribute("amenity", amenity);
     }
 
     public String getOpening_hours() {
         return getAttribute("opening_hours");
     }
 
     public void setOpening_hours(String opening_hours) {
         putAttribute("opening_hours", opening_hours);
     }
 
     public Date getLastUpdated() {
         return lastUpdated;
     }
 
     public void setLastUpdated(Date lastUpdated) {
         this.lastUpdated = lastUpdated;
     }
     
     public void parseOpeningHours() {
         String openingHoursString = this.getOpening_hours();
         // If the node doesn't have a key 'opening_hours', the weekDayMap is to remain null
         if (openingHoursString == null) return;
         openingHoursString = openingHoursString.toLowerCase();
         
         // Split the string into day ranges (i.e. Mo-Fr, Sa-Su etc) and don't
         // care if the semi colon has a trailing space or not (regex)
         String[] ohComponents = openingHoursString.split("[;][ ]{0,1}");
 
         // Process every single oh component (d'uh)
         for (int i = 0; i < ohComponents.length; i++) {
 
             if (ohComponents[i].substring(0, 1).matches("[mtwfs]")) {
                 // For all hour ranges 'hh:mm-hh:mm' of one day, create a list
                 // of HourRanges. To do that, split the current oh comp in week
                 // day components. First field contains the week days, second
                 // field contains one or more hour ranges
                 String[] wdComponents = ohComponents[i].split(" ");
                 ArrayList<HourRange> hours = null;
                 if (wdComponents.length > 1) {
                     // Split all hour ranges in seperate ones
                     String[] hoursString = wdComponents[1].split(",");
                     hours = new ArrayList<HourRange>();
                     for (int j = 0; j < hoursString.length; j++) {
                         // Only create HourRange object, if string matches
                         // certain
                         // regex pattern, because HourRange doesn't have any
                         // sanity
                         // checks
                         Matcher regularOpeningHoursMatcher = openingHoursPattern.matcher(hoursString[j]);
                         if (regularOpeningHoursMatcher.matches()) {
                             hours.add(new HourRange(hoursString[j]));
                         }
                     }
                 }
 
                 // Split the week days (if they're a range)
                 if (wdComponents[0].contains("-")) {
                     String weekDays[] = wdComponents[0].split("-");
                     int start, end;
                     // TODO extremely broken, fix later :D
                     Integer bla = weekDayToInt.get(weekDays[0]);
                     if (bla == null) {
                         Log.d(getClass().getSimpleName(), "output of weekDayToInt is null, crash very likely to happen... wait let me fix this the dirty way");
                         String _weekDays[] = weekDays[0].split(",");
                         bla = weekDayToInt.get(_weekDays[0]);
                     }
                     start = end = bla;
                     // If we have a range of week days, adjust 'end'
                     if (weekDays.length == 2) {
                        end = weekDayToInt.get(weekDays[1]);
                     }
                     // For every week day in the range, assign the list of hour
                     // ranges
                     // to the appropriate int key of the weekDayMap
                     if (end < start) {
                         int tmp = end;
                         end = start;
                         start = tmp;
                     }
                     for (int j = start; j <= end; j++) {
                         weekDayMap.put(j, hours);
                     }
                 }
                 else if (wdComponents[0].contains(",")) {
                     String weekDays[] = wdComponents[0].split(",");
                     for (String day : weekDays) {
                         weekDayMap.put(weekDayToInt.get(day), hours);
                     }
                 }
                 else {
                     weekDayMap.put(weekDayToInt.get(wdComponents[0]), hours);
                 }
             }
             else if (ohComponents[i].substring(0, 1).matches("[0-9]")) {
                 String[] hoursString = ohComponents[i].split(",");
                 ArrayList<HourRange> hours = new ArrayList<HourRange>();
                 
                 // If the <node> is open 24 hours a day, 7 times a week, put midnight
                 // to midnight for every day in the week
                 if (hoursString[0].equals("24/7")) {
                     HourRange hr = new HourRange(0, 0, 23, 59);
                     hours.add(hr);
                 }
                 else {
                     for (int j = 0; j < hoursString.length; j++) {
                         Matcher regularOpeningHoursMatcher = openingHoursPattern
                                 .matcher(hoursString[j]);
                         if (regularOpeningHoursMatcher.matches()) {
                             hours.add(new HourRange(hoursString[j]));
                         }
                     }
                 }
                 weekDayMap.put(Calendar.MONDAY, hours);
                 weekDayMap.put(Calendar.TUESDAY, hours);
                 weekDayMap.put(Calendar.WEDNESDAY, hours);
                 weekDayMap.put(Calendar.THURSDAY, hours);
                 weekDayMap.put(Calendar.FRIDAY, hours);
                 weekDayMap.put(Calendar.SATURDAY, hours);
                 weekDayMap.put(Calendar.SUNDAY, hours);
             }
             else {
                 // TODO not parsable
             }
         }
     }
     
     public shopStatus isOpenNow() {
         // return values (for now):
         // 0 - closed
         // 1 - open
         // 2 - maybe (open end)
         // -1 not set
         if (weekDayMap == null)
             return shopStatus.UNSET;
         
         shopStatus result = shopStatus.CLOSED;
         Calendar now = Calendar.getInstance();
 
         ArrayList<HourRange> today = weekDayMap.get(now.get(Calendar.DAY_OF_WEEK));
 
         if (today == null)
             return shopStatus.CLOSED;
 
         for (int i = 0; i < today.size(); i++) {
             int nowHour = now.get(Calendar.HOUR_OF_DAY);
             int nowMinute = now.get(Calendar.MINUTE);
             HourRange curRange = today.get(i);
             if (nowHour >= curRange.getStartingHour() && nowMinute >= curRange.getStartingMinute()) {
                 if (nowHour <= curRange.getEndingHour() && nowMinute <= curRange.getEndingMinute()) {
                     result = shopStatus.OPEN;
                 }
                 else if (curRange.getEndingHour() == -1) {
                     result = shopStatus.MAYBE;
                 }
             }
         }
         return result;
     }
     
 }

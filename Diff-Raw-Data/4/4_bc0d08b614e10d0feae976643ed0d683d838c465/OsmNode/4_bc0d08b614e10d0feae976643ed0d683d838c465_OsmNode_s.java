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
     private int parseError;
     
     static {
         weekDayToInt.put("mo", Calendar.MONDAY);
         weekDayToInt.put("tu", Calendar.TUESDAY);
         weekDayToInt.put("we", Calendar.WEDNESDAY);
         weekDayToInt.put("th", Calendar.THURSDAY);
         weekDayToInt.put("do", Calendar.THURSDAY);
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
     
     public void parseOpeningHours() throws java.text.ParseException {
         parseError = 0;
         String openingHoursString = this.getOpening_hours().toLowerCase();
         if (openingHoursString.equals("24/7")) {
             ArrayList<HourRange> hours = new ArrayList<HourRange>();
             HourRange hr = new HourRange(0, 0, 23, 59);
             hours.add(hr);
             weekDayMap.put(Calendar.MONDAY, hours);
             weekDayMap.put(Calendar.TUESDAY, hours);
             weekDayMap.put(Calendar.WEDNESDAY, hours);
             weekDayMap.put(Calendar.THURSDAY, hours);
             weekDayMap.put(Calendar.FRIDAY, hours);
             weekDayMap.put(Calendar.SATURDAY, hours);
             weekDayMap.put(Calendar.SUNDAY, hours);
         }
         else {
             String[] components = openingHoursString.split("[;][ ]{0,1}");
             for (String part : components) {
                 parseComponent(part);
                 parseError += part.length();
             }
         }
     }
     
     private void parseComponent(String part) throws java.text.ParseException {
         if (part.substring(0, 1).matches("[mtwfs]")) {
             parseWeekDayRange(part);
         }
         else if (part.substring(0, 1).matches("[0-9]")) {
             parseHourDayRange(part);
         }
         else {
             throw new java.text.ParseException("Part " + part + " not parsable: Doesn't start with a weekday nor with a time.", parseError);
         }
     }
 
     private void parseHourDayRange(String part) throws java.text.ParseException {
         ArrayList<HourRange> hours = parseHours(part);
         weekDayMap.put(Calendar.MONDAY, hours);
         weekDayMap.put(Calendar.TUESDAY, hours);
         weekDayMap.put(Calendar.WEDNESDAY, hours);
         weekDayMap.put(Calendar.THURSDAY, hours);
         weekDayMap.put(Calendar.FRIDAY, hours);
         weekDayMap.put(Calendar.SATURDAY, hours);
         weekDayMap.put(Calendar.SUNDAY, hours);
     }
 
     private void parseWeekDayRange(String part) throws java.text.ParseException {
         String[] weekDayComponents = part.split(" ");
         if (weekDayComponents.length != 2) {
             throw new java.text.ParseException("Week day range " + part + " not parsable: Should contain 2 parts divided by a space.", parseError);
         }
         ArrayList<Integer> weekDays = parseDays(weekDayComponents[0]);
         ArrayList<HourRange> hours = parseHours(weekDayComponents[1]);
         
         for (int i = 0; i < weekDays.size(); i++) {
             weekDayMap.put(weekDays.get(i), hours);
         }
     }
 
     private ArrayList<HourRange> parseHours(String rawHourRange) throws java.text.ParseException {
         ArrayList<HourRange> hours = new ArrayList<HourRange>();
         String[] hourRanges = rawHourRange.split(",");
         for (String hourRange : hourRanges) {
             Matcher regularOpeningHoursMatcher = openingHoursPattern.matcher(hourRange);
             if (!regularOpeningHoursMatcher.matches()) throw new java.text.ParseException("Hour range " + hourRange + " not parsable: Doesn't match regular expression.", parseError);
             hours.add(new HourRange(hourRange));
         }
         return hours;
     }
 
     private ArrayList<Integer> parseDays(String rawDayRange) throws java.text.ParseException {
         ArrayList<Integer> weekDays = new ArrayList<Integer>();
         String[] commaSeparatedDays = rawDayRange.split(",");
         for (String commaDay : commaSeparatedDays) {
             if (commaDay.contains("-")) {
                 String[] dashSeparatedDays = commaDay.split("-");
                 if (dashSeparatedDays.length != 2) throw new java.text.ParseException("Week day range " + commaDay + " not parsable: Should contain exactly two weekdays separated by a dash.", parseError);
                 Integer firstIntWeekDay = weekDayToInt.get(dashSeparatedDays[0]);
                 if (firstIntWeekDay == null) throw new java.text.ParseException("Week day " + dashSeparatedDays[0] + " not parsable: Doesn't exist.", parseError);
                 Integer secondIntWeekDay = weekDayToInt.get(dashSeparatedDays[1]);
                 if (secondIntWeekDay == null) throw new java.text.ParseException("Week day " + dashSeparatedDays[1] + " not parsable: Doesn't exist.", parseError);
                 int i = firstIntWeekDay;
                 while (i != secondIntWeekDay) {
                     weekDays.add(i);
                     i++;
                     if (i>7) i=1;
                 }
                 weekDays.add(secondIntWeekDay);
             }
             else {
                 Integer weekDay = weekDayToInt.get(commaDay);
                 if (weekDay == null) throw new java.text.ParseException("Week day " + commaDay + " not parsable: Doesn't exist.", parseError);
                 weekDays.add(weekDay);
             }
         }
         return weekDays;
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

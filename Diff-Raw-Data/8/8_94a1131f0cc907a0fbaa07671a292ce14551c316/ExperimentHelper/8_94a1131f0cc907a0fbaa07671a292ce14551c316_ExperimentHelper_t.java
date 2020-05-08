 package org.agmip.functions;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import org.agmip.ace.util.AcePathfinderUtil;
 import org.agmip.common.Event;
 import org.agmip.common.Functions;
 import static org.agmip.common.Functions.*;
 import org.agmip.common.Functions.CompareMode;
 import static org.agmip.functions.SoilHelper.*;
 import org.agmip.util.MapUtil;
 import static org.agmip.util.MapUtil.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provide static functions for experiment data handling
  *
  * @author Meng Zhang
  * @version 0.1
  */
 public class ExperimentHelper {
 
     private static final Logger LOG = LoggerFactory.getLogger(ExperimentHelper.class);
 
     /**
      * This function will calculate the planting date which is the first date
      * within the planting window<br/> that has an accumulated rainfall amount
      * (P) in the previous n days.
      *
      * @param data The HashMap of experiment (including weather data)
      * @param eDate Earliest planting date (mm-dd or mmdd)
      * @param lDate Latest planting date (mm-dd or mmdd)
      * @param rain Threshold rainfall amount (mm)
      * @param days Number of days of accumulation
      *
      * @return An {@code ArrayList} of {@code pdate} for each year in the
      * weather data.
      */
     public static HashMap<String, ArrayList<String>> getAutoPlantingDate(HashMap data, String eDate, String lDate, String rain, String days) {
 
         Map wthData;
         ArrayList<Map> dailyData;
         ArrayList<HashMap<String, String>> eventData;
         Event event;
         Calendar eDateCal = Calendar.getInstance();
         Calendar lDateCal = Calendar.getInstance();
         int intDays;
         int duration;
         double accRainAmtTotal;
         double accRainAmt;
         int expDur;
         int startYear = 0;
         Window[] windows;
         ArrayList<String> pdates = new ArrayList<String>();
         HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
 
         // Check input dates
         if (!isValidDate(eDate, eDateCal, "-")) {
             LOG.error("INVALID EARLIST DATE:[" + eDate + "]");
             return new HashMap<String, ArrayList<String>>();
         }
         if (!isValidDate(lDate, lDateCal, "-")) {
             LOG.error("INVALID LATEST DATE:[" + lDate + "]");
             return new HashMap<String, ArrayList<String>>();
         }
         if (eDateCal.after(lDateCal)) {
             lDateCal.set(Calendar.YEAR, lDateCal.get(Calendar.YEAR) + 1);
         }
         try {
             duration = Integer.parseInt(convertMsToDay(lDateCal.getTimeInMillis() - eDateCal.getTimeInMillis()));
         } catch (Exception e) {
             duration = 0;
         }
         // Check Number of days of accumulation
         try {
             intDays = Integer.parseInt(days);
         } catch (Exception e) {
             LOG.error("INVALID NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
             return new HashMap<String, ArrayList<String>>();
         }
         if (intDays <= 0) {
             LOG.error("NON-POSITIVE NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
             return new HashMap<String, ArrayList<String>>();
         }
 
         // Check Threshold rainfall amount
         try {
             accRainAmtTotal = Double.parseDouble(rain);
         } catch (Exception e) {
             LOG.error("INVALID NUMBER FOR THRESHOLD RAINFALL AMOUNT");
             return new HashMap<String, ArrayList<String>>();
         }
         if (accRainAmtTotal <= 0) {
             LOG.error("NON-POSITIVE NUMBER FOR THRESHOLD RAINFALL AMOUNT");
             return new HashMap<String, ArrayList<String>>();
         }
 
         // Validation for input parameters
         // Weather data check and try to get daily data
         dailyData = WeatherHelper.getDailyData(data);
         if (dailyData.isEmpty()) {
             LOG.error("EMPTY DAILY WEATHER DATA.");
             return new HashMap<String, ArrayList<String>>();
         }
 
         // Check experiment data
         // Case for multiple data json structure
         Map mgnData = getObjectOr(data, "management", new HashMap());
         eventData = getObjectOr(mgnData, "events", new ArrayList());
 
         // Remove all planting events, for now, as a default. This is because this generates new replaced planting events.
         // NOTE: This is the "safest" way to remove items during iteration.
 //        Iterator<HashMap<String, String>> iter = eventData.iterator();
 //        while (iter.hasNext()) {
 //            if (getValueOr(iter.next(), "event", "").equals("planting")) {
 //                iter.remove();
 //            }
 //        }
 
         // Check EXP_DUR is avalaible
         try {
             expDur = Integer.parseInt(getValueOr(data, "exp_dur", "1"));
         } catch (Exception e) {
             expDur = 1;
         }
 
         LOG.debug("EXP_DUR FOUND: {}", expDur);
 
         // The starting year for multiple year runs may be set with SC_YEAR.
         if (expDur > 1) {
             try {
                 startYear = Integer.parseInt(getValueOr(data, "sc_year", "").substring(0, 4));
             } catch (Exception e) {
                 LOG.warn("Invalid SC_YEAR: {}", data.get("sc_year"));
                 startYear = -99;
             }
         }
 
         LOG.debug("START YEAR: {}", startYear);
         windows = new Window[expDur];
 
 
 
         // Check if there is eventData existing
         if (eventData.isEmpty()) {
             LOG.warn("EMPTY EVENT DATA.");
 //            event = new Event(new ArrayList(), "planting");
         } else {
             event = new Event(eventData, "planting");
             // If only one year is to be simulated, the recorded planting date year will be used (if available).
             if (expDur == 1) {
                 if (event.isEventExist()) {
                     Map plEvent = event.getCurrentEvent();
                     try {
                         startYear = Integer.parseInt(getValueOr(plEvent, "date", "").substring(0, 4));
                     } catch (Exception e) {
                         LOG.warn("Invalid planting date: {}", plEvent.get("date"));
                         startYear = -99;
                     }
                 } else {
                     startYear = -99;
                 }
             }
         }
 
         int startYearIndex = getStartYearIndex(dailyData, startYear);
         // If start year is out of weather data range
         if (startYearIndex == dailyData.size()) {
             // If one year duration, then use the first year
             if (expDur == 1) {
                 startYearIndex = 0;
             } // If multiple year duration, then report error and end function
             else {
                 LOG.error("THE START YEAR IS OUT OF DATA RANGE (SC_YEAR:[" + startYear + "]) {}", startYearIndex);
                 return new HashMap<String, ArrayList<String>>();
             }
         }
 
         // Find the first record which is the ealiest date for the window in each year
         int end;
         int start = getDailyRecIndex(dailyData, eDate, startYearIndex, 0);
         for (int i = 0; i < windows.length; i++) {
             end = getDailyRecIndex(dailyData, lDate, start, duration);
             windows[i] = new Window(start, end);
             if (i + 1 < windows.length) {
                 start = getDailyRecIndex(dailyData, eDate, end, 365 - duration);
             }
         }
 
         if (windows[0].start == dailyData.size()) {
             LOG.warn("NO VALID DAILY DATA FOR SEARCH WINDOW");
 //            return new HashMap<String, ArrayList<String>>();
         }
 
         // Loop each window to try to find appropriate planting date
         for (int i = 0; i < windows.length; i++) {
 
             // Check first n days
             int last = Math.min(windows[i].start + intDays, windows[i].end);
             accRainAmt = 0;
             for (int j = windows[i].start; j < last; j++) {
 
                 try {
                     accRainAmt += Double.parseDouble(getValueOr(dailyData.get(j), "rain", "0"));
                 } catch (Exception e) {
                     continue;
                 }
                 if (accRainAmt >= accRainAmtTotal) {
                     LOG.debug("1: " + getValueOr(dailyData.get(j), "w_date", "") + " : " + accRainAmt + ", " + (accRainAmt >= accRainAmtTotal));
                     //event.updateEvent("date", getValueOr(dailyData.get(j), "w_date", ""));
                     //AcePathfinderUtil.insertValue((HashMap)data, "pdate", getValueOr(dailyData.get(j), "w_date", ""));
                     pdates.add(getValueOr(dailyData.get(j), "w_date", ""));
                     break;
                 }
             }
 
             if (accRainAmt >= accRainAmtTotal) {
                 continue;
             }
 
 //            // If the window size is smaller than n
 //            if (last > windows[i].end) {
 //                LOG.info("NO APPROPRIATE DATE WAS FOUND FOR NO." + (i + 1) + " PLANTING EVENT");
 //                // TODO remove one planting event
 //                // event.removeEvent();
 //            }
 
             // Check following days
             int outIndex = last;
             for (int j = last; j <= windows[i].end; j++) {
 
                 try {
                     accRainAmt -= Double.parseDouble(getValueOr(dailyData.get(j - intDays), "rain", "0"));
                     accRainAmt += Double.parseDouble(getValueOr(dailyData.get(j), "rain", "0"));
                 } catch (Exception e) {
                     continue;
                 }
                 if (accRainAmt >= accRainAmtTotal) {
                     LOG.debug("2:" + getValueOr(dailyData.get(j), "w_date", "") + " : " + accRainAmt + ", " + (accRainAmt >= accRainAmtTotal));
                     //event.updateEvent("date", getValueOr(dailyData.get(j), "w_date", ""));
                     //AcePathfinderUtil.insertValue((HashMap)data, "pdate", getValueOr(dailyData.get(j), "w_date", ""));
                     pdates.add(getValueOr(dailyData.get(j), "w_date", ""));
                     break;
                 }
                 outIndex++;
             }
 
             if (accRainAmt < accRainAmtTotal) {
                 String lastDay;
                 if (windows[i].end >= dailyData.size()) {
                     lastDay = getValueOr(dailyData.get(dailyData.size() - 1), "w_date", "");
                 } else {
                     lastDay = getValueOr(dailyData.get(windows[i].end), "w_date", "");
                 }
                 LOG.warn("Could not find an appropriate day to plant, using {}", lastDay);
                 pdates.add(lastDay);
             }
         }
         results.put("pdate", pdates);
         return results;
     }
 
     /**
      * This function will calculate the planting date which is the first date
      * within the planting window<br/> that has an accumulated rainfall amount
      * (P) in the previous n days. The calculation will be done then planting
      * date is missing in the valid planting event.
      *
      * @param data The HashMap of experiment (including weather data)
      * @param eDate Earliest planting date (mm-dd or mmdd)
      * @param lDate Latest planting date (mm-dd or mmdd)
      * @param rain Threshold rainfall amount (mm)
      * @param days Number of days of accumulation
      *
      * @return An {@code ArrayList} of {@code pdate} for each year in the
      * weather data.
      */
     public static HashMap<String, ArrayList<String>> getAutoFillPlantingDate(HashMap data, String eDate, String lDate, String rain, String days) {
 
         ArrayList<Map> dailyData;
         ArrayList<HashMap<String, String>> eventData;
         Event event;
         Calendar eDateCal = Calendar.getInstance();
         Calendar lDateCal = Calendar.getInstance();
         int intDays;
         int duration;
         double accRainAmtTotal;
         double accRainAmt;
         int startYear;
         ArrayList<Window> windows = new ArrayList<Window>();
         ArrayList<String> pdates = new ArrayList<String>();
         HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
 
         // Check input dates
         if (!isValidDate(eDate, eDateCal, "-")) {
             LOG.error("INVALID EARLIST DATE:[" + eDate + "]");
             return new HashMap<String, ArrayList<String>>();
         }
         if (!isValidDate(lDate, lDateCal, "-")) {
             LOG.error("INVALID LATEST DATE:[" + lDate + "]");
             return new HashMap<String, ArrayList<String>>();
         }
         if (eDateCal.after(lDateCal)) {
             lDateCal.set(Calendar.YEAR, lDateCal.get(Calendar.YEAR) + 1);
         }
         duration = (int) ((lDateCal.getTimeInMillis() - eDateCal.getTimeInMillis()) / 86400000);
 
         // Check Number of days of accumulation
         try {
             intDays = Integer.parseInt(days);
         } catch (Exception e) {
             LOG.error("INVALID NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
             return new HashMap<String, ArrayList<String>>();
         }
         if (intDays <= 0) {
             LOG.error("NON-POSITIVE NUMBER FOR NUMBER OF DAYS OF ACCUMULATION");
             return new HashMap<String, ArrayList<String>>();
         }
 
         // Check Threshold rainfall amount
         try {
             accRainAmtTotal = Double.parseDouble(rain);
         } catch (Exception e) {
             LOG.error("INVALID NUMBER FOR THRESHOLD RAINFALL AMOUNT");
             return new HashMap<String, ArrayList<String>>();
         }
         if (accRainAmtTotal <= 0) {
             LOG.error("NON-POSITIVE NUMBER FOR THRESHOLD RAINFALL AMOUNT");
             return new HashMap<String, ArrayList<String>>();
         }
 
         // Validation for input parameters
         // Weather data check and try to get daily data
         dailyData = WeatherHelper.getDailyData(data);
         if (dailyData.isEmpty()) {
             LOG.error("EMPTY DAILY WEATHER DATA.");
             return new HashMap<String, ArrayList<String>>();
         }
 
         // Check experiment data
         // Case for multiple data json structure
         Map mgnData = getObjectOr(data, "management", new HashMap());
         eventData = getObjectOr(mgnData, "events", new ArrayList());
 
         // Check if there is eventData existing and if PDATE is already available
         if (eventData.isEmpty()) {
             LOG.error("EMPTY EVENT DATA");
 //            event = new Event(new ArrayList(), "planting");
             return new HashMap<String, ArrayList<String>>();
         } else {
             event = new Event(eventData, "planting");
             if (event.isEventExist()) {
                 try {
                     startYear = Integer.parseInt(getValueOr(data, "sc_year", "").substring(0, 4));
                 } catch (Exception e) {
                     startYear = -99;
                 }
                 int startYearIndex = getStartYearIndex(dailyData, startYear);
 
                 // Find the first record which is the ealiest date for the window in each year
                 // Currently only support single planting event, so no loop hanlding here
                 int start = getDailyRecIndex(dailyData, eDate, startYearIndex, 0);
                 int end = getDailyRecIndex(dailyData, lDate, start, duration);
 //                startYearIndex = getDailyRecIndex(dailyData, eDate, end, 365 - duration);
                 Map plEvent = event.getCurrentEvent();
                 String pdate = getValueOr(plEvent, "date", "");
                 if (!pdate.equals("")) {
                     LOG.debug("Find oringal PDATE {}, NO calculation required, AUTO_PDATE() exist", pdate);
                     return new HashMap<String, ArrayList<String>>();
                 } else {
                     windows.add(new Window(start, end));
                 }
             }
         }
 
         if (windows.get(0).start == dailyData.size()) {
             LOG.warn("NO VALID DAILY DATA FOR SEARCH WINDOW");
 //            return new HashMap<String, ArrayList<String>>();
         }
 
         // Loop each window to try to find appropriate planting date
         for (int i = 0; i < windows.size(); i++) {
 
             // Check first n days
             int last = Math.min(windows.get(i).start + intDays, windows.get(i).end);
             accRainAmt = 0;
             for (int j = windows.get(i).start; j < last; j++) {
 
                 try {
                     accRainAmt += Double.parseDouble(getValueOr(dailyData.get(j), "rain", "0"));
                 } catch (Exception e) {
                     continue;
                 }
                 if (accRainAmt >= accRainAmtTotal) {
                     LOG.debug("1: " + getValueOr(dailyData.get(j), "w_date", "") + " : " + accRainAmt + ", " + (accRainAmt >= accRainAmtTotal));
                     //event.updateEvent("date", getValueOr(dailyData.get(j), "w_date", ""));
                     //AcePathfinderUtil.insertValue((HashMap)data, "pdate", getValueOr(dailyData.get(j), "w_date", ""));
                     pdates.add(getValueOr(dailyData.get(j), "w_date", ""));
                     break;
                 }
             }
 
             if (accRainAmt >= accRainAmtTotal) {
                 continue;
             }
 
             // Check following days
             int outIndex = last;
             for (int j = last; j <= windows.get(i).end; j++) {
 
                 try {
                     accRainAmt -= Double.parseDouble(getValueOr(dailyData.get(j - intDays), "rain", "0"));
                     accRainAmt += Double.parseDouble(getValueOr(dailyData.get(j), "rain", "0"));
                 } catch (Exception e) {
                     continue;
                 }
                 if (accRainAmt >= accRainAmtTotal) {
                     LOG.debug("2:" + getValueOr(dailyData.get(j), "w_date", "") + " : " + accRainAmt + ", " + (accRainAmt >= accRainAmtTotal));
                     //event.updateEvent("date", getValueOr(dailyData.get(j), "w_date", ""));
                     //AcePathfinderUtil.insertValue((HashMap)data, "pdate", getValueOr(dailyData.get(j), "w_date", ""));
                     pdates.add(getValueOr(dailyData.get(j), "w_date", ""));
                     break;
                 }
                 outIndex++;
             }
 
             if (accRainAmt < accRainAmtTotal) {
                 String lastDay;
                 if (windows.get(i).end >= dailyData.size()) {
                     lastDay = getValueOr(dailyData.get(dailyData.size() - 1), "w_date", "");
                 } else {
                     lastDay = getValueOr(dailyData.get(windows.get(i).end), "w_date", "");
                 }
                 LOG.error("Could not find an appropriate day to plant, using {}", lastDay);
                 pdates.add(lastDay);
             }
         }
         results.put("pdate", pdates);
         return results;
     }
 
     /**
      * Store a start index and end index of daily data array for a window
      */
     private static class Window {
 
         public int start;
         public int end;
 
         public Window(int start, int end) {
             this.start = start;
             this.end = end;
         }
     }
 
     /**
      * To check if the input date string is valid and match with the required
      * format
      *
      * @param date The input date string, which should comes with the format of
      * yyyy-mm-dd, the separator should be same with the third parameter
      * @param out The Calendar instance which will be assigned with input year,
      * month and day
      * @param separator The separator string used in date format
      * @return check result
      */
     private static boolean isValidDate(String date, Calendar out, String separator) {
         try {
             String[] dates = date.split(separator);
             out.set(Calendar.DATE, Integer.parseInt(dates[dates.length - 1]));
             out.set(Calendar.MONTH, Integer.parseInt(dates[dates.length - 2]));
             if (dates.length > 2) {
                 out.set(Calendar.YEAR, Integer.parseInt(dates[dates.length - 3]));
             }
         } catch (Exception e) {
             try {
                 out.set(Calendar.DATE, Integer.parseInt(date.substring(date.length() - 2, date.length())));
                 out.set(Calendar.MONTH, Integer.parseInt(date.substring(date.length() - 4, date.length() - 2)) - 1);
                 if (date.length() > 4) {
                     out.set(Calendar.YEAR, Integer.parseInt(date.substring(date.length() - 8, date.length() - 4)) - 1);
                 }
             } catch (Exception e2) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * To check if two input date string is same date with no matter about 2nd
      * input's separator
      *
      * @param date1 1st input date string with format yyyymmdd
      * @param date2 2nd input date string with format mmdd or mm-dd
      * @param separator The separator used in 2nd string
      * @return comparison result
      */
     private static boolean isSameDate(String date1, String date2, String separator) {
 
         date2 = date2.replace(separator, "");
         if (date2.equals("0229")) {
             try {
                 int year1 = Integer.parseInt(date1.substring(2, 4));
                 if (year1 % 4 != 0) {
                     return date1.endsWith("0228");
                 }
             } catch (Exception e) {
                 return false;
             }
         }
 
         return date1.endsWith(date2);
     }
 
     /**
      * Find the index of daily data array for the particular date
      *
      * @param dailyData The array of daily data
      * @param findDate The expected date
      * @param start The start index for searching
      * @param expectedDiff The default difference between start index and
      * expected index (will try this index first, if failed then start loop)
      * @return The index for the expected date, if no matching data, will return
      * the size of array
      */
     private static int getDailyRecIndex(ArrayList<Map> dailyData, String findDate, int start, int expectedDiff) {
         String date;
         if (start + expectedDiff < dailyData.size()) {
             date = getValueOr(dailyData.get(start + expectedDiff), "w_date", "");
             if (isSameDate(date, findDate, "-")) {
                 return start + expectedDiff;
             } else {
                 expectedDiff++;
                 date = getValueOr(dailyData.get(start + expectedDiff), "w_date", "");
                 if (isSameDate(date, findDate, "-")) {
                     return start + expectedDiff;
                 }
             }
         }
 
         for (int j = start; j < dailyData.size(); j++) {
             date = getValueOr(dailyData.get(j), "w_date", "");
             if (isSameDate(date, findDate, "-")) {
                 return j;
             }
         }
         return dailyData.size();
     }
 
     private static int getStartYearIndex(ArrayList<Map> dailyData, int startYear) {
         // If no starting year is provided, the multiple years will begin on the first available weather year.
         int startYearIndex;
         if (startYear == -99) {
             LOG.warn("SC_YEAR is not valid in the data set, will using first year of weather data as start year");
             startYearIndex = 0;
         } else {
             startYearIndex = dailyData.size();
             for (int i = 0; i < dailyData.size(); i++) {
                 String w_date = getValueOr(dailyData.get(i), "w_date", "");
                 if (w_date.equals(startYear + "0101")) {
                     startYearIndex = i;
                     break;
                 } else if (w_date.endsWith("0101")) {
                     i += 364;
                 }
             }
         }
         return startYearIndex;
     }
 
     /**
      * Often the total amount of fertilizer in a growing season has been
      * recorded, but no details of application dates, types of fertilizer,etc.
      * This function allows a user to specify rules for fertilizer application
      * in a region. As a result, "N" fertilizer events are added to the JSON
      * object.
      *
      * @param num Number of fertilizer applications
      * @param fecd The code for type of fertilizer added
      * @param feacd The code for fertilizer application method
      * @param fedep The depth at which fertilizer is applied (cm)
      * @param offsets The array of date as offset from planting date (days)
      * (must be paired with ptps)
      * @param ptps The array of proportion of total N added (%) (must be paired
      * with offsets, the sum must be 100%)
      * @param data The experiment data holder
      *
      * @return An {@code ArrayList} of generated {@code fertilizer event} based
      * on each planting date
      */
     public static ArrayList<HashMap<String, String>> getFertDistribution(HashMap data, String num, String fecd, String feacd, String fedep, String[] offsets, String[] ptps) {
         int iNum;
         //Map expData;
         ArrayList<Map> eventData;
         String fen_tot;
         String[] fdates;
         //Event events;
         String pdate;
         ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
 
         try {
             iNum = Integer.parseInt(num);
             if (iNum < 1) {
                 LOG.error("INPUT NUMBER OF FERTILIZER APPLICATIONS MUST BE A POSIIVE NUMBER");
                 return results;
             }
         } catch (Exception e) {
             LOG.error("INPUT NUMBER OF FERTILIZER APPLICATIONS IS NOT A NUMBERIC STRING [" + num + "]");
             return results;
         }
 
         // Check if the two input array have "num" pairs of these data
         if (iNum != offsets.length || iNum != ptps.length) {
             if (iNum > offsets.length || !compare("100", round(sum(Arrays.copyOfRange(ptps, 0, iNum)), 0), CompareMode.EQUAL)) {
                 LOG.error("THE REQUESTED NUMBER OF APPLICATION IS NOT MATCH WITH THE GIVEN OFFSET DATA");
                 return results;
             }
         } // Check if the sum of PTPs is 100%
         else if (!compare("100", round(sum(ptps), 0), CompareMode.EQUAL)) {
             LOG.error("THE SUM OF PROPORTION OF TOTAL N ADDED (%) IS NOT EQUAL TO 100%");
             return results;
         }
 
         // Check if experiment data is available
         //ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
         //if (exps.isEmpty()) {
         //    LOG.error("NO EXPERIMENT DATA.");
         //    return;
         //} else {
         //    expData = exps.get(0);
         //    if (expData.isEmpty()) {
         //        LOG.error("NO EXPERIMENT DATA.");
         //       return;
         //    } else {
         Map mgnData = getObjectOr(data, "management", new HashMap());
         eventData = getObjectOr(mgnData, "events", new ArrayList());
         //    }
 
         // Check FEN_TOT is avalaible
         try {
             fen_tot = getValueOr(data, "fen_tot", "");
         } catch (Exception e) {
             LOG.error("FEN_TOT IS INVALID");
             return results;
         }
 
         // Check planting date is avalaible
         // events = new Event(eventData, "planting");
         // if (events.isEventExist()) {
         //     pdate = getValueOr(events.getCurrentEvent(), "date", "");
         //     if (convertFromAgmipDateString(pdate) == null) {
         //         LOG.error("PLANTING DATE IS MISSING");
         //         return;
         //     }
         // } else {
         //     LOG.error("PLANTING EVENT IS MISSING");
         //     return;
         // }
         //HashMap<String, Object> dest = new HashMap<String, Object>();
         ArrayList<String> output = new ArrayList<String>();
         for (Map events : eventData) {
             if (getValueOr(events, "event", "").equals("planting")) {
                 pdate = getValueOr(events, "date", "");
                 //}
 
                 // Check input days and ptps
                 try {
                     fdates = new String[iNum];
                     for (int i = 0; i < iNum; i++) {
                         fdates[i] = dateOffset(pdate, offsets[i]);
                         if (fdates[i] == null) {
                             LOG.error("INVALID OFFSET NUMBER OF DAYS [" + offsets[i] + "]");
                             return results;
                         }
                     }
                 } catch (Exception e) {
                     LOG.error("PAIR DATA IS IN VALID [" + e.getMessage() + "]");
                     return results;
                 }
 
                 //events.setEventType("fertilizer");
                 for (int i = 0; i < iNum; i++) {
                     String feamn = round(product(fen_tot, ptps[i], "0.01"), 0);
                    if (feamn == null) {
                        LOG.error("Invalid value for FEN_TOT {} or PTP {}", fen_tot, ptps[i]);
                    }
                     output.add(String.format("%s|%s", fdates[i], feamn));
                 }
                 break;
             }
         }
         HashMap result = new HashMap();
         for (String addNew : output) {
             String[] tmp = addNew.split("[|]");
             AcePathfinderUtil.insertValue(result, "fedate", tmp[0]);
             AcePathfinderUtil.insertValue(result, "fecd", fecd);
             AcePathfinderUtil.insertValue(result, "feacd", feacd);
             AcePathfinderUtil.insertValue(result, "fedep", fedep);
            if (!tmp[1].equals("null")) {
                AcePathfinderUtil.insertValue(result, "feamn", tmp[1]);
            }
         }
         results = MapUtil.getBucket(result, "management").getDataList();
 
         return results;
     }
 
     /**
      * Organic matter applications include manure, crop residues, etc. As a
      * result, the organic matter application event is updated with missing
      * data.
      *
      * @param expData The experiment data holder
      * @param offset application date as days before (-) or after (+) planting
      * date (days)
      * @param omcd code for type of fertilizer added
      * @param omc2n C:N ratio for applied organic matter
      * @param omdep depth at which organic matter is incorporated (cm)
      * @param ominp percentage incorporation of organic matter (%)
      * @param dmr
      *
      * @return An updated {@code ArrayList} of {@code event}, the generated or
      * updated {@code Organic matter event} will be included and sorted inside
      * the {@code ArrayList}
      */
     public static ArrayList<HashMap<String, String>> getOMDistribution(HashMap expData, String offset, String omcd, String omc2n, String omdep, String ominp, String dmr) {
 
         String omamt;
         ArrayList<HashMap<String, String>> eventData;
         Event events;
         String pdate;
         String odate;
 
         // Check if experiment data is available
         // ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
         // if (exps.isEmpty()) {
         //     LOG.error("NO EXPERIMENT DATA.");
         //     return;
         // } else {
         //     Map expData = exps.get(0);
         //     if (expData.isEmpty()) {
         //         LOG.error("NO EXPERIMENT DATA.");
         //         return;
         //     } else {
 //        Map mgnData = getObjectOr(expData, "management", new HashMap());
 //        eventData = getObjectOr(mgnData, "events", new ArrayList());
         eventData = new ArrayList();
         ArrayList<HashMap<String, String>> originalEvents = MapUtil.getBucket(expData, "management").getDataList();
 //        for (int i = 0; i < originalEvents.size(); i++) {
 //            HashMap tmp = new HashMap();
 //            tmp.putAll(originalEvents.get(i));
 //            eventData.add(tmp);
 //        }
 
         //    }
         // Get the omamt from the first? OM event
         omamt = getValueOr(expData, "om_tot", "");
         if (omamt == null || omamt.equals("")) {
             LOG.debug("OM_TOT IS NOT AVAILABLE, USING OMAMT");
             Event omEvent = new Event(originalEvents, "organic_matter");
             omamt = (String) omEvent.getCurrentEvent().get("omamt");
         }
         if (omamt == null || omamt.equals("")) {
             LOG.error("NEITHER OM_TOT NOR OMAMT ARE AVAILABLE");
             return eventData;
         }
         //omamt = getValueOr(expData, "omamt", ""); // TODO will be replace by generic getting method
 
         //}
 
         // Get planting date and om_date
         events = new Event(originalEvents, "planting");
         pdate = (String) events.getCurrentEvent().get("date");
         if (pdate == null || pdate.equals("")) {
             LOG.error("PLANTING DATE IS NOT AVAILABLE");
             return eventData;
         }
         odate = dateOffset(pdate, offset);
         if (odate == null) {
             LOG.error("INVALID OFFSET NUMBER OF DAYS [" + offset + "]");
             return eventData;
         }
 
         String omnpct = divide(divide("100.0", dmr, 3), omc2n, 2);
         if (omnpct == null) {
             LOG.error("INVALID VALUES FOR DMR and OMC2N");
             return eventData;
         }
         // Update organic material event
 //        events.setEventType("organic_matter");
 //        if (events.isEventExist()) {
 //            events.updateEvent("date", odate, false);
 //            events.updateEvent("omcd", omcd, false);
 //            events.updateEvent("omamt", omamt, false);
 //            events.updateEvent("omc2n", omc2n, false);
 //            events.updateEvent("omdep", omdep, false);
 //            events.updateEvent("ominp", ominp, false);
 //            events.updateEvent("omn%", omnpct, true);
 //        }
         HashMap result = new HashMap();
         AcePathfinderUtil.insertValue(result, "omdat", odate);
         AcePathfinderUtil.insertValue(result, "omcd", omcd);
         AcePathfinderUtil.insertValue(result, "omamt", omamt);
         AcePathfinderUtil.insertValue(result, "omc2n", omc2n);
         AcePathfinderUtil.insertValue(result, "omdep", omdep);
         AcePathfinderUtil.insertValue(result, "ominp", ominp);
         AcePathfinderUtil.insertValue(result, "omn%", omnpct);
         eventData = MapUtil.getBucket(result, "management").getDataList();
         return eventData;
     }
 
     /**
      * Calculate Stable C (g[C]/100g[soil]) fraction distribution in soil layers
      * and save the result into initial condition layers
      *
      * @param data The experiment data holder
      * @param som3_0 fraction of total soil organic C which is stable, at
      * surface (fraction)
      * @param pp depth of topsoil where maximum SOM3 fraction is relatively
      * constant (cm)
      * @param rd depth at which soil C is relatively stable (~98% stable C) (cm)
      *
      * @return An {@code ArrayList} of calculated {@code SLSC} for each layer of
      * given soil
      */
     public static HashMap<String, ArrayList<String>> getStableCDistribution(HashMap data, String som3_0, String pp, String rd) {
 
         HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
         ArrayList<String> slscArr = new ArrayList();
         ArrayList<HashMap> soilLayers;
         String k;
         String som2_0;
         String f;
         String som3_fac;
         String[] sllbs;
         String[] slocs;
 //        double mid;
         String mid;
         int finalScale = 2;
 
         LOG.debug("Checkpoint 1");
         try {
             k = divide(log("0.02") + "", substract(rd, pp), finalScale + 1);
             som2_0 = multiply("0.95", substract("1", som3_0));
         } catch (Exception e) {
             LOG.error("INVALID INPUT FOR NUMBERIC VALUE");
             return results;
         }
 
         soilLayers = getSoilLayer(data);
         if (soilLayers == null) {
             return results;
         } else if (soilLayers.isEmpty()) {
             LOG.error("SOIL LAYER DATA IS EMPTY");
             return results;
         } else {
             try {
                 sllbs = new String[soilLayers.size()];
                 slocs = new String[soilLayers.size()];
                 for (int i = 0; i < soilLayers.size(); i++) {
                     sllbs[i] = getObjectOr(soilLayers.get(i), "sllb", "");
                     slocs[i] = getObjectOr(soilLayers.get(i), "sloc", "");
                 }
             } catch (NumberFormatException e) {
                 LOG.error("INVALID NUMBER FOR SLOC OR SLLB IN DATA [" + e.getMessage() + "]");
                 return results;
             }
         }
 
         LOG.debug("Checkpoint 2");
         // Check if initial condition layer data is available
         // ArrayList<Map> exps = getObjectOr(data, "experiments", new ArrayList());
         // if (exps.isEmpty()) {
         //     LOG.error("NO EXPERIMENT DATA.");
         //     return;
         // } else {
         //     Map expData = exps.get(0);
         //     if (expData.isEmpty()) {
         //         LOG.error("NO EXPERIMENT DATA.");
         //         return;
         //     } else {
         // Map icData = getObjectOr(data, "soil", new HashMap());
         // icLayers = getObjectOr(icData, "soilLayer", new ArrayList());
         // if (icLayers.isEmpty()) {
         //     LOG.error("NO SOIL DATA.");
         //     return;
         // } else if (icLayers.size() != soilLayers.size()) {
         //     LOG.error("THE LAYER DATA IN THE INITIAL CONDITION SECTION IS NOT MATCHED WITH SOIL SECTION");
         //     return;
         // }
         //     //}
         // //}
 
         LOG.debug("Checkpoint 3");
         String last = "0";
         for (int i = 0; i < soilLayers.size(); i++) {
             mid = average(sllbs[i], last);
             last = sllbs[i];
             f = getGrowthFactor(mid, pp, k, som2_0);
             som3_fac = substract("1", divide(max("0.02", f), "0.95", finalScale + 1));
             slscArr.add(round(multiply(slocs[i], som3_fac), finalScale));
 //            LOG.debug((String)icLayers.get(i).get("icbl") + ", " + (String)icLayers.get(i).get("slsc"));
         }
         results.put("slsc", slscArr);
         return results;
     }
 
     /**
      * This function will clone the original management events for each year in
      * the experiment duration. Only the date of each event will be increased
      * year by year. If experiment duration is no longer than 1 year, no
      * generation will be executed.
      *
      * @param data The HashMap of experiment (including weather data)
      *
      * @return Several groups of {@code ArrayList} of {@code Event} for each
      * year in the experiment duration. The original group of {@code Event} will
      * only be included when {@code duration} > 1.
      */
     public static ArrayList<ArrayList<HashMap<String, String>>> getAutoEventDate(Map data) {
 
         ArrayList<ArrayList<HashMap<String, String>>> results = new ArrayList<ArrayList<HashMap<String, String>>>();
 
         // Get Experiment duration
         int expDur;
         try {
             expDur = Integer.parseInt(getValueOr(data, "exp_dur", "1"));
         } catch (Exception e) {
             expDur = 1;
         }
         // If no more planting event is required
         if (expDur <= 1) {
             LOG.warn("Experiment duration is not more than 1, AUTO_REPLICATE_EVENTS won't be applied.");
             return results;
         }
 
         // Get Event date
         ArrayList<HashMap<String, String>> events = MapUtil.getBucket(data, "management").getDataList();
         while (results.size() < expDur) {
             results.add(new ArrayList());
         }
 
         Calendar cal = Calendar.getInstance();
         for (int i = 0; i < events.size(); i++) {
             HashMap<String, String> event = events.get(i);
             String date = getValueOr(event, "date", "");
             if (date.equals("")) {
                 String eventType = getValueOr(event, "event", "unknown");
                 LOG.warn("Original {} event has an invalid date: [{}].", eventType, date);
             }
             String edate = getValueOr(event, "edate", "");
             for (int j = 0; j < expDur; j++) {
                 HashMap<String, String> newEvent = new HashMap();
                 newEvent.putAll(event);
                 if (!date.equals("")) {
                     newEvent.put("date", yearOffset(date, j + ""));
                 }
                 if (!edate.equals("")) {
                     newEvent.put("edate", yearOffset(edate, j + ""));
                 }
 
                 results.get(j).add(newEvent);
             }
         }
         return results;
     }
 
     /**
      * This function will clone the original management events based on given
      * new planting dates. The generated event will keep the original days after
      * planting date (DAP).
      *
      * For example: New event date = New planting date + original DAP.
      *
      * @param data The HashMap of experiment (including weather data)
      * @param pdates The given planting dates for generation
      *
      * @return Several groups of {@code ArrayList} of {@code Event} for each
      * planting date in the experiment duration.
      */
     public static ArrayList<ArrayList<HashMap<String, String>>> getAutoEventDate(Map data, String[] pdates) {
 
         ArrayList<ArrayList<HashMap<String, String>>> results = new ArrayList<ArrayList<HashMap<String, String>>>();
 
         // If no more planting event is required
         if (pdates.length < 1) {
             LOG.error("There is no PDATE can be used for event generation.");
             return results;
         }
 
         // Get Event date
         ArrayList<HashMap<String, String>> events = MapUtil.getBucket(data, "management").getDataList();
         while (results.size() < pdates.length) {
             results.add(new ArrayList());
         }
         
         String orgPdate = getValueOr(data, "origin_pdate", "-99");
         if (orgPdate.equals("")) {
             LOG.error("The original PDATE is missing, can't calculate other event date");
         } else if (orgPdate.equals("-99")) {
             orgPdate = getFstPdate(data, "");
         }
         if (orgPdate.equals("")) {
             LOG.warn("The original PDATE is missing, use first given PDATE {} as original one", pdates[0]);
             orgPdate = pdates[0];
         } else {
             LOG.debug("Find original PDATE {}", orgPdate);
         }
         for (int i = 0; i < events.size(); i++) {
             HashMap<String, String> event = events.get(i);
             // Get days after planting for current event date
             String date = getValueOr(event, "date", "");
             String orgDap = "";
             String eventType = getValueOr(event, "event", "unknown");
             // if it is a planting event
             if (eventType.equals("planting")) {
                 orgDap = "0";
             } else {
                 if (date.equals("")) {
                     LOG.debug("Original {} event has an invalid date: [{}].", eventType, date);
                 } else {
                     orgDap = calcDAP(date, orgPdate);
                     LOG.debug("Original {} event date: [{}].", eventType, date);
                     LOG.debug("Original {} event's DAP: [{}]", eventType, orgDap);
                 }
             }
             // Special handling for edate
             String edate = getValueOr(event, "edate", "");
             String orgEDap = "";
             if (!edate.equals("")) {
                 orgEDap = calcDAP(edate, orgPdate);
                 LOG.debug("Original EDATE's DAP: [{}].", orgDap);
             }
             for (int j = 0; j < pdates.length; j++) {
                 HashMap<String, String> newEvent = new HashMap();
                 newEvent.putAll(event);
                 if (!date.equals("")) {
                     newEvent.put("date", dateOffset(pdates[j], orgDap));
                 }
                 if (!edate.equals("")) {
                     newEvent.put("edate", dateOffset(pdates[j], orgEDap));
                 }
 
                 results.get(j).add(newEvent);
             }
         }
         return results;
     }
 
     /**
      * This function allows standard paddy management inputs to be generalized
      * for a field or group of fields.
      *
      * @param data The experiment data holder
      * @param bundNum Number of Bund height entries
      * @param percRate The Percolation rate (mm/d)
      * @param plowpanDept The Plowpan depth (mm)
      * @param offsets The array of date as offset from planting date (days)
      * (must be paired with Max and Min)
      * @param maxVals The array of Max flood (bund) height (mm)
      * @param minVals The array of Min flood height (mm)
      *
      * @return An {@code ArrayList} of generated {@code irrigation event} based
      * on first planting date
      */
     public static ArrayList<HashMap<String, String>> getPaddyIrrigation(HashMap data, String bundNum, String percRate, String plowpanDept, String[] offsets, String[] maxVals, String[] minVals) {
         int iNum;
         ArrayList<HashMap<String, String>> eventData;
         String[] idates;
         Event events;
         String pdate;
         ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
 
         try {
             iNum = Integer.parseInt(bundNum);
             if (iNum < 1) {
                 LOG.error("INPUT NUMBER OF BUND HEIGHT MUST BE A POSITIVE NUMBER");
                 return results;
             }
         } catch (Exception e) {
             LOG.error("INPUT NUMBER OF FERTILIZER APPLICATIONS IS NOT A NUMBERIC STRING [" + bundNum + "]");
             return results;
         }
 
         // Check if the two input array have "num" pairs of these data
         if (iNum != offsets.length || iNum != maxVals.length || iNum != minVals.length) {
             LOG.error("THE REQUESTED NUMBER OF APPLICATION IS NOT MATCH WITH THE GIVEN OFFSET DATA");
             return results;
         }
 
         // Get original event data array
         Map mgnData = getObjectOr(data, "management", new HashMap());
         eventData = getObjectOr(mgnData, "events", new ArrayList());
 
         // Get planting date and om_date
         events = new Event(eventData, "planting");
         pdate = (String) events.getCurrentEvent().get("date");
         if (pdate == null || pdate.equals("")) {
             LOG.error("PLANTING DATE IS NOT AVAILABLE");
             return eventData;
         }
 
         // Create irrigation dates based on planting date and given offset days
         idates = new String[iNum];
         try {
             for (int i = 0; i < iNum; i++) {
                 idates[i] = dateOffset(pdate, offsets[i]);
                 if (idates[i] == null) {
                     LOG.error("INVALID OFFSET NUMBER OF DAYS [" + offsets[i] + "]");
                     return results;
                 }
             }
         } catch (Exception e) {
             LOG.error("PAIR DATA IS IN VALID [" + e.getMessage() + "]");
             return results;
         }
 
         // Generate new irrigation event
         HashMap result = new HashMap();
         // puddling, plowpan depth
         AcePathfinderUtil.insertValue(result, "idate", idates[0]);
         AcePathfinderUtil.insertValue(result, "irop", "IR010");
         AcePathfinderUtil.insertValue(result, "irval", plowpanDept);
         // percolation rate
         AcePathfinderUtil.insertValue(result, "idate", idates[0]);
         AcePathfinderUtil.insertValue(result, "irop", "IR008");
         AcePathfinderUtil.insertValue(result, "irval", percRate);
         // For each irrigation date
         for (int i = 0; i < idates.length; i++) {
             // bund height
             AcePathfinderUtil.insertValue(result, "idate", idates[i]);
             AcePathfinderUtil.insertValue(result, "irop", "IR009");
             AcePathfinderUtil.insertValue(result, "irval", maxVals[i]);
             // target minimum flood level
             AcePathfinderUtil.insertValue(result, "idate", idates[i]);
             AcePathfinderUtil.insertValue(result, "irop", "IR011");
             AcePathfinderUtil.insertValue(result, "irval", minVals[i]);
         }
         results = MapUtil.getBucket(result, "management").getDataList();
 
         return results;
     }
     
     /**
      * Get the first planting date from given data set.
      * 
      * @param data The experiment data holder
      * @param defValue The value used for return when planting date is unavailable
      * @return The planting date
      */
     public static String getFstPdate(Map data, String defValue) {
         ArrayList<HashMap<String, String>> events = getBucket(data, "management").getDataList();
         Event event = new Event(events, "planting");
         return getValueOr(event.getCurrentEvent(), "date", defValue);
     }
     
     /**
      * Event Type
      */
     public enum EventType {
 
         PLANTING, IRRIGATION, AUTO_IRRIG, FERTILIZER, TILLAGE, ORGANIC_MATTER, HARVEST, CHEMICALS, MULCH
     }
     
     /**
      * 
      * 
      * @param typeStr
      * @param date
      * @param info
      * @return 
      */
     public static HashMap<String, String> createEvent(HashMap data, String typeStr, String dap, HashMap<String, String> info) {
         HashMap newEvent = new HashMap<String, String>();
         
         EventType type;
         try {
             type = EventType.valueOf(typeStr.toUpperCase());
         } catch (IllegalArgumentException e) {
             LOG.error("{} event is not recognized, please try other event name", typeStr);
             return new HashMap<String, String>();
         } catch (Exception e) {
             LOG.error(getStackTrace(e));
             return new HashMap<String, String>();
         }
         newEvent.put("event", type.toString().toLowerCase());
         
         String pdate = getFstPdate(data, "");
         if (!pdate.equals("")) {
             String date = dateOffset(pdate, dap);
             if (date != null) {
                 newEvent.put("date", date);
             } else {
                 LOG.error("Given days after planting has a invalid value {}", dap);
                 return new HashMap<String, String>();
             }
         } else {
             LOG.error("Planting date is not available in the given data set");
             return new HashMap<String, String>();
         }
         
         newEvent.putAll(info);
         return newEvent;
     }
 }

 package ee.alkohol.juks.sirvid.containers;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import ee.alkohol.juks.sirvid.containers.ICalendar.*;
 import ee.alkohol.juks.sirvid.containers.ICalEvent;
 import ee.alkohol.juks.sirvid.exporters.ical.*;
 import ee.alkohol.juks.sirvid.math.Astronomy;
 
 public class ICalculator {
     
     public final static String[] LANG = {Keys.LANGUAGE, "ET"};
    public final static String UIDDATE = "%d%02d%02d";
     
     public InputData inputData;
     public ICalendar iCal;
     public Exporter exporter;
     public ArrayList<String> errorMsgs;
     public String timespan;
     
     public static enum DbIdStatuses {
         
         UNDEFINED (-1, ""),
         MOON_NEW_M2 (10, "\u25cf -2d"),
         MOON_NEW (11, "\u25cf"),
         MOON_NEW_P2 (12, "\u25cf +2d"),
         MOON_1ST (13, "\u263d"),
         MOON_FULL_M2 (14, "\u25ef -2d"),
         MOON_FULL (15, "\u25ef"),
         MOON_FULL_P2 (16, "\u25ef +2d"),
         MOON_LAST (17, "\u263e"),
         SOLSTICE (20, "\u2295"),
         SUNRISE (30, "\u263c"),
         SUNSET (31, "\u2600"),
         LEAPDAY (229, "");
 
         private int dbId;
         private String name;
         
         public int getDbId() {
             return dbId;
         }
         public String getName() {
             return name;
         }
         public void setName(String name) {
             this.name = name;
         }
         
         DbIdStatuses(int dbId, String name) {
             this.dbId = dbId;
             this.name = name;
         }
 
     }
     
     public ICalculator(InputData inputData) throws SQLException {
         
         this.inputData = inputData;
         
         // initialize current time
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(inputData.getDate());
         boolean isLeapYear = cal.isLeapYear(cal.get(Calendar.YEAR));
         
         // initialize period of calculation
         GregorianCalendar calendarBegin = new GregorianCalendar();
         int periodEnd = cal.get(Calendar.YEAR)*10000;
         StringBuilder calName = new StringBuilder();
         calName.append(cal.get(Calendar.YEAR));
         if(inputData.getTimespan().equals(InputData.FLAGS.PERIOD.MONTH)
         		|| inputData.getTimespan().equals(InputData.FLAGS.PERIOD.DAY)) {
         	
 		        	calName.append(String.format("-%02d", cal.get(Calendar.MONTH)+1));
 		        	if(inputData.getTimespan().equals(InputData.FLAGS.PERIOD.DAY)) { // day
 		            	calName.append(String.format("-%02d", cal.get(Calendar.DATE)));
 		            	calendarBegin.setTime(inputData.getDate());
 		            	periodEnd += (cal.get(Calendar.MONTH)+1)*100 + cal.get(Calendar.DATE);
 		            } else { // month
 		            	calendarBegin.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
 		            	periodEnd += (cal.get(Calendar.MONTH)+1)*100  + cal.getActualMaximum(Calendar.DAY_OF_MONTH);
 		            }
 		        	
         } else { // year
         	calendarBegin.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
         	periodEnd += 1231;
         }
         
         int periodStart = calendarDate2int(calendarBegin);
         this.timespan = calName.toString();
         
         // initialize calendar container
         LinkedHashMap<String,ICalProperty> initData = new LinkedHashMap<String,ICalProperty>();
         initData.put(Keys.CALENDAR_NAME, new ICalProperty("JKalender " + this.timespan, null));
         initData.put(Keys.CALENDAR_TIMEZONE, new ICalProperty(inputData.getTimezone(), null));
         iCal = new ICalendar(initData);
         
         // initialize DB connection
         DaoKalenderJDBCSqlite CalendarDAO = new DaoKalenderJDBCSqlite(inputData.jbdcConnect);
         
         HashMap<String,String> eventTranslations = new HashMap<String,String> ();
         if(CalendarDAO.dbConnection != null) {
             ResultSet eventTrRS = CalendarDAO.getEventTranslations();
             while(eventTrRS.next()) { 
             	eventTranslations.put(eventTrRS.getString("dbid"), eventTrRS.getString("name"));
             }
         }
         for (DbIdStatuses dbids : DbIdStatuses.values()) {
             String dbKey = "" + dbids.getDbId();
             if(eventTranslations.get(dbKey) == null) { eventTranslations.put(dbKey, dbids.getName()); }
         }
         
         // sunsets and sunrises
         if(inputData.isCalculateSunrisesSunsets()) {
             
         	int nextDate;
         	String coordinates = "" + inputData.getLatitude() + ";" + inputData.getLongitude();
         	String[] driverData = {
                     Astronomy.Keys.J_RISE, eventTranslations.get("" +DbIdStatuses.SUNRISE.getDbId()),
                     Astronomy.Keys.J_SET, eventTranslations.get("" +DbIdStatuses.SUNSET.getDbId())
             };
         	
         	iCal.vVenue = new LinkedHashMap<String,ICalProperty>();
         	iCal.vVenue.put(Keys.UID, new ICalProperty("a_" + inputData.getLatitude()+ "_o_" + inputData.getLongitude() + iCal.generateUID(null), null));
         	iCal.vVenue.put(Keys.GEOGRAPHIC_COORDINATES, new ICalProperty(coordinates, null));
         	
         	while(true) {
                 
                 HashMap<String,Double> results = Astronomy.gregorianSunrise(
                         Astronomy.gregorian2JDN(calendarBegin.get(Calendar.YEAR), calendarBegin.get(Calendar.MONTH)+1, calendarBegin.get(Calendar.DATE)), 
                         -inputData.getLongitude(), inputData.getLatitude()
                 );
                 
                 for(int i=0; i < driverData.length; i+=2) {
                 	
                     int[] sg = Astronomy.JD2calendarDate(results.get(driverData[i]));
                     GregorianCalendar sunCal = new GregorianCalendar();
                     sunCal.set(sg[0], sg[1]-1, sg[2], sg[3], sg[4], sg[5]);
                     
                     ICalEvent event = new ICalEvent();
                     event.dbID = driverData[i].equals(Astronomy.Keys.J_RISE) 
                             ? DbIdStatuses.SUNRISE.getDbId()
                             : DbIdStatuses.SUNSET.getDbId();
                     event.properties.put(Keys.SUMMARY, new ICalProperty(driverData[i+1], null));
                     event.properties.put(Keys.UID, 
                             new ICalProperty("d_" + 
                            		String.format(UIDDATE,
                             					sg[0],
                             					sg[1],
                             					sg[2])
                             				+ "_" +iCal.generateUID(event.dbID), null));
                     event.properties.put(Keys.EVENT_START, new ICalProperty(sunCal.getTime(), new String[]{Keys.VALUE, Values.DATETIME}));
                     // use VVENUE component instead
                     //event.properties.put(Keys.GEOGRAPHIC_COORDINATES, new ICalProperty(coordinates, null));
                     event.allDayEvent = false;
                     iCal.vEvent.add(event);
                 }
                 
         		calendarBegin.add(Calendar.DATE, 1);
         		nextDate = calendarDate2int(calendarBegin);
         		if(nextDate > periodEnd) { break; }
         	}
         	
         }
         
         // solstices
         if(inputData.isCalculateSolistices()) {
         	ArrayList<int[]> sol = new ArrayList<int[]>();
         	if(inputData.getTimespan().equals(InputData.FLAGS.PERIOD.YEAR)) {
         		for(short m = 3; m < 13; m+=3) {
         			sol.add(Astronomy.JD2calendarDate(Astronomy.solstice(cal.get(Calendar.YEAR), m, inputData.isUseDynamicTime())));
         		}
         	} else {
         	    short m = (short)(1 + cal.get(Calendar.MONTH));
         	    if(m > 2 && (m % 3 == 0)) {
         	        sol.add(Astronomy.JD2calendarDate(Astronomy.solstice(cal.get(Calendar.YEAR), m, inputData.isUseDynamicTime())));
         	    }
         	}
         	String solsticeLabel = eventTranslations.get("" +DbIdStatuses.SOLSTICE.getDbId());
         	for(int[] solistice : sol) {
         	    GregorianCalendar solCal = new GregorianCalendar();
                 solCal.set(solistice[0], solistice[1]-1, solistice[2], solistice[3], solistice[4], solistice[5]);
                 ICalEvent event = new ICalEvent();
                 event.dbID = DbIdStatuses.SOLSTICE.getDbId();
                 event.properties.put(Keys.SUMMARY, new ICalProperty(solsticeLabel, null));
                event.properties.put(Keys.UID, new ICalProperty("m_" + solistice[0] + "-" + solistice[1] + iCal.generateUID(event.dbID), null));
                 event.properties.put(Keys.EVENT_START, new ICalProperty(solCal.getTime(), new String[]{Keys.VALUE, Values.DATETIME}));
                 event.allDayEvent = false;
                 iCal.vEvent.add(event);
         	}
         }
         
         // nothing to do further, if there is no DB connection
         if(CalendarDAO.dbConnection == null) { return; }
         
         // if calendar dates are required
         if(!inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.NONE)) {
         	
 	        String[] nameFields = inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA)
 	        	? new String[]{"maausk"}
 	        	: new String[]{"event","maausk"};
 	        String nameDelimiter = "; ";
 	        
 	        // anniversaries
 	        ResultSet anniversaries = CalendarDAO.getAnniversaries(periodStart%10000, periodEnd%10000,
 	        		inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA));
 	        if(anniversaries != null) {
 	            while(anniversaries.next())
 	            {
 	              int dbID = anniversaries.getInt("id");
 	              if(dbID == DbIdStatuses.LEAPDAY.getDbId() && !isLeapYear) { continue; }
 	              
 	              ICalEvent event = new ICalEvent();
 	              event.dbID = dbID;
 	              
 	              event.properties.put(Keys.SUMMARY, new ICalProperty(generateEstonianDayName(anniversaries, nameFields, nameDelimiter), LANG));
 	              event.properties.put(Keys.UID, new ICalProperty(iCal.generateUID(dbID), null));
 	              event.properties.putAll(generateAllDayEvent(cal.get(Calendar.YEAR), (int)(dbID / 100), dbID % 100));
 	              String descr = anniversaries.getString("more");
 	              if(isNotEmptyStr(descr)) {
 	                  event.properties.put(Keys.DESCRIPTION, new ICalProperty(descr, LANG));
 	              }
 	              // obvious
 	              //if(dbID != DbIdStatuses.LEAPDAY.getDbId()) { event.properties.put("rrule", new ICalProperty("FREQ=YEARLY", null));}
 	              
 	              iCal.vEvent.add(event);
 	            }
 	        }
         
         }
         
     }
     
     public void initExport() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
         @SuppressWarnings("rawtypes")
         Class clazz = Class.forName("ee.alkohol.juks.sirvid.exporters.ical.Exporter"+inputData.getOutputFormat().toUpperCase());
         exporter = (Exporter)clazz.newInstance();
     }
     
     // helpers
     public static int calendarDate2int(GregorianCalendar cal) {
     	return cal.get(Calendar.YEAR)*10000 + (cal.get(Calendar.MONTH)+1)*100 + cal.get(Calendar.DATE);
     }
     
     public static LinkedHashMap<String,ICalProperty> generateAllDayEvent(int year, int month, int date) {
         LinkedHashMap<String,ICalProperty> y = new LinkedHashMap<String,ICalProperty>();
         GregorianCalendar cal = new GregorianCalendar();
         cal.set(year, month-1, date, 0, 0, 0);
         y.put(Keys.EVENT_START, new ICalProperty(cal.getTime(), new String[]{Keys.VALUE,Values.DATE}));
         cal.add(Calendar.DATE, 1);
         y.put(Keys.EVENT_END, new ICalProperty(cal.getTime(), new String[]{Keys.VALUE,Values.DATE}));
         return y;
     }
     
     public static boolean isNotEmptyStr(String str) {
         return str != null && !str.trim().equals("");
     }
     
     public static String generateEstonianDayName(ResultSet eventRow, String[] nameFields, String nameDelimiter) {
         StringBuilder y = new StringBuilder();
         String value = null;
         for(String name: nameFields) {
             try { value = eventRow.getString(name); }
                 catch(SQLException e) { e.printStackTrace(); }
             if(isNotEmptyStr(value)) {
                 if(y.length()>0) { y.append(nameDelimiter); }
                 y.append(value);
             }
         }
         return y.toString();
     }
     
 }	

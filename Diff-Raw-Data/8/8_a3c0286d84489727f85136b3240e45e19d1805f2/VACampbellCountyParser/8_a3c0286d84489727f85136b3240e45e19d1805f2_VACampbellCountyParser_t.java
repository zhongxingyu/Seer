 package net.anei.cadpage.parsers.VA;
 
 import java.util.Comparator;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 DAPRO Systems http://www.daprosystems.com/ (This version is different enough that I did not use the builtin dapro
 Campbell County, VA 
 Contact: efhammermeister@yahoo.com,EFHAMMERMEISTER@vtext.com
 MAILBOX:SQ02 MVC/MOTOR VEHICLE CRASH/ACCIDE 002364 WATERLICK RD/LEESVILLE RD CFS# 2010-061119 REF TO A 3 CAR MVA. AT FIRST A REPORT OF NO PI. NO NEED FOR FIRE. VASI ADV THAT T
 MAILBOX:SQ02 (3)ACCIDENT 021400 TIMBERLAKE RD CFS# 2010-061105 2 CAR MVA. WITH ENTRAPMENT
 MAILBOX:SQ02 STABBING 005450 COLONIAL HWY EVI CFS# 2010-061047 STAGE IN THE AREA, DO NOT GO TO THE SCENE
 MAILBOX:SQ02 UNRESPONSIVE 000218 LAKE FOREST DR CFS# 2010-060825
 MAILBOX:SQ02 HANGUP 911 000381 HORIZON DR CFS# 2010-060777 FEMALE ADVISED SOMETHING ABOUT THE RESCUE SQ. LINE DISCONNECTED
 
MAILBOX:CO12 BRUSH/FIELD FIRE 000175 WOODHAVEN DR CFS# 2011-024304

 */
 
 public class VACampbellCountyParser extends SmartAddressParser {
   
   
   private static final String[] KEYWORDS = new String[]{"LOC", "CFS"};
   
  private static final Pattern MARKER = Pattern.compile("^MAILBOX:([A-Z]{2}\\d{2}) ");
   
   public VACampbellCountyParser() {
     super("CAMPBELL COUNTY","VA");
     buildCallDictionary();
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     data.defState="VA";
     data.defCity = "CAMPBELL COUNTY";
     
     // Locate the marker to determine where our part of the message starts
     Matcher match = MARKER.matcher(body);
     if (! match.find()) return false;
    data.strSource = match.group(1);
     body = body.substring(match.end()).trim();
     
     // Adjust it a bit and parse out the main fields
     body = "LOC:" + body.replaceAll(" CFS#", " CFS:");
     Properties props = parseMessage(body, KEYWORDS);
     
     // Lets do the easy ones first
     String strCall = props.getProperty("CFS", "");
 
     // Need to handle Supp being after the CFS
     int idx = strCall.indexOf(" ");
     if (idx != -1) {
       if (idx < strCall.length()-1) {
       data.strSupp = strCall.substring(idx+1);
       data.strCallId = strCall.substring(0,idx);
       }
     } else {
     data.strCallId = strCall.trim();
     }
     
     data.strCross = props.getProperty("CROSS", "");
     
     
     // OK, now to work out the address field
     String sAddress = props.getProperty("LOC", "");
     
     // See if we can identify a call description from our canned list
     String callDesc = getCallDesc(sAddress);
     if (callDesc != null) {
       
       // We got one, the call description comes off the front
       data.strCall = callDesc;
       sAddress = sAddress.substring(callDesc.length()).trim();
       
       // And the last word is the city
       int pt = sAddress.lastIndexOf(' ');
       if (pt >= 0) {
         data.strCity = sAddress.substring(pt+1);
         sAddress = sAddress.substring(0, pt).trim();
       }
       
       // Anything else an an address
       parseAddress(sAddress, data);
       
       // No call description match eh
       // We'll have to rely on the smart parser to save us
     } else {
       parseAddress(StartType.START_CALL, sAddress, data);
     }
     
     // Either way, we need to convert the city code
     data.strCity = data.defCity;
     
     return true;
   }
   
   // This is a tree set containing all of the expected call descriptions
   // sorted in reverse order because we need to search the tree backward and
   // Android implementation of TreeSet lacks features that make that easy
   private static TreeSet<String> callDictionary = new TreeSet<String>(new Comparator<String>(){
     @Override
     public int compare(String str1, String str2) {
       return -str1.compareTo(str2);
     }});
   
   /**
    * Build call description dictionary
    */
   private void buildCallDictionary() {
     
     // Loop through all call descriptions
     for (String call : new String[]{
         "EMS-OTHER",
         "EMS-OTHER/DEFINE",
         "EMS-ABDOMINAL PAIN",
         "EMS-ALLERGIC REACTION",
         "EMS-ALS ASSIST",
         "EMS-ALTERED LOC",
         "EMS-AMPUTATION",
         "EMS-ASSAULT; INJURIES FROM",
         "EMS-AVULSION",
         "EMS-BACK PAIN",
         "EMS-ANIMAL BITE",
         "EMS-BURNS",
         "EMS-CARDIAC",
         "EMS-CARDIAC CONDITION",
         "EMS-CHEST PAIN",
         "EMS-CHOKING",
         "EMS-CO INHALATION",
         "EMS-CODE BLUE",
         "EMS-DIABETIC EMERGENCY",
         "EMS-DIFFICULTY BREATHING",
         "EMS-PERSON DOWN",
         "EMS-ELECTROCUTION",
         "EMS-EYE PROBLEM",
         "EMS-FALLS",
         "EMS-GUNSHOT",
         "EMS-HEADACHE",
         "EMS-HEAT/COLD EXPOSURE",
         "EMS-HEMORRHAGING",
         "EMS-HIGH BLOOD PRESSURE",
         "EMS-ILLNESS",
         "EMS-LACERATION",
         "EMS-MENTAL PROBLEM",
         "EMS-OB RELATED EVENT",
         "EMS-OVERDOSE/POISONING",
         "EMS-PATIENT FALLEN",
         "EMS-SEIZURES",
         "EMS-STAB WOUND",
         "EMS-STROKE",
         "EMS-TRAUMA INJURIES",
         "EMS-UNCONSCIOUS",
         "EMS-UNKNOWN MADICAL",
         "911 OPEN LINE",
         "HANGUP 911",
         "AUTOMATIC FIRE ALARM",
         "AIRCRAFT INCIDENT",
         "MEDICAL ALARM",
         "ALARM SOUNDING",
         "ATTEMPTED SUICIDE",
         "BOMB THREAT",
         "BRUSH FIRE",
         "BRUSH FIRE THREATENING A STRUC",
         "CHIMNEY FIRE",
         "CO DETECTOR ACTIVATION",
         "POSSIBLE CO EVENT",
         "STRUCTURE COLLAPSE",
         "CONSTRUCTION ACCIDENT",
         "CONTROLLED BURN",
         "TRAFFIC CRASH",
         "TRAFFIC CRASH INVOLVING A BUS",
         "TRAFFIC CRASH W/PEDESTRIAN",
         "TRAFFIC CRASH INVOLVING A STRU",
         "DUMPSTER FIRE",
         "ELEVATOR ENTRAPMENT",
         "EXPLOSION",
         "FARM ACCIDENT",
         "FILL QUARTERS",
         "FORREST FIRE",
         "GRASS FIRE",
         "GRASS FIRE THREATENING A STRUC",
         "HAZMAT INCIDENT",
         "HAZMAT LEVEL 3 INCIDENT",
         "HOSTAGE/BARRICADE",
         "INDUSTRIAL ACCIDENT",
         "LANDING ZONE",
         "LINES DOWN",
         "LOCK OUT SERVICE",
         "MENTAL CASE",
         "MULCH FIRE",
         "MULCH FIRE THREATENING A STRUC",
         "ODOR INVESTIGATION",
         "ODOR INVESTIGATION IN STRUCTUR",
         "OFFICER INVOLVED SHOOTING",
         "SUSPICIOUS PACKAGE",
         "PUBLIC SERVICE CALL",
         "RAPE",
         "REKINDLE",
         "GROUND RESCUE",
         "GROUND SEARCH",
         "SMOKE INVESTIGATION",
         "LARGE SPILL",
         "SMALL SPILL",
         "STAND-BY",
         "POSSIBLE STRUCTURE FIRE",
         "STRUCTURE FIRE",
         "SUICIDE",
         "TECHNICAL RESCUE",
         "TRAIN CRASH",
         "TRAIN FIRE",
         "TRANSFORMER FIRE",
         "TRASH FIRE",
         "TREE FIRE",
         "TREE FIRE THREATENING A STRUCTUR",
         "UNAUTHORIZED BURN",
         "UNKNOWN SITUATION",
         "UNKNOWN FIRE",
         "VEHICLE FIRE",
         "VEHICLE FIRE THREATENING A STRUCTURE",
         "WATER EVENT",
         "WATER REMOVAL"
     }) callDictionary.add(call + " ");
   }
   
   private String getCallDesc(String body) {
     
     // Search the call dictionary sorted set for the highest entry less than or
     // equal to message body.  If the body starts with this string, we have a
     // match.  If not, we have to keep searching backward through the sorted set
     // for the entry less than or equal to the message body
     
     // We reversed the tree order so we can accomplish this trick without
     // needing a backward read feature, with Android seems to be lacking
     String firstWord = new Parser(body).get(' ');
     SortedSet<String> tail =  callDictionary.tailSet(body);
     for (String call : tail) {
       if (body.startsWith(call)) return call.trim();
       if (!call.startsWith(firstWord)) return null;
     }
     return null;
   }
 }

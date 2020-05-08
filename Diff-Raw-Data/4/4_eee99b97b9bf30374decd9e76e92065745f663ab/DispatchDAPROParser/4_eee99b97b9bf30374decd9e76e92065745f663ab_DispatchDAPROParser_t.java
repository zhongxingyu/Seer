 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.Comparator;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 /*
 DAPRO Systems http://www.daprosystems.com/
 Currently used by Rockingham, VA and Franklin, VA
 
 Rockingham County, VA 
 R40 EMS-CARDIAC CONDITION 1751 MAIN AVE HAR CFS# 2010-082726 CROSS: GARBERS CHURCH RD/S HIGH ST
 R40 EMS-MENTAL PROBLEM 445 N MAIN ST 44 HAR CFS# 2010-082451 CROSS: WOLFE ST/ROCK ST
 R40 EMS-ABDOMINAL PAIN 1737 MORELAND DR HAR CFS# 2010-083119 CROSS: PHEASANT RUN CIR/ASHFORD CT
 R40 EMS-CHEST PAIN 235 LAYMAN ST 101 HAR CFS# 2010-083046 CROSS: N MAIN ST/LONGVIEW DR
 R40 TRAFFIC CRASH 300 BOYERS RD BLK HAR CFS# 2010-082984 CROSS: MYSTIC WOODS LN/CULLISON CT
 R40 TRAFFIC CRASH RESERVOIR ST & CANTRELL AV HAR CFS# 2010-082327
 (Rescue 40) R40 ODOR INVESTIGATION IN STRUCTUR 290 WARREN SERVICE DR HAR CFS# 2010-091415 CROSS: BLUESTONE DR/DEAD END
 S: M:MAILBOX:DP1 ACCIDENT-INJURY 330 FRANKLIN ST RMT CFS# 2011-000224 CROSS: CLAIBORNE AVE/CHURCH ST
 
 Franklin County, VA 
 MAILBOX:S07 EMS-OTHER/DEFINE 18360 VIRGIL H GOODE HWY 124 RMT CFS# 2010-030542 CROSS: SHADY LN/BLACKWATER RIVER
 MAILBOX:S07 EMS-CHEST PAIN 413 WOODDALE DR RMT CFS# 2010-030355 CROSS: VIRGIL H GOODE HWY/DEAD END
 MAILBOX:S07 EMS-PATIENT FALLEN 3005 GREEN LEVEL RD RMT CFS# 2010-030541 CROSS: GRASSY HILL RD/LITTLE MOUNTAIN DR
 MAILBOX:S07 EMS-CARDIAC VIRGIL H GOODE HWY & LINK ST RMT CFS# 2010-030580
 MAILBOX:S07 EMS-HIGH BLOOD PRESSURE 1808 BETHLEHEM RD BML CFS# 2010-030643 CROSS: BETHANY RD/DILLONS MILL RD
 
 Somewhere in VA
 MAILBOX:RS3 PAIN 16 E MAIN ST LUR CFS# 2011-000782 CROSS: BROAD ST/TANNERY RD
 */
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 public class DispatchDAPROParser extends SmartAddressParser {
   
   private Properties cityCodeTable;
 	
   private static final String[] KEYWORDS = new String[]{"LOC", "CFS", "CROSS"};
   
  private static final Pattern MARKER = Pattern.compile("([SRC]\\d\\d|DP\\d)\\b");
   
   public DispatchDAPROParser(Properties cityCodeTable, String defCity, String defState) {
     super(cityCodeTable, defCity, defState);
     this.cityCodeTable = cityCodeTable;
     buildCallDictionary();
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Locate the marker to determine where our part of the message starts
     Matcher match = MARKER.matcher(body);
     if (! match.find()) return false;
    if (match.start() > 20) return false;
     data.strSource = body.substring(match.start(), match.end());
     body = body.substring(match.end()).trim();
     
     // Adjust it a bit and parse out the main fields
     body = "LOC:" + body.replaceAll(" CFS#", " CFS:");
     Properties props = parseMessage(body, KEYWORDS);
     
     // Lets do the easy ones first
     data.strCallId = props.getProperty("CFS", "");
     data.strCross = props.getProperty("CROSS", "");
     
     // OK, now to work out the address field
     // Google doesn't handle the "BLK" qualifier so get rid of it
     String sAddress = props.getProperty("LOC");
     sAddress = sAddress.replaceAll(" BLK ", " ");
     
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
     data.strCity = convertCodes(data.strCity, cityCodeTable);
     
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

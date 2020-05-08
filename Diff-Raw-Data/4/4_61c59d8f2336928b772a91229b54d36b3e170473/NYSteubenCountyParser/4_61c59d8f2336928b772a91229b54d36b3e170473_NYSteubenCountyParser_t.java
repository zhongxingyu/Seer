 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 
 /*
 Steuben County, NY
 Contact:  Shane Henry <shenry@gmx.us>
 Sender: messaging@iamresponding.com
 
 messaging@iamresponding.com (Avoca FD) 64 S MAIN ST #APT OFFICE, AVOCA VILLAGE OF (STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME)6D4 Breathing Problems Clammy\nAVOCAFDA
 messaging@iamresponding.com (Avoca FD) 4434 COUNTY ROUTE 6 , AVOCA TOWN OF (STATE ROUTE 415 / COUNTY ROUTE 70) 17B3 Falls Unknown status\nAVOCAFDAMB:2011:98
 messaging@iamresponding.com (Avoca FD) 9439 SPAULDING RD , WHEELER TOWN OF ( / COUNTY ROUTE 7)WIRES EMERG\nAVOCFD:2011:51
 messaging@iamresponding.com (Avoca FD) 8806 COUNTY ROUTE 105 , AVOCA TOWN OF ( / N MAIN ST) 9E1 Cardiac or Respiratory Arrest not breathing at all\nAVOCAFDAMB:2011:99
 messaging@iamresponding.com (Avoca FD) 7 CHARLESWORTH AV , AVOCA VILLAGE OF (EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS) 17B1 Falls Possibly dangerous body area\nAVOCAFDAMB
 messaging@iamresponding.com (Avoca FD) 4652 COUNTY ROUTE 70A , AVOCA TOWN OF (COUNTY ROUTE 69 / ) 26A1 Sick Person No priority symptoms (complaint conditions 2-11 not identified)\nA
 messaging@iamresponding.com (Avoca FD) 7 CHARLESWORTH AV , AVOCA VILLAGE OF (EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS) 17B1 Falls Possibly dangerous body area\nAVOCAFDAMB
 messaging@iamresponding.com (Avoca FD) 8012 SMITH POND RD , HOWARD TOWN OF (WESSELS HILL RD / BURDIN HILL RD) 12B1 Convulsions Effective Breathing not verified &lt;35\nAVOCAFDAMB:20
 messaging@iamresponding.com (Avoca FD) 118 E STEUBEN ST , BATH VILLAGE OF (HALSEY ST / GANSEVOORT ST; Near: PRO ACTION) 32B3 Unknown Problem Unknown Status/Other Codes Not Applicab
 messaging@iamresponding.com (Avoca FD) ::::RECALLED:::::: 118 E STEUBEN ST , BATH VILLAGE OF (HALSEY ST / GANSEVOORT ST; Near: PRO ACTION) 32B3 Unknown Problem Unknown Status/Other
 messaging@iamresponding.com (Avoca FD) 4230 SHARPS CORNERS RD , HOWARD TOWN OF 21A2 Hemorrhage/Lacerations minor hemorrhage\nAVOCAFDAMB:2011:105
 messaging@iamresponding.com (Avoca FD) 64 S MAIN ST , AVOCA VILLAGE OF (STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME) 17B1 Falls Possibly dangerous body area\nAVOCAFD
 messaging@iamresponding.com (Avoca FD) /ARROW MART GAS STATION (S 99 MAIN ST AVOCA VILLAGE OF ) 10A1 Chest Pain Breathing normaly &lt; 35\nAVOCAFDAMB:2011:107
 messaging@iamresponding.com (Avoca FD) 5172 LOUCKS POND RD , AVOCA TOWN OF (OLD AVOCA RD / DYER HILL RD) 10C1 Chest Pain Abnormal breathing\nAVOCAFDAMB:2011:108
 messaging@iamresponding.com (Avoca FD) 54 GRANT ST , AVOCA VILLAGE OF ( / SMITH ST) 21A1 Hemorrhage/Lacerations not dangerous hemorrhage\nAVOCAFDAMB:2011:109
 messaging@iamresponding.com (Avoca FD) WEEKLY PAGER TEST TEST\nAVOCFD:2011:52
 messaging@iamresponding.com (Avoca FD) 60617 STATE ROUTE 415 , AVOCA TOWN OF Misc FIRE\nAVOCFD:2011:53
 messaging@iamresponding.com (Avoca FD) 64 S MAIN ST , AVOCA VILLAGE OF (STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME) 17A2 Falls non-recent &gt; 6 hrs (without prior
 messaging@iamresponding.com (Avoca FD) 5572 MICHIGAN HOLLOW RD , WHEELER TOWN OF (BEEKERMAN RD / ) 6C1 Breathing Problems Abnormal breathing\nAVOCAFDAMB:2011:112
 messaging@iamresponding.com (Avoca FD) 7 GRANT ST , AVOCA VILLAGE OF (S MAPLE AV / S MAIN ST) 8O1 Carbon Monoxide Dector Alarm (w/out priority symptoms\nAVOCFD:2011:54
 messaging@iamresponding.com (Avoca FD) 7 CHARLESWORTH AV , AVOCA VILLAGE OF (EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS) AUTOMATIC ALARM Fire\nAVOCFD:2011:55
 messaging@iamresponding.com (Avoca FD) 61619 STATE ROUTE 415 #TRLR, AVOCA TOWN OF 31A1 Unconscious/Fainting Fainting episode(s) and alert &gt;35 (without cardiac history)\nAVOCAFDAM
 messaging@iamresponding.com (Avoca FD) 3628 COUNTY ROUTE 70A , HOWARD TOWN OF (HOUSE RD / SMITH POND RD S) STRUCTURE FIRE\nHOWAFD:2011:43
 messaging@iamresponding.com (Avoca FD) ::::RECALLED:::::: 3628 COUNTY ROUTE 70A , HOWARD TOWN OF (HOUSE RD / SMITH POND RD S) STRUCTURE FIRE\nHOWAFD:2011:43
 messaging@iamresponding.com (Avoca FD) 4550 COUNTY ROUTE 105 , AVOCA TOWN OF (WALLACE BACK RD / CHURCH ST) 31E1 Unconscious/Fainting Ineffective breathing\nAVOCAFDAMB:2011:115
 messaging@iamresponding.com (Avoca FD) /BOMBARDIER ( 7940 STATE ROUTE 415 AVOCA TOWN OF ) Misc FIRE\nAVOCFD:2011:56
 messaging@iamresponding.com (Avoca FD) 9068 FAIRBROTHER RD , AVOCA TOWN OF (VAN AUCKER RD / WATERBURY HILL RD) 29B4 Traffic Accident Unknown Status/Other Codes Not Applicable\nAVOCA
 messaging@iamresponding.com (Avoca FD) 7 ALEXANDER ST , AVOCA VILLAGE OF (N MAPLE AV / N MAIN ST) 25B6 Psychiatric/Suicide attempt Unknown Status/Other Codes Not Applicable\nAVOCAFD
 messaging@iamresponding.com (Avoca FD) 9374 KHORKS AV , AVOCA TOWN OF ( / WALLACE BACK RD) 17A1 Falls not dangerous body area\nAVOCAFDAMB:2011:119
 messaging@iamresponding.com (Avoca FD) 8980 REEVES RD , WHEELER TOWN OF ( / HEMLOCK RD) 25B3 Psychiatric/Suicide attempt Threatening Suicide\nAVOCAFDAMB:2011:120
 messaging@iamresponding.com (Avoca FD) 4252 COUNTY ROUTE 70A , HOWARD TOWN OF (HARRIS HILL RD / MILL RD) 26A 2-11 Sick Person Non-Priority Complaints\nAVOCAFDAMB:2011:121
 messaging@iamresponding.com (Avoca FD) 5279 TRACY RD , AVOCA TOWN OF ( / COUNTY ROUTE 8) 1A1 Abdominal Pain\nAVOCAFDAMB:2011:122
 messaging@iamresponding.com (Avoca FD) 8764 CLARK RD , AVOCA TOWN OF ( / CROSS RD) 28C02 Stroke (CVA)  Abnormal breathing\nAVOCAFDAMB:2011:123
 messaging@iamresponding.com (Avoca FD) 8980 REEVES RD , WHEELER TOWN OF ( / HEMLOCK RD) 25A1 Psychiatric/Suicide attempt Non-suicidal and alert\nAVOCAFDAMB:2011:124
 messaging@iamresponding.com (Avoca FD) /MILE 139 ( I86 BETWEEN EXIT 35 + ) 29B1 Traffic Accidents  Injuries\nAVOCAFDAMB:2011:125
 messaging@iamresponding.com (Avoca FD) ::::RECALLED:::::: /MILE 139 ( I86 BETWEEN EXIT 35 + ) 29B1 Traffic Accidents  Injuries\nAVOCAFDAMB:2011:125
 messaging@iamresponding.com (Avoca FD) 61619 STATE ROUTE 415 #TRLR, AVOCA TOWN OF ( / WENTWORTH RD) 19D4 Heart Problems Clammy\nAVOCAFDAMB:2011:126
 messaging@iamresponding.com (Avoca FD) 226 W WASHINGTON ST #APT 201, BATH VILLAGE OF (HUBBELL ST / FREEMAN TERR) 10D2 Chest Pain Difficulty Speaking Between Breaths\nBATHAMB:2011:77
 messaging@iamresponding.com (Avoca FD) 8039 SMITH POND RD , HOWARD TOWN OF (MACKEY RD / WESSELS HILL RD) Brush Fire\nAVOCAFDAMB:2011:127
 messaging@iamresponding.com (Avoca FD) THIS IS A TEST MESSAGE FOR I AM RESPONDING TEST\nAVOCFD:2011:57
 
 Contact: Rick Towner <towner.rick@gmail.com>
 Sender: messaging@iamresponding.com
 (Cohocton FD) /CARPET RACK ( 8 MAPLE AV COHOCTON VILLAGE OF )\n12D1 Convulsions Not breathing\nCOHOFAMB:2011:76
 (Cohocton FD) 86 MAPLE AV #FLR 1, COHOCTON VILLAGE OF (STATE ROUTE 962D / SHULTS AV)\n26A 2-11 Sick Person Non-Priority Complaints\nCOHOFAMB:2011:70
 (Cohocton FD) 10298 COUNTY ROUTE 9 , COHOCTON TOWN OF (EVELAND RD / )\n25B6 Psychiatric/Suicide attempt Unknown Status/Other Codes Not Applicable\nCOHOFAM
 (Cohocton FD) 24 N DANSVILLE ST #APT 110, COHOCTON VILLAGE OF\n21D3 Hemorrhage/Lacerations Dangerous Henorrhage\nCOHOFAMB:2011:72
 (Cohocton FD) 6 ERIE ST , COHOCTON TOWN OF (MAIN ST / BOGGS ST)\n12D3 Convulsions Agonal/Ineffective Breathing\nATLANFDAMB:2011:33
 (Cohocton FD) 5 ROSENCRANS ST , COHOCTON VILLAGE OF (LARROWE ST / MAPLE AV)\n31D2 Unconscious/Fainting Unconscious - Effective Breathing\nCOHOFAMB:2011:75
 
 Contact: E Brown <car9702@gmail.com>
 (S. Corning FD) 1567 MARTIN HILL RD , CATON TOWN OF (W CATON RD / PINE BREEZE LN)\nVEHICLE FIRE\nCATOFD:2011:72
 
 Contact: "Conrad, Robert" <RobertC@co.steuben.ny.us>
 /ZEMAKS GREENHOUSE ( 6714 STATE ROUTE 415 BATH TOWN OF ) Brush Fire BTFD:2011:272
 /MILE 154 ( I86 BETWEEN EXIT 39 + ) 29D2 Traffic Accidents High mechanism BTFD:2011:177
 --{Don't Respond: Controlled Burn}-- 7630 COUNTY ROUTE 16 , BATH TOWN OF (QUINN RD / FREEMAN HOLLOW RD) Controlled Burn
 105 GENEVA ST , BATH VILLAGE OF (STATE ROUTE 54 / GRATTON DR; Near: LAKEVIEW APARTMENTS) AUTOMATIC ALARM Fire BTFD:2011:263
 [2ndCall] 25 MAINE ST , BATH VILLAGE OF (ROBIE ST / HAVERLING ST) 1D1 Abdominal Pain Not alert BATHAMB:2011:1393 
 [2ndCall] 3 CLUB VIEW DR , BATH VILLAGE OF ( / MAY ST) 28C06 Stroke (CVA) Sudden loss of balance or coordination BATHAMB:2011:1392
 
 */
 
 
 public class NYSteubenCountyParser extends SmartAddressParser {
   
   public NYSteubenCountyParser() {
     super(CITY_LIST, "STEUBEN COUNTY", "NY");
   }
   
   @Override
   public String getFilter() {
     return "messaging@iamresponding.com";
   }
 
   private static final Pattern MARKER = Pattern.compile("^messaging@iamresponding.com \\( *(.*?) *\\) ");
   private static final Pattern MASTER1 = Pattern.compile("/ *(.*?) *\\( *(.*?) *\\)  *(?:(\\d{1,2}[A-Z]\\d{1,2}) +)?(.*)");
   private static final Pattern MASTER2 = Pattern.compile("(.*?) *, *(.*?) *\\( *(.*?/.*?) *\\) *(?:(\\d{1,2}[A-Z]\\d{1,2}) +)?(.*)");
   private static final Pattern MASTER3 = Pattern.compile("(.*?) *, *([^\\(\\)]*?) *(\\d{1,2}[A-Z]\\d{1,2}) +(.*)");
   private static final Pattern MASTER4 = Pattern.compile("(.*?) *, *([^\\(\\)]*?) (?:TOWN|VILLAGE) OF +(.*)");
 
   
   private static final Pattern TRAILER = Pattern.compile("\\n[A-Z]+(?::\\d*)*$");
   private static final Pattern TRAILER2 = Pattern.compile("\\b([A-Z]+)(?::\\d+){2}$");
   
   private static final Pattern INFO_HEADER = Pattern.compile("^--\\{(.*)\\}--");
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) { 
 
     // There are a couple differnent text signatures we look for
     boolean sigMatch = true;
     Matcher match;
     do {
 	    // Look for starting page signature
 	    match = MARKER.matcher(body);
 	    if (match.find()) {
 	      data.strSource = match.group(1);
 	      body = body.substring(match.end()).trim();
 	      break;
 	    }
 	    
 	    // Sometimes the starting sender address is missing, which means the
 	    // station code ends up in the description
 	    if (subject.endsWith(" FD")) {
 	      data.strSource = subject;
 	      break;
 	    }
 	    
 	    // Nothing worked, we will keep trying, but will have a much stricter 
 	    // standard of what constitutes a CAD page
 	    sigMatch = false;
     } while (false);
 
     // If we found a start signature match, which included a station source
     // Look for and remove any trailing station and call ID
     if (sigMatch) {
       match = TRAILER.matcher(body);
       if (match.find()) body = body.substring(0,match.start()).trim();
     }
     
     // If we didn't fidn a start signature match, we have a stricter definition
     // of what the trailer should look like, and we will extract the source code
     // from it
     else {
       match = TRAILER2.matcher(body);
 	    if (match.find()) {
 	      data.strSource = match.group(1);
 	      body = body.substring(0,match.start()).trim();
 	    }
     }
     body = body.replace('\n', ' ');
     
     // Check for special information header
     match = INFO_HEADER.matcher(body);
     if (match.find()) {
       data.strSupp = match.group(1);
       body = body.substring(match.end()).trim();
     }
     
     // Check for RECALLED tag
     if (body.startsWith("::::RECALLED:::::: ")) {
       data.strCall = "RECALLED - ";
       body = body.substring(19).trim();
    } else if (subject.equals("2ndCall") || subject.equals("2nd Call")) {
      data.strCall = subject + " - ";
     }
     
     body = body.replace("COUNTY ROUTE", "COUNTY ROAD");
     
     // There a couple basic pattern
     // /<place> (<addr city) [code] <call>
     if ((match = MASTER1.matcher(body)).matches()) {
       parseAddress(match.group(1), data);
       data.strPlace = match.group(1);
       parseAddress(StartType.START_ADDR, match.group(2), data);
       String left = getLeft();
       if (left.startsWith("TOWN OF")) left = left.substring(7).trim();
       else if (left.startsWith("VILLAGE OF")) left = left.substring(10).trim();
       data.strCross = left;
       data.strCode = cleanCode(match.group(3));
       data.strSupp = append(data.strSupp, " / ", match.group(4));
     }
     
     // <addr> , <city> ( <cross> ) [code] <info>
     else if ((match = MASTER2.matcher(body)).matches()) {
       parseAddress(match.group(1), data);
       data.strCity = cleanCity(match.group(2));
       data.strCross = cleanCross(match.group(3));
       data.strCode = cleanCode(match.group(4));
       data.strSupp = append(data.strSupp, " / ", match.group(5));
     }
     
     // <addr> , <city> <code> <info>
     else if ((match = MASTER3.matcher(body)).matches()) {
       parseAddress(match.group(1), data);
       data.strCity = cleanCity(match.group(2));
       data.strCode = cleanCode(match.group(3));
       data.strSupp = append(data.strSupp, " / ", match.group(4));
     }
     
     // <addr> , <city> <info>
     else if ((match = MASTER4.matcher(body)).matches()) {
       parseAddress(match.group(1), data);
       data.strCity = cleanCity(match.group(2));
       data.strSupp = append(data.strSupp, " / ", match.group(3));
     }
     
     // Anything else is just a info
     // If we didn't find a matching start signature and didn't find a
     // master pattern match, the we had better conclude that this is not
     // reall a CAD page
     else {
       if (!sigMatch) return false;
       data.strSupp = append(data.strSupp, " / ", body);
     }
     
     if (data.strSupp.length() > 0 && data.strSupp.length() <= 40) {
       data.strCall = data.strCall + data.strSupp;
       data.strSupp = "";
     } else {
       data.strCall = data.strCall + "ALERT";
     }
     return true;
   }
 
   private String cleanCity(String field) {
     if (field.endsWith(" TOWN OF")) {
       field = field.substring(0, field.length()-8).trim();
     } else if (field.endsWith(" VILLAGE OF")) {
       field = field.substring(0, field.length()-11);
     }
     return field;
   }
 
   private String cleanCross(String field) {
     if (field.startsWith("/")) field = field.substring(1).trim();
     if (field.endsWith("/")) field = field.substring(0,field.length()-1).trim();
     return field;
   }
   
   private String cleanCode(String field) {
     return (field == null ? "" : field);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ADDISON",
     "ALMOND",
     "ARKPORT",
     "AVOCA",
     "BATH",
     "BRADFORD",
     "CAMERON",
     "CAMPBELL",
     "CANISTEO",
     "CANISTEO",
     "CATON",
     "COHOCTON",
     "CORNING",
     "DANSVILLE",
     "ERWIN",
     "FREMONT",
     "GANG MILLS",
     "GREENWOOD",
     "HAMMONDSPORT",
     "HARTSVILLE",
     "HORNBY",
     "HORNELL",
     "HORNELLSVILLE",
     "HOWARD",
     "JASPER",
     "LINDLEY",
     "NORTH HORNELL",
     "PAINTED POST",
     "PRATTSBURGH",
     "PULTENEY",
     "RATHBONE",
     "RIVERSIDE",
     "STEPHENS MILLS",
     "SAVONA",
     "SOUTH CORNING",
     "THURSTON",
     "TROUPSBURG",
     "TUSCARORA",
     "URBANA",
     "WAYLAND",
     "WAYNE",
     "WEST UNION",
     "WHEELER",
     "WOODHULL"
   };
 }
 	

 package net.anei.cadpage.parsers.OH;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 taccad:SQUAD RUN 593 GATES ST CAROLYN RUNYON, AGE 64, POSSIBLE STROKE
 taccad:ACCIDENT EASTERN RD AKRON RD 911 CELL CALL FALINE JONES REF ACCIDENT UNK INJURIES. NORTON FIRE / POLICE RESPONDING.
 taccad:ASSAULT 351 COLLIER DR 911 CELL JESSICA KLATIK REQUEST OFFICER FOR AN ASSAULT
 taccad:FIRE 12315 BLACK DIAMOND RD UNKNOWN TYPE OF FIRE CLOSE TO THE HOUSE.
 taccad:SQUAD RUN 142 HOMAN DR 68 YOF DIFF. BREATHING MARCELLA PRIDE
 taccad:ALARM 68 N PORTAGE ST CMS SECURITY FOR PHONE IN ALARM - HEAT PULL STATION
 taccad:SQUAD RUN 176 STONE RIDGE CIR 911 CALL JUDITH DAKOSKI REQUEST SQUAD 75 YOM HEART CONDITION NOW VOMITING AND DIARRHEA
 taccad:SQUAD RUN 490 E CLINTON 911 CALL - 57 YOF DIFF BREATHING
 taccad:SQUAD RUN 464 GATES ST WALKIN IN FOR CHEST PAINS
 taccad:ACCIDENT GREAT LAKES BLVD GRILL RD MULTIPLE 911 CALLS PICK UP ON ITS TOP IN THE MEDIUM
 taccad:OPEN BURNING CHIPPEWA FIRE OFFICER 19163 EDWARDS RD GENE YEAGER REPORTED NEIGHBOR IS BURNING LEAVES AND SMOKING UP THE NEIGHBORHOOD.
 taccad:SQUAD RUN 14740 OAK GROVE DR 28 NICOLE SHORTRIDGE REQUEST SQUAD 26 YOF WITHDRAWAL FROM MEDICATIONS
 taccad:SQUAD RUN 14083 HATFIELD RD 87 YO M FALL VICTIM POSSIBLE BROKEN HIP
 */
 
 public class OHWayneCountyParser extends SmartAddressParser {
   
   private static final Pattern MASTER = Pattern.compile("taccad:(.*)");
   
   public OHWayneCountyParser() {
     super("WAYNE COUNTY", "OH");
   }
   
   @Override
   public String getFilter() {
     return "@pd.rittman.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
    parseAddress(StartType.START_CALL, match.group(1).trim(), data);
     String sPlace = getLeft();
     data.strSupp = sPlace;
     if (data.strAddress.length() < 4 ){
       data.strPlace = body;
        data.strCall = "GENERAL ALERT";
         return true;
     }
     
     return true;
   }
 
   
 }

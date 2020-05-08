 package net.anei.cadpage.parsers.SC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Oconee County, SC
 Contact: William Rochester <william.rocheste
 Sender: 911@oconeelaw.com
 System: Aegis
 
 [911 Message]  S80 - CORONARY PROBLEM  1280 N  STHY 11 XStreet: SPRINGDALE DR / SCENIC HEIGHTS RD, FOWLER RD WEST UNION    2011-00000815  09/26/11 22:27  Narr:   S
 [911 Message]  S80 - CORONARY PROBLEM  206 S  TUGALOO ST XStreet: W MAULDIN ST / BOOKER DR WALHALLA    2011-00000809  09/25/11 06:49  Narr:   PATIENT IS HER FATHER
 [911 Message]  S46 - ALTERED MENTAL STATUS  308 N  LAUREL ST XStreet: ARDASHIR LN / WALHALLA GARDENS CIR WALHALLA    2011-00000777  09/16/11 12:24  Narr:   76 YOA
 (911 Message) S4 - DIABETIC REACTION  100 PINE MANOR CIR APT 3 XStreet: STOUDEMIRE ST / STOUDEMIRE ST WALHALLA  COUNTRY RIDGE APTS  2011-00000872  10/26/11 19:21
 (911 Message) LIFT ASSISTANCE  313 MANOR LN XStreet: INDUSTRIAL PARK PL / DEAD END SENECA     12/02/11 01:06  Narr:   COME TO THE BACK DOOR  NEEDS HELP GETTING UP
 (911 Message) S86 - CHEST PAIN  3440 BLUE RIDGE BLVD XStreet: MISTY DR, THE OLE HOME PLACE LN / TRAVELLERS BLVD WEST UNION  EDWARDS AUTO SALES  2011-00000946  11/
 (911 Message) LIFT ASSISTANCE  313 MANOR LN XStreet: INDUSTRIAL PARK PL / DEAD END SENECA     12/02/11 01:06  Narr:   COME TO THE BACK DOOR  NEEDS HELP GETTING UP
 
 Contact: support@active911.com
 (911 Message) S74 - RESPIRATORY DISTRESS  502 VERA DR XStreet: HOBSON ST / DEAD END WESTMINSTER    2012-00000025  01/14/12 01:28  Narr:   TONED RQ5  DOES HAVE HEART PROBLEMS  HEART PT HAVING TROUBLE BREATHING  E911 Info - Class of Service: RESD Special Response Info: WESTMINSTER CITY PD WESTMINSTER CITY FIRE  EMS ER-5 ER-3
 
(911 Message) 70S - STRUCTURE FIRE  10941 CLEMSON BLVD XStreet: PRESSLEY PL / SONNYS DR SENECA  BQS #5 CLEMSON BLVD   01/16/12 17:44  Narr:   IN NEIGHBORHOOD BEHIND THE ROAD RUNNER  SOMEWHERE ACROSS STREET  E911 Info - Class of Service: BUSN Special Response Info: SHERIFF DEPT SENECA  CORINTH-SHILOH FIRE #3  EMS ER-1 ER-2
(911 Message) LIFT ASSISTANCE  1407 W LITTLE RIVER DR XStreet: AZURE COVE CT / KEOWEE LAKESHORE DR SENECA     01/15/12 20:43  Narr:   NO 10-52 RESPONDING UNLESS NEEDED  HE HAS FALLEN  NEEDS HELP LIFTING SOMEONE OUT OF THE FLOOR  E911 Info - Class of Service: RESD Special Response Info: SHERIFF DEPT SENECA CORINTH-SHILOH FIRE #3  EMS ER-1 ER-2
(911 Message) S32 - SPINAL INJURY  105 GLORIA LN XStreet: SHILOH RD / DEAD END SENECA 2012-00000009  01/13/12 17:35  Narr:   CANT MOVE HIS LEG  HUSBAND FELL BATHROOM FLOOR

  */
 
 
 public class SCOconeeCountyParser extends FieldProgramParser {
   
   private static final Pattern ID_PTN = Pattern.compile("  (\\d{4}-\\d{8})  ");
  private static final Pattern DATE_TIME_MARK = Pattern.compile("  +(\\d\\d/\\d\\d/\\d\\d) (\\d\\d:\\d\\d)\\b");
   private static final Pattern DATE_TIME_MARK2 = Pattern.compile("^(\\d\\d/\\d\\d/\\d\\d) (\\d\\d:\\d\\d)\\b");
   private static final String DATE_TIME_MARK3 = "NN/NN/NN NN:NN";
   
   public SCOconeeCountyParser() {
     super(CITY_LIST, "OCONEE COUNTY", "SC",
            "BASE! Narr:INFO E911_Info_-_Class_of_Service:SKIP Special_Response_Info:SKIP");
   }
   
   @Override
   public String getFilter() {
     return "911@oconeelaw.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("911 Message")) return false;
     return super.parseMsg(body, data);
   }
   
   private class BaseField extends Field {
 
     @Override
     public void parse(String body, Data data) {
       
       // Look for an ID marker
       String sLeader = null;
       String sTrailer = null;
       do {
         Matcher match = ID_PTN.matcher(body);
         if (match.find()) {
           
           // Found it, get the ID and identify leading portion
           data.strCallId = match.group(1);
           sLeader = body.substring(0,match.start()).trim();
           sTrailer = body.substring(match.end()).trim();
           
           // This should be followed by a date and time
           match = DATE_TIME_MARK2.matcher(sTrailer);
           if (match.find()) {
             data.strDate = match.group(1);
             data.strTime = match.group(2);
             sTrailer = sTrailer.substring(match.end()).trim();
             break;
           }
           
           // If it isn't, see if it is a truncated Date/time
           String sCheck = sTrailer.replaceAll("\\d", "N");
           if (! DATE_TIME_MARK3.startsWith(sCheck)) abort();
           sTrailer = "";
           break;
         }
         
         match = DATE_TIME_MARK.matcher(body);
         if (match.find()) {
           data.strDate = match.group(1);
           data.strTime = match.group(2);
           sLeader = body.substring(0,match.start()).trim();
           sTrailer = body.substring(match.end()).trim();
           break;
         }
         abort();
       } while (false);
 
       int pt = sLeader.indexOf("  ");
       if (pt < 0) abort();
       data.strCall = sLeader.substring(0,pt);
       String sAddr = sLeader.substring(pt+2).trim();
       
       sAddr = sAddr.replace(" XStreet:", " XS:");
       parseAddress(StartType.START_ADDR, FLAG_START_FLD_REQ, sAddr, data);
       data.strPlace = getLeft();
     }
     
     @Override
     public String getFieldNames() {
       return "CALL ADDR X CITY ID DATE TIME";
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf("  ");
       if (pt >= 0) field = field.substring(pt+2).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   protected Field getField(String name) {
     if (name.equals("BASE")) return new BaseField();
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "SALEM",
     "SENECA",
     "WALHALLA",
     "WEST UNION",
     "WESTMINSTER",
     
     "FAIR PLAY",
     "LONG CREEK",
     "MOUNTAIN REST",
     "NEWRY",
     "OAKWAY",
     "RICHLAND",
     "TAMASSEE",
     "TOWNVILLE",
     "UTICA"
   };
 }

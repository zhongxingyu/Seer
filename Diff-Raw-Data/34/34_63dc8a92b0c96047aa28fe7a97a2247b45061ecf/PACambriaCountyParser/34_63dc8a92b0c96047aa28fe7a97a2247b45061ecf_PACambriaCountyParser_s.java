 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Cambria County, PA
 Contact: jacob berkey <jwb932@gmail.com>
 Sender: alerts@cambria.ealertgov.com
 
 [29] 11 Time: 15:02:29 Nature: 17A01G-Alpha FALL Location: 216 MAIN ST-JO | Sta 35
 [29] 11 Time: 11:54:47 Nature: 29B01-Bravo VEH ACC WITH INJURIES Location: 110 PLAZA DR-LY | Sta 30, Sta 23, Sta 35
 [29] 11 Time: 08:03:01 Nature: 10C02-Charlie CHEST PAIN Location: 315 LOCUST ST-JO | Sta 35 
 [29] 11 Time: 06:45:29 Nature: 17B03G-Bravo FALL Location: WALNUT ST-JO/MAIN ST-JO | Sta 35
 [28] 11 Time: 21:39:31 Nature: 06D03A-Delta BREATHING PROBLEMS Location: 205 CHANDLER AVE-JO | Sta 35, Sta 21, Sta 36
 
 Contact: Ben Smith <cnfederatediesel@gmail.com>
 Contact: Paul Zabinsky <pzabinsky@gmail.com>
 Date: 06/02/11\nTime: 20:47:52\nNature: 10C01-Charlie CHEST PAIN\nLocation: 1518 W 2ND ST-CR\n| Sta 75
 Date: 06/04/11\nTime: 11:03:04\nNature: 06D02-Delta  BREATHING PROBLEMS\nLocation: 908 SHORT AVE-CR\n| Sta 75
 Date: 06/04/11\nTime: 17:24:03\nNature: STB-STAND BY\nLocation: 721 MAIN ST-PORB\n| Sta 73
 Date: 06/05/11\nTime: 18:00:42\nNature: 52C04G-Charlie FIRE ALARM\nLocation: 111 ASHCROFT AVE-CB\n| Sta 70
 Date: 06/13/11\nTime: 15:53:48\nNature: 02D02-Delta  ALLERGIC REACTION\nLocation: 1237 ST AUGUSTINE RD-CF\n| Sta 60, Sta 67
 
 Contact: Paul Bomboy <training@easthillsems.com>
 Sender: Cambria 9-1-1
 FRM:Cambria 9-1-1\nMSG:Date: 11/07/11\nTime: 18:02:55\nNature: 10C01-Charlie CHEST PAIN\nLocation: 349 VO TECH DR-RI\n|
 
**** NOT PARSING ****
 Contact: support@active911.com
 Sender; "Cambria 9-1-1" <alerts@cambria.ealertgov.com>
 Date: 01/24/12\nTime: 18:02:50\nNature: 69D03-Delta STRUCTURE FIRE\nLocation: 7458 ADMIRAL PEARY HWY-CB\nSta 70, Sta 71, Sta 72, Sta 75
 
 */
 
 public class PACambriaCountyParser extends FieldProgramParser {
   
   private static final Pattern MARKER1 = Pattern.compile("\\d\\d");
   private static final Pattern MARKER2 = Pattern.compile("^\\d\\d ");
   
   public PACambriaCountyParser() {
     super(CITY_CODES, "CAMBRIA COUNTY", "PA",
           "Date:DATE? Time:TIME! Nature:CALL! Location:ADDR/y! Sta:UNIT");
   }
   
   @Override
   public String getFilter() {
     return "alerts@cambria.ealertgov.com,Cambria 9-1-1";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (MARKER1.matcher(subject).matches()) {
       Matcher match = MARKER2.matcher(body);
       if (!match.find()) return false;
       body = body.substring(match.end()).trim();
     }
     
    body = body.replace('\n', ' ').replace(" | ", " Sta: ");
     if (body.endsWith("|")) body = body.substring(0,body.length()-1).trim();
    return super.parseMsg(body, data);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ADAM", "ADAMS TWP",
       "ASHB", "ASHVILLE",
       "BARR", "BARR TWP",
       "BN", "BROWNSTOWN",
       "BT", "BLACKLICK TWP",
       "CA", "CASSANDRA",
       "CB", "CRESSON",
       "CF", "CLEARFIELD TWP",
       "CH", "CHEST TWP",
       "CL", "CARROLLTOWN",
       "CM", "CAMBRIA TWP",
       "CONT", "CONEMAUGH TWP",
       "CP", "CHEST SPRINGS",
       "CR", "CRESSON TWP",
       "CROY", "CROYLE TWP",
       "DB", "DALE",
       "DE", "DEAN TWP",
       "DT", "DAISYTOWN",
       "EA", "EAST TAYLOR TWP",
       "EB", "EBENSBURG",
       "ECON", "EAST CONEMAUGH",
       "EF", "EHRENFELD",
       "ECAR", "EAST CARROLL TWP",
       "ET", "ELDER TWP",
       "FB", "FERNDALE",
       "FR", "FANKLIN",
       "GB", "GALLITZIN",
       "GE", "GEISTOWN",
       "GT", "GALLITZIN TWP",
       "HB", "HASTINGS",
       "JO", "JOHNSTOWN",
       "JT", "JACKSON TWP",
       "LB", "LORETTO",
       "LI", "LILLY",
       "LO", "LORAIN",
       "LY", "LOWER YODER TWP",
       "MT", "MIDDLE TAYLOR TWP",
       "MU", "MUNSTER TWP",
       "NC", "NORTHERN CAMBRIA",
       "NG", "NANTY GLO",
       "PATB", "PATTON ",
       "PORB", "PORTAGE",
       "PORT", "PORTAGE TWP",
       "RI", "RICHLAND TWP",
       "RT", "READE TWP",
       "SB", "SANKERTOWN",
       "SC", "STONYCREEK TWP",
       "SF", "SOUTH FORK",
       "SL", "SCALP LEVEL",
       "SM", "SOUTHMONT",
       "SQ", "SUSQUEHANNA TWP",
       "SUMB", "SUMMERHILL",
       "SUMT", "SUMMERHILL TWP",
       "TB", "TUNNELHILL",
       "UY", "UPPER YODER TWP",
       "VB", "VINTONDALE",
       "WB", "WESTOVER",
       "WC", "WEST CARROLL TWP",
       "WE", "WHITE TWP",
       "WI", "WILMORE",
       "WM", "WESTMONT",
       "WS", "WEST TAYLOR TWP",
       "WT", "WASHINGTON TWP"
   });
 }

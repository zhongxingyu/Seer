 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Lancaster County, PA
 Contact: Benjamin Herskowitz <bherskowitz@gmail.com>
 Sender: 911@lcwc911.us
 (VEH ACCIDENT-STANDBY) MANHEIM TWP~LITITZ PIKE / PETERSBURG RD~~ENG271,SQ27~20:19:57^
 (VEH ACCIDENT-MASS TRANSIT-1A) MANHEIM TWP~2475 OREGON PIKE~WHITEMARSH DR~VALLEY RD~ENG271~08:13:50^
 (SPILL CONTROL) EAST DONEGAL TWP~1158 RIVER RD~N BANK ST~ANDERSON FERRY RD~HAZ291~11:55:37^
 (AUTO ALARM-HIGH OCCUPANCY) MANHEIM TWP~115 BLUE STREAK BLVD~SCHOOL RD~VALLEY RD~ENG271,ENG272~15:12:34^
 (HAZ MAT INCIDENT-1A) LANC CITY~753 S PLUM ST~17 ALY~JUNIATA ST~HAZ291~08:19:13^
 
 Contact: Matthew Lenker <strappy39@gmail.com>
 VEH ACCIDENT-ENTRAPMENT-1A / ELIZABETHTOWN BORO~E COLLEGE AVE / S SPRUCE ST~~TRK74,ENG741~08:54:12^\n
 
 Contact: James Taylor <jtaylor574@gmail.com>
 VEH ACCIDENT-CLASS 2 / WEST COCALICO TWP~N KING ST / E QUEEN ST~~RES13~13:10:45^\n
 
 Contact: Justin Fisher <justin62911@gmail.com>
 Sender: messaging@iamresponding.com
 FRM:messaging@iamresponding.com\nSUBJ:WBFC\nMSG:STANDBY-TRANSFER TO STATION\nWEST LAMPETER TWP~2901 WILLOW STREET PIKE~W WYNWOOD DR~DONNELLY DR~ENG906~14:44:13\n
 FRM:messaging@iamresponding.com\nSUBJ:WBFC\nMSG:BUILDING-DWELLING-1A\nPROVIDENCE TWP~417 LANC PIKE~MOUNT AIRY RD~DENNIS DR~ENG906~14:52:09\n
 FRM:messaging@iamresponding.com\nSUBJ:WBFC\nMSG:SPILL CONTROL\nMANOR TWP~2601 RIVER RD~ANCHOR RD~LETORT RD~ENG903,TAN903,BR903,TAN907~16:04:15\n
 
 Contact: Kirstie Breneman <kirstieloveshorses@gmail.com>
 Sender: conf-1012818233@everbridge.net
 (INVESTIGATION-INSIDE) WEST LAMPETER TOWNSHIP~1802 MILLPORT RD~RESCH LN~SOUTHGATE DR~TRK50~22:51:15^ [Attachment(s) removed]
 
Contact: Josiah Einwechter <jceinwechter@gmail.com>
Sender: 911@lcwc911.us
FRM:911@lcwc911.us\nSUBJ:ALARM BELLS\nMSG:DENVER BOROUGH~543 MAIN ST~S 5TH ST~N 6TH ST~ENG132,ENG131~22:16:39^

 */
 
 public class PALancasterCountyParser extends MsgParser {
   
   private static Pattern LANC_PTN = Pattern.compile("\\bLANC\\b", Pattern.CASE_INSENSITIVE);
   
   public PALancasterCountyParser() {
     super("LANCASTER COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "911@lcwc911.us,messaging@iamresponding.com,@everbridge.net,141000";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (! body.contains("~")) return false;
     
     if (subject.indexOf(' ') >= 0 || subject.indexOf('-') >= 0) { 
       data.strCall = subject;
     } else {
       data.strSource = subject;
     }
     
     int ndx = 0;
     for (String line : body.split("~")) {
       ndx++;
       line = line.trim();
       switch (ndx) {
       
       case 1:
         int pt = line.indexOf('/');
         if (pt < 0) pt = line.indexOf('\n');
         if (pt >= 0) {
           data.strCall = append(data.strCall, " / ", line.substring(0,pt).trim());
           line = line.substring(pt+1).trim();
         } else {
           if (data.strCall.length() == 0) return false;
         }
         data.strCity = line;
         if (data.strCity.endsWith(" BORO")) data.strCity = data.strCity.substring(0, data.strCity.length()-5).trim();
        if (data.strCity.endsWith(" BOROUGH")) data.strCity = data.strCity.substring(0, data.strCity.length()-8).trim();
         if (data.strCity.startsWith("LANC")) data.strCity = "LANCASTER";
         break;
         
       case 2:
         line = LANC_PTN.matcher(line).replaceAll("LANCASTER");
         parseAddress(line, data);
         break;
         
       case 3:
         data.strCross = line;
         break;
         
       case 4:
         // Could be second cross st, or if there wasn't a cross street, this
         // would be the unit field
         if (line.length() == 0) break;
         if (data.strCross.length() > 0) {
           data.strCross += " & " + line;
           break;
         }
         ndx++;
         // fall through to case 5
     
       case 5:
         data.strUnit = line;
         break;
       }
     }
     return true;
   }
 }

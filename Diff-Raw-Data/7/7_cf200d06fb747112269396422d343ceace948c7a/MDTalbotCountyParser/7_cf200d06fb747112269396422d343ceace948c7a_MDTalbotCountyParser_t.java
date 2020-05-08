 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /******************************************************************************
 Talbot County, MD
 Contact: "jg0169586@yahoo.com" <jg0169586@yahoo.com>
 Contact: Scott Redmer <sredmer140@gmail.com>
 Sender: msg@cfmsg.com
 
 Talbot 911:*D 61 B-ABDOMINAL PAIN 319 LINDEN AVE T60
 Talbot 911:*D 70 A-CHEST PAIN 5656 BLACK WALNUT POINT RD BREATHING NORMAL <35 T70
 Talbot 911:*D 66 MVA/PI OCEAN GTWY / GOLDSBOROUGH ST T60
 Talbot 911:*D 40C B-FALLS/BACK TRAUMA 103 MILES LN POSS DANGER BODY PT T40
 Talbot 911:*D DOR ASSIST EMS 821 FIELDCREST DR D01
 Talbot 911:*D 61 POLICE REQUEST EMS IDLEWILD PARK @116 IDLEWILD AVE BRAVO RESPONSE T60
 Talbot 911:*D 43 SM NON-DWELLING FIRE 7011 HOPKINS NECK RD SHED/DETACH GARAGE T40
 
Contact: Chris Hash <east66side@gmail.com>
Sender: 4109247197
Talbot 911:*D 62AH C-SMOKE DETECTOR 700 PORT ST @HEART FIELDS AT EAST COMMERCIAL STRUCTURE T60

 The codes following the *D are the box area and then the T__ and such is the
 station number that has the call. We have the following stations in the
 county.
 20  -  Oxford
 30  -  Trappe
 40  -  St. Michaels
 50  -  Cordova
 60  -  Easton
 70  -  Tilghman
 80  -  Queen Ann Hillsboro
 90  -  Talbot County EMS
 
 The D01 is for assisting Dorchester county station 1
 ******************************************************************************/
 
 public class MDTalbotCountyParser extends SmartAddressParser {
   
   private static final Pattern MARKER = Pattern.compile("^Talbot 911:\\*D ");
   
   public MDTalbotCountyParser() {
     super("TALBOT COUNTY", "MD");
   }
   
   @Override
   public String getFilter() {
     return "msg@cfmsg.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     Matcher match = MARKER.matcher(body);
     if (! match.find()) return false;
     body = body.substring(match.end()).trim();
     
     int pt = body.indexOf(' ');
     if (pt < 0) return false;
     data.strBox = body.substring(0,pt);
     body = body.substring(pt+1).trim();
     
     // OK, go do your magic!!
    parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_AT_BOTH, body, data);
     data.strSupp = getLeft();
     return true;
   }
 }

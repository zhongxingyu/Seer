 package net.anei.cadpage.parsers.MI;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Richmond, MI (St Clair and Macomb counties)
 Contact: Erik Haynes <ehaynes078@gmail.com>
 Sender: richmondpaging@comcast.net
 
 ** FIRE ** 1707 WITHEY   VEHICLE FIRE  COLUMBUS
 ** FIRE RESCUE ** PALMS @ GRATIOT  PIA COLUMBUS
 ** MEDICAL ** 35388 DIANE LN  HEART ATTACK
 ** FIRE RESCUE ** PALAM @ GRATIOT  PIA COLUMBUS
 ** FIRE ** 9590 GRATIOT  INJURY ACCIDENT  COLUMBUS TWP
 ** RESCUE ** 10973 GRATIOT SUICIDE  CASCO
 **FIRE RESCUE**  ROLLOVER PI MAYER/GRATIOT. COLUMBUS TWP
 **FIRE RESCUE**  ROLLOVER PI INTERSECTION MAYER AND GRATIOT
 **RESCUE**  CHEST PAINS 35601 WOODSIDE DR
 **RESCUE**  CHEST PAINS 35601 WOODSIDE @ FOREST
 **FIRE**  SHED FIRE LINDSEY @ PALMS.  SHED CLOSE TO HOME
 **FIRE**  VEHICLE FIRE 31 MILE @ COUNTY LINE
 **FIRE** ODOR INVESTIGATION 34901 DIVISION. SMELL OF NATURAL GAS.
 **FIRE** MUTUAL AIDE TO MEMPHIS, TANKER AND MANPOWER TO DOLAN & BAUMAN
 **RESCUE**  CPR IN PROGRESS.  35388 DIANE.
 **FIRE** ARMADA RIDGE @ WELDING.  BIRD HOUSE FULLY INVOLVED
 **RESCUE** FRESHMAN STUFFED IN LOCKER.  DIVISION @ MAIN.
 **TEST**  I-94/CHURCH RD,  CASCO. ROLLOVER ACCIDENT
 
 */
 
 public class MIRichmondParser extends SmartAddressParser {
   
   private static String[] CITY_LIST = new String[]{
     "CASCO",
     "COLUMBUS",
     "COLUMBUS TWP",
     "PIA COLUMBUS"
   };
   private static Set<String> CITY_SET = new HashSet<String>(Arrays.asList(CITY_LIST));
   
   public String getFilter() {
    return "CAD@livingstoncounty.livco";
   }
   
   public MIRichmondParser() {
     super(CITY_LIST, "", "MI");
   }
   
  private static final Pattern MARKER = Pattern.compile("^\\*\\*([A-Z ]+)\\*\\*");
   private static final Pattern[] ADDRESS = new Pattern[]{ 
     Pattern.compile("(?:(.*) )?(\\b\\d+\\b.*)"),
     Pattern.compile("(?:(.*) )?(\\b[-A-Z]+(?: LN| RD | ST| AV)? ?&.*)")
   };
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     // Only think we can really count on in the start signature containing the call description
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     data.strCall = match.group(1).trim();
     body = body.substring(match.end()).trim();
     
     // Replace periods and commas with blanks
     // Replace @ with &
     body = body.replace('.', ' ');
     body = body.replace(',', ' ');
     body = body.replace('@', '&');
     body = body.replace('/', '&');
     body = body.trim();
     
     // Split into fields separated by multiple blanks
     String[] flds = body.split("  +");
     for (String field : flds) {
       
       // See if we can identify this as a city
       if (CITY_SET.contains(field)) {
         if (field.equals("PIA COLUMBUS")) field = "COLUMBUS";
         data.strCity = field;
         continue;
       }
       
       // See if (fat chance) the smart address parser can find an address
       Result res = parseAddress(StartType.START_PLACE, field);
       if (res.getStatus() > 0) {
         res.getData(data);
         data.strCall = append(data.strCall, " - ", data.strPlace);
         data.strPlace = "";
         data.strCall = append(data.strCall, " - ", res.getLeft());
         continue;
       }
       
       // See if it matches one of our address patterns
       boolean found = false;
       for (Pattern ptn : ADDRESS) {
         match = ptn.matcher(field);
         if (match.matches()) {
           data.strCall = append(data.strCall, " - ", getOptGroup(match.group(1)));
           parseAddress(getOptGroup(match.group(2)), data);
           found = true;
           break;
         }
       }
       if (found) continue;
       
       // Otherwise treat it as a call description
       data.strCall = append(data.strCall, " - ", field);
     }
     
     return true;
   }
 }

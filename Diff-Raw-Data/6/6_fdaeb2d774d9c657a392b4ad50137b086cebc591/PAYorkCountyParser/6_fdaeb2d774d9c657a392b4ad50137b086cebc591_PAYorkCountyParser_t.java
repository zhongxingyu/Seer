 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 York County, PA
 Contact: wrblackwell@comcast.net
 Contact: "Matthew Houck" <gr8tff@comcast.net> (megabytes of text samples)
Contact: "firefighter_24@netzero.net" <firefighter_24@netzero.net>
 Sender: FIRE@mantwp.com
 
 NORTH CODORUS TWP \n\n1983 SLAGEL RD STOVERSTOWN RD / VIEW DR FIRE STRUCT RESID Working Fire 12-01 S99-1 RIT1 17:05 
 YORK TWP COUNTRY MEADOWS \n\n2760 PINE GROVE RD 2760 PINE GROVE RD LEADER HEIGHTS RD, PINE GROVE CIR / PINE GROVE CMN FIRE AFA HIGH RISK general alm from the Leader Heights Building 19-04 FIRESTA19 E19 E18-1 E89-2 E35-2 TK19 TK34 EMSSTA18 A18 12:22 
 PENN TWP \n\n1060 WILSON AVE OLD RIDGE RD / YORK ST, GRANDVIEW RD FIRE VEHICLE HR mulching machine on fire 49-17 FIRESTA49 E49-2 E46-3 E49-1 
 YORK CITY \n\n900 E KING ST S TREMONT ST / S CAMBRIDGE ST HAZMAT NO INJ hit gas service outside residence 99-06 FIRESTA99 E99-5 E99-1 TK99-1 DO99 15:23
 MANCH TWP METRO BANK MANCH TWP \n\n55 ARSENAL RD I 83X21BARSENALRD NB / I 83 NB VEH ACC INJ CL 2 30/F/leaking fluids 24-07 FIRESTA24 E24-1 R24  13:20
 HELLAM TWP \n\n4045 CODORUS FURNACE RD RIVER FARM RD / JENNIFER LN LANDING ZONE Jennifer and Furnace for LZ 21-13 R24  11:08
 Fire Incident / YORK TWP I83 SB EXIT 16A\n\nI 83 SB I 83X16ASQUEENST SB FIRE VEHICLE HR cancel call 19-832 UFIRE24 UFIRE25 UEMS24  00:27¿\n\n\n
 MANCH TWP CODORUS STONE COMPANY\n\n135 MUNDIS RACE RD EMIG RD, MUNDIS LN / DELLINGER RD FIRE AFA HIGH RISK / 2nd floor smoke /  24-03 FIRESTA24 E24-1 E89-3 TK24 EMSSTA24 A23-2  11:58
 MANCH TWP GRAHAM PKG 3280 FARMTRAIL RD\n\n3280 FARMTRAIL RD CHURCH RD / FARMBROOK LN FIRE AFA HIGH RISK general fire alarm 24-10 FIRESTA24 E24-1 E23-1 TK24 EMSSTA24 A24-2  04:41
 Fire Incident / MANCH TWP \n\nCHURCH RD BOARD RD DEBRIS REMOVAL veh acc no inj leaking 24-02 FIRESTA24 E24-1  18:17¿\n\n\n
Subject:Fire Incident\nMANCH TWP    975 DETWILER DR BELAIR WAY / STILLMEADOW LN FIRE STRUCT RESID shed on fire 24-14 FIRESTA24 E24-1 E2-1 TK24 R
 
 */
 
 public class PAYorkCountyParser extends SmartAddressParser {
   
  private static final Pattern MASTER = Pattern.compile("(.*)(?:\n\n|    )(.*) (\\d{2}-\\d{2,3}) (.*?)(?: \\d\\d:\\d\\d.?)?");
   private static final Pattern CITY_PTN = Pattern.compile("^.*? (?:CITY|BORO|TWP)\\b");
   private static final Pattern SRC_PTN = Pattern.compile("^(FIRESTA\\d+) ");
   
   public PAYorkCountyParser() {
     super("YORK COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "FIRE@mantwp.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     String part1 = match.group(1).trim();
     String part2 = match.group(2).trim();
     data.strMap = match.group(3).trim();
     String part3 = match.group(4).trim();
     
     // work on the message header
     // May contain a call description followed by slash
     int pt = part1.indexOf(" / ");
     if (pt >= 0) part1 = part1.substring(pt+3).trim();
     
     // Rest should be a city name followed by an optional place name
     // City names always end in BORO, CITY or TWP (we hope)
     match = CITY_PTN.matcher(part1);
     if (match.find()) {
       data.strCity = match.group();
       data.strPlace = part1.substring(match.end()).trim();
     } else {
       data.strCity = part1;
     }
     if (data.strCity.equals("MANCH TWP")) data.strCity = "MANCHESTER TWP";
     
     // Message body consists of an address followed by a description
     part2 = part2.replace(',', ' ');
     parseAddress(StartType.START_ADDR, FLAG_IMPLIED_INTERSECT, part2, data);
     part2 = getLeft();
     
     // The address may be a simple address followed by a cross street
     if (part2.startsWith("/")) part2 = part2.substring(1).trim();
     parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS | FLAG_IMPLIED_INTERSECT, part2, data);
     
     data.strCall = getLeft();
     if (data.strCall.length() == 0) {
       data.strCall = data.strCross;
       data.strCross = "";
     }
     
     // last part may contain a station in first position.  Everything else is unit
     match = SRC_PTN.matcher(part3);
     if (match.find()) {
       data.strSource = match.group(1);
       part3 = part3.substring(match.end()).trim();
     }
     data.strUnit = part3;
     
     return true;
   }
 }

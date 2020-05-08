 //Sender: rc.263@c-msg.net
 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Charles County, MD
 Contact: Darrin Tomlinson <lackeychargers08@verizon.net>
 Contact: Emmitt Newman <bigpickles25@gmail.com> ,3013998478@messaging.sprintpcs.com
 Contact: Mark Kaufmann <markeng1trk@gmail.com>
 System: Keystone
 Sender: rc.263@c-msg.net
 
 (*CAD*) [CAD] UNDETERMINED FIRE MIDDLETOWN RD / LYLES PL 3H11-9G1 CALLER LIVES ON CADWELL ST AND CAN SEE A LARGE FIRE THRU THE WOODS...UNSURE WHAT IT IS F103490
 (*CAD*) [CAD] TROUBLE BREATHING, RESPIRATORY DISTRESS, EMS, ALS, 6C, 6D 3465 LAUREL DR 7 H3 65 year old, Female, Conscious, Breathing. Breathing Problems. Abnor
 (*CAD*) [CAD] CO DETECTOR, CARBON MONOXIDE, 8A 2204 JASON CT 7 K2 CALLER ADVISED HIS BSMT CO DETECTOR IS GOING OFF. F103510001 1022578 02:01
 (*CAD*) [CAD] UNKNOWN MEDICAL ALARM, EMS, BLS, 32B 3487 HIGHGROVE DR 8 H5 Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Unknown 
 (*CAD*) [CAD] 10-50 PI, EMS, BLS, 29A HAWTHORNE RD / CHICAMUXEN RD 7 F11 Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Unknown n
 (*CAD*) [CAD] MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT, 130 KALMIA CT 17 F6 DRYER ON FIRE / ADVISED TO GET OUT F103550006 1022816 12:07
 Subject:*CAD*\n[CAD] GAS LEAK INSIDE\nA S/F SINGLE FAMILY DWELLING 5748 OAK CT 7 E11 SMELLS GAS INSIDE HOME AND IN GARAGE F103640012 102
 Subject:*CAD*\n[CAD] 10-50 PI EXTRICATION, TRAPPED, PINNED, EMS, ALS, 29B, 29D 9275 IRONSIDES RD RT 425, MT HOPE ELEM SCHOOL A/O 23 H4-
 Subject:*CAD*\n[CAD] GAS LEAK\nINSIDE A S/F SINGLE FAMILY DWELLING 5748 OAK CT 7 E11 SMELLS GAS INSIDE HOME AND IN GARAGE F103640012 102
 Subject:*CAD*\n[CAD] 10-50 PI FIRE EMS APPARATUS TRUCK AMBULANCE HAWTHORNE RD / MANOR LA 15 J3 10-17 ADV SHE WAS ASSAULTED BY HER FRIEN
 Subject:*CAD*\n[CAD] 10-50 PI, EMS, BLS, 29A HAWTHORNE RD / CHICAMUXEN RD 7 F11 Age unknown, Gender unknown, Consciousness unknown, Bre
 Subject:*CAD*\n[CAD] COMMERCIAL BUILDING FIRE, STRUCTURE, BLDG, 4975 HAWTHORNE RD, PENNYS TAVERN NEXT TO 7 C7 NEXT TO BLDG, CHILD CARE
 Subject:*CAD*\n[CAD] UNDETERMINED FIRE INNSBRUCK CT / MASONS SPRING RD 14 K7 CALLER REPORTING LARGE FIRE VISIBLE A/F INNBRUCK CT,UNK WH
 Subject:*CAD*\n[CAD] 10-50 PI MOTORCYCLE, EMS, ALS, ATV, BICYCLE, BIKE, 29B, 29D RIPLEY RD, 2 MILES FROM ANNAPOLIS WOODS RD 15 F6 Age u
 Subject:*CAD*\n[CAD] MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT, 1 HICKORY LN, APT #307 17 G6 CALLER ADVISED HAS ODOR OF SMOKE
 Subject:*CAD*\n[CAD] MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT, 28 FAIRHILL LN 6 H6 ODER OF SMOKE IN EQUIPMENT ROOM. F10338000
 Subject:*CAD*\n[CAD] AFA - SINGLE FAMILY DWELLING, FIRE ALARM, S/F HOUSE 6445 MASON SPRINGS RD 15 A4 HALLWAY S/D, PHONE WAS PUT DOWN, N
 
 (*CAD*) [CAD] ASSIST THE AMBULANCE, MISC 10200 LA PLATA RD (RT 488), CCNH 109B 17 H6-K4 NEEDED FOR MANPOWER F110610005 1103803 11:10
 
  */
 
 public class MDCharlesCountyParser extends SmartAddressParser {
   
   private static final Pattern UNIT_PATTERN = Pattern.compile("(,? +(EMS|ALS|BLS|APPARATUS|TRUCK|AMBULANCE|MOTORCYCLE|ATV|BICYCLE|BIKE|MISC|\\d{1,2}[A-D]))+");
  private static final Pattern MAP_PATTERN = Pattern.compile("\\b\\d{1,2} [A-Z]\\d{1,2}(?:-[A-Z]\\d{1,2})?\\b");
   private static final Pattern ID_PATTERN = Pattern.compile("\\bF\\d{9}\\b");
   
   @Override
   public String getFilter() {
     return "rc.263@c-msg.net";
   }
 
 
   public MDCharlesCountyParser() {
     super( "CHARLES COUNTY", "MD");
 
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (! subject.equals("CAD") && ! subject.equals("*CAD*")) return false;
     
     Matcher match = ID_PATTERN.matcher(body);
     if (match.find()) {
       data.strCallId = body.substring(match.start(),match.end());
       body = body.substring(0,match.start()).trim();
     }
     
     // There is almost always a code pattern (or whatever it really is)
     // marking the end of the address
    int mapSt = -1;
    int mapEnd = -1;
    match = MAP_PATTERN.matcher(body);
     if (match.find()) {
      mapSt = match.start();
      mapEnd = match.end();
     }
     
     // And we can usually find a unit pattern marking the beginning of the address
     // There might be more than one, pick the last one in front of the code pattern
     int unitSt = -1;
     int unitEnd = -1;
     match = UNIT_PATTERN.matcher(body);
     while (match.find()) {
      if (mapSt >= 0 && match.end() >= mapSt) break;
       unitSt = match.start();
       unitEnd = match.end();
     }
     
     // If there was a code match, strip off the code and trailing info
    if (mapSt >= 0) {
      data.strMap = body.substring(mapSt, mapEnd);
      data.strSupp = body.substring(mapEnd).trim();
      body = body.substring(0,mapSt).trim();
     }
     
     // If there was a unit match, strip off the leading call description and unit
     if (unitSt >= 0) {
       data.strUnit = body.substring(unitSt, unitEnd).trim();
       if (data.strUnit.startsWith(", ")) data.strUnit = data.strUnit.substring(2);
       data.strCall = body.substring(0, unitSt);
       body = body.substring(unitEnd).trim();
       
     }
     
     // If we have both a unit and code mark, what is left is an address
     // with a possible comma separated place
     // Dummy loop to break out of
     do {
      if (mapSt >= 0 && unitSt >= 0) {
         Parser p = new Parser(body);
         parseAddress(p.get(','), data);
         data.strPlace = p.get();
         break;
       }
       
       // We have some special logic if we have the end of the address marked
       // and we have comma separated fields in what is left
      if (mapSt >= 0) {
         int pt = body.lastIndexOf(',');
         if (pt >= 0) {
           
           // Split out last field.  If it contains a valid address, make it so
           String fld1 = body.substring(0,pt).trim();
           String fld2 = body.substring(pt+1).trim();
           Result res2 = parseAddress(StartType.START_CALL, FLAG_ANCHOR_END, fld2);
           if (res2.getStatus() > 0) {
             res2.getData(data);
             data.strCall = append(fld1, ", ",  data.strCall);
             break;
           } 
 
           // If not, turn it into a place name and parse what is left
           data.strPlace = fld2;
           body = fld1;
         }
       } 
       
       // Otherwise we have to use the smart parser to separate out what we didn't get
       StartType start = (unitSt >= 0 ? StartType.START_ADDR : StartType.START_CALL);
       // Otherwise we have to use the smart parser to separate out what we didn't get
      int flags = (mapSt >= 0 ? FLAG_ANCHOR_END : 0);
       parseAddress(start, flags, body, data);
 
       if (data.strCall.endsWith(",")) data.strCall = data.strCall.substring(0, data.strCall.length()-1).trim();
       
       // If We end up with a comma in the address field, it must be separating a place name
       int pt = data.strAddress.indexOf(',');
       if (pt >= 0) {
         data.strPlace = data.strAddress.substring(pt+1).trim();
         data.strAddress = data.strAddress.substring(0,pt).trim();
       }
     } while (false);
     
     // If the place name we found looks like an apt number, move it
     if (data.strPlace.toUpperCase().startsWith("APT ")) {
       data.strApt = data.strPlace.substring(4).trim();
       data.strPlace = "";
     } else if (data.strPlace.startsWith("#")) {
       data.strApt = data.strPlace;
       data.strPlace = "";
     }
     
     return true;
   }
 }

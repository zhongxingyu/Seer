 package net.anei.cadpage.parsers.DE;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Kent County, DE (Variant A)
 Contact: John Wothers <eng416res@yahoo.com>
 Sender: rc.187@c-msg.net
 System: New World Systems
 
 [!]  [K] 12D1 Seizure - Not Breathing 263 MORGANS CHOICE RD Camden Wyoming : Xst's: WILLOW GROVE RD / MUMFIELD LN Caller: COLE JR EARL
 [!]  [K] Alarms-Residential (single) 121 BIRKDALE LN Camden Wyoming : TRAYBERN Xst's: BIRKDALE WAY / N HIGH HILL RD, TRAYBERN WAY Caller: ADT
 [!]  [K] Structure-Chimney 6871 WILLOW GROVE RD Camden Wyoming : Xst's: HENRY COWGILL RD / WILLOW TREE CIR Caller: PETE
 [!]  [K] 31D2 Unconscious/Fainting 10829 WESTVILLE RD Camden Wyoming : Xst's: UNKNOWN / GREENS VILLAGE DR Caller: JOHNSON WANDA
 [!]  [K] 29B1 MVC Injuries HENRY COWGILL RD Camden Wyoming : Xst's: Caller:
 [!]  [K] Alarms-Residential (single) 233 TOWER RD Camden Wyoming : Xst's: UNKNOWN / DARLING FARM RD Caller: GUARDIAN TINA
 [!]  [K] Stand-By,Cover-up 2 N MAIN ST STATION 55 Magnolia : MAGNOLIA Xst's: W WALNUT ST, S MAIN ST, E WALNUT ST / BOOTY DR Caller:
 [!]  [K] 17B3G Falls - Unknown 852 STEELES RIDGE RD Camden Wyoming : Xst's: ASHLAND AVE / HENRY COWGILL RD, MAIN ST Caller: GARY
 [!]  [K] Alarms-Commercial 1678 S GOVERNORS AVE Dover : Xst's: S DUPONT HWY / GUNNING BEDFORD DR Caller: DAVID
 [!]  [K] 29D2L MVC Motorcycle/Bicycle 375 GATEWAY SOUTH BLVD SONIC - SOUTH DOVER Dover : Xst's: DEAD END / DEAD END Caller:
 [!]  [K] Structure-Residential Single 241 E CAMDEN WYOMING AVE Camden Wyoming : CAMDEN Xst's: S MAIN ST, N MAIN ST / S WEST ST, N WEST ST Caller: HARRISON THERE'S
 
Contact: Tom <tw4715@comcast.net>
[CAD]33C1T Transfer/Interfacility 2809 ANDREWVILLE RD Harrington : Xst's: TODDS CHAPEL RD, PROSPECT CHURCH RD / GREENWOOD RD Caller: CAY HOLLY

  */
 
 
 public class DEKentCountyAParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile("[^ ]*:");
   
   public DEKentCountyAParser() {
     super(CITY_LIST, "KENT COUNTY", "DE",
            "ADDR/SCXP Xsts:X CALLER:NAME");
   }
   
   @Override
   public String getFilter() {
     return "rc.187@c-msg.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    if (!subject.equals("!|K") && !subject.equals("CAD")) return false;
     body = body.replace("Xst's:", "Xsts:");
     return parseFields(splitMsg(body), data);
   }
   
   private String[] splitMsg(String body) {
     List<String> list = new ArrayList<String>();
     Matcher match = DELIM.matcher(body);
     int pt = 0;
     String key = "";
     while (match.find()) {
       list.add((key + body.substring(pt,match.start())).trim());
       pt = match.end();
       key = match.group();
       if (key.length() == 1) key = "";
     }
     String tail = body.substring(pt);
     if (tail.length() > 0) list.add(tail);
     return list.toArray(new String[list.size()]);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "CAMDEN WYOMING",
     "DOVER",
     "HARRINGTON",
     "MILFORD",
     "BOWERS",
     "CAMDEN",
     "CHESWOLD",
     "CLAYTON",
     "FARMINGTON",
     "FELTON",
     "FREDERICA",
     "HARTLY",
     "HOUSTON",
     "KENTON",
     "LEIPSIC",
     "LITTLE CREEK",
     "MAGNOLIA",
     "SMYRNA",
     "VIOLA",
     "WOODSIDE",
     "WYOMING"
   };
 }
 
 

 package net.anei.cadpage.parsers.WA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Clark County, WA
 Contact:  Doug Gelsleichter <dgelsleichter@ecfr.us>
 Sender: 777171393073
 
 CRESA LOC: 3615 NE EVERETT ST CAM MAP: 1302 OPS:   SEIZURE SUB TYPE: A1 PRI: 4 TIME: 18:51:00 EV#: 12166142 ALARM: 1  Disp: E41,E91,M41
 CRESA LOC: 1315 NE 310TH AVE CLK MAP: 2430 OPS:   PUBLIC ASSIST/SERVICE CALL SUB TYPE: BU PRI: 4 TIME: 07:42:36 EV#: 12166548 ALARM: 1  Disp: SQ91
 CRESA LOC: 39801 NE 26TH ST CLK: @STATION 96 S96 MAP: 2424 OPS:   PUBLIC ASSIST/Service Call SUB TYPE: A PRI: 5 TIME: 15:36:47 EV#: 12168096 ALARM: 1 Disp: SQ96
 CRESA LOC: 26719 NE HATHAWAY RD CLK MAP: 2326 OPS:   FALLS SUB TYPE: A1 PRI: 5 TIME: 11:41:49 EV#: 12167852 ALARM: 1  Disp: E91,M45
 CRESA LOC: NE 261ST AVE/NE 38TH ST CLK MAP: 2323 OPS:   UNKNOWN PROBLEM SUB TYPE: B3 PRI: 3 TIME: 07:52:21 EV#: 12168775 ALARM: 1  Disp: E91,M45
 CRESA LOC: 38900 SE NICHOLS HILL RD CLK MAP: 1402 OPS:   CARDIAC OR RESPIRATORY ARREST / DEATH SUB TYPE: D1 PRI: 1 TIME: 12:46:01 EV#: 12169964 ALARM: 1  Disp: C91,E171,E94,M43
 CRESA LOC: 2516 NE 277TH AVE CLK MAP: 2324 OPS:   TRAFFIC ACCIDENT SUB TYPE: B4 PRI: 3 TIME: 15:59:57 EV#: 12170126 ALARM: 1  Disp: E91,M41,SQ91
 CRESA LOC: 3600 NE CAVITT RD CLK MAP: 2324 OPS:   TRAFFIC ACCIDENT SUB TYPE: B4 PRI: 3 TIME: 15:59:57 EV#: 12170126 ALARM: 1  Disp: C91,E91,M41,SQ91
 CRESA LOC: 26801 NE 9TH ST CLK,SP 50: @OAK MEADOWS MOBILE PARK MAP: 2326 OPS: OPS51  STRUCTURE FIRE SUB TYPE: R PRI: 1 TIME: 17:20:40 EV#: 12170184 ALARM: 1  Disp: A93,E171,E41,E42,E91,RE93,WT91,WT93
 CRESA LOC: 936 NE 41ST AVE CAM MAP: 2335 OPS:   PSYCHIATRIC / SUICIDE ATTEMPT SUB TYPE: B6V PRI: 3 TIME: 21:45:53 EV#: 12170453 ALARM: 1  Disp: E41,E91,M41
 CRESA LOC: 33008 NE WASHOUGAL RIVER RD CLK MAP: 2429 OPS:   ASSAULT SUB TYPE: A1 PRI: 4 TIME: 21:56:35 EV#: 12171336 ALARM: 1  Disp: E94,M41
 CRESA LOC: NE 3RD ST/NE 267TH AVE CLK MAP: 2326 OPS: OPS51  TRAFFIC ACCIDENT SUB TYPE: D2L PRI: 2 TIME: 11:39:51 EV#: 12171765 ALARM: 1  Disp: C91,E41,E91,M41
 CRESA LOC: 28416 NE REILLY RD CLK MAP: 2324 OPS:   STROKE SUB TYPE: C1L PRI: 2 TIME: 13:39:23 EV#: 12171853 ALARM: 1  Disp: E91,M41
 CRESA LOC: 605 SE TAMPA ST CAM MAP: 1312 OPS:   BREATHING PROBLEMS SUB TYPE: D1 PRI: 2 TIME: 16:01:38 EV#: 12171988 ALARM: 1  Disp: E42,E91,M45
 CRESA LOC: 4354 V ST WAS MAP: 1409 OPS:   BREATHING PROBLEMS SUB TYPE: D2 PRI: 2 TIME: 18:25:20 EV#: 12172139 ALARM: 1  Disp: E94,M43
 CRESA LOC: 600 NE 267TH AVE CLK: @STATION 91 S91 MAP: 2326 OPS:   PUBLIC ASSIST/Service Call SUB TYPE: T PRI: 5 TIME: 09:48:03 EV#: 12172571 ALARM: 1  Disp: E91
 CRESA LOC: 1922 SE BLAIR RD CLK MAP: 1405 OPS:   CHEST PAIN SUB TYPE: D2 PRI: 2 TIME: 14:16:12 EV#: 12172800 ALARM: 1  Disp: E91,M43
 
Contact: "C. Ebeling" <cebeling@iinet.com>
Sender: ipage@clark.wa.gov
LOC: 2210 NE 284TH AVE CLK MAP: 2324 OPS:   UNKNOWN PROBLEM SUB TYPE: B2 PRI: 4 TIME: 09:37:54 EV#: 12210465 ALARM: 1  Disp: E91,M43\r\n

 */
 
 
 public class WAClarkCountyParser extends FieldProgramParser {
   
   public WAClarkCountyParser() {
     super(CITY_CODES, "CLARK COUNTY", "WA",
           "SRC LOC:ADDR/S! MAP:MAP! OPS:CALL! SUB_TYPE:CODE! PRI:PRI! TIME:TIME! EV#:ID! ALARM:SKIP! Disp:UNIT!");
   }
   
   @Override
   public String getFilter() {
     return "777";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!super.parseMsg(body, data)) return false;
     if (data.strCity.equals("Clark County")) data.strCity = "";
     return true;
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strPlace = p.getLastOptional(": @");
       data.strApt = p.getLastOptional(',');
       super.parse(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "BG",   "Battleground",
       "CAM",  "Camas",
       "CLK",  "Clark County",
       "CPD",  "Camas",
       "LCPD", "La Center",
       "RPD",  "Ridgefield",
       "VAN",  "Vancouver",
       "VPD",  "Vancouver",
       "WAS",  "Washougal",
       "WPD",  "Washougal"
       
   });
 }

 package net.anei.cadpage.parsers.NC;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Cabarrus County, NC
 Contact: Michael Martocchio <maccaronimartocchio@gmail.com>
 Contact: "codymac08@yahoo.com" <codymac08@yahoo.com>
 Contact: Travis McGhee <mcgheetp@gmail.com>
 Contact: ryan sellers <rsellers43@gmail.com>
Contact: Jimmy Greene <fireman11607@yahoo.com>
 Sender: CAD@cabarruscounty.us,930xxxxxxx
 
 CAD:ASSAULT W/ INJURY;2339 ODELL SCHOOL RD;CON;CONCORD HOUSE;HILLSDELL DR;LOCKWOOD RD;RAINTREE, HEALTH CARE OF CONCO
 CAD:BREATHING PROBLEMS;5412 ASHBURY LN;DAV;TERRILL RIDGE DR;TERRILL RIDGE DR;BRACEY III JOSEPH O     
 CAD:STRUCTURE FIRE;1606 DUCKHORN ST NW;CON;From CPD:;RAVENSCROFT LN NW;INDIAN BEECH AV NW;BUCKELEW, HEATHER     
 CAD:LOCAL RESPONSE;509 THREE GREENS DR;HUN;WOODHALL DR;VINTAGE HILL LN
 CAD:ASSAULT W/ INJURY;2339 ODELL SCHOOL RD;CON;CONCORD HOUSE;HILLSDELL DR;LOCKWOOD RD;RAINTREE, HEALTH CARE OF CONCO   
 CAD:FIRE STANDBY;4240 SHILOH CHURCH RD;DAV;ODELL VFD STATION 2;MARIPOSA PL;ODELL SCHOOL RD;KPD   
 CAD:FALL WITH INJURY;1087 ODELL SCHOOL RD;CONCORD;UNTZ RD;ARBOR COMMONS LN 
 CAD:FALL WITH INJURY;8552 INDIAN SUMMER TR;HAR;TOM SAVAGE DR
 CAD:DAVIDSON RD/MOORESVILLE RD;DAV;SUSPICIOUS VEHICLE
 CAD:CANCEL;DAVIDSON RD/MOORESVILLE RD; DAV
 CAD:ASSAULT W/ INJURY;5048 DAFFODIL LN;CON;BRAXTON DR;SABLE CT;BRITTANY STIREWALT
CAD:HEART PROBLEMS \A.I.C.D;9900 ARBORDALE AV;CHL;MIDVALE TER;BRANCH ST;BUCKALOO, PATRICIA
 
 Contact: Kevin Aube <aubercn41@aol.com>
 Contact: David Winecoff <dwinecoff6@gmail.com>
 2847:CAD:OPS2,A3,E21,E41;1;MVC INJURY- NO PIN IN/ENTRAP;2500 S MAIN ST;K401;HAMPTON LN;SPRINGWAY DR;JORDAN HOT SPOT;{FROM CSO: CALLER HUNG UP ON ME [11/27/11 14:30:54 PSBLACKWELL]} [11/27/11 14:28:05 TRANSFER] ** Transfer from CSO ** CSO received: 11/27/2011 14:30:17 Source: PHONE Nature: TRAFFIC ACCIDENT PI
 2845:CAD:A2,E11;1;RESPIRATORY DISTRESS;1307 POPLAR GLEN DR;K102;CANEVIEW CT;73 YO [11/27/11 12:25:16 HSTEPHENS]
 2846:CAD:A2,E31,E41,OPS1;1;MVC INJURY- NO PIN IN/ENTRAP;2400 WONDER DR NE/ROXIE ST;K303;** Transfer from CSO ** CSO received: 11/27/2011 14:04:46 Source: PHONE Nature: TRAFFIC ACCIDENT PI Business: MCDONALDS Phone:7047865822 Call #: 752 Event Id: 11094816 Console: CBA4 Call Take
 2847:CAD:OPS2,A3,E21,E41;1;MVC INJURY- NO PIN IN/ENTRAP;2500 S MAIN ST;K401;HAMPTON LN;SPRINGWAY DR;JORDAN HOT SPOT;{FROM CSO: CALLER HUNG UP ON ME [11/27/11 14:30:54 PSBLACKWELL]} [11/27/11 14:28:05 TRANSFER] ** Transfer from CSO ** CSO received: 11/27/2011 14:30:17 Source: PHONE Nature: TRAFFIC ACCIDENT PI
 2848:CAD:A2,E21;1;RESPIRATORY DISTRESS;110 WESTOVER AV;K201;RENA ST;BEUNA ST;** Transfer from CSO ** CSO received: 11/27/2011 16:50:57 Source: E911 Nature: BREATHING PROBLEMS Call #: 792 Event Id: 11094854 Console: CBA7 Call Taker: SADEMPSEY Notes: [11/27/11 16:48:28
 4311:CAD:B1,E31;1;SEIZURES;902 VIRGINIA ST;K303;PENNSYLVANIA AV;FLORIDA AV;TERRI CLARK;** Transfer from CSO ** CSO received: 11/25/2011 20:27:26: E911 Nature: CONVULSIONS \ SEIZURES Call #: 357 Event Id: 11094423 Console: CBA2 Call Taker: SVWHITLEY Notes: [11/25/11 20:2
 2861:CAD:BC1,L14,A3,E21,OPS1,E12;1;FIRE ALARM- NO WATER FLOW;1445 OAKWOOD AV;K203;ROBINHOOD LANE EXT;WOODMOORE LN;KANNAPOLIS MIDDLE SCHOOL;pull station room 103 [11/30/11 09:05:34 HSTEPHENS]
 
 */
 
 public class NCCabarrusCountyAParser extends DispatchOSSIParser {
   
   private static final Pattern OPER_PTN = Pattern.compile("^\\d+:");
   
   @Override
   public String getFilter() {
     return "CAD@cabarruscounty.us, 93001";
   }
   
   public NCCabarrusCountyAParser() {
     super(CITY_CODES, "CABARRUS COUNTY", "NC",
            "( UNIT PRI CALL! ADDR! MAP X X? INFO+ | CALL! ADDR! CITY PLACE? X X NAME )");
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = OPER_PTN.matcher(body);
     if (match.find()) body = body.substring(match.end()).trim();
     return super.parseMsg(body, data);
   }
   
   private static final Pattern UNIT_PTN = Pattern.compile("[A-Z]+[0-9]+");
   private class MyUnitField extends UnitField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       String[] terms = field.split(",");
       StringBuffer units = new StringBuffer();
       StringBuffer chan = new StringBuffer();
       for (String term : terms) {
         if (!UNIT_PTN.matcher(term).matches()) return false;
         if (term.startsWith("OPS")) {
           if (chan.length() > 0) chan.append(',');
           chan.append(term);
         } else {
           if (units.length() > 0) units.append(',');
           units.append(term);
         }
       }
       data.strUnit = units.toString();
       data.strChannel = chan.toString();
       return true;
     }
     
     @Override
     public String getFieldNames() {
       return "UNIT CH";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("MAP")) return new MapField("[A-Z]\\d{3}");
     return super.getField(name);
   }
   
   private static Properties CITY_CODES = buildCodeTable(new String[]{
       "HAR",  "HARRISBURG",
       "HUN",  "HUNTERSVILLE",
       "MP",   "MT PLEASANT",
       "CON",  "CONCORD",
       "KAN",  "KANNAPOLIS",
       "DAV",  "DAVIDSON",
       "LOC",  "LOCUST",
      "CHL",  "CHARLOTTE",
       "CLT",  "CHARLOTTE",
       "MID",  "MIDLAND"
   });
 }

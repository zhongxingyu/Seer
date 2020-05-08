 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCCabarrusCountyAParserTest extends BaseParserTest {
   
   public NCCabarrusCountyAParserTest() {
     setParser(new NCCabarrusCountyAParser(), "CABARRUS COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:ASSAULT W/ INJURY;2339 ODELL SCHOOL RD;CON;CONCORD HOUSE;HILLSDELL DR;LOCKWOOD RD;RAINTREE, HEALTH CARE OF CONCO",
         "CALL:ASSAULT W/ INJURY",
         "ADDR:2339 ODELL SCHOOL RD",
         "CITY:CONCORD",
         "PLACE:CONCORD HOUSE",
         "X:HILLSDELL DR & LOCKWOOD RD",
         "NAME:RAINTREE, HEALTH CARE OF CONCO");
 
     doTest("T2",
         "CAD:BREATHING PROBLEMS;5412 ASHBURY LN;DAV;TERRILL RIDGE DR;TERRILL RIDGE DR;BRACEY III JOSEPH O",
         "CALL:BREATHING PROBLEMS",
         "ADDR:5412 ASHBURY LN",
         "CITY:DAVIDSON",
         "X:TERRILL RIDGE DR & TERRILL RIDGE DR",
         "NAME:BRACEY III JOSEPH O");
 
     doTest("T3",
         "CAD:STRUCTURE FIRE;1606 DUCKHORN ST NW;CON;From CPD:;RAVENSCROFT LN NW;INDIAN BEECH AV NW;BUCKELEW, HEATHER",
         "CALL:STRUCTURE FIRE",
         "ADDR:1606 DUCKHORN ST NW",
         "CITY:CONCORD",
         "PLACE:From CPD:",
         "X:RAVENSCROFT LN NW & INDIAN BEECH AV NW",
         "NAME:BUCKELEW, HEATHER");
 
     doTest("T4",
         "CAD:LOCAL RESPONSE;509 THREE GREENS DR;HUN;WOODHALL DR;VINTAGE HILL LN",
         "CALL:LOCAL RESPONSE",
         "ADDR:509 THREE GREENS DR",
         "CITY:HUNTERSVILLE",
         "X:WOODHALL DR & VINTAGE HILL LN");
 
     doTest("T5",
         "CAD:ASSAULT W/ INJURY;2339 ODELL SCHOOL RD;CON;CONCORD HOUSE;HILLSDELL DR;LOCKWOOD RD;RAINTREE, HEALTH CARE OF CONCO",
         "CALL:ASSAULT W/ INJURY",
         "ADDR:2339 ODELL SCHOOL RD",
         "CITY:CONCORD",
         "PLACE:CONCORD HOUSE",
         "X:HILLSDELL DR & LOCKWOOD RD",
         "NAME:RAINTREE, HEALTH CARE OF CONCO");
 
     doTest("T6",
         "CAD:FIRE STANDBY;4240 SHILOH CHURCH RD;DAV;ODELL VFD STATION 2;MARIPOSA PL;ODELL SCHOOL RD;KPD",
         "CALL:FIRE STANDBY",
         "ADDR:4240 SHILOH CHURCH RD",
         "CITY:DAVIDSON",
         "PLACE:ODELL VFD STATION 2",
         "X:MARIPOSA PL & ODELL SCHOOL RD",
         "NAME:KPD");
 
     doTest("T7",
         "CAD:FALL WITH INJURY;1087 ODELL SCHOOL RD;CONCORD;UNTZ RD;ARBOR COMMONS LN",
         "CALL:FALL WITH INJURY",
         "ADDR:1087 ODELL SCHOOL RD",
         "CITY:CONCORD",
         "X:UNTZ RD & ARBOR COMMONS LN");
 
     doTest("T8",
         "CAD:ASSAULT W/ INJURY;5048 DAFFODIL LN;CON;BRAXTON DR;SABLE CT;BRITTANY STIREWALT",
         "CALL:ASSAULT W/ INJURY",
         "ADDR:5048 DAFFODIL LN",
         "CITY:CONCORD",
         "X:BRAXTON DR & SABLE CT",
         "NAME:BRITTANY STIREWALT");
   }
   
   // @Test
   public void fixLater() {
     doTest("XX",
         "CAD:DAVIDSON RD/MOORESVILLE RD;DAV;SUSPICIOUS VEHICLE",
         "ADDR:DAVIDSON RD & MOORESVILLE",
         "CITY:DAVIDSON",
         "CALL:SUSPICIOUS VEHICLE");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "2847:CAD:OPS2,A3,E21,E41;1;MVC INJURY- NO PIN IN/ENTRAP;2500 S MAIN ST;K401;HAMPTON LN;SPRINGWAY DR;JORDAN HOT SPOT;{FROM CSO: CALLER HUNG UP ON ME [11/27/11 14:30:54 PSBLACKWELL]} [11/27/11 14:28:05 TRANSFER] ** Transfer from CSO ** CSO received: 11/27/2011 14:30:17 Source: PHONE Nature: TRAFFIC ACCIDENT PI",
         "UNIT:A3,E21,E41",
         "CH:OPS2",
         "PRI:1",
         "CALL:MVC INJURY- NO PIN IN/ENTRAP",
         "ADDR:2500 S MAIN ST",
         "MAP:K401",
         "X:HAMPTON LN & SPRINGWAY DR",
         "INFO:JORDAN HOT SPOT / {FROM CSO: CALLER HUNG UP ON ME / } / ** Transfer from CSO ** CSO received: 11/27/2011 14:30:17 Source: PHONE Nature: TRAFFIC ACCIDENT PI");
 
     doTest("T2",
         "2845:CAD:A2,E11;1;RESPIRATORY DISTRESS;1307 POPLAR GLEN DR;K102;CANEVIEW CT;73 YO [11/27/11 12:25:16 HSTEPHENS]",
         "UNIT:A2,E11",
         "PRI:1",
         "CALL:RESPIRATORY DISTRESS",
         "ADDR:1307 POPLAR GLEN DR",
         "MAP:K102",
         "X:CANEVIEW CT",
         "INFO:73 YO");
 
     doTest("T3",
         "2846:CAD:A2,E31,E41,OPS1;1;MVC INJURY- NO PIN IN/ENTRAP;2400 WONDER DR NE/ROXIE ST;K303;** Transfer from CSO ** CSO received: 11/27/2011 14:04:46 Source: PHONE Nature: TRAFFIC ACCIDENT PI Business: MCDONALDS Phone:7047865822 Call #: 752 Event Id: 11094816 Console: CBA4 Call Take",
         "UNIT:A2,E31,E41",
         "CH:OPS1",
         "PRI:1",
         "CALL:MVC INJURY- NO PIN IN/ENTRAP",
         "ADDR:2400 WONDER DR NE & ROXIE ST",
         "MADDR:2400 WONDER DR NE",
         "MAP:K303",
         "X:** Transfer from CSO ** CSO received: 11/27/2011 14:04:46 Source: PHONE Nature: TRAFFIC ACCIDENT PI Business: MCDONALDS Phone:7047865822 Call #: 752 Event Id: 11094816 Console: CBA4 Call Take");
 
     doTest("T4",
         "2847:CAD:OPS2,A3,E21,E41;1;MVC INJURY- NO PIN IN/ENTRAP;2500 S MAIN ST;K401;HAMPTON LN;SPRINGWAY DR;JORDAN HOT SPOT;{FROM CSO: CALLER HUNG UP ON ME [11/27/11 14:30:54 PSBLACKWELL]} [11/27/11 14:28:05 TRANSFER] ** Transfer from CSO ** CSO received: 11/27/2011 14:30:17 Source: PHONE Nature: TRAFFIC ACCIDENT PI",
         "UNIT:A3,E21,E41",
         "CH:OPS2",
         "PRI:1",
         "CALL:MVC INJURY- NO PIN IN/ENTRAP",
         "ADDR:2500 S MAIN ST",
         "MAP:K401",
         "X:HAMPTON LN & SPRINGWAY DR",
         "INFO:JORDAN HOT SPOT / {FROM CSO: CALLER HUNG UP ON ME / } / ** Transfer from CSO ** CSO received: 11/27/2011 14:30:17 Source: PHONE Nature: TRAFFIC ACCIDENT PI");
 
     doTest("T5",
         "2848:CAD:A2,E21;1;RESPIRATORY DISTRESS;110 WESTOVER AV;K201;RENA ST;BEUNA ST;** Transfer from CSO ** CSO received: 11/27/2011 16:50:57 Source: E911 Nature: BREATHING PROBLEMS Call #: 792 Event Id: 11094854 Console: CBA7 Call Taker: SADEMPSEY Notes: [11/27/11 16:48:28",
         "UNIT:A2,E21",
         "PRI:1",
         "CALL:RESPIRATORY DISTRESS",
         "ADDR:110 WESTOVER AV",
         "MADDR:110 WESTOVER AVE",
         "MAP:K201",
         "X:RENA ST & BEUNA ST",
         "INFO:** Transfer from CSO ** CSO received: 11/27/2011 16:50:57 Source: E911 Nature: BREATHING PROBLEMS Call #: 792 Event Id: 11094854 Console: CBA7 Call Taker: SADEMPSEY Notes:");
 
     doTest("T6",
         "4311:CAD:B1,E31;1;SEIZURES;902 VIRGINIA ST;K303;PENNSYLVANIA AV;FLORIDA AV;TERRI CLARK;** Transfer from CSO ** CSO received: 11/25/2011 20:27:26: E911 Nature: CONVULSIONS \\ SEIZURES Call #: 357 Event Id: 11094423 Console: CBA2 Call Taker: SVWHITLEY Notes: [11/25/11 20:2",
         "UNIT:B1,E31",
         "PRI:1",
         "CALL:SEIZURES",
         "ADDR:902 VIRGINIA ST",
         "MAP:K303",
         "X:PENNSYLVANIA AV & FLORIDA AV",
         "INFO:TERRI CLARK / ** Transfer from CSO ** CSO received: 11/25/2011 20:27:26: E911 Nature: CONVULSIONS \\ SEIZURES Call #: 357 Event Id: 11094423 Console: CBA2 Call Taker: SVWHITLEY Notes:");
 
     doTest("T7",
         "2861:CAD:BC1,L14,A3,E21,OPS1,E12;1;FIRE ALARM- NO WATER FLOW;1445 OAKWOOD AV;K203;ROBINHOOD LANE EXT;WOODMOORE LN;KANNAPOLIS MIDDLE SCHOOL;pull station room 103 [11/30/11 09:05:34 HSTEPHENS]",
         "UNIT:BC1,L14,A3,E21,E12",
         "CH:OPS1",
         "PRI:1",
         "CALL:FIRE ALARM- NO WATER FLOW",
         "ADDR:1445 OAKWOOD AV",
         "MADDR:1445 OAKWOOD AVE",
         "MAP:K203",
         "X:ROBINHOOD LANE EXT & WOODMOORE LN",
         "INFO:KANNAPOLIS MIDDLE SCHOOL / pull station room 103");
 
   }
   
   public static void main(String[] args) {
     new NCCabarrusCountyAParserTest().generateTests("T1");
   }
 }

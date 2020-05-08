 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PABlairCountyParserTest extends BaseParserTest {
   
   public PABlairCountyParserTest() {
     setParser(new PABlairCountyParser(), "BLAIR COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T2",
         "Inc: Vehicle Accident w/injuries  Add: MCMAHON RD and 17TH ST  City: LOGAN TWP  Units: 1102 430 519 512 518 433 15A 16A  15A  Lat/Lon: @40.49756 @78.38746  9/7/2011 4:04:59 PM  DR #: 11-0150208",
         "CALL:Vehicle Accident w/injuries",
         "ADDR:MCMAHON RD and 17TH ST",
         "CITY:LOGAN TWP",
         "UNIT:1102 430 519 512 518 433 15A 16A  15A",
         "ID:11-0150208");
 
     doTest("T3",
         "Inc: Flooding  Add: 219 N 16TH AVE  City: LOGAN TWP  Units: 18A 1101 16A  16A  Lat/Lon: @40.53965 @78.39608  9/7/2011 8:20:01 AM  DR #: 11-0160247",
         "CALL:Flooding",
         "ADDR:219 N 16TH AVE",
         "CITY:LOGAN TWP",
         "UNIT:18A 1101 16A  16A",
         "ID:11-0160247");
 
     doTest("T4",
         "Inc: Inside/Outside Invest  Add: 3545 JUNIATA GAP RD  City: LOGAN TWP  Units: 18A  18A  Lat/Lon: @40.54878 @78.42869  9/7/2011 10:26:17 AM  DR #: 11-0180238",
         "CALL:Inside/Outside Invest",
         "ADDR:3545 JUNIATA GAP RD",
         "CITY:LOGAN TWP",
         "UNIT:18A  18A",
         "ID:11-0180238");
 
     doTest("T5",
         "Inc: WIRES DOWN/TREE DOWN  Add: 1100 OLD MILL RUN RD  City: LOGAN TWP  Units: 12A 14A  12A  Lat/Lon: @40.51172 @78.46085  9/4/2011 10:50:24 AM  DR #: 11-0120148",
         "CALL:WIRES DOWN/TREE DOWN",
         "ADDR:1100 OLD MILL RUN RD",
         "CITY:LOGAN TWP",
         "UNIT:12A 14A  12A",
         "ID:11-0120148");
 
     doTest("T6",
         "Inc: Structure Fire  Add: 1632 CAMPUS VIEW DR  City: LOGAN TWP  Units: 18A 1621 1632 512 1811 1851 1211 1221  12A  Lat/Lon: @40.54572 @78.41391  8/25/2011 6:20:44 PM  DR #: 11-0120145",
         "CALL:Structure Fire",
         "ADDR:1632 CAMPUS VIEW DR",
         "CITY:LOGAN TWP",
         "UNIT:18A 1621 1632 512 1811 1851 1211 1221  12A",
         "ID:11-0120145");
 
     doTest("T7",
         "Inc: FIRE ASSIST  Add: 531 E WOPSONONOCK AVE  City: ALTOONA  Units: 18A  18A  Lat/Lon: @40.53712 @78.40229  8/19/2011 3:03:56 PM  DR #: 11-0180225",
         "CALL:FIRE ASSIST",
         "ADDR:531 E WOPSONONOCK AVE",
         "CITY:ALTOONA",
         "UNIT:18A  18A",
         "ID:11-0180225");
 
     doTest("T8",
         "Inc: WIRES DOWN/TREE DOWN  Add: E PLEASANT VALLEY BLVD and BUCKNELL LN  City: LOGAN TWP  Units: 16A  16A  Lat/Lon: @40.53196 @78.36087  8/15/2011 1:49:26 AM  DR #: 11-0160229",
         "CALL:WIRES DOWN/TREE DOWN",
         "ADDR:E PLEASANT VALLEY BLVD and BUCKNELL LN",
         "CITY:LOGAN TWP",
         "UNIT:16A  16A",
         "ID:11-0160229");
 
     doTest("T9",
         "Inc: Vehicle Accident w/injuries  Add: 17TH ST and VALLEY VIEW BLVD  City: LOGAN TWP  Units: 15A 16A 1211  15A  Lat/Lon: @40.49939 @78.39013  8/22/2011 3:52:39 PM  DR #: 11-0150194",
         "CALL:Vehicle Accident w/injuries",
         "ADDR:17TH ST and VALLEY VIEW BLVD",
         "CITY:LOGAN TWP",
         "UNIT:15A 16A 1211  15A",
         "ID:11-0150194");
 
     doTest("T10",
         "Inc: Structure Fire  Add: 1105 3RD AVE  City: DUNCANSVI  Units: 10A 20A 420 626 421 2001 2021 604 5011 816 5001 2031 1011 5031 1551  15A  Lat/Lon: @40.42410 @78.43086  8/24/2011 6:56:20 PM  DR #: 11-0150196",
         "CALL:Structure Fire",
         "ADDR:1105 3RD AVE",
         "CITY:DUNCANSVILLE",
         "UNIT:10A 20A 420 626 421 2001 2021 604 5011 816 5001 2031 1011 5031 1551  15A",
         "ID:11-0150196");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(Inc: WIRES DOWN/TREE DOWN) Inc: WIRES DOWN/TREE DOWN \nAdd: 605 5TH ST \nCity: TYRONE BO \nUnits: 400 31A\n31A\nLat/Lon: @40.66879 @78.25321\n9/27/2011 11:05:05 P",
         "CALL:WIRES DOWN/TREE DOWN",
         "ADDR:605 5TH ST",
         "CITY:TYRONE",
         "UNIT:400 31A 31A");
 
     doTest("T2",
         "(Inc: Commercial Fire Alarm) Inc: Commercial Fire Alarm \nAdd: 505 3RD ST \nCity: TYRONE BO \nUnits: 400 31A\n31A\nLat/Lon: @40.66619 @78.25446\n10/1/2011 2:43:28",
         "CALL:Commercial Fire Alarm",
         "ADDR:505 3RD ST",
         "CITY:TYRONE",
         "UNIT:400 31A 31A");
 
     doTest("T3",
         "(Inc: FIRE ASSIST) Inc: FIRE ASSIST \nAdd: STATION 1 \nCity:  \nUnits: 9011 31A\n31A\nLat/Lon: @-1.00000 @-1.00000\n10/5/2011 2:27:41 AM \nDR #: 311100256",
         "CALL:FIRE ASSIST",
         "ADDR:STATION 1",
         "UNIT:9011 31A 31A",
         "ID:311100256");
 
   }
   
   public static void main(String[] args) {
     new PABlairCountyParserTest().generateTests("T2");
   }
 }

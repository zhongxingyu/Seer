 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NC.NCHaywoodCountyParser;
 
 import org.junit.Test;
 
 
 public class NCHaywoodCountyParserTest extends BaseParserTest {
   
   public NCHaywoodCountyParserTest() {
     setParser(new NCHaywoodCountyParser(), "HAYWOOD COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:GOODWILL 75 PLAZA LOOP CANTON NEW CLYDE HWY X MINGUS HILL RD MDL 21D03 2011040779 17:35:40 EMERGENCY HEMORRHAGE LACERATIO",
         "PLACE:GOODWILL",
         "ADDR:75 PLAZA LOOP",
         "CITY:CANTON",
         "X:NEW CLYDE HWY / MINGUS HILL RD",
         "CODE:21D03",
         "ID:2011040779",
         "TIME:17:35:40",
         "INFO:EMERGENCY HEMORRHAGE LACERATIO");
 
     doTest("T2",
         "CAD:41 HOLTZCLAW ST CANTON ORANGE ST X WINFIELD ST MDL 12D02-E 2011040926 12:48:52 EMERGENCY CONVULSIONS SEIZURE",
         "ADDR:41 HOLTZCLAW ST",
         "CITY:CANTON",
         "X:ORANGE ST / WINFIELD ST",
         "CODE:12D02-E",
         "ID:2011040926",
         "TIME:12:48:52",
         "INFO:EMERGENCY CONVULSIONS SEIZURE");
 
     doTest("T3",
         "CAD:44 HAYWOOD AV CANTON MINGUS ST X PUPPY DOG TR 2011040659 22:00:38 Fire Alarm - Smoke Detector SMOKE DETECTOR",
         "ADDR:44 HAYWOOD AV",
         "MADDR:44 HAYWOOD AVE",
         "CITY:CANTON",
         "X:MINGUS ST / PUPPY DOG TR",
         "ID:2011040659",
         "TIME:22:00:38",
         "INFO:Fire Alarm - Smoke Detector SMOKE DETECTOR");
 
     doTest("T4",
         "CAD:2219 RUSS AV 8 WAYNESVILLE CORTLAND CT X JULE NOLAND DR FDL 69D05 2011040700 08:15:46 RESIDENT STRUCT MULTIPLE",
         "ADDR:2219 RUSS AV 8",
         "MADDR:2219 RUSS AVE 8",
         "CITY:WAYNESVILLE",
         "X:CORTLAND CT / JULE NOLAND DR",
         "CODE:69D05",
         "ID:2011040700",
         "TIME:08:15:46",
         "INFO:RESIDENT STRUCT MULTIPLE");

    doTest("T1",
        "CAD:ARBY`S (CANTON) 701 CHAMPION DR CANTON DUSTY DR X CHAMPION - 1-40 EAST MDL 26A01 20:30:24 ROUTINE SICK PERSON",
        "PLACE:ARBY`S (CANTON)",
        "ADDR:701 CHAMPION DR",
        "CITY:CANTON",
        "X:DUSTY DR / CHAMPION - 1-40 EAST",
        "CODE:26A01",
        "TIME:20:30:24");
  }
   
   public static void main(String[] args) {
     new NCHaywoodCountyParserTest().generateTests("T1", "PLACE ADDR CITY X CODE ID TIME INFO");
   }
 }

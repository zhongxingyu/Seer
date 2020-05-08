 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCRowanCountyParserTest extends BaseParserTest {
   
   public NCRowanCountyParserTest() {
     setParser(new NCRowanCountyParser(), "ROWAN COUNTY", "NC");
   }
   
   @Test
  public void testBad() {
    doBadTest("CAD:Davidson County Rescue Squad is hold a barbecue chicken dinner at St6 until food is gone. Dinner includes chicken, beans, potato salad, roll , and dess");
  }
  
  @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:10C4 CHEST PAIN;201 N FRANKLIN ST;SWINK ST;PARK ST;CHGV;4301;RFG1",
         "CALL:10C4 CHEST PAIN",
         "ADDR:201 N FRANKLIN ST",
         "X:SWINK ST & PARK ST",
         "CITY:CHINA GROVE",
         "UNIT:4301",
         "CH:RFG1");
 
     doTest("T2",
         "CAD:FIRE DEPT SERVICE ASSIGNMENT;213 WASHINGTON ST;KLONDALE ST;CHGV;4301",
         "CALL:FIRE DEPT SERVICE ASSIGNMENT",
         "ADDR:213 WASHINGTON ST",
         "X:KLONDALE ST",
         "CITY:CHINA GROVE",
         "UNIT:4301");
 
     doTest("T3",
         "CAD:13D1 DIABETIC PROBLEM;1075 DEAL RD;KARRIKER FARMS RD;FIELD TRACE RD;MOOR;CARE INN RETIREMENT CENTER;4002;RFG4",
         "CALL:13D1 DIABETIC PROBLEM",
         "ADDR:1075 DEAL RD",
         "X:KARRIKER FARMS RD & FIELD TRACE RD",
         "CITY:MOORESVILLE",
         "PLACE:CARE INN RETIREMENT CENTER",
         "UNIT:4002",
         "CH:RFG4");
 
     doTest("T4",
         "CAD:RFD COMMAND ESTABLISHED;2470 E NC 152 HWY; CHGV",
         "CALL:RFD COMMAND ESTABLISHED",
         "ADDR:2470 E NC 152 HWY",
         "MADDR:2470 E NC 152",
         "CITY:CHINA GROVE");
 
     doTest("T5",
         "CAD:29B1 TRAFFIC ACCIDENT WITH INJ;E NC 152 HWY/S I 85;CHGV;4123;RFG2",
         "CALL:29B1 TRAFFIC ACCIDENT WITH INJ",
         "ADDR:E NC 152 HWY & S I 85",
         "MADDR:E NC 152 & S I 85",
         "CITY:CHINA GROVE",
         "UNIT:4123",
         "CH:RFG2");
 
     doTest("T6",
         "CAD:RFD COMMAND ESTABLISHED;E NC 152 HWY/S I 85; CHGV",
         "CALL:RFD COMMAND ESTABLISHED",
         "ADDR:E NC 152 HWY & S I 85",
         "MADDR:E NC 152 & S I 85",
         "CITY:CHINA GROVE");
 
     doTest("T7",
         "CAD:STRUCTURE FIRE;625 SIDES RD;GOLD KNOB RD;GOLD KNOB RD;SALS;7106;RFG1",
         "CALL:STRUCTURE FIRE",
         "ADDR:625 SIDES RD",
         "X:GOLD KNOB RD & GOLD KNOB RD",
         "CITY:SALISBURY",
         "UNIT:7106",
         "CH:RFG1");
 
     doTest("T8",
         "CAD:13D1 DIABETIC PROBLEM;3035 STOKES FERRY RD;EDZELL DR;EARNHARDT RD;SALS;7604;RFG4",
         "CALL:13D1 DIABETIC PROBLEM",
         "ADDR:3035 STOKES FERRY RD",
         "X:EDZELL DR & EARNHARDT RD",
         "CITY:SALISBURY",
         "UNIT:7604",
         "CH:RFG4");
 
     doTest("T9",
         "CAD:10D1 CHEST PAIN;6235 FISH POND RD;SWANNER PARK DR;ODDIE RD;SALS;7605;RFG1",
         "CALL:10D1 CHEST PAIN",
         "ADDR:6235 FISH POND RD",
         "X:SWANNER PARK DR & ODDIE RD",
         "CITY:SALISBURY",
         "UNIT:7605",
         "CH:RFG1");
 
     doTest("T10",
         "CAD:6C1 BREATHING PROBLEMS;140 PARTRIDGE TR;PALOMINO DR;DEER RD;SALS;7603;RFG1",
         "CALL:6C1 BREATHING PROBLEMS",
         "ADDR:140 PARTRIDGE TR",
         "X:PALOMINO DR & DEER RD",
         "CITY:SALISBURY",
         "UNIT:7603",
         "CH:RFG1");
 
     doTest("T11",
         "CAD:FIRE ALARM;1190 LONG FERRY RD;N LONG FERRY OFF;WILLOW CREEK DR;SALS;RUSHCO (LIBERTY) STATION #18;6401;RFG1",
         "CALL:FIRE ALARM",
         "ADDR:1190 LONG FERRY RD",
         "X:N LONG FERRY OFF & WILLOW CREEK DR",
         "CITY:SALISBURY",
         "PLACE:RUSHCO (LIBERTY) STATION #18",
         "UNIT:6401",
         "CH:RFG1");
 
     doTest("T12",
         "STRUCTURE FIRE;209 E 15TH ST;SALS;N LEE ST;FAMILY CRISIS CENTER;OPS02",
         "CALL:STRUCTURE FIRE",
         "ADDR:209 E 15TH ST",
         "CITY:SALISBURY",
         "X:N LEE ST",
         "PLACE:FAMILY CRISIS CENTER",
         "CH:OPS02");
 
     doTest("T13",
         "CAD:31D2 UNCONSCIOUS;6130 OLD US 70 HWY;CLEV;GLENN FAUST RD;CLOVER RIDGE CT;4501;OPS12",
         "CALL:31D2 UNCONSCIOUS",
         "ADDR:6130 OLD US 70 HWY",
         "MADDR:6130 OLD US 70",
         "CITY:CLEVELAND",
         "X:GLENN FAUST RD / CLOVER RIDGE CT",
         "UNIT:4501",
         "CH:OPS12");
 
     doTest("T14",
         "CAD:31D2 UNCONSCIOUS;3855 WOODLEAF BARBER RD;CLEV;THOMPSON RD;MT HALL RD;4504;OPS13",
         "CALL:31D2 UNCONSCIOUS",
         "ADDR:3855 WOODLEAF BARBER RD",
         "CITY:CLEVELAND",
         "X:THOMPSON RD / MT HALL RD",
         "UNIT:4504",
         "CH:OPS13");
 
     doTest("T15",
         "CAD:29B1 TRAFFIC ACCIDENT INJURY;2050 MOUNTAIN RD;CLEV;LONE MOUNTAIN TR;CARSON RD;4506;OPS14",
         "CALL:29B1 TRAFFIC ACCIDENT INJURY",
         "ADDR:2050 MOUNTAIN RD",
         "CITY:CLEVELAND",
         "X:LONE MOUNTAIN TR / CARSON RD",
         "UNIT:4506",
         "CH:OPS14");
 
     doTest("T16",
         "CAD:FIRE ALARM ACTIVATION;11550 STATESVILLE BLVD;CLEV;W MAIN ST;AMITY HILL RD;FREIGHTLINER TRUCK MFG;4503;OPS14",
         "CALL:FIRE ALARM ACTIVATION",
         "ADDR:11550 STATESVILLE BLVD",
         "CITY:CLEVELAND",
         "X:W MAIN ST / AMITY HILL RD",
         "PLACE:FREIGHTLINER TRUCK MFG",
         "UNIT:4503",
         "CH:OPS14");
 
     doTest("T17",
         "CAD:6C1 BREATHING PROBLEMS;303 N DEPOT ST;CLEV;W FOARD ST;4503;OPS14",
         "CALL:6C1 BREATHING PROBLEMS",
         "ADDR:303 N DEPOT ST",
         "CITY:CLEVELAND",
         "X:W FOARD ST",
         "UNIT:4503",
         "CH:OPS14");
 
     doTest("T18",
         "CAD:6D4 BREATHING PROBLEMS;11170 STATESVILLE BLVD;CLEV;MIMOSA ST;SCHOOL ST;CLEVELAND FIRE STN 45;4503;OPS13",
         "CALL:6D4 BREATHING PROBLEMS",
         "ADDR:11170 STATESVILLE BLVD",
         "CITY:CLEVELAND",
         "X:MIMOSA ST / SCHOOL ST",
         "PLACE:CLEVELAND FIRE STN 45",
         "UNIT:4503",
         "CH:OPS13");
 
     doTest("T19",
         "CAD:COUNTY FIRE MOVEUP;11170 STATESVILLE BLVD;CLEV;MIMOSA ST;SCHOOL ST;CLEVELAND FIRE STN 45;4503",
         "CALL:COUNTY FIRE MOVEUP",
         "ADDR:11170 STATESVILLE BLVD",
         "CITY:CLEVELAND",
         "X:MIMOSA ST / SCHOOL ST",
         "PLACE:CLEVELAND FIRE STN 45",
         "UNIT:4503");
 
     doTest("T20",
         "CAD:FIRE ALARM ACTIVATION;1020 EDMISTON RD;MTUL;CARSWELL RD;NC 801 HWY;6608;OPS12",
         "CALL:FIRE ALARM ACTIVATION",
         "ADDR:1020 EDMISTON RD",
         "X:CARSWELL RD",
         "CITY:MT ULLA",
         "PLACE:NC 801 HWY",
         "UNIT:6608",
         "CH:OPS12");
 
     doTest("T21",
         "BCAD:32D3 UNKNOWN MEDICAL;175 WHISPERING OAKS LN;MOCK;OAK MEADOW LN;RATLEDGE RD;7305;OPS",
         "CALL:BCAD:32D3 UNKNOWN MEDICAL",
         "ADDR:175 WHISPERING OAKS LN",
         "X:OAK MEADOW LN / RATLEDGE RD",
         "CITY:MOCKSVILLE",
         "UNIT:7305",
         "CH:OPS");
 
     doTest("T22",
         "CAD:FYI: ;HAZMAT LEVEL 1;1625 N JACKSON ST;SALS;W 15TH ST;S ROWAN AV;301",
         "CALL:HAZMAT LEVEL 1",
         "ADDR:1625 N JACKSON ST",
         "X:W 15TH ST / S ROWAN AV",
         "CITY:SALISBURY",
         "CH:301");
  }
   
   @Test
   public void testOutOfCounty() {
     
     setDefaults("", "NC");
 
     doTest("T1",
         "[] CAD:FIRE ALARM ACTIVATION;136 MARGINAL ST;COOLEEMEE ELEM SCHOOL\n",
         "CALL:FIRE ALARM ACTIVATION",
         "ADDR:136 MARGINAL ST",
         "PLACE:COOLEEMEE ELEM SCHOOL");
 
     doTest("T2",
         "[] CAD:FIRE ALARM ACTIVATION;136 MARGINAL ST;COOLEEMEE ELEMENTARY SCHOOL\n",
         "CALL:FIRE ALARM ACTIVATION",
         "ADDR:136 MARGINAL ST",
         "PLACE:COOLEEMEE ELEMENTARY SCHOOL");
     
 //    // Commented out because it fails in NCCombRowanDavidsonParserTest
 //    doTest("T3",
 //        "[] CAD:FIRE ALARM ACTIVATION;136 MARGIN ST\n",
 //        "CALL:FIRE ALARM ACTIVATION",
 //        "ADDR:136 MARGIN ST");
 //
 //    doTest("T4",
 //        "[] CAD:SMOKE VISABLE IN STRUCTURE;252 WATT ST\n",
 //        "CALL:SMOKE VISABLE IN STRUCTURE",
 //        "ADDR:252 WATT ST");
 //
 //    doTest("T5",
 //        "[] CAD:CANCEL;252 WATT ST\n",
 //        "CALL:CANCEL",
 //        "ADDR:252 WATT ST");
 
     doTest("T6",
         "[] CAD:FIRE ALARM ACTIVATION;264 NOLLIE DR;JOCKEY\n",
         "CALL:FIRE ALARM ACTIVATION",
         "ADDR:264 NOLLIE DR",
         "PLACE:JOCKEY");
       
   }
   
 
   public static void main(String[] args) {
     new NCRowanCountyParserTest().generateTests("T1");
   }
 }

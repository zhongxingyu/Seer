 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDFrederickCountyParser;
 
 import org.junit.Test;
 
 
 public class MDFrederickCountyParserTest extends BaseParserTest {
   
   public MDFrederickCountyParserTest() {
     setParser(new MDFrederickCountyParser(), "FREDERICK COUNTY", "MD");
   }
   
   @Test
   public void testBadMsg() {
     doBadTest("I will be there till about 11 so if u want come on in");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 6801 HARBAUGH RD CSAB:NEXT TO HER HOME ESZ: 626 MAP: 3996A6 Disp: [23]",
         "SRC:FredCo",
         "CALL:HOUSE/FIRE-VISIBLE",
         "ADDR:6801 HARBAUGH RD",
         "CITY:Sabillasville",
         "PLACE:NEXT TO HER HOME",
         "BOX:626",
         "MAP:3996A6");
 
     doTest("T2",
         "(CAD) [FredCo] CT: WOODS FIRE 7819-B FRIENDS CREEK RD CEMB ESZ: 627 MAP: 3996F6 Disp: E63,B66,DNR [36]",
         "SRC:FredCo",
         "CALL:WOODS FIRE",
         "ADDR:7819-B FRIENDS CREEK RD",
         "MADDR:7819 FRIENDS CREEK RD",
         "CITY:Emmitsburg",
         "BOX:627",
         "MAP:3996F6",
         "UNIT:E63,B66,DNR");
 
     doTest("T3",
         "(CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 4 PAWS PL THUR: @CATOCTIN VETERINARY CLINIC ESZ: 1001 MAP: 4108E9 Disp: E102,TWR6 [37]",
         "SRC:FredCo",
         "CALL:COMMERCIAL FIRE ALARM/AUTOMATIC",
         "ADDR:4 PAWS PL",
         "CITY:Thurmont",
         "PLACE:CATOCTIN VETERINARY CLINIC",
         "BOX:1001",
         "MAP:4108E9",
         "UNIT:E102,TWR6");
 
     doTest("T4",
         "(CAD) [FredCo] CT: OUTSIDE INVESTIGATION @MA ADAM CO: @RT15SB/BOYLE RD ESZ: -1 Disp: E63 [38]",
         "SRC:FredCo",
         "CALL:Mutual Aid: OUTSIDE INVESTIGATION",
         "ADDR:RT15SB & BOYLE RD",
         "MADDR:RT 15 & BOYLE RD",
         "CITY:Adams County",
         "ST:PA",
         "BOX:-1",
         "UNIT:E63");
 
     doTest("T5",
         "(CAD) [FredCo] CT: HOUSE / ODOR 9152 OLD KILN RD CEMB ESZ: 617 MAP: 4109E4 Disp: E63,E102,A268,TWR6,ET64 [39]",
         "SRC:FredCo",
         "CALL:HOUSE/ODOR",
         "ADDR:9152 OLD KILN RD",
         "CITY:Emmitsburg",
         "BOX:617",
         "MAP:4109E4",
         "UNIT:E63,E102,A268,TWR6,ET64");
 
     doTest("T6",
         "(CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 102 OLD OAK PL THUR ESZ: 1001 MAP: 4222D2 Disp: E102,TWR6 [41]",
         "SRC:FredCo",
         "CALL:COMMERCIAL FIRE ALARM/AUTOMATIC",
         "ADDR:102 OLD OAK PL",
         "CITY:Thurmont",
         "BOX:1001",
         "MAP:4222D2",
         "UNIT:E102,TWR6");
 
     doTest("T7",
         "(CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / WATERFLOW 100 CREAMERY CT: @EMMITSBURG GLASS CO ESZ: 620 MAP: 3997K9 Disp: E63,TWR6 [42]",
         "SRC:FredCo",
         "CALL:COMMERCIAL FIRE ALARM/WATERFLOW",
         "ADDR:100 CREAMERY CT",
         "PLACE:EMMITSBURG GLASS CO",
         "BOX:620",
         "MAP:3997K9",
         "UNIT:E63,TWR6");
 
     doTest("T8",
         "(CAD) [FredCo] CT: PERSON FIRE (INSIDE) 16825 S SETON AVE CEMB: @NATIONAL EMERGENCY TRAINING CENTER:SIMULATION LAB ESZ: 620 MAP: 3997H9 Disp: [34]                    Rmk:\"",
         "SRC:FredCo",
         "CALL:PERSON FIRE (INSIDE)",
         "ADDR:16825 S SETON AVE",
         "CITY:Emmitsburg",
         "PLACE:NATIONAL EMERGENCY TRAINING CENTER:SIMULATION LAB",
         "BOX:620",
         "MAP:3997H9");
 
     doTest("T9",
         "(FredCo) [CAD] CT: CHEST PAIN  1811 MONOCACY BLVD FRE1: @WAL MART: PHARMACY  ESZ: 215 MAP: 4448J9  Disp: A29,A247,M17",
         "SRC:FredCo",
         "CALL:CHEST PAIN",
         "ADDR:1811 MONOCACY BLVD",
         "CITY:Frederick City",
         "PLACE:WAL MART: PHARMACY",
         "BOX:215",
         "MAP:4448J9",
         "UNIT:A29,A247,M17");
 
     doTest("T10",
         "(CAD) [FredCo] CT: TROUBLE BREATHING 142 E SOUTH ST FRE1 ESZ: 303 MAP: 4566E5 Disp: A38,M31 [85]",
         "SRC:FredCo",
         "CALL:TROUBLE BREATHING",
         "ADDR:142 E SOUTH ST",
         "CITY:Frederick City",
         "BOX:303",
         "MAP:4566E5",
         "UNIT:A38,M31");
 
     doTest("T11",
         "(FredCo) [CAD] CT: HOUSE / FIRE-VISIBLE CACO: @MA CARRCO: 1594 BAUST CHURCH RD ESZ: 9902 Disp: ET94,",
         "SRC:FredCo",
         "CALL:Mutual Aid: HOUSE / FIRE-VISIBLE CACO:",
         "ADDR:1594 BAUST CHURCH RD",
         "CITY:Carroll County",
         "BOX:9902",
         "UNIT:ET94,");
 
     doTest("T12",
         "(FredCo) [CAD] CT: HEMORRHAGE CACO: @MA CARRCO: 915 FRANCIS SCOTT KEY HWY ESZ: 9902 Disp: SU9",
         "SRC:FredCo",
         "CALL:Mutual Aid: HEMORRHAGE CACO:",
         "ADDR:915 FRANCIS SCOTT KEY HWY",
         "CITY:Carroll County",
         "BOX:9902",
         "UNIT:SU9");
 
     doTest("T13",
         "[FredCo] CT: INJURY FROM VEHICLE ACCIDENT LL(-77:23:59.6013,39:21:53.1520): @I270NB / MONOCACY RIVER ESZ: 327002 MAP: 46",
         "SRC:FredCo",
         "CALL:INJURY FROM VEHICLE ACCIDENT",
         "ADDR:LL(-77:23:59.6013,39:21:53.1520)",
         "PLACE:I270NB / MONOCACY RIVER",
         "BOX:327002",
         "MAP:46");
 
     doTest("T14",
         "(CAD) [FredCo] CT: HOUSE / SMOKE 238 DEPAUL ST EMMB ESZ: 601 MAP: 3997J8 Disp: E63,ACE232,E102,CCE111,E132,A268,RS6,TWR6,CCLDR5,BAT900,SFT900",
         "SRC:FredCo",
         "CALL:HOUSE/SMOKE",
         "ADDR:238 DEPAUL ST",
         "PLACE:EMMB",
         "BOX:601",
         "MAP:3997J8",
         "UNIT:E63,ACE232,E102,CCE111,E132,A268,RS6,TWR6,CCLDR5,BAT900,SFT900");
 
     doTest("T15",
         "(CAD) [FredCo] CT: CARDIAC PATIENT 12 AFTON CT BRUN ESZ: 502 MAP: 4683C9 SUBDIV: GALYN MANOR Disp: A199,M20,A209,5FR [93]",
         "SRC:FredCo",
         "CALL:CARDIAC PATIENT",
         "ADDR:12 AFTON CT",
         "CITY:Brunswick",
         "BOX:502",
         "MAP:4683C9 SUBDIV: GALYN MANOR",
         "UNIT:A199,M20,A209,5FR");
 
     doTest("T16",
         "(CAD) [FredCo] CT: TROUBLE BREATHING / default 13862-A OLD NATIONAL PIKE CMTY TIME: 01:39:41 ESZ: 1521 MAP: 4690B2 Disp: CCM19,CCLS1,RE153",
         "SRC:FredCo",
         "CALL:TROUBLE BREATHING/default",
         "ADDR:13862-A OLD NATIONAL PIKE",
         "MADDR:13862 OLD NATIONAL PIKE",
         "CITY:Mt Airy",
         "TIME:01:39:41",
         "BOX:1521",
         "MAP:4690B2",
         "UNIT:CCM19,CCLS1,RE153");
 
     doTest("T17",
         "(CAD) [FredCo] CT: HOUSE / APPLIANCE FIRE (STRUCTURE PRE-ALERT) / default 6405 SPRING FOREST RD CFR1 TIME: 17:14:24 ESZ: 3302 MAP: 4567F5 SUBDIV: SPRING RIDGE Dis",
         "SRC:FredCo",
         "CALL:HOUSE/APPLIANCE FIRE (STRUCTURE PRE-ALERT)/default",
         "ADDR:6405 SPRING FOREST RD",
         "CITY:Frederick City",
         "TIME:17:14:24",
         "BOX:3302",
         "MAP:4567F5 SUBDIV: SPRING RIDGE");
 
     doTest("T18",
         "(CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 808 E SOUTH ST FRE1 TIME: 03:24:42 ESZ: 308 MAP: 4566G5 Disp: E31,E22,E331,E311,A38,RS3,TR42,TWR1,T1,ET233,ET154,BAT900,SF",
         "SRC:FredCo",
         "CALL:HOUSE/FIRE-VISIBLE",
         "ADDR:808 E SOUTH ST",
         "CITY:Frederick City",
         "TIME:03:24:42",
         "BOX:308",
         "MAP:4566G5",
         "UNIT:E31,E22,E331,E311,A38,RS3,TR42,TWR1,T1,ET233,ET154,BAT900,SF");
 
     doTest("T19",
         "(CAD) [FredCo] CT: TROUBLE BREATHING / default 7 S FEDERAL ST NEWM TIME: 12:02:44 ESZ: 1501 MAP: 4568H9 Disp: A159,M17",
         "SRC:FredCo",
         "CALL:TROUBLE BREATHING/default",
         "ADDR:7 S FEDERAL ST",
         "CITY:New Market",
         "TIME:12:02:44",
         "BOX:1501",
         "MAP:4568H9",
         "UNIT:A159,M17");
 
     doTest("T20",
         "(CAD) [FredCo] CT: OUTSIDE INVESTIGATION / default 5018 GREEN VALLEY RD CMON TIME: 21:24:52 ESZ: 1503 MAP: 4688G1 Disp: RE153",
         "SRC:FredCo",
         "CALL:OUTSIDE INVESTIGATION/default",
         "ADDR:5018 GREEN VALLEY RD",
         "CITY:Monrovia",
         "TIME:21:24:52",
         "BOX:1503",
         "MAP:4688G1",
         "UNIT:RE153");
 
     doTest("T21",
         "(CAD) [FredCo] CT: SICK PERSON / default 5850 EAGLEHEAD DR CIJM: @OAKDALE HIGH SCHOOL TIME: 08:52:07 ESZ: 1513 MAP: 4568A7 Disp: A159",
         "SRC:FredCo",
         "CALL:SICK PERSON/default",
         "ADDR:5850 EAGLEHEAD DR",
         "CITY:New Market",
         "PLACE:OAKDALE HIGH SCHOOL",
         "TIME:08:52:07",
         "BOX:1513",
         "MAP:4568A7",
         "UNIT:A159");
 
     doTest("T22",
         "(CAD) [FredCo] CT: VEHICLE ACCIDENT / default BELLS LN/BALTIMORE RD CFR4 TIME: 09:58:14 ESZ: 3306 MAP: 4567F7 Disp: A339,RE153,E331",
         "SRC:FredCo",
         "CALL:VEHICLE ACCIDENT/default",
         "ADDR:BELLS LN & BALTIMORE RD",
         "CITY:Frederick City",
         "TIME:09:58:14",
         "BOX:3306",
         "MAP:4567F7",
         "UNIT:A339,RE153,E331");
 
     doTest("T23",
         "(CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 1103 VILLAGE GATE DR MTAY: NEXT TOO TIME: 11:20:23 ESZ: 1527 MAP: 4570C10 Disp: RE153,E251,TWR23",
         "SRC:FredCo",
         "CALL:HOUSE/FIRE-VISIBLE",
         "ADDR:1103 VILLAGE GATE DR",
         "CITY:Mt Airy",
         "PLACE:NEXT TOO",
         "TIME:11:20:23",
         "BOX:1527",
         "MAP:4570C10",
         "UNIT:RE153,E251,TWR23");
 
     doTest("T24",
         "(CAD) [!] CT: ASSIST PATIENT - NON-EMERGENCY RESPONSE / default 222 BROADWAY ST FRE1: @WEINBERG HOUSE MAP: 4566D5 Disp: A38,E31",
         "SRC:!",
         "CALL:ASSIST PATIENT - NON-EMERGENCY RESPONSE/default",
         "ADDR:222 BROADWAY ST",
         "CITY:Frederick City",
         "PLACE:WEINBERG HOUSE",
         "MAP:4566D5",
         "UNIT:A38,E31");
 
     doTest("T25",
         "(CAD) [FredCo] CT: ENGINE TRANSFER / default 14026 GRACEHAM RD CTHU: @STATION 18 MAP: 4222J2 Disp: ET154",
         "SRC:FredCo",
         "CALL:ENGINE TRANSFER/default",
         "ADDR:14026 GRACEHAM RD",
         "CITY:Thurmont",
         "PLACE:STATION 18",
         "MAP:4222J2",
         "UNIT:ET154");
 
     doTest("T26",
         "(CAD) [FredCo] CT: VEHICLE ACCIDENT / default CMON: @RT80 / KEMPTOWN CHURCH RD MAP: 4689D7 Disp: A259,RE153,ET254",
         "SRC:FredCo",
         "CALL:VEHICLE ACCIDENT / default",
         "ADDR:RT80 & KEMPTOWN CHURCH RD",
         "MADDR:RT 80 & KEMPTOWN CHURCH RD",
         "CITY:Monrovia",
         "MAP:4689D7",
         "UNIT:A259,RE153,ET254");
 
     doTest("T27",
         "(JFC 2) CT: BACK PAIN /  801 TOLL HOUSE AVE FRE1: SUITE C-3 MAP: 4566D3  Disp: A29",
         "SRC:JFC 2",
         "CALL:BACK PAIN /",
         "ADDR:801 TOLL HOUSE AVE",
         "CITY:Frederick City",
         "PLACE:SUITE C-3",
         "MAP:4566D3",
         "UNIT:A29");
 
    doTest("T28",
        "CT: INJURED PERSON /  160 WILLOWDALE DR FRE2: @BROOKSIDE II APTS: APT M-403 MAP: 4565J3  Disp: A28",
        "CALL:INJURED PERSON /",
        "ADDR:160 WILLOWDALE DR",
        "CITY:Frederick City",
        "PLACE:BROOKSIDE II APTS: APT M-403",
        "MAP:4565J3",
        "UNIT:A28");

   }
   
   public static void main(String[] args) {
     new MDFrederickCountyParserTest().generateTests("T1");
   }
 }

 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYWestchesterCountyParserTest extends BaseParserTest {
   
   public NYWestchesterCountyParserTest() {
     setParser(new NYWestchesterCountyParser(), "WESTCHESTER COUNTY", "NY");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "Subject:IPage\n" +
         "50 ELKAN RD MAMARONECK_T: @ELKAN APARTMENTS, Cross: PALMER AVE, Type:ALS, , Time out: 12:12:51 Area: LARCH,Alarm lev: 0 ,Comments:   DIFF BREATHING",
 
         "ADDR:50 ELKAN RD",
         "CITY:LARCHMONT",
         "PLACE:ELKAN APARTMENTS",
         "X:PALMER AVE",
         "CALL:ALS",
         "TIME:12:12:51",
         "INFO:DIFF BREATHING");
 
     doTest("T2",
         "Subject:IPage\n" +
         "MAMARONECK_T: @I95 NB 07.1 EXIT 17 (CHATSWORTH AVE), Cross: , Type:MVA, INJURIES, Time out: 11:03:47 Area: LARCH,Alarm",
 
         "ADDR:I95 NB 07.1 EXIT 17 (CHATSWORTH AVE)",
         "MADDR:I 95 07.1 EXIT 17 (CHATSWORTH AVE)",
         "CITY:LARCHMONT",
         "CALL:MVA / INJURIES",
         "TIME:11:03:47");
 
     doTest("T3",
         "Subject:IPage\n30 HALSTEAD PL RYE, Cross: ORMOND PL, Type:STRU, , Time out: 21:00:33 Area: RYE,Alarm lev: 1 ,Comments:   WPH1",
         "ADDR:30 HALSTEAD PL",
         "CITY:RYE",
         "X:ORMOND PL",
         "CALL:STRU",
         "TIME:21:00:33",
         "INFO:WPH1");
 
     doTest("T4",
         "Subject:IPage\nMADISON AVE/JEFFERSON ST MAMARONECK_T, Cross: MADISON AVE, Type:HAZARD, FLUID, Time out: 14:30:33 Area: MAMTW,Alarm",
         "ADDR:MADISON AVE & JEFFERSON ST",
         "CITY:MAMARONECK",
         "X:MADISON AVE",
         "CALL:HAZARD / FLUID",
         "TIME:14:30:33");
 
     doTest("T5",
         "Subject:IPage\n" +
         "35 N CHATSWORTH AVE MAMARONECK_T: @CARLTON HOUSE APARTMENTS, Cross: JEFFERSON ST, Type:ALARM, COMM, Time out: 19:27:55",
 
         "ADDR:35 N CHATSWORTH AVE",
         "CITY:MAMARONECK",
         "PLACE:CARLTON HOUSE APARTMENTS",
         "X:JEFFERSON ST",
         "CALL:ALARM / COMM",
         "TIME:19:27:55");
 
     doTest("T6",
         "Subject:IPage\n" +
         "280 WEAVER ST MAMARONECK_T: @ST JOHN & PAUL SCHOOL, Cross: EDGEWOOD AVE, Type:ALARM, COMM, Time out: 23:48:57 Area: MAMTW,Alarm lev: 0 ,Comments:   GENERAL FIRE ALARM",
 
         "ADDR:280 WEAVER ST",
         "CITY:MAMARONECK",
         "PLACE:ST JOHN & PAUL SCHOOL",
         "X:EDGEWOOD AVE",
         "CALL:ALARM / COMM",
         "TIME:23:48:57",
         "INFO:GENERAL FIRE ALARM");
 
     doTest("T7",
         "Subject:IPage\n833 FENIMORE RD MAMARONECK_T, Cross: MOHEGAN RD, Type:ALARM, CO, Time out: 13:39:20 Area: MAMTW,Alarm lev: 0",
         "ADDR:833 FENIMORE RD",
         "CITY:MAMARONECK",
         "X:MOHEGAN RD",
         "CALL:ALARM / CO",
         "TIME:13:39:20");
 
     doTest("T8",
         "Subject:IPage\n35 N CHATSWORTH AVE MAMARONECK_T: @CARLTON HOUSE APARTMENTS:APT 3W, Cross: JEFFERSON ST, Type:ALS, , Time out:",
         "ADDR:35 N CHATSWORTH AVE",
         "CITY:MAMARONECK",
         "PLACE:CARLTON HOUSE APARTMENTS",
         "APT:3W",
         "X:JEFFERSON ST",
         "CALL:ALS");
 
     doTest("T9",
         "Subject:IPage\n" +
         "15 MADISON AVE MAMARONECK_T: @NEW YORK SPORTS CLUB, Cross: BYRON PL, Type:ALS, , Time out: 15:34:49 Area: LARCH,Alarm lev: 0 ,Comments:   KNOX BOX ON PREMISE   DISLOCATED SHOULDER",
 
         "ADDR:15 MADISON AVE",
         "CITY:LARCHMONT",
         "PLACE:NEW YORK SPORTS CLUB",
         "X:BYRON PL",
         "CALL:ALS",
         "TIME:15:34:49",
         "INFO:KNOX BOX ON PREMISE   DISLOCATED SHOULDER");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(IPage) 2 PRINCE WILLOW LN MAMARONECK_T, Cross: OLD WHITE PLAINS RD, Type:SERVICE, WATER, Time out: 17:22:17 Area: MAMTW,Alarm lev: 0 ,Comments:   WATER CONDITION",
         "ADDR:2 PRINCE WILLOW LN",
         "CITY:MAMARONECK",
         "X:OLD WHITE PLAINS RD",
         "CALL:SERVICE / WATER",
         "TIME:17:22:17",
         "INFO:WATER CONDITION");
 
     doTest("T2",
         "IPage / 4 DUDLEY LN MAMARONECK_T, Cross: FENIMORE RD, Type:ALARM, RESD, Time out: 09:42:46 Area: MAMTW,Alarm lev: 0 ,Comments:",
         "ADDR:4 DUDLEY LN",
         "CITY:MAMARONECK",
         "X:FENIMORE RD",
         "CALL:ALARM / RESD",
         "TIME:09:42:46");
 
     doTest("T3",
         "IPage / 50 ELKAN RD MAMARONECK_T: @ELKAN APARTMENTS, Cross: PALMER AVE, Type:ALS, , Time out: 12:12:51 Area: LARCH,Alarm lev:",
         "ADDR:50 ELKAN RD",
         "CITY:LARCHMONT",
         "PLACE:ELKAN APARTMENTS",
         "X:PALMER AVE",
         "CALL:ALS",
         "TIME:12:12:51");
 
     doTest("T4",
         "IPage / MAMARONECK_T: @I95 NB 07.3, Cross: , Type:MVF, CAR, Time out: 22:12:12 Area: MAMTW,Alarm lev: 0 ,Comments:",
         "ADDR:I95 NB 07.3",
         "MADDR:I 95 07.3",
         "CITY:MAMARONECK",
         "CALL:MVF / CAR",
         "TIME:22:12:12");
 
     doTest("T5",
         "IPage / MAMARONECK_T: @I95 NB 07.1 EXIT 17 (CHATSWORTH AVE), Cross: , Type:MVA, INJURIES, Time out: 11:03:47 Area: LARCH,Alarm",
         "ADDR:I95 NB 07.1 EXIT 17 (CHATSWORTH AVE)",
         "MADDR:I 95 07.1 EXIT 17 (CHATSWORTH AVE)",
         "CITY:LARCHMONT",
         "CALL:MVA / INJURIES",
         "TIME:11:03:47");
 
     doTest("T6",
         "IPage / 42 MAPLEWOOD ST MAMARONECK_T, Cross: MURRAY AVE, Type:SERVICE, LOCKOUT, Time out: 17:27:08 Area: MAMTW,Alarm lev: 0",
         "ADDR:42 MAPLEWOOD ST",
         "CITY:MAMARONECK",
         "X:MURRAY AVE",
         "CALL:SERVICE / LOCKOUT",
         "TIME:17:27:08");
 
     doTest("T7",
         "IPage / 30 HALSTEAD PL RYE, Cross: ORMOND PL, Type:STRU, , Time out: 21:00:33 Area: RYE,Alarm lev: 1 ,Comments:   WPH1",
         "ADDR:30 HALSTEAD PL",
         "CITY:RYE",
         "X:ORMOND PL",
         "CALL:STRU",
         "TIME:21:00:33",
         "INFO:WPH1");
 
     doTest("T8",
         "(IPage) 21 HEMLOCK HILLS NEW_CASTLE, Cross: HILLTOP DR, Type:STRU, , Time out: 17:39:49 Area: MILLW,Alarm lev: 0 ,Comments:",
         "ADDR:21 HEMLOCK HILLS",
         "CITY:MILLWOOD",
         "X:HILLTOP DR",
         "CALL:STRU",
         "TIME:17:39:49");
 
     doTest("T9",
         "(IPage) 2365 BOSTON POST RD LARCHMONT, Cross: DEANE PL, Type:MVA, INJURIES, Time out: 19:23:17 Area: LARCH,Alarm lev: 0",
         "ADDR:2365 BOSTON POST RD",
         "CITY:LARCHMONT",
         "X:DEANE PL",
         "CALL:MVA / INJURIES",
         "TIME:19:23:17");
 
     doTest("T10",
         "(IPage) 14 COWDRAY PARK DR NORTH_CASTLE:MAIN HOUSE, Cross: HURLINGHAM DR, Type:ALARM, CO, Time out: 12:51:45 Area: BANKS,Alarm",
         "ADDR:14 COWDRAY PARK DR",
        "CITY:BEDFORD",
         "ST:CT",
         "PLACE:MAIN HOUSE - BANKSVILLE",
         "X:HURLINGHAM DR",
         "CALL:ALARM / CO",
         "TIME:12:51:45");
 
     doTest("T11",
         "(IPage) 16 ST MARYS CHURCH RD NORTH_CASTLE, Cross: E MIDDLE PATENT RD, Type:ALARM, RESD, Time out: 18:54:25 Area: BANKS,Alarm",
         "ADDR:16 ST MARYS CHURCH RD",
        "CITY:BEDFORD",
         "ST:CT",
         "PLACE:BANKSVILLE",
         "X:E MIDDLE PATENT RD",
         "CALL:ALARM / RESD",
         "TIME:18:54:25");
 
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "(Email Copy Message From Hiplink) 647 FOREST AVE MAMARONECK_T, Cross: ROCKINGSTONE AVE, Type:INVEST, CO-NO, Time out: 17:53:26 Area: MAMTW,Alarm lev: 0 ,Comments:",
         "ADDR:647 FOREST AVE",
         "CITY:MAMARONECK",
         "X:ROCKINGSTONE AVE",
         "CALL:INVEST / CO-NO",
         "TIME:17:53:26");
 
     doTest("T2",
         "(Email Copy Message From Hiplink) 20 AVON RD MAMARONECK_T, Cross: LANCIA LN, Type:ALS, , Time out: 16:27:11 Area: LARCH,Alarm lev: 0 ,Comments:   CHILD SWALLOWED SHAMPOO",
         "ADDR:20 AVON RD",
         "CITY:LARCHMONT",
         "X:LANCIA LN",
         "CALL:ALS",
         "TIME:16:27:11",
         "INFO:CHILD SWALLOWED SHAMPOO");
 
     doTest("T3",
         "(Email Copy Message From Hiplink) 1165 OLD WHITE PLAINS RD MAMARONECK_T, Cross: GATE HOUSE LN, Type:INVEST, OUTSIDE, Time out: 17:19:51 Area: MAMTW,Alarm lev: 0 ,Comments:   ODOR OF GAS IN THE AREA",
         "ADDR:1165 OLD WHITE PLAINS RD",
         "CITY:MAMARONECK",
         "X:GATE HOUSE LN",
         "CALL:INVEST / OUTSIDE",
         "TIME:17:19:51",
         "INFO:ODOR OF GAS IN THE AREA");
     
   }
   
   public static void main(String[] args) {
     new NYWestchesterCountyParserTest().generateTests("T1");
   }
 }

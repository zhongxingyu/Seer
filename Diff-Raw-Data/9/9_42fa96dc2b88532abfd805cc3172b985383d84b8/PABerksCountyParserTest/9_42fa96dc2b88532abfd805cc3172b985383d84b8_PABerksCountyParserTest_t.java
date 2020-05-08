 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PABerksCountyParser;
 
 import org.junit.Test;
 
 
 public class PABerksCountyParserTest extends BaseParserTest {
   
   public PABerksCountyParserTest() {
     setParser(new PABerksCountyParser(), "BERKS COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Unit:M590A Status:Dispatched ALS MEDICAL ; 215 TOWER RD ; ; S PARK AVE / CLAY RD ; Muni: LONGSWAMP ; 64 YOM / IN ACTIVE SIEZURE  ; 07/10/12 12:57",
         "UNIT:M590A",
         "CALL:ALS MEDICAL",
         "ADDR:215 TOWER RD",
         "X:S PARK AVE / CLAY RD",
         "CITY:LONGSWAMP",
         "INFO:64 YOM / IN ACTIVE SIEZURE",
         "DATE:07/10/12",
         "TIME:12:57");
 
     doTest("T2",
         "Unit:M590B Status:Dispatched MVAUNK ; W MAIN ST NORMAL AVE ; ; ; Muni: KUTZTOWN ; ; 07/10/12 14:06",
         "UNIT:M590B",
         "CALL:MVAUNK",
         "ADDR:W MAIN ST & NORMAL AVE", // Streets don't intersect??
         "CITY:KUTZTOWN",
         "DATE:07/10/12",
         "TIME:14:06");
 
     doTest("T3",
         "Unit:M535A Status:Dispatched ALS MEDICAL ; 281 MOSELEM CHURCH RD ; ; SCHWOYER RD / MISTY LN ; Muni: RICHMOND ; 79 YOF/VOMITING/COUGHING/PAIN IN SIDE ; 07/11/12 10:04 Sent by Berks County RSAN to Fleetwood EMS All CALL -- You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org",
         "UNIT:M535A",
         "CALL:ALS MEDICAL",
         "ADDR:281 MOSELEM CHURCH RD",
         "X:SCHWOYER RD / MISTY LN",
         "CITY:RICHMOND",
         "INFO:79 YOF/VOMITING/COUGHING/PAIN IN SIDE",
         "DATE:07/11/12",
         "TIME:10:04");
 
     doTest("T4",
         "Unit:M535A Status:Dispatched ALS MEDICAL ; 224 FAITH DR ; ; GENESIS DR, HOPE DR / HOPE DR ; Muni: MAIDENCREEK ; 39YOM/NOT MAKING SENSE/LAYING ON THE FL ; 07/11/12 21:59 Sent by Berks County RSAN to Fleetwood EMS All CALL -- You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org",
         "UNIT:M535A",
         "CALL:ALS MEDICAL",
         "ADDR:224 FAITH DR",
         "X:GENESIS DR, HOPE DR / HOPE DR",
         "CITY:MAIDENCREEK",
         "INFO:39YOM/NOT MAKING SENSE/LAYING ON THE FL",
         "DATE:07/11/12",
         "TIME:21:59");
 
     doTest("T5",
         "Unit:585A Status:Dispatched MVAWITH ; 2330 INTERSTATE 78 E ; I78 MP 23.3 EB; UNKNOWN / RAMP B7 ; Muni: UPPER BERN ; 2 vehs/ ILVER SEDAN / UNK INJS / TX TO PSP  ; 07/11/12 16:33 On Jul 11, 2012 5:55 PM, \"Ken Corbin\" <ken@cadpage.org> wrote:",
         "UNIT:585A",
         "CALL:MVAWITH",
         "ADDR:2330 INTERSTATE 78 E",
         "PLACE:I78 MP 23.3 EB",
         "X:UNKNOWN / RAMP B7",
         "CITY:UPPER BERN",
         "INFO:2 vehs/ ILVER SEDAN / UNK INJS / TX TO PSP");
 
     doTest("T6",
         "Unit:M590A Status:Dispatched MVAWITH ; S BALDY ST TREXLER AVE ; ; ; Muni: KUTZTOWN ; ; 07/11/12 07:10",
         "UNIT:M590A",
         "CALL:MVAWITH",
         "ADDR:S BALDY ST & TREXLER AVE",
         "CITY:KUTZTOWN",
         "DATE:07/11/12",
         "TIME:07:10");
 
     doTest("T7",
         "Unit:M590A Status:Dispatched BLS MEDICAL ; 625 SPRING ST ; PENNSYLVANIA DIALYSIS; CROSSING DR / N 6 ST ; Muni: WYOMISSING ; ; 07/10/12 19:58",
         "UNIT:M590A",
         "CALL:BLS MEDICAL",
         "ADDR:625 SPRING ST",
         "PLACE:PENNSYLVANIA DIALYSIS",
         "X:CROSSING DR / N 6 ST",
         "CITY:WYOMISSING",
         "DATE:07/10/12",
         "TIME:19:58");
 
     doTest("T8",
         "Unit:M590B Status:Dispatched MVAUNK ; W MAIN ST NORMAL AVE ; ; ; Muni: KUTZTOWN ; ; 07/10/12 14:06",
         "UNIT:M590B",
         "CALL:MVAUNK",
         "ADDR:W MAIN ST & NORMAL AVE", // Ditto
         "CITY:KUTZTOWN",
         "DATE:07/10/12",
         "TIME:14:06");
 
     doTest("T9",
         "Unit:R27 Status:Dispatched MVAUNK ; ROUTE 183 MILL RD ; ; ; Muni: UPPER TULPEHOCKEN ; ; 07/12/12 04:48",
         "UNIT:R27",
         "CALL:MVAUNK",
         "ADDR:ROUTE 183 & MILL RD",
         "CITY:UPPER TULPEHOCKEN",
         "DATE:07/12/12",
         "TIME:04:48");
 
     doTest("T10",
         "Unit:535A Status:Dispatched BLS TRAUMA ; 2 W PINE ST ; ; S FRANKLIN ST, E PINE ST /NEW ALY ; Muni: FLEETWOOD ; 79 YOM FELL IN THE DRIVEWAY ; 07/11/12 14:13",
         "UNIT:535A",
         "CALL:BLS TRAUMA",
         "ADDR:2 W PINE ST",
         "X:S FRANKLIN ST, E PINE ST /NEW ALY",
         "CITY:FLEETWOOD",
         "INFO:79 YOM FELL IN THE DRIVEWAY",
         "DATE:07/11/12",
         "TIME:14:13");
 
     doTest("T11",
         "Unit:M590A Status:Dispatched ALS MEDICAL ; 7723 HAMILTON BLVD ; ; ; Muni: ; UPPERMACUNGIE TWP- LEHIGH CO/ ; 07/11/12 21:51",
         "UNIT:M590A",
         "CALL:ALS MEDICAL",
         "ADDR:7723 HAMILTON BLVD",
         "CITY:UPPERMACUNGIE TWP,LEHIGH CO",
         "DATE:07/11/12",
         "TIME:21:51");
 
     doTest("T12",
         "Unit:M590A Status:Dispatched ALS MEDICAL ; 1107 APEX AVE ; ; UNKNOWN / LIFESTYLE LN ; Muni: MAXATAWNY ; 20ish yof layin on ground /is cons ; 07/11/12 20:41",
         "UNIT:M590A",
         "CALL:ALS MEDICAL",
         "ADDR:1107 APEX AVE",  // Not mapping
         "X:UNKNOWN / LIFESTYLE LN",
         "CITY:MAXATAWNY",
         "INFO:20ish yof layin on ground /is cons",
         "DATE:07/11/12",
         "TIME:20:41");
 
     doTest("T13",
         "Unit:M590A Status:Dispatched ALS TRAUMA ; 15620 KUTZTOWN RD ; ; UNKNOWN / CHURCH RD ; Muni: MAXATAWNY ; 10 YOF FELL FROM A WINDOW  UNCONS ; 07/12/12 12:38",
         "UNIT:M590A",
         "CALL:ALS TRAUMA",
         "ADDR:15620 KUTZTOWN RD",
         "X:UNKNOWN / CHURCH RD",
         "CITY:MAXATAWNY",
         "INFO:10 YOF FELL FROM A WINDOW  UNCONS",
         "DATE:07/12/12",
         "TIME:12:38");
 
     doTest("T14",
         "Unit:M590A Status:Dispatched BLS MEDICAL ; 247 RHOADES RD ; ; NUNEMAKER RD / ROTH RD ; Muni: GREENWICH ; RESPONDING FOR BLSMED ; 07/12/12 15:14",
         "UNIT:M590A",
         "CALL:BLS MEDICAL",
         "ADDR:247 RHOADES RD",
         "X:NUNEMAKER RD / ROTH RD",
         "CITY:GREENWICH",
         "INFO:RESPONDING FOR BLSMED",
         "DATE:07/12/12",
         "TIME:15:14");
 
     doTest("T15",
         "Unit:M590A Status:Dispatched LIFT ASSIST ; 35 S WHITEOAK ST SHADE RES ; ; SANDER ALY, N WHITEOAK ST / NORMAL AVE ; Muni: KUTZTOWN ; 74YOM FELL NOT INJ JUST NEEDS HELP GETTING UP ; 07/12/12 17:38",
         "UNIT:M590A",
         "CALL:LIFT ASSIST",
         "ADDR:35 S WHITEOAK ST",
         "NAME:SHADE", 
         "X:SANDER ALY, N WHITEOAK ST / NORMAL AVE",
         "CITY:KUTZTOWN",
         "INFO:74YOM FELL NOT INJ JUST NEEDS HELP GETTING UP",
         "DATE:07/12/12",
         "TIME:17:38");
 
     doTest("T16",
         "Unit:M590A Status:Dispatched BLS MEDICAL ; 247 RHOADES RD ; ; NUNEMAKER RD / ROTH RD ; Muni: GREENWICH ; RESPONDING FOR BLSMED ; 07/12/12 15:14",
         "UNIT:M590A",
         "CALL:BLS MEDICAL",
         "ADDR:247 RHOADES RD",
         "X:NUNEMAKER RD / ROTH RD",
         "CITY:GREENWICH",
         "INFO:RESPONDING FOR BLSMED",
         "DATE:07/12/12",
         "TIME:15:14");
 
     doTest("T17",
         "Unit:M590A Status:Dispatched ALS TRAUMA ; 15620 KUTZTOWN RD ; ; UNKNOWN / CHURCH RD ; Muni: MAXATAWNY ; 10 YOF FELL FROM A WINDOW  UNCONS ; 07/12/12 12:38",
         "UNIT:M590A",
         "CALL:ALS TRAUMA",
         "ADDR:15620 KUTZTOWN RD",
         "X:UNKNOWN / CHURCH RD",
         "CITY:MAXATAWNY",
         "INFO:10 YOF FELL FROM A WINDOW  UNCONS",
         "DATE:07/12/12",
         "TIME:12:38");
 
     doTest("T18",
         "Unit:CO61 Status:Dispatched CMA ; 303 WYNDCLIFFE AVE ; ; HILLTOP CT / CHESTNUT ST, WINTERSON DR ; Muni: HAMBURG ;",
         "UNIT:CO61",
         "CALL:CMA",
         "ADDR:303 WYNDCLIFFE AVE",
         "X:HILLTOP CT / CHESTNUT ST, WINTERSON DR",
         "CITY:HAMBURG");
 
     doTest("T19",
         "Unit:CO61 Status:Dispatched AFA ; 218 PINE ST ; ; S PEACH ALY / S 2 ST ; Muni: HAMBURG ; ; 07/12/12 06:37",
         "UNIT:CO61",
         "CALL:AFA",
         "ADDR:218 PINE ST",
         "X:S PEACH ALY / S 2 ST",
         "CITY:HAMBURG",
         "DATE:07/12/12",
         "TIME:06:37");
 
     doTest("T20",
         "Unit:CO85 Status:Dispatched MVAWITH ; 1780 ACORN DR ; ; TIMBER LN / GRAVEL HILL DR ; Muni: SPRING ; FM SCREAMING SHE IS BLEEDING/  ; 07/12/12 02:03\n\n" +
         "Sent by Berks County RSAN to CO85 All Call",
 
         "UNIT:CO85",
         "CALL:MVAWITH",
         "ADDR:1780 ACORN DR",
         "X:TIMBER LN / GRAVEL HILL DR",
         "CITY:SPRING",
         "INFO:FM SCREAMING SHE IS BLEEDING/",
         "DATE:07/12/12",
         "TIME:02:03");
 
     doTest("T21",
         "Unit:E85-2 Status:Dispatched HAZMAT ; 2227 MCKINLEY AVE ; ; UNKNOWN / WEST WYOMISSING BLVD ; Muni: SPRING ; CHF85 AND E85-2 EN FOR ODOR OF GASOLINE IN RESIDENCE ; 07/11/12 10:07\n\n" +
         "Sent by Berks County RSAN to CO85 All Call",
 
         "UNIT:E85-2",
         "CALL:HAZMAT",
         "ADDR:2227 MCKINLEY AVE",
         "X:UNKNOWN / WEST WYOMISSING BLVD",
         "CITY:SPRING",
         "INFO:CHF85 AND E85-2 EN FOR ODOR OF GASOLINE IN RESIDENCE",
         "DATE:07/11/12",
         "TIME:10:07");
 
     doTest("T22",
         "Unit:REN85 Status:Dispatched SF ; 44 WATER ST ; ; POTTSVILLE PKE / N 5 STREET HWY ; Muni: MUHLENBERG ; elderly fm / not answering questions  ; 07/12/12 00:54\n\n" +
         "Sent by Berks County RSAN to CO85 All Call",
 
         "UNIT:REN85",
         "CALL:SF",
         "ADDR:44 WATER ST",
         "X:POTTSVILLE PKE / N 5 STREET HWY",
         "CITY:MUHLENBERG",
         "INFO:elderly fm / not answering questions",
         "DATE:07/12/12",
         "TIME:00:54");
 
     doTest("T23",
         "Unit:R1 Status:Dispatched MVAUNK ; OAK LN / RICHMOND ST ; ; ; Muni: LOWER ALSACE ; mc into ditch / on oak ln / ; 07/12/12 12",
         "UNIT:R1",
         "CALL:MVAUNK",
         "ADDR:OAK LN & RICHMOND ST",
         "CITY:LOWER ALSACE",
         "INFO:mc into ditch / on oak ln /");
   
   }
   
   @Test
   public void testActive911A() {
 
     doTest("T1",
         "(berks.co85@rsix.roamsecure.net) Unit:CO85 Status:Dispatched MF ; 828 BLUE GATE LN ; ; PERSIMMON DR / HEARTHSTONE LN ; Muni: SPRING ; ; 07/13/12 14:09 Sent by Berks County RSAN to CO85 All Call -- You received this message because you registered on Alert Berks. ",
         "UNIT:CO85",
         "CALL:MF",
         "ADDR:828 BLUE GATE LN",
         "X:PERSIMMON DR / HEARTHSTONE LN",
         "CITY:SPRING",
         "DATE:07/13/12",
         "TIME:14:09");
 
     doTest("T2",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched BLS MEDICAL ; 1320 BROADCASTING RD ; ; MERIDIAN BLVD / CONNEL DR ; Muni: SPRING ; RESPONDING FOR BLS MED / DIRECT CALL TO STATION / NFI ; 07/12/12 15:18\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:BLS MEDICAL",
         "ADDR:1320 BROADCASTING RD",
         "X:MERIDIAN BLVD / CONNEL DR",
         "CITY:SPRING",
         "INFO:RESPONDING FOR BLS MED / DIRECT CALL TO STATION / NFI",
         "DATE:07/12/12",
         "TIME:15:18");
 
     doTest("T3",
         "[berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched ALS MEDICAL ; 3121 STATE HILL RD RM17 ; COLUMBIA COTTAGE; YERGER BLVD, WESTBURY DR / LELAND WAY ; Muni: SPRING ; FEMALE CHEST PAIN  ; 07/12/12 15:19\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650B",
         "CALL:ALS MEDICAL",
         "ADDR:3121 STATE HILL RD",
         "APT:17",
         "PLACE:COLUMBIA COTTAGE",
         "X:YERGER BLVD, WESTBURY DR / LELAND WAY",
         "CITY:SPRING",
         "INFO:FEMALE CHEST PAIN",
         "DATE:07/12/12",
         "TIME:15:19");
 
     doTest("T4",
         "[berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched CLASS 4 MENTAL ; 107 S 5 ST ROOM 5 ; ; FRANKLIN ST / CHESTNUT ST ; Muni: READING ; FEELING SUICIDAL ; 07/12/12 19:20\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650E",
         "CALL:CLASS 4 MENTAL",
         "ADDR:107 S 5 ST",
         "APT:5",
         "X:FRANKLIN ST / CHESTNUT ST",
         "CITY:READING",
         "INFO:FEELING SUICIDAL",
         "DATE:07/12/12",
         "TIME:19:20");
 
     doTest("T5",
         "[berks.650@rsix.roamsecure.net] Unit:M650I Status:Dispatched ALS MEDICAL ; 404 S CHURCH ST ROOM 1 ; GOLDEN RIDGE; PATTERSON DR / PATTERSON DR ; Muni: ROBESONIA ; 88 YOF/ POS TIA ; 07/12/12 19:59\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650I",
         "CALL:ALS MEDICAL",
         "ADDR:404 S CHURCH ST",
         "APT:1",
         "PLACE:GOLDEN RIDGE",
         "X:PATTERSON DR / PATTERSON DR",
         "CITY:ROBESONIA",
         "INFO:88 YOF/ POS TIA",
         "DATE:07/12/12",
         "TIME:19:59");
 
     doTest("T6",
         "[berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched ALS MEDICAL ; 1802 TULPEHOCKEN RD RM 277 ; ; UNKNOWN / KISSINGER LN ; Muni: WYOMISSING ; 88 YOF/  ; 07/12/12 21:28\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650E",
         "CALL:ALS MEDICAL",
         "ADDR:1802 TULPEHOCKEN RD",
         "APT:277",
         "X:UNKNOWN / KISSINGER LN",
         "CITY:WYOMISSING",
         "INFO:88 YOF/",
         "DATE:07/12/12",
         "TIME:21:28");
 
     doTest("T7",
         "[berks.650@rsix.roamsecure.net] Unit:M650I Status:Dispatched ALS MEDICAL ; 101 E FRONT ST ; ; S MAIN ST, N MAIN ST / N WOLF ST ; Muni: BERNVILLE ; 63YOM LABORED BREATHING ; 07/12/12 22:17\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650I",
         "CALL:ALS MEDICAL",
         "ADDR:101 E FRONT ST",
         "X:S MAIN ST, N MAIN ST / N WOLF ST",
         "CITY:BERNVILLE",
         "INFO:63YOM LABORED BREATHING",
         "DATE:07/12/12",
         "TIME:22:17");
 
     doTest("T8",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 3012 SUSANNA DR ; ; DANIEL DR / KATHARINE DR ; Muni: SPRING ; ALS ; 07/12/12 22:34\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:ALS MEDICAL",
         "ADDR:3012 SUSANNA DR",
         "X:DANIEL DR / KATHARINE DR",
         "CITY:SPRING",
         "INFO:ALS",
         "DATE:07/12/12",
         "TIME:22:34");
 
     doTest("T9",
         "[berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched DOMESTIC INJURY ; 654 ANNE DR ; ; UNKNOWN / CHRISTOPHER DR ; Muni: WERNERSVILLE ; COMP VS EX HUSB OF GIRLFRIEND/  ; 07/12/12 22:45\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650B",
         "CALL:DOMESTIC INJURY",
         "ADDR:654 ANNE DR",
         "X:UNKNOWN / CHRISTOPHER DR",
         "CITY:WERNERSVILLE",
         "INFO:COMP VS EX HUSB OF GIRLFRIEND/",
         "DATE:07/12/12",
         "TIME:22:45");
 
     doTest("T10",
         "[berks.650@rsix.roamsecure.net] Unit:650T Status:Dispatched CLASS 4 MENTAL ; 4050 CONRAD WEISER PRKWY 4 ; ; MARION DR / SHERIDAN RD ; Muni: MARION ; 21YOF/NEEDS TO BE 302 ; 07/12/12 23:37\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:650T",
         "CALL:CLASS 4 MENTAL",
         "ADDR:4050 CONRAD WEISER PRKWY 4",
         "X:MARION DR / SHERIDAN RD",
         "CITY:MARION",
         "INFO:21YOF/NEEDS TO BE 302",
         "DATE:07/12/12",
         "TIME:23:37");
 
     doTest("T11",
         "[berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched ALS MEDICAL ; 511 HUNTERS RD ; ; UNKNOWN / UNKNOWN ; Muni: CUMRU ; 76YOM/ BLOOD SUGAR 35/  ; 07/12/12 23:52\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650E",
         "CALL:ALS MEDICAL",
         "ADDR:511 HUNTERS RD",
         "X:UNKNOWN / UNKNOWN",
         "CITY:CUMRU",
         "INFO:76YOM/ BLOOD SUGAR 35/",
         "DATE:07/12/12",
         "TIME:23:52");
 
     doTest("T12",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 350 SPORTSMAN RD ; WERNERSVILLE STATE HOSPITAL; WSH ORCHARD RD / WSH MAIN ST ; Muni: SOUTH HEIDELBERG ; 26YOM COUGHING UP BLOOD ; 07/13/12 00:01\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:ALS MEDICAL",
         "ADDR:350 SPORTSMAN RD",
         "PLACE:WERNERSVILLE STATE HOSPITAL",
         "X:WSH ORCHARD RD / WSH MAIN ST",
         "CITY:SOUTH HEIDELBERG",
         "INFO:26YOM COUGHING UP BLOOD",
         "DATE:07/13/12",
         "TIME:00:01");
 
     doTest("T13",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched BLS MEDICAL ; 517 OAK HILL LN ; ; UNKNOWN / UNKNOWN ; Muni: SPRING ; 84 YOF VOMITTING ; 07/13/12 00:51\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:BLS MEDICAL",
         "ADDR:517 OAK HILL LN",
         "X:UNKNOWN / UNKNOWN",
         "CITY:SPRING",
         "INFO:84 YOF VOMITTING",
         "DATE:07/13/12",
         "TIME:00:51");
 
     doTest("T14",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 135 BALTHASER RD ; ; N ARTHUR DR, S ARTHUR DR / PRESTON RD ; Muni: SOUTH HEIDELBERG ; 42YOF/ COMP IS PT/  ; 07/13/12 06:39\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:ALS MEDICAL",
         "ADDR:135 BALTHASER RD",
         "X:N ARTHUR DR, S ARTHUR DR / PRESTON RD",
         "CITY:SOUTH HEIDELBERG",
         "INFO:42YOF/ COMP IS PT/",
         "DATE:07/13/12",
         "TIME:06:39");
 
     doTest("T15",
         "[berks.650@rsix.roamsecure.net] Unit:M650E Status:Enroute BLS MEDICAL ; 1801 CAMBRIDGE AVE D20 ; TENNIS PARK CONDOS; OLD MILL RD / LAUERS LN ; Muni: WYOMISSING ; ; 07/13/12 08:05\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650E",
         "CALL:BLS MEDICAL",
         "ADDR:1801 CAMBRIDGE AVE",
         "APT:D20",
         "PLACE:TENNIS PARK CONDOS",
         "X:OLD MILL RD / LAUERS LN",
         "CITY:WYOMISSING",
         "DATE:07/13/12",
         "TIME:08:05");
 
     doTest("T16",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 2701 NORTH MERIDIAN BLVD ; IHOP; BROADCASTING RD / MERIDIAN BLVD ; Muni: SPRING ; ML IN 20'S HAVING SEIZURE ; 07/13/12 10:25\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:ALS MEDICAL",
         "ADDR:2701 NORTH MERIDIAN BLVD",
         "PLACE:IHOP",
         "X:BROADCASTING RD / MERIDIAN BLVD",
         "CITY:SPRING",
         "INFO:ML IN 20'S HAVING SEIZURE",
         "DATE:07/13/12",
         "TIME:10:25");
 
     doTest("T17",
         "[berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched ALS MEDICAL ; 1863 LINCOLN AVE VINCENT BECKER RES ; ; BRANDYWINE RD / BRANDYWINE CT ; Muni: WYOMISSING ; 92 YOM ARM IS NUMB/RIGHT EYE DROOPY ; 07/13/12 11:20\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650E",
         "CALL:ALS MEDICAL",
         "ADDR:1863 LINCOLN AVE",
         "NAME:VINCENT BECKER",
         "X:BRANDYWINE RD / BRANDYWINE CT",
         "CITY:WYOMISSING",
         "INFO:92 YOM ARM IS NUMB/RIGHT EYE DROOPY",
         "DATE:07/13/12",
         "TIME:11:20");
 
     doTest("T18",
         "[berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched CLASS 4 MENTAL ; 13 BIRCHWOOD RD ; ; LARCHWOOD RD, CEDARWOOD RD / LARCHWOOD RD ; Muni: WYOMISSING ; sam crisis ; 07/13/12 11:30\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650A",
         "CALL:CLASS 4 MENTAL",
         "ADDR:13 BIRCHWOOD RD",
         "X:LARCHWOOD RD, CEDARWOOD RD / LARCHWOOD RD",
         "CITY:WYOMISSING",
         "INFO:sam crisis",
         "DATE:07/13/12",
         "TIME:11:30");
 
     doTest("T19",
         "[berks.650@rsix.roamsecure.net] Unit:M650I Status:Dispatched ALS MEDICAL ; 356 S FREEMAN ST ; ; E MEADOW AVE / W FREEMAN ST, FURNACE ST ; Muni: ROBESONIA ; 90 YOF / VERY WEAK / DEHYDRATED  ; 07/13/12 12:06\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650I",
         "CALL:ALS MEDICAL",
         "ADDR:356 S FREEMAN ST",
         "X:E MEADOW AVE / W FREEMAN ST, FURNACE ST",
         "CITY:ROBESONIA",
         "INFO:90 YOF / VERY WEAK / DEHYDRATED",
         "DATE:07/13/12",
         "TIME:12:06");
 
     doTest("T20",
         "[berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched ALS MEDICAL ; 310 STITZER AVE ; ; E WILSON AVE, W WILSON AVE / E NORMAN ST, W NORMAN ST ; Muni: WERNERSVILLE ; 88 YOM NOT RESPONDING WELL ; 07/13/12 12:40\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650B",
         "CALL:ALS MEDICAL",
         "ADDR:310 STITZER AVE",
         "X:E WILSON AVE, W WILSON AVE / E NORMAN ST, W NORMAN ST",
         "CITY:WERNERSVILLE",
         "INFO:88 YOM NOT RESPONDING WELL",
         "DATE:07/13/12",
         "TIME:12:40");
 
     doTest("T21",
         "[berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched BLS TRAUMA ; 2912 STATE HILL RD APT B3 ; ; No Cross Streets Found ; Muni: SPRING ; BERKSHIRE ESTATES APTS ; 07/13/12 13:43\n" +
         "Sent by Berks County RSAN to Western Berks EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "UNIT:M650B",
         "CALL:BLS TRAUMA",
         "ADDR:2912 STATE HILL RD",
         "APT:B3",
         "CITY:SPRING",
         "INFO:BERKSHIRE ESTATES APTS",
         "DATE:07/13/12",
         "TIME:13:43");
 
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "Unit:B42 Status:Dispatched BF ; 100 BLIMLINE RD BLOCK ; ; DEAD END / DEER CREEK RD ; SPRING ; ; 09/01/12 07:51",
         "UNIT:B42",
         "CALL:BF",
         "ADDR:100 BLIMLINE RD",
         "APT:BLOCK",
         "CITY:SPRING",
         "X:DEAD END / DEER CREEK RD",
         "DATE:09/01/12",
         "TIME:07:51");
 
    doTest("T2",
        "Subject:1/2\nUnit:M590A Status:Dispatched ALS MEDICAL ; 120 TREXLER AVE RM 114 ; KUTZTOWN MANOR; LENNI ST / CONSTITUTION BLVD \r",
        "UNIT:M590A",
        "CALL:ALS MEDICAL",
        "ADDR:120 TREXLER AVE",
        "APT:114",
        "PLACE:KUTZTOWN MANOR",
        "X:LENNI ST / CONSTITUTION BLVD");

   }
   
   @Test
   public void testActive911B() {
 
     doTest("T1",
         "[berks.680@rsix.roamsecure.net] Unit:M680A Status:Dispatched ALS MEDICAL ; 202 HAYCREEK RD ; ; UNKNOWN / BROOKE MNR ; BIRDSBORO ; F TEMP OF 104 ; 08/28/12 18:14\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680A",
         "CALL:ALS MEDICAL",
         "ADDR:202 HAYCREEK RD",
         "CITY:BIRDSBORO",
         "X:UNKNOWN / BROOKE MNR",
         "INFO:F TEMP OF 104",
         "DATE:08/28/12",
         "TIME:18:14");
 
     doTest("T2",
         "[berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched BLS MEDICAL ; 920 N 4 ST ; METROPOLITAN APARTMENTS; WINDSOR ST / SPRING ST ; READING ; WAITING OUTSIDE ON STAIRS ; 08/28/12 18:33\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680E",
         "CALL:BLS MEDICAL",
         "ADDR:920 N 4 ST",
         "CITY:READING",
         "PLACE:METROPOLITAN APARTMENTS",
         "X:WINDSOR ST / SPRING ST",
         "INFO:WAITING OUTSIDE ON STAIRS",
         "DATE:08/28/12",
         "TIME:18:33");
 
     doTest("T3",
         "[berks.680@rsix.roamsecure.net] Unit:680I Status:Dispatched ALS MEDICAL ; 1254 HIGHVIEW PL ; ; MANOR PL / FAIRVIEW CHAPEL RD ; EXETER ; 24 YOF / SEIZURE / HIT HEAD ON GROUND  ; 08/28/12 19:24\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:680I",
         "CALL:ALS MEDICAL",
         "ADDR:1254 HIGHVIEW PL",
         "CITY:EXETER",
         "X:MANOR PL / FAIRVIEW CHAPEL RD",
         "INFO:24 YOF / SEIZURE / HIT HEAD ON GROUND",
         "DATE:08/28/12",
         "TIME:19:24");
 
     doTest("T4",
         "[berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched ALS TRAUMA ; 450 FURNACE RD ; ; MELODY LN / CYPRESS LN ; ROBESON ; ; 08/28/12 19:45\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680E",
         "CALL:ALS TRAUMA",
         "ADDR:450 FURNACE RD",
         "CITY:ROBESON",
         "X:MELODY LN / CYPRESS LN",
         "DATE:08/28/12",
         "TIME:19:45");
 
     doTest("T5",
         "[berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched ALS MEDICAL ; 31 WASHINGTON ST ; ; S MILLER ST / NEW HOLLAND AVE ; SHILLINGTON ; 54 YOM / DISORIENTED / DIAPHORETIC /  ; 08/28/12 21:06\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680E",
         "CALL:ALS MEDICAL",
         "ADDR:31 WASHINGTON ST",
         "CITY:SHILLINGTON",
         "X:S MILLER ST / NEW HOLLAND AVE",
         "INFO:54 YOM / DISORIENTED / DIAPHORETIC /",
         "DATE:08/28/12",
         "TIME:21:06");
 
     doTest("T6",
         "[berks.680@rsix.roamsecure.net] Unit:M680I Status:Dispatched MVAUNK ; W BENJAMIN FRANKLIN HWY RIVER BRIDGE RD ; ; ; AMITY ; VEH IN GRASS ; 08/28/12 21:38\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680I",
         "CALL:MVAUNK",
         "ADDR:W BENJAMIN FRANKLIN HWY & RIVER BRIDGE RD",
         "CITY:AMITY",
         "INFO:VEH IN GRASS",
         "DATE:08/28/12",
         "TIME:21:38");
 
     doTest("T7",
         "[berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched BLS TRAUMA ; 1623 MORGANTOWN RD ROOM 158 ; HEALTH SOUTH; HOPE WAY / CARRIAGE LN ; CUMRU ; 86YOM/ FELL OUT OF BED/ LACERATION TO HEAD ; 08/29/12 01:16\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680E",
         "CALL:BLS TRAUMA",
         "ADDR:1623 MORGANTOWN RD",
         "APT:158",
         "CITY:CUMRU",
         "PLACE:HEALTH SOUTH",
         "X:HOPE WAY / CARRIAGE LN",
         "INFO:86YOM/ FELL OUT OF BED/ LACERATION TO HEAD",
         "DATE:08/29/12",
         "TIME:01:16");
 
     doTest("T8",
         "[berks.680@rsix.roamsecure.net] Unit:M680A Status:Dispatched LIFT ASSIST ; 406 S WALNUT ST ; ; E 4 ST / E 5 ST ; BIRDSBORO ; 55YOF/ FELL AND NEEDS HELP GETTING UP ; 08/29/12 05:30\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680A",
         "CALL:LIFT ASSIST",
         "ADDR:406 S WALNUT ST",
         "CITY:BIRDSBORO",
         "X:E 4 ST / E 5 ST",
         "INFO:55YOF/ FELL AND NEEDS HELP GETTING UP",
         "DATE:08/29/12",
         "TIME:05:30");
 
     doTest("T9",
         "[berks.680@rsix.roamsecure.net] Unit:M680I Status:Dispatched ALS MEDICAL ; 1152 E BENJAMIN FRANKLIN HWY 219 ; VILLA AT MORLATTON ASSISTED LIVING; MAPLEWOOD DR / OLD AIRPORT RD ; AMITY ; 91 YOF SLURRED SPEECH ; 08/29/12 05:32\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680I",
         "CALL:ALS MEDICAL",
         "ADDR:1152 E BENJAMIN FRANKLIN HWY",
         "APT:219",
         "CITY:AMITY",
         "PLACE:VILLA AT MORLATTON ASSISTED LIVING",
         "X:MAPLEWOOD DR / OLD AIRPORT RD",
         "INFO:91 YOF SLURRED SPEECH",
         "DATE:08/29/12",
         "TIME:05:32");
 
     doTest("T10",
         "[berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched ALS TRAUMA ; 70 N ONEIL ST ; ; UNKNOWN / W MADISON ST ; MOHNTON ; 90 YOM FALL ; 08/29/12 09:00\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680E",
         "CALL:ALS TRAUMA",
         "ADDR:70 N ONEIL ST",
         "CITY:MOHNTON",
         "X:UNKNOWN / W MADISON ST",
         "INFO:90 YOM FALL",
         "DATE:08/29/12",
         "TIME:09:00");
 
     doTest("T11",
         "[berks.680@rsix.roamsecure.net] Unit:M680I Status:Dispatched MVAWITH ; W BENJAMIN FRANKLIN HWY MONOCACY HILL RD ; ; ; AMITY ; ; 08/29/12 09:18\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680I",
         "CALL:MVAWITH",
         "ADDR:W BENJAMIN FRANKLIN HWY & MONOCACY HILL RD",
         "CITY:AMITY",
         "DATE:08/29/12",
         "TIME:09:18");
 
     doTest("T12",
         "[berks.680@rsix.roamsecure.net] Unit:M680F Status:Dispatched BLS TRAUMA ; 790 MAPLEWOOD AVE ; ; CEDARWOOD LN / MOHNTON BLVD ; CUMRU ; 70YOM/STAGE 4 PROSTATE CANCER/WEAK/FELL THIS MORN\r\n" +
         " ; 08/29/12 09:42\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680F",
         "CALL:BLS TRAUMA",
         "ADDR:790 MAPLEWOOD AVE",
         "CITY:CUMRU",
         "X:CEDARWOOD LN / MOHNTON BLVD",
         "INFO:70YOM/STAGE 4 PROSTATE CANCER/WEAK/FELL THIS MORN",
         "DATE:08/29/12",
         "TIME:09:42");
 
     doTest("T13",
         "[berks.680@rsix.roamsecure.net] Unit:680A Status:Dispatched ALS MEDICAL ; 24 BUTTERCUP RD H213-27 ; ; DEAD END / SUNSET DR ; AMITY ; 50 YOF ASTHMA ; 08/29/12 10:21\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:680A",
         "CALL:ALS MEDICAL",
         "ADDR:24 BUTTERCUP RD",
         "APT:H213-27",
         "CITY:AMITY",
         "X:DEAD END / SUNSET DR",
         "INFO:50 YOF ASTHMA",
         "DATE:08/29/12",
         "TIME:10:21");
 
     doTest("T14",
         "[berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched BLS TRAUMA ; 20 S SUMMIT AVE APT 1002 ; SENCIT APARTMENTS; COMMUNITY DR / HARDING AVE, BROOKE BLVD ; CUMRU ; FALL VICTIM CUT  ; 08/29/12 11:48\r\n" +
         "\r\n" +
         "Sent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n" +
         "--\r\r\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n" +
         "\r\n",
 
         "UNIT:M680E",
         "CALL:BLS TRAUMA",
         "ADDR:20 S SUMMIT AVE",
         "APT:1002",
         "CITY:CUMRU",
         "PLACE:SENCIT APARTMENTS",
         "X:COMMUNITY DR / HARDING AVE, BROOKE BLVD",
         "INFO:FALL VICTIM CUT",
         "DATE:08/29/12",
         "TIME:11:48");
   
   }
   
   
   public static void main(String[] args) {
     new PABerksCountyParserTest().generateTests("T1");
   }
 }

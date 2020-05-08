 package net.anei.cadpage.parsers.CA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CASonomaCountyParserTest extends BaseParserTest {
   
   public CASonomaCountyParserTest() {
     setParser(new CASonomaCountyParser(), "SONOMA COUNTY", "CA");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "Loc: HWY1/VALLEY FORD ROAD VFR BOX: 3540 TYP:TC CN: CHP LOG # 632 C#: TYPE CODE: TC CALLER NAME: CHP LOG # 632 CALLER ADDR:  TIME: 11:07:31 COM: ** Case n",
         "ADDR:HWY1 & VALLEY FORD ROAD",
         "SRC:VFR",
         "BOX:3540",
         "CALL:TC",
         "NAME:CHP LOG # 632",
         "INFO:** Case n");
 
     doTest("T2",
         "Loc: 1320 BAY VIEW ST BBY:@ BODEGA UNION CHURCH BOX: 3433 B3 TYP: GAS-IN CN: BODEGA BAY UNION CHURCH C#: (707)875-3559 TYPE CODE: GAS-IN CALLER NAME: BODEGA",
         "ADDR:1320 BAY VIEW ST",
         "SRC:BBY",
         "PLACE:BODEGA UNION CHURCH",
         "BOX:3433 B3",
         "CALL:GAS-IN",
         "NAME:BODEGA",
         "PHONE:(707)875-3559");
 
     doTest("T3",
         "Loc: BBY:@HWY 1MM012.42 BOX:3332 B TYP: TC-EX CN: CHP LOG 344 C#: TYPE CODE: TC_EX CALLER NAME:CHP LOG 344 CALLER ADDR:  TIME: 03:44:34 COM: OVER TURN",
         "ADDR:HWY 1MM012.42",
         "SRC:BBY",
         "BOX:3332 B",
         "CALL:TC-EX",
         "NAME:CHP LOG 344",
         "INFO:OVER TURN");
 
     doTest("T4",
         "Loc: 1400 VALLEY FORD FREESTONE RD VFR BOX: 3439 B TYP: SER-PA CN: JOANNA C#: (707) 876-3288 TYPE CODE: SER-PA CALLER NAME: JOANNA CALLER ADDR:  TIME: 13:01",
         "ADDR:1400 VALLEY FORD FREESTONE RD",
         "SRC:VFR",
         "BOX:3439 B",
         "CALL:SER-PA",
         "NAME:JOANNA",
         "PHONE:(707) 876-3288");
 
     doTest("T5",
         "Loc: 2458 BIG OAK DR SR BOX: 2946 D4 TYP: STRUW CN: AT&T MOBILITY 80 635 6840  4 C#: (707) 327-7382 TYPE CODE: STRUW CALLER NAME: AT&T MOBILITY",
         "ADDR:2458 BIG OAK DR",
         "SRC:SR",
         "BOX:2946 D4",
         "CALL:STRUW",
         "NAME:AT&T MOBILITY",
         "PHONE:(707) 327-7382");
 
     doTest("T6",
         "Loc: CALISTOGA RD/CHALFANT RD RIN BOX: 2452 D TYP: TC-EX CN: CHP LOG#1987 C#:  TYPE CODE: TC-EX CALLER NAME: CHP LOG#1987 CALLER ADDR:  TIME: 19:32:46 COM:",
         "ADDR:CALISTOGA RD & CHALFANT RD",
         "SRC:RIN",
         "BOX:2452 D",
         "CALL:TC-EX",
         "NAME:CHP LOG#1987");
 
     doTest("T7",
         "Loc: 801 SANTA BARBARA DR SRO BOX: 3049 B4 TYP: GAS-IN CN: JOEY C#: (707) 391-8596 TYPE CODE: GAS-IN CALLER NAME: JOEY CALLER ADDR:  TIME: 18:14:37 COM:  Ev",
         "ADDR:801 SANTA BARBARA DR",
         "SRC:SRO",
         "BOX:3049 B4",
         "CALL:GAS-IN",
         "NAME:JOEY",
         "PHONE:(707) 391-8596",
         "INFO:Ev");
 
     doTest("T8",
         "Loc: 311 BELHAVEN CI SR BOX: 2955 C3 TYP: STRU CN: COUGHLAN JAQUELINE C#: (707) 538-7881 TYPE CODE: STRU CALLER NAME: COUGHLAN JAQUELINE CALLER ADDR: 311 BE",
         "ADDR:311 BELHAVEN CI",
         "SRC:SR",
         "BOX:2955 C3",
         "CALL:STRU",
         "NAME:COUGHLAN JAQUELINE",
         "PHONE:(707) 538-7881");
 
     doTest("T9",
         "Loc: 187 PEACH BLOSSOM DR SR BOX: 2847 B4 TYP: GAS-IN CN:  C#: (707) 523-4778 TYPE CODE: GAS-IN CALLER NAME:  CALLER ADDR:  TIME: 12:50:50 COM:  UNSURE WHAT",
         "ADDR:187 PEACH BLOSSOM DR",
         "SRC:SR",
         "BOX:2847 B4",
         "CALL:GAS-IN",
         "PHONE:(707) 523-4778",
         "INFO:UNSURE WHAT");
 
     doTest("T10",
         "Loc: 2637 WILD BILL CI SR BOX: 3048 C4 TYP: STRU CN: BARRAGAN MARTHA C#: (707) 544-5719 TYPE CODE: STRU CALLER NAME: BARRAGAN MARTHA CALLER ADDR: 2637 WILD",
         "ADDR:2637 WILD BILL CI",
         "SRC:SR",
         "BOX:3048 C4",
         "CALL:STRU",
         "NAME:BARRAGAN MARTHA",
         "PHONE:(707) 544-5719");
 
     doTest("T11",
         "Loc: DUTTON MEADOW/TUXHORN DR SR BOX: 3048 D4 CN: TOGNERI JOHN W C#: (707) 545-7701 TYP: STRU CALLER ADDR: 2742 MORGAN CREEK TIME: 14:58:04 COM:  OUT BLDG /",
         "ADDR:DUTTON MEADOW & TUXHORN DR",
         "SRC:SR",
         "CALL:STRU",
         "BOX:3048 D4",
         "NAME:TOGNERI JOHN W",
         "PHONE:(707) 545-7701",
         "INFO:OUT BLDG /");
 
     doTest("T12",
         "Loc: 4585 OLD REDWOOD HW LAR BOX: 2747 B2 CN: AT&T MOBILITY 800 635 6840  4 C#: (707) 953-6573 TYP: VEG CALLER ADDR: 434 Pacific Heights Drive S RS TIME: 17",
         "ADDR:4585 OLD REDWOOD HWY",
         "SRC:LAR",
         "BOX:2747 B2",
         "CALL:VEG",
         "NAME:AT&T MOBILITY 800 635 6840  4",
         "PHONE:(707) 953-6573");
 
     doTest("T13",
         "Loc: SR: @SB 101 AT 12 BOX: 3049 A2 CN: CHP LOG 721 C#:  TYP: TC-EX CALLER ADDR:  TIME: 10:44:33 COM:  CRO 3 ** Case number SRS11016088 has been assigned fo",
         "ADDR:SB 101 & 12",
         "SRC:SR",
         "BOX:3049 A2",
         "CALL:TC-EX",
         "NAME:CHP LOG 721",
         "INFO:CRO 3 ** Case number SRS11016088 has been assigned fo");
 
     doTest("T14",
         "Loc: 2055 RANGE AV SR,326: @LAMPLIGHTERS SENIOR CITIZEN INN BOX: 2848 D3 CN: MCNEIL K C#: (707) 528-6259 TYP: FA-RES CALLER ADDR: 2055 RANGE AV TIME: 22:39:",
         "ADDR:2055 RANGE AV",
         "SRC:SR",
         "PLACE:LAMPLIGHTERS SENIOR CITIZEN INN",
         "BOX:2848 D3",
         "CALL:FA-RES",
         "NAME:MCNEIL K",
         "PHONE:(707) 528-6259",
         "APT:326");
 
     doTest("T15",
         "Loc: 2389 MCBRIDE LN SR,12: @PARK VILLA APTS BOX: 2848 D1 CN:  C#:  TYP: STRU CALLER ADDR:  TIME: 17:46:46 COM:  apt 12 ** Case number SRS11015986 has been",
         "ADDR:2389 MCBRIDE LN",
         "SRC:SR",
         "PLACE:PARK VILLA APTS",
         "BOX:2848 D1",
         "CALL:STRU",
         "APT:12",
         "INFO:apt 12 ** Case number SRS11015986 has been");
 
     doTest("T16",
         "Loc: BEL: @NB 101 NO TODD BOX: 3149 C4 CN:  C#:  TYP: TC-EX CALLER ADDR:  TIME: 08:36:59 COM:  VEH OVERTURNED ** Case number SRS11015469 has been assigned f",
         "ADDR:NB 101 NO TODD",
         "SRC:BEL",
         "BOX:3149 C4",
         "CALL:TC-EX",
         "INFO:VEH OVERTURNED ** Case number SRS11015469 has been assigned f");
 
     doTest("T17",
         "Loc: 2350 MCBRIDE LN SR,C9: @MCBRIDE APTS BOX: 2848 D2 CN: GUTIERREZ A C#: (707) 576-7446 TYP: GAS-IN CALLER ADDR: 2350 MC BRIDE LN TIME: 22:04:37 COM:  **",
         "ADDR:2350 MCBRIDE LN",
         "SRC:SR",
         "PLACE:MCBRIDE APTS",
         "BOX:2848 D2",
         "CALL:GAS-IN",
         "NAME:GUTIERREZ A",
         "PHONE:(707) 576-7446",
         "APT:C9",
         "INFO:**");
 
     doTest("T18",
         "Loc: WALKER AV/TODD RD BEL BOX: 3247 A CN:  C#:  TYP: STRU CALLER ADDR:  TIME: 02:07:14 COM:  FLAMES SEEN RP FROM 5002 LLANO RD APPEARS TO BE SPREADING ** C",
         "ADDR:WALKER AV & TODD RD",
         "SRC:BEL",
         "BOX:3247 A",
         "CALL:STRU",
         "INFO:FLAMES SEEN RP FROM 5002 LLANO RD APPEARS TO BE SPREADING ** C");
 
     doTest("T19",
         "Loc: 555 1ST ST SR: @LOT 12 BOX: 2949 D1 CN: BARRY C#: 1-800-470-1000 TYP: FA-WF CALLER ADDR: BAY ALARM TIME: 13:51:38 COM:  CONTROL 3 WATER FLOW 6 INCH MAI",
         "ADDR:555 1ST ST",
         "SRC:SR",
         "PLACE:LOT 12",
         "BOX:2949 D1",
         "CALL:FA-WF",
         "NAME:BARRY",
         "PHONE:1-800-470-1000",
         "INFO:CONTROL 3 WATER FLOW 6 INCH MAI");
 
     doTest("T20",
         "Loc: W 3RD ST/TRACKS SR BOX: 2949 C3 CN:  C#:  TYP: VEG CALLER ADDR:  TIME: 14:06:16 COM:  AT THE CREEK PEOPLE WERE SCREAMING FOR SOMEONE TO CALL UNDER THE",
         "ADDR:W 3RD ST & TRACKS",
         "SRC:SR",
         "BOX:2949 C3",
         "CALL:VEG",
         "INFO:AT THE CREEK PEOPLE WERE SCREAMING FOR SOMEONE TO CALL UNDER THE");
 
     doTest("T21",
         "Loc: SR: @SB 101 AT CORBY OFF BOX: 3049 A4 TYP: VEHFULL CN: AT&T MOBILITY 800 635 6840  4 C#: (707) 480-1310 TYPE CODE: VEHFULL CALLER NAME: AT&T MOBILITY 8",
         "ADDR:SB 101 & CORBY OFF",
         "SRC:SR",
         "BOX:3049 A4",
         "CALL:VEHFULL",
         "NAME:AT&T MOBILITY 8",
         "PHONE:(707) 480-1310");
 
     doTest("T22",
         "Loc: 1621 HERBERT ST SR,7 BOX: 2848 D3 CN:  C#:  TYP: STRU CALLER ADDR:  TIME: 00:11:13 COM:  SOME KIND OF ODD BEEPING HAS BEEN GOING ON INSIDE THE APT 7 RP",
         "ADDR:1621 HERBERT ST",
         "SRC:SR",
         "BOX:2848 D3",
         "CALL:STRU",
         "APT:7",
         "INFO:SOME KIND OF ODD BEEPING HAS BEEN GOING ON INSIDE THE APT 7 RP");
 
     doTest("T23",
         "Loc: 1051 YUBA DR ROS BOX: 3147 B2 CN: WILLIAMS RUTH C#: (707) 544-8224 TYP: STRU CALLER ADDR: 962 YUBA DR TIME: 17:59:51 COM:  SMOKE FROM THE ROOF RED HOUS",
         "SRC:ROS",
         "ADDR:1051 YUBA DR",
         "BOX:3147 B2",
         "CALL:STRU",
         "NAME:WILLIAMS RUTH",
         "PHONE:(707) 544-8224",
         "INFO:SMOKE FROM THE ROOF RED HOUS");
 
  }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "Loc: 1142 DUER RD WSR BOX: 3045 C4 TYP: HC CN: SANDRA C#: (707) 484-0358 TYPE CODE: HC CALLER NAME: SANDRA CALLER ADDR:  TIME: 14:50:20 COM:  BROKEN SPRINKL",
         "ADDR:1142 DUER RD",
         "SRC:WSR",
         "BOX:3045 C4",
         "CALL:HC",
         "NAME:SANDRA",
         "PHONE:(707) 484-0358",
         "INFO:BROKEN SPRINKL");
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "Loc: 420 GREVE LN SO BOX: 3662 A1 CN:  C#: (707) 939-9366 TYP: MED CALLER ADDR:  TIME: 01:38:32 COM:  HIGH BP FEELING ILL ** Case number SON11002627 has bee",
         "ADDR:420 GREVE LN",
         "SRC:SO",
         "BOX:3662 A1",
         "CALL:MED",
         "PHONE:(707) 939-9366",
         "INFO:HIGH BP FEELING ILL ** Case number SON11002627 has bee");
 
     doTest("T2",
         "Loc: 1850 SPERRING RD SCH BOX: 3759 B1 CN: STANLEY 877 476 4968 C#: (707) 933-9300 TYP: FA-RES CALLER ADDR:  TIME: 09:26:52 COM:  SMOKE DETECTOR HALLWAY CON",
         "ADDR:1850 SPERRING RD",
         "SRC:SCH",
         "BOX:3759 B1",
         "CALL:FA-RES",
         "NAME:STANLEY 877 476 4968",
         "PHONE:(707) 933-9300",
         "INFO:SMOKE DETECTOR HALLWAY CON");
 
     doTest("T3",
         "Loc: 800 OREGON ST SO: @MERRILL GARDENS,266 BOX: 3660 B4 CN: DOMOGALLA VERN F C#: (707) 996-8354 TYP: SER-PA CALLER ADDR: 800 OREGON TIME: 09:52:45 COM:  AP",
         "ADDR:800 OREGON ST",
         "SRC:SO",
         "PLACE:MERRILL GARDENS,266",
         "BOX:3660 B4",
         "CALL:SER-PA",
         "NAME:DOMOGALLA VERN F",
         "PHONE:(707) 996-8354",
         "INFO:AP");
 
   }
   
   @Test
   public void testEmailParser() {
 
     doTest("T1",
         "Loc: VALLEY FORD RD/HWY 1 VFR BOX: 3540 TYP: TC CN: AT&T MOBILITY 800 635 6840  4 C#: (650) 455-7732 TYPE CODE: TC CALLER NAME: AT&T MOBILITY 800 635 6840  4 CALLER ADDR: 2885 BAY HILL RD BDGA TIME: 16:00:06 COM:  N -122.7320 T 38.40650 METERS 2758 MOTORCYCLE",
         "ADDR:VALLEY FORD RD & HWY 1",
         "SRC:VFR",
         "BOX:3540",
         "CALL:TC",
         "NAME:AT&T MOBILITY 800 635 6840  4",
         "PHONE:(650) 455-7732",
         "INFO:N -122.7320 T 38.40650 METERS 2758 MOTORCYCLE");
 
     doTest("T2",
         "Loc: 14460 SCHOOL ST VFR BOX: 3539 A2 TYP: MED CN: BEAL FRED C#: (707) 876-3232 TYPE CODE: MED CALLER NAME: BEAL FRED CALLER ADDR: 14460 SCHOOL TIME: 20:34:31 COM:  SCSO 911 HAD SURGERY IS SF A WEEK AGO LEG SWOLLEN FROM WHERE THE SURGERY WAS IN HIS LEG CONTROL 2 PT HAS A 102 TEMP DR TOLD HIM TO CALL 911 78 YOM",
         "ADDR:14460 SCHOOL ST",
         "SRC:VFR",
         "BOX:3539 A2",
         "CALL:MED",
         "NAME:BEAL FRED",
         "PHONE:(707) 876-3232",
         "INFO:SCSO 911 HAD SURGERY IS SF A WEEK AGO LEG SWOLLEN FROM WHERE THE SURGERY WAS IN HIS LEG CONTROL 2 PT HAS A 102 TEMP DR TOLD HIM TO CALL 911 78 YOM");
 
     doTest("T3",
         "Loc: 814 OWL CT BBY BOX: 3535 C1 TYP: STRU CN: CASTLE,STACEY C#: (707) 875-9870 TYPE CODE: STRU CALLER NAME: CASTLE,STACEY CALLER ADDR: 814 OWL CT TIME: 10:17:55 COM:  SCSO- 911 SMOKE COMING FROM AN ELECTICAL OUTELT",
         "ADDR:814 OWL CT",
         "SRC:BBY",
         "BOX:3535 C1",
         "CALL:STRU",
         "NAME:CASTLE,STACEY",
         "PHONE:(707) 875-9870",
         "INFO:SCSO- 911 SMOKE COMING FROM AN ELECTICAL OUTELT");
 
     doTest("T4",
         "Loc: BLOOMFIELD RD/SUTTON ST BLO BOX: 3542 B4 TYP: HC CN: 8540 C#:  TYPE CODE: HC CALLER NAME: 8540 CALLER ADDR:  TIME: 20:39:02 COM:  veh in ditch and water start 8580",
         "ADDR:BLOOMFIELD RD & SUTTON ST",
         "SRC:BLO",
         "BOX:3542 B4",
         "CALL:HC",
         "NAME:8540",
         "INFO:veh in ditch and water start 8580");
 
     doTest("T5",
         "BOX: 3437 TYP: VEG CN: VERIZON WIRELESS 800 451 5242 4 C#: (707) 321-9508 TYPE CODE: VEG CALLER NAME: VERIZON WIRELESS 800 451 5242 4 CALLER ADDR: 2885 BAY HILL RD BDGA BAY TIME: 12:33:30 COM:  N -122.9738 T 38.33546 METERS 41 RIGHT BEFORE THE BODEGA HWY TURN OFF SHE COULD NOT GIVE AN ADDRESS OR A MILE MARKER",
         "ADDR:2885 BAY HILL RD",
         "CITY:BODEGA BAY",
         "BOX:3437",
         "CALL:VEG",
         "NAME:VERIZON WIRELESS 800 451 5242 4",
         "PHONE:(707) 321-9508",
         "INFO:N -122.9738 T 38.33546 METERS 41 RIGHT BEFORE THE BODEGA HWY TURN OFF SHE COULD NOT GIVE AN ADDRESS OR A MILE MARKER");
 
     doTest("T6",
         "Loc: BBY: @HWY 1 MM012.42 BOX: 3332 B TYP: TC-EX CN: CHP LOG 344 C#:  TYPE CODE: TC-EX CALLER NAME: CHP LOG 344 CALLER ADDR:  TIME: 03:44:34 COM:  OVER TURNED VEH NEAR SALMON CREEK CONTROL 2",
         "ADDR:HWY 1 MM012.42",
         "SRC:BBY",
         "BOX:3332 B",
         "CALL:TC-EX",
         "NAME:CHP LOG 344",
         "INFO:OVER TURNED VEH NEAR SALMON CREEK CONTROL 2");
 
     doTest("T7",
         "Loc: HWY 1/BODEGA HW BOD BOX: 3436 TYP: TC-EX CN: CHP C#:  TYPE CODE: TC-EX CALLER NAME: CHP CALLER ADDR:  TIME: 20:46:20 COM:  OVERTURNED VEH ON HWY 1, BETWEEN VALLEY FORD AND BODEGA HWY BAD CONNECTION WITH RP, PER CHP",
         "ADDR:HWY 1 & BODEGA HWY",
         "SRC:BOD",
         "BOX:3436",
         "CALL:TC-EX",
         "NAME:CHP",
         "INFO:OVERTURNED VEH ON HWY 1, BETWEEN VALLEY FORD AND BODEGA HWY BAD CONNECTION WITH RP, PER CHP");
 
     doTest("T8",
         "(Wilmar) Loc: 200 KUCK LN PET BOX: 3749 D CN:COM:  DIARRHEA DIFFICULTY BREATHING **",
         "ADDR:200 KUCK LN",
         "SRC:PET",
         "BOX:3749 D",
         "NAME:COM:  DIARRHEA DIFFICULTY BREATHING **");
     
   }
   
   public static void main(String[] args) {
     new CASonomaCountyParserTest().generateTests("T1");
   }
 }

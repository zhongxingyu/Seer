 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PABucksCountyAParser;
 
 import org.junit.Test;
 
 
 public class PABucksCountyAParserTest extends BaseParserTest {
   
   public PABucksCountyAParserTest() {
     setParser(new PABucksCountyAParser(), "BUCKS COUNTY", "PA");
   }
   
   @Test
   public void testParserOld() {
 
     doTest("T1",
         "911:SQ134  type:BTAI    adr:S LIMEKILN PK/UPPER STATE RD ,48  aai:  box:74064  map:3033F7  tm:21:05:23  ED1042264",
         "SRC:SQ134",
         "CALL:BTAI - BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
         "ADDR:S LIMEKILN PK & UPPER STATE RD",
         "MADDR:S LIMEKILN PIKE & UPPER STATE RD",
         "CITY:NEW BRITAIN TWP",
         "BOX:74064",
         "MAP:3033F7",
         "TIME:21:05:23",
         "ID:ED1042264");
 
     doTest("T2",
         "911:SQ134  type:ARESP   adr:4217 HORSESHOE WY ,36  btwn BERRY BROW DR &amp; SPARROW WY  aai:  box:60002  map:2921B10  tm:05:02:04  ED1042293  ",
         "SRC:SQ134",
         "CALL:ARESP - RESPIRATORY DISTRESS",
         "ADDR:4217 HORSESHOE WY",
         "CITY:HILLTOWN TWP",
         "X:BERRY BROW DR & SPARROW WY",
         "BOX:60002",
         "MAP:2921B10",
         "TIME:05:02:04",
         "ID:ED1042293");
 
     doTest("T3",
         "911:SQ134  type:ARESP   adr:221 HAMPSHIRE DR ,48  btwn CORNWALL DR &amp; SURREY RD  aai:  box:74062  map:3033E6  tm:14:06:02  ED1042334  ",
         "SRC:SQ134",
         "CALL:ARESP - RESPIRATORY DISTRESS",
         "ADDR:221 HAMPSHIRE DR",
         "CITY:NEW BRITAIN TWP",
         "X:CORNWALL DR & SURREY RD",
         "BOX:74062",
         "MAP:3033E6",
         "TIME:14:06:02",
         "ID:ED1042334");
 
     doTest("T4",
         "911:SQ134  type:AFAINT  adr:1553 LOWER STATE RD ,29  btwn BRISTOL RD &amp; OLD NEW RD  aai:  box:78054  map:3034E7  tm:20:28:45  ED1042385  ",
         "SRC:SQ134",
         "CALL:AFAINT - SYNCOPAL EPISODE",
         "ADDR:1553 LOWER STATE RD",
         "CITY:DOYLESTOWN TWP",
         "X:BRISTOL RD & OLD NEW RD",
         "BOX:78054",
         "MAP:3034E7",
         "TIME:20:28:45",
         "ID:ED1042385");
 
     doTest("T5",
         "911:SQ134  type:BTAI    adr:1235 S TOWNSHIP LINE RD ,36  btwn CHALFONT RD &amp; LIMEKILN PK  aai:  box:61003  map:2921D10  tm:12:02:56  ED1042577  ",
         "SRC:SQ134",
         "CALL:BTAI - BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
         "ADDR:1235 S TOWNSHIP LINE RD",
         "CITY:HILLTOWN TWP",
         "X:CHALFONT RD & LIMEKILN PK",
         "BOX:61003",
         "MAP:2921D10",
         "TIME:12:02:56",
         "ID:ED1042577");
 
     doTest("T6",
         "911:SQ134  type:BBLED   adr:102 HARRISON FORGE CT ,48  btwn E FAIRWOOD DR &amp; CUL DE SAC  aai:  box:34030  map:3033H1  tm:14:38:26  ED1042595  ",
         "SRC:SQ134",
         "CALL:BBLED - BLEEDING (NON TRAUMATIC)",
         "ADDR:102 HARRISON FORGE CT",
         "CITY:NEW BRITAIN TWP",
         "X:E FAIRWOOD DR & CUL DE SAC",
         "BOX:34030",
         "MAP:3033H1",
         "TIME:14:38:26",
         "ID:ED1042595");
 
     doTest("T7",
         "911:SQ134  type:BINJ    adr:BARC HOUSE ,47 at 80 PUEBLO RD ,47 btwn LENAPE DR &amp; UTE RD  aai:DOWNSTAIRS  box:34033  map:3033J2  tm:16:57:04  ED1043086  ",
         "SRC:SQ134",
         "CALL:BINJ - MINOR INJURY - NON CRITICAL",
         "PLACE:BARC HOUSE",
         "ADDR:80 PUEBLO RD",
         "CITY:NEW BRITAIN",
         "X:LENAPE DR & UTE RD",
         "INFO:DOWNSTAIRS",
         "BOX:34033",
         "MAP:3033J2",
         "TIME:16:57:04",
         "ID:ED1043086");
 
     doTest("T8",
         "911:STA19  type:FALRM   adr:DTH ,29 at 595 W STATE ST ,29 btwn PROGRESS DR &amp; RT 611 BY  aai:215 345-2200  box:19051  map:3034G2  tm:12:37:42  FD101999",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "PLACE:DTH",
         "ADDR:595 W STATE ST",
         "CITY:DOYLESTOWN TWP",
         "X:PROGRESS DR & RT 611 BY",
         "INFO:215 345-2200",
         "BOX:19051",
         "MAP:3034G2",
         "TIME:12:37:42",
         "ID:FD101999");
 
     doTest("T9",
         "911:SQ134  type:ASEIZ   adr:BRISTOL RD/UPPER STATE RD ,29  aai:AT ENTR TO BLOOMING GLEN  box:34041  map:3033K5  tm:11:01:28  ED1043679",
         "SRC:SQ134",
         "CALL:ASEIZ - SEIZURES - ACTIVE",
         "ADDR:BRISTOL RD & UPPER STATE RD",
         "CITY:DOYLESTOWN TWP",
         "INFO:AT ENTR TO BLOOMING GLEN",
         "BOX:34041",
         "MAP:3033K5",
         "TIME:11:01:28",
         "ID:ED1043679");
 
     doTest("T10",
         "911: STA19  type:FUNK    adr:EDISON RD/RT 611 ,29  aai:  box:79028  map:3034K5  tm:12:44:52  FD1108375    Run: E79    \n",
         "SRC:STA19",
         "CALL:FUNK - UNKNOWN TYPE FIRE (LOC)",
         "ADDR:EDISON RD & RT 611",
         "CITY:DOYLESTOWN TWP",
         "BOX:79028",
         "MAP:3034K5",
         "TIME:12:44:52",
         "ID:FD1108375",
         "UNIT:E79");
 
     doTest("T11",
         "911: STA16  type:RAUTO   adr:ROBS TRUCK & TRAIL ,22 at 750 STREET RD ,22 btwn CLIVE AV & SAMUEL AV  aai:IFO  box:37017  map:3374A8  tm:16:43:05  FD1116594    Ru",
         "SRC:STA16",
         "CALL:RAUTO - AUTO EXTRICATION (RBOX)",
         "PLACE:ROBS TRUCK & TRAIL",
         "ADDR:750 STREET RD",
         "CITY:BENSALEM TWP",
         "X:CLIVE AV & SAMUEL AV",
         "INFO:IFO",
         "BOX:37017",
         "MAP:3374A8",
         "TIME:16:43:05",
         "ID:FD1116594");
 
     doTest("T12",
         "911:SQ134  type:ATRAN   adr:2343 BETHLEHEM PK #312 ,36 -- DOCK MEADOWS NH btwn UNIONVILLE PK &amp; CHURCH RD  aai:GO TO CHURCH RD NORTH ENTRANCE box:60063 map:3032E3 &nbsp;tm:14:48:06 ED1042598&nbsp;",
         "SRC:SQ134",
         "CALL:ATRAN - ALS/UNSPECIFIED EMERGENCY",
         "PLACE:DOCK MEADOWS NH",
         "ADDR:2343 BETHLEHEM PK",
         "MADDR:2343 BETHLEHEM PIKE",
         "APT:312",
         "CITY:HILLTOWN TWP",
         "X:UNIONVILLE PK & CHURCH RD",
         "INFO:GO TO CHURCH RD NORTH ENTRANCE",
         "BOX:60063",
         "MAP:3032E3",
         "TIME:14:48:06",
         "ID:ED1042598");
 
     doTest("T13",
         "911: SQ134  type:BFALL   adr:800 MANOR DR #315 ,48 -- NEW SEASONS ASSIST btwn E SCHOOL HOUSE RD & COUNTY LINE  aai:  box:74066 map:3033E8 tm:16:50:43  ED1101813",
         "SRC:SQ134",
         "CALL:BFALL - FALL VICTIM - NON CRITICAL",
         "PLACE:NEW SEASONS ASSIST",
         "ADDR:800 MANOR DR",
         "APT:315",
         "CITY:NEW BRITAIN TWP",
         "X:E SCHOOL HOUSE RD & COUNTY LINE",
         "BOX:74066",
         "MAP:3033E8",
         "TIME:16:50:43",
         "ID:ED1101813");
 
     doTest("T14",
         "911: SQ134  type:AALLR   adr:4371 COUNTY LINE RD ,48 -- VERTIS DIRECT MKTG btwn BRITAIN WOOD CI & RR CROSSING  aai:  box:74096 map:3033C7 tm:09:28:45  ED1102262",
         "SRC:SQ134",
         "CALL:AALLR - ANAPHYLAXIS",
         "PLACE:VERTIS DIRECT MKTG",
         "ADDR:4371 COUNTY LINE RD",
         "CITY:NEW BRITAIN TWP",
         "X:BRITAIN WOOD CI & RR CROSSING",
         "BOX:74096",
         "MAP:3033C7",
         "TIME:09:28:45",
         "ID:ED1102262");
 
     doTest("T15",
         "911: STA57  type:FEMS    adr:SLEEPY HOLLOW RD/SPINNERSTOWN RD ,45  aai:  box:75011  map:2693K9  tm:10:26:44  FD1104574    Run: E75",
         "SRC:STA57",
         "CALL:FEMS - FIRE ASSIST EMS (LOC)",
         "ADDR:SLEEPY HOLLOW RD & SPINNERSTOWN RD",
         "CITY:MILFORD TWP",
         "BOX:75011",
         "MAP:2693K9",
         "TIME:10:26:44",
         "ID:FD1104574",
         "UNIT:E75");
 
     doTest("T16",
         "911: STA57  stype:FTAI    adr:FRIER RD/MILL RD ,45  aai:  box:57002  map:  tm:13:51:38  FD1104463    Run: E57",
         "SRC:STA57",
         "CALL:FTAI - ACCIDENT W/FIRE & INJ (LOC)",
         "ADDR:FREIER RD & MILL RD",
         "CITY:MILFORD TWP",
         "BOX:57002",
         "TIME:13:51:38",
         "ID:FD1104463",
         "UNIT:E57");
 
     doTest("T17",
         "911: STA4  type:RAUTO   adr:PHILMONT AV/E STREET RD ,43  aai:PHILMONT SO STREET  box:04003  map:3261B8  tm:10:21:22  FD1104774    Run: R4 R6 E1",
         "SRC:STA4",
         "CALL:RAUTO - AUTO EXTRICATION (RBOX)",
         "ADDR:PHILMONT AV & E STREET RD",
         "MADDR:PHILMONT AVE & E STREET RD",
         "CITY:LOWER SOUTHAMPTON TWP",
         "INFO:PHILMONT SO STREET",
         "BOX:04003",
         "MAP:3261B8",
         "TIME:10:21:22",
         "ID:FD1104774",
         "UNIT:R4 R6 E1");
 
     doTest("T18",
         "911: STA53  type:RMAR    adr:LIONS PARK ,24 at 102 SAMUEL CLIFT ST ,24 btwn MILL ST & MARKET ST  aai:  box:51012  map:3375D5  tm:00:59:10  FD1105678 Run: R53 M53 M11",
         "SRC:STA53",
         "CALL:RMAR - MARINE RESCUE (MBOX)",
         "PLACE:LIONS PARK",
         "ADDR:102 SAMUEL CLIFT ST",
         "CITY:BRISTOL",
         "X:MILL ST & MARKET ST",
         "BOX:51012",
         "MAP:3375D5",
         "TIME:00:59:10",
         "ID:FD1105678",
         "UNIT:R53 M53 M11");
 
     doTest("T19",
         "911: STA53  type:WIREIN  adr:2218 TRENTON AV ,24  btwn BARRY PL & TAFT ST  aai:  box:53034  map:3375F2  tm:11:40:15  FD1104667    Run: L25 Q51 E50 E52",
         "SRC:STA53",
         "CALL:WIREIN - WIRES IN DWELLING (TAC)",
         "ADDR:2218 TRENTON AV",
         "MADDR:2218 TRENTON AVE",
         "CITY:BRISTOL",
         "X:BARRY PL & TAFT ST",
         "BOX:53034",
         "MAP:3375F2",
         "TIME:11:40:15",
         "ID:FD1104667",
         "UNIT:L25 Q51 E50 E52");
 
     doTest("T20",
         "911: STA53  stype:RAUTO   adr:NEW FALLS RD/PINE AV ,25  aai:  box:12044  map:3262G9  tm:21:30:34  FD1107837    Run: R13 R53 E82",
         "SRC:STA53",
         "CALL:RAUTO - AUTO EXTRICATION (RBOX)",
         "ADDR:NEW FALLS RD & PINE AV",
         "MADDR:NEW FALLS RD & PINE AVE",
         "CITY:BRISTOL TWP",
         "BOX:12044",
         "MAP:3262G9",
         "TIME:21:30:34",
         "ID:FD1107837",
         "UNIT:R13 R53 E82");
 
     doTest("T21",
         "911: STA19  type:WATER   adr:EIGHTEENFOURTEEN H ,28 at 50 S MAIN ST ,28 btwn W OAKLAND AV & TAYLOR AV  aai:  box:19022  map:2922J10  tm:02:29:07  FD11",
         "SRC:STA19",
         "CALL:WATER - WATER FLOW (LOC)",
         "PLACE:EIGHTEENFOURTEEN H",
         "ADDR:50 S MAIN ST",
         "CITY:DOYLESTOWN",
         "X:W OAKLAND AV & TAYLOR AV",
         "BOX:19022",
         "MAP:2922J10",
         "TIME:02:29:07",
         "ID:FD11");
 
     doTest("T22",
         "911: STA19  type:RVBLD   adr:LOBSTER CLAW ,57 at 800 N EASTON RD ,57 btwn BKHAM TWP LINE & CROSS KEYS DR  aai:  box:20001  map:2922J6  tm:16:03:22  FD",
         "SRC:STA19",
         "CALL:RVBLD - VEHICLE INTO A BUILDING (RBOX)",
         "PLACE:LOBSTER CLAW",
         "ADDR:800 N EASTON RD",
         "CITY:PLUMSTEAD TWP",
         "X:BKHAM TWP LINE & CROSS KEYS DR",
         "BOX:20001",
         "MAP:2922J6",
         "TIME:16:03:22",
         "ID:FD");
 
     doTest("T23",
         "911: STA58  type:FBLD    adr:121 PARK AV ,58  btwn FRONT ST & S 2ND ST  aai:QUAKERTOWN ASSOCIATES 215-901-8033  box:17001  map:2695H10  tm:17:44:38   FD1110887    Run: L57 SQ18 E36 R18 R58 ",
         "SRC:STA58",
         "CALL:FBLD - BUILDING FIRE (BOX)",
         "ADDR:121 PARK AV",
         "MADDR:121 PARK AVE",
         "CITY:QUAKERTOWN",
         "X:FRONT ST & S 2ND ST",
         "INFO:QUAKERTOWN ASSOCIATES 215-901-8033",
         "BOX:17001",
         "MAP:2695H10",
         "TIME:17:44:38",
         "ID:FD1110887",
         "UNIT:L57 SQ18 E36 R18 R58");
 
     doTest("T24",
         "911: STA14  type:EXBLD   adr:3501 BRISTOL OXF VALLEY RD #1107 ,25 -- AVALON COURT APT btwn LESLIE DR & BORDER  aai:  box:82015  map:3263A9  tm:22:06:28  FD1110945  Run: TW14 E12",
         "SRC:STA14",
         "CALL:EXBLD - EXTINGUISHED STRUCTURE (TAC)",
         "PLACE:AVALON COURT APT",
         "ADDR:3501 BRISTOL OXF VALLEY RD",
         "APT:1107",
         "CITY:BRISTOL TWP",
         "X:LESLIE DR & BORDER",
         "BOX:82015",
         "MAP:3263A9",
         "TIME:22:06:28",
         "ID:FD1110945",
         "UNIT:TW14 E12");
 
     doTest("T25",
         "911: SQ134  type:APOSXP  adr:307 W BUTLER AV ,48  btwn BRITTANY DR & SURREY RD  aai:NE SECTOR  box:74062  map:3033E6  tm:07:24:51  ED1126192",
         "SRC:SQ134",
         "CALL:APOSXP - POSSIBLE EXPIRATION",
         "ADDR:307 W BUTLER AV",
         "MADDR:307 W BUTLER AVE",
         "CITY:NEW BRITAIN TWP",
         "X:BRITTANY DR & SURREY RD",
         "INFO:NE SECTOR",
         "BOX:74062",
         "MAP:3033E6",
         "TIME:07:24:51",
         "ID:ED1126192");
 
     doTest("T26",
         "911: STA4  type:APOSXP  adr:3449 AZALEA AV ,22  btwn TRAILWOOD DR & VALLEY RD  aai:  box:04007  map:3261E8  tm:05:29:00  ED1126324  ",
         "SRC:STA4",
         "CALL:APOSXP - POSSIBLE EXPIRATION",
         "ADDR:3449 AZALEA AV",
         "MADDR:3449 AZALEA AVE",
         "CITY:BENSALEM TWP",
         "X:TRAILWOOD DR & VALLEY RD",
         "BOX:04007",
         "MAP:3261E8",
         "TIME:05:29:00",
         "ID:ED1126324");
 
     doTest("T27",
         "911: SQ134  type:AASSLT  adr:STADIUM BAR & GRIL ,36 at 1903 BETHLEHEM PK ,36 btwn SWARTLEY RD & MILL RD aai:  box:60063  map:3032F3  tm:02:33:13  ED1127778  ",
         "SRC:SQ134",
         "CALL:AASSLT - ASSAULT W/ TRAUMA",
         "PLACE:STADIUM BAR & GRIL",
         "ADDR:1903 BETHLEHEM PK",
         "MADDR:1903 BETHLEHEM PIKE",
         "CITY:HILLTOWN TWP",
         "X:SWARTLEY RD & MILL RD",
         "BOX:60063",
         "MAP:3032F3",
         "TIME:02:33:13",
         "ID:ED1127778");
 
     doTest("T28",
         "911: STA4  type:RDOM    adr:NESHA STATE PARK ,22 at 3401 STATE RD ,22 btwn DUNKSFERRY RD & WINKS LA  aai:NEXT TO POOL AREA AT PICNIC AREA  box:37023  map:3374C7  tm:12:52:17  FD1111780    Run: R16 SQ37 R4",
         "SRC:STA4",
         "CALL:RDOM - DOMESTIC RESCUE (RBOX)",
         "PLACE:NESHA STATE PARK",
         "ADDR:3401 STATE RD",
         "CITY:BENSALEM TWP",
         "X:DUNKSFERRY RD & WINKS LA",
         "INFO:NEXT TO POOL AREA AT PICNIC AREA",
         "BOX:37023",
         "MAP:3374C7",
         "TIME:12:52:17",
         "ID:FD1111780",
         "UNIT:R16 SQ37 R4");
 
     doTest("T29",
         "911: SQ134  type:ATRAN   adr:CENTER SQUARE TOWR #823-A ,28 at 555 BROAD ST #823-A ,28 btwn ATKINSON DR & VETE  aai:  box:19052  map:2922H10 tm:18:18:15  ED1127865  ",
         "SRC:SQ134",
         "CALL:ATRAN - ALS/UNSPECIFIED EMERGENCY",
         "PLACE:CENTER SQUARE TOWR #823-A",
         "ADDR:555 BROAD ST",
         "APT:823-A",
         "CITY:DOYLESTOWN",
         "X:ATKINSON DR & VETE",
         "BOX:19052",
         "MAP:2922H10",
         "TIME:18:18:15",
         "ID:ED1127865");
 
     doTest("T30",
         "STA19  type:FALRM   adr:LINDEN ELEM SCH ,28 at 480 LINDEN AV ,28 btwn EAST ST & ROHR DR  aai:  box:19093  map:2923A10  tm:20:27:42  FD1111756    ",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "PLACE:LINDEN ELEM SCH",
         "ADDR:480 LINDEN AV",
         "MADDR:480 LINDEN AVE",
         "CITY:DOYLESTOWN",
         "X:EAST ST & ROHR DR",
         "BOX:19093",
         "MAP:2923A10",
         "TIME:20:27:42",
         "ID:FD1111756");
 
     doTest("T31",
         "911: STA19  type:GALRM   adr:6 FLINT CI ,29  btwn WINDSOR WY & CUL DE SAC  aai:  box:79020  map:3034H5  tm:23:32:45  FD1112459    Run: E79    \n",
         "SRC:STA19",
         "CALL:GALRM - GENERAL ALARM",
         "ADDR:6 FLINT CI",
         "MADDR:6 FLINT CIR",
         "CITY:DOYLESTOWN TWP",
         "X:WINDSOR WY & CUL DE SAC",
         "BOX:79020",
         "MAP:3034H5",
         "TIME:23:32:45",
         "ID:FD1112459",
         "UNIT:E79");
 
     doTest("T32",
         "911: SQ122  type:ESPEC   adr:HARTS FIRE CO ,74 at 1195 YORK RD ,74 btwn OLD YORK RD & BRISTOL RD  aai:  box:93030  map:3147E4  tm:05:04:12  ED1131511",
         "SRC:SQ122",
         "CALL:ESPEC - STANDBY / SPECIAL ASSIGNMENT (EMS)",
         "PLACE:HARTS FIRE CO",
         "ADDR:1195 YORK RD",
         "CITY:WARMINSTER TWP",
         "X:OLD YORK RD & BRISTOL RD",
         "BOX:93030",
         "MAP:3147E4",
         "TIME:05:04:12",
         "ID:ED1131511");
 
     doTest("T33",
         "911: SQ134  type:BASSLT  adr:248 INVERNESS CI ,48  btwn BONNIE LARK CT & CUL DE SAC  aai:  box:74059  map:3033F7  tm:16:38:31  ED1133930  ",
         "SRC:SQ134",
         "CALL:BASSLT - ASSAULT - NON CRITICAL",
         "ADDR:248 INVERNESS CI",
         "MADDR:248 INVERNESS CIR",
         "CITY:NEW BRITAIN TWP",
         "X:BONNIE LARK CT & CUL DE SAC",
         "BOX:74059",
         "MAP:3033F7",
         "TIME:16:38:31",
         "ID:ED1133930");
 
     doTest("T34",
         "911: SQ134  type:FIRCAL  adr:4201 MILORDS LA ,26  btwn SIR ANDREW CT & SIR ANDREW CT  aai:NEXT DOOR  box:19018  map:2923B4  tm:19:34:11  ED1136021  ",
         "SRC:SQ134",
         "CALL:FIRCAL - FIRE CALL (EMS)",
         "ADDR:4201 MILORDS LA",
         "MADDR:4201 MILORDS LN",
         "CITY:BUCKINGHAM TWP",
         "X:SIR ANDREW CT & SIR ANDREW CT",
         "INFO:NEXT DOOR",
         "BOX:19018",
         "MAP:2923B4",
         "TIME:19:34:11",
         "ID:ED1136021");
 
     doTest("T35",
         "911: STA19  type:WIRES   adr:RT 202/N SHADY RETREAT RD ,47  aai:  box:19006  map:3034E1  tm:13:59:42  FD1116344    Run: E19    \n",
         "SRC:STA19",
         "CALL:WIRES - WIRES DOWN (LOC)",
         "ADDR:RT 202 & N SHADY RETREAT RD",
         "CITY:NEW BRITAIN",
         "BOX:19006",
         "MAP:3034E1",
         "TIME:13:59:42",
         "ID:FD1116344",
         "UNIT:E19");
 
     doTest("T36",
         "911: STA19  type:FDWL    adr:260 SANDY RIDGE RD ,29  btwn CAVALLO WY & IRON HILL RD  aai:  box:19006  map:3034C1  tm:12:08:50  FD1116685    Run: L79 E",
         "SRC:STA19",
         "CALL:FDWL - DWELLING FIRE (TAC)",
         "ADDR:260 SANDY RIDGE RD",
         "CITY:DOYLESTOWN TWP",
         "X:CAVALLO WY & IRON HILL RD",
         "BOX:19006",
         "MAP:3034C1",
         "TIME:12:08:50",
         "ID:FD1116685",
         "UNIT:L79 E");
 
     doTest("T37",
         "911: SQ134  type:ATAI    adr:BETHLEHEM PK/HILLTOWN PK ,36  aai:  box:60041  map:3032H4  tm:21:37:43  ED1139682  ",
         "SRC:SQ134",
         "CALL:ATAI - ALS TRAFFIC ACCIDENT INJURIES",
         "ADDR:BETHLEHEM PK & HILLTOWN PK",
         "MADDR:BETHLEHEM PIKE & HILLTOWN PIKE",
         "CITY:HILLTOWN TWP",
         "BOX:60041",
         "MAP:3032H4",
         "TIME:21:37:43",
         "ID:ED1139682");
 
     doTest("T38",
         "911: STA5  type:FALRM   adr:5263 MOUNTAIN TOP RD ,26  btwn HOLICONG RD & DEAD END  aai:MARTINI RESD #2157942445  box:05027  map:2924E9 tm:19:46:52  FD1118872    Run: E5",
         "SRC:STA5",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "ADDR:5263 MOUNTAIN TOP RD",
         "CITY:BUCKINGHAM TWP",
         "X:HOLICONG RD & DEAD END",
         "INFO:MARTINI RESD #2157942445",
         "BOX:05027",
         "MAP:2924E9",
         "TIME:19:46:52",
         "ID:FD1118872",
         "UNIT:E5");
 
     doTest("T39",
         "911: STA5  type:RAUTO   adr:HOLICONG RD/YORK RD ,26  aai:ON YORK NO HOLICONG  box:05021  map:2924B6  tm:12:43:08  FD1117995    Run: R5 E5",
         "SRC:STA5",
         "CALL:RAUTO - AUTO EXTRICATION (RBOX)",
         "ADDR:HOLICONG RD & YORK RD",
         "CITY:BUCKINGHAM TWP",
         "INFO:ON YORK NO HOLICONG",
         "BOX:05021",
         "MAP:2924B6",
         "TIME:12:43:08",
         "ID:FD1117995",
         "UNIT:R5 E5");
 
     doTest("T40",
         "911: STA5  type:FBLD    adr:4201 MILORDS LA ,26  btwn SIR ANDREW CT & SIR ANDREW CT  aai:NEXT DOOR  box:19018  map:2923B4  tm:19:34:01 FD1116371    Run: L5 E79 E20 TW24",
         "SRC:STA5",
         "CALL:FBLD - BUILDING FIRE (BOX)",
         "ADDR:4201 MILORDS LA",
         "MADDR:4201 MILORDS LN",
         "CITY:BUCKINGHAM TWP",
         "X:SIR ANDREW CT & SIR ANDREW CT",
         "INFO:NEXT DOOR",
         "BOX:19018",
         "MAP:2923B4",
         "TIME:19:34:01",
         "ID:FD1116371",
         "UNIT:L5 E79 E20 TW24");
 
     doTest("T41",
         "911: STA5  type:WIRES   adr:RT 413/UPPER MOUNTAIN RD ,26  aai: box:05025  map:2924B10  tm:17:35:03  FD1118467    Run: E15-1",
         "SRC:STA5",
         "CALL:WIRES - WIRES DOWN (LOC)",
         "ADDR:RT 413 & UPPER MOUNTAIN RD",
         "CITY:BUCKINGHAM TWP",
         "BOX:05025",
         "MAP:2924B10",
         "TIME:17:35:03",
         "ID:FD1118467",
         "UNIT:E15-1");
 
     doTest("T42",
         "911: STA5  type:WIRES   adr:CHURCH SCHOOL RD/MECHANICSVILLE RD ,26 aai:ON MECHANICSVILLE  box:15021  map:2923F9  tm:14:06:20  FD1118031 Run: E15",
         "SRC:STA5",
         "CALL:WIRES - WIRES DOWN (LOC)",
         "ADDR:CHURCH SCHOOL RD & MECHANICSVILLE RD",
         "CITY:BUCKINGHAM TWP",
         "INFO:ON MECHANICSVILLE",
         "BOX:15021",
         "MAP:2923F9",
         "TIME:14:06:20",
         "ID:FD1118031",
         "UNIT:E15");
 
     doTest("T43",
         "911: STA5  type:ELEVAT  adr:OUR LADY GUADAL ,26 at 5194 COLD SPRING CREAME RD ,26 btwn GREYSTONE DR & RT 413  aai:  box:15052  map:2923F2 tm:14:31:10  FD1116465    Run: E15",
         "SRC:STA5",
         "CALL:ELEVAT - STUCK ELEVATOR (LOC)",
         "PLACE:OUR LADY GUADAL",
         "ADDR:5194 COLD SPRING CREAMERY RD",
         "CITY:BUCKINGHAM TWP",
         "X:GREYSTONE DR & RT 413",
         "BOX:15052",
         "MAP:2923F2",
         "TIME:14:31:10",
         "ID:FD1116465",
         "UNIT:E15");
 
     doTest("T44",
         "911: STA5  type:FINV    adr:AQUETONG RD/TAYLOR LA ,65  aai:  box:05031  map:2924E1  tm:21:09:49  FD1117661    Run: E5",
         "SRC:STA5",
         "CALL:FINV - FIRE INVESTIGATION (LOC)",
         "ADDR:AQUETONG RD & TAYLOR LA",
         "MADDR:AQUETONG RD & TAYLOR LN",
         "CITY:SOLEBURY TWP",
         "BOX:05031",
         "MAP:2924E1",
         "TIME:21:09:49",
         "ID:FD1117661",
         "UNIT:E5");
 
     doTest("T45",
         "911: STA4  type:FSPEC   adr:2318 KAY AV ,22  btwn DEAD END & MASTER AV  aai:  box:04005  map:3261C10  tm:14:27:35  FD1200565    Run: E4-1",
         "SRC:STA4",
         "CALL:FSPEC - SPECIAL ASSIGNMENT",
         "ADDR:2318 KAY AV",
         "MADDR:2318 KAY AVE",
         "CITY:BENSALEM TWP",
         "X:DEAD END & MASTER AV",
         "BOX:04005",
         "MAP:3261C10",
         "TIME:14:27:35",
         "ID:FD1200565",
         "UNIT:E4-1");
 
     doTest("T46",
         "911: STA4  type:FALRM   adr:3338 RICHLIEU RD ,22 -- COUNTRY COMMONS btwn DEBRA DR & PA TP  aai:215 639 2800  box:65031  map:3261H9  tm:17:34:44 FD1200573    Run: E65 E",
         "SRC:STA4",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "PLACE:COUNTRY COMMONS",
         "ADDR:3338 RICHLIEU RD",
         "CITY:BENSALEM TWP",
         "X:DEBRA DR & PA TP",
         "INFO:215 639 2800",
         "BOX:65031",
         "MAP:3261H9",
         "TIME:17:34:44",
         "ID:FD1200573",
         "UNIT:E65 E");
 
     doTest("T47",
         "911: SQ134  type:COP     adr:90 ROBIN LA ,36  sbtwn MAPLE LEAF DR & COURT  saai:  box:27031  map:2920C5  tm:12:14:59  ED1202205  ",
         "SRC:SQ134",
         "CALL:COP - CARBON MONOXIDE POISONING",
         "ADDR:90 ROBIN LA",
         "MADDR:90 ROBIN LN",
         "CITY:HILLTOWN TWP",
         "X:MAPLE LEAF DR & COURT",
         "BOX:27031",
         "MAP:2920C5",
         "TIME:12:14:59",
         "ID:ED1202205");
 
     doTest("T48",
         "911: SQ134  type:BTRAN   adr:139 BIRCHWOOD DR ,27  btwn BIRCHWOOD DR & LYNWOOD DR  aai:  box:34011  map:3033E3  tm:19:20:35  ED1203702  ",
         "SRC:SQ134",
         "CALL:BTRAN - EMERGENCY TRANSPORTATION",
         "ADDR:139 BIRCHWOOD DR",
         "CITY:CHALFONT",
         "X:BIRCHWOOD DR & LYNWOOD DR",
         "BOX:34011",
         "MAP:3033E3",
         "TIME:19:20:35",
         "ID:ED1203702");
 
     doTest("T49",
         "911: SQ134  type:BBLED   adr:DOCK MEADOWS NH #14 ,36 at 2343 BETHLEHEM PK #14 ,36 btwn UNIONVILLE PK & CHURCH  aai:USE RT 309 ENTRANCE  box:60063  map:3032E3  tm:18:28:26  ED1203695  ",
         "SRC:SQ134",
         "CALL:BBLED - BLEEDING (NON TRAUMATIC)",
         "PLACE:DOCK MEADOWS NH #14",
         "ADDR:2343 BETHLEHEM PK",
         "MADDR:2343 BETHLEHEM PIKE",
         "APT:14",
         "CITY:HILLTOWN TWP",
         "X:UNIONVILLE PK & CHURCH",
         "INFO:USE RT 309 ENTRANCE",
         "BOX:60063",
         "MAP:3032E3",
         "TIME:18:28:26",
         "ID:ED1203695");
 
     doTest("T50",
         "911: STA19  type:FALRM   adr:GRUNDY HALL ,29 at 1290 ALMSHOUSE RD ,29 btwn TURK RD & RT 611  aai:  box:79057  map:3034J7  tm:03:30:43  FD1201380    Ru",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "PLACE:GRUNDY HALL",
         "ADDR:1290 ALMSHOUSE RD",
         "CITY:DOYLESTOWN TWP",
         "X:TURK RD & RT 611",
         "BOX:79057",
         "MAP:3034J7",
         "TIME:03:30:43",
         "ID:FD1201380");
 
     doTest("T51",
         "911: SQ134  type:ACVA    adr:NEW SEASONS ASSIST #110 ,48 at 800 MANOR DR #110 ,48 btwn E SCHOOL HOUSE RD & AN  aai:  box:74066  map:3033E8  tm:18:15:21  ED1203569  ",
         "SRC:SQ134",
         "CALL:ACVA - CVA/STROKE",
         "PLACE:NEW SEASONS ASSIST #110",
         "ADDR:800 MANOR DR",
         "APT:110",
         "CITY:NEW BRITAIN TWP",
         "X:E SCHOOL HOUSE RD & AN",
         "BOX:74066",
         "MAP:3033E8",
         "TIME:18:15:21",
         "ID:ED1203569");
 
     doTest("T52",
         "911: SQ134  type:AFAINT  adr:1500 HORIZON DR #112 ,48 -- ENDODONTIC SPECIAL btwn HORIZON CI & COUNTY LINE RD  aai:  box:74058  map:3033F8  tm:16:29:39  ED1203559  ",
         "SRC:SQ134",
         "CALL:AFAINT - SYNCOPAL EPISODE",
         "PLACE:ENDODONTIC SPECIAL",
         "ADDR:1500 HORIZON DR",
         "APT:112",
         "CITY:NEW BRITAIN TWP",
         "X:HORIZON CI & COUNTY LINE RD",
         "BOX:74058",
         "MAP:3033F8",
         "TIME:16:29:39",
         "ID:ED1203559");
 
     doTest("T53",
         "911: SQ134  type:ABLED   adr:DOCK MEADOWS NH #14 ,36 at 2343 BETHLEHEM PK #14 ,36 btwn UNIONVILLE PK & CHURCH  aai:  box:60063  map:3032E3  tm:09:32:32  ED1203769  ",
         "SRC:SQ134",
         "CALL:ABLED - ACUTE HEMORRHAGE",
         "PLACE:DOCK MEADOWS NH #14",
         "ADDR:2343 BETHLEHEM PK",
         "MADDR:2343 BETHLEHEM PIKE",
         "APT:14",
         "CITY:HILLTOWN TWP",
         "X:UNIONVILLE PK & CHURCH",
         "BOX:60063",
         "MAP:3032E3",
         "TIME:09:32:32",
         "ID:ED1203769");
 
     doTest("T54",
         "911: SQ134  type:BFALL   adr:WALMART STORE ,36 at 1515 BETHLEHEM PK ,36 btwn HILLTOWN PK & SWARTLEY RD  aai:W SECTOR  box:60062  map:3032H4  tm:14:32:51  ED1203813  ",
         "SRC:SQ134",
         "CALL:BFALL - FALL VICTIM - NON CRITICAL",
         "PLACE:WALMART STORE",
         "ADDR:1515 BETHLEHEM PK",
         "MADDR:1515 BETHLEHEM PIKE",
         "CITY:HILLTOWN TWP",
         "X:HILLTOWN PK & SWARTLEY RD",
         "INFO:W SECTOR",
         "BOX:60062",
         "MAP:3032H4",
         "TIME:14:32:51",
         "ID:ED1203813");
 
     doTest("T55",
         "911: SQ134  type:AABDO   adr:54 CHEROKEE RD ,47  btwn LENAPE DR & PUEBLO RD  aai:  box:34033  map:3033J2  tm:23:45:25  ED1203857  ",
         "SRC:SQ134",
         "CALL:AABDO - ACUTE ABDOMEN",
         "BOX:34033",
         "ADDR:54 CHEROKEE RD",
         "CITY:NEW BRITAIN",
         "X:LENAPE DR & PUEBLO RD",
         "MAP:3033J2",
         "TIME:23:45:25",
         "ID:ED1203857");
 
     doTest("T56",
         "911: STA57  type:BTAI    adr:PORTZER RD/RT 663 ,45  aai:ON PORTZER,,PAST ANIMAL HOSP  box:57007  map:2695A9  tm:18:05:19  ED1203958",
         "SRC:STA57",
         "CALL:BTAI - BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
         "BOX:57007",
         "ADDR:PORTZER RD & RT 663",
         "CITY:MILFORD TWP",
         "INFO:ON PORTZER,,PAST ANIMAL HOSP",
         "MAP:2695A9",
         "TIME:18:05:19",
         "ID:ED1203958");
 
     doTest("T57",
         "911: STA57  type:STDBY   adr:PORTZER RD/RT 663 ,45  aai:  box:57007  map:2695A9  tm:18:05:25  FD1201500    Run: E57",
         "SRC:STA57",
         "CALL:STDBY - FIRE STANDBY",
         "BOX:57007",
         "ADDR:PORTZER RD & RT 663",
         "CITY:MILFORD TWP",
         "MAP:2695A9",
         "TIME:18:05:25",
         "ID:FD1201500",
         "UNIT:E57");
 
     doTest("T58",
         "911: STA57  type:FPOL    adr:OLD BETHLEHEM PK/PORTZER RD ,45  aai:  box:57003  map:2695A8  tm:18:16:46  FD1201502\\4sRun: FP57 FP75",
         "SRC:STA57",
         "CALL:FPOL - FIRE POLICE REQUEST",
         "BOX:57003",
         "ADDR:OLD BETHLEHEM PK & PORTZER RD",
         "MADDR:OLD BETHLEHEM PIKE & PORTZER RD",
         "CITY:MILFORD TWP",
         "MAP:2695A8",
         "TIME:18:16:46",
         "ID:FD1201502\\4sRun: FP57 FP75");
 
     doTest("T59",
         "911: STA57  type:BTAI    adr:PORTZER RD/RT 663 ,45  aai:ON PORTZER,,PAST ANIMAL HOSP  box:57007  map:2695A9  tm:18:05:19  ED1203958",
         "SRC:STA57",
         "CALL:BTAI - BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
         "BOX:57007",
         "ADDR:PORTZER RD & RT 663",
         "CITY:MILFORD TWP",
         "INFO:ON PORTZER,,PAST ANIMAL HOSP",
         "MAP:2695A9",
         "TIME:18:05:19",
         "ID:ED1203958");
   }
   
   @Test
   public void testParserPatrick() {
 
     doTest("T1",
         "(#6331  1/1) FDWL\n" +
         "adr:2008 BRISTOL RD ,75  btwn SHADOW CREEK LA & GUINEA LA\n" +
         "box:29045\n" +
         "tm:22:34:01 FD1200729\n" +
         "Run: E29 E78 L29 E93",
 
         "CALL:FDWL - DWELLING FIRE (TAC)",
         "ADDR:2008 BRISTOL RD",
         "CITY:WARRINGTON TWP",
         "X:SHADOW CREEK LA & GUINEA LA",
         "BOX:29045",
         "TIME:22:34:01",
         "ID:FD1200729",
         "UNIT:E29 E78 L29 E93");
 
     doTest("T2",
         "(#6779  1/1) FEMS\nadr:1837 JERICHO DR ,75\nbox:29058\ntm:01:33:42 FD1201317\nRun: E29",
         "CALL:FEMS - FIRE ASSIST EMS (LOC)",
         "ADDR:1837 JERICHO DR",
         "CITY:WARRINGTON TWP",
         "BOX:29058",
         "TIME:01:33:42",
         "ID:FD1201317",
         "UNIT:E29");
 
     doTest("T3",
         "(#6613  1/1) WIREIN\nadr:2401 GREENSWARD RD N ,75\nbox:29501\ntm:11:51:55 FD1201251\n",
         "CALL:WIREIN - WIRES IN DWELLING (TAC)",
         "ADDR:2401 GREENSWARD RD N",
         "CITY:WARRINGTON TWP",
         "BOX:29501",
         "TIME:11:51:55",
         "ID:FD1201251");
 
     doTest("T4",
         "(#6393  1/1) FUMEIN\nadr:2009 CEDAR DR ,76  btwn WINDSOR LA & APPLE CT\nbox:66001\ntm:13:50:55 FD1200871\nRun: TW66 E66 E29 E93 R29",
         "CALL:FUMEIN - FUMES INSIDE STRUCTURE (TAC)",
         "BOX:66001",
         "ADDR:2009 CEDAR DR",
         "CITY:WARWICK TWP",
         "X:WINDSOR LA & APPLE CT",
         "TIME:13:50:55",
         "ID:FD1200871",
         "UNIT:TW66 E66 E29 E93 R29");
 
     doTest("T5",
         "(#6417  1/1) BFALL\nadr:3018 STREET RD ,75  btwn HONORA ST & FOLLY RD\nbox:78013\ntm:15:33:54 ED1202384",
         "CALL:BFALL - FALL VICTIM - NON CRITICAL",
         "BOX:78013",
         "ADDR:3018 STREET RD",
         "CITY:WARRINGTON TWP",
         "X:HONORA ST & FOLLY RD",
         "TIME:15:33:54",
         "ID:ED1202384");
 
     doTest("T6",
         "(#6499  1/1) FDWL\nadr:2221 SARAHS LA ,75  btwn MARIE CI & CUL DE SAC\nbox:29059\ntm:22:13:24 FD1201135\nRun: E29 E78 L29 E66",
         "CALL:FDWL - DWELLING FIRE (TAC)",
         "BOX:29059",
         "ADDR:2221 SARAHS LA",
         "MADDR:2221 SARAHS LN",
         "CITY:WARRINGTON TWP",
         "X:MARIE CI & CUL DE SAC",
         "TIME:22:13:24",
         "ID:FD1201135",
         "UNIT:E29 E78 L29 E66");
 
     doTest("T7",
         "(#6613  1/1) WIREIN\nadr:2401 GREENSWARD RD N ,75\nbox:29501\ntm:11:51:55 FD1201251\nRun: E29 E78 L90",
         "CALL:WIREIN - WIRES IN DWELLING (TAC)",
         "BOX:29501",
         "ADDR:2401 GREENSWARD RD N",
         "CITY:WARRINGTON TWP",
         "TIME:11:51:55",
         "ID:FD1201251",
         "UNIT:E29 E78 L90");
 
     doTest("T8",
         "(#7645  1/1) BTAI\nadr:W STREET RD/YORK RD ,74\nbox:90041 map:3147E7\ntm:08:14:08 ED1204133",
         "CALL:BTAI - BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
         "BOX:90041",
         "ADDR:W STREET RD & YORK RD",
         "CITY:WARMINSTER TWP",
         "MAP:3147E7",
         "TIME:08:14:08",
         "ID:ED1204133");
 
     doTest("T9",
         "(#7651  1/2) BFALL\nadr:2010 YORK RD ,76 -- YORK TOWN MANOR\nbtwn:MEYER WY & PARK DR\naai:UNIT 119\nbox:66050 map:3035F10\ntm:09:07:36",
         "CALL:BFALL - FALL VICTIM - NON CRITICAL",
         "BOX:66050",
         "PLACE:YORK TOWN MANOR",
         "ADDR:2010 YORK RD",
         "CITY:WARWICK TWP",
         "X:MEYER WY & PARK DR",
         "INFO:UNIT 119",
         "MAP:3035F10",
         "TIME:09:07:36");
 
   }
   
   @Test
   public void testRyanPankoe() {
 
     doTest("T1",
         "Subject:1/2\nFAPT\nadr:CREEK VILL APT #F18 ,71 at 160 FALLSINGTON TULLYT RD #F18 ,71\nbox:33022\ntm:18:57:03 FD1201370\nRun: E33 L3",
         "CALL:FAPT - APARTMENT FIRE (BOX)",
         "BOX:33022",
         "PLACE:CREEK VILL APT #F18",
         "ADDR:160 FALLSINGTON TULLYTOWN RD",
         "APT:F18",
         "CITY:TULLYTOWN",
         "TIME:18:57:03",
         "ID:FD1201370",
         "UNIT:E33 L3");
 
     doTest("T2",
         "Subject:1/1\nFSPEC\nadr:1812 FARRAGUT AV ,24\nbox:53039\ntm:12:44:12 FD1201444\nRun: L25",
         "CALL:FSPEC - SPECIAL ASSIGNMENT",
         "BOX:53039",
         "ADDR:1812 FARRAGUT AV",
         "MADDR:1812 FARRAGUT AVE",
         "CITY:BRISTOL",
         "TIME:12:44:12",
         "ID:FD1201444",
         "UNIT:L25");
 
     doTest("T3",
         "Subject:1/2\nFAPT\nadr:GRUNDY TOWERS #1010 ,24 at 205 POND ST #1010 ,24\nbox:50092\ntm:18:46:52 FD1201459\nRun: E50 E52 E53 Q51 L25",
         "CALL:FAPT - APARTMENT FIRE (BOX)",
         "BOX:50092",
         "PLACE:GRUNDY TOWERS #1010",
         "ADDR:205 POND ST",
         "APT:1010",
         "CITY:BRISTOL",
         "TIME:18:46:52",
         "ID:FD1201459",
         "UNIT:E50 E52 E53 Q51 L25");
 
     doTest("T4",
         "Subject:1/1\nFBLD\nadr:301 WOOD ST ,24 -- LBUCK SENIOR CITIZ\nbox:51015\ntm:09:59:54 FD1201481\nRun: E50 E52 E53 Q51 TW14 E11 R10",
         "CALL:FBLD - BUILDING FIRE (BOX)",
         "BOX:51015",
         "PLACE:LBUCK SENIOR CITIZ",
         "ADDR:301 WOOD ST",
         "CITY:BRISTOL",
         "TIME:09:59:54",
         "ID:FD1201481",
         "UNIT:E50 E52 E53 Q51 TW14 E11 R10");
 
     doTest("T5",
         "Subject:1/2\nFALRM\nadr:ST ANDREWS EPISCOP ,79 at 55 W AFTON AV ,79\nbtwn:PENN VALLEY DR & BREECE DR\naai:PARRISH HOUSE\nbox:00071",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:00071",
         "PLACE:ST ANDREWS EPISCOP",
         "ADDR:55 W AFTON AV",
         "MADDR:55 W AFTON AVE",
         "CITY:YARDLEY",
         "X:PENN VALLEY DR & BREECE DR",
         "INFO:PARRISH HOUSE");
 
     doTest("T6",
         "FAPT\n" +
         "adr:1577 NESHAMINY VALLEY DR ,22\n" +
         "btwn:SHEWELL RD & COUNTRY LIGHT CHAL E\n" +
         "box:88015\n" +
         "tm:06:33:52 FD1201865  Run: E8=8 L65 L8 E84 E7 E4=",
 
         "CALL:FAPT - APARTMENT FIRE (BOX)",
         "BOX:88015",
         "ADDR:1577 NESHAMINY VALLEY DR",
         "CITY:BENSALEM TWP",
         "X:SHEWELL RD & COUNTRY LIGHT CHAL E",
         "TIME:06:33:52",
         "ID:FD1201865",
         "UNIT:E88 L65 L8 E84 E7 E4");
 
     doTest("T7",
         "Subject:1/1\nATRAN \nadr:101 BARBERRY CT ,48 \nbtwn:CUL DE SAC & E FAIRWOOD DR \nbox:34030 map:3033H1 \ntm:11:11:13 ED1205059",
         "CALL:ATRAN - ALS/UNSPECIFIED EMERGENCY",
         "BOX:34030",
         "ADDR:101 BARBERRY CT",
         "CITY:NEW BRITAIN TWP",
         "X:CUL DE SAC & E FAIRWOOD DR",
         "MAP:3033H1",
         "TIME:11:11:13",
         "ID:ED1205059");
 
     doTest("T8",
         "Subject:1/1\n" +
         "SQ125 SQ134:ACVA\n" +
         "adr:216 UNION ST ,28\n" +
         "btwn:HARVEY AV & N HAMILTON ST\n" +
         "aai:\"ALLEN GREER\" RESD 215 348 1001\n" +
         "box:1901ap:2922H10\n" +
         "tm:06:34:10 ED1205674",
 
         "CALL:ACVA - CVA/STROKE",
         "BOX:1901ap:2922H10",
         "ADDR:216 UNION ST",
         "CITY:DOYLESTOWN",
         "X:HARVEY AV & N HAMILTON ST",
         "INFO:\"ALLEN GREER\" RESD 215 348 1001",
         "TIME:06:34:10",
         "ID:ED1205674",
         "UNIT:SQ125 SQ134");
 
     doTest("T9",
         "Subject:1/1\nSQ134:AFALL\nadr:10 DUBLIN RD ,36\nbtwn:HILLTOWN PK & PINESIDE DR\nbox:23017 map:2921D4\ntm:18:47:44 ED1205765",
         "CALL:AFALL - FALL VICTIM - CRITICAL",
         "BOX:23017",
         "ADDR:10 DUBLIN RD",
         "CITY:HILLTOWN TWP",
         "X:HILLTOWN PK & PINESIDE DR",
         "MAP:2921D4",
         "TIME:18:47:44",
         "ID:ED1205765",
         "UNIT:SQ134");
 
     doTest("T10",
         "Subject:1/1\nSQ134:AFAINT \nadr:102 ASPEN CT ,48 \nbtwn:E FAIRWOOD DR & CUL DE SAC \nbox:34030 map:3033H1 \ntm:05:28:39 ED1205933",
         "CALL:AFAINT - SYNCOPAL EPISODE",
         "BOX:34030",
         "ADDR:102 ASPEN CT",
         "CITY:NEW BRITAIN TWP",
         "X:E FAIRWOOD DR & CUL DE SAC",
         "MAP:3033H1",
         "TIME:05:28:39",
         "ID:ED1205933",
         "UNIT:SQ134");
 
     doTest("T11",
         "Subject:1/1\nSQ134:ATRAN\nadr:209 OVERLOOK DR ,48\nbtwn:STONY HILL CT & RIDGE CT\nbox:74052 map:3033D5\ntm:00:21:23 ED1205914",
         "CALL:ATRAN - ALS/UNSPECIFIED EMERGENCY",
         "BOX:74052",
         "ADDR:209 OVERLOOK DR",
         "CITY:NEW BRITAIN TWP",
         "X:STONY HILL CT & RIDGE CT",
         "MAP:3033D5",
         "TIME:00:21:23",
         "ID:ED1205914",
         "UNIT:SQ134");
 
     doTest("T12",
         "Subject:1/1\nSQ134:AFAINT\nadr:102 ASPEN CT ,48\nbtwn:E FAIRWOOD DR & CUL DE SAC\nbox:34030 map:3033H1\ntm:05:28:39 ED1205933",
         "CALL:AFAINT - SYNCOPAL EPISODE",
         "BOX:34030",
         "ADDR:102 ASPEN CT",
         "CITY:NEW BRITAIN TWP",
         "X:E FAIRWOOD DR & CUL DE SAC",
         "MAP:3033H1",
         "TIME:05:28:39",
         "ID:ED1205933",
         "UNIT:SQ134");
 
     doTest("T13",
         "STA19:WIREIN adr:131 WESTBURY CT ,29 btwn:AVENUE A & CUL DE SAC box:19042 tm:10:45:39 FD1202161 Run: L79 E19 E19-1 E79 Sent by mss911 Bucks to STA19,",
         "SRC:STA19",
         "CALL:WIREIN - WIRES IN DWELLING (TAC)",
         "BOX:19042",
         "ADDR:131 WESTBURY CT",
         "CITY:DOYLESTOWN TWP",
         "X:AVENUE A & CUL DE SAC",
         "TIME:10:45:39",
         "ID:FD1202161",
         "UNIT:L79 E19 E19-1 E79");
 
     doTest("T14",
         "SQ134:AUNR\n" +
         "adr:800 MANOR DR #325 ,48 -- NEW SEASONS ASSIST\n" +
         "btwn:E SCHOOL HOUSE RD & ANDRE DR\n" +
         "box:74066 map:3033E8:12:14:32 ED1205978",
 
         "CALL:AUNR - UNRESPONSIVE SUBJECT",
         "BOX:74066",
         "PLACE:NEW SEASONS ASSIST",
         "ADDR:800 MANOR DR",
         "APT:325",
         "CITY:NEW BRITAIN TWP",
         "X:E SCHOOL HOUSE RD & ANDRE DR",
         "MAP:3033E8:12:14:32 ED1205978",
         "UNIT:SQ134");
 
     doTest("T15",
         "Subject:1/1\nSQ134 STA60:ACHESP\nadr:31 CHURCH RD ,36\nbtwn:BETHLEHEM PK & PARK RD\nbox:60011\ntm:14:03:32 ED1205994",
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "BOX:60011",
         "ADDR:31 CHURCH RD",
         "CITY:HILLTOWN TWP",
         "X:BETHLEHEM PK & PARK RD",
         "TIME:14:03:32",
         "ID:ED1205994",
         "UNIT:SQ134 STA60");
 
     doTest("T16",
         "Subject:1/1\nSQ134:ASEIZ\nadr:MANOR CARE HEALTH SERVICES#228,MONT TWP\naai:640 BETHLEHEM PIKE\nbox: map:\ntm:19:53:41 ED1206032",
         "CALL:ASEIZ - SEIZURES - ACTIVE",
         "ADDR:MANOR CARE HEALTH SERVICES",
         "APT:228",
         "CITY:MONT TWP",
         "INFO:640 BETHLEHEM PIKE",
         "TIME:19:53:41",
         "ID:ED1206032",
         "UNIT:SQ134");
 
     doTest("T17",
         "Subject:1/2\nSQ134:FIRCAL\nadr:SALVAGE DIRECT ,47 at 77 BRISTOL RD ,47\nbtwn:W BUTLER AV & UNAMI TL\nbox:34025 map:3033J4\ntm:22:0",
         "CALL:FIRCAL - FIRE CALL (EMS)",
         "BOX:34025",
         "PLACE:SALVAGE DIRECT",
         "ADDR:77 BRISTOL RD",
         "CITY:NEW BRITAIN",
         "X:W BUTLER AV & UNAMI TL",
         "MAP:3033J4",
         "TIME:22:0",
         "UNIT:SQ134");
 
     doTest("T18",
         "STA19:FSTORE adr:NATIONAL PENN INSU ,28 at 169 BROAD ST ,28 btwn:N MAIN ST & UNION ST box:19012 tm:13:22:16 FD1202212 Run: L79 E19 E19-1 E79 Sent by m",
         "SRC:STA19",
         "CALL:FSTORE - SMALL STORE FIRE (TAC)",
         "BOX:19012",
         "PLACE:NATIONAL PENN INSU",
         "ADDR:169 BROAD ST",
         "CITY:DOYLESTOWN",
         "X:N MAIN ST & UNION ST",
         "TIME:13:22:16",
         "ID:FD1202212",
         "UNIT:L79 E19 E19-1 E79 Sent by m");
 
     doTest("T19",
         "STA19:FALRM adr:GOLDEN LIVING ,28 at 432 MAPLE AV ,28 btwn:COTTAGE ST & EAST ST box:19055 tm:13:27:51 FD1202214 Run: E15 Sent by mss911 Bucks to STA19",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:19055",
         "PLACE:GOLDEN LIVING",
         "ADDR:432 MAPLE AV",
         "MADDR:432 MAPLE AVE",
         "CITY:DOYLESTOWN",
         "X:COTTAGE ST & EAST ST",
         "TIME:13:27:51",
         "ID:FD1202214",
         "UNIT:E15");
 
     doTest("T20",
         "STA19:FALRM adr:GRUNDY HALL ,29 at 1290 ALMSHOUSE RD ,29 btwn:TURK RD & RT 611 box:79057 tm:12:13:33 FD1202423 Run: E79 Sent by mss911 Bucks to STA19,",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:79057",
         "PLACE:GRUNDY HALL",
         "ADDR:1290 ALMSHOUSE RD",
         "CITY:DOYLESTOWN TWP",
         "X:TURK RD & RT 611",
         "TIME:12:13:33",
         "ID:FD1202423",
         "UNIT:E79");
 
     doTest("T21",
         "STA19:FALRM adr:1290 ALMSHOUSE RD ,29 - GRUNDY HALL btwn:TURK RD & RT 611 aai:2153434117 box:79057 tm:18:47:59 FD1202516 Run: E79 Sent by mss911 Bucks",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:79057",
         "ADDR:1290 ALMSHOUSE RD",
         "CITY:DOYLESTOWN TWP",
         "PLACE:GRUNDY HALL",
         "X:TURK RD & RT 611",
         "INFO:2153434117",
         "TIME:18:47:59",
         "ID:FD1202516",
         "UNIT:E79");
 
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "FRM:Bucks RSAN\n" +
         "SUBJ:1/1\n" +
         "MSG:WIREIN\n" +
         "adr:1871 WHITE BRIAR RD ,73\n" +
         "box:02047\n" +
         "tm:09:52:53 FD1201327\n" +
         "Run: E2 L1 E4 R6=",
 
         "CALL:WIREIN - WIRES IN DWELLING (TAC)",
         "ADDR:1871 WHITEBRIAR RD",
         "CITY:UPPER SOUTHAMPTON TWP",
         "BOX:02047",
         "TIME:09:52:53",
         "ID:FD1201327",
         "UNIT:E2 L1 E4 R6");
 
     doTest("T2",
         "FRM:Bucks RSAN\n" +
         "SUBJ:1/1\n" +
         "MSG:AUNR\n" +
         "adr:BELMONT HILLS ELEM ,22 at 5000 NESHAMINY BL ,22\n" +
         "box:84048\n" +
         "tm:09:05:59 ED1203237=",
 
         "CALL:AUNR - UNRESPONSIVE SUBJECT",
         "PLACE:BELMONT HILLS ELEM",
         "ADDR:5000 NESHAMINY BL",
         "CITY:BENSALEM TWP",
         "BOX:84048",
         "TIME:09:05:59",
         "ID:ED1203237");
 
     doTest("T3",
         "FRM:Bucks RSAN\n" +
         "SUBJ:1/1\n" +
         "MSG:FALRM\n" +
         "adr:ANDYS DINER ,22 at 2224 LINCOLN HY ,22\n" +
         "aai:215-638-1444\n" +
         "box:04032\n" +
         "tm:13:07:04 FD1201397\n" +
         "Run: E4-1=",
 
         "CALL:FALRM - FIRE ALARM (LOC)",
         "PLACE:ANDYS DINER",
         "ADDR:2224 LINCOLN HY",
         "MADDR:2224 LINCOLN HWY",
         "CITY:BENSALEM TWP",
         "INFO:215-638-1444",
         "BOX:04032",
         "TIME:13:07:04",
         "ID:FD1201397",
         "UNIT:E4-1");
 
     doTest("T1",
         "FRM:Bucks RSAN\n" +
         "SUBJ:1/1\n" +
         "MSG:STA1 STA65 STA2 STA4 STA3:FAPT\n" +
         "adr:400 E STREET RD ,43 -- CROFTWOOD APT\n" +
         "btwn:CENTRAL AV & STERNER MILL RD\n" +
         "aai:BUILDI=NG 4\n" +
         "box:01050\n" +
         "tm:10:01:14 FD1202073  Run: L65 L2 MSS11 E4 Q73 R4=",
 
         "SRC:STA1 STA65 STA2 STA4 STA3",
         "CALL:FAPT - APARTMENT FIRE (BOX)",
         "BOX:01050",
         "PLACE:CROFTWOOD APT",
         "ADDR:400 E STREET RD",
         "CITY:LOWER SOUTHAMPTON TWP",
         "X:CENTRAL AV & STERNER MILL RD",
         "INFO:BUILDING 4",
         "TIME:10:01:14",
         "ID:FD1202073",
         "UNIT:L65 L2 MSS11 E4 Q73 R4");
 
   }
   
   @Test
   public void testParser4() {
 
     doTest("T1",
         "RAUTO  Box:15023\nAdr: CHURCH RD/RT 413 ,26\nbtwn:RT 413 3/4 OF A MILE FROM CHURCH\n01/21/12  12:43:51\nFD1201070\nRun: R5 E15",
         "CALL:RAUTO - AUTO EXTRICATION (RBOX)",
         "BOX:15023",
         "ADDR:CHURCH RD & RT 413",
         "CITY:BUCKINGHAM TWP",
         "X:RT 413 3/4 OF A MILE FROM CHURCH",
         "DATE:01/21/12",
         "TIME:12:43:51",
         "ID:FD1201070",
         "UNIT:R5 E15");
 
     doTest("T2",
         "COALRM Box:05035 adr: 3425 AQUETONG RD ,65 btwn:MECHANICSVILLE RD & SAW MILL RD 01/28/12  15:44:26 FD1201409   Run: C5",
         "CALL:COALRM - CARBON MONOXIDE ALARM (LOC)",
         "BOX:05035",
         "ADDR:3425 AQUETONG RD",
         "CITY:SOLEBURY TWP",
         "X:MECHANICSVILLE RD & SAW MILL RD",
         "DATE:01/28/12",
         "TIME:15:44:26",
         "ID:FD1201409",
         "UNIT:C5");
 
     doTest("T3",
         "FALRM  Box:05031 adr: 5960 HONEY HOLLOW RD ,65 btwn:STREET RD & DEWEES RD RESID-MINNAUGH 01/29/12  20:16:02 FD1201463   Run: E5",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:05031",
         "ADDR:5960 HONEY HOLLOW RD",
         "CITY:SOLEBURY TWP",
         "X:STREET RD & DEWEES RD RESID-MINNAUGH",
         "DATE:01/29/12",
         "TIME:20:16:02",
         "ID:FD1201463",
         "UNIT:E5");
 
   }
   
   @Test
   public void testParser5() {
 
     doTest("T1",
         "Subject:1/1\nSQ134 STA60:ATAI\nadr:1235 S TOWNSHIP LINE RD ,36\nbtwn:CHALFONT RD & LIMEKILN PK\nbox:61003\ntm:08:57:03 ED1205458",
         "CALL:ATAI - ALS TRAFFIC ACCIDENT INJURIES",
         "BOX:61003",
         "ADDR:1235 S TOWNSHIP LINE RD",
         "CITY:HILLTOWN TWP",
         "X:CHALFONT RD & LIMEKILN PK",
         "TIME:08:57:03",
         "ID:ED1205458",
         "UNIT:SQ134 STA60");
 
     doTest("T2",
         "Subject:1/1\nSQ134:AABDO\nadr:10 DUBLIN RD ,36\nbtwn:HILLTOWN PK & PINESIDE DR\nbox:23017 map:2921D4\ntm:13:27:42 ED1205485",
         "CALL:AABDO - ACUTE ABDOMEN",
         "BOX:23017",
         "ADDR:10 DUBLIN RD",
         "CITY:HILLTOWN TWP",
         "X:HILLTOWN PK & PINESIDE DR",
         "MAP:2921D4",
         "TIME:13:27:42",
         "ID:ED1205485",
         "UNIT:SQ134");
 
     doTest("T3",
         "Subject:1/1\nSQ134 SQ124:AABDO\nadr:10 DUBLIN RD ,36\nbtwn:HILLTOWN PK & PINESIDE DR\nbox:23017 map:2921D4\ntm:13:27:42 ED1205485",
         "CALL:AABDO - ACUTE ABDOMEN",
         "BOX:23017",
         "ADDR:10 DUBLIN RD",
         "CITY:HILLTOWN TWP",
         "X:HILLTOWN PK & PINESIDE DR",
         "MAP:2921D4",
         "TIME:13:27:42",
         "ID:ED1205485",
         "UNIT:SQ134 SQ124");
 
     doTest("T4",
         "911: STA19  type:RAUTO   adr:S MAIN ST/S RT 611 ,29  aai:  box:79025  map:3034K3  tm:23:13:16  FD1201989    Run: R19 E79    \n",
         "SRC:STA19",
         "CALL:RAUTO - AUTO EXTRICATION (RBOX)",
         "BOX:79025",
         "ADDR:S MAIN ST & S RT 611",
         "CITY:DOYLESTOWN TWP",
         "MAP:3034K3",
         "TIME:23:13:16",
         "ID:FD1201989",
         "UNIT:R19 E79");
 
     doTest("T5",
         "Subject:1/1\nSQ134:ASEIZ\nadr:517 N MAIN ST ,27\nbtwn:W HILLCREST AV & PEACE VALLEY RD\nbox:34008 map:3033E2\ntm:20:10:13 ED1205526",
         "CALL:ASEIZ - SEIZURES - ACTIVE",
         "BOX:34008",
         "ADDR:517 N MAIN ST",
         "CITY:CHALFONT",
         "X:W HILLCREST AV & PEACE VALLEY RD",
         "MAP:3033E2",
         "TIME:20:10:13",
         "ID:ED1205526",
         "UNIT:SQ134");
 
     doTest("T6",
         "911: STA19  type:FALRM   adr:COURT HOUSE MTR IN #137 ,28 at 625 N MAIN ST #137 ,28 btwn FONTHILL DR & CHAPMAN  aai:  box:19014  map:2922J8  tm:19:44:2",
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:19014",
         "PLACE:COURT HOUSE MTR IN #137",
         "ADDR:625 N MAIN ST",
         "APT:137",
         "CITY:DOYLESTOWN",
         "X:FONTHILL DR & CHAPMAN",
         "MAP:2922J8",
         "TIME:19:44:2");
 
     doTest("T7",
         "Subject:1/2\nSQ134 SQ125:ACHESP\nadr:1080 PATRICK PL ,27\nbtwn:LINDENFIELD PY & LINDENFIELD PY\nbox:34028 map:3033G3\ntm:12:27:07 E",
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "BOX:34028",
         "ADDR:1080 PATRICK PL",
         "CITY:CHALFONT",
         "X:LINDENFIELD PY & LINDENFIELD PY",
         "MAP:3033G3",
         "TIME:12:27:07",
         "ID:E",
         "UNIT:SQ134 SQ125");
      
   }
   
   @Test
   public void testJonDinola() {
 
     doTest("T1",
         "(#9823  1/1) STA0:FEMS\nadr:4008 WALTHAM CT ,42\nbtwn:COVINGTON RD & CUL DE SAC\nbox:80012\ntm:19:07:17 FD1202015  Run: UT80",
         "SRC:STA0",
         "CALL:FEMS - FIRE ASSIST EMS (LOC)",
         "BOX:80012",
         "ADDR:4008 WALTHAM CT",
         "CITY:LOWER MAKEFIELD TWP",
         "X:COVINGTON RD & CUL DE SAC",
         "TIME:19:07:17",
         "ID:FD1202015",
         "UNIT:UT80");
 
     doTest("T2",
         "(#16847  1/1) STA0:FCHIMN\nadr:2331 LAKEVIEW DR ,42\nbtwn:WEINMANN WY & OXFORD RD\nbox:00103\ntm:07:13:58 FD1202958  Run: L0 R0",
         "SRC:STA0",
         "CALL:FCHIMN - CHIMNEY FIRE (TAC)",
         "BOX:00103",
         "ADDR:2331 LAKEVIEW DR",
         "CITY:LOWER MAKEFIELD TWP",
         "X:WEINMANN WY & OXFORD RD",
         "TIME:07:13:58",
         "ID:FD1202958",
         "UNIT:L0 R0");
 
   }
   
   @Test
   public void testParser7() {
 
     doTest("T1",
         "(#10067  1/2) STA57:FPOL\n" +
         "adr:1940 GERYVILLE PK ,45\n" +
         "btwn:SLEEPY HOLLOW RD & TITLOW RD\n" +
         "box:75001\n" +
         "tm:18:15:10 FD1202048  Run: FP75 FP",
 
         "SRC:STA57",
         "CALL:FPOL - FIRE POLICE REQUEST",
         "BOX:75001",
         "ADDR:1940 GERYVILLE PK",
         "MADDR:1940 GERYVILLE PIKE",
         "CITY:MILFORD TWP",
         "X:SLEEPY HOLLOW RD & TITLOW RD",
         "TIME:18:15:10",
         "ID:FD1202048",
         "UNIT:FP75 FP");
 
     doTest("T2",
         "STA53:FALRM\n" +
         "adr:HARBOUR LIGHTS APT #A ,24 at 1032 RADCLIFFE ST #A ,24\n" +
         "btwn:JEFFERSON AV & LINCOLN AV\n" +
         "box:53061\n" +
         "tm:20:54 FD1202049  Run: E53",
 
         "SRC:STA53",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:53061",
         "PLACE:HARBOUR LIGHTS APT #A",
         "ADDR:1032 RADCLIFFE ST",
         "APT:A",
         "CITY:BRISTOL",
         "X:JEFFERSON AV & LINCOLN AV",
         "TIME:20:54",
         "ID:FD1202049",
         "UNIT:E53");
   }
   
   @Test
   public void testParser8() {
 
     doTest("T1",
         "Subject:1/1\n" +
         "SQ168:ABLED\n" +
         "adr:SHERWOOD RESIDENCE ,44 at 92 OAKWOOD DR ,44\n" +
         "btwn:BUTTONWOOD DR & BUTTONWOOD DR\n" +
         "box:21011 map:3262E tm:09:50:07 ED1205837",
 
         "CALL:ABLED - ACUTE HEMORRHAGE",
         "BOX:21011",
         "PLACE:SHERWOOD RESIDENCE",
         "ADDR:92 OAKWOOD DR",
         "CITY:MIDDLETOWN TWP",
         "X:BUTTONWOOD DR & BUTTONWOOD DR",
         "MAP:3262E",
         "TIME:09:50:07",
         "ID:ED1205837",
         "UNIT:SQ168");
 
     doTest("T2",
         "Subject:1/2\n" +
         "STA45 STA0:FAPT \n" +
         "adr:106 DISPATCH DR ,72 \n" +
         "btwn:LEXINGTON CT & DISPATCH DR \n" +
         "box:71038 \n" +
         "tm:19:20:02 FD1202317  Run: TR7 ",
 
         "SRC:STA45 STA0",
         "CALL:FAPT - APARTMENT FIRE (BOX)",
         "BOX:71038",
         "ADDR:106 DISPATCH DR",
         "CITY:UPPER MAKEFIELD TWP",
         "X:LEXINGTON CT & DISPATCH DR",
         "TIME:19:20:02",
         "ID:FD1202317",
         "UNIT:TR7");
   }
   
   @Test
   public void testJecashjr() {
 
     doTest("T1",
         "STA26 STA23 STA87 STA17 STA18:FDWL adr:1032 OLD BETHLEHEM RD ,33 btwn:RIDGE RD & CREEK RD box:76030 tm:09:07:08 FD1202476 Run: TW26 TR23 E26 E17 Sent ",
         "SRC:STA26 STA23 STA87 STA17 STA18",
         "CALL:FDWL - DWELLING FIRE (TAC)",
         "BOX:76030",
         "ADDR:1032 OLD BETHLEHEM RD",
         "CITY:EAST ROCKHILL",
         "X:RIDGE RD & CREEK RD",
         "TIME:09:07:08",
         "ID:FD1202476",
         "UNIT:TW26 TR23 E26 E17 Sent");
 
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "[Important message from Bucks County RSAN] STA19:WIRES\n" +
         "adr:400 OLD DUBLIN PK ,29 -- PENN COLOR INC\n" +
         "btwn:TRAFALGAR RD & PINE RUN RD\n" +
         "aai:IFO WAREHOUSE\n" +
         "box:19005\n" +
         "tm:16:47:17 FD1202274  Run: E19\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:WIRES - WIRES DOWN (LOC)",
         "BOX:19005",
         "PLACE:PENN COLOR INC",
         "ADDR:400 OLD DUBLIN PK",
         "MADDR:400 OLD DUBLIN PIKE",
         "CITY:DOYLESTOWN TWP",
         "X:TRAFALGAR RD & PINE RUN RD",
         "INFO:IFO WAREHOUSE",
         "TIME:16:47:17",
         "ID:FD1202274",
         "UNIT:E19");
 
     doTest("T2",
         "[Important message from Bucks County RSAN] STA19:FINV\n" +
         "adr:42 PEBBLE WOODS DR N ,29\n" +
         "btwn:TURK RD & OAK DR\n" +
         "aai:IRO\n" +
         "box:79025\n" +
         "tm:22:35:50 FD1202284  Run: E79\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FINV - FIRE INVESTIGATION (LOC)",
         "BOX:79025",
         "ADDR:42 PEBBLE WOODS DR N",
         "CITY:DOYLESTOWN TWP",
         "X:TURK RD & OAK DR",
         "INFO:IRO",
         "TIME:22:35:50",
         "ID:FD1202284",
         "UNIT:E79");
 
     doTest("T3",
         "[Important message from Bucks County RSAN] STA19:FALRM\n" +
         "adr:641 N MAIN ST ,28 -- CYCLE SPORTS\n" +
         "btwn:MERCER RD & SWAMP RD\n" +
         "box:19014\n" +
         "tm:12:03:27 FD1202341  Run: E19\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:19014",
         "PLACE:CYCLE SPORTS",
         "ADDR:641 N MAIN ST",
         "CITY:DOYLESTOWN",
         "X:MERCER RD & SWAMP RD",
         "TIME:12:03:27",
         "ID:FD1202341",
         "UNIT:E19");
 
     doTest("T4",
         "[Important message from Bucks County RSAN] STA19:FCHIMN\n" +
         "adr:3 WASHINGTON SQ ,29\n" +
         "btwn:OLDE COLONIAL DR & DEAD END\n" +
         "box:19007\n" +
         "tm:19:18:33 FD1202357  Run: L79 E19 E19-1 E79\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FCHIMN - CHIMNEY FIRE (TAC)",
         "BOX:19007",
         "ADDR:3 WASHINGTON SQ",
         "CITY:DOYLESTOWN TWP",
         "X:OLDE COLONIAL DR & DEAD END",
         "TIME:19:18:33",
         "ID:FD1202357",
         "UNIT:L79 E19 E19-1 E79");
 
     doTest("T5",
         "[Important message from Bucks County RSAN] STA19:FBRU\n" +
         "adr:217 UNION ST ,28\n" +
         "btwn:N HAMILTON ST & N CLINTON ST\n" +
         "box:19012\n" +
         "tm:14:24:40 FD1202382  Run: E19 F79\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FBRU - BRUSH FIRE (LOC)",
         "BOX:19012",
         "ADDR:217 UNION ST",
         "CITY:DOYLESTOWN",
         "X:N HAMILTON ST & N CLINTON ST",
         "TIME:14:24:40",
         "ID:FD1202382",
         "UNIT:E19 F79");
 
     doTest("T6",
         "[Important message from Bucks County RSAN] STA29 STA19 STA34:FDWL\n" +
         "adr:57 OLD NEW RD ,29\n" +
         "btwn:DAVIDS WY & WESTAWAY LA\n" +
         "box:78054\n" +
         "tm:01:57:28 FD1202404  Run: E78 E29 L29 E79 TR34\n" +
         "Sent by mss911 Bucks to STA19, STA29, STA34, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA29 STA19 STA34",
         "CALL:FDWL - DWELLING FIRE (TAC)",
         "BOX:78054",
         "ADDR:57 OLD NEW RD",
         "CITY:DOYLESTOWN TWP",
         "X:DAVIDS WY & WESTAWAY LA",
         "TIME:01:57:28",
         "ID:FD1202404",
         "UNIT:E78 E29 L29 E79 TR34");
 
     doTest("T7",
         "[Important message from Bucks County RSAN] STA19:FALRM\n" +
         "adr:GRUNDY HALL ,29 at 1290 ALMSHOUSE RD ,29\n" +
         "btwn:TURK RD & RT 611\n" +
         "box:79057\n" +
         "tm:12:13:33 FD1202423  Run: E79\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:79057",
         "PLACE:GRUNDY HALL",
         "ADDR:1290 ALMSHOUSE RD",
         "CITY:DOYLESTOWN TWP",
         "X:TURK RD & RT 611",
         "TIME:12:13:33",
         "ID:FD1202423",
         "UNIT:E79");
 
     doTest("T8",
         "[Important message from Bucks County RSAN] STA19:FALRM\n" +
         "adr:MOOSE LODGE ,28 at 127 E STATE ST ,28\n" +
         "btwn:BROAD ST & CHURCH ST\n" +
         "aai:2158507282\n" +
         "box:19023\n" +
         "tm:14:01:57 FD1202430  Run: E19\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:19023",
         "PLACE:MOOSE LODGE",
         "ADDR:127 E STATE ST",
         "CITY:DOYLESTOWN",
         "X:BROAD ST & CHURCH ST",
         "INFO:2158507282",
         "TIME:14:01:57",
         "ID:FD1202430",
         "UNIT:E19");
 
     doTest("T9",
         "(Important message from Bucks County RSAN) SQ134 SQ125:FIRCAL\n" +
         "adr:SALVAGE DIRECT ,47 at 77 BRISTOL RD ,47\n" +
         "btwn:W BUTLER AV & UNAMI TL\n" +
         "box:34025 map:3033J4\n" +
         "tm:22:06:32 ED1206040\n\n" +
         "Sent by mss911 Bucks to SQ134, SQ125, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:FIRCAL - FIRE CALL (EMS)",
         "BOX:34025",
         "PLACE:SALVAGE DIRECT",
         "ADDR:77 BRISTOL RD",
         "CITY:NEW BRITAIN",
         "X:W BUTLER AV & UNAMI TL",
         "MAP:3033J4",
         "TIME:22:06:32",
         "ID:ED1206040",
         "UNIT:SQ134 SQ125");
 
     doTest("T10",
         "(Important message from Bucks County RSAN) SQ134:ACHESP\n" +
         "adr:31 CHURCH RD ,36\n" +
         "btwn:BETHLEHEM PK & PARK RD\n" +
         "box:60011 map:3032E1\n" +
         "tm:14:03:32 ED1205994\n\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "BOX:60011",
         "ADDR:31 CHURCH RD",
         "CITY:HILLTOWN TWP",
         "X:BETHLEHEM PK & PARK RD",
         "MAP:3032E1",
         "TIME:14:03:32",
         "ID:ED1205994",
         "UNIT:SQ134");
 
     doTest("T11",
         "(Important message from Bucks County RSAN) SQ134:ACHESP\n" +
         "adr:1600 HORIZON DR #117 ,48 -- GWYNEDD FAMILY MED\n" +
         "btwn:HORIZON CI & COUNTY LINE RD\n" +
         "aai:STE 117\n" +
         "box:74058 map:3033F8\n" +
         "tm:16:11:55 ED1206008\n\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "BOX:74058",
         "PLACE:GWYNEDD FAMILY MED",
         "ADDR:1600 HORIZON DR",
         "APT:117",
         "CITY:NEW BRITAIN TWP",
         "X:HORIZON CI & COUNTY LINE RD",
         "INFO:STE 117",
         "MAP:3033F8",
         "TIME:16:11:55",
         "ID:ED1206008",
         "UNIT:SQ134");
 
     doTest("T12",
         "(Important message from Bucks County RSAN) SQ134:ACHESP\n" +
         "adr:1600 HORIZON DR #117 ,48 -- GWYNEDD FAMILY MED\n" +
         "btwn:HORIZON CI & COUNTY LI\n" +
         "aai:STE 117\n" +
         "box:74058 map:3033F8\n" +
         "tm:16:11:55 ED1206008\n\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "BOX:74058",
         "PLACE:GWYNEDD FAMILY MED",
         "ADDR:1600 HORIZON DR",
         "APT:117",
         "CITY:NEW BRITAIN TWP",
         "X:HORIZON CI & COUNTY LI",
         "INFO:STE 117",
         "MAP:3033F8",
         "TIME:16:11:55",
         "ID:ED1206008",
         "UNIT:SQ134");
 
     doTest("T13",
         "(Important message from Bucks County RSAN) SQ134:ACHESP\n" +
         "adr:1600 HORIZON DR #117 ,48 -- GWYNEDD FAMILY MED\n" +
         "btwn:HORIZON CI & COUNTY LI\n" +
         "aai:STE 117\n" +
         "box:74058 map:3033F8\n" +
         "tm:16:11:55 ED1206008\n\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "BOX:74058",
         "PLACE:GWYNEDD FAMILY MED",
         "ADDR:1600 HORIZON DR",
         "APT:117",
         "CITY:NEW BRITAIN TWP",
         "X:HORIZON CI & COUNTY LI",
         "INFO:STE 117",
         "MAP:3033F8",
         "TIME:16:11:55",
         "ID:ED1206008",
         "UNIT:SQ134");
 
     doTest("T14",
         "(Important message from Bucks County RSAN) SQ134:ASEIZ\n" +
         "adr:MANOR CARE HEALTH SERVICES#228,MONT TWP\n" +
         "aai:640 BETHLEHEM PIKE\n" +
         "box: map:\n" +
         "tm:19:53:41 ED1206032\n\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ASEIZ - SEIZURES - ACTIVE",
         "ADDR:MANOR CARE HEALTH SERVICES",
         "APT:228",
         "CITY:MONT TWP",
         "INFO:640 BETHLEHEM PIKE",
         "TIME:19:53:41",
         "ID:ED1206032",
         "UNIT:SQ134");
 
     doTest("T15",
         "(Important message from Bucks County RSAN) SQ134:FIRCAL\n" +
         "adr:SALVAGE DIRECT ,47 at 77 BRISTOL RD ,47\n" +
         "btwn:W BUTLER AV & UNAMI TL\n" +
         "box:34025 map:3033J4\n" +
         "tm:22:06:32 ED1206040\n\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:FIRCAL - FIRE CALL (EMS)",
         "BOX:34025",
         "PLACE:SALVAGE DIRECT",
         "ADDR:77 BRISTOL RD",
         "CITY:NEW BRITAIN",
         "X:W BUTLER AV & UNAMI TL",
         "MAP:3033J4",
         "TIME:22:06:32",
         "ID:ED1206040",
         "UNIT:SQ134");
 
     doTest("T16",
         "[Important message from Bucks County RSAN] STA19:FALRM\n" +
         "adr:1290 ALMSHOUSE RD ,29 -- GRUNDY HALL\n" +
         "btwn:TURK RD & RT 611\n" +
         "aai:2153434117\n" +
         "box:79057\n" +
         "tm:18:47:59 FD1202516  Run: E79\n" +
         "Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "SRC:STA19",
         "CALL:FALRM - FIRE ALARM (LOC)",
         "BOX:79057",
         "PLACE:GRUNDY HALL",
         "ADDR:1290 ALMSHOUSE RD",
         "CITY:DOYLESTOWN TWP",
         "X:TURK RD & RT 611",
         "INFO:2153434117",
         "TIME:18:47:59",
         "ID:FD1202516",
         "UNIT:E79");
 
     doTest("T17",
         "(Important message from Bucks County RSAN) SQ134:AFALL\n" +
         "adr:2800 STATE RD #315 ,77 -- LUTHERAN COMMUNITY btwn:TELFO BORO LINE & AUTUMN LEA CI aai:MEADOWS APTS\n" +
         "box:37501 map:2919H7\n" +
         "tm:12:30:21 ED1212342\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:AFALL - FALL VICTIM - CRITICAL",
         "BOX:37501",
         "PLACE:LUTHERAN COMMUNITY",
         "ADDR:2800 STATE RD",
         "APT:315",
         "CITY:WEST ROCKHILL TWP",
         "X:TELFO BORO LINE & AUTUMN LEA CI",
         "INFO:MEADOWS APTS",
         "MAP:2919H7",
         "TIME:12:30:21",
         "ID:ED1212342",
         "UNIT:SQ134");
 
     doTest("T18",
         "[Important message from Bucks County RSAN] SQ134 STA60:ADIAB\n" +
         "adr:31 CHURCH RD ,36\n" +
         "btwn:BETHLEHEM PK & PARK RD\n" +
         "box:60011 map:3032E1\n" +
         "tm:23:13:05 ED1215516\n" +
         "Sent by mss911 Bucks to SQ134, STA60, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n",
 
         "CALL:ADIAB - DIABETIC EMERGENCY",
         "BOX:60011",
         "ADDR:31 CHURCH RD",
         "CITY:HILLTOWN TWP",
         "X:BETHLEHEM PK & PARK RD",
         "MAP:3032E1",
         "TIME:23:13:05",
         "ID:ED1215516",
         "UNIT:SQ134 STA60");
   }
   
   @Test
   public void testOOCMutualAid() {
 
     doTest("T1",
         "[Important message from Bucks County RSAN] SQ134:ACVA\n" +
         "adr:54 NESHAMINY FALLS CIRCLE X/WOODSBLUFF\n" +
         "aai:RN -CV 345A MONTGO TWP\n" +
         "box: map:\n" +
         "tm:16:14:29 ED1208711\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ACVA - CVA/STROKE",
         "ADDR:54 NESHAMINY FALLS CIRCLE",
         "X:WOODSBLUFF",
         "INFO:RN -CV 345A MONTGO TWP",
         "CITY:MONTGOMERY COUNTY",
         "TIME:16:14:29",
         "ID:ED1208711",
         "UNIT:SQ134");
 
     doTest("T2",
         "[Important message from Bucks County RSAN] SQ134:ACHESP\n" +
         "adr:46 CENTER COURT X/WOODSBLUFF RUN\n" +
         "aai:CV A345\n" +
         "box: map:\n" +
         "tm:10:28:46 ED1208799\n" +
         "Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
 
         "CALL:ACHESP - CHEST PAINS (CARDIAC SYMPT.)",
         "ADDR:46 CENTER COURT",
         "X:WOODSBLUFF RUN",
         "INFO:CV A345",
         "CITY:MONTGOMERY COUNTY",
         "TIME:10:28:46",
         "ID:ED1208799",
         "UNIT:SQ134");
 
   }
   
   @Test
   public void testLorenBarret() {
 
     doTest("T1",
         "alert31089@alert.bucksema.org /1/1 /STA13:FINV\nadr:INDIAN CREEK DR/INDIAN CREEK PS ,25\nbox:13005\ntm:04:27:01 FD1205233  Run: SQ13",
         "SRC:STA13",
         "CALL:FINV - FIRE INVESTIGATION (LOC)",
         "BOX:13005",
         "ADDR:INDIAN CREEK DR & INDIAN CREEK PS",
         "CITY:BRISTOL TWP",
         "TIME:04:27:01",
         "ID:FD1205233",
         "UNIT:SQ13");
 
     doTest("T2",
         "alert31277@alert.bucksema.org /1/1 /STA13 STA10:WATER\n" +
         "adr:ACME MARKET ,25 at 6800 NEW FALLS RD ,25\n" +
         "btwn:WOERNER AV & EDGELY AV\n" +
         "aai:2159461840\n",
 
         "CALL:WATER - WATER FLOW (LOC)",
         "PLACE:ACME MARKET",
         "ADDR:6800 NEW FALLS RD",
         "CITY:BRISTOL TWP",
         "X:WOERNER AV & EDGELY AV",
         "INFO:2159461840",
         "UNIT:STA13 STA10");
 
     doTest("T3",
         "alert33407@alert.bucksema.org /1/1 /STA13:FALRM\n" +
         "adr:ADRIATIC PIZZA ,25 at 7757 NEW FALLS RD ,25\n" +
         "btwn:APPLETREE DR & WISTAR RD\n" +
         "aai:;2159498520\n",
 
         "CALL:FALRM - FIRE ALARM (LOC)",
         "PLACE:ADRIATIC PIZZA",
         "ADDR:7757 NEW FALLS RD",
         "CITY:BRISTOL TWP",
         "X:APPLETREE DR & WISTAR RD",
         "INFO:;2159498520",
         "UNIT:STA13");
 
     doTest("T4",
         "alert31277@alert.bucksema.org /1/2 /STA13 STA10:WATER\n" +
         "adr:ACME MARKET ,25 at 6800 NEW FALLS RD ,25\n" +
         "btwn:WOERNER AV & EDGELY AV\n" +
         "aai:2159461840\n",
 
         "CALL:WATER - WATER FLOW (LOC)",
         "PLACE:ACME MARKET",
         "ADDR:6800 NEW FALLS RD",
         "CITY:BRISTOL TWP",
         "X:WOERNER AV & EDGELY AV",
         "INFO:2159461840",
         "UNIT:STA13 STA10");
 
   }
   
   @Test
   public void testGeneralAlert() {
 
     doTest("T1",
         "(Important message from Bucks County RSAN) STA19, ANY AVAIL STATION 19 CHIEF OFFICER PHONE FIRE COMMUNICATIONS Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
         "SRC:STA19",
         "CALL:GENERAL ALERT",
         "PLACE:ANY AVAIL STATION 19 CHIEF OFFICER PHONE FIRE COMMUNICATIONS");
 
     doTest("T2",
         "(Important message from Bucks County RSAN) A134 ***test test test test*** Sent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
         "SRC:A134",
         "CALL:GENERAL ALERT",
         "PLACE:***test test test test***");
 
     doTest("T3",
         "(Important message from Bucks County RSAN) STA19,ANY AVAIL STATION 19 CHIEF OFFICER PHONE FIRE COMMUNICATIONS Sent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN",
         "SRC:STA19",
         "CALL:GENERAL ALERT",
         "PLACE:ANY AVAIL STATION 19 CHIEF OFFICER PHONE FIRE COMMUNICATIONS");
 
     doTest("T4",
         "[911 Data]  05/29/12  10:17:27 ~TO~ INT1 FROM FS01:\nSTA19,ANY AVAIL STA 19 OR 79 CHIEF OFC CALL FIRE COMM/#764\n",
         "SRC:STA19",
         "CALL:GENERAL ALERT",
         "PLACE:ANY AVAIL STA 19 OR 79 CHIEF OFC CALL FIRE COMM/#764",
         "DATE:05/29/12",
         "TIME:10:17:27");
   }
   
   
   public static void main(String[] args) {
     new PABucksCountyAParserTest().generateTests("T1");
   }
 }

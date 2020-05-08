 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Bucks County, PA
 Contact: "Ryan Pankoe" <rpankoe@gmail.com>
 Contact: Ed Ackerman <ed.ackerman@gmail.com>
 Contact: Terry <securetalk@gmail.com>
 Contact: "Russ Swinehart Jr." <snoopfire10@yahoo.com>
 Contact: Jayson Wagner <jayson.wagner58@yahoo.com>
 Contact: John Shipp <dieseltruckie14@gmail.com>
 Sender: 8276,a@bnn.us
 
 911:SQ134  type:BTAI    adr:S LIMEKILN PK/UPPER STATE RD ,48  aai:  box:74064  map:3033F7  tm:21:05:23  ED1042264
 911:SQ134  type:ARESP   adr:4217 HORSESHOE WY ,36  btwn BERRY BROW DR &amp; SPARROW WY  aai:  box:60002  map:2921B10  tm:05:02:04  ED1042293  
 911:SQ134  type:ARESP   adr:221 HAMPSHIRE DR ,48  btwn CORNWALL DR &amp; SURREY RD  aai:  box:74062  map:3033E6  tm:14:06:02  ED1042334  
 911:SQ134  type:AFAINT  adr:1553 LOWER STATE RD ,29  btwn BRISTOL RD &amp; OLD NEW RD  aai:  box:78054  map:3034E7  tm:20:28:45  ED1042385  
 911:SQ134  type:BTAI    adr:1235 S TOWNSHIP LINE RD ,36  btwn CHALFONT RD &amp; LIMEKILN PK  aai:  box:61003  map:2921D10  tm:12:02:56  ED1042577  
 911:SQ134  type:BBLED   adr:102 HARRISON FORGE CT ,48  btwn E FAIRWOOD DR &amp; CUL DE SAC  aai:  box:34030  map:3033H1  tm:14:38:26  ED1042595  
 911:SQ134  type:BINJ    adr:BARC HOUSE ,47 at 80 PUEBLO RD ,47 btwn LENAPE DR &amp; UTE RD  aai:DOWNSTAIRS  box:34033  map:3033J2  tm:16:57:04  ED1043086  
 911:STA19  type:FALRM   adr:DTH ,29 at 595 W STATE ST ,29 btwn PROGRESS DR &amp; RT 611 BY  aai:215 345-2200  box:19051  map:3034G2  tm:12:37:42  FD101999
 911:SQ134  type:ASEIZ   adr:BRISTOL RD/UPPER STATE RD ,29  aai:AT ENTR TO BLOOMING GLEN  box:34041  map:3033K5  tm:11:01:28  ED1043679
 911: STA19  type:FUNK    adr:EDISON RD/RT 611 ,29  aai:  box:79028  map:3034K5  tm:12:44:52  FD1108375    Run: E79    \n
 
 Contact: Kevin Coughlin <kcoughlin37@gmail.com>
 Sender: 2155102861
 911: STA16  type:RAUTO   adr:ROBS TRUCK & TRAIL ,22 at 750 STREET RD ,22 btwn CLIVE AV & SAMUEL AV  aai:IFO  box:37017  map:3374A8  tm:16:43:05  FD1116594    Ru
 
 0001/0002 911:SQ134  type:ATRAN   adr:2343 BETHLEHEM PK #312 ,36 -- DOCK MEADOWS NH btwn UNIONVILLE PK &amp; CHURCH RD  aai:GO TO CHURCH RD NORTH ENTRANCE  
 0002/0002 box:60063 map:3032E3 &nbsp;tm:14:48:06 ED1042598&nbsp;
 
 0001/0002 911: SQ134  type:BFALL   adr:800 MANOR DR #315 ,48 -- NEW SEASONS ASSIST btwn E SCHOOL HOUSE RD & COUNTY LINE  aai:  box:74066 map:3033E8
 0002/0002 tm:16:50:43  ED1101813
 
 0001/0002 911: SQ134  type:AALLR   adr:4371 COUNTY LINE RD ,48 -- VERTIS DIRECT MKTG btwn BRITAIN WOOD CI & RR CROSSING  aai:  box:74096 map:3033C7
 0002/0002 tm:09:28:45  ED1102262
 
 911: STA57  type:FEMS    adr:SLEEPY HOLLOW RD/SPINNERSTOWN RD ,45  aai:  box:75011  map:2693K9  tm:10:26:44  FD1104574    Run: E75
 911: STA57  stype:FTAI    adr:FRIER RD/MILL RD ,45  aai:  box:57002  map:  tm:13:51:38  FD1104463    Run: E57
 911: STA4  type:RAUTO   adr:PHILMONT AV/E STREET RD ,43  aai:PHILMONT SO STREET  box:04003  map:3261B8  tm:10:21:22  FD1104774    Run: R4 R6 E1
 911: STA53  type:RMAR    adr:LIONS PARK ,24 at 102 SAMUEL CLIFT ST ,24 btwn MILL ST & MARKET ST  aai:  box:51012  map:3375D5  tm:00:59:10  FD1105678 Run: R53 M53 M11
 911: STA53  type:WIREIN  adr:2218 TRENTON AV ,24  btwn BARRY PL & TAFT ST  aai:  box:53034  map:3375F2  tm:11:40:15  FD1104667    Run: L25 Q51 E50 E52
 911: STA53  stype:RAUTO   adr:NEW FALLS RD/PINE AV ,25  aai:  box:12044  map:3262G9  tm:21:30:34  FD1107837    Run: R13 R53 E82
 911: STA19  type:WATER   adr:EIGHTEENFOURTEEN H ,28 at 50 S MAIN ST ,28 btwn W OAKLAND AV & TAYLOR AV  aai:  box:19022  map:2922J10  tm:02:29:07  FD11
 911: STA19  type:RVBLD   adr:LOBSTER CLAW ,57 at 800 N EASTON RD ,57 btwn BKHAM TWP LINE & CROSS KEYS DR  aai:  box:20001  map:2922J6  tm:16:03:22  FD
 911: STA58  type:FBLD    adr:121 PARK AV ,58  btwn FRONT ST & S 2ND ST  aai:QUAKERTOWN ASSOCIATES 215-901-8033  box:17001  map:2695H10  tm:17:44:38   FD1110887    Run: L57 SQ18 E36 R18 R58 
 911: STA14  type:EXBLD   adr:3501 BRISTOL OXF VALLEY RD #1107 ,25 -- AVALON COURT APT btwn LESLIE DR & BORDER  aai:  box:82015  map:3263A9  tm:22:06:28  FD1110945  Run: TW14 E12
 911: SQ134  type:APOSXP  adr:307 W BUTLER AV ,48  btwn BRITTANY DR & SURREY RD  aai:NE SECTOR  box:74062  map:3033E6  tm:07:24:51  ED1126192
 911: STA4  type:APOSXP  adr:3449 AZALEA AV ,22  btwn TRAILWOOD DR & VALLEY RD  aai:  box:04007  map:3261E8  tm:05:29:00  ED1126324  
 911: SQ134  type:AASSLT  adr:STADIUM BAR & GRIL ,36 at 1903 BETHLEHEM PK ,36 btwn SWARTLEY RD & MILL RD aai:  box:60063  map:3032F3  tm:02:33:13  ED1127778  
 911: STA4  type:RDOM    adr:NESHA STATE PARK ,22 at 3401 STATE RD ,22 btwn DUNKSFERRY RD & WINKS LA  aai:NEXT TO POOL AREA AT PICNIC AREA  box:37023  map:3374C7  tm:12:52:17  FD1111780    Run: R16 SQ37 R4
 911: SQ134  type:ATRAN   adr:CENTER SQUARE TOWR #823-A ,28 at 555 BROAD ST #823-A ,28 btwn ATKINSON DR & VETE  aai:  box:19052  map:2922H10 tm:18:18:15  ED1127865  
 STA19  type:FALRM   adr:LINDEN ELEM SCH ,28 at 480 LINDEN AV ,28 btwn EAST ST & ROHR DR  aai:  box:19093  map:2923A10  tm:20:27:42  FD1111756\4s
 911: STA19  type:GALRM   adr:6 FLINT CI ,29  btwn WINDSOR WY & CUL DE SAC  aai:  box:79020  map:3034H5  tm:23:32:45  FD1112459\4sRun: E79\4s\n
 
 Contact: Patrick Roberts <pgremt@gmail.com>
 Sender: 2156696195
 911: SQ122  type:ESPEC   adr:HARTS FIRE CO ,74 at 1195 YORK RD ,74 btwn OLD YORK RD & BRISTOL RD  aai:  box:93030  map:3147E4  tm:05:04:12  ED1131511
 
 Contact: "Ryan Pankoe" <rpankoe@gmail.com>
 911: SQ134  type:BASSLT  adr:248 INVERNESS CI ,48  btwn BONNIE LARK CT & CUL DE SAC  aai:  box:74059  map:3033F7  tm:16:38:31  ED1133930  
 911: SQ134  type:FIRCAL  adr:4201 MILORDS LA ,26  btwn SIR ANDREW CT & SIR ANDREW CT  aai:NEXT DOOR  box:19018  map:2923B4  tm:19:34:11  ED1136021  
 911: STA19  type:WIRES   adr:RT 202/N SHADY RETREAT RD ,47  aai:  box:19006  map:3034E1  tm:13:59:42  FD1116344    Run: E19    \n
 911: STA19  type:FDWL    adr:260 SANDY RIDGE RD ,29  btwn CAVALLO WY & IRON HILL RD  aai:  box:19006  map:3034C1  tm:12:08:50  FD1116685    Run: L79 E
 911: SQ134  type:ATAI    adr:BETHLEHEM PK/HILLTOWN PK ,36  aai:  box:60041  map:3032H4  tm:21:37:43  ED1139682  
 
 Contact: "Shapp, Dave" <dave.shapp@midwayvfc.com>
 911: STA5  type:FALRM   adr:5263 MOUNTAIN TOP RD ,26  btwn HOLICONG RD & DEAD END  aai:MARTINI RESD #2157942445  box:05027  map:2924E9 tm:19:46:52  FD1118872    Run: E5
 911: STA5  type:RAUTO   adr:HOLICONG RD/YORK RD ,26  aai:ON YORK NO HOLICONG  box:05021  map:2924B6  tm:12:43:08  FD1117995    Run: R5 E5
 911: STA5  type:FBLD    adr:4201 MILORDS LA ,26  btwn SIR ANDREW CT & SIR ANDREW CT  aai:NEXT DOOR  box:19018  map:2923B4  tm:19:34:01 FD1116371    Run: L5 E79 E20 TW24
 911: STA5  type:WIRES   adr:RT 413/UPPER MOUNTAIN RD ,26  aai: box:05025  map:2924B10  tm:17:35:03  FD1118467    Run: E15-1
 911: STA5  type:WIRES   adr:CHURCH SCHOOL RD/MECHANICSVILLE RD ,26 aai:ON MECHANICSVILLE  box:15021  map:2923F9  tm:14:06:20  FD1118031 Run: E15
 911: STA5  type:ELEVAT  adr:OUR LADY GUADAL ,26 at 5194 COLD SPRING CREAME RD ,26 btwn GREYSTONE DR & RT 413  aai:  box:15052  map:2923F2 tm:14:31:10  FD1116465    Run: E15
 911: STA5  type:FINV    adr:AQUETONG RD/TAYLOR LA ,65  aai:  box:05031  map:2924E1  tm:21:09:49  FD1117661    Run: E5
 
  */
 
 
 public class PABucksCountyParser extends FieldProgramParser {
   
   public PABucksCountyParser() {
     super("BUCKS COUNTY", "PA",
            "SRC! type:CALL! adr:ADDR! btwn:X aai:INFO box:BOX map:MAP tm:ID% Run:UNIT");
   }
   
   @Override
   public String getFilter() {
     return "8276,@bnn.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (body.startsWith("911:")) body = body.substring(4).trim();
    data.expectMore = true;
     
     body = body.replaceAll(" btwn ", " btwn:").replaceAll(" stype:", " type:").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&").trim();
     return super.parseMsg(body, data);
   }
   
   private class MyCallField extends CallField {
     
     @Override
     public void parse(String field, Data data) {
       String desc = TYPE_CODES.getProperty(field);
       if (desc != null) field = field + " - " + desc;
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String sAddr, Data data) {
       Parser p = new Parser(sAddr);
       data.strPlace = p.getOptional(" at ");
       if (data.strPlace.length() == 0) data.strPlace = p.getLastOptional("--");
       if (data.strPlace.length() > 0) {
         int pt = data.strPlace.indexOf(',');
         if (pt >= 0) data.strPlace = data.strPlace.substring(0, pt).trim();
       }
       String cityCode = p.getLastOptional(',');
       super.parse(p.get(), data);
       if (cityCode.length() > 0) {
         try {
           int iCity = Integer.parseInt(cityCode);
           iCity = iCity - INIT_TOWN_CODE;
           if (iCity >= 0 && iCity < TOWN_CODES.length){
             data.strCity = TOWN_CODES[iCity];
           }
         } catch (NumberFormatException ex) {}
       }
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames() + " CITY";
     }
   }
     
   private class MyIdField extends IdField {
     
     @Override
     public void parse(String tm, Data data) {
       if (tm != null) {
         Parser p = new Parser(tm);
         data.strCallId = p.getLastOptional(' ');
       }
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
   
   private static final int INIT_TOWN_CODE = 21;
   private static final String[] TOWN_CODES = new String[]{
   /*21*/ "BEDMINSTER TWP",
   /*22*/ "BENSALEM TWP",
   /*23*/ "BRIDGETON TWP",
   /*24*/ "BRISTOL",
   /*25*/ "BRISTOL TWP",
   /*26*/ "BUCKINGHAM TWP",
   /*27*/ "CHALFONT",
   /*28*/ "DOYLESTOWN",
   /*29*/ "DOYLESTOWN TWP",
   /*30*/ "",
   /*31*/ "DUBLIN",
   /*32*/ "DURHAM TWP",
   /*33*/ "EAST ROCKHILL",
   /*34*/ "FALLS TWP",
   /*35*/ "HAYCOCK TWP",
   /*36*/ "HILLTOWN TWP",
   /*37*/ "HULMEVILLE",
   /*38*/ "IVYLAND",
   /*39*/ "LANGHORNE",
   /*40*/ "",
   /*41*/ "LANGHORNE MANOR",
   /*42*/ "LOWER MAKEFIELD TWP",
   /*43*/ "LOWER SOUTHAMPTON TWP",
   /*44*/ "MIDDLETOWN TWP",
   /*45*/ "MILFORD TWP",
   /*46*/ "MORRISVILLE",
   /*47*/ "NEW BRITAIN",
   /*48*/ "NEW BRITAIN TWP",
   /*49*/ "NEW HOPE",
   /*50*/ "",
   /*51*/ "NEWTOWN",
   /*52*/ "NEWTOWN TWP",
   /*53*/ "NOCKAMIXON TWP",
   /*54*/ "NORTHAMPTON TWP",
   /*55*/ "PENNDEL",
   /*56*/ "PERKASIE",
   /*57*/ "PLUMSTEAD TWP",
   /*58*/ "QUAKERTOWN TWP",
   /*59*/ "RICHLAND TWP",
   /*60*/ "",
   /*61*/ "RICHLANDTOWN",
   /*62*/ "RIEGELSVILLE",
   /*63*/ "SELLERSVILLE",
   /*64*/ "SILVERDALE",
   /*65*/ "SOLEBURY TWP",
   /*66*/ "SPRINGFIELD TWP",
   /*67*/ "TELFORD",
   /*68*/ "TINICUM TWP",
   /*69*/ "TRUMBAUERSVILLE",
   /*70*/ "",
   /*71*/ "TULLYTOWN",
   /*72*/ "UPPER MAKEFIELD TWP",
   /*73*/ "U SOUTHAMPTON TWP",
   /*74*/ "WARMINSTER TWP",
   /*75*/ "WARRINGTON TWP",
   /*76*/ "WARWICK TWP",
   /*77*/ "PENNRIDGE REGIONAL PD",
   /*78*/ "WRIGHTSTOWN TWP",
   /*79*/ "YARDLEY"
   };
   
   private static final Properties TYPE_CODES = buildCodeTable(new String[]{
       "AIRC",     "AIRPLANE CRASH (TAC)",
       "ACARDA",   "Cardiac/Respiratory Arrest",
       "APOSXP",   "POSSIBLE EXPIRATION",
       "BOMB",     "BOMB THREAT",
       "BURN",     "BURN VICTIM (LOC)",
       "CBURN",    "CONTROLLED BURN",
       "COALRM",   "CARBON MONOXIDE ALARM (LOC )",
       "DECON",    "DECON ASSIGNMENT (LOC)",
       "ELEVAT",   "STUCK ELEVATOR (LOC)",
       "EXBLD",    "EXTINGUISHED STRUCTURE (TAC)",
       "FALRM",    "FIRE ALARM (LOC)",
       "FAPL",     "APPLIANCE FIRE (TAC)",
       "FAPT",     "APARTMENT FIRE (BOX)",
       "FAST",     "FIRE ASSIST (OUT OF COUNTY)",
       "FAUTO",    "AUTOMOBILE (LOC)",
       "FBARN",    "BARN FIRE (TAC)",
       "FBLD",     "BUILDING FIRE (BOX)",
       "FBOAT",    "BOAT FIRE (LOC)",
       "FBRU",     "BRUSH FIRE (LOC)",
       "FBUS",     "BUS FIRE (LOC)",
       "FCHIMN",   "CHIMNEY FIRE (TAC)",
       "FCOV",     "FIRE STATION COVER",
       "FDRILL",   "FIRE DRILL",
       "FDUMP",    "DUMPSTER FIRE (LOC)",
       "FDWL",     "DWELLING FIRE (TAC)",
       "FEXP",     "EXPLOSION (LOC)",
       "FEMS",     "FIRE ASSIST EMS (LOC)",
       "FGARAG",   "GARAGE FIRE (TAC)",
       "FGRILL",   "GRILL FIRE (LOC)",
       "FHAZ",     "HAZMAT FIXED LOCATION (TAC)",
       "FHOSP",    "HOSPITAL FIRE",
       "FINFO",    "FIRE INFORMATION",
       "FINV",     "FIRE INVESTIGATION (LOC)",
       "FIRE",     "FIRE OTHERS (LOC)",
       "FIRCAL",   "FIRE CALL (EMS)",
       "FMPAGE",   "FM PAGE REQUEST",
       "FNURS",    "NURSING HOME FIRE (BOX)",
       "FOBLD",    "OUTBUILDING FIRE (TAC)",
       "FPAGE",    "FD PAGE REQUEST",
       "FPOL",     "FIRE POLICE REQUEST",
       "FRUB",     "RUBBISH FIRE (LOC)",
       "FSCHOL",   "SCHOOL FIRE (BOX)",
       "FSPEC",    "SPECIAL ASSIGNMENT",
       "FSPILL",   "FUEL LEAK INCIDENT (LOC)",
       "FSTORE",   "SMALL STORE FIRE (TAC)",
       "FTA",      "ACCIDENT W/FIRE (LOC)",
       "FTAI",     "ACCIDENT W/FIRE & INJ (LOC)",
       "FTHAZ",    "TRANSPORTATION HAZMAT",
       "FTRAIN",   "TRAIN FIRE (TAC)",
       "FTRUCK",   "TRUCK FIRE (LOC)",
       "FUMEIN",   "FUMES INSIDE STRUCTURE (TAC)",
       "FUMES",    "FUMES OUTSIDE (LOC)",
       "FUNK",     "UNKNOWN TYPE FIRE",
       "GALRM",    "GENERAL ALARM",
       "RAUTO",    "AUTO EXTRICATION",
       "RDOM",     "DOMESTIC RESCUE",
       "RMAR",     "MARINE RESCUE",
       "RVBLD",    "VEHICLE INTO A BUILDING",
       "STDBY",    "FIRE STANDBY",
       "WATER",    "WATER FLOW",
       "WIREIN",   "WIRES IN DWELLING",
       "WIRES",    "WIRES DOWN (LOC)",
 
       "AABDO",      "ACUTE ABDOMEN",
       "AALLR",      "ANAPHYLAXIS",
       "AASSL",      "ASSAULT VICTIM (ALS)",
       "ABLED",      "ACUTE HEMORRHAGE",
       "ABURN",      "CRITICAL BURN VICTIM",
       "ACHESP",     "CHEST PAINS (CARDIAC SYMPT.)",
       "ACHOKE",     "OBSTRUCTED AIRWAY (ACTIVE)",
       "ACOP",       "CARBON MONOXIDE POISONING",
       "ACVA",       "CVA/STROKE",
       "ADIAB",      "DIABETIC EMERGENCY",
       "ADROWN",     "DROWNING - CRITICAL",
       "AELEC",      "ELECTRICAL SHOCK VICTIM",
       "AENVIR",     "ENVIRONMENTAL EMERGENCY",
       "AFAINT",     "SYNCOPAL EPISODE",
       "AFALL",      "FALL VICTIM - CRITICAL",
       "AGUN",       "GUNSHOT VICTIM",
       "AHRI",       "ALS HIT & RUN ACCIDENT WITH INJURY",
       "AOBG",       "ALS OB/GYN EMERGENCY",
       "AOD",        "OVERDOSE/POISONING - CRITICAL",
       "APREG",      "MATERNITY - CRITICAL",
       "ARESP",      "RESPIRATORY DISTRESS",
       "ASEIZ",      "SEIZURES - ACTIVE",
       "AASSLT",     "ASSAULT W/ TRAUMA",
       "ASTAB",      "STABBING VICTIM",
       "ATAI",       "ALS TRAFFIC ACCIDENT INJURIES",
       "ATRAN",      "ALS/UNSPECIFIED EMERGENCY",
       "ATRAUM",     "MULTI-SYSTEMS TRAUMA",
       "AUNC",       "UNCONSCIOUS SUBJECT",
       "AUNR",       "UNRESPONSIVE SUBJECT",
       "BABDO",      "ABDOMINAL PAIN",
       "BALLR",      "ALLERGIC REACTION",
       "BASSLT",      "ASSAULT - NON CRITICAL",
       "BBLED",      "BLEEDING (NON TRAUMATIC)",
       "BCHESP",     "CHEST PAIN  (NON CRITICAL)",
       "BINJ",       "MINOR INJURY - NON CRITICAL",
       "BLAC",       "ANIMAL BITE",
       "BPSYCH",     "PSYCHIATRIC TRANSPORTATION",
       "BOD",        "OVERDOSE/POISONING - NON CRITICAL",
       "BPREG",      "MATERNITY - NON CRITICAL",
       "BUNK",       "BLS/UNKNOWN PROBLEM",
       "BBURN",      "SCALDING/MINOR BURNS",
       "BDROWN",     "AQUATIC ACCIDENT (NON CRITICAL)",
       "BFALL",      "FALL VICTIM - NON CRITICAL",
       "BLAC",       "LACERATION/CONTROLLED BLEEDING",
       "BPTAST",     "PATIENT ASSIST",
       "BTAI",       "BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
       "BTRAN",      "EMERGENCY TRANSPORTATION",
       "ESPEC",      "STANDBY / SPECIAL ASSIGNMENT (EMS)",
       "MALRM",      "MEDICAL ALARM"
   });
 }

 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 Bucks County, PA
 Contact: "Ryan Pankoe" <rpankoe@gmail.com>
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
 
 0001/0002 911:SQ134  type:ATRAN   adr:2343 BETHLEHEM PK #312 ,36 -- DOCK MEADOWS NH btwn UNIONVILLE PK &amp; CHURCH RD  aai:GO TO CHURCH RD NORTH ENTRANCE  
 0002/0002 box:60063 map:3032E3 &nbsp;tm:14:48:06 ED1042598&nbsp;
 
 0001/0002 911: SQ134  type:BFALL   adr:800 MANOR DR #315 ,48 -- NEW SEASONS ASSIST btwn E SCHOOL HOUSE RD & COUNTY LINE  aai:  box:74066 map:3033E8
 0002/0002 tm:16:50:43  ED1101813
 
 0001/0002 911: SQ134  type:AALLR   adr:4371 COUNTY LINE RD ,48 -- VERTIS DIRECT MKTG btwn BRITAIN WOOD CI & RR CROSSING  aai:  box:74096 map:3033C7
 0002/0002 tm:09:28:45  ED1102262
 
 Contact: Terry <securetalk@gmail.com>
 Sender: 2674243300
 911: STA57  type:FEMS    adr:SLEEPY HOLLOW RD/SPINNERSTOWN RD ,45  aai:  box:75011  map:2693K9  tm:10:26:44  FD1104574    Run: E75
 911: STA57  stype:FTAI    adr:FRIER RD/MILL RD ,45  aai:  box:57002  map:  tm:13:51:38  FD1104463    Run: E57

  */
 
 
 public class PABucksCountyParser extends SmsMsgParser {
   
   private static final String[] KEYWORDS = 
      new String[]{"911", "type", "stype", "adr", "btwn", "aai", "box", "map", "tm"};
   
   public PABucksCountyParser() {
     super("BUCKS COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "8276,@bnn.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     int pt = body.indexOf("911:");
     if (pt < 0) return false;
     data.expectMore = true;
     
     body = body.replaceAll(" btwn ", " btwn:").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&").trim();
     Properties props = parseMessage(body, KEYWORDS);
     
     String sAddr = props.getProperty("adr", "");
     Parser p = new Parser(sAddr);
     data.strPlace = p.getOptional(" at ");
     if (data.strPlace.length() == 0) data.strPlace = p.getLastOptional("--");
     if (data.strPlace.length() > 0) {
       pt = data.strPlace.indexOf(',');
       if (pt >= 0) data.strPlace = data.strPlace.substring(0, pt).trim();
     }
     String cityCode = p.getLastOptional(',');
     parseAddress(p.get(), data);
     if (cityCode.length() > 0) {
       try {
         int iCity = Integer.parseInt(cityCode);
         iCity = iCity - INIT_TOWN_CODE;
         if (iCity >= 0 && iCity < TOWN_CODES.length){
           data.strCity = TOWN_CODES[iCity];
         }
       } catch (NumberFormatException ex) {}
     }
     
     
     data.strSource = props.getProperty("911", "");
     data.strCall = props.getProperty("type", "");
    if (data.strCall.length() == 0) data.strCall = props.getProperty("stype", "");
     data.strCross = props.getProperty("btwn", "");
     data.strSupp = props.getProperty("aai", "");
     data.strBox = props.getProperty("box", "");
     data.strMap = props.getProperty("map", "");
     
     String tm = props.getProperty("tm");
     if (tm != null) {
       data.expectMore = false;
       p = new Parser(tm);
       data.strCallId = p.getLastOptional(' ');
     }
     
     // Add call description
     String desc = TYPE_CODES.getProperty(data.strCall);
     if (desc != null) data.strCall = data.strCall + " - " + desc;
     
     return true;
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
       "FIRCAL",   "FIR CALL (EMS)",
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
 
       "AABDO",      "ACUTE ABDOMEN",
       "BABDO",      "ABDOMINAL PAIN",
       "AALLR",      "ANAPHYLAXIS",
       "BALLR",      "ALLERGIC REACTION",
       "BLAC",       "ANIMAL BITE",
       "BINJ",       "MINOR INJURY - NON CRITICAL",
       "ABLED",      "ACUTE HEMORRHAGE",
       "BBLED",      "BLEEDING (NON TRAUMATIC)",
       "ARESP",      "RESPIRATORY DISTRESS",
       "BPTAST",     "PATIENT ASSIST",
       "ACHESP",     "CHEST PAINS (CARDIAC SYMPT.)",
       "BCHESP",     "CHEST PAIN  (NON CRITICAL)",
       "ACHOKE",     "OBSTRUCTED AIRWAY (ACTIVE)",
       "ADIAB",      "DIABETIC EMERGENCY",
       "BTRAN",      "EMERGENCY TRANSPORTATION",
       "AENVIR",     "ENVIRONMENTAL EMERGENCY",
       "AOBG",       "ALS OB/GYN EMERGENCY",
       "ACVA",       "CVA/STROKE",
       "ATRAN",      "NEUROLOGICAL - HEAD TRAUMA",
       "BBLED",      "BLEEDING (NON TRAUMATIC)",
       "AOD",        "OVERDOSE/POISONING - CRITICAL",
       "AGUN",       "GUNSHOT VICTIM",
       "ASTAB",      "STABBING VICTIM",
       "BPSYCH",     "PSYCHIATRIC TRANSPORTATION",
       "AOD",        "OVERDOSE/POISONING - CRITICAL",
       "BOD",        "OVERDOSE/POISONING - NON CRITICAL",
       "ACOP",       "CARBON MONOXIDE POISONING",
       "APREG",      "MATERNITY - CRITICAL",
       "BPREG",      "MATERNITY - NON CRITICAL",
       "ASEIZ",      "SEIZURES - ACTIVE",
       "AUNR",       "UNRESPONSIVE SUBJECT",
       "ATRAN",      "ALS/UNSPECIFIED EMERGENCY",
       "ACVA",       "CVA/STROKE",
       "AUNC",       "UNCONSCIOUS SUBJECT",
       "AFAINT",     "SYNCOPAL EPISODE",
       "AUNR",       "UNRESPONSIVE",
       "BUNK",       "BLS/UNKNOWN PROBLEM",
       "ABURN",      "CRITICAL BURN VICTIM",
       "AELEC",      "ELECTRICAL SHOCK VICTIM",
       "BBURN",      "SCALDING/MINOR BURNS",
       "ADROWN",     "DROWNING - CRITICAL",
       "BDROWN",     "AQUATIC ACCIDENT (NON CRITICAL)",
       "AFALL",      "FALL VICTIM - CRITICAL",
       "ATRAUM",     "MULTI-SYSTEMS TRAUMA",
       "BFALL",      "FALL VICTIM - NON CRITICAL",
       "BLAC",       "LACERATION/CONTROLLED BLEEDING",
       "ATAI",       "ALS TRAFFIC ACCIDENT INJURIES",
       "AHRI",       "ALS HIT & RUN ACCIDENT WITH INJURY",
       "BTAI",       "BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
       "ATRAUM",     "MULTI-SYSTEMS TRAUMA"
   });
 }

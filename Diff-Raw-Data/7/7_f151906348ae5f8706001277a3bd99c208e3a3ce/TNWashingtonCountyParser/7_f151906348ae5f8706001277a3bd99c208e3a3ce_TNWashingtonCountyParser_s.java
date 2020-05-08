 package net.anei.cadpage.parsers.TN;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Johnson City, TN (Washington County)
 Contact: nathan carrier <ncarrier454@gmail.com>
 Contact: david harrison <firemist317@gmail.com>
 Contact: Travis Justice <tcjustice8892@gmail.com>
 Sender: JCFDTEXT@johnsoncitytn.org
 prvs=182efbc0f=JCFDTEXT@johnsoncitytn.org House/Residential Fire E7,E4,L44,E3,MP29\nALLISON LN/HELEN CT\nALLISON PLACE SD\nMap 53C 16:50:40 115143\nThink green: Only print this e-mail
 prvs=1838ca3f5=JCFDTEXT@johnsoncitytn.org GRASS/BRUSH FIRE E4\n208 GARDEN DR\nGRACE TEMPLE CHURCH\nMap 46C 19:57:36 115181
 prvs=184df15ee=JCFDTEXT@johnsoncitytn.org School Fire Alarm\nE4,L44,E3,MP29,E5\n1509 JOHN EXUM PKWY\nSCIENCE HILL HIGH SCHOOL\nMap 46A 02:23:08 115185
 prvs=184df15ee=JCFDTEXT@johnsoncitytn.org Diabetic Problems-CHARLIE M13,E3\n212 W POPLAR ST #APT1\nMap 54B 00:12:17 11111569
 prvs=184df15ee=JCFDTEXT@johnsoncitytn.org Unconscious/Fainting-DELTA M7,E5\n1811 W LAKEVIEW DR #B9\nSUMMERCHASE APARTMENTS\nMap 38B 23:05:56 11111542
 prvs=1838ca3f5=JCFDTEXT@johnsoncitytn.org GRASS/BRUSH FIRE E4\n208 GARDEN DR\nGRACE TEMPLE CHURCH\nMap 46C 19:57:36 115181
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Heart Problems/AICD-DELTA R2,E4\n1806 LAMONS LN #101\nHARRISON VILLAGE APARTMENTS\nMap 54A 12:39:23 11113129
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Nursing Home Fire Alarm\nE5,L45,E9,E6,MP29,L44\n406 E MOUNTAINVIEW RD\nBROADMORE FACILITY\nMap 29C 14:01:23 115275
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Motor Vehicle Crash - Injury\nE2,E3,MP29\nI26W/OKOLONA EXIT\nMap 63C 14:16:38 115276
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Unconscious/Fainting-DELTA\nM2,E7,R2\n2215 N GREENWOOD DR\nMap 54D 14:20:38 11113195
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Stroke(CVA)-CHARLIE M6,E6,R3\n3711 N ROAN ST #E28\nNORTHPLACE APARTMENTS\nMap 29C 16:31:15 11113269
 prvs=1887ea331=JCFDTEXT@johnsoncitytn.org Sick Person-CHARLIE M2,R2,E4\n1208 BAIR VETTE AV\nMap 45C 53B 21:42:56 11113379
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Chest Pain(Non-Traumatic)-DELTA\nM2,R2,E4\n1518 SYLVAN DR\nMap 45C 19:51:57 11113336
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Falls-ALPHA E2\n2802 PLYMOUTH RD\nRAINBOW HOMES\nMap 55D 19:20:41 11113321
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Heart Problems/AICD-DELTA M6,E9,R3\n409 SCENIC OAK DR\nMap 21D 19:07:03 11113313
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-DELTA\nM2,R2,E4\n301 ROYAL CR #79\nCLARK MANOR APARTMENTS\nMap 54A 17:30:11 11113287\nThink green: Only print this e-mail
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-CHARLIE\nM1,E2,R1\nS ROAN ST/UNIVERSITY PKWY\nMap 54B 15:06:18 11113223
 prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Motor Vehicle Crash - Injury\nE2,E3,MP29\nI26W/OKOLONA EXIT\nMap 63C 14:16:38 115276
 prvs=256783305=JCFDTEXT@johnsoncitytn.org Sick Person-ALPHA E4\n840 W MARKET ST\nCVS PHARMACY\nX-STR= CLARK ST\nKNOB CREEK RD\nMap 54A 20:51:54 11149314\nThink green: Only print this e-
 prvs=2677ee746=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-DELTAM2,E2,R2\n1209 COLLEGE HEIGHTS DR\nX-STR= DEAD END\nUNIVERSITY PL\n;EAST OF SOUTHWEST AV\nMap 54C 17:18:07 11155033
 prvs=2677ee746=JCFDTEXT@johnsoncitytn.org Unk Problem(MedicalAlarm)-BRAVO M6,R3,E5\n3207 BRISTOL HY\nCOLONIAL HILL RETIREMENT CENTER\nX-STR= MULBERRY ST\nSHANNON LN\nMap 30D 14:48:16
 prvs=268de3f1a=JCFDTEXT@johnsoncitytn.org Unconscious/Fainting-CHARLIEM2,R1,E3\n1430 S ROAN ST\nWALGREENS DRUG STORES (ROA/UNI)\nX-STR= TERRACE CT\nE HIGHLAND RD\nMap 46C 20:15:55 111
 prvs=268de3f1a=JCFDTEXT@johnsoncitytn.orgOverdose/Ingestion/Poisoning-CHARLIE M13,E3\n110 TERRACE CT #1\nTERRACE COURT APARTMENTS\nX-STR= S ROAN ST\nDEAD END\n;REECE TERRACE APTS\nMap
 prvs=268de3f1a=JCFDTEXT@johnsoncitytn.org Chest Pain(Non-Traumatic)-DELTA M6,R5,E5\n208 STRAWBERRY FIELD DR\nX-STR= SUGAR MILL DR\nBROOKE GREEN DR\n;STRAWBERRY FIELD S/D\nMap 45A 10:58:
 prvs=269c08782=JCFDTEXT@johnsoncitytn.org Breathing Problems/ASTHMA-DELTA E7\n1410 COLONY PARK DR #10\nSOUTHRIDGE APTS BLDG 2\nX-STR= 2300 BK S GREENWOOD DR\nCURVE IN ROAD\nMap 62A 22:
 prvs=269c08782=JCFDTEXT@johnsoncitytn.org Hemorrhage/Lacerations-BRAVO M1,E1,R1\n2307 SARAH ST\nX-STR= DEAD END\nMOSE ST\n;BEHIND BYLO MKT\nMap 39A 00:54:20 11155726\nThink green: Only p
 prvs=268de3f1a=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-CHARLIE M2,R4,E5\n541 SID MARTIN RD\nCITI COMMERCE SOLUTIONS\nX-STR= BOB DAVIS RD\nBOBBY HICKS HY\nMap 12D 13:55:39 11155
 prvs=270a5699b=JCFDTEXT@johnsoncitytn.org School Fire Alarm E7,E2,TR2,E4,TR3,E3\n33 S DOSSETT DR\nSTONE HALL\nWOMENS RESIDENCE HALL\nMap 54D 12:45:10 117235\nThink green: Only print t
 prvs=27112cb81=JCFDTEXT@johnsoncitytn.org Motor Vehicle Crash - Injury E3\nCARTER COUNTY LINE/MILLIGAN HY\nCARTER COUNTY LINE\n11:18:15 117253\nThink green: Only print this e-mail and
 prvs=272df6cbd=JCFDTEXT@johnsoncitytn.org Motor Vehicle Crash - Injury E8,E6\nI26E/MILE MARKER 14\n15:38:04 117287\nThink green: Only print this e-mail and any attachment if necessary
 prvs=2751bae6e=JCFDTEXT@johnsoncitytn.org Assault 442,431,E4\n241 W MAIN ST\nDOWNTOWN APARTMENTS\nCross Streets N BOONE ST\nWHITNEY ST\nW WATAUGA AV 22:50:49\nThink green: Only print th
 prvs=280581d2f=JCFDTEXT@johnsoncitytn.org Breathing Problems/ASTHMA-DELTA M1,E4,R1\n554 WASHINGTON AV\nX-STR= DEAD END\nBELMONT ST\n;TURN RT FROM MAIN ST\nMap 46C 12:13:40 11161605\nThin
 prvs=281dbe8be=JCFDTEXT@johnsoncitytn.org Abdominal Pain/Problems-CHARLIE M2,E7,R2\n2911 CHATHAM DR #3-1\nCHATHAM APTS\nX-STR= NEWTON DR\nCARTER SELLS RD\nMap 53C 12:46:55 11162258\nThi
 prvs=2861b1534=JCFDTEXT@johnsoncitytn.org Chest Pain(Non-Traumatic)-CHARLIE M1,R2,E7\n310 N STATE OF FRANKLIN RD\nMEDICAL OFFICE BUILDING 2\nX-STR= RAIL ROAD OVERPASS/W WALNUT ST\nPRO
 
 prvs=377b8c42e=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-DELTA M3,R3,E5\n138 VIEW BEND RD\nX-STR= WOODSTONE CT\n;WOODSTONE CONDOS\nMap 30D 21:06:59 12019235\nThink green: Only pri
 
 Contact: Jason Powell <firedupleadership@gmail.com>
 Sender: CAD@wc911.org
 Breathing Problems-DELTA M1,E3 \n300 W WALNUT ST #2 \nSHAMROCK \nMap 54b 14:59:59 11111863 \nThink green: Only print this e-mail and any attachment 
 Breathing Problems-CHARLIE M1,E1,R1 \n1 UNAKA CT #7 \nMap 39D 21:23:05 11112056 \nThink green: Only print this e-mail and any attachment if necessar 
 House/Residential Fire Alarm E2 \n2701 S ROAN ST #124 \nHERITAGE TRAILER PARK \nMap 55D 22:50:03 115214 \nThink green: Only print this e-mail and an
 Assault/Sexual Assault-BRAVO M3,R3,E9 \n2918 BRISTOL HY \nADVANCE AUTO PARTS \nMap 38A 19:53:06 11113913 \nThink green: Only print this e-mail and a
 Chest Pain(Non-Traumatic)-DELTA M1,R1,E3\n805 KENTUCKY ST\nX-STR= COLORADO ST\nORLEANS ST\nMap 47D 16:50:49 11147626\nThink green: Only print thi
 
 Contact: Derick Hughes <smokeeater1@gmail.com>
 Sender: cfdtext@johnsoncitytn.org
 prvs=425025d88=jcfdtext@johnsoncitytn.org (<No Subject>) Convulsions/Seizures-CHARLIE M2,E7,R2\n3211 MAYFIELD DR #28\nTIMBERRIDGE TRAILER PARK\nX-STR= NUNLEY LN\nW WALNUT ST\nMap 53C 1
 
 */
 
 public class TNWashingtonCountyParser extends FieldProgramParser {
   
   private static final String EXTRA = "Think green: ";
   private static final Pattern TRAILER = Pattern.compile("\\b(\\d\\d:\\d\\d:\\d\\d)(?: (\\d+))?$");
   private static final Pattern TRAILER2 = Pattern.compile(" +\\d\\d:[\\d:]*$");
   
   private boolean ok = false;
   
   public TNWashingtonCountyParser() {
     super("WASHINGTON COUNTY", "TN",
            "CPU UNIT2? ADDR PLACE+? ( X X2+? | ) MAP");
   }
   
   @Override
   public String getFilter() {
     return "JCFDTEXT@johnsoncitytn.org,CAD@wc911.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Dump trailing comment
     int pt = body.lastIndexOf('\n');
     if (pt < 0) return false;
     String tail = body.substring(pt+1).trim();
     if (tail.startsWith(EXTRA) || EXTRA.startsWith(tail)) {
       body = body.substring(0,pt).trim();
     }
     
     ok = false;
     Matcher match = TRAILER.matcher(body);
     if (match.find()) {
       ok = true;
       data.strTime = match.group(1);
       data.strCallId = getOptGroup(match.group(2));
       body = body.substring(0,match.start());
     }
     String flds[] = body.split("\n");
     if (!ok && flds.length < 4) return false;
     if (!ok) {
       String sMap = flds[flds.length-1];
       if (sMap.startsWith("Map")) {
         ok = true;
         match = TRAILER2.matcher(sMap);
         if (match.find()) {
           flds[flds.length-1] = sMap.substring(0,match.start());
         }
       }
     }
     if (!parseFields(flds, data)) return false;
     return ok;
   }
   
   @Override
   public String getProgram() {
     return super.getProgram() + " TIME ID";
   }
   
   // CPU - Combined Call / Priority / Unit field
  private static final Pattern UNIT_PTN = Pattern.compile("[A-Z0-9]{2,4}(?:,[A-Z0-9]{2,4})*$");
   private static final Pattern PRI_PTN = Pattern.compile("-(?:ALPHA|BRAVO|CHARLIE|DELTA)");
   private class CallPriUnitField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = PRI_PTN.matcher(field);
       if (match.find()) {
         ok = true;
         data.strCall = field.substring(0,match.start()).trim();
         data.strPriority = (match.group().substring(1,2));
         data.strUnit = field.substring(match.end()).trim();
       } else {
         match = UNIT_PTN.matcher(field);
         if (match.find()) {
           data.strUnit = match.group();
           field = field.substring(0,match.start()).trim();
         }
         data.strCall = field;
       }
     }
     
     @Override
     public String getFieldNames() {
       return "CALL PRI UNIT";
     }
   }
   
   // UNIT2 Lists units if there wern'e included in the CPU field
   private class Unit2Field extends UnitField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (data.strUnit.length() > 0) return false;
       Matcher match = UNIT_PTN.matcher(field);
      if (!match.matches()) abort();
       data.strUnit = field;
       return true;
     }
     
     @Override
     public String getFieldNames() {
       return "UNIT";
     }
   }
   
   private class MyPlaceField extends PlaceField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.startsWith("X-STR=") || isMapField(field)) return false;
       data.strPlace = append(data.strPlace, " - ", field);
       return true;
     }
   }
   
   private class MyCrossField extends CrossField {
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (! field.startsWith("X-STR=")) return false;
       ok = true;
       field = field.substring(6).trim();
       super.parse(field, data);
       return true;
     }
   }
   
   private class MyCross2Field extends CrossField {
     @Override
     public boolean checkParse(String field, Data data) {
       if (isMapField(field)) return false;
       parse(field, data);
       return true;
     }
   }
   
   // MAP field has to start with map
   private class MyMapField extends MapField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("Map ")) {
         field = field.substring(4).trim();
         super.parse(field, data);
       } else {
         if (!"Map ".startsWith(field)) abort();
       }
     }
   }
   
   private boolean isMapField(String field) {
     if (field.startsWith("Map ")) return true;
     if ("Map".startsWith(field)) return true;
     return false;
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CPU")) return new CallPriUnitField();
     if (name.equals("UNIT2")) return new Unit2Field();
     if (name.equals("PLACE")) return new MyPlaceField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("X2")) return new MyCross2Field();
     if (name.equals("MAP")) return new MyMapField();
     return super.getField(name);
   }
 }

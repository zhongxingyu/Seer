 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PAErieCountyAParserTest;
 
 import org.junit.Test;
 
 
 public class PAErieCountyAParserTest extends BaseParserTest {
   
   public PAErieCountyAParserTest() {
     setParser(new PAErieCountyAParser(), "ERIE COUNTY", "PA");
   }
 
  
   
   @Test
   public void testParser() {
     doTest("T1",
         "ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 8165 PLATZ RD XS: MARKET RD FAIRVIEW TWP LIST JOHN C Map:2034 Grids:, Cad: 2011-0000044804",
         "SRC:ERIE911",
         "CALL:STRUC FIRE-SINGLE RESIDENTIAL",
         "ADDR:8165 PLATZ RD",
         "X:MARKET RD",
         "NAME:LIST JOHN C",
         "CITY:FAIRVIEW TWP",
         "MAP:2034",
         "ID:2011-0000044804");
     
     doTest("T2",
         "ERIE911:55B2P >ELEC HAZ/PWR REPT DISCONNECTED 7656 MAPLE DR XS: CHESTNUT ST FAIRVIEW TWP MUSANTE, JANET Map:2202 Grids:, Cad: 2011-0000045114",
         "SRC:ERIE911",
         "CALL:ELEC HAZ/PWR REPT DISCONNECTED",
         "ADDR:7656 MAPLE DR",
         "X:CHESTNUT ST",
         "NAME:MUSANTE, JANET",
         "CITY:FAIRVIEW TWP",        
         "MAP:2202",
         "ID:2011-0000045114");
 
     doTest("T3",
         "ERIE911:29B4 >MVA - UNKNOWN STATUS 17 I 90 EB XS: I 90 EB RAMP EXIT 16 FAIRVIEW TWP LORD, ISAAC Map:1888 Grids:, Cad: 2011-0000043981",
         "SRC:ERIE911",
         "CALL:MVA - UNKNOWN STATUS",
         "ADDR:17 I 90 EB",
         "MADDR:17 I 90",
         "X:I 90 EB RAMP EXIT 16",
         "NAME:LORD, ISAAC",
         "CITY:FAIRVIEW TWP",
         "MAP:1888",
         "ID:2011-0000043981");
 
     doTest("T4",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:West Ridge Fire\n" +
         "MSG:10C4 >CHEST PAIN - BREATH NORMAL=>35 5125 W RIDGE RD Apt: 314 Bldg XS:CARLETON CT MILLCREEK TWP\r\n",
 
         "SRC:West Ridge Fire",
         "CALL:CHEST PAIN - BREATH NORMAL=>35",
         "APT:314 Bldg",
         "ADDR:5125 W RIDGE RD",
         "CITY:MILLCREEK TWP",
         "X:CARLETON CT");
   }
   
   @Test
   public void testParser2() {
         
     doTest("T4",
         "ERIE911:29B4 >MVA - UNKNOWN STATUS W LAKE RD&WHITEHALL PL XS: LORD RD FAIRVIEW TWP WOOD, RODNEY Cad: 2011-0000042496",
         "SRC:ERIE911",
         "CALL:MVA - UNKNOWN STATUS",
         "ADDR:W LAKE RD & WHITEHALL PL",
         "X:LORD RD",
         "CITY:FAIRVIEW TWP",
         "NAME:WOOD, RODNEY",
         "ID:2011-0000042496");
         
     doTest("T5",
         "ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 6683 OTTEN CT FAIRVIEW TESTI JULIE Cad: 2011-00000399770",
         "SRC:ERIE911",
         "CALL:STRUC FIRE-SINGLE RESIDENTIAL",
         "ADDR:6683 OTTEN CT",
         "CITY:FAIRVIEW",
         "NAME:TESTI JULIE",
         "ID:2011-00000399770");
         
     doTest("T6",
         "ERIE911:52C3G >FIRE/GENERAL ALARM-COMM STRUC 7301 KLIER DR XS: UNFAIRVIEW FAIRVIEW TWP DAN Map:2302 Grids:, Cad: 2011-0000040143",
         "SRC:ERIE911",
         "CALL:FIRE/GENERAL ALARM-COMM STRUC",
         "ADDR:7301 KLIER DR",
         "X:UNFAIRVIEW",
         "NAME:DAN",
         "MAP:2302",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000040143");
     
     doTest("T7",
         "ERIE911:10D4 >CHEST PAIN 5757 W RIDGE RD XS: MILLFAIR RD FAIRVIEW TWP NICOLE Map:1988 Grids:, Cad: 2011-0000047247",
         "SRC:ERIE911",
         "CALL:CHEST PAIN",
         "ADDR:5757 W RIDGE RD",
         "X:MILLFAIR RD",
         "NAME:NICOLE",
         "MAP:1988",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000047247");
     
     doTest("T8",
         "ERIE911:17D3 >FALLS 7648 WELCANA DR XS: LYNANN LN FAIRVIEW TWP SANDELL, CECELIA Map:2213 Grids:, Cad: 2011-0000047240",
         "SRC:ERIE911",
         "CALL:FALLS",
         "ADDR:7648 WELCANA DR",
         "X:LYNANN LN",
         "NAME:SANDELL, CECELIA",
         "MAP:2213",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000047240");
     
     doTest("T9",
         "ERIE911:26A1 >SICK PERSON 8300 W RIDGE RD XS: DOBLER RD FAIRVIEW TWP WIECZOREK, BOB Map:2185 Grids:, Cad: 2011-0000046184",
         "SRC:ERIE911",
         "CALL:SICK PERSON",
         "ADDR:8300 W RIDGE RD",
         "X:DOBLER RD",
         "NAME:WIECZOREK, BOB",
         "MAP:2185",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000046184");
     
     doTest("T10",
         "ERIE911:13A1 >DIABETIC PROBLEMS 8475 MIDDLE RD XS: BLAIR RD FAIRVIEW TWP SEAN Map:2174 Grids:, Cad: 2011-0000046843",
         "SRC:ERIE911",
         "CALL:DIABETIC PROBLEMS",
         "ADDR:8475 MIDDLE RD",
         "X:BLAIR RD",
         "NAME:SEAN",
         "MAP:2174",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000046843");
     
     doTest("T11",
         "ERIE911:52B1H >RES (SINGLE) HEAT DETECTOR 1530 TAYLOR RIDGE CT FAIRVIEW TWP ADT/DIONNA Map:2540 Grids:, Cad: 2011-0000046825",
         "SRC:ERIE911",
         "CALL:RES (SINGLE) HEAT DETECTOR",
         "ADDR:1530 TAYLOR RIDGE CT",
         "NAME:ADT / DIONNA",
         "MAP:2540",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000046825");
   }
   
   @Test
   public void testDanEdwards() {
 
     doTest("T1",
         "ERIE911:6C1 >BREATHING PROBLEMS 817 POTOMAC AVE XS: W LAKE RD MILLCREEK TWP WATTS, BETTY Map:9214 Grids:, Cad: 2011-0000076275",
         "SRC:ERIE911",
         "CALL:BREATHING PROBLEMS",
         "ADDR:817 POTOMAC AVE",
         "X:W LAKE RD",
         "NAME:WATTS, BETTY",
         "MAP:9214",
         "CITY:MILLCREEK TWP",
         "ID:2011-0000076275");
     
   }
   
   @Test
   public void testMatFuller() {
 
     doTest("T1",
         "ERIE911:HASKINS RD IS NOW OPEN",
         "SRC:ERIE911",
         "CALL:GENERAL ALERT",
         "PLACE:HASKINS RD IS NOW OPEN");
 
     doTest("T2",
         "ERIE911:ACTIVE SHOOTER INCIDENTS - MANDATORY TRAINING - SEPT 6,7 OR 8TH. EIGHT HOUR COURSE. REQUIRED TO ATTEND ONE OF THE DAYS.",
         "SRC:ERIE911",
         "CALL:GENERAL ALERT",
         "PLACE:ACTIVE SHOOTER INCIDENTS - MANDATORY TRAINING - SEPT 6,7 OR 8TH. EIGHT HOUR COURSE. REQUIRED TO ATTEND ONE OF THE DAYS.");
 
     doTest("T3",
         "ERIE911:SAMPSON RD NOW OPEN......",
         "SRC:ERIE911",
         "CALL:GENERAL ALERT",
         "PLACE:SAMPSON RD NOW OPEN......");
 
     doTest("T4",
         "ERIE911:32B1 >UNKNOWN PROBLEM 10793 ETTER RD XS: LAKE PLEASANT RD GREENE TWP DUSILA,CANDY Map:277 Grids:, Cad: 2011-0000090035",
         "SRC:ERIE911",
         "CALL:UNKNOWN PROBLEM",
         "ADDR:10793 ETTER RD",
         "X:LAKE PLEASANT RD",
         "CITY:GREENE TWP",
         "NAME:DUSILA,CANDY",
         "MAP:277",
         "ID:2011-0000090035");
 
     doTest("T5",
         "ERIE911:29D2N2>MVA -EJECTION- HIGH MECHANISM PLUM RD VENANGO TWP ADAM Map:489 Grids:, Cad: 2011-0000096580",
         "SRC:ERIE911",
         "CALL:MVA -EJECTION- HIGH MECHANISM",
         "ADDR:PLUM RD",
         "CITY:VENANGO TWP",
         "NAME:ADAM",
         "MAP:489",
         "ID:2011-0000096580");
 
     doTest("T4",
         "1 of 2:ERIE911:65A >MUTUAL AID/ASSIST OUTSIDE AGEN 328 HIGH ST XS: N PARK ROW WATERFORD BORO CHIEF CHRIS BARBER",
         "SRC:ERIE911",
         "CALL:MUTUAL AID/ASSIST OUTSIDE AGEN",
         "ADDR:328 HIGH ST",
         "CITY:WATERFORD",
         "X:N PARK ROW",
         "NAME:BORO CHIEF CHRIS BARBER");
 
     doTest("T5",
         "ERIE911:1C4 >ABDOM PAIN - FEM 12-50 W/FAINT 9625 MARK RD XS: WATTSBURG RD GREENE TWP WEED DAVID Map:307 Grids:, Cad: 2012-0000006327",
         "SRC:ERIE911",
         "CALL:ABDOM PAIN - FEM 12-50 W/FAINT",
         "ADDR:9625 MARK RD",
         "CITY:GREENE TWP",
         "X:WATTSBURG RD",
         "NAME:WEED DAVID",
         "MAP:307",
         "ID:2012-0000006327");
 
     doTest("T6",
         "ERIE911:29D4A >MVA-TRAPPED MULTI PT/ADD RESPO LAKE PLEASANT RD&TATE RD XS: TATE RD GREENE TWP Cad: 2012-0000020343",
         "SRC:ERIE911",
         "CALL:MVA-TRAPPED MULTI PT/ADD RESPO",
         "ADDR:LAKE PLEASANT RD & TATE RD",
         "CITY:GREENE TWP",
         "X:TATE RD",
         "ID:2012-0000020343");
 
     doTest("T7",
         "ERIE911:ROAD CLOSING ARBUCKLE RD XS: FOOTEMILL RD Cad: 2012-0000043019 ARBUCKLE FROM FOOTMILL TO LAKE PLEASANT RD CLOSED BY STREET DEPT",
         "SRC:ERIE911",
         "CALL:ERIE911: ROAD CLOSING",
         "ADDR:ARBUCKLE RD",
         "MADDR:ARBUCKLE RD & FOOTEMILL RD",
         "X:FOOTEMILL RD",
         "ID:2012-0000043019",
         "INFO:ARBUCKLE FROM FOOTMILL TO LAKE PLEASANT RD CLOSED BY STREET DEPT");
 
     doTest("T8",
         "ERIE911:17B1G >FALLS (GRD/FLR) POSS DANGER BO 9184 OLD WATTSBURG RD XS: HORSESHOE DR GREENE TWP Map:298 Grids:, Cad: 2012-0000045463",
         "SRC:ERIE911",
         "CALL:FALLS (GRD/FLR) POSS DANGER BO",
         "ADDR:9184 OLD WATTSBURG RD",
         "CITY:GREENE TWP",
         "X:HORSESHOE DR",
         "MAP:298",
         "ID:2012-0000045463");
 
     doTest("T9",
         "ERIE911:10D2 >CHEST PAIN DIFF SPEAK BTW BRE 10101 WATTSBURG RD XS: DEWEY RD/DUTCH RD GREENE TWP Map:173 Grids:, Cad: 2012-0000059733",
         "SRC:ERIE911",
         "CALL:CHEST PAIN DIFF SPEAK BTW BRE",
         "ADDR:10101 WATTSBURG RD",
         "CITY:GREENE TWP",
         "X:DEWEY RD / DUTCH RD",
         "MAP:173",
         "ID:2012-0000059733");
 
     doTest("T10",
         ".... (Kuhl Hose Fire) ERIE911:26A6 >SICK PERSON - NAUSEA 10707 WATTSBURG RD XS: MAY RD GREENE TWP",
         "SRC:ERIE911",
         "CALL:SICK PERSON - NAUSEA",
         "ADDR:10707 WATTSBURG RD",
         "CITY:GREENE TWP",
         "X:MAY RD");
  }
   
   @Test
   public void testMatExley() {
 
     doTest("T1",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:21B1 &gt;HEMORRHAGE / LACERATIONS 4242 ASBURY RD XS: THOROUGHBRED LOOP MILLCREEK TWP\n",
         "SRC:West Ridge Fire",
         "CALL:HEMORRHAGE / LACERATIONS",
         "ADDR:4242 ASBURY RD",
         "CITY:MILLCREEK TWP",
         "X:THOROUGHBRED LOOP");
 
     doTest("T2",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:9E1 &gt;CARDIAC/RESP ARREST / DEATH 4411 FOREST GLEN DR XS: W 38TH ST MILLCREEK TWP\n",
         "SRC:West Ridge Fire",
         "CALL:CARDIAC/RESP ARREST / DEATH",
         "ADDR:4411 FOREST GLEN DR",
         "CITY:MILLCREEK TWP",
         "X:W 38TH ST");
 
     doTest("T3",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:28C10G&gt;STROKE (CVA) BREATH NORM &gt; 35 3643 MEADOW DR XS: CAUGHEY RD MILLCREEK TWP\n",
         "SRC:West Ridge Fire",
         "CALL:STROKE (CVA) BREATH NORM > 35",
         "ADDR:3643 MEADOW DR",
         "CITY:MILLCREEK TWP",
         "X:CAUGHEY RD");
 
     doTest("T4",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:29B1 >MVA - WITH INJURIES 4440 ASBURY RD XS: LESTER LN MILLCREEK TWP\r\n",
         "SRC:West Ridge Fire",
         "CALL:MVA - WITH INJURIES",
         "ADDR:4440 ASBURY RD",
         "CITY:MILLCREEK TWP",
         "X:LESTER LN");
 
     doTest("T5",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:West Ridge Fire\n" +
         "MSG:26D1 >SICK PERSON - NOT ALERT 3912 E BOARDWALK PL XS: MEDITERRANEAN PL MILLCREEK TWP\r\n",
 
         "SRC:West Ridge Fire",
         "CALL:SICK PERSON - NOT ALERT",
         "ADDR:3912 E BOARDWALK PL",
         "CITY:MILLCREEK TWP",
         "X:MEDITERRANEAN PL");
 
     doTest("T6",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:West Ridge Fire\n" +
         "MSG:10C2 >CHEST PAIN - CARDIAC HISTORY 3121 PINE TREE TER XS: VILLAGE ST MILLCREEK TWP\r\n",
 
         "SRC:West Ridge Fire",
         "CALL:CHEST PAIN - CARDIAC HISTORY",
         "ADDR:3121 PINE TREE TER",
         "CITY:MILLCREEK TWP",
         "X:VILLAGE ST");
 
     doTest("T7",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:West Ridge Fire\n" +
         "MSG:29D4 >MVA - PINNED / ENTRAPPED GRUBB RD&HERSHEY RD XS: HERSHEY RD MILLCREEK TWP\r\n",
 
         "SRC:West Ridge Fire",
         "CALL:MVA - PINNED",
         "ADDR:& ENTRAPPED GRUBB RD & HERSHEY RD",
         "CITY:MILLCREEK TWP",
         "X:HERSHEY RD");
 
     doTest("T8",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:29D1 >MVA - MAJOR INCIDENT W RIDGE RD&W 23RD ST Cad: 2012-0000059501\r\n\r\n",
         "SRC:West Ridge Fire",
         "CALL:MVA - MAJOR INCIDENT",
         "ADDR:W RIDGE RD & W 23RD ST",
         "ID:2012-0000059501");
   }
 
   @Test
   public void testKmccaslin1() {
 
     doTest("T1",
         "ERIE911:28C2L >STROKE-ABNORMAL BREATH <2HRS 835 LINDEN AVE XS: W LAKE RD MILLCREEK TWP Map:9769 Grids:, Cad: 2012-0000044276",
         "SRC:ERIE911",
         "CALL:STROKE-ABNORMAL BREATH <2HRS",
         "ADDR:835 LINDEN AVE",
         "CITY:MILLCREEK TWP",
         "X:W LAKE RD",
         "MAP:9769",
         "ID:2012-0000044276");
  }
   
   @Test
   public void testMattExley() {
 
     doTest("T1",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:West Ridge Fire\n" +
         "MSG:69C >STRUCTURE FIRE CHARLEY RESPONS 705 POTOMAC AVE XS: W LAKE RD MILLCREEK TWP\r\n",
 
         "SRC:West Ridge Fire",
         "CALL:STRUCTURE FIRE CHARLEY RESPONS",
         "ADDR:705 POTOMAC AVE",
         "CITY:MILLCREEK TWP",
         "X:W LAKE RD");
 
     doTest("T2",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:West Ridge Fire\n" +
         "MSG:10D1 >CHEST PAIN - PATIENT NOT ALERT 4101 RIDGEWOOD DR XS: VISTA DR MILLCREEK TWP\r\n",
 
         "SRC:West Ridge Fire",
         "CALL:CHEST PAIN - PATIENT NOT ALERT",
         "ADDR:4101 RIDGEWOOD DR",
         "CITY:MILLCREEK TWP",
         "X:VISTA DR");
 
   }
   
   public static void main(String[] args) {
     new PAErieCountyAParserTest().generateTests("T1", "SRC CALL ADDR CITY X NAME MAP PLACE ID INFO");
   }
 }

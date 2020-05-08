 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJMorrisCountyParserTest extends BaseParserTest {
   
   public NJMorrisCountyParserTest() {
     setParser(new NJMorrisCountyParser(), "MORRIS COUNTY", "NJ");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "PORZIO BROMBERG & NEWMAN (22) , 100 SOUTHGATE PKY [Morris Twp] (SICK PERSN) - SERIOUS PAIN IN LEGS... UNABLE TO WALK... - E2368 17:14",
         "PLACE:PORZIO BROMBERG & NEWMAN",
         "ADDR:100 SOUTHGATE PKY",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:SERIOUS PAIN IN LEGS... UNABLE TO WALK...",
         "UNIT:E2368");
 
     doTest("T2",
         "4 JARDINE CT [Morris Twp] (UNCONSCN) - 84 YOM UNCONCIOUS IS BREATHING. - E2368 15:37",
         "ADDR:4 JARDINE CT",
         "CITY:Morris Twp",
         "CALL:UNCONSCN",
         "INFO:84 YOM UNCONCIOUS IS BREATHING.",
         "UNIT:E2368");
 
     doTest("T3",
         "340 SPEEDWELL AVE [Morris Plains] (BREATHING) - MORISM FAMILY MEDICINE ASSOC. DR. JOSEPH OFFICE 50 YOF DIFF BREATHING IS ON OXYGEN FULL TIME - 2398 11:25",
         "ADDR:340 SPEEDWELL AVE",
         "CITY:Morris Plains",
         "CALL:BREATHING",
         "INFO:MORISM FAMILY MEDICINE ASSOC. DR. JOSEPH OFFICE 50 YOF DIFF BREATHING IS ON OXYGEN FULL TIME",
         "UNIT:2398");
 
     doTest("T4",
         "26 CLEVELAND AVE [Morris Twp] (SICK PERSN) - 47 YOF W/A FOOT INJURY - E2366 07:57",
         "ADDR:26 CLEVELAND AVE",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:47 YOF W/A FOOT INJURY",
         "UNIT:E2366");
 
     doTest("T5",
         "MORRIS VIEW NURSING HOME (22) , 540 W HANOVER AVE BLDG MORRIS [Morris  Twp] (BLEEDING) - LACERATION HER RIGHT LEG... IN UNIT 1A ROOM 1108... - E2368 07:54",
         "PLACE:MORRIS VIEW NURSING HOME",
         "ADDR:540 W HANOVER AVE",
         "APT:MORRIS",
         "CITY:Morris  Twp",
         "CALL:BLEEDING",
         "INFO:LACERATION HER RIGHT LEG... IN UNIT 1A ROOM 1108...",
         "UNIT:E2368");
 
     doTest("T6",
         "***626 HANOVER 22 [Morris Twp] (SICK PERSN) - MALE NOT FEELING WELL CALLER STATES THAT HE IS WHITE AS A SHEET 2ND TO LAST HOUSE BEFORE RANDOLPH - 2398 04:56",
         "ADDR:626 HANOVER 22",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:MALE NOT FEELING WELL CALLER STATES THAT HE IS WHITE AS A SHEET 2ND TO LAST HOUSE BEFORE RANDOLPH",
         "UNIT:2398");
 
     doTest("T7",
         "35 SHERMAN AVE [Morris Twp] (CHEST PAIN) - 64 YOF CHEST PAIN, JUST HAD DFIB INSTALLED - E2368 21:39",
         "ADDR:35 SHERMAN AVE",
         "CITY:Morris Twp",
         "CALL:CHEST PAIN",
         "INFO:64 YOF CHEST PAIN, JUST HAD DFIB INSTALLED",
         "UNIT:E2368");
 
     doTest("T8",
         "1 MOLLY STARK DR [Morris Twp] (FALL) - CALLER STATED SHE WAS FEALING WEAK AND SHE FELL. SHE HAS A CUT TO HER HEAD AND SHE CANT GET UP. - E2368 14:06",
         "ADDR:1 MOLLY STARK DR",
         "CITY:Morris Twp",
         "CALL:FALL",
         "INFO:CALLER STATED SHE WAS FEALING WEAK AND SHE FELL. SHE HAS A CUT TO HER HEAD AND SHE CANT GET UP.",
         "UNIT:E2368");
 
     doTest("T9",
         "43 W HANOVER AVE [Randolph Twp] (MUTUAL AID) - MUTUAL AID INTO RANDOLPH FOR PSYCH TRANSPORT... NO LIGHTS/SIRENS. - 3281,2398 14:03",
         "ADDR:43 W HANOVER AVE",
         "CITY:Randolph Twp",
         "CALL:MUTUAL AID",
         "INFO:MUTUAL AID INTO RANDOLPH FOR PSYCH TRANSPORT... NO LIGHTS/SIRENS.",
         "UNIT:3281,2398");
 
     doTest("T10",
         "1 BROOK DR S [Morris Plains] (UNCONSCN) - 83 YOM ALTERED MENTAL STATUS - 2398 19:14",
         "ADDR:1 BROOK DR S",
         "CITY:Morris Plains",
         "CALL:UNCONSCN",
         "INFO:83 YOM ALTERED MENTAL STATUS",
         "UNIT:2398");
 
     doTest("T11",
         "MADISON HOTEL (22), 1 CONVENT RD [Morris Twp] (SICK PERSN) - SICK FEMALE IN LOBBY - E2368 19:01",
         "PLACE:MADISON HOTEL",
         "ADDR:1 CONVENT RD",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:SICK FEMALE IN LOBBY",
         "UNIT:E2368");
 
     doTest("T12",
         "CANFIELD RD / PIPPINS WAY [Morris Twp] (OVERDOSE) - INTOXICATED FEMALE, AT LOCATION OF AN MVA - E2368 01:46",
         "ADDR:CANFIELD RD & PIPPINS WAY",
         "CITY:Morris Twp",
         "CALL:OVERDOSE",
         "INFO:INTOXICATED FEMALE, AT LOCATION OF AN MVA",
         "UNIT:E2368");
 
     doTest("T13",
         "UNIT 1D ROOM 1422 MORRIS VIEW NURSING HOME (22), 540 W HANOVER AVE [Morris Twp] (SICK PERSN) - FEMALE WITH ALTERMENTAL STATUS - E2366 09:37",
         "PLACE:UNIT 1D ROOM 1422 MORRIS VIEW NURSING HOME",
         "ADDR:540 W HANOVER AVE",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:FEMALE WITH ALTERMENTAL STATUS",
         "UNIT:E2366");
 
     doTest("T14",
         "ST PAULS CHURCH, MOUNTAIN WAY / HILLVIEW AVE [Morris Plains] (UNCONSCN) - FEMALE PASSED OUT. CONSCIOUS AT THIS TIME - 2398 10:44",
         "PLACE:ST PAULS CHURCH",
         "ADDR:MOUNTAIN WAY & HILLVIEW AVE",
         "CITY:Morris Plains",
         "CALL:UNCONSCN",
         "INFO:FEMALE PASSED OUT. CONSCIOUS AT THIS TIME",
         "UNIT:2398");
 
     doTest("T15",
         "RACEWAY GAS(23), 1701 RT 10 [Morris Plains] (CAR FIRE) - CAR FIRE IN THE FAST LANE - 2399,2398 22:46",
         "PLACE:RACEWAY GAS",
         "ADDR:1701 RT 10",
         "CITY:Morris Plains",
         "CALL:CAR FIRE",
         "INFO:CAR FIRE IN THE FAST LANE",
         "UNIT:2399,2398");
 
     doTest("T16",
         "901 RT 10 [Hanover Twp] (MUTUAL AID) - THE JCC BUILDING FOR A FEMAL WITH A LACERATION TO THE FINGER/ MORRIS MIN MEN - 2398 16:54",
         "ADDR:901 RT 10",
         "CITY:Hanover Twp",
         "CALL:MUTUAL AID",
         "INFO:THE JCC BUILDING FOR A FEMAL WITH A LACERATION TO THE FINGER/ MORRIS MIN MEN",
         "UNIT:2398");
 
     doTest("T17",
         "POST91-7 (MENNEN ARENA), 161 E HANOVER AVE [Morris Twp] (FALL) - IN BACK OF RINK 1 ON RIGHT HAND SIDE. 40 YR OLD FEMALE FELL, AND HURT HER RIGHT KNEE. HEARD CRACK ON FALL. - 2398 10:38",
         "PLACE:POST91-7 (MENNEN ARENA)",
         "ADDR:161 E HANOVER AVE",
         "CITY:Morris Twp",
         "CALL:FALL",
         "INFO:IN BACK OF RINK 1 ON RIGHT HAND SIDE. 40 YR OLD FEMALE FELL, AND HURT HER RIGHT KNEE. HEARD CRACK ON FALL.",
         "UNIT:2398");
 
     doTest("T18",
         "AMERICAN RD / E HANOVER AVE [Morris Plains] (MVA) - 2 CAR MVA, PATIENT COMPLAINING OF HEAD PAIN. HONDA CIVIC VS FORD. VEHICLES IN INTERSECTION - 2398 16:53",
         "ADDR:AMERICAN RD & E HANOVER AVE",
         "CITY:Morris Plains",
         "CALL:MVA",
         "INFO:2 CAR MVA, PATIENT COMPLAINING OF HEAD PAIN. HONDA CIVIC VS FORD. VEHICLES IN INTERSECTION",
         "UNIT:2398");
 
     doTest("T19",
         "LITTLETON RD / COURT RD [Morris Plains] (MVA) - PD REQUEST 1 RIG, AIR BAG DEPLOYMENT - 2398 12:15",
         "ADDR:LITTLETON RD & COURT RD",
         "CITY:Morris Plains",
         "CALL:MVA",
         "INFO:PD REQUEST 1 RIG, AIR BAG DEPLOYMENT",
         "UNIT:2398");
 
     doTest("T20",
         "SUNRISE ASSISTED LIVING (23), 209 LITTLETON RD [Morris Plains] (SICK PERSN) - 1ST FLOOR...CALLER REPORTS THE FEMALE PATIENT IS SHAKING AND SCREAMING. CALLE",
         "PLACE:SUNRISE ASSISTED LIVING",
         "ADDR:209 LITTLETON RD",
         "CITY:Morris Plains",
         "CALL:SICK PERSN",
         "INFO:1ST FLOOR...CALLER REPORTS THE FEMALE PATIENT IS SHAKING AND SCREAMING. CALLE");
 
     doTest("T21",
         "prvs=3175827e30=dispatch@co.morris.nj.us Police Academy Morris Cty, 500 W HANOVER AVE [Parsippany-Troy] (UNCONSCNS) - CONSTRUCTION WORKER STATED HE BLACKED OUT. HE IS SITTING INSID",
         "PLACE:Police Academy Morris Cty",
         "ADDR:500 W HANOVER AVE",
         "CITY:Parsippany-Troy",
         "CALL:UNCONSCNS",
         "INFO:CONSTRUCTION WORKER STATED HE BLACKED OUT. HE IS SITTING INSID");
   }
   
   public static void main(String[] args) {
     new NJMorrisCountyParserTest().generateTests("T21", "PLACE ADDR APT CITY CALL INFO UNIT");
   }
 }

 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYSuffolkCountyDParser;
 
 import org.junit.Test;
 
 
 public class NYSuffolkCountyDParserTest extends BaseParserTest {
   
   public NYSuffolkCountyDParserTest() {
     setParser(new NYSuffolkCountyDParser(), "SUFFOLK COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "UNKNOWN PROBLEM - STANDING, SITTING UP, MOVING OR TALKING code: 32-B-1 at: 3 STANDISH DR c/s: CHIPPENDALE DR d/t: 06/30 00:01:30",
         "CALL:UNKNOWN PROBLEM - STANDING, SITTING UP, MOVING OR TALKING",
         "CODE:32-B-1",
         "ADDR:3 STANDISH DR",
         "X:CHIPPENDALE DR");
 
     doTest("T2",
         "PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - NON SUICIDAL AND ALERT code: 25-A-1 at: 60 N COUNTRY RD c/s: PINE HILL RD d/t: 06/30 19:53:51",
         "CALL:PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - NON SUICIDAL AND ALERT",
         "CODE:25-A-1",
         "ADDR:60 N COUNTRY RD",
         "X:PINE HILL RD");
 
     doTest("T3",
         "SICK - NO PRIORITY SYMPTOMS (COMPLAINT CONDITIONS 2-11 NOT IDENTIFIED) code: 26-A-1 at: 11 BURNETT CT c/s: LOUDEN LP d/t: 06/30 11:27:10",
         "CALL:SICK - NO PRIORITY SYMPTOMS (COMPLAINT CONDITIONS 2-11 NOT IDENTIFIED)",
         "CODE:26-A-1",
         "ADDR:11 BURNETT CT",
         "X:LOUDEN LP");
 
     doTest("T4",
         "BURNS (SCALDS) / EXPLOSION - FIRE ALARM (UNKNOWN SITUATION) code: 7-A-2 at: 11 BRADDOCK CT c/s: STRATHMORE CT d/t: 06/28 19:37:00",
         "CALL:BURNS (SCALDS) / EXPLOSION - FIRE ALARM (UNKNOWN SITUATION)",
         "CODE:7-A-2",
         "ADDR:11 BRADDOCK CT",
         "X:STRATHMORE CT");
 
     doTest("T5",
         "PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - UNKNOWN STATUS / OTHER CODES NOT APPLICABLE code: 25-B-6V at: 99 CLIFF RD c/s: CROOKED OAK RD d/t: 06/26 07:27:08",
         "CALL:PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - UNKNOWN STATUS / OTHER CODES NOT APPLICABLE",
         "CODE:25-B-6V",
         "ADDR:99 CLIFF RD",
         "X:CROOKED OAK RD");
 
     doTest("T6",
         "RESPIRATORY - DIFFICULTY SPEAKING BETWEEN BREATHS code: 6-D-2 at: 640 BELLE TERRE RD c/s: PRINCESS TREE CT d/t: 06/27 15:57:11",
         "CALL:RESPIRATORY - DIFFICULTY SPEAKING BETWEEN BREATHS",
         "CODE:6-D-2",
         "ADDR:640 BELLE TERRE RD",
         "X:PRINCESS TREE CT");
 
     doTest("T7",
         "BLEEDING / LACERATIONS - NOT ALERT code: 21-D-2 at: 70 N COUNTRY RD c/s: PINE HILL RD d/t: 06/24 15:58:51",
         "CALL:BLEEDING / LACERATIONS - NOT ALERT",
         "CODE:21-D-2",
         "ADDR:70 N COUNTRY RD",
         "X:PINE HILL RD");
 
     doTest("T8",
         "MOTOR VEHICLE ACCIDENT - NOT ALERT code: 29-D-5 at: CANAL RD c/s: OSBORNE AV d/t: 06/25 13:15:03",
         "CALL:MOTOR VEHICLE ACCIDENT - NOT ALERT",
         "CODE:29-D-5",
         "ADDR:CANAL RD",
         "X:OSBORNE AV");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "30-D-2 TRAUMATIC INJURY (SPECIFIC)-NOT ALERT at: 6 REDBROOK LN c/s: RANDALL RD & FARMHOUSE LN d/t: 10/09 18:41",
         "CALL:TRAUMATIC INJURY (SPECIFIC)-NOT ALERT",
         "CODE:30-D-2",
         "ADDR:6 REDBROOK LN",
         "X:RANDALL RD & FARMHOUSE LN");
 
     doTest("T2",
         "29-B-1 MOTOR VEHICLE ACCIDENT-INJURIES at:  c/s: MEDFORD RD & MIDDLE COUNTRY RD d/t: 10/09 09:31",
         "CALL:MOTOR VEHICLE ACCIDENT-INJURIES",
         "CODE:29-B-1",
        "ADDR:MEDFORD RD & MIDDLE COUNTRY RD");
 
     doTest("T3",
         "1-C-5 ABDOMINAL PAINS-MALES WITH PAIN ABOVE NAVEL >=35 at: 44 RIDGE RD c/s: MIDDLE COUNTRY RD & SHARON CT d/t: 10/08 14:21",
         "CALL:ABDOMINAL PAINS-MALES WITH PAIN ABOVE NAVEL >=35",
         "CODE:1-C-5",
         "ADDR:44 RIDGE RD",
         "X:MIDDLE COUNTRY RD & SHARON CT");
 
     doTest("T4",
         "17-A-1G FALLS-NOT DANGEROUS BODY AREA at: 15D GUILFORD CT c/s:  & BRIDGEWATER DR d/t: 10/10 09:53",
         "CALL:FALLS-NOT DANGEROUS BODY AREA",
         "CODE:17-A-1G",
         "ADDR:15D GUILFORD CT",
         "X:& BRIDGEWATER DR");
 
     doTest("T5",
         "25-B-6 PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICI-UNKNOWN STATUS / OTHER CODES NOT APPLICA at: 262 EDINBURGH DR c/s: BROWNFIELD DR & KINGSTON ",
         "CALL:PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICI-UNKNOWN STATUS / OTHER CODES NOT APPLICA",
         "CODE:25-B-6",
         "ADDR:262 EDINBURGH DR",
         "X:BROWNFIELD DR & KINGSTON");
 
    doTest("T6",
        "29-B-1 MOTOR VEHICLE ACCIDENT-INJURIES at:  c/s: SB WILLIAM FLOYD PKWY & MORICHES MIDDLE ISLAND RD d/t: 10/27 09:13",
        "CALL:MOTOR VEHICLE ACCIDENT-INJURIES",
        "CODE:29-B-1",
        "ADDR:SB WILLIAM FLOYD PKWY & MORICHES MIDDLE ISLAND RD");

   }
   
   public static void main(String[] args) {
     new NYSuffolkCountyDParserTest().generateTests("T1");
   }
 
 }

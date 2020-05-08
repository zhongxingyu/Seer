 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTTollandCountyParserTest extends BaseParserTest {
   
   public CTTollandCountyParserTest() {
     setParser(new CTTollandCountyParser(), "TOLLAND COUNTY", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(TN Alert) 10 COTTAGE ST / UNIT D Vernon ALS Difficulty Breathing 21:14 Cross Street ST BERNARDS TER / SCHOOL ST 2011-00001770",
         "ADDR:10 COTTAGE ST",
         "APT:UNIT D",
         "CITY:Vernon",
         "CALL:ALS Difficulty Breathing",
         "X:ST BERNARDS TER / SCHOOL ST",
         "ID:2011-00001770");
 
     doTest("T2",
         "[TN Alert]  DOBSON RD / CAMPBELL AVE Vernon Vehicle Accident 2ND AMB  12:59 Cross Street 2011-00001773",
         "ADDR:DOBSON RD & CAMPBELL AVE",
         "CITY:Vernon",
         "CALL:Vehicle Accident 2ND AMB",
         "ID:2011-00001773");
 
     doTest("T3",
         "[TN Alert]  32 FRANKLIN PARK E / Vernon ALS DIFF BREATHING 09:07 Cross Street DEAD END / FRANKLIN ST 2011-00001772",
         "ADDR:32 FRANKLIN PARK E",
         "CITY:Vernon",
         "CALL:ALS DIFF BREATHING",
         "X:DEAD END / FRANKLIN ST",
         "ID:2011-00001772");
 
     doTest("T4",
         "[TN Alert]  140 WINDSORVILLE RD / Vernon BLS back pain 02:21 Cross Street PENFIELD AVE / SEPTIC TREATMENT PLANT 2011-00001771",
         "ADDR:140 WINDSORVILLE RD",
         "CITY:Vernon",
         "CALL:BLS back pain",
         "X:PENFIELD AVE / SEPTIC TREATMENT PLANT",
         "ID:2011-00001771");
 
     doTest("T5",
         "[TN Alert]  DOBSON RD / CAMPBELL AVE Vernon Vehicle Accident 12:59 Cross Street 2011-00001773",
         "ADDR:DOBSON RD & CAMPBELL AVE",
         "CITY:Vernon",
         "CALL:Vehicle Accident",
         "ID:2011-00001773");
 
     doTest("T6",
         "[TN Alert]  95 HARTFORD TPKE / Vernon * Golf Land Vehicle Accident MINOR INJURIES - EVALUATION",
         "ADDR:95 HARTFORD TPKE",
         "CITY:Vernon",
         "CALL:Golf Land Vehicle Accident MINOR INJURIES - EVALUATION");
 
     doTest("T7",
         "[TN Alert]  67 BLUE RIDGE DR / Vernon Tree/Wires Down 14:38 Cross Street HICKORY HL / AUTUMN WOOD LN 2011-00001775",
         "ADDR:67 BLUE RIDGE DR",
         "CITY:Vernon",
         "CALL:Tree/Wires Down",
         "X:HICKORY HL / AUTUMN WOOD LN",
         "ID:2011-00001775");
 
     doTest("T8",
         "(TN Alert) 155 W   MAIN ST 317 / Vernon ALS DIFFICULTY BREATHING 14:03 Cross Street ORCHARD ST, SPRING ST / WARD ST 2011-00001981",
         "ADDR:155 W MAIN ST 317",
         "CITY:Vernon",
         "CALL:ALS DIFFICULTY BREATHING",
         "X:ORCHARD ST, SPRING ST / WARD ST",
         "ID:2011-00001981");
 
     doTest("T9",
         "(TN Alert) 36 GARDNER ST / Warehouse Point Fire Alarm 17:11 Cross Street PLEASANT ST / BRIDGE ST 2011-00000190",
         "ADDR:36 GARDNER ST",
         "CITY:Warehouse Point",
         "CALL:Fire Alarm",
         "X:PLEASANT ST / BRIDGE ST",
         "ID:2011-00000190");
 
     doTest("T10",
         "(TN Alert) 69 BROOKLYN ST / (CLERKS OFC) Vernon BLS SICK- ELDERLY FEMALE  13:20 Cross Street COURT ST / VERNON AVE 2011-00002812",
         "ADDR:69 BROOKLYN ST",
         "APT:(CLERKS OFC)",
         "CITY:Vernon",
         "CALL:BLS SICK- ELDERLY FEMALE",
         "X:COURT ST / VERNON AVE",
         "ID:2011-00002812");
 
     doTest("T11",
         "(TN Alert) 29 BRACE RD / Somers ALS ARM INJURY, BLEEDING 22:42 Cross Street GEORGE WOOD RD / HALL HILL RD 2011-00000821",
         "ADDR:29 BRACE RD",
         "CITY:Somers",
         "CALL:ALS ARM INJURY, BLEEDING",
         "X:GEORGE WOOD RD / HALL HILL RD",
         "ID:2011-00000821");
 
     doTest("T12",
         "(TN Alert) 69 COLTON RD / Somers ALS ? CVA 18:34 Cross Street PARSONS ST / SOKOL RD 2011-00000820",
         "ADDR:69 COLTON RD",
         "CITY:Somers",
         "CALL:ALS ? CVA",
         "X:PARSONS ST / SOKOL RD",
         "ID:2011-00000820");
 
     doTest("T13",
         "(TN Alert) 234 JAMES RD / Ashford BLS Fall 15:25 Cross Street HORSE HILL RD / WESTFORD RD 2011-00001969",
         "ADDR:234 JAMES RD",
         "CITY:Ashford",
         "CALL:BLS Fall",
         "X:HORSE HILL RD / WESTFORD RD",
         "ID:2011-00001969");
 
     doTest("T14",
         "(TN Alert) 140 RIVER RD / Willington * Lyon Manor BLS Commital 06:29 Cross Street DEPOT RD / VILLAGE ST 2011-00000860",
         "ADDR:140 RIVER RD",
         "CITY:Willington",
         "CALL:Lyon Manor BLS Commital",
         "X:DEPOT RD / VILLAGE ST",
         "ID:2011-00000860");
 
     doTest("T15",
         "(TN Alert) 957 N  RIVER RD / Coventry ALS DIFF BREATHING 17:59 Cross Street MERROW RD / GOOSE LN 2012-00000183",
         "ADDR:957 N RIVER RD",
         "CITY:Coventry",
         "CALL:ALS DIFF BREATHING",
         "X:MERROW RD / GOOSE LN",
         "ID:2012-00000183");
 
     doTest("T16",
         "(TN Alert) 34 CAMPBELL AVE / Vernon ALS abdominal pain 01:29 Cross Street DEAD END / DOBSON RD 2012-00001475",
         "ADDR:34 CAMPBELL AVE",
         "CITY:Vernon",
         "CALL:ALS abdominal pain",
         "X:DEAD END / DOBSON RD",
         "ID:2012-00001475");
 
     doTest("T17",
         "[TN Alert] 10 PITKIN RD / Vernon Smoke/Odor Investigation STOP AND SHOP 12:07 Cross Street STOP AND SHOP PLAZA / DOBSON RD 2012-00001471 ",
         "ADDR:10 PITKIN RD",
         "CITY:Vernon",
         "CALL:Smoke/Odor Investigation STOP AND SHOP",
         "X:STOP AND SHOP PLAZA / DOBSON RD",
         "ID:2012-00001471");

    doTest("T18",
        "(TN Alert) 664 SILVER ST / APT B Coventry ALS SICK/ALT.MENT.STAT. 18:38 Cross Street LOVE LN / SOUTH ST 2012-00000205",
        "ADDR:664 SILVER ST",
        "APT:B",
        "CITY:Coventry",
        "CALL:ALS SICK/ALTMENTSTAT",
        "X:LOVE LN / SOUTH ST",
        "ID:2012-00000205");
  }
   
   public static void main(String[] args) {
     new CTTollandCountyParserTest().generateTests("T1", "ADDR APT CITY CALL X ID");
   }
 }

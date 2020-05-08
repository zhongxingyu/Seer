 package net.anei.cadpage.parsers.AL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.AL.ALOzarkParserTest;
 
 import org.junit.Test;
 
 
 public class ALOzarkParserTest extends BaseParserTest {
   
   public ALOzarkParserTest() {
     setParser(new ALOzarkParser(), "OZARK", "AL");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(ALERT!) \"MEDICAL EMERGENCY\"6491 E ANDREWS AVE,REF DIABETIC UNABLE TO EAT & VOIMITTING",
         "CALL:MEDICAL EMERGENCY",
         "ADDR:6491 E ANDREWS AVE",
         "INFO:DIABETIC UNABLE TO EAT & VOIMITTING");
 
     doTest("T2",
         "(ALERT!) SERVICE CALL 192 CEDAR CT OFF PINE AVE OFF NEWTON AVE REF LIFTING ASSISTANCE PER MEDIC 1",
         "CALL:SERVICE CALL",
         "ADDR:192 CEDAR CT",
         "X:PINE AVE & NEWTON AVE",
         "INFO:LIFTING ASSISTANCE PER MEDIC 1");
 
     doTest("T3",
         "(ALERT!) MED EMER 145 PATTERSON OFF E EUFAULA, MALE SUBJECT SEVERAL LAC TO ARM, BLEEDING HEAVILY",
         "CALL:MED EMER",
         "ADDR:145 PATTERSON",
         "X:E EUFAULA",
         "INFO:MALE SUBJECT SEVERAL LAC TO ARM, BLEEDING HEAVILY");
 
     doTest("T4",
         "(ALERT!) DELAY PAGE.DOUGLAS DR OFF E HWY 27,REF SUBJECT STABBED,UNITS STAG FOR OPD",
         "CALL:DELAY PAGE",
         "ADDR:DOUGLAS DR",
         "MADDR:DOUGLAS DR & E HWY 27",
         "X:E HWY 27",
         "INFO:SUBJECT STABBED,UNITS STAG FOR OPD");
 
     doTest("T5",
         "(ALERT!) MEDICAL EMER 135 JUDSON DR OFF WILL LOGAN RD REF PATIENT CONFUSED AND CLAMY",
         "CALL:MEDICAL EMER",
         "ADDR:135 JUDSON DR",
         "X:WILL LOGAN RD",
         "INFO:PATIENT CONFUSED AND CLAMY");
   }
   
   public static void main(String[] args) {
     new ALOzarkParserTest().generateTests("T1", "CALL ADDR APT X INFO");
   }
 }

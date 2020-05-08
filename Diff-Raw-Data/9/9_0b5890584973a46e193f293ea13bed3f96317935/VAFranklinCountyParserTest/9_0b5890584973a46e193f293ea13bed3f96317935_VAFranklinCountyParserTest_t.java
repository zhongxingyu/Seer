 package net.anei.cadpage.parsers;
 
 import org.junit.Test;
 
 
 public class VAFranklinCountyParserTest extends BaseParserTest {
   
   public VAFranklinCountyParserTest() {
     setParser(new VAFranklinCountyParser(), "FRANKLIN COUNTY", "VA");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
         "MAILBOX:S07 EMS-OTHER/DEFINE 18360 VIRGIL H GOODE HWY 124 RMT CFS# 2010-030542 CROSS: SHADY LN/BLACKWATER RIVER" ,
         "SRC:S07",
         "CALL:EMS-OTHER/DEFINE",
         "CITY:Rocky Mount",
         "ADDR:18360 VIRGIL H GOODE HWY 124",
         "X:SHADY LN/BLACKWATER RIVER",
         "ID:2010-030542"
         );
     doTest("T2",
     		"MAILBOX:S07 EMS-CHEST PAIN 413 WOODDALE DR RMT CFS# 2010-030355 CROSS: VIRGIL H GOODE HWY/DEAD END",
         "SRC:S07",
     		"CALL:EMS-CHEST PAIN",
     		"CITY:Rocky Mount",
     		"ADDR:413 WOODDALE DR",
     		"X:VIRGIL H GOODE HWY/DEAD END",
     		"ID:2010-030355"
     	);
     doTest("T3",
     		"MAILBOX:S07 EMS-PATIENT FALLEN 3005 GREEN LEVEL RD RMT CFS# 2010-030541 CROSS: GRASSY HILL RD/LITTLE MOUNTAIN DR",
         "SRC:S07",
     		"CALL:EMS-PATIENT FALLEN",
     		"CITY:Rocky Mount",
     		"ADDR:3005 GREEN LEVEL RD",
     		"X:GRASSY HILL RD/LITTLE MOUNTAIN DR",
     		"ID:2010-030541"
     	);
     doTest("T4",
     		"MAILBOX:S07 EMS-CARDIAC VIRGIL H GOODE HWY & LINK ST RMT CFS# 2010-030580",
         "SRC:S07",
     		"CALL:EMS-CARDIAC",
     		"CITY:Rocky Mount",
     		"ADDR:VIRGIL H GOODE HWY & LINK ST",
     		"X:",
     		"ID:2010-030580"
     	);
     doTest("T5",
     		"MAILBOX:S07 EMS-HIGH BLOOD PRESSURE 1808 BETHLEHEM RD BML CFS# 2010-030643 CROSS: BETHANY RD/DILLONS MILL RD",
         "SRC:S07",
     		"CALL:EMS-HIGH BLOOD PRESSURE",
     		"CITY:Boones Mill",
     		"ADDR:1808 BETHLEHEM RD",
     		"X:BETHANY RD/DILLONS MILL RD",
     		"ID:2010-030643"
     	);
    doTest("T6",
        "MAILBOX:C07 FIRE-ALARM COMMERCIAL 3325 GRASSY HILL RD RMT CFS# 2010-033866 CROSS: JAMESTOWN RD/CALLAWAY RD",
        "SRC:C07",
        "CALL:FIRE-ALARM COMMERCIAL",
        "CITY:Rocky Mount",
        "ADDR:3325 GRASSY HILL RD",
        "X:JAMESTOWN RD/CALLAWAY RD",
        "ID:2010-033866"
        );
     
   }   
 }
   

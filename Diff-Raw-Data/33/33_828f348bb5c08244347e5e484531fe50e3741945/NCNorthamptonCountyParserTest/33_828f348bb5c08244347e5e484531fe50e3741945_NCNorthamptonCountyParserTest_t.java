 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCNorthamptonCountyParserTest extends BaseParserTest {
   
   public NCNorthamptonCountyParserTest() {
     setParser(new NCNorthamptonCountyParser(), "NORTHAMPTON COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "S: M:Northampton911:* URIAH MARTIN RD // NEAR CEMETERY* * * CONWAY* * FIRE - BRUSH* * * * EMS3,FS20* * Medical: No* Hazards: No* * ",
         "ADDR:URIAH MARTIN RD",
         "MADDR:NEAR CEMETERY,URIAH MARTIN RD",
         "CITY:CONWAY",
         "PLACE:NEAR CEMETERY",
         "CALL:FIRE - BRUSH",
         "UNIT:EMS3,FS20",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T2",
         "S: M:Northampton911:* BOAT LANDING* * * WELDON* * MISSING PERS* * * * EMS8,FS20* * Medical: No* Hazards: No* * ",
         "ADDR:BOAT LANDING",
         "CITY:WELDON",
         "CALL:MISSING PERS",
         "UNIT:EMS8,FS20",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T3",
         "S: M:Northampton911:* 907 HORNE RD* * * PENDLETON* * FIRE - SMOKE* * * * EMS3,FS18,FS20* * Medical: No* Hazards: No* * ",
         "ADDR:907 HORNE RD",
         "CITY:PENDLETON",
         "CALL:FIRE - SMOKE",
         "UNIT:EMS3,FS18,FS20",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T4",
         "S: M:Northampton911:* 203 WHITE ST* * * CONWAY* * ODOR OF GAS* * * * FS20* * Medical: No* Hazards: No* * ",
         "ADDR:203 WHITE ST",
         "CITY:CONWAY",
         "CALL:ODOR OF GAS",
         "UNIT:FS20",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T5",
         "S: M:Northampton911:* 113 ARBORVITAE CIR* * * LITTLETON* * ASSISTANCE* * * * FS11* * * * * ",
         "ADDR:113 ARBORVITAE CIR",  // Not mapping
         "CITY:LITTLETON",
         "CALL:ASSISTANCE",
         "UNIT:FS11");

    doTest("T6",
        "S: M:Northampton911:* 125 FIRMAN MYRICK ST* * * GASTON* * FIGHT* * * * 475,EMS8* * * * * ",
        "ADDR:125 FIRMAN MYRICK ST",
        "CITY:GASTON",
        "CALL:FIGHT",
        "UNIT:475,EMS8");
   }
 
   public static void main(String[] args) {
     new NCNorthamptonCountyParserTest().generateTests("T1");
   }
 }

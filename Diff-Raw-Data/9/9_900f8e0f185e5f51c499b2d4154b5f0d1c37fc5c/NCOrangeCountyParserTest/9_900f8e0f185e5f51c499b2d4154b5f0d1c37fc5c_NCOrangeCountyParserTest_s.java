 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCOrangeCountyParserTest extends BaseParserTest {
   
   public NCOrangeCountyParserTest() {
     setParser(new NCOrangeCountyParser(), "ORANGE COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(CAD Page for CFS 060911-335) OPS \nTRNG/WRK SESSION\nTRNG AT 1900 HRS\nApt: \nBuild:",
         "ID:060911-335",
         "CH:OPS",
         "CALL:TRNG/WRK SESSION",
         "ADDR:TRNG AT 1900 HRS");
 
     doTest("T2",
         "(CAD Page for CFS 060511-228) OPS 2\nAuto Fire Alarm\n6514 DOC CORBETT RD\nApt: \nBuild:",
         "ID:060511-228",
         "CH:OPS 2",
         "CALL:Auto Fire Alarm",
         "ADDR:6514 DOC CORBETT RD");
 
     doTest("T3",
         "(CAD Page for CFS 060411-348) OPS 1\nBREATHING\n8100 NC 86 N\nApt: \nBuild:",
         "ID:060411-348",
         "CH:OPS 1",
         "CALL:BREATHING",
         "ADDR:8100 NC 86 N");
 
     doTest("T4",
         "(CAD Page for CFS 060311-397) OPS 1\nTRAUMATIC INJURY\nMILL CREEK RD and WAXSTAFF RD\nApt: \nBuild:",
         "ID:060311-397",
         "CH:OPS 1",
         "CALL:TRAUMATIC INJURY",
         "ADDR:MILL CREEK RD and WAXSTAFF RD",
         "MADDR:MILL CREEK RD & WAXSTAFF RD");
 
     doTest("T5",
         "(CAD Page for CFS 052911-170) OPS 1\nTraffic Accident-PI\nMCDADE STORE RD and NC 86 N\nApt: \nBuild:",
         "ID:052911-170",
         "CH:OPS 1",
         "CALL:Traffic Accident-PI",
         "ADDR:MCDADE STORE RD and NC 86 N",
         "MADDR:MCDADE STORE RD & NC 86 N");
 
     doTest("T6",
         "(CAD Page for CFS 051311-370) OPS \nINFO FOR ALL UNITS\n643 tanker back in service\nApt: \nBuild:",
         "ID:051311-370",
         "CH:OPS",
         "CALL:INFO FOR ALL UNITS",
         "ADDR:643 tanker back in service");
 
     doTest("T7",
         "Subject:CAD Page for CFS 082311-84\nOPS 1\nUNCONSCIOUS\n412 N ELLIOTT RD\nApt: \nBuild: \n",
         "ID:082311-84",
         "CH:OPS 1",
         "CALL:UNCONSCIOUS",
         "ADDR:412 N ELLIOTT RD");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(CAD Page for CFS 111611-425) TG: OPS 1\nINC: \nLOC: 1098 BURNING TREE DR\nAPT: \nBLDG:",
         "ID:111611-425",
         "CH:OPS 1",
         "ADDR:1098 BURNING TREE DR");
 
     doTest("T2",
         "(CAD Page for CFS 111911-222) TG: OPS 1\nINC: BREATHING\nLOC: 1508 JO MAC RD\nAPT: \nBLDG:",
         "ID:111911-222",
         "CH:OPS 1",
         "CALL:BREATHING",
         "ADDR:1508 JO MAC RD");
 
     doTest("T3",
         "Subject:CAD Page for CFS 112211-111\nTG: OPS \nINC: Auto Fire Alarm\nLOC: 511 HIGHGROVE DR\nAPT: \nBLDG: \n",
         "ID:112211-111",
         "CH:OPS",
         "CALL:Auto Fire Alarm",
         "ADDR:511 HIGHGROVE DR");
    
   }
   
 
   public static void main(String[] args) {
     new NCOrangeCountyParserTest().generateTests("T1");
   }
 }

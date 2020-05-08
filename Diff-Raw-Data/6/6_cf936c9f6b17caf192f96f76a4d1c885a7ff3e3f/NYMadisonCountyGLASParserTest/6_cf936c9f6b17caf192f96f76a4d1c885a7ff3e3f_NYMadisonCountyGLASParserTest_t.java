 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYMadisonCountyGLASParserTest extends BaseParserTest {
   
   public NYMadisonCountyGLASParserTest() {
     setParser(new NYMadisonCountyGLASParser(), "MADISON COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n7738 WISE RD , LENOX ( / N COURT)",
         "CALL:Sick Person",
         "ADDR:7738 WISE RD",
         "CITY:LENOX",
         "X:N COURT");
 
     doTest("T2",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Assist\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Assist",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T3",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Heart Problem\n@MADISON COUNTY DSS (133 NORTH COURT ST (WAMPSVILLE VIL",
         "CALL:Heart Problem",
         "ADDR:@MADISON COUNTY DSS",
         "MADDR:@MADISON COUNTY DSS & 133 NORTH COURT ST (WAMPSVILLE VIL",
         "X:133 NORTH COURT ST (WAMPSVILLE VIL");
 
     doTest("T4",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Convulsions/Seizures\n7216 NELSON RD , LENOX (SENECA TRNPK / PAVONE PL)",
         "CALL:Convulsions/Seizures",
         "ADDR:7216 NELSON RD",
         "CITY:LENOX",
         "X:SENECA TRNPK / PAVONE PL");
 
     doTest("T5",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Sick Person",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T6",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Chest Pain\n123 CAYUGA AV , SULLIVAN ( ONEIDA LAKE AV / ROUTE 31)",
         "CALL:Chest Pain",
         "ADDR:123 CAYUGA AV",
         "MADDR:123 CAYUGA AVE",
         "CITY:SULLIVAN",
         "X:ONEIDA LAKE AV / ROUTE 31");
 
     doTest("T7",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n7885 TACKABURY RD , LENOX ( DITCH BANK RD / INDIAN OPENING RD)",
         "CALL:Sick Person",
         "ADDR:7885 TACKABURY RD",
         "CITY:LENOX",
         "X:DITCH BANK RD / INDIAN OPENING RD");
 
     doTest("T8",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG: Chest Pain\n3881 COTTONS RD , LINCOLN ( CLOCKVILLE RD / NELSON RD)",
         "CALL:Chest Pain",
         "ADDR:3881 COTTONS RD",
         "CITY:LINCOLN",
         "X:CLOCKVILLE RD / NELSON RD");
 
     doTest("T9",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Personal Injury\n@MM 261.7 (261 70 I90 )",
         "CALL:MVA - Personal Injury",
         "ADDR:@MM 261.7",
         "MADDR:@MM 261.7 & 261 70 I 90",
         "X:261 70 I90");
 
     doTest("T10",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\nRAILROAD \\ DEPOT (, CANASTOTA VILLAGE)",
         "CALL:MVA - Unknown",
         "ADDR:RAILROAD \\ DEPOT",
         "CITY:CANASTOTA VILLAGE");
 
     doTest("T11",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Sick Person",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T12",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Personal Injury\n5050 BURLESON RD , LINCOLN ( VEDDER RD / FOREST AV)",
         "CALL:MVA - Personal Injury",
         "ADDR:5050 BURLESON RD",
         "CITY:LINCOLN",
         "X:VEDDER RD / FOREST AV");
 
     doTest("T13",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Falls\n400 LAMB AV #144, CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Falls",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "APT:144",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T14",
         "(Greater Lenox) Convulsions/Seizures\n7216 NELSON RD , LENOX ( SENECA TRNPK / PAVONE PL)",
         "CALL:Convulsions/Seizures",
         "ADDR:7216 NELSON RD",
         "CITY:LENOX",
         "X:SENECA TRNPK / PAVONE PL");
 
    doTest("T15",
        "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\nCANAL RD , LENOX",
        "CALL:MVA - Unknown",
        "ADDR:CANAL RD",
        "CITY:LENOX");

   }
   
   public static void main(String[] args) {
     new NYMadisonCountyGLASParserTest().generateTests("T1", "CALL ADDR APT CITY X");
   }
 }

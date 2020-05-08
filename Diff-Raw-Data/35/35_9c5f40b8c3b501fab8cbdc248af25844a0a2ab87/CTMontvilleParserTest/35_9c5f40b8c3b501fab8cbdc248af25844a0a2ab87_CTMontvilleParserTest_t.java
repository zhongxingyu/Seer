 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTMontvilleParserTest extends BaseParserTest {
   
   public CTMontvilleParserTest() {
     setParser(new CTMontvilleParser(), "MONTVILLE", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Imc Solutions Page) MOTOR VEHICLE ACCIDENT\n" +
         "CUMBERLAND FARMS\n" +
         "863 NORWICH NEW LONDON TPKE \n" +
         "MONTVILLE\n" +
         "Fire District: MFD1\n" +
         "5/24/2012 18:17",
 
         "CALL:MOTOR VEHICLE ACCIDENT",
         "NAME:CUMBERLAND FARMS",
         "ADDR:863 NORWICH NEW LONDON TPKE",
         "CITY:MONTVILLE",
         "UNIT:MFD1",
         "DATE:5/24/2012",
         "TIME:18:17");
 
     doTest("T2",
         "(Imc Solutions Page) STRUCTURE FIRE\nVILLAGE APARTMENTS, LLC\n82 JEROME RD \nMONTVILLE\nFire District: MFD1\n6/2/2012 16:36",
         "CALL:STRUCTURE FIRE",
         "NAME:VILLAGE APARTMENTS, LLC",
         "ADDR:82 JEROME RD",
         "CITY:MONTVILLE",
         "UNIT:MFD1",
         "DATE:6/2/2012",
         "TIME:16:36");
 
     doTest("T3",
         "(Imc Solutions Page) ASSIST IN TOWN\r\n" +
         "ORCHARD GROVE SPECIALTY CARE\r\n" +
         "5 RICHARD BROWN DR \r\n" +
         "MONTVILLE\r\n" +
         "Fire District: MOH1\r\n" +
         "6/6/2012 21:29",
 
         "CALL:ASSIST IN TOWN",
         "PLACE:ORCHARD GROVE SPECIALTY CARE",
         "ADDR:5 RICHARD BROWN DR",
         "CITY:MONTVILLE",
         "UNIT:MOH1",
         "DATE:6/6/2012",
         "TIME:21:29");
 
     doTest("T4",
         "(Imc Solutions Page) AMBULANCE EMERGENCY\r\nAZEVEDO, BRIAN\r\n9 SALTBOX CIR \r\nCOLD RESPONSE\r\nLEG PAIN / NUMBNESS",
         "CALL:AMBULANCE EMERGENCY",
         "NAME:AZEVEDO, BRIAN",
         "ADDR:9 SALTBOX CIR",
         "INFO:COLD RESPONSE / LEG PAIN / NUMBNESS");
 
     doTest("T5",
         "(Imc Solutions Page) ASSIST IN TOWN\r\nGOSS, DAVID M.\r\n6 CRANDALL HILL RD APT A\r\nCOLD RESPONSE\r\nLEG PAIN / NUMBNESS",
         "CALL:ASSIST IN TOWN",
         "NAME:GOSS, DAVID M.",
         "ADDR:6 CRANDALL HILL RD",
         "APT:A",
         "INFO:COLD RESPONSE / LEG PAIN / NUMBNESS");
 
     doTest("T6",
         "(Imc Solutions Page) AMBULANCE EMERGENCY\r\nMARKHAM, ELIZABETH\r\n3 POLLY'S LN \r\nMONTVILLE\r\nFire District: MOH1\r\n7/28/2012 22:32",
         "CALL:AMBULANCE EMERGENCY",
         "NAME:MARKHAM, ELIZABETH",
         "ADDR:3 POLLY'S LN",
         "CITY:MONTVILLE",
         "UNIT:MOH1",
         "DATE:7/28/2012",
         "TIME:22:32");

    doTest("T7",
        "(Imc Solutions Page) AMBULANCE EMERGENCY\r\n" +
        "9 MAPLE AVE EXT APT 2\r\n" +
        "55 YOM DIZZYNESS, QUESTION HIGH BLOOD PRESSURE.\r\n" +
        "HOT\r\n" +
        "Fire District: MFD1\r\n" +
        "9/11/2012 14:41",

        "CALL:AMBULANCE EMERGENCY",
        "ADDR:9 MAPLE AVE EXT",
        "MADDR:9 MAPLE AVE",
        "APT:2",
        "INFO:55 YOM DIZZYNESS, QUESTION HIGH BLOOD PRESSURE. / HOT",
        "UNIT:MFD1",
        "DATE:9/11/2012",
        "TIME:14:41");

  }
   
   public static void main(String[] args) {
     new CTMontvilleParserTest().generateTests("T1");
   }
 }

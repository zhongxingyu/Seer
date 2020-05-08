 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PAElkCountyParser;
 
 import org.junit.Test;
 
 
 public class PAElkCountyParserTest extends BaseParserTest {
   
   public PAElkCountyParserTest() {
     setParser(new PAElkCountyParser(), "ELK COUNTY", "PA");
   }
   
   @Test
   public void testElkCounty() {
     
     doTest("T1",
           "Inc: DIABETIC PROBLEMS Add: 890 BURNING WELL RD\nCity: JONES\nXSt: OLD KANE RD * ROCKY RUN RD\nAgency: ST MARYS AMB",
           "CALL:DIABETIC PROBLEMS",
           "ADDR:890 BURNING WELL RD",
           "CITY:JONES",
           "X:OLD KANE RD * ROCKY RUN RD",
           "SRC:ST MARYS AMB");
 
     doTest("T2",
           "Inc: FALLS Add: 185 CENTER ST\nCity: ST_MARYS\nXSt: N SAINT MARYS ST * MCGILL ST\nAgency: ST MARYS AMB", 
           "CALL:FALLS",
           "ADDR:185 CENTER ST",
           "CITY:ST MARYS",
           "X:N SAINT MARYS ST * MCGILL ST",
           "SRC:ST MARYS AMB");
     
     doTest("T3",
           "Inc: SICK PERSON Add: 352 STATE ST\nCity: ST_MARYS\nXSt: ANTHONY RD * RIDGWAY ST MARYS RD\nAgency: ST MARYS AMB", 
           "CALL:SICK PERSON",
           "ADDR:352 STATE ST",
           "CITY:ST MARYS",
           "X:ANTHONY RD * RIDGWAY ST MARYS RD",
           "SRC:ST MARYS AMB");
  }
  
  @Test
  public void testMikeHealy() {

    doTest("T1",
        "Inc: ELEC HAZ-ELECTRICAL HAZ-WATER Add: 1449 SHAWMUT RD \n" +
        "City: HORTON \n" +
        "XSt: ROUTE 219 * ROCKY LN \n" +
        "Agency: ELK COMPANY 1 FD \n\n" +
        "5/29/2012 4:03:58 PM",

        "CALL:ELEC HAZ-ELECTRICAL HAZ-WATER",
        "ADDR:1449 SHAWMUT RD",
        "CITY:HORTON",
        "X:ROUTE 219 * ROCKY LN",
        "SRC:ELK COMPANY 1 FD",
        "DATE:5/29/2012",
        "TIME:16:03:58");
   
   }
   
   public static void main(String[] args) {
     new PAElkCountyParserTest().generateTests("T1", "CALL ADDR CITY X SRC");
   }
 }

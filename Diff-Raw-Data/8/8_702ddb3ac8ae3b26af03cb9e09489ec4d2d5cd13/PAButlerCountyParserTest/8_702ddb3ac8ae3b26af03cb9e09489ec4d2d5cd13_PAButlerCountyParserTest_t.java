 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PAButlerCountyParserTest extends BaseParserTest {
   
   public PAButlerCountyParserTest() {
     setParser(new PAButlerCountyParser(), "BUTLER COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "\".\"@butlerco.911 :ALAF >ALARM/FIRE 20436 ROUTE 19 CRANBERRY TWP GUARDIAN Map: Grids:00000,000 Cad: 2011-0000075272",
         "CALL:ALARM/FIRE",
         "ADDR:20436 ROUTE 19",
         "CITY:CRANBERRY TWP",
         "NAME:GUARDIAN",
         "ID:2011-0000075272");
 
     doTest("T2",
         "\".\"@butlerco.911 :CO3 >CO DETECTOR / FIRE RESPONSE 437 SETTLERS VILLAGE CIR CRANBERRY TWP ALEJANDRO, GABRIEL Map: Grids:00000,000 Cad: 2011-0000074937",
         "CALL:CO DETECTOR/FIRE RESPONSE",
         "ADDR:437 SETTLERS VILLAGE CIR",
         "CITY:CRANBERRY TWP",
         "NAME:ALEJANDRO, GABRIEL",
         "ID:2011-0000074937");
 
     doTest("T3",
         "\".\"@butlerco.911 :SERV >SERVICE CALL 143 FOX RUN RD CRANBERRY TWP CHEETHAM WILLIAM Map: Grids:00000,000 Cad: 2011-0000075245",
         "CALL:SERVICE CALL",
         "ADDR:143 FOX RUN RD",
         "CITY:CRANBERRY TWP",
         "NAME:CHEETHAM WILLIAM",
         "ID:2011-0000075245");
 
     doTest("T4",
         "\".\"@butlerco.911 :FIRST >FIRE - STRUCTURE 20036 ROUTE 19 CRANBERRY TWP CANDLEWOOD EXTENDED STAY Map: Grids:00000,000 Cad: 2011-0000074503",
         "CALL:FIRE - STRUCTURE",
         "ADDR:20036 ROUTE 19",
         "CITY:CRANBERRY TWP",
         "NAME:CANDLEWOOD EXTENDED STAY",
         "ID:2011-0000074503");

    doTest("T5",
        "\".\"@co.butler.pa.us :ALAF >ALARM/FIRE 20620 ROUTE 19 CRANBERRY TWP RAMPART Map: Grids:00000,000 Cad: 2012-0000006337",
        "CALL:ALARM/FIRE",
        "ADDR:20620 ROUTE 19",
        "CITY:CRANBERRY TWP",
        "NAME:RAMPART",
        "ID:2012-0000006337");
   }
   
   public static void main(String[] args) {
     new PAButlerCountyParserTest().generateTests("T1", "CALL ADDR CITY X PLACE NAME PHONE MAP ID");
   }
 }

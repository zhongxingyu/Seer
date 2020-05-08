 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class VAAugustaCountyParserTest extends BaseParserTest {
   
   public VAAugustaCountyParserTest() {
     setParser(new VAAugustaCountyParser(), "AUGUSTA COUNTY", "VA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD: Chest Pain:81 Fountain Cave RD:RT 605 - Wonderly / 10203: Cave Hill LN:Rockfish RD",
         "CALL:Chest Pain",
         "ADDR:81 Fountain Cave RD",
         "MAP:102-03",
         "X:Cave Hill LN & Rockfish RD");
 
     doTest("T2",
         "CAD: OB EMERGENCY:WEYERS CAVE SHELL/ TEXACO:149 WEYERS CAVE RD:P O BOX 184:TRIANGLE DR:SR 222:CNTY-1",
         "CALL:OB EMERGENCY",
         "PLACE:WEYERS CAVE SHELL/ TEXACO",
         "ADDR:149 WEYERS CAVE RD",
         "X:TRIANGLE DR & SR 222",
         "INFO:CNTY-1");
 
     doTest("T3",
         "CAD:LANDING ZONE:BEVERLEY MANOR ELEMENTARY SCH:116 CEDAR GREEN RD:STEVENS LN:PARKERSBURG TPKE",
         "CALL:LANDING ZONE",
         "PLACE:BEVERLEY MANOR ELEMENTARY SCH",
         "ADDR:116 CEDAR GREEN RD",
         "X:STEVENS LN & PARKERSBURG TPKE");
 
     doTest("T4",
         "CAD:AUTOMOBILE ACCIDENT:1519 PARKERSBURG TPKE:159-03:NUCKOLES LN:EIDSON HILL LN:CNTY-2",
         "CALL:AUTOMOBILE ACCIDENT",
         "ADDR:1519 PARKERSBURG TPKE",
         "MAP:159-03",
         "X:NUCKOLES LN & EIDSON HILL LN",
         "INFO:CNTY-2");
 
     doTest("T5",
         "CAD:CONTROL BURN INVESTIGATION:300-BLK TRINITY POINT RD:PARKERSBURG TPKE:MILLERS HOLLOW LN:CNTY-2",
         "CALL:CONTROL BURN INVESTIGATION",
         "ADDR:300-BLK TRINITY POINT RD",
         "MADDR:300 TRINITY POINT RD",
         "X:PARKERSBURG TPKE & MILLERS HOLLOW LN",
         "INFO:CNTY-2");
 
     doTest("T6",
         "CAD:AUTOMOBILE ACCIDENT:3426 MORRIS MILL RD:AREA OF:GALLOPING HILLS RD:SHELLEY LN",
         "CALL:AUTOMOBILE ACCIDENT",
         "PLACE:AREA OF",
         "ADDR:3426 MORRIS MILL RD",
         "X:GALLOPING HILLS RD & SHELLEY LN");
 
     doTest("T7",
         "CAD:ELECTRICAL FIRE - DLOC:135 CRAWFORD DR:CRAWFORD MANOR - 11104:MADISON DR:WAYBURN ST:CNTY-",
         "CALL:ELECTRICAL FIRE - DLOC",
         "ADDR:135 CRAWFORD DR",
         "MAP:111-04",
         "X:MADISON DR & WAYBURN ST",
         "INFO:CNTY-");
 
     doTest("T8",
         "CAD:CONTROL BURN INVESTIGATION:3445 OLD GREENVILLE RD:RT 613 / 245-04:GLORY LN:CHESTNUT RIDGE RD:CNTY-2",
         "CALL:CONTROL BURN INVESTIGATION",
         "ADDR:3445 OLD GREENVILLE RD",
         "MAP:245-04",
         "X:GLORY LN & CHESTNUT RIDGE RD",
         "INFO:CNTY-2");
 
     doTest("T9",
         "CAD:AUTOMOBILE ACCIDENT:I81NB;AREA MM 217.9",
         "CALL:AUTOMOBILE ACCIDENT",
         "ADDR:I-81NB",
         "MADDR:AREA MM 217.9,I 81",
         "PLACE:AREA MM 217.9");
   }
   
   // @Test
   public void testFixLater() {
 
     doTest("T10",
         "CAD:AUTOMOBILE ACCIDENT:I81NB AREA MM 217.9",
         "CALL:AUTOMOBILE ACCIDENT",
         "ADDR:I-81 PLACE:AREA MM 217.9",
         "MADDR:I 81 PLACE:AREA MM 217.9");
     
   }
   
   public static void main(String[] args) {
     new VAAugustaCountyParserTest().generateTests("T1");
   }
 }

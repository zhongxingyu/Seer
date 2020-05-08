 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCMooreCountyParserTest extends BaseParserTest {
   
   public NCMooreCountyParserTest() {
     setParser(new NCMooreCountyParser(), "MOORE COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[[911 NOTIFICATION] ]  505 PINEHURST ST ABERDEEN 2011024766 19:52:16 F69 STRUCTURE FIRE LOOKS LIKE BACK DECK ON FIRE",
         "ADDR:505 PINEHURST ST",
         "CITY:ABERDEEN",
         "ID:2011024766",
         "TIME:19:52:16",
         "UNIT:F69",
         "INFO:STRUCTURE FIRE LOOKS LIKE BACK DECK ON FIRE");
 
     doTest("T2",
         "[[911 NOTIFICATION] ]  1 S. VINELAND SOUTH CELL TOWER 2011025029 09:31:20 M25 PSYCHIATRIC/SUICIDE ATTEMPT",
         "ADDR:1 S VINELAND SOUTH CELL TOWER",
         "ID:2011025029",
         "TIME:09:31:20",
         "UNIT:M25",
         "INFO:PSYCHIATRIC/SUICIDE ATTEMPT");
 
     doTest("T3",
         "[[911 NOTIFICATION] ]  126 ROBIN HOOD LN ABERDEEN 2011025065 13:37:27 F52 ALARM-FIRE ACTIVATION AC 800 932-3822 OPER SP8",
         "ADDR:126 ROBIN HOOD LN",
         "CITY:ABERDEEN",
         "ID:2011025065",
         "TIME:13:37:27",
         "UNIT:F52",
         "INFO:ALARM-FIRE ACTIVATION AC 800 932-3822 OPER SP8");
 
     doTest("T4",
         "[[911 NOTIFICATION] ]  632 SAND PIT RD ABERDEEN MDL 07A01 2011025721 11:06:58 M7 BURNS/EXPLOSIONS",
         "ADDR:632 SAND PIT RD",
         "CITY:ABERDEEN",
         "CODE:07A01",
         "ID:2011025721",
         "TIME:11:06:58",
         "UNIT:M7",
         "CALL:BURNS/EXPLOSIONS");
 
     doTest("T5",
         "[[911 NOTIFICATION] ]  218 BERRY ST PINE BLUFF 2011026135 14:36:24 F67 OUTSIDE FIRE/WOODS/BRUSH TREE ON FIRE",
         "ADDR:218 BERRY ST",
         "CITY:PINE BLUFF",
         "ID:2011026135",
         "TIME:14:36:24",
         "UNIT:F67",
         "INFO:OUTSIDE FIRE/WOODS/BRUSH TREE ON FIRE");
 
     doTest("T6",
         "[[911 NOTIFICATION] ]  1 E NEW ENGLAND /S PEAR 2011025862 07:31:07 F67 OUTSIDE FIRE/WOODS/BRUSH",
         "ADDR:1 E NEW ENGLAND & S PEAR",
         "MADDR:1 E NEW ENGLAND",
         "ID:2011025862",
         "TIME:07:31:07",
         "UNIT:F67",
         "INFO:OUTSIDE FIRE/WOODS/BRUSH");
 
     doTest("T7",
         "6 PAR DR WHISPERING PINES 2011045777 11:36:50 M10 CHEST PAIN CHEST PAIN",
         "ADDR:6 PAR DR",
         "PLACE:WHISPERING PINES",
         "ID:2011045777",
         "TIME:11:36:50",
         "UNIT:M10",
         "INFO:CHEST PAIN CHEST PAIN");

    doTest("T8",
        "232 ALLEN LN ABERDEEN MDL 30A01 2012028950 15:18:54 M30 TRAUMATIC INJURIES\r",
        "ADDR:232 ALLEN LN",
        "CITY:ABERDEEN",
        "CODE:30A01",
        "ID:2012028950",
        "TIME:15:18:54",
        "UNIT:M30",
        "CALL:TRAUMATIC INJURIES");
   }
   
 
   public static void main(String[] args) {
     new NCMooreCountyParserTest().generateTests("T1", "ADDR CITY PLACE CODE ID TIME UNIT CALL INFO");
   }
 }

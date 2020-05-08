 package net.anei.cadpage.parsers.MS;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MSMarionCountyParserTest extends BaseParserTest {
   
   public MSMarionCountyParserTest() {
     setParser(new MSMarionCountyParser(), "MARION COUNTY", "MS");
   }
   
   @Test
   public void testBadMsg() {
     doBadTest("-  - 911-CENTER:EVENT: SIG72 LOC:37 STRINGER BULLOCK RD Cad: 2012-0000001996 ENR >21:01 1023 >21:15 1024 >21:41\n");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "-  - 911-CENTER:SFIRE >STRUCTURE FIRE 1129 PICKWICK RD FOXWORTH FORBES, WILLEASE Map: Grids:, Cad: 2012-0000001317\n",
         "CALL:STRUCTURE FIRE",
         "ADDR:1129 PICKWICK RD",
         "CITY:FOXWORTH",
         "NAME:FORBES, WILLEASE",
         "ID:2012-0000001317");
 
     doTest("T2",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL 577 MT CARMEL CHURCH RD FOXWORTH AT&T MOBILITY Map: Grids:, Cad: 2012-0000001285\n",
         "CALL:MEDICAL CALL",
         "ADDR:577 MT CARMEL CHURCH RD",
         "CITY:FOXWORTH",
         "NAME:AT&T MOBILITY",
         "ID:2012-0000001285");
 
     doTest("T3",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL 244 SPRING HILL CHURCH RD SANDY HOOK AT&T MOBILITY Map: Grids:, Cad: 2012-0000001188\n",
         "CALL:MEDICAL CALL",
         "ADDR:244 SPRING HILL CHURCH RD",
         "CITY:SANDY HOOK",
         "NAME:AT&T MOBILITY",
         "ID:2012-0000001188");
 
     doTest("T4",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL 185 SHILOH FIRETOWER RD FOXWORTH BROOM, PAUL & MARTHA Map: Grids:, Cad: 2012-0000000979",
         "CALL:MEDICAL CALL",
         "ADDR:185 SHILOH FIRETOWER RD",
         "CITY:FOXWORTH",
         "NAME:BROOM, PAUL & MARTHA",
         "ID:2012-0000000979");
 
     doTest("T5",
         "-  - 911-CENTER:SIG2F >ALARM FIRE 109 HWY 98 E COLUMBIA Map: Grids:, Cad: 2012-0000000949\n",
         "CALL:ALARM FIRE",
         "ADDR:109 HWY 98 E",
         "CITY:COLUMBIA",
         "ID:2012-0000000949");
 
     doTest("T6",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL 491 JOHNSON RD SANDY HOOK MINGO, KAREN Map: Grids:, Cad: 2012-0000000646\n",
         "CALL:MEDICAL CALL",
         "ADDR:491 JOHNSON RD",
         "CITY:SANDY HOOK",
         "NAME:MINGO, KAREN",
         "ID:2012-0000000646");
 
     doTest("T7",
         "-  - 911-CENTER:SIG80 >MISSING PERSON / RUNAWAY 191 STOGNER RD FOXWORTH CELLULAR SOUTH Map: Grids:, Cad: 2012-0000000431\n",
         "CALL:MISSING PERSON/RUNAWAY",
         "ADDR:191 STOGNER RD",
         "CITY:FOXWORTH",
         "NAME:CELLULAR SOUTH",
         "ID:2012-0000000431");
 
     doTest("T8",
         "-  - 911-CENTER:SIG1 >MVA 115 KOKOMO RD KOKOMO TONEY, JACK Map: Grids:, Cad: 2012-0000000429\n",
         "CALL:MVA",
         "ADDR:115 KOKOMO RD",
         "CITY:KOKOMO",
         "NAME:TONEY, JACK",
         "ID:2012-0000000429");
 
     doTest("T9",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL 91 HURRICANE CREEK RD SANDY HOOK CELLULAR SOUTH Map: Grids:, Cad: 2012-0000000390\n",
         "CALL:MEDICAL CALL",
         "ADDR:91 HURRICANE CREEK RD",
         "CITY:SANDY HOOK",
         "NAME:CELLULAR SOUTH",
         "ID:2012-0000000390");
 
     doTest("T10",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL 244 SPRING HILL CHURCH RD SANDY HOOK LEWIS, MARY Map: Grids:, Cad: 2012-0000001643\n",
         "CALL:MEDICAL CALL",
         "ADDR:244 SPRING HILL CHURCH RD",
         "CITY:SANDY HOOK",
         "NAME:LEWIS, MARY",
         "ID:2012-0000001643");
 
     doTest("T11",
         "-  - 911-CENTER:SIG1S >MVA WITH INJURIES TEN MILE CREEK RD FOXWORTH AT&T MOBILITY Map: Grids:, Cad: 2012-0000004324\n",
         "CALL:MVA WITH INJURIES",
         "ADDR:TEN MILE CREEK RD",
         "CITY:FOXWORTH",
         "NAME:AT&T MOBILITY",
         "ID:2012-0000004324");
 
    doTest("T12",
         "-  - 911-CENTER:SIG72 >MEDICAL CALL TAYLORS CUT OFF FOXWORTH BLANSETT, ELISABETH Map: Grids:, Cad: 2012-0000005837\n",
        "CALL:MEDICAL CALL",
        "ADDR:TAYLORS CUTOFF",
        "CITY:FOXWORTH",
         "NAME:BLANSETT, ELISABETH",
         "ID:2012-0000005837");
  }
   
   @Test
   public void testProwler() {
 
     doTest("T1",
         "911-CENTER:SIG72 >MEDICAL CALL 221 NEW HOPE KOKOMO RD FOXWORTH AT&T MOBILITY Map: Grids:, Cad: 2012-0000004183",
         "CALL:MEDICAL CALL",
         "ADDR:221 NEW HOPE KOKOMO RD",
         "CITY:FOXWORTH",
         "NAME:AT&T MOBILITY",
         "ID:2012-0000004183");
 
     doTest("T2",
         "911-CENTER:SIG72 >MEDICAL CALL 1837 HWY 35 S FOXWORTH Map: Grids:, Cad: 2012-0000004092",
         "CALL:MEDICAL CALL",
         "ADDR:1837 HWY 35 S",
         "CITY:FOXWORTH",
         "ID:2012-0000004092");
 
     doTest("T3",
         "911-CENTER:SIG72 >MEDICAL CALL 1831 HWY 35 S FOXWORTH RILEY, BETTY Map: Grids:, Cad: 2012-0000004015",
         "CALL:MEDICAL CALL",
         "ADDR:1831 HWY 35 S",
         "CITY:FOXWORTH",
         "NAME:RILEY, BETTY",
         "ID:2012-0000004015");
 
     doTest("T4",
         "911-CENTER:SIG34 >FIGHT 238 JONES RD FOXWORTH DUNCAN, JOSEPH Map: Grids:, Cad: 2012-0000004278",
         "CALL:FIGHT",
         "ADDR:238 JONES RD",
         "CITY:FOXWORTH",
         "NAME:DUNCAN, JOSEPH",
         "ID:2012-0000004278");
 
   }
   
   public static void main(String[] args) {
     new MSMarionCountyParserTest().generateTests("T1", "CALL ADDR X CITY NAME PHONE MAP ID");
   }
 }

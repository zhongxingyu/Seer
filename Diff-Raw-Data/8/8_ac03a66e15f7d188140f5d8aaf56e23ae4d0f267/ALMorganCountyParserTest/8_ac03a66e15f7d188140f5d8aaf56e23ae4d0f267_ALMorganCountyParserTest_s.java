 package net.anei.cadpage.parsers.AL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.AL.ALMorganCountyParserTest;
 
 import org.junit.Test;
 
 
 public class ALMorganCountyParserTest extends BaseParserTest {
   
   public ALMorganCountyParserTest() {
     setParser(new ALMorganCountyParser(), "MORGAN COUNTY", "AL");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:MORGAN CO JAIL 119 LEE ST NE SEIZURES CAT1 CANAL ST NE OAK ST NE 11009615 DFS1",
         "PLACE:MORGAN CO JAIL",
         "ADDR:119 LEE ST NE",
         "CALL:SEIZURES",
         "PRI:1",
         "X:CANAL ST NE OAK ST NE",
         "ID:11009615",
         "SRC:DFS1");
 
     doTest("T2",
         "CAD:707-1/2 5TH AV SE UNCONSCIOUS OR SYNCOPE CAT1 PROSPECT DR SE 4TH ST SE DFS1",
         "ADDR:707-1/2 5TH AV SE",
         "CALL:UNCONSCIOUS OR SYNCOPE",
         "PRI:1",
         "X:PROSPECT DR SE 4TH ST SE",
         "SRC:DFS1");
 
     doTest("T3",
         "CAD:404 7TH AV SW TRAUMA WITH INJURY CAT1 1ST ST SW 2ND ST SW 11009612 DFS1",
         "ADDR:404 7TH AV SW",
         "CALL:TRAUMA WITH INJURY",
         "PRI:1",
         "X:1ST ST SW 2ND ST SW",
         "ID:11009612",
         "SRC:DFS1");
 
     doTest("T4",
         "CAD:BETTY ST SW/5TH AV SW SMOKE INVESTIGATION 11009584 DFS1",
         "ADDR:BETTY ST SW & 5TH AV SW",
         "CALL:SMOKE INVESTIGATION",
         "ID:11009584",
         "SRC:DFS1");
 
     doTest("T5",
         "CAD:1810 MARTIN ST SE UNCONSCIOUS OR SYNCOPE CAT1 18TH AV SE 19TH AV SE 11009594 DFS3",
         "ADDR:1810 MARTIN ST SE",
         "CALL:UNCONSCIOUS OR SYNCOPE",
         "PRI:1",
         "X:18TH AV SE 19TH AV SE",
         "ID:11009594",
         "SRC:DFS3");
 
     doTest("T6",
         "CAD:GORDON DR SW/CENTRAL PKWY SW STRUCTURE FIRE 11009333 DFS1",
         "ADDR:GORDON DR SW & CENTRAL PKWY SW",
         "CALL:STRUCTURE FIRE",
         "ID:11009333",
         "SRC:DFS1");
 
   }
   
   public static void main(String[] args) {
     new ALMorganCountyParserTest().generateTests("T1", "PLACE ADDR CALL PRI X ID SRC");
   }
 }

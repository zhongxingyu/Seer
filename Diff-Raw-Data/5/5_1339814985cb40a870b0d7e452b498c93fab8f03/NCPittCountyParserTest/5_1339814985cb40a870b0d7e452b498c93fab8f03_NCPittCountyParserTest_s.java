 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCPittCountyParserTest extends BaseParserTest {
   
   public NCPittCountyParserTest() {
     setParser(new NCPittCountyParser(), "PITT COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "S:911 Fire Call M:F24|Rcvd:05/31/2012 03:34|Rsn:FIRE ALARM ACT|Note:SAME|Adr:1550  QUALITY LN",
         "SRC:F24",
         "DATE:05/31/2012",
         "TIME:03:34",
         "CALL:FIRE ALARM ACT",
         "ADDR:1550  QUALITY LN");  // Google can't find
 
     doTest("T2",
         "S:911 Fire Call M: F14 | Rcvd:05/27/2012 01:16 | Rsn: MVC PI | Note:MARTIN LUTHER KING JR DR | WHICHARD RD | Adr:0 ",
         "SRC:F14",
         "DATE:05/27/2012",
         "TIME:01:16",
         "CALL:MVC PI",
         "ADDR:MARTIN LUTHER KING JR DR",
         "MADDR:MARTIN LUTHER KING JR DR & WHICHARD RD", // Google can't find
         "X:WHICHARD RD");
 
     doTest("T3",
         "S:911 Fire Call M: F14 | Rcvd:05/25/2012 17:12 | Rsn:47 YOM STROKE | Note:618 DANIELLE DR | Adr:0",
         "SRC:F14",
         "DATE:05/25/2012",
         "TIME:17:12",
         "CALL:47 YOM STROKE",
         "ADDR:618 DANIELLE DR");
 
     doTest("T4",
         "S:911 Fire Call M: F14 | Rcvd:05/25/2012 01:41 | Rsn: POSS ELECTRICAL FIRE |Note:3157 OLD RIVER RD | Adr:0",
         "SRC:F14",
         "DATE:05/25/2012",
         "TIME:01:41",
         "CALL:POSS ELECTRICAL FIRE",
         "ADDR:3157 OLD RIVER RD");
 
     doTest("T5",
         "S:911 Fire Call M: F14 | Rcvd:05/23/2012 19:55 | Rsn: MVC PI | Note:OLDR RIVER RD | BROWN PLACE DR @ THE BRIDGE | Adr:0",
         "SRC:F14",
         "DATE:05/23/2012",
         "TIME:19:55",
         "CALL:MVC PI",
         "ADDR:OLDR RIVER RD", // OLD RIVER RD & BLACK PLACE DR
         "MADDR:OLDR RIVER RD & BROWN PLACE DR",
         "X:BROWN PLACE DR",
         "PLACE:THE BRIDGE");
 
     doTest("T6",
         "S:911 Fire Call M: F24 | Rcvd:05/22/2012 17:16 | Rsn: FIRE ALARM | Note:SAME | Adr:2185 OAKLEY RD",
         "SRC:F24",
         "DATE:05/22/2012",
         "TIME:17:16",
         "CALL:FIRE ALARM",
         "ADDR:2185 OAKLEY RD");
   }
 
   public static void main(String[] args) {
     new NCPittCountyParserTest().generateTests("T1");
   }
 }

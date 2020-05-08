 package net.anei.cadpage.parsers.ID;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.ID.IDGoodingCountyParserTest;
 
 import org.junit.Test;
 
 
 public class IDGoodingCountyParserTest extends BaseParserTest {
   
   public IDGoodingCountyParserTest() {
     setParser(new IDGoodingCountyParser(), "GOODING COUNTY", "ID");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         " SIRCOMM / 1:34 PM\nNEW EVENT WND092\nFIRE\nFIRE ALL OTHER OR UN\n2000 E 3100 SOUTH\nWND AREA\n",
         "TIME:12:01:34",
         "SRC:WND092",
         "CALL:FIRE ALL OTHER OR UN",
         "ADDR:2000 E 3100 SOUTH",
         "CITY:WENDELL");
 
     doTest("T2",
         " SIRCOMM / 1:55 AM\nNEW EVENT WND092\nACCU\nACCIDENT W/UNKNOWN I\n2285 E 3300 SOUTH\nWND AREA\n",
         "TIME:00:01:55",
         "SRC:WND092",
         "CALL:ACCIDENT W/UNKNOWN I",
         "ADDR:2285 E 3300 SOUTH",
         "CITY:WENDELL");
 
     doTest("T3",
         " SIRCOMM / 9:59 PM\nNEW EVENT WND092\nFIREI\nFIRE INVESTIGATION\n1511 E 3600 SOUTH\nWND AREA\n",
         "TIME:12:09:59",
         "SRC:WND092",
         "CALL:FIRE INVESTIGATION",
         "ADDR:1511 E 3600 SOUTH",
         "CITY:WENDELL");
 
     doTest("T4",
         " SIRCOMM / 12:39 PM\nNEW EVENT WND092\n911\n911 DROP OFF/OPEN LI\n2175 E 3600 SOUTH\nWND AREA\n",
         "TIME:12:12:39",
         "SRC:WND092",
         "CALL:911 DROP OFF/OPEN LI",
         "ADDR:2175 E 3600 SOUTH",
         "CITY:WENDELL");
 
     doTest("T5",
         " SIRCOMM / 7:39 AM\nNEW EVENT WND092\nFIREV\nVEHICLE FIRE\n3232 S 1800 EAST\nWND AREA\n",
         "TIME:00:07:39",
         "SRC:WND092",
         "CALL:VEHICLE FIRE",
         "ADDR:3232 S 1800 EAST",
         "CITY:WENDELL");
 
     doTest("T6",
         " SIRCOMM / 8:58 AM\nNEW EVENT WND092\nUT\nUTILITY PROBLEMS\n336 3RD AVE E\nWENDELL\n",
         "TIME:00:08:58",
         "SRC:WND092",
         "CALL:UTILITY PROBLEMS",
         "ADDR:336 3RD AVE E",
         "CITY:WENDELL");
 
     doTest("T7",
         " SIRCOMM / 7:33 AM\nNEW EVENT WND092\nFIREB\nBRUSH/GRASS FIRE\n1425 E 2700 SOUTH\nWND AREA\n",
         "TIME:00:07:33",
         "SRC:WND092",
         "CALL:BRUSH/GRASS FIRE",
         "ADDR:1425 E 2700 SOUTH",
         "CITY:WENDELL");
 
     doTest("T8",
         " SIRCOMM / 9:14 PM\nNEW EVENT WND092\nFIREI\nFIRE INVESTIGATION\n1800 E 2800 SOUTH\nWND AREA\n",
         "TIME:12:09:14",
         "SRC:WND092",
         "CALL:FIRE INVESTIGATION",
         "ADDR:1800 E 2800 SOUTH",
         "CITY:WENDELL");
 
     doTest("T9",
         " SIRCOMM / 8:28 PM\nNEW EVENT WND092\nFIREB\nBRUSH/GRASS FIRE\n2132 NIAGRA SPRINGS GR\nWND AREA\n",
         "TIME:12:08:28",
         "SRC:WND092",
         "CALL:BRUSH/GRASS FIRE",
         "ADDR:2132 NIAGRA SPRINGS GR",  // Should be NIAGRA SPRINTS RD
         "MADDR:2132 NIAGRA SPRINGS GRADE",
         "CITY:WENDELL");
 
     doTest("T10",
         " SIRCOMM / 8:52 AM\nNEW EVENT WND092\nUT\nUTILITY PROBLEMS\n145 N HAGERMAN ST\nWENDELL\n",
         "TIME:00:08:52",
         "SRC:WND092",
         "CALL:UTILITY PROBLEMS",
         "ADDR:145 N HAGERMAN ST",
         "CITY:WENDELL");
  }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(SIRCOMM) 7:45 PM\r\nNEW EVENT GDG099\r\nMED\r\nMEDICAL CALL\r\n521 UTAH ST\r\nGOODING [Attachment(s) removed]",
         "TIME:12:07:45",
         "SRC:GDG099",
         "CALL:MEDICAL CALL",
         "ADDR:521 UTAH ST",
         "CITY:GOODING");
 
     doTest("T2",
         "(SIRCOMM) 7:49 AM\r\nNEW EVENT GDG099\r\nACCI\r\nACCIDENT W/INJURIES\r\n2200 E 3000 SOUTH\r\nWND AREA [Attachment(s) removed]",
         "TIME:00:07:49",
         "SRC:GDG099",
         "CALL:ACCIDENT W/INJURIES",
         "ADDR:2200 E 3000 SOUTH",
         "CITY:WENDELL");
 
   }
   
   public static void main(String[] args) {
     new IDGoodingCountyParserTest().generateTests("T1");
   }
 }

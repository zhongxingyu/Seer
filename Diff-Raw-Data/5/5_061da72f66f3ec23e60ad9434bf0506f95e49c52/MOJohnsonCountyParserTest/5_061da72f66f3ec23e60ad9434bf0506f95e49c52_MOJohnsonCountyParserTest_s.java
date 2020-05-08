 package net.anei.cadpage.parsers.MO;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MOJohnsonCountyParserTest extends BaseParserTest {
   
   public MOJohnsonCountyParserTest() {
     setParser(new MOJohnsonCountyParser(), "JOHNSON COUNTY", "MO");
   }
 
  
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[ 911 Page ] Dispatch: 7/19/2011 17:44:07 80 - Carbon Monoxide/Hazmat 1064 NE 75 RD, Apt. 112 JOHNSON COUNTY",
         "CALL:80 - Carbon Monoxide/Hazmat",
         "ADDR:1064 NE 75 RD",
         "APT:112");
 
     doTest("T2",
         "[ 911 Page ] Dispatch: 7/20/2011 19:24:58 FIRE VEHCILE 793 NE 101 RD JOHNSON COUNTY MO",
         "CALL:FIRE VEHCILE",
         "ADDR:793 NE 101 RD");
 
     doTest("T3",
         "[ 911 Page ] Dispatch: 7/18/2011 22:25:03 FIRE GRASS SE DD HWY & SE 341 RD JOHNSON COUNTY",
         "CALL:FIRE GRASS",
         "ADDR:SE DD HWY & SE 341 RD");
 
     doTest("T4",
         "[ Subject: 911 Page ] 7/17/2011 21:02:06 FIRE STRUCTURE 1072 NW 475 RD JOHNSON COUNTY",
         "CALL:FIRE STRUCTURE",
         "ADDR:1072 NW 475 RD");
   }
   
   public static void main(String[] args) {
     new MOJohnsonCountyParserTest().generateTests("T1", "CALL ADDR APT CITY");
   }
 }

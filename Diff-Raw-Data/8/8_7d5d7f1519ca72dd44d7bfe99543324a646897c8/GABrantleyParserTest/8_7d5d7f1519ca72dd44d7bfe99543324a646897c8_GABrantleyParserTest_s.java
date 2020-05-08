 package net.anei.cadpage.parsers.GA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class GABrantleyParserTest extends BaseParserTest {
   
   public GABrantleyParserTest() {
     setParser(new GABrantleyCountyParser(), "BRANTLEY COUNTY", "GA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Disp CVFD Case # 2011-2-92 Call # 22763) 2/2/2011 14:51:00 /1070 /52 /SAMANTHA LN /YARD FIRE / STRUCTURE IN DANGER",
         "SRC:CVFD",
         "ID:22763",
         "CALL:1070",
         "ADDR:52 SAMANTHA LN",
         "INFO:YARD FIRE / STRUCTURE IN DANGER");
 
     doTest("T2",
         "[Disp CVFD Case # 2011-1-1473 Call # 22262]  1/25/2011 0:55:31 /1070 /9513 /HWY 121 /BRUSH BEHIND HUMPTEY DUMPTEY",
         "SRC:CVFD",
         "ID:22262",
         "CALL:1070",
         "ADDR:9513 HWY 121",
         "INFO:BRUSH BEHIND HUMPTEY DUMPTEY");
 
     doTest("T3",
         "[Disp CVFD Case # 2011-1-1452 Call # 22241]  1/24/2011 17:06:14 /1070 /602 /WARE COUNTY LINE RD /WOODS FIRE",
         "SRC:CVFD",
         "ID:22241",
         "CALL:1070",
         "ADDR:602 WARE COUNTY LINE RD",
         "INFO:WOODS FIRE");
 
     doTest("T4",
         "[Disp CVFD Case # 2011-1-1317 Call # 22106]  1/22/2011 17:02:58 /1070 /2074 /MATTIE SHUMAN RD /CONTROL BURN",
         "SRC:CVFD",
         "ID:22106",
         "CALL:1070",
         "ADDR:2074 MATTIE SHUMAN RD",
         "INFO:CONTROL BURN");
 
     doTest("T5",
         "[Disp CVFD Case # 2011-1-1260 Call # 22049]  1/21/2011 15:23:22 /1052 /5013 /WHITEHALL CHURCH RD /62 YOM SICK; HOME HEALTH NURSE IS AT THE RESIDENCE AN",
         "SRC:CVFD",
         "ID:22049",
         "CALL:1052",
         "ADDR:5013 WHITEHALL CHURCH RD",
         "INFO:62 YOM SICK; HOME HEALTH NURSE IS AT THE RESIDENCE AN");
 
     doTest("T6",
         "[Disp CVFD Case # 2011-1-841 Call # 21630]  1/15/2011 13:53:32 /1070 /132 /SADDLE CLUB RD /FIELD ON FIRE",
         "SRC:CVFD",
         "ID:21630",
         "CALL:1070",
         "ADDR:132 SADDLE CLUB RD",
         "INFO:FIELD ON FIRE");
     
   }
   
   public static void main(String[] args) {
     new GABrantleyParserTest().generateTests("T1");
   }
 }

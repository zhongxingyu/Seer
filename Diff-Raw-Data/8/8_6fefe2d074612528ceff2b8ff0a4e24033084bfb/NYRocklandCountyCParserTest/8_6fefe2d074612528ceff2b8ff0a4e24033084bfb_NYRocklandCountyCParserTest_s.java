 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYRocklandCountyCParserTest extends BaseParserTest {
   
   public NYRocklandCountyCParserTest() {
     setParser(new NYRocklandCountyCParser(), "ROCKLAND COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(ACR# 5141)Add: 475 New Hempstead Rd, \nX St: Near the intersection of New Hempstead Rd and Old Schoolhouse Rd\nName: Center-Rehab &amp; Nurse, Friedwald Ph",
         "ID:5141",
         "ADDR:475 New Hempstead Rd",
         "X:Near New Hempstead Rd and Old Schoolhouse Rd",
         "NAME:Center-Rehab & Nurse, Friedwald Ph");
 
     doTest("T2",
         "(ACR# 5143)Add: 24 Deerwood Rd, \nX St: Between Cara Dr and Charlotte Dr\nName: Eisenberg, Mordechai\nPhone: 8453628456\nComp: \n9/9/2011 - 9:52 AM",
         "ID:5143",
         "ADDR:24 Deerwood Rd",
         "X:Cara Dr and Charlotte Dr",
         "NAME:Eisenberg, Mordechai",
         "PHONE:8453628456");
 
     doTest("T3",
         "(ACR# 5146) Add: 10  WIDMAN CT, \nX St: Near the intersection of Widman Ct and Bluefield Dr\nName: 7782, \nPhone: \nComp: \n9/9/2011 - 9:55 AM",
         "ID:5146",
         "ADDR:10  WIDMAN CT",
         "X:Near Widman Ct and Bluefield Dr",
         "NAME:7782,");
 
     doTest("T4",
         "(ACR# 5184) Add: 230 Kearsing Pkwy, \nX St: Between Jill Ln and RT-306\nName: Krausz, Arnold\nPhone: 8454267408\nComp: CHEST PAIN\n9/13/2011 - 10:17 AM",
         "ID:5184",
         "ADDR:230 Kearsing Pkwy",
         "X:Jill Ln and RT-306",
         "NAME:Krausz, Arnold",
         "PHONE:8454267408");
 
     doTest("T5",
         "(ACR# 5181) Add: 10 Melnick Dr, \nX St: Between Robert Pitt Dr and Spruill Ct\nName: Sale Furniture Warehouse, Whole\nPhone: 8453716664\nComp: EDP\n9/12/2011 -",
         "ID:5181",
         "ADDR:10 Melnick Dr",
         "X:Robert Pitt Dr and Spruill Ct",
         "NAME:Sale Furniture Warehouse, Whole",
         "PHONE:8453716664");
   }
   
   public static void main(String[] args) {
     new NYRocklandCountyCParserTest().generateTests("T1");
   }
 }

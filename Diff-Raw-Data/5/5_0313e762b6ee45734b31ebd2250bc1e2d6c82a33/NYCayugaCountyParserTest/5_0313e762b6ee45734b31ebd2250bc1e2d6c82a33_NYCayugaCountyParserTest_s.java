 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYCayugaCountyParserTest extends BaseParserTest {
   
   public NYCayugaCountyParserTest() {
     setParser(new NYCayugaCountyParser(), "CAYUGA COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(From 911 Center) 02/14/11 16:26 5 CEDAR LN BRUTUS Cardiac",
         "ADDR:5 CEDAR LN",
         "CITY:BRUTUS",
         "CALL:Cardiac");
 
     doTest("T2",
         "(From 911 Center) 02/14/11 14:59 8318 W  LOOP RD MONTEZUMA General",
         "ADDR:8318 W LOOP RD",
         "CITY:MONTEZUMA",
         "CALL:General");
 
     doTest("T3",
         "(From 911 Center) 02/14/11 12:19 PINE ST & PARK ST PORT BYRON Collapse",
         "ADDR:PINE ST & PARK ST",
         "CITY:PORT BYRON",
         "CALL:Collapse");
 
     doTest("T4",
         "(From 911 Center) 02/14/11 10:29 7 EVERGREEN LN BRUTUS Stroke",
         "ADDR:7 EVERGREEN LN",
         "CITY:BRUTUS",
         "CALL:Stroke");
 
     doTest("T5",
         "FRM:cayuga911@co.cayuga.ny.us\nSUBJ:From911Center\nMSG:\n04/20/11 13:51 3204 FRANKLIN STREET RD SENNETT Alarm - Fire\n",
         "ADDR:3204 FRANKLIN STREET RD",
         "CITY:SENNETT",
         "CALL:Alarm - Fire");
 
     doTest("T6",
         "(From 911 Center) 06/15/11 00:03 215 CAYUGA ST UNION SPRINGS Cardiac  Call Number 467 was created from Call Number 462(Jun 14 2011 11:20PM)  stried the pho",
         "ADDR:215 CAYUGA ST",
         "CITY:UNION SPRINGS",
         "INFO:Cardiac Call Number 467 was created from Call Number 462(Jun 14 2011 11: 20PM) stried the pho");
  }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(From 911 Center) 08/27/11 02:10 RIVER RD RT 38 CONQUEST MVAPI  subject came into bar  bloody states car accident   pirates landing bar  mvapi   believes",
         "ADDR:RIVER RD RT 38",
         "CITY:CONQUEST",
         "INFO:MVAPI subject came into bar bloody states car accident pirates landing bar mvapi believes");
 
     doTest("T2",
         "(From 911 Center) 08/27/11 00:42 HEJAMADA CAMPGROUND764 MC DONALD RD MONTEZUMA General Illness  mohawk section    off to left after gate  68 yo f   left a",
         "ADDR:HEJAMADA CAMPGROUND764 MC DONALD RD",
         "CITY:MONTEZUMA",
         "INFO:General Illness mohawk section off to left after gate 68 yo f left a");
 
     doTest("T3",
         "(From 911 Center) 08/26/11 14:49 8860 S  WILLOW ST WEEDSPORT General Illness  60 Y/O FEMALE  CALLER STATES SHE NEEDS TO GO TO ST JOES PER HER DR... ABDOME",
         "ADDR:8860 S WILLOW ST",
         "CITY:WEEDSPORT",
         "INFO:General Illness 60 Y / O FEMALE CALLER STATES SHE NEEDS TO GO TO ST JOES PER HER DR ABDOME");
 
     doTest("T4",
         "(From 911 Center) 08/26/11 09:59 8860 S  WILLOW ST WEEDSPORT Injury from a Fall  70 FEMALE FELL YESTERDAY/DIFF AMBULATING TODAY  E911 Info - Class of Serv",
         "ADDR:8860 S WILLOW ST",
         "CITY:WEEDSPORT",
         "INFO:Injury from a Fall 70 FEMALE FELL YESTERDAY / DIFF AMBULATING TODAY E911 Info - Class of Serv");
 
     doTest("T5",
         "(From 911 Center) 08/24/11 22:00 8750 CENTERPORT RD MENTZ Cardiac  79 Y/O FEMALE CHEST PAINS...  DERBY RESIDENCE",
         "ADDR:8750 CENTERPORT RD",
         "CITY:MENTZ",
         "INFO:Cardiac 79 Y / O FEMALE CHEST PAINS DERBY RESIDENCE");
  }
   
   public static void main(String[] args) {
     new NYCayugaCountyParserTest().generateTests("T1", "ADDR CITY CALL INFO");
   }
 }

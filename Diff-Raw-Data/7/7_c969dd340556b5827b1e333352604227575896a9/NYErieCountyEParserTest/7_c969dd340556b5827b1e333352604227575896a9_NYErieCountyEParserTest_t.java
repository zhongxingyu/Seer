 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYErieCountyEParser;
 
 import org.junit.Test;
 
 
 public class NYErieCountyEParserTest extends BaseParserTest {
   
   public NYErieCountyEParserTest() {
     setParser(new NYErieCountyEParser(), "ERIE COUNTY", "NY");
     
     // This is for the test generator, For unit tests it will be reset
     // the the default value before each test run
     setFromAddress("9300xxxx");
   }
   
   @Test
   public void testErieParser() {
     doTest("T1",
         "ALERT@ERIE.GOV FIRE CO-DETECTOR 262 MILLER ST APT: GARAGE LANCASTER TOWN CO DETECTOR ACTIV / NO SYMPTOMS",
         "CALL:FIRE CO-DETECTOR",
         "ADDR:262 MILLER ST",
         "APT:GARAGE",
         "CITY:LANCASTER TOWN",
         "INFO:CO DETECTOR ACTIV / NO SYMPTOMS");
 
     doTest("T2",
         "ALERT@ERIE.GOV EMS 4779 TRANSIT RD LANCASTER TOWN GREEN DODGE CALIBER 25 YO FEMALE FEELS AS THOUGH SHE MAY PASS OUT",
         "CALL:EMS",
         "ADDR:4779 TRANSIT RD",
         "CITY:LANCASTER TOWN",
         "INFO:GREEN DODGE CALIBER 25 YO FEMALE FEELS AS THOUGH SHE MAY PASS OUT");
 
     doTest("T3",
         "ALERT@ERIE.GOV EMS 4805 TRANSIT RD APT: 1106 LANCASTER TOWN EMS- 59 YO MALE, LEG PAIN AS A RESULT OF A FALL EARLIER",
         "CALL:EMS",
         "ADDR:4805 TRANSIT RD",
         "APT:1106",
         "CITY:LANCASTER TOWN",
         "INFO:EMS- 59 YO MALE, LEG PAIN AS A RESULT OF A FALL EARLIER");
 
     doTest("T4",
         "ALERT@ERIE.GOV EMS 48 BENTLEY CR LANCASTER TOWN 79 Y/O FEMALE LETHARGIC",
         "CALL:EMS",
         "ADDR:48 BENTLEY CR",
         "MADDR:48 BENTLEY CIR",
         "CITY:LANCASTER TOWN",
         "INFO:79 Y / O FEMALE LETHARGIC");
     
     doTest("T5",
         "ALERT@ERIE.GOV FIRE CO-DETECTOR 47 VIA DONATO E LANCASTER TOWN CO DETECTOR NO SYMPTOMS REFER TWIN DISTRICT FD",
         "CALL:FIRE CO-DETECTOR",
         "ADDR:47 VIA DONATO E",
         "CITY:LANCASTER TOWN",
         "INFO:CO DETECTOR NO SYMPTOMS REFER TWIN DISTRICT FD");
 
    doTest("T6",
        "ALERT@ERIE.GOV FIRE-COMMERCIAL 2170 UNION RD WSE GENERAL FIRE ALARM GOING OFF.",
        "CALL:FIRE-COMMERCIAL",
        "ADDR:2170 UNION RD",
        "CITY:WEST SENECA",
        "INFO:GENERAL FIRE ALARM GOING OFF");

   }
   
   @Test
   public void testDepewParser() {
     setFromAddress("9300xxxxxx");
 
     doTest("T1",
         "NOTIFICATIONS 85 MANITOU ST DEPEW DEPEW FIRE DEPARTMENT- DAILY TEST",
         "CALL:NOTIFICATIONS",
         "ADDR:85 MANITOU ST",
         "CITY:DEPEW",
         "INFO:DEPEW FIRE DEPARTMENT- DAILY TEST");
 
     doTest("T2",
         "NOTIFICATIONS 85 MANITOU ST DEPEW CONDUCTED THE DAILY FIRE RADIO AND PAGER TEST",
         "CALL:NOTIFICATIONS",
         "ADDR:85 MANITOU ST",
         "CITY:DEPEW",
         "INFO:CONDUCTED THE DAILY FIRE RADIO AND PAGER TEST");
 
     doTest("T3",
         "4569 BROADWAY DEPEW COMMERCIAL FIRE ALARM COMING FROM THE BANQUET ROOM",
         "CALL:COMMERCIAL FIRE ALARM COMING FROM THE BANQUET ROOM",
         "ADDR:4569 BROADWAY",
         "CITY:DEPEW");
     
   }
   
   @Test
   public void testEvansParser() {
 
     doTest("T1",
         "(Call) FIRE RESIDENTIAL 6680 LAKE SHORE RD EVANS FOREGIN OBJECT STRUCK A LIVE ELECTRICAL OUTLET.?\n",
         "CALL:FIRE RESIDENTIAL",
         "ADDR:6680 LAKE SHORE RD",
         "CITY:EVANS",
         "INFO:FOREGIN OBJECT STRUCK A LIVE ELECTRICAL OUTLET");
 
     doTest("T2",
         "(Call) EMS 1425 STURGEON PT RD EVANS H/NE/RM ACT  CARBON MONOXIDE DETECTOR WITH ILLNESS IN THE RESIDENCE?\n",
         "CALL:EMS",
         "ADDR:1425 STURGEON PT RD",
         "CITY:EVANS",
         "INFO:H / NE / RM ACT CARBON MONOXIDE DETECTOR WITH ILLNESS IN THE RESIDENCE");
 
     doTest("T3",
         "(Call) NE ACT FOR A ENGINE M/A TO LAKEVIEW FOR AN AUTO ACCIDENT ROUTE 5 AND OLD LAKESHORE RD\n",
         "CALL:NE ACT FOR A ENGINE M/A TO LAKEVIEW FOR AN AUTO ACCIDENT",
         "ADDR:ROUTE 5 AND OLD LAKESHORE RD");
 
     doTest("T4",
         "(Call) WIRES DOWN 7967 KEENE RD @ BURNS DISP NE - PATROL REQUESTING A CHIEF TO THE SCENE FOR WIRES IN THE TREE ARCHING?\n",
         "CALL:WIRES DOWN 7967 KEENE RD",
         "ADDR:BURNS DISP NE - PATROL REQUESTING A CHIEF TO THE SCENE FOR WIRES IN THE TREE ARCHING");
 
     doTest("T5",
         "(Call) RE TRANSMIT OF PAGE FOR INJURY ACCIDENT IFO 8061 SOUTHWESTERN-NORTH EVANS ONLY?\r\n",
         "CALL:RE TRANSMIT OF PAGE FOR INJURY ACCIDENT IFO",
         "ADDR:8061 SOUTHWESTERN-NORTH",
         "CITY:EVANS",
         "INFO:ONLY");
 
     doTest("T6",
         "(Call) ALARM RINGING 8545 N MAIN ST EVANS FIRE ALARM ACTIVATION COMING FROM FIRE PANEL?\r\n",
         "CALL:ALARM RINGING",
         "ADDR:8545 N MAIN ST",
         "CITY:EVANS",
         "INFO:FIRE ALARM ACTIVATION COMING FROM FIRE PANEL");
 
   }
   
   public static void main(String[] args) {
     new NYErieCountyEParserTest().generateTests("T1", "CALL ADDR APT CITY INFO");
   }
 }

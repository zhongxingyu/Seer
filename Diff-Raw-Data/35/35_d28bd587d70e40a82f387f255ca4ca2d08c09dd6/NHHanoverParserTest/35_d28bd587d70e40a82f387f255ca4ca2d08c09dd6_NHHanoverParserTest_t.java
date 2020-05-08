 package net.anei.cadpage.parsers.NH;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NHHanoverParserTest extends BaseParserTest {
   
   public NHHanoverParserTest() {
     setParser(new NHHanoverParser(), "HANOVER", "NH");
   }
   
   @Test
  public void testParserVT() {
    
    setDefaults("HANOVER", "VT");
 
     doTest("T1",
         "(_) MEDU 72 Y M FALLEN\nHUNTINGTON APARTMENTS\n493 S MAIN ST \nBRADFORD\n2/9/2012 18:54",
         "CALL:MEDU 72 Y M FALLEN",
         "PLACE:HUNTINGTON APARTMENTS",
         "ADDR:493 S MAIN ST",
         "CITY:BRADFORD",
         "DATE:2/9/2012",
         "TIME:18:54");
 
     doTest("T2",
         "(_) FD- STILL MV FIRE\nMAIN ST \nBRADFORD\n12/4/2011 19:19",
         "CALL:FD- STILL MV FIRE",
         "ADDR:MAIN ST",
         "CITY:BRADFORD",
         "DATE:12/4/2011",
         "TIME:19:19");
 
     doTest("T3",
         "(_) ACCIDENT (WITH INJURY)\nON RAMP\nI-91 S\nBRADFORD\n12/5/2011 06:40",
         "CALL:ACCIDENT (WITH INJURY)",
         "PLACE:ON RAMP",
         "ADDR:I-91 S",
         "MADDR:ON RAMP,I 91 S",
         "CITY:BRADFORD",
         "DATE:12/5/2011",
         "TIME:06:40");
 
     doTest("T4",
         "(_) AMBULANCE ASSIST\n33 S MAIN ST APT 4\nBRADFORD\n11/25/2011 23:53",
         "CALL:AMBULANCE ASSIST",
         "ADDR:33 S MAIN ST",
         "APT:4",
         "CITY:BRADFORD",
         "DATE:11/25/2011",
         "TIME:23:53");
 
     doTest("T5",
         "(_) FDS CO DETECTOR ACTIVATION\n5003 SOUTH RD\nBRADFORD\n10/30/2011 19:24",
         "CALL:FDS CO DETECTOR ACTIVATION",
         "ADDR:5003 SOUTH RD",
         "CITY:BRADFORD",
         "DATE:10/30/2011",
         "TIME:19:24");
 
     doTest("T6",
         "(_) FDS FUEL SPILL\n286 WAITS RIVER RD\nBRADFORD\n10/24/2011 09:05",
         "CALL:FDS FUEL SPILL",
         "ADDR:286 WAITS RIVER RD",
         "CITY:BRADFORD",
         "DATE:10/24/2011",
         "TIME:09:05");
 
     doTest("T7",
         "(_) FIRE CALL - FIRE ALARM\n378 N MAIN ST\nBRADFORD\n10/22/2011 16:09",
         "CALL:FIRE CALL - FIRE ALARM",
         "ADDR:378 N MAIN ST",
         "CITY:BRADFORD",
         "DATE:10/22/2011",
         "TIME:16:09");
 
     doTest("T8",
         "(_) FDS POWER LINES DOWN\n1025 GOSHEN RD\nBRADFORD\n10/15/2011 16:26",
         "CALL:FDS POWER LINES DOWN",
         "ADDR:1025 GOSHEN RD",
         "CITY:BRADFORD",
         "DATE:10/15/2011",
         "TIME:16:26");
 
     doTest("T9",
         "(_) FIRE CALL - FIRE ALARM\n378 N MAIN ST\nBRADFORD\n10/13/2011 21:18",
         "CALL:FIRE CALL - FIRE ALARM",
         "ADDR:378 N MAIN ST",
         "CITY:BRADFORD",
         "DATE:10/13/2011",
         "TIME:21:18");
 
     doTest("T10",
         "(_) FIRE CALL - FIRE ALARM\n378 N MAIN ST\nBRADFORD\n10/13/2011 21:18",
         "CALL:FIRE CALL - FIRE ALARM",
         "ADDR:378 N MAIN ST",
         "CITY:BRADFORD",
         "DATE:10/13/2011",
         "TIME:21:18");
 
     doTest("T11",
         "[_]  MEDU - DIFF BREATHING\nOXBOW HIGH SCHOOL\n36 OXBOW DR\nBRADFORD\n1/30/2012 09:59",
         "CALL:MEDU - DIFF BREATHING",
         "PLACE:OXBOW HIGH SCHOOL",
         "ADDR:36 OXBOW DR",
         "CITY:BRADFORD",
         "DATE:1/30/2012",
         "TIME:09:59");
 
     doTest("T12",
         "[_]  MEDU\nVALLEY VISTA\n23 UPPER PLAIN\nBRADFORD\n1/30/2012 13:19",
         "CALL:MEDU",
         "PLACE:VALLEY VISTA",
         "ADDR:23 UPPER PLAIN",
         "CITY:BRADFORD",
         "DATE:1/30/2012",
         "TIME:13:19");
 
     doTest("T13",
         "[_]  MEDU- 57 YO M DIFF BREATHING\n101 ERWIN RD\nBRADFORD\n1/30/2012 20:48",
         "CALL:MEDU- 57 YO M DIFF BREATHING",
         "ADDR:101 ERWIN RD",
         "CITY:BRADFORD",
         "DATE:1/30/2012",
         "TIME:20:48");
 
     doTest("T14",
         "[_]  FD- STILL\n1176 FAIRGROUND RD\nBRADFORD\n2/2/2012 11:01",
         "CALL:FD- STILL",
         "ADDR:1176 FAIRGROUND RD",
         "CITY:BRADFORD",
         "DATE:2/2/2012",
         "TIME:11:01");
 
     doTest("T15",
         "[_]  MEDU 80 YO MALE FEELING ILL\n613 UPPER PLAIN\nBRADFORD\n2/2/2012 18:37",
         "CALL:MEDU 80 YO MALE FEELING ILL",
         "ADDR:613 UPPER PLAIN",
         "CITY:BRADFORD",
         "DATE:2/2/2012",
         "TIME:18:37");
 
     doTest("T16",
         "[_]  MEDU 30 Y M MENTAL HEALTH PROB\n245 S MAIN ST APT 5\nBRADFORD\n2/2/2012 21:44",
         "CALL:MEDU 30 Y M MENTAL HEALTH PROB",
         "ADDR:245 S MAIN ST",
         "APT:5",
         "CITY:BRADFORD",
         "DATE:2/2/2012",
         "TIME:21:44");
 
     doTest("T17",
         "[_]  MEDU 56 Y M FALLEN BACK INJ\nCLEAR MEADOW LN\nBRADFORD\n2/3/2012 14:47",
         "CALL:MEDU 56 Y M FALLEN BACK INJ",
         "ADDR:CLEAR MEADOW LN",
         "CITY:BRADFORD",
         "DATE:2/3/2012",
         "TIME:14:47");
 
     doTest("T18",
         "[_]  MEDU\nBRADFORD MINI MART\n530 WAIT'S RIVER RD\nBRADFORD\n2/4/2012 19:33",
         "CALL:MEDU",
         "PLACE:BRADFORD MINI MART",
         "ADDR:530 WAIT'S RIVER RD",
         "CITY:BRADFORD",
         "DATE:2/4/2012",
         "TIME:19:33");
 
     doTest("T19",
         "[_]  MEDU76 F LETHARGIC CHEST PAIN\n1022 LOWER PLAIN\nBRADFORD\n2/5/2012 09:25",
         "CALL:MEDU76 F LETHARGIC CHEST PAIN",
         "ADDR:1022 LOWER PLAIN",
         "CITY:BRADFORD",
         "DATE:2/5/2012",
         "TIME:09:25");
 
     doTest("T20",
         "[_]  MEDU - 87 F LOW O2 LEVELS\nGILBERT RESD - FARMERS EXCHG BLDG\n245 S MAIN ST APT 2\nBRADFORD\n2/6/2012 16:53",
         "CALL:MEDU - 87 F LOW O2 LEVELS",
         "PLACE:GILBERT RESD - FARMERS EXCHG BLDG",
         "ADDR:245 S MAIN ST",
         "APT:2",
         "CITY:BRADFORD",
         "DATE:2/6/2012",
         "TIME:16:53");
 
     doTest("T21",
         "(_)  MEDU 72 Y M FALLEN\nHUNTINGTON APARTMENTS\n493 S MAIN ST\nBRADFORD\n2/9/2012 18:54",
         "CALL:MEDU 72 Y M FALLEN",
         "PLACE:HUNTINGTON APARTMENTS",
         "ADDR:493 S MAIN ST",
         "CITY:BRADFORD",
         "DATE:2/9/2012",
         "TIME:18:54");
    
  }
  
  @Test
  public void testParserNH() {

    doTest("T1",
        "(_) MUTUAL AID FIRE/AMB CALL\nHAVERHILL CORNER STATION\nHAVERHILL\nWITH ENGINE AND TANKER FOR COVERAGE\n6/10/2012 17:05",
        "CALL:MUTUAL AID FIRE/AMB CALL",
        "ADDR:HAVERHILL CORNER STATION",
        "CITY:HAVERHILL",
        "INFO:WITH ENGINE AND TANKER FOR COVERAGE",
        "DATE:6/10/2012",
        "TIME:17:05");
   }
 
   public static void main(String[] args) {
     new NHHanoverParserTest().generateTests("T1");
   }
 }

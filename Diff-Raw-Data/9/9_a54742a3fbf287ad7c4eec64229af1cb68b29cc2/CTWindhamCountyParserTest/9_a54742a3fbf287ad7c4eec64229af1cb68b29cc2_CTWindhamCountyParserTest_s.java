 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTWindhamCountyParserTest extends BaseParserTest {
   
   public CTWindhamCountyParserTest() {
     setParser(new CTWindhamCountyParser(), "WINDHAM COUNTY", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Moosup FD Page) STA94 STA594 MOOS1  UHF-4 PRI 1 person fallen Medical Alarm 1 SARA LN / Moosup (X-STS HIGH ST / DEAD END ) 07:31",
         "UNIT:STA94 STA594 MOOS1",
         "CH:UHF-4",
         "PRI:1",
         "CALL:person fallen Medical Alarm",
         "ADDR:1 SARA LN",
         "CITY:Moosup",
         "X:HIGH ST / DEAD END");
 
     doTest("T2",
         "(Moosup FD Page) STA94 STA594 MOOS1  33.88 PRI 1 Sick Person 109 WHIP POOR WILL DR / Moosup (X-STS ) 10:57",
         "UNIT:STA94 STA594 MOOS1",
         "CH:33.88",
         "PRI:1",
         "CALL:Sick Person",
         "ADDR:109 WHIP POOR WILL DR",
         "CITY:Moosup");
 
     doTest("T3",
         "(Moosup FD Page) STA94 STA594 MOOS1  UHF-4 PRI 1 Sick Person BARBER HILL RD / N  MAIN ST Moosup (X-STS ) 17:35",
         "UNIT:STA94 STA594 MOOS1",
         "CH:UHF-4",
         "PRI:1",
         "CALL:Sick Person BARBER",
         "ADDR:HILL RD & N  MAIN ST",
         "CITY:Moosup");
 
     doTest("T4",
         "(Moosup FD Page) STA94 94OFF  33.90 Vehicle Fire I 395 SOUTH SOUTH OF REST AREA / Moosup (X-STS ) 14:24",
         "UNIT:STA94 94OFF",
         "CH:33.90",
         "CALL:Vehicle Fire",
         "ADDR:I 395 SOUTH SOUTH OF REST AREA",
         "CITY:Moosup");
 
     doTest("T5",
         "(Moosup FD Page) STA94  UHF-4 Wire Down Starting a Small Grass Fire / Tree/Wires Down 201 STARKWEATHER RD / Moosup (X-STS GROVE ST  STERLING HILL RD / PLAINFIELD",
         "UNIT:STA94",
         "CH:UHF-4",
         "CALL:Wire Down Starting a Small Grass Fire/Tree/Wires Down",
         "ADDR:201 STARKWEATHER RD",
         "CITY:Moosup",
         "X:GROVE ST  STERLING HILL RD / PLAINFIELD");
 
     doTest("T6",
         "(Moosup FD Page) STA94  33.88 Please Operate 33.88/  Lift Assist 2 N  POTVIN AVE / Moosup (X-STS DAGGETT ST / POTVIN AVE ) 11:34",
         "UNIT:STA94",
         "CH:33.88",
         "CALL:Please Operate 3388/Lift Assist",
         "ADDR:2 N POTVIN AVE",
         "CITY:Moosup",
         "X:DAGGETT ST / POTVIN AVE");
 
     doTest("T7",
         "(Moosup FD Page) STA94  33.76 OUT BUILDING SMOLDERING Outside/Unknown Fire 399 SNAKE MEADOW RD / SNAKE MEADOW CLUB Moosup (X-STS OLEAROS HILL RD / DEMERS RD )",
         "UNIT:STA94",
         "CH:33.76",
         "CALL:OUT BUILDING SMOLDERING Outside/Unknown Fire",
         "ADDR:399 SNAKE MEADOW RD",
         "PLACE:SNAKE MEADOW CLUB",
         "CITY:Moosup",
         "X:OLEAROS HILL RD / DEMERS RD");
 
     doTest("T8",
         "(Moosup FD Page) STA94  UHF-4 Alarm Reset / Smoke from Cooking /  Fire Alarm 25 SUNNY ACRES / Moosup (X-STS DAGGETT ST / DEAD END ) 10:19",
         "UNIT:STA94",
         "CH:UHF-4",
         "CALL:Alarm Reset/Smoke from Cooking/Fire Alarm",
         "ADDR:25 SUNNY ACRES",
         "CITY:Moosup",
         "X:DAGGETT ST / DEAD END");
 
     doTest("T9",
         "(Moosup FD Page) STA94  33.90 PRI 1 ROLL OVER / STA94 MANPOWER REQUESTED / Vehicle Accident I 395 S / EXIT 88 / Plainfield (X-STS ) 11:27",
         "UNIT:STA94",
         "CH:33.90",
         "PRI:1",
         "CALL:ROLL OVER/STA94 MANPOWER REQUESTED/Vehicle Accident",
         "ADDR:I 395 S & EXIT 88",
         "CITY:Plainfield");
 
     doTest("T10",
         "(Moosup FD Page) QVMedic STA94 STA594 MOOS1  UHF-4 PRI 1 PARTIAL AIR WAS OBSTRUCTIONS / Choking/Airway Problem 67 S MAIN ST / Moosup (X-STS VOSSLER DR / WARD LN",
         "UNIT:QVMedic STA94 STA594 MOOS1",
         "CH:UHF-4",
         "PRI:1",
         "CALL:PARTIAL AIR WAS OBSTRUCTIONS/Choking/Airway Problem",
         "ADDR:67 S MAIN ST",
         "CITY:Moosup",
         "X:VOSSLER DR / WARD LN");
 
     doTest("T11",
         "(Moosup FD Page) STA94 STA594 MOOS2  UHF-4 PRI 1 MOOS AMB 2ND CREW / Vehicle Accident SNAKE MEADOW RD / VALLEY VIEW RD / Moosup (X-STS ) 22:10",
         "UNIT:STA94 STA594 MOOS2",
         "CH:UHF-4",
         "PRI:1",
         "CALL:MOOS AMB 2ND CREW/Vehicle Accident SNAKE",
         "ADDR:MEADOW RD & VALLEY VIEW RD",
         "CITY:Moosup");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "STA97 STA594 MOOS2  UHF-4 PRI 1 2ND CREW NEEDED Cardiac Distress 30 PICKETT RD / Central Village (X-STS TEX",
         "UNIT:STA97 STA594 MOOS2",
         "CH:UHF-4",
         "PRI:1",
         "CALL:2ND CREW NEEDED Cardiac Distress",
         "ADDR:30 PICKETT RD",
         "CITY:Central Village",
         "X:TEX");
  
   }
   
   public static void main(String[] args) {
     new CTWindhamCountyParserTest().generateTests("T1", "UNIT CH PRI CALL ADDR PLACE CITY X");
   }
 }

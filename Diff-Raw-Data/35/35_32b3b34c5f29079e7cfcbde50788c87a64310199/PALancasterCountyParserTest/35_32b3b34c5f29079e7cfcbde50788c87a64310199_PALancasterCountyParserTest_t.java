 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PALancasterCountyParser;
 
 import org.junit.Test;
 
 
 public class PALancasterCountyParserTest extends BaseParserTest {
   
   public PALancasterCountyParserTest() {
     setParser(new PALancasterCountyParser(), "LANCASTER COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
         "(VEH ACCIDENT-STANDBY) MANHEIM TWP~LITITZ PIKE / PETERSBURG RD~~ENG271,SQ27~20:19:57^",
         "CALL:VEH ACCIDENT-STANDBY",
         "CITY:MANHEIM TWP",
         "ADDR:LITITZ PIKE & PETERSBURG RD",
         "UNIT:ENG271,SQ27");
     
     doTest("T2",
         "(VEH ACCIDENT-MASS TRANSIT-1A) MANHEIM TWP~2475 OREGON PIKE~WHITEMARSH DR~VALLEY RD~ENG271~08:13:50^",
         "CALL:VEH ACCIDENT-MASS TRANSIT-1A",
         "CITY:MANHEIM TWP",
         "ADDR:2475 OREGON PIKE",
         "X:WHITEMARSH DR & VALLEY RD",
         "UNIT:ENG271");
         
     doTest("T3",
         "(SPILL CONTROL) EAST DONEGAL TWP~1158 RIVER RD~N BANK ST~ANDERSON FERRY RD~HAZ291~11:55:37^",
         "CALL:SPILL CONTROL",
         "CITY:EAST DONEGAL TWP",
         "ADDR:1158 RIVER RD",
         "X:N BANK ST & ANDERSON FERRY RD",
         "UNIT:HAZ291");
 
     doTest("T4",
         "(AUTO ALARM-HIGH OCCUPANCY) MANHEIM TWP~115 BLUE STREAK BLVD~SCHOOL RD~VALLEY RD~ENG271,ENG272~15:12:34^",
         "CALL:AUTO ALARM-HIGH OCCUPANCY",
         "CITY:MANHEIM TWP",
         "ADDR:115 BLUE STREAK BLVD",
         "X:SCHOOL RD & VALLEY RD",
         "UNIT:ENG271,ENG272");
 
     doTest("T5",
         "(HAZ MAT INCIDENT-1A) LANC CITY~753 S PLUM ST~17 ALY~JUNIATA ST~HAZ291~08:19:13^",
         "CALL:HAZ MAT INCIDENT-1A",
         "CITY:LANCASTER",
         "ADDR:753 S PLUM ST",
         "X:17 ALY & JUNIATA ST",
         "UNIT:HAZ291");
 
     doTest("T6",
         "VEH ACCIDENT-ENTRAPMENT-1A / ELIZABETHTOWN BORO~E COLLEGE AVE / S SPRUCE ST~~TRK74,ENG741~08:54:12^\n",
         "CALL:VEH ACCIDENT-ENTRAPMENT-1A",
         "CITY:ELIZABETHTOWN",
         "ADDR:E COLLEGE AVE & S SPRUCE ST",
         "UNIT:TRK74,ENG741");
 
     doTest("T7",
         "VEH ACCIDENT-CLASS 2 / WEST COCALICO TWP~N KING ST / E QUEEN ST~~RES13~13:10:45^\n",
         "CALL:VEH ACCIDENT-CLASS 2",
         "CITY:WEST COCALICO TWP",
         "ADDR:N KING ST & E QUEEN ST",
         "UNIT:RES13");
   }
   
  @Test
  public void testParser2() {

    doTest("T1",
        "FRM:messaging@iamresponding.com\nSUBJ:WBFC\nMSG:STANDBY-TRANSFER TO STATION\nWEST LAMPETER TWP~2901 WILLOW STREET PIKE~W WYNWOOD DR~DONNELLY DR~ENG906~14:44:13\n",
        "SRC:WBFC",
        "CALL:STANDBY-TRANSFER TO STATION",
        "CITY:WEST LAMPETER TWP",
        "ADDR:2901 WILLOW STREET PIKE",
        "X:W WYNWOOD DR & DONNELLY DR",
        "UNIT:ENG906");

    doTest("T2",
        "FRM:messaging@iamresponding.com\nSUBJ:WBFC\nMSG:BUILDING-DWELLING-1A\nPROVIDENCE TWP~417 LANC PIKE~MOUNT AIRY RD~DENNIS DR~ENG906~14:52:09\n",
        "SRC:WBFC",
        "CALL:BUILDING-DWELLING-1A",
        "CITY:PROVIDENCE TWP",
        "ADDR:417 LANCASTER PIKE",
        "X:MOUNT AIRY RD & DENNIS DR",
        "UNIT:ENG906");

    doTest("T3",
        "FRM:messaging@iamresponding.com\nSUBJ:WBFC\nMSG:SPILL CONTROL\nMANOR TWP~2601 RIVER RD~ANCHOR RD~LETORT RD~ENG903,TAN903,BR903,TAN907~16:04:15\n",
        "SRC:WBFC",
        "CALL:SPILL CONTROL",
        "CITY:MANOR TWP",
        "ADDR:2601 RIVER RD",
        "X:ANCHOR RD & LETORT RD",
        "UNIT:ENG903,TAN903,BR903,TAN907");
    
  }
  
   
   public static void main(String[] args) {
    new PALancasterCountyParserTest().generateTests("T1", "SRC CALL CITY ADDR X UNIT");
   }
 }

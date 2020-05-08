 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJSussexCountyParserTest extends BaseParserTest {
   
   public NJSussexCountyParserTest() {
     setParser(new NJSussexCountyParser(), "SUSSEX COUNTY", "NJ");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "(I-B2011-004928) ALARMF @ 172 LACKAWANNA DRIVE  , BYRAM - PROTECTIVE SERVICES (800-633-2677) OPERATOR 1663. GENERAL FIRE.",
         "ID:I-B2011-004928",
         "CALL:ALARMF",
         "ADDR:172 LACKAWANNA DRIVE",
         "CITY:BYRAM",
         "INFO:PROTECTIVE SERVICES (800-633-2677) OPERATOR 1663. GENERAL FIRE.");
 
     doTest("T2",
         "[I-B2010-009210]  ALARMF @ 7 WATERLOO ROAD  , BYRAM - ALL AMERICAN CRAFT. ALARMS PLUS (800-932-3822) OPERATOR 1224. UTILITY ROOM",
         "ID:I-B2010-009210",
         "CALL:ALARMF",
         "ADDR:7 WATERLOO ROAD",
         "CITY:BYRAM",
         "INFO:ALL AMERICAN CRAFT. ALARMS PLUS (800-932-3822) OPERATOR 1224. UTILITY ROOM");
 
     doTest("T3",
         "[I-B2011-004688]  FIRE @ 33 SLEEPY HOLLOW ROAD  , BYRAM - CALLER REPORTS HIS LIVING ROOM IS SMOKING.",
         "ID:I-B2011-004688",
         "CALL:FIRE",
         "ADDR:33 SLEEPY HOLLOW ROAD",
         "CITY:BYRAM",
         "INFO:CALLER REPORTS HIS LIVING ROOM IS SMOKING.");
 
     doTest("T4",
         "(I-B2011-003217) FIREPD @ 4 MOUNTAIN AVENUE  , BYRAM - CALLER REPORTS THICK BLACK SMOKE COMING FROM THE RESIDENCE.",
         "ID:I-B2011-003217",
         "CALL:FIREPD",
         "ADDR:4 MOUNTAIN AVENUE",
         "CITY:BYRAM",
         "INFO:CALLER REPORTS THICK BLACK SMOKE COMING FROM THE RESIDENCE.");
 
     doTest("T5",
         "[I-B2011-000701]  FIRE @ 228 TOMAHAWK TRAIL  , BYRAM - STRUCTURE FIRE",
         "ID:I-B2011-000701",
         "CALL:FIRE",
         "ADDR:228 TOMAHAWK TRAIL",
         "CITY:BYRAM",
         "INFO:STRUCTURE FIRE");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "KBROWN@andpd (I-2011-000118) MVA-F @ DECKER POND ROAD/SUNSET DRIVE  , GREEN TWP - CAR VS GUARDRAIL - MINOR INJURIES.",
         "ID:I-2011-000118",
         "CALL:MVA-F",
         "ADDR:DECKER POND ROAD & SUNSET DRIVE",
         "CITY:GREEN TWP",
         "INFO:CAR VS GUARDRAIL - MINOR INJURIES.");
 
     doTest("T2",
         "KBROWN@andpd (I-2011-000121) MVA-F @ HIBLER ROAD/WINTERMUTE ROAD  , GREEN TWP - CALLER REPORTING MOTORCYCLE MVA -- CALLER STATES APPEARS NO L",
         "ID:I-2011-000121",
         "CALL:MVA-F",
         "ADDR:HIBLER ROAD & WINTERMUTE ROAD",
         "CITY:GREEN TWP",
         "INFO:CALLER REPORTING MOTORCYCLE MVA -- CALLER STATES APPEARS NO L");
 
     setDefaults("", "");
     doTest("T3",
         "dcrater@andpd (I-2011-000117) ASSIST-F @  OUT OF TOWN  ,  - 44 KISHPAUGH RD FULLY INVOLVED STRUCTURE",
         "ID:I-2011-000117",
         "CALL:ASSIST-F",
         "ADDR:44 KISHPAUGH RD",
         "INFO:FULLY INVOLVED STRUCTURE");
     setDefaults("SUSSEX COUNTY", "NJ");
 
     doTest("T4",
         "kwilson@andpd (I-2011-000099) BURN-F @ 1 SCENIC DRIVE  , GREEN TWP - permit: A-3251 burning all day ** DO NOT RESPOND**",
         "ID:I-2011-000099",
         "CALL:BURN-F",
         "ADDR:1 SCENIC DRIVE",
         "CITY:GREEN TWP",
         "INFO:permit: A-3251 burning all day ** DO NOT RESPOND**");
 
     doTest("T5",
         "jcasella@andpd (I-2011-000122) MVA-F @ 21 SUTTON ROAD  , GREEN TWP - CAR VS TREE",
         "ID:I-2011-000122",
         "CALL:MVA-F",
         "ADDR:21 SUTTON ROAD",
         "CITY:GREEN TWP",
         "INFO:CAR VS TREE");
 
     doTest("T6",
         "jragsdale@andpd (I-2011-000108) TRANSF @ 71 WOLFS CORNER ROAD  , GREEN TWP - MUNICIPAL BLDG CALLED- HAS PASSERBY ADV OF TRANSFORMER FIRE hasEML = false;",
         "ID:I-2011-000108",
         "CALL:TRANSF",
         "ADDR:71 WOLFS CORNER ROAD",
         "CITY:GREEN TWP",
         "INFO:MUNICIPAL BLDG CALLED- HAS PASSERBY ADV OF TRANSFORMER FIRE hasEML = false;");
 
     doTest("T7",
         "kbrown@andpd (I-2011-000155) PUMP @ 6 RAMSEY COURT  , FRELINGHUYSEN - WARREN COUNTY COMMUNICATIONS REQUESTING A PUMP OUT FOR THE FOOT OF WATER IN BASEMENT.",
         "ID:I-2011-000155",
         "CALL:PUMP",
         "ADDR:6 RAMSEY COURT",
         "CITY:FRELINGHUYSEN",
         "INFO:WARREN COUNTY COMMUNICATIONS REQUESTING A PUMP OUT FOR THE FOOT OF WATER IN BASEMENT.");
  }
   
   public static void main(String[] args) {
     new NJSussexCountyParserTest().generateTests("T7", "ID CALL ADDR CITY INFO");
   }
 }

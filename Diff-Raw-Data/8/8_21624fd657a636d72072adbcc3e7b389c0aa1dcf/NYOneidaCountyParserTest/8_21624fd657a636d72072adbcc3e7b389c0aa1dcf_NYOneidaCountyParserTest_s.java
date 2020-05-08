 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYOneidaCountyParser;
 
 import org.junit.Test;
 
 
 public class NYOneidaCountyParserTest extends BaseParserTest {
   
   public NYOneidaCountyParserTest() {
     setParser(new NYOneidaCountyParser(), "ONEIDA COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "o;?WHIF:2010:0648\nDispatched\nEMS/BREATHING PROBLEMS\n111 HARDCASTLE AVE, WHITESBORO VILLAGE (MERITON DR/PERIMETER RD)",
         "ID:2010:0648",
         "CALL:EMS/BREATHING PROBLEMS",
         "ADDR:111 HARDCASTLE AVE",
         "CITY:WHITESBORO VILLAGE",
         "X:MERITON DR/PERIMETER RD");
 
     doTest("T2",
         "o;?WHIF:2010:0641\nDispatched\nEMS/DIABETIC PROBLEM\n31 MOHAWK ST, WHITESBORO VILLAGE (/SAUQUOIT STNear:AMERICAN LEGION POST",
         "ID:2010:0641",
         "CALL:EMS/DIABETIC PROBLEM",
         "ADDR:31 MOHAWK ST",
         "CITY:WHITESBORO VILLAGE",
         "X:SAUQUOIT ST",
         "INFO:Near:AMERICAN LEGION POST");
     
     doTest("T3",
         "o;?WHIF:2010:0636\nDispatched\nINVESTIGATE\n124 HARTS HILL TERR, WHITESTOWN (GILBERT RD/Near:HARTS HILL INN)",
         "ID:2010:0636",
         "CALL:INVESTIGATE",
         "ADDR:124 HARTS HILL TERR",
         "CITY:WHITESTOWN",
         "X:GILBERT RD",
         "INFO:Near:HARTS HILL INN");
     
     doTest("T4",
         "o;?WHIF:2010:0644\nDispatched\nMVA-PI\nHUGHES ST, WHITESBORO VILLAGE/ WEST ST, WHITESBORO VILLAGE",
         "ID:2010:0644",
         "CALL:MVA-PI",
         "ADDR:HUGHES ST",
         "MADDR:HUGHES ST & WEST ST",
         "CITY:WHITESBORO VILLAGE",
         "X:WEST ST / WHITESBORO VILLAGE");
     
     doTest("T5",
         "o;?WHIF:2010:0677\nDispatched\nEMS/PSYCHIATRIC/SUICIDE ATTEMPT\n19 ELLMORE DR, WHITESBORO VILLAGE (SAUQUOIT ST/WIND PL)",
         "ID:2010:0677",
         "CALL:EMS/PSYCHIATRIC/SUICIDE ATTEMPT",
         "ADDR:19 ELLMORE DR",
         "CITY:WHITESBORO VILLAGE",
         "X:SAUQUOIT ST/WIND PL");
     
     doTest("T6",
         "i>¿WEVF:2010:0170\nDispatched\nEMS/STROKE/CVA\n9132 MAIN ST, WESTERN (GIFFORD HILL RD/GEORGE STNear:WOODS VALLEY",
         "ID:2010:0170",
         "CALL:EMS/STROKE/CVA",
         "ADDR:9132 MAIN ST",
         "CITY:WESTERN",
         "X:GIFFORD HILL RD/GEORGE ST",
         "INFO:Near:WOODS VALLEY");
     
     doTest("T7",
         "o;?WHIF:2010:0704>Dispatched>EMS/ABDOMINAL PAIN>20 MOHAWK ST, WHITESBORO VILLAGE (SAUQUOIT ST/FERNBANK CIR; Near:AMERICAN LEGION POST 1113)",
         "ID:2010:0704",
         "CALL:EMS/ABDOMINAL PAIN",
         "ADDR:20 MOHAWK ST",
         "CITY:WHITESBORO VILLAGE",
         "X:SAUQUOIT ST/FERNBANK CIR",
         "INFO:Near:AMERICAN LEGION POST 1113");
 
     doTest("T8",
         "?LECF:2011:0085>Dispatched\n>17A03 - PUBLIC ASSIST (NO INJURIES AND NO PRIORITY SYMPTOMS)\n>5368 SLONE RD, LEE (LEE CENTER TABERG RD/SKINNER RD;)",
         "ID:2011:0085",
         "CODE:17A03",
         "CALL:PUBLIC ASSIST (NO INJURIES AND NO PRIORITY SYMPTOMS)",
         "ADDR:5368 SLONE RD",
         "CITY:LEE",
         "X:LEE CENTER TABERG RD/SKINNER RD");
 
     doTest("T9",
         "Body:?DEEF:2011:0075>Dispatched\n>WIRES DOWN/BURN\n>6352 WALKER RD, DEERFIELD (CRUIKSHANK RD/MILLER RD;)",
         "ID:2011:0075",
         "CALL:WIRES DOWN/BURN",
         "ADDR:6352 WALKER RD",
         "CITY:DEERFIELD",
         "X:CRUIKSHANK RD/MILLER RD");
 
     doTest("T10",
         "?LADF:2011:0165>Dispatched\n>FIRE STRUCTURE\n>8428 DAWN DR, ROME (EVENING RD/;)",
         "ID:2011:0165",
         "CALL:FIRE STRUCTURE",
         "ADDR:8428 DAWN DR",
         "CITY:ROME",
         "X:EVENING RD");
 
     doTest("T11",
         "?VEOF:2011:0407>Dispatched\n>ALARM FIRE\n>5218 PATRICK RD (COUNTY ROUTE 48A), VERONA (VERONA, ROUTE 365/SNYDER RD; Near:TURNING STONE CASINO)",
         "ID:2011:0407",
         "CALL:ALARM FIRE",
         "ADDR:5218 PATRICK RD",
         "CITY:VERONA",
         "X:COUNTY ROUTE 48A",
         "INFO:VERONA, ROUTE 365/SNYDER RD; Near:TURNING STONE CASINO");
 
     doTest("T12",
         "WATA:2011:0305 >Dispatched >EMS CALL >358 MADISON ST, WATERVILLE VILLAGE (/TERRY MEADOWS DR; Near:WATERVILLE HIGH SCHOOL)",
         "ID:2011:0305",
         "CALL:EMS CALL",
         "ADDR:358 MADISON ST",
         "CITY:WATERVILLE VILLAGE",
         "X:TERRY MEADOWS DR",
         "INFO:Near:WATERVILLE HIGH SCHOOL");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0356 &gt;Dispatched &gt;32B03 - UNKNOWN STATUS/OTHER CODES NOT APPLICABLE &gt;9218 MAIN ST N BROOKFIELD",
         "SRC:Waterville Amb",
         "ID:2011:0356",
         "CODE:32B03",
         "CALL:UNKNOWN STATUS/OTHER CODES NOT APPLICABLE",
         "ADDR:9218 MAIN ST",
         "CITY:N BROOKFIELD");
 
     doTest("T2",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0354 &gt;Dispatched &gt;26A11 - VOMITING &gt;97 STAFFORD AVE N, WATERVILLE VILLAGE (W BACON ST, E BACON ST/",
         "SRC:Waterville Amb",
         "ID:2011:0354",
         "CODE:26A11",
         "CALL:VOMITING",
         "ADDR:97 STAFFORD AVE N",
         "CITY:WATERVILLE VILLAGE",
         "X:W BACON ST / E BACON ST");
 
     doTest("T3",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0353 &gt;Dispatched &gt;17B03 - UNKNOWN STATUS/OTHER CODES NOT APPLICABLE &gt;774 ROUTE 12, SANGERFIELD (CR",
         "SRC:Waterville Amb",
         "ID:2011:0353",
         "CODE:17B03",
         "CALL:UNKNOWN STATUS/OTHER CODES NOT APPLICABLE",
         "ADDR:774 ROUTE 12",
         "CITY:SANGERFIELD",
         "X:CR");
 
     doTest("T4",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0352 &gt;Dispatched &gt;TRANSPORT &gt;@HARDINGS NURSING HOME  (220 TOWER ST), WATERVILLE VILLAGE",
         "ID:2011:0352",
         "SRC:Waterville Amb",
         "CALL:TRANSPORT",
         "PLACE:HARDINGS NURSING HOME",
         "ADDR:220 TOWER ST",
         "CITY:WATERVILLE VILLAGE");
 
     doTest("T5",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0351 &gt;Dispatched &gt;EMS CALL &gt;@WATERVILLE MEMORIAL PARK ELEMENTARY SCHOOL  (145 E BACON ST), WATERVI",
         "ID:2011:0351",
         "SRC:Waterville Amb",
         "CALL:EMS CALL",
         "PLACE:WATERVILLE MEMORIAL PARK ELEMENTARY SCHOOL",
         "ADDR:145 E BACON ST",
         "CITY:WATERVILLE VILLAGE");
 
     doTest("T6",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0350 &gt;Dispatched &gt;EMS CALL &gt;ONEIDA  COUNTY",
         "ID:2011:0350",
         "SRC:Waterville Amb",
         "CALL:EMS CALL",
         "ADDR:ONEIDA COUNTY");
 
     doTest("T7",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0349 &gt;Dispatched &gt;17B01 - POSSIBLY DANGEROUS BODY AREA &gt;2522 ROUTE 315, MARSHALL (CALIFORNIA RD/HU",
         "ID:2011:0349",
         "SRC:Waterville Amb",
         "CODE:17B01",
         "CALL:POSSIBLY DANGEROUS BODY AREA",
         "ADDR:2522 ROUTE 315",
         "CITY:MARSHALL",
         "X:CALIFORNIA RD/HU");
 
     doTest("T8",
         "[Waterville Amb]  &#239;&#187;&#191;WATA:2011:0348 &gt;Dispatched &gt;06C01-ABNORMAL BREATHING &gt;144 HUNTINGTON PL, WATERVILLE VILLAGE (/TOWER ST) #MID",
         "ID:2011:0348",
         "SRC:Waterville Amb",
         "CODE:06C01",
         "CALL:ABNORMAL BREATHING",
         "ADDR:144 HUNTINGTON PL",
         "CITY:WATERVILLE VILLAGE",
         "X:TOWER ST");
     
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "(Bridgewater Fire) &#239;&#187;&#191;BRIA:2012:0001 &gt;Dispatched &gt;28C05 - SUDDEN PARALYSIS OR FACIAL DROOP (ONE SIDE) &gt;130 ELMWOOD AVE N, WATERV",
         "SRC:Bridgewater Fire",
         "ID:2012:0001",
         "CODE:28C05",
         "CALL:SUDDEN PARALYSIS OR FACIAL DROOP (ONE SIDE)",
         "ADDR:130 ELMWOOD AVE N",
         "CITY:WATERVILLE VILLAGE");
   }
   
   @Test
   public void testParser4() {
 
     doTest("T1",
         "(New Hartford Fire) Dispatched >EMS CALL >@NH Post Office  (40 CAMPION RD), NEW HARTFORD",
         "SRC:New Hartford Fire",
         "CALL:EMS CALL",
         "PLACE:NH Post Office",
         "ADDR:40 CAMPION RD",
         "CITY:NEW HARTFORD");
 
     doTest("T2",
         "(New Hartford Fire) Dispatched &gt;MVA-UNKNOWN &gt;@K-Mart  (4645 COMMERCIAL DR), NEW HARTFORD",
         "SRC:New Hartford Fire",
         "CALL:MVA-UNKNOWN",
         "PLACE:K-Mart",
         "ADDR:4645 COMMERCIAL DR",
         "CITY:NEW HARTFORD");
   }
   
   @Test
   public void testParser5() {
 
     doTest("T1",
         ".... (Waterville Amb) &#239;&#187;&#191;WATA:2012:0010 &gt;Dispatched &gt;EMS CALL &gt;141 SANGER AVE, WATERVILLE VILLAGE (PARK PL/MADISON ST)",
         "SRC:Waterville Amb",
         "ID:2012:0010",
         "CALL:EMS CALL",
         "ADDR:141 SANGER AVE",
         "CITY:WATERVILLE VILLAGE",
         "X:PARK PL/MADISON ST");
 
   }
   
   public static void main(String[] args) {
     new NYOneidaCountyParserTest().generateTests("T1", "SRC ID CODE CALL PLACE ADDR CITY X INFO");
   }
 }

 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDPrinceGeorgesCountyFireBizParser;
 
 import org.junit.Test;
 
 
 public class MDPrinceGeorgesCountyFireBizParserTest extends BaseParserTest {
   
   public MDPrinceGeorgesCountyFireBizParserTest() {
     setParser(new MDPrinceGeorgesCountyFireBizParser(), "PRINCE GEORGES COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "55: BUILDING FIRE\nE809 TK809 BO804\n4637 37TH ST (ALLISON ST & DEAD END)\n12/07 04:51\nhttp://fireblitz.com/09/9.html",
         "SRC:55",
         "CALL:BUILDING FIRE",
         "UNIT:E809 TK809 BO804",
         "ADDR:4637 37TH ST",
         "X:ALLISON ST & DEAD END",
         "DATE:12/07",
         "TIME:04:51",
         "INFO:http://fireblitz.com/09/9.html");
 
     doTest("T2",
         "0-2 H12: PI\nS11 R11\nPG INDIAN HEAD HWY/FARMINGTON RD WEST\nNotes: WANTS ENGINE AND AMBULANCE FROM STATION 11\n12/07 06:14\nhttp://fireblitz.com/11/0.html",
         "SRC:0",
         "MAP:2 H12",
         "CALL:PI",
         "UNIT:S11 R11",
         "ADDR:INDIAN HEAD HWY & FARMINGTON RD WEST",
         "DATE:12/07",
         "TIME:06:14",
         "INFO:WANTS ENGINE AND AMBULANCE FROM STATION 11\nhttp://fireblitz.com/11/0.html");
 
     doTest("T3",
         "26: INVESTIGATION\nE817\n6036 PARKLAND CT (OLD SILVER HILL RD & OLD SILVER HILL RD)\n12/06 22:40\nhttp://fireblitz.com/17/9.html",
         "SRC:26",
         "CALL:INVESTIGATION",
         "UNIT:E817",
         "ADDR:6036 PARKLAND CT",
         "X:OLD SILVER HILL RD & OLD SILVER HILL RD",
         "DATE:12/06",
         "TIME:22:40",
         "INFO:http://fireblitz.com/17/9.html");
 
     doTest("T4",
         "48: FD ACCIDENT\nSQ818 B802\nANNAPOLIS RD/PRINCESS GARDEN PY (9006 ANNAPOLIS RD/5900 PRINCESS GARD\n12/07 07:02\nhttp://fireblitz.com/18/6.html",
         "SRC:48",
         "CALL:FD ACCIDENT",
         "UNIT:SQ818 B802",
         "ADDR:ANNAPOLIS RD & PRINCESS GARDEN PY",
         "X:9006 ANNAPOLIS RD/5900 PRINCESS GARD",
         "DATE:12/07",
         "TIME:07:02",
         "INFO:http://fireblitz.com/18/6.html");
 
     doTest("T5",
         "24: OVERDOSE - ALS\nA824 MD847, CHARLIE OD/NOT ALRT\n1605 AIRPORT LN (SCHALL RD & BERRY RD)\n12/07 07:14\nhttp://fireblitz.com/24/8.html",
         "SRC:24",
         "CALL:OVERDOSE - ALS",
         "UNIT:A824 MD847",
         "ADDR:1605 AIRPORT LN",
         "X:SCHALL RD & BERRY RD",
         "DATE:12/07",
         "TIME:07:14",
         "INFO:CHARLIE OD/NOT ALRT\nhttp://fireblitz.com/24/8.html");
 
     doTest("T6",
         "27: INJURED/SICK\nA827\n6304 MAXWELL DR #4 (CARSWELL AV & MORRIS AV)\n12/07 05:14\nhttp://fireblitz.com/27/2.html",
         "SRC:27",
         "CALL:INJURED/SICK",
         "UNIT:A827",
         "ADDR:6304 MAXWELL DR",
         "APT:4",
         "X:CARSWELL AV & MORRIS AV",
         "DATE:12/07",
         "TIME:05:14",
         "INFO:http://fireblitz.com/27/2.html");
 
     doTest("T7",
         "46: BUILDING FIRE\nE833 TK833 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/33/9.html",
         "SRC:46",
         "CALL:BUILDING FIRE",
         "UNIT:E833 TK833 BO802",
         "ADDR:1200 CAPITAL CENTER BL",
         "X:BLVD AT THE CAP CTR",
         "DATE:12/07",
         "TIME:00:51",        
         "INFO:http://fireblitz.com/33/9.html");
 
     doTest("T8",
         "46: BUILDING FIRE\nE837 TK837 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/37/5.html",
         "SRC:46",
         "CALL:BUILDING FIRE",
         "UNIT:E837 TK837 BO802",
         "ADDR:1200 CAPITAL CENTER BL",
         "X:BLVD AT THE CAP CTR",
         "DATE:12/07",
         "TIME:00:51",
         "INFO:http://fireblitz.com/37/5.html");
 
     doTest("T9",
         "49: MEDIC LOCAL\nA849\n13218 DEERFIELD RD (DEAD END & MATTHEWS CT)\n12/06 23:00\nhttp://fireblitz.com/49/1.html",
         "SRC:49",
         "CALL:MEDIC LOCAL",
         "UNIT:A849",
         "ADDR:13218 DEERFIELD RD",
         "X:DEAD END & MATTHEWS CT",
         "DATE:12/06",
         "TIME:23:00",
         "INFO:http://fireblitz.com/49/1.html");
 
     doTest("T10",
         "48: TOWNHOUSE FIRE\nE818 BO802\n9903 BREEZY KNOLL CT [DEAD END & GREEN HAVEN RD]\n12/23 23:32\nhttp://fireblitz.com/18/8.shtm",
         "SRC:48",
         "CALL:TOWNHOUSE FIRE",
         "UNIT:E818 BO802",
         "ADDR:9903 BREEZY KNOLL CT",
         "X:DEAD END & GREEN HAVEN RD",
         "DATE:12/23",
         "TIME:23:32",
         "INFO:http://fireblitz.com/18/8.shtm");
 
     doTest("T11",
         "45: PIA-HIGHWAY\nSQ827\nNB CRAIN HWY SE/NB HEATHERMORE BLVD, PP - <8322/ 0>\n07/05 10:02\nhttp://fireblitz.com/27/0.shtm",
         "SRC:45",
         "CALL:PIA-HIGHWAY",
         "UNIT:SQ827",
         "ADDR:NB CRAIN HWY SE & NB HEATHERMORE BLVD",
         "MADDR:CRAIN HWY SE & HEATHERMORE BLVD",
         "DATE:07/05",
         "TIME:10:02",
         "INFO:http://fireblitz.com/27/0.shtm");
 
     doTest("T12",
         "27: PIA-HIGHWAY\nA827 SQ827\nSB CAP BELT IL A HWY/NB BRANCH AVE, PP\n07/06 10:20\nhttp://fireblitz.com/27/5.shtm",
         "SRC:27",
         "CALL:PIA-HIGHWAY",
         "UNIT:A827 SQ827",
         "ADDR:SB CAP BELT IL A HWY & NB BRANCH AVE",
         "MADDR:CAP BELT IL A HWY & BRANCH AVE",
         "DATE:07/06",
         "TIME:10:20",
         "INFO:http://fireblitz.com/27/5.shtm");
 
     doTest("T13",
         "32: HOUSE FIRE\nE827 BO885\n5311 ACORN DR (GLEN OAK DR and WHITE OAK AVE)\n10/01 15:59\nhttp://fireblitz.com/PG/27/3.shtm",
         "SRC:32",
         "CALL:HOUSE FIRE",
         "UNIT:E827 BO885",
         "ADDR:5311 ACORN DR",
         "X:GLEN OAK DR and WHITE OAK AVE",
         "DATE:10/01",
         "TIME:15:59",
         "INFO:http://fireblitz.com/PG/27/3.shtm");
 
     doTest("T14",
         "26: APARTMENT FIRE\nSQ827 BO883\n6912 WALKER MILL RD #C2 (KAREN BLVD and SHADY GLEN DR)\n10/01 21:35\nhttp://fireblitz.com/PG/27/0.shtm",
         "SRC:26",
         "CALL:APARTMENT FIRE",
         "UNIT:SQ827 BO883",
         "ADDR:6912 WALKER MILL RD",
         "APT:C2",
         "X:KAREN BLVD and SHADY GLEN DR",
         "DATE:10/01",
         "TIME:21:35",
         "INFO:http://fireblitz.com/PG/27/0.shtm");
 
     doTest("T15",
         "10-09: SEIZURES\nB13\n8896 FORT SMALLWOOD RD-PA 1 SPIN & TRIM LAUDROMAT (ELIZABETH RD & AMHERST CT)\nhttp://fireblitz.com/AA/13/8.shtm",
         "SRC:10",
         "MAP:09",
         "CALL:SEIZURES",
         "UNIT:B13",
         "ADDR:8896 FORT SMALLWOOD RD",
         "CITY:PASSADENA",
         "PLACE:1 SPIN & TRIM LAUDROMAT",
         "X:ELIZABETH RD & AMHERST CT",
         "INFO:http://fireblitz.com/AA/13/8.shtm");
 
     doTest("T16",
         "1: MEDIC LOCAL\nE801\n6505 BELCREST RD\n02/10 14:55\nhttp://fireblitz.com/PG/01/5.shtm",
         "SRC:1",
         "CALL:MEDIC LOCAL",
         "UNIT:E801",
         "ADDR:6505 BELCREST RD",
         "DATE:02/10",
         "TIME:14:55",
         "INFO:http://fireblitz.com/PG/01/5.shtm");
   }
   
   public static void main(String[] args) {
     new MDPrinceGeorgesCountyFireBizParserTest().generateTests("T1");
   }
 }

 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYNassauCountyFiretrackerParserTest extends BaseParserTest {
   
   public NYNassauCountyFiretrackerParserTest() {
     setParser(new NYNassauCountyFiretrackerParser(), "NASSAU COUNTY", "NY");
   }
   
   @Test
   public void testFFDParser() {
 
     doTest("T1",
         "(FirePage) ** FFD FIRE CALL ** GENERAL PROB WITH GAS STOVE-POSS LEAK 170 JAY ST\nC/S: DEAN (E) ST / MILTON (E) ST TOA: 17:02 [FireTracker]",
         "SRC:FFD",
         "CALL:GENERAL PROB WITH GAS STOVE-POSS LEAK",
         "ADDR:170 JAY ST",
         "X:DEAN (E) ST / MILTON (E) ST");
 
     doTest("T2",
         "(FirePage) ** FFD FIRE CALL ** DISTRIC DIST CALL FOR SMOKE IN THE AREA CHURCH ST\nC/S: ARCHER ST TOA: 19:12 [FireTracker]",
         "SRC:FFD",
         "CALL:DISTRIC DIST CALL FOR SMOKE IN THE AREA",
         "ADDR:CHURCH ST",
         "MADDR:CHURCH ST & ARCHER ST",
         "X:ARCHER ST");
 
     doTest("T3",
         "(FirePage) ** FFD FIRE CALL ** GENERAL APT 72 / 3RD FLOOR / SMOKE 100 N MAIN ST\nC/S: RANDALL AVE / GRAND AVE TOA: 18:50 [FireTracker]",
         "SRC:FFD",
         "CALL:GENERAL/3RD FLOOR/SMOKE",
         "APT:72",
         "ADDR:100 N MAIN ST",
         "X:RANDALL AVE / GRAND AVE");
 
     doTest("T4",
         "(FirePage) ** FFD FIRE CALL ** STILL WILLOWBROOK LA C/S: BROOKSIDE (N) AVE TOA:\n09:16 [FireTracker]",
         "SRC:FFD",
         "CALL:STILL",
         "ADDR:WILLOWBROOK LA",
         "MADDR:WILLOWBROOK LN & BROOKSIDE (N) AVE",
         "X:BROOKSIDE (N) AVE");
 
     doTest("T5",
         "(FirePage) ** FFD FIRE CALL ** GENERAL ELEC PROBLEM/APT 415 45 WALLACE ST C/S:\nBROOKLYN AVE / RANDALL AVE TOA: 18:26 [FireTracker]",
         "SRC:FFD",
         "CALL:GENERAL ELEC PROBLEM /",
         "APT:415",
         "ADDR:45 WALLACE ST",
         "X:BROOKLYN AVE / RANDALL AVE");
 
 
     doTest("T6",
         "(FirePage) ** FFD FIRE CALL ** CARBON APT 33 116 WEST END AVE C/S: RAY ST / ELINOR\nPL TOA: 11:23 [FireTracker]",
         "SRC:FFD",
         "CALL:CARBON",
         "ADDR:116 WEST END AVE",
         "APT:33",
         "X:RAY ST / ELINOR PL");
 
     doTest("T7",
         "(FirePage) ** FFD FIRE CALL ** GENERAL 22 PEARSALL AVE C/S: LONG BEACH (N) AVE /\nPENNSYLVANIA AVE TOA: 15:56 [FireTracker]",
         "SRC:FFD",
         "CALL:GENERAL",
         "ADDR:22 PEARSALL AVE",
         "X:LONG BEACH (N) AVE / PENNSYLVANIA AVE");
 
     doTest("T8",
         "FirePage / ** FFD SIGNAL 9 ** SIG 9 MALE VOMITTING 21 NORTON ST C/S: GUY LOMBARDO\nAVE / HUDSON AVE TOA: 18:42 [FireTracker]\n",
         "SRC:FFD",
         "CALL:SIG 9 MALE VOMITTING",
         "ADDR:21 NORTON ST",
         "X:GUY LOMBARDO AVE / HUDSON AVE");
 
     doTest("T9",
         "FirePage / ** FFD SIGNAL 9 ** SIG 9 MALE VOMITTING 21 NORTON ST C/S: GUY LOMBARDO\nAVE / HUDSON AVE TOA: 18:42 [FireTracker]\n",
         "SRC:FFD",
         "CALL:SIG 9 MALE VOMITTING",
         "ADDR:21 NORTON ST",
         "X:GUY LOMBARDO AVE / HUDSON AVE");

    doTest("T10",
        "** FFD FIRE CALL ** CARBON AUTOMATIC/NO PERMIT 109 MARYLAND AVE C/S: SEAMAN (W) AVE / CALIFORNIA AVE TOA: 20:55 [FireTracker]",
        "SRC:FFD",
        "CALL:CARBON AUTOMATIC/NO PERMIT",
        "ADDR:109 MARYLAND AVE",
        "X:SEAMAN (W) AVE / CALIFORNIA AVE");
  
   }
   
   @Test
   public void testFSMFDParser() {
 
     doTest("T1",
         "(FirePage) *FSMFD* 4/22/2011 TOA:20:47 AMBU [AMBU] 178 RINTIN ST BENRIS AVE ARM\nINJURY [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:178 RINTIN ST",
         "X:BENRIS AVE",
         "INFO:ARM INJURY");
 
     doTest("T2",
         "(FirePage) *FSMFD* 4/27/2011 TOA:21:12 AMBU [AMBU] 820 FRANKLIN PL GARDEN CITY ROAD\nDIZZY FEMALE VOMITING [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:820 FRANKLIN PL",
         "X:GARDEN CITY ROAD",
         "INFO:DIZZY FEMALE VOMITING");
 
     doTest("T3",
         "(FirePage) *FSMFD* 4/28/2011 TOA:14:59 AMBU [AMBU] 1150 HEMPSTEAD TPK RENKEN BLVD\nIN THE SICK BAY SICK FEMALE [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:1150 HEMPSTEAD TPK",
         "X:RENKEN BLVD",
         "INFO:IN THE SICK BAY SICK FEMALE");
 
     doTest("T4",
         "(FirePage) *FSMFD* 4/28/2011 TOA:19:45 AMBU [AMBU] 947 MAPLE DR COURT HOUSE ROAD\nMALE,SHORT OF BREATH [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:947 MAPLE DR",
         "X:COURT HOUSE ROAD",
         "INFO:MALE,SHORT OF BREATH");
 
     doTest("T5",
         "(FirePage) *FSMFD* 4/28/2011 TOA:22:16 AMBU [AMBU] 1031 MOSEFAN ST WILLIAM AVE\nFEMALE FELL,WRIST INJURY [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:1031 MOSEFAN ST",
         "X:WILLIAM AVE",
         "INFO:FEMALE FELL,WRIST INJURY");
 
     doTest("T6",
         "[FirePage]  *FSMFD* 4/28/2011 TOA:22:16 AMBU [AMBU] 1031 MOSEFAN ST WILLIAM AVE FEMALE FELL,WRIST INJURY [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:1031 MOSEFAN ST",
         "X:WILLIAM AVE",
         "INFO:FEMALE FELL,WRIST INJURY");
 
     doTest("T7",
         "(FirePage) *FSMFD* 4/28/2011 TOA:23:14 CARB 1 [CARB] 551 CHESMAN ST NASSAU BLVD NO\nAIDED [FireTracker]",
         "SRC:FSMFD",
         "CALL:CARB 1",
         "ADDR:551 CHESMAN ST",
         "X:NASSAU BLVD",
         "INFO:NO AIDED");
 
     doTest("T8",
         "(FirePage) *FSMFD* 4/29/2011 TOA:19:21 AMBU [AMBU] 775 CORNELL ROAD ETON ROAD MALE\nFELL-HIP INJURY [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:775 CORNELL ROAD",
         "X:ETON ROAD",
         "INFO:MALE FELL-HIP INJURY");
 
     doTest("T9",
         "(FirePage) *FSMFD* 4/29/2011 TOA:21:59 MUTUAID [MAF] 125 MEACHAM AVE POST AVE\n[FireTracker]",
         "SRC:FSMFD",
         "CALL:MUTUAID",
         "ADDR:125 MEACHAM AVE",
         "X:POST AVE");
 
     doTest("T10",
         "(FirePage) FSMFD 5/2/2011 TOA:17:36 AMBU [AMBU] 766 WILLOW ROAD BIRCH ST BROKEN\nHIP [FireTracker]",
         "SRC:FSMFD",
         "CALL:AMBU",
         "ADDR:766 WILLOW ROAD",
         "X:BIRCH ST",
         "INFO:BROKEN HIP");
     
   }
   
   @Test
   public void testNMFDParser() {
 
     doTest("T1",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [GENERAL] [HOUSE] 7 ARMS AVE C/S: CAMP AVE / GARFIELD (E) ST -\nM/A 64 UPSTAIRS\n(Con' 2 of 2\nTOA:01:57 6/20/2011 Town Of: MERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[GENERAL] [HOUSE]",
         "ADDR:7 ARMS AVE",
         "X:CAMP AVE / GARFIELD (E) ST",
         "INFO:M/A 64 UPSTAIRS");
 
     doTest("T2",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [AMBU] [AMBU] 119 RHODE AVE C/S: HENRY ROAD / WILLIAM ST - M/A\n65 PAIN IN LEFT\n(Con' 2 of 2\nSIDE TOA:09:46 6/21/2011 Town Of: NO MERRICK\n[FireTracker](End)",
         "SRC:NMFD",
         "CALL:[AMBU] [AMBU]",
         "ADDR:119 RHODE AVE",
         "X:HENRY ROAD / WILLIAM ST",
         "INFO:M/A 65 PAIN IN LEFT SIDE");
 
     doTest("T3",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [AMBU] [AMBU] 4 WILLIAM ST C/S: BRIARCLIFF DR / RICHARD AVE -\nM/A 65 93F\n(Con't) 2 of 2\nCARDIAC/CARDIAC HISTORY TOA:14:02 6/21/2011 Town Of: NO\nMERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[AMBU] [AMBU]",
         "ADDR:4 WILLIAM ST",
         "X:BRIARCLIFF DR / RICHARD AVE",
         "INFO:M/A 65 93F CARDIAC/CARDIAC HISTORY");
     
     doTest("T4",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [GENERAL] [CARB] 185 E LOINES AV C/S: GRACE AVE / -DEAD END--\nM/A 64 TOA:12:20\n(Con 2 of 2\n6/22/2011 Town Of: MERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[GENERAL] [CARB]",
         "ADDR:185 E LOINES AV",
         "MADDR:185 E LOINES AVE",
         "X:GRACE AVE / -DEAD END-- M/A 64");
 
     doTest("T5",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [AMBU] [AMBU] 28 SURREY DR C/S: BAKER AVE / CROYDON DR - M/A 65\nFM FAINTING\n(Con't) 2 of 2\nTOA:16:41 6/23/2011 Town Of: NO MERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[AMBU] [AMBU]",
         "ADDR:28 SURREY DR",
         "X:BAKER AVE / CROYDON DR",
         "INFO:M/A 65 FM FAINTING");
 
     doTest("T6",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [AMBU] [AMBU] MERRICK AVE C/S: JERUSALEM AVE - M/A 65 FEMALE\nLYING IN ROADWAY\n(Con't 2 of 2\nTOA:16:52 6/23/2011 Town Of: NO MERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[AMBU] [AMBU]",
         "ADDR:MERRICK AVE",
         "MADDR:MERRICK AVE & JERUSALEM AVE",
         "X:JERUSALEM AVE",
         "INFO:M/A 65 FEMALE LYING IN ROADWAY");
 
     doTest("T7",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [MVA] [MVA] PARK AVE C/S: CAMERON AVE - M/A 65 AIDED WITH CHEST\nPAINS TOA:23:00\n(Con 2 of 2\n6/23/2011 Town Of: NO MERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[MVA] [MVA]",
         "ADDR:PARK AVE",
         "MADDR:PARK AVE & CAMERON AVE",
         "X:CAMERON AVE",
         "INFO:M/A 65 AIDED WITH CHEST PAINS");
 
     doTest("T8",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [AMBU] [AMBU] 1817 WILLIS AVE [ALARM PENDING] C/S: STATE ST /\nSCHERMERHORN ST -\n(C 2 of 2\nM/A 64 PERSON CHOKING TOA:12:22 6/25/2011 Town Of:\nMERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[AMBU] [AMBU]",
         "ADDR:1817 WILLIS AVE",
         "PLACE:[ALARM PENDING]",
         "X:STATE ST / SCHERMERHORN ST",
         "INFO:(C 2 of 2 M/A 64 PERSON CHOKING");
 
     doTest("T9",
         "1 of 2\nFRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [AMBU] [AMBU] 1766 MERRICK AVE [DUNKIN DONUTS] C/S: WEBSTER ST\n/ GARFIELD ST -\n(Co 2 of 2\nM/A 64 ASSAULT VICTIM / R/O TOA:15:51 6/25/2011 Town Of:\nNO MERRICK [FireTracker](End)",
         "SRC:NMFD",
         "CALL:[AMBU] [AMBU]",
         "ADDR:1766 MERRICK AVE",
         "PLACE:[DUNKIN DONUTS]",
         "X:WEBSTER ST / GARFIELD ST",
         "INFO:(Co 2 of 2 M/A 64 ASSAULT VICTIM / R/O");
 
     doTest("T10",
         "FRM:dispatch@firetracker.net\nSUBJ:FirePage\nMSG:**NMFD** [MVA] [MVA] WEBSTER ST C/S: MERRICK AVE - M/A 64 TOA:11:56\n6/28/2011 Town Of: MERRICK [FireTracker",
         "SRC:NMFD",
         "CALL:[MVA] [MVA]",
         "ADDR:WEBSTER ST",
         "MADDR:WEBSTER ST & MERRICK AVE",
         "X:MERRICK AVE",
         "INFO:M/A 64");
  }
   
   @Test
   public void testWPFDParser() {
 
     doTest("T1",
         "(FirePage) **WPFD** [CARBON] (CARB) [UPSTAIRS BEDROOM-NO AIDED] 141 COLLINS AVE\n" +
         "WILLISTON PARK C/S:LAFAYETTE ST / CENTER ST TOA:18:12 4/4/2012\n" +
         "[",
 
         "SRC:WPFD",
         "CALL:[CARBON] (CARB) [UPSTAIRS BEDROOM-NO AIDED]",
         "ADDR:141 COLLINS AVE",
         "PLACE:WILLISTON PARK",
         "X:LAFAYETTE ST / CENTER ST");
 
   }
   
   public static void main(String[] args) {
     new NYNassauCountyFiretrackerParserTest().generateTests("T1");
     //new NYNassauCountyFiretrackerParserTest().generateTests("T1", "SRC CALL ADDR X INFO");
   }
 }

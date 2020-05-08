 package net.anei.cadpage.parsers.GA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class GACamdenCountyParserTest extends BaseParserTest {
   
   public GACamdenCountyParserTest() {
     setParser(new GACamdenCountyParser(), "CAMDEN COUNTY", "GA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "astudstill@co.camden.ga.us:2011-101695* HIGHWAY 17 STEFFANS* * * KINGSLAND* * Traffic Stop* TRAFFIC STOP* * * 1128,1140,1150,1152,509,514,523,532,LS3,R3* * Med",
         "ID:2011-101695",
         "ADDR:HIGHWAY 17",
         "MADDR:STEFFANS,HIGHWAY 17",
         "CITY:KINGSLAND",
         "PLACE:STEFFANS",
         "CALL:TRAFFIC STOP",
         "UNIT:1128,1140,1150,1152,509,514,523,532,LS3,R3",
         "INFO:Med");
 
     doTest("T2",
         "astudstill@co.camden.ga.us:2011-104696* 4059 MARTIN LUTHER KING BLVD* N4* * 514,541,546,ENG4,LS4,MED4 KINGSLAND* * INJURY* INJURY* 509,ENG5,LS3* * Medical: No",
         "ID:2011-104696",
         "ADDR:4059 MARTIN LUTHER KING BLVD",
         "APT:N4",
         "CITY:KINGSLAND",
         "CALL:INJURY",
         "UNIT:509,ENG5,LS3",
         "INFO:Medical: No");
 
     doTest("T3",
         "astudstill@co.camden.ga.us:2011-101995* 405 & HIGHWAY 40 OLD WAFFLE HOUSE* * * KINGSLAND* * ACCIDENT* ACCIDENT* ELAINE* 386-208-4465* 514,541,546,ENG4,LS4,MED4",
         "ID:2011-101995",
         "ADDR:405 & HIGHWAY 40",
         "CITY:KINGSLAND",
         "PLACE:OLD WAFFLE HOUSE",
         "CALL:ACCIDENT",
         "NAME:ELAINE",
         "PHONE:386-208-4465",
         "UNIT:514,541,546,ENG4,LS4,MED4");
 
     doTest("T4",
         "astudstill@co.camden.ga.us:2011-104843* 429 EAGLE BLVD* * * KINGSLAND* * PERSON SICK* PERSON SICK* * 540,ENG4,LS4* * Medical: No* Haz",
         "ID:2011-104843",
         "ADDR:429 EAGLE BLVD",
         "CITY:KINGSLAND",
         "CALL:PERSON SICK",
         "UNIT:540,ENG4,LS4",
         "INFO:Medical: No / Haz");
 
     doTest("T5",
         "214 REDWOOD ST* * * KINGSLAND* * PERSON SICK* PERSON SICK* MS FAGEN*912-269-6157* LS3,R3* * Medical: No* Hazards: No*",
         "ADDR:214 REDWOOD ST",
         "CITY:KINGSLAND",
         "CALL:PERSON SICK",
         "NAME:MS FAGEN",
         "PHONE:912-269-6157",
         "UNIT:LS3,R3",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T6",
         "astudstill@co.camden.ga.us:2011-181161* ADVANCE COLLISION CENTER* * * KINGSLAND* * WIRE DOWN* WIRE DOWN* JOHN* 316-258-1559* ENG4,LS4* * Medical: No* Haz",
         "ID:2011-181161",
         "ADDR:ADVANCE COLLISION CENTER",
         "CITY:KINGSLAND",
         "CALL:WIRE DOWN",
         "NAME:JOHN",
         "PHONE:316-258-1559",
         "UNIT:ENG4,LS4",
         "INFO:Medical: No / Haz");
 
     doTest("T7",
         "astudstill@co.camden.ga.us:2012-037466* BOONE AND SUMMERBROOK* * * KINGSLAND* * INVESTIGATE* INVESTIGATE SUSPICIOUS PERSON/VEHICLE* JUAN RODRIGUEZ* 912-8",
         "ID:2012-037466",
         "ADDR:BOONE AND SUMMERBROOK",
         "CITY:KINGSLAND",
         "CALL:INVESTIGATE SUSPICIOUS PERSON/VEHICLE",
         "NAME:JUAN RODRIGUEZ",
         "PHONE:912-8");
 
     doTest("T8",
         "astudstill@co.camden.ga.us:2012-142354* CMC* * * ST MARYS* * * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED3* * Medical: No* Hazards: No* Lin",
         "ID:2012-142354",
         "ADDR:CMC",
         "CITY:ST MARYS",
         "CALL:TRANSFER TO ... (MEDICAL OR FIRE)",
         "UNIT:MED3",
         "INFO:Medical: No / Hazards: No / Lin");
 
     doTest("T9",
         "astudstill@co.camden.ga.us:2012-142422* 119 TERESA LN* * * KINGSLAND* * * * * DOMESTIC* DOMESTIC PROBLEM* HUNTER* 912-576-1327* 525,531,544,LS3,R3* * Med",
         "ID:2012-142422",
         "ADDR:119 TERESA LN",
         "CITY:KINGSLAND",
         "CALL:DOMESTIC PROBLEM",
         "UNIT:525,531,544,LS3,R3",
         "NAME:HUNTER",
         "PHONE:912-576-1327",
         "INFO:Med");
 
     doTest("T10",
         "astudstill@co.camden.ga.us:2012-142419* CMC TO SHANDS* * * KINGSLAND* * * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED4* * Medical: No* Hazar",
         "ID:2012-142419",
         "ADDR:CMC TO SHANDS",
         "CITY:KINGSLAND",
         "CALL:TRANSFER TO ... (MEDICAL OR FIRE)",
         "UNIT:MED4",
         "INFO:Medical: No / Hazar");
 
     doTest("T11",
         "astudstill@co.camden.ga.us:2012-142755* 102 ALMOND CIR* * * KINGSLAND* * * * * UNCONSCIOUS* UNCONSCIOUS PERSON* * 912-674-6173* 542,ENG4,LS4* * Medical: ",
         "ID:2012-142755",
         "ADDR:102 ALMOND CIR",
         "CITY:KINGSLAND",
         "CALL:UNCONSCIOUS PERSON",
         "UNIT:542,ENG4,LS4",
         "PHONE:912-674-6173",
         "INFO:Medical:");
 
     doTest("T12",
         "astudstill@co.camden.ga.us:2012-142422* 119 TERESA LN* * * KINGSLAND* * * * * DOMESTIC* DOMESTIC PROBLEM* HUNTER* 912-576-1327* 525,531,544,LS3,R3* * Med",
         "ID:2012-142422",
         "ADDR:119 TERESA LN",
         "CITY:KINGSLAND",
         "CALL:DOMESTIC PROBLEM",
         "UNIT:525,531,544,LS3,R3",
         "NAME:HUNTER",
         "PHONE:912-576-1327",
         "INFO:Med");
 
     doTest("T13",
         "astudstill@co.camden.ga.us:2012-143146* 200 CHESTNUT CT* * * KINGSLAND* * * * * INJURY* INJURY* * 912- -* 511,ENG4,KFD,LS4* * Medical: No* Hazards: No* L",
         "ID:2012-143146",
         "ADDR:200 CHESTNUT CT",
         "CITY:KINGSLAND",
         "CALL:INJURY",
         "UNIT:511,ENG4,KFD,LS4",
         "PHONE:912- -",
         "INFO:Medical: No / Hazards: No / L");
 
     doTest("T14",
         "astudstill@co.camden.ga.us:2012-142419* CMC TO SHANDS* * * KINGSLAND* * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED4* * Medical: No* Hazar",
         "ID:2012-142419",
         "ADDR:CMC TO SHANDS",
         "CITY:KINGSLAND",
         "CALL:TRANSFER TO ... (MEDICAL OR FIRE)",
         "UNIT:MED4",
         "INFO:Medical: No / Hazar");
 
     doTest("T15",
         "astudstill@co.camden.ga.us:2012-142222* STATION 4* * * KINGSLAND* * * * PERSON SICK* PERSON SICK* * * ENG4,LS4* * Medical: No* Hazards: No* Line18=*",
         "ID:2012-142222",
         "ADDR:STATION 4",
         "CITY:KINGSLAND",
         "CALL:PERSON SICK",
         "UNIT:ENG4,LS4",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T16",
         "astudstill@co.camden.ga.us:2012-142193* 2060 HWY 40 E* * * KINGSLAND* * * * FIRE ALARM* FIRE ALARM* * * ENG4,LS2,FIRE3,FIRE4,Q9,LAD3,ENG3,ENG5* Medic",
         "ID:2012-142193",
         "ADDR:2060 HWY 40 E",
         "CITY:KINGSLAND",
         "CALL:FIRE ALARM",
         "UNIT:ENG4,LS2,FIRE3,FIRE4,Q9,LAD3,ENG3,ENG5",
         "INFO:Medic");
 
     doTest("T17",
         "astudstill@co.camden.ga.us:2012-142755* 102 ALMOND CIR* * * KINGSLAND* * * * UNCONSCIOUS* UNCONSCIOUS PERSON* * 912-674-6173* 542,ENG4,LS4* * Medical:",
         "ID:2012-142755",
         "ADDR:102 ALMOND CIR",
         "CITY:KINGSLAND",
         "CALL:UNCONSCIOUS PERSON",
         "UNIT:542,ENG4,LS4",
         "PHONE:912-674-6173",
         "INFO:Medical:");
 
     doTest("T18",
         "astudstill@co.camden.ga.us:2012-143345* SATILLA TO CMC* * * KINGSLAND* * * * * TRANSPORT* PICKUP PRISONER/SUBJECT* * * MED3* * Medical: No* Hazards: No* ",
         "ID:2012-143345",
         "ADDR:SATILLA TO CMC",
         "CITY:KINGSLAND",
         "CALL:PICKUP PRISONER/SUBJECT",
         "UNIT:MED3",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T19",
         "astudstill@co.camden.ga.us:2012-143364* CMC* * * ST MARYS* * * * * TRANSPORT* PICKUP PRISONER/SUBJECT* * * MED3* * Medical: No* Hazards: No* Line18=*",
         "ID:2012-143364",
         "ADDR:CMC",
         "CITY:ST MARYS",
         "CALL:PICKUP PRISONER/SUBJECT",
         "UNIT:MED3",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T20",
         "astudstill@co.camden.ga.us:2012-143368* 955 S GROVE BLVD* LOT 42* * KINGSLAND* * * * * BREATHING* DIFFICULTY BREATHING* DESTINY* 912- -* 526,542,KFD,LS3,",
         "ID:2012-143368",
         "ADDR:955 S GROVE BLVD",
         "APT:LOT 42",
         "CITY:KINGSLAND",
         "CALL:DIFFICULTY BREATHING",
         "UNIT:526,542,KFD,LS3,",
         "NAME:DESTINY",
         "PHONE:912- -");
 
    doTest("T21",
        "astudstill@co.camden.ga.us:* 105 MILLERS BRANCH DR* * * ST MARYS* * * * * SMOKE INVEST* SMOKE INVESTIGATION* * 541-231-9242* LS4* * Medical: No* Hazards:",
        "ADDR:105 MILLERS BRANCH DR",
        "CITY:ST MARYS",
        "CALL:SMOKE INVESTIGATION",
        "UNIT:LS4",
        "PHONE:541-231-9242",
        "INFO:Medical: No / Hazards:");

   }
   
   public static void main(String[] args) {
     new GACamdenCountyParserTest().generateTests("T1");
   }
 }

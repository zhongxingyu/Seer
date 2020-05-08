 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Harris County ESD #1, TX
 Contact: Nathan Mathews <nbmathews807@gmail.com>
 Contact: Richy Hancock <richy@huffmanfire.org>
 Contact: Ennio Ponte <EPonte@channelviewfire.com>
 Sender: cadnoreply@proxy.hcec.com
 Sender: 930010nn
 ID#:11-06-20685 - - TRASH FIRE - 111 Bayou Dr - Apt:C - Bldg: - Key Map: 498D - Cross Streets:EAST FWY/MARKET - Box #:2002
 ID#:11-06-20710 - - FIRE - PreAlert - 16217 Ridlon - Apt: - Bldg: intra SERVICES - Key Map: 498C - Cross Streets:SHEILA LN/WOODLAND D
 ID#:11-06-20658 - - 29A1 MOTOR VEHICLE A - East Fwy / Delldale - Apt: - Bldg: - Key Map: 498E - Cross Streets: - Box #:2004
 ID#:11-06-20627 - - 29A1 MOTOR VEHICLE A - 16410 Ave D - Apt:1 - Bldg: AVE D APTS - Key Map: 498D - Cross Streets:WOODLAND DR/CEDAR L
 
 ID#:11-07-25331 - - ALARMS - 1518 Great Dover Cir - Apt: - Bldg: - Key Map: 458W
 ID#:11-07-25341 - 06D02 - 6D1 RESPIRATORY - 11870 Greenloch Ln - Apt: - Bldg: - Key Map: 416K - Cross Streets:GREENCANYON DR/GREENROC 
 ID#:11-07-25342 - 25A01 - PSYCHIATRIC - 15215 S Brentwood Dr - Apt: - Bldg: - Key Map: 498E - Cross Streets:Dead End/DELLDALE - Box #:2004
 ID#:11-07-25378 - - Stroke (CVA) - 11715 Greenglen Dr - Apt: - Bldg: - Key Map: 416K - Cross Streets:JOHN RALSTON RD/GREENROCK LN - B 
 ID#:11-07-25416 - - HEMORRHAGE/LACERATIO - 12370 S San Circle Dr - Apt: - Bldg: UNK TRAILER PARK - Key Map: 456C - Cross Streets:BARK 
 ID#:11-08-25733 - 55B04 - ELECTRICAL HAZARD - 16102 E Ih 10 - Apt: - Bldg: OLD RIVER CHURCH DAY - Key Map: 498G - Cross Streets:IH 10 EAST BAYOU RAMP/IH 10 SH -
 ID#:11-08-25969 - 29D02m - 29D5 MOTOR VEHICLE A - 539 S Sheldon Rd - Apt: - Bldg: - Key Map: 498K - Cross Streets:JACINTO PORT BLVD/MARKET - Box #:2001
 ID#:11-08-25936 - 29B04 - Traffic/Trans Incide - E BW 8 N / WOODFOREST BW 8 NB - Apt: - Bldg: - Key Map: 457Y - Cross Streets: - Box #:2005
 ID#:11-08-26006 - - Citizen Assist - 5500 E Bw 8 N - Apt: - Bldg: big tex storage - Key Map: 457Y - Cross Streets:BW 8 NB WALLISVILLE EXIT RAMP/ - Box #:2005
 ID#:11-08-28674 - Units:M151 - 32B01 - 32B1 UNKNOWN PROBLEM - 606 Commons Vista Dr - Apt: - Bldg: - Key Map: 298Q - Cross Streets:COM
 ID#:11-08-29430 - - FALLS - 17015 Faring Rd - Apt: - Bldg: - Key Map: 418X - Cro
 ID#:11-08-29436 - - ALARMS - 1714 Hidden Terrace Dr - Apt: - Bldg: - Key Map: 458S - Cross Streets:HIDDEN CREST DR/HIDDEN MANOR D - Box #:2005
 ID#:11-08-26039 - - FUEL SPILL - 15880 Wallisville Rd - Apt: - Bldg: LEGEND OAKS - EAST - Key Map: 457V - Cross Streets:HIDDEN PARK DR/HYACINTH PATH W - Box #:2005
 ID#:11-08-26105 - 21B01 - HEMORRHAGE/LACERATIO - 11803 Greenmesa Dr - Apt: - Bld
 ID#:11-08-26080 - 13D01 - DIABETIC PROBLEMS - 10710 Green River Dr - Apt: - Bldg
 ID#:11-08-26159 - - Stab / GSW (B) - 919 Holbech - Apt: - Bldg: - Key Map: 497D
 ID#:11-08-26160 - - HEART PROBLEMS - 9402 Abbotshall Ln - Apt: - Bldg: - Key Map: 456D - Cross Streets:ARBOR FIELD LN/STONEFIELD MANO - Box #:3304
 
  1 of 2\nFRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170 - 67D02 - 67D2 LG GRASS AND WO - US 90 / SHELDON US 90 RAMP - Apt: - Bldg: - Key Map:\n(Con't) 2 of 2\n458C - Cross Streets: - Box #:3304(End)
 
 ID#:11-08-29410 - Address changed to:uvalde/kinsman - Cross Streets: - Key Map:NOT F
 ID#:11-08-26067 - Address changed to:620 SHELDON RD - Cross Streets:AVE C/RIDLON - Key Map:498B
 ID#:11-08-26080 - Address changed to:10710 Greens Crossing Blvd - Cross Streets
 
 ID#:11-08-29436 - UNIT:L32 - DISP: 21:06:25 - ER: - OUT: - TO: - AT: - AIR: 21:0
 FRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170- UNIT:E13 - DISP: 11:19:11 - ER: - OUT: - AIR: 11:23:42
 FRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170- UNIT:BRU23 - DISP: 10:57:35 - ER: - OUT: - AIR: 11:23:56
 
 Contact: Active911
 [] ID#:12-06-23555 - - 60B1 GAS LEAK/ODOR ( - 11000 E Canal Rd - Apt: - Bldg: - Key Map: 460J - Cross Streets:OAK AVE/HOLLY DR - Box #:1705\n
 []  ID#:12-06-23555 - Address changed to:1100 E CANAL RD - Cross Streets:OAK AVE/HOLLY DR - Key Map:460J\n
 []  ID#:12-06-23588 - Address changed to:ORLEANS ST / COTTONTAIL DR - Cross Streets: - Key Map:420W\n
 []  ID#:12-06-23588 - Address changed to:630 COTTONTAIL DR - Cross Streets:JOAN OF ARC ST/ORLEANS ST - Key Map:419Z\n
 
 Contact: Active911
 Agency name: Aldine Fire Department Location: Aldine, TX 
 Sender: cadnoreply@proxy.hcec.com
 
 ID#:12-07-26086 - UNIT:GPE1 - Call Rcvd: 10:35:44 - DISP: 10:47:25 - ER: - OUT: - TO: - AT: - AIR: 10:57:50
 ID#:12-07-26106 - UNIT:E11 - DISP: 12:30:22 - ER: - OUT: - AIR: 12:31:28
 ID#:12-07-26109 - UNIT:E31 - Call Rcvd: 12:37:33 - DISP: 12:38:15 - ER: - OUT: - TO: - AT: - AIR: 12:44:01
 ID#:12-07-26309 - UNIT:E31 - Call Rcvd: 22:36:45 - DISP: 22:37:39 - ER: 22:40:01 - OUT: 22:47:50 - TO: - AT: - AIR: 23:05:03
 ID#:12-07-26353 - UNIT:SQ31 - Call Rcvd: 06:57:00 - DISP: 06:58:43 - ER: - OUT: 07:06:50 - TO: - AT: - AIR: 07:11:00
 ID#:12-07-26416 - UNIT:E21 - Call Rcvd: 16:40:37 - DISP: 16:41:39 - ER: 16:43:12 - OUT: 16:44:28 - TO: - AT: - AIR: 17:19:35
 
 ID#:12-07-26090 - - FIRE - PreAlert - 2409 Foley Rd - Apt: - Bldg: CROSBY FIRE STATION - Key Map: NOT F - Cross Streets:BRIDGEWOOD DR/SUMMIT TRAIL RD - Box #:8004
  ID#:12-07-26090 - Address changed to:123 TEST STREET - Cross Streets:Dead End/Dead End - Key Map:496K
  ID#:12-07-26090 - Address changed to:5335 ALDINE BENDER RD - Cross Streets:CROSSWINDS DR/SURLES DR - Key Map:374Z
 GPM1 - - MEDICAL - PreAlert - 5335 ALDINE BENDER RD - Apt: - Bldg: TIMBER RID - Key Map:374Z - Cross Streets:CROSSWINDS DR/SURLES DR - Box #:2601
  ID#:12-07-26090 - Address changed to:123 S DIAMONDHEAD BLVD - Cross Streets:CORAL BAY ST/PORT OCALL ST - Key Map:419C
 ID#:12-07-26090 - - MEDICAL - PreAlert - 123 S DIAMONDHEAD BLVD - Apt: - Bldg: - Key Map: 419C - Cross Streets:CORAL BAY ST/PORT OCALL ST - Box #:8005
 SQ31 - 31D03 - 31D3 UNCONSCIOUS PAR - 433 Northpark Central Dr - Apt: - Bldg: ZOYTO - Key Map:332Y - Cross Streets:NORTHVIEW PARK DR/WOODHAM DR - Box #:1101
 E11 - 69D06T - 69D6 FIRE IN THE RE - 4723 Cicada Ln - Apt: - Bldg: - Key Map:414G - Cross Streets:BOREAS/CRIEFFE RD - Box #:2506
  ID#:12-07-26186 - Address changed to:4715 CICADA LN - Cross Streets:BOREAS/CRIEFFE RD - Key Map:414G
 SQ31 - 06E01 - 6E1 RESPIRATORY - 503 Salma Ct - Apt: - Bldg: - Key Map:372H - Cross Streets:Dead End/DESERT MARIGOLD DR - Box #:1103
 SQ31 - - 12A1 SEIZURES - 15516 Martineau St - Apt: - Bldg: - Key Map:373R - Cross Streets:MARVELL DR/GREENBRIAR COLONY D - Box #:1106
 E11 - 12C01E - 12C1 SEIZURES - 15516 Martineau St - Apt: - Bldg: - Key Map:373R - Cross Streets:MARVELL DR/GREENBRIAR COLONY D - Box #:1106
 E11 - 69D01 - 69D1 STRUCTURE FIRE - 12345 Vickery St - Apt: - Bldg: OLESON ELE - Key Map:414G - Cross Streets:CHARRITON DR/ALDINE MAIL RD - Box #:2506
 E31 - 29D02m - 29D2 MOTOR VEHICLE A - 14900 North Fwy - Apt: 203 - Bldg: VALUE PLAC - Key Map:372C - Cross Streets:Dead End/AIRTEX ENTR NB 45 - Box #:1103
 E11 - - 69D6 FIRE IN THE RE - 823 Bandon Ln - Apt: - Bldg: - Key Map:372H - Cross Streets:PRAIRIE LARKSPUR DR/REMINGTON - Box #:1103
 SQ31 - - 29A1 MOTOR VEHICLE A - Hardy Toll Rd / Rankin Hardy Toll R - Apt: - Bldg: - Key Map:373F - Cross Streets: - Box #:1104
  - - 29A1 MOTOR VEHICLE A - N Sam Houston Pkwy E / Hardy Bw 8 W - Apt: - Bldg: - Key Map:373S - Cross Streets: - Box #:4102
 E11 - - 29A1 MOTOR VEHICLE A - N Sam Houston Pkwy E / Hardy Bw 8 W - Apt: - Bldg: - Key Map:373S - Cross Streets: - Box #:4102
 SQ31 - 29D02n - 29D2 MOTOR VEHICLE A - E Hardy Rd / N Sam Houston Pkwy E - Apt: - Bldg: THE PIT HA - Key Map:373T - Cross Streets: - Box #:1106
 SQ31 - - BREATHING PROBLEMS - 17630 Wayforest Dr - Apt: 390 - Bldg: AGUSTA - Key Map:373J - Cross Streets:HARVEST TIME LN/RICHCREST DR - Box #:1106
 E11 - 06C01A - 6C1 RESPIRATORY - 17630 Wayforest Dr - Apt: 390 - Bldg: AGUSTA - Key Map:373J - Cross Streets:HARVEST TIME LN/RICHCREST DR - Box #:1106
 SQ31 - - 29A1 MOTOR VEHICLE A - E Richey Rd / Oil Center Blvd - Apt: - Bldg: - Key Map:333X - Cross Streets: - Box #:1102
 SQ31 - - 29A1 MOTOR VEHICLE A - 16600 E Hardy Rd - Apt: - Bldg: - Key Map:373K - Cross Streets:Dead End/RANKIN RD - Box #:1106
 E11 - - 69D6 FIRE IN THE RE - 14330 E Hardy Rd - Apt: - Bldg: MOGAS - Key Map:373X - Cross Streets:PINAFORE LN/BUSCHONG - Box #:1107
 E31 - 69D03 - 69D3 COMMERCIAL BUI - 14330 E Hardy Rd - Apt: - Bldg: MOGAS - Key Map:373X - Cross Streets:PINAFORE LN/BUSCHONG - Box #:1107
 SQ31 - 12A02E - 12A2 SEIZURES - 830 Vista Grove Cir - Apt: - Bldg: - Key Map:332Z - Cross Streets:GROVEDALE DR/GROVEDALE DR - Box #:1108
 E11 - - 29A1 MOTOR VEHICLE A - N Sam Houston Pkwy E / Aldine Westf - Apt: - Bldg: - Key Map:373U - Cross Streets: - Box #:1106
 E31 - - ODOR(STRANGE, UNKNOW - 507 Remington Lodge Ct - Apt: - Bldg: - Key Map:372D - Cross Streets:REMINGTON PARK DR/Dead End - Box #:1103
 E11 - 69D09 - 69D9 SHED ON FIRE - 14615 Aldine Westfield Rd - Apt: - Bldg: CHRISTIAN - Key Map:373Z - Cross Streets:BUSCHONG/ALDINE BENDER RD - Box #:1107
 SQ31 - 29D02m - 29D2 MOTOR VEHICLE A - 818 Richcrest Dr - Apt: - Bldg: GREENBRIAR - Key Map:373J - Cross Streets:CHISHOLM TRL/WAYFOREST DR - Box #:1106
 SQ31 - - 29A1 MOTOR VEHICLE A - North Fwy / Airtex - Apt: - Bldg: - Key Map:372C - Cross Streets: - Box #:1103
 E31 - 52C03G - 52C3 ALARMS - 16110 North Fwy - Apt: - Bldg: CARMAX - Key Map:332U - Cross Streets:RICHEY EXIT NB 45/RICHEY ENTR - Box #:1101
 E31 - - 53A1 CITIZEN ASSIST - 19423 Remington Prairie Dr - Apt: - Bldg: - Key Map:372G - Cross Streets:REMINGTON GREEN CT/REMINGTON B - Box #:1103
 E11 - 69D06 - 69D6 FIRE IN THE RE - 2719 Brea Crest St - Apt: - Bldg: - Key Map:414N - Cross Streets:INNSBURY DR/SHADY LN - Box #:2502
 E21 - - 52B1 ALARMS - 2700 W W Thorne Blvd - Apt: - Bldg: N HARRIS C - Key Map:333S - Cross Streets:E HARDY RD/OIL CENTER BLVD - Box #:1102
 SQ31 - 10D04 - 10D4 CARDIAC - 22226 Queenbury Hills Dr - Apt: - Bldg: - Key Map:332R - Cross Streets:LANCASTER LAKE DR/RED ALDER CI - Box #:1108
 SQ31 - - BREATHING PROBLEMS - 15403 E Vantage Pkwy - Apt: - Bldg: THE WORD C - Key Map:373V - Cross Streets:N SAM HOUSTON PKWY E/WAVERLY D - Box #:1106
 E11 - 06D02 - 6D2 RESPIRATORY - 15403 E Vantage Pkwy - Apt: - Bldg: THE WORD C - Key Map:373V - Cross Streets:N SAM HOUSTON PKWY E/WAVERLY D - Box #:1106

 
 *** Not parsing 
 []  - - 32B1 UNKNOWN PROBLEM - 123 Test Street - Apt: - Bldg: TESTING AP - Key Map:496K - Cross Streets:Dead End/Dead End - Box #:4104\n
 
 */
 
 public class TXHarrisCountyESD1BParser extends FieldProgramParser {
   
   private static final Pattern REPORT_PTN = Pattern.compile("^ID#:[^ ]+ *- *UNIT:[^ ]+ .*- *DISP:");
   private static final Pattern DELIM = Pattern.compile("(?:^| )-");
 
   public TXHarrisCountyESD1BParser() {
     super("HARRIS COUNTY", "TX",
            "( ID/Z ADDRCH! Cross_Streets:X Key_Map:MAP " +
            "| ( ID UNIT2? | UNIT ) CODE CALL! PREALERT? ADDR! Apt:APT! Bldg:PLACE Key_Map:MAP% Cross_Streets:X Box_#:BOX )");
   }
   
   @Override
   public String getFilter() {
     return "cadnoreply@proxy.hcec.com,93001";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (REPORT_PTN.matcher(body).find()) {
       data.strCall = "RUN REPORT";
       data.strPlace = body;
       return true;
     }
     return parseFields(DELIM.split(body), data);
   }
   
   @Override
   public String getProgram() {
     return super.getProgram() + " PLACE";
   }
   
   private class MyIdField extends IdField {
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("ID#:")) return false;
       super.parse(field.substring(4).trim(), data);
       return true;
     }
     
     @Override
     public void parse(String field, Data data) {
       if (!checkParse(field, data)) abort();
     }
   }
   
   // Address change field 
   private class AddressChangeField extends AddressField {
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("Address changed to:")) return false;
       data.strCall = "Address changed to:";
       field = field.substring(19).trim();
       super.parse(field, data);
       return true;
     }
   }
   
   // Unit field only exists if it is tagged
   private class MyUnit2Field extends MyUnitField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("Units:")) return false;
       super.parse(field.substring(6).trim(), data);
       return true;
     }
   }
   
   private static final Pattern UNIT_PTN = Pattern.compile("[A-Z]+[0-9]+|");
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       if (!UNIT_PTN.matcher(field).matches()) abort();
       super.parse(field, data);
     }
   }
   
   // Code field has to fit specific pattern
   private static final Pattern CODE_PTN = Pattern.compile("\\d\\d[A-Z]\\d\\d[A-Za-z]?|");
   private class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(CODE_PTN, true);
     }
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(' ');
       if (pt >= 0) {
         String code = field.substring(0,pt);
         if (CODE_PTN.matcher(code).matches()) {
           if (data.strCode.length() == 0) data.strCode = code;
           field = field.substring(pt+1).trim();
         }
       }
       super.parse(field, data);
     }
   }
   
   // PreAlert is a special call extender field
   private class PreAlertField extends CallField {
     
     public PreAlertField() {
       setPattern(Pattern.compile("PreAlert"));
     }
     
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   // There are two fields, APT and BLD mapped to the apartment field.  But they
   // occur in reverse order, if both are specified, we want to to put bldg first.
   private class MyAptField extends AptField {
     @Override
     public void parse(String field, Data data) {
       data.strApt = append(field, "-", data.strApt);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("ADDRCH"))  return new AddressChangeField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("UNIT2")) return new MyUnit2Field();
     if (name.equals("CODE")) return new MyCodeField();
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("PREALERT")) return new PreAlertField();
     if (name.equals("APT")) return new MyAptField();
     return super.getField(name);
   }
   
 }

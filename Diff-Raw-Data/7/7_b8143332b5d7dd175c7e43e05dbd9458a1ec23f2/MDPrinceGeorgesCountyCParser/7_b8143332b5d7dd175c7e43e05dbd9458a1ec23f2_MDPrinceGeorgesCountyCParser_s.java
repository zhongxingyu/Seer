 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Prince Georges County, MD (Replacement for C)
 Contact: "Khaloughi, Kayman" <kkhaloughi@co.pg.md.us>
 Sender: @alert.princegeorgescountymd.gov - now
 Sender: @alert.co.pg.md.us - soon
 
 Contact: Christopher Olson <lilsmokeeater5@yahoo.com>
 E812 Suicide, HAGERSTOWN HALL - UM 258, UM, at 258 ELLICOTT DR, UM, TGA2, 1210, Units:A812, E812, F121220001
 E816 Non-Emerg Service, BOWIE HEALTH CENTER, PP, at 15001 HEALTH CENTER DR, PP, TGA2, 1608, Unit:E816, F121220004
 E841 House Fire, 3408 OLIVE BRANCH, TGA2, MAMC, Units:BO886, E831, PE841B, SQ849, TW810, F121220113
 House Fire, 3408 OLIVE BRANCH, TGA2, MAMC, Units:BO886, E831, PE841B, SQ849, TW810, F121220113
 E831 House Fire, 3408 OLIVE BRANCH, TGA2, MAMC, Units:BO886, E831, PE841B, SQ849, TW810, F121220113
 E833 BLS Amb, LANDOVER METRO STATION, PP, at 3000 PENNSY DR, PP, TGA2, 3301, Units:A830, E833, F121220089
 A836 BLS Amb, BRANDYWINE FD 40, PP, at 14201 BRANDYWINE RD, PP, TGC2, 4010, Unit:A836, F121220133
 E836 Medic Local, MOLLY BERRY RD/VAN BRADY RD, PP,  <12000/ 13899>, TGC2, 4016, Units:E836, MD845, F121220136
 A848 Medic Local, DUVAL HIGH SCHOOL, PP, at 9880 GOOD LUCK RD, PP, TGA2, 4825, Units:A848, MD830, F121220179
 A848 F121220192: Medic Local, ELEANOR ROOSEVELT HIGH SCHOOL, GP, at 7601 HANOVER PKY, GP, TGA2, 3506, Units:A848, E835, MD830
 E828 F121220324: PIA Limited Access, WB JOHN HANSON HWY WB AT NB CAP BELT OL A HWY, PP, at WB JOHN HANSON HWY WB AT NB CAP BELT OL A HWY, PP <1900/, TGA2, 0619, Units:A830, E828B
 F121230149: Assault, 4351 GARDEN CITY DR, PP, btwn CORPORATE DR and PROFESSIONAL PL, TGA2, 2809, Units:A830, E828B, MD818
 A848 F121230155: BLS Amb, 8600 GLENARDEN PKY, PP, btwn MARTIN LUTHER KING HWY and MCLAIN AVE, TGA2, 3314, Unit:A848
 F121230193: Street Alarm, 3722 HALLOWAY PL, PP, btwn HALLOWAY CT and HALLOWAY NORTH, TA11, 2002, Units:BO887, E843, E845, SQ820, TW839
 A848 F121230210: BLS Amb, DUVAL HIGH SCHOOL, PP, at 9880 GOOD LUCK RD, PP, TGA2, 4825, Unit:A848
 F121240261: PIA Limited Access, IL 495 PRIOR TO THE COUNTY LINE, TGA2, MAMC, Units:A841, BO886, PE841B, SQ814B
 F121240264: PIA Limited Access, NB CAP BELT OL A HWY AT NB BALTIMORE WASHINGTON PKY NB, PP, at NB CAP BELT OL A HWY AT NB BALTIMORE WASHINGTON PKY NB, P, TGB3, 2833, Units:A830, E828B, SQ814B
 F121240280: Medic Local, MAGNOLIA GARDENS NURSG HOME, PP, at 8200 GOOD LUCK RD, PP, TGA2, 4807, Units:A84S8, MD818
 F121330384: PIA Limited Access, WB JOHN HANSON HWY WB/NB CAP BELT OL A HWY, PP, at WB JOHN HANSON HWY WB/NB CAP BELT OL A HWY, PP <1900/0>, TGB3, 0619, Units:A848, E828B
 
 Contact: Francis Bauer <pgfd702@gmail.com>
 Sender: alrt509933@alert.princegeorgescountymd.gov
 E816 Fire Alarm-AFA, 607 JARVIS CREST CT, PP, btwn END and JOHNSTONE LN, TGA2, 4313, Unit:E816, F1212201
 PA816 F121220329: Assault, 5611 CHURCH RD, PP, btwn WESTVIEW FOREST DR and URBANA LN, TGA2, 3906, Units: E839, PA816
 F121230316: Departmental Acci, EB ANNAPOLIS RD/HILLMEADE RD, PP,  <12699/ 5899>, TGB3, 1815, Units:A839,
 
 Contact: "nhedrick36@aol.com" <nhedrick36@aol.com>
 Sender: alrt510628@alert.princegeorgescountymd.gov
 E836 F121220300: Medic Local, NB CAP BELT OL NO HWY/GLENARDEN PKY, PP, TGA2, 3332, Unit:E836
 
 Contact: "ga_cavaliere1 Cavaliere" <ga_cavaliere1@msn.com>
 Sender: rc.505@c-msg.net
 FRM:rc.505@c-msg.net\nSUBJ:CAD\nMSG:[PG11] F121230151: Acc w/Inj, 9330 BALTIMORE AVE, PP, btwn ERIE ST and FOX ST, TGA2, 1103, Units:A811B, E811\r\n\r\n
 FRM:rc.505@c-msg.net\nSUBJ:CAD\nMSG:[PG11] F121230126: Medic Local, 9020 49TH AVE, PP, btwn BLACKFOOT RD and CHEROKEE ST, TGA2, 1111, Units:A811B, MD812\r\n\r\n
 FRM:rc.505@c-msg.net\nSUBJ:CAD\nMSG:[PG11] F121220412: Fire Alarm-AFA, 9300 CHERRY HILL RD, PP, btwn PARK DR and SOUTH FARM DR, TGA2, 1101, Unit:E811\r\n\r\n
 1 of 2\nFRM:rc.505@c-msg.net\nSUBJ:CAD\nMSG:[PG11] F121220359: PIA Limited Access, NB CAP BELT OL A/SB I95, PP, at NB CAP BELT OL A/SB I95, PP\n(Con't) 2 of 2\n<23268/0>, TGB3, 1119, Units:A811B, E811, SQ814B\r\n\r\n\r\n(End)
 1 of 2\nFRM:rc.505@c-msg.net\nSUBJ:CAD\nMSG:[PG11] F121230187: Pedestrian Struck, 9591 BALTIMORE AVE, PP, btwn INDIAN LN and PERU RD, TGA2, 1103,\n(Con't) 2 of 2\nUnits:A811B, E811, MD812\r\n\r\n(End)
 FRM:rc.505@c-msg.net\nSUBJ:CAD\nMSG:[PG11] F121230226: Medic Local, 2213 APACHE ST, PP, btwn RIGGS RD and 23RD AVE, TGA2, 3405, Units:A811B, E834B, MD812\r\n\r\n
 
 Contact: John Crisman <pumperdriver@gmail.com>
 A820 F121240285: Acc w/Inj, LARGO RD AT BROCK DR, PP,  <3100/ 3099>, TGA2, 2011, Units:A820, PE846, SQ820
 
 Contact: 
 
 */
 
 public class MDPrinceGeorgesCountyCParser extends FieldProgramParser {
   
   private static final Pattern AGENCY_PTN = Pattern.compile("^([A-Z]{1,2}\\d{3}) ");
   private static final Pattern CALL_PTN = Pattern.compile("(F\\d{6,}): ");
   private static final Pattern AT_PTN = Pattern.compile("\\bAT\\b", Pattern.CASE_INSENSITIVE);
   
   public MDPrinceGeorgesCountyCParser() {
     super("PRINCE GEORGES COUNTY", "MD",
            "CALL ADDR PP? AT? X? PP2? CH! BOX Units:UNIT% UNIT+? ID");
   }
   
   @Override
   public String getFilter() {
     return "@alert.princegeorgescountymd.gov,@alert.co.pg.md.us,rc.505@c-msg.net";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     
     // Check for leading agency code
     Matcher match = AGENCY_PTN.matcher(body);
     if (match.find()) {
       data.strSource = match.group(1);
       body = body.substring(match.end()).trim();
     }
     
     // Check for leading call ID
     match = CALL_PTN.matcher(body);
     if (match.find()) {
       data.strCallId = match.group(1);
       body = body.substring(match.end()).trim();
     }
     
     body = body.replace(" Unit:", " Units:");
     if (!parseFields(body.split(","), data)) return false;
     if (data.strUnit.length() == 0) data.strUnit = data.strSource;
     data.strAddress = AT_PTN.matcher(data.strAddress).replaceAll("&");
     return true;
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames();
     }
   }
   
   // The AT field starts with an at keyword
   // It indicates that this is the real address, and what we originally
   // parsed as an address is a place name
   private class AtField extends AddressField {
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("at ")) return false;
       data.strPlace = data.strAddress;
       data.strAddress = "";
       parse(field.substring(3).trim(), data);
       if (data.strAddress.equals(data.strPlace)) data.strPlace = "";
       return true;
     }
   }
   
   // Cross field only exist if it has the correct keyword
   private class MyCrossField extends CrossField {
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("btwn ")) return false;
       field = field.substring(5).trim();
       super.parse(field, data);
       return true;
     }
   }
   
   // Unit fields join together with comma separators
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       data.strUnit = append(data.strUnit, ",", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
    if (name.equals("PP")) return new SkipField("[A-Z]{1,2}", true);
     if (name.equals("AT")) return new AtField();
     if (name.equals("X")) return new MyCrossField();
    if (name.equals("PP2")) return new SkipField("[A-Z]{1,2} *(?:<\\d.*)?|<\\d.*", true);
     if (name.equals("CH")) return new ChannelField("TG?[A-F]\\d{1,2}", true);
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("ID")) return new IdField("F\\d{6,}");
     return super.getField(name);
   }
 
   
   @Override
   public String adjustMapAddress(String sAddress) {
     // Undo various abbreviations of CAPITAL BELTWAY
     return CAP_BELT_PTN.matcher(sAddress).replaceAll("CAPITAL BELTWAY");
   }
   private static final Pattern CAP_BELT_PTN = 
       Pattern.compile("\\bCAP BELT(?:WAY)?(?: OL [A-Z]{1,2})(?: HWY)?\\b", Pattern.CASE_INSENSITIVE);
 }

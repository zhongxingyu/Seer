 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Prince Georges County, MD
 Requires reverse merge support
 Contact: Kyle Armstrong <pgfd19338@yahoo.com>
 Contact: kyle hastings <kwhastings@gmail.com
 Contact: "Trey Kelso" <tkelso@laurelvfd.org>
 Contact: "Khaloughi, Kayman" <kkhaloughi@co.pg.md.us>
 Sender: @alert.princegeorgescountymd.gov
 Sender: alrt113574@alert.princegeorgescountymd.gov
 
 A830 Incident: F111850165, Type: BLS Amb, Loc:7008 VARNUM ST, PP, btwn ALLISON ST and 71ST AVE, TalkGroup: TGA2, Box: 3005, Map: 5410 G 9, Text: Medical ProQA recommends
 E830 Incident: F111880399, Type: Medic Local, Loc: 4714 66TH PL, PP, btwn WEBSTER ST and 66TH AVE, TalkGroup: TGA2, Box: 3004, Map: 5410 F 10, Text: Medical ProQA recommends dispatch at this time, Units:A830,PE830
 A830 Incident: F111920132, Type: BLS Amb, Loc: 7321 LANDOVER RD, PP, btwn PINEBROOK AVE and KENT TOWN DR, TalkGroup: TGA2, Box: 3302, Map: 5530 H 2, Text: Police ProQA recommends dispatch at this time, Unit:A830
 A843 Incident: F111840152, Type: Suicide, Loc:2217 HINDLE LN, PP, btwn HALLOW LN and HAWK LN, TalkGroup: TGA2, Box: 1608, Map: 5413 D 9, Text: Police ProQA recommends dispatch at this time, Unit:A843
 E812 Incident: F111860096, Type: Working Code, Loc:KENT HALL - UM 22, UM, at 22 LEHIGH RD, UM, btwn PREINKERT DR and END, TalkGroup: TGA2, Box: 1207, Map: 5409 J 3, Text: UNCON MALE W/HEAD INJ, IFO BUILDING,UMPD ENROUTE, Unit:E812
 E812 Incident: F111870042, Type: Invest Any Type, Loc:CUMBERLAND HALL - UM 122, UM, at 122 VALLEY DR, UM, btwn FARM DR and END, TalkGroup: TG1, Box: 1211, Map: 5409 K 2,Text: BELLS, Unit:E812
 E812 Incident: F111870154, Type: Building Fire, Loc:UM 795 - AVRUM GUDELSKY VET, UM, at 8050 GREENMEADE DR, UM, btwn END and METZEROTT RD, TalkGroup: TA11, Box: 1109, Map: 5287 J 10, Text: SMOKE IN THE BUILD LOADING DOCK AREA UM PD L,L, Units:BO886, E712, E812, E834, E835, TK801, TK814, TW807
 E812 Incident: F111870241, Type: Fire Alarm-AFA, Loc: HAGERSTOWN HALL - UM 258, UM, at 258 ELLICOTT DR, UM, btwn HAGERSTOWN DR and FARM DR, TalkGroup: TG1, Box: 1210, Map: 5409 J 2, Text: GEN FIRE ALRM, Unit:E812
 E812 Incident: F111880093, Type: Working Code, Loc: UM 998 UNIVERSITY COMMONS 3, PP, at 4318 KNOX RD, PP, btwn ROSSBURG DR and STERLING PL, TalkGroup: TGA2, Box: 1203, Map: 5409 K 4, Text: Medical ProQA recommends dispatch at this time, Units:A812, E812, MD812
 E812 Incident: F111880115, Type: House Fire, Loc: 511 EAST INDIAN SPRING DR, TalkGroup: TG1, Box: MAMC,Text: BOX 16-7 4TH DUE ENG...7C, Unit:E812
 E812 Incident: F111880271, Type: BLS Amb, Loc: 4107 BUCK CREEK RD, PP, btwn MORTON PL and TRUDE ST, TalkGroup: TGC2, Box: 3221, Map: 5765 K 2, Text: Medical ProQA recommends dispatch at this time, Unit:E812
 TK812 Incident: F111890027, Type: Street Alarm, Loc: 7867 BEAVER DAM RD, PP, btwn SHEEP RD and RESEARCH RD, TalkGroup: TGA9, Box: 3120, Map: 5288 K 6, Text: Fire ProQA recommends dispatch at this time, Units:BO886, E831, E835, TK812, TK814
 TK812 Incident: F111890028, Type: House Fire, Loc: 36 RIDGE RD #Q, GP, btwn HAMILTON PL and RIDGE CT, TalkGroup: TGA4, Box: 3510, Map: 5289 A 10, Text: CALLER ADV SHE CAN SEE AND SMELL SMOKE...., Units:BO886, E807, E811, E818, E835, TK812, TK814, TK831
 A810 Incident: F111900107, Type: BLS Amb, Loc: 11217 BASSWOOD TER, PP, btwn BASSWOOD CT and END, TalkGroup: TGA2, Box: 4929, Map: 5289 H 3, Text: Medical ProQA recommends dispatch at this time, Unit:A810
 E810 Incident: F111900138, Type: Fire Alarm-AFA, Loc: 14402 LAUREL PL, LP, btwn MULBERRY ST and CATALPA ST, TalkGroup: TG1, Box: 1017, Map: 5169 C 6, Text: Fire ProQA recommends dispatch at this time, Unit:E810
 A810 Incident: F111900241, Type: BLS Amb, Loc: 14200 LAUREL PARK DR, LP, btwn PARK CENTER DR and VAN DUSEN RD, TalkGroup: TGA2, Box: 1016, Map: 5168 K 7, Text: Medical ProQA recommends dispatch at this time RM 103 BED A, Unit:A810
 TW810 Incident: F111910137, Type: House Fire, Loc: 10 MAIN ST, LP, btwn END and LAFAYETTE AVE, TalkGroup: TGA2, Box: 4911, Map: 5169 F 4, Text: CHILD ADV FIRE HERE HIS FRIEND LIVES HERE THEN DISC LINE CANT CALLBACK, Units:AE27, BO886, E831, E849, HE11, HTK06, TK715, TW810
 E820 Incident: F111970009, Type: Medic Local, Loc: 12124 WHEELING AVE, PP, btwn END and BASKERVILLE PL,TalkGroup: TGC2, Box: 4503, Map: 5768 C 7, Text: Medical ProQA recommends dispatch at this time, Units:A823, E820, MD825
 E808 Incident: F111940223, Type: Street Alarm, Loc: 6982 WALKER MILL RD, PP, btwn KAREN BLVD and SHADY GLEN DR, TalkGroup: TGC3, Box: 2617, Map: 5650 J 1, Text: Fire ProQA recommends dispatch at this time, Units:BO883, E808, E826, QT838, TK829 Eff Body:its:BO883, E808, E826, QT838, TK829
 E808 Incident: F111940099, Type: Medic Local, Loc: ROLLINS AVE/DENISE DR, PP,  <1200/ 1199>, TalkGroup:TGA2, Box: 0820, Map: 5650 F 1, Text: Medical ProQA recommends dispatch at this time, Units:A826, E808,MD846
 E808 Incident: F111940284, Type: Medic Local, Loc: 7945 CENTRAL AVE, PP, btwn JONQUIL AVE and WESTHAMPTON AVE, TalkGroup: TGA2, Box: 0806, Map: 5531 A 8, Text: Medical ProQA recommends dispatch at this time, Units:E808, PA838
 E820 Incident: F111970009, Type: Medic Local, Loc: 12124 WHEELING AVE, PP, btwn END and BASKERVILLE PL,TalkGroup: TGC2, Box: 4503, Map: 5768 C 7, Text: Medical ProQA recommends dispatch at this time, Units:A823, E820, MD825
 TK834 Incident: F112000125, Type: Apartment Gas Leak, Loc: 1521 MADISON ST #204, PP, btwn NICHOLSON ST and 16TH AVE, TalkGroup: TGA2, Box: 4401, Map: 5409 D 7, Text: Fire ProQA recommends dispatch at this time, Units:BO884, E855, PE844, TK801, TK834
 E810 Incident: F112050265, Type: Fire Alarm-AFA, Loc: 14402 LAUREL PL, LP, btwn MULBERRY ST and CATALPA ST, TalkGroup: TG1, Box: 1017, Map: 5169 C 6, Text: Fire ProQA recommends dispatch at this time, Unit:E810
 E808 Incident: F112050338<tel:112050338>, Type: Apartment Gas Leak, Loc: 5231 MARLBORO PIKE, PP, btwn SHAMROCK AVE and LEE JAY DR, TalkGroup: TGA4, Box: 2627, Map: 5650 D 2, Text: Fire ProQA recommends dispatch at this time, Units:BO883, E808, E817, QT838, TK829
 RE833 Incident: F112500109, Type: PIA Limited Access, Loc: WB JOHN HANSON HWY WB/BALTIMORE WASHINGTON PK Y NB, PP, at WB JOHN HANSON HWY WB/BALTIMORE WASHINGTON PKY NB, PP <0, TalkGroup: TGA2, Box: 2213, Map: 5530 A 3, Text: COMPL ADV OF A SIG 9I, LOC IS NEAR THE RAMP LOC IS B4 THE KENILWORTH EXIT ON THE RIGHT// E809, RE833
 
 Contact:Christopher Olson <lilsmokeeater5@yahoo.com>
 Sender:  alrt383233@alert.princegeorgescountymd.gov
 BC 882 Incident: F120220163, Type: House Gas Leak, Loc: 12112 FORGE LN, PP, btwn FEDERAL LN and FOXHILLLN, TalkGroup: TGG3, Box: 3910, Map: 5412 K 6, Text: Fire ProQA recommends 
 TK828 Incident: F120220163, Type: House Gas Leak, Loc: 12112 FORGE LN, PP, btwn FEDERAL LN and FOXHILL LN, TalkGroup: TGG3, Box: 3910, Map: 5412 K 6, Text: Fire ProQA recommends 
 BC 886 Incident: F120220232, Type: Street Alarm, Loc: 4726 MANGUM RD, PP, btwn 47TH PL and MUSKOGEE ST,TalkGroup: TGG3, Box: 1102, Map: 5288 B 8, Text: *CALLER ADV SMELL SMOKE FRM BASEMENT, Units:BO886, E811B, E812B, TK812, TK814
WI Incident: F120710288, Type: PIA Limited Access, Loc: MANGO CAFE, BP, at 4719 ANNAPOLIS RD, BP, btwn K ENILWORTH AVE and 48TH ST, TalkGroup: TGA2, Box: 0914, Map: 5410 A 10, Text: 2 VEH ACC UNBK INJ UNK TRAP PED PEOPLE IN OTHER CALL CRYING, Unit:WI
 
 Contact: support@active911.com
 Subject: CAD Feed
 Engine 811 Incident: F120060261, Type: Medic Local, Loc: 11000 BALTIMORE AVE #105, PP, btwn SELLMAN RD and HARFORD AVE, TalkGroup: TGA2, Box: 3104, Map: 5288 E 5, Text: Medical ProQA recommends dispatch at this time, Units:A814, E811B, MD810\n\nSent to Prince George's alert recipients (E-mail, Wireless) through Alert Prince George's\n... powered by Cooper Notification's Roam Secure Alert Network\n--\n
 
 */
 
 public class MDPrinceGeorgesCountyCParser extends FieldProgramParser {
   
   public MDPrinceGeorgesCountyCParser() {
     super("PRINCE GEORGES COUNTY", "MD",
            "SRC! Incident:ID! Type:CALL! Loc:ADDR! PP ( AT PP | ) X? TalkGroup:CH% Box:BOX% Map:MAP? Text:INFO+ Units:UNIT+");
   }
   
   @Override
   public String getFilter() {
     return "@alert.princegeorgescountymd.gov";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     body = body.replace(" Incident:", ", Incident:");
     body = body.replace(" Unit:", " Units:");
     if (!parseFields(body.split(","), data)) return false;
     if (data.strUnit.length() == 0) data.strUnit = data.strSource;
     return true;
   }
   
   //  The PP field isn't parsed, but it must match PP or LP or UM
   private class PPField extends SkipField {
     public PPField() {
      setPattern(Pattern.compile("(?:PP|PL|GP|LP|UM|BP)(?: .*)?"), true);
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
       return true;
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames();
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
   
   // Info field needs to skip junk
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (field.contains("ProQA ")) return;
       super.parse(field, data);
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
     if (name.equals("PP")) return new PPField();
     if (name.equals("AT")) return new AtField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
 }

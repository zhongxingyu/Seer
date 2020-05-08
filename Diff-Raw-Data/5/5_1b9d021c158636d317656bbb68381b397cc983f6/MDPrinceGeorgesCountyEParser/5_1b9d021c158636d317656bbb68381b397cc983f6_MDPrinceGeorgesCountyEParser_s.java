 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Prince Georges County, MD (Replacement for C)
 Contact: Christopher Olson <lilsmokeeater5@yahoo.com>
 Cotnact: Tommy Scaggs <rtscaggs@gmail.com>
 Contact: kyle hastings <kwhastings@gmail.com>
 Contact: Robin Scaggs <Robin.Scaggs@erickson.com>
 Sender: @alert.co.pg.md.us
 
 (CAD Feed) ALS1, Medic Local, 8500 MAGNOLIA DR, PP, btwn WOOD THRUSH DR and BLUE BIRD CT, TGA2, 4820, 5411 C 4, Medical ProQA recommends dispatch at this time, Units:A848, MD818 - From FED01 06/14/2012 19:47:08\n\n
 PIAT, PIA w/Entrapment, NB CAP BELT OL A HWY AT NB INDIAN HEAD HWY, PP, at NB CAP BELT OL A HWY AT NB INDIAN HEAD HWY, PP <6298/0>, TGB3, 4219, 5765 A 2, 3 VEHICLE ACC, AT LEAST ONE BLU VEH INTO THE JERSEY WALL,..POSS ENTRAPPED... UNK INJ, Units:A847, BO885, E821B, MD847, PE842, SQ847 - From FED03 06/14/2012 19:31:19
 (CAD Feed) ALS1, Medic Local, 7737 RIVERDALE RD #204, PP, btwn MAHONEY DR and ANNAPOLIS RD, NEW CARROLLTON, TGA2, 2831, 5410 K 7, Medical ProQA recommends dispatch at this time, Units:A830, E828, PE830 - From FED01 06/14/2012 19:11:54
 STREET, Street Alarm, 3745 WILKINSON DR, PP, btwn CHERRY DR and PARK BLVD, SUITLAND, TC11, 2906, 5649 J 7, Fire ProQA recommends dispatch at this time, Units:BO883, E826B, E829B, QT838, TK832 - From FES01 06/14/2012 20:51:25
 (CAD Feed) PIAT, PIA w/Entrapment, POWDER MILL RD/LAUREL BOWIE RD, PP,  <9999/ 11400>, LAUREL, TGB3, 1902, 5289 K 3, Police ProQA recommends dispatch at this time, Units:BO882, MD818 - From FED03 06/14/2012 22:19:35
 (CAD Feed) HOUSEF, House Fire, 9300 ALLENTOWN RD, PP, btwn PINEHURST DR and CONSTANTINE DR, FORT WASHINGTON, TGC3, 3206, 5765 G 9, Fire ProQA recommends dispatch at this time, Units:BO885, E821B, E823B, E832B, PE842, SQ847, TK825, TK829 - From FED10 06/14/2012 22:25:10
 (CAD Feed) PIAH, PIA Limited Access, SB CAP BELT IL A HWY/SB BALTIMORE WASHINGTON PKY SB, PP, at SB CAP BELT IL A HWY/SB BALTIMORE WASHINGTON PKY SB, PP, TGB3, 3516, 5410 J 2, COMPL ADV 2 CAR ACC BROWN MITSUBISHI VS GIANT FOOD TRACTOR TRAILER, UNK INJ, FEM MAY NEED AMBO. WILL NOTF MSP, Units:A830, E828 - From FED03 06/15/2012 00:59:09
 ALS1, Medic Local, 7321 SPLIT RAIL LN, LP, btwn MILL CREEK CT and DORSET RD, LAUREL,MD, TGA2, 1013, 5168 K 3, Medical ProQA recommends dispatch at this time, Units:A724, E810C, HMD06 - From FED01 06/15/2012 13:11:28
 DEPFD, Departmental Acci, SB CAP BELT IL A HWY/NB BALTIMORE WASHINGTON PKY NB, PP, at SB CAP BELT IL A HWY/NB BALTIMORE WASHINGTON PKY NB, PP, TGA2, 3520, 5410 J 2, EMSDO REAR ENDED BY A VEHICLE ON THE BELTWAY WHILE SHE WAS RESPONDING TO PGTC....UNABLE TO GET A DESCRIPTION OF THE STRIKING VEHICLE....PROPERTY DAMAGE ONLY, Units:BO886, E835, EMSDO, NSO, SO8 00
 F121790236: Brush Fire, 8310 ANNAPOLIS RD, PP, btwn RIVERDALE RD and 85TH AVE, TGA2, 2831, Unit:E828
 F121790235: BLS Amb, TIPICOS LAS AMERICAS, PP, at 9452 LANHAM SEVERN RD, PP, TGA2, 4816, Unit:A848
 F121800211: Acc w/Inj, EB ANNAPOLIS RD BEFORE NB CAP BELT OL A HWY, PP,\2s<0/ 0>, TGA2, 2806, Units:A830, TK828
 F121800225: Medic Local, 8300 QUINTANA ST, PP, btwn END and 84TH AVE, TGA2, 2825, Units:A807, E828, MD818
 F121800232: Medic Local, 9206 5TH ST, PP, btwn MAIN ST and END, TGA2, 4809, Units:A848, MD830
 F121800279: PIA w/Entrapment, EQUESTRIAN CENTER, PP, at 5651 WATER ST, PP, TGB3, 2020, Units:A823, BO887, E845, MD845, SQ820
 F121800373: Collapse, 1501 CABIN BRANCH DR, PP, btwn SAVAGE ST and COLUMBIA PARK RD, TGA7, 3808, Unit:TW810

 
 Contact: Active911
 [CAD Feed] STREET, Street Alarm, 9001 MCHUGH DR, PP, btwn END and CAMPUS WAY N, TA11, 0616, 5531 D 3, ODOR OF ELECTRICAL BURNING IN THE STORE, Units:BO881, E833, E837, QT838, TK828\n
 [CAD Feed] ELEV, Stuck Elevator, 9301 ANNAPOLIS RD, PP, btwn MORLEY RD and WASHINGTON BLVD, TGA2, 4802, 5411 D 6, Fire ProQA recommends dispatch at this time, Unit:E828B\n
 [CAD Feed] PIAH, PIA Limited Access, 2200 CAP BELT OL A HWY, PP, btwn BALTIMORE WASHINGTON PKY SB and NB CAP BELT HWY OFRP SB, TGB3, 2833, 5410 J 2, CALLER ADV THAT A TIRE FELL OFF OF A VEH AND MADE A VEH SPIN OUT ADVI TS ON THE LEFT SIDE, Units:A819, E828B, SQ814\n
 [CAD Feed] HOUSEF, House Fire, 400 COMMERCE DR, PP, btwn COMMERCE CT and TRADE ZONE AVE, TGA3, 4308, 5533 D 9, Fire ProQA recommends dispatch at this time, Units:BO882, E816, E820B, E845, PE846, QT838, RE833, TW839\n
 [CAD Feed] OUTF, Outside Fire, EB JOHN HANSON HWY EB/NB MARTIN LUTHER KING HWY, PP, at EB JOHN HANSON HWY EB/NB MARTIN LUTHER KING HWY, PP <939, TGA2, 2832, 5411 E 9, Fire ProQA recommends dispatch at this time, Unit:E828\n
 [CAD Feed] PIAT, PIA w/Entrapment, 9052 KENILWORTH AVE, GP, btwn CAP BELT OL A HWY and NB CAP BELT HWY OFRP SB KENILWORT, TGB3, 3519, 5288 G 10, ONE OVERTURNED, Units:A831, BO886, E811, MD712, SQ814\n
 [CAD Feed] PIAH, PIA Limited Access, EB JOHN HANSON HWY EB/NB CRAIN HWY NE, PP, at EB JOHN HANSON HWY EB/NB CRAIN HWY NE, PP <4698/0>, TGB3, 1619, 5413 F 8, VEH ACCIDENT POSSIBLE INJ.. ADV HER VEH IS SMOKING, Units:A830, E828\n
 [CAD Feed] ALS1, Medic Local, 7501 GOOD LUCK RD, PP, btwn CATHEDRAL AVE and CAP BELT IL SO HWY, TGA2, 4812, 5410 K 4, C, Units:A830, E828, MD818\n
 [CAD Feed] COLAPI, Collapse Invest, 6805 10TH AVE, PP, btwn FAIRVIEW AVE and EAST WEST HWY, TGD3, 4411, 5409 B 5, Police ProQA recommends dispatch at this time, Units:BO884, MD844, PE844, SQ801, TS814\n
 [CAD Feed] PIA, Acc w/Inj, 8796 ANNAPOLIS RD, PP, btwn SB CAP BELT HWY ONRP EB ANNAPOLIS RD and SB CAP BELT HW, TGA2, 2805, 5411 B 7, Police ProQA recommends dispatch at this time, Units:A833, E828\n
 [CAD Feed] DEPFD, Departmental Acci, FIRESTATION 31 - BELTSVILLE, PP, at 4911 PRINCE GEORGES AVE, PP, btwn TALBOT AVE and QUEEN ANNE AVE, TGA2, 3130, 5288 E 4, U831 INVOLVED IN PDO...REQ PD AND NOTIFICATIONS ...NO INJ, Units:BO886, NSO, SO800, U831\n
 [CAD Feed] DEPFD, Departmental Acci, FIRESTATION 31 - BELTSVILLE, PP, at 4911 PRINCE GEORGES AVE, PP, btwn TALBOT AVE and QUEEN ANNE AVE, TGA2, 3130, 5288 E 4, U831 INVOLVED IN PDO...REQ PD AND NOTIFICATIONS ...NO INJ, Units:BO886, NSO, SO800, U831\n
 [CAD Feed] STREET, Street Alarm, 6900 JARRETT AVE, PP, btwn END and DUMFRIES ST, TC11, 2108, 5765 C 3, SMELLS BURNING IN THE UTILITY CLOSET, Units:BO885, E832B, PE842, SQ847, TW821\n
 [CAD Feed] BUILDF, Building Fire, 905 MARCY AVE #3T, PP, btwn NEPTUNE AVE and DEAL DR, TGC3, 4203, 5649 A 10, Units:BO885, E829B, E832B, E847, PE842, SQ827, TK825, TW821\n
 [CAD Feed] ALS1, Medic Local, 8120 GAVIN ST, PP, btwn END and QUENTIN ST, TGA2, 4811, 5410 J 6, Medical ProQA recommends dispatch at this time, Units:A848, MD830\n
 [CAD Feed] BLS, BLS Amb, 9008 ANNAPOLIS RD, PP, btwn PRINCESS GARDEN PKY and LANHAM SEVERN RD, TGA2, 4805, 5411 B 7, MSPCP ONSCENE, 1 WITH A HEAD INJURY, REQ AMBO, Units:A813, E828\n
 [CAD Feed] ALS2, Medic Local, MAGNOLIA GARDENS NURSG HOME, PP, at 8200 GOOD LUCK RD, PP, btwn HANOVER PKY and MALLERY DR, TGA2, 4807, 5411 B 3, Medical ProQA recommends dispatch at this time, Units:A848, E848, MD818\n
 [CAD Feed] PIAH, PIA Limited Access, O/L 49/ NEW HAMPSHIRE AVE, TGA2, MAMC, BETWEEN CO-LLINE AND NEW HAMPSHIRE A41 PE41 SQ14, Units:A841, BO886, PE841, SQ814\n
 [CAD Feed] TOWNHF, Townhouse Fire, 626 BRIGHT SUN DR, PP, btwn LAZY DAY LN and END, TGA3, 4604, 5531 J 6, Fire ProQA recommends dispatch at this time, Units:BO881, E806, E833, E837, PE846, QT838, RE818, TK828\n
 [CAD Feed] ALS1, Medic Local, 9301 ANNAPOLIS RD, PP, btwn MORLEY RD and WASHINGTON BLVD, TGA2, 4802, 5411 D 6, Medical ProQA recommends dispatch at this time, Units:A848, MD830\n
 [CAD Feed] ALS1, Medic Local, 5100 PHILADELPHIA WAY #B, PP, btwn PHILADELPHIA CT and END, TGA2, 4832, 5411 F 7, Medical ProQA recommends dispatch at this time, Units:A806, E828, MD812\n
 [CAD Feed] STREET, Street Alarm, 4678 SUITLAND RD, PP, btwn HOMER AVE and HURON AVE, TC11, 1708, 5650 B 6, Units:BO883, E817, E826B, SQ827, TK829\n
 [CAD Feed] SHOT, Shooting, 7104 LORY LN, PP, btwn LOIS LN and LOIS LN, TGA2, 2824, 5410 H 6, Police ProQA recommends dispatch at this time, Units:A813, E828, MD830\n
 [CAD Feed] DEPFD, Departmental Acci, CLINTON FD 25, PP, at 9025 WOODYARD RD, PP, btwn CLINTON ST and PISCATAWAY RD, TGC2, 2507, 5766 G 8, MD825 BACKED INTO THE BUILDING...NO ONE INJURED.., Units:BO887, SBO, SDC, SO800\n
 [CAD Feed] BLS0, BLS Amb, 9801 LANHAM SEVERN RD, PP, btwn 98TH AVE and SANTA CRUZ ST, TGA2, 4804, 5411 F 4, Medical ProQA processing complete, Unit:A848\n
 [CAD Feed] BLS, BLS Amb, METRO NEW CARROLLTON - R, PP, at 4300 GARDEN CITY DR, PP, btwn WB JOHN HASON HWY OFRP GARDEN CITY DR and CORPORATE DR, TGA2, 2809, 5411 A 9, HAVE A EMPLOYEE WITH HEAT RELATED ILLNESS WILL BE AT THE METRO YARD NOT THE STATION ... IN THE ADMIN BUILDING, Units:A806, E828\n
 [CAD Feed] BLS0, BLS Amb, 9537 ELVIS LN, PP, btwn LURIA LN and ANITA LN, TGA2, 4822, 5411 F 3, Medical ProQA recommends dispatch at this time, Unit:A848\n
 [CAD Feed] HOUSEG, House Gas Leak, 7412 HENDRICKS DR, PP, btwn END and 76TH AVE, TA11, 2819, 5410 J 8, Fire ProQA recommends dispatch at this time, Units:A809, BO882, E828, PE830, RE833, TK809\n
 [CAD Feed] HOUSEG, House Gas Leak, 7412 HENDRICKS DR, PP, btwn END and 76TH AVE, TA11, 2819, 5410 J 8, Fire ProQA recommends dispatch at this time, Units:A809, BO882, E828, PE830, RE833, TK809\n
 [CAD Feed] STREET, Street Alarm, 20317 AQUASCO RD, PP, btwn CROOM RD and AQUASCO FARM RD, TGC3, 3606, 6119 D 3, Fire ProQA recommends dispatch at this time, Units:BO887, CSQ02, CTK03, CTN02, E836, E840\n
 [CAD Feed] PIA, Acc w/Inj, DOCTORS HOSPITAL, PP, at 8118 GOOD LUCK RD, PP, btwn HANOVER PKY and MALLERY DR, TGA2, 4807, 5411 B 3, CIT CALLED STATION ADV 9I INFRONT OF THE HOSPITAL ON GOOD LUCK, Units:A848, E828\n
 [CAD Feed] PIA, Acc w/Inj, DOCTORS HOSPITAL, PP, at 8118 GOOD LUCK RD, PP, btwn HANOVER PKY and MALLERY DR, TGA2, 4807, 5411 B 3, CIT CALLED STATION ADV 9I INFRONT OF THE HOSPITAL ON GOOD LUCK, Units:A848, E828\n
 [CAD Feed] LOC, Lock In/Out, 9443 ANNAPOLIS RD, PP, btwn FONTANA DR and CARTER AVE, TGA2, 4802, 5411 E 6, ANIMAL LOCKED INSIDE OF CAR,,, Unit:E828\n
 
 Contact: CodeMessaging
 [PG09] ALS1, Medic Local, 3521 54TH AVE #1, PP, btwn MACBETH ST and NEWTON ST, TGA2, 0916, 5530 B 1, Medical ProQA recommends dispatch at this time, Units:A830, E809B, PE830
 [PGWFD] F121510277: House Fire, 5311 LORRAINE DR, PP, btwn JOYCE DR and END, TGC3, 2931, Units:A827, EMSDO, MD829, SSO, WI
 [BATT6] F121520221: House Gas Leak, 3713 GREEN ASH CT, PP, btwn GREEN ASH LN and END, TA11, 4101, Units:BO886, PE712, PE841, TK812, TK831
 [PGWFD] F121520220: Building Fire, 4221 BRANCH AVE, PP, btwn SB BRANCH AVE ONRP EB IVERSON ST and ST BARNABAS RD, TGC3, 2907, Units:A827, BO881, EMSDO, MD826, NSO, WI
 [SAFE] F121520220: Building Fire, 4221 BRANCH AVE, PP, btwn SB BRANCH AVE ONRP EB IVERSON ST and ST BARNABAS RD, TGC3, 2907, Units:A827, BO881, EMSDO, MD826, NSO, WI
 [PGWFD] F121520346: PIA w/Entrapment, 2254 HANNON ST, PP, btwn FORDHAM ST and 23RD AVE, TGB3, 3412, Unit:WI
 [BATT4] F121530476: Street Alarm, 4108 CRITTENDEN ST, HP, btwn 40TH PL and RHODE ISLAND AVE, TA11, 5511, Units:BO884, E809B, E855, TK809, TW807
 [BATT6] F121540040: House Fire, 3307 MAJOR DENTON DR, PP, btwn CHERRY HILL RD and CHERRY HILL CT, TGA3, 4101, Units:BO886, E811B, E812B, E831, E834B, TK812, TK814, TW715
 [BATT4] F121540061: PIA w/Entrapment, 9200 NEW HAMPSHIRE AVE, PP, btwn METZEROTT RD and END, TGB3, 3410, Units:A834, BO884, E834B, MD712, SQ814
 [BATT6] F121540097: House Fire, 7525 LOCKMAN LN, PP, btwn CEDARHURST DR and LONGWOOD DR, TGA3, 3115, Units:BO886, E811B, E831, E849, PE841, TK812, TK814, TW810
 [SAFE] F121540252: Water Rescue, NATIONAL HARBOR, PP, at 300 WATERFRONT ST, PP, TGD3, 5801, Units:BO885, BT847, BT856, E847, EMSDO, MD847, NSO, PA842, PE842, TR806, WI, WR856, WR857
 
 */
 
 public class MDPrinceGeorgesCountyEParser extends FieldProgramParser {
   
  private static final Pattern ID_PTN = Pattern.compile("^(F\\d{6,}):");
   private static final Pattern TRAILER = Pattern.compile(" - From [A-Z0-9]+ (\\d\\d/\\d\\d/\\d{4}) (\\d\\d:\\d\\d:\\d\\d)$");
   private static final Pattern AT_PTN = Pattern.compile("\\bAT\\b", Pattern.CASE_INSENSITIVE);
   
   public MDPrinceGeorgesCountyEParser() {
     super("PRINCE GEORGES COUNTY", "MD",
            "CODE? CALL ADDR PP? AT? X? PP2? ( CITY ST CH | CITY CH | CH! ) BOX MAP INFO+ Units:UNIT% UNIT+");
   }
   
   @Override
   public String getFilter() {
     return "@alert.co.pg.md.us,rc.505@c-msg.net";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     Matcher match = ID_PTN.matcher(body);
     if (match.find()) {
       data.strCallId = match.group(1);
       body = body.substring(match.end()).trim();
     }
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     
     match = TRAILER.matcher(body);
     if (match.find()) {
       data.strDate = match.group(1);
       data.strTime = match.group(2);
       body = body.substring(0,match.start()).trim();
     }
     
     body = body.replace(" Unit:", " Units:");
     if (!parseFields(body.split(","), data)) return false;
     if (data.strUnit.length() == 0) data.strUnit = data.strSource;
     data.strAddress = AT_PTN.matcher(data.strAddress).replaceAll("&");
     
     // Truncated messages may confuse PP field for address
     if (data.strAddress.length() < 5 || data.strAddress.contains("<")) return false;
     
     if (data.strState.equals("MD")) data.strState = "";
     
     return true;
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram() + " DATE TIME";
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
   
   // Info field drops ProQA comments
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (field.contains("ProQA recommends dispatch")) return;
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
     if (name.equals("CODE")) return new CodeField("[A-Z]+\\d?");
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("PP")) return new SkipField("[A-Z]{1,2}|", true);
     if (name.equals("AT")) return new AtField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("PP2")) return new SkipField("[A-Z]{1,2} *(?:<\\d.*)?|<\\d.*|", true);
     if (name.equals("CH")) return new ChannelField("T?G?[A-F]\\d{1,2}", true);
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("UNIT")) return new MyUnitField();
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

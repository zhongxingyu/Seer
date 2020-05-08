 package net.anei.cadpage.parsers.IN;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Porter County, IN
 Contact: Chad Elwood <chadelwood@gmail.com>
 Contact: "Gregory Adair" <gadair@washingtonfire.net>
 Contact: Mike Luczak <firefighter3198@gmail.com>
 Sender: pcdisp@porterco-ps.org
 System: Tiberon
 
 Subject:CAD Page\n\n#110089211 - \nF35P2:\nEMS AST-P2 @ 380 N 400 E,WGT\nCROSS: btwn AUGUSTINE DR and E 400 N\npcdisp@porterco-ps.orgGRP:WF\nPRI:2\ncomment: Medical P
 Subject: CAD Page\n\n#110085268 - \nF35:\nMISC. AST EMS @ 4 WILLIAMSBURG DR,WGT\nCROSS: btwn DEAD END and VIRGINIA CT\nGRP:WF\nPRI:4\ncomment: UN
 Subject: CAD Page\n\n#110085607 -\nF35P2:\nEMS AST-P2 @ 342 N 325 E,WGT\nCROSS: btwn LONG PRAIRIE RD and STREAMWOOD DR GRP:WF\nPRI:2\ncomment:
 Subject: CAD Page\n\n#110086732 - \nF3:\nELECTRICAL FIRE @ 3705 VALE PARK RD,WGT\nCROSS: btwn ST RD 49 and BARTZ RD\nGRP:WF\nPRI:1\ncomment: 2 PO
 Subject: CAD Page\n\n#110086764 - \nF33:\nMANPOWER AST EMS @ 13 WILLIAMSBURG DR,WGT\nCROSS: btwn DEAD END and JAMESTOWN AV\nGRP:WF\nPRI:1\ncommen
 Subject: CAD Page\n\n#110087784 - \nF35P2:\nEMS AST-P2 @ WASHINGTON TWP HIGH SCHOOL,WGT\n - at 383 E ST RD 2,WGT\nCROSS: btwn E EVANS AV and N
 Subject: CAD Page\n\n#110088677 - \nF41:\nMUT.AID/RESP. TO SCN @ 177 HEATHERHILL DR,CTT\nCROSS: btwn DEAD END and HACKNEY LN\nGRP:WF\nPRI:1\ncomm
 Subject: CAD Page\n\n#110089211 - \nF35P2:\nEMS AST-P2 @ 380 N 400 E,WGT\nCROSS: btwn AUGUSTINE DR and E 400 N\nGRP:WF\nPRI:2\ncomment: Medical P
 Subject: CAD Page\n\n#110089866 -\nE29\nMV CRASH/TRANS.ACC @ E US HWY 6 / EDMOND CT, LBT - < 0/ 801>\nGRP:PM\nPRI:1\ncomment: CAR VS PICKUP, CAR IS IN DITCH
 
Contact: Gregory Adair <gadair@washingtonfire.net>
 
 */
 
 public class INPorterCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
 
       "CCT", "Center Twp",
       "CTT", "Center Twp",  // Dispatcher Typo?
       "JKT", "Jackson Twp",
       "LBT", "Liberty Twp",
       "MGT", "Morgan Twp",
       "PGT", "Portage Twp",
       "PLT", "Pleasant Twp",
       "POT", "Porter Twp",
       "UNT", "Union Twp",
       "WGT", "Washington Twp",
       "WCT", "Weswtchester Twp",
       "WNT", "Windfield Twp",
       
       "BHB", "Burns Harbor",
       "CHE", "Chesterton",
       "HEB", "Hebron",
       "PTG", "Portage",
       "PTR", "Porter",
       "VAL", "Valparaiso",
       
   });
   
   public INPorterCountyParser() {
     super("PORTER COUNTY", "IN",
            "ID UNIT ADDR! ADDR2 CROSS:X? GRP:SKIP? PRI:PRI comment:INFO");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("CAD Page")) return false;
     if (!parseFields(body.split("\n"), data)) return false;
     String sAddr = data.strAddress;
     data.strAddress = "";
     parseAddress(sAddr, data);
     return true;
   }
   
   private static final Pattern ID_PTN = Pattern.compile("#(\\d{9}) -");
   private class MyIdField extends IdField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = ID_PTN.matcher(field);
       if (!match.matches()) abort();
       super.parse(match.group(1), data);
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(":")) field = field.substring(0,field.length()-1).trim();
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void  parse(String field, Data data) {
       int pt1 = field.lastIndexOf('@');
       int pt2 = field.lastIndexOf(',');
       if (pt1 < 0 || pt2 < 0 || pt1 > pt2) abort();
       data.strCall = field.substring(0,pt1).trim();
       data.strAddress = field.substring(pt1+1,pt2).trim();
       String sCity = field.substring(pt2+1).trim();
       pt1 = sCity.indexOf('-');
       if (pt1 >= 0) sCity = sCity.substring(0,pt1).trim();
       data.strCity = convertCodes(sCity, CITY_CODES);
     }
     
     @Override 
     public String getFieldNames() {
       return "CALL PLACE " + super.getFieldNames() + " CITY";
     }
   }
   
   private class MyAddress2Field extends AddressField {
     @Override
     public void parse(String field, Data data) {
      if (field.startsWith("- at ")) field = field.substring(5).trim();
       int pt = field.lastIndexOf(',');
       if (pt >= 0) field = field.substring(0,pt).trim();
       data.strPlace = data.strAddress;
       data.strAddress = field;
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("btwn ")) field = field.substring(5).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("ADDR2")) return new MyAddress2Field();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
 }

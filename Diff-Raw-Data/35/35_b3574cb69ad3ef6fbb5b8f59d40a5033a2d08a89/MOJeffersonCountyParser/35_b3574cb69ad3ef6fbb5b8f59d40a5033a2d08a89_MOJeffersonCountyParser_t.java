 package net.anei.cadpage.parsers.MO;
 
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 
 /*
 Jefferson County, MO
 Contact: jason barton <jbarton_911@yahoo.com>
 Sender: Random
 
 Location: 201 E MILLER ST DES: @DESOTO RURAL FIRE STATION 1 TYPE CODE: MOVEUP MUP CALLER ADDR: Disp: 6514
 Location: 110 SEVEN SPRINGS CT TYPE CODE: 1STALRM RES-1ST CALLER ADDR: Disp: 7702,7724
 Location: @ST CLAIR STATION 2 TYPE CODE: MOVEUP MUP CALLER ADDR: Comments: 470 E NORTH ST Disp: 7724
 Location: 12545 WARE RD CNTY: @DESOTO RURAL FIRE STATION 3 TYPE CODE: MOVEUP MUP CALLER ADDR: Disp: 7710
 Location: 4432 MOCKINGBIRD LN CNTY TYPE CODE: EMS SEIZURES CALLER ADDR: Comments: CARD 65019 Disp: 6513,7537
 Location: 5165 ATHLONE CT CNTY TYPE CODE: STILL BRUSH CALLER ADDR: Comments: CARD 77011 MAP 89 Disp: 2438,6102,6128,6418,6518,7618,7701,7710,7714,7718,7728,7734
Location: 380 CEDARVIEW DR TYPE CODE: 1STALRM RES-1ST CALLER ADDR: Comments: MAP 67 Disp: 7734
 
 */
 
 public class MOJeffersonCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
     "CNTY", "",
     "DES", "DE SOTO"
   });
  
   public MOJeffersonCountyParser() {
     super(CITY_CODES, "JEFFERSON COUNTY", "MO",
           "Location:ADDR/S! TYPE_CODE:CALL! CALLER_ADDR:INFO? Comments:INFO? Disp:UNIT%");
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(": @");
       if (pt >= 0) {
         data.strPlace = field.substring(pt+3).trim();
         field = field.substring(0,pt).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }

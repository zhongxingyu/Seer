 package net.anei.cadpage.parsers.MO;
 
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 
 /*
 Jefferson County, MO
 Contact: jason barton <jbarton_911@yahoo.com>
 Sender: Random
 
 Location: @PACIFIC STATION 2 Call Type: MOVEUP MUP Comments: 7376 HIGHWAY O RP's Address: Pri: 4 Area: 77 Disp: 7724
 Location: 16 SYCAMORE GREEN ACRES CNTY Call Type: STILL CHKBLDG Comments: CARD 77272 MAP 77 RP's Address: 16 SYCAMORE GREEN ACRES Pri: 3 Area: 77 Disp: 7710
 Location: OLD MORSE MILL RD/STATE RD EE CNTY Call Type: EMS TRAUMATIC_INJURI Comments: CARD 77323 MAP 61 RP's Address: Pri: 1 Area: 77 Disp: 5437,7714 
 Location: 7890 DITTMER RIDGE RD CNTY:ST MARTINS CHURCH Call Type: ALARMSND FIREALMC Comments: CARD 77159 MAP 64 RP's Address: Pri: 2 Area: 77 Disp: 7724,7710  
 Location: GRAHAM RD/CEDAR HILL RD CNTY: IN THE AREA Call Type: STILL ILLBURN Comments: 77047 25 and 25a RP's Address: Pri: 4 Area: 77 Disp: 7710 
 Location: 4132 SLEEPY HOLLOW LN CNTY Call Type: 1STALRM RES-1ST Comments: CARD 77413 MAP 85 RP's Address: 4132 SLEEPY HOLLOW LN Pri: 2 Area: 77 Disp: 0002/0002 2433,2434,5437,6413,7713,7723,7734,7702,6434,7710 
 
 */
 
 public class MOJeffersonCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
     "CNTY", ""
   });
  
   public MOJeffersonCountyParser() {
     super(CITY_CODES, "JEFFERSON COUNTY", "MO",
          "Location:ADDR/S! Call_Type:CALL! Comments:INFO! RP's_Address:INFO2 Pri:PRI Area:MAP Disp:UNIT%");
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(':');
       if (pt >= 0) {
         data.strPlace = field.substring(pt+1).trim();
         field = field.substring(0,pt).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
   
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (data.strAddress.startsWith("@")) {
         data.strPlace = data.strAddress.substring(1).trim();
         data.strAddress = "";
         parseAddress(field, data);
       }  else {
         super.parse(field, data);
       }
     }
   }
   
   private class Info2Field extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (field.length() == 0) return;
       field = "RP's ADDR:" + field;
       data.strSupp = append(data.strSupp, " ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("INFO2")) return new Info2Field();
     return super.getField(name);
   }
 }

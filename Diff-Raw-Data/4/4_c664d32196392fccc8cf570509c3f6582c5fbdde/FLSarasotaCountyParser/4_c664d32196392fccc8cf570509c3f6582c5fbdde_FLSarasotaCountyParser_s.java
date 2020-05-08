 package net.anei.cadpage.parsers.FL;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Sarasota County, FL
 Contact: Bill Hoag <bikeh1@gmail.com>
 Sender: 77711
 System: Integraph
 
 SARASOTA COUNTY EVENT TYPE: ODOR-INSIDE Location: 4546 DEER CREEK BLVD NCTY G225 TIME: 21:39:20  Disp: BN3,E14,E15,R14
 SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 4681 MACEACHEN BLVD NCTY G146 TIME: 18:23:38  Disp: BN3,E11,E12,E8,R2,R7,T18,TRK11
 SARASOTA COUNTY EVENT TYPE: FALLS Location: 1035 N LIME AVE SARA G056 TIME: 14:35:29  Disp: BN1,E1,EMS1,R1
 SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 3908 WALNUT AVE NCTY G033 TIME: 16:12:52  Disp: BN2,E1,E4,E5,EMS1,R3,R5,SQ8,TRK5
 SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 3908 WALNUT AVE NCTY G033 TIME: 16:12:52  Disp:
 SARASOTA COUNTY EVENT TYPE: ASSIST OTHER MEDICAL AGENCY Location: 5731 BEE RIDGE RD NCTY G128: @DOCTORS TIME: 18:51:05  Disp: BN2,E8,R8
 SARASOTA COUNTY EVENT TYPE: VEHICLE CRASH WITH INJURIES Location: BEE RIDGE RD/MAUNA LOA BLVD NCTY G149 TIME: 02:06:18  Disp: BN2,E16,R8,SQ8
 SARASOTA COUNTY EVENT TYPE: OUTSIDE FIRE-UNKNOWN Location: STONEYCREEK BLVD/CENTER RD SCTY G511 TIME: 19:00:05  Disp: BN4,E21,E26,R21
 SARASOTA COUNTY EVENT TYPE: VEHICLE CRASH WITH INJURIES Location: S TUTTLE AVE/SUNNYSIDE ST NCTY G124 TIME: 19:28:44  Disp: BN1,E2,EMS1,R2
 
 Contact: Scott Titus <stitus@cityofnorthport.com>
 Sender: 93001
 EVENT TYPE: OUTSIDE FIRE-BRUSH Location: COLFAX LN/TOWN TER NPRT G544 TIME: 17:26:42 Disp: BN8,DOF,E81,T83,TRK84
 EVENT TYPE: STRUCTURE FIRE Location: 2633 RIDGEWOOD DR NPRT G544 TIME: 01:26:04
 EVENT TYPE: STRUCTURE FIRE Location: PAN AMERICAN BLVD/LA BREA ST NPRT G565 TIME: 19:07:40
 
 Contact: Bill Hoag <bikeh1@gmail.com>
 Sender: 7771
 SARASOTA COUNTY EVENT TYPE: VEHICLE FIRE Location: 4700 FORBES TRL AREA: G434 MUN: SCTY TIME: 12:12:10\2sDisp: B26,BN4,E21,E24,E26,R21
 
 *** FREE FORM - NOT PARSED ***
 WORKING BRUSH FIRE @ 3261 TOWN TER NORTH PORT, XST YEADON TER, APX 1/4 ACRE// MED TO HEAVY FUEL// FAST MOVING// UNITS ON SCENE // FD3 DISPATCH 2641 B82 B83 BN8 DOF E81 RAV84 T83 TRK84
 BAYFLIGHT REF DROWNING AT 2125 WENONA DR NPRT G524//BN8, E83, R84, TRK84 ON SCENE WITH 2YOM CONS/BREATHING//DISPATCHER #2675
 OUTSIDE FIRE AT MM 175 SB // WEST SIDE IN TREES/ 1/4 ACRE MED FUEL GRID 526 BN8 TRK84 ON SCENE/DOF E81 E83 T83 EN ROUTE ID 2565
 
  */
 
 public class FLSarasotaCountyParser extends FieldProgramParser {
   
   public FLSarasotaCountyParser() {
     super(CITY_CODES, "SARASOTA COUNTY", "FL",
        "EVENT_TYPE:CALL! Location:ADDR/S! AREA:MAP? MUN:CITY? TIME:TIME! Disp:UNIT");
   }
   
   @Override
   public int getMapFlags() {
     return  MAP_FLG_SUPPR_LA;
   }
 
   @Override
   public String getFilter() {
     return "7771,93001";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.startsWith("SARASOTA COUNTY ")) body = body.substring(16).trim();
     return super.parseMsg(body, data);
   }
   
   private static final Pattern MAP_PTN = Pattern.compile(" (G\\d{3})$");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       
       int pt = field.indexOf(": @");
       if (pt >= 0) {
         data.strPlace = field.substring(pt+3).trim();
         field = field.substring(0,pt).trim();
       }
       
       Matcher match = MAP_PTN.matcher(field);
       if (match.find()) {
         data.strMap = match.group(1);
         field = field.substring(0,match.start()).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " MAP PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "NCTY", "",
       "NPRT", "NORTH PORT",
       "SCTY", "",
       
       "SARA", "SARASOTA"
   });
 }

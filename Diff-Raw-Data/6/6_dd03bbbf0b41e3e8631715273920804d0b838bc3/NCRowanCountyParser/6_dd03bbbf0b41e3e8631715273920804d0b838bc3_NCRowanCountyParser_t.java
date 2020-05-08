 package net.anei.cadpage.parsers.NC;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Rowan County, NC
 Contact: Timothy Ross <firefighter4047@gmail.com>
 Sender: 93001041
 System: OSSI Dispatch
 
 CAD:10C4 CHEST PAIN;201 N FRANKLIN ST;SWINK ST;PARK ST;CHGV;4301;RFG1
 CAD:FIRE DEPT SERVICE ASSIGNMENT;213 WASHINGTON ST;KLONDALE ST;CHGV;4301
 CAD:13D1 DIABETIC PROBLEM;1075 DEAL RD;KARRIKER FARMS RD;FIELD TRACE RD;MOOR;CARE INN RETIREMENT CENTER;4002;RFG4
 CAD:RFD COMMAND ESTABLISHED;2470 E NC 152 HWY; CHGV
 CAD:29B1 TRAFFIC ACCIDENT WITH INJ;E NC 152 HWY/S I 85;CHGV;4123;RFG2
 CAD:RFD COMMAND ESTABLISHED;E NC 152 HWY/S I 85; CHGV
 
 Contact: Jamie Thomas <jamie.thomas75@yahoo.com>
 Sender: 93001018
 CAD:STRUCTURE FIRE;625 SIDES RD;GOLD KNOB RD;GOLD KNOB RD;SALS;7106;RFG1
 
 Contact: Jeremy Shimberg <jshimberg@carolina.rr.com>
 Sender: CAD@co.rowan.nc.us
 CAD:13D1 DIABETIC PROBLEM;3035 STOKES FERRY RD;EDZELL DR;EARNHARDT RD;SALS;7604;RFG4
 CAD:10D1 CHEST PAIN;6235 FISH POND RD;SWANNER PARK DR;ODDIE RD;SALS;7605;RFG1
 CAD:6C1 BREATHING PROBLEMS;140 PARTRIDGE TR;PALOMINO DR;DEER RD;SALS;7603;RFG1
 CAD:FIRE ALARM;1190 LONG FERRY RD;N LONG FERRY OFF;WILLOW CREEK DR;SALS;RUSHCO (LIBERTY) STATION #18;6401;RFG1
 
 Contact: joey mcclanahan <rebelforlif@yahoo.com>
 Sender:CAD@co.rowan.nc.us
 STRUCTURE FIRE;209 E 15TH ST;SALS;N LEE ST;FAMILY CRISIS CENTER;OPS02
 
 Contact: Trey Hoshall <treyhoshall@gmail.com>
 Sender: CAD@co.rowan.nc.us
 CAD:31D2 UNCONSCIOUS;6130 OLD US 70 HWY;CLEV;GLENN FAUST RD;CLOVER RIDGE CT;4501;OPS12
 CAD:31D2 UNCONSCIOUS;3855 WOODLEAF BARBER RD;CLEV;THOMPSON RD;MT HALL RD;4504;OPS13
 CAD:29B1 TRAFFIC ACCIDENT INJURY;2050 MOUNTAIN RD;CLEV;LONE MOUNTAIN TR;CARSON RD;4506;OPS14
 CAD:FIRE ALARM ACTIVATION;11550 STATESVILLE BLVD;CLEV;W MAIN ST;AMITY HILL RD;FREIGHTLINER TRUCK MFG;4503;OPS14
 CAD:6C1 BREATHING PROBLEMS;303 N DEPOT ST;CLEV;W FOARD ST;4503;OPS14
 CAD:6D4 BREATHING PROBLEMS;11170 STATESVILLE BLVD;CLEV;MIMOSA ST;SCHOOL ST;CLEVELAND FIRE STN 45;4503;OPS13
 CAD:COUNTY FIRE MOVEUP;11170 STATESVILLE BLVD;CLEV;MIMOSA ST;SCHOOL ST;CLEVELAND FIRE STN 45;4503
 
 Contact: George Stewart <tank84123@gmail.com>
 Sender: 93001018
 CAD:FIRE ALARM ACTIVATION;1020 EDMISTON RD;MTUL;CARSWELL RD;NC 801 HWY;6608;OPS12
 
 Contact: Trey Hoshall <treyhoshall@gmail.com>
 Sender: CAD@co.rowan.nc.us
 BCAD:32D3 UNKNOWN MEDICAL;175 WHISPERING OAKS LN;MOCK;OAK MEADOW LN;RATLEDGE RD;7305;OPS
 
 Contact: Andrew King <aking.salisburyfire@gmail.com>
 Sender: CAD@co.rowan.nc.us
 CAD:FYI: ;HAZMAT LEVEL 1;1625 N JACKSON ST;SALS;W 15TH ST;S ROWAN AV;301
 
 Out of County mutual aid calls
 Contact: support@active911.com
 [] CAD:FIRE ALARM ACTIVATION;136 MARGINAL ST;COOLEEMEE ELEM SCHOOL\n
 [] CAD:FIRE ALARM ACTIVATION;136 MARGINAL ST;COOLEEMEE ELEMENTARY SCHOOL\n
 [] CAD:FIRE ALARM ACTIVATION;136 MARGIN ST\n
 [] CAD:SMOKE VISABLE IN STRUCTURE;252 WATT ST\n
 [] CAD:CANCEL;252 WATT ST\n
 [] CAD:FIRE ALARM ACTIVATION;264 NOLLIE DR;JOCKEY\n
 
 First 4 in Cooleemee, Davies County
 Last could be Nellie dr in Davidson County
 
*** General alert
CAD:Davidson County Rescue Squad is hold a barbecue chicken dinner at St6 until food is gone. Dinner includes chicken, beans, potato salad, roll , and dess

 */
 
 public class NCRowanCountyParser extends DispatchOSSIParser {
   
   public NCRowanCountyParser() {
     super(CITY_CODES, "ROWAN COUNTY", "NC",
            "FYI? CALL ADDR ( OPTPLACE INFO+ | X/Z+? CITY XPLACE+? UNIT? CH )");
   }
   
   @Override
   public String getFilter() {
     return "9300,CAD";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     boolean ok = body.startsWith("CAD:");
     if (!ok) body = "CAD:" + body;
     if (!super.parseMsg(body, data)) return false;
     
     // If we never got a city, assume this is an out of county mutual aid call
     // If we didn't have the CAD: prefix and don't have a city, this is just
     // to chancy to accept
     if (data.strCity.length() == 0) {
       if (!ok) return false;
       data.defCity = "";
     }
    return data.strAddress.length() > 0;
   }
   
   // Special place field that only triggers if there is no city field
   // further downstream.  This indicates an out of county mutual aid call
   private class OptionalPlaceField extends PlaceField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // Only valid if we detect a downstream city field (length <= 4)
       for (int ndx = 0; ; ndx++) {
         String fld = getRelativeField(ndx);
         if (fld.length() == 0) break;
         if (fld.length() <= 4) return false;
       }
       parse(field, data);
       
       // We don't know what county this is, but we now it is not Rowan County
       return true;
     }
       
     
   }
   
   // City field is required, and must be found in table to rule out
   // similar transactions from other locations
   private class MyCityField extends CityField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.length() > 4) return false;
       parse(field, data);
       return true;
     }
     
     @Override
     public void parse(String field, Data data) {
       super.parse(field, data);
       if (field.equals(data.strCity)) abort();
     }
   }
   
   private class MyCrossPlaceField extends Field {
     
     @Override 
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.length() <= 5) return false;
       parse(field, data);
       return true;
     }
     
     @Override
     public void parse(String field, Data data) {
       if (checkAddress(field) > 0) {
         data.strCross = append(data.strCross, " / ", field);
       } else {
         data.strPlace = append(data.strPlace, " / ", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE X";
     }
   }
   
   @Override
   protected Field getField(String name) {
     if (name.equals("FYI")) return new SkipField("FYI:|UPDATE:");
     if (name.equals("OPTPLACE")) return new OptionalPlaceField();
     if (name.equals("CITY")) return new MyCityField();
     if (name.equals("XPLACE")) return new MyCrossPlaceField();
     if (name.equals("UNIT")) return new UnitField("\\d{4}", true);
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CHGV", "CHINA GROVE",
       "CLEV", "CLEVELAND",
       "CLVD", "CLEVELAND",
       "ESPN", "EAST SPENCER",
       "FATH", "FAITH",
       "GOLD", "GOLD HILL",
       "GRQY", "GRANITE QUARRY",
       "KANN", "KANNAPOLIS",
       "LAND", "LANDIS",
       "MOCK", "MOCKSVILLE",
       "MOOR", "MOORESVILLE",
       "MTUL", "MT ULLA",
       "ROCK", "ROCKWELL",
       "SALS", "SALISBURY",
       "SPEN", "SPENCER",
       "WOOD", "WOODLEAF"
   }); 
 }

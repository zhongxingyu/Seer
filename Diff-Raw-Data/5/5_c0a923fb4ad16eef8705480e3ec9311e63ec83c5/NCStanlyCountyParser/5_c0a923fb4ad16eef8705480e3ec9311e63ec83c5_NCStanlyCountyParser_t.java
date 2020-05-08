 package net.anei.cadpage.parsers.NC;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Stanly County, NC
 Contact: "D. E. Gerald" <trafficfuzz@yahoo.com>
 Sender:CAD@sclg.gov
 
 CAD:E6-BREATHING PROBLEMS/533 BARRINGER CHAPEL RD/NOR/WIL-KRIS DR/FORK RD/[Medical Priority Info] PROBLEM: cant breathe # PATS: 1 AGE: 24 Years SE
 CAD:E12-SEIZURES/CONVULSION/5761 DOVE LN/NOR/LOOP RD/[Medical Priority Info] PROBLEM: # PATS: 1 AGE: 49 Years SEX: Male CONSCIOUS: No BREATHING: Y
 CAD:E6-BREATHING PROBLEMS/533 BARRINGER CHAPEL RD/NOR/WIL-KRIS DR/FORK RD/[Medical Priority Info] PROBLEM: cant breathe # PATS: 1 AGE: 24 Years SE
 CAD:80-DWELLING ALARM/8947 WHITLEY RD/NOR/W WHITLEY ST﻿
 CAD:80-STRUCTURE ALARM/9029 COTTONVILLE RD/NOR/HINSON RD/S STANLY SCHOOL RD/3 KEYPAD FIRE ALARM, 2 BURGLAR, AND HOLD UP ALARM [03/03/11 19:12:48 C﻿
 CAD:E10-CHEST PAINS/231 PRICE ST/NOR/CARVER ST/WIL-KRIS DR/[Medical Priority Info] PROBLEM: CHEST PAIINS.VOMITTING # PATS: 1 AGE: 66 Years SEX: Ma
 
 CAD:80-STRUCTURE ALARM/OLD CHARLOTTE RD/HARWOOD ST/ALB/SMOKE COMING FROM BARN ON OLD CHARLOTTE [04/03/11 17:44:15 BWHITLEY] B/T HARWOOD AND COBLE [04/03/11 17:
 CAD:80-STRUCTURE ALARM/1700 WOODHURST LN/ALB/BIRD RD/ANDERSON RD/manual activation [04/03/11 17:54:08 DSHAVER]
 CAD:80-DWELLING ALARM/601 EAST ST/ALB/N SEVENTH ST/N EIGHTH ST
 
 Contact: Chris Allan <nlcap2405@gmail.com>
 CAD:E13-DIABETIC PROBLEMS/40206 US 52 HWY N/NEW/AUSTIN RD/[Medical Priority Info] PROBLEM: diab prob # PATS: 1 AGE: 83 Years SEX: Male
 
 Contact: Roger Stamper <capt3607@gmail.com>
 CAD:E26-SICK PERSON/28345 ROWLAND RD/MTP/[Medical Priority Info] PROBLEM: HIGH FEVER, SINUS INFECTION, VOMITING # PATS: 1 AGE: 15 Years SEX: Female CONSCIOUS: Yes BREATHIN
 
 Contact: Brian Shie <bshue4637@gmail.com>
 CAD:E17-FALL WITH INJURY/614 NC HWY 200 S/SFD/COYLE RD/NC 200 HWY/[Medical Priority Info] PROBLEM: FELL POSS SEIZURE RELATED # PATS: 1 AGE: 50 Yea
 CAD:80-BRUSH ALARM/ROCK HOLE RD/NC 200 HWY/SFD/WHEAT FIELD CUT BUT ON FIRE / APPX 1 ACRE [07/02/11 18:42:57 TSMITH] ONTO ROCK HOLE ON LEFT [07/02/
 
Contact: "inque74@hotmail.com" <inque74@hotmail.com>
CAD:E29-10-50 PI/249 DEESE ST/RFD/GOLD BRANCH RD/N CEMETERY ST/[Medical Priority Info] PROBLEM: car flipped # PATS: 1 AGE: 16 Years SEX: Female CONSCIOUS: Yes BREATHIN

 */
 
 public class NCStanlyCountyParser extends DispatchOSSIParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ALB", "ALBEMARLE",
       "BAD", "BADIN",
       "LOC", "LOCUST",
       "MTP", "MT PLEASANT",
       "NEW", "NEW LONDON",
       "NOR", "NORWOOD",
       "OAK", "OAKBORO",
      "RFD", "RICHFIELD",
       "SFD", "STANFIELD"
   });
   
   private List<String> addressList = new ArrayList<String>();
   
   public NCStanlyCountyParser() {
     super(CITY_CODES, "STANLY COUNTY", "NC",
            "CALL ADDR/Z+? CITY! X X? INFO+");
     setDelimiter('/');
   }
   
   @Override
   public String getFilter() {
     return "CAD@sclg.gov";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     boolean result = super.parseMsg(subject, body, data);
     addressList.clear();
     return result;
   }
   
   // Things get complicated here, the address field just accumulates fields
   // until we find a city field
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String fld, Data data) {
       addressList.add(fld);
     }
   }
   
   // The city list is where we decide what to do with the address fields
   private class MyCityField extends CityField {
     @Override
     public boolean checkParse(String fld, Data data) {
       if (!super.checkParse(fld, data)) return false;
       
       // OK, we found address, now figure out what to do with the address
       // If there are more than one address field and the last one is a simple
       // naked road, then merge the last two fields together to form the
       // address field.  If not, the last field is the only address field
       int addrNdx = addressList.size()-1;
       if (addrNdx < 0) abort();
       String sAddr = addressList.get(addrNdx);
       if (addrNdx > 0 && checkAddress(sAddr) == 1) {
         sAddr = addressList.get(--addrNdx) + " & " + sAddr;
       }
       parseAddress(sAddr, data);
       
       // Any fields in front of the address field will be appended to the
       // call description
       for (int ii = 0; ii<addrNdx; ii++) {
         data.strCall = append(data.strCall, "/", addressList.get(ii));
       }
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CITY")) return new MyCityField();
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }

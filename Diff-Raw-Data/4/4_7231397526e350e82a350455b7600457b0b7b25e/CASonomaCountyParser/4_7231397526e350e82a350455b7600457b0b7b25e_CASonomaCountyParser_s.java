 package net.anei.cadpage.parsers.CA;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Sonoma County, CA
 Contact: Michael Gevas <mgevas@valleyfordfire.com>
 Sender: sclec@sonoma-county.org
 
 Loc: HWY1/VALLEY FORD ROAD VFR BOX: 3540 TYP:TC CN: CHP LOG # 632 C#: TYPE CODE: TC CALLER NAME: CHP LOG # 632 CALLER ADDR:  TIME: 11:07:31 COM: ** Case n
 Loc: 1320 BAY VIEW ST BBY:@ BODEGA UNION CHURCH BOX: 3433 B3 TYP: GAS-IN CN: BODEGA BAY UNION CHURCH C#: (707)875-3559 TYPE CODE: GAS-IN CALLER NAME: BODEGA
 Loc: BBY:@HWY 1MM012.42 BOX:3332 B TYP: TC-EX CN: CHP LOG 344 C#: TYPE CODE: TC_EX CALLER NAME:CHP LOG 344 CALLER ADDR:  TIME: 03:44:34 COM: OVER TURN
 Loc: 1400 VALLEY FORD FREESTONE RD VFR BOX: 3439 B TYP: SER-PA CN: JOANNA C#: (707) 876-3288 TYPE CODE: SER-PA CALLER NAME: JOANNA CALLER ADDR:  TIME: 13:01
 
 Contact: Sage Howell <sagehowellmobile@gmail.com>
 Sender: clec@sonoma-county.org
 Loc: 2458 BIG OAK DR SR BOX: 2946 D4 TYP: STRUW CN: AT&T MOBILITY 80 635 6840  4 C#: (707) 327-7382 TYPE CODE: STRUW CALLER NAME: AT&T MOBILITY
 Loc: CALISTOGA RD/CHALFANT RD RIN BOX: 2452 D TYP: TC-EX CN: CHP LOG#1987 C#:  TYPE CODE: TC-EX CALLER NAME: CHP LOG#1987 CALLER ADDR:  TIME: 19:32:46 COM:
 Loc: 801 SANTA BARBARA DR SRO BOX: 3049 B4 TYP: GAS-IN CN: JOEY C#: (707) 391-8596 TYPE CODE: GAS-IN CALLER NAME: JOEY CALLER ADDR:  TIME: 18:14:37 COM:  Ev
 Loc: 311 BELHAVEN CI SR BOX: 2955 C3 TYP: STRU CN: COUGHLAN JAQUELINE C#: (707) 538-7881 TYPE CODE: STRU CALLER NAME: COUGHLAN JAQUELINE CALLER ADDR: 311 BE
 Loc: 187 PEACH BLOSSOM DR SR BOX: 2847 B4 TYP: GAS-IN CN:  C#: (707) 523-4778 TYPE CODE: GAS-IN CALLER NAME:  CALLER ADDR:  TIME: 12:50:50 COM:  UNSURE WHAT
 Loc: 2637 WILD BILL CI SR BOX: 3048 C4 TYP: STRU CN: BARRAGAN MARTHA C#: (707) 544-5719 TYPE CODE: STRU CALLER NAME: BARRAGAN MARTHA CALLER ADDR: 2637 WILD
 
 Contact: "jpich@aol.com" <jpich@aol.com>
 Loc: 1142 DUER RD WSR BOX: 3045 C4 TYP: HC CN: SANDRA C#: (707) 484-0358 TYPE CODE: HC CALLER NAME: SANDRA CALLER ADDR:  TIME: 14:50:20 COM:  BROKEN SPRINKL
 
 Email variants
 Loc: VALLEY FORD RD/HWY 1 VFR BOX: 3540 TYP: TC CN: AT&T MOBILITY 800 635 6840  4 C#: (650) 455-7732 TYPE CODE: TC CALLER NAME: AT&T MOBILITY 800 635 6840  4 CALLER ADDR: 2885 BAY HILL RD BDGA TIME: 16:00:06 COM:  N -122.7320 T 38.40650 METERS 2758 MOTORCYCLE 
 Loc: 14460 SCHOOL ST VFR BOX: 3539 A2 TYP: MED CN: BEAL FRED C#: (707) 876-3232 TYPE CODE: MED CALLER NAME: BEAL FRED CALLER ADDR: 14460 SCHOOL TIME: 20:34:31 COM:  SCSO 911 HAD SURGERY IS SF A WEEK AGO LEG SWOLLEN FROM WHERE THE SURGERY WAS IN HIS LEG CONTROL 2 PT HAS A 102 TEMP DR TOLD HIM TO CALL 911 78 YOM 
 Loc: 814 OWL CT BBY BOX: 3535 C1 TYP: STRU CN: CASTLE,STACEY C#: (707) 875-9870 TYPE CODE: STRU CALLER NAME: CASTLE,STACEY CALLER ADDR: 814 OWL CT TIME: 10:17:55 COM:  SCSO- 911 SMOKE COMING FROM AN ELECTICAL OUTELT 
 Loc: BLOOMFIELD RD/SUTTON ST BLO BOX: 3542 B4 TYP: HC CN: 8540 C#:  TYPE CODE: HC CALLER NAME: 8540 CALLER ADDR:  TIME: 20:39:02 COM:  veh in ditch and water start 8580
 BOX: 3437 TYP: VEG CN: VERIZON WIRELESS 800 451 5242 4 C#: (707) 321-9508 TYPE CODE: VEG CALLER NAME: VERIZON WIRELESS 800 451 5242 4 CALLER ADDR: 2885 BAY HILL RD BDGA BAY TIME: 12:33:30 COM:  N -122.9738 T 38.33546 METERS 41 RIGHT BEFORE THE BODEGA HWY TURN OFF SHE COULD NOT GIVE AN ADDRESS OR A MILE MARKER
 Loc: BBY: @HWY 1 MM012.42 BOX: 3332 B TYP: TC-EX CN: CHP LOG 344 C#:  TYPE CODE: TC-EX CALLER NAME: CHP LOG 344 CALLER ADDR:  TIME: 03:44:34 COM:  OVER TURNED VEH NEAR SALMON CREEK CONTROL 2 
 Loc: HWY 1/BODEGA HW BOD BOX: 3436 TYP: TC-EX CN: CHP C#:  TYPE CODE: TC-EX CALLER NAME: CHP CALLER ADDR:  TIME: 20:46:20 COM:  OVERTURNED VEH ON HWY 1, BETWEEN VALLEY FORD AND BODEGA HWY BAD CONNECTION WITH RP, PER CHP 
 
  */
 
 
 public class CASonomaCountyParser extends FieldProgramParser {
   
   private static final Properties STATION_CODES = buildCodeTable(new String[]{
     "BBY",  "BBY", // Bodega Bay
     "BLO",  "BLO", // Bloomfield
     "BOD",  "BOD", // ???
     "RIN",  "RIN",
     "SR",   "SR",
     "SRO",  "SRO",
     "VFR",  "VFR", // Valley Ford
     "WSR",  "WSR",
 
     "BDGA",      "BODEGA",
     "BDGA BAY",  "BODEGA BAY",
   });
   
   private static final Pattern HW_PTN = Pattern.compile("\\bHW\\b");
   
   public CASonomaCountyParser() {
     super(STATION_CODES, "SONOMA COUNTY", "CA",
           "Loc:ADDR? BOX:BOX TYP:CALL! CN:NAME CC:PHONE TYPE_CODE:SKIP CALLER_NAME:NAME CALLER_ADDR:ADDR2/S TIME:SKIP COM:INFO");
   }
   
   @Override
   public String getFilter() {
     return "sclec@sonoma-county.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     body = body.replaceAll("C#:", "CC:");
     if (! super.parseMsg(body, data)) return false;
     if (data.strAddress.length() == 0) return false;
     data.strAddress = HW_PTN.matcher(data.strAddress).replaceAll("HWY");
     return true;
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String fld, Data data) {
       fld = fld.replaceAll(": @", ":@");
       int pt = fld.indexOf(":@");
       if (pt >= 0) {
         data.strPlace = fld.substring(pt+2).trim();
         fld = fld.substring(0,pt).trim();
       }
       if (fld.length() <= 3) {
         data.strSource = fld;
         data.strAddress = data.strPlace;
         data.strPlace = "";
       } else {
         parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, fld, data);
         if (data.strCity.length() <= 3) {
           data.strSource = data.strCity;
           data.strCity = "";
         }
       }
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR SRC PLACE";
     }
   }
   
   private class MyAddress2Field extends AddressField {
     @Override
     public void parse(String field, Data data) {
       if (data.strAddress.length() > 0) return;
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("ADDR2")) return new MyAddress2Field();
     return super.getField(name);
   }
 }

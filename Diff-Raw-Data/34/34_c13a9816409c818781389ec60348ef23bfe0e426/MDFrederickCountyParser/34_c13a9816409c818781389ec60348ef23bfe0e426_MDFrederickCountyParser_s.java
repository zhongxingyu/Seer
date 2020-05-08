 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /***
 **********************************************************
 Frederick County, MD
 Contact: dalefogle@aol.com
 Contact: Dale Fogle <pullersnet@gmail.com>
 Contact: Brian Kelch <flamekiller20@gmail.com>
 Sender:  www.codemessaging.net,CAD@psb.net
  
 (CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 6801 HARBAUGH RD CSAB:NEXT TO HER HOME ESZ: 626 MAP: 3996A6 Disp: [23]
 (CAD) [FredCo] CT: WOODS FIRE 7819-B FRIENDS CREEK RD CEMB ESZ: 627 MAP: 3996F6 Disp: E63,B66,DNR [36]
 (CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 4 PAWS PL THUR: @CATOCTIN VETERINARY CLINIC ESZ: 1001 MAP: 4108E9 Disp: E102,TWR6 [37]
 (CAD) [FredCo] CT: OUTSIDE INVESTIGATION @MA ADAM CO: @RT15SB/BOYLE RD ESZ: -1 Disp: E63 [38]
        This was a mutual aid call to Adams Co. Pa at Rt.15 South Bound at the Boyle Rd. overpass
 (CAD) [FredCo] CT: HOUSE / ODOR 9152 OLD KILN RD CEMB ESZ: 617 MAP: 4109E4 Disp: E63,E102,A268,TWR6,ET64 [39]
 (CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 102 OLD OAK PL THUR ESZ: 1001 MAP: 4222D2 Disp: E102,TWR6 [41]
 (CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / WATERFLOW 100 CREAMERY CT: @EMMITSBURG GLASS CO ESZ: 620 MAP: 3997K9 Disp: E63,TWR6 [42]
 (CAD) [FredCo] CT: PERSON FIRE (INSIDE) 16825 S SETON AVE CEMB: @NATIONAL EMERGENCY TRAINING CENTER:SIMULATION LAB ESZ: 620 MAP: 3997H9 Disp: [34]                    Rmk:"
 (FredCo) [CAD] CT: CHEST PAIN  1811 MONOCACY BLVD FRE1: @WAL MART: PHARMACY  ESZ: 215 MAP: 4448J9  Disp: A29,A247,M17
 
 Contact: Mike Ringer <mpr0317@gmail.com>
 Sender: rc.360@c-msg.net <From%3Arc.360@c-msg.net>
 (CAD) [FredCo] CT: TROUBLE BREATHING 142 E SOUTH ST FRE1 ESZ: 303 MAP: 4566E5 Disp: A38,M31 [85]
 
 Contact: "windyhollowgrowers@hotmail.com" <windyhollowgrowers@hotmail.com>
 (FredCo) [CAD] CT: HOUSE / FIRE-VISIBLE CACO: @MA CARRCO: 1594 BAUST CHURCH RD ESZ: 9902 Disp: ET94,
 (FredCo) [CAD] CT: HEMORRHAGE CACO: @MA CARRCO: 915 FRANCIS SCOTT KEY HWY ESZ: 9902 Disp: SU9
 
 [FredCo] CT: INJURY FROM VEHICLE ACCIDENT LL(-77:23:59.6013,39:21:53.1520): @I270NB / MONOCACY RIVER ESZ: 327002 MAP: 46
 
 Contact: Bill Green <wbgreen1@gmail.com>
 (CAD) [FredCo] CT: HOUSE / SMOKE 238 DEPAUL ST EMMB ESZ: 601 MAP: 3997J8 Disp: E63,ACE232,E102,CCE111,E132,A268,RS6,TWR6,CCLDR5,BAT900,SFT900
 
 Contact: "bubbaclary@yahoo.com" <bubbaclary@yahoo.com>
 (CAD) [FredCo] CT: CARDIAC PATIENT 12 AFTON CT BRUN ESZ: 502 MAP: 4683C9 SUBDIV: GALYN MANOR Disp: A199,M20,A209,5FR [93]
 
 Contact: Bill Stearn <wstearn@gmail.com>
 Sender: rc.404@c-msg.net
 (CAD) [FredCo] CT: TROUBLE BREATHING / default 13862-A OLD NATIONAL PIKE CMTY TIME: 01:39:41 ESZ: 1521 MAP: 4690B2 Disp: CCM19,CCLS1,RE153
 (CAD) [FredCo] CT: HOUSE / APPLIANCE FIRE (STRUCTURE PRE-ALERT) / default 6405 SPRING FOREST RD CFR1 TIME: 17:14:24 ESZ: 3302 MAP: 4567F5 SUBDIV: SPRING RIDGE Dis
 (CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 808 E SOUTH ST FRE1 TIME: 03:24:42 ESZ: 308 MAP: 4566G5 Disp: E31,E22,E331,E311,A38,RS3,TR42,TWR1,T1,ET233,ET154,BAT900,SF
 (CAD) [FredCo] CT: TROUBLE BREATHING / default 7 S FEDERAL ST NEWM TIME: 12:02:44 ESZ: 1501 MAP: 4568H9 Disp: A159,M17
 (CAD) [FredCo] CT: OUTSIDE INVESTIGATION / default 5018 GREEN VALLEY RD CMON TIME: 21:24:52 ESZ: 1503 MAP: 4688G1 Disp: RE153
 (CAD) [FredCo] CT: SICK PERSON / default 5850 EAGLEHEAD DR CIJM: @OAKDALE HIGH SCHOOL TIME: 08:52:07 ESZ: 1513 MAP: 4568A7 Disp: A159
 (CAD) [FredCo] CT: VEHICLE ACCIDENT / default BELLS LN/BALTIMORE RD CFR4 TIME: 09:58:14 ESZ: 3306 MAP: 4567F7 Disp: A339,RE153,E331
 (CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 1103 VILLAGE GATE DR MTAY: NEXT TOO TIME: 11:20:23 ESZ: 1527 MAP: 4570C10 Disp: RE153,E251,TWR23
 
 Contact: Derek Wildasin <wildasindc@gmail.com>
 Sender: rc.393@c-msg.net
 (CAD) [!] CT: ASSIST PATIENT - NON-EMERGENCY RESPONSE / default 222 BROADWAY ST FRE1: @WEINBERG HOUSE MAP: 4566D5 Disp: A38,E31
 
 Contact: Bill Stearn <wstearn@gmail.com>
 Sender: rc.404@c-msg.net
 (CAD) [FredCo] CT: ENGINE TRANSFER / default 14026 GRACEHAM RD CTHU: @STATION 18 MAP: 4222J2 Disp: ET154
 (CAD) [FredCo] CT: VEHICLE ACCIDENT / default CMON: @RT80 / KEMPTOWN CHURCH RD MAP: 4689D7 Disp: A259,RE153,ET254
 
 Contact: Joseph Wentz <ccsgt82@gmail.com>
 Sender: messaging@iamresponding.com
 (JFC 2) CT: BACK PAIN /  801 TOLL HOUSE AVE FRE1: SUITE C-3 MAP: 4566D3  Disp: A29
 
 ***/
 
 public class MDFrederickCountyParser extends FieldProgramParser {
 
   public MDFrederickCountyParser(){
     super(CITY_CODES, "FREDERICK COUNTY", "MD",
           "CT:ADDR! TIME:TIME? ESZ:BOX? MAP:MAP Disp:UNIT");
   }
 
   @Override
   public String getFilter() {
     return "www.codemessaging.net,CAD@psb.net,@c-msg.net";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (subject.length() == 0) return false;
    String[] subjects = subject.split("\\|");
    if (subjects.length > 2) return false;
    if (subjects.length == 2) {
      if (subjects[0].equals("CAD")) subject = subjects[1];
      else if (subjects[1].equals("CAD")) subject = subjects[0];
      else return false;
     }
    data.strSource = subject;
    return super.parseMsg(body, data);
   }
   
   // Address field gets complicated
   private static final Pattern CITY_PTN = Pattern.compile(" ([A-Z0-9]{4}(?: CO)?):(?: @)?");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
 
       // Everything changes with a mutual aid call
       // should be followed by a city: address
       int ipt = field.indexOf(" @MA ");
       if (ipt >= 0) {
         data.strCall = "Mutual Aid: " + field.substring(0, ipt).trim();
         int ipt2 = field.indexOf(':', ipt);
         if (ipt2 < 0) {
           data.strCity = convertCodes(field.substring(ipt+5).trim(), CITY_CODES);
         } else {
           data.strCity = convertCodes(field.substring(ipt+5, ipt2).trim(), CITY_CODES);
           field = field.substring(ipt2+1).trim().replaceAll("@", "");
           parseAddress(field, data);
         }
       }
   
       else {
         
         // Not mutual aid.
         // See if we can find a city: marker
         Matcher match = CITY_PTN.matcher(field);
         String sCity = null;
         if (match.find()) {
           sCity = CITY_CODES.getProperty(match.group(1));
           if (sCity != null) {
             data.strCity = sCity;
             
             // If we find one, things get complicated...
             // Sometimes it marks the boundary between the call description and address
             ///Sometimes it marks the boundary between the address and a place name
             String part1 = field.substring(0,match.start()).trim();
             String part2 = field.substring(match.end()).trim();
             Result res = parseAddress(StartType.START_ADDR, part2);
             if (res.getStatus() > 0) {
               data.strCall = part1;
               res.getData(data);
               data.strPlace =  res.getLeft();
             } else {
               parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, part1, data);
               data.strPlace = part2;
             }
           }
         }
         
         // If we didn't find a city parse a generic call/address/place field
         if (sCity == null) {
           int pt = field.indexOf(": @");
           if (pt >= 0) {
             data.strPlace = field.substring(pt+3).trim();
             field = field.substring(0,pt);
             parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, field, data);
           } else {
             parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, field, data);
             data.strPlace = getLeft();
           }
         }
       }
       
       // If call is to Franklin or Adams county, set state to PA
       if (data.strCity.equals("Franklin County") || data.strCity.equals("Adams County")) {
         data.strState = "PA";
       }
     }
     
     @Override
     public String getFieldNames() {
       return "CALL ADDR APT CITY ST PLACE";
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       if (field.contains("[") && field.contains("]")) {
         field = field.substring(0,field.indexOf("[")).trim();
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private static final Properties CITY_CODES = 
     buildCodeTable(new String[]{
         "ADAM","Adams County", //PA
         "ADAM CO", "Adams County",
         "ADCO","Franklin County", //PA
         "BRUN","Brunswick",   
         "CARRCO", "Carroll County",
         "CEMB","Emmitsburg",
         "CFR1","Frederick City",
         "CFR2","Frederick City",
         "CFR3","Frederick City",
         "CFR4","Frederick City",
         "CIJM", "New Market",
         "CMON", "Monrovia",
         "CNMA","New Market",
         "CMTY","Mt Airy",
         "CSAB","Sabillasville",
         "CTHU","Thurmont",
         "CWAL","Walkersville",
         "FRAN CO", "Franklin County",
         "FRE1", "Frederick City",
         "MTAY", "Mt Airy",
         "NEWM", "New Market",
         "THUR","Thurmont",
         "WOOD","Woodsboro",
   });
 }

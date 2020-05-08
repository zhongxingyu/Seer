 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
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
 
 
 ***/
 
 public class MDFrederickCountyParser extends SmartAddressParser {
   
   private static final String DEF_STATE = "MD";
   private static final String DEF_CITY = "FREDERICK COUNTY";
 
  private static final String[]Fredrickkeywords = new String[]{"CT", "CMTY TIME", "ESZ", "MAP", "Disp",};
   
   private static final Properties CITY_CODE_TABLE = 
     buildCodeTable(new String[]{
         "ADAM","Adams County", //PA
         "ADAM CO", "Adams County",
         "ADCO","Franklin County", //PA
         "BRUN","Brunswick",   
         "CARRCO", "Carroll County",
         "CEMB","Emmitsburg",
         "CFR2","Frederick City",
         "CNMA","New Market",
         "CSAB","Sabillasville",
         "CWAL","Walkersville",
         "FRAN CO", "Franklin County",
         "FRE1", "Frederick City",
         "THUR","Thurmont",
         "WOOD","Woodsboro",
     });
 
   public MDFrederickCountyParser(){
     super(CITY_CODE_TABLE, DEF_CITY, DEF_STATE);
   }
 
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     String[] subjects = subject.split("\\|");
     if (subjects.length > 2) return false;
     if (subjects.length == 2) {
       if (subjects[0].equals("CAD")) subject = subjects[1];
       else if (subjects[1].equals("CAD")) subject = subjects[0];
       else return false;
     }
     if (!subject.equals("FredCo")) return false;
     
     // If there is a : right after city then there is Supp data between City Code and ESZ:
     
     Properties props = parseMessage(body, Fredrickkeywords);
     String strAddr = props.getProperty("CT");
     if (strAddr == null) return false;
     
     // Everything changes if this is a Mutual aid call
     int ipt = strAddr.indexOf(" @MA ");
     if (ipt >= 0) {
       data.strCall = "Mutual Aid: " + strAddr.substring(0, ipt).trim();
       int ipt2 = strAddr.indexOf(':', ipt);
       if (ipt2 < 0) {
         data.strCity = strAddr.substring(ipt+5).trim();
       } else {
         data.strCity = strAddr.substring(ipt+5, ipt2).trim();
         strAddr = strAddr.substring(ipt2+1).trim().replaceAll("@", "");
         parseAddress(strAddr, data);
       }
     }
 
     else {
       
       // Look for a colon marking the start of the place name
       // too make things more interesting, we have to skip over any colons
       // in an LL(...) function which marks GPS coordinates
       int p1 = strAddr.indexOf(':');
       if (p1 >= 0) {
         int p2 = strAddr.indexOf('(');
         if (p2 >= 0 && p2 < p1) {
           p2 = strAddr.indexOf(')', p2+1);
           if (p2 >= 0) p1 = strAddr.indexOf(':', p2+1);
         }
       }
       if (p1 >= 0) {
         data.strPlace = strAddr.substring(p1+1).trim();
         strAddr = strAddr.substring(0, p1).trim();
       }
       parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END , strAddr, data);
     }
     
     data.strMap = props.getProperty("MAP", "");
     data.strBox = props.getProperty("ESZ", "");
     data.strUnit = props.getProperty("Disp", "");
     data.strCity = convertCodes(data.strCity, CITY_CODE_TABLE);
     if (data.strCity.equals("Franklin County") || data.strCity.equals("Adams County")) {
       data.strState = "PA";
     }
     if (data.strUnit.contains("[") && data.strUnit.contains("]")) {
         data.strUnit = data.strUnit.substring(0,data.strUnit.indexOf("[")).trim();
       }
     return true;
   }
 }

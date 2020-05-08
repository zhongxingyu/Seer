 
 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 /*
 Watauga County, NC
 Contact: Michael Vollmer <michael@wataugarescue.org>
 System: Southern
 
 HOWARDS CREEK RD / SUGARLOAF RD FDL 71B01 2011013987 16:02:05 VEHICLE FIRE 421N-RT TATER HILL RD-LT HOWARDS CREEK RD TO SUGARLOAF RD
 524 JONES DR BOONE MDL 01A01 2011013875 02:10:09 ABDOMINAL PAIN 194S-CHESTNUT GRV RD-LT COBBS CREEK RD-1ST RT JONES DR
 131 BIG VALLEY ST BOONE 2011013813 13:15:57 STRUCTURE FIRE 321S-LT DEERFIELD RD-TRAVEL 2.0 MI-RT BIG VALLEY ST INTO DEERFIELD MEADOWS BUSINESS PARK
 200 QUAIL ST BOONE 2011013939 03:11:32 STANDBY
 853 HIDDEN VALLEY DR BOONE MDL 09E01 2011013997 20:13:36 ARREST-ADULT 105S-BESIDE HIDDEN VALLEY MOTEL-LT HIDDEN VALLEY DR
 
 Contact:  Jacob Burleson <jacobburleson@gmail.com>
 Sender: wcso911@wataugacounty.org
 wcso911:190 MCGUIRE MOUNTAIN RD BANNER ELK 2011015829 22:33:54 10-50 PI 105S-RT BROADSTONE RD-LT 194S TO MATNEY-LT PIGEON ROOST RD-3RD LT MCGUIRE MTN RD
 
Contact: shane garland <boone2137@gmail.com>
wcso911@wataugacounty.org 164 BERTON ST BOONE MDL 06D02 2011017104 01:24:25 SICK PERSON 421S-RT OLD 421S-RT BROWNS CHAPEL RD-2ND LT NORTHRDG DR-1ST RT BERTON ST

 */
 
 
 public class NCWataugaCountyParser extends DispatchSouthernParser {
   
   private static final String[] CITY_LIST = new String[]{
     "BEECH MOUNTAIN",
     "BLOWING ROCK",
     "BOONE",
     "SEVEN DEVILS",
     
     "DEEP GAP",
     "SUGAR GROVE",
     "VALLE CRUCIS",
     "ZIONVILLE",
     "FOSCOE"
   };
 
   public NCWataugaCountyParser() {
     super(CITY_LIST, "WATAUGA COUNTY", "NC", DSFLAG_OPT_DISPATCH_ID);
   }
   
   @Override
   public String getFilter() {
     return "wcso911@wataugacounty.org";
   }
 
   @Override
   protected void parseExtra(String sExtra, Data data) {
     int pt = sExtra.indexOf(' ');
     if (pt < 0) {
       data.strCall =  sExtra;
       return;
     }
     int pt2 = sExtra.indexOf('-', pt+1);
     if (pt2 >= 0) pt = sExtra.lastIndexOf(' ', pt2);
     data.strCall = sExtra.substring(0,pt).trim();
     data.strSupp = sExtra.substring(pt+1).trim();
   }
 }

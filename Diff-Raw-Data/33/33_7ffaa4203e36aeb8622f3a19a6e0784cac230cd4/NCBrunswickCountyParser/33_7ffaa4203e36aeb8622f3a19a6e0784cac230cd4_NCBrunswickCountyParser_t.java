 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 /* 
 Brunswick County, NC
 Contact: "~Heather~" <sunshine5grl@gmail.com>
 Contact: Tom Rogers <tomrogers2@gmail.com
 Sender: pagegate@brunswickes.com
 System: Southern Software
 
 349:560 CUMBERLAND ST SE BOLIVIA MDL 09B01 11-020650 17:02:34 Cardiac or Respiratiory Arrest / Death RECV CALL FROM FUNERAL HOME ADVING THAT THEY RECV CALL STATING A FEMALE WAS DEAD AT THIS LOCATION AD
 415:111 SE 32ND ST OAK ISLAND VERIZON WIRELESS 7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Respiratiory Arrest - De
 411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD
 399:1295 DECATOR ST SW SUPPLY PAM 9106339368 MDL 09E01 11-025419 22:11:30 Cardiac or RespArrest  CALLER ADV THE PT IS STIFF
 428:BAY POINT CT / JENNIS AV FELICIA  9103685110 MDL 27D04 11-026412 21:09:48 Stab-Gunshot-Pen Trauma AT THE END OF  RD .
 426:BAY POINT CT-JENNIS AV FELICIA 9103685110 11-026412 21:07:25 Stab-Gun-Trauma AT THE END OF BAYPOINT RD MALE SUBJ IS CUT
 415:111 SE 32ND ST OAK ISLAND  7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Resp Arrest - Death TRANS BY OKI... ADVD TH
 411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD,
 
Contact: Anthony Nathan <tnathan55@gmail.com>
1333 SOUTH DICKINSON DR LELAND 11-053813 10:30:06 Take Written Report (10-92) AT SUITE 110....10-83 WITH MIRANDA REF PROPERTY DAMAGE TO VEH
1111 NEW POINTE BLVD LELAND 11-053827 11:42:05 911 HANG UP ON CALL BACK SPOKE W/ LAURA ADVD SUBJS SHE WAS CALLING IN REF TO LEFT THE BUSN

 */
 
 public class NCBrunswickCountyParser extends DispatchSouthernParser {
   
   private static final Pattern PHONE_PTN = Pattern.compile("\\b\\d{10}\\b");
   
   public NCBrunswickCountyParser() {
    super(CITY_LIST, "BRUNSWICK COUNTY", "NC", DSFLAG_OPT_DISPATCH_ID);
   }
   
   @Override
   public String getFilter() {
     return "pagegate@brunswickes.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    
     if (!super.parseMsg(body, data)) return false;
     
     // The space usually occupied by a common name is used for personal name information here
     Matcher match = PHONE_PTN.matcher(data.strPlace);
     if (match.find()) {
       data.strPhone = match.group();
       data.strPlace = data.strPlace.substring(0,match.start()) + data.strPlace.substring(match.end());
       data.strPlace = data.strPlace.replaceAll("  +", " ").trim();
     }
     data.strName = data.strPlace;
     data.strPlace = "";
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "LOCKWOODS FOLLY",
     "NORTHWEST",
     "SHALLOTTE",
     "SMITHVILLE",
     "TOWN CREEK",
     "WACCAMAW",
 
     "BALD HEAD ISLAND",
     "BELVILLE",
     "BOILING SPRING LAKES           ",
     "BOLIVIA",
     "CALABASH",
     "CAROLINA SHORES",
     "CASWELL BEACH",
     "HOLDEN BEACH",
     "LELAND",
     "NAVASSA",
     "NORTHWEST",
     "OAK ISLAND",
     "OCEAN ISLE BEACH",
     "SANDY CREEK",
     "SHALLOTTE",
     "SOUTHPORT",
     "ST. JAMES",
     "SUNSET BEACH",
     "VARNAMTOWN",
 
     "ANTIOCH",
     "ASH",
     "BATARORA",
     "BELL SWAMP",
     "BISHOP",
     "BIVEN",
     "BONAPARTE LANDING",
     "BOONES NECK",
     "BOWENSVILLE",
     "BRUNSWICK STATION",
     "CAMP BRANCH",
     "CEDAR GROVE",
     "CEDAR HILL",
     "CIVIETOWN",
     "CLAIRMONT",
     "CLARENDON",
     "COOLVALE",
     "DOE CREEK",
     "EASTBROOK",
     "EASY HILL",
     "PINEY GROVE",
     "SUPPLY",
     "SUNSET HARBOR",
     "WINNABOW",
   };
 
 }

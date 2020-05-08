 package net.anei.cadpage.parsers.NY;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Suffolk County, NY
 Sender: paging@scfres.com
 System: Intergraph
 
 Brentwood
 Contact: Jason Pedota <jpedota805@gmail.com>
 Sender: 6317967579
 TYPE: GAS LEAKS / GAS ODOR (NATURAL / L.P.G.) LOC: 11 BRENTWOOD PKWY BRENTW HOMELESS SHELTER CROSS: PENNSYLVANIA AV / SUFFOLK AV CODE: 60-B-2 TIME: 12:54:16
 TYPE: STRUCTURE FIRE LOC: 81 NEW HAMPSHIRE AV NBAYSH  CROSS: E FORKS RD / E 3 AV CODE: 69-D-10 TIME: 16:36:48
 TYPE: OPEN BURNING LOC: 65 GRANT AVE BRENTW CROSS: SUFFOLK AVE CODE: 54-C-6 TIME: 18:39:20
 TYPE: BLEEDING / LACERATIONS LOC: 462 SPUR DR N NBAYSH  CROSS: WB SSP OFF RAMP-X42N 5TH AV / E 3 AV CODE: 21-A-1 TIME: 03:36:22
 TYPE: PREGNANCY / CHILDBIRTH / MISCARRIAGE LOC: 330 MOTOR PKWY HAUPPA:@FELDMAN, KRAMER & MONACO STE 400  CROSS: WASHINGTON AV / MARCUS BLVD C
 TYPE: PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE LOC: 200 WIRELESS BLVD HAUPPA: @SOCIAL SERVICES HAUPPAUGE INTERVIEW AREA CROSS: MORELAND RD /
 
 Center Moriches, NY
 Contact: William Renzetti <williamrenzetti@gmail.com> (works for Suffolk County 911)
 FAINTING (NEAR) LOC: 46 SAUNDERS AV CENTEM:@KINGS CHAPEL CHURCH  CROSS: ROWEIN RD CODE: 31-D-2 TIME: 17:24:12
 SEIZURES LOC: 20 TRAINOR AV CENTEM CROSS: SUNRISE HWY S  BERNSTEIN BLVD CODE: 12-C-1 TIME: 03:39:02
 ABDOMINAL PAINS LOC: 18 INWOOD RD CENTEM CROSS: UNION AV / BEACHFERN RD CODE: 1-A-1 TIME: 21:21:39
 UNKNOWN PROBLEM LOC: 150 CHICHESTER AV CENTEM CROSS: YARMOUTH LN / FROWEIN RD CODE: TIME: 08:01:10
 FAINTING (NEAR) LOC: 6 FROWEIN RD EMORIC: @CEDAR LODGE NURSING HOME IN THE DINING ROOM CROSS: WALDEN CT  OAK ST CODE: 31-D-3 TIME: 18:51:38
 GAS ODOR (NATURAL  L.P.G.) LOC: MCGRAW ST SHIRLE I/V/O SHIRLEY PLAZA CROSS: GRAND AV / OAK AV CODE: 60-C-2 TIME: 02:06:34
 FAINTING (NEAR) LOC: 53-10 LONG TREE LN MORICH: @PINE HILLS SOUTH CLUBHOUSE COMMUNITY ROOM CROSS:    CODE: 31-E-1 TIME: 14:54:18
 TYPE: ALARMS LOC: 127 CRYSTAL BEACH BLVD MORICH  CROSS: BEVERLY CT / CODE: 52-B-1C TIME: 21:15:11
 
 Manorville Community Ambulance, NY
 Contact: Christopher Greco <cgreco31@gmail.com>
 TYPE: PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE LOC: 79 ABBOTT AV MASTIC ***_VIP_***: CROSS: ELGIN ST / FOXCROFT ST CODE: 25-B-6 TIME: 14:36:05
 
 Oakdale
 Contact: Michael Heerbrandt <wsfd145@gmail.com>
 Sender: PAGING@WSFD.COM
 TYPE: ALARMS LOC: 100 PATRICIA CT OAKDAL @OAKDALE APARTMENTS    APARTMENT 3 CROSS: RACE PL /  CODE: 52-C-1S TIME: 19:16:55
 
 Contact: Sean Hendrickson <sean.hendrickson5@gmail.com>
 FWD: TYPE: STRUCTURE FIRE LOC: 1 WILBUR AV MANORV  CROSS: SOHMER ST /  CODE: 69-D-5 TIME: 17:17:43
 
 Contact: Chris Wagner <cwagner021@gmail.com>
 TYPE: MOTOR VEHICLE ACCIDENT  CROSS: CHURCH ST / LAKELAND AV CODE: 29-B-1U TIME: 09:04:01
 
 Contact: Scott Parris <fireteacher@gmail.com>
 TYPE: ALARMS LOC: 311 BAY AV EPATCH: @BAY HOUSE  CROSS: NEWINS ST / PARK ST CODE: 52-C-3G TIME: 15:33:58\n\n
 
 Contact: Chris Wagner <cwagner021@gmail.com>
 Sender: paging+bncCAAQveHY8wQaBJlKIvM@communityamb.org
 TYPE: CHEST PAIN LOC: 3845 VETERANS MEMORIAL HWY RONKON: @HOLIDAY INN RONKONKOMA: @HOLIDAY INN RONKONKOMA:06:12.4418,40:48:13.4 PARKI
 
 Contact: Michael Bellis <bellis.michael@gmail.com>
 Sender: paging@scfres.com
 TYPE: UNKNOWN PROBLEM LOC: 195 CUBA HILL RD GREENL  CROSS: MANOR RD / DANVILLE DR CODE: 32-B-2 TIME: 17:30:40
 TYPE: STRUCTURE FIRE LOC: 1 ARNOLD DR HUNTIN  CROSS: PARTRIDGE LN /  CODE: default TIME: 06:38:03
 TYPE: STRUCTURE FIRE LOC: 6 MAJESTIC DR DIXHIL  CROSS: ROYAL LN / REGENCY LN CODE: 69-D-6 TIME: 02:22:25
 TYPE: FALLS LOC: 37 WATERSIDE AV NORTHP  CROSS: MONROE ST / WILLIS ST CODE: 17-B-3 TIME: 13:40:45
 
 Contact: Stuart NOHEJO <stupman1975@gmail.com>
 Sender: paging@scfres.com
 TYPE: HEADACHE LOC: 68 FOREST AV SHIRLE  CROSS: DAWN DR / WINSTON DR CODE: 18-C-2 TIME: 16:09:54\n\n
 
 Contact: PJ Llobell <pllobell@gmail.com>
 Sender: paging@scfres.com
 TYPE: RESPIRATORY LOC: 16 TEAL CRSN GREATR   CROSS:  / WIDGEON CT CODE: 6-D-1 TIME: 15:59:03
 
 Contact: Michael Bellis <bellis.michael@gmail.com>
 TYPE: STRUCTURE FIRE LOC: 605 7 AV ENORTH   CROSS: 6 ST / OWEN CT CODE: 69-D-6 TIME: 11:34:38
 
Contact: Joe Sauerwein <jfsauerwein@gmail.com>
Sender: paging@scfres.com
TYPE: ALARMS LOC: 32 MONTAUK HWY BLUEPO : @BURGER KING BLUE POINT  CROSS: BELL AV / BUFFIN LN CODE: 52-C-3P TIME: 13:20:43\r\n\r\n\r

 */
 
 public class NYSuffolkCountyAParser extends SmartAddressParser {
 
   private static final String[] KEYWORDS = new String[]{"TYPE", "LOC", "CROSS", "CODE", "TIME"};
 
   private static final Properties CITY_TABLE = buildCodeTable(new String[]{
       "BAYSHO", "Bay Shore",
      "BLUEPO", "Blue Point",
       "BRENTW", "Brentwood",
       "CENTEM", "Center Moriches",
       "DIXHIL", "Dix Hills",
       "EMORIC", "East Moriches",
       "ENORTH", "East Northport",
       "EPATCH", "East Patchogue",
       "GREATR", "Great River",
       "GREENL", "Greenlawn",
       "HUNTIN", "Huntington",
       "HUNTIS", "Huntington Station",
       "HAUPPA", "Hauppauge",
       "MANORV", "Manorville",
       "MASTIC", "Mastic",
       "MORICH", "Moriches",
       "NBAYSH", "Bay Shore",
       "NORTHP", "Northport",
       "OAKDAL", "Oakdale",
       "PATCH",  "Patchogue",
       "SHIRLE", "Shirley",
       "RONKON", "Ronkonkoma"
   });
   
   private static final String DEF_STATE = "NY";
   private static final String DEF_CITY = "SUFFOLK COUNTY";
   
   public NYSuffolkCountyAParser() {
     super(CITY_TABLE, DEF_CITY, DEF_STATE);
   }
   
   @Override
   public String getFilter() {
     return "paging@scfres.com,@communityamb.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Anything starting with 3 asterisks is the similar but different (B) variant
     if (body.startsWith("***")) return false;
 
     // Some formats cut the initial TYPE: code
     if (body.startsWith("FWD:")) body = body.substring(4).trim();
     if (!body.startsWith("TYPE:")) body = "TYPE:" + body;
 
     Properties props = parseMessage(body, KEYWORDS);
     
     data.strCall = props.getProperty("TYPE");
     if (data.strCall == null) return false;
 
     data.strCross = props.getProperty("CROSS", "");
     
     String sAddress = props.getProperty("LOC");
     if (sAddress == null) {
       if (data.strCross.length() == 0) return false;
       parseAddress(data.strCross, data);
       data.strCross = "";
     } else {
       sAddress = sAddress.replaceAll(":", " ");
       int pt = sAddress.indexOf('@');
       if (pt >= 0) {
         String sPlace = sAddress.substring(pt+1).trim();
         int pt2 = sPlace.indexOf('@');
         if (pt2 >= 0) {
           data.strSupp = sPlace.substring(pt2+1).trim();
           sPlace = sPlace.substring(0,pt2).trim();
         }
         data.strPlace = sPlace;
         sAddress = sAddress.substring(0,pt).trim();
         pt = sAddress.lastIndexOf(' ');
         if (pt >= 0) {
           data.strCity = sAddress.substring(pt+1);
           sAddress = sAddress.substring(0, pt).trim();
         }
         parseAddress(sAddress, data);
       }
       else {
         parseAddress(StartType.START_ADDR, sAddress, data);
         data.strPlace = getLeft();
       }
     }
     
     data.strCode = props.getProperty("CODE", "");
     data.strCity = convertCodes(data.strCity, CITY_TABLE);
     String sTime = props.getProperty("TIME", "");
     if (sTime.length() > 5 && sTime.length() < 8) sTime = sTime.substring(0,5);
     if (sTime.length() >= 5) data.strTime = sTime;
     
     return true;
   }
 }

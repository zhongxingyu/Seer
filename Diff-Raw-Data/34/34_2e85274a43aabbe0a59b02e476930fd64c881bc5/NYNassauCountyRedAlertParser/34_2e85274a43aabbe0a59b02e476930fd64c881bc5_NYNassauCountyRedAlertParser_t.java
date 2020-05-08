 package net.anei.cadpage.parsers.NY;
 
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchRedAlertParser;
 
 /*
 Nassau County, NY
 Contact: Besnik Gjonlekaj <tigerfdny@gmail.com>
 Sender: paging@rednmxcad.com & paging@alpinesoftware.com
 System: Red Alert
 
 Belmore, NY
 Contact: Timothy Beuth <tbeuth@gmail.com>
 RESCUE at 2438 BEDFORD PLACE. . 11:34:04
 AUTO ACCIDENT at intersection of BELTAGH AVENUE and BELLMORE AVENUE. . 15:03:39
 AUTO ACCIDENT at 2971 LEE PLACE. . 04:08:29
 MUTUAL AID RESCUE at 3015 CHERYL ROAD. . 19:12:04
 AUTOMATIC ALARM BUILDING . . UNREGISTERED ALARM at 609 SUNRISE HIGHWAY. . 11:46:13
 HAZMAT - NATURAL GAS INTERIOR . . ODOR INSIDE at 1 SUNRISE HIGHWAY. . 05:07:37
 HOUSE FIRE . . SMOKE IN THE HOUSE at 2764 MARTIN AVENUE. . 17:09:53
 6011 IS OUT OF SERVICE
 6011 IS BACK IN SERVICE 
 
 
 Levittown Fire Department
 Contact: Adam Freed <afreed00789@gmail.com>
 Sender: paging@rednmxcad.com <From%3Apaging@rednmxcad.com>
 System: Red Alert
 [Auto Alarm]  at 2890 HEMPSTEAD TPKE #A, LEVITTOWN  c/s: SHELTER LN   O: THE LITTLE GYM . . 16:01:16
 [Phone In]  at 47 STEVEDORE LN, LEVITTOWN  c/s: PARSON LN   O: CAIOLA . . 10:24:26
 Signal 9 at 62 FLAMINGO RD, LEVITTOWN  c/s: GULL LN   O: OLSEN . . 09:34:27
 Brush Fires at TARRY LN / BAYBERRY LN, LEVITTOWN . . 14:28:07
 
 Hicksville FD
 SIGNAL 9 at 17 BRIDLE LN, HICKSVILLE c/s: BLUEBERRY LN O: KENDALL, ANNA..14:48:48
 BUILDING FIRE: SMOKE IN THE STRUCTURE  at 6 W VILLAGE GRN, HICKSVILLE  c/s: BARTER LN   O: ANNE'S WASH AND DRY . . 13:29:45
 AUTOMATIC ALARM: ZONE 58 / AFA NO ANSWER ON THE CALL BACK  at 111 CANTIAGUE ROCK RD, WESTBURY  c/s: W JOHN ST   O: NASSAU BOCES - CAREER PREPRATORY HIGH S
 AUTOMATIC ALARM: WATER FLOW at 108 NEW SOUTH RD #A, HICKSVILLE  c/s: COMMERCIAL ST   O: 108-122 NEW SOUTH ROAD REALTY INC.  (MULTIPLE BUSINESSES) . . 14:2
 GENERAL ALARM, AUTOMATIC ALARM: FRONT SMOKE DETECTOR/PULL STATION at 284 W PARK AVE, LONG BEACH  c/s: LAURELTON BLVD   O: LONG BEACH
 
 Glen Cove FD
 GENERAL ALARM, POSSIBLE HOUSE FIRE: SMOKE IN THE HOUSE at 62 TOWN PATH RD, GLEN COVE  c/s: WALNUT RD   O: CAPOBIANCO . . 12:34:31
 SIG 8, CARBON MONOXIDE ACTIVATION at 238 WALNUT RD EXT, GLEN COVE  c/s: SEAMAN RD   O: IANNONE . . 11:32:25
 GENERAL ALARM, AUTOMATIC FIRE ALARM at 5 MCKINLEY PL, GLEN COVE  c/s: GRANT PL   O: RASENBERGER . . 19:11:33
 GENERAL ALARM, ELECTRICAL BURNING at 10 YALE PL, GLEN COVE c/s: PURDUE RD O: MORRISSEY..09:27:08
 
 Oyster Bay FD
 RESCUE, AMBU at OYSTER BAY LIBRARY 89 MAIN ST E, OYSTER BAY c/s: PEARL ST / ANSTICE ST, PEDESTRIAN STRUCK . . 12:45:00
 CHIEF, WIRE at  SANDY HILL ROAD, OYSTER BAY c/s: MAIN (E) ST, WIRES BURNING . . 00:20:00
 
 ??? FD
 Contact: Daniel Dutton <ncfm80@gmail.com>,NCFM80@vtext.com,5163186932@vzwpix.com
 Automatic Alarm - Residential, Water Flow: Zone: 43, Response:CO4, CO3\nat 51 GREAT NECK RD, GREAT NECK   c/s: SO. MIDDLE NECK RD.   O: ATRIA (Use For Fire
 MVA w/ Aided: Zone: 54, Response:CO5, CO4, CO6\nat LAKEVILLE RD / NORTHERN STATE PKWY, LAKE SUCCESS . . 10:28:36
 Automatic Alarm - Residential: Zone: 21, Response:CO2, CO1\nat 32 BUCKMINSTER LA, STRATHMORE VILLAGE  c/s: SUSSEX DR   O: URALLEY . . 21:03:10
 Automatic Alarm - Commercial: Zone: 54, Response:CO5, CO4\nat 1 DELAWARE DR, LAKE SUCCESS QUARD  c/s: MARCUS AV   O: PARKER JEWISH CENTER-PRO HEALTH . . 1
 MVA w/ Aided: Zone: 43, Response:CO4, CO3, CO6\nat 363 GREAT NECK RD, GREAT NECK  c/s: WATER MILL LA   O: REMA AUTO COLLISION . . 12:56:01
 [Brush] Trees: Zone: 45, Response:CO4, CO5\nWIRES DOWN at WESTMINSTER RD / BATES RD, LAKE SUCCESS . . 15:59:47
 MVA w/ Aided: Zone: 43, Response:CO4, CO3\nat NORTHERN BLVD / GREAT NECK RD, GREAT NECK . . 17:45:30
 [Brush] Trees: Zone: 54, Response:CO5\nat NEW HYDE PARK RD / NORTH SERVICE RD, NEW HYDE PARK . . 18:32:59
 Carbon Monoxide w/ Aided: Zone: 52, Response:CO5, CO6\nat 45 CHESTNUT HILL, NORTH HILLS  c/s: ACORN PONDS DR . . 00:59:33
 
 Contact: Nick Stein <snickphotos@gmail.com>,15166443087@tmomail.net
 / STILL ALARM: ODOR OF GAS at CENTRAL AVE / ROCKAWAY TPKE, LAWRENCE . . 14:20:55
 
Plainview FD
Contact: Jan Schatzberg <390janpfd@gmail.com>
Sender: notifications@Plainviewfd.com
BUILDING FIRE, AUTOMATIC ALARM - BUILDING at 205 BETHPAGE SWEETHOLLOW ROAD, OLD BETHPAGE  c/s: HUB DRIVE   O: ALJO-GEFFA PRECISION MANUFACTURING, LLC

 */
 
 public class NYNassauCountyRedAlertParser extends DispatchRedAlertParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "LAKE SUCCESS QUARD", "LAKE SUCCESS",
       "STRATHMORE VILLAGE", "STRATHMORE"
   });
   
   public NYNassauCountyRedAlertParser() {
     super("NASSAU COUNTY","NY");
   }
 
   @Override
   public String getFilter() {
    return super.getFilter() + ",alarms@rvcny.us,notifications@Plainviewfd.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     body = body.replace('\n', ' ');
     if (!super.parseMsg(subject, body, data)) return false;
     
     // Supp info can have all kinds of fun things
     Parser p = new Parser(data.strSupp);
     data.strUnit = p.getLastOptional(", Response:");
     data.strMap = p.getLastOptional("Zone:");
     String sInfo = p.get();
     if (sInfo.endsWith(":")) sInfo = sInfo.substring(0, sInfo.length()-1).trim();
     data.strSupp = sInfo;
     
     // Some of the city names need to be adjusted
     data.strCity = convertCodes(data.strCity, CITY_CODES);
     return true;
   }
 }

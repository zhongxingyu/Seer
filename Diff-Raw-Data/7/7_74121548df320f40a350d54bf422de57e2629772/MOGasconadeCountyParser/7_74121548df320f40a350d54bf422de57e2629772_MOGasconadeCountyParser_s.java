 package net.anei.cadpage.parsers.MO;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchGlobalDispatchParser;
 
 /*
 Gasconade County, MO
 Contact: Jason Reed <reedjasonf@gmail.com>
 Sender: central@fidmail.com
 System: Global Dispatch
 
 MESSAGE / M50 CHEST PAIN  4449 MARIES CR 426 OUT OF COUNTY MapRegions:  CrossStreets: Description:  Dispatch: 3/25/2011 03:51:43\n\n\n\n\n
 MESSAGE / M51 MOTOR VEHICLE ACCIDENT-INJURIES  HWY 89  MapRegions:  CrossStreets: Description:  Dispatch: 3/26/2011 11:29:32\n\n\n\n\n
 MESSAGE / M51 MOTOR VEHICLE ACCIDENT-UNKNOWN INJ  HWY 89  MapRegions:  CrossStreets: Description:  Dispatch: 3/26/2011 11:39:53\n\n\n\n\n
 M51 FALL- PERSON FELL 408 N ALVARADO AVE BELLE MapRegions: CrossStreets: E BELAIR AVE 0.03 mi S E ROHRER DR 0.13 mi N Description: Dispatch: 3/24/2011 11:47:54
 M50 SICK PERSON- SICK CASE 4644 HIGHWAY B, Apt. BOX 14 GASCONADE COUNTY MapRegions: PAGE 103 CrossStreets: ELK HEAD RD 0.08 mi SW RED BIRD RD 0.46 mi N Description: HALF MILE SOUTH OF REDBIRD RD Dispatch: 3/24/2011 11:25:45
 M50 SICK PERSON- SICK CASE 901 W EIGHTH ST BELLE MapRegions: CrossStreets: S SHOCKLEY AVE 0.01 mi NE S APPLE AVE 0.19 mi E Description: Dispatch: 3/24/2011 21:05:48
 
 Contact:  bretth530 <bretth530@gmail.com>
 501 FALL- PERSON FELL FRENE VALLEY HEALTHCARE SOUTH 1016 W HIGHWAY 28 OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 072 CrossStreets: HIGHWAY Y 0.05 mi N S L&D DR 0.44 mi SW Description: 96 YOA FALL BROKEN WRIST Dispatch: 4/3/2011 06:00:43
 501 ACCIDENTAL INJURY  1009 SPRINGFIELD RD, Apt. APT 21 OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 065, PAGE 122 CrossStreets: CHERRY AVE 0.06 mi SE INDUSTRIAL DR 0.22 mi NE Description:  Dispatch: 4/6/2011 12:08:04\n\n\n\n
 O164 O171 O172 501 MOTOR VEHICLE ACCIDENT WITH RESCUE GASCONADE COUNTY E-911 405 E LINCOLN AVE OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 073, PAGE 123 CrossStreets: OLIVE ST 0.08 mi E N WALNUT ST 0.04 mi W Description: TEST CALL FOR OFD***********TEST**********TEST Dispatch: 5/10/2011 1:39:36 PM Dispatch: 5/10/2011 1:39:36 PM Dispatch: 5/10/2011 1:39:36 PM Dispatch: 5/10/2011 1:39:36 PM
 O164 O171 O172 O170 501 726 O160 GAS LEAK OUTSIDE  2935 LAKE NORTHWOODS RD OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 057, PAGE 117 CrossStreets: HIGHWAY 19 0.22 mi W Description: STRUTMANS. PROPANE TANK ROLLED OVER.\n\nCALLER STATES THAT HE WAS MOWING GRASS AND SAW THE PROPANE TANK ROLL OVER WITH GAS LEAKING . CALLER STATES THAT THE TANK IS ABOUT 50 FEET    FROM THE HOUSE. INITIAL CALLER STATES THAT HE KNOCKED ON THE DOOR WITH NO ANSWER, CALLER IS CALLING FROM THE NEIGHBORS HOUSE WHICH IS ALSO THE SISTER. ADVISED CALLER TO STAY AWAY AND STAY SAFE. CALLER IS STATING A VERY STRONG SMELL OF GAS . \n CALLED THE RP BACK AN\n More?
 502 TRAUMATIC INJURY  412 S CUBA ST OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 073, PAGE 125 CrossStreets: E JEFFERSON AVE 0.06 mi N E MADISON AVE 0.02 mi SW Description:  Dispatch: 5/11/2011 18:14:27\n\n\n\n
 
 1 of 3\nFRM:central@fidmail.com\nSUBJ:MESSAGE\nMSG:MT155 MT163 O172 501 MOTOR VEHICLE ACCIDENT WITH RESCUE  HIGHWAY A & HIGHWAY 50 GASCONADE COUNTY\n(Con't) 2 of 3\nMapRegions: OFD SECTOR 2, PAGE 045 CrossStreets: Description: 2 VEHICLE WITH ROLL OVER, DRIVER IS OUT. Dispatch: 6/20/2011 19:12:16\n(Con't) 3 of 3\nDispatch: 6/20/2011 19:12:16 Dispatch: 6/20/2011 19:12:16 Dispatch: 6/20/2011 19:12:16\n\n\n\n(End)

 
 */
 
 
 public class MOGasconadeCountyParser extends DispatchGlobalDispatchParser {
   
   private static final String[] CITY_TABLE = new String[]{
     "BLAND",
     "BELLE",
     "GASCONADE",
     "HERMANN",
     "MORRISON",
     "MT STERLING",
     "OWENSVILLE",
     "ROSEBUD",
     "GASCONADE COUNTY",
     "OUT OF COUNTY"
   };
   
  private static final Pattern UNIT_PTN = Pattern.compile("(?:[OM]|MT)?\\d{3}");
   
   public MOGasconadeCountyParser() {
     super(CITY_TABLE, "", "MO", null, UNIT_PTN);
   }
   
   @Override
   public String getFilter() {
     return "central@fidmail.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.startsWith("MESSAGE / ")) body = body.substring(10).trim();
     if (!super.parseMsg(body, data)) return false;
     if (data.strCity.equals("GASCONADE COUNTY")) data.strCity = "";
     return true;
   }
 }

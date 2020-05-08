 package net.anei.cadpage.parsers.SD;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Minnehaha county, SD
 Contact: brubin@med-starambulance.com
 Contact: James Dietz <kc0usq@gmail.com>
 Sender: 911metrodispatch@911metro.org
 
 930 Quad 2070 - VS 100 CLIFF AVE 2 VS Unconsc/Faint C3 2011-00000870
 930 Quad 460 - BR 904 SNOWBERRY AVE BR Conv Seizure C3 2011-00000374
 930 Quad 2060 - VS I 90 MM 410 VS Sick Person C1 2011-00000028
 930 Quad 2060 - VS 26472 484TH AVE VS Diabetic C2 2011-00000003
 930 Quad 2010 - SR 26525 482ND AVE BR Medical Emergency 2011-00000621
 930 Quad 460 - BR 3008 E ASPEN BLVD BR Falls C1 2011-00000051
 930 Quad 460 - BR 1625 RUSHMORE DR BR Family Dispute 2011-00000347 
 
 Contact: Dusty Kiner <dusty.kiner@gmail.com>
 (Dispatch Page) BR  Quad 460 - BR 1413 RUSHMORE DR 12 BR Falls C1 2011-00000111
 
 Contact: Craig Beaubien <sdiver1973@gmail.com>
 (Dispatch Page) Quad 800 - HD N WESTERN AVE OAKS DR HD Injury Accident C3 2011-00000163
 
 Contact: Joe Zweifel <jlzweifel@yahoo.com>
 (Dispatch Page) DR EM  Quad 660 - DR I 29 MM 98DR Inj Accident C1 + Fire 2011-00000192
 (Dispatch Page) CO LY HD EM  Quad 550 - CO 46166 250TH ST CO Structure Fire 2011-00000091
 (Dispatch Page) HU HD EM  Quad 940 - HU I 90 MM 382HU Structure Fire 2011-00000085
 
 */
 
 public class SDMinnehahaCountyParser extends SmartAddressParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "BA", "BALTIC",
       "BR", "BRANDON",
       "CO", "COLTON",
       "CR", "CROOKS",
 //      "DR", "DELL RAPIDS",  // gets confused drive DR road suffix :(
       "GA", "GARRETSON",
       "HD", "", // Should be Hartford but actually in sioux falls
       "HU", "HUMBOLT",
       "LY", "LYONS",
       "RE", "RENNER",
       "VS", "VALLEY SPRINGS",
       "SR", "SPLIT ROCK",
       "EM", ""
   });
   
   private static final Pattern CAD_MSG_PTN = 
     Pattern.compile("(?:(\\d{3}) +|((?:[A-Z]{2} +)+))?(Quad \\d{3,4}) - ([A-Z]{2}) +(.+?)(?: (C\\d))? (\\d{4}-\\d{8})");
   
   private static final Pattern MM_PTN = Pattern.compile("( MM \\d+)([A-Z]{2} )");
  
   public SDMinnehahaCountyParser() {
     super(CITY_CODES, "MINNEHAHA COUNTY", "SD");
   }
   
   @Override
   public String getFilter() {
     return "911metrodispatch@911metro.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = CAD_MSG_PTN.matcher(body);
     if (!match.matches()) return false;
     
     data.strSource = getOptGroup(match.group(1));
     data.strUnit = getOptGroup(match.group(2));
     data.strMap = match.group(3);
     String sCityCode = match.group(4);
     String sAddrFld = match.group(5);
     data.strCode = getOptGroup(match.group(6));
     data.strCallId = match.group(7);
     
     // Dispatch never puts a blank between mile markers and city codes :(
     sAddrFld = MM_PTN.matcher(sAddrFld).replaceFirst("$1 $2");
     
     // Whose bright idea was it to use DR as a city code?
     int pt = -1;
     if (sCityCode.equals("DR")) pt = sAddrFld.lastIndexOf(" DR ");
     if (pt >= 0) {
      parseAddress(sAddrFld.substring(0,pt).trim(), data);
       data.strCall = sAddrFld.substring(pt+4).trim();
       data.strCity = "DELL RAPIDS";
     } else {
      parseAddress(StartType.START_ADDR, sAddrFld, data);
       data.strCall = getLeft();
     }
     return true;
   }
 }

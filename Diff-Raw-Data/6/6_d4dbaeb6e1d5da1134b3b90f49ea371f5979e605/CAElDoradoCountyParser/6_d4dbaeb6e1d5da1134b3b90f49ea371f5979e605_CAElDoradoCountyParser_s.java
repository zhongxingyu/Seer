 package net.anei.cadpage.parsers.CA;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 El Dorado County, CA
 Contact: Ken Pauley <ken@pauley.org>
 Contact: "fyregoose@hotmail.com" <fyregoose@hotmail.com>
 Contact: "farlymot@hotmail.com" <farlymot@hotmail.com>
 Contact: Paul Brewer <pbrewer81@gmail.com>
 Sender: AEUCAD@fire.ca.gov
 
 (CAD Page) 15-May-2011/16:10:27: HAZ, ELECTRICAL: Inc# 010021: 5537 MORGAN RANCH LOOP ,GEORGETOWN  GEOCHF GEOENG M61 61; ; X: -120 50.5096  Y: 38 54.859
 (CAD Page) 19-May-2011/12:37:08: MEDICAL: Inc# 010306: 6530 WENTWORTH SPRINGS RD ,GEORGETOWN (GEORGETOWN ELEMENTARY SCHOOL) GEOCHF GEOENG M89 61; ; X: -120 49.84
 (CAD Page) 17-May-2011/01:52:37: MED, TRAFFIC COLLISION: Inc# 010121: 4800 BLACK OAK MINE RD / 4702 MARSHALL RD ,GARDEN_VALLEY  GRVCHF E2772 E251N M61 51 GEOENGN
 (CAD Page) 20-May-2011/12:16:59: MED, CODE 2: Inc# 010397: 5961 RESERVOIR RD ,GEORGETOWN  GEOCHF GEOENG M89 61; ; X: -120 51.3994  Y: 38
 (CAD Page) 20-May-2011/18:24:36: FIRE, SMOKE CHECK: Inc# 010430: 2990 GREENWOOD RD / 6272 HY 193 ,GEORGETOWN  GEOENGN M61 61; ; X: -120 50.5456  Y: 38 54.4036;
 (CAD Page) 21-May-2011/19:39:31: MEDICAL: Inc# 010537: 2850 DERRICK DR ,GREENWOOD  GRVCHF GEOENGN M25 53 E251N; ; X: -120 54.1379  Y: 38 53.9151;
 (CAD Page) 20-Jun-2011/16:46:01: MEDICAL: Inc# 013054: UNIVERSITY FALLS GATE @ =L(38.906110,-120.645832) ,GEORGETOWN  GEOCHF E33F M61 65 GEOENG; ; X: -120 38.749
 (CAD Page) 11-Aug-2011/06:18:13: MEDICAL: Inc# 018175: 5031 CEDAR DR ,GEORGETOWN  GEOCHF GEOENGN M61 61; ; X: -120 49.9174  Y: 38 53.4478;
 (CAD Page) 10-Aug-2011/10:03:52: MED, TRAFFIC COLLISION: Inc# 018090: 15250 WENTWORTH SPRINGS RD / 999998 ELEVEN PINES 14N08 ,GEORGETOWN_RANGER_DISTRI GEOCHF E54
 (CAD Page) 25-Sep-2011/19:06:43: MED, CODE 2: Inc# 022661: 1000 HY 193 / 3000 HY 49 ,COOL  E22 M61 72; ; X: -121 0.9537  Y: 38 53.2183;
 (CAD Page) 21-Jul-2012/18:36:57; FIRE, WILDLAND; Inc# 016722; 6085BLK W CHINA HILL RD ,EL_DORADO; B2709 U8113 E2751 E2769 E2784 E2774 E2752 E2782 E46 D2741 W46 W
 (CAD Page) 22-Jul-2012/12:23:38; MED, TRAFFIC COLLISION; Inc# 016781; 4732 HY 193 / 4600 MALTBY MINE RD ,GEORGETOWN; GEOCHF GEOENG M61 61 E51 E2752; X: -120 53.3
 (CAD Page) 26-Jul-2012/02:31:20; MEDICAL; Inc# 017152; 5780 TRAVERSE CREEK RD ,GARDEN_VALLEY; E2752 M61 52 E51; X: -120 48.3054 Y: 38 49.5004;
 (CAD Page) 03-Sep-2012/15:43:58; FIRE, SMOKE CHECK; Inc# 020679; =B(BALD MOUNTAIN LOOKOUT@272.5,6.0),GEORGETOWN; GEOENG M61 61 E2762; X: -120 48.7971 Y: 38 54.32
 
  */
 
 
 public class CAElDoradoCountyParser extends MsgParser {
   
   private static final Pattern MARKER = Pattern.compile("^(\\d{1,2}-\\w{3}-\\d{4})/(\\d\\d:\\d\\d:\\d\\d)[:;] ");
  private static final Pattern MASTER = Pattern.compile("^(.*?)[:;] Inc# (.*?)[:;] (.*?) *,([^,]*?)[ ;](?:\\((.*?)\\) )? *(.*?)(?:;|$)");
  private static final Pattern B_ADDR = Pattern.compile("=B\\(.*\\)");
   private static final DateFormat DATE_FMT = new SimpleDateFormat("dd-MMM-yyyy");
   
   public CAElDoradoCountyParser() {
     super("EL DORADO COUNTY", "CA");
   }
   
   @Override
   public String getFilter() {
     return "AEUCAD@fire.ca.gov";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("CAD Page")) return false;
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     setDate(DATE_FMT, match.group(1), data);
     data.strTime = match.group(2);
     body = body.substring(match.end()).trim();
     
     match = MASTER.matcher(body);
     if (!match.find()) return false;
     
     data.strCall = match.group(1).trim();
     data.strCallId = match.group(2).trim();
     String addr = match.group(3).trim();
     if (!B_ADDR.matcher(addr).matches()) {
       int pt = addr.indexOf('@');
       if (pt >= 0) {
         data.strPlace = addr.substring(0,pt).trim();
         addr = addr.substring(pt+1).trim();
       }
     }
     parseAddress(addr, data);
     data.strCity = match.group(4).trim().replace('_', ' ');
     if (data.strCity.startsWith("GEORGETOWN ")) data.strCity = "GEORGETOWN";
     String sPlace = match.group(5);
     if (sPlace != null) data.strPlace = sPlace.trim();
     data.strUnit = match.group(6).trim();
     
     body = body.substring(match.end()).trim();
     if (body.startsWith(";") || body.startsWith(";")) body = body.substring(1).trim();
     if (body.endsWith(";") || body.endsWith(";")) body = body.substring(0,body.length()-1).trim();
     data.strGPSLoc = body;
     
     return true;
   }
 }

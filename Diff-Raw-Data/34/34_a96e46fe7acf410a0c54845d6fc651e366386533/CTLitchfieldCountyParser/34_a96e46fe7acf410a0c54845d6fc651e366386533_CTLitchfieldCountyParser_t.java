 package net.anei.cadpage.parsers.CT;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Litchfield County, CT
 Contact: "ltglenn13@aol.com" <ltglenn13@aol.com>
 Contact: Duane Knox <duaneknox@gmail.com>
 Contact: Phillip Loveless <psloveless@gmail.com>
 Sender: textmsg@lcd911.com
 
 (LCD) MEDIC 4 RESPOND TO 30 PARK LANE EAST   NEW MILFORD, ,   UNCONSCIOUS, 31-D-3 :09:22
 FRM:textmsg@lcd911.com\nSUBJ:LCD\nMSG:MEDIC 4 RESPOND TO 80 CONNELLY RD   NEW MILFORD, , 80 M DIFF BREATH, 6-C-1 :12:02
 (LCD) MEDIC 4 RESPOND TO 70 CHAPEL HILL RD   SHERMAN, , 92Y M DIFF BREATH, 6-D-2 :12:36
 (LCD) MEDIC 4 RESPOND TO  WELLERS BRIDGE RD   HEMLOCK ROXBURY, ,   TRAFFIC ACCIDENT,  :10:44
 FRM:textmsg@lcd911.com\nSUBJ:LCD\nMSG:BRIDGEWATER FIRE RESPOND TO 209 NORTHROP ST BADER RESIDENCE  BRIDGEWATER, , CO ALARM ,:15:00
 (LCD) MEDIC 4 RESPOND TO 33 HARRY BROOK VILLAGE   NEW MILFORD, , 63 M HEART PROBLEM, 19-D-4 :18:31
 (LCD) MEDIC 4 RESPOND TO  WELLSVILLE AVE AREA OF CANTERBURY ARMS  NEW MILFORD, , 53 F DIABETIC, 13-C-1 :19:43
 (LCD) MEDIC 4 RESPOND TO 33 HARRY BROOK VILLAGE   NEW MILFORD, , 63 M HEART PROBLEM, 19-D-4 :18:31
 (LCD) MEDIC 4 RESPOND TO 1 GLEN AYRE DR   NEW MILFORD, , 60 F SICK PERSON, 26-C-1 :10:36
 (LCD) MEDIC 4 RESPOND TO 11 OLD PARK LANE RD NEW MILFORD MEDICAL GROUP  NEW MILFORD, ,   HEART PROBLEM, 19-C-4 :10:57
 (LCD) MEDIC 4 RESPOND TO 1 OLD STATE RD   NEW MILFORD, ,   DIFF BREATH, 6-C-1 :11:31
 (LCD) MEDIC 4 RESPOND TO 58 PICKETT DISTRICT RD KIMBERLY CLARK, NORTH GATE  NEW MILFORD, , 40 M SEIZURE, 12-D-2 :14:56
 (LCD) MEDIC 4 RESPOND TO 20 MAUWEEHOO HL PARKER RESIDENCE  SHERMAN, , 19Y F UNCONSCIOUS, 31-D-3 :16:30
 (LCD) MEDIC 4 RESPOND TO 104 DANBURY RD HOME DEPOT PARKING LOT  NEW MILFORD, , 79 F FALL, 17-D-3 :18:35
 (LCD) HARWINTON FIRE RESPOND TO 160 LITCHFIELD RD THOMASTON SAVINGS BANK HARWINTON, , ODOR OF GAS ,:13:50
 (LCD) HARWINTON FIRE RESPOND TO 285 BURLINGTON RD   HARWINTON, , TRAFFIC ACCIDENT ,:15:12
 (LCD) HARWINTON FIRE RESPOND TO 119 HARMONY HILL RD   HARWINTON, , TRANSFORMER FIRE ,:23:12
 (LCD) NEW HARTFORD FIRE RESPOND TO 1111 W HILL RD RESIDENCE NEW HARTFORD, , ACTIVATED FIRE ALARM ,:10:49
(LCD) MEDIC 4 RESPOND TO 46 MAPLE ST CANTERBURY UNIT   KENT, THE KENT, 72Y F INTERFACILITY, 33-C-1 :11:09

 */
 
 public class CTLitchfieldCountyParser extends SmartAddressParser {
   
  private static final Pattern MASTER = Pattern.compile("(.*) RESPOND TO (.*), (.*), (.*),(.*):\\d\\d:\\d\\d");
   private static final Pattern MAU_HILL = Pattern.compile("^(.*) MAUWEEHOO H(?:IL)?L (.*)$");
   
   public CTLitchfieldCountyParser() {
     super(CITY_LIST, "LITCHFIELD COUNTY", "CT");
     addExtendedDirections();
   }
   
   @Override
   public String getFilter() {
     return "textmsg@lcd911.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("LCD")) return false;
     
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strSource = match.group(1).trim();
     String sAddr = match.group(2).trim();
    data.strPlace = match.group(3).trim();
    data.strCall = match.group(4).trim();
    data.strCode = match.group(5).trim();
     
     parseAddress(StartType.START_ADDR, FLAG_PAD_FIELD | FLAG_ANCHOR_END, sAddr, data);
     if (data.strCity.equals("HEMLOCK ROXBURY")) data.strCity = "ROXBURY";
    String sPlace = getPadField();
     
     // There is a street called MAUWEEHOO HILL (or HL) that just confuses
     // the heck out of the smart parser so we will make some special checks for it
     if (data.strPlace.length() == 0) {
       match = MAU_HILL.matcher(data.strAddress);
       if (match.find()) {
         data.strAddress = match.group(1) + " MAUWEEHOO HILL";
        sPlace = match.group(2);
       }
     }
    if (data.strPlace.length() == 0) data.strPlace = sPlace;
    else data.strApt = sPlace;
     return true;
   }
   
   private static String[] CITY_LIST = new String[]{
     "BARKHAMSTED",
     "BETHLEHEM",
     "BRIDGEWATER",
     "CANAAN",
     "COLEBROOK",
     "CORNWALL",
     "GOSHEN",
     "HARWINTON",
     "NORTHWEST HARWINTON",
     "KENT",
     "SOUTH KENT",
     "LITCHFIELD",
     "BANTAM",
     "MORRIS",
     "NEW HARTFORD",
     "NEW MILFORD",
     "GAYLORDSVILLE",
     "NORFOLK",
     "NORTH CANAAN",
     "PLYMOUTH",
     "TERRYVILLE",
     "ROXBURY",
     "SALISBURY",
     "SHARON",
     "THOMASTON",
     "TORRINGTON",
     "WARREN",
     "WASHINGTON",
     "NEW PRESTON",
     "WATERTOWN",
     "OAKVILLE",
     "WINCHESTER",
     "WINSTED",
     "WOODBURY",
     
     "SHERMAN",
     "HEMLOCK ROXBURY"
   };
 }

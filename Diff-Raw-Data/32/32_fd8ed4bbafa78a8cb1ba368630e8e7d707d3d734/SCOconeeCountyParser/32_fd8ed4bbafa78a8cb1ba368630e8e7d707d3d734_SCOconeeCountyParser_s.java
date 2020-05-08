 package net.anei.cadpage.parsers.SC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Oconee County, SC
 Contact: William Rochester <william.rocheste
 Sender: 911@oconeelaw.com
 System: Aegis
 
 [911 Message]  S80 - CORONARY PROBLEM  1280 N  STHY 11 XStreet: SPRINGDALE DR / SCENIC HEIGHTS RD, FOWLER RD WEST UNION    2011-00000815  09/26/11 22:27  Narr:   S
 [911 Message]  S80 - CORONARY PROBLEM  206 S  TUGALOO ST XStreet: W MAULDIN ST / BOOKER DR WALHALLA    2011-00000809  09/25/11 06:49  Narr:   PATIENT IS HER FATHER
 [911 Message]  S46 - ALTERED MENTAL STATUS  308 N  LAUREL ST XStreet: ARDASHIR LN / WALHALLA GARDENS CIR WALHALLA    2011-00000777  09/16/11 12:24  Narr:   76 YOA

  */
 
 
 public class SCOconeeCountyParser extends SmartAddressParser {
   
  private static final Pattern MASTER = Pattern.compile("([A-Z0-9]+) - (.*?)    (\\d{4}-\\d{8})  \\d\\d/\\d\\d/\\d\\d \\d\\d:\\d\\d\\b(.*)");
   public SCOconeeCountyParser() {
     super(CITY_LIST, "OCONEE COUNTY", "SC");
   }
   
   @Override
   public String getFilter() {
     return "911@oconeelaw.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("911 Message")) return false;
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strUnit = match.group(1);
    String sAddr = match.group(2).trim();
    data.strCallId = match.group(3);
    String strTail = match.group(4).trim();
     
     sAddr = sAddr.replace(" XStreet:", " XS:");
    parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, sAddr, data);
     if (strTail.startsWith("Narr:")) strTail = strTail.substring(5).trim();
     data.strSupp = strTail;
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "SALEM",
     "SENECA",
     "WALHALLA",
     "WEST UNION",
     "WESTMINSTER",
     
     "FAIR PLAY",
     "LONG CREEK",
     "MOUNTAIN REST",
     "NEWRY",
     "OAKWAY",
     "RICHLAND",
     "TAMASSEE",
     "TOWNVILLE",
     "UTICA"
   };
 }

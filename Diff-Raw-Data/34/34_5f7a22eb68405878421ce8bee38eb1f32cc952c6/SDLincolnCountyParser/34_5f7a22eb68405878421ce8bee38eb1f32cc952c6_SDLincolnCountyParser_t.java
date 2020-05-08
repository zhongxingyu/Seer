 package net.anei.cadpage.parsers.SD;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Lincoln County, SD
  */
 public class SDLincolnCountyParser extends SmartAddressParser {
   
   private static final Pattern CALL_ID_PTN = Pattern.compile("^\\{?(\\d\\d-\\d+)\\b\\}?");
   private static final Pattern MASTER_PTN = Pattern.compile("\\{?(.*?)\\}? *(\n| - )(.*)");
   private static final Pattern STANDBY_PTN = Pattern.compile("^STANDBY +(?:AT +)", Pattern.CASE_INSENSITIVE);
  private static final Pattern APT_PTN = Pattern.compile("^# *([^,]+?) *,");
   private static final Pattern CITY_ST_PTN = Pattern.compile("^([A-Z ]+)\\b *, *([A-Z]{2})(?: +\\d{5})?", Pattern.CASE_INSENSITIVE);
   private static final Pattern INFO_JUNK_PTN = Pattern.compile(" *Please respond immediately\\.? *", Pattern.CASE_INSENSITIVE);
  
   public SDLincolnCountyParser() {
     super(CITY_LIST, "LINCOLN COUNTY", "SD");
   }
   
   @Override
   public String getFilter() {
     return "no-reply@ledsportal.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (subject.length() == 0) return false;
     
     // See if subject contains the address
     // with possible city and state qualifier
     Parser p = new Parser(subject);
     String addr = p.get(',');
     Result res = parseAddress(StartType.START_ADDR, FLAG_CHECK_STATUS | FLAG_ANCHOR_END, addr);
     if (res.getStatus() > 1) {
       res.getData(data);
       if (data.strCity.length() == 0) data.strCity = p.get(',');
       data.strState = p.get(',');
       data.strCall = body;
       return true;
     }
     
     boolean good = false;
     Matcher match = CALL_ID_PTN.matcher(subject);
     if (match.find()) {
       data.strCallId = match.group(1);
       subject = subject.substring(match.end()).trim();
       if (subject.startsWith("-")) subject = subject.substring(1).trim();
       if (subject.startsWith("{")) subject = subject.substring(1).trim();
       if (subject.endsWith("}")) subject = subject.substring(subject.length()-1).trim();
       if (subject.length() == 0) subject = "ALERT";
       good = true;
     }
     data.strCall = subject;
     
     String info = "";
     boolean hardBreak = false;
     match = MASTER_PTN.matcher(body);
     if (match.matches()) {
       body = match.group(1).trim();
       hardBreak = match.group(2).equals("\n");
       info = match.group(3).trim();
     }
 
     // Check for leading STANDBY qualifier
     match = STANDBY_PTN.matcher(body);
     if (match.find()) {
       data.strCall = "STANDBY: " + data.strCall;
       body = body.substring(match.end());
     }
     
     // See if there is an comma terminating the address
     int pt = body.indexOf(',');
     if (pt < 0) {
       
       // Use the smart address parser to try and find and address
       // if unsuccessful, return false.  If successful, mark as good
       if (hardBreak) {
         parseAddress(StartType.START_ADDR, FLAG_CHECK_STATUS | FLAG_ANCHOR_END, body, data);
       } else {
         parseAddress(StartType.START_ADDR, FLAG_CHECK_STATUS, body, data);
         info = append(getLeft(), " - ", info);
       }
       if (!good && getStatus() == 0 &&
            (!isPositiveId() || info.length() == 0)) return false;
     } 
     
     else {
       
       // Otherwise, we have found an address, so parse it as best we can
       parseAddress(body.substring(0,pt).trim(), data);
       body = body.substring(pt+1).trim();
       
      // Might be followed by an apartment
      match = APT_PTN.matcher(body);
      if (match.find()) {
        data.strApt = match.group(1);
        body = body.substring(match.end()).trim();
      }
      
       // See if what is left can be identified as a city, st combination
       match = CITY_ST_PTN.matcher(body);
       if (match.find()) {
         data.strCity = match.group(1);
         data.strState = getOptGroup(match.group(2));
         if (!CITY_SET.contains(data.strCity.toUpperCase())) return false;
         body = body.substring(match.end()).trim();
         if (info == null) info = body;
         else info = append(body, " / ", info);
       } else if (info.length() > 0) {
         data.strCity = body;
         if (!CITY_SET.contains(data.strCity.toUpperCase())) return false;
       } else {
         parseAddress(StartType.START_ADDR, FLAG_ONLY_CITY, body, data);
         if (data.strCity.length() == 0) return false;
         info = getLeft();
       }
     }
     
     info = INFO_JUNK_PTN.matcher(info).replaceAll(" ").trim();
     if (info.endsWith("/")) info = info.substring(0,info.length()-1).trim();
     data.strSupp = info;
     
     if (data.strCity.equalsIgnoreCase("CA")) data.strCity = "CANTON";
     if (data.strCity.equalsIgnoreCase("INWOOD")) data.strState = "IA";
     return true;
   }
   
   private static final String[] CITY_LIST = new String[] {
     "BERESFORD",
     "CA",
     "CANTON",
     "FAIRVIEW",
     "HARRISBURG",
     "HUDSON",
     "LENNOX",
     "MOE",
     "NORWAY CENTER",
     "SHINDLER",
     "SIOUX FALLS",
     "TEA",
     "WORTHING",
     
     "INWOOD"
   };
   
   private static final Set<String> CITY_SET  = new HashSet<String>(Arrays.asList(CITY_LIST));
 }

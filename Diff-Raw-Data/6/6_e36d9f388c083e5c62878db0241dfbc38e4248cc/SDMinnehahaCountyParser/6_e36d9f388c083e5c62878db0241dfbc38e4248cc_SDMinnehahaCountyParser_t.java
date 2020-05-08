 package net.anei.cadpage.parsers.SD;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Minnehaha county, SD
 Contact: brubin@med-starambulance.com
 Sender: mtooley@911metro.org
 
 930 Quad 2070 - VS 100 CLIFF AVE 2 VS Unconsc/Faint C3 2011-00000870
 930 Quad 460 - BR 904 SNOWBERRY AVE BR Conv Seizure C3 2011-00000374
 930 Quad 2060 - VS I 90 MM 410 VS Sick Person C1 2011-00000028
 930 Quad 2060 - VS 26472 484TH AVE VS Diabetic C2 2011-00000003
 930 Quad 2010 - SR 26525 482ND AVE BR Medical Emergency 2011-00000621
 930 Quad 460 - BR 3008 E ASPEN BLVD BR Falls C1 2011-00000051
 930 Quad 460 - BR 1625 RUSHMORE DR BR Family Dispute 2011-00000347 
 
Contact: Dusty Kiner <dusty.kiner@gmail.com>
(Dispatch Page) BR  Quad 460 - BR 1413 RUSHMORE DR 12 BR Falls C1 2011-00000111

 */
 
 public class SDMinnehahaCountyParser extends SmartAddressParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "BR", "BRANDON",
       "VS", "VALLEY SPRINGS",
       "SR", "SPLITROCK TWP"
   });
   
   private static final Pattern CAD_MSG_PTN = 
    Pattern.compile("(\\d{3}|[A-Z]{2}) +(Quad \\d{3,4}) - [A-Z]{2} +(.+?)(?: (C\\d))? (\\d{4}-\\d{8})");
  
   public SDMinnehahaCountyParser() {
     super(CITY_CODES, "MINNEHAHA COUNTY", "SD");
   }
   
   @Override
   public String getFilter() {
     return "mtooley@911metro.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = CAD_MSG_PTN.matcher(body);
     if (!match.matches()) return false;
     
     data.strSource = match.group(1);
     data.strMap = match.group(2);
     String sAddrFld = match.group(3);
     data.strCode = match.group(4);
     if (data.strCode == null) data.strCode = "";
     data.strCallId = match.group(5);
     
     parseAddress(StartType.START_ADDR, sAddrFld, data);
     data.strCall = getLeft();
     return true;
   }
 }

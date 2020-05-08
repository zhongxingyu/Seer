 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /* 
 Alexander County, NC
 Contact: Josh Reese <jrreese1@catamount.wcu.edu>
 Contact: Foster, Greg <gfoster@alexandercountync.gov>
 Sender: CAD@alexandercountync.gov
 
 (CAD:91 GREEN ACRES LN;FALL;31D03;ICARD RIDGE RD)
 CAD:2370 NC 90 HWY E;NC DEPT OF TRANSPORTATION;TRAFFIC ACCIDENT;ADAMS POND LN;WINTERHAVEN RD
 CAD:1522 BLACK OAK RIDGE RD;STANDARD STRUCTURE FIRE;JONES RD;CARSON CHAPEL RD
 CAD:400 LEE MATHESON RD;HEART PROBLEMS;19D04;BOSTON RD;TAYLORSVILLE MFG RD
 CAD:33 LEWITTES RD;PIEDMONT FIBERGLASS;LARGE ALARM;NC 90 HWY E;RUSSELL LN
 
 Contact: Josh Davis <paramedicdavis@gmail.com>
 |CAD:80 PRESLAR LN;BREATHING PROBLEMS;06D02;NC 127 HWY
 
 Contact: Chappy <kchapman@taylorsvillefire.org>
 CAD:350 SCHOOL DR;TAYLORSVILLE HOUSE;LARGE ALARM;LILEDOUN RD;E JAY DR
 
Contact: trent tedder <emt4448@yahoo.com>
CAD:65 MOONEY DR;TRAUMATIC INJURIES;30A02;US 64 90 HWY W

 */
 
 public class NCAlexanderCountyParser extends SmartAddressParser {
   
   private static final Pattern CODE_PTN = Pattern.compile("\\d{1,2}[A-Z]\\d{1,2}");
   
   public NCAlexanderCountyParser() {
     super("ALEXANDER COUNTY", "NC");
   }
   
   @Override
   public String getFilter() {
     return "CAD@alexandercountync.gov";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (body.length() == 0) body = subject;
     if (body.startsWith("|")) body = body.substring(1).trim();
     if (!body.startsWith("CAD:")) return false;
     body = body.substring(4).trim();
     
     String[] flds = body.split(";");
     parseAddress(flds[0].trim(), data);
     
     int ndx = 2;
     boolean code;
     while (true) {
      if (ndx >= flds.length) return false;
       String field = flds[ndx].trim();
       code = CODE_PTN.matcher(field).matches();
       if (code || field.startsWith("NC ") || field.startsWith("US ") || 
           checkAddress(field) > 0) break;
       if (++ndx > 3) return false;
     }
     
     data.strCall = flds[ndx-1].trim();
     if (ndx == 3) data.strPlace = flds[ndx-2].trim();
     
     if (code) data.strCode = flds[ndx++].trim();
     
     int stx = ndx;
     for (ndx = stx; ndx<flds.length && ndx < stx+2; ndx++) {
       data.strCross = append(data.strCross, " & ", flds[ndx].trim());
     }
     return true;
   }
 }

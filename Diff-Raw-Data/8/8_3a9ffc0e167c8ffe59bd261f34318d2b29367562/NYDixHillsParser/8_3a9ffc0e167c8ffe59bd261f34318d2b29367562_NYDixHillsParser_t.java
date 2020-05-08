 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 /*
 contact: "Craig Caviness" <ccaviness@dixhillsfd.org>
 sender: paging@dixhillsfd.xohost.com
 2010-001784 23:36 *** 16- Rescue *** 17 BRYCEWOOD DR SPIEGEL, LORI A Dix Hills HQ ARISTA DR 31-A-1 UNCONSCIOUS / FAINTING (NEAR) CEWOOD DR DIXHIL TYPE:
 2010-001779 10:34 *** 24/16- Mutual Aid *** 51 BALDWIN PATH  Dix Hills HQ
 2010-001777 15:54 *** 16- Rescue ***  DEER PARK AV DEER PARK AV & MARYLAND ST Dix Hills HQ MARYLAND ST
 2010-001778 15:54 *** 23- Misc Fire ***  DEER PARK AV DEER PARK AV & MARYLAND ST Dix Hills HQ MARYLAND ST
 2010-001774 11:56 *** 13- Structure Fire *** 29 WHITE BIRCH DR FULGONI, MARISSA Dix Hills HQ BALSAM DR 52-B-1G ALARMS TE BIRCH DR DIXHIL TYPE: ALARMS 
 2010-001766 05:54 *** 13- Structure Fire *** 29 WILDWOOD DR TRUEN, CRAIG Dix Hills HQ WHITNEY CT 60-C-1O GAS LEAKS / GAS ODOR (NATURAL / L.P.G.) DWOOD
 2010-001919 01:48 *** 16- Rescue *** 179 OAKFIELD AV BRUZZESE, ROCCO  SEAMAN NECK RD 6-D-3 RESPIRATORY KFIELD AV DIXHIL TYPE: RESPIRATORY LOC: 179  OAKFI
 2010-001923 11:44 *** 16- Rescue *** 337 DEER PARK AV SUNRISE ASSISTED LIVING RED MAPLE LA Alert
2010-001991 08:42 *** 23- Misc Fire ***  CARLLS STRAIGHT PATH CARLLS STRAIGHT PA & S SERVICE RD S SERVICE RD
*** Google doesn't understand PA, translate to CARLS STRAIGHT PATH & S SERVICE RD
 */
 
 public class NYDixHillsParser extends SmsMsgParser {
 
   @Override
   public boolean isPageMsg(String body) {
     return body.length() > 22 && body.substring(17, 22).equals(" *** ");
   }
 
   @Override
   protected void parse(String body, Data data) {
     Log.v("DecodeDixHillsPage: Message Body of:" + body);
     data.defState="NY";
     data.defCity="Dix Hills";
     body = body.trim();
     if (body.length() < 11) return;
     data.strCallId = body.substring(0,11);
     int pt = body.indexOf("***");
     if (pt < 0) return;
     int pta = body.indexOf("***",pt+3); 
     if (pta < 0) return;
     data.strCall = body.substring(pt+3, pta).trim();
     body = body.substring(pta+3).trim();
     int ptb = body.toUpperCase().indexOf(" DIX HILLS");
     if (ptb < 0) ptb = body.length();
     parseAddress(body.substring(0, ptb), data);
     int ptc = data.strAddress.indexOf(",");
     if (ptc >= 0) {
       int ptd = data.strAddress.lastIndexOf(" ",ptc);
       if (ptd >= 0) data.strAddress = data.strAddress.substring(0,ptd);
     }
   }
 }

 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchShieldwareParser;
 
 /*
 Halifax County, VA
 Contact: "devinsnead@hotmail.com" <devinsnead@hotmail.com>
 Contact: Dennis Blanks <dblanks09@gmail.com>
 Contact: George Hodges <grh850@embarqmail.com>
 Sender: halifaxeoc@co.halifax.va.us
 
 [from Central]  11-023176 FALLEN PATIENT\n2210 N TERRYS BRIDGE RD\nJAMES D HAGOOD HWY //SUNSET LN\nHALIFAX
 [from Central]  11-023175 FALLEN PATIENT\n1167 LEWIS FERRELL RD\nOAK LEVEL RD / LITTL/E CIR\nSOUTH BOSTON
 [from Central]  11-023166 EMERGENCY MEDICAL SERVICE\n111-22 ROSEHILL DR\nPOWELL RD / DEAD END/EN\nROSE HILL APARTMENTS  SOUTH BOSTON
 [from Central]  11-023086 UNRESPONSIVE PATIENT\n1110 ASBURY CHURCH RD\nCHATHAM RD / PAUL PR/Y TRL\nYOUNGERS HOME FOR ADULTS  HALIFAX
 [from Central]  11-023050 FALLEN PATIENT\n3471 OLD HALIFAX RD\nHIGH SCHOOL CIR /  C/ENTERVILLE PARK RD\nWAL-MART  SOUTH BOSTON
 [from Central]  11-023040 EMERGENCY MEDICAL SERVICE\n2082 BURTON RD\nWAGSTAFF LN / CLAYS/MILL RD\nSCOTTSBURG
 [from Central]  11-023038 FALLEN PATIENT\n103 WHITE OAK DR\nSYCAMORE RD / SYCAMO/RE RD\nSOUTH BOSTON
 [from Central]  11-017088 SHORTNESS OF BREATH, ETC.\n1110 HENRYS TRL\nAMBERSTONE DR / DEAD/END\nNATHALIE
 (from Central) 11-022783 ACCIDENT INVOLVING PEDESTRIAN\n104-20 LOVE SHOP PARK RD\nOLD HALIFAX RD / DEA/D END\nLOVE SHOP TRAILER PARK  SOUTH BOSTON
 
 Contact: Randy Dockery <k3rn3lkill3r@gmail.com>
 Sender: halifaxeoc@co.halifax.va.us
 (from Central) 12-024703 EMERGENCY MEDICAL SERVICE\r\n\r\nReported: 06/22/2012 03:50:22\r\n\r\n510 GREENWAY DR\r\n\r\nWILBORN AVE / NORWOO/D AVE\r\n\r\nSOUTH BOSTON
 (from Central) 12-024641 RESIDENTIAL\r\n\r\nReported: 06/21/2012 18:23:30\r\n\r\n2123 HORSESHOE TRL\r\n\r\nHUELL MATTHEWS HWY //DEAD END\r\n\r\nALTON
 (from Central) 12-024654 GENERAL COMPLAINT - SICK\r\n\r\nReported: 06/21/2012 20:13:43\r\n\r\n1419 WASHINGTON AVE\r\n\r\nEDMUNDS ST / WEBSTER/ST\r\n\r\nSOUTH BOSTON
 
 Contact: ray mason <raycls8@gmail.com>
 Sender: halifaxeoc@co.halifax.va.us
 S:from Central M:12-027904 GENERAL COMPLAINT - SICK\n\nReported: 07/12/2012 06:45:04\n\n3180 LP BAILEY MEM HWY\n\nCARTER S LN /  DUDLEY/RD\n\nSEKPEH HOME FOR ADULTS  HALIFAX\n\n\n\n
 
  */
 public class VAHalifaxCountyParser extends DispatchShieldwareParser {
   
   public VAHalifaxCountyParser() {
     super("HALIFAX COUNTY", "VA", FLG_NO_UNIT);
   }
   
   @Override
   public String getFilter() {
     return "halifaxeoc@co.halifax.va.us";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("from Central")) return false;
    return super.parseMsg(body, data);
   }
 }

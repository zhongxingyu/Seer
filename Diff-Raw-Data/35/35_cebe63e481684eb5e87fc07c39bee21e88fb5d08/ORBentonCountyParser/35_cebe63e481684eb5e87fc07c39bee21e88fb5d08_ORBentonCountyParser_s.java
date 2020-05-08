 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
 import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*    
 Sample Benton County Page
 Contact: kencx@peak.org
 Sender: alerts@corvallis.ealertgov.com
 (Corvallis Alert) INC: CODE 1 MEDICAL\nADD:1740 MAIN ST\nAPT:\nCITY:PHILOMATH\nX:N 17TH ST * N 18TH ST\nMAP:540-365\nCFS:0907010-119\nDIS:PHILOMATH FIRE
 (Corvallis Alert) INC:COMM FIRE ALARM\nADD:421 S 19TH ST\nAPT:\nCITY:PHILOMATH\nX:ASH ST * CEDAR ST\nMAP:540-360\nCFS:100410-188\nDIS:PHILOMATH FIRE\nDIS:PHI
 */
 
 public class ORBentonCountyParser extends SmsMsgParser {
 
   @Override
   public boolean isPageMsg(String body) {
     return body.startsWith("(Corvallis Alert)");
   }
 
   @Override
   protected void parse(String body, Data data) {
     
     Log.v("DecodeBentonCo: Message Body of:" + body);
     data.defState="OR";
     data.defCity="BENTON COUNTY";
     Properties props = parseMessage(body, "\n");
     
     data.strCall=props.getProperty("INC", "");
     data.strAddress=props.getProperty("ADD", "");
     data.strCity=props.getProperty("CITY", "");
     data.strApt =props.getProperty("APT", "");
     data.strCross=props.getProperty("X", "");
     data.strMap=props.getProperty("MAP", "");
     data.strCallId = props.getProperty("CFS", ""); 
   }
 }

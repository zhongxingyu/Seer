 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Wight County, VA
 Contact: "G.B. Wills III" <gbwillsiii@gmail.com>
 Sender: CAD@isleofwightUS.net
 
 11365 CENTRAL HILL RD;S CAROLINA AVE;COURTHOUSE HWY;FIRE ALARM;smoke detector zone 16 res last name babb 757-353-0490 [06/20/11 19:01:18 MPMARSH] 
 YELLOW HAMMER RD/FIRE TOWER RD;1050;[LAW] {37} ONE FINGER AND JAW/ SEC CONCUSION [06/22/11 04:02:28 SHILL] {37} HAVE INJURIES [06/22/11 04:02:04 SHILL] VSP AD: 
 23352 COURTHOUSE HWY;CONSULATE HEALTH CARE WINDSOR;KENSINGTON CT;JOYNER TOWN LN;FIRE ALARM;[LAW] WILL TRY TO CONTACT KEYHOLDER [06/22/11 12:20:01 MDUNN] zone
 77 CASTLE ST;LIBERTY AVE;BELLMONT ST;FIRE OTHER NOT LISTED;microwave hot and not in use [06/23/11 09:59:22 JTEMPLETON].   
 W WINDSOR BLVD/N PRINCE BLVD;ACCIDENT WITH INJURIES;supreme prking lot. 2 veh. chryslter pt cruiser vs dodge ram piick up [06/23/11 13:13:34 JTEMPLETON]
 
  */
 public class VAIsleOfWightCountyParser extends DispatchOSSIParser {
   
   public VAIsleOfWightCountyParser() {
     super("WIGHT COUNTY", "VA",
            "ADDR ( X X? CALL | PLACE X X? CALL | CALL ) INFO+");
   }
   
   @Override
   public String getFilter() {
     return "CAD@isleofwightUS.net";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    return super.parseMsg("CAD:" + body, data);
   }
 }

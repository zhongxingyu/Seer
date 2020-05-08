 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Cy Creek Comm Center
  */
 public class TXCyFairParser extends MsgParser {
   
   public TXCyFairParser() {
     super("HARRIS COUNTY", "TX");
   }
   
   @Override
   public String getFilter() {
     return "CAD@CYFAIRVFD.ORG";
   }
 
   @Override
   public String getLocName() {
     return "Cypress-Harris, TX";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (body.length() < 53) return false;
     if (body.charAt(10) != '/' || body.charAt(51) != '/') return false;
     data.strUnit = substring(body, 0, 10);
     data.strCode = substring(body, 11, 21);
     data.strCall = substring(body, 21, 51);
     parseAddress(substring(body, 52, 92), data);
     data.strCross = substring(body, 92, 172);
     data.strMap = substring(body, 204, 209);
     String unit2 = substring(body, 225);
     if (unit2.length() > 0) data.strUnit = unit2;
     return true;
   }
 }

 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Williamson County, TX
 Contact: Don Carlson <dcarlson@huttovfd.org>
 Sender: cad@wilco.org
 
 CAD:REQUEST EMS;501 EXCHANGE BLVD;HU;HUE1
 CAD:FYI: ;SA CITIZEN ASSIST;200-BLK ED SCHMIDT BLVD;HU
 CAD:REQUEST FIRE;201 ED SCHMIDT BLVD;HU;HUE1
 CAD:FYI: ;SICK PERSON SPEC DIAGNOSIS;206 ARROWHEAD TRL;CP
 CAD:REQUEST FIRE;200-BLK BROOKE ST;HU;HUE1
 CAD:FYI: ;SA CITIZEN ASSIST;100 CR 165;HU
 CAD:FYI: ;GA OUTSIDE FIRE;5634 CHANDLER RD/SH 130 TOLL;HU
 CAD:Update: ;TRAFFIC TRANSPORTATION ACC;2058 LIMMER LOOP/CR 108;HU;HUE1
 CAD:Update: ;GA OUTSIDE FIRE;5634 CHANDLER RD/SH 130 TOLL;HU;HUBT1,HUE1
 
 */
 
 
 public class TXWilliamsonCountyParser extends DispatchOSSIParser {
 
   public TXWilliamsonCountyParser() {
     super("WILLIAMSON COUNTY", "TX",
           "HEAD? CALL ADDR UNIT+");
   }
   
   @Override
   public String getFilter() {
     return "cad@wilco.org";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     body = body.replaceAll("SH 130 TOLL", "TX 130");
     return super.parseMsg(body, data);
   }
   
   private class HeadField extends SkipField {
     public HeadField() {
       setPattern(Pattern.compile("FYI:|Update:"));
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("HEAD")) return new HeadField();
     return super.getField(name);
   }
   
 }

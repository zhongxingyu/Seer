 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Northampton County, PA
 Contact: Matt Winter <mattgwinter@gmail.com>
 Sender: @notifync.orgaddress
 DispatchBParser subclass
 
 [e49]ALS >ADVANCED LIFE SUPPORT CALL 602 E 21ST ST Apt: 119 Bldg NORTHAMPTON DIANE ECK Map: Grids:0,0 Cad: 2011-0000086714
 [e49]BLS >BASIC LIFE SUPPORT CALL 1323 NEWPORT AVE Apt: REAR Bldg NORTHAMPTON TONY FERRERA Map: Grids:0,0 Cad: 2011-00000
 [e49]MVAI >MVA WITH INJURIES 248 AT PENNSVILLE LIGHT LEHIGH TWP NATALIE BRODIANO Cad: 2011-0000086361
 [e49]ALS >ADVANCED LIFE SUPPORT CALL 612 E 10TH ST NORTHAMPTON ROBERTS PAMELA Map: Grids:0,0 Cad: 2011-0000086262
 [e49]BLS >BASIC LIFE SUPPORT CALL 1001 WASHINGTON AVE Apt: 105 Bldg NORTHAMPTON MEGAN MOREY Map: Grids:0,0 Cad: 2011-000008
 [e49]ALS >ADVANCED LIFE SUPPORT CALL 5962 KEYSTONE DR EAST ALLEN CHRISTINA WIGMER Map: Grids:0,0 Cad: 2011-0000086103
 [e49]BLS >BASIC LIFE SUPPORT CALL 1323 NEWPORT AVE NORTHAMPTON TONY CABRERA Map: Grids:0,0 Cad: 2011-0000086010
 
 Contact: "richierrs@aol.com" <richierrs@aol.com>
 Subject:#6550\n[f14]MVAU >MVA WITH UNKNOW INJUIRIES WILLOW PARK RD BETHLEHEM TWP P1736 Map: Grids:0,0 Cad: 2011-0000131105
 
 Contact: Edmund Lamontagne <edmundl79@gmail.com>
 *3750: *alert_6JP4@notifync.org /  / [f25]ODOR >ODOR / OTHER THAN SMOKE ARNDT RD FORKS Map: Grids:0,0 Cad: 2011-0000171220 <20110000171220>
 
 */
 
 public class PANorthamptonCountyParser extends DispatchBParser {
   
   private static final String[] CITY_LIST = new String[]{
     "BETHLEHEM",
     "EASTON",
 
     "BANGOR",
     "BATH",
     "CHAPMAN",
     "EAST ALLEN",
     "EAST BANGOR",
     "FREEMANSBURG",
     "GLENDON",
     "HELLERTOWN",
     "NAZARETH",
     "NORTH CATASAUQUA",
     "NORTHAMPTON",
     "PEN ARGYL",
     "PORTLAND",
     "ROSETO",
     "STOCKERTOWN",
     "TATAMY",
     "WALNUTPORT",
     "WEST EASTON",
     "WILSON",
     "WIND GAP",
 
     "ALLEN TWP",
     "BETHLEHEM TWP",
     "BUSHKILL TWP",
     "EAST ALLEN TWP",
     "FORKS TWP",
     "HANOVER TWP",
     "LEHIGH TWP",
     "LOWER MOUNT BETHEL TWP",
     "LOWER NAZARETH TWP",
     "LOWER SAUCON TWP",
     "MOORE TWP",
     "PALMER TWP",
     "PLAINFIELD TWP",
     "UPPER MOUNT BETHEL TWP",
     "UPPER NAZARETH TWP",
     "WASHINGTON TWP",
     "WILLIAMS TWP"
   };
   
   private static final Pattern MARKER = Pattern.compile(">.* Cad: ");
   
   @Override
   public String getFilter() {
    return "@notifync.orgaddress";
   }
  
   @Override
   protected boolean isPageMsg(String body) {
     return MARKER.matcher(body).find();
   }
 
   public PANorthamptonCountyParser() {
     super(CITY_LIST, "NORTHAMPTON COUNTY", "PA");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     data.strUnit = new Parser(subject).getLast('|');
     body = body.replace(" CALL ", " AT ");
     if (!super.parseMsg(body, data)) return false;
     return true;
   }
 
   @Override
   protected boolean parseAddrField(String field, Data data) {
     // TODO Auto-generated method stub
     return super.parseAddrField(field, data);
   }
   
   
 }

 package net.anei.cadpage.parsers.CT;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Roxbury, CT
 Contact: Mike Josefson <mikejosefson@gmail.com>
 Sender: globalpaging@nowestps.org
 
 FRM:globalpaging@nowestps.org\nSUBJ:From Northwest\nMSG:FLAG SWAMP RD & SOUTH ST  ROXBURY MVA EMS ROX AMB RO EMSX RO ENG12 ROX RES9 ROX FD1 Primary Incident: 040734\n\n(End)
 FRM:globalpaging@nowestps.org\nSUBJ:From Northwest\nMSG:39 TRANSYLVANIA RD  ROXBURY CHOKING ABNORM BREATH (PARTIAL OBSTRUCT) ROX AMB RO EMSX Primary Incident: 040410\n\n(End)
 FRM:globalpaging@nowestps.org\nSUBJ:From Northwest\nMSG:102 PAINTER HILL RD  ROXBURY FAINTING/SYNCOPE ROX AMB RO EMSX Primary Incident: 040378\n\n(End)
 FRM:globalpaging@nowestps.org\nSUBJ:From Northwest\nMSG:314 PAINTER HILL RD  ROXBURY FIRE ALARM RO ENG12 RO ENG10 RO TKR11 ROX RES9 ROX FD1 Primary Incident: 00120\n\n(End)
 FRM:globalpaging@nowestps.org\nSUBJ:From Northwest\nMSG:71 SOUTH ST  ROXBURY FIRE ALARM RO ENG12 RO ENG10 RO TKR11 ROX RES9 ROX FD1 Primary Incident: 00119\n\n\n(End)
 FRM:globalpaging@nowestps.org\nSUBJ:From Northwest\nMSG:BOTSFORD HILL RD & GOLDEN HARVEST RD  ROXBURY WIRES DOWN/FIRE ROX RES9 ROX FD1 Primary Incident: 00109
 
 Seymour, CT
 Contact: support@active911.com
 Sender: globalpaging@nowestps.org
 (From Northwest) 21 LANTERN DR  SEYMOUR PSYCHIATRIC PROBLEM TANGO 5 SEY 1ST Primary Incident: 012658 21 LANTERN DR
 (From Northwest) 15 SHARI DR, Apt. B  SEYMOUR DIFFICULTY BREATHING, SOB TANGO 5 SEY 1ST Primary Incident: 012581 15 SHARI DR, Apt. B
 (From Northwest) 1 ELMWOOD DR  SEYMOUR CONVULSION/SEIZURE CONT/MULTI SEIZURES TANGO 5 SEY 1ST Primary Incident: 012603 1 ELMWOOD DR
 (From Northwest) DE*FOREST ST & MAIN ST  SEYMOUR MVA PEDESTRIAN TANGO 6 SEY 1ST Primary Incident: 012610 DE*FOREST ST & MAIN ST
 
 */
 
 public class CTRoxburyParser extends SmartAddressParser {
   
  private static final Pattern UNIT_PTN = Pattern.compile("\\b(?:ROX?|TANGO)\\b");
 
   public CTRoxburyParser() {
     super(CITY_LIST, "ROXBURY", "CT");
   }
   
   @Override
   public String getFilter() {
     return "globalpaging@nowestps.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("From Northwest")) return false;
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     if (body.startsWith("DE*")) body = body.substring(3).trim();
     Parser p = new Parser(body);
     String sAddr = p.get("Primary Incident:");
     data.strCallId = p.get(' ');
     if (data.strCallId.length() == 0) return false;
     
    sAddr = sAddr.replace(',', ' ').trim();
     parseAddress(StartType.START_ADDR, sAddr, data);
     body = getLeft();
     Matcher match = UNIT_PTN.matcher(body);
     if (match.find()) {
       data.strUnit = body.substring(match.start());
      body = body.substring(0,match.start()).trim();
     }
     data.strCall = body;
     return true;
   }
   
   private static String[] CITY_LIST = new String[]{
     "ROXBURY"  
   };
 }

 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Pitt County, NC
 Contact: noah smith <firefighter2114@gmail.com>
 Sender: donotreply@pittcountync.gov
 
 S:911 Fire Call M:F24|Rcvd:05/31/2012 03:34|Rsn:FIRE ALARM ACT|Note:SAME|Adr:1550  QUALITY LN
 S:911 Fire Call M: F14 | Rcvd:05/27/2012 01:16 | Rsn: MVC PI | Note:MARTIN LUTHER KING JR DR | WHICHARD RD | Adr:0 
 S:911 Fire Call M: F14 | Rcvd:05/25/2012 17:12 | Rsn:47 YOM STROKE | Note:618 DANIELLE DR | Adr:0
 S:911 Fire Call M: F14 | Rcvd:05/25/2012 01:41 | Rsn: POSS ELECTRICAL FIRE |Note:3157 OLD RIVER RD | Adr:0
 S:911 Fire Call M: F14 | Rcvd:05/23/2012 19:55 | Rsn: MVC PI | Note:OLDR RIVER RD | BROWN PLACE DR @ THE BRIDGE | Adr:0
 S:911 Fire Call M: F24 | Rcvd:05/22/2012 17:16 | Rsn: FIRE ALARM | Note:SAME | Adr:2185 OAKLEY RD
 
 Contact: Active911
 Agency name: City of Greenville Fire Location: Greenville, NC 
 sender: donotreply@pittcountync.gov
 
 (911 Fire Call) F01|Rcvd:06/30/2012 09:02|Rsn:FIRE ALARM|Note:SLAY HALL|Adr:701 E 5TH ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:06/30/2012 15:42|Rsn:FIRE ALARM|Note:SAME|Adr:408  RAINTREE DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:06/30/2012 15:51|Rsn:FIRE ALARM|Note:SAME|Adr:2459  EMERALD PL\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:06/30/2012 21:06|Rsn:FIRE ALARM ACTIVATION|Note:FICKLIN STADIUM|Adr:701 E 5TH ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:06/30/2012 22:21|Rsn:FIRE ALARM ACTIVATION|Note:517 MOYE BLVD | MOYE MEDICAL 2|Adr:701 E 5TH ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 02:42|Rsn:MALE CHEST PAIN|Note:HOWELL AND PITT|Adr:0  -\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 05:36|Rsn:FIRE ALARM ACTIVATION|Note:|Adr:1055  WATERFORD COMMONS DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 06:50|Rsn:FIRE ALARM|Note:800-230-6975/OPERATOR TARA|Adr:1055  WATERFORD COMMONS DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 07:21|Rsn:FIRE ALARM|Note:SAME|Adr:2095  STANTONSBURG RD\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 16:09|Rsn:FENCE FIRE|Note:N HARDING ST BEHIND 109 N LIBRARY|Adr:0  -\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 16:20|Rsn:FIRE ALARM|Note:CLEMMENT HALL|Adr:701 E 5TH ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 19:21|Rsn:FEMALE BREATHING PROBLEM|Note:|Adr:1805  KENNEDY CIR  A\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 20:38|Rsn:TREE FALLEN ON HOUSE|Note:1605 MURFIELD DR|Adr:121  WILKSHIRE DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 20:49|Rsn:TREE FALLEN ON HOUSE|Note:1605 MURFIELD DR|Adr:121  WILKSHIRE DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 20:50|Rsn:FEMALE STROKE|Note:902 BANCROFT AV|Adr:0  -\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/01/2012 21:01|Rsn:TREE FALLEN ON HOUSE|Note:1605 MURFIELD DR|Adr:121  WILKSHIRE DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/02/2012 07:34|Rsn:MVC PI|Note:LINE AV | FARMVILLE BL|Adr:100 W 3RD ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/02/2012 11:25|Rsn:FEMALE SEIZURES|Note:SAME|Adr:2114  MYRTLE AVE\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/02/2012 11:49|Rsn:MVC PI|Note:E 5TH ST | S HARDING ST|Adr:500 S GREENE ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/02/2012 12:29|Rsn:FIRE ALARM ACTIVATION|Note:SAME|Adr:214 E FIRE TOWER RD\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/02/2012 21:25|Rsn:FIRE ALARM|Note:108 CHANDLER DR G|Adr:1717 W 5TH ST\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 (911 Fire Call) F01|Rcvd:07/03/2012 09:24|Rsn:FIRE ALARM ACTIVATION|Note:GENERAL // NO KEYHOLDER LIST|Adr:2313 S MEMORIAL DR\n\n________________________________\n\nThis e-mail is for the intended recipient only.\nIf you have received it by mistake please let us know by reply and then delete it from your system; access, disclosure, copying, distribution or reliance on any of it by anyone else is prohibited.\nIf you as intended recipient have received this e-mail incorrectly, please notify the sender (via e-mail) immediately.
 
NCPittCountyParser
 */
 
 public class NCPittCountyParser extends FieldProgramParser {
   
   public NCPittCountyParser() {
     super("PITT COUNTY", "NC",
            "SRC! Rcvd:DATETIME! Rsn:CALL! Note:INFO X Adr:ADDR!");
   }
   
   @Override
   public String getFilter() {
     return "donotreply@pittcountync.gov";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("911 Fire Call")) return false;
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     if (!parseFields(body.split("\\|"), data)) return false;
     if (data.strAddress.length() == 0 || data.strAddress.equals("0") || data.strAddress.startsWith("0 ")) {
       data.strAddress = "";
       parseAddress(StartType.START_ADDR, data.strSupp, data);
       data.strSupp = getLeft();
     } else if (data.strSupp.equals("SAME")) {
       data.strSupp = "";
     }
     return true;
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf('@');
       if (pt >= 0) {
         data.strCross = field.substring(0,pt).trim();
         data.strPlace = field.substring(pt+1).trim();
       } else {
         if (checkAddress(field) > 0) {
           data.strCross = field;
         } else {
           data.strPlace = field;
         }
       }
     }
     
     @Override
     public String getFieldNames() {
       return "X PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("DATETIME")) return new DateTimeField("\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d", true);
     if (name.equals("SAME")) return new SkipField("SAME", true);
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
 }

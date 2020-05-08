 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Prince Georges County, MD
 Contact: Randy Kuenzli <chief9.rk@gmail.com>
 Contact: Dewey Thomas <dewey@codemessaging.net>
 
 [PG09] 9: OUTSIDE GAS LEAK  At: 04/12 10:12  Dispatch: E809  All: E809  To: BLADENSBURG ELEMENTARY
 [PG09] 9: ROUTINE TRANSPORT  At: 04/13 07:23  Backup: A809  All: A809  To: PGGH - HOSPITAL  
 [PG09] 13: MEDIC LOCAL\nAt: 04/12 20:34\nDispatch: A809\nAll: E807 MD830 A809\nTo: 5435 KENILWORTH AVE (KENNEDY ST and JEFFERSON ST)
 [PG09] 13: PIA\nAt: 04/12 19:37\nDispatch: A809\nAll: A809 E807\nTo: EB RIVERDALE RD/EB NB KENILWORTH AVE OFRP EB EAST WEST HWY
 [PG09] 8: INJURED/SICK\nAt: 04/12 18:46\nDispatch: A809\nAll: A809\nTo: 1 YOST PL (ALPACA PL and EAST CAPITOL ST)
 [PG09] 9: ASSAULT\nAt: 04/12 12:04\nDispatch: A809\nAll: A809\nTo: 55TH AVE/EB LANDOVER RD
 [PG09] 9: OUTSIDE GAS LEAK\nAt: 04/12 10:12\nDispatch: E809\nAll: E809\nTo: BLADENSBURG ELEMENTARY
 [PG09] 9: MEDIC LOCAL\nAt: 04/12 10:02\nDispatch: A809\nAll: A809 MD830\nTo: PORT TOWNS ELEMENTARY
 [PG09] 9: OUTSIDE GAS LEAK\nAt: 04/12 09:20\nDispatch: E809\nAll: E809\nTo: BLADENSBURG ELEMENTARY
 [PG09] 9: INJURED/SICK\nAt: 04/12 04:42\nDispatch: A809\nAll: A809\nTo: 6001 LOGAN WAY #C10 (EMERSON ST and TOWNSEND WAY)
 [PG09] 28: BUILDING FIRE\nAt: 04/12 00:12\nDispatch: TK809 BO882\nAll: E828 PE830 E833 E833B TW833 TK809 SQ806 BO882\nTo: 8500 ANNAPOLIS RD
 [PG09] 13: STREET ALARM  At: 04/13 12:37  Dispatch: TK809 BO884  All: E807 TK809 E807B TK801 BO884  To: 5906 62ND AVE (GREENVALE PKY and 64TH AVE)  
(CAD)[PG09] 12: BUILDING FIRE  At: 07/17 20:50  Dispatch: E809 BO884  All: E812 TK812 E811 TK814 E807B TK801 E809 BO884  To: 5211 PAINT BRANCH PKY (52ND AVE and RIVERSIDE AVE)  
 
 */
 
 public class MDPrinceGeorgesCountyDParser extends FieldProgramParser {
   
   private static Pattern MARKER = Pattern.compile("^\\d+: ");
   
   public MDPrinceGeorgesCountyDParser() {
     super("PRINCE GEORGES COUNTY", "MD",
            "CALL! At:DATETIME! Dispatch:SKIP? Backup:SKIP? All:UNIT! To:ADDR!");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (subject.length() == 0) return false;
    data.strSource = new Parser(subject).getLast('|');;
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).replace('\n', ' ').trim();
     return super.parseMsg(body, data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(")")) {
         int pt = field.indexOf('(');
         if (pt >= 0) {
           data.strCross = field.substring(pt+1, field.length()-1).trim();
           field = field.substring(0,pt).trim();
         }
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " X";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }

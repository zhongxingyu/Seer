 package net.anei.cadpage.parsers.WV;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Cabell County. WV
 Contact: Everett Cooper <firefighterorr6@gmail.com>
 Sender: dispatch@ccerc911.org
 
 1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22225) CCERC911\nMSG:113030126/10-30-11/06:42:08/Other Fire/8160 FRAZIERS LN/LESAGE/LORENA LN/KATHI\n(Con't) 2 of 2\nLN/50YF/BLEEDING FROM A SURGICAL WOUND IN STOMACH//(End)
 1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22343) CCERC911\nMSG:113030199/10-30-11/14:16:29/Structure Fire/6419 RT 60/BARBOURSVILLE/GEORGES DRY\n(Con't) 2 of 2\nCLEANERS/FARMDALE RD/MAIN ST/201 231 ENRT/237/(End)
 1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22412) CCERC911\nMSG:113030260/10-30-11/16:53:24/Abdominal Pain/6430 OHIO RIVER RD APT1/LESAGE/TAN\n(Con't) 2 of 2\nZONE/DOUTHAT LN/NINE MILE RD/22YR FEMALE/27WKS PREGNANT/(End)
 1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22719) CCERC911\nMSG:113040332/10-31-11/20:53:50/MVA Inj./4805 HILLVIEW DR/HUNTINGTON/OHIO RIVER\n(Con't) 2 of 2\nRD/END/ONE VEH OVER THE HILL/ ADV ABOUT 50 YARD OVER A HILL/(End)
 1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22809) CCERC911\nMSG:113050042/11-01-11/05:35:54/MVA Inj./5315 OHIO RIVER RD BLK/HUNTINGTON/THREE MILE\n(Con't) 2 of 2\nCREEK RD/MOBILE CITY RD/TRACTOR TRAILOR / TRUCK IS ON THE TRACKS/(End)
 
 Contact: JR VanOoteghem <jr@ccerc911.org>
 Sender: 7771
 " " (29109) CCERC911\r\n: 121420298/05-21-12/18:02:31/ARMED ROBBERY/1670 11TH AVE/HUNTINGTON/CYPRESS ST/17TH ST/MALE WAS HELD AT GUN POINT/ TXT STOP to opt-out
 " " (29154) CCERC911\r\n: 121420322/05-21-12/19:09:39/Jumper/9999 6TH ST/HUNTINGTON/6TH STREET BRIDGE/MALE ION THE BRIDGE THREATS TO JUMP/ TXT STOP to opt-out
 " " (29096) CCERC911\r\n: 121420255/05-21-12/16:31:23/Fire Alarm/7850 OHIO RIVER RD/LESAGE/RUSTOLEUM/CLUTTS HILL RD/OLD RT 2/GENERAL/ TXT STOP to opt-out
 " " (29283) CCERC911\r\n: 121420376/05-21-12/20:57:24/PURSUIT/900 8TH ST INTERSECTN/HUNTINGTON/9TH AVE/SB ON 8TH ST// TXT STOP to opt-out
 " " (29458) CCERC911\r\n: 121430056/05-22-12/03:56:08/Shooting/200 8TH AVE INTERSECTN/HUNTINGTON/2ND ST/COMP ADV MALE SUBJ WAS SHOOT IN THE LEG VICT IS ON THE/ TXT STOP to opt-out
 " " (29217) CCERC911\r\n: 121420332/05-21-12/19:26:29/Structure Fire/722 4TH ST/HUNTINGTON/RR TRACKS/8TH AVE/SEND E1 PER HFD43/ TXT STOP to opt-out
 " " (29364) CCERC911\r\n: 121430008/05-22-12/00:20:02/Structure Fire/1801 12TH AVE/HUNTINGTON/18TH ST/19TH ST/VACANT HOUSE // TXT STOP to opt-out
 " " (29169) CCERC911\r\n: 121420325/05-21-12/19:11:12/Smoke Investigation/7800 OHIO RIVER RD INTERSECTN/LESAGE/OLD RT 2/JUST BEFORE THIS/SEVERAL MEN STANDING BY THE ROADWAY AND/ TXT STOP to opt-out
 
 */
 public class WVCabellCountyParser extends FieldProgramParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("\\(\\d+\\) CCERC911");
   private static final Pattern MARKER_PTN = Pattern.compile("^CCERC911\r?\n:");
   
   public WVCabellCountyParser() {
     super("CABELL COUNTY", "WV",
            "ID DATE TIME CALL ADDR CITY! PLACE? X X? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@ccerc911.org";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     do {
       if (SUBJECT_PTN.matcher(subject).matches()) break;
       Matcher match = MARKER_PTN.matcher(body);
       if (match.find()) {
         body = body.substring(match.end()).trim();
         break;
       }
       return false;
     } while (false);
     
     return parseFields(body.split("/"), data);
   }
   
 
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern("\\d{9}", true);
     }
   }
   
   private class MyDateField extends DateField {
     public MyDateField() {
       setPattern("\\d\\d-\\d\\d-\\d\\d", true);
     }
   }
   
   private class MyTimeField extends TimeField {
     public MyTimeField() {
       setPattern("\\d\\d:\\d\\d:\\d\\d", true);
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(" INTERSECTN")) field = field.substring(0,field.length()-11).trim();
       super.parse(field, data);
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public boolean checkParse(String field, Data data) {
      if (field.equals("END")) {
         parse(field, data);
         return true;
       }
       return super.checkParse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("DATE")) return new MyDateField();
     if (name.equals("TIME")) return new MyTimeField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
   
   
 }

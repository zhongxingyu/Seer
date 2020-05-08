 package net.anei.cadpage.parsers.OH;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Medina County, OH
 
 Contact: Shane Jenkins <sjenkins@lafayettetwpfire.com>
 Sender: rc.223@c-msg.net
 (lCAD) [!] EMS//6387 CARSTEN RD MEDINA BETWEEN CHATHAM RD / COON CLUB RD//FEMALE FALL VIC TIM NEEDS LIFT ASSIST NO INJURY RECENTLY OUT OF THE HOSPITAL
 
 Contact: "jhmcfd@hotmail.com" <jhmcfd@hotmail.com>
 Sender: rc.531@c-msg.net
 [gCAD]  [!] - NATURE: 2 //WB MEDINA RD / MEDINA L INE RD MEDINA//CALLBK=041.135838-081 .678843 1 VEH ON THE NORTH SIDE OF THE ROAD NO ONE AROUND IT
 [gCAD]  [!] MVA WITH INJURIES//2389 MEDINA RD MEDINA BETWEEN BEACH RD / BONETA RD//CALLBK=041.136428-081.773643 WB CAR ROLL OVER CAR IS ON ITS TOP CALLER ADV SHE DONT SEE ANYONE IN THE VEH
 [gCAD]  [!] EMS//200 GRANGER RD MEDINA BETWEEN N MEDINA LINE RD / DRUERIE LN//APT 7 FEMAL E FALLEN OUT OF BED 87 YR FEMALE NOT INJURED
 [gCAD]  [!] EMS//4519 RIDGE RD WADSWORTH BETWEEN GRANGER RD / MEDINA RD//87 YR OLD FEMALE FALL VICTIM BACK & SHOULDER INJURIES
 [gCAD]  [!] MVA WITH INJURIES//2900 STATE RD MEDINA B ETWEEN LEDGE RD / REMSEN RD//CALLBK=041.192775-081.710042 IN THE SB LANE PICK UP TRUCK ON ITS SIDE MALE OUTSIDE THE TRUCK
 [gCAD]  [!] MVA WITH INJURIES//3275 STATE RD MEDINA B ETWEEN REMSEN RD / WILBUR RD//+041.187100-081.71163 0 MVA MINOR INJ
 [gCAD]  [!] EMS//171 GRANGER RD // 1 47 MEDINA BETWEEN N MEDINA LINE RD / DRUERIE LN//+ 041.134625-081.703584 87 YR FEMALE CONFUSED, NOT FEELING WE LL
 [gCAD]  [!] EMS//861 MEDINA RD MEDINA B ETWEEN STATE RD / BAMBECK RD//75-4 CALLED IN ADVISE D THEY RECXEIVED A CALL ON STATION FOR A TRANSPORT REF AN IN FECTION
 
  */
 
 public class OHMedinaCountyParser extends FieldProgramParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile(".CAD\\|!");
   public OHMedinaCountyParser() {
     super(CITY_LIST, "MEDINA COUNTY", "OH",
            "CALL! ADDR/S! INFO! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
    if (!SUBJECT_PTN.matcher(subject).matches()) return false;
    return parseFields(body.split("//"), data);
   }
   
   private static final Pattern BETWEEN_PTN = Pattern.compile("B *ETWEEN");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = BETWEEN_PTN.matcher(field);
       if (match.find()) {
         data.strCross = field.substring(match.end()).trim();
         field = field.substring(0,match.start()).trim();
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
   
   private static final String[] CITY_LIST = new String[]{
     "BRUNSWICK",
     "MEDINA",
     "RITTMAN",
     "WADSWORTH",
     
     "CHIPPEWA LAKE",
     "CRESTON",
     "GLORIA GLENS PARK",
     "LODI",
     "SEVILLE",
     "SPENCER",
     "WESTFIELD CENTER",
     
     "BRUNSWICK HILLS TWP",
     "CHATHAM TWP",
     "GRANGER TWP",
     "GUILFORD TWP",
     "HARRISVILLE TWP",
     "HINCKLEY TWP",
     "HOMER TWP",
     "LAFAYETTE TWP",
     "LITCHFIELD TWP",
     "LIVERPOOL TWP",
     "MEDINA TWP",
     "MONTVILLE TWP",
     "SHARON TWP",
     "SPENCER TWP",
     "WADSWORTH TWP",
     "WESTFIELD TWP",
     "YORK TWP",
     
     "BEEBETOWN",
     "HOMERVILLE",
     "LITCHFIELD",
     "VALLEY CITY",
   };
 }

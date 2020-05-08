 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Harnett County, NC
 Contact: Beverly Williams <bwilliams.co9@gmail.com>
 Contact: Keith combs <keithcombs2007@gmail.com>
 Contact: Michael Moston <mikemoston@gmail.com>
 Contact: edward woodall <ewoodall1971@gmail.com>
 Sender: cadpage@harnett.org
 
 1102-012074*8326 NC 27 W***LILLINGTON***RESIDENTIAL STRUCTURE FIRE****1175****
 S: M:1107-061052*3058 CRAWFORD RD***ERWIN***MOTOR VEHICLE ACCIDENT****1186****\n
 FRM:cadpage@harnett.org\nMSG:1108-070330*1995 BETHEL BAPTIST RD***SPRING LAKE***ELECTRICAL HAZARD****1120****
 1109-079514*117 SCRANTON CT***CAMERON***EMS DEAD ON ARRIVAL CODE 3****1109****
 
 Contact: Chris McCormick <mac3295@gmail.com>
 FRM:cadpage@harnett.org\nMSG:1111-092766*1 CLARK RD***LILLINGTON***MISC. FIRE****1175****
 FRM:cadpage@harnett.org\nMSG:1111-093153*8030 RAY MEDE***SPRING LAKE***MUTUAL AID WITH OTHER COUNTY********
 FRM:cadpage@harnett.org\nMSG:1201-003613*2728 DARROCH RD*****FIRE OTHER*********
 FRM:cadpage@harnett.org\nMSG:1201-002111*11860 NC 210 S*****FIRE RES STR*********
 
 Contact: Roy Gibbs <boham29@gmail.com>
 B1201-004551*11 BROOK LN*****EMS ABD PAIN*********
1201-009149*1 YOUNG RD***ANGIER**FIRE ILLEGAL*********
 
 Contact: Chris McCormick <mac3295@gmail.com>
 FRM:cadpage@harnett.org\nMSG:1201-006955*2802 WALKER RD***LINDEN**FIRE RES STR*********
 
 */
 
 public class NCHarnettCountyParser extends FieldProgramParser {
   
   public NCHarnettCountyParser() {
     super(CITY_LIST, "HARNETT COUNTY", "NC",
            "ID ADDR CITY? CALL! SEQ");
   }
   
   @Override
   public String getFilter() {
     return "cadpage@harnett.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     String flds[] = body.split("\\*+");
     if (flds.length < 3) return false;
     return parseFields(flds, data);
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{4}-\\d{6}"), true);
     }
   }
   
   private class SeqField extends SkipField {
     public SeqField() {
       setPattern(Pattern.compile("\\d{4}"), true);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("SEQ")) return new SeqField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     
     // Townships
     "ANDERSON CREEK",
     "AVERASBORO",
     "BARBECUE",
     "BLACK RIVER",
     "BUCKHORN",
     "DUKE",
     "GROVE",
     "HECTORS CREEK",
     "JOHNSONVILLE",
     "LILLINGTON",
     "NEILLS CREEK",
     "STEWARTS CREEK",
     "UPPER LITTLE RIVER",
     
     // Cities and towns
     "CAMERON",
     "COATS",
     "DUNN",
     "ERWIN",
     "LILLINGTON",
     "SPRING LAKE",
     
     // Unincorporated Communities
     "ANDERSON CREEK",
     "BARBECUE",
     "BARCLAYSVILLE",
     "BUIES CREEK",
     "BUNNLEVEL",
     "CAPE FEAR",
     "CHALYBEATE SPRINGS",
     "COKESBURY",
     "DUNCAN",
     "JOHNSONVILLE",
     "HARNETT",
     "KIPLING",
     "LUART",
     "MAMERS",
     "OLIVIA",
     "OVERHILLS",
     "PINEVIEW",
     "RYES",
     "SEMINOLE",
     "SHAWTOWN",
     "SPOUT SPRINGS",
     "TURLINGTON",
     
     // Other
    "LINDEN",
    "ANGIER"
   };
 }

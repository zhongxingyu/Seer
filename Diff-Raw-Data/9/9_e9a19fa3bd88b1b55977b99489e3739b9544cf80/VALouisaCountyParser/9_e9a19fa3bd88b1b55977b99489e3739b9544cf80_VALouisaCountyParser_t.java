 package net.anei.cadpage.parsers.VA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /***
 Louisa County, VA
 Contact: Michael Thaw <yankee.fire11@gmail.com>
 Contact: "Jaiebo" <jaiebo@yahoo.com>
 Sender: CADLCFEMS@louisa.org
 Sender: CADEMS2@louisa.org
 Sender: CADCo2@louisa.org
 
 E2011014354;RESCUE NEEDED;UNCONSCIOUS/UNRESPONSIVE/FAINTING;296 SHADY LN;INFO-44 YO M -- FEELING FAINT & DIZZY -- FEELING A LITTLE BETTER AT THIS TIME -- WANTI
 E2011016323;MVC;UNK INJURIES;2375 WEST BROAD ST;INFO-CALL TRANS'D TO GOOCHLAND
 E2011016344;FIRE;SMOKE-OUTSIDE;383 MOON SHADOW LN;INFO-CAN SMELL AND SEE IN THE SMOKE, NO ACTIVE FLAMES, NO CONTROL BURNS LOGGED IN THE AREA
 E2011016321;RESCUE NEEDED;ALLERGIC REACTION;409 POINDEXTER RD [# [@CO7;INFO-41YOM, BEE STING, AWAKE AND TALKING NORMALLY, BECOMING ITCHY, NO EPI PEN AVAILABLE, S
 E2011016299;RESCUE NEEDED;SICK/OTHER;1116 SIGNBOARD RD;INFO-32YOF HBP, VOMITING.  ONSET A COUPLE HOURS AGO - VOMITTED UP HER MEDS, STOMACH MEDS
E2011016568;FIRE;Advised;4745 EAST GRAY FOX CIRCLE;INFO-SHED ON FIRE ALSOWOODS ON FIRE
 E2011016535;MVC;UNK INJURIES;FREDERICKS HALL RD;INFO-1 CAR OFF ROADWAY  OUT OF CAR, SOME BRUISING - SAYS BRAKES WENT OUT.
 E2011016569;MVC;NO INJURIES;JAMES MADISON HWY & LOUISA RD;INFO-2 CAR MVC NO INJURIES OUT OF THE RD WAY SUBJS ARE OUT OF THE VEH
 E2011017511;FIRE;STRUCTURE-RESIDENTIAL;9996 KENTUCKY SPRINGS RD;INFO-HOUSE IS ON FIRE
 
 Contact: "Jason Slater" <jaiebo@yahoo.com>
 E2011019295;FIRE;Advised;E FIRST ST & ST CECELIA AVE;INFO-
 
 
 ***/
 
 public class VALouisaCountyParser extends FieldProgramParser {
 
   
   public VALouisaCountyParser() {
     super("LOUISA COUNTY", "VA",
            "ID CALL CALL ADDR! APT:APT? UNIT:UNIT? INFO:INFO!");
   }
   
   @Override
   public String getFilter() {
     return "@louisa.org,";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     body = body.replace("[#", ";APT:").replace("[@",";UNIT:").replace(";INFO-",";INFO:");
     return parseFields(body.split(";"), data);
   }
   
   // ID field, confirm has correct form
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("E\\d{10}"), true);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
 }

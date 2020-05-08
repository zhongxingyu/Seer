 package net.anei.cadpage.parsers.NC;
 
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Wilson County, NC
 Contact: Lori OBriant <lobriant@gmail.com>
 Sender: 930010NN
 
 The CAD number associated with the call (1100831), the EMD code (06D02),
 what the call is (BREATHING PROBLEMS), the address (5033 US 264A E), the
 district associated with the CAD for assigning units (2C), cross streets for
 address (RANCH FARM CR; HOLDERS CROSS RD) and the rest of the page is mostly
 what is in the notes field of the call in CAD.  Most of the calls are
 processed this way.
 
 CAD:11000831;06D02;BREATHING PROBLEMS;5036 US 264A E;2C;RANCH FARM CT;HOLDENS CROSS RD;[Medical Priority Info] Key Questions Complete RESPONSE:
 CAD:OUTSIDE FIRE - SMALL;5047 SAWDUST RD;4C;LASSITER MILL RD;TARTTS MILL RD
 CAD:11000827;69D10;STRUCTURE FIRE 1;4659 EVANSDALE RD;5B;OLD STANTONSBURG RD;CASTLEWOOD LN;403 enroute [05/19/11 13:53:19 DPITTMAN] [Fire Priority Info] Key Que
 CAD:STRUCTURE FIRE 1;4659 EVANSDALE RD;5B;OLD STANTONSBURG RD;CASTLEWOOD LN;291-5847 [05/19/11 13:51:05 SBATTS]
 CAD:11000827;69D10;STRUCTURE FIRE 1;4659 EVANSDALE RD;5B;OLD STANTONSBURG RD;CASTLEWOOD LN;403 enroute [05/19/11 13:53:19 DPITTMAN] [Fire Priority Info] Key Que
 CAD:11000831;06D02;BREATHING PROBLEMS;5036 US 264A E;2C;RANCH FARM CT;HOLDENS CROSS RD;[Medical Priority Info] Key Questions Complete RESPONSE: MEDIC & FR RESPO
 CAD:11000844;ACCIDENT PROPERTY DAMAGE;NC 222/ASPEN GROVE CHURCH RD;2A;on nc 222 just before aspen grove church while on the phone with hp she advised a hissing


 CAD:B&E to occupied residence occurred at 612 Fairfax Ave. 2 suspects busted in the back door, homeowner yelled and subjects ran. One subject 10-72 at this time
 CAD:ROLLING DOMESTIC - 701 HNES STREET - MALE PARTY STABBED - BOTH SUBJECTS 10-72 - MALE PARTY HAS NON-LIFE THREATNING INJURIES - AUTH 301 -- WILSON 911 TC LYLE
 
 */
 
 public class NCWilsonCountyParser extends DispatchOSSIParser {
   
   public NCWilsonCountyParser() {
     super("WILSON COUNTY", "NC",
            "ID? CODE? CALL ADDR! SRC X+? INFO+");
   }
 
   @Override
   public String getFilter() {
     return "930010";
   }
   
   // ID field has to validate it's existence
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{6,}"));
     }
   }
   
   // CODE field also has to validate its existence
   private class MyCodeField extends CodeField {
     public MyCodeField() {
      setPattern(Pattern.compile("\\d\\d[A-Z]\\d\\d"));
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("CODE")) return new MyCodeField();
     return super.getField(name);
   }
   
 }

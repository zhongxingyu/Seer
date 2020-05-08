 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Erie County, PA
 Contact: stephen skwaryk <hookandcan33@gmail.com>
 Sender: messaging@iamresponding.com
 
 Belle Valley / 26C1 &gt;SICK PERSON 41 W GORE RD Apt 282 Bldg XS OLD FRENCH RD MILLCREEK TWP RN - SUSAN\n\n
 Belle Valley / 26C1 &gt;SICK PERSON 41 W GORE RD Apt 282 Bldg XS OLD FRENCH RD MILLCREEK TWP RN - SUSAN\n\n
 Belle Valley / 17A1G &gt;FALLS (ON THE GROUND/FLOOR) 4717 CONRAD RD XS ZENITH DR MILLCREEK TWP WRAY, ROSE\n\n
 Belle Valley / 26A9 &gt;SICK PERSON 5450 CIDER MILL RD XS COVINGTON VALLEY DR MILLCREEK TWP GREGORY, CHRO\n\n
 Belle Valley / 17A1G &gt;FALLS (ON THE GROUND/FLOOR) 41 W GORE RD Apt 282 Bldg XS OLD FRENCH RD MILLCREEK TWP PEGGY\n\n
 Belle Valley / 52C1P &gt;ALM HI-LIFE HAZ PULL STATION 1928 WAGER RD XS BUNDY DR MILLCREEK TWP 37655 FROM SIMPLEX\n\n
 
Contact: Tyrell Davis <relley13@gmail.com>
(Belle Valley) 17B3G &gt;FALLS (ON THE GROUND/FLOOR) 1248 E ARLINGTON RD XS BRANDES ST MILLCREEK TWP DORAK, JANE\n
(Belle Valley) 26A5 &gt;SICK PERSON 5298 CRABAPPLE DR Apt 211 Bldg XS CIDER MILL RD MILLCREEK TWP OSBORN M L\n
(Belle Valley) 26C2 &gt;SICK PERSON 1747 E GRANDVIEW BLVD XS ALVIN ST MILLCREEK TWP HILL,RONALD\n
(Belle Valley) 10C2 &gt;CHEST PAIN 5143 SCHRIMPER RD XS RIPLEY DR MILLCREEK TWP MOREHOUSE, ADINA\n
(Belle Valley) 26A1 &gt;SICK PERSON 1188 E ARLINGTON RD XS BELLEVIEW DR MILLCREEK TWP SMITH PAUL\n

 Belle Valley / REMINDER THAT TONIGHT STARTS THE FIRST CLASS OF THE TRENCH RESCUE COURSE....360\n\n\n
 
  */
 
 
 public class PAErieCountyCParser extends FieldProgramParser {
   
   private static final Pattern MARKER = Pattern.compile("^(Belle Valley) / ");
   
   public PAErieCountyCParser() {
     super(PAErieCountyParser.CITY_LIST, "ERIE COUNTY", "PA",
            "ADDR/SC! Apt:APT Bldg:APT XS:X!");
   }
   
   @Override
   public String getFilter() {
     return "messaging@iamresponding.com";
   }
   
   @Override
  protected boolean parseMsg(String subject, String body, Data data) {
    do {
      if (subject.equals("Belle Valley")) {
        data.strSource = subject;
        break;
      }
      Matcher match = MARKER.matcher(body);
      if (match.find()) {
        data.strSource = match.group(1);
        body = body.substring(match.end()).trim();
        break;
      }
      return false;
    } while (false);
    
     body = body.replace(" Apt ", " Apt: ").replace(" Bldg ", " Bldg: ")
                .replace(" XS ", " XS: ");
     return super.parseMsg(body, data);
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf('>');
       if (pt < 0) abort();
       data.strCode = field.substring(0,pt).trim();
       field = field.substring(pt+1).trim();
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "CODE " + super.getFieldNames();
     }
   }
   
   private class MyAptField extends AptField {
     @Override
     public void parse(String field, Data data) {
       data.strApt = append(field, " - ", data.strApt);
     }
   }
   
   private class MyCrossField extends CrossField {
     
     @Override
     public void parse(String field, Data data) {
       parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS | FLAG_ONLY_CITY, field, data);
       data.strName = getLeft();
     }
     
     @Override
     public String getFieldNames() {
       return "X CITY NAME";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("APT")) return new MyAptField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
 }

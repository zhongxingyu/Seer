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
  protected boolean parseMsg(String body, Data data) {
    Matcher match = MARKER.matcher(body);
    if (!match.find()) return false;
    data.strSource = match.group(1);
    body = body.substring(match.end()).trim();
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

 package net.anei.cadpage.parsers.OR;
 
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 public class ORCrookCountyParser extends FieldProgramParser {
   
   public ORCrookCountyParser() {
     super("CROOK COUNTY", "OR",
           "DATETIME EVENT:ID! CALL! PRIORITY:SKIP! LOCATION:ADDR! CITY:CITY APT:APT PREMISE:PLACE COMMENT:INFO");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@prinevillepd.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     body = body.replace("\nEVENT # ","\nEVENT:")
                .replace("\nPRIORITY ","\nPRIORITY:").replace("\nLOCATION ","\nLOCATION:")
                .replace("\nCITY ","\nCITY:").replace("\nAPT ","\nAPT:");
     return parseFields(body.split("\n"), data);
   }
  
  private static final Pattern DATE_TIME_PTN = Pattern.compile("(\\d\\d?/\\d\\d?/\\d{4}) +(\\d\\d)(\\d\\d)");
  private class MyDateTimeField extends DateTimeField {
    @Override
    public void parse(String field, Data data) {
      Matcher match = DATE_TIME_PTN.matcher(field);
      if (!match.matches()) abort();
      data.strDate = match.group(1);
      data.strTime = match.group(2) + ':' + match.group(3);
    }
  }
  
  @Override
  public Field getField(String name) {
    if (name.equals("DATETIME")) return new MyDateTimeField();
    return super.getField(name);
  }
 }

 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Cabarrus County, NC (variant B)
 Contact: Mark Brown <hzmtinstructor@gmail.com>
Sender: 930010XX
 
 29:1|ODOR OF SMOKE INSIDE|OPS3|920 CHURCH ST N|ARDSLEY AVE NE|CONCORD PKWY N|2|21KA|10/04/2011 10:24:12|NEMC MAIN BUILDING(CMH)|7047836595|4431|1106269
 30:FYI: |1|COMMERCIAL FIRE ALARM|2460 ROCK HILL CHURCH RD|CLOISTER CT NW|CAVALIER CT NW|3|371|10/04/2011 14:37:43|CLARE BRIDGE (DISCOVERY)|7047200990|1106276
 34:10/05/2011 22:21:57|DWELLING FIRE|199 GOAR ST SW|FERN AVE SW|RONE AVE SW
 37:10/07/2011 22:05:08|DWELLING FIRE|2873 SIGNAL CT SW|PULLMAN ST SW
 
77:FYI: |1|RESIDENTIAL FIRE ALARM|764 JUANITA DR SW|JOSEPHINE LN SW|KIM ST SW|(S)YATES MEADOW SD (N)YATES MEADOWS|5|583|10/23/2011 19:55:32|1106744

 */
 
 public class NCCabarrusCountyBParser extends FieldProgramParser {
   
   private static final Pattern MARKER = Pattern.compile("^\\d\\d:");
   
   public NCCabarrusCountyBParser() {
     super("CABARRUS COUNTY", "NC",
           "( DATETIME CALL CH? ADDR! X X | FYI? DIGIT CALL CH? ADDR! X X INFO? PRI UNIT DATETIME ( ID | PLACE PHONE SKIP? ID ) )");
   }
   
   @Override
   public String getFilter() {
     return "93001";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     return parseFields(body.split("\\|"), data);
   }
   
   private class FYIField extends SkipField {
     public FYIField() {
       setPattern(Pattern.compile("FYI:"), true);
     }
   }
   
   private class DigitField extends SkipField {
     public DigitField() {
       setPattern(Pattern.compile("\\d"), true);
     }
   }
   
   private class MyChannelField extends ChannelField {
     public MyChannelField() {
       setPattern(Pattern.compile("OPS\\d+"));
     }
   }
   
  private class MyPriorityField extends PriorityField {
    public MyPriorityField() {
      setPattern(Pattern.compile("\\d"), true);
    }
  }
  
   private static final Pattern DATE_TIME_PTN = Pattern.compile("\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d:\\d\\d");
   private class MyDateTimeField extends DateTimeField {
     public MyDateTimeField() {
       setPattern(DATE_TIME_PTN, true);
     }
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{7}"));
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("FYI")) return new FYIField();
     if (name.equals("DIGIT")) return new DigitField();
     if (name.equals("CH")) return new MyChannelField();
    if (name.equals("PRI")) return new MyPriorityField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
 }

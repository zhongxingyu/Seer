 package net.anei.cadpage.parsers.WI;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Eau Claire, WI
 Contact: "Cody Davey" <cody@wisconsinrangers.net>
 Sender: tfddispatch@att.net
 
 [Case# 11-253]  @5:23:16 AM // Brunswick // W2250 Rim Rock Rd // Vehicle Fire // vehicle fire, no exposures
 [Case# 11-252]  @7:21:18 PM // Washington // 4940 Meadowbrook Ln // Sounding Fire Alarm // sounding fire alarm, showing 1st & 2nd floor smoke detector
 [Case# 11-251]  @4:03:35 PM // Washington // 4400 blk South Lowes creek  rd // Fire //
 [Case# 11-250]  @12:48:10 PM // Union // HWY 12 & N. Crossing // Motorvehicle Collision // 10/50 PI
 [Case# 11-249]  @1:04:04 PM // Brunswick // W2601 HWY 37 // Electrical/Powerline Emergency // powerline is down and pole is on fire
 [Case# 11-248]  @4:48:14 PM // Union // I-94, 59mm, Westbound // Motorvehicle Collision // 10/50 PI
 [Case# 11-244]  @1:01:11 PM // Brunswick // Jopke Rd & HWY 37 // Motorvehicle Collision // 10/50 PI
 [Case# 11-241]  @10:25:42 AM // Washington // I94 WB Mile Marke 65 // Rescue/Medical Assist // 61 y/o male poss stroke
 
Contact: Andrew Gaede <veghunter10@gmail.com>
Sender: tfddispatch@att.net
@4:38:53 PM // Rock Creek (Dunn) // HWY 85 & 870th // Motorvehicle\nCollision // 3 vehicle 10/50 PI, no extrication, Station 5 requested for\nassistance

  */
 public class WIEauClaireParser extends FieldProgramParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("Case# (\\d\\d-\\d{3})");
   
   public WIEauClaireParser() {
     super("EAU CLAIRE", "WI",
            "TIME CITY ADDR CALL INFO+");
   }
   
   @Override
   public String getFilter() {
     return "tfddispatch@att.net";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     Matcher  match = SUBJECT_PTN.matcher(subject);
    if (match.matches()) {
      data.strCallId = match.group(1);
    }
    body = body.replace("\n", " ").replaceAll("  +", " ");
     return parseFields(body.split("//"), data);
   }
   
  private class MyTimeField extends TimeField {
     public MyTimeField() {
       setPattern(Pattern.compile("@\\d{1,2}:\\d{1,2}:\\d{1,2} [AP]M"), true);
     }
    
    @Override
    public void parse(String field, Data data) {
      field = field.substring(2).trim();
      super.parse(field, data);
    }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("TIME")) return new MyTimeField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
 }

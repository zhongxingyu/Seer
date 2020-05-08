 package net.anei.cadpage.parsers.MI;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Saginaw County, MI (replacement)
 Contact: David Smigiel <bportfire2@gmail.com>
 Sender: 27538
 System: NXT
 LOC:S PORTSMOUTH RD / KING RD DESC:LAT: <43.384720>  LONG: <-83.837678> APT: TYP:*M*PDA PIN IN JUST OCCU
 
 Contact: "tld2a@sbcglobal.net" <tld2a@sbcglobal.net>
 Sender: 27538
 LOC:2673 OHIO DESC:52/M DIFF BREATHING/CHEST PAINS APT: TYP:*M*SICK PERSON IN PROGRESS
 LOC:3833 ORANGE ST DESC:79/F DIFF BREATHING/CHEST PAIN APT: TYP:*M*SICK PERSON IN PROGRESS
 LOC:3459 WOODLAND CT DESC:86 FE DIFF BREATHING APT: TYP:*M*SICK PERSON IN PROGRESS
 LOC:4030 JORDAN DR DESC:80/M  CHEST PAIN APT: TYP:*M*SICK PERSON IN PROGRESS
 LOC:S PORTSMOUTH RD / KING RD DESC:LAT: <43.384720>  LONG: <-83.837678> APT: TYP:*M*PDA PIN IN JUST OCCU
 LOC:7723 DIXIE HWY DESC: APT: TYP:*M*FIRE OUTSIDE
 LOC:2895 E MOORE RD DESC:80F CHEST PAIN APT: TYP:*M*SICK PERSON IN PROGRESS
 
 Contact: Chris Gerding <atfdgerding@yahoo.com>
 Sender: 27538
 LOC:NICHOLS RD / ASHMAN DESC:ENG SQUAD TENDER APT: TYP:*M*MUTUAL AID
 
 Contact: Eric Brown <e.j.brown678@gmail.com>
 Sender: 27538
 LOC:442 S 24TH DESC:32F- DIFF BREATH   STOMACH PAIN APT: TYP:*M*SICK PERSON IN PROGRESS
 
 Contact: Josh Mosher <mosherj2@gmail.com>
 Sender: 27538
 LOC:4891 IRONWOOD ST DESC:GREASE SPILL - SMOKE NO FLAMES APT: TYP:*M*APPLIANCE FIRE
 LOC:4019 EMERICK ST DESC:BONFIRE IN BACK YD APT: TYP:*M*FIRE OUTSIDE
 LOC:4787 FASHION SQUARE BLVD DESC:MACY UPPERLEVEL SMELL OF ELECTRICAL BURN APT: TYP:*M*ELEVATOR ENTRAPMENT RESCUE
 
  */
 public class MISaginawCountyParser extends FieldProgramParser {
   
   public MISaginawCountyParser() {
     super("SAGINAW COUNTY", "MI",
           "LOC:ADDR! DESC:CALL! APT:APT! TYP:CALL2!");
   }
   
   @Override
   public String getFilter() {
     return "27538";
   }
   
   private static final Pattern GPS_PTN = Pattern.compile("LAT: <([+-]?[\\d\\.]+)> +LONG: <([+-]?[\\d\\.]+)>");
  private class MyCall2Field extends CallField {
    @Override
     public void parse(String field, Data data) {
      Matcher match = GPS_PTN.matcher(data.strCall);
       if (match.find()) {
         data.strGPSLoc = match.group(1) + "," + match.group(2);
      } else if (data.strCall.length() > 0) return;
      if (field.startsWith("*M*")) field = field.substring(3).trim();
      data.strCall = field;
     }
     
     @Override
     public String getFieldNames() {
      return "GPS";
     }
   }
   
   @Override
   public Field getField(String name) {
    if (name.equals("CALL2")) return new MyCall2Field();
     return super.getField(name);
   }
 }

 package net.anei.cadpage.parsers.AL;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Dothan AL
 Contact: "hassain3738@gmail.com" <hassain3738@gmail.com>
 Contact: Tracy Faulkner <ycartf@gmail.com>
 Sender: Robot.ALERT@dothan.org
  
 15:49:38/SC-Service Call Non-Emergency/801 E LAFAYETTE ST/DOTHAN/14523968/ref lines fell at front door, see c404/
 16:31:23/SC-Service Call Non-Emergency/401 W INEZ RD/DOTHAN/TANGLEWOOD APARTMENTS/24135869/POWER LINES DOWN AT ENTRANCE, POSS SPARKING, RELAYED BY/
 18:35:57/MAID-Mutual Aid Request/5499 MONTGOMERY HWY INTERSECTN/DOTHAN/JOHN D ODOM RD/63981245/ECHO RESCUE --/
 18:43:58/S38B-Medical Call-Code III-Emergy/107 RIDGE RD/DOTHAN/95163482/63 YOM HEAD INJURY, S24/
 19:03:20/S22CF-Assault-Code II/1486 KINSEY RD/DOTHAN/51943628/ARM HURT, WAS PUSHED/
 19:08:26/S38B-Medical Call-Code III-Emergy/1303 SUMMIT ST/DOTHAN/14529368/WEAKNESS, PAIN/
 19:17:49/OF-Outdoor Fire-Level 1/1241 KINSEY RD APT186/DOTHAN/JOHNSON 186/51943628/SPARKS SHOOTING FROM POLE/
 19:58:39/SC-Service Call Non-Emergency/175 BUFORD LN/DOTHAN/LAKEWOOD FIRE STATION/24318569/A/O -- DO NOT RESPOND/
 20:11:48/S8BF-MVC-Code III/2299 JOHN D ODOM RD INTERSECTN/DOTHAN/MURPHY MILL RD/63891245/unk further~~ pr disconnected/
 20:15:39/S38B-Medical Call-Code III-Emergy/403 S APPLETREE ST APTJ31/DOTHAN/MARVIN LEWIS J31/14523968/pt low sugar/
 20:14:11/S38B-Medical Call-Code III-Emergy/210 N SAINT ANDREWS ST/DOTHAN/CRIMINAL JUSTICE BUILDING/15439268/INMATE -- 31 YOM CP -- HX: 2 HEART SURGERIES/
 11:22:21/SC-Service Call Non-Emergency/200 S APPLETREE ST APT3/DOTHAN/14523968/47 YOF, PT FELL,REQUEST LIFT ASSISTANCE/
 12:49:37/S8CF-MVC-Code II/3800 ROSS CLARK CIR INTERSECTN/DOTHAN/DENTON RD/39618452/VEHS PULLED INTO P/L OF THE OLD FOOD WORLD P/L/
 15:53:10/S8BF-MVC-Code III/2399 S PARK AVE INTERSECTN/DOTHAN/WOODLAND DR/24135869/pt c/o of neck pain...inv s5/
 18:04:21/S38A-Medical Call-Code III Crit-Emg/1 SAWGRASS DR INTERSECTN/DOTHAN/WESTBROOK RD/83621945/53 YOF C/O CHEST PAIN, N/V, SHAKING ~ OCCP >>>/
 18:09:20/S38B-Medical Call-Code III-Emergy/6007 W MAIN ST STE1/DOTHAN/WIREGRASS SHRINE CLUB/83621945/REF FEMALE PT FALLEN~FROM STANDING POSITION~~BLEEDING FR
 18:51:49/S22CF-Assault-Code II/2233 WESTGATE PKY APTL81/DOTHAN/WESTGATE VILLAGE L81/96315842/INV JUVS --- 6 YOM, BLEEDING FROM NOSE/
 20:52:18/S38B-Medical Call-Code III-Emergy/4310 MONTGOMERY HWY STE1/DOTHAN/WALMART NS/63981245/26 yom fallen on water / c/o back pain //
 21:37:04/S38A-Medical Call-Code III Crit-Emg/2031 THIRD AVE APTK51/DOTHAN/GRADYS WALK K51/41253896/58 YOF C/O HEAD PAIN, RADIATING THRU NECK & LEFT SHOULDER
 23:20:14/S22BF-Assault-Code III/1304 CORNELL AVE APTE26/DOTHAN/ROCK CREEK E26/36198425/pr refused/
 00:07:44/S38B-Medical Call-Code III-Emergy/102 JOHNSON CIR APT34/DOTHAN/JOHNSON 34/51943628/48 YOF POSS STROKE...FALLEN FROM STANDING POSITION/
 00:07:44/S38B-Medical Call-Code III-Emergy/102 JOHNSON CIR APT34/DOTHAN/JOHNSON 34/51943628/48 YOF POSS STROKE...FALLEN FROM STANDING POSITION/
 05:04:51/MAID-Mutual Aid Request/5028 REEVES ST INTERSECTN/DOTHAN/CITY LIMITS/95163482/ref s38b 49 yom actively seizing, 1040 w/abbeville rescue/
 10:25:33/S8CF-MVC-Code II/1 COLUMBIA HWY INTERSECTN/DOTHAN/E MAIN ST/15439268/
 14:52:12/S8BF-MVC-Code III/2410 ROSS CLARK CIR INTERSECTN/DOTHAN/S PARK AVE/24135869/PT JUST RELEASED FROM HOSPITAL, UNK IF INJURIED/
 11:15:34/S38B-Medical Call-Code III-Emergy/6726 E HIGHWAY 134/DOTHAN/769381254/elderly male appears disoriented ~~ 10-5 by ozark/dale co/
 09:48:02/S38B-Medical Call-Code III-Emergy/101 LOCKWYNN TRC/DOTHAN/68392145/20YOF SWEATING UNK FURTHER RELAYED BY THIRD PARTY/
 14:33:21/S38B-Medical Call-Code III-Emergy/342 S SAINT ANDREWS ST APT216/DOTHAN/VAUGHN TOWERS 216/14523968/58YOF  PAIN ALL OVER/
 00:30:09/MAID-Mutual Aid Request/17230 E STATE HWY 52 BLK/COLUMBIA/COFDA/F-COFD/
 16:52:57/OF-Outdoor Fire-Level 1/100 PEMCO DR/DOTHAN/PEMCO/769381254/BEHIND HANGER 15 GRASS IN FIELD.. TRANSFORMER BLOWN/
 15:21:59/S38C-Medical Call-Code II-Non Emgy/115 W ADAMS ST STE2/DOTHAN/MAGISTRATES/DOTHAN/15394628/
 12:52:08/S38A-Medical Call-Code III Crit-Emg/25 GOVERNOR DR/DOTHAN/WALLACE COLLEGE/769381254/PREGNANT PT ~~ LIGHT HEADED ~~ WALLACE HALL BLDG 25 UNK AGE/
 
 */
 public class ALDothanParser extends FieldProgramParser {
   
   
   public ALDothanParser() {
     super("DOTHAN", "AL",
            "TIME CALL ADDR/SXa CITY! PLACE+? ID INFO+");
   }
   
   @Override
   public String getFilter() {
     return "Robot.ALERT@dothan.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
    if (!parseFields(body.split("/"), data)) return false;
    
    // ID field is required, unless this is a mutual aid call
    return (data.strCallId.length() > 0 || data.strCall.startsWith("MAID-"));
   }
   
   private class TimeField extends SkipField {
     public TimeField() {
       setPattern(Pattern.compile("\\d\\d:\\d\\d:\\d\\d"), true);
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       super.parse(field, data);
       if (data.strApt.endsWith("INTERSECTN")) {
         data.strApt = data.strApt.substring(0, data.strApt.length()-10).trim();
       }
       if (data.strApt.startsWith("APT")) data.strApt = data.strApt.substring(3).trim();
     }
   }
   
   private class MyPlaceField extends PlaceField {
     
     @Override
     public void parse(String field, Data data) {
       if (checkAddress(field) > 0) {
         data.strCross = append(data.strCross, " & ", field);
       } else {
         data.strPlace = append(data.strPlace, "/", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "X PLACE";
     }
   }
   
   private static final Pattern ID_PTN = Pattern.compile("\\d{8,9}");
   private class MyIdField extends IdField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // checkParse is called when we are checking to see if this is a valid
       // ID field or a place field.  Here we make the normal pattern validation
       if (!ID_PTN.matcher(field).matches()) return false;
       super.parse(field, data);
       return true;
     }
     
     @Override
     public void parse(String field, Data data) {
       
       // parse is called if we have already parsed a place name and are looking
       // for the real ID field.  Generally we should succeed if this matches the
       // normal ID pattern and fail if it does not.
       // One important exception, if this is a mutual aid call, the call ID
       // may be missing entirely, in which case we will treat this as an info
       // field
       if (ID_PTN.matcher(field).matches()) {
         super.parse(field, data);
       } else if (data.strCall.startsWith("MAID-")) {
         data.strSupp = field;
       } else abort();
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("TIME")) return new TimeField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("PLACE")) return new MyPlaceField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
 }

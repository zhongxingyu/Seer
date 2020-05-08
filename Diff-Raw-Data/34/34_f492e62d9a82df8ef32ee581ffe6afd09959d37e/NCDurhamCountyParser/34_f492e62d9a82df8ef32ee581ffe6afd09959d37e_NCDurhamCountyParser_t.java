 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Durham County, NC
 Contact: "Greg Parrott" <gregp@pvfd.com>,9196674642@vzwpix.com
 System: OSSI
 CAD:TRANSPORTATION ACCIDENT;2399 S ALSTON AVE/RIDDLE RD;{M8} CODE 2 RESPONSE [06/30/11 15:48:22 GRIFFINT] {M8} SEND ADDTL MEDIC UNIT [06/30/11 15:46:11 GRIFFINT] [
 CAD:BACK PAIN;5499 SUTTERIDGE CT/LYON FARM DR;[Medical Priority Info] PROBLEM: back pain # PATS: 1 AGE: 50 Years SEX: Male CONSCIOUS: Yes BREATHING: Yes [07/05/11
 CAD:PREGNANCY;5438 NEW HOPE COMMONS DR;MT MORIAH RD;HOFFLER LN
 CAD:CHEST PAIN;8210 RENAISSANCE PKWY;CHEST PAIN .. [07/05/11 12:37:45 GARRETTG] EMPLOYEE INJ .. [07/05/11 12:37:15 GARRETTG] ;KNOLL CIR;LEONARDO DR
 CAD:CHEST PAIN;2238 W NC 54 HWY;CELESTE CIR
 CAD:TRANSPORTATION ACCIDENT;4117 EMPEROR BLVD;**************OPS1 [07/05/11 06:06:46 ARCH] ;S MIAMI BLVD;SWABIA CT
 CAD:TRANSPORTATION ACCIDENT;28163 I40 E/EXIT 283;BLK 4DR VOLVO, BLK VW JETTER, BLK 2 DR JETTA [07/04/11 20:06:12 GAY]
 CAD:SICK PERSON;101 GREEN CEDAR LN;[Medical Priority Info] RESPONSE: Charlie RESPONDER SCRIPT: Unknown status/Other codes not applicable (Unknown when the symptoms
 CAD:TRANSPORTATION ACCIDENT;6405 FAYETTEVILLE RD;THE CALLER WORKS INSIDE AND WAS NOT INVOLVED [07/04/11 14:35:13 BELLD] in the parking lot involving a gold toyota
 CAD:FALL;1101 EXCHANGE PL;[Medical Priority Info] PROBLEM: fell # PATS: 1 AGE: 80 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes [07/02/11 13:35:49 HINESLEY] ;MER
 CAD:DIABETIC PROBLEM;5219 PAGE RD;CREEKSTONE DR;TERRACE PINE DR
 
 Contact: Jeff Howard <parkwood151@gmail.com>
 From: CAD@durhamnc.gov
 (CAD:) ASST PD SEIZURE;1125 W NC 54 HWY;[LAW] {A324} CON BREATHING AND ALERT [09/10/11 01:52:07 MITCHELLM] Event spawned from INTOXICATED DRIVER. [09/10/2011 01:51
 
 Contact: "tc9008@aol.com" <tc9008@aol.com>
 Sender: CAD@durhamnc.gov
 CAD:SICK PERSON;6234 DELLO ST;***caller req no sirens*** [03/06/12 02:05:55 HOWARDS] ;DONLEE DR;DOYLE RD
 
 Contact: support.active911.com
 Sender: CAD@durhamnc.gov
 (CAD:ALLERGIES;249 COUNTRY CLUB DR;GREEN LANE DR;GREEN LANE DR)  
 (CAD:FALL;5600 N ROXBORO ST;GIVEN BY CALLER  [05/26/12 05)  47:42 ROBINSONC] 9194795652 [05/26/12 05:47:34 ROBINSONC] ;GOODWIN RD;N ROXBORO RD
 (CAD:EYE PROBLEM;5600 N ROXBORO ST;no eval  [05/27/12 15)  59:18 TANKSLEY] ;GOODWIN RD;N ROXBORO RD
 (CAD:BREATHING PROBLEM;2202 MILTON RD;WHITT RD;EDSEL RD)  
 (CAD:)  DIABETIC PROBLEM;5600 N ROXBORO ST;patient has not been evaluated by nurse or doctor- [05/27/12 12:06:40 SNYDERS] ;GOODWIN RD;N ROXBORO RD
 (CAD:UNCONSCIOUS;5617 LAUREL CREST DR;GOODWIN RD)  
 (CAD:FIRE ALARM;6014 GUESS RD;OPS1  [05/27/12 09)  48:49 WEAVERM] hall smoke detector [05/27/12 09:48:40 MILLS] ;CARDENS CREEK DR;MILTON RD
 (CAD:UNCONSCIOUS;5616 RUSSELL RD;FITZFORD CT;LAKE VISTA DR)  
 (CAD:)  STRUCTURE FIRE;7121 BLALOCK RD;UNABLE TO CONFIRM IF ANYONE IS IN THE HOUSE [05/25/12 16:21:27 SMITHK] OPS2 O [05/25/12 16:20:44 SMITHK] ACROSS THE STREET [05/25/12 16:20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE 
 (CAD:STRUCTURE FIRE;7121 BLALOCK RD;ACROSS THE STREET  [05/25/12 16)  20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE STATION RD;SEINEVIEW LN
 (CAD:ALLERGIES;249 COUNTRY CLUB DR;GREEN LANE DR;GREEN LANE DR) 
 (CAD:FALL;5600 N ROXBORO ST;GIVEN BY CALLER  [05/26/12 05) 47:42 ROBINSONC] 9194795652 [05/26/12 05:47:34 ROBINSONC] ;GOODWIN RD;N ROXBORO RD
 (CAD:EYE PROBLEM;5600 N ROXBORO ST;no eval  [05/27/12 15) 59:18 TANKSLEY] ;GOODWIN RD;N ROXBORO RD
 (CAD:BREATHING PROBLEM;2202 MILTON RD;WHITT RD;EDSEL RD) 
 (CAD:) DIABETIC PROBLEM;5600 N ROXBORO ST;patient has not been evaluated by nurse or doctor- [05/27/12 12:06:40 SNYDERS] ;GOODWIN RD;N ROXBORO RD
 (CAD:UNCONSCIOUS;5617 LAUREL CREST DR;GOODWIN RD) 
 (CAD:FIRE ALARM;6014 GUESS RD;OPS1  [05/27/12 09) 48:49 WEAVERM] hall smoke detector [05/27/12 09:48:40 MILLS] ;CARDENS CREEK DR;MILTON RD
 (CAD:) TRASH FIRE;800 KNIGHT DR;UNK WHAT MATERIAL IS BURNING IN THE BARRELL- [05/29/12 10:56:20 SNYDERS] CALLER WAS ADVISED TO CALL WHEN HIS NEIGHBOR IS BURNING A FIRE IN THE BACK OF HIS HOUSE, NOT THREATENING A STRUCTURE [05/29/;GEOFFRY RD;PR
 (CAD:) STRUCTURE FIRE;7121 BLALOCK RD;UNABLE TO CONFIRM IF ANYONE IS IN THE HOUSE [05/25/12 16:21:27 SMITHK] OPS2 O [05/25/12 16:20:44 SMITHK] ACROSS THE STREET [05/25/12 16:20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE 
 (CAD:STRUCTURE FIRE;7121 BLALOCK RD;ACROSS THE STREET  [05/25/12 16) 20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE STATION RD;SEINEVIEW LN
 (CAD:SICK PERSON;429 CONTINENTAL DR;attempting to get further  [05/28/12 18) 21:03 DILLARD] male in the frt yard not feeling good -- [05/28/12 18:20:50 DILLARD] ;LARK LN;GREEN LANE DR
 
 */
 
 public class NCDurhamCountyParser extends DispatchOSSIParser {
   
   public NCDurhamCountyParser() {
     super("DURHAM COUNTY", "NC",
            "CALL ADDR CH? X? X? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "CAD@durhamnc.gov";
   }
   
   private static final Pattern UNIT_PTN = Pattern.compile("^\\{(.*?)\\}");
   private static final Pattern JOIN_PTN = Pattern.compile("\\d\\d/\\d\\d/\\d\\d \\d\\d$");
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (subject.startsWith("CAD:")) {
       String join;
       if (subject.length() == 4) join = "";
       else if (JOIN_PTN.matcher(subject).find()) join = ":";
       else join = "";
       body = subject + join + body;
     }
     if (!super.parseMsg(body, data)) return false;
     Matcher match = UNIT_PTN.matcher(data.strSupp);
     if (match.find()) {
       data.strUnit = match.group(1);
       data.strSupp = data.strSupp.substring(match.end()).trim();
     }
     return true;
   }
   
   private static final Pattern CH_PTN = Pattern.compile("\\**(OP.*)");
   private class MyChannelField extends ChannelField {
 
     @Override
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       Matcher  match = CH_PTN.matcher(field);
       if (!match.matches()) return false;
       super.parse(match.group(1), data);
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CH")) return new MyChannelField();
     return super.getField(name);
   }
 }

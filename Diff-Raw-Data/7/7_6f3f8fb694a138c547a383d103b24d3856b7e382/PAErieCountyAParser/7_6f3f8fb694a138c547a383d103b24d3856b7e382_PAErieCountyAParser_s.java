 package net.anei.cadpage.parsers.PA;
 
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 
 /*
 Erie County, PA
 Contact: 11fullern@gmail.com
 Contact: Patrick Jackson <patjackson52@gmail.com>
 Contact: 7604734437@vtext.com
 
 ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 8165 PLATZ RD XS: MARKET RD FAIRVIEW TWP LIST JOHN C Map:2034 Grids:, Cad: 2011-0000044804
 ERIE911:55B2P >ELEC HAZ/PWR REPT DISCONNECTED 7656 MAPLE DR XS: CHESTNUT ST FAIRVIEW TWP MUSANTE, JANET Map:2202 Grids:, Cad: 2011-0000045114
 ERIE911:29B4 >MVA - UNKNOWN STATUS 17 I 90 EB XS: I 90 EB RAMP EXIT 16 FAIRVIEW TWP LORD, ISAAC Map:1888 Grids:, Cad: 2011-0000043981
 ERIE911:29B4 >MVA - UNKNOWN STATUS W LAKE RD&WHITEHALL PL XS: LORD RD FAIRVIEW TWP WOOD, RODNEY Cad: 2011-0000042496
 ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 6683 OTTEN CT FAIRVIEW TESTI JULIE Cad: 2011-0000039977
 ERIE911:52C3G >FIRE/GENERAL ALARM-COMM STRUC 7301 KLIER DR XS: UNFAIRVIEW FAIRVIEW TWP DAN Map:2302 Grids:, Cad: 2011-0000040143
 ERIE911:69D2 >STRUCTURE FIRE- HIGH RISE 222 W MAIN ST XS: MYRTLE ST GIRARD BORO Map:3217 Grids:, Cad: 2011-0000041382
 ERIE911:10D4 >CHEST PAIN 5757 W RIDGE RD XS: MILLFAIR RD FAIRVIEW TWP NICOLE Map:1988 Grids:, Cad: 2011-0000047247
 ERIE911:17D3 >FALLS 7648 WELCANA DR XS: LYNANN LN FAIRVIEW TWP SANDELL, CECELIA Map:2213 Grids:, Cad: 2011-0000047240
 ERIE911:26A1 >SICK PERSON 8300 W RIDGE RD XS: DOBLER RD FAIRVIEW TWP WIECZOREK, BOB Map:2185 Grids:, Cad: 2011-0000046184
 ERIE911:13A1 >DIABETIC PROBLEMS 8475 MIDDLE RD XS: BLAIR RD FAIRVIEW TWP SEAN Map:2174 Grids:, Cad: 2011-0000046843
 ERIE911:52B1H >RES (SINGLE) HEAT DETECTOR 1530 TAYLOR RIDGE CT FAIRVIEW TWP ADT/DIONNA Map:2540 Grids:, Cad: 2011-0000046825
 
 Contact: dan edwards <mainofic@gmail.com>
 ERIE911:6C1 >BREATHING PROBLEMS 817 POTOMAC AVE XS: W LAKE RD MILLCREEK TWP WATTS, BETTY Map:9214 Grids:, Cad: 2011-0000076275
 
 Contact: Matt Fuller <mfullererie@gmail.com>
 ERIE911:HASKINS RD IS NOW OPEN
 ERIE911:ACTIVE SHOOTER INCIDENTS - MANDATORY TRAINING - SEPT 6,7 OR 8TH. EIGHT HOUR COURSE. REQUIRED TO ATTEND ONE OF THE DAYS.
 ERIE911:SAMPSON RD NOW OPEN......
 ERIE911:32B1 >UNKNOWN PROBLEM 10793 ETTER RD XS: LAKE PLEASANT RD GREENE TWP DUSILA,CANDY Map:277 Grids:, Cad: 2011-0000090035
 ERIE911:29D2N2>MVA -EJECTION- HIGH MECHANISM PLUM RD VENANGO TWP ADAM Map:489 Grids:, Cad: 2011-0000096580
 
 Contact: Matt Exley <mexley46@gmail.com>
 FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:21B1 &gt;HEMORRHAGE / LACERATIONS 4242 ASBURY RD XS: THOROUGHBRED LOOP MILLCREEK TWP\n
 FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:9E1 &gt;CARDIAC/RESP ARREST / DEATH 4411 FOREST GLEN DR XS: W 38TH ST MILLCREEK TWP\n
 FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:28C10G&gt;STROKE (CVA) BREATH NORM &gt; 35 3643 MEADOW DR XS: CAUGHEY RD MILLCREEK TWP\n
 FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:10C4 >CHEST PAIN - BREATH NORMAL=>35 5125 W RIDGE RD Apt: 314 Bldg XS:CARLETON CT MILLCREEK TWP\r\n
 
 Contact: Matt Fuller <mfullererie@gmail.com>
 From: 8144312596,8149232720
 1 of 2:ERIE911:65A >MUTUAL AID/ASSIST OUTSIDE AGEN 328 HIGH ST XS: N PARK ROW WATERFORD BORO CHIEF CHRIS BARBER
 ERIE911:1C4 >ABDOM PAIN - FEM 12-50 W/FAINT 9625 MARK RD XS: WATTSBURG RD GREENE TWP WEED DAVID Map:307 Grids:, Cad: 2012-0000006327
 ERIE911:29D4A >MVA-TRAPPED MULTI PT/ADD RESPO LAKE PLEASANT RD&TATE RD XS: TATE RD GREENE TWP Cad: 2012-0000020343
 ERIE911:ROAD CLOSING ARBUCKLE RD XS: FOOTEMILL RD Cad: 2012-0000043019 ARBUCKLE FROM FOOTMILL TO LAKE PLEASANT RD CLOSED BY STREET DEPT
 ERIE911:17B1G >FALLS (GRD/FLR) POSS DANGER BO 9184 OLD WATTSBURG RD XS: HORSESHOE DR GREENE TWP Map:298 Grids:, Cad: 2012-0000045463
 
 Contact: "kmccaslin1@verizon.net" <kmccaslin1@verizon.net>
 Sender: 8144409141
 ERIE911:28C2L >STROKE-ABNORMAL BREATH <2HRS 835 LINDEN AVE XS: W LAKE RD MILLCREEK TWP Map:9769 Grids:, Cad: 2012-0000044276
 
 Contact: Matt Exley <mexley46@gmail.com>
 Sender: messaging@iamresponding.com
 FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:29B1 >MVA - WITH INJURIES 4440 ASBURY RD XS: LESTER LN MILLCREEK TWP\r\n
 
 */
 
 public class PAErieCountyAParser extends DispatchBParser {
   
   private static final Pattern MARKER2 = Pattern.compile("^[0-9A-Z]+ ?>");
  
   public PAErieCountyAParser() {
     super(PAErieCountyParser.CITY_LIST, "ERIE COUNTY", "PA");
   }
   
   @Override
   protected boolean isPageMsg(String body) {
     return true;
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     // Dummy do loop
     do {
       if (body.startsWith("ERIE911:")) {
         data.strSource = "ERIE911";
         break;
       }
       
       if (subject.length() > 0) {
         if (MARKER2.matcher(body).find()) {
           data.strSource = subject;
           break;
         }
       }
       
       return false;
     } while (false);
     
     boolean result = super.parseMsg(body, data);
     result = result && 
       (data.strCross.length() > 0 || 
        data.strCity.length() > 0 || 
        data.strCallId.length() > 0);
     if (!result && body.startsWith("ERIE911:")) {
       data.parseGeneralAlert(body.substring(8).trim());
       data.strSource = "ERIE911";
       result = true;
     }
     return result;
   }
 }

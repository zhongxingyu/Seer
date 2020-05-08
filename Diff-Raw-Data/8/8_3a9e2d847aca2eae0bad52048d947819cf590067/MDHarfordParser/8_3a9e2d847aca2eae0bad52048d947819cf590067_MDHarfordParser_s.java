 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*
 Harford, MD Sample pages
 Contact: "Greg Dietrich" <greg.dietrich@nvfc10.org>
 Sender:  rc.279@c-msg.net
 Under box number, the N in this case actually represents a 10## box. Also notice that a separate text is sent for each piece of equipment due on a box assignment (see the two last text messages below.
 [!] EOC:A1091 UNC >UNCONSCIOUS 2200 CHANNEL RD XS: X BIG BRANCH BRDG #127 PYLESVILLE CRIZER,KIM BOX: N08 Cad: 2010-0000162717
 [!] EOC:A1091 SICK >SICK PERSON 1606 SCOTT RD XS: X FAWN GROVE RD PYLESVILLE BATCHELDER,MICHELLE BOX: N04 Cad: 2010-0000162427
 [!] EOC:A1091 DIAB >DIABETIC EMRG. 2145 HARKINS RD XS: SAINT PAULS CHURCH RD PYLESVILLE ROHREAUGH,KEN BOX: N04 Cad: 2010-0000162445
 [!] EOC:E1011 MISC >MISC 4873 CAREA RD XS: X WALNUT SPRING CT WHITE HALL SHERBS,ROBERT BOX: N08 Cad: 2010-0000162395
 [!] EOC:A1091 MVA >MV ACCIDENT W/INJURY SRT24&SAINT MARYS RD XS: SAINT MARYS RD PYLESVILLE DEDRICK.PATTI Cad: 2010-0000162161
 [!] EOC:E1011 MVA >MV ACCIDENT W/INJURY SRT24&SAINT MARYS RD XS: SAINT MARYS RD PYLESVILLE DEDRICK.PATTI Cad: 2010-0000162161
 
 
  */
 public class MDHarfordParser extends SmartAddressParser {
   
   private static final String DEF_CITY = "HARFORD";
   private static final String DEF_STATE = "MD";
   
   private static final String[] CITY_LIST =new String[]{
     "ABERDEEN", "BEL AIR", "HAVRE DE GRACE", "JOPPA", "ABINGDON", "BELCAMP",
     "LEVEL", "CHURCHVILLE", "DARLINGTON", "WHITEFORD", "JARRETTSVILLE", "STREET",
     "HICKORY", "FALLSTON", "JOPPATOWNE", "FAWN GROVE", "DELTA", "PYLESVILLE", 
    "WHITE HALL"};
   
   private static final String[] KEYWORDS = new String[]{"BOX", "Cad"}; 
 
   public MDHarfordParser() {
     super(CITY_LIST, DEF_STATE);
   }
   @Override
   public boolean isPageMsg(String body) {
    return body.startsWith("[!] EOC:");
   }
 
   @Override
   protected void parse(String body, Data data) {
     
     data.defCity = DEF_CITY;
     data.defState=DEF_STATE;
     
     // First token following EOC: is the unit
     int pt = body.indexOf("EOC:");
     if (pt < 0) return;
     body = body.substring(pt+4).trim();
     pt = body.indexOf(' ');
     data.strUnit = body.substring(0, pt);
     body = body.substring(pt+1).trim();
     
     // Now we need the smart parser
     parseAddress(StartType.START_CALL, body, data);
     if (data.strCross.startsWith("X ")) {
       data.strCross = data.strCross.substring(2).trim();
     }
     body = getLeft();
     
     // The rest can be parsed reasonably 
     Properties props = parseMessage(body, KEYWORDS);
     data.strBox = props.getProperty("BOX", "");
     data.strCallId = props.getProperty("Cad", "");
   }
 }

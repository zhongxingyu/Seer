 package net.anei.cadpage;
 
 import java.util.Properties;
 
 
 /**
  * This clas is responsible for parsing useful information from an SMS page message
  */
 public class SmsMsgParser {
 
   private String strBody = "";
   private String strCall = "";
   private String strAddress = "";
   private String strCity = "";
   private String strApt = "";
   private String strCross = "";
   private String strBox= "" ;
   private String strUnit= "" ;
   private String strState="";
   private String strMap = "";
   private String strCallId = "";
   private String defCity = "";
   private String defState="";
   
   /**
    * Return original message text
    */
   public String getBody() {
     return strBody;
   }
   
   /**
    * @return call ID
    */
   public String getCallId() {
     return strCallId;
   }
 
   /**
    * @return the Call description
    */
   public String getCall() {
     return strCall;
   }
 
   /**
    * @return the call address
    */
   public String getAddress() {
     return strAddress;
   }
 
   public String getMapAddress() {
     StringBuilder sb = new StringBuilder(strAddress);
     String city = strCity;
     if (city.length() == 0) city = defCity;
     if (city.length() > 0) {
       sb.append(",");
       sb.append(city);
     } 
     String state = strState;
     if (state.length() == 0) state = defState;
     if (state.length() > 0) {
       sb.append(",");
       sb.append(state);
     }
     
     return sb.toString();
 	}
   /**
    * @return the call city
    */
   public String getCity() {
     return strCity;
   }
 
   /**
    * @return the call city
    */
   public String getState() {
     return strState;
   }
 
   /**
    * @return the call appt number
    */
   public String getApt() {
     return strApt;
   }
 
   /**
    * @return the call Cross street description
    */
   public String getCross() {
     return strCross;
   }
 
   /**
    * @return the call box
    */
   public String getBox() {
     return strBox;
   }
 
   /**
    * @return the call unit
    */
   public String getUnit() {
     return strUnit;
   }
   
   /**
    * @return map code
    */
   public String getMap() {
     return strMap;
   }
   
   /**
    * @return true if this message contain the text phrases that indicate it is 
    * a valid page message
    * @return true
    */
   public static boolean isPageMsg(String msgText) {
 
     // Next look up location code and use it to see if this message contains the trigger phrase
     String sLocation = ManagePreferences.location();
     int iLocation = Integer.parseInt(sLocation);
     String[] phrases = new String[]{"Call:", "TYPE:", "Map:", "(Corvallis Alert)","Cad:","OCSO E911:","CAD:","***","***"};
     if (iLocation > phrases.length) return false;
     boolean result = (msgText.indexOf(phrases[iLocation-1]) >= 0);
     return result;
   }
   
   /**
    * constructor
    * @param strMessage SMS message text to be parsed
    */
   public SmsMsgParser(String strMessage) {
 
     // Save message text
     strBody = strMessage;
     
     // Decode the call page and place the data in the database
         switch (Integer.parseInt(ManagePreferences.location())) {
         case 1:
           decodeLCFRPage(strMessage);
           break;
         case 2:
           decodeSuffolkPage(strMessage);
           break;
         case 3: 
           decodeHarrisPage(strMessage);
           break;
         case 4:
           decodeBentonCoPage(strMessage);
           break;
         case 5:
         	decodeHerkimerPage(strMessage);
         	break;
         case 6:
         	decodeOconeePage(strMessage);
         	break;
         case 7:
         	decodeLivingstonPage(strMessage);
         	break;
         case 8:
         	decodeDixHillsPage(strMessage);
         	break;
         case 9:
         	decodeBabylonPage(strMessage); 
         	break;
         case 10:
         	decodeMaconPage(strMessage);
         	break;
         }
 
   }
 
   private void decodeMaconPage(String body) {
 	/*
 911 CENTER:88S >STRUCTURE FIRE REPORTED AT 1650 WIDE HORIZON DR FRANKLIN AAAAA, AAAAA 5555554752 Map: Grids:0,0
 911 CENTER:90F >FIRE ALARM 120 RIVERVIEW ST FRANKLIN ANGEL MEDICAL CENTER 5555558411 Map: Grids:0,0
 911 CENTER:88S >STRUCTURE FIRE REPORTED AT 289 SUTTON LN FRANKLIN AAAAA, AAAAAAA& AAAAAA 5555550077 Map: Grids:0,0
 911 CENTER:MED >EMERGENCY RUN 248 HARRISON AVE XS: W MAIN ST TO BRYSON CITY RD FRANKLIN AAAAAA, CHARMAYNE 5555553738 Map: Grids:0,0
 911 CENTER:C1 >EMERGENCY RUN 11 CHATTACHOEE LN FRANKLIN VERIZON WIRELESS 5555555059
 911 CENTER:C1 >EMERGENCY RUN 236 PAULINE AVE XS: ULCO DR FRANKLIN AAAAA, AAAAAAA P 5555555567 Map: Grids:0,0
 911 CENTER:88S >STRUCTURE FIRE REPORTED AT 1682 LEATHERMAN GAP RD FRANKLIN  AAA, ANGIE 5555554829 Map: Grids:0,0
 911 CENTER:50 >VEHICLE ACCIDENT 837 GEORGIA RD FRANKLIN AAAAA 5555558237 Map: Grids:0,0
 	 */
 	  Log.v("decodeLCFRPage: Message Body of:" + body);
 	  defState = "NC";
 	  defCity = "MACON COUNTY";
 	  body= body.replace(">","Call:");
 	  
 	  
 }
 
 private static Properties LCFRCityCodes = buildCodeTable(new String[]{
       "CH", "Chantilly",
       "LB", "Leesburg",
       "AL", "Aldie",
       "ST", "Sterling",
       "MB", "Middleburg",
       "AB", "Ashburn",
       "SP", "Sterling",
       "BL", "Bluemont",
       "CE", "Centreville",
       "HA", "Hamilton",
       "LV", "Lovettsville",
       "PA", "Paris",
       "PV", "Purceville",
       "PS", "Paeonian",
       "RH", "Round Hill",
       "UP", "Upperville",
       "FX19", "Fairfax",
       "FX", "Fairfax",
       "FQ", "Faquier"});
   private void decodeLCFRPage(String body) {
     /*
      * Reporting email: MadisonFD <madisonfd@windstream.net>
      * Call:12D02E-SEIZURES CONT,42914 OVERLY SQ-CH Apt:,X-St:NORRINGTON SQ KIR,A6092,Box:1908 ,ADC:5520 G06 [90]
      * Call:17D03-FALL PATIENT N,42848 RECTORS CHASE WAY-AB Apt:,X-St:HOYSVILLE MANOR D,A6092 904 ACO9,Box:2618 ,ADC:5400 G03 [89]
      * Call:17D03-FALL PATIENT N,42848 RECTORS CHASE WAY-AB Apt:,X-St:HOYSVILLE MANOR D,A6092 904 ACO9,Box:2618 ,ADC:5400 G03 [89]
      * Call:20D01H-HEAT EXPOSURE,STONE SPRINGS BLVD-AL/MINERAL SPRINGS CIR-AL Apt:,X-St:GREENSTONE DR & M,A6092 9942 ACO9,Box:0910 ,ADC:5520 B02 [77]
      */
 
 
     Log.v("decodeLCFRPage: Message Body of:" + body);
     defState = "VA";
 
     // Needs some massaging before it can be run through the standard parser
     body = body.replace(" Apt:", ",Apt:");
     Properties props = parseMessage(body, ",", new String[]{"Addr", "Unit"});
     strCall = props.getProperty("Call", "");
     parseAddress(props.getProperty("Addr", ""));
     strApt = props.getProperty("Apt", "");
     strCross = props.getProperty("X-st", "");
     strUnit = props.getProperty("Unit", "");
     strBox = props.getProperty("Box", "");
     strMap = props.getProperty("ADC", "");
     
     strCity = convertCodes(strCity, LCFRCityCodes);
   }
 
   private static String[] SuffolkCityCodes = new String[]{
     "BRENTW", "Brentwood",
     "NBAYSH", "Bay Shore",
     "BAYSHO", "Bay Shore"
   };
   private void decodeSuffolkPage(String body) {
     /* Sample Suffolk Page
      * TYPE: GAS LEAKS / GAS ODOR (NATURAL / L.P.G.) LOC: 11 BRENTWOOD PKWY BRENTW HOMELESS SHELTER CROSS: PENNSYLVANIA AV / SUFFOLK AV CODE: 60-B-2 TIME: 12:54:16
      * or  TYPE: STRUCTURE FIRE LOC: 81 NEW HAMPSHIRE AV NBAYSH  CROSS: E FORKS RD / E 3 AV CODE: 69-D-10 TIME: 16:36:48
      * 
      */
     
       Log.v("DecodeSuffolkPage: Message Body of:" + body);
       defState="NY";
       defCity="SUFFOLK COUNTY";
 
       Properties props = parseMessage(body, new String[]{"LOC", "CROSS", "CODE", "TIME"});
       strCall = props.getProperty("TYPE", "");
       parseAddress(props.getProperty("LOC", ""));
       strCross = props.getProperty("CROSS", "");
       
       for (int ndx = 0; ndx<SuffolkCityCodes.length-1; ndx+= 2) {
         int ipt = strAddress.lastIndexOf(" " + SuffolkCityCodes[ndx]);
         if (ipt >= 0) {
           strAddress = strAddress.substring(0, ipt);
           strCity = SuffolkCityCodes[ndx+1];
           break;
         }
       }
   }
 
   private void decodeHarrisPage(String body) {
     /* Sample Harris Page
      * 11:58 W HILLSIDE DR/EASTEX FRWY, ; Map:414D Sub: Nat:MA-MUTUAL AID / ASSIST AGENCY Units:E91 T81 T73 E-E39 X-St:EASTEX
 
   1of2:09/06 11:56 W HILLSIDE DR/EASTEX FRWY, ; Map:414D Sub: Nat:MA-MUTUAL AID / ASSIST AGENCY Units:E91 T81 T73 E-L19 X-St:EASTEX
 
   1of2:09/06 11:28 19707 WOOD WALK LN, ; Map:337U Sub:PINEHURST OF ATASCOCITA Nat:09E01-ARREST - NOT BREATHING Units:E-M19 E-M29 E-7900
 
   1of2:09/05 08:56 19226 AQUATIC DR, ; Map:378A Sub:WALDEN ON LAKE HOUSTON Nat:52B01G-FIRE ALARM - RESIDENTIAL Units:ATFD E-E39 X-
 
   1of2:09/04 19:45 17219 KOBUK VALLEY CIR, ; Map:377E Sub:EAGLE SPRINGS Nat:67B03U-OUTSIDE FIRE - INVESTIGA Units:E-E39 X-St:*** Dead
 
   1of2:09/03 08:14 LILES LN/WOODLAND HILLS DR, ; Map:376H Sub:ATASCOCITA FOREST Nat:29-MOTOR VEHICLE INCIDENT Units:E-M19 E-E39 X-
 
     
      */
   
     Log.v("DecodeHarrisPage: Message Body of:" + body);
     defState="TX";
     defCity="HARRIS COUNTY";
     
     // Strip first 16 characters off of message
     if (body.length() <= 16) return;
     body = "Loc:" + body.substring(16);
     
     Properties props = parseMessage(body, new String[]{"Map", "Sub", "Nat", "Units", "X-St"});
     parseAddress(props.getProperty("Loc", ""));
     strMap = props.getProperty("Map", "");
     strCall = props.getProperty("Nat", "");
     strUnit = props.getProperty("Units", "");
     strCross = props.getProperty("X-st", "");
     
     // Strip extra stuff off of address line
     int ipt = strAddress.indexOf(',');
     if (ipt > 0) strAddress = strAddress.substring(0, ipt).trim();
     if (strCall.contains("MA-MUTUAL AID")) strCity = "Humble";
   }
   
 
   private void decodeBentonCoPage(String body) {
       
       // Sample Benton County Page
       // from: alerts@corvallis.ealertgov.com
       // (Corvallis Alert) INC: CODE 1 MEDICAL\nADD:1740 MAIN ST\nAPT:\nCITY:PHILOMATH\nX:N 17TH ST * N 18TH ST\nMAP:540-365\nCFS:0907010-119\nDIS:PHILOMATH FIRE
       // (Corvallis Alert) INC:COMM FIRE ALARM\nADD:421 S 19TH ST\nAPT:\nCITY:PHILOMATH\nX:ASH ST * CEDAR ST\nMAP:540-360\nCFS:100410-188\nDIS:PHILOMATH FIRE\nDIS:PHI
       
       Log.v("DecodeBentonCo: Message Body of:" + body);
       defState="OR";
       defCity="BENTON COUNTY";
       Properties props = parseMessage(body, "\n");
       
       strCall=props.getProperty("INC", "");
       strAddress=props.getProperty("ADD", "");
       strCity=props.getProperty("CITY", "");
       strApt =props.getProperty("APT", "");
       strCross=props.getProperty("X", "");
       strMap=props.getProperty("MAP", "");
       strCallId = props.getProperty("CFS", ""); 
   }
 
 
   private void decodeOconeePage(String body) {
     /* Sample Oconee Page
      * 
      * OCSO E911:Return Phone: 7060000000 S28 SICK PERSON 4047 COLHAM FERRY RD 8583046 
      * OCSO E911:Return Phone: 7060000000 S28 SICK PERSON 385 JEFFERSON AVE 2029728 
      * OCSO E911:1090F FIRE ALARM 1021 WOOD HOLLOW LN 5482767 CRYSTAL HILLS DR
      * OCSO E911:1073 SMOKE 1421 BEVERLY DR 5495253 NONA DRIVE
      * OCSO E911:1070 FIRE 1280 ASHLAND DR 7250300 HWY 53
      */
 
     Log.v("DecodeOconeePage: Message Body of:" + body);
     defState="GA";
     defCity="Oconee";
 
     // Skip everything up to first colon
     int ipt = body.indexOf(':');
     if (ipt >= 0) body = body.substring(ipt+1).trim();
 
     // Skip phone number
     boolean skip = false;
     int mode = 0;
     int callCnt = 0;
     for (String token : body.split("\\s+")) {
       switch (mode) {
       
       // Processing call
       // Ignore phone number, terminated by numeric token which is assumed to
       // be start of address
       case 0:
         if (skip) {
           skip = false;
           continue;
         }
         if (token.equalsIgnoreCase("Phone:")) {
           strCall = "";
           callCnt = 0;
           skip = true;
           continue;
         }
         
         if (callCnt >= 3 || callCnt > 0 && token.matches("\\d{2,}")) {
         	mode++;
         	// no break, fall through to address processing
         } else {
           callCnt++;
           if (strCall.length() > 0) strCall += " ";
           strCall += token;
           break;
         }
         
       // Processing address
       // terminated by 7 digit call ID (We think)
       case 1:
         if (token.matches("\\d{7}")) {
           strCallId = token;
           mode++;
           break;
         }
         if (strAddress.length() > 0) strAddress += " ";
         strAddress += token;
         break;
         
       // Cross street processing
       // This actually looks more like an approach street, but assigning it
       // to cross will at least get it to display
       case 2:
         if (strCross.length() > 0) strCross += " ";
         strCross += token;
         break;
       }
     }
   }
   
   private void decodeHerkimerPage(String body) {
 	    /* 
 	     * Sample Herkimer Pages
 	     * Reporting email: Chris Conover <conovercj@ntcnet.com>
 	     * Sender: HC911@herkimercounty.org
        * (EMS   >EMS CALL) 185 GUIDEBOARD RD XS: DAIRY HILL RD NORWAY AAAAAAA AAAAAAA 3150000000 Map: Grids:, Cad: 2010-0000049305
        * (MVA   >MOTOR VEHICLE ACCIDENT) 5781 STHY 28 XS: TOWN LINE NEWPORT AAAAAA AAAAA 3150000000 Map: Grids:, Cad: 2010-0000049651
        * (EMS   >EMS CALL) 808 OLD STATE RD NEWPORT AAAAAAAA 8880000000 Map: Grids:, Cad: 2010-0000049432
These may be new herkimer county pages that are not complatable with the previous ones.  Further information
has been requested.
 (EMS>EMS CALL) 112 N Main St\nGrids:,, NY X:Fairfield St\nMiddleville Village Paula, Wayne\n5419991234 MAP:
 (EMS>EMS CALL) 54 Fairfield St\nGrids:,, NY X:Fairfield St\nMiddleville Village Willoughby, Ruby\n5419991234 MAP:
 (LIFT>LIFT ASSIST/NON EMER EMS)\n112 N Main St Grids:,, NY\nX:Fairfield St Jones, Ronald\n5419991234 MAP: 
 	     */
 	    
 	      Log.v("DecodeHerkimerPage: Message Body of:" + body);
 	      defState="NY";
 	      
 	      body = body.trim();
 	      if (body.charAt(0) == '(') {
 	        int pt = body.indexOf(')');
 	        if (pt >= 1) {
 	          strCall = body.substring(1, pt);
 	          strCall = strCall.replaceAll("\\s*>", ">");
 	          body = body.substring(pt+1).trim();
 	        }
 	      }
 	      
 	      // Strip CAD number off end of message
 	      int pt = body.indexOf(" Cad:");
 	      if (pt >= 0) {
 	        strCallId = body.substring(pt+5).trim();
 	        body = body.substring(0, pt).trim();
 	      }
 	      
 	      // Address formatting is very strange.
 	      // Parse by whitespace separated tokens terminated by an entry that
 	      // contains a string of 3 or more 'A' characters.  The last entry 
 	      // before the terminator is the city.
 	      // And any tokens after an "XS:" token are added to the cross rather
 	      // than the address
 	      boolean cross = false;
 	      for (String token : body.split("\\s+")) {
 	        if (token.matches("AAA+")) break;
 	        if (strCity.length() > 0) {
 	          if (!cross) {
 	            if (strAddress.length() > 0) strAddress += " ";
 	            strAddress += strCity;
 	          } else {
 	            if (strCross.length() > 0) strCross += " ";
 	            strCross += strCity;
 	          }
 	        }
           if (token.equals("XS:")) {
             cross = true;
             strCity = "";
           } else {
             strCity = token;
           }
 	      }
 	  }
   private void decodeLivingstonPage(String body) { // Issue 17 
 	  /*
 	   * CAD:FYI: ;ALARMF;11555 PLEASANT VIEW DR;INDIAN TRL;SMOKE ALARMS GOING OFF IN THE
 HOUSE FOR THE LAST 30 MIN ON AND OFF/ NO SMOKE OR SMOKE SHOWING [09/24/10
 04:17:07 ECOOK]
 CAD:FYI: ;UNKACC;E M36/PETTYS RD;VEHICLE HIT A POLE/ MALE RUNNING E/B FROM SCENE
 [09/23/10 21:45:06 ECOOK]
 CAD:FYI: ;CHSTPN;8422 PAWNEE TRL;SHOSHONI PASS;[Medical Priority Info] RESPONSE:
 P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 72 year old, Female, Conscious,
 Breathing. Chest Pain (Non-Traumatic). Abnormal breathing. Cal
 CAD:FYI: ;UNCON;8732 RIVER VALLEY RD;[Medical Priority Info] RESPONSE: P1 STA 1
 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 63 year old, Female, Conscious, Breathing.
 Unconscious / Fainting (Near). Not alert. Caller Statement: UNCON.
 	   */
 	  Log.v("DecodeLivingstonPage: Message Body of:" + body);
       defState="MI";
       defCity = "Livingston";
       String tmpAddress="";
       body = body.trim();
       try { 
     	  String[] AData = body.split(";");
       strCall = AData[1];
       // Need to check for single address or Intersection address.
       if (AData[2].contains("/")  ){
         // This is an intersection and not a street
          String[] strTemp = AData[2].split("/");
          strTemp[0] = strTemp[0].substring(2);
         //strAddress = strTemp[0].substring(0,(strTemp[0].indexOf("-")));
          tmpAddress = strTemp[0];
 
         tmpAddress = tmpAddress + " and " +  strTemp[1].toString();
       }else {
         tmpAddress = AData[2].toString();
       }
       // Intersection address has a / and two  - cities
       strAddress= tmpAddress;
       if (strAddress.length() < 4) {
         strAddress = "Error Street not Found.";
       }
 
       //strApt = AData[1].substring(AData[1].indexOf("Apt:"));
       strApt= "";
       if (AData[3].length() <20) { //this is a cross street (guessing)
       strCross =  AData[3];
       strUnit = AData[4];
       } else {
     	  strCross = "";
     	  strUnit = AData[3]; //AData[3];
       }
       strBox = "";//AData[4].substring(4);  
       } catch (IndexOutOfBoundsException ex) {
         Log.v("Exception in decodeLivingston-" + ex.getMessage());
       }
   }
   
  private void decodeBabylonPage(String body) {
 	 /*
 *** 13 - Structure Fire *** 147 CHERUBINA LN CS: LEADER AVE  / SKIDMORE RD TOA: 22:37 09/22/10 OIL BURNER NORTH BABYLON FC 2010-002398 HY: 8' 11
 *** 13 - Structure Fire *** 514 MOUNT PL CS: ESSEX ST  / LAKEWAY DR TOA: 19:55 09/22/10 NORTH BABYLON FC 2010-002393 HY: 12' 533 MOUNT PL @ ESSE
 *** 2nd/16 - Rescue *** 733 HIGHRIDGE RD CS: OCONNER RD  / NARLAND LN TOA: 20:46 09/22/10 a/m pysch emer NORTH BABYLON FC 2010-002395
 *** 23 - Miscellaneous Fire *** SR CITZ APTS (5 BLDGS) COMPLEX 15 WEEKS RD CS: DEER PARK AVE  / MULHOLLAND DR TOA: 11:07 09/23/10 INVEST NORTH B
 	  */
 	 Log.v("DecodeBabylonPage: Message Body of:" + body);
 	  defState="NY";
 	  String tmpAddress="";
 	  body = body.trim();
       if (body.contains("***")) {
 	        int pt = body.indexOf("***");
 	        if (pt >= 0) {
 	          int pta = body.indexOf("***",pt+3);	
 	          strCall = body.substring(pt+3, pta);
 	          body = body.substring(pta+1).trim();
 	          
 	        }
 	      }
       try {
     	  String[] AData = body.split(":");
           // Need to check for single address or Intersection address.
     	  strAddress = AData[0].substring(2,AData[0].length()-2);
           if (AData[1].contains("/")  ){
             // This is an intersection and not a street
              String[] strTemp = AData[1].split("/");
              //strTemp[0] = strTemp[0].substring(2);
             //strAddress = strTemp[0].substring(0,(strTemp[0].indexOf("-")));
              tmpAddress = strTemp[0];
 
             tmpAddress = tmpAddress + " and " +  strTemp[1].substring(0,strTemp[1].length()-3);
           }else {
             tmpAddress = AData[1].toString();
           }
           strCross = tmpAddress;
           strUnit = AData[3];
           if (body.contains("FC ")){
         	  int iStart = body.indexOf("FC ");
         	  int iEnd = body.indexOf(" ",iStart+3);
         	  if (iEnd <=0) {iEnd = body.length(); }
         	  strUnit = strUnit + " #" + body.substring(iStart+3, iEnd);      	  
           }
       }catch (IndexOutOfBoundsException ex) {
         Log.v("Exception in decodeDixHillsPage-" + ex.getMessage());
       }
       
 	}
 
  private void decodeDixHillsPage(String body) {
 		/*
 2010-001784 23:36 *** 16- Rescue *** 17 BRYCEWOOD DR SPIEGEL, LORI A Dix Hills HQ ARISTA DR 31-A-1 UNCONSCIOUS / FAINTING (NEAR) CEWOOD DR DIXHIL TYPE:
 2010-001779 10:34 *** 24/16- Mutual Aid *** 51 BALDWIN PATH  Dix Hills HQ
 2010-001777 15:54 *** 16- Rescue ***  DEER PARK AV DEER PARK AV & MARYLAND ST Dix Hills HQ MARYLAND ST
 2010-001778 15:54 *** 23- Misc Fire ***  DEER PARK AV DEER PARK AV & MARYLAND ST Dix Hills HQ MARYLAND ST
 2010-001774 11:56 *** 13- Structure Fire *** 29 WHITE BIRCH DR FULGONI, MARISSA Dix Hills HQ BALSAM DR 52-B-1G ALARMS TE BIRCH DR DIXHIL TYPE: ALARMS 
 2010-001766 05:54 *** 13- Structure Fire *** 29 WILDWOOD DR TRUEN, CRAIG Dix Hills HQ WHITNEY CT 60-C-1O GAS LEAKS / GAS ODOR (NATURAL / L.P.G.) DWOOD
 		 */
 	  Log.v("DecodeDixHillsPage: Message Body of:" + body);
 	  defState="NY";
     defCity="Dix Hills";
 	  String tmpAddress="";
 	  body = body.trim();
       if (body.contains("***")) {
 	        int pt = body.indexOf("***");
 	        if (pt >= 0) {
 	          int pta = body.indexOf("***",pt+3);	
 	          strCall = body.substring(pt+3, pta);
 	          body = body.substring(pta+1).trim();
 	          int ptb = body.indexOf("Dix Hills");
 	          tmpAddress = body.substring(2,ptb);
 	          if (tmpAddress.contains(",")){
 	        	  int ptc = tmpAddress.indexOf(",");
 	        	  int ptd = tmpAddress.lastIndexOf(" ",ptc);
 	        	  strAddress = tmpAddress.substring(0,ptd);
 	        	  
 	          }
 	        }
 	      }
       strUnit = "";
       strCross= "";
       
 	}
  
  /** 
   * General purpose parser for formats where there is not a clear delimiter
   * between key: value item pairs.
   * @param body message body to be parsed
   * @param keyWords list of expected keywords
   * @return Properties object containing the parsed key: value pairs
   */
  private Properties parseMessage(String body, String[] keyWords) {
    StringBuffer sb = new StringBuffer(body);
    for (String keyWord : keyWords) {
      int ipt = sb.indexOf(" " + keyWord + ":");
      if (ipt >= 0) sb.setCharAt(ipt, '\n');
    }
    return parseMessage(sb.toString(), "\n");
  }
 
  /**
   * General purpose message parser for formats where there is a clear delimiter
   * between key: value data pairs 
   * @param body
   * @param delim line delimiter
   * @return
   */
  private Properties parseMessage(String body, String delim) {
    return parseMessage(body, delim, null);
  }
  
  /**
   * General purpose parser
   * @param body of message to be parsed
   * @param delim expression to be used to separate lines in message
   * @param missedKeys if not null, an array of keywords to be assigned to
   * lines that are missing a keyword
   * @return Properties object containing the parsed line tokens
   */
  private Properties parseMessage(String body, String delim, String[] missedKeys) {
    
      Properties props = new Properties();
      String[] lines = body.split(delim);
      int missed = 0;
      for (String line : lines) {
          line = line.trim();
          int ndx = line.indexOf(':');
          if (ndx < 0) {
            if (missedKeys != null && missed < missedKeys.length) {
              props.put(missedKeys[missed++], line);
            }
            continue;
          }
          if (ndx+1 == line.length()) continue;
          String key = line.substring(0, ndx).trim();
          String value = line.substring(ndx+1).trim();
          ndx = key.lastIndexOf(' ');
          if (ndx >= 0) key = key.substring(ndx+1);
          props.put(key, value);
      }
      return props;
  }
 
  /**
   * Parse address line into address and city fields
   * @param addressLine address line to be parsed
   */
  private void parseAddress(String addressLine) {
    for (String addr : addressLine.split("/")) {
      String[] tmp = addr.split("-");
      if (strAddress.length() > 0) strAddress += " and ";
      strAddress += tmp[0];
      if (tmp.length > 1) {
        tmp[1] = tmp[1].trim();
        int pta = tmp[1].indexOf(" ");
        if (pta <=0) pta = tmp[1].length();
        strCity = tmp[1].substring(0,pta);
      }
    }
  }
 
  /**
   * Build a code table for use by convertCodeTable
   * @param table array containing pairs of strings, each pair will be considered
   * as a key/value pair to be inserted into the code table
   * @return resulting code table
   */
  private static Properties buildCodeTable(String[] table) {
    Properties props = new Properties();
    for (int ndx = 0; ndx < table.length-1; ndx+=2) {
      props.put(table[ndx], table[ndx+1]);
    }
    return props;
  }
 
  /**
   * Convert any special codes in an item
   * @param item String item to be converted
   * @param codeTable table of codes with corresponding values
   * @return if item is a key to a codeTable entry, returns the associated value,
   * otherwise returns item
   */
  private String convertCodes(String item, Properties codeTable) {
    String value = codeTable.getProperty(item);
    return (value != null ? value : item);
  }
 }

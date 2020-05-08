 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Genesee County, NY
 Contact: rtripp@2ki.net
 Contact: Robert Bennett <rbennett8994@gmail.com>
 Sender: 911center@co.genesee.ny.us
 
 [Dispatch]  Acc PIAA ** 392.3 RT 90 E** , BATAVIA - ** ONE VEHICLE ROLLOVER ** LEWISTON RD, I 90 / KELSEY RD, I 90 ** 07/12/11 17:29 **
 [Dispatch]  Alarm Fire ** ** 2614 W  MAIN STREET RD , BATAVIA - ** 2ND FLOOR SMOKE DETECRTOR KENNETH HERMAN RESIDENCE ** READ
 [Dispatch]  Fire Auto ** 387.6 RT 90 W** , BATAVIA - ** POSS TRACTOR TRAILER FIRE - BOX TRAILER ** I 90, I-90 CONNECTOR / I 90,
 [Dispatch]  Fire Nat Propane Gas ** ** 8470 VIOLET LN , BATAVIA - ** COMPL STRUCK GAS LINE OUT THE RESIDENCE 1/2" LINE  ** DANA
 [Dispatch]  Acc PIAA ** ** 5402 ELLICOTT STREET RD , BETHANY - ** ONE VEHICLE ACCIDENT ** CLAPSADDLE RD / MAYNE RD ** 06/19/11 02:22 **
 (Dispatch) Acc PIAA ** FRONTIER KITCHENS** 8392 LEWISTON RD , BATAVIA - ** TWO CAR MVA MINER INJ ** PARK RD / W MAIN STREET RD
 (Dispatch) Alarm Fire ** COLLEGE VILLAGE** 8170 BATAVIA STAFFORD TOWNLINE RD , BATAVIA - ** F 201- FIRE ALARM - ALARM HAS RESE
 [Dispatch]  Acc PIAA ** Johnson Automotive** 3924 W  MAIN STREET RD , BATAVIA - ** 2 CAR MVA W INJ ** COUNTY BLDG / COLONIAL BL
 
 Sender: 7771
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Acc PIAA ** BANK STREET RD , ASSEMBLYMAN R. STEPHEN HAWLEY DR BATAVIA - ** 3 CAR MVA ** ** 01/19/12 11:07
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Acc PIAA * ** BATAVIA STAFFORD TOWNLINE RD , FOTCH RD BATAVIA - ** ** ** 01/20/12 16:08 ** 2012-00001287 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Fire CO Detector ** ** 3645 GALLOWAY RD , BATAVIA - ** CO DECTOR IS GOING OFF ** LEWISTON RD / KELSEY RD ** 01/18/12 18:52 ** 2012-00000016 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Acc PIAA * ** BANK STREET RD , ASSEMBLYMAN R. STEPHEN HAWLEY DR BATAVIA - ** 3 CAR MVA ** ** 01/19/12 11:07 ** 2012-00000188 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:EP56 Status:Dispatched EMD Alpha ** ** 2128 MAIN RD , PEMBROKE - ** 78 Y/O MALE COMPLAINING OF DIZZINESS ** INDIAN FALLS RD / CLEVELAND RD ** 01/22/12 06:57 ** 2012-00000021 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH EMD Delta ** ** 2584 MAIN RD , PEMBROKE - LOWER ** 25 YO MALE TROUBLE BREATHING - CO REQ NO FIRE DEPT ** SLUSSER RD / CHURCH ST ** 02/13/12 04:59 ** 2012-00000035 ** TXT STOP to opt-out
 
 Contact: Stephen Smelski <steveepfd@gmail.com>
 Sender: 77712
 GENESEE COUNTY DISPATCH Unit:EP57 Status:Dispatched EMD Alpha ** Batavia Mobile Home Park** 3322 W MAIN STREET RD , BATAVIA - 31 ** 40 Y/O MALE VOMITTING ** WORTENDYKE RD / STEGMAN RD ** 01/29/12 10:53 ** 2012-00000025 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH EMD Charlie ** ** 1858 MAIN RD , PEMBROKE - ** 91 YOM POSS STROKE ** CLEVELAND RD / BOYCE RD ** 01/30/12 20:25 ** 2012-00000026 ** TXT STOP to opt-out
 
 Contact: Ryan" <hinz_ryan@yahoo.com>
 From:7771
 GENESEE COUNTY DISPATCH EMD Charlie ** ** 11208 MAPLEWOOD RD , ALEXANDER - ** 10 MO FEMALE PASSING OUT ** STROH RD / GENESEE ST ** 02/07/12 19:53 ** 2012-00000026 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:AX10 Status:Dispatched EMD Delta ** ** 89 COLONY RUN , ATTICA - APT D ** 78YO MALE TROUBLE BREATHING  ** COLONY CT / ALEXANDER RD ** 02/26/12 20:04 ** 2012-00000046 ** TXT STOP to opt-out
 
 Contact: "wkirch@rochester.rr.com" <wkirch@rochester.rr.com>
 Sender: 777132537038
 GENESEE COUNTY DISPATCH Unit:AX06 Status:Dispatched Fire Mutual Aid ** * <UNKNOWN> , - ** FAST TEAM TO THE HOUSE FIRE ** ** 02/09/12 10:21 ** 2012-00000027 ** TXT STOP to opt-out
 
 Contact: Bob Tripp <btripp2003@gmail.com>
 Sender: 777147513270
 GENESEE COUNTY DISPATCH Alarm Fire ** College Village** 8170 BATAVIA STAFFORD TOWNLINE RD , BATAVIA - C 102 ** COMMERCIAL FIRE ALARM ** ASSEMBLYMAN R. STEPHEN HAWLEY DR / BYRON RD ** 03/20/12 21:11 ** 2012-00000064 ** TXT STOP to opt-out
 
Contact: Brian S <sabrefan7bs@gmail.com>
Sender: 777143485475
GENESEE COUNTY DISPATCH Unit:451 Status:Dispatched Fire Mutual Aid ** * 157 ASPEN ST , NEWSTEAD - ** STANDBY TANKERS IN YOUR HALL ** ** 03/31/12 14:44 ** 2012-00000055 ** TXT STOP to opt-out\r

 Contact: support@active911.com
 [Dispatch] Unit:EP57 Status:Dispatched EMD Alpha ** JIM'S SALOON** 2677 W  MAIN STREET RD , BATAVIA - ** 45 YO MALE FALLEN WITH BACK INJURY ** EAST AVE / HARTSHORN RD ** 04/01/12 02:37 ** 2012-00006425 **\nCONFIDENTIALITY NOTICE:  The information contained in this message and any documents, files, previous messages or other information attached to it, may be privileged, confidential and protected from disclosure. If the reader of this message is not the intended recipient(s), you are hereby notified that any dissemination, distribution, or copying of this communication is strictly prohibited. If you have received this communication in error, please notify us immediately by replying to the message and deleting it from your computer.�  \n
 [Dispatch] Unit:601 Status:Dispatched EMD Omega ** ** 3445 PRATT RD , BATAVIA - ** LIFTING ASSISTANCE  ** KELSEY RD / MILLER RD ** 04/01/12 14:59 ** 2012-00000082 **\nCONFIDENTIALITY NOTICE:  The information contained in this message and any documents, files, previous messages or other information attached to it, may be privileged, confidential and protected from disclosure. If the reader of this message is not the intended recipient(s), you are hereby notified that any dissemination, distribution, or copying of this communication is strictly prohibited. If you have received this communication in error, please notify us immediately by replying to the message and deleting it from your computer.�  \n
 
 [Dispatch] MENTAL HEALTH EVALUATION - SCENE IS SECURE - NYSP ON SCENE\nCONFIDENTIALITY NOTICE:  The information contained in this message and any documents, files, previous messages or other information attached to it, may be privileged, confidential and protected from disclosure. If the reader of this message is not the intended recipient(s), you are hereby notified that any dissemination, distribution, or copying of this communication is strictly prohibited. If you have received this communication in error, please notify us immediately by replying to the message and deleting it from your computer.�  \n
 
 */
 
 
 public class NYGeneseeCountyParser extends FieldProgramParser {
   
   public NYGeneseeCountyParser() {
     super(CITY_LIST, "GENESEE COUNTY", "NY",
            "Unit:UNIT? Status:CALL! PLACE? ADDR CITY? INFO! X DATETIME ID");
   }
   
   @Override
   public String getFilter() {
     return "911center@co.genesee.ny.us,7771";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     do {
       
       if (body.startsWith("GENESEE COUNTY DISPATCH ")) {
         body = body.substring(24).trim();
         break;
       }
       
       if (subject.equals("Dispatch")) break;
         
       return false;
     } while (false);
     
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     String saveBody = body;
     
     if (body.startsWith("Unit:")) {
       body = body.replace(" Status:", "**Status:");
     } else if(!body.startsWith("Status:")) {
       body = "Status: " + body;
     }
     
     String flds[] = body.split("\\*\\*");
     return parseFields(flds, 3, data) ||
             data.parseGeneralAlert(saveBody);
   }
   
   @Override
   public String getProgram() {
     return "UNIT " + super.getProgram();
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("Dispatched ")) field = field.substring(11).trim();
       if (field.endsWith("*")) field = field.substring(0,field.length()-1).trim();
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public boolean checkParse(String field, Data data) {
       
      // remove leading asterisk
      if (field.startsWith("*")) field = field.substring(1).trim();
      
       // If field contains comma, parse as address and cross / city
       String sApt = "";
       int pt = field.indexOf(',');
       if (pt >= 0) {
 	      String sAddr = field.substring(0,pt).trim();
 	      String sCity = field.substring(pt+1).trim();
 	      int pt2 = sCity.lastIndexOf('-');
 	      if (pt2 >= 0) {
 	        sApt = sCity.substring(pt2+1).trim();
 	        if (sApt.startsWith("APT ")) sApt = sApt.substring(4);
 	        sCity = sCity.substring(0,pt2).trim();
 	      }
 	      parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS | FLAG_ONLY_CITY | FLAG_ANCHOR_END, sCity, data);
         parseAddress(sAddr, data);
         data.strApt = append(data.strApt, "-", sApt);
 	      return true;
       }
       
       // Otherwise, treat it as an address if it starts with a digit
       // or is recognized as an address
       if (field.length() < 1) return false;
       if (Character.isDigit(field.charAt(0)) || checkAddress(field) > 0) {
         parseAddress(field, data);
         return true;
       }
       
       // Otherwise, not a valid address field
       return false;
     }
     
     @Override
     public void parse(String field, Data data) {
       if (!checkParse(field, data)) abort();
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY APT";
     }
   }
   
   private class MyCityField extends CityField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // If we got the city from the address field, we don't need this one
       if (data.strCity.length() > 0) return false;
       
       // Strip off trailing dash
       if (! field.startsWith(",")) return false;
       if (! field.endsWith("-")) return false;
       data.strCity = field.substring(1, field.length()-1).trim();
       return true;
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ALABAMA",
     "ALEXANDER",
     "ATTICA",
     "BATAVIA",
     "BERGEN",
     "BERGEN",
     "BETHANY",
     "BYRON",
     "CORFU",
     "DARIEN",
     "ELBA",
     "LE ROY",
     "OAKFIELD",
     "PAVILION",
     "PEMBROKE",
     "STAFFORD"
   };
 }

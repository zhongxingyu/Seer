 package net.anei.cadpage.parsers.NY;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.Message;
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Erie County, NY
 
 Contact: nyerpa96 <nyerpa96@gmail.com>
 Contact: Scott Yager <yagers13@yahoo.com>
 ALERT@ERIE.GOV FIRE CO-DETECTOR 262 MILLER ST APT: GARAGE LANCASTER TOWN CO DETECTOR ACTIV / NO SYMPTOMS
 ALERT@ERIE.GOV EMS 4779 TRANSIT RD LANCASTER TOWN GREEN DODGE CALIBER 25 YO FEMALE FEELS AS THOUGH SHE MAY PASS OUT
 ALERT@ERIE.GOV EMS 4805 TRANSIT RD APT: 1106 LANCASTER TOWN EMS- 59 YO MALE, LEG PAIN AS A RESULT OF A FALL EARLIER
 ALERT@ERIE.GOV EMS 48 BENTLEY CR LANCASTER TOWN 79 Y/O FEMALE LETHARGIC
 ALERT@ERIE.GOV FIRE CO-DETECTOR 47 VIA DONATO E LANCASTER TOWN CO DETECTOR NO SYMPTOMS REFER TWIN DISTRICT FD
 ALERT@ERIE.GOV EMS 4805 TRANSIT RD APT: 3105 LANCASTER TOWN EMS/66 YO FEMALE SEVERE HEADACHE/GENERAL ILLNESS REFER TWIN DISTRICT FD REF
 ALERT@ERIE.GOV FIRE-COMMERCIAL 2170 UNION RD WSE GENERAL FIRE ALARM GOING OFF.
 
 Contact: Todd Rzeszutek <firefighter5502@yahoo.com>
 Sender: 9300xxxx
 NOTIFICATIONS 85 MANITOU ST DEPEW DEPEW FIRE DEPARTMENT- DAILY TEST
 NOTIFICATIONS 85 MANITOU ST DEPEW CONDUCTED THE DAILY FIRE RADIO AND PAGER TEST
 4569 BROADWAY DEPEW COMMERCIAL FIRE ALARM COMING FROM THE BANQUET ROOM
 
 Contact: Kira Wilson <kira.jeanne0106@gmail.com>
 ALERT@ERIE.GOV EMS DOG BITE 140 VALLEY VIEW
 
 Contact: Bob Taylor <btthevoice@gmail.com>
 Sender: 15373@alert.erie.gov
 (Call) FIRE RESIDENTIAL 6680 LAKE SHORE RD EVANS FOREGIN OBJECT STRUCK A LIVE ELECTRICAL OUTLET.?\n
 (Call) EMS 1425 STURGEON PT RD EVANS H/NE/RM ACT  CARBON MONOXIDE DETECTOR WITH ILLNESS IN THE RESIDENCE?\n
 (Call) NE ACT FOR A ENGINE M/A TO LAKEVIEW FOR AN AUTO ACCIDENT ROUTE 5 AND OLD LAKESHORE RD?\n
 (Call) WIRES DOWN 7967 KEENE RD @ BURNS DISP NE - PATROL REQUESTING A CHIEF TO THE SCENE FOR WIRES IN THE TREE ARCHING?\n
 (Call) RE TRANSMIT OF PAGE FOR INJURY ACCIDENT IFO 8061 SOUTHWESTERN-NORTH EVANS ONLY?\r\n
 (Call) ALARM RINGING 8545 N MAIN ST EVANS FIRE ALARM ACTIVATION COMING FROM FIRE PANEL?\r\n
 
 Not yet implemented!
 Contact: peter putnam <medic4451@gmail.com>
 Sender: 17165415102
 Erie_Alert MUTUAL AID TO LAKEVIEW - FIRE ALARM ACTIVATION AT 5502 BIRCHWOOD.  <NOREPLY>
 
 
 Possible call code values
 
 AMA             MUTUAL AID-Automatic Mutual Aid                   
 AP              AIRPORT ALERT                                    
 AST             MISCELLANEOUS-Assist                             
 BA              ALARMS-Box Alarm                                 
 DUP             Duplicate Call                                    
 E911            Emergency Call                                    
 EMA             ALARMS-EMS Alarm                                 
 EMS             EMS-Emergency Medical Service                    
 EMSC            EMS-Emergency Medical Service Cold               
 F               Structure                                        
 FAL             ALARMS-Fire Alarm Activation                     
 HM              HAZMAT-Leak, Spill                               
 MAA             MUTUAL AID-Ambulance                             
 MAF             MUTUAL AID-Fire                                  
 MAR             MUTUAL AID-Rescue                                
 MAS             MUTUAL AID-Standby                               
 MF              Miscellaneous                                    
 MVA             EMS-Motor Vehicle Accident                       
 MVF             Motor Vehicle                                    
 NOT             Notification                                     
 POL             Police Emergency                                 
 R               EMS-Rescue                                       
 SA              Silent Alarm                                     
 TR              EMS-Transportation                               
 TRC             EMS-Transportation Cold                          
 TRH             EMS-Transportation Hot  
 */
 
 
 public class NYErieCountyEParser extends SmartAddressParser {
   
   private Message msg;
   
   public NYErieCountyEParser() {
     super(CITY_LIST, "ERIE COUNTY", "NY");
   }
 
   @Override
   protected Data parseMsg(Message msg, int parserFlags) {
     this.msg = msg;
     return super.parseMsg(msg, parserFlags);
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (msg.getAddress().equals("ALERT@ERIE.GOV")) return parseErieMsg(body, data);
     if (subject.equals("Call") || msg.getAddress().startsWith("9300")) return parseDepewMsg(body, data); 
     return false;
   }
 
   private boolean parseErieMsg(String body, Data data) {
     parseAddress(StartType.START_CALL, body, data);
     data.strSupp = getLeft();
    if (data.strCity.length() == 0) return false;
     data.strCity = convertCodes(data.strCity, CITY_CODES);
     return true;
   }
 
   private boolean parseDepewMsg(String body, Data data) {
     if (body.endsWith("?")) body = body.substring(0,body.length()-1).trim();
     parseAddress(StartType.START_CALL, body, data);
     if (data.strAddress.length() == 0) return false;
     if (data.strCall.length() == 0) {
       data.strCall = getLeft();
     } else {
       data.strSupp = getLeft();
     }
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ALDEN TOWN",
     "ALDEN",
     "AMHERST TOWN",
     "AMHERST",
     "ANGOLA ON THE LAKE",
     "ANGOLA",
     "AURORA TOWN",
     "AURORA",
     "BILLINGTON HEIGHTS",
     "BLASDELL",
     "BOSTON TOWN",
     "BOSTON",
     "BRANT TOWN",
     "BRANT",
     "BUFFALO",
     "CHEEKTOWAGA TOWN",
     "CHEEKTOWAGA",
     "CLARENCE CENTER",
     "CLARENCE TOWN",
     "CLARENCE",
     "COLDEN TOWN",
     "COLDEN",
     "COLLINS TOWN",
     "COLLINS",
     "CONCORD TOWN",
     "CONCORD",
     "DEPEW",
     "EAST AMHERST",
     "EAST AURORA",
     "EDEN TOWN",
     "EDEN",
     "ELMA CENTER",
     "ELMA TOWN",
     "ELMA",
     "EVANS TOWN",
     "EVANS",
     "FARNHAM",
     "GOWANDA",
     "GRAND ISLAND TOWN",
     "GRAND ISLAND",
     "HAMBURG TOWN",
     "HAMBURG",
     "HARRIS HILL",
     "HOLLAND TOWN",
     "HOLLAND",
     "KENMORE",
     "LACKAWANNA",
     "LAKE ERIE BEACH",
     "LAKE VIEW",
     "LANCASTER TOWN",
     "LANCASTER",
     "MARILLA TOWN",
     "MARILLA",
     "NEWSTEAD TOWN",
     "NEWSTEAD",
     "NORTH BOSTON",
     "NORTH COLLINS TOWN",
     "NORTH COLLINS",
     "ORCHARD PARK TOWN",
     "ORCHARD PARK",
     "SARDINIA TOWN",
     "SARDINIA",
     "SLOAN",
     "SPRINGVILLE",
     "TONAWANDA TOWN",
     "TONAWANDA",
     "TOWN LINE",
     "WALES TOWN",
     "WALES",
     "WEST SENECA TOWN",
     "WES SENECA",
     "WILLIAMSVILLE",
     
     "WSE"
   };
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "WSE",  "WEST SENECA",
   });
 }

 package net.anei.cadpage.parsers.NY;
 
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchRedAlertParser;
 
 /*
 Erie County, NY (again)
 Contact: afshar yusuf <afshar.yusuf@gmail.com>
 Contact: Joe Gallagher <patchinfirefigher@gmail.com>
 Sender: dispatch@townofhamburgny.com
 
 *2EMS: 60/M LONG FALL POSSIBLE ARM AND LEG FRACTURE ILS RESPONSE at 3425 OLD LAKEVIEW RD, Hamburg  c/s: ENGEL DR . . 17:31:57\n\n-- \nThis message has bee
 *2EMS: Male fell has a elbow laceration Cold Response at 25 BUFFALO ST #420, Village of Hamburg  c/s: MAIN ST   O: CREEK BEND HGTS APTS . . 05:16:00\n\n-
 *2EMS: 23 male poss od uncx at 7219 LIEBLER RD, Boston  c/s: COLE RD . . 19:44:01\n\n-- \nThis message has been scanned for viruse
 *2EMS: 51 female dyspnea paramedic response at 8169 BOSTON STATE RD, Boston  c/s: MAY DR   O: PEOPLE INC . . 21:26:12\n\n-- \nThis
 *2Misc Electrical Problem: TELEPHONE POLE ON FIRE NEAR ROADWAY at 6674 LIEBLER RD, Boston  c/s: BOSTON STATE RD . . 07:50:52\n\n--
 *2EMS: 79m weakness in right arm and leg poss stroke bls hot response no members on duty contact hfc at 6784 REDWING DR, Boston  c
 *2EMS: 71 Y/F NOT ALERT  PARAMEDIC RESPONSE   NO MEMBERS ON DUTY  at 8799 ROCKWOOD RD, Boston  c/s: PA/BO LINE . . 13:54:46\n\n--
  
 Contact: Matthew Whiting <firefighter015@gmail.com>
 Sender: dispatch@townofhamburgny.com
 *2Alarm Activation: General fire alarm at 7207 WARD RD, Boston  c/s: COLE RD . . 17:41:51
 *2EMS: 15 MONTH CHILD - SEIZURES - BLS HOT at BOSTON STATE RD / WILDWOOD DR, Boston . . 14:57:05
 *2Misc Rubbish/Brush, RUBBISH PROBLEM: LEAVES AND UNDERBRUSH ON FIRE IN WOODS WELL AWAY FROM HOUSE at 6754 OMPHALIUS RD, Boston  c/s: EDDY RD . . 13:47
 *2EMS: 70F Uncon Medic at 6714 Hilcroft . . 00:39:43
 *2EMS Auto Accident: AUTO ACCIDENT WITH INJURY POSS ROLL OVER 20 & 29 TO THE HALL at LOWER EAST HILL RD / WOHLHUETER RD, Boston . . 23:16:18
 *2Alarm Activation: general fire alarm at evans nat bank at 7205 BOSTON STATE RD, Boston  c/s: ZIMMERMAN RD   O: EVANS NATIONAL BANK . . 16:11:51
 *2EMS: 69 Years Male Chest pain- medic response at 7898 BOSTON STATE RD #40, Boston  c/s: WILDWOOD DR   O: BOSTON HILLS MOBILE HOME PARK . . 19:09:24
 *2EMS: M/chest pains at 8112 COLE RD, Boston  c/s: OMPHALIUS RD . . 20:05:16
 
Contact: eric dahlgren <ercdahl@gmail.com>
Sender: dispatch@townofhamburgny.com
EMS: HA 8 RESP FOR A TRANSPORT.  NO FURTHER REPONSE NECESSARY. at 239 BROOKWOOD DR, Village of Hamburg  c/s: BIRKSHIRE RD . . 21:3\r

 */
 
 public class NYErieCountyRedAlertParser extends DispatchRedAlertParser {
   
   public NYErieCountyRedAlertParser() {
     super("ERIE COUNTY","NY");
     setupMultiWordStreets(
         "BOSTON STATE",
         "LOWER EAST HILL"
     );
   }
 
   @Override
   public String getFilter() {
     return "dispatch@townofhamburgny.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
    if (body.startsWith("*2")) body = body.substring(2).trim();
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt);
     return super.parseMsg(subject, body, data);
   }
 }

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
 
 */
 
 public class NYErieCountyRedAlertParser extends DispatchRedAlertParser {
   
   public NYErieCountyRedAlertParser() {
     super("ERIE COUNTY","NY");
   }
 
   @Override
   public String getFilter() {
     return "dispatch@townofhamburgny.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!body.startsWith("*2")) return false;
     body = body.substring(2).trim();
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt);
     return super.parseMsg(subject, body, data);
   }
 }

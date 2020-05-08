 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Orange County, NC
 Contact: Rachel Reid <rachelr832@gmail.com>
 Sender: @cedargrovefire.org
 
 (CAD Page for CFS 060911-335) OPS \nTRNG/WRK SESSION\nTRNG AT 1900 HRS\nApt: \nBuild:
 (CAD Page for CFS 060511-228) OPS 2\nAuto Fire Alarm\n6514 DOC CORBETT RD\nApt: \nBuild:
 (CAD Page for CFS 060411-348) OPS 1\nBREATHING\n8100 NC 86 N\nApt: \nBuild:
 (CAD Page for CFS 060311-397) OPS 1\nTRAUMATIC INJURY\nMILL CREEK RD and WAXSTAFF RD\nApt: \nBuild:
 (CAD Page for CFS 052911-170) OPS 1\nTraffic Accident-PI\nMCDADE STORE RD and NC 86 N\nApt: \nBuild:
 (CAD Page for CFS 051311-370) OPS \nINFO FOR ALL UNITS\n643 tanker back in service\nApt: \nBuild:
 
 COntact: Ray Enoch <renoch357@gmail.com>
 Sender: cadpage@orangeem.org
 Subject:CAD Page for CFS 082311-84\nOPS 1\nUNCONSCIOUS\n412 N ELLIOTT RD\nApt: \nBuild: \n
 
 Contact: Chris Tomlin <tomlin@smokealert.net>
 Sender: cadpage@orangeem.org
 (CAD Page for CFS 111611-425) TG: OPS 1\nINC: \nLOC: 1098 BURNING TREE DR\nAPT: \nBLDG:
 
 Contact:"Bradley T. Hinton" <brad.hinton@duke.edu>
 Sender: cadpage@orangeem.org
 (CAD Page for CFS 111911-222) TG: OPS 1\nINC: BREATHING\nLOC: 1508 JO MAC RD\nAPT: \nBLDG:
 
 Contact: Rob Pruitt <repruitt@gmail.com>
 Sender: cadpage@orangeem.org
 Subject:CAD Page for CFS 112211-111\nTG: OPS \nINC: Auto Fire Alarm\nLOC: 511 HIGHGROVE DR\nAPT: \nBLDG: \n
Subject:CAD Page for CFS 012512-269\nRECD: 16:43:44\nTG: OPS 1\nINC: HEART PROBLEMS\nLOC: 741 W BARBEE CHAPEL RD\nAPT: \nBLDG: \nEMD: 19C03
 
  */
 
 
 public class NCOrangeCountyParser extends FieldProgramParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("CAD Page for CFS (\\d{6}-\\d{1,3})");
   
   public NCOrangeCountyParser() {
     super("ORANGE COUNTY", "NC",
           "( CH CALL ADDR! Apt:APT Build:APT | RECD:TIME? TG:CH INC:CALL LOC:ADDR! APT:APT BLDG:APT EMD:CODE )");
   }
   
   @Override
   public String getFilter() {
     return "@cedargrovefire.org,cadpage@orangeem.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     Matcher match = SUBJECT_PTN.matcher(subject);
     if (!match.matches()) return false;
     data.strCallId = match.group(1);
     return parseFields(body.split("\n"), data);
   }
   
   private class MyAptField extends AptField {
     @Override
     public void parse(String field, Data data) {
       data.strApt = append(field, " - ", data.strApt);
     }
   }
 
   @Override
   protected Field getField(String name) {
    if (name.equals("CH")) return new ChannelField("OPS.*");
     if (name.equals("APT")) return new MyAptField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
 }

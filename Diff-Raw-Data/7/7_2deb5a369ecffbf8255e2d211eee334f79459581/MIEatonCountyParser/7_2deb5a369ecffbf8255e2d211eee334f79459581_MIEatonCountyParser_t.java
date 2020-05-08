 package net.anei.cadpage.parsers.MI;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Eaton County, MI
 Contact: Steve Grosshans <firefighter925@gmail.com>
 Contact: Chris Sloan <sloanc35@gmail.com>
 Sender: cad@eatoncounty.org
 
 CAD:FYI: ;20110000176;10/04/2011 20:35:39;BRUSH FIRE;W MT HOPE HWY/N SHAYTOWN RD;CALLER PASSERBY [10/04/11 20:36:48 KANTCLIFF] N OF ABOVE ADDRESS
 CAD:FYI: ;20110000177;10/09/2011 09:45:21;MEDICAL ASSIST;3286 E EATON HWY;Event spawned from EMS PRIORITY 2. [10/09/2011 09:45:21
 CAD:FYI: ;20110000178;10/10/2011 07:59:43;MEDICAL ASSIST;10763 N SHAYTOWN RD;Event spawned from EMS PRIORITY 3. [10/10/2011 07:59:
 CAD:FYI: ;20110000179;10/11/2011 15:13:37;MEDICAL ASSIST;9040 W GRAND LEDGE HWY;Event spawned from EMS PRIORITY 1
 CAD:FYI: ;20110000180;10/11/2011 19:55:29;WIRES DOWN;11600 N IONIA RD;SEMI WENT BY TOOK DOWN WIRE RIPPED METER BOX FROM CALLERS HOUSE
 CAD:FYI: ;20110000485;09/16/2011 20:40:28;MEDICAL ASSIST;1931 GIDNER RD;Event spawned from EMS PRIORITY 1. [09/16/2011 20:40:28 JBLOUNT] {122} REQ MANPOWER [09/
 CAD:FYI: ;20110000487;09/20/2011 00:38:14;MEDICAL ASSIST;2704 S PERKEY RD;Event spawned from EMS PRIORITY 1. [09/20/2011 00:38:14 KANTCLIFF] [LAW] VICTIM IS TER
 CAD:FYI: ;20110000484;09/16/2011 19:23:15;SPECIAL RESCUE;ACKLEY RD/BELL HWY;Event spawned from UNKNOWN PROBLEM. [09/16/2011 19:23:15 JBLOUNT] ONE MAY BE CAUGHT
 CAD:FYI: ;20110000471;09/08/2011 14:27:39;MEDICAL ASSIST;414 RAILROAD ST;Event spawned from EMS PRIORITY 1. [09/08/2011 14:27:39 ROWENS] HX LEAKY VALVE IN HEART
 CAD:FYI: ;20110000486;09/17/2011 19:48:03;COMPLAINT FIRE INVESTIGATION;800 E SHAW ST;EVERYONE IS OUTSIDE, REF EMS. [09/17/11 19:49:20 JBLOUNT]
 CAD:FYI: ;20110000482;09/15/2011 19:42:53;COMPLAINT FIRE INVESTIGATION;W FIVE POINT HWY/MATTHEWS RD;W OF MATTHEWS [09/15/11 19:43:57 KANTCLIFF] NE SIDE OF THE R
 CAD:FYI: ;20110000481;09/15/2011 08:19:47;COMMERCIAL FIRE ALARM;417 MAYNARD ST;BEEN GOING FOR 5 MINS. [09/15/11 08:21:10 MBIALKOWSKI] CLLR IN THE NORTH WING. GO
 CAD:FYI: ;20110000480;09/14/2011 19:20:41;MISCELLANEOUS FIRE RUN;S PERKEY RD/E CLINTON TRL;Event spawned from MISCELLANEOUS INCIDENT. [09/14/2011 19:20:41 CYARG
 

Contact: Rebecca Bell <rjbell06@gmail.com>
CAD:FYI: ;EEMS;20120003108;01/29/2012 01:17:26;PRIORTY3;7291 TUCKER RD;ERCY;70/F VOMITTING THE PAST 3-4 HRS [01/29/12 01:21:07 MLUNDQUIST]

  */
 public class MIEatonCountyParser extends DispatchOSSIParser {
   
   public MIEatonCountyParser() {
     super("EATON COUNTY", "MI",
           "SKIP CALL? ID DATETIME CALL ADDR INFO+");
   }
   
   @Override
   public String getFilter() {
     return "cad@eatoncounty.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!body.startsWith("CAD:FYI: ;")) return false;
     return super.parseMsg(body, data);
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{11}"), true);
     }
   }
   
   private class MyDateTimeField extends DateTimeField {
     public MyDateTimeField() {
       setPattern(Pattern.compile("\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d:\\d\\d"), true);
     }
   }
   
   private static final Pattern PRIORITY_PTN = Pattern.compile("Event spawned .* PRIORITY (\\d).*");
   private class MyInfoField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = PRIORITY_PTN.matcher(field);
       if (match.matches()) {
         data.strPriority = match.group(1);
       } else {
         super.parse(field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "PRI INFO";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }

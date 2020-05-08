 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Guilford County, NC
 Contact: Shane Taylor <dstaylor9@gmail.com>
 Sender: @edispatches.com 
 System: OSSI CAD?
 [summerfieldfiredist Jan24-10:01]  CAD:SMFD;ST09;1;52C03C;FIRAL;7607 BLUE SAGE CT
 [summerfieldfiredist Jan19-18:15]  CAD:SMFD;ST29;1;50PI;SCALESVILLE RD/LAKE BRANDT RD
 [summerfieldfiredist Jan18-19:08]  CAD:1;MUTUAL;150 WAYNOR DR;GALLENT RD;STRUCTURE FIRE
 (summerfieldfiredist Jan18-17:00) CAD:SMFD;ST29;1;25D01;PSYCH;8579 HUDSON JAMES RD;WRIGHTS FARM LN
 [summerfieldfiredist Jan18-03:16]  CAD:SMFD;ST39;1;29B01;50PI;PLAINFIELD RD/LAKE BRANDT RD;BETWEEN LAKE BRANDT AND CHURCH ST
 (summerfieldfiredist Jan21-03:25) CAD:SMFD;ST09;1;67B01;OSFIR;2523 PLEASANT RIDGE RD;SADDLEWOOD DR;DIST: 35.58 FT
 (summerfieldfiredist Jan23-06:49) CAD:SMFD;ST39;1;17B03;FALL;6251 LAKE BRANDT RD;HI-LO LN
 [summerfieldfiredist Jan23-19:20]  CAD:OKRD;ST51;1;69D11;STRUC;4529 PEEPLES RD;RIDING TRAIL CT
 [summerfieldfiredist Jan23-19:18]  CAD:OKRD;ST51;1;STRUC;4529 PEEPLES RD;RIDING TRAIL CT
 [summerfieldfiredist Jan23-20:23]  CAD:OKRD;ST51;1;69D06;STRUC;5371 NORTHWEST SCHOOL RD;TRAIL PIEDMONT GREENWAY
 (summerfieldfiredist Jan24-10:31) CAD:UNDER CONTROL;7607 BLUE SAGE CT; SU
 (summerfieldfiredist Jan23-11:25) CAD:UNDER CONTROL;6308 PEPPERMILL DR; OAK
 (summerfieldfiredist Jan23-19:48) CAD:UNDER CONTROL;4529 PEEPLES RD; OAK
 (summerfieldfiredist Jun10-01:00) summerfieldfiredist\nCAD:UNDER CONTROL;5322 BUNCH RD; SU
 (summerfieldfiredist Jun10-19:27) summerfieldfiredist\nCAD:FD13;ST58;1;60C02;GAS;210 JAMES DOAK PKWY;BRIXHAM DR
 
 Contact: krpaschal@gmail.com <krpaschal@gmail.com>
 Sender: CAD@greensboro-nc.gov
 CAD@greensboro-nc.gov:CAD:CODE 47;7700 NEWSOME CT; B;CARDIAC / RESPIRATORY ARREST
 
 Contact: "Michael Capps" <firefightercapps@yahoo.com>
 FRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun11-00:23\nMSG:summerfieldfiredist\nCAD:UNDER CONTROL;210 JAMES DOAK PKWY; G\n
 1 of 2\nFRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun17-16:22\nMSG:summerfieldfiredist\nCAD:OPA4;OPA4;1;MEDEM;5570 US 220 N;M AND M\n(Con't) 2 of 2\nTIRE AND AUTOMOTIVE;SPOTSWOOD RD\n(End)
 FRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun17-14:57\nMSG:summerfieldfiredist\nCAD:SMFD;ST39;1;09E02;CARDIA;5718 OAK TREE RD;AIR HARBOR RD\n
 
 Contact: Greg Creech <gcreech1551@gmail.com>
 CAD:UNDER CONTROL;878 CROSSCREEK RD; K ;. [27]
 
Contact: Matt Young <myoung92939@gmail.com>
Subject:summerfieldfiredist Aug06-16:44\nsummerfieldfiredist \nCAD:SMFD;ST09;1;31D02;UNCON;7912 SUMMERFIELD RD;FAIRGROVE BAPTIST CHURCH; 
Subject:summerfieldfiredist Aug06-06:57\nsummerfieldfiredist \nCAD:OKRD;ST15;1;69D03;STRUC;8005 MARKETPLACE DR;TRACTOR SUPPLY CO;NC 68 N 

 */
 
 public class NCGuilfordCountyParser extends DispatchOSSIParser {
   
  private static final Pattern MARKER = Pattern.compile("^([\\w@\\-\\.]+?) *[\n:](?=CAD:)");
   
   public NCGuilfordCountyParser() {
     super("GUILFORD COUNTY", "NC",
            "( CALL2 ADDR! | SKIP MUTUAL ADDR! | INFO INFO INFO CODE? CALL ADDR! ) XINFO+");
   }
   
   @Override
   public String getFilter() {
     return "@edispatches.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (subject.length() > 0) {
       data.strSource = new Parser(subject).get(' ');
     }
     Matcher match = MARKER.matcher(body);
     if (match.find()) {
       data.strSource = match.group(1);
       body = body.substring(match.end()).trim();
     }
     if (!super.parseMsg(body, data)) return false;
     
     // If out of county mutual aid call, cancel the default county
     if (data.strCall.equals("MUTUAL")) data.defCity = "";
     return true;
   }
 
 
 
   private class Call2Field extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.length() < 6) return false;
       parse(field, data);
       return true;
     }
   }
   
   private class MutualField extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.equals("MUTUAL")) return false;
       parse(field, data);
       return true;
     }
   }
   
   private class CrossInfoField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       if (checkAddress(field) > 0) {
         data.strCross = append(data.strCross, " & ", field);
       } else {
         data.strSupp = append(data.strSupp, " / ", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "X INFO";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL2")) return new Call2Field();
     if (name.equals("MUTUAL")) return new MutualField();
     if (name.equals("XINFO")) return new CrossInfoField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
 }

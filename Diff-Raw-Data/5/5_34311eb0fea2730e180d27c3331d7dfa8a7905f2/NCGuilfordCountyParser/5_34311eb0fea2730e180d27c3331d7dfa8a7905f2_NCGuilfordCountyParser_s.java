 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Guilford County, NC
 Contact: Shane Taylor <dstaylor9@gmail.com>
Sender: @edsipatches.com 
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
 
 */
 
 public class NCGuilfordCountyParser extends DispatchOSSIParser {
   
   public NCGuilfordCountyParser() {
     super("GUILFORD COUNTY", "NC",
            "( CALL2 ADDR! | SKIP MUTUAL ADDR! | INFO INFO INFO CODE? CALL ADDR! ) X X");
   }
   
   @Override
   public String getFilter() {
    return "@edsipatches.com";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
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
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL2")) return new Call2Field();
     if (name.equals("MUTUAL")) return new MutualField();
     return super.getField(name);
   }
   
 }

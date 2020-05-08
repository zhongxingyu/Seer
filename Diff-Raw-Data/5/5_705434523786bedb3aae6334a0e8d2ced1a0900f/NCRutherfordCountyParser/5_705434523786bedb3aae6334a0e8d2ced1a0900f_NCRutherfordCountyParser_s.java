 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Rutherford County, NC
 Contact: Jeffrey Lynn <lynnj163@yahoo.com>
 Sender: paging@rutherfordcountync.gov
 
 Paging: Location= 159 WALKER DR*\nAPT/ROOM* City=Forest City* Call \nType= FIRE STRUCT* Units=SMFD1*\nApr24,3:05am
 Paging: Location= 557 US 221A HWY*\nAPT/ROOM* City=Forest City* Call \nType= FIRE POWERLN* Units=DUKE1, SMFD1*\nApr23,4:27am
 Paging: Location= 140 WALKER DR*\nAPT/ROOM* City=Forest City* Call \nType= FIRE ILLEGAL* Units=SMFD1*\nApr23,3:53am
 Paging: Location= 139 GREENE RD*\nAPT/ROOM* City=Forest City* Call \nType= FIRE STRUCT* Units=SMFD1*\nApr19,5:36pm
 Paging: Location= HIGH SHOALS CHURCH RD*\nAPT/ROOM* City=Cliffside* Call \nType= FIRE STRUCT* Units=CFD1, EFD1,SMFD1*\nApr18,10:50pm
 Paging: Location= OLD HENRIETTA RD & BYERS RD*\nAPT/ROOM* City=Ellenboro* Call \nType= STRANGE ODOR* Units=SMFD1*\nApr 25,10:59am
 Paging: Location= US221 S HWY & HARRIS HENRIETTA RD*\nAPT/ROOM* City=Forest City* Call \nType= ACCIDENT PI* Units=SMFD1,RCR1,EMS1*\nApr 25, 3:31pm
 
 */
 
 public class NCRutherfordCountyParser extends FieldProgramParser {
   
   public NCRutherfordCountyParser() {
     super("RUTHERFORD COUNTY", "NC",
           "Location:ADDR! City:CITY! Call_Type:CALL! Units:UNIT!");
   }
   
   @Override
   public String getFilter() {
     return "paging@rutherfordcountync.gov";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (!body.startsWith("Paging: ")) return false;
     body = body.substring(8).trim();
    body = body.replace('\n', ' ').replace('=', ':');
     return super.parseFields(body.split("\\*"), data);
   }
 }

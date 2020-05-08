 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Greene County NY (or Athens FD)
 Contact: 8456569957@mms.att.net
Sender: 200-200-0004
 GREENE 911:WIRES >WIRES DOWN / ARCING 117 RIVER RD ATHENS BRAUMULLER, HELENE Map: Grids:0,0 Cad: 2010-0000033052
 GREENE 911:ALARMF>FIRE ALARM 558 SLEEPY HOLLOW RD ATHENS ADT SECURITY-OP ANDREA- Map:62 Grids:0,0 Cad: 2010-0000033344
 
 Contact: Stephen Quick <stephen.d.quick@gmail.com>
 GREENE 911:SMOKE >SMOKE INVESTIGATION 2ND ST VENTE JOEAN Cad: 2010-0000034969
 GREENE 911:PIAA >MVA WITH INJURIES MARKET ST ATHENS BUCHAKIAN,DAVID Cad: 2010-0000033852
 GREENE 911:STRUCT>STRUCTURE FIRE 408 W BRIDGE ST CATSKILL LAIRD, JACQUELYNN Cad: 2010-0000034875
 */
 
 public class NYGreeneCountyParser extends DispatchBParser {
   
   private static final String[] CITY_CODES = new String[]{
     "ASHLAND", "ATHENS", "CAIRO", "CATSKILL", "COXSACKIE", "DURHAM", "GREENVILLE",
     "HALCOTT", "HUNTER", "JEFFERSON HEIGHTS", "JEWETT", "LEEDS", "LEXINGTON", "NEW BALITMORE",
     "PALENVILLE", "PRATTSVILLE", "TANNERSVILLE", "WINDHAM"};
 
   
   
   private static final String DEF_STATE = "NY";
   private static final String DEF_CITY = "GREENE COUNTY";
  
   public NYGreeneCountyParser() {
     super(CITY_CODES, DEF_CITY, DEF_STATE);
   }
   
   @Override
   public String getFilter() {
    return "200-200-0004";
   }
   
   @Override
   protected boolean isPageMsg(String body) {
     return body.startsWith("GREENE 911:");
   }
 }

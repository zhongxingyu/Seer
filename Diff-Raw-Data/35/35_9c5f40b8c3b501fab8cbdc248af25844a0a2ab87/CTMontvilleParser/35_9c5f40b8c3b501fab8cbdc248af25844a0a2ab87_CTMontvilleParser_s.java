 package net.anei.cadpage.parsers.CT;
 
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Montville, CT
 Contact: Jim fear <jimmyboy762@yahoo.com>
 Contact: William Betters <betters49@gmail.com>
 Sender: dispatch@montville-ct.org

 (Imc Solutions Page) MOTOR VEHICLE ACCIDENT\nCUMBERLAND FARMS\n863 NORWICH NEW LONDON TPKE \nMONTVILLE\nFire District: MFD1\n5/24/2012 18:17
 (Imc Solutions Page) STRUCTURE FIRE\nVILLAGE APARTMENTS, LLC\n82 JEROME RD \nMONTVILLE\nFire District: MFD1\n6/2/2012 16:36
 (Imc Solutions Page) ASSIST IN TOWN\r\nORCHARD GROVE SPECIALTY CARE\r\n5 RICHARD BROWN DR \r\nMONTVILLE\r\nFire District: MOH1\r\n6/6/2012 21:29
 (Imc Solutions Page) AMBULANCE EMERGENCY\r\nAZEVEDO, BRIAN\r\n9 SALTBOX CIR \r\nCOLD RESPONSE\r\nLEG PAIN / NUMBNESS
 (Imc Solutions Page) ASSIST IN TOWN\r\nGOSS, DAVID M.\r\n6 CRANDALL HILL RD APT A\r\nCOLD RESPONSE\r\nLEG PAIN / NUMBNESS
 (Imc Solutions Page) AMBULANCE EMERGENCY\r\nMARKHAM, ELIZABETH\r\n3 POLLY'S LN \r\nMONTVILLE\r\nFire District: MOH1\r\n7/28/2012 22:32
 
 */
 
 public class CTMontvilleParser extends FieldProgramParser {
   
   public CTMontvilleParser() {
     super(CITY_LIST, "MONTVILLE", "CT",
          "CALL PLACENAME ADDR! CITY? INFO+ Fire_District:UNIT DATETIME");
   }
 
   @Override
   public String getFilter() {
    return "dispatch@montville-ct.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("Imc Solutions Page")) return false;
     return parseFields(body.split("\r?\n"), 4, data);
   }
   
   private static final String[] CITY_LIST= new String[]{
     "MONTVILLE"
   };
 }

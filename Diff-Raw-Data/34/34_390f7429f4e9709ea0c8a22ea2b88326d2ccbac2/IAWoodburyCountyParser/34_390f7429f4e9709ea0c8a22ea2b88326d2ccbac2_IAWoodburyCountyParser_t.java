 package net.anei.cadpage.parsers;
 
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser.StartType;
 
 /*
  * J:FIRE ALARM/813 N DERBY LN NORTH SIOUX PORTIONABLES/ FREEDOM DR & DEVILSFOOD DR/NSIOUX/ 72B1 72EMS9 72E4 72E5 72F7 72Q6 72R8/ PORTIONABLES.... WATER FLOW ALARM
 J:FIRE SM VEHICLE/110 SODRAC DR NORTH SIOUX RED CARPET INN/ RIVER DR &/NSIOUX/ 72B1 72EMS9 72E4 72E5 72F7 72Q6 72R8/ TRUCK SMOKING LOOKS ON FIRE- NOBODY AROUND -IN FRONT OF INN
 J:STROKE/300 CENTENNIAL DR NORTH SIOUX ALORICA/ N MERRILL AVE & GATEWAY DR/NSIOUX/ 72EMS9/ 49YO FEMALE  - HAS HAD ONE IN THE PAST - USE MIDDLE DOORS
 J:CHOKING/215 WALLACE AVE NORTH SIOUX/ N MAIN ST & ROSE ST/NSIOUX/ 72EMS9
 J:TROUBLE BREATHING/800 STEVENS PORT DR DAKOTA DUNES/DDUNES/ 72EMS9/VIZIO BUSINESS. SMALLER BLDG, 1ST BLDG. BOTTOM SUITE
 J:SICK PERSON/24 NORTHSHORE DR NORTH SIOUX/STREETER DR & LEISURE LN/NSIOUX/ 72EMS9/FEMALE HAS BEEN ASLEEP FOR 15 HOURS AND CANT SEEM TO STAY
 J:EMS FOR PI/1 I29 NORTH SIOUX MM1 I29 SD/IOWA BORDER &/NSIOUX/ 72B1 72EMS9 72E4 72E5 72F7 72Q6 72R8/UNDERNEATH THE
 J:STROKE/300 CENTENNIAL DR NORTH SIOUX ALORICA/N MERRILL AVE & GATEWAY DR/NSIOUX/ 72EMS9/49YO FEMALE - HAS HAD ONE IN THE PAST - USE MIDDLE DOORS
 */
 
 
 public class IAWoodburyCountyParser extends SmartAddressParser {
   
   public IAWoodburyCountyParser() {
     super(CITY_LIST, "WOODBURY COUNTY", "IA");
   }
 
   private static final String[] CITY_LIST = new String[] {
     "NORTH SIOUX", "DAKOTA DUNES", "WYNSTONE", "ANTHON","BRONSON","CORRECTIONVILLE","CUSHING","DANBURY",
     "HORNICK","LAWTON","MOVILLE","OTO","PIERSON","SALIX","SERGEANT BLUFF","SIOUX CITY","SLOAN","SMITHLAND"
   };
   private static final String[] SDCITY_LIST = new String[] {
     "NORTH SIOUX", "DAKOTA DUNES", "WYNSTONE"
   };
   
   private boolean isPageMsg(String body) {
     return body.length() > 20 && body.contains("J:");
   } 
 
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (!isPageMsg(body)) return false;
     
     String[] lines = body.split("/");
     int ndx = 0;
    int iCrossShift = 0;
    
     for (String line : lines) {
       line = line.trim();
       switch (ndx++) {
       
       case 0:
         // Call description
         if (line.startsWith("J:")){
           data.strCall = line.substring(2);
         } else {
         data.strCall = line;
         }
         break;
         
       case 1:
         // Address line
         parseAddress(StartType.START_ADDR,line,data);
         break;
       
       case 2:
         // Cross Street. If only one street will end with & which we drop
        // If There is no cross. Move on.
        if (line.contains(" ")) {
          if (line.endsWith("&")){
            data.strCross = line.substring(0,line.length()-1).trim();
          } else {
            data.strCross = line;
          }
         } else {
          iCrossShift = 1;
         }
         break;
 
      case 3:
        if (iCrossShift ==1 ){
          data.strUnit = line;
        }
        break;
        
       case 4:
        // Unit or supp
        if (iCrossShift ==0 ){
         data.strUnit = line;
        } else {
          if (line.length() > 0) {
            if (data.strSupp.length() > 0) data.strSupp += " / ";
            data.strSupp += line;
          }
          ndx--;
        }
         break;
         
       case 5: 
         // supp info, repeats till end of msg
         if (line.length() > 0) {
           if (data.strSupp.length() > 0) data.strSupp += " / ";
           data.strSupp += line;
         }
         ndx--;
         break;
       }
       
     }
     return true;
   }
   
 
 }

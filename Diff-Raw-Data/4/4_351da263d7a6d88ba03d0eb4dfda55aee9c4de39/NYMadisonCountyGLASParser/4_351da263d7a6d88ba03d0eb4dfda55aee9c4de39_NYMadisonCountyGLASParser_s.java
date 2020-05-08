 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.MsgParser;
 
 
 /*
 Madison County - Greator Lenox Amulance Service (GLAS), NY
 Contact: "Kyle M. Cashel" <kcashel@gmail.com>
 Contact: "emtdebbie33@yahoo.com" <emtdebbie33@yahoo.com>
 Contact: Erick Haas <ericknjenn@gmail.com>
 Sender: messaging@iamresponding.com
 
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n7738 WISE RD , LENOX ( / N COURT)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Assist\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Heart Problem\n@MADISON COUNTY DSS (133 NORTH COURT ST (WAMPSVILLE VIL
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Convulsions/Seizures\n7216 NELSON RD , LENOX (SENECA TRNPK / PAVONE PL)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Chest Pain\n123 CAYUGA AV , SULLIVAN ( ONEIDA LAKE AV / ROUTE 31)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n7885 TACKABURY RD , LENOX ( DITCH BANK RD / INDIAN OPENING RD)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG: Chest Pain\n3881 COTTONS RD , LINCOLN ( CLOCKVILLE RD / NELSON RD)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Personal Injury\n@MM 261.7 (261 70 I90 )
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\nRAILROAD \ DEPOT (, CANASTOTA VILLAGE)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Personal Injury\n5050 BURLESON RD , LINCOLN ( VEDDER RD / FOREST AV)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Falls\n400 LAMB AV #144, CANASTOTA VILLAGE ( / DEPPOLITI AV)
 (Greater Lenox) Convulsions/Seizures\n7216 NELSON RD , LENOX ( SENECA TRNPK / PAVONE PL)
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\nCANAL RD , LENOX
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Chest Pain\n@THERMOLD & RMH CORPORATION (7059 HARP RD (LENOX) )
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Chest Pain\n@THERMOLD & RMH CORPORATION (7059 HARP RD (LENOX) )
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Traumatic Injuries\n@SCHOOL CANASTOTA ROBERTS ST JR SR HIGH SCHOOL (302 ROBERTS ST (CANASTOTA VILLAGE) )
 FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Psychiatric/Suicide Attempt\r\n403 S PETERBORO , CANASTOTA VILLAGE ( / RASBACH)
 
 */
 
 
 public class NYMadisonCountyGLASParser extends MsgParser {
   
   private static final Pattern MASTER = Pattern.compile("(.*?)\r?\n(.*?)(?: *, (.*?))?(?: \\((.*?)\\)?)?");
   
   public NYMadisonCountyGLASParser() {
     super("MADISON COUNTY", "NY");
   }
   
   @Override
   public String getFilter() {
     return "messaging@iamresponding.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("Greater Lenox")) return false;
     
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strCall = match.group(1).trim();
     String sPart1 = match.group(2).trim();
     String sPart2 = getOptGroup(match.group(3));
     String sPart3 = getOptGroup(match.group(4));
     if (sPart1.startsWith("@")) {
      data.strPlace = sPart1.substring(1).trim(); 
      if (sPart2.length() > 0) {
        parseAddress(sPart2, data);
        data.strCross = sPart3;
      } else {
        Parser p = new Parser(sPart3);
        parseAddress(p.get('('), data);
        data.strCity = p.get(')');
      }
     } else {
       parseAddress(sPart1, data);
       data.strCity = sPart2;
       if (sPart3.startsWith("/")) sPart3 = sPart3.substring(1).trim();
       if (sPart3.startsWith(",")) {
         data.strCity = sPart3.substring(1).trim();
       } else {
         data.strCross = sPart3;
       }
     }
     
     // Check for truncated VILLAGE following city
     int pt = data.strCity.lastIndexOf(' ');
     if (pt >= 0) {
       String last = data.strCity.substring(pt+1).trim().toUpperCase();
      for (String city : new String[]{"VILLAGE"}) {
         if (city.equals(last)) break;
         if (city.startsWith(last)) {
           data.strCity = data.strCity.substring(0,pt+1) + city;
           break;
         }
       }
     }
     return true;
   }
 }
 	

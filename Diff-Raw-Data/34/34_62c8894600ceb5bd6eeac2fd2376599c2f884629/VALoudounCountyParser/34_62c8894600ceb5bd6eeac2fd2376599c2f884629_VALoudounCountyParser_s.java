 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /***
 
 Call:12D02E-SEIZURES CONT,42914 OVERLY SQ-CH Apt:,X-St:NORRINGTON SQ KIR,A6092,Box:1908 ,ADC:5520 G06 [90]
 Call:17D03-FALL PATIENT N,42848 RECTORS CHASE WAY-AB Apt:,X-St:HOYSVILLE MANOR D,A6092 904 ACO9,Box:2618 ,ADC:5400 G03 [89]
 Call:20D01H-HEAT EXPOSURE,STONE SPRINGS BLVD-AL/MINERAL SPRINGS CIR-AL Apt:,X-St:GREENSTONE DR & M,A6092 9942 ACO9,Box:0910 ,ADC:5520 B02 [77]
 
 ([cad13] ) Call:30A01-TRAUMA NOT DAN,775 GATEWAY DR SE-LB Apt:CLUBHS,X-St:SYCOLIN RD SE VAN,M6132 ACO13,Box:2013 ,ADC:515
 ([cad13] ) Call:PUBR-PUBLIC SERVICE-,60 IDA LEE DR NW-LB Apt:,X-St:KING ST N  ,A6133 A6134 Z613,Box:0113 ,ADC:5157 K01 [2
 ([cad13] ) Call:ALS-ALS EMERGENCY   ,16591 COURAGE CT-LB Apt:,X-St:LOUDOUN CENTER PL,M6131 ACO13,Box:2024 ,ADC:5158 C08 [
 ***/
 
 public class VALoudounCountyParser extends SmsMsgParser {
 
  private static final String[]LCFRkeywords = new String[]{"Call:", "Apt:", "X-St:"};
   private static final Properties LCFRCityCodes = buildCodeTable(new String[]{
         "CH", "Chantilly",
         "LB", "Leesburg",
         "AL", "Aldie",
         "ST", "Sterling",
         "MB", "Middleburg",
         "AB", "Ashburn",
         "SP", "Sterling",
         "BL", "Bluemont",
         "CE", "Centreville",
         "HA", "Hamilton",
         "LV", "Lovettsville",
         "PA", "Paris",
         "PV", "Purceville",
         "PS", "Paeonian",
         "RH", "Round Hill",
         "UP", "Upperville",
         "FX19", "Fairfax",
         "FX11", "Fairfax",
         "FX", "Fairfax",
         "FQ", "Faquier"});
   
   public VALoudounCountyParser() {
     super("LOUDOUN COUNTY", "VA");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
    if (! isPageMsg(body, LCFRkeywords)) return false;
    
     // Clean off extra junk at both ends
    body = body.trim();
    if (body.charAt(0) == '(') {
      int pt = body.indexOf(')');
      if (pt >= 0) body = body.substring(pt+1).trim();
    }
    int pt = body.lastIndexOf('[');
     if (pt >= 0) body = body.substring(0, pt).trim();
 
     // Needs some massaging before it can be run through the standard parser
     body = body.replace(" Apt:", ",Apt:");
     Properties props = parseMessage(body, ",", new String[]{"Addr", "Unit"});
     data.strCall = props.getProperty("Call", "");
     parseAddressCity(props.getProperty("Addr", ""), data);
    data.strApt = props.getProperty("Apt", "");
    data.strCross = props.getProperty("X-St", "");
     data.strUnit = props.getProperty("Unit", "");
     data.strBox = props.getProperty("Box", "");
     data.strMap = props.getProperty("ADC", "");
     
     data.strCity = convertCodes(data.strCity, LCFRCityCodes);
     
     return true;
   }
 }

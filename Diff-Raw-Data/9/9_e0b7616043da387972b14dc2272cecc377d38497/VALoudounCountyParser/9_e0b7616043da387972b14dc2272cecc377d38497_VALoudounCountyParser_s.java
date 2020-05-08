 package net.anei.cadpage.parsers.VA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /***
 
 Call:12D02E-SEIZURES CONT,42914 OVERLY SQ-CH Apt:,X-St:NORRINGTON SQ KIR,A6092,Box:1908 ,ADC:5520 G06 [90]
 Call:17D03-FALL PATIENT N,42848 RECTORS CHASE WAY-AB Apt:,X-St:HOYSVILLE MANOR D,A6092 904 ACO9,Box:2618 ,ADC:5400 G03 [89]
 Call:20D01H-HEAT EXPOSURE,STONE SPRINGS BLVD-AL/MINERAL SPRINGS CIR-AL Apt:,X-St:GREENSTONE DR & M,A6092 9942 ACO9,Box:0910 ,ADC:5520 B02 [77]
 
 ([cad13] ) Call:30A01-TRAUMA NOT DAN,775 GATEWAY DR SE-LB Apt:CLUBHS,X-St:SYCOLIN RD SE VAN,M6132 ACO13,Box:2013 ,ADC:515
 ([cad13] ) Call:PUBR-PUBLIC SERVICE-,60 IDA LEE DR NW-LB Apt:,X-St:KING ST N  ,A6133 A6134 Z613,Box:0113 ,ADC:5157 K01 [2
 ([cad13] ) Call:ALS-ALS EMERGENCY   ,16591 COURAGE CT-LB Apt:,X-St:LOUDOUN CENTER PL,M6131 ACO13,Box:2024 ,ADC:5158 C08 [
 
 Contact: rebecca mckenna <emtco9516@gmail.com>
 bmckenna+caf_=5712385054=vtext.com@arcolavfd.org Call:31A01-FAINTED NOW AL,24801 PINEBROOK RD-CH Apt:110,X-St:TALL CEDARS PKWY ,A619 M6091 ACO19,Box:1921 ,ADC:5520 F01 [97]
 
 Contact: Ffightertl2 <ffightertl2@yahoo.com>
 Sender: CC_Message_Notification@usamobility.net
 1 of 2\nFRM:CC_Message_Notification@usamobility.net\nMSG:Call:CHIM-CHIMNEY FIRE,13396 BERLIN TPKE-LV Apt:,X-St:MILLTOWN CREEK RO,W612 ER602 TL602\n(Con't) 2 of 2\nK623 A6122 BC602 ECO12 ACO12,Box:1222 ADC:4922 G01 [14]\n\n-- \n[LC602](End)
 
From: Derrick Bennett <dbennett@arcolavfd.org>
 Sender: 6245
 CC_Message_Notification@usamobility.net Call:SF-STRUCTURE FIRE,1200 BLOCK OF DUNVEGAN DRIVE Apt:,X-St:,K604,Box:FQ   ,ADC: [42]\r\n\r\n-- \r\n[LC609]\r
 
 ***/
 
 public class VALoudounCountyParser extends MsgParser {
 
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
     
     // Clean off extra junk at both ends
     int pt = body.indexOf("Call:");
     if (pt < 0) return false;
     
     body = body.substring(pt);
     pt = body.lastIndexOf('[');
     if (pt >= 0) body = body.substring(0, pt).trim();
 
     // Needs some massaging before it can be run through the standard parser
     body = body.replace(" Apt:", ",Apt:");
     Properties props = parseMessage(body, ",", new String[]{"Addr", "Unit"});
     data.strCall = props.getProperty("Call", "");
     parseAddressCity(props.getProperty("Addr", ""), data);
     if (data.strAddress.length() == 0) return false;
     data.strApt = props.getProperty("Apt");
     if (data.strApt == null) return false;
     data.strCross = props.getProperty("X-St");
     if (data.strCross == null) return false;
     data.strUnit = props.getProperty("Unit", "");
     data.strBox = props.getProperty("Box", "");
     String sMap = props.getProperty("ADC", "");
     pt = sMap.indexOf('[');
     if (pt >= 0) sMap = sMap.substring(0,pt).trim();
     data.strMap = sMap;
     data.strCity = convertCodes(data.strCity, LCFRCityCodes);
     
     return true;
   }
 }

 package net.anei.cadpage.parsers.VA;
 
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*Prince William County, VA (class I)
 Contact: Rodney Fielding <rfielding@d-trs.com>
 Sender: cc_message_notification@usamobility.net
 
 29-Oct-2010/11:07:25/OUTF/15742 DUMFRIES RD / 15726 CARRS BROOKE WY ,PWC /17/E517 STA17 /ON NB  DUMFRIES RD  / BTW CARRS BROOK AND TODDBURY
 29-Oct-2010/10:38:09/FALARM/15750 NORRIS POINT WY ,PWC /23/E510B STA23 TW512 /LANDING AT MARKHAM  /// 703-580-8121 /  GENERAL ALARM [72]
 29-Oct-2010/09:41:32/INVEST/18911 FULLER HEIGHTS RD ,PWC /03F/E503B STA03F /CABLE LYING ON THE ROUND - BOTH ENDS ARE CONNECTED TO POLES - NO
 29-Oct-2010/02:17:37/SICA/17428 KAGERA DR ,PWC /03R/E503B M503 STA03R /DAD HAVING POSS HEART ATTACK/ [60]
 29-Oct-2010/09:21:10/UNC/18303 OLD TRIANGLE RD ,PWC /03R/E503B E523 M503 STA03R female screaming something is wrong w male in t he house .
 28-Oct-2010/18:35:32/OUTF/CARBOROUGH ST / 19140 WINDSOR RD ,PWC /03R/E503B STA03R /BRUSH ON FIRE BESIDES THE HIGHWAY [53]
 05-Nov-2010/10:08:33/FALARM/19043 FULLER HEIGHTS RD ,PWC (INSPIRATION > HOUSE DAY CARE)/03F/E503B STA03F TW512 /busn / 703 441 0997 / aud / ge
 
 Contact: support@active911.com
 Sender: @rsan.pwcgov.org
 
 Subject: Important message from PW-ALERT\n 22-May-2012/11:29:56/STOP/8961 JASMINE CT ,MNSS /01R/BC582 E501C M501 R501 STA01R /45 YOM\n \n Sent by PW Alert to M501, R501, E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/06:33:24/HAZARD/9230 NIKI PL ,MNSS /01R/E501C STA01R /water leaking from unit 201through bathroom ceiling of unit 101\n \n Sent by PW Alert to E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/07:52:18/INJA/9114 EUCLID AV ,MNSS /01F/E501C M501 STA01F /3P // FELL OFF LADDER // NOT SURE HOW FAR\n \n Sent by PW Alert to M501, E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/03:08:08/SWRES/11200 ADEN RD / 12210 NOKESVILLE RD ,PWC /05/A505B BC501X BC504Y E501C E507 M507 M525 STA05 TW525 Z507 Z507B ZS507 /OFC SEES VEH IN ALOT OF WATER POSS 2\n \n Sent by PW Alert to BC501X, BC504Y, E507, M507, M525, Z507, A505, C507, E501, T525, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/00:39:23/SWRES/12014 BALLS FORD RD / 12601 PRINCE WILLIAM PY ,PWC /11/A511B BC505 BC507F E501C E511B M511 Z503 Z517 /saab stuck w/caller in veh//\n \n Sent by PW Alert to BC505, BC507F, M511, Z503, Z517, A511, E501, E511, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/00:55:51/SWRES/11200 ADEN RD / 12210 NOKESVILLE RD ,PWC /05/A505B BC501X BC504Y E501C E525B M525 R510 TW525 Z507B /3p/703-895-0498-son armando stuck on roof of veh in h\n \n Sent by PW Alert to BC501X, BC504Y, M525, R510, A505, C507, E501, E525, T525, Z507, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/01:16:35/SWRES/12601 FOREMOST CT / 12522 ADEN RD ,PWC /05/5D A505B BC501X BC504Y E501C E505 M525 R506 STA05 Z507 /on foremost-veh is now in back yard off the creek whic\n \n Sent by PW Alert to BC501X, BC504Y, E505, M525, R506, Z507, A505, C507, E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/07:11:17/SWRES/GC BRISTOW MANOR @ 11507 VALLEY VIEW DR ,PWC /05/5E BC502 BC504 E501C E505 M525 SF504 SF505 STA05 T501 Z507 /sil bmw stuck in water....someone inside of\n \n Sent by PW Alert to BC502, BC504, E505, M525, SF504, SF505, T501, Z507, E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/06:29:45/SWRES/12298 LUCASVILLE RD / 12408 BRISTOW RD ,PWC /07/5E BC502 BC504 E505 E506B M525 STA07 T501 Z507B /MAN W/SUBMERGED VEHICLE//SITTING ON ROOF//APPROACHING BR\n \n Sent by PW Alert to BC502, BC504, E505, M525, T501, E506, Z507, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/07:11:17/SWRES/GC BRISTOW MANOR @ 11507 VALLEY VIEW DR ,PWC /05/5E BC502 E501C E505 M525 STA05 T501 Z507 /sil bmw stuck in water....someone inside of veh.....about foot\n \n Sent by PW Alert to BC502, E505, M525, T501, Z507, E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/06:29:45/SWRES/12298 LUCASVILLE RD / 12408 BRISTOW RD ,PWC /07/5E AC506 BC504 E505 E506B E507B M525 SF504 T501 Z507B /MAN W/SUBMERGED VEHICLE//SITTING ON ROOF//APPROACH\n \n Sent by PW Alert to AC506, BC504, E505, M525, SF504, T501, E506, E507, Z507, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 21-May-2012/21:11:17/ACCB/8800 LIBERIA AV / 8900 CENTREVILLE RD ,MNSS /01F/A511B E501C /bwt the nissan and subura car dealership-motor vehicle accident // poss 2 vehicles // one ve\n \n Sent by PW Alert to A511, E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/06:29:45/SWRES/12298 LUCASVILLE RD / 12408 BRISTOW RD ,PWC /07/BC502 BC504 E505 E506B M525 STA07 T501 Z507B Z517 /MAN W/SUBMERGED VEHICLE//SITTING ON ROOF//APPROACHING\n \n Sent by PW Alert to BC502, BC504, E505, M525, T501, Z517, E506, Z507, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/07:31:41/SICB/8691 PHOENIX DR ,MNSS (SUPER 8 MOTEL)/01F/E501C M509B STA01F /PHONE EXT 124//STOMACH PAINS//\n \n Sent by PW Alert to E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 21-May-2012/23:16:48/SICA/9706 ASPEN PL ,MNSS /01F/E501C M501D /42 YO MALE // IS CONS // VOMITTING BLOOD//\n \n Sent by PW Alert to E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 21-May-2012/23:19:48/FALARM/8411 VIRGINIA MEADOWS DR ,PWC (WELLINGTON WASTEWATER TRMNT)/25/E501C E507B TW525 /busn; craft food 571-921-1400//duct alrm//will notify//\n \n Sent by PW Alert to E501, E507, T525, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 21-May-2012/23:44:15/HAZARD/9503 PARK ST ,MNSS /01R/E501C /water coming in thru the ceiling//\n \n Sent by PW Alert to E501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/07:11:17/SWRES/GC BRISTOW MANOR @ 11507 VALLEY VIEW DR ,PWC /05/5E BC502 E501C E505 M525 STA05 T501 Z507 /sil bmw stuck in water....someone inside of veh.....about foot\n \n Sent by PW Alert to T501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/08:21:30/FALARM/125 MARKET ST ,MNPK (CITY CENTER RES & COMM)/09/E509 STA09 T501 /COMM  CITY CENTER APTMENT...703-330-0540  AUD PULL STATION WILL ATC\n \n Sent by PW Alert to T501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 22-May-2012/06:29:45/SWRES/12298 LUCASVILLE RD / 12408 BRISTOW RD ,PWC /07/5E AC506 BC502 BC504 E505 E506B E507B M525 STA07 T501 Z507B /MAN W/SUBMERGED VEHICLE//SITTING ON ROOF//AP\n \n Sent by PW Alert to AC506, BC502, BC504, E505, M525, T501, E506, E507, Z507, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 Subject: Important message from PW-ALERT\n 21-May-2012/19:10:42/FALARM/170 MARKET ST ,MNPK (CITY CENTER RES & COMM)/09/E509 T501 /COMM/ 703 330 0540/ AUD-GENERAL PULL STATION / WILL ATC\n \n Sent by PW Alert to T501, DFR CAD through Prince William County RSAN\n \n --\n \n - update your account at https://rsan1.rsan.pwcgov.org
 
 */
 
 
 public class VAPrinceWilliamCountyParser extends FieldProgramParser {
   
   public VAPrinceWilliamCountyParser() {
     super(CITY_CODES, "PRINCE WILLIAM COUNTY", "VA",
           "DATE TIME CODE ADDR X/Z+? BOX UNIT! INFO+");
   }
 
   @Override
   public String getFilter() {
     return "cc_message_notification@usamobility.net,@rsan.pwcgov.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
    return parseFields(body.split("/"), 6, data);
   }
   
   private static final DateFormat DATE_FMT = new SimpleDateFormat("dd-MMM-yyyy");
   private class MyDateField extends DateField {
     @Override
     public void parse(String field, Data data) {
       setDate(DATE_FMT, field, data);
     }
   }
   
   private class MyTimeField extends TimeField {
     public MyTimeField() {
       setPattern("\\d\\d:\\d\\d:\\d\\d", true);
     }
   }
   
   private class MyCodeField extends CodeField {
     @Override
     public void parse(String field, Data data) {
       data.strCode = field;
       data.strCall = convertCodes(field, CALL_CODES);
     }
     
     @Override
     public String getFieldNames() {
       return "CODE CALL";
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       String sPlace = append(p.getOptional('@'), " - ", p.getLastOptional('('));
       if (sPlace.endsWith(")")) sPlace = sPlace.substring(0, sPlace.length()-1).trim();
       data.strPlace = sPlace;
       data.strCity = convertCodes(p.getLastOptional(','), CITY_CODES);
       parseAddress(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " CITY PLACE";
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(',');
       if (pt >= 0) {
         if (data.strCity.length() == 0) data.strCity = convertCodes(field.substring(pt+1).trim(), CITY_CODES);
         field = field.substring(0,pt).trim();
       }
       
       // very occasionally, the address will be a cross street and the
       // cross street will be an address
       if (data.strCross.length() == 0 && field.length() > 0 && Character.isDigit(field.charAt(0)) &&
           (data.strAddress.length() == 0 || !Character.isDigit(data.strAddress.charAt(0)))) {
         data.strCross = data.strAddress;
         data.strAddress = "";
         parseAddress(field, data);
       } else {
         super.parse(field, data);
       }
     }
   }
   
   private class MyBoxField extends BoxField {
     public MyBoxField() {
       setPattern("\\d\\d[A-Z]?", true);
     }
   }
   
   private static final Pattern ID_PTN = Pattern.compile("\\[(\\d+)]?$");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = ID_PTN.matcher(field);
       if (match.find()) {
         data.strCallId = match.group(1);
         field = field.substring(0,match.start()).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "INFO ID";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("DATE")) return new MyDateField();
     if (name.equals("TIME")) return new MyTimeField();
     if (name.equals("CODE")) return new MyCodeField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("BOX")) return new MyBoxField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "MNPK", "MANASSAS PARK",
       "MNSS", "MANASSAS",
       "PWC",  ""
   });
   
   private static final Properties CALL_CODES = buildCodeTable(new String[]{
     "ACCA",   "Auto Accident ALS",
     "ACCB",   "Auto Accident BLS",
     "ACCX",   "Auto Accident with Entrapment",
     "APPA",   "Appliance Fire in an Apartment",
     "APPH",   "Appliance Fire in an House",
     "APPT",   "Appliance Fire in an Townhouse",
     "APT",    "Apartment Fire",
     "ASSA",   "Assault - ALS",
     "ASSB",   "Assault - BLS",
     "BLDGA",  "Building Collapse in a Apartment",
     "BLDGC",  "Building Collapse in a Commercial Building",
     "BLDGH",  "Building Collapse in a House ",
     "BLDGT",  "Building Collapse in a Townhouse",
     "BRUSH",  "Brush Fire",
     "CO",     "Carbon Monoxide Leak",
     "COMM",   "Commercial Fire",
     "FALARM", "Fire Alarm",
     "HAZARD", "Hazard",
     "HOUSE",  "House Fire",
     "INGASA", "Inside Gas Leak in a Apartment",
     "INGASC", "Inside Gas Leak in a Commercial Building",
     "INGAST", "Inside Gas Leak in a Townhouse",
     "INGASH", "Inside Gas Leak in a House",
     "INJB",   "Injury - BLS",
     "INJA",   "Injury - ALS",
     "INVEST", "Investigation",
     "ODA",    "Overdose ALS",
     "ODB",    "Overdose BLS",
     "OUTF",   "Outside Fire",
     "OUTGAS", "Outside Gas Leak",
     "RESCUE", "Rescue",
     "SICA",   "Sickness - ALS",
     "SICB",   "Sickness - BLS",
     "STOP",   "Stoppage Of Breathing",
     "SWRES",  "Swift Water Rescue",
     "TH",     "Townhouse Fire",
     "TRUCK", "Truck Fire",
     "UNC",    "Unconscious",
     "VEH", "Vehicle Fire"
 
   });
 }

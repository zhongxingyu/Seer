 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Putnam County, NY 
 Contact: mike miller <eightytwo247@gmail.com>
 Sender: 911@putnamcountyny.gov,messaging@iamresponding.com
 STRUCTURE FIRE/RESIDENTIAL||34 MOUNTAIN BROOK DR  STA 11 XS MEMORIAL DR/LEONA DR|NARR WPH1-E
 (CAD) CHEST PAIN||35 OAK ST  STA 11 XS HOYT  ST/MERRITT ST|NARR PROQA SUMMARY:DELTA 10D01 CHEST PAIN PATIENT WITH CHEST PAIN. 55-YEAR-OLD  FEMALE, CONSCIOUS
 AUTOMATIC FIRE ALARM|METRO NORTH MECHANICAL SHOP|PROSPECT HILL RD  STA 11 XS  |NARR GENERAL
 TRAFFIC/TRANSPORTATION ACCIDEN||I 684  N STA 11 XS I-84  E|NARR MVA W/ INJURIES- FEMALE SUBJECT COMPLAINING OF NECK AND BACK PAIN
 TRAFFIC/TRANSPORTATION ACCIDEN||92 FOGGINTOWN RD  STA 11 XS FARM TO MARKET RD/HAWKS LN|NARR 1 CAR PIAA/UNK INJ PERSON: (COMPLAINANT) (FMLS) CHRISTINE
 .... (11 Brewster) MEDIC ALERT||4100 STONECREST DR  STA 11 XS
 (2 Carmel) AUTOMATIC FIRE ALARM|CARMEL HIGH SCHOOL|30 FAIR ST  STA 12 XS VINK  DR/COUNTY CTR|NARR 845 225-8352/GE'L FIRE PERSON: (COMPLAINANT) (FMLS)
 
 Contact: brandon timmins <btimmins33@gmail.com>
 .... (18 Mahopac) PSYCHIATRIC/ABNORMAL BEHAVIOR||151 W SHORE DR  STA 18 XS FARVIEW  RD/STEBBINS RD|NARR MALE EDP, SCENE SECURE..
 
 Contact: "smadsen70@aol.com" <smadsen70@aol.com>
 Sender: 777119178284
 .... (17 Lake Carmel) BREATHING PROBLEMS||78 DREW RD  STA 17 XS  |NARR PROQA SUMMARY:CHARLIE 06C01 BREATHING PROBLEMS PATIENT WITH BREATHING PROBLEMS.  93-YEAR-OLD MALE, CONSCIOUS AND BREATHING.
 .... (17 Lake Carmel) UNCONSCIOUS/FAINTING (NEAR)||18 STANLEY CT  STA 17 XS DEAD END /MINELLO DR|NARR PROQA SUMMARY:ALPHA 31A01 FALLS PATIENT IS UNCONSCIOUS (OR HAS FAINTED). THE PATIENT  IS A 71-YEAR-OLD MALE, CONSCIOUS AND BREATHING.
 
 Contact: Dan Venezia <danrango@gmail.com>
 Sender: 777166991580
 .... (31 Carmel VAC) TRANSFER/INTERFACILITY|PHC| 670 STONELEIGH AV,CARMEL |STA 12 XS VISTA ON THE LAKE /ALEXANDRA CT
 
 Station numbers FYI
 11 Brewster
 12 Carmel
 13 Cold Spring
 14 Continental Village
 15 Garrison
 16 Kent
 17 Lake Carmel
 18 Mahopac
 19 Mahopac Falls
 20 Nelsonville *
 21 North Highlands
 22 Patterson
 23 Putnam Lake
 24 Putnam Valley
 31 Carmel Ambulance
 32 Garrison Ambulance
 33 Philipstown Ambulance
 34 Putnam Valley Ambulance
  
  */
 
 public class NYPutnamCountyParser extends MsgParser {
   
   private static final Pattern STA_MARKER = Pattern.compile("[\\| ]STA ");
 
 
   public NYPutnamCountyParser() {
     super("PUTNAM COUNTY", "NY");
   }
   
 
 @Override
   public String getFilter() {
     return "911@putnamcountyny.gov,messaging@iamresponding.com,777";
   }
 
 @Override
   protected boolean parseMsg(String body, Data data) {
   
     Matcher match = STA_MARKER.matcher(body);
     if (!match.find()) return false;
     String sPart1 = body.substring(0,match.start()).trim();
     String sPart2 = body.substring(match.end()).trim();
     
     Parser p = new Parser(sPart1);
     data.strCall = p.get('|');
     data.strPlace = p.get('|');
     parseAddress(p.get(','), data);
    data.strCity = p.get();
     p = new Parser(sPart2);
     data.strSource = p.get(' ');
     data.strCross = p.get('|');
     if (data.strCross.equals("XS")) data.strCross = "";
     if (data.strCross.startsWith("XS ")) data.strCross = data.strCross.substring(3).trim();
     data.strSupp = p.get();
     if (data.strSupp.startsWith("NARR ")) data.strSupp = data.strSupp.substring(5).trim();
     return true;
   }
   
 }

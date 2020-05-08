 package net.anei.cadpage.parsers.WI;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Kenosha County, WI
 Contact:    David Wilkinson <firemedic53142@gmail.com>
 Sender: @kccjs.org
 
 LMC213 #:002011070865 ILS MED at 7701 95TH ST Rem: 50 M ;HEART PROBLEM CONSCIOUS: YES ,BREATHING: YES
 NLB260 #:002011071148 ALS MED at 4204 109TH ST Rem: 69 F ;CHEST PAIN CONSCIOUS: YES ,BREATHING: YES
 DMB193 #:002011072020 BLS MED at 10519 120TH AVE EAST TOURIST INFO CNTR Rem: CHEST PAIN
 DMB193 #:002011072054 SUPERVISOR ALRM at 7887 94TH AVE HOLIDAY INN EXPRESS
 DMB193 #:002011072060 MVA at 80TH AVE and COOPER RD
 
 Contact: "ksgrate@yahoo.com" <ksgrate@yahoo.com>
 From: CNT304 #:002011086007 ALS MED at 835 HICKORY RD Rem: 53 F ;SEIZURES CONSCIOUS: YES ,BREATHING: YES\37s
 
 Contact: Jeremy <Sprint43@yahoo.com>
 From: JDR315 #:002011089429 ALS MED at 21922 121ST ST Rem: 64 F ;HEART PROBLEM CONSCIOUS: YES ,BREATHING: YES
 
 Contact: "ksgrate@yahoo.com" <ksgrate@yahoo.com>
 From: SRZ272 #:002011089666 ILS MED at 1747 WILMOT AVE Rem: 15MONTH M ;SICK UNKNOWN CONSCIOUS: YES ,BREATHING: YES
 From: LMC213 #:002011088748 BLS MED at 34100 GENEVA RD NEW MUNSTER Rem: 60 M ;TRAUMA W/INJURY CONSCIOUS: YES ,BREATHING: YES
 From: KAL296 #:002011087809 ALS MED at 9130 402ND AVE Rem: 13 F CONSCIOUS: NO ,BREATHING: YES
 From: UAS203 #:002011089931 ALS MED at 5516 353RD AVE Rem: 50 F CONSCIOUS: NO ,BREATHING: YES
 From: UAS203 #:002011089931 ALS MED at 5516 353RD AVE Rem: 50 F CONSCIOUS: NO ,BREATHING: YES\50s
 
 Contact: Henry Will <hmwill55@gmail.com>
 Sender: call2@kccjs.org
 From: DMC105 #:002011098714 SINGLE ENGINE at 11252 254TH CT SALEM HIGHWAY DEPT ( Rem: LARGE BRUSH FIRE BEHIND BLDG APPEARS UNATTENDED
 
Contact: Bill Carey <bicar1204@gmail.com>
Sender: unknown@kccjs.org
From: RR114  #:002011105103 ALS MED at 9739 269TH AVE Rem: 67 F ;BREATHING DIFFICULTY CONSCIOUS: YES ,BREATHING: YES

 */
 
 
 public class WIKenoshaCountyParser extends SmartAddressParser {
   
  private static final Pattern MARKER = Pattern.compile("^(?:From: )?[A-Z]{2,3}\\d{3} +#:");
   
   public WIKenoshaCountyParser() {
     super("KENOSHA COUNTY", "WI");
   }
   
   @Override
   public String getFilter() {
     return "@kccjs.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     
     Parser p = new Parser(body);
     data.strCallId = p.get(' ');
     data.strCall = p.get(" at ");
     parseAddress(StartType.START_ADDR, p.get(" Rem: "), data);
     data.strPlace = getLeft();
     if (data.strAddress.length() == 0) return false;
     data.strSupp = p.get();
     return true;
   }
 }

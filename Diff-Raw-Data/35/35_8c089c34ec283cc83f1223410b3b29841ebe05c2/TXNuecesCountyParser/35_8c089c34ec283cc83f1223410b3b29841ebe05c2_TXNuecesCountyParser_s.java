 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /* 
 Nueces County, TX
 Contact: Mike Clack <chief@annavillevfd.com>
 Sender: ccpdpaging@cctexas.com<mailto:ccpdpaging@cctexas.com
 cc = corpus christi
 neucs = neuces
 
 Subject:MetroCom Notification\nMEDICAL - UNCON, ALRM: 0, PRI: 1, ESZ: 67 / 2141 TULOSO RD CC NUECS: @SUNTIDE FEDERAL CU- TULOSO RD
 Subject:MetorCom Notifiction\nAlarm – COMM-SML, ALRM: 0, PRI: 2, ESZ: 107 / 16318 fm 624 NUECS: @RIVER HILLS BASPTIST CHURCH
 Subject:MetorCom Notifiction\nMEDICAL – ALLERGY, ALRM: 0, PRI: 2, ESZ: 2009 / 11559 LEOPARD ST CC NUECS: @CALALLEN MINOR EMERGENCY CLINI
 Subject:MetorCom Notifiction\nFIRE-OTHER – GRASS, ALRM: 0, PRI: 1, ESZ 107 / HWY 77 FWY SB/CR 48 NUECS
 Subject:MetorCom Notifiction\nMEDICAL – CHEST, ALRM: 0, PRI: 1, 104 / FM 43/FM 763 NUECS
 
  */
 
 public class TXNuecesCountyParser extends SmsMsgParser {
   
   public TXNuecesCountyParser() {
     super("NUECES COUNTY", "TX");
   }
   
   @Override
   public String getFilter() {
     return "ccpdpaging@cctexas.com";
   }
   
   private static final Pattern SUBJECT_PTN = 
     Pattern.compile("Met[or]{2}Com Notifica?tion");
   private static final Pattern MASTER = 
     Pattern.compile("([^,]+), ALRM: ., PRI: (.), (?:ESZ:? )?(\\d+) / ([^:]+)(?:: @(.*))?");
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!SUBJECT_PTN.matcher(subject).matches()) return false;
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     
     data.strCall = match.group(1).trim();
     data.strPriority = match.group(2).trim();
     data.strUnit = match.group(3).trim();
     String sAddr = match.group(4).trim();
     data.strPlace = getOptGroup(match.group(5));
     
     if (sAddr.endsWith(" NUECS")) sAddr = sAddr.substring(0, sAddr.length()-6).trim();
     if (sAddr.endsWith(" CC")) {
       sAddr = sAddr.substring(0, sAddr.length()-3).trim();
       data.strCity = "CORPUS CHRISTI";
     }
     parseAddress(sAddr, data);
     return true;
   }
 }

 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /******************************************************************************
 Centreville, MD Sample messages
 Contact: Contact: David baker <baker.davidm@gmail.com> ?
 qac911:*D 4-2 COMMERCIAL BLDG FIRE DUNKIN DONUTS @500 CHURCH HILL RD COMMERCIAL BOX Q04
 qac911:*D 4-5 MVC W/ENTRAPMENT RT301NB / RT304 RESCUE BOX Q04
 qac911:*D 5-1 LG BRUSH/GRASS FIRE RT405 / WINDSWEPT FARM LN BRUSH BOX Q05
 qac911:*D 4-5 UNCONSCIOUS/FAINTING RT301NB / ROLLING BRIDGE RD UNCONSCIOUS Q04
 qac911:*D 5-2 MVC W/ROLLOVER RT301NB / HAYDEN RD RESCUE BOX Q05
 qac911:*D 4-7 MVC W/ROLLOVER 1627 RUTHSBURG RD RESCUE BOX Q04
 (text message) qac911:*D 4-2 CO ALARM 212 NORTHFIELD WAY LOCAL BOX Q04
 
 Contact: david baker <baker.davidm@gmail.com>
 sender:QA911com@qac.org <From%3AQA911com@qac.org>
 (Text Message) QA911com:*D 4-1 UNKNOWN PROBLEM 206 ORCHESTRA PL LIFE STATUS ? Q04
 
 Contact: Joshua Ruby <josh@computerrescuemd.com>
 sender: QA911com@qac.org
 (Text Message) QA911com:*D 1-14 FALLS 1401 CALVERT RD POSS DANG BODY AREA Q01
 
 ******************************************************************************/
 
 public class MDCentrevilleParser extends SmartAddressParser {
   
   private static final String DEF_STATE = "MD";
   private static final String DEF_CITY = "CENTREVILLE";
   
  private static final Pattern MARKER = Pattern.compile("^(qac911|QA911com):\\*D ");
   
   public MDCentrevilleParser() {
     super(DEF_CITY, DEF_STATE);
   }
   
   @Override
   public String getFilter() {
     return "qac911@qac.org,QA911com@qac.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     Matcher match = MARKER.matcher(body);
     if (! match.find()) return false;
     body = body.substring(match.end());
     
     // OK, go do your magic!!
     parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, body, data);
     data.strSupp = getLeft();
     return true;
   }
 }

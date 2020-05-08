 package net.anei.cadpage.parsers.KS;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Butler County, KS
 Contact: Brandon Russell <brandon.russell11@gmail.com>
 Sender: dispatch@bucoks.com
 System: New World
 he actual communication center is located in El
 Doardo, KS. Other cities in the county include Augusta, Andover, Rose Hill,
 Benton, Towanda, Leon, Douglass, Latham and Cassoday, KS.
 
Old text formats, before they swapped (MAP PAGE) and (ADDRESS)
 [Incident Notification]  17D FALL (MAP PAGE): 26E (ADDRESS): 1611 N FAIRWAY DR Augusta (CROSS ST): N CLUB HOUSE DR / E FAIRWAY CT (NARR):  ProQA Medical Questionnai
 [Incident Notification]  6D DIFF BREATHING (MAP PAGE): 22C (ADDRESS): 524 S EMPORIA ST El Dorado (CROSS ST): W CAVE SPRINGS AVE / W LOCUST AVE (NARR): ProQA Medica
 (Incident Notification) 10D CHEST PAIN (MAP PAGE): 9 (ADDRESS): 5957 NE 80TH ST County (CROSS ST): NE ELLIS RD / NE HWY 177 (NARR):  ProQA Medical Questionnaire Co
 (Incident Notification) 6E ECHO DIFF BREATH (MAP PAGE): 36F (ADDRESS): 218 E ROSEWOOD ST Rose Hill (CROSS ST): N MAIN ST / N MEEKER CT (NARR):  a  as fd3  es fd3
 (Incident Notification) 31D UNCONCIOUS (MAP PAGE): 15 (ADDRESS): 955 N HAVERHILL RD El Dorado (CROSS ST): W PIONEER DR / W 6TH AVE (NARR):  acting alittle confused
 [Incident Notification]  13D DIABETIC EMERGENCY (MAP PAGE): 25B (ADDRESS): 726 N PROSPERITY LN Andover (CROSS ST): W ELLEN LN / DEAD END (NARR):  rp
 
New text format
(Incident Notification) 10D CHEST PAIN (ADDRESS): 300 N MAIN ST El Dorado (MAP PAGE): 22C (CROSS ST): E 2ND AVE, W 2ND AVE / E 3RD AVE, W 3RD AVE (NARR):  ProQA Me

 */
 public class KSButlerCountyParser extends FieldProgramParser {
   
   private static final String DEF_CITY = "BUTLER COUNTY";
   private static final String DEF_STATE = "KS";
   
   private static final String[] CITY_LIST = new String[]{
     "EL DORADO", "ANDOVER", "AUGUSTA", "ROSE HILL", "DOUGLASS", "TOWANDA", 
     "BENTON", "LEON", "WHITEWATER", "POTWIN", "ELBING", "LATHAM", "CASSODAY",
     "BEAUMONT", "BOIS D'ARC", "BRAINERD", "CHELSEA", "DEGRAFF","GORDON", 
     "HAVERFILL", "HOPKINS", "KEIGHLEY", "LORENA", "PONTIAC", "PROSPECT", 
     "PROVIDENCE", "ROSALIA", "SALTER", "SMILEYBERG", "VANORA"
   };
   
   private static final Pattern PARENS = Pattern.compile("[\\(\\)]");
 
   public KSButlerCountyParser() {
     super(CITY_LIST, DEF_CITY, DEF_STATE,
           "CALL! ADDRESS:ADDR/S! MAP_PAGE:MAP CROSS_ST:X NARR:INFO");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@bucoks.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.contains("Incident Notification")) return false;
     body = PARENS.matcher(body).replaceAll("");
     return super.parseMsg(body, data);
   }
 }

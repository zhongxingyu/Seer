 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Nassau County, NY (Firetracker)
 Contact: peter Griebel <griebz217@yahoo.com>
 Sender: dispatch@firetracker.net
 
 (FirePage) ** FFD FIRE CALL ** GENERAL PROB WITH GAS STOVE-POSS LEAK 170 JAY ST\nC/S: DEAN (E) ST / MILTON (E) ST TOA: 17:02 [FireTracker]
 (FirePage) ** FFD FIRE CALL ** DISTRIC DIST CALL FOR SMOKE IN THE AREA CHURCH ST\nC/S: ARCHER ST TOA: 19:12 [FireTracker]
 (FirePage) ** FFD FIRE CALL ** GENERAL APT 72 / 3RD FLOOR / SMOKE 100 N MAIN ST\nC/S: RANDALL AVE / GRAND AVE TOA: 18:50 [FireTracker]
 (FirePage) ** FFD FIRE CALL ** STILL WILLOWBROOK LA C/S: BROOKSIDE (N) AVE TOA:\n09:16 [FireTracker]
 (FirePage) ** FFD FIRE CALL ** GENERAL ELEC PROBLEM/APT 415 45 WALLACE ST C/S:\nBROOKLYN AVE / RANDALL AVE TOA: 18:26 [FireTracker]
 (FirePage) ** FFD FIRE CALL ** CARBON APT 33 116 WEST END AVE C/S: RAY ST / ELINOR\nPL TOA: 11:23 [FireTracker]
 (FirePage) ** FFD FIRE CALL ** GENERAL 22 PEARSALL AVE C/S: LONG BEACH (N) AVE /\nPENNSYLVANIA AVE TOA: 15:56 [FireTracker]
 
 Contact: Louis Sabatino <lousab1@optonline.net>
 Contact: Mike Torregrossa <torr393@gmail.com>
 Sender: dispatch@firetracker.net
 (FirePage) *FSMFD* 4/22/2011 TOA:20:47 AMBU [AMBU] 178 RINTIN ST BENRIS AVE ARM\nINJURY [FireTracker]
 (FirePage) *FSMFD* 4/27/2011 TOA:21:12 AMBU [AMBU] 820 FRANKLIN PL GARDEN CITY ROAD\nDIZZY FEMALE VOMITING [FireTracker]
 (FirePage) *FSMFD* 4/28/2011 TOA:14:59 AMBU [AMBU] 1150 HEMPSTEAD TPK RENKEN BLVD\nIN THE SICK BAY SICK FEMALE [FireTracker]
 (FirePage) *FSMFD* 4/28/2011 TOA:19:45 AMBU [AMBU] 947 MAPLE DR COURT HOUSE ROAD\nMALE,SHORT OF BREATH [FireTracker]
 (FirePage) *FSMFD* 4/28/2011 TOA:22:16 AMBU [AMBU] 1031 MOSEFAN ST WILLIAM AVE\nFEMALE FELL,WRIST INJURY [FireTracker]
 [FirePage]  *FSMFD* 4/28/2011 TOA:22:16 AMBU [AMBU] 1031 MOSEFAN ST WILLIAM AVE FEMALE FELL,WRIST INJURY [FireTracker]
 (FirePage) *FSMFD* 4/28/2011 TOA:23:14 CARB 1 [CARB] 551 CHESMAN ST NASSAU BLVD NO\nAIDED [FireTracker]
 (FirePage) *FSMFD* 4/29/2011 TOA:19:21 AMBU [AMBU] 775 CORNELL ROAD ETON ROAD MALE\nFELL-HIP INJURY [FireTracker]
 (FirePage) *FSMFD* 4/29/2011 TOA:21:59 MUTUAID [MAF] 125 MEACHAM AVE POST AVE\n[FireTracker]
 
(FirePage) FSMFD 5/2/2011 TOA:17:36 AMBU [AMBU] 766 WILLOW ROAD BIRCH ST BROKEN\nHIP [FireTracker]

 */
 public class NYNassauCountyFiretrackerParser extends FieldProgramParser {
 
   public NYNassauCountyFiretrackerParser() {
     super("NASSAU COUNTY", "NY", 
            "ADDR/SC! CS:X TOA:SKIP");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@firetracker.net";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("FirePage")) return false;
     if (!body.endsWith("[FireTracker]")) return false;
     body = body.substring(0,body.length()-13).trim();
     if (body.startsWith("** FFD FIRE CALL ** ")) {
       data.strSource = "FFD";
       body = body.substring(20).trim();
       body = body.replace('\n', ' ').replace("C/S:", "CS:").replace(" LA ", " LN ");
       return super.parseMsg(body, data);
     }
     
    if (body.startsWith("*FSMFD* ") || body.startsWith("FSMFD")) {
       data.strSource = "FSMFD";
       Parser p = new Parser(body);
       p.get("TOA:");
       p.get(" ");
       data.strCall = p.get('[');
       p.get(']');
       body = p.get();
       if (body.length() == 0) return false;
       body = body.replace('\n', ' ');
       parseAddress(StartType.START_ADDR, body, data);
       body = getLeft();
       Result res = parseAddress(StartType.START_ADDR, body);
       if (res.getStatus() > 0) {
         res.getCrossData(data);
         body = res.getLeft();
       }
       data.strSupp = body;
       return true;
       
     }
     return false;
   }
   
   private static final Pattern APT_PTN = Pattern.compile("\\bAPT ([^ ]+)\\b");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = APT_PTN.matcher(field);
       if (match.find()) {
         data.strApt = match.group(1);
         field = field.substring(0,match.start()) + field.substring(match.end());
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
 }

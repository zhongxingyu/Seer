 package net.anei.cadpage.parsers.NJ;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 
 /*
 Morris County, NJ
 Contact: Lee Bender <leebender@morrisminutemen.org>
 Sender:  Dispatch@co.morris.nj.us
 System: Keystone
 
 PORZIO BROMBERG & NEWMAN (22) , 100 SOUTHGATE PKY [Morris Twp] (SICK PERSN) - SERIOUS PAIN IN LEGS... UNABLE TO WALK... - E2368 17:14
 4 JARDINE CT [Morris Twp] (UNCONSCN) - 84 YOM UNCONCIOUS IS BREATHING. - E2368 15:37
 340 SPEEDWELL AVE [Morris Plains] (BREATHING) - MORISM FAMILY MEDICINE ASSOC. DR. JOSEPH OFFICE 50 YOF DIFF BREATHING IS ON OXYGEN FULL TIME - 2398 11:25
 26 CLEVELAND AVE [Morris Twp] (SICK PERSN) - 47 YOF W/A FOOT INJURY - E2366 07:57
 MORRIS VIEW NURSING HOME (22) , 540 W HANOVER AVE BLDG MORRIS [Morris  Twp] (BLEEDING) - LACERATION HER RIGHT LEG... IN UNIT 1A ROOM 1108... - E2368 07:54
 ***626 HANOVER 22 [Morris Twp] (SICK PERSN) - MALE NOT FEELING WELL CALLER STATES THAT HE IS WHITE AS A SHEET 2ND TO LAST HOUSE BEFORE RANDOLPH - 2398 04:56
 35 SHERMAN AVE [Morris Twp] (CHEST PAIN) - 64 YOF CHEST PAIN, JUST HAD DFIB INSTALLED - E2368 21:39
 1 MOLLY STARK DR [Morris Twp] (FALL) - CALLER STATED SHE WAS FEALING WEAK AND SHE FELL. SHE HAS A CUT TO HER HEAD AND SHE CANT GET UP. - E2368 14:06
 43 W HANOVER AVE [Randolph Twp] (MUTUAL AID) - MUTUAL AID INTO RANDOLPH FOR PSYCH TRANSPORT... NO LIGHTS/SIRENS. - 3281,2398 14:03
 1 BROOK DR S [Morris Plains] (UNCONSCN) - 83 YOM ALTERED MENTAL STATUS - 2398 19:14
 MADISON HOTEL (22), 1 CONVENT RD [Morris Twp] (SICK PERSN) - SICK FEMALE IN LOBBY - E2368 19:01
 CANFIELD RD / PIPPINS WAY [Morris Twp] (OVERDOSE) - INTOXICATED FEMALE, AT LOCATION OF AN MVA - E2368 01:46
 UNIT 1D ROOM 1422 MORRIS VIEW NURSING HOME (22), 540 W HANOVER AVE [Morris Twp] (SICK PERSN) - FEMALE WITH ALTERMENTAL STATUS - E2366 09:37
 ST PAULS CHURCH, MOUNTAIN WAY / HILLVIEW AVE [Morris Plains] (UNCONSCN) - FEMALE PASSED OUT. CONSCIOUS AT THIS TIME - 2398 10:44
 RACEWAY GAS(23), 1701 RT 10 [Morris Plains] (CAR FIRE) - CAR FIRE IN THE FAST LANE - 2399,2398 22:46
 901 RT 10 [Hanover Twp] (MUTUAL AID) - THE JCC BUILDING FOR A FEMAL WITH A LACERATION TO THE FINGER/ MORRIS MIN MEN - 2398 16:54
 POST91-7 (MENNEN ARENA), 161 E HANOVER AVE [Morris Twp] (FALL) - IN BACK OF RINK 1 ON RIGHT HAND SIDE. 40 YR OLD FEMALE FELL, AND HURT HER RIGHT KNEE. HEARD CRACK ON FALL. - 2398 10:38
 AMERICAN RD / E HANOVER AVE [Morris Plains] (MVA) - 2 CAR MVA, PATIENT COMPLAINING OF HEAD PAIN. HONDA CIVIC VS FORD. VEHICLES IN INTERSECTION - 2398 16:53
 LITTLETON RD / COURT RD [Morris Plains] (MVA) - PD REQUEST 1 RIG, AIR BAG DEPLOYMENT - 2398 12:15
 SUNRISE ASSISTED LIVING (23), 209 LITTLETON RD [Morris Plains] (SICK PERSN) - 1ST FLOOR...CALLER REPORTS THE FEMALE PATIENT IS SHAKING AND SCREAMING. CALLE
 
 prvs=3175827e30=dispatch@co.morris.nj.us Police Academy Morris Cty, 500 W HANOVER AVE [Parsippany-Troy] (UNCONSCNS) - CONSTRUCTION WORKER STATED HE BLACKED OUT. HE IS SITTING INSID
prvs=62791756a7=dispatch@co.morris.nj.us PRO HAIRCUTTERS INC (23), 1711 RT 10 E [Morris Plains] (HEM/CUTS) - 82 Y/O FEMALE LACERATION TO LEG - 2398 16:07
 
 */
 
 public class NJMorrisCountyParser extends SmsMsgParser {
   
   private static final Pattern MASTER_PTN = 
    Pattern.compile("^(.*) \\[([-A-Za-z ]+)\\] \\(([A-Z/ ]+)\\) - (.*)$");
   
   private static final Pattern PLACE_CODE_PTN = Pattern.compile("\\(\\d+\\)$");
   
   public NJMorrisCountyParser() {
     super("MORRIS COUNTY", "NJ");
   }
   
   @Override
   public String getFilter() {
     return "Dispatch@co.morris.nj.us,mcdispatch@optimum.net";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     Matcher match = MASTER_PTN.matcher(body);
     if (!match.matches()) return false;
     
     String sAddress = match.group(1).trim();
     if (sAddress.startsWith("***")) sAddress = sAddress.substring(3).trim();
     Parser p = new Parser(sAddress);
     data.strPlace = p.getOptional(',');
     Matcher match2 = PLACE_CODE_PTN.matcher(data.strPlace);
     if (match2.find()) 
       data.strPlace = data.strPlace.substring(0,match2.start()).trim();
     data.strApt = p.getLastOptional(" BLDG ");
     parseAddress(p.get(), data);
     
     data.strCity = match.group(2).trim();
     data.strCall = match.group(3).trim();
     p = new Parser(match.group(4));
     String sExtra = p.getLastOptional(" - ");
     data.strSupp = p.get();
     p = new Parser(sExtra);
     data.strUnit = p.get(' ');
     return true;
   }
 }

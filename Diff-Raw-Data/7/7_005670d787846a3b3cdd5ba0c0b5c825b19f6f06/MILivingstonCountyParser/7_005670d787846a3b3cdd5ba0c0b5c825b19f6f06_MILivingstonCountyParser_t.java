 package net.anei.cadpage.parsers.MI;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Livingston County, MI
 Sender: CAD@livingstoncounty.livco
 
 CAD:FYI: ;ALARMF;11555 PLEASANT VIEW DR;INDIAN TRL;SMOKE ALARMS GOING OFF IN THE HOUSE FOR THE LAST 30 MIN ON AND OFF/ NO SMOKE OR SMOKE SHOWING [09/24/10 04:17:07 ECOOK]
 CAD:FYI: ;UNKACC;E M36/PETTYS RD;VEHICLE HIT A POLE/ MALE RUNNING E/B FROM SCENE [09/23/10 21:45:06 ECOOK]
 CAD:FYI: ;CHSTPN;8422 PAWNEE TRL;SHOSHONI PASS;[Medical Priority Info] RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 72 year old, Female, Conscious, Breathing. Chest Pain (Non-Traumatic). Abnormal breathing. Cal
 CAD:FYI: ;UNCON;8732 RIVER VALLEY RD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 63 year old, Female, Conscious, Breathing. Unconscious / Fainting (Near). Not alert. Caller Statement: UNCON.
 CAD:FYI: ;HEMLAC;SHEHAN RD/WHITEWOOD RD;[Medical Priority Info]RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 35 year old, Female, Conscious, Brea
 
 Pagecopy-Fr:CAD@lc911\nCAD:FYI: ;CHSTPN;7251 HAMBURG RD;WELLE RD;[Medical Priority Info] RESPONSE: P1 STA 1 3 4 5 6 7 8 FC FT RESPONDER S
 
 Contact: John Masek <john.sta33@gmail.com>
 CAD:FYI: ;CITAF;5579 E GRAND RIVER;WILDWOOD DR;Event spawned from CITIZEN ASSIST LAW. [12/10/10 20:08:59 SPHILLIPS] CALLER LIVES NEXT DOOR TO THE ADDRESS OF THE WATER MAIN BREAK [12/10/10 20:04:40 HROSSNER] CALLER ADV OF A WATER MAIN BREAK
 
 CAD:FYI: ;UNCON;SANCTURY OF WOODLAND;7533 W GRAND RIVER;COURT ST;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 80 year old, Female, Unconscious, Breathing. Unconscious / Fainting (Near). Unconscious -- Eff
 CAD:FYI: ;DIABET;31 STRATFORD LN;NEWCASTLE LN;[Medical Priority Info] RESPONSE: P1 STA 1 2 4 5 6 7 8 FC FT RESPONDER SCRIPT: 47 year old, Female, :1of2
 
 Contact: Jordan Zernick <zernick1115@gmail.com>
 Pagecopy-Fr:CAD@livingstoncounty.livco\nCAD:FYI: ;OVDOSE;4676 KENMORE DR;[Medical Priority Info] RESPONSE: P1 STA 1
 
 Contact: Casey Yost <caseyyost@gmail.com>
 firediver11+caf_=5176671194=vtext.com@gmail.comPagecopy-Fr:CAD@livingstoncounty.livco\nCAD:FYI: ;SEIZUR;131 STRATFORD LN;BELMONT LN;[Medical Priority Info]
 
 Contact: Jeff Vaden <jeffvaden@gmail.com>
 Pagecopy-Fr:CAD@livingstoncounty.livco CAD:Update: ;FALL;3031 WM36;[EMS] HAS BEEN VOMITTING - DIABETIC [02/14/11 09:55:08 RLADOUCEUR] [Me
 
 Contact: Aaron Gage <aarongage16@gmail.com>
 Sender: 93001011
 CAD:FYI: ;OPENBU;8360 MCCLEMENTS RD;DEKASSEL DR;BURNING GARBAGE ONE HOUSE EAST OF CALLER [07/01/11 19:44:56 SSTURGIS]
 
 Contact: Steve Hoyer <nottheface@gmail.com>
 Sender: CAD@livingstoncounty.livco
 FYI: ;SMKOUT;CHEROKEE BEND DR/RED OAKS DR;CALLER HEARD AN EXPLOSION ON CHEROKEE BEND, DOES NOT SEE ANYTHING AT THIS TIME. SMELLS SMOKE. [09/21/11 21:53:2
 FYI: ;CHOKE;SORG RESIDENCE;2917 LANIER CT;TAHOE BLVD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 3 year old, Male, C
 
 Contact: John Masek <john.sta33@gmail.com>
 Sender: CAD@livingstoncounty.livco
 FRM:CAD@livingstoncounty.livco\nMSG:CAD:FYI: ;PIA;DORR RD/E GRAND RIVER;JEEP LIBERTY VS SILVER VAN [11/30/11 09:18:58 CWESTPHAL]
 
 Contact: James P Krywko <James.Krywko@ipaper.com>
 Sender: CAD@livingstoncounty.livco
 CAD:CANCEL;N NATIONAL ST/E GRAND RIVER AVE; HOWL;AT CORNER
 CAD:Update: ;OIOUT;REGAL RECYCLERS;645 LUCY RD;INDUSTRIAL ST;{EN34} REQ CONSUMERS [12/28/11 17:51:25 LLOWE]
 CAD:FYI: ;CHOKE;5385 GREEN RD;CLYDE RD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8<tel:12345678> FC FT RESPONDER SCRIPT: 78 year old, Male, Conscious, Not breathing. Choking. Not alert. Caller Statement: CHOKING ON FOOD . 1.He is n
 CAD:FYI: ;PIA;N LATSON RD/E HIGHLAND RD;RED TRUCK VS SMALL SEDAN PER CALLER SUBJECT IS NOT OK [12/28/11 19:50:00 MSPICER]
 
 Contact: Dave Korponic <dkorps@gmail.com>
 Sender: Dave Korponic <dkorps@gmail.com>
 CAD:FYI: ;SICK;10165 CARLEE JUNE DR;RUNYAN LAKE RD;[Medical Priority Info] RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 37 year old, Male, Consci
 
Contact: Steve Hoyer <nottheface@gmail.com>
Update: ;TREEF;5117 CULVER RD;CANYON OAKS DR;CALLER STATES TREE COMPLETELY BLOCKING CULVER RD, [08/11/12 02:25:45 JMURPHY-KE]
Update: ;DEATH;4601 GOLF VIEW DR;GOLF VIEW CT;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 83 year old, Male, Unconsc
 
 */
 
 public class MILivingstonCountyParser extends DispatchOSSIParser {
   
   public String getFilter() {
     return "CAD@livingstoncounty.livco,93001";
   }
   
   public MILivingstonCountyParser() {
     super(CITY_CODES, "LIVINGSTON COUNTY", "MI",
            "( CANCEL ADDR CITY | SKIP CALL PLACE? ADDR/s! X? X? ) INFO+");
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
    if (body.startsWith("FYI: ;") || body.startsWith("Update: ;")) body = "CAD:" + body;
     return super.parseMsg(body, data);
   }
   
   private class CancelField extends CallField {
     public CancelField() {
       setPattern("CANCEL");
     }
     
     @Override
     public String getFieldNames() {
       return "CALL";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CANCEL")) return new CancelField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "HOWL", "HOWELL"
   });
 }

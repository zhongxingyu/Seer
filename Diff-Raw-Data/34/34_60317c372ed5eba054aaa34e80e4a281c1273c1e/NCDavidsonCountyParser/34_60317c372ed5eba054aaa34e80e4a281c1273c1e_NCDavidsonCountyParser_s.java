 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /***
 Davidson County, NC
 Contact: Rsk <rskbok09@yahoo.com>
 Contact: "Zachary, Marcus M [CON]" <Marcus.Zachary@sprint.com>
 Sender: cad@davidsoncountync.gov
 
 CAD:50PD-MOTOR VEH/PD ONLY;3248 FRIENDSHIP LEDFORD RD;2010024643;SADDLECHASE LN;MOTSINGER RD;OIL AND FLUIDS ON ROADWAY [12/17/10 11:05:24 BHUGHES] UDTS: 
 CAD:P29-TRAFFIC ACCIDENT-BRAVO;I 85 S/MM 99;2010024707;[Medical Priority Info] PROBLEM: traffic accident # PATS: 1 AGE: Unknown Range SEX: Unknown CONSCIOU
 CAD:HOUSE/SINGLE DWELLING;3637 BECKS CHURCH RD;2010024363;HUCKLE PL;NC HWY 47;[Fire Priority Info] PROBLEM: KITCHEN FIRE [12/14/10 18:25:04 BPOOLE] [Fire P
 CAD:P26-SICK-DELTA;4970 REID RD;2010024356;EASTVIEW DR;[Medical Priority Info] PROBLEM: SICK # PATS: 1 AGE: 78 Years SEX: Female CONSCIOUS: Yes BREATHING:
 CAD:Co Fire Tac3 for call;I 85 S/MM 99; THA;7703351053
 CAD:cancel further response;I 85 S/MM 99; THA;7703351053
 CAD:P29-TRAFFIC ACCIDENT-BRAVO;I 85 S/MM 96;2010025333;unk on inj, near mm97 [12/26/10 09:57:18 CHUFF] JUST NORTH OF THE 96....UKN INJ
 CAD:SMOKE INVESTIGATION;JOHNSONTOWN RD/TAFT HEDRICK RD;2010025211;no need for dispatch [12/25/2010 14:19:26 ASTILL]
 CAD:P29-TRAFFIC ACCIDENT-BRAVO;I 85 N/MM 98;2010025251;[Medical Priority Info] PROBLEM: 10-50 # PATS: 255 AGE: Unknown Range SEX: Unkn
 CAD:CANCEL;2006 JOHNSONTOWN RD; THA
 CAD:Co Fire TAC4 for call;I 85 S/MM 96; LEX;4075477164
 
 Contact: jon story <jstory2186@gmail.com>
 CAD:Co Fire Tac3 for call;3136 MOCK RD; HP
 ***/
 
 public class NCDavidsonCountyParser extends DispatchOSSIParser {
 
 
   public NCDavidsonCountyParser() {
     super("DAVIDSON COUNTY", "NC",
            "CALL ADDR! INTLS? ID? X+? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "cad@davidsoncountync.gov";
   }
 }

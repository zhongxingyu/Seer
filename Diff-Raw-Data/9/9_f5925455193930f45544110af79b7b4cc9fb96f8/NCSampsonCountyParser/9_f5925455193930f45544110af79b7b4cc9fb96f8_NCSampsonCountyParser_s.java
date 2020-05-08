 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Sampson County, NC
 Contact: "klee@intrstar.net" <klee@intrstar.net>
 Sender: EMS@sampsonnc.com
 System: VisionCAD
 
 EMS:2012-013061* 2280 HOBBTON HWY* * * CLINTON* WEDGEWOOD LN* BUMPY LN* K12* * ABDOMINAL* ABDOMINAL PAIN* CSRS,EMS79* 1328A* Medical: No* Hazards:  
 EMS:2012-013062* 219 W CARTER ST* * * CLINTON* BARDEN ST* BUNTING ST* L11* * BREATH DIFF* BREATHING DIFFICULTIES* CSRS,EMS75* 1379* Medical: No* Hazards: No* 02/2
 EMS:2012-013047* I40 MM 343* * * NEWTON GROVE* MCLAMB RD UNDERPASS* EXIT 343 HOBBTON HWY* D11* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* EMS79* 1263* Medical
 EMS:2012-013001* 91 N CHURCH AVE* * * GARLAND* W THIRD ST* W FOURTH ST* T10* * CHEST PAIN* CHEST PAIN* EMS77* 1376* Medical: No* Hazards: No* *
 EMS:2012-012994* 303 MARTIN LUTHER KING JR BLVD* * * CLINTON* WEEKS ST* SOUTHEAST BLVD* L12* * UNCONSCIOUS* UNCONSCIOUS/FAINTING* CSRS,EMS77* 1380* Medical: No* H
 EMS:2012-012960* 120 SOUTHWOOD DR* 305A* * CLINTON* SOUTH WEST BLVD* * M12* Landmark Comment: UPDATED 01/07* BREATH DIFF* BREATHING DIFFICULTIES* CSRS,EMS75* 1380
 EMS:2012-012906* 229 e MORISEY BLVD* * * CLINTON* LISBON ST* DEVANE ST* L12* * BURNS* BURNS* CSRS,EMS75* 1275* Medical: No* Hazards: No* *
 EMS:2012-012904* 134 W SECOND ST* * * GARLAND* S CHURCH AVE* BROOKS AVE* T10* * SICK CALLS* SICK CALLS* 7112* 1376* Medical: No* Hazards: No* *
 EMS:2012-012890* 2100 REEDSFORD RD* * * CLINTON* MELVABROOK DR* BILLY LN* M13,M14* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* CSRS,EMS76,STA14* 1330* Medical: Comment: TRACT 10 OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *
EMS:2012-012887* 249 KAYLA LN* * * CLINTON* KING RD* KING RD* I14* Geo Comment: TRACT 10 OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *
 EMS:2012-012877* 233 EFFIE PETERSON LN* * * ROSEBORO* PORTER RD* DEAD END* M9* * STRUCTURE FI* STRUCTURE FIRE* CSRS,EMS76* 1336* Medical: No* Hazards: No* *
 EMS:2012-012839* 4056 REEDSFORD RD* * * CLINTON* HUCKLEBERRY LN* STEVE HARRIS LN* N14,N15* * CHEST PAIN* CHEST PAIN* CSRS,EMS71* 1347A* Medical: No* Hazards: No*
 EMS:2012-012887* 249 KAYLA LN* * * CLINTON* KING RD* KING RD* I14* Geo Comment: TRACT 10 OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *
 EMS:2012-012838* 1498 HAWLEY RD* * OR* DUNN* FRED TEW RD* UNION GROVE CH RD* C4* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* EMS72,PVRS,Z951RT* 1254C* Medical:
 EMS:2012-012833* 253 PINE OAK LN* * * DUNN* MIDWAY ELEM SCH RD* DEAD END* E6* * SICK CALLS* SICK CALLS* EMS78* 1255A* Medical: No* Hazards: No* *
 EMS:2012-012830* 203 N MAIN ST* * * SALEMBURG* CLINTON ST* CHURCH ST* K6* * BREATH DIFF* BREATHING DIFFICULTIES* EMS76* 1378* Medical: No* Hazards: No* 02/26/2012
 EMS:2012-012826* 939 SOUTHWEST BLVD* * TAC3* CLINTON* MARTIN LUTH KING BLV* * L12* * STRUCTURE FI* STRUCTURE FIRE* CSRS,EMS72,STA14,STA16,STA8* 1380* Medical: No*
 EMS:2012-012816* 301 MAIN ST* * * NEWTON GROVE* E CIRCLE ST* N CHURCH ST* C11* Landmark Comment: UPDATED 2-02* BREATH DIFF* BREATHING DIFFICULTIES* EMS71,EMS72,NG
 
 */
 public class NCSampsonCountyParser extends FieldProgramParser {
   
  private static final Pattern DELIM = Pattern.compile("\\* *");
   
   public NCSampsonCountyParser() {
     super("SAMPSON COUNTY", "NC",
            "ID ADDR APT CH CITY X X MAP INFO CALL INFO UNIT INFO+");
   }
   
   @Override
   public String getFilter() {
     return "EMS@sampsonnc.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!body.startsWith("EMS:")) return false;
     body = body.substring(4).trim();
     String[] flds = DELIM.split(body);
     if (flds.length < 12) return false;
     return parseFields(flds, data);
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new IdField("\\d{4}-\\d{6}", true);
     return super.getField(name);
   }
   
 }

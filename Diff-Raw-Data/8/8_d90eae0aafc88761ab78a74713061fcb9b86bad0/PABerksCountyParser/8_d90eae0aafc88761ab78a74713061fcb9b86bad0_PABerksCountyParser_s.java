 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Berks County, PA (replace)
 Contact: "john.manmiller" <john.manmiller@gmail.com>
 Contact: "kreinert@lyons35fire.com" <kreinert@lyons35fire.com>
 Contact: Diana York <hawkmeadow@gmail.com>
 Contact: Duane Good <duaneg123@gmail.com>
 Contact: "jamesmiller4525@gmail.com" <jamesmiller4525@gmail.com>
 Contact: "thirty5tripleuno@yahoo.com" <thirty5tripleuno@yahoo.com>
 Contact: 4843343577@messaging.sprintpcs.com
 Contact: Mike Roth <mroth@springtwpberks.org>
 Contact: VJ GREEK <vjgreek@gmail.com>
 Contact: Buddy <fwardiv@gmail.com>
 Sender: alrt_3WPJ@berks.alertpa.org,@rsix.roamsecure.net,14100
 Unit:M590A Status:Dispatched ALS MEDICAL ; 215 TOWER RD ; ; S PARK AVE / CLAY RD ; Muni: LONGSWAMP ; 64 YOM / IN ACTIVE SIEZURE  ; 07/10/12 12:57
 Unit:M590B Status:Dispatched MVAUNK ; W MAIN ST NORMAL AVE ; ; ; Muni: KUTZTOWN ; ; 07/10/12 14:06
 Unit:M535A Status:Dispatched ALS MEDICAL ; 281 MOSELEM CHURCH RD ; ; SCHWOYER RD / MISTY LN ; Muni: RICHMOND ; 79 YOF/VOMITING/COUGHING/PAIN IN SIDE ; 07/11/12 10:04 Sent by Berks County RSAN to Fleetwood EMS All CALL -- You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org
 Unit:M535A Status:Dispatched ALS MEDICAL ; 224 FAITH DR ; ; GENESIS DR, HOPE DR / HOPE DR ; Muni: MAIDENCREEK ; 39YOM/NOT MAKING SENSE/LAYING ON THE FL ; 07/11/12 21:59 Sent by Berks County RSAN to Fleetwood EMS All CALL -- You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org
 Unit:585A Status:Dispatched MVAWITH ; 2330 INTERSTATE 78 E ; I78 MP 23.3 EB; UNKNOWN / RAMP B7 ; Muni: UPPER BERN ; 2 vehs/ ILVER SEDAN / UNK INJS / TX TO PSP  ; 07/11/12 16:33 On Jul 11, 2012 5:55 PM, "Ken Corbin" <ken@cadpage.org> wrote:
 Unit:M590A Status:Dispatched MVAWITH ; S BALDY ST TREXLER AVE ; ; ; Muni: KUTZTOWN ; ; 07/11/12 07:10
 Unit:M590A Status:Dispatched BLS MEDICAL ; 625 SPRING ST ; PENNSYLVANIA DIALYSIS; CROSSING DR / N 6 ST ; Muni: WYOMISSING ; ; 07/10/12 19:58
 Unit:M590B Status:Dispatched MVAUNK ; W MAIN ST NORMAL AVE ; ; ; Muni: KUTZTOWN ; ; 07/10/12 14:06
 Unit:R27 Status:Dispatched MVAUNK ; ROUTE 183 MILL RD ; ; ; Muni: UPPER TULPEHOCKEN ; ; 07/12/12 04:48
 Unit:535A Status:Dispatched BLS TRAUMA ; 2 W PINE ST ; ; S FRANKLIN ST, E PINE ST /NEW ALY ; Muni: FLEETWOOD ; 79 YOM FELL IN THE DRIVEWAY ; 07/11/12 14:13
 Unit:M590A Status:Dispatched ALS MEDICAL ; 7723 HAMILTON BLVD ; ; ; Muni: ; UPPERMACUNGIE TWP- LEHIGH CO/ ; 07/11/12 21:51
 Unit:M590A Status:Dispatched ALS MEDICAL ; 1107 APEX AVE ; ; UNKNOWN / LIFESTYLE LN ; Muni: MAXATAWNY ; 20ish yof layin on ground /is cons ; 07/11/12 20:41
 Unit:M590A Status:Dispatched ALS TRAUMA ; 15620 KUTZTOWN RD ; ; UNKNOWN / CHURCH RD ; Muni: MAXATAWNY ; 10 YOF FELL FROM A WINDOW  UNCONS ; 07/12/12 12:38
 Unit:M590A Status:Dispatched BLS MEDICAL ; 247 RHOADES RD ; ; NUNEMAKER RD / ROTH RD ; Muni: GREENWICH ; RESPONDING FOR BLSMED ; 07/12/12 15:14
 Unit:M590A Status:Dispatched LIFT ASSIST ; 35 S WHITEOAK ST SHADE RES ; ; SANDER ALY, N WHITEOAK ST / NORMAL AVE ; Muni: KUTZTOWN ; 74YOM FELL NOT INJ JUST NEEDS HELP GETTING UP ; 07/12/12 17:38
 Unit:M590A Status:Dispatched BLS MEDICAL ; 247 RHOADES RD ; ; NUNEMAKER RD / ROTH RD ; Muni: GREENWICH ; RESPONDING FOR BLSMED ; 07/12/12 15:14
 Unit:M590A Status:Dispatched ALS TRAUMA ; 15620 KUTZTOWN RD ; ; UNKNOWN / CHURCH RD ; Muni: MAXATAWNY ; 10 YOF FELL FROM A WINDOW  UNCONS ; 07/12/12 12:38
 Unit:CO61 Status:Dispatched CMA ; 303 WYNDCLIFFE AVE ; ; HILLTOP CT / CHESTNUT ST, WINTERSON DR ; Muni: HAMBURG ;
 Unit:CO61 Status:Dispatched AFA ; 218 PINE ST ; ; S PEACH ALY / S 2 ST ; Muni: HAMBURG ; ; 07/12/12 06:37
 Unit:CO85 Status:Dispatched MVAWITH ; 1780 ACORN DR ; ; TIMBER LN / GRAVEL HILL DR ; Muni: SPRING ; FM SCREAMING SHE IS BLEEDING/  ; 07/12/12 02:03\n\nSent by Berks County RSAN to CO85 All Call
 Unit:E85-2 Status:Dispatched HAZMAT ; 2227 MCKINLEY AVE ; ; UNKNOWN / WEST WYOMISSING BLVD ; Muni: SPRING ; CHF85 AND E85-2 EN FOR ODOR OF GASOLINE IN RESIDENCE ; 07/11/12 10:07\n\nSent by Berks County RSAN to CO85 All Call
 Unit:REN85 Status:Dispatched SF ; 44 WATER ST ; ; POTTSVILLE PKE / N 5 STREET HWY ; Muni: MUHLENBERG ; elderly fm / not answering questions  ; 07/12/12 00:54\n\nSent by Berks County RSAN to CO85 All Call
 Unit:R1 Status:Dispatched MVAUNK ; OAK LN / RICHMOND ST ; ; ; Muni: LOWER ALSACE ; mc into ditch / on oak ln / ; 07/12/12 12
 
 Contact: Active911
 (berks.co85@rsix.roamsecure.net) Unit:CO85 Status:Dispatched MF ; 828 BLUE GATE LN ; ; PERSIMMON DR / HEARTHSTONE LN ; Muni: SPRING ; ; 07/13/12 14:09 Sent by Berks County RSAN to CO85 All Call -- You received this message because you registered on Alert Berks. 
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched BLS MEDICAL ; 1320 BROADCASTING RD ; ; MERIDIAN BLVD / CONNEL DR ; Muni: SPRING ; RESPONDING FOR BLS MED / DIRECT CALL TO STATION / NFI ; 07/12/12 15:18\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched ALS MEDICAL ; 3121 STATE HILL RD RM17 ; COLUMBIA COTTAGE; YERGER BLVD, WESTBURY DR / LELAND WAY ; Muni: SPRING ; FEMALE CHEST PAIN  ; 07/12/12 15:19\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched CLASS 4 MENTAL ; 107 S 5 ST ROOM 5 ; ; FRANKLIN ST / CHESTNUT ST ; Muni: READING ; FEELING SUICIDAL ; 07/12/12 19:20\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650I Status:Dispatched ALS MEDICAL ; 404 S CHURCH ST ROOM 1 ; GOLDEN RIDGE; PATTERSON DR / PATTERSON DR ; Muni: ROBESONIA ; 88 YOF/ POS TIA ; 07/12/12 19:59\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched ALS MEDICAL ; 1802 TULPEHOCKEN RD RM 277 ; ; UNKNOWN / KISSINGER LN ; Muni: WYOMISSING ; 88 YOF/  ; 07/12/12 21:28\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650I Status:Dispatched ALS MEDICAL ; 101 E FRONT ST ; ; S MAIN ST, N MAIN ST / N WOLF ST ; Muni: BERNVILLE ; 63YOM LABORED BREATHING ; 07/12/12 22:17\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 3012 SUSANNA DR ; ; DANIEL DR / KATHARINE DR ; Muni: SPRING ; ALS ; 07/12/12 22:34\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched DOMESTIC INJURY ; 654 ANNE DR ; ; UNKNOWN / CHRISTOPHER DR ; Muni: WERNERSVILLE ; COMP VS EX HUSB OF GIRLFRIEND/  ; 07/12/12 22:45\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:650T Status:Dispatched CLASS 4 MENTAL ; 4050 CONRAD WEISER PRKWY 4 ; ; MARION DR / SHERIDAN RD ; Muni: MARION ; 21YOF/NEEDS TO BE 302 ; 07/12/12 23:37\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched ALS MEDICAL ; 511 HUNTERS RD ; ; UNKNOWN / UNKNOWN ; Muni: CUMRU ; 76YOM/ BLOOD SUGAR 35/  ; 07/12/12 23:52\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 350 SPORTSMAN RD ; WERNERSVILLE STATE HOSPITAL; WSH ORCHARD RD / WSH MAIN ST ; Muni: SOUTH HEIDELBERG ; 26YOM COUGHING UP BLOOD ; 07/13/12 00:01\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched BLS MEDICAL ; 517 OAK HILL LN ; ; UNKNOWN / UNKNOWN ; Muni: SPRING ; 84 YOF VOMITTING ; 07/13/12 00:51\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 135 BALTHASER RD ; ; N ARTHUR DR, S ARTHUR DR / PRESTON RD ; Muni: SOUTH HEIDELBERG ; 42YOF/ COMP IS PT/  ; 07/13/12 06:39\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650E Status:Enroute BLS MEDICAL ; 1801 CAMBRIDGE AVE D20 ; TENNIS PARK CONDOS; OLD MILL RD / LAUERS LN ; Muni: WYOMISSING ; ; 07/13/12 08:05\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched ALS MEDICAL ; 2701 NORTH MERIDIAN BLVD ; IHOP; BROADCASTING RD / MERIDIAN BLVD ; Muni: SPRING ; ML IN 20'S HAVING SEIZURE ; 07/13/12 10:25\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650E Status:Dispatched ALS MEDICAL ; 1863 LINCOLN AVE VINCENT BECKER RES ; ; BRANDYWINE RD / BRANDYWINE CT ; Muni: WYOMISSING ; 92 YOM ARM IS NUMB/RIGHT EYE DROOPY ; 07/13/12 11:20\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650A Status:Dispatched CLASS 4 MENTAL ; 13 BIRCHWOOD RD ; ; LARCHWOOD RD, CEDARWOOD RD / LARCHWOOD RD ; Muni: WYOMISSING ; sam crisis ; 07/13/12 11:30\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650I Status:Dispatched ALS MEDICAL ; 356 S FREEMAN ST ; ; E MEADOW AVE / W FREEMAN ST, FURNACE ST ; Muni: ROBESONIA ; 90 YOF / VERY WEAK / DEHYDRATED  ; 07/13/12 12:06\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched ALS MEDICAL ; 310 STITZER AVE ; ; E WILSON AVE, W WILSON AVE / E NORMAN ST, W NORMAN ST ; Muni: WERNERSVILLE ; 88 YOM NOT RESPONDING WELL ; 07/13/12 12:40\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 [berks.650@rsix.roamsecure.net] Unit:M650B Status:Dispatched BLS TRAUMA ; 2912 STATE HILL RD APT B3 ; ; No Cross Streets Found ; Muni: SPRING ; BERKSHIRE ESTATES APTS ; 07/13/12 13:43\nSent by Berks County RSAN to Western Berks EMS All CALL\n--\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n
 
 Contact: Gary Brady <ggbrady@verizon.net>
Sender: alrt_4CBC@berks.alertpa.org
 Unit:B42 Status:Dispatched BF ; 100 BLIMLINE RD BLOCK ; ; DEAD END / DEER CREEK RD ; SPRING ; ; 09/01/12 07:51
 
 Contact: Active911
 [berks.680@rsix.roamsecure.net] Unit:M680A Status:Dispatched ALS MEDICAL ; 202 HAYCREEK RD ; ; UNKNOWN / BROOKE MNR ; BIRDSBORO ; F TEMP OF 104 ; 08/28/12 18:14\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched BLS MEDICAL ; 920 N 4 ST ; METROPOLITAN APARTMENTS; WINDSOR ST / SPRING ST ; READING ; WAITING OUTSIDE ON STAIRS ; 08/28/12 18:33\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:680I Status:Dispatched ALS MEDICAL ; 1254 HIGHVIEW PL ; ; MANOR PL / FAIRVIEW CHAPEL RD ; EXETER ; 24 YOF / SEIZURE / HIT HEAD ON GROUND  ; 08/28/12 19:24\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched ALS TRAUMA ; 450 FURNACE RD ; ; MELODY LN / CYPRESS LN ; ROBESON ; ; 08/28/12 19:45\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched ALS MEDICAL ; 31 WASHINGTON ST ; ; S MILLER ST / NEW HOLLAND AVE ; SHILLINGTON ; 54 YOM / DISORIENTED / DIAPHORETIC /  ; 08/28/12 21:06\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680I Status:Dispatched MVAUNK ; W BENJAMIN FRANKLIN HWY RIVER BRIDGE RD ; ; ; AMITY ; VEH IN GRASS ; 08/28/12 21:38\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched BLS TRAUMA ; 1623 MORGANTOWN RD ROOM 158 ; HEALTH SOUTH; HOPE WAY / CARRIAGE LN ; CUMRU ; 86YOM/ FELL OUT OF BED/ LACERATION TO HEAD ; 08/29/12 01:16\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680A Status:Dispatched LIFT ASSIST ; 406 S WALNUT ST ; ; E 4 ST / E 5 ST ; BIRDSBORO ; 55YOF/ FELL AND NEEDS HELP GETTING UP ; 08/29/12 05:30\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680I Status:Dispatched ALS MEDICAL ; 1152 E BENJAMIN FRANKLIN HWY 219 ; VILLA AT MORLATTON ASSISTED LIVING; MAPLEWOOD DR / OLD AIRPORT RD ; AMITY ; 91 YOF SLURRED SPEECH ; 08/29/12 05:32\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched ALS TRAUMA ; 70 N ONEIL ST ; ; UNKNOWN / W MADISON ST ; MOHNTON ; 90 YOM FALL ; 08/29/12 09:00\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680I Status:Dispatched MVAWITH ; W BENJAMIN FRANKLIN HWY MONOCACY HILL RD ; ; ; AMITY ; ; 08/29/12 09:18\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680F Status:Dispatched BLS TRAUMA ; 790 MAPLEWOOD AVE ; ; CEDARWOOD LN / MOHNTON BLVD ; CUMRU ; 70YOM/STAGE 4 PROSTATE CANCER/WEAK/FELL THIS MORN\r\n ; 08/29/12 09:42\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:680A Status:Dispatched ALS MEDICAL ; 24 BUTTERCUP RD H213-27 ; ; DEAD END / SUNSET DR ; AMITY ; 50 YOF ASTHMA ; 08/29/12 10:21\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 [berks.680@rsix.roamsecure.net] Unit:M680E Status:Dispatched BLS TRAUMA ; 20 S SUMMIT AVE APT 1002 ; SENCIT APARTMENTS; COMMUNITY DR / HARDING AVE, BROOKE BLVD ; CUMRU ; FALL VICTIM CUT  ; 08/29/12 11:48\r\n\r\nSent by Berks County RSAN to Southern Berks EMS All CALL\r\r\n--\r\r\nYou received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\r\r\n\r\n
 
 */
 
 public class PABerksCountyParser extends FieldProgramParser {
   
   public PABerksCountyParser() {
     super("BERKS COUNTY", "PA",
           "UNITCALL! ADDR/iSXa! PLACE! X! CITY! INFO DATETIME");
   }
   
   @Override
   public String getFilter() {
     return "@berks.alertpa.org,@rsix.roamsecure.net,14100";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Strip off message trailer
     int pt = body.indexOf("Sent by Berks County RSAN");
     if (pt >= 0) body = body.substring(0,pt).trim();
     
     // There used to be a Muni: field label.  Which we remove from old  messages
     body = body.replace("; Muni:", ";");
     
     return parseFields(body.split(";"), data);
   }
   
   private static final Pattern UNIT_CALL_PTN = Pattern.compile("Unit:([-A-Z0-9]+) Status:(?:Dispatched|Enroute) (.*)");
   private class MyUnitCallField extends Field {
     @Override
     public void parse(String field, Data data) {
       Matcher match = UNIT_CALL_PTN.matcher(field);
       if (!match.matches()) abort();
       data.strUnit = match.group(1);
       data.strCall = match.group(2).trim();
     }
     
     @Override
     public String getFieldNames() {
       return "UNIT CALL";
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(" RES")) {
         field = field.substring(0,field.length()-4).trim();
         parseAddress(StartType.START_ADDR, FLAG_IMPLIED_INTERSECT, field, data);
         data.strName = getLeft();
       } else {
         super.parse(field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " NAME";
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (field.equals("No Cross Streets Found")) return;
       super.parse(field, data);
     }
   }
   
   private static final Pattern INFO_CITY_COUNTY_PTN  = Pattern.compile("(.*)-(.*)/(.*)");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = INFO_CITY_COUNTY_PTN.matcher(field);
       if (match.matches()) {
         data.strCity = match.group(1).trim() + ',' + match.group(2).trim();
         field = match.group(3).trim();
       }
       super.parse(field, data);
     }
   }
   
   private static final Pattern DATE_TIME_PTN = Pattern.compile("\\d\\d/\\d\\d/\\d\\d \\d\\d:\\d\\d");
   private class MyDateTimeField extends DateTimeField {
     @Override
     public void parse(String field, Data data) {
       if (!DATE_TIME_PTN.matcher(field).matches()) return;
       super.parse(field, data);
     }
   }
   
   @Override
   protected Field getField(String name) {
     if (name.equals("UNITCALL")) return new MyUnitCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     return super.getField(name);
   }
   
 }

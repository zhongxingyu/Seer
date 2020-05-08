 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Peoria County, IL
 12/23/10 1223 N MURPHY RD CO :(12100) W JO DAN CT JUST N OF HERE, SEES ALOT OF BLK SMOKE 1137,006
 12/26/10 2800 S ZESSIN RD CO :(20200) W MC DONALD RD 84 YOM CARRYING SOMETHING IN GARAGE, FELL BACKWARD, HIT HEAD ON CONCRETE, C/A/B OK
 12/27/10 533 N BRAD CT HC :( 600) N PEKIN LN 82 YO MALE FELL HAS HEAD AND HIP INJ NO BLEEDING 1527,004
 12/28/10 501 N MAIN ST HC :(12700) W LOGAN ST 74YOM C/A/B HAS A STAPLE EMBEDDED IN HER LEFT KNEE UNK TO PULL OUT
 12/28/10 {10301}W LANCASTER RD CO :( 5500) S HARKERS CORNER RD ROLL OVER 1300,006
 01/14/11 4712 W FARMINGTON RD CO :( 900) N SUNSET DR LAUNDRYMAT ON FIRE PEOPLE INSIDE BLDG UPSTAIRS IN AN APT OD WPEO FIREMAN SEES SMOKE
 01/16/11 9726 W WHITTINGHAM PT CO :( 9700) W LAKE LANCELOT DR FEM FELL HR AGO CRACKED NOW VOMITING NOT ALERT ALSO IS A DIABETC 30 YR FEM
 01/18/11 715 N MORAN RD CO :(19100) W FARMINGTON RD 72YOF PASSED OUT NOT ALERT BUT BREATH CARDIAC HX 0817,005
 01/18/11 2922 S TURBETT RD CO :(12100) W SMITHVILLE RD FEM IN 20S FELL OUTSIDE UNC/B NEAR FORD EXPLORER 1505,005
 01/19/11 13911 W SMITHVILLE RD CO :( 2500) S HANNA CITY-GLASFORD RD FEM DIFF BREATH 62 YOA CONSC/ALERT COME TO BACK DOOR
 01/20/11 9508 W LAKE CAMELOT DR CO :( 4500) S TEWKESBURY RD 78YO MALE UNRESPONSIVE W/SHALLOW BREATH 0914,005
 01/20/11 16327 W RIEKENA RD CO :(16300) W SMITHVILLE RD 72 YO MALE JST RELEASED FRM HOSP TODAY FRM BYPASS HAS AN1513,005 INCISION ON CHEST SERIOUS BLEEDING HAS SOAKED THRU TSHIRT1513,005 C/A/B OK RIGHT NOW 1513,005 (02/02)
 S: M:10/03/11 415 W BUTTERNUT ST EL :( 400) N MORGAN ST MALE W/BAD HEADACHE FELL ON SAT AND WAS CKED OUT THEN BUT NOW REQ TRANSP BACK TO HOSP 94 Y (01/02)\n\n
 
 St Joseph County, IN
 Subject:CAD\n[!] ^ 11425 MCKINLEY HW PE :(55231) BELAIR STGAS LEAK FROM PUTING IN A POST 1626,006NIPSCO 1627,014PEN4A\n
 Subject:CAD\n[!] ^ 17 RIVERCREST DR OS :( 600) N BEECH RDFROM MEDICAL ALARM COMP UNK MED 1617,006CALL BACK # TO ALARM COMP 800-325-17\n
 Subject:CAD\n[!] CRASH021 ^ 2 BYPASS HW PE :(60000) ELM RD2 VEH UNK INJ 0613,010ME10 ,PENN1 
 Subject:CAD\n[!] COMAL021 ^ 55693 ASH RD PE :(10000) ELSIE HWDUNGARVIN - COMMERCIAL FIRE ALARM - MANUAL 1233,014NOVI 800-877-3624 PRE 
 Subject:CAD\n[!] RESAL021 ^ 54895 MONTPELLIER DR PE :(11500) BOISE RUN STRESIDENTIAL FIRE ALARM - HALLWAY SMOKE DETECTOR 0813,014PENN 
 Subject:CAD\n[!] ^ 56375 CURRANT RD PE :(13402) ESTHER AVM. HEAD INJ FROM ASSULT 2147,014ME12 ,PENN2 
 (NC)[!] INVES 03/23/12 POPPY LN :( ) OBTAIN MORE LOCATION INFOBETWEEN HICKORY AND HOLLY - UNKNOWN TYPE FIRE
 (NC)[!] CRASH009 G-453 03/23/12 30750 SR 2 HW NC :(56000) LARRISON DRPI
 (NC)[!] COMAL009 G-428 03/23/12 31869 CHICAGO TR NC :(51452) HAMILTON TRGENERAL FIRE-SIMPLEX SECURITY
 
 St Joseph County, IN
 Fr:<alerts@etieline.com>\nSu:\nTxt: 08:49PM 07/24/2012 Ic# 26 MEDIC020 Ds# 135001 Al# 1 Utl# 203-204 07/24/1262052 OAK RD SW :(23500) ROOSEVELT RDF STRUCK IN THE FACE WITH A HORSE 2050,014ME10 ,SWD2
 Fr:<alerts@etieline.com>\nSu:\nTxt: 07:51AM 07/25/2012 Ic# 3 MEDIC020 Ds# 135002 Al# 1 Utl# G-89 07/25/1255096 MOSS RD SW :(23115) EDISON RDM. FELL - STRUCK HIS HEAD 0751,0141660 ,ME12 ,SWD1
 Fr:<alerts@etieline.com>\nSu:\nTxt: 09:14AM 07/25/2012 Ic# 4 CRASH020 Ds# 135002 Al# 1 Utl# G-101 07/25/1256000 MAYFLOWER RD SW :(23500) FILMORE RDUNK 0914,014SWD1
 Fr:<alerts@etieline.com>\nSu:\nTxt: 06:00PM 07/25/2012 Ic# 17 MEDIC020 Ds# 135002 Al# 1 Utl# G-89 07/25/1255591 MELROSE AV SW :(23200) SIDNEY AVM ABD PX THROWING UP CHEST PX NUMBNESS L ARM 1800,006ME11 ,SWD1
 Fr:<alerts@etieline.com>\nSu:\nTxt: 08:41PM 07/25/2012 Ic# 21 MEDIC020 Ds# 135001 Al# 1 Utl# G-185 07/25/1262430 LOCUST RD SW :(21500) ROOSEVELT RDF UNRESPONSIVE 2040,006ME41 ,CLAME,SWD2
 Fr:<alerts@etieline.com>\nSu:\nTxt: 10:14PM 07/25/2012 Ic# 25 INVES020 Ds# 135002 Al# 1 Utl# G-101 07/25/1256227 OAK RD SW :(23900) FILMORE RDUNK TYPE FIRE ON BUTTERNUT BEHIND THIS ADDRESS 2214,014SWD1
 Fr:<alerts@etieline.com>\nSu:\nTxt: 12:54PM 07/26/2012 Ic# 9 MEDIC020 Ds# 135001 Al# 1 Utl# G-209 07/26/1224125 NEW RD SW :(63500) OAK RDM. SUGAR 27 1254,006ME11 ,1660 ,SWD2
 Fr:<alerts@etieline.com>\nSu:\nTxt: 02:20PM 07/26/2012 Ic# 15 INVES020 Ds# 135001 Al# 1 Utl# G-156 07/26/1219502 PULLING ST SW :(60700) FELLOW STBURNING SMELL IN THE BASEMENT, NO SMOKE OR FLAMES SEEN, SMOKEALARMS ARE NOW GOING OFF 1420,0065 PPL AND 2 K9S HAVE EVACUATE (01/02)
 Fr:<alerts@etieline.com>\nSu:\nTxt: 06:55PM 07/26/2012 Ic# 24 MEDIC020 Ds# 135001 Al# 1 Utl# G-156 07/26/1219778 YODER ST SW :(60500) CARROLL STF DIZZY, SWETY, DIABETIC HX 1854,010SWD2
 Fr:<alerts@etieline.com>\nSu:\nTxt: 05:41PM 07/27/2012 Ic# 29 MEDIC020 Ds# 135001 Al# 1 Utl# G-158 07/27/1260788 GREENRIDGE CT SW :(21758) CARRIAGE DRM.UNKNOWN MEDICAL 1741,006ME10 ,SWD2
 
 Fr:<alerts@etieline.com>\nSu:\nTxt: 09:15PM 07/24/2012 ->Inc Addr = 62052 OAK RDEquip = SWD2Create = 20 50 01Asgned = 20 50 34Eroute = 20 51 13Scene  = 20 57 42Transp =Arrive =Avail  = 21 16 14Rpt # = 12-20000621
 Fr:<alerts@etieline.com>\nSu:\nTxt: 08:12AM 07/25/2012 ->Inc Addr = 55096 MOSS RDEquip = SWD1Create =  7 51 32Asgned =  7 52 23Eroute =  7 53 28Scene  =  7 56 46Transp =Arrive =Avail  =  8 12 57Rpt # = 12-20000622
 Fr:<alerts@etieline.com>\nSu:\nTxt: 09:31AM 07/25/2012 ->Inc Addr = 56000 MAYFLOWER RDEquip = SWD1Create =  9 14 26Asgned =  9 14 52Eroute =  9 15 27Scene  =  9 16 02Transp =Arrive =Avail  =  9 31 49Rpt # = 12-20000623
 
 St Jospeh County, IN
 (*CAD*) [CAD] [VEST] FIELD021 12 Ic# 11 FIELD021 Ds# 125001 Al# 1 Utl# 142-143 06/27/122 BYPASS HW PE <> ELM RDSOUTH SIDE OF ROAD - JUST E/O ELM 1147 TESTING 245 JUST TESTING DO NOT RESPOND
 (*CAD*) [CAD] [VEST] FIELD021 12 Ic# 11 FIELD021 Ds# 125001 Al# 1 Utl# 142-143 06/27/122 BYPASS HW PE <> ELM RDSOUTH SIDE OF ROAD - JUST E/O ELM 1147 TESTING 245 JUST TESTING DO NOT RESPOND
 (*CAD*) [CAD] [VEST] FIELD021 12 Ic# 11 FIELD021 Ds# 125001  Utl# 142-143 06/27/122 BYPASS HW PE <> ELM RDSOUTH SIDE OF ROAD - JUST E/O ELM 1147 TESTING 250 JUST TESTING DO NOT RESPOND
 (VEST) [VEST] INVES021 12 Ic# 20 INVES021 Ds# 125003 Al# 1 Utl# G-145 06/29/1211400 DRAGOON TR PE <> BASSWOOD RDFIELD FIRE FROM LINES DOWN - BETWEEN DRAGOON AND 12TH 1400
 (VEST) [VEST] ASSIS010 12 Ic# 51 ASSIS010 Ds# 132001 Al# 1 Utl# 06/29/12101 HARRISON ST LI <> JEFFERSON STVEST. LIVE LINE DOWN IN ROADWAY. 2224
 (VEST) [VEST] INVES002 12 Ic# 5 INVES002 Ds# 120001 Al# 1 Utl# 61-62 06/30/1253272 JUNIPER RD CL <> OAKMONT SOUTH DRTREE AND LINES DOWN AND THEIR LIVE 0616
 (VEST) [VEST] INVES021 12 Ic# 15 INVES021 Ds# 125003 Al# 1 Utl# G-117 06/30/1258211 ASH RD PE <> HARRISON RDTREE AND LINES DOWN 1327
 (VEST) [VEST] ASSIS004 12 Ic# 22 ASSIS004 Ds# 121002 Al# 1 Utl# 4-34 06/30/1213279 ADAMS RD HA <> CHERRY FARM TRVEST REQUESTED BY SJCPD FOR TREE IN THE ROADWAY 1754
 (VEST) [VEST] CRASH012 12 Ic# 28 CRASH012 Ds# 130001 Al# 1 Utl# G-115 07/04/121800 W LINCOLNWAY HW OS <> N PEONY AVHEAD ON CRASH 2244
 (VEST) [VEST] CRASH004 12 Ic# 37 CRASH004 Ds# 121001 Al# 1 Utl# G-2 07/04/1250585 SR 23 HW HA <> BIRCH RDROLLOVER NO ENTRAPMENT 2317
 
 */
 
 
 public class DispatchA6Parser extends SmartAddressParser {
   
   public DispatchA6Parser(Properties cityCodes, String defCity, String defState) {
     super(cityCodes, defCity, defState);
   }
   
   private static final Pattern LEAD_DATE_PAT = Pattern.compile("^(?:([- A-Z0-9]+) )?(?:(?:Ic# +(.*?) +Ds# ([^ ]*?) (?:Al# ([^ ]*?))? Utl# (?:([^ ]*?) +)?)?(\\d\\d/\\d\\d/\\d\\d)|\\^ )");
   private static final Pattern CROSS_MARK_PAT = Pattern.compile(" :\\( *(\\d*) *\\) | <> ");
   private static final Pattern TIME_UNIT_PAT = Pattern.compile("(?<!\\d)(\\d{4}),(\\d{3})");
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = LEAD_DATE_PAT.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     data.strCall = getOptGroup(match.group(1));
     String call = match.group(2);
     if (call != null) {
       data.strCall = call;
       data.strCallId = append(match.group(3), "-", getOptGroup(match.group(4)));
       data.strMap = getOptGroup(match.group(5));
     }
     if (data.strDate.length() == 0) data.strDate = getOptGroup(match.group(6));
     
     match = CROSS_MARK_PAT.matcher(body);
     if (!match.find()) return false;
     String crossNumber = match.group(1);
     
     String sAddr = body.substring(0,match.start()).replace('{', ' ').replace('}', ' ').trim();
     parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, sAddr, data);
     body = body.substring(match.end()).trim();
     
     match = TIME_UNIT_PAT.matcher(body);
     if (match.find()) {
       String time = match.group(1);
       if (data.strTime.length() == 0) data.strTime = time.substring(0,2) + ":" + time.substring(2);
       if (data.strUnit.length() == 0) data.strUnit = match.group(2);
       
       body = match.replaceAll(" / ").replaceAll("  +", " ").trim();
       if (body.endsWith("/")) body = body.substring(0,body.length()-1).trim();
     }
     
     Result res = parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS, body);
     if (res.getStatus() > 0) {
       res.getData(data);
       data.strSupp = res.getLeft();
       if (crossNumber != null) data.strCross = append(crossNumber, " ", data.strCross);
     } else {
       data.strSupp = body;
     }
     
     return true;
   }
 }

 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYNiagaraCountyParserTest extends BaseParserTest {
   
   public NYNiagaraCountyParserTest() {
     setParser(new NYNiagaraCountyParser(), "NIAGARA COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "((12474) NCFC MMYERS ) 3111 Sndrs Stlmnt Rd-NCCC-C Bldg unk age f labor pains no further info. BLSP EMS/Air Ops. 1617hrs numerous other calls in cnty. From email 7164383393@niagaracounty.com",
         "ID:12474",
         "CALL:GENERAL ALERT",
         "PLACE:3111 Sndrs Stlmnt Rd-NCCC-C Bldg unk age f labor pains no further info. BLSP EMS/Air Ops. 1617hrs numerous other calls in cnty. From email 7164383393@niagaracounty.com");
 
     doTest("T2",
         "(10444 NCFC JBEILEIN ) DUERO/WILLIAMS  MVA W MINOR INJURIES. From email 7164383393@niagaracounty.com",
         "ID:10444",
         "CALL:GENERAL ALERT",
         "PLACE:DUERO/WILLIAMS  MVA W MINOR INJURIES. From email 7164383393@niagaracounty.com");
 
     doTest("T3",
         "(550 NCFC RZASTROW ) 1-AMB TO MVA WITH SHAWNEE/LKPT & SHAWNEE RDS. From email 7164383393@niagaracounty.com",
         "ID:550",
         "CALL:GENERAL ALERT",
         "PLACE:1-AMB TO MVA WITH SHAWNEE/LKPT & SHAWNEE RDS. From email 7164383393@niagaracounty.com");
 
     doTest("T4",
         "((61571) NCFC TLEADERS ) ALS requested by Barker 1382 Quaker rd",
         "ID:61571",
         "CALL:ALS requested by Barker",
         "ADDR:1382 Quaker rd");
 
     doTest("T5",
         "(61571 NCFC TLEADERS ) ALS requested by Barker 1382 Quaker rd hope this helps",
         "ID:61571",
         "CALL:ALS requested by Barker",
         "ADDR:1382 Quaker rd",
         "INFO:hope this helps");
 
     doTest("T6",
         "(59339 NCFC JABROWN ) 6331 Bayview Station leaking hot water tank",
         "ID:59339",
         "CALL:GENERAL ALERT",
         "PLACE:6331 Bayview Station leaking hot water tank");
 
     doTest("T7",
         "(59687 NCFC MDIPASQU ) 1691,,LOCKPORT,ST,,80 YOF FELL SINK BLS-P BACK HALL",
         "ID:59687",
         "CALL:80 YOF FELL SINK BLS-P BACK HALL",
         "ADDR:1691 LOCKPORT ST");
 
     doTest("T8",
         "(60104 NCFC HBABIS ) 3445 murphy rd/74yof weak/ 8/10 1611",
         "ID:60104",
         "CALL:/ 74yof weak /",
         "ADDR:3445 murphy rd",
         "DATE:8/10",
         "TIME:16:11");
 
     doTest("T9",
         "(60395 NCFC HBABIS ) Fire alarm act/eastern niagara newfane hosp 2600 williams st/8/10 1852",
         "ID:60395",
         "CALL:Fire alarm act/eastern niagara newfane hosp",
         "ADDR:2600 williams st",
         "INFO:/",
         "DATE:8/10",
         "TIME:18:52");
 
     doTest("T10",
         "((6414) NCFC HBABIS ) 6061 edward av 18/94yof incoherant cons/br 9/27 1746",
         "ID:6414",
         "CALL:18 / 94yof incoherant cons / br",
         "ADDR:6061 edward av",
         "MADDR:6061 edward ave",
         "DATE:9/27",
         "TIME:17:46");
 
     doTest("T11",
         "((6492) NCFC HBABIS ) 5981 ide rd fire alarm act 2nd floor 9/27 1813",
         "ID:6492",
         "CALL:fire alarm act 2nd floor",
         "ADDR:5981 ide rd",
         "DATE:9/27",
         "TIME:18:13");
 
     doTest("T12",
         "((9597) NCFC RZASTROW ) fire alm newfane rehab ctr/2709 transit rd/olcott s-by in qtrs/OPS 1",
         "ID:9597",
         "CALL:fire alm newfane rehab ctr /",
         "ADDR:2709 transit rd",
         "INFO:/ olcott s-by in qtrs / OPS 1");
 
     doTest("T13",
         "((7792) NCFC MMYERS ) Newfane Elem School - fire alarm - PER 11M7 stand down burnt popcorn.  1602hrs",
         "ID:7792",
         "CALL:GENERAL ALERT",
         "PLACE:Newfane Elem School - fire alarm - PER 11M7 stand down burnt popcorn.  1602hrs");
 
     doTest("T14",
         "((8038) NCFC MMYERS ) Hartland Fire Hall - 34 yof in p/u truck seizing. ALSP EMS/Air Ops 2034hrs",
         "ID:8038",
         "CALL:GENERAL ALERT",
         "PLACE:Hartland Fire Hall - 34 yof in p/u truck seizing. ALSP EMS/Air Ops 2034hrs");
 
     doTest("T15",
         "((9307) NCFC RZASTROW ) NEWF ELEM SCHOOL/2909 TRANSIT RD/RM 141 HEAT DET ACTIVATED/OPS 1",
         "ID:9307",
         "CALL:NEWF ELEM SCHOOL /",
         "ADDR:2909 TRANSIT RD",
         "INFO:/ RM 141 HEAT DET ACTIVATED / OPS 1");
 
     doTest("T16",
         "((10575) NCFC TKONG ) #681 EMS 2171 HESS RD  80/F scald burns to arms and forehead",
         "ID:10575",
         "CALL:#681 EMS",
         "ADDR:2171 HESS RD",
         "INFO:80 / F scald burns to arms and forehead");
 
     doTest("T17",
         "((13341) NCFC MDIPASQU ) 1608 FRANKLIN 59 YOF DIF BREATHING ALS-P",
         "ID:13341",
         "CALL:GENERAL ALERT",
         "PLACE:1608 FRANKLIN 59 YOF DIF BREATHING ALS-P");
 
     doTest("T18",
         "((7720) NCFC MMYERS ) 4895 Gasport Rd - m in 50s in driveway was uncon now conscious. ALSP EMS/Air Ops RM264 from Ward/Lkpt 1551hrs",
         "ID:7720",
         "CALL:m in 50s in driveway was uncon now conscious ALSP EMS / Air Ops RM264 from Ward / Lkpt",
         "ADDR:4895 Gasport Rd",
         "TIME:15:51");
 
     doTest("T19",
         "((8994) NCFC RZASTROW ) FIRE POLICE TO MVA/ROCHESTER & BOLTON RDS.",
         "ID:8994",
         "CALL:GENERAL ALERT",
         "PLACE:FIRE POLICE TO MVA/ROCHESTER & BOLTON RDS.");
 
     doTest("T20",
         "[(11511] NCFC WWALKER ) ASSIST GASPORT WITH A LIFT ASSIST 4540 LINCOLN DR 311D 10/1/11 1834",
         "ID:11511",
         "CALL:ASSIST GASPORT WITH A LIFT ASSIST",
         "ADDR:4540 LINCOLN DR",
         "INFO:311D",
         "DATE:10/1/11",
         "TIME:18:34");
 
     doTest("T21",
         "((17295) NCFC JBEILEIN ) 8815 CHESTNUT RIDGE RD 35YOF SEVERE UPPER ABDOMINAL PAIN ALSO BACK PAIN ALS P",
         "ID:17295",
         "CALL:35YOF SEVERE UPPER ABDOMINAL PAIN ALSO BACK PAIN ALS P",
         "ADDR:8815 CHESTNUT RIDGE RD");
 
     doTest("T22",
         "((18371) NCFC RZASTROW ) car fire/mill & gasport rds/ops 1",
         "ID:18371",
         "CALL:GENERAL ALERT",
         "PLACE:car fire/mill & gasport rds/ops 1");
 
     doTest("T23",
         "[(35020] NCFC JBEILEIN ) 7963 CHESTNUT RIDGE RD 85YOF ILL PARTY LEG PAIN BLS S",
         "ID:35020",
         "CALL:85YOF ILL PARTY LEG PAIN BLS S",
         "ADDR:7963 CHESTNUT RIDGE RD");
 
     doTest("T24",
         "((37315) NCFC JBEILEIN ) 25E2 OUT OF SERVICE UNTIL FURTHER NOTICE",
         "ID:37315",
         "CALL:GENERAL ALERT",
         "PLACE:25E2 OUT OF SERVICE UNTIL FURTHER NOTICE");
 
     doTest("T25",
         "[(7561] NCFC RYUREK ) IF YOU PLAN TO ATTEND THE FIRE PREVENTION EVENT ON 10/5, PLEASE MEET AT GASPORTS MAIN ALL AT 0800 PER 6M7",
         "ID:7561",
         "CALL:GENERAL ALERT",
         "PLACE:IF YOU PLAN TO ATTEND THE FIRE PREVENTION EVENT ON 10/5, PLEASE MEET AT GASPORTS MAIN ALL AT 0800 PER 6M7");
 
     doTest("T26",
         "((44203) NCFC MSTEEN ) 6107 strauss rd apt g for ems alarm no contact",
         "ID:44203",
         "CALL:for ems alarm no contact",
         "ADDR:6107 strauss rd",
         "APT:g");
 
     doTest("T27",
         "((35250) NCFC JBEILEIN ) 5082 RIDGE RD FIRE ALARM ZONE 21 SMOKE DETECTORS",
         "ID:35250",
         "CALL:FIRE ALARM ZONE 21 SMOKE DETECTORS",
         "ADDR:5082 RIDGE RD");
 
     doTest("T28",
         "((38762) NCFC JBEILEIN ) LOCKPORT JUNCTION/UPPER MOUNTAIN RD MVA CAR STRUCK BUS BLS P",
         "ID:38762",
        "CALL:LOCKPORT JUNCTION /",
        "ADDR:UPPER MOUNTAIN RD",
         "INFO:MVA CAR STRUCK BUS BLS P");
 
     doTest("T29",
         "[(44051] NCFC KTOMTISH ) 5460 Ernest Rd - 82 YOF - Pace Maker pain - Radiating down left arm into fingers - EMS Opps on location",
         "ID:44051",
         "CALL:82 YOF - Pace Maker pain - Radiating down left arm into fingers - EMS Opps on location",
         "ADDR:5460 Ernest Rd");
 
     doTest("T30",
         "((27156) NCFC RYUREK ) o & c requested by rapids 7588 dysinger rd call fire control if available 10/11 1822",
         "ID:27156",
         "CALL:o & c requested by rapids",
         "ADDR:7588 dysinger rd",
         "INFO:call fire control if available",
         "DATE:10/11",
         "TIME:18:22");
 
     doTest("T31",
         "((3559) NCFC RYUREK ) ma/ ambulance to rapids 6718 riddle rd 71m fell, back pain 09/26 1012",
         "ID:3559",
         "CALL:ma/ambulance to rapids",
         "ADDR:6718 riddle rd",
         "INFO:71m fell back pain",
         "DATE:09/26",
         "TIME:10:12");
 
     doTest("T32",
         "((41014) NCFC JBRUCE ) 8376 AKRON RD 82 YOF DIF BREATHING/ALS PRIOR",
         "ID:41014",
         "CALL:82 YOF DIF BREATHING / ALS PRIOR",
         "ADDR:8376 AKRON RD");
 
     doTest("T33",
         "((62154) NCFC CGREENE ) Requested by Hartland for Dump Truck fire behind Chapman's Market - 9202 Ridge Rd - Call Fire Control if responding. 09/21/11",
         "ID:62154",
         "CALL:Requested by Hartland for Dump Truck fire behind Chapman's Market",
         "ADDR:9202 Ridge Rd",
         "INFO:Call Fire Control if responding 09 / 21 / 11");
 
     doTest("T34",
         "((42817) NCFC MDIPASQU ) 6699 Wolcottsville rd 52 yof sick dehidrated bls-p",
         "ID:42817",
         "CALL:52 yof sick dehidrated bls-p",
         "ADDR:6699 Wolcottsville rd");
 
     doTest("T35",
         "((42996) NCFC MDIPASQU ) 42 ALEXANDER PK 82 F FELL UNKNOWN INJ POSS LIFT ASSIST BLS-S",
         "ID:42996",
         "CALL:82 F FELL UNKNOWN INJ POSS LIFT ASSIST BLS-S",
         "ADDR:42 ALEXANDER PK",
         "MADDR:42 ALEXANDER PIKE");
 
     doTest("T36",
         "((41328) NCFC JBRUCE ) EMS ALARM @ 6666 SHETRAM RD",
         "ID:41328",
         "CALL:EMS ALARM",
         "ADDR:6666 SHETRAM RD");
 
     doTest("T37",
         "(45676) NCFC DLEVEN) 425 West Lane/CO Detector with no symptoms/Ops 1",
         "ID:45676",
         "CALL:/ CO Detector with no symptoms / Ops 1",
         "ADDR:425 West Lane");
 
     doTest("T38",
         "(43218) NCFC TLEADERS 454 lockport st. 91 yom difficulty breathing. ALS-P 31R9 is at ECMC returning",
         "ID:43218",
         "CALL:GENERAL ALERT",
         "PLACE:454 lockport st. 91 yom difficulty breathing. ALS-P 31R9 is at ECMC returning");
 
     doTest("T39",
         "((23406) NCFC CGREENE ) EMS @ 7300 Winbert Dr - 45yof Chest Pain - 01/06/12 @ 0442/hrs - EMS OPS  ALSP",
         "ID:23406",
         "CALL:EMS",
         "ADDR:7300 Winbert Dr",
         "INFO:45yof Chest Pain EMS OPS ALSP",
         "DATE:01/06/12",
         "TIME:04:42");
 
     doTest("T40",
         "((64253) NCFC CGREENE) FIRE ALM @ Wilson Central School - 374 Lake St (Main Bldg) - OPS 1 - 01/31/12 @ 0355/hrs",
         "ID:64253",
         "CALL:FIRE ALM",
         "PLACE:Wilson Central School",
         "ADDR:374 Lake St",
         "INFO:(Main Bldg) - OPS 1",
         "DATE:01/31/12",
         "TIME:03:55");
  }
   
   public static void main(String[] args) {
     new NYNiagaraCountyParserTest().generateTests("T1", "ID CALL PLACE ADDR APT INFO DATE TIME");
   }
 }

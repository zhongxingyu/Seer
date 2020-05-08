 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*    
 Singerly VFD in Cecil County, MD
 Contact: Christopher Olson <lilsmokeeater5@yahoo.com>
 Contact: Rob <robertjr1975@aol.com>
 Sender: singerly@gmail.com
 FRM:singerly@gmail.com\nMSG:1304A\nBUILDING FIRE\nELKTON MIDDLE SCHOOL @615 NORT\n23:20:22\n11021680\n23:23:02\nCALLER DRIVING BY SCHOOL AND S
 singerly@gmail.com: 0201A\nHOUSE \nHOUSE FIRE\n470 BLAIR SHORE RD\nBLAIR LN\nWOOD DUCK LN\n22:39:45\n12001775\n22:42:05\nSMELL OF SOMETHING BURNING GET
 singerly@gmail.com: 1401\nHOUSE \nHOUSE FIRE\n1030 KIRK RD\nBLACKSNAKE RD\nPROVIDENCE RD\n07:47:54\n12001740\n07:50:02\nCHIMNEY FIRE WITH FLAMES CALLE
 singerly@gmail.com: 0304\nMVA (M\nMOTOR VEHICLE ACC\nSINGERLY FIRE COMPAN @399 E PU\n00:49:36\n12001778\n00:49:54
 singerly@gmail.com: 1303\nAFA (A\nAUTOMATIC FIRE ALARM\n515 FLETCHWOOD RD @WAREHOUSE S\nOAKMONT DR\nOAKRIDGE CT\n01:25:41\n12001779\n01:26:04\nWAREHOUSE AREA WATER FLOW, BUS
 singerly@gmail.com: 0304\nMVA (M\nMOTOR VEHICLE ACC\nE PULASKI HWY / DELANCY RD\n13:56:29\n12001803\n13:58:00\n2 VEHICLES 2 PATIENTS
 singerly@gmail.com: 0305\nMVA (M\nMOTOR VEHICLE ACC\nW PULASKI HWY / LANDING LN\n14:23:22\n12001804\n14:25:09\nPOL
 singerly@gmail.com: 1302\nAFA (A\nAUTOMATIC FIRE ALARM\n110 JAMES ST @ELKTON HIGH SCHO\n07:05:19\n12001833\n07:06:12\nGENERAL ALARM/WILL ATTEMPT TO
 singerly@gmail.com: 1303\nAFA (A\nAUTOMATIC FIRE ALARM\n515 FLETCHWOOD RD @WAREHOUSE S\nOAKMONT DR\nOAKRIDGE CT\n23:02:22\n12001882\n23:03:48\nZONE 86 WATERFLOW WEARHOUSE 1
 singerly@gmail.com: 1401\nHOUSE \nHOUSE FIRE\n20 ROLLING GREEN LN\nHILLTOP RD\nGOMER CT\n20:36:11\n12002025\n20:36:49\nFIRE ACROSS STREET BELIEVED T 
 singerly@gmail.com: 1301\nMVA (M\nMOTOR VEHICLE ACC\nE OLD PHILADELPHIA RD / PULASK\n23:21:51\n12002033\n23:22:19\nMALE IS INJURIEDTRAFFIC ACCIDE 
 singerly@gmail.com: 1405\nMVA (M\nMOTOR VEHICLE ACC\nHIGHS @5655 TELEGRAPH RD\n23:22:43\n12002034\n23:23:56\nACROSS FROM THE HIGHS STORE DR \n 
 
  */
 
 public class MDCecilCountyBParser extends FieldProgramParser {
   
   private static final Pattern NORT_PTN = Pattern.compile("\\bNORT\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern TIME_PTN = Pattern.compile("\\d\\d:\\d\\d:\\d\\d");
 
   public MDCecilCountyBParser() {
     super("CECIL COUNTY", "MD",
            "CODE XTRA? CALL ADDR/S0P X/Z+? TIME ID INFO+");
   }
 
   @Override
   public String getFilter() {
     return "singerly@gmail.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     body = NORT_PTN.matcher(body).replaceAll("NORTH");
     if (! parseFields(body.split("\n"), data)) return false;
     return true;
   }
   
   private class MyTimeField extends TimeField {
     public MyTimeField() {
       setPattern(TIME_PTN, true);
     }
   }
   
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (TIME_PTN.matcher(field).matches()) return;
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
    if (name.equals("CODE")) return new CodeField("\\d{4}[A-Z]?", true);
     if (name.equals("XTRA")) return new SkipField(".{3,6}");
     if (name.equals("TIME")) return new MyTimeField();
     if (name.equals("ID")) return new IdField("\\d{8}", true);
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }

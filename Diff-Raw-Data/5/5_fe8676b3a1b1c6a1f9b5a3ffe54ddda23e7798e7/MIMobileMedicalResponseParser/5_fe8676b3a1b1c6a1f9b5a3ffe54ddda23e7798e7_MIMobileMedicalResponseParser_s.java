 package net.anei.cadpage.parsers.MI;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchProQAParser;
 
 /*
 Mobile Medical Response, MI (Claire, Isabella, and Gratiot Counties)
 Contact: Sean Duffy <emt210@gmail.com>, 2315981587@messaging.sprintpcs.com
 Sender: mailghost@mobilemedical.org
 System: ProQA
 
 Subject:<CAD> - part 1 of 1\nRC:Run# 17656/11775 N ISABELLA RD/P-2 Emergency No Lights//Chest Pain (Non-Traumatic)/herrick and pere marquette
 Subject:<CAD> - part 1 of 1\nRC:Run# 17612/7800 LUDINGTON DR/P-3 Downgrade  (No Lts/Sirens)//Falls/x gibson and lake station ave on 
 Subject:<CAD> - part 1 of 1\nRC:Run# 16888/7181 N CLARE/P-1 Life Threatening Emergency//Cardiac or Respiratory Arrest/Death/x forres
 Subject:<CAD> - part 1 of 1\nRC:Run# 17723/600 SE 4TH ST/P-1 Life Threatening Emergency/nurses station/Sick Person (Specific Diagnos
 Subject:<CAD> - part 1 of 1\nRC:Run# 17923/10197 S CLARE AVE/P-2 Emergency No Lights//Chest Pain (Non-Traumatic)/dept corrections bu
 Subject:<CAD> - part 1 of 1\nRC:Run# 17874/521 S SCOTT DR/P-1 Life Threatening Emergency//Breathing Problems/was in ER Saturday/ dx 
 Subject:<CAD> - part 1 of 1\nRC:Run# 17991/391 MILL ST/P-2 Emergency No Lights/D/Sick Person (Specific Diagnosis)/pt had knee replac
 Subject:<CAD> - part 1 of 1\nRC:Run# 17893/235 GATEWAY DR/P-1 Life Threatening Emergency/105/Breathing Problems//ALS/CLARE///<Unknow
 Subject:<CAD> - part 1 of 1\nRC:Run# 17970/602 BEECH ST/P-5 Local Transport/2ND FLOOR 205-A MED SURG/Int-Sick Person-Specify in Comm
 
 prvs=10834168ba=mailghost@mobilemedical.org (<CAD> - part 1 of 1) RC:Run# 24453/5186 FENTON/P-3 Downgrade  (No Lts/Sirens)//Overdose / Poisoning (Ingestion)/mundy twp // at kroger
 
 Contact: "jenmdawson@yahoo.com" <jenmdawson@yahoo.co 
 Sender: 6573
 <CAD> - part 1 of 1/ RC:Run# 34376/3300 PAULAN DR/P-1 Life Threatening Emergency//Chest Pain (Non-Traumatic)/72/F/ALS/BAY CITY///<Unknown>/20:04\n\n
 <CAD> - part 1 of 1/ RC:Run# 34848/3375 BOY SCOUT RD/P-1 Life Threatening Emergency//Convulsions/Seizures/x Sherwood & Euclid // 35 F/ALS/BAY CITY///<Unkno
 <CAD> - part 1 of 1/ Run# 0/ 800 S WASHINGTON AVE/ P-7 Long Distance/ 1/ Int-Overdose/Poisoning (Ingestion)/ EXTRAS: NONE... DECONDITIONING.../PT READY / PCS RE
 
 Contact: "chrisgrzegorczyk@hotmail.com" <chrisgrzegorczyk@hotmail.com>
 (<CAD> - part 1 of 1) Run# 55683/ 508 N MONROE ST/ P-3 Downgrade (No Lts/Sirens)/ Sick Person (Specific Diagnosis)/ 19/f/ ALS/ BAY CITY/ 1900 COLUMBUS A
 
  */
 
 
 public class MIMobileMedicalResponseParser extends DispatchProQAParser {
   
   private static final Pattern MISMATCH1_PTN = Pattern.compile("\\([^\\)]*$");
   private static final Pattern MISMATCH2_PTN = Pattern.compile("^[^\\(]*\\)");
   
   public MIMobileMedicalResponseParser() {
     super("", "MI", 
            "ADDR CALL! XTRA+? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "mailghost@mobilemedical.org,6344,6573";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     // dummy loop statement
     do {
       if (subject.startsWith("<CAD>")) break;
       if (body.startsWith("<CAD> - ")) {
         body = body.substring(8).trim();
         break;
       }
       return false;
     } while (false);
     
     return super.parseMsg(body, data);
   }
 
   // Field following call can be
   //   extension of call
   //   apt number
   //   or absent
   private class ExtraField extends Field {
 
     @Override
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       
       // if call has an open left paren, and this field has an open right paren
       // Assume that this is an extension of the call description split by
       // an inadvertent / and put them back together again
       if (MISMATCH1_PTN.matcher(data.strCall).find() &&
           MISMATCH2_PTN.matcher(field).find()) {
         data.strCall = data.strCall + '/' + field;
         return true;
       }
       
       // If field length <= 3, assume it is an apt
       if (field.length() <= 3) {
         data.strApt = field;
         return true;
       }
       
       // Otherwise we can ignore this field and treat data as a normal info field
       return false;
     }
 
     @Override
     public void parse(String field, Data data) {
       throw new RuntimeException("Not supported");
     }
     
     @Override
     public String getFieldNames() {
       return "APT";
     }
   }
   
   // Normal INFO field sometimes contain cross street info
   private class MyInfoField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       if (field.toUpperCase().startsWith("X ")) {
         data.strCross = append(data.strCross, " & ", field.substring(2).trim());
       } else {
         super.parse(field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "INFO X";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("XTRA")) return new ExtraField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }

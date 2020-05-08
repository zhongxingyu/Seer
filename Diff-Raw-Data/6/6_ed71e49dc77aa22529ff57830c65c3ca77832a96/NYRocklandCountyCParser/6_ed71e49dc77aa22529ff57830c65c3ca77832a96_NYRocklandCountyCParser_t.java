 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Rockland County, NY (alternate) (Hatzoloh EMS)
 Contact: "Mordy Eisenberg (Absolute Medical)" <mordy@absolutemed.com>, 8455177842@vtext.com
 Sender: dispatch@hatzolohems.org
 
 (ACR# 5141)Add: 475 New Hempstead Rd, \nX St: Near the intersection of New Hempstead Rd and Old Schoolhouse Rd\nName: Center-Rehab &amp; Nurse, Friedwald Ph
 (ACR# 5143)Add: 24 Deerwood Rd, \nX St: Between Cara Dr and Charlotte Dr\nName: Eisenberg, Mordechai\nPhone: 8453628456\nComp: \n9/9/2011 - 9:52 AM
 (ACR# 5146) Add: 10  WIDMAN CT, \nX St: Near the intersection of Widman Ct and Bluefield Dr\nName: 7782, \nPhone: \nComp: \n9/9/2011 - 9:55 AM
 (ACR# 5184) Add: 230 Kearsing Pkwy, \nX St: Between Jill Ln and RT-306\nName: Krausz, Arnold\nPhone: 8454267408\nComp: CHEST PAIN\n9/13/2011 - 10:17 AM
 (ACR# 5181) Add: 10 Melnick Dr, \nX St: Between Robert Pitt Dr and Spruill Ct\nName: Sale Furniture Warehouse, Whole\nPhone: 8453716664\nComp: EDP\n9/12/2011 -
(ACR# 5535) Addr: 24 Deerwood Rd\nX St: Between Cara Dr and Charlotte Dr\nName: Eisenberg, Mordechai\nPhone: 8453628456\nComp: MEDICAL\n10/22/20 11 - 8:27 PM 
 
 */
 
 public class NYRocklandCountyCParser extends FieldProgramParser {
   
   public NYRocklandCountyCParser() {
     super("ROCKLAND COUNTY", "NY",
        "Addr:ADDR! X_St:X Name:NAME Phone:PHONE Comp:SKIP");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@hatzolohems.org";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.startsWith("ACR# ")) return false;
     data.strCallId = subject.substring(5).trim();
    if (body.startsWith("Add:")) body = "Addr:" + body.substring(4);
     return parseFields(body.split("\n"), data);
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       super.parse(p.get(','), data);
       data.strCity = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " CITY";
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("Between ")) {
         field = field.substring(8).trim();
       } else if (field.startsWith("Near the intersection of ")) {
         field = "Near " + field.substring(25).trim();
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
     
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
 }

 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 
 public class PAChesterCountyAParser extends PAChesterCountyBaseParser {
   
   public PAChesterCountyAParser() {
     super("Final_Type:CALL! Loc:ADDRCITY! btwn:X? AKA:INFO");
   }
   
   @Override
   public String getFilter() {
     return "gallison39@comcast.net,messaging@iamresponding.com";
   }
 
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (isVariantGMsg(body)) return false;
     
     data.strSource = subject;
 
     // Replace key chars for easier parsing
     body = body.replace("\n"," ");
     body = body.replace(" btwn "," btwn:");
     body = body.replace("(V)", "");
     body = body.replaceAll("  +", " ");
     return super.parseMsg(body, data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressCityField extends AddressCityField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(" -- ");
       if (pt >= 0) {
         data.strPlace = field.substring(pt+4).trim();
         field = field.substring(0,pt).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDRCITY")) return new MyAddressCityField();
     return super.getField(name);
   }
 } 

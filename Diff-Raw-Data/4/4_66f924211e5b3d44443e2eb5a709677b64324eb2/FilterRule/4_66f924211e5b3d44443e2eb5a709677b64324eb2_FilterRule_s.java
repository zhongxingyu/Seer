 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Email;
 
 /**
  *
  * @author Bargavi
  */
 public class FilterRule {
 
     String ruleId;
     String fromText;
     String subjectText;
     String contentText;
     String moveToFolder;
 
     public boolean matches(String messageid) {
 
         MessageController controller = MessageController.getInstance();
        if (controller.getEmailHeader(messageid, "X-MeetingId") == "") {
            return false;
        }
         String from = controller.getEmailHeader(messageid, "From");
         String subject = controller.getEmailHeader(messageid, "Subject");
         String content = controller.getEmailContent(messageid);
 
         if ((from.toLowerCase().contains(fromText.toLowerCase()))
                 && (subject.toLowerCase().contains(subjectText.toLowerCase()))
                 && (content.toLowerCase().contains(contentText.toLowerCase()))) {
             return true;
         }
         return false;
     }
 
     public void setFromField(String fromText) {
         this.fromText = fromText;
     }
 
     public void setsubjectField(String subjectText) {
         this.subjectText = subjectText;
     }
 
     public void setcontentField(String contentText) {
         this.contentText = contentText;
     }
 
     public void setmoveToField(String moveToFolder) {
         this.moveToFolder = moveToFolder;
     }
 
     public String getFromField() {
         return this.fromText;
     }
 
     public String getsubjectField() {
         return this.subjectText;
     }
 
     public String getcontentField() {
         return this.contentText;
     }
 
     public String getmoveToField() {
         return this.moveToFolder;
     }
 
     public String getRuleId() {
         return this.ruleId;
     }
 
     public void setRuleId(String ruleId) {
         this.ruleId = ruleId;
     }
 }

 package org.sakaiproject.assignment2.tool.beans;
 
 import org.sakaiproject.assignment2.model.Assignment2;
 
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 public class Assignment2Validator  {
 	
 	public boolean validate(Assignment2 assignment, TargettedMessageList messages) {
 	  
 		boolean valid = true;
 		String key = "";
 		if (assignment.getAssignmentId() == null){
 			key = "new 1";
 		} else {
 			key = assignment.getAssignmentId().toString();
 		}
 		
 		
 		//check for empty title
 		if (assignment.getTitle() == null || assignment.getTitle().equals("")) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_title_empty",
 					new Object[] { assignment.getTitle() }, "Assignment2." + key + ".title"));
 			valid = false;
 		}
 		
 		//check for graded but no gradable object id
		if (!assignment.isUngraded() && (assignment.getGradableObjectId() == null || !assignment.getGradableObjectId().equals(""))) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_graded_no_gb_item", 
 					new Object[] {}, "Assignment2."+ key + ".gradableObjectId"));
 			valid = false;
 		}
 		
 		//check that the gradable object exists!
 		
 		return valid;
 	}
   
 }

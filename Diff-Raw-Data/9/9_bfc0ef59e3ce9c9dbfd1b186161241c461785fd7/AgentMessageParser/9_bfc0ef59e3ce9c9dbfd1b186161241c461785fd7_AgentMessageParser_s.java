 package edu.umich.sbolt.language;
 
 import java.util.Set;
 
 import edu.umich.sbolt.language.Patterns.LingObject;
 import edu.umich.sbolt.world.WorkingMemoryUtil;
 import sml.Identifier;
 
 public class AgentMessageParser
 {
     public static String translateAgentMessage(Identifier id){
         String message = null;
         String type = WorkingMemoryUtil.getValueOfAttribute(id, "type");
         Identifier fieldsId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "fields");
         if(type == null || fieldsId == null){
             return null;
         } else if(type.equals("different-attribute-question")){
             message = translateDifferentAttributeQuestion(fieldsId);
         } else if(type.equals("value-question")){
             message = translateValueQuestion(fieldsId);
         } else if(type.equals("common-attribute-question")){
             message = translateCommonAttributeQuestion(fieldsId);
         } else if(type.equals("attribute-presence-question")){
             message = translateAttributePresenceQuestion(fieldsId);
         } else if(type.equals("category-of-word")){
             message = translateCategoryQuestion(fieldsId);
         } else if(type.equals("describe-object")){
             message = translateDescription(fieldsId);
         } else if(type.equals("dont-know")){
         	message = "I don't know";
         } else if(type.equals("no-prep")){
            message = "I don't know that preposition. Please give an example:";
         } else if(type.equals("single-word")){
         	message = WorkingMemoryUtil.getValueOfAttribute(fieldsId, "word");
         } else if(type.equals("no-object")){
         	message = "I do not see the object you are talking about";
         } else if(type.equals("count-response")){
         	message = String.format("There are %d", Integer.parseInt(WorkingMemoryUtil.getValueOfAttribute(fieldsId, "count")));
         } else if(type.equals("unknown-message")){
         	message = "I was not able to understand your last message";
         } else if(type.equals("teaching-request")){
         	message = translateTeachingRequest(fieldsId);
         } else if(type.equals("which-question")){
         	message = translateWhichQuestion(fieldsId);
         }
         return message;
     }
     
     private static String translateTeachingRequest(Identifier id){
     	LingObject obj = LingObject.createFromSoarSpeak(id, "description");
    	return "I don't see " + obj.toString() + ".\nPlease teach me to recognize one";
     }
     
     private static String translateDifferentAttributeQuestion(Identifier id){
         Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
         String exceptionStr = getExceptionString(exceptions);
         LingObject differentObject = LingObject.createFromSoarSpeak(id, "different-object");
         Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "similar-object");
         String message = String.format("How does %s differ from ", differentObject.toString());
         for(LingObject obj : similarObjects){
             message += obj.toString() + "; ";
         }
         return exceptionStr + message;
     }
     
     private static String translateCommonAttributeQuestion(Identifier id){
         Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
         String exceptionStr = getExceptionString(exceptions);
         Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "object");
         String message = "What do ";
         for(LingObject obj : similarObjects){
             message += obj.toString() + "; ";
         }
         
         return exceptionStr + message + " have in common?";
     }
     
     private static String translateAttributePresenceQuestion(Identifier id){
         Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
         String exceptionStr = getExceptionString(exceptions);
         LingObject object = LingObject.createFromSoarSpeak(id, "object");
         String message = String.format("What attribute does %s have?", object.toString());
 
         return exceptionStr + message;
     }
     
     private static String translateCategoryQuestion(Identifier id){
         String word = WorkingMemoryUtil.getValueOfAttribute(id, "word");
         return String.format("What category does %s belong to?", word);
     }
     
     private static String translateValueQuestion(Identifier id){
         Identifier attRelationId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "attribute-relation");
         String objString = LingObject.createFromSoarSpeak(attRelationId, "object1").toString();
         String attribute = WorkingMemoryUtil.getValueOfAttribute(attRelationId, "word");
         
         return String.format("What %s is %s?", attribute, objString);
     }
     
     private static String getExceptionString(Set<String> exceptions){
         String exceptionStr = "";
         if (exceptions.size() > 0)
         {
             exceptionStr = "Other than ";
             for(String exception : exceptions){
                 exceptionStr += exception + ", ";
             }
             exceptionStr = exceptionStr.substring(0, exceptionStr.length() - 2);
             exceptionStr += "; ";
         }
         return exceptionStr;
     }
     
     private static String translateDescription(Identifier id){
     	
     	if(id == null){
             return null;
         }
     	
     	//kind of a hack :(
     	Identifier objectId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "object");
     	if (objectId == null)
     		return "nothing";
     	
         return LingObject.createFromSoarSpeak(id, "object").toString();
     }
     
     private static String translateWhichQuestion(Identifier id){
     	Identifier objectId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "description");
     	if (objectId == null)
     		return "Which one?";
     	
         return "Which " + LingObject.createFromSoarSpeak(id, "description") + "?";
     }
 }

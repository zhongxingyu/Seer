 package edu.umich.sbolt;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.soartech.bolt.testing.ActionType;
 
 import sml.*;
 import sml.Agent.OutputEventInterface;
 import sml.Agent.RunEventInterface;
 
 import abolt.lcmtypes.*;
 import april.util.TimeUtil;
 
 import edu.umich.sbolt.world.*;
 import edu.umich.sbolt.language.AgentMessageParser;
 import edu.umich.sbolt.language.Patterns.LingObject;
 
 public class OutputLinkHandler implements OutputEventInterface, RunEventInterface
 {
 	
     private List<training_label_t> newLabels;
 
     public OutputLinkHandler(BoltAgent boltAgent)
     {
         String[] outputHandlerStrings = { "message", "action", "pick-up", "push-segment", "pop-segment",
                 "put-down", "point", "send-message","remove-message","send-training-label", "set-state", 
                 "report-interaction", "home"};
 
         for (String outputHandlerString : outputHandlerStrings)
         {
         	boltAgent.getSoarAgent().AddOutputHandler(outputHandlerString, this, null);
         }
         
         boltAgent.getSoarAgent().RegisterForRunEvent(
                 smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
         
         newLabels = new ArrayList<training_label_t>();
     }
     
     public void runEventHandler(int eventID, Object data, Agent agent, int phase)
     {
     	Identifier outputLink = agent.GetOutputLink();
     	if(outputLink != null){
         	WMElement waitingWME = outputLink.FindByAttribute("waiting", 0);
         	ChatFrame.Singleton().setReady(waitingWME != null);
     	}
     	if(newLabels.size() > 0){
         	SBolt.broadcastTrainingData(newLabels);
         	newLabels.clear();
     	}
     }
     
     public List<training_label_t> extractNewLabels(){
     	if(newLabels.size() == 0){
     		return null;
     	} else {
     		List<training_label_t> retVal = newLabels;
     		newLabels = new ArrayList<training_label_t>();
     		return retVal;
     	}
     }
 
     @Override
     public void outputEventHandler(Object data, String agentName,
             String attributeName, WMElement wme)
     {
     	synchronized(this){
     		if (!(wme.IsJustAdded() && wme.IsIdentifier()))
             {
                 return;
             }
     		Identifier id = wme.ConvertToIdentifier();
             System.out.println(wme.GetAttribute());
             
 
             try{
 	            if (wme.GetAttribute().equals("set-state"))
 	            {
 	                processSetCommand(id);
 	            } 
 	            else if(wme.GetAttribute().equals("message")) 
 	            {
 	            	processMessage(id);
 	            }
 	            else if (wme.GetAttribute().equals("send-message"))
 	            {
 	                processOutputLinkMessage(id);
 	            }
 	            else if (wme.GetAttribute().equals("pick-up"))
 	            {
 	                processPickUpCommand(id);
 	            }
 	            else if (wme.GetAttribute().equals("put-down"))
 	            {
 	                processPutDownCommand(id);
 	            }
 	            else if (wme.GetAttribute().equals("point"))
 	            {
 	                processPointCommand(id);
 	            }
 	            else if (wme.GetAttribute().equals("remove-message"))
 	            {
 	            	processRemoveMesageCommand(id);
 	            }
 	            else if(wme.GetAttribute().equals("send-training-label"))
 	            {
 	            	processSendTrainingLabelCommand(id);
 	            } 
 	            else if(wme.GetAttribute().equals("push-segment"))
 	            {
 	            	processPushSegmentCommand(id);
 	            } 
 	            else if(wme.GetAttribute().equals("pop-segment"))
 	            {
 	            	processPopSegmentCommand(id);
 	            }
	            else if(wme.GetAttribute().equals("report-interaction"))
	            {
 	            	processReportInteraction(id);
 	            } else if(wme.GetAttribute().equals("home")){
 	            	processHomeCommand(id);
 	            }
 	            SBolt.Singleton().getBoltAgent().commitChanges();
             } catch (IllegalStateException e){
             	System.out.println(e.getMessage());
             }
     	}
     }
     
     private void processMessage(Identifier messageId) {
         Identifier cur = WorkingMemoryUtil.getIdentifierOfAttribute(messageId, "first");
         String msg = "";
         while(cur != null) {
         	String word = WorkingMemoryUtil.getValueOfAttribute(cur, "value");
         	if(word.equals(".") || word.equals("?") || word.equals("!") || word.equals(")"))
         		msg += word;
         	else if(msg.equals(""))
         		msg += word;
         	else
         		msg += " "+word;
         	cur = WorkingMemoryUtil.getIdentifierOfAttribute(cur, "next");
         }
         
         ChatFrame.Singleton().addMessage(msg, ActionType.Agent);
     }
 
 	private void processRemoveMesageCommand(Identifier messageId) {
 		int id = Integer.parseInt(WorkingMemoryUtil.getValueOfAttribute(messageId, "id", "Error (remove-message): No id"));
 		World.Singleton().destroyMessage(id);
 		messageId.CreateStringWME("status", "complete");
 	}
 
 	private void processOutputLinkMessage(Identifier messageId)
     {	
 		if (messageId == null)
         {
             return;
         }
 
         if (messageId.GetNumberChildren() == 0)
         {
             messageId.CreateStringWME("status", "error");
             throw new IllegalStateException("Message has no children");
         }
         
         if(WorkingMemoryUtil.getIdentifierOfAttribute(messageId, "first") == null){
         	processAgentMessageStructureCommand(messageId);
         } else {
         	processAgentMessageStringCommand(messageId);
         }
     }
 	
     private void processAgentMessageStructureCommand(Identifier messageId)
     {
         String type = WorkingMemoryUtil.getValueOfAttribute(messageId, "type",
                 "Message does not have ^type");
         String message = "";
         message = AgentMessageParser.translateAgentMessage(messageId);
         if(!message.equals("")){
             ChatFrame.Singleton().addMessage(message, ActionType.Agent);
         }
         messageId.CreateStringWME("status", "complete");
     }
 	
 	private void processAgentMessageStringCommand(Identifier messageId){
 
         String message = "";
         WMElement wordsWME = messageId.FindByAttribute("first", 0);
         if (wordsWME == null || !wordsWME.IsIdentifier())
         {
             messageId.CreateStringWME("status", "error");
             throw new IllegalStateException("Message has no first attribute");
         }
         Identifier currentWordId = wordsWME.ConvertToIdentifier();
 
         // Follows the linked list down until it can't find the 'rest' attribute
         // of a WME
         while (currentWordId != null)
         {
             Identifier nextWordId = null;
             for (int i = 0; i < currentWordId.GetNumberChildren(); i++)
             {
                 WMElement child = currentWordId.GetChild(i);
                 if (child.GetAttribute().equals("word"))
                 {
                     message += child.GetValueAsString() + " ";
                 }
                 else if (child.GetAttribute().equals("next")
                         && child.IsIdentifier())
                 {
                     nextWordId = child.ConvertToIdentifier();
                 }
             }
             currentWordId = nextWordId;
         }
 
         if (message == "")
         {
             messageId.CreateStringWME("status", "error");
             throw new IllegalStateException("Message was empty");
         }
 
         message += ".";
         ChatFrame.Singleton().addMessage(message.substring(0, message.length() - 1), ActionType.Agent);
 
         messageId.CreateStringWME("status", "complete");
     }
 
     /**
      * Takes a pick-up command on the output link given as an identifier and
      * uses it to update the internal robot_command_t command. Expects pick-up
      * ^object-id [int]
      */
     private void processPickUpCommand(Identifier pickUpId)
     {
         String objectIdStr = WorkingMemoryUtil.getValueOfAttribute(pickUpId,
                 "object-id", "pick-up does not have an ^object-id attribute");
         
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.action = String.format("GRAB=%d", Integer.parseInt(objectIdStr));
         command.dest = new double[6];
         SBolt.broadcastRobotCommand(command);
         pickUpId.CreateStringWME("status", "complete");
     }
 
     /**
      * Takes a put-down command on the output link given as an identifier and
      * uses it to update the internal robot_command_t command Expects put-down
      * ^location <loc> <loc> ^x [float] ^y [float] ^z [float]
      */
     private void processPutDownCommand(Identifier putDownId)
     {
         Identifier locationId = WorkingMemoryUtil.getIdentifierOfAttribute(
                 putDownId, "location",
                 "Error (put-down): No ^location identifier");
         double x = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                 locationId, "x", "Error (put-down): No ^location.x attribute"));
         double y = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                 locationId, "y", "Error (put-down): No ^location.y attribute"));
         double z = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                 locationId, "z", "Error (put-down): No ^location.z attribute"));
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.action = "DROP";
         command.dest = new double[]{x, y, z, 0, 0, 0};
         SBolt.broadcastRobotCommand(command);
         putDownId.CreateStringWME("status", "complete");
     }
 
     /**
      * Takes a set-state command on the output link given as an identifier and
      * uses it to update the internal robot_command_t command
      */
     private void processSetCommand(Identifier id)
     {
         String objId = WorkingMemoryUtil.getValueOfAttribute(id, "id",
                 "Error (set-state): No ^id attribute");
         String name = WorkingMemoryUtil.getValueOfAttribute(id,
                 "name", "Error (set-state): No ^name attribute");
         String value = WorkingMemoryUtil.getValueOfAttribute(id, "value",
                 "Error (set-state): No ^value attribute");
 
         String action = String.format("ID=%s,%s=%s", objId, name, value);
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.action = action;
         command.dest = new double[6];
         SBolt.broadcastRobotCommand(command);
 
         id.CreateStringWME("status", "complete");
     }
 
     private void processPointCommand(Identifier pointId)
     {
         Identifier poseId = WorkingMemoryUtil.getIdentifierOfAttribute(pointId, "pose",
         		"Error (point): No ^pose identifier");
         String x = WorkingMemoryUtil.getValueOfAttribute(poseId, "x",
         		"Error (point): No ^pose.x identifier");
         String y = WorkingMemoryUtil.getValueOfAttribute(poseId, "y",
         		"Error (point): No ^pose.y identifier");
         String z = WorkingMemoryUtil.getValueOfAttribute(poseId, "z",
         		"Error (point): No ^pose.z identifier");
         
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.dest = new double[]{Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z), 0, 0, 0};
     	command.action = "POINT";
     	SBolt.broadcastRobotCommand(command);
         
         pointId.CreateStringWME("status", "complete");
     }
     
     private void processSendTrainingLabelCommand(Identifier id){
     	Integer objId = Integer.parseInt(WorkingMemoryUtil.getValueOfAttribute(id, "id", 
     			"Error (send-training-label): No ^id attribute"));
     	String label = WorkingMemoryUtil.getValueOfAttribute(id, "label", 
     			"Error (send-training-label): No ^label attribute");
     	String category = WorkingMemoryUtil.getValueOfAttribute(id, "category", 
     			"Error (send-training-label): No ^category attribute");
     	training_label_t newLabel = new training_label_t();
     	Integer catNum = PerceptualProperty.getCategoryID(category);
     	if(catNum == null){
     		return;
     	}
     	
     	newLabel.cat = new category_t();
     	newLabel.cat.cat = catNum;
     	newLabel.id = objId;
     	newLabel.label = label;
     	
     	newLabels.add(newLabel);
     	id.CreateStringWME("status", "complete");
     }
     
     private void processPushSegmentCommand(Identifier id){
     	String type = WorkingMemoryUtil.getValueOfAttribute(id, "type", "Error (push-segment): No ^type attribute");
     	String originator = WorkingMemoryUtil.getValueOfAttribute(id, "originator", "Error (push-segment): No ^originator attribute");
     	SBolt.Singleton().getBoltAgent().getStack().pushSegment(type, originator);
     	id.CreateStringWME("status", "complete");
     }
     
     private void processPopSegmentCommand(Identifier id){
     	SBolt.Singleton().getBoltAgent().getStack().popSegment();
     	id.CreateStringWME("status", "complete");
     }
     
     private void processHomeCommand(Identifier id){
     	robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.dest = new double[6];
     	command.action = "HOME";
     	SBolt.broadcastRobotCommand(command);
         
         id.CreateStringWME("status", "complete");
     }
     
     private void processReportInteraction(Identifier id){
     	String type = WorkingMemoryUtil.getValueOfAttribute(id, "type");
     	String originator = WorkingMemoryUtil.getValueOfAttribute(id, "originator");
     	Identifier sat = WorkingMemoryUtil.getIdentifierOfAttribute(id, "satisfaction");
     	String eventType = sat.GetChild(0).GetAttribute();
     	String eventName = sat.GetChild(0).ConvertToIdentifier().FindByAttribute("type", 0).GetValueAsString();
     	Identifier context = WorkingMemoryUtil.getIdentifierOfAttribute(id, "context");
     	
     	String message = "";
     	if(type.equals("get-next-task")){
     		message = "I am idle and waiting for you to initiate a new interaction";
     	} else if(type.equals("get-next-subaction")){
     		message = "I cannot continue further with the current action and I need the next step";
     	} else if(type.equals("category-of-word")){
     		String word = WorkingMemoryUtil.getValueOfAttribute(context, "word");
     		message = "I do not know the category of " + word + ". " + 
     		"You can say something like 'a shape' or 'blue is a color'";
     	} else if(type.equals("which-question")){
     		String objStr = LingObject.createFromSoarSpeak(context, "description").toString();
     		message = "I see multiple examples of '" + objStr + "' and I need clarification";
     	} else if(type.equals("teaching-request")){
     		String objStr = LingObject.createFromSoarSpeak(context, "description").toString();
     		message = "I do not know '" + objStr + "' " + 
     		"Please give more teaching examples and tell me 'finished' when you are done";
     	}
     	ChatFrame.Singleton().addMessage(message, ActionType.Agent);
     }
 }

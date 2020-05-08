 package edu.umich.sbolt;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import sml.Agent.OutputEventInterface;
 import sml.Agent.RunEventInterface;
 import sml.Agent;
 import sml.Identifier;
 import sml.WMElement;
 import sml.smlRunEventId;
 import abolt.lcmtypes.category_t;
 import abolt.lcmtypes.robot_command_t;
 import abolt.lcmtypes.training_label_t;
 import edu.umich.sbolt.world.VisualProperty;
 import april.util.TimeUtil;
 import edu.umich.sbolt.world.WorkingMemoryUtil;
 import edu.umich.sbolt.world.WorldObject;
 import edu.umich.sbolt.language.AgentMessageParser;
 
 public class OutputLinkHandler implements OutputEventInterface, RunEventInterface
 {
 
     private SBolt sbolt;
     
     private List<training_label_t> newLabels;
 
     public OutputLinkHandler(SBolt sbolt)
     {
         this.sbolt = sbolt;
         String[] outputHandlerStrings = { "goto", "action", "pick-up", "push-segment", "pop-segment",
                 "put-down", "point", "send-message","remove-message","send-training-label", "set-state"};
         for (String outputHandlerString : outputHandlerStrings)
         {
            this.sbolt.getAgent().AddOutputHandler(outputHandlerString, this, null);
         }
         
 
         sbolt.getAgent().RegisterForRunEvent(
                 smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
         
         newLabels = new ArrayList<training_label_t>();
         
     }
     
     public void runEventHandler(int eventID, Object data, Agent agent, int phase)
     {
     	Identifier outputLink = agent.GetOutputLink();
     	if(outputLink != null){
         	WMElement waitingWME = outputLink.FindByAttribute("waiting", 0);
         	SBolt.getSingleton().getChatFrame().setReady(waitingWME != null);
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
 
 //    public robot_command_t getCommand()
 //    {
 //    	/*
 //        if (command.updateDest)
 //        {
 //            // Check to see if we've reached our destination and turn off
 //            // updateDest flag
 //            Robot robot = sbolt.getWorld().getRobot();
 //            double x = robot.getPose().getX();
 //            double y = robot.getPose().getY();
 //            double z = robot.getPose().getZ();
 //            double delta = (x - command.dest[0]) * (x - command.dest[0])
 //                    + (y - command.dest[1]) * (y - command.dest[1])
 //                    + (z - command.dest[2]) * (z - command.dest[2]);
 //            if (delta < .01)
 //            {
 //                command.updateDest = false;
 //            }
 //        }*/
 //    }
 
     @Override
     public void outputEventHandler(Object data, String agentName,
             String attributeName, WMElement wme)
     {
     	synchronized(this){
     		if (!(wme.IsJustAdded() && wme.IsIdentifier()))
             {
                 return;
             }
             System.out.println(wme.GetAttribute());
 
             if (wme.GetAttribute().equals("set-state"))
             {
                 processSetCommand(wme.ConvertToIdentifier());
             }
             else if (wme.GetAttribute().equals("send-message"))
             {
                 processOutputLinkMessage(wme.ConvertToIdentifier());
             }
             else if (wme.GetAttribute().equals("pick-up"))
             {
                 processPickUpCommand(wme.ConvertToIdentifier());
             }
             else if (wme.GetAttribute().equals("put-down"))
             {
                 processPutDownCommand(wme.ConvertToIdentifier());
             }
             else if (wme.GetAttribute().equals("point"))
             {
                 processPointCommand(wme.ConvertToIdentifier());
             }
             else if (wme.GetAttribute().equals("remove-message"))
             {
             	processRemoveMesageCommand(wme.ConvertToIdentifier());
             } else if(wme.GetAttribute().equals("send-training-label")){
             	processSendTrainingLabelCommand(wme.ConvertToIdentifier());
             } else if(wme.GetAttribute().equals("push-segment")){
             	processPushSegmentCommand(wme.ConvertToIdentifier());
             } else if(wme.GetAttribute().equals("pop-segment")){
             	processPopSegmentCommand(wme.ConvertToIdentifier());
             }
 
             if (this.sbolt.getAgent().IsCommitRequired())
             {
                 this.sbolt.getAgent().Commit();
             }
     	}
     }
 
 	private void processRemoveMesageCommand(Identifier messageId) {
 		
 		if (messageId == null)
 		{
 			return;
 		}
 		
 		sbolt.getWorld().destroyMessage(Integer.parseInt(messageId.FindByAttribute("id", 0).GetValueAsString()));
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
             sbolt.getChatFrame().addMessage(message);
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
         sbolt.getChatFrame().addMessage(
                 message.substring(0, message.length() - 1));
         messageId.CreateStringWME("status", "complete");
     }
 
     /**
      * Takes a pick-up command on the output link given as an identifier and
      * uses it to update the internal robot_command_t command. Expects pick-up
      * ^object-id [int]
      */
     private void processPickUpCommand(Identifier pickUpId)
     {
         if (pickUpId == null)
         {
             return;
         }
         
         String objectIdStr = WorkingMemoryUtil.getValueOfAttribute(pickUpId,
                 "object-id", "pick-up does not have an ^object-id attribute");
         
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.action = String.format("GRAB=%d", Integer.parseInt(objectIdStr));
         command.dest = new double[6];
         sbolt.broadcastRobotCommand(command);
        
         pickUpId.CreateStringWME("status", "complete");
     }
 
     /**
      * Takes a put-down command on the output link given as an identifier and
      * uses it to update the internal robot_command_t command Expects put-down
      * ^location <loc> <loc> ^x [float] ^y [float] ^z [float]
      */
     private void processPutDownCommand(Identifier putDownId)
     {
         if (putDownId == null)
         {
             return;
         }
         
         WMElement locationWME = putDownId.FindByAttribute("location", 0);
         if(locationWME == null){
             putDownId.CreateStringWME("status", "error");
             return;
         }
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         
         String action;
         if(locationWME.IsIdentifier()){
             Identifier locationId = WorkingMemoryUtil.getIdentifierOfAttribute(
                     putDownId, "location",
                     "put-down does not have a ^location identifier");
             double x = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                     locationId, "x",
                     "put-down.location does not have an ^x attribute"));
             double y = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                     locationId, "y",
                     "put-down.location does not have an ^y attribute"));
             double z = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                     locationId, "z",
                     "put-down.location does not have an ^z attribute"));
             command.action = "DROP";
             command.dest = new double[]{x, y, z, 0, 0, 0};
             sbolt.broadcastRobotCommand(command);
             
             putDownId.CreateStringWME("status", "complete");
         } else {
             putDownId.CreateStringWME("status", "error");	
         }
     }
 
     /**
      * Takes a set-state command on the output link given as an identifier and
      * uses it to update the internal robot_command_t command
      */
     private void processSetCommand(Identifier id)
     {
         if (id == null)
         {
             return;
         }
 
         String objId = WorkingMemoryUtil.getValueOfAttribute(id, "id",
                 "action does not have an ^id attribute");
         String name = WorkingMemoryUtil.getValueOfAttribute(id,
                 "name", "action does not have a ^name attribute");
         String value = WorkingMemoryUtil.getValueOfAttribute(id, "value",
                 "action does not have a ^value attribute");
 
         String action = String.format("ID=%s,%s=%s", objId, name, value);
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.action = action;
         command.dest = new double[6];
         sbolt.broadcastRobotCommand(command);
 
         id.CreateStringWME("status", "complete");
     }
 
     private void processPointCommand(Identifier pointId)
     {
         if (pointId == null)
         {
             return;
         }
         String objectIdStr = WorkingMemoryUtil.getValueOfAttribute(pointId, "id");
         
         Identifier poseId = WorkingMemoryUtil.getIdentifierOfAttribute(pointId, "pose");
         String x = WorkingMemoryUtil.getValueOfAttribute(poseId, "x");
         String y = WorkingMemoryUtil.getValueOfAttribute(poseId, "y");
         String z = WorkingMemoryUtil.getValueOfAttribute(poseId, "z");
         
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         if(x != null && y != null && z != null){
             command.dest = new double[]{Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z), 0, 0, 0};
         	command.action = "POINT";
         } else {
             pointId.CreateStringWME("status", "error");
             return;
         }
         sbolt.broadcastRobotCommand(command);
         
         pointId.CreateStringWME("status", "complete");
     }
     
     private void processSendTrainingLabelCommand(Identifier id){
     	Integer objId = Integer.parseInt(WorkingMemoryUtil.getValueOfAttribute(id, "id", "No id on send-training-label"));
     	String label = WorkingMemoryUtil.getValueOfAttribute(id, "label", "No label on send-training-label");
     	String category = WorkingMemoryUtil.getValueOfAttribute(id, "category", "No category on send-training-label");
     	
     	training_label_t newLabel = new training_label_t();
     	Integer catNum = VisualProperty.getCategoryType(category);
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
     	String type = WorkingMemoryUtil.getValueOfAttribute(id, "type", "No type on push-segment");
     	String originator = WorkingMemoryUtil.getValueOfAttribute(id, "originator", "No originator on push-segment");
     	sbolt.getChatFrame().getStack().pushSegment(type, originator);
     	id.CreateStringWME("status", "complete");
     }
     
     private void processPopSegmentCommand(Identifier id){
     	sbolt.getChatFrame().getStack().popSegment();
     	id.CreateStringWME("status", "complete");
     }
 }

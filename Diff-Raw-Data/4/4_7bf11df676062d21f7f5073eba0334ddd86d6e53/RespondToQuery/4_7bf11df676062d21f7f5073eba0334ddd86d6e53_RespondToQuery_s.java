 package jadeCW;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import jade.core.AID;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 public class RespondToQuery extends CyclicBehaviour {
 
 	private HospitalAgent hospitalAgent;
 	
 	/*
 	 *  Gets a request for a slot and returns the owner of the slot
 	 */
 	
 	public RespondToQuery(HospitalAgent hospitalAgent) {
 		super(hospitalAgent);
 		this.hospitalAgent = hospitalAgent;
 	}
 
 	@Override
 	public void action() {
 		String conversationId = "find-appointment-owner";
 		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
 				MessageTemplate.MatchConversationId(conversationId));
 
 		ACLMessage request = hospitalAgent.receive(reqTemplate);
 		
 		if (request != null){
 			
 			AID patientAgent = request.getSender();
 			Integer requestedSlot = Integer.parseInt(request.getContent());
 			ACLMessage reply = request.createReply();
 			
			if (requestedSlot > hospitalAgent.getAppointments().length) {
 				/*
 				 * Slot does not exist, should not come into this case!
 				 */
 				reply.setPerformative(ACLMessage.FAILURE);
 				reply.setContent("Slot not existent");
 				
 			} else {
 
 				AID owner = hospitalAgent.getAppointments()[requestedSlot];
 				reply.setPerformative(ACLMessage.INFORM);
 
 				if (owner == null) {
 					/*
 					 * Slot is not free, send the patient that owns it
 					 */
 					try {
 						reply.setContentObject(hospitalAgent.getAID());
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				} else {
 					/*
 					 * Slot is taken, send it's owner
 					 */
 					try {
 						reply.setContentObject(owner);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			hospitalAgent.send(reply);		
 			
 		}
 		else{
 			block();
 		}
 		
 	}
 
 }

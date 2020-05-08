 package pl.ug.airport.behaviours;
 
 import pl.ug.airport.helpers.AirportLogger;
 import pl.ug.airport.messages.AgentAddresses;
 import pl.ug.airport.messages.StringMessages;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 
 public class StaffAssignmentBehaviour extends Behaviour {
 
 	private String TAG = "StaffAssignment: ";
 	private Agent agent;
 	
 	public StaffAssignmentBehaviour(Agent _agent) {
 		this.agent = _agent;
 	}
 	
 	@Override
 	public void action() {
 		ACLMessage msg = myAgent.receive();
 		if (msg != null) {
 			StringMessages messageStr = StringMessages.parseString(msg.getContent());
 			messageHandler(messageStr);
 		}
 		else {
		block();
 		} 
 			
 	}
 
 	private void messageHandler(StringMessages messageStr) {
 		switch(messageStr) {
 		case REQUEST_CREW:
 			AirportLogger.log(TAG + " Sending crew");
			//send(AgentAddresses.getPassangerAgentAddress(1), StringMessages.RESPONSE_OK);
 			break;
 			
 		case LEAVING_AT:
 			AirportLogger.log(TAG + " Preparation for flight");
 			break;
 			
 		default:
 			AirportLogger.log(TAG + "Unknown message received " + messageStr);
 			break;
 
 	
 		}
 		
 	}
 	
 	private void sendCrew() {
 		StringMessages rsv = StringMessages.BOARD_PLANE;
 		send(AgentAddresses.getPlaneAgentAddress(1), rsv);
 		
 	}
 	
 	@Override
 	public boolean done() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	private void send(String address, StringMessages msgContent) {
 		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
 		msg.addReceiver(new AID(address, AID.ISLOCALNAME));
 		msg.setLanguage(AgentAddresses.getLang());
 		msg.setContent(msgContent.toString());
 		agent.send(msg);
 	}
 
 }

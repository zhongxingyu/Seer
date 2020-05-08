 package es.upm.dit.gsi.beast.mocks.bridgeMock;
 
import es.upm.dit.gsi.beast.mocks.common.AgentBehaviour;
import es.upm.dit.gsi.beast.mocks.common.MockAgentPlan;

 import jadex.base.fipa.SFipa;
 import jadex.bdi.runtime.IMessageEvent;
 import jadex.bridge.IComponentIdentifier;
 
 /**
 * Class to send a Inform message
  * 
  * @author Jorge Solitario
  */
 public class RequestCommunicationPlan extends MockAgentPlan {
 
 	/** Serial version UID of the serializable class CommunicationPlan. */
 	private static final long serialVersionUID = 4476473302410302L;
 
 	public void body() {
 		IMessageEvent actReq = (IMessageEvent) getReason();
 
 		String type = (String) actReq.getParameter("performative").getValue();
 		String agent_name = null;
 		try {
 			agent_name = (String) ((IComponentIdentifier) actReq.getParameter(
 					SFipa.SENDER).getValue()).getLocalName();
 		} catch (Exception e) {
 			System.out.println("Received message has no sender");
 		}
 		Object in_content = actReq.getParameter(SFipa.CONTENT).getValue();
 		// System.out.println("Type: "+type+" ---- Sender_name: "+agent_name+" ---- Content: "+in_content);
 
 		AgentBehaviour behaviour = (AgentBehaviour) getBeliefbase().getBelief(
 				"agent_behaviour").getFact();
 
 		String df_service;
 		String msgType;
 		Object out_content;
 		if (agent_name == null) {
 			df_service = (String) behaviour.processMessage(type, in_content);
 			msgType = (String) behaviour.processMessage(type, in_content);
 			out_content = behaviour.processMessage(type, in_content);
 		} else {
 			df_service = (String) behaviour.processMessage(type, agent_name,
 					in_content);
 			msgType = (String) behaviour.processMessage(type, agent_name,
 					in_content);
 			out_content = behaviour
 					.processMessage(type, agent_name, in_content);
 		}
 		// System.out.println("OUT: DF-Service "+
 		// df_service+"; MsgType "+msgType+"; Content " +out_content);
 
 		if (msgType == "SFipa.REQUEST")
 			sendRequestToDF(df_service, out_content);
 		if (type == "SFipa.INFORM")
 			sendInformToDF(df_service, out_content);
 	}
 
 	// /**
 	// * This method is an skeleton for future code-changes.
 	// * The idea is to process the arriving information (in_content) and create
 	// the output (out_content),
 	// * instead of being given directly by the Scenario.
 	// *
 	// * @param content The SFipa.CONTENT of the arriving message
 	// * @return The content of the new messages that are gonna be sended
 	// */
 	// private Object processContent(Object content) {
 	// return content;
 	// }
 }

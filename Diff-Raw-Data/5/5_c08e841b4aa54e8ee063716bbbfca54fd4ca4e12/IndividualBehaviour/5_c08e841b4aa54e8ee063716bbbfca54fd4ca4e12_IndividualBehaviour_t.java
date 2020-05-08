 package individual;
 
 import jade.core.AID;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 
 public abstract class IndividualBehaviour extends CyclicBehaviour implements messaging.Messaging {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Migration m = null;
 	
 	
 	
 	@Override
 	public void onStart(){
 		m = new Migration((Individual)myAgent);
 	}
 
 	@Override
 	public void action() {
 		ACLMessage msg = myAgent.blockingReceive();
 		if (msg == null) {
 			block();
 			return;
 		}
 
 		try {
 			//if (msg.getPerformative() == ACLMessage.INFORM) {
 			switch(msg.getPerformative()){
 				case ACLMessage.INFORM:{
 					String language = msg.getLanguage();
 					if(language.compareTo(CONTINUE_REPRODUCTION) == 0){
 						reproduce(msg.getContentObject());
 					}
 					else{
 						ACLMessage reply = msg.createReply();
 						String query = msg.getLanguage();
 						Individual indiv = (Individual)myAgent;
 						if(query.equals(START_DIE)) {
 							// TODO
 							double randVal = Math.random();
 							float prob = indiv.getSetting(settings.Vocabulary.Param.Survival);
 							if(indiv.age != 0) prob *= indiv.getSetting(settings.Vocabulary.Param.CompetitivenessFactorFirst);
 							if(randVal <= prob) {
 								reply.setPerformative(ACLMessage.REFUSE);
 								reply.setContent(NO);
								myAgent.send(reply);
 							}
 							else {
 								reply.setPerformative(ACLMessage.AGREE);
 								reply.setContent(YES);
								myAgent.send(reply);
 								myAgent.doDelete();
 							}
 						}
 						else
 						if(query.equals(DIE)){
 								myAgent.doDelete();
 						}
 						else{
 							reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
 							myAgent.send(reply);
 						}
 					}
 					break;
 				}
 				case ACLMessage.REQUEST:{
 					if(msg.getLanguage().compareTo("Migration") == 0){
 						m.action((AID)msg.getContentObject());
 					}
 					break;
 				}
 				default:{
 				}
 			} 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	protected abstract void reproduce(Object msgContent);
 
 }

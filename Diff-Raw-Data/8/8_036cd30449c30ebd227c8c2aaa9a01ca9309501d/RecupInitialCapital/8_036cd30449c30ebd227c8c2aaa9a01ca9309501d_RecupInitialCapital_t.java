 package behaviour;
 
 import jade.core.behaviours.OneShotBehaviour;
 import jade.core.behaviours.ParallelBehaviour;
 import jade.lang.acl.ACLMessage;
 import util.Logger;
 import agent.AgentJoueur;
 import behaviour.player.ActivePlayerBehaviour;
 import behaviour.player.PassivePlayerBehaviour;
 
 public class RecupInitialCapital extends OneShotBehaviour {
 	private static final long serialVersionUID = 1L;
 	private AgentJoueur agentJoueur;
 	private ACLMessage messageReceived;
 	private Object[] params;
 
 	public RecupInitialCapital(AgentJoueur agentJoueur, Object[] params) {
 		this.agentJoueur = agentJoueur;
 		this.params = params;
 	}
 
 	@Override
 	public void action() {
 		messageReceived = myAgent.blockingReceive();
 		if (messageReceived != null) { 
 			if(messageReceived.getPerformative() == ACLMessage.INFORM){ 
 				String line = messageReceived.getContent();
 				agentJoueur.setCapitalJoueur(Integer.parseInt(line));
				System.out.println("L'agent " + agentJoueur.getLocalName() +  " a recupere sa dotation initiale");
 			}
 			else
 				System.err.println("Le comportement RecupInitialCapital a reu un message imprvu de type : " + messageReceived);
 		} 
 	}
 	
 	public int onEnd(){ //Dmarre le comportement normal de l'agent joueur
 		System.out.println("Le joueur " + agentJoueur.getLocalName() +  " commence  jouer");
 		reset(); 
 		ParallelBehaviour parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
 		parallel.addSubBehaviour(new ActivePlayerBehaviour(agentJoueur, params));
 		parallel.addSubBehaviour(new PassivePlayerBehaviour(agentJoueur));
 		agentJoueur.addBehaviour(new ActivePlayerBehaviour(agentJoueur, params));
 	    return super.onEnd();
 	}
 }

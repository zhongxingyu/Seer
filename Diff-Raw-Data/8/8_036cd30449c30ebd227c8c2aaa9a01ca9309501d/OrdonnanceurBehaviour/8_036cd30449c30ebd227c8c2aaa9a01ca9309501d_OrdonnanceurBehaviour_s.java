 package behaviour;
 
 import jade.core.AID;
 import jade.core.behaviours.Behaviour;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.lang.acl.ACLMessage;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Vector;
 
 import util.Constantes;
 import view.Plateau;
 import agent.AgentMonopoly;
 
 public class OrdonnanceurBehaviour extends Behaviour {
 
 	private static final long serialVersionUID = 1L;
 	private Vector<DFAgentDescription> lesJoueurs;
 	private HashMap<DFAgentDescription, Integer> lesPositionsDesJoueurs;
 	private int currentTour;
 	private AID prison;
 	
 	public OrdonnanceurBehaviour(AgentMonopoly agentMonopoly,
 			Vector<DFAgentDescription> j, AID p) {
 		super(agentMonopoly);
 		lesJoueurs = j;
 		prison = p;
 		lesPositionsDesJoueurs = new HashMap<DFAgentDescription, Integer>();
 		for ( DFAgentDescription joueur : lesJoueurs ) {
 			lesPositionsDesJoueurs.put(joueur, 0);
 		}
 		currentTour = 0; 
 		// Tous les joueurs sont initialement sur la case depart
 		ACLMessage go = myAgent.blockingReceive(); 
 	}
 	
 	public void sendToJail(AID player) {
 		System.out.println("Envoi du joueur " + player + " en prison");
 		ACLMessage tick = new ACLMessage(ACLMessage.CONFIRM);
 		tick.addReceiver(prison);
 		try {
 			tick.setContentObject(player);
 			myAgent.send(tick);
 		} 
 		catch (IOException e) { System.out.println(e.getMessage()); }
 	}
 	
 	public void libererJoueur(AID player) {
 		System.out.println("Liberation du joueur " + player + " emprisonne");
 		ACLMessage tick = new ACLMessage(ACLMessage.DISCONFIRM);
 		tick.addReceiver(prison);
 		try {
 			tick.setContentObject(player);
 			myAgent.send(tick);
 		} 
 		catch (IOException e) { System.out.println(e.getMessage()); }
 	}
 
 
 	@Override
 	public void action() {  
 		DFAgentDescription joueur = lesJoueurs.get(currentTour); 
 		
 		// Envoi d'un message au joueur pour qu'il lance les des
 		throwDice(joueur);
  
 		// Reception du score fait par le joueur
 		ACLMessage message = myAgent.blockingReceive(); 
 		// Deplacement du pion du joueur
 		if ( message != null ) { 
 			if ( message.getPerformative() == ACLMessage.INFORM) { 
 				Integer value = lesPositionsDesJoueurs.get(joueur);
 				Integer diceValue = Integer.parseInt(message.getContent());
 				Integer newPos;
 
 				// Le joueur doit aller sur la case prison
 				if ( diceValue < 0 ) {
 					newPos = Constantes.CASE_PRISON;
 					sendToJail(joueur.getName());
 				}
 				
 				// Cas ou le joueur est en prison : il doit faire 12 pour en sortir 
 				if ( value == Constantes.CASE_PRISON && diceValue != 12 ) {
 					newPos = Constantes.CASE_PRISON; // S'il n'a pas fait 12, il reste sur sa case
 				}
 				else {
 					// Le joueur peut se deplacer
 					if ( value == Constantes.CASE_PRISON ) { // On envoie un message a l'agent prison pour liberer le joueur
 						libererJoueur(joueur.getName());
 					}
 					if ( value + diceValue < Constantes.CASE_FIN ) {
 						newPos = value + diceValue;
 					}
 					else {
 						// On a fait un tour de plateau -> donner de l'argent au joueur !
 						newPos = value + diceValue - Constantes.CASE_FIN;
 					}
 				}
 				lesPositionsDesJoueurs.put(joueur, newPos);
 				// L'agent Monopoly envoie au joueur la case
 				ACLMessage caseCourante = new ACLMessage(ACLMessage.INFORM_REF);
 				Plateau p = ((AgentMonopoly) myAgent).getPlateau();
 				try {
 					caseCourante.setContentObject(p.getCase(newPos));
					System.err.println(caseCourante);
 					caseCourante.addReceiver(joueur.getName());
 					myAgent.send(caseCourante);
 				} 
 				catch (IOException e1) { e1.printStackTrace(); }
 				try {
 					Thread.sleep(Constantes.DUREE_ANIMATION);
 				} catch (InterruptedException e) { 
 					e.printStackTrace();
 				}
 			}
 			else if (message.getPerformative() == ACLMessage.INFORM_REF)
 			{
 				// Joueur en faillite
 			}
 		}  
 		// On passe au joueur suivant
 		tourSuivant();
 	}
 
 	private void throwDice(DFAgentDescription joueur) {
 		ACLMessage tick = new ACLMessage(ACLMessage.PROPAGATE);
 		tick.addReceiver(joueur.getName());
 		myAgent.send(tick);
 	}
 
 	private void tourSuivant() {
 		currentTour ++;
 		if ( currentTour >= Constantes.NB_JOUEURS ) {
 			currentTour = 0;
 		}
 	}
 
 	@Override
 	public boolean done() {
 		// A voir
 		return false;
 	}
 
 }

 package pl.sskucinski.jade.agents;
 
 import jade.content.Concept;
 import jade.content.ContentElement;
 import jade.content.lang.Codec;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.basic.Action;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.core.behaviours.SequentialBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import pl.sskucinski.jade.interfaces.CarsBase;
 import pl.sskucinski.jade.ontology.CarOntology;
 import pl.sskucinski.jade.utils.Available;
 import pl.sskucinski.jade.utils.Rent;
 
 public class CarDealerAgent extends Agent implements CarsBase {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Codec codec = new SLCodec();
 	private Ontology ontology = CarOntology.getInstance();
 	
 	protected void setup() {
 		getContentManager().registerLanguage(codec);
 		getContentManager().registerOntology(ontology);
 		
 		SequentialBehaviour sb = new SequentialBehaviour();
 	    sb.addSubBehaviour(new RegisterIn(this));
 	    sb.addSubBehaviour(new ReceiveMessage(this));
 	    addBehaviour(sb);
 	}
 	
 	public void agentBadReply(ACLMessage msg) {
 		
 		try {
 	         java.io.Serializable content = msg.getContentObject();
 	         ACLMessage reply = msg.createReply();
 	         reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
 	         reply.setContentObject(content);
 	         send(reply);
 	      }
 	      catch(Exception ex) { 
 	    	  ex.printStackTrace();
 	      }
 		
 	}
 	
 	// Behavior of register in
 	
 	 class RegisterIn extends OneShotBehaviour {
 		 
 		RegisterIn(Agent a) {
 	         super(a);
 	    }
 
 		@Override
 		public void action() {
 			
 			ServiceDescription sd = new ServiceDescription();
 			sd.setType(SERVER_AGENT);
 	        sd.setName(getName());
 	        sd.setOwnership("Test0332");
 	        DFAgentDescription dfd = new DFAgentDescription();
 	        dfd.setName(getAID());
 	        dfd.addServices(sd);
 	        try {
 	            DFAgentDescription[] dfds = DFService.search(myAgent, dfd);
 	            if (dfds.length > 0 ) {
 	               DFService.deregister(myAgent, dfd);
 	            }
 	            DFService.register(myAgent, dfd);
 	            System.out.println(getLocalName() + " is ready.");
 	         }
 	         catch (Exception ex) {
 	            System.out.println("Failed registering! Shutting down...");
 	            ex.printStackTrace();
 	            doDelete();
 	         }
 		}
 		 
 		 
 		 
 	 }
 	
 	// Behavior of receiving messages
 	
 	class ReceiveMessage extends CyclicBehaviour {
 		
 		public ReceiveMessage (Agent a){
 			super(a);
 		}
 
 		@Override
 		public void action() {
 			ACLMessage msg = receive();
 			
 			if(msg == null) {
 				block();
 				return;
 			}
 			
 			try {
 				
 				ContentElement content = getContentManager().extractContent(msg);
 				Concept action = ((Action)content).getAction();
 				
 				switch (msg.getPerformative()) {
 				case (ACLMessage.REQUEST):
 					System.out.println("Request from " + msg.getSender().getLocalName());
 					
					if (content instanceof Available) {
 						
 						addBehaviour(new HandleAvailable(myAgent, msg));
 						
 					} else if (content instanceof Rent) {
 						
 						addBehaviour(new HandleRentCar(myAgent, msg));
 						
 					} else {
 						
 						agentBadReply(msg);
 						break;
 						
 					}
 				
 				}
 				
 			} catch (Exception e) {
 				
 				e.getStackTrace();
 				
 			}
 			
 			
 		}
 		
 	}
 	
 	class HandleAvailable extends OneShotBehaviour {
 		
 		private ACLMessage request;
 
 	      HandleAvailable(Agent a, ACLMessage request) {
 
 	         super(a);
 	         this.request = request;
 	      }
 
 		@Override
 		public void action() {
 			try{
 				
 				ContentElement content = getContentManager().extractContent(request);
				Available av = (Available) ((Action)content).getAction();
 				
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 	
 	class HandleRentCar extends OneShotBehaviour {
 		
 		private ACLMessage request;
 
 	      HandleRentCar(Agent a, ACLMessage request) {
 
 	         super(a);
 	         this.request = request;
 	      }
 
 		@Override
 		public void action() {
 			
 			try{
 				
 				ContentElement content = getContentManager().extractContent(request);
 				Rent rt = (Rent) ((Action)content).getAction();
 				
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 	
 
 }

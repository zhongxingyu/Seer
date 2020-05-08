 package pikater;
 
 import jade.content.ContentElement;
 import jade.content.lang.Codec;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.content.onto.basic.Result;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.RefuseException;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREInitiator;
 import jade.proto.AchieveREResponder;
 import jade.proto.IteratedAchieveREInitiator;
 import jade.proto.SubscriptionResponder.Subscription;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
import jade.util.leap.LinkedList;
 import jade.util.leap.List;
 import jade.wrapper.AgentController;
 import jade.wrapper.ControllerException;
 import jade.wrapper.PlatformController;
 import jade.wrapper.StaleProxyException;
 
 import java.util.Date;
 import java.util.Vector;
 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 
 import pikater.ontology.messages.Computation;
 import pikater.ontology.messages.Compute;
 import pikater.ontology.messages.CreateAgent;
 import pikater.ontology.messages.Data;
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.Execute;
 import pikater.ontology.messages.ExecuteParameters;
 import pikater.ontology.messages.GetNextParameters;
 import pikater.ontology.messages.GetOptions;
 import pikater.ontology.messages.Options;
 import pikater.ontology.messages.MessagesOntology;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.Results;
 import pikater.ontology.messages.Solve;
 import pikater.ontology.messages.Task;
 
 public class Agent_OptionsManager extends Agent {
 
 	private Codec codec = new SLCodec();
 	private Ontology ontology = MessagesOntology.getInstance();
 	
 	private String search_agent_name;
 	private Computation computation;
 	
 	private String trainFileName;
 	private String testFileName;
 
 	private Computation receivedComputation;
 
 	private String receiver;
 	private String problem_id;
 	private String start;
 
 	private List evaluations = new ArrayList();
 	private List options = new ArrayList();		
 	
 	private List results = new ArrayList();
 	
 	protected float error_rate = (float) 0.3;
 	protected int maximum_tries = 10;
 
 	private int task_i = 0; // task number
 
 	private long timeout = -1;
 
 	boolean working = false;
 	boolean finished = false;
 	protected pikater.ontology.messages.Evaluation evaluation;
 	protected List Options;
 	protected pikater.ontology.messages.Agent Agent;
 
 	private ACLMessage msgPrev = new ACLMessage(ACLMessage.FAILURE);
 	private boolean sendAgain = false;
 
 	protected String getAgentType() {
 		return "Option Manager";
 	}
 
 	protected void executeTasks(List next_options_list){
 		evaluations = new ArrayList();  // premazani Listu, kdyz prijde vic pozadavku najednou (coz by se zatim nemelo dit)
 		options = new ArrayList();
 		
 		Iterator itr = next_options_list.iterator();
 		while (itr.hasNext()) {
 			List next_options = ((Options) itr.next()).getList();
 			next_options = addMutableOptions(next_options);
 			System.out.println("Next options for agent " + computation.getAgent().getName() + " received:");
 			Iterator no_itr = next_options.iterator();
 			while (no_itr.hasNext()) {
 				Option next = (Option) no_itr.next();
 				System.out.println("   " + next.getName() + ": " + next.getValue());							
 			}
 
 			// send request to the computing agent(s) (add behavior for each options)
 			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
 			msg.addReceiver(new AID(computation.getAgent().getName(), false));
 			msg.setLanguage(codec.getName());
 			msg.setOntology(ontology.getName());
 			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
 
 			Execute execute = new Execute();
 			Task task = new Task();
 			// change options
 			pikater.ontology.messages.Agent agent = computation.getAgent();
 			agent.setOptions(next_options);
 			task.setAgent(agent);
 			
 			String id = computation.getId() + "_" + task_i;
 			task_i++;
 			task.setId(id);							
 			task.setComputation_id(computation.getId());
 			task.setProblem_id(computation.getProblem_id());
 			task.setData(computation.getData());
 			task.setGet_results(computation.getGet_results());
 			task.setGui_agent(computation.getGui_agent());
 			task.setSave_results(computation.getSave_results());
 			task.setStart(getDateTime());
 			
 			execute.setTask(task);
 			
 			Action a = new Action();
 			a.setAction(execute);
 			a.setActor(this.getAID());
 					
 			try {
 				getContentManager().fillContent(msg, a);
 			} catch (CodecException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			} catch (OntologyException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}	
 				
 			ACLMessage reply = null;
 			try {
 				reply = FIPAService.doFipaRequestClient(this, msg);
 			} catch (FIPAException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}		
 			if (reply != null && reply.getPerformative() == ACLMessage.INFORM){
 				if (reply != null) {
 					ContentElement content;
 					try {
 						content = getContentManager().extractContent(reply);
 						if (content instanceof Result) {
 							Result result = (Result) content;
 							if (result.getValue() instanceof pikater.ontology.messages.Evaluation) {														
 								Evaluation ev = (pikater.ontology.messages.Evaluation) result.getValue();
 								evaluations.add(ev);
 								task.setResult(ev);
 								task.setFinish(getDateTime());
 								results.add(task);
 							}
 						}
 					} catch (UngroundedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (CodecException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (OntologyException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}	
 			}
 			else{
 				// TODO 
 				System.err.println("Agent " + this.getLocalName() + ": computing agent "
 						+ reply.getSender().getLocalName() + "didn't execute the task.");
 			}
 		}
 	}
 
 	protected class RequestServer extends CyclicBehaviour {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1902726126096385876L;
 		/**
 			 * 
 			 */
 		private MessageTemplate resMsgTemplate = MessageTemplate
 				.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
 						MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
 								MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
 										MessageTemplate.MatchOntology(ontology.getName()))));
 
 		public RequestServer(Agent agent) {			
 			super(agent);
 		}
 
 		@Override 
 		public void action() {
 			
 			ACLMessage request = receive(resMsgTemplate);
 			if (request != null) {
 				try {
 					ContentElement content = getContentManager().extractContent(request);
 					if (((Action) content).getAction() instanceof Compute) {
 						ACLMessage response = request.createReply();
 						response.setPerformative(ACLMessage.AGREE);
 						send(response);
 						
 						Compute compute = (Compute) (((Action) content).getAction());
 						computation = compute.getComputation();
 						Options = computation.getAgent().getOptions(); 
 						
 						// create search agent												
 						ACLMessage msg_ca = new ACLMessage(ACLMessage.REQUEST);
 						msg_ca.addReceiver(new AID("agentManager", false));
 						msg_ca.setLanguage(codec.getName());
 						msg_ca.setOntology(ontology.getName());
 						CreateAgent ca = new CreateAgent();
 						ca.setType(computation.getMethod().getType());
 												
 						Action a = new Action();
 						a.setAction(ca);
 						a.setActor(myAgent.getAID());
 								
 						String search_agent_name = null;
 						try {
 							getContentManager().fillContent(msg_ca, a);	
 							ACLMessage msg_name = FIPAService.doFipaRequestClient(myAgent, msg_ca);
 							search_agent_name = msg_name.getContent();
 						} catch (FIPAException e) {
 							System.err.println("Exception while adding agent"
 									+ computation.getId() + ": " + e);		
 						} catch (CodecException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (OntologyException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						// send request to the search agent
 						
 						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
 						msg.addReceiver(new AID(search_agent_name, false));
 						msg.setLanguage(codec.getName());
 						msg.setOntology(ontology.getName());
 						msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
 
 						GetNextParameters gnp = new GetNextParameters();
 						gnp.setOptions(getMutableOptions(computation.getAgent().getOptions()));						
 						gnp.setSearch_options(computation.getMethod().getOptions());
 						
 						a = new Action();
 						a.setAction(gnp);
 						a.setActor(myAgent.getAID());
 								
 						getContentManager().fillContent(msg, a);	
 
 						addBehaviour(new StartGettingParameters(myAgent, msg, request));
 						
 						return;
 					}
 					if (((Action) content).getAction() instanceof ExecuteParameters) {
 						// options manager received options to execute
 						
 						ExecuteParameters ep = (ExecuteParameters) (((Action) content).getAction());		             
 						// go through list of Options, merge it with the immutable Options						
 						List next_options_list = ep.getParameters(); // 2d list
 
 						executeTasks(next_options_list);
 						
 						// send reply to search agent
 						ACLMessage eval_msg = request.createReply();
 						eval_msg.setPerformative(ACLMessage.INFORM);
 						List l = new ArrayList();
 						l.add(options);
 						l.add(evaluations); // ! evaluations je prazdnej list TODO
 						System.out.println("EEE2"+ evaluations);
 						Result result = new Result((Action) content, l);								
 						
 						getContentManager().fillContent(eval_msg, result);
 						send(eval_msg);
 						
 						return;
 					}
 
 				} catch (CodecException ce) {
 					ce.printStackTrace();
 				} catch (OntologyException oe) {
 					oe.printStackTrace();
 				}
 				ACLMessage result_msg = request.createReply();
 				result_msg.setPerformative(ACLMessage.NOT_UNDERSTOOD);
 				send(result_msg);
 				return;
 			} else {
 				block();
 			}
 		}
 	}
 	
 	private class SendExecuteTask extends AchieveREInitiator {
 		
 		public SendExecuteTask(Agent a, ACLMessage request, Options opt) {
 			super(a, request);
 			options.add(opt);
 			System.out.println(a.getLocalName()
 					+ ": SendExecuteTask behavior created.");
 		}
 
 		
 		protected void handleInform(ACLMessage inform) {
 			System.out.println(getLocalName() + ": Agent "
 					+ inform.getSender().getName() + ": sent results.");
 			
 			if (inform != null) {
 				ContentElement content;
 				try {
 					content = getContentManager().extractContent(inform);
 					if (content instanceof Result) {
 						Result result = (Result) content;
 						if (result.getValue() instanceof pikater.ontology.messages.Evaluation) {														
 							Evaluation ev = (pikater.ontology.messages.Evaluation) result.getValue();
 							evaluations.add(ev);
 						}
 					}
 				} catch (UngroundedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (CodecException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (OntologyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}			
 		}
 
 		protected void handleRefuse(ACLMessage refuse) {
 			System.out.println(getLocalName() + ": Agent "
 					+ refuse.getSender().getName()
 					+ " refused to perform the requested action");
 		}
 
 		protected void handleFailure(ACLMessage failure) {
 		}
 
 	};
 
 	private class StartGettingParameters extends AchieveREInitiator {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -2796507853769993352L;
 		private ACLMessage request;
 		private ACLMessage original_request;
 		
 		public StartGettingParameters(Agent a, ACLMessage _request, ACLMessage _original_request) {
 			super(a, _request);
 			System.out.println(a.getLocalName()
 					+ ": StartGettingParameters behavior created.");
 			request = _request;
 			original_request = _original_request;
 		}
 
 		
 		protected void handleInform(ACLMessage inform) {
 			System.out.println(getLocalName() + ": Agent "
 					+ inform.getSender().getName() + ": sending of Options have been finished.");
 			// sending of Options have been finished -> send message to Manager
 			
 			ACLMessage msgOut = original_request.createReply();
 			msgOut.setPerformative(ACLMessage.INFORM);
 			
 			// prepare the outgoing message content:
 			Results _results = new Results();
 			_results.setResults(results);
 			_results.setComputation_id(computation.getId());
 			_results.setProblem_id(computation.getProblem_id());
 
 			ContentElement content;
 				try {
 					content = getContentManager().extractContent(original_request);
 					Result result = new Result((Action) content, _results);
 					getContentManager().fillContent(msgOut, result);
 					
 					send(msgOut);
 					
 				} catch (UngroundedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (CodecException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (OntologyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}			
 		}
 
 		protected void handleRefuse(ACLMessage refuse) {
 			System.out.println(getLocalName() + ": Agent "
 					+ refuse.getSender().getName()
 					+ " refused to perform the requested action");
 			// + preposlat zpravu managerovi
 		}
 
 		protected void handleFailure(ACLMessage failure) {
 			// preposlat zpravu managerovi
 		}
 
 	};
 	
 	protected boolean registerWithDF() {
 		// register with the DF
 
 		DFAgentDescription description = new DFAgentDescription();
 		// the description is the root description for each agent
 		// and how we prefer to communicate.
 
 		description.setName(getAID());
 		// the service description describes a particular service we
 		// provide.
 		ServiceDescription servicedesc = new ServiceDescription();
 		// the name of the service provided (we just re-use our agent name)
 		servicedesc.setName(getLocalName());
 
 		// The service type should be a unique string associated with
 		// the service.s
 		String typeDesc = getAgentType();
 
 		servicedesc.setType(typeDesc);
 
 		// the service has a list of supported languages, ontologies
 		// and protocols for this service.
 		// servicedesc.addLanguages(language.getName());
 		// servicedesc.addOntologies(ontology.getName());
 		// servicedesc.addProtocols(InteractionProtocol.FIPA_REQUEST);
 
 		description.addServices(servicedesc);
 
 		// add "OptionsManager agent service"
 		ServiceDescription servicedesc_g = new ServiceDescription();
 
 		servicedesc_g.setName(getLocalName());
 		servicedesc_g.setType("OptionsManager");
 		description.addServices(servicedesc_g);
 
 		// register synchronously registers us with the DF, we may
 		// prefer to do this asynchronously using a behaviour.
 		try {
 			DFService.register(this, description);
 			System.out.println(getLocalName()
 					+ ": successfully registered with DF; service type: "
 					+ typeDesc);
 			return true;
 		} catch (FIPAException e) {
 			System.err.println(getLocalName()
 					+ ": error registering with DF, exiting:" + e);
 			// doDelete();
 			return false;
 
 		}
 	} // end registerWithDF
 
 	@Override
 	protected void setup() {
 		System.out.println(getLocalName() + " is alive...");
 
 		getContentManager().registerLanguage(codec);
 		getContentManager().registerOntology(ontology);
 
 		registerWithDF();
 			
 		addBehaviour(new RequestServer(this));
 
 	} // end setup
 
 	private String getImmutableOptions() {
 		String str = "";
 		Iterator itr = Options.iterator();
 		while (itr.hasNext()) {
 			Option next_option = (Option) itr.next();
 			if (!next_option.getMutable() && next_option.getValue() != null) {
 				if (next_option.getData_type().equals("BOOLEAN")
 						&& next_option.getValue().equals("True")) {
 					str += "-" + next_option.getName() + " ";
 				} else {
 					str += "-" + next_option.getName() + " "
 							+ next_option.getValue() + " ";
 				}
 			}
 		}
 		return str;
 	}
 	
 	private List getMutableOptions(List Options){
 		List mutable = new ArrayList();
 		Iterator itr = Options.iterator();
 		while (itr.hasNext()) {
 			Option o = (Option) itr.next();
 			if (o.getMutable()){				
 				mutable.add(o);
 			}
 		}
 		return mutable;
 	}
 	
 	private List addMutableOptions(List newOptions){
 		// copy Options

                if (newOptions == null)
                    newOptions = new LinkedList();
 		List _Options = new ArrayList();
 		Iterator itr = Options.iterator();
 		while (itr.hasNext()) {
 			Option opt = (Option) itr.next();
 			_Options.add(opt);		
 		}
 		
 		itr = _Options.iterator();
 		while (itr.hasNext()) {
 			Option opt = (Option) itr.next();
 			
 			Iterator itr_new = newOptions.iterator();
 			while (itr_new.hasNext()) {
 				Option opt_new = (Option) itr_new.next();			
 				if (opt.getName().equals(opt_new.getName())){				
 					opt.setValue(opt_new.getValue());					
 				}
 			}
 		}
 		return _Options;
 	}
 
     private String getDateTime() {
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
         Date date = new Date();
         return dateFormat.format(date);
     }    
 
 }

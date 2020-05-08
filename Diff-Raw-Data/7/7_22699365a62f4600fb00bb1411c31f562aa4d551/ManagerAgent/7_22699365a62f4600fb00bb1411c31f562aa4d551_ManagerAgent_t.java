 package pikater.agents.management;
 
 import jade.content.lang.Codec.CodecException;
 import jade.content.onto.OntologyException;
 import jade.content.onto.basic.Action;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.RefuseException;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREResponder;
 import jade.util.leap.List;
 import jade.wrapper.AgentController;
 import jade.wrapper.ControllerException;
 import jade.wrapper.PlatformController;
 import pikater.agents.PikaterAgent;
 import pikater.configuration.Argument;
 import pikater.ontology.messages.CreateAgent;
 import pikater.ontology.messages.LoadAgent;
 import pikater.ontology.messages.SaveAgent;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ManagerAgent extends PikaterAgent {
 	private Map<String, AgentTypeDefinition> agentTypes = new HashMap<>();
     private AgentTypesProvider agentTypesProvider=(AgentTypesProvider)context.getBean("agentTypesProvider");
     private ManagerAgentRequestResponder responder=new ManagerAgentRequestResponder(this);
 
 	@Override
	protected void setup() {
        initDefault();
 		File data = new File("saved");
         if (!data.exists()) {
             log("Creating directory saved");
             if (data.mkdirs()) {
                 log("Succesfully created directory saved");
             } else {
                 logError("Error creating directory saved");
             }
         }

         registerWithDF();
         
 		getAgentTypesFromFile();
 		
 		MessageTemplate mt = MessageTemplate.and(MessageTemplate
 				.MatchOntology(ontology.getName()), MessageTemplate
 				.MatchPerformative(ACLMessage.REQUEST));
 
 		addBehaviour(new AchieveREResponder(this, mt) {
 
 			private static final long serialVersionUID = 7L;
 
 			@Override
 			protected ACLMessage handleRequest(ACLMessage request)
 					throws NotUnderstoodException, RefuseException {
 				try {
 					Action a = (Action) getContentManager().extractContent(request);
                     if (a.getAction() instanceof LoadAgent) {
                         return responder.RespondToLoadAction(request);
 					}
 					else if (a.getAction() instanceof SaveAgent) {
 						// write it into database
                         return responder.RespondToSaveAction(request);
 					}
 				 	else if (a.getAction() instanceof CreateAgent){
                         return responder.RespondToCreateAction(request);
 					}
 				} catch (OntologyException e) {
 					e.printStackTrace();
 					logError("Problem extracting content: " + e.getMessage());
 				} catch (CodecException e) {
 					e.printStackTrace();
 					logError("Codec problem: " + e.getMessage());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				ACLMessage failure = request.createReply();
 				failure.setPerformative(ACLMessage.FAILURE);
 				logError("Failure responding to request: " + request.getContent());
 				return failure;
 			}
 		});
 	}
     
 	public String createAgent(String type, String name, List args){
 		// get a container controller for creating new agents
 		PlatformController container = getContainerController();				
 
 		Argument[] args1 = new Argument[0];
         Argument[] args2 = new Argument[0];
 
 		if (agentTypes.get(type).getOptions() != null){
 			args1 = agentTypes.get(type).getOptions();
 		}
 		
 		if (args != null){
             args2=new Argument[args.size()];
             for (int i=0;i<args.size();i++)
             {
                 args2[i]=(Argument)args.get(i);
             }
 		}
 		
 		int size = args1.length + args2.length;
 		Argument[] Args = new Argument[size];
         System.arraycopy(args1,0,Args,0,args1.length);
         System.arraycopy(args2,0,Args,args1.length,args2.length);
 		String nameToGenerate=name = generateName(name);
 			try {
                 String agentType=agentTypes.get(type).getTypeName();
                 if (agentType==null){
                     agentType=type;
                 }
 				AgentController agent = container.createNewAgent(name, agentType, Args);
 				agent.start();
 			} catch (ControllerException e) {
 				 e.printStackTrace();
             }
 		// provide agent time to register with DF etc.
 		doWait(300);
 		return nameToGenerate;
 	}
 
 	private String generateName(String name) {
 		int i = 0;
 		while (name.charAt(name.length()-i-1) >= 48 &&
 				name.charAt(name.length()-i-1) <= 57){
 			i++;			
 		}
         PlatformController container = getContainerController();
         String namePrefix=name.substring(0, name.length()-i);
         try {
             AgentController agentWithTheSameName= container.getAgent(namePrefix);
         }
         catch (ControllerException exc)
         {
             //agent with the same name does not exist, we are good
             return namePrefix;
         }
         int nameSuffix=0;
         if (i != 0){
             // numbers Occurred
             nameSuffix = Integer.parseInt(name.substring(name.length()-i, name.length()))+1;
         }
         for (int tryNr=nameSuffix;tryNr<nameSuffix+1000;i++)
         {
             int currentSuffix=nameSuffix+tryNr;
             String currentAgentName=namePrefix+ currentSuffix;
             try {
                 //TODO: write without exceptions
                 AgentController agentWithTheSameName= container.getAgent(currentAgentName);
             }
             catch (ControllerException exc)
             {
                 //agent with the same name does not exist, we are good
                 nameSuffix=currentSuffix;
                 break;
             }
         }
         return namePrefix+ nameSuffix;
 	}
 	
 	private void getAgentTypesFromFile(){
 		// Sets up a file reader to read the agent_types file
 		agentTypes= agentTypesProvider.GetAgentTypes();
 	}
 }

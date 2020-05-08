 package env;
 
 import static jason.eis.Translator.literalToAction;
 import static jason.eis.Translator.perceptToLiteral;
 import jason.JasonException;
 import jason.asSyntax.Literal;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.Structure;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import arch.WorldModel;
 
 import c4jason.CartagoEnvironment;
 import eis.AgentListener;
 import eis.EILoader;
 import eis.EnvironmentInterfaceStandard;
 import eis.EnvironmentListener;
 import eis.iilang.EnvironmentState;
 import eis.iilang.Percept;
 
 /**
  * Defines possible actions to be performed by the agents. Also update the
  * agent percept with the information received from the server.
  * 
  * This class adapts an EIS environment to be used as a Jason/Cartago environment
  * (based on EIS-Jason-0.3).
  * 
  * @author mafranko
  */
 public class MarsEnv extends CartagoEnvironment implements AgentListener {
 
 	private static MarsEnv instance;
 
 	private EnvironmentInterfaceStandard ei;
 
 	static Logger logger = Logger.getLogger(MarsEnv.class.getName());
 
 	public MarsEnv() {
 		super();
 	}
 
 	/**
 	 * Called before the MAS execution with the args informed in .mas2j.
 	 * Connects to the massim server.
 	 */
     @Override
     public void init(String[] args) {
     	super.init(args);
 
     	// sets the team's name and user prefix
     	String teamName = args[3];
     	Literal l = Literal.parseLiteral(teamName);
     	if (l.getFunctor().equals("teamName")){
 			String team = l.getTerm(0).toString();
 			WorldModel.myTeam = team;
     	}
     	String usernamePrefix = args[4];
     	l = Literal.parseLiteral(usernamePrefix);
     	if (l.getFunctor().equals("usernamePrefix")){
 			String prefix = l.getTerm(0).toString();
 			WorldModel.usernamePrefix = prefix;
     	}
 
     	try {
     		//instantiate the environment-interface-class via this very class-loader
     		String cn = "massim.eismassim.EnvironmentInterface";
     		ei = EILoader.fromClassName(cn);
 
     		ei.attachEnvironmentListener(new EnvironmentListener() {
                 @Override
                 public void handleNewEntity(String entity) {
                 }
                 @Override
                 public void handleStateChange(EnvironmentState s) {
                     logger.info("new state " + s);
                 }
                 @Override
                 public void handleDeletedEntity(String arg0, Collection<String> arg1) {
                 }
                 @Override
                 public void handleFreeEntity(String arg0, Collection<String> arg1) {
                 }
             });
 
     		for(String e: ei.getEntities()) {
                 ei.registerAgent(e);
                 ei.attachAgentListener(e, this);
                 ei.associateEntity(e, e);
             }
 
             try {
                 if (ei.getState() != EnvironmentState.PAUSED)
                     ei.pause(); // EIS requires a pause before running
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             if (ei.isStartSupported())
                 ei.start();
 
             instance = this;
 		} catch (Exception e) {
 			logger.severe("Mars Environment - setup failed.");
 			e.printStackTrace();
 		}
     }
 
     /**
      * Handler for percepts received from the server.
     * 
     * This method is called if the environment-interface sends a
	 * percept as a notification. Note, that sending percepts-via-notifications
	 * must be explicitely activated for the environment-interface.
      */
 	@Override
 	public void handlePercept(String agent, Percept percept) {
 		try {
             addPercept(agent, perceptToLiteral(percept));
         } catch (JasonException e) {
             e.printStackTrace();
         }
         informAgsEnvironmentChanged(agent); // wake up the agent
 	}
 
 	/**
 	 * Returns percepts for an agent. A full copy of both common and agent's percepts lists
 	 * is returned. It returns null if the agent's perception doesn't changed since last call. 
 	 */
 	@Override
     public List<Literal> getPercepts(String agName) {
         List<Literal> percepts = super.getPercepts(agName);  
 //        clearPercepts(agName);
         if (percepts == null) 
             percepts = new ArrayList<Literal>();
 
         if (ei != null) {
             try {
                 Map<String,Collection<Percept>> perMap = ei.getAllPercepts(agName);
                 for (String entity: perMap.keySet()) {
                     for (Percept p: perMap.get(entity)) {
                         percepts.add(perceptToLiteral(p));
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return percepts;
     }
 
 	/**
 	 * Executes an action on the environment.
 	 */
     @Override
     public boolean executeAction(String agName, Structure action) {
         if (ei == null) {
             logger.warning("There is no environment loaded! Ignoring action "+action);
             return false;
         }
         try {
             // check case of action on an entity: ae(<action>,<entity as string>)
             if (action.getArity() == 2 && action.getFunctor().equals("ae") && action.getTerm(1).isString()) {
                 String entity = ((StringTerm)action.getTerm(1)).getString();
 //                System.out.println(agName+" doing "+action.getTerm(0)+" as "+entity);
                 ei.performAction(agName, literalToAction((Literal)action.getTerm(0)), entity);                  
             } else {
                 ei.performAction(agName, literalToAction(action));
 //                logger.info("***"+r+" for "+action);
             }
 //            for (ActionResult r: result) {
 //                if (r.getName().equals("success")) 
             return true;
 //            }
         } catch (Exception e) {
             //e.printStackTrace();
             logger.warning("Error in action '"+action+"' by "+agName+": "+e);
         } 
         return false;
     }
 
     /**
      * Called before the end of MAS execution to close the connection to the massim server.
      */
     @Override
     public void stop() {
         if (ei != null) {
             try {
                 if (ei.isKillSupported())
                     ei.kill();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         super.stop();
     }
 
     /**
 	 * Get the instance of this environment.
 	 * 
 	 * @return the instance.
 	 */
 	public static MarsEnv getInstance(){
 		return instance;
 	}

 }

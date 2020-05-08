 package es.upm.dit.gsi.shanks;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import sim.engine.Schedule;
 import sim.engine.SimState;
 import sim.engine.Stoppable;
 import es.upm.dit.gsi.shanks.agent.ShanksAgent;
 import es.upm.dit.gsi.shanks.agent.exception.DuplicatedActionIDException;
 import es.upm.dit.gsi.shanks.agent.exception.DuplicatedAgentIDException;
 import es.upm.dit.gsi.shanks.agent.exception.UnkownAgentException;
 import es.upm.dit.gsi.shanks.exception.ShanksException;
 import es.upm.dit.gsi.shanks.model.ScenarioManager;
 import es.upm.dit.gsi.shanks.model.element.exception.TooManyConnectionException;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementFieldException;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 import es.upm.dit.gsi.shanks.model.scenario.exception.DuplicatedIDException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.ScenarioNotFoundException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.UnsupportedScenarioStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.ScenarioPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.exception.DuplicatedPortrayalIDException;
 import es.upm.dit.gsi.shanks.notification.NotificationManager;
 
 /**
  * Model class
  * 
  * This class represents the model which manage all the simulation
  * 
  * @author Daniel Lara
  * @version 0.1
  * 
  */
 
 public class ShanksSimulation extends SimState {
 
     private static final long serialVersionUID = -2238530527253654867L;
 
     public Logger logger;
 
     private ScenarioManager scenarioManager;
     
     private NotificationManager notificationManager;
 
     private HashMap<String, ShanksAgent> agents;
     
     private HashMap<String, Stoppable> stoppables;
 
     private int numOfResolvedFailures;
 
     /**
      * @param seed
      * @param scenarioClass
      * @param scenarioID
      * @param initialState
      * @param properties
      * @throws SecurityException
      * @throws IllegalArgumentException
      * @throws NoSuchMethodException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws InvocationTargetException
      * @throws UnsupportedNetworkElementFieldException
      * @throws TooManyConnectionException
      * @throws UnsupportedScenarioStatusException
      * @throws DuplicatedIDException
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      * @throws DuplicatedActionIDException
      * @throws DuplicatedAgentIDException
      */
     public ShanksSimulation(long seed, Class<? extends Scenario> scenarioClass,
             String scenarioID, String initialState, Properties properties) throws ShanksException {
 //            throws SecurityException, IllegalArgumentException,
 //            NoSuchMethodException, InstantiationException,
 //            IllegalAccessException, InvocationTargetException,
 //            UnsupportedNetworkElementFieldException,
 //            TooManyConnectionException, UnsupportedScenarioStatusException,
 //            DuplicatedIDException, DuplicatedPortrayalIDException,
 //            ScenarioNotFoundException, DuplicatedAgentIDException,
 //            DuplicatedActionIDException {
         super(seed);
         this.logger = Logger.getLogger(scenarioID);
         this.scenarioManager = this.createScenarioManager(scenarioClass,
                 scenarioID, initialState, properties);
         this.notificationManager = this.createNotificationManager();
         this.agents = new HashMap<String, ShanksAgent>();
         this.stoppables = new HashMap<String, Stoppable>();
         this.registerShanksAgents();
     }
 
     /**
      * Register all agents to the scenario
      * 
      * @throws DuplicatedActionIDException
      * @throws DuplicatedAgentIDException
      */
     public void registerShanksAgents() throws ShanksException {
         logger.info("No agents to add...");
     }
 
     /**
      * This method will set all required information about Scenario
      * 
      * @return the completed Scenario object
      * @throws DuplicatedIDException
      * @throws UnsupportedScenarioStatusException
      * @throws TooManyConnectionException
      * @throws UnsupportedNetworkElementFieldException
      * @throws NoSuchMethodException
      * @throws SecurityException
      * @throws InvocationTargetException
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws IllegalArgumentException
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     private ScenarioManager createScenarioManager(
             Class<? extends Scenario> scenarioClass, String scenarioID,
             String initialState, Properties properties) throws ShanksException {
 //            throws UnsupportedNetworkElementFieldException,
 //            TooManyConnectionException, UnsupportedScenarioStatusException,
 //            DuplicatedIDException, SecurityException, NoSuchMethodException,
 //            IllegalArgumentException, InstantiationException,
 //            IllegalAccessException, InvocationTargetException,
 //            DuplicatedPortrayalIDException, ScenarioNotFoundException {
 
         Constructor<? extends Scenario> c;
         Scenario s = null;
         try {
             c = scenarioClass
                     .getConstructor(new Class[] { String.class, String.class,
                             Properties.class, Logger.class });
             s = c.newInstance(scenarioID, initialState, properties, this.getLogger());
         } catch (SecurityException e) {
             throw new ShanksException(e);
         } catch (NoSuchMethodException e) {
             throw new ShanksException(e);
         } catch (IllegalArgumentException e) {
             throw new ShanksException(e);
         } catch (InstantiationException e) {
             throw new ShanksException(e);
         } catch (IllegalAccessException e) {
             throw new ShanksException(e);
         } catch (InvocationTargetException e) {
             throw new ShanksException(e);
         }
         logger.fine("Scenario created");
         ScenarioPortrayal sp = s.createScenarioPortrayal();
        if (sp == null && !properties.get(Scenario.SIMULATION_GUI).equals(Scenario.NO_GUI)) {
            logger.severe("ScenarioPortrayals is null");
            logger.severe("Impossible to follow with the execution...");
            throw new ShanksException("ScenarioPortrayals is null. Impossible to continue with the simulation...");
         }
         ScenarioManager sm = new ScenarioManager(s, sp, logger);
         return sm;
     }
     
     private NotificationManager createNotificationManager() {
         // Class<? extends Scenario> scenarioClass, String scenarioID,
         // String initialState, Properties properties) {
         //TODO add the way to create the NM with initial values taken from properties. 
         NotificationManager nm = new NotificationManager(this);
         return nm;
     }
 
     /**
      * @return ScenarioManager of the simulation
      */
     public ScenarioManager getScenarioManager() {
         return this.scenarioManager;
     }
 
     /**
      * @return Scenario of the simulation
      */
     public Scenario getScenario() {
         return this.scenarioManager.getScenario();
     }
 
     /**
      * @return ScenarioPortrayal of the scenario of the simulation
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     public ScenarioPortrayal getScenarioPortrayal()
             throws ShanksException {
         ScenarioPortrayal sp = this.scenarioManager.getPortrayal();
         if (sp == null) {
             sp = this.scenarioManager.getScenario().createScenarioPortrayal();
             this.scenarioManager.setPortrayal(sp);
         }
         return sp;
     }
 
     /**
      * @return the numOfResolvedFailures
      */
     public int getNumOfResolvedFailures() {
         return numOfResolvedFailures;
     }
 
     /**
      * @param numOfResolvedFailures
      *            the numOfResolvedFailures to set
      */
     public void setNumOfResolvedFailures(int numOfResolvedFailures) {
         this.numOfResolvedFailures = numOfResolvedFailures;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see sim.engine.SimState#start()
      */
     @Override
     public void start() {
         super.start();
         logger.finer("-> start method");
         try {
             startSimulation();
         } catch (ShanksException e) {
             logger.severe("ShanksException: " + e.getMessage());
             e.printStackTrace();
         }
     }
 
     /**
      * The initial configuration of the scenario
      * 
      * @throws DuplicatedAgentIDException
      * @throws DuplicatedActionIDException
      */
     public void startSimulation() throws ShanksException {
         schedule.scheduleRepeating(Schedule.EPOCH, 0, this.scenarioManager, 2);
         schedule.scheduleRepeating(Schedule.EPOCH, 1, this.notificationManager, 1);
         this.addAgents();
         this.addSteppables();
     }
 
     /**
      * Add ShanksAgent's to the simulation using registerShanksAgent method
      * 
      * @throws DuplicatedActionIDException
      */
     private void addAgents() throws ShanksException {
         for (Entry<String, ShanksAgent> agentEntry : this.agents.entrySet()) {
             stoppables.put(agentEntry.getKey(), schedule.scheduleRepeating(Schedule.EPOCH, 2, agentEntry.getValue(), 1));
         }
     }
 
     /**
      * This method adds and registers the ShanksAgent
      * 
      * @param agent
      *            The ShanksAgent
      * @param order
      *            The agent will be executed in this order
      * @param interval
      *            The agent will be executed every "x=interval" steps
      * @throws DuplicatedAgentIDException
      */
     public void registerShanksAgent(ShanksAgent agent)
             throws ShanksException {
         if (!this.agents.containsKey(agent.getID())) {
             this.agents.put(agent.getID(), agent);
         } else {
             throw new DuplicatedAgentIDException(agent.getID());
         }
     }
     
     /**
      * Unregisters an agent.
      * 
      * @param agentID - The ID for the agent
      */
     public void unregisterShanksAgent(String agentID){
         if (this.agents.containsKey(agentID)) {
             //  this.agents.get(agentID).stop();
             if (stoppables.containsKey(agentID)) {
                 this.agents.remove(agentID);
                 this.stoppables.remove(agentID).stop();
             } else {
                 //No stoppable, stops the agent
                 logger.warning("No stoppable found while trying to stop the agent. Attempting direct stop...");
                 this.agents.remove(agentID).stop();
             }
         }
     }
 
     /**
      * @param agentID
      * @return ShanksAgent with ID equals to agentID
      * @throws UnkownAgentException
      */
     public ShanksAgent getAgent(String agentID) throws ShanksException {
         if (this.agents.containsKey(agentID)) {
             return this.agents.get(agentID);
         } else {
             throw new UnkownAgentException(agentID);
         }
     }
     
     /**
      * @return A collection with all agents
      */
     public Collection<ShanksAgent> getAgents() {
         return this.agents.values();
     }
 
     /**
      * It is required to add Steppables
      * 
      * @return Schedule of the simulation
      */
     public Schedule getSchedule() {
         return schedule;
     }
 
     /**
      * This method is called during the start phase of the simulation. The
      * command: schedule.scheduleRepeating(Schedule.EPOCH, 0,
      * this.scenarioManager, 2); is always executed in the first place.
      * 
      * In this method, for example, the steppable responsible of print graphics
      * can be added.
      */
     public void addSteppables() {
         logger.info("No steppables added...");
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         doLoop(ShanksSimulation.class, args);
         System.exit(0);
     }
 
     /**
      * @return the notificationManager
      */
     public NotificationManager getNotificationManager() {
         return notificationManager;
     }
     
     /**
      * Return the simulation logger.
      * 
      * @return
      */
     public Logger getLogger() {
         return this.logger;
     }
 }

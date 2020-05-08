 package es.upm.dit.gsi.shanks.model.scenario;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import sim.engine.Steppable;
 import ec.util.MersenneTwisterFast;
 import es.upm.dit.gsi.shanks.ShanksSimulation;
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.exception.TooManyConnectionException;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.event.Event;
 import es.upm.dit.gsi.shanks.model.event.PeriodicEvent;
 import es.upm.dit.gsi.shanks.model.event.ProbabilisticEvent;
 import es.upm.dit.gsi.shanks.model.failure.Failure;
 import es.upm.dit.gsi.shanks.model.failure.exception.NoCombinationForFailureException;
 import es.upm.dit.gsi.shanks.model.failure.exception.UnsupportedElementInFailureException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.DuplicatedIDException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.ScenarioNotFoundException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.UnsupportedScenarioStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario3DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.ScenarioPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.exception.DuplicatedPortrayalIDException;
 
 /**
  * Scenarios class
  * 
  * This class create the different scenarios
  * 
  * @author Daniel Lara
  * @author a.carrera
  * @version 0.1.1
  * 
  */
 /**
  * @author a.carrera
  * 
  */
 public abstract class Scenario {
 
     private Logger logger = Logger.getLogger(Scenario.class.getName());
 
     public static final String SIMULATION_GUI = "SIMULATION GUI";
     public static final String SIMULATION_2D = "2D";
     public static final String SIMULATION_3D = "3D";
     public static final String NO_GUI = "NO GUI";
     
     private String id;
     private Properties properties;
     private List<String> possibleStates;
     private String currentStatus;
     private HashMap<String, NetworkElement> currentElements;
     private HashMap<Failure, Integer> currentFailures;
     private HashMap<Class<? extends Failure>, List<Set<NetworkElement>>> possibleFailures;
     private HashMap<Class<? extends Event>, List<Set<NetworkElement>>> possiblesEventsOnNE;
     private HashMap<Class<? extends Event>, List<Set<Scenario>>> possiblesEventsOnScenario;
     private HashMap<Class<? extends Failure>, List<Integer>> generatedFailureConfigurations;
 
     /**
      * Constructor of scenario
      * 
      * @param id
      * @param initialState
      * @param properties
      * @throws UnsupportedNetworkElementStatusException
      * @throws TooManyConnectionException
      * @throws UnsupportedScenarioStatusException
      * @throws DuplicatedIDException
      */
     public Scenario(String id, String initialState, Properties properties)
             throws UnsupportedNetworkElementStatusException,
             TooManyConnectionException, UnsupportedScenarioStatusException,
             DuplicatedIDException {
         this.id = id;
         this.setProperties(properties);
         this.possibleStates = new ArrayList<String>();
         this.currentElements = new HashMap<String, NetworkElement>();
         this.currentFailures = new HashMap<Failure, Integer>();
         this.possibleFailures = new HashMap<Class<? extends Failure>, List<Set<NetworkElement>>>();
         this.generatedFailureConfigurations = new HashMap<Class<? extends Failure>, List<Integer>>();
         this.possiblesEventsOnNE = new HashMap<Class<? extends Event>, List<Set<NetworkElement>>>();
         this.possiblesEventsOnScenario = new HashMap<Class<? extends Event>, List<Set<Scenario>>>();
 
         this.setPossibleStates();
         this.addNetworkElements();
         this.addPossibleFailures();
         this.addPossibleEvents();
 
         this.setCurrentStatus(initialState);
 
         logger.info("Scenario " + this.getID() + " successfully created.");
     }
 
     /**
      * Create the scenario portrayal (2D o 3D).
      * 
      * @return Scenario2DPortrayal or Scenario3DPortrayal object
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     public ScenarioPortrayal createScenarioPortrayal()
             throws DuplicatedPortrayalIDException, ScenarioNotFoundException {
         logger.fine("Creating Scenario Portrayal...");
         String dimensions = (String) this.getProperty(Scenario.SIMULATION_GUI);
         if (dimensions.equals(Scenario.SIMULATION_2D)) {
             logger.fine("Creating Scenario2DPortrayal");
             return this.createScenario2DPortrayal();
         } else if (dimensions.equals(Scenario.SIMULATION_3D)) {
             logger.fine("Creating Scenario3DPortrayal");
             return this.createScenario3DPortrayal();
         } else if (dimensions.equals(Scenario.NO_GUI)) {
             return null;
         }
         return null;
 
     }
 
     /**
      * @return a Scenario3DPortrayal
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     abstract public Scenario2DPortrayal createScenario2DPortrayal()
             throws DuplicatedPortrayalIDException, ScenarioNotFoundException;
 
     /**
      * @return a Scenario2DPortrayal
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     abstract public Scenario3DPortrayal createScenario3DPortrayal()
             throws DuplicatedPortrayalIDException, ScenarioNotFoundException;
 
     /**
      * @return the id
      */
     public String getID() {
         return id;
     }
 
     /**
      * @return the currentStatus
      */
     public String getCurrentStatus() {
         return currentStatus;
     }
 
     /**
      * @param desiredStatus
      *            the currentStatus to set
      * @return true if the status was set correctly and false if the status is
      *         not a possible status of the network element
      * @throws UnsupportedNetworkElementStatusException
      */
     public boolean setCurrentStatus(String desiredStatus)
             throws UnsupportedScenarioStatusException {
         if (this.isPossibleStatus(desiredStatus)) {
             this.currentStatus = desiredStatus;
             return true;
         } else {
             logger.warning("Impossible to set status: " + desiredStatus
                     + ". This network element " + this.getID()
                     + "does not support this status.");
             throw new UnsupportedScenarioStatusException();
         }
     }
 
     /**
      * @param possibleStatus
      * @return
      */
     private boolean isPossibleStatus(String possibleStatus) {
         if (this.possibleStates.contains(possibleStatus)) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * @param possibleStatus
      */
     public void addPossibleStatus(String possibleStatus) {
         if (!this.possibleStates.contains(possibleStatus)) {
             this.possibleStates.add(possibleStatus);
             logger.fine("Status: " + possibleStatus
                     + " was added as possible status to Scenario "
                     + this.getID());
         } else {
             logger.fine("Status: " + possibleStatus
                     + " was already added as possible status to Scenario "
                     + this.getID());
         }
     }
 
     /**
      * @param possibleStatus
      */
     public void removePossibleStatus(String possibleStatus) {
         if (this.possibleStates.contains(possibleStatus)) {
             this.possibleStates.remove(possibleStatus);
             logger.fine("Status: " + possibleStatus
                     + " was removed as possible status to Scenario "
                     + this.getID());
         } else {
             logger.fine("Status: " + possibleStatus
                     + " was not removed as possible status to Scenario "
                     + this.getID()
                     + " because it was not a possible status previously.");
         }
     }
 
     /**
      * @param element
      * @throws DuplicatedIDException
      */
     public void addNetworkElement(NetworkElement element)
             throws DuplicatedIDException {
         if (!this.currentElements.containsKey(element.getID())) {
             this.currentElements.put(element.getID(), element);
         } else {
             throw new DuplicatedIDException(element);
         }
     }
 
     /**
      * @param element
      */
     public void removeNetworkElement(NetworkElement element) {
         this.currentElements.remove(element.getID());
     }
 
     /**
      * @return Map with key: NetworkElementID and value: NetworkElement
      */
     public HashMap<String, NetworkElement> getCurrentElements() {
         return this.currentElements;
     }
 
     /**
      * @return the properties
      */
     public Properties getProperties() {
         return properties;
     }
 
     /**
      * @param properties
      *            the properties to set
      */
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 
     /**
      * @param propertyKey
      * @return the property value
      */
     public Object getProperty(String propertyKey) {
         return this.properties.getProperty(propertyKey);
     }
 
     /**
      * @param propertyKey
      * @param propertyValue
      */
     public void addProperty(String propertyKey, String propertyValue) {
         this.properties.put(propertyKey, propertyValue);
     }
 
     /**
      * @param propertyKey
      */
     public void removePorperty(String propertyKey) {
         this.properties.remove(propertyKey);
     }
 
     /**
      * 
      * 
      * @param failure
      *            Failure to add
      * @param configuration
      *            Configuration of the failure
      */
     public void addFailure(Failure failure, int configuration) {
         if (this.possibleFailures.containsKey(failure.getClass())) {
             this.currentFailures.put(failure, configuration);
         } else {
             logger.warning("Failure was not added, because this scenario does not support this type of Failure. Failure type: "
                     + failure.getClass().getName());
         }
     }
 
     /**
      * @param failure
      */
     public void removeFailure(Failure failure) {
         this.currentFailures.remove(failure);
     }
 
     /**
      * @return set of current active failures in the scenario
      */
     public Set<Failure> getCurrentFailures() {
         return this.currentFailures.keySet();
     }
 
     /**
      * @return Map with key: Failure object and value: Combination Number
      */
     protected HashMap<Failure, Integer> getFullCurrentFailures() {
         return this.currentFailures;
     }
 
     /**
      * @param failure
      * @param possibleCombinations
      */
     public void addPossibleFailure(Class<? extends Failure> failure,
             List<Set<NetworkElement>> possibleCombinations) {
         this.possibleFailures.put(failure, possibleCombinations);
     }
 
     /**
      * @param failure
      * @param set
      */
     public void addPossibleFailure(Class<? extends Failure> failure,
             Set<NetworkElement> set) {
         List<Set<NetworkElement>> list = new ArrayList<Set<NetworkElement>>();
         list.add(set);
         this.possibleFailures.put(failure, list);
     }
 
     /**
      * @param failure
      * @param element
      */
     public void addPossibleFailure(Class<? extends Failure> failure,
             NetworkElement element) {
         List<Set<NetworkElement>> list = new ArrayList<Set<NetworkElement>>();
         Set<NetworkElement> set = new HashSet<NetworkElement>();
         set.add(element);
         list.add(set);
         this.possibleFailures.put(failure, list);
     }
 
     //
 
     public void addPossibleEventsOfNE(Class<? extends Event> event,
             NetworkElement element) {
         List<Set<NetworkElement>> list = new ArrayList<Set<NetworkElement>>();
         Set<NetworkElement> set = new HashSet<NetworkElement>();
         set.add(element);
         list.add(set);
         this.possiblesEventsOnNE.put(event, list);
 
     }
 
     public void addPossibleEventsOfNE(Class<? extends Event> event,
             Set<NetworkElement> set) {
         List<Set<NetworkElement>> list = new ArrayList<Set<NetworkElement>>();
         list.add(set);
         this.possiblesEventsOnNE.put(event, list);
     }
 
     public void addPossibleEventsOfNE(Class<? extends Event> event,
             List<Set<NetworkElement>> possibleCombinations) {
         this.possiblesEventsOnNE.put(event, possibleCombinations);
     }
 
     public void addPossibleEventsOfScenario(Class<? extends Event> event,
             Scenario scen) {
         List<Set<Scenario>> list = new ArrayList<Set<Scenario>>();
         Set<Scenario> set = new HashSet<Scenario>();
         set.add(scen);
         list.add(set);
         this.possiblesEventsOnScenario.put(event, list);
 
     }
 
     public void addPossibleEventsOfScenario(Class<? extends Event> event,
             Set<Scenario> set) {
         List<Set<Scenario>> list = new ArrayList<Set<Scenario>>();
         list.add(set);
         this.possiblesEventsOnScenario.put(event, list);
     }
 
     public void addPossibleEventsOfScenario(Class<? extends Event> event,
             List<Set<Scenario>> possibleCombinations) {
         this.possiblesEventsOnScenario.put(event, possibleCombinations);
     }
 
     /**
      * @param failureType
      */
     public void removePossibleFailure(Class<Failure> failureType) {
         this.possibleFailures.remove(failureType);
     }
 
     /**
      * @return Map with key: Concrete Failure Class and value: List of
      *         combinations of the failure
      */
     public HashMap<Class<? extends Failure>, List<Set<NetworkElement>>> getPossibleFailures() {
         return this.possibleFailures;
     }
 
     public HashMap<Class<? extends Event>, List<Set<NetworkElement>>> getPossibleEventsOfNE() {
         return this.possiblesEventsOnNE;
     }
 
     public HashMap<Class<? extends Event>, List<Set<Scenario>>> getPossibleEventsOfScenario() {
         return this.possiblesEventsOnScenario;
     }
 
     /**
      *
      */
     abstract public void setPossibleStates();
 
     /**
      * @throws UnsupportedNetworkElementStatusException
      * @throws TooManyConnectionException
      * @throws DuplicatedIDException
      * 
      */
     abstract public void addNetworkElements()
             throws UnsupportedNetworkElementStatusException,
             TooManyConnectionException, DuplicatedIDException;
 
     /**
      * 
      */
     abstract public void addPossibleFailures();
 
     abstract public void addPossibleEvents();
 
     /**
      * 
      * Algorithm used to generate failures during the simulation
      * 
      * @throws UnsupportedScenarioStatusException
      * @throws NoCombinationForFailureException
      * @throws UnsupportedElementInFailureException
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws UnsupportedNetworkElementStatusException
      * @throws NoSuchMethodException
      * @throws SecurityException
      * @throws InvocationTargetException
      * @throws IllegalArgumentException
      * 
      */
 
     public void generateNetworkElementEvents(ShanksSimulation sim)
             throws UnsupportedScenarioStatusException, InstantiationException,
             IllegalAccessException, UnsupportedNetworkElementStatusException,
             SecurityException, NoSuchMethodException, IllegalArgumentException,
             InvocationTargetException {
         MersenneTwisterFast random = new MersenneTwisterFast();
         Iterator<Class<? extends Event>> it = this.getPossibleEventsOfNE()
                 .keySet().iterator();
         while (it.hasNext()) {
             Class<? extends Event> type = it.next();
             double prob = 0;
             Constructor<? extends Event> c = null;
             if (ProbabilisticEvent.class.isAssignableFrom(type)) {
                 c = type.getConstructor(new Class[] {Steppable.class});
                 Event event = c.newInstance(sim.getScenarioManager());
                 List<Set<NetworkElement>> list = this.getPossibleEventsOfNE()
                         .get(type);
                 int numberOfCombinations = list.size();
                 prob = ((ProbabilisticEvent) event).getProb();
                 double aux = Math.random();
                 int combinationNumber = random.nextInt(numberOfCombinations);
                 if (aux < prob) {
                     Set<NetworkElement> elementsSet;
                     elementsSet = list.get(combinationNumber);
                     this.setupNetworkElementEvent(event, elementsSet,
                             combinationNumber);
                     event.launchEvent();
                 }
             } else if (PeriodicEvent.class.isAssignableFrom(type)) {
                 c = type.getConstructor(new Class[] {Steppable.class});
                 Event event = c.newInstance(sim.getScenarioManager());
                 if ( (sim.getSchedule().getSteps() != 0) && 
                         (((PeriodicEvent) event).getPeriod()% sim.getSchedule().getSteps() == 0)) {
                     List<Set<NetworkElement>> list = this
                             .getPossibleEventsOfNE().get(type);
                     int numberOfCombinations = list.size();
                     int combinationNumber = random
                             .nextInt(numberOfCombinations);
                     Set<NetworkElement> elementSet;
                     elementSet = list.get(combinationNumber);
                     this.setupNetworkElementEvent(event, elementSet,
                             combinationNumber);
                     event.launchEvent();
                 }
             } else {
                 // TODO ¿generate an exception?
                 logger.warning("No NE.Events where generated.");
                 return;
             }
         }
     }
 
     public void generateScenarioEvents(ShanksSimulation sim)
             throws UnsupportedScenarioStatusException, InstantiationException,
             IllegalAccessException, UnsupportedNetworkElementStatusException,
             SecurityException, NoSuchMethodException, IllegalArgumentException,
             InvocationTargetException {
         MersenneTwisterFast random = new MersenneTwisterFast();
         Iterator<Class<? extends Event>> it = this
                 .getPossibleEventsOfScenario().keySet().iterator();
         while (it.hasNext()) {
             Class<? extends Event> type = it.next();
             double prob = 0;
             Constructor<? extends Event> c = null;
             if (ProbabilisticEvent.class.isAssignableFrom(type)) {
                 c = type.getConstructor(new Class[] {Steppable.class});
                 Event event = c.newInstance(sim.getScenarioManager());
                 List<Set<Scenario>> list = this.getPossibleEventsOfScenario()
                         .get(type);
                 int numberOfCombinations = list.size();
                 prob = ((ProbabilisticEvent) event).getProb();
                 double aux = Math.random();
                 int combinationNumber = random.nextInt(numberOfCombinations);
                 if (aux < prob) {
                     Set<Scenario> scenarioSet;
                     scenarioSet = list.get(combinationNumber);
                     this.setupScenarioEvent(event, scenarioSet,
                             combinationNumber);
                     event.launchEvent();
                 }
             } else if (PeriodicEvent.class.isAssignableFrom(type)) {
                 c = type.getConstructor(new Class[] {Steppable.class});
                 Event event = c.newInstance(sim.getScenarioManager());
                 if ( (sim.getSchedule().getSteps() != 0) && 
                         (((PeriodicEvent) event).getPeriod()% sim.getSchedule().getSteps() == 0)) {
                     List<Set<Scenario>> list = this
                             .getPossibleEventsOfScenario().get(type);
                     int numberOfCombinations = list.size();
                     int combinationNumber = random
                             .nextInt(numberOfCombinations);
                     Set<Scenario> scenarioSet;
                     scenarioSet = list.get(combinationNumber);
                     this.setupScenarioEvent(event, scenarioSet,
                             combinationNumber);
                     event.launchEvent();
                 }
             } else {
                 // TODO ¿generate an exception?
                 logger.warning("No NE.Events where generated.");
                 return;
             }
         }
     }
 
     public void setupNetworkElementEvent(Event event,
             Set<NetworkElement> elementsSet, int configurationNumber) {
         for (NetworkElement ne : elementsSet) {
             event.addAffectedElement(ne);
         }
     }
 
     public void setupScenarioEvent(Event event, Set<Scenario> scenarioSet,
             int configurationNumber) {
         for (Scenario ne : scenarioSet) {
             event.addAffectedScenario(ne);
         }
     }
 
     public void generateFailures() throws UnsupportedScenarioStatusException,
             NoCombinationForFailureException,
             UnsupportedElementInFailureException, InstantiationException,
             IllegalAccessException, UnsupportedNetworkElementStatusException {
         MersenneTwisterFast randomizer = new MersenneTwisterFast();
         String status = this.getCurrentStatus();
         HashMap<Class<? extends Failure>, Double> penalties = this
                 .getPenaltiesInStatus(status);
         Iterator<Class<? extends Failure>> it = this.getPossibleFailures()
                 .keySet().iterator();
         while (it.hasNext()) {
             Class<? extends Failure> type = it.next();
             double penalty = 0;
             double prob = 0;
             try {
                 Failure failure = type.newInstance();
                 List<Set<NetworkElement>> list = this.getPossibleFailures()
                         .get(type);
                 int numberOfCombinations = list.size();
                 int combinationNumber = randomizer
                         .nextInt(numberOfCombinations);
                 try {
                     if (penalties.containsKey(type)) {
                         // Apply penalty
                         penalty = penalties.get(type);
                         if (penalty > 0) {
                             prob = failure.getOccurrenceProbability()
                                     * numberOfCombinations * penalty;
                         } else {
                             prob = -1.0; // Impossible failure
                         }
                     } else {
                         prob = failure.getOccurrenceProbability()
                                 * numberOfCombinations;
                     }
                 } catch (Exception e) {
                     logger.fine("There is no penalty for failures: "
                             + type.getName() + " in status " + status);
                 }
                 // double aux = randomizer.nextDouble(); // THIS OPTION GENERATE
                 // MANY FAULTS OF THE SAME TYPE AT THE SAME TIME
                 double aux = Math.random(); // THIS WORKS BETTER, MORE RANDOMLY
                 if (aux < prob) {
                     // Generate failure
                     Set<NetworkElement> elementsSet;
                     if (numberOfCombinations >= 1) {
                         elementsSet = list.get(combinationNumber);
                         this.setupFailure(failure, elementsSet,
                                 combinationNumber);
                     } else if (this.generatedFailureConfigurations.get(type)
                             .size() == 0) {
                         throw new NoCombinationForFailureException(failure);
                     }
                 }
             } catch (NoCombinationForFailureException e) {
                 throw e;
             } catch (UnsupportedElementInFailureException e) {
                 logger.severe("Impossible to instance failure: "
                         + type.getName()
                         + ". All failures must have a default constructor that calls other constructor Failure(String id, double occurrenceProbability)");
                 logger.severe("Exception: " + e.getMessage());
                 throw e;
             }
         }
     }
 
     /**
      * @param failure
      * @param elementsSet
      * @throws UnsupportedElementInFailureException
      * @throws UnsupportedNetworkElementStatusException
      */
     // TODO Retocar la manera de ver ahora los fallos (Lo hare cuando modifique
     // escenario para adatarlo a eventos y acciones)
     //
     private void setupFailure(Failure failure, Set<NetworkElement> elementsSet,
             int configurationNumber)
             throws UnsupportedElementInFailureException,
             UnsupportedNetworkElementStatusException {
         for (NetworkElement element : elementsSet) {
             for (String statusToSet : element.getStatus().keySet()) {
                 if (element.getStatus().containsKey(statusToSet)) {
                     // String valueToSee =
                     // failure.getPossibleAffectedElements().get(element);
                     boolean value = element.getStatus().get(statusToSet);
                     failure.addAffectedElement(element, statusToSet, value);
                 } else if (element.getProperties().containsKey(statusToSet)) {
                     Object value = element.getProperty(statusToSet);
                     failure.addAffectedPropertiesOfElement(element,
                             statusToSet, value);
                 }
             }
         }
         if (!this.generatedFailureConfigurations
                 .containsKey(failure.getClass())) {
             this.generatedFailureConfigurations.put(failure.getClass(),
                     new ArrayList<Integer>());
         }
         List<Integer> numList = this.generatedFailureConfigurations.get(failure
                 .getClass());
         if (!numList.contains(configurationNumber)) {
             numList.add(configurationNumber);
             this.generatedFailureConfigurations
                     .put(failure.getClass(), numList);
             failure.activateFailure();
             this.addFailure(failure, configurationNumber);
             logger.fine("Generated Failure " + failure.getID()
                     + " with configuration " + configurationNumber);
         }
 
     }
 
     /**
      * This method can return multipliers >1.0 (more probable failures) or
      * <1.0(less probable failure). CAUTION: If the multiplier is <=0.0, the
      * failure never happends.
      * 
      * @param status
      * @return Multiplier for each type of failure
      * @throws UnsupportedScenarioStatusException
      */
     abstract public HashMap<Class<? extends Failure>, Double> getPenaltiesInStatus(
             String status) throws UnsupportedScenarioStatusException;
 
     /**
      * @return resolved failures
      */
     public List<Failure> checkResolvedFailures() {
         List<Failure> resolvedFailures = new ArrayList<Failure>();
         for (Failure failure : this.currentFailures.keySet()) {
             if (failure.isResolved()) {
                 resolvedFailures.add(failure);
                 List<Integer> numList = this.generatedFailureConfigurations
                         .get(failure.getClass());
                 Integer conf = this.currentFailures.get(failure);
                 numList.remove((Integer) this.currentFailures.get(failure));
                 this.generatedFailureConfigurations.put(failure.getClass(),
                         numList);
                 logger.fine("Resolved failure " + failure.getID()
                         + ". Failure class: " + failure.getClass().getName()
                         + " with configuration " + conf);
             }
         }
         for (Failure resolved : resolvedFailures) {
             this.currentFailures.remove(resolved);
         }
         return resolvedFailures;
     }
 
     /**
      * Return the network element with these id
      * 
      * @param id
      * @return NetworkElement object
      */
     public NetworkElement getNetworkElement(String id) {
         return this.currentElements.get(id);
     }
 
 }

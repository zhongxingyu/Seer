     package RSLBench;
 
 import RSLBench.Helpers.Logging.Markers;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import RSLBench.Search.DistanceInterface;
 import RSLBench.Search.Graph;
 import RSLBench.Search.SearchAlgorithm;
 import RSLBench.Search.SearchFactory;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import rescuecore2.Constants;
 import rescuecore2.standard.components.StandardAgent;
 import rescuecore2.standard.entities.Area;
 import rescuecore2.standard.entities.Blockade;
 import rescuecore2.standard.entities.Building;
 import rescuecore2.standard.entities.Human;
 import rescuecore2.standard.entities.Refuge;
 import rescuecore2.standard.entities.Road;
 import rescuecore2.standard.entities.StandardEntity;
 import rescuecore2.standard.entities.StandardEntityURN;
 import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
 import rescuecore2.worldmodel.Entity;
 import rescuecore2.worldmodel.EntityID;
 
 /**
    Abstract base class for agents.
    @param <E> The subclass of StandardEntity this agent wants to control.
  */
 public abstract class PlatoonAbstractAgent<E extends StandardEntity> extends StandardAgent<E> {
     private static final Logger Logger = LogManager.getLogger(PlatoonAbstractAgent.class);
 
     public static final String THINK_TIME_KEY = "kernel.agents.think-time";
 
     private static final int RANDOM_WALK_LENGTH = 50;
 
     //private static final String SAY_COMMUNICATION_MODEL = StandardCommunicationModel.class.getName();
     private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class.getName();
 
     /** Queue to receive assignments from the central */
     private BlockingQueue<EntityID> assignmentQueue = new ArrayBlockingQueue<>(1);
 
     /**
        Whether to use AKSpeak messages or not.
     */
     protected boolean useSpeak;
 
     /**
        Cache of building IDs.
     */
     protected List<EntityID> buildingIDs;
 
     /**
        Cache of road IDs.
     */
     protected List<EntityID> roadIDs;
 
     /**
      * Cache of refuge IDs.
      */
     protected List<EntityID> refugeIDs;
 
     /**
      * the connectivity graph of all places in the world
      */
     protected Graph connectivityGraph;
 
     /**
      * a matrix containing the pre-computed distances between each two areas in the world
      */
     protected DistanceInterface distanceMatrix;
 
     /**
      * The search algorithm.
      */
     protected SearchAlgorithm search;
 
     protected EntityID randomExplorationGoal = null;
 
     /**
      * Construct an AbstractSampleAgent.
      */
 
     @Override
     protected void postConnect() {
         super.postConnect();
         buildingIDs = new ArrayList<>();
         roadIDs = new ArrayList<>();
         refugeIDs = new ArrayList<>();
         for (StandardEntity next : model) {
             if (next instanceof Building) {
                 buildingIDs.add(next.getID());
             }
             if (next instanceof Road) {
                 roadIDs.add(next.getID());
             }
             if (next instanceof Refuge) {
                 refugeIDs.add(next.getID());
             }
         }
 
         // load correct search algorithm
         search = SearchFactory.buildSearchAlgorithm(config);
         connectivityGraph = new Graph(model);
         distanceMatrix = new DistanceInterface(model);
 
         useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
         Logger.debug("Communcation model: " + config.getValue(Constants.COMMUNICATION_MODEL_KEY));
         Logger.debug(useSpeak ? "Using speak model" : "Using say model");
     }
 
     public boolean enqueueAssignment(EntityID target) {
         return assignmentQueue.offer(target);
     }
 
     /**
      * Fetch the latest assignment as computed by the DCOP algorithm.
      *
      * @return EntityID of the target assigned to this agent.
      */
     protected EntityID fetchAssignment() {
         EntityID assignment = null;
        
         Logger.debug("Agent {} waiting for command.", getID());
         try {
             assignment = assignmentQueue.poll(
                     config.getIntValue(THINK_TIME_KEY) - 100, TimeUnit.MILLISECONDS);
         } catch (InterruptedException ex) {
             Logger.error("Agent {} unable to fetch its assingment.",
                     ex, getID());
             return null;
         }
         Logger.debug("Agent {} approaching {}!", getID(), assignment);
         return assignment;
     }
 
 
     /**
        Construct a random walk starting from this agent's current location to a random building.
        @return A random walk.
     */
     protected List<EntityID> randomWalk() {
         List<EntityID> result = new ArrayList<>(RANDOM_WALK_LENGTH);
         Set<EntityID> seen = new HashSet<>();
         EntityID current = ((Human)me()).getPosition();
         for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
             result.add(current);
             seen.add(current);
             List<Area> possible = new ArrayList<>(connectivityGraph.getNeighbors(current));
             Collections.shuffle(possible, random);
             boolean found = false;
             for (Area next : possible) {
                 if (seen.contains(next.getID())) {
                     continue;
                 }
                 current = next.getID();
                 found = true;
                 break;
             }
             if (!found) {
                 // We reached a dead-end.
                 break;
             }
         }
         return result;
     }
 
     protected List<EntityID> randomExplore()
     {
         // check if goal reached
         EntityID position = ((Human)me()).getPosition();
         if (randomExplorationGoal != null)
         {
             int distance = model.getDistance(position, randomExplorationGoal);
             Logger.debug(Markers.BLUE, "RANDOM_EXPLORATION: distance to goal: " + distance);
             if (distance <= 20000)
             {
                 randomExplorationGoal = null;
                 Logger.debug(Markers.BLUE, "RANDOM_EXPLORATION: goal reached");
             }
         }
 
         // select new exploration goal
         if (randomExplorationGoal == null)
         {
             //Logger.debugColor("RANDOM_EXPLORATION: selecting new goal", Logger.BG_BLUE);
             Collection<StandardEntity> roads = model.getEntitiesOfType(StandardEntityURN.ROAD);
             Entity[] roadArray = roads.toArray(new Entity[0]);
             int index = random.nextInt(100000);
             int step = getID().getValue();
             while (randomExplorationGoal == null)
             {
                 index += step;
                 index %= roadArray.length;
                 Entity entity = roadArray[index];
                 if (model.getDistance(position, entity.getID()) > 20000)
                 {
                     randomExplorationGoal = entity.getID();
                     Logger.debug(Markers.BLUE, "RANDOM_EXPLORATION: new goal selected");
                 }
             }
         }
 
         // plan path to goal
         return search.search(position, randomExplorationGoal, connectivityGraph, distanceMatrix).getPathIds();
     }
 
     public void removeBlockade(Blockade blockade) {
         if (model == null) {
             Logger.error("Null model in agent {}", this);
         }
         if (blockade == null) {
             Logger.error("Null blockade in agent {}", this);
         }
         model.removeEntity(blockade.getID());
     }
 
 }

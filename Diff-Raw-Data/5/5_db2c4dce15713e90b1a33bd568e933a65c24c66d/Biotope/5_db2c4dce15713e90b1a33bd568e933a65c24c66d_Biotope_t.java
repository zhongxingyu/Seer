 package galapagos;
 
 import java.util.*;
 import java.lang.*;
 
 public class Biotope extends Observable {
     private int width, height;
     private double breedingProbability;
     private int maxHitpoints, initialHitpoints, hitpointsPerRound;
     private int minMaxAge, maxMaxAge;
     private int numberOfBehaviors;
     private int finchesPerBehavior;
     private int round;
     private World<GalapagosFinch> world;
     private TreeMap<String,Statistics> statisticsTree;
     private List<Behavior> finchBehaviors;
 
     private final static int HelpedGotHelpValue = 3;
     private final static int HelpedDidntGetHelpValue = 0;
     private final static int DidntHelpGotHelpValue = 5;
     private final static int DidntHelpDidntGetHelpValue = 1;
     private ArrayList<Boolean> engagedFinches;
     
     public Biotope (List<Behavior> behaviors) {
         width = 300;
         height = 200;
         initialHitpoints = 7;
         hitpointsPerRound = 3;
         minMaxAge = 10;
         maxMaxAge = 13;
         finchesPerBehavior = 40;
         breedingProbability = 1.00/3.00;
         finchBehaviors = behaviors;
         initialize();
     }
     
     public Biotope (int width, int height /*eller Dimension*/, double breedingProbability, int maxHitpoints, 
             int initialHitpoints, int hitpointsPerRound, int minMaxAge, int maxMaxAge,
             int finchesPerBehavior, List<Behavior> behaviors) {
         throw new Error("Unimplemented");
         //initialize();
     }
     
     /**
      * Do initialization of objects common to all constructors.
      */
     private void initialize () {
         numberOfBehaviors = 5;
         engagedFinches = new ArrayList(width * height);
         statisticsTree = new TreeMap<String,Statistics>();
         world = new World<GalapagosFinch>(width, height);
 
         for (int i = 0; i < width * height; i++)
             engagedFinches.add(false);
 
         
         for (Behavior b : finchBehaviors) {
             statisticsTree.put(b.toString(), new Statistics());
             for (int fcounter = 0; fcounter < finchesPerBehavior; fcounter++)
                 addRandomFinch(b.clone());
         }
     }
     
     private void addRandomFinch (Behavior behavior) {
         int x = (int) (Math.random() * width);
         int y = (int) (Math.random() * height);
         statisticsTree.get(behavior.toString()).incPopulation();
         if (world.getAt(x, y).element() == null) {
            world.setAt(x, y, new GalapagosFinch(initialHitpoints,randomMaxAge(),behavior));
         } else addRandomFinch(behavior);
     }
 
     private int randomMaxAge () {
         return minMaxAge + (int)(Math.random() * (maxMaxAge - minMaxAge));
     }
 
     public void runRound () {
         for (Statistics stat : statisticsTree.values())
             stat.newRound();
         breed();
         makeMeetings();
         grimReaper();
         round++;
         setChanged();
         notifyObservers();
     }
     
     private void breed () {
         for (Iterator<World<GalapagosFinch>.Place> i = world.randomIterator(); i.hasNext();) {
             World<GalapagosFinch>.Place place = i.next();
             GalapagosFinch finch = place.element();
             if (finch != null && finch.age() > 0 && Math.random() <= breedingProbability) {
                 List<World<GalapagosFinch>.Place> neighbours = place.emptyNeighbours();
                 if (!neighbours.isEmpty()) {
                     world.setAt(neighbours.get(0).xPosition(), neighbours.get(0).yPosition(),
                            new GalapagosFinch(initialHitpoints, randomMaxAge(), finch.behavior().clone()));
                     Statistics stat = statisticsTree.get(finch.behavior().toString());
                     stat.incPopulation();
                     stat.incBorn();
                 }
             }
         }
     }
     
     private void makeMeetings() {
         clearEngagementKnowledge();
         for (Iterator i = world.randomIterator(); i.hasNext(); ) {
             World.Place p = (World.Place)i.next();
             maybeMakeMeeting(p);
         }
     }
 
     private void maybeMakeMeeting(World.Place place) {
         if (place.element() != null && isUnengaged(place)) {
             List<World.Place> filledNeighbours = place.filledNeighbours();
 
             for (World.Place p : filledNeighbours)
                 if (isUnengaged(p)) {
                     engage(p);
                     engage(place);
                     meet((GalapagosFinch)place.element(), (GalapagosFinch)p.element());
                     return;
                 }
         }
     }
 
     private void clearEngagementKnowledge() {
         for (int i = 0; i < engagedFinches.size(); i++)
             engagedFinches.set(i, false);
     }
 
     private void engage(World.Place place) {
         engagedFinches.set(place.xPosition() * height + place.yPosition(), true);
     }
 
     private boolean isUnengaged(World.Place place) {
         return !engagedFinches.get(place.xPosition() * height + place.yPosition());
     }
     
     private void meet(GalapagosFinch finch1, GalapagosFinch finch2) {
         Action finch1Action = finch1.decide(finch2);
         Action finch2Action = finch2.decide(finch1);
 
         finch1.addHitpoints(getMeetingResult(finch1Action, finch2Action));
         finch2.addHitpoints(getMeetingResult(finch2Action, finch1Action));
 
         finch1.response(finch2, finch2Action);
         finch2.response(finch1, finch1Action);
     }
 
     private int getMeetingResult(Action finch1Action, Action finch2Action) {
         if (finch1Action == Action.CLEANING) {
             if (finch2Action == Action.CLEANING) {
                 return HelpedGotHelpValue;
             }
             else if (finch2Action == Action.IGNORING) {
                 return HelpedDidntGetHelpValue;
             }
         } else if (finch1Action == Action.IGNORING) {
             if (finch2Action == Action.CLEANING) {
                 return DidntHelpGotHelpValue;
             }
             else if (finch2Action == Action.IGNORING) {
                 return DidntHelpDidntGetHelpValue;
             }
         }
         throw new Error("Unhandled Action combination");
     }
     
     /**
      * Decrease the hitpoints of all finches by hitpointsPerRound, and find dead finches
      * and remove them from world, and store the changes in statisticsTree.
      */
     private void grimReaper () {
         for (World<GalapagosFinch>.Place p : world) if (p.element() != null) {
             GalapagosFinch f = p.element();
             f.addHitpoints(-hitpointsPerRound);
             f.makeOlder();
             FinchStatus newStatus = f.status();
             if (newStatus != FinchStatus.ALIVE) {
                 Statistics s = statisticsTree.get(f.behavior().toString());
                 if (newStatus == FinchStatus.DEAD_AGE)
                     s.incDeadByAge();
                     else s.incDeadByTicks();
                 s.decPopulation();
                 world.setAt(p.xPosition(),p.yPosition(),null);
             }
         }
     }
     
     public Statistics statistics(Behavior behavior) {
         assert(finchBehaviors.contains(behavior));
         return statisticsTree.get(behavior.toString());
     }
     
     public int round () {
         return round;
     }
     
     public Iterator<World<GalapagosFinch>.Place> worldIterator () {
         return world.iterator();
     }
 }

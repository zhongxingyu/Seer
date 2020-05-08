 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Collections;
 /*
  * @author afisher
  */
 public class NpcWorld implements World {
     private NpcPopulation population;
 
     private int oldAge;
 
     private double mutationChance;
     private double crossoverChance;
     private double deathChance;
 
     // max capacities
     private int eatingCapacity;
     private int sleepingCapacity;
     private int matingCapacity;
 
     // current availability
     private int eatingAvailability;
     private int sleepingAvailability;
     private int matingAvailability;
 
     private int maxHunger;
     private int maxSleepiness;
 
     private int hungerChange;
     private int sleepinessChange;
 
     private int stepNumber;
     private int currentID;
 
     public NpcWorld() {
         population = new NpcPopulation();
 
         // initialize the population
         for (int i = 0; i < Settings.POPULATION_SIZE; i++) {
             population.add(new NpcIndividual(currentID));
             currentID++;
         }
 
         oldAge = Settings.OLD_AGE;
 
         stepNumber = 0;
         currentID  = 0;
 
         mutationChance  = Settings.MUTATION_CHANCE;
         crossoverChance = Settings.CROSSOVER_CHANCE;
         deathChance     = Settings.DEATH_CHANCE;
 
         eatingCapacity   = Settings.EATING_CAPACITY;
         sleepingCapacity = Settings.SLEEPING_CAPACITY;
         matingCapacity   = Settings.MATING_CAPACITY;
 
         eatingAvailability   = eatingCapacity;
         sleepingAvailability = sleepingCapacity;
         matingAvailability   = matingCapacity;
 
         maxHunger     = Settings.MAX_HUNGER;
         maxSleepiness = Settings.MAX_SLEEPINESS;
 
         hungerChange     = Settings.HUNGER_CHANGE;
         sleepinessChange = Settings.SLEEPINESS_CHANGE;
     }
 
     public void setMutationChance(double c) {
         mutationChance = c / 100;
     }
 
     public void setCrossoverChance(double c) {
         crossoverChance = c / 100;
     }
 
     public void setDeathChance(double c) {
         deathChance = c / 100;
     }
 
     // setters for capacities
     public void setEatingCapacity(int c) {
         eatingAvailability += c - eatingCapacity; 
         eatingCapacity = c;
     }
 
     public void setSleepingCapacity(int c) {
         sleepingAvailability += c - sleepingCapacity; 
         sleepingCapacity = c;
     }
 
     public void setMatingCapacity(int c) {
         matingAvailability += c - matingCapacity; 
         matingCapacity = c;
     }
 
     public void setHungerChange(int c) {
         hungerChange = c;
     }
 
     public void setSleepinessChange(int c) {
         sleepinessChange = c;
     }
 
     // genetic operators
     public Dna crossover(Dna d1, Dna d2) {
         System.out.println("Crossing over!");
         return null;
     }
 
     public Dna mutate(Dna d) {
         System.out.println("Mutating some DNA!");
         return null;
     }
 
     public Population merge(Population p1, Population p2) {
         System.out.println("Merging two populations");
         return null;
     }
 
     public void step() {
         System.out.println("Step number " + stepNumber);
         stepNumber++;
 
         ArrayList<Integer> keys = new ArrayList<Integer>(population.getKeys());
         Collections.shuffle(keys);
 
         // go through all of the individuals and increment availabilities
         // if the individual is done with its action
         for (Integer k : keys) {
             NpcIndividual curIndividual = (NpcIndividual)population.get(k);
 
             if (curIndividual.getStepsRemaining() == 0) {
                 if (curIndividual.getCurrentAction() == Const.EATING) {
                     eatingAvailability++;
                 } else if (curIndividual.getCurrentAction() == Const.SLEEPING) {
                     sleepingAvailability++;
                 }
             }
         }
 
         ArrayList<NpcIndividual> matingPoolMales = new ArrayList<NpcIndividual>();
         ArrayList<NpcIndividual> matingPoolFemales = new ArrayList<NpcIndividual>();
 
         for (Integer k : keys) {
             NpcIndividual curIndividual = (NpcIndividual)population.get(k);
 
             // make the individual choose an action and act on it
             // based on the current state of the population
             if (curIndividual.getStepsRemaining() == 0) {
                 ArrayList<Integer> actions = new ArrayList<Integer>();
                 if (eatingAvailability > 0) {
                     actions.add(Const.EATING);
                 }
                 if (sleepingAvailability > 0) {
                     actions.add(Const.SLEEPING);
                 }
                 if (matingAvailability > 0) {
                     actions.add(Const.MATING);
                 }
                 actions.add(Const.PLAYING);
 
                 int action = curIndividual.chooseAction(actions);
 
                 //TODO update the individual's icon
                 if (action == Const.EATING) {
                     eatingAvailability--;
                 } else if (action == Const.SLEEPING) {
                     sleepingAvailability--;
                 } else if (action == Const.MATING) {
                     matingAvailability--;
                     if (curIndividual.getGender() == Const.MALE) {
                         matingPoolMales.add(curIndividual);
                     } else {
                        matingPoolFemales.add(curIndividual);
                     }
                 }
            }
            curIndividual.decreaseStepsRemaining();
 
             if (curIndividual.getCurrentAction() == Const.EATING) {
                 curIndividual.decreaseHunger(hungerChange);
             } else if (curIndividual.getCurrentAction() == Const.SLEEPING) {
                 curIndividual.decreaseSleepiness(sleepinessChange);
             }
 
             curIndividual.increaseHunger();
             curIndividual.increaseSleepiness();
             curIndividual.increaseAge();
 
             //TODO implement death chance
             if (curIndividual.getHunger() > maxHunger) {
                 population.remove(curIndividual);
             }
         }
 
         reproduce(matingPoolMales, matingPoolFemales); // mate the individuals who chose to mate
     }
 
     public void reproduce(ArrayList<Individual> males, ArrayList<Individual> females) {
         System.out.println("Reproducing");
     }
 
     public Population getPopulation() {
         return population;
     }
 
     public Individual mate(Individual i1, Individual i2) {
         System.out.println("Mating two individuals");
         return null;
     }
 }

 package genetic;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 /**
  * 
  * @author Salim
  * 
  */
 public class Genetic {
 	// ***************************************************************************//
 	// ***************************************************************************//
 	// ***************************** CONFIG **********************************//
 	// ***************************************************************************//
 	// ***************************************************************************//
 	public static final int POPULATION_SIZE = 5;
 	public static final int MAX_ROUNDS = 100;
 	public static final double MAX_MUTATATION_PORTION = 0.5;
 	public static final boolean MUTATATION = true;
 	// ***************************************************************************//
 	// ***************************************************************************//
 	// ***************************************************************************//
 
 	private int rounds;
 
 	/* Goal 1 */Individual goal = new Individual(new int[] { 1, 1, 1, 0, 1, 0,
 			1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1 });// length:20
 	// /* Goal 2 */Individual goal = new Individual(new int[] { 1, 1, 1, 0, 1, 0,
 	// 1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1,1,1,1,1,1,0,1,0,1,1,0,0,0,0,1,0,1,0 });// length:40
 	//
 	ArrayList<Individual> population = new ArrayList<Individual>();
 
 	public Genetic() {
 	}
 
 	/**
 	 * This function runs the genetic algorithm
 	 */
 	public void doGen() {
 		createInitialPopulation();
 		// evaluateIndividuals();
 		rounds = 0;
 		while (rounds < MAX_ROUNDS) {
 			System.out.println("Round: " + rounds);
 			rePopulate();
 			evaluateIndividuals();
 			selectNextGeneration();
 			rounds++;
 			printBest();
 		}
 
 	}
 
 	/**
 	 * This function simply prints the best individual in population
 	 */
 	private void printBest() {
 		Individual best = null;
 		for (Individual ind : population) {
 			if (best == null || ind.getValue() > best.getValue())
 				best = ind;
 		}
 		System.out.print("Individual: {");
 		for (int i = 0; i < best.getSequence().length; i++) {
 			if (i != 0)
 				System.out.print(",");
 			if (best.getSequence()[i] != goal.getSequence()[i])
 				System.out.print("(");
 			System.out.print(best.getSequence()[i]);
 			if (best.getSequence()[i] != goal.getSequence()[i])
 				System.out.print(")");
 		}
 		System.out.println("} \t\t Best: " + best.getValue());
 	}
 
 	/**
 	 * This function is responsible of choosing the next Generation out of the parents (old population) and the new ones.
 	 */
 	private void selectNextGeneration() {
 		Collections.sort(population, new IndComp());
 		for (int i = population.size() - 1; i > POPULATION_SIZE - 1; i--) {
 			population.remove(i);
 		}
 	}
 
 	/**
 	 * Simply ReGenerates a new population using either crossover or mutation
 	 */
 	private void rePopulate() {
 
 		if (MUTATATION)
 			mutate();
 		else
 			crossover();
 	}
 
 	/**
 	 * Implementation of mutation algorihtm
 	 */
 	private void mutate() {
		float portion = 0;
 		double probability = (double) 1 / individualLength();
 		int[] tmp = new int[individualLength()];
 		for (int i = 0; i < POPULATION_SIZE; i++) {
 			for (int j = 0; j < individualLength(); j++) {
				if (portion > MAX_MUTATATION_PORTION)
 					break;
 				double rand = Math.random();
 				if (rand < probability) {// mutate => flip bit
 					tmp[j] = 1 - population.get(i).getSequence()[j];
 				} else {
 					tmp[j] = population.get(i).getSequence()[j];
 				}
 			}
 			population.add(new Individual(tmp));
 		}
 	}
 
 	public int individualLength() {
 		return goal.getSequence().length;
 	}
 
 	/**
 	 * Implementation of Crossover technique
 	 * 
 	 * @return
 	 */
 	private void crossover() {
 		Collections.shuffle(population);
 		int size = population.size();
 		for (int i = 0; i < size; i += 2) {
 			if (i == size - 1)
 				break;
 			Individual i1 = population.get(i);
 			Individual i2 = population.get(i + 1);
 			int crossoverPoint = (int) Math.min(
 					(Math.random() * goal.getSequence().length),
 					goal.getSequence().length - 1);
 			crossoverPoint = Math.max(1, crossoverPoint);
 			int[] tmp1 = new int[goal.getSequence().length];
 			int[] tmp2 = new int[goal.getSequence().length];
 			for (int j = 0; j < tmp2.length; j++) {
 				if (j < crossoverPoint) {
 					tmp1[j] = i1.getSequence()[j];
 					tmp2[j] = i2.getSequence()[j];
 				} else {
 					tmp1[j] = i2.getSequence()[j];
 					tmp2[j] = i1.getSequence()[j];
 				}
 			}
 			population.add(new Individual(tmp1));
 			population.add(new Individual(tmp2));
 		}
 
 	}
 
 	/**
 	 * Evaluates (sets the values) of individuals in population
 	 */
 	private void evaluateIndividuals() {
 
 		for (Individual ind : population) {
 			int count = 0;
 			for (int i = 0; i < goal.getSequence().length; i++) {
 				if (goal.getSequence()[i] == ind.getSequence()[i])
 					count++;
 			}
 			ind.setValue((double) count / goal.getSequence().length);
 		}
 
 	}
 
 	/**
 	 * Randomly generates a new population
 	 */
 	private void createInitialPopulation() {
 		for (int i = 0; i < POPULATION_SIZE; i++) {
 			population.add(randomIndividual());
 		}
 	}
 
 	/**
 	 * Creates a single random individual
 	 * 
 	 * @return
 	 */
 	private Individual randomIndividual() {
 		int[] tmp = new int[goal.getSequence().length];
 		for (int i = 0; i < goal.getSequence().length; i++) {
 			int num = 0;
 			if (Math.random() > 0.5) {
 				num = 1;
 			}
 			tmp[i] = num;
 		}
 		return new Individual(tmp);
 	}
 
 	public static void main(String[] args) {
 		Genetic g = new Genetic();
 		g.doGen();
 
 	}
 }
 
 /**
  * A comparator for comparing Individuals that is used for sorting them.
  * 
  * @author Salim
  * 
  */
 class IndComp implements Comparator<Individual> {
 
 	@Override
 	public int compare(Individual i1, Individual i2) {
 		if (i1.getValue() > i2.getValue())
 			return -1;
 		else if (i1.getValue() < i2.getValue())
 			return 1;
 		else
 			return 0;
 	}
 
 }

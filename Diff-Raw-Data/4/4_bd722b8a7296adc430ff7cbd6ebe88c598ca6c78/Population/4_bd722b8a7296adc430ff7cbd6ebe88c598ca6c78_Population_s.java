 package edu.ntnu.EASY;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.ntnu.EASY.incubator.Incubator;
 import edu.ntnu.EASY.individual.Individual;
 
 public class Population<GType, PType> implements Iterable<Individual<GType, PType>>{
 	
 	private FitnessCalculator<PType> fitCalc;
 	private List<Individual<GType, PType>> population;
 	
 	public Population(FitnessCalculator<PType> fitCalc) {
 		this.fitCalc = fitCalc;
 		this.population = new LinkedList<Individual<GType, PType>>();
 	}
 	
 	public Population(Population<GType, PType> population) {
 		this.fitCalc = population.fitCalc;
 		for (Individual<GType, PType> individual : population) {
 			this.population.add(individual);
 		}
<<<<<<< HEAD
 	}
 	
 	public static <GType, PType> Population<GType, PType> getRandomPopulation(Incubator<GType, PType> incubator,
 				FitnessCalculator<PType> fitCalc, 
 				int numIndividuals) {
 		Population<GType, PType> population= new Population<GType, PType>(fitCalc);
 			while(population.size() < numIndividuals) {
 				population.add(incubator.randomIndividual());
 		}
 		return population;
 	}
 	
 	public Population(List<Individual<GType,PType>> individuals) {
 		this.population = new LinkedList<Individual<GType, PType>>(individuals);
=======
>>>>>>> branch 'master' of git+ssh://git@github.com/Expez/EASY.git
 	}
 	
 	public void add(Individual<GType, PType> individual ){
 		population.add(individual);
 	}
 
 	public void addAll(Population<GType, PType> individuals){
 		for(Individual<GType,PType> individual : individuals)
 			population.add(individual);
 	}
 	
 	public Individual<GType, PType> get(int index) {
 		return population.get(index);
 	}
 	
 	public int size() {
 		return population.size();
 	}
 	
 	public Iterator<Individual<GType, PType>> iterator() {
 		return population.iterator();
 	}
 
 	public void drop(int n) {
 		Collections.sort(population);
 		for(int i = 0; i < n; i++){
 			population.remove(0);
 		}
 	}
 	
 	public void drop(Individual<GType, PType> ind) {
 		population.remove(ind);
 	}
 	
 	public void sort(){
 		sort(false);
 	}
 	
 	public void sort(boolean desc){
 		Collections.sort(population);
 		if(desc)
 			Collections.reverse(population);
 	}
 	
 	public Population<GType, PType> copy() {
 		return new Population<GType, PType>(this);
 	}
 	
 	public void clear() {
 		population.clear();
 	}
 	
 	public void shuffle() {
 		Collections.shuffle(population);
 	}
 
 	public Population<GType, PType> getSubset(int size) {
 		Population<GType, PType> copy = copy();
 		while( size < copy.size() ) {
 			copy.population.remove(0);
 		}
 		return copy;
 	}
 	
 	public void updateFitness(){
 		fitCalc.setPopulation(this);
 		for(Individual<GType,PType> individual : population){
 			individual.updateFitness(fitCalc);
 		}
 	}
 
 	public FitnessCalculator<PType> getFitnessCalculator() {
 		return fitCalc;
 	}
 }

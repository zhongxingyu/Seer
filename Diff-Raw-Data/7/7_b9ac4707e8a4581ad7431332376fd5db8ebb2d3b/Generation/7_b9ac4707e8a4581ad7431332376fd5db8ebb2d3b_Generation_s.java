 package net.sprauer.sitzplaner.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import net.sprauer.sitzplaner.EA.Chromosome;
 import net.sprauer.sitzplaner.EA.Configuration;
 import net.sprauer.sitzplaner.EA.EAFactory;
 import net.sprauer.sitzplaner.EA.Strategy;
 
 public class Generation {
 
 	private List<Chromosome> population = new Vector<Chromosome>();
 	Chromosome bestSolution = null;
 	Chromosome worstSolution = null;
 	private final Configuration configuration;
 
 	public Generation(Configuration conf) throws Exception {
 		this.configuration = conf;
 		add(EAFactory.greedy(conf));
 	}
 
 	public Chromosome getBestSolution() {
 		return bestSolution;
 	}
 
 	public void clear() {
 		population = new Vector<Chromosome>();
 		bestSolution = null;
 		worstSolution = null;
 	}
 
 	public Configuration getConfiguration() {
 		return configuration;
 	}
 
 	public List<Chromosome> getPopulation() {
 		return population;
 	}
 
 	private void add(List<Chromosome> children) {
 		for (Chromosome chromosome : children) {
 			add(chromosome);
 		}
 	}
 
 	public void add(Chromosome chrome) {
 		population.add(chrome);
 		if (bestSolution == null || chrome.getFitness() >= bestSolution.getFitness()) {
 			bestSolution = chrome;
 		}
 		if (worstSolution == null || chrome.getFitness() <= worstSolution.getFitness()) {
 			worstSolution = chrome;
 		}
 	}
 
 	public synchronized void evolve() {
 		sortPopulation();
 		List<Chromosome> parents = selecteTheBestAndKillTheRest();
 
 		List<Chromosome> children = new ArrayList<Chromosome>();
 		if (configuration.getStrategy() == Strategy.Rank) {
 			children.addAll(rankBasedSwaps(parents));
 			children.addAll(rankBasedInversions(parents));
 		} else {
 			for (Chromosome chromosome : parents) {
 				children.addAll(chromosome.swap());
 				children.addAll(chromosome.invert());
 			}
 		}
 		add(children);
 	}
 
 	private List<Chromosome> rankBasedSwaps(List<Chromosome> parents) {
 		int round = 1;
 		List<Chromosome> children = new ArrayList<Chromosome>();
 		while (children.size() < configuration.getDescendantsUsingSwap()) {
 			for (int i = 0; i < round && i < parents.size(); i++) {
 				children.add(parents.get(i).doOneSwap());
 			}
 			round++;
 		}
 		return children;
 	}
 
 	private List<Chromosome> rankBasedInversions(List<Chromosome> parents) {
 		int round = 1;
 		List<Chromosome> children = new ArrayList<Chromosome>();
 		while (children.size() < configuration.getDescendantsUsingInversion()) {
 			for (int i = 0; i < round && i < parents.size(); i++) {
 				children.add(parents.get(i).doOneInvert());
 			}
 			round++;
 		}
 		return children;
 	}
 
 	private List<Chromosome> selecteTheBestAndKillTheRest() {
 		List<Chromosome> parents = new ArrayList<Chromosome>();
 		if (configuration.getStrategy() == Strategy.Tournament) {
 			tournamentSelection(parents);
 		} else {
 			int index = 0;
 			double fitness = Double.MAX_VALUE;
 			do {
 				Chromosome chromosome = population.get(index);
 				double delta = Math.abs(fitness - chromosome.getFitness());
				if (delta > 0.5)
 					parents.add(chromosome);
 				index++;
				fitness = chromosome.getFitness();
 			} while (parents.size() < Math.min(population.size(), configuration.getParents()) && index < population.size());
 		}
 
 		clear();
 		if (configuration.isStrategiePlus()) {
 			add(parents);
 		}
 		return parents;
 	}
 
 	private void tournamentSelection(List<Chromosome> parents) {
 		int best = population.size();
 		for (int i = 0; i < configuration.getTournamentSize(); i++) {
 			int index = (int) (Math.random() * population.size());
 			if (index < best) {
 				best = index;
 			}
 		}
 		parents.add(population.get(best));
 	}
 
 	public Chromosome getWorstSolution() {
 		return worstSolution;
 	}
 
 	public void sortPopulation() {
 		Collections.sort(population);
 	}
 }

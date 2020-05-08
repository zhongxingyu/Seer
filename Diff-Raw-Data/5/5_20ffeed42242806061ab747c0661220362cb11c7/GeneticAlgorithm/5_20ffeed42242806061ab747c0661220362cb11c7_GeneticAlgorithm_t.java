 package ch.dritz.zhaw.ci.geneticalg;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Genetic algorithm for dimensions of cylinder with minimal surface and a
  * volume of at least 300. Implemented with:
  * - Individuals with 10bits (5bit diameter, 5bit height)
  * - Rank based selection
  *
  * @author D. Ritz
  */
 public class GeneticAlgorithm
 {
 	private static final int BITS = 5;
 	private static final int NUM = 30;
 	private static final double MIN_G = 300D;
 
 	private static final int MAX = 1 << BITS;
 	private static final int MASK = MAX - 1;
 
 	private static Random rand = new Random();
 
 	private List<Individual> individuals;
 	private List<Individual> bestList;
 
 
 	public GeneticAlgorithm(int num)
 	{
 		individuals = new ArrayList<Individual>(num);
 		bestList = new ArrayList<Individual>();
 
 		for (int i = 0; i < num; i++) {
 			individuals.add(
 				Individual.encode(i, rand.nextInt(MAX), rand.nextInt(MAX)));
 		}
 	}
 
 	public void rankSelection()
 	{
 		// calculate fitness, filter
 		List<Individual> okList = new ArrayList<Individual>();
 		for (Individual ind : individuals) {
 			if (ind.fitness(MIN_G))
 				okList.add(ind);
 		}
 
 		// sort in preparation to calculate rank
 		Collections.sort(okList, new Comparator<Individual>() {
 			@Override
 			public int compare(Individual o1, Individual o2)
 			{
 				/*
 				 * We want to assign the highest rank the lowest fitness value
 				 * because we want to minimize f().
 				 * This sorts the biggest element first.
 				 */
 				return (int) Double.compare(o2.fitness, o1.fitness);
 			}
 		});
 
 		// set rank based on position in sorted list
 		int ranks = 0;
 		for (int i = 0; i < okList.size(); i++) {
 			okList.get(i).rank = i + 1;
 			ranks += i + 1;
 		}
 
 		// calculate a start value between 0.0 and 1.0 based on rank
 		double start = 0;
 		for (Individual ind : okList) {
 			ind.start = start;
 			start += 1D / (double) ranks * ind.rank;
 		}
 
 		// randomly select individuals
 		List<Individual> selectedInd = new ArrayList<Individual>();
 		for (int i = 0; i < individuals.size(); i++) {
 			double r = rand.nextDouble();
 			for (int j = okList.size() - 1; j >= 0; j--) {
 				Individual ind = okList.get(j);
 				if (ind.start <= r) {
 					Individual tmp = ind.duplicate();
 					tmp.index = i;
 					tmp.fitness(MIN_G);
 					selectedInd.add(tmp);
 					break;
 				}
 			}
 		}
 
 		individuals = selectedInd;
 	}
 
 	/**
 	 * Recombines the two individuals
 	 * @param ind1
 	 * @param ind2
 	 */
 	public static void recombine(Individual ind1, Individual ind2)
 	{
 		int where = rand.nextInt(2 * BITS - 2) + 1;
 		int mask1 = (1 << where) - 1;
 		int mask2 = ~mask1 & 0x7FFFFFFF;
 
 		int new1 = (ind1.val & mask2) | (ind2.val & mask1);
 		int new2 = (ind1.val & mask1) | (ind2.val & mask2);
 
 		ind1.reset();
 		ind2.reset();
 		ind1.val = new1;
 		ind2.val = new2;
 	}
 
	public void recombine(int numPairs)
 	{
 		List<Integer> indices = new ArrayList<Integer>(individuals.size());
 		for (int i = 0; i < individuals.size(); i++)
 			indices.add(i);
 
 		for (int i = 0; i < numPairs; i++) {
 			int r = rand.nextInt(indices.size());
 			int idx1 = indices.remove(r);
 
 			r = rand.nextInt(indices.size());
 			int idx2 = indices.remove(r);
 
 			recombine(individuals.get(idx1), individuals.get(idx2));
 		}
 	}
 
 	/**
 	 * for each bit in the individual, flip with the given probability, stopping
 	 * @param ind
 	 * @param prob
 	 */
 	public static void mutate(Individual ind, double prob)
 	{
 		int mask = 1;
 		for (int i = 0; i < BITS; i++) {
 			mask <<= 1;
 			double r = rand.nextDouble();
 			if (r <= prob) {
 				ind.val ^= mask;
 			}
 		}
 		ind.fitness(MIN_G);
 	}
 
 	public void mutate(double prob)
 	{
 		for (Individual ind : individuals)
 			mutate(ind, prob);
 	}
 
 	public void show()
 	{
 		for (Individual ind : individuals) {
 			ind.fitness(MIN_G);
 			System.out.println(ind);
 		}
 	}
 
 	public void saveBest()
 	{
 		Individual best = null;
 		for (Individual ind : individuals) {
 			if (!ind.fitness(MIN_G))
 				continue;
 			if (best == null || ind.fitness < best.fitness)
 				best = ind;
 		}
 		if (best != null) {
 			Individual tmp = best.duplicate();
 			tmp.index = best.index;
 			tmp.fitness(MIN_G);
 			bestList.add(tmp);
 		}
 	}
 
 	public void showBest()
 	{
 		for (int i = 0; i < bestList.size(); i++) {
 			System.out.print("round: ");
 			System.out.print(String.format("%03d", i));
 			System.out.print(" BEST: ");
 			System.out.println(bestList.get(i));
 		}
 	}
 
 	public void round(double mutationProb, int recombinePairs)
 	{
 		rankSelection();
 		if (recombinePairs > 0)
			recombine(recombinePairs);
 		mutate(mutationProb);
 		saveBest();
 	}
 
 	private static class Individual
 	{
 		int index;
 		Integer val;
 
 		int rank = 0;
 		double start = 0D;
 		double fitness = 0D;
 		double g = 0D;
 		boolean ok = false;
 
 		private static Individual encode(int index, int d, int h)
 		{
 			Individual ret = new Individual();
 			ret.index = index;
 			ret.val = (d & MASK) << BITS | (h & MASK);
 			return ret;
 		}
 
 		public int decodeD()
 		{
 			return (val >> BITS) & MASK;
 		}
 
 		public int decodeH()
 		{
 			return val & MASK;
 		}
 
 		public boolean fitness(double minG)
 		{
 			double d = decodeD();
 			double h = decodeH();
 
 			fitness = Math.PI * d * d / 2 + Math.PI * d * h;
 
 			g = Math.PI * d * d * h / 4;
 			ok = g >= minG;
 
 			return ok;
 		}
 
 		public void reset()
 		{
 			start = 0D;
 			rank = 0;
 			fitness = 0D;
 			g = 0D;
 			ok = false;
 		}
 
 		@Override
 		public String toString()
 		{
 			StringBuilder sb = new StringBuilder();
 			sb.append("Individual at ").append(String.format("%02d", index));
 			sb.append(", value: ").append(String.format("%03x", val));
 			sb.append(", d: ").append(String.format("%02d", decodeD()));
 			sb.append(", h: ").append(String.format("%02d", decodeH()));
 			sb.append(", fit: ").append(String.format("%04.3f", fitness));
 			sb.append(", g: ").append(String.format("%04.3f", g));
 			sb.append(", ok: ").append(ok);
 			sb.append(", rank: ").append(rank);
 			sb.append(", start: ").append(String.format("%1.3f", start));
 
 			return sb.toString();
 		}
 
 		public Individual duplicate()
 		{
 			Individual ind = new Individual();
 			ind.val = val;
 			ind.rank = rank;
 			ind.start = start;
 			return ind;
 		}
 	}
 
 
 	////////////////////////////////////////////////////////////////////////////
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		GeneticAlgorithm me = new GeneticAlgorithm(NUM);
 		me.show();
 
 		for (int i = 0; i < 100; i++) {
 			System.out.print("============= Round ");
 			System.out.print(String.format("%03d", i));
 			System.out.println(" ===========================================================");
 			me.round(0.1D, 0);
 			me.show();
 		}
 		me.showBest();
 	}
 }

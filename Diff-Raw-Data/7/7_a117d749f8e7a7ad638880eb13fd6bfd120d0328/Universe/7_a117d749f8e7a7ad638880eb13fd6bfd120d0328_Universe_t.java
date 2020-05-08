 /*
  * Copyright (C) 2010 PIRAmIDE-SP3 authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This software consists of contributions made by many individuals, 
  * listed below:
  *
  * Author: Aitor Almeida <aitor.almeida@deusto.es>
  *         Pablo Orduña <pablo.orduna@deusto.es>
  *         Eduardo Castillejo <eduardo.castillejo@deusto.es>
  *
  */
 package piramide.interaction.reasoner.creator.membershipgenerator.iterativeregion;
 
 import java.util.Arrays;
 import java.util.Map;
 
 class Universe {
 	private final Individual [] individuals;
 	private final int linguisticTermNumber;
 	private final Region [] regions;
 	private final double sumOfTrends;
 	private static final int LIMIT = 30; 
 	
 	Universe(Map<Number, Double> values2trends, String ... linguisticTerms){
 		
 		if(linguisticTerms.length > LIMIT)
 			throw new IllegalArgumentException("Too many linguistic terms! Maximum: " + LIMIT);
 		
 		this.individuals = new Individual[values2trends.size()];
 		
 		final Number [] orderedKeys = values2trends.keySet().toArray(new Number[]{});
 		Arrays.sort(orderedKeys);
 		
 		double trendSum = 0.0;
 		for(int i = 0; i < this.individuals.length; ++i){
 			final Number identity = orderedKeys[i];
 			final double trend = values2trends.get(identity).doubleValue();
 			this.individuals[i] = new Individual(identity, trend);
 			trendSum += trend;
 		}
 		
 		if(trendSum == 0.0)
 			throw new IllegalArgumentException("All values2trends have trend = 0, which can not be splitted");
 		
 		this.sumOfTrends = trendSum;
 		this.linguisticTermNumber = linguisticTerms.length;
 		
 		if(this.linguisticTermNumber < 1)
 			throw new IllegalArgumentException("There must be at least one linguistic term");
 		
 		this.regions = new Region[this.linguisticTermNumber - 1];
 		
 		this.initializeToLeft();
 	}
 	
 	
 	private Universe(Individual [] individuals, int linguisticTermNumber, Region [] regions, double sumOfTrends){
 		this.individuals          = individuals;
 		this.linguisticTermNumber = linguisticTermNumber;
 		this.regions              = regions;
 		this.sumOfTrends		  = sumOfTrends;
 	}
 	
 	private double getIdealTrendForRegion(){
 		return this.sumOfTrends / this.regions.length;
 	}
 	
 	double getSumOfTrends() {
 		return this.sumOfTrends;
 	}
 	
 	private void initializeToLeft(){
 		final double targetTrend = getIdealTrendForRegion();
 		
 		double accumulatedTrend = 0.0;
 		int regionNumber = 1;
 		int previousBoundary = 0;
 		for (int i = 0; i < this.individuals.length; i++) {
 			accumulatedTrend += this.individuals[i].getTrend();
			while(greaterThan(accumulatedTrend, regionNumber * targetTrend, 0.0000001)){
 				this.regions[regionNumber - 1] = new Region(new Boundary(previousBoundary), new Boundary(i), this, regionNumber - 1);
 				if(accumulatedTrend == (regionNumber * targetTrend)){
 					previousBoundary = i + 1;
 				}else{
 					previousBoundary = i;
 				}
 				regionNumber++;
 			}			
 		}
 	}
	
	private boolean greaterThan(double original, double compared, double delta){
		return original >= (compared - delta);
	}
 
 	Individual[] getIndividuals() {
 		return this.individuals;
 	}
 	
 	Region [] getRegions(){
 		return this.regions;
 	}
 
 	int getLinguisticTermNumber() {
 		return this.linguisticTermNumber;
 	}
 	
 	boolean detectRepeatedLinguisticTerms(){
 		for(int i = 0; i < this.regions.length; ++i)
 			for(int j = 0; j < this.regions.length; ++j)
 				if(i != j)
 					if(this.regions[i].getLeftBoundary().getPos() == this.regions[j].getLeftBoundary().getPos() && 
 							this.regions[i].getRightBoundary().getPos() == this.regions[j].getRightBoundary().getPos())
 						return true;
 		return false;
 	}
 	
 	Universe copy(){
 		final Individual [] newIndividuals = new Individual[this.individuals.length];
 		for(int i = 0; i < newIndividuals.length; ++i)
 			newIndividuals[i] = this.individuals[i].copy();
 		
 		final Region [] newRegions = new Region[this.regions.length];
 		final Universe newUniverse = new Universe(newIndividuals, this.linguisticTermNumber, newRegions, this.sumOfTrends);
 		
 		for(int i = 0; i < newRegions.length; ++i)
 			newRegions[i] = this.regions[i].copy(newUniverse);
 		
 		return newUniverse;
 	}
 	
 	int getPermutationsSize(){
 		int value = 1;
 		for(int i = 0; i < this.regions.length; ++i)
 			value *= 4;
 		return value / 2; // Last one does not count
 	}
 	
 	Universe getPermutation(int permutationNumber) throws ArrayIndexOutOfBoundsException {
 		if(permutationNumber >= this.getPermutationsSize() || permutationNumber < 0)
 			throw new ArrayIndexOutOfBoundsException(permutationNumber);
 			
 		final Universe newUniverse = this.copy();
 		int mask = 0;
 		do{
 			final int regionToChange = mask / 2;
 			
 			mask++;
 			final boolean leftRequiresChange = (permutationNumber % (1 << mask) / (1 << (mask - 1))) != 0;
 			mask++;
 			final boolean rightRequiresChange = (permutationNumber % (1 << mask) / (1 << (mask - 1))) != 0;
 			
 			newUniverse.regions[regionToChange] = newUniverse.regions[regionToChange].copy(leftRequiresChange, rightRequiresChange);
 		}while((permutationNumber / (1 << mask)) > 0);
 		
 		return newUniverse;
 	}
 	
 	private static enum Heuristic{
 		closestToMean,
 		closestEachOther
 	}
 	
 	private Heuristic heuristicMethod = Heuristic.closestEachOther;
 	
 	double heuristicValue(){
 		// If any value is missing, return Double.MAX_VALUE
 		if(mustBePenalized())
 			return Double.MAX_VALUE;
 		
 		switch(this.heuristicMethod){
 			case closestToMean:
 				return heuristicClosestToMean();
 			case closestEachOther:
 				return heuristicClosestToEachOther();
 		}
 		
 		throw new IllegalStateException("Heuristic: " + this.heuristicMethod + " not implemented");
 	}
 
 	private double heuristicClosestToEachOther() {
 		double regionMean = 0.0;
 		for(Region region : this.regions)
 			regionMean += region.getTrendSum();
 		
 		regionMean /= this.regions.length;
 		
 		double heuristic = 0.0;
 		for(Region region : this.regions)
 			heuristic += Math.abs(region.getTrendSum() - regionMean);
 	
 		return heuristic;
 	}
 	
 	private double heuristicClosestToMean() {
 		double heuristic = 0.0;
 		
 		final double idealTrend = getIdealTrendForRegion();
 		
 		for(Region region : this.regions){
 			final double regionTrend = region.getTrendSum();
 			heuristic += Math.abs(regionTrend - idealTrend);
 		}
 		
 		return heuristic;
 	}
 
 	/*
 	 * A Universe must be penalized if an individual is not listed in any region. This may happen for two reasons:
 	 *  a) The first boundary has been moved
 	 *  b) There a right boundary that has been moved to the right while the left boundary has not been moved
 	 */
 	private boolean mustBePenalized(){
 		if(this.regions[0].getLeftBoundary().getPos() > 0)
 			return true;
 		
 		for(Region region : this.regions)
 			if(region.getLeftBoundary().getPos() > region.getRightBoundary().getPos())
 				return true;		
 		
 		for(int i = 0; i < this.regions.length - 1; ++i){
 			final Region currentRegion = this.regions[i];
 			final Region nextRegion    = this.regions[i + 1];
 			
 			final int diffValue = nextRegion.getLeftBoundary().getPos() - currentRegion.getRightBoundary().getPos();
 			if(diffValue > 1) //if the two regions overlap, penalize
 				return true;
 		}
 		
 		return false;
 	}
 }

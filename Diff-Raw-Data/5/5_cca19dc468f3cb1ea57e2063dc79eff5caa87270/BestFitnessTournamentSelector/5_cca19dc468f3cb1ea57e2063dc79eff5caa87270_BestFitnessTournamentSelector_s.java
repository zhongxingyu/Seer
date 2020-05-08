 /**
  * 
  */
 package gr.auth.ee.lcs.geneticalgorithm;
 
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import java.util.Arrays;
 
 /**
  * A tournament selecting the best fitness classifier
  * @author Miltos Allamanis
  *
  */
 public class BestFitnessTournamentSelector implements INaturalSelector {
 
 	/**
 	 * The size of the tournaments
 	 */
 	private int tournamentSize;
 	
 	public BestFitnessTournamentSelector(int sizeOfTournaments){
 		this.tournamentSize=sizeOfTournaments;
 	}
 	
 	/**
 	 * Selects from the fromPopulation a set of classifiers that have competed to the tournament
 	 * @see gr.auth.ee.lcs.geneticalgorithm.INaturalSelector#select(int, gr.auth.ee.lcs.classifiers.ClassifierSet, gr.auth.ee.lcs.classifiers.ClassifierSet)
 	 */
 	@Override
 	public void select(int howManyToSelect, ClassifierSet fromPopulation,
 			ClassifierSet toPopulation) {
 		
 		for (int i=0;i<howManyToSelect;i++){
 			//Generate random participants
 			int participants[]=new int[tournamentSize];
 			for (int j=0;i<tournamentSize;i++){
 				participants[j]=(int) Math.floor((Math.random()*fromPopulation.totalNumerosity));
 			}
 			this.tournament(fromPopulation, participants, toPopulation);
 		}
 			
 
 	}
 	
 	/**
 	 * Runs a tournament in the fromPopulation with the size defined in tournamentSize (during construction)
 	 * The winner of the tournament is added to the toPopulation
 	 * @param fromPopulation the source population to run the tournament in
 	 * @param toPopulation the destination population of the tournament winners
 	 * @param participants the int[] of indexes of participants
 	 */
	protected void tournament( ClassifierSet fromPopulation, int[] participants, ClassifierSet toPopulation){
 		
 		//Sort by order
 		Arrays.sort(participants);
 		double bestFitness=Double.NEGATIVE_INFINITY; //Best fitness found in tournament
 		int currentMaxParticipantIndex=-1; //the index in the participants array of the current tournament competitor
 		int bestMacroclassifierParticipant=-1; //The current best participant
 		int currentClassifierIndex=0; //the index of the actual classifier (counting numerosity)
 		int currentMacroclassifierIndex=0; //The index of the macroclassifier being used
 		//Run tournament 
 		do{
 			currentMaxParticipantIndex+=fromPopulation.getClassifierNumerosity(currentMacroclassifierIndex);
 			while (currentClassifierIndex<tournamentSize && participants[currentClassifierIndex]<=currentMaxParticipantIndex){ //currentParicipant is in this macroclassifier
 				if (fromPopulation.getClassifier(currentMacroclassifierIndex).fitness>bestFitness){
 					bestMacroclassifierParticipant=currentMacroclassifierIndex;
 					bestFitness=fromPopulation.getClassifier(currentMacroclassifierIndex).fitness;
 				}
				currentMaxParticipantIndex++; //Next!
 			}
 			currentMacroclassifierIndex++;
 		}while(currentClassifierIndex<tournamentSize);
 		
 		
 		//Add winner to set
 		toPopulation.addClassifier(fromPopulation.getClassifier(bestMacroclassifierParticipant), 1);
 		
 	}
 
 }

 /**
  * 
  */
 package gr.auth.ee.lcs.geneticalgorithm.selectors;
 
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.classifiers.Macroclassifier;
 import gr.auth.ee.lcs.geneticalgorithm.INaturalSelector;
 
 import java.util.Arrays;
 
 /**
  * A tournament selecting the best fitness classifier.
  * 
  * @author Miltos Allamanis
  * 
  */
 public class TournamentSelector implements INaturalSelector {
 
 	/**
 	 * The size of the tournaments.
 	 */
 	private final int tournamentSize;
 
 	/**
 	 * The type of the tournaments.
 	 */
 	private final boolean max;
 
 	/**
 	 * The percentage of population size, used for tournament selection.
 	 */
 	private final double percentSize;
 
 	/**
 	 * The comparison mode for the tournaments.
 	 */
 	private final int mode;
 
 	public TournamentSelector(final double sizeOfTournaments,
 			final boolean max, final int comparisonMode) {
 		this.tournamentSize = 0;
 		this.max = max;
 		this.mode = comparisonMode;
 		percentSize = sizeOfTournaments;
 	}
 
 	/**
 	 * The default constructor of the selector.
 	 * 
 	 * @param sizeOfTournaments
 	 *            the size of the tournament
 	 * @param max
 	 *            if true the we select the max fitness participant, else we
 	 *            select the min.
 	 * @param comparisonMode
 	 *            comparison mode @see
 	 *            gr.auth.ee.lcs.data.UpdateAlgorithmFactoryAndStrategy
 	 */
 	public TournamentSelector(final int sizeOfTournaments, final boolean max,
 			final int comparisonMode) {
 		this.tournamentSize = sizeOfTournaments;
 		this.max = max;
 		this.mode = comparisonMode;
 		percentSize = 0;
 	}
 
 	@Override
 	public int select(ClassifierSet fromPopulation) {
 		int size;
 		if (tournamentSize == 0)
 			size = (int) Math.floor(fromPopulation.getTotalNumerosity()
 					* percentSize);
 		else
 			size = tournamentSize;
 
 		int[] participants = new int[size];
 		// Create random participants
 		for (int j = 0; j < participants.length; j++) {
 			participants[j] = (int) Math.floor((Math.random() * fromPopulation
 					.getTotalNumerosity()));
 		}
 		return this.tournament(fromPopulation, participants);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.geneticalgorithm.INaturalSelector#select(int,
 	 * gr.auth.ee.lcs.classifiers.ClassifierSet,
 	 * gr.auth.ee.lcs.classifiers.ClassifierSet)
 	 */
 	@Override
 	public final void select(final int howManyToSelect,
 			final ClassifierSet fromPopulation, final ClassifierSet toPopulation) {
 
 		for (int i = 0; i < howManyToSelect; i++) {
 
 			toPopulation.addClassifier(
 					new Macroclassifier(fromPopulation.getClassifier(this
 							.select(fromPopulation)), 1), false);
 
 		}
 
 	}
 
 	/**
 	 * Runs a tournament in the fromPopulation with the size defined in
 	 * tournamentSize (during construction) The winner of the tournament is
 	 * added to the toPopulation.
 	 * 
 	 * @param fromPopulation
 	 *            the source population to run the tournament in
 	 * @param participants
 	 *            the int[] of indexes of participants
 	 * @return the index of the tournament winner
 	 */
 	public final int tournament(final ClassifierSet fromPopulation,
 			final int[] participants) {
 
 		// Sort by order
 		Arrays.sort(participants);
 
 		// Best fitness found in tournament
 		double bestFitness = max ? Double.NEGATIVE_INFINITY
 				: Double.POSITIVE_INFINITY;
 
 		// the index in the participants array of the current competitor
 		int currentBestParticipantIndex = -1;
 
 		// The current best participant
 		int bestMacroclassifierParticipant = -1;
 
 		// the index of the actual classifier (counting numerosity)
 		int currentClassifierIndex = 0;
 
 		// The index of the macroclassifier being used
 		int currentMacroclassifierIndex = 0;
 		// Run tournament
 		do {
 			currentBestParticipantIndex += fromPopulation
 					.getClassifierNumerosity(currentMacroclassifierIndex);
 			while ((currentClassifierIndex < participants.length)
 					&& (participants[currentClassifierIndex] <= currentBestParticipantIndex)) {
 
 				// currentParicipant is in this macroclassifier
 				double fitness = fromPopulation.getClassifier(
 						currentMacroclassifierIndex).getComparisonValue(mode);
 
 				if ((max ? 1. : -1.) * (fitness - bestFitness) > 0) {
 					bestMacroclassifierParticipant = currentMacroclassifierIndex;
 					bestFitness = fitness;
 				}
 				currentClassifierIndex++; // Next!
 			}
 			currentMacroclassifierIndex++;
 		} while (currentClassifierIndex < participants.length);
		
		if (bestMacroclassifierParticipant >= 0)
			return bestMacroclassifierParticipant;
		else
			return 0;
 
 	}
 
 }

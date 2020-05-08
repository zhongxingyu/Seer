 package Genetics;
 
 public class Algorithm {
 	
 	private static final double uniformRate = 0.5;
 	private static final double mutationRate = 0.015;
 	private static final int tournamentSize = 5;
 	private static final boolean elitism = true;
 	
 	public static Population evolvePopulation(Population pop)
 	{
 		Population newPopulation = new Population(pop.size(), false);
 		
 		if(elitism){
 			newPopulation.saveIndividual(pop.getFittest());
 		}
 		
 		//crossover population
 		int elitismOffset;
 		if(elitism)
 		{
 			elitismOffset = 1;
 		} else {
 			elitismOffset = 0;
 		}
 		
 		for(int i = elitismOffset; i<pop.size(); i++)
 		{
 			PlayerContainer player1 = tournamentSelection(pop);
			PlayerContianer player2 = tournamentSelection(pop);
 			
 			PlayerContainer newPlayer = crossover(player1, player2);
 			newPopulation.saveIndividual(newPlayer);
 		}
 		
 		//mutate
 		for(int i=elitismOffset; i<newPopulation.size(); i++)
 		{
 			mutate(newPopulation.getPlayer(i));
 		}
 		
 		return newPopulation;
 	}
 	
 	private static PlayerContainer crossover(PlayerContainer p1, PlayerContainer p2)
 	{
 		PlayerContainer newPlayer = new PlayerContainer();
 		
 		for(int i=0; i<p1.size(); i++)
 		{
 			if(Math.random() <= uniformRate)
 			{
 				newPlayer.setGene(i, p1.getGene(i));
 			} else {
 				newPlayer.setGene(i, p2.getGene(i));
 			}
 		}
 		
 		return newPlayer;
 	}
 	
 	private static void mutate(PlayerContainer player)
 	{
 		for(int i=0; i<player.size(); i++)
 		{
 			if(Math.random() <= mutationRate)
 			{
 				double gene = Math.round(Math.random());
 				player.setGene(i, gene);
 			}
 		}
 	}
 	
 	private static PlayerContainer tournamentSelection(Population pop)
 	{
 		Population tournament = new Population(tournamentSize, false);
 		
 		for(int i=0; i< tournamentSize; i++)
 		{
 			int randomId = (int) (Math.random() * pop.size());
 			tournament.saveIndividual(pop.getPlayer(randomId));
 		}
 		
 		PlayerContainer fittest = tournament.getFittest();
 		return fittest;
 	}
 	
 	
 	
 
 }

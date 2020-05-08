 package Genetics;
 
 import AIComponents.aiPlayer;
 
 public class PlayerContainer {
 	
 	aiPlayer myPlayer;
 	
 	public PlayerContainer()
 	{
 		myPlayer = new aiLearner();
 	}
 	
 	public PlayerContainer(aiPlayer player)
 	{
 		myPlayer = player;
 	}
 	
 	public int fitness()
 	{
 		return 1;
 	}
 }

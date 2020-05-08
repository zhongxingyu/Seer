 package me.queekus.programs.ThePotionGame;
 import me.queekus.TissueParticle.*;
 import me.queekus.programs.ThePotionGame.Objects.*;
 import me.queekus.programs.ThePotionGame.api.*;
 
 public class ThePotionGame{
	public static Version version = new Version(0, 0, 0, 5, DevStates.Pre_Alpha);	
 	public static Cauldron Cauldron = new Cauldron();
 	public static PotGameWindow gui;
 	public static void main(String[] args){
 		gui = new PotGameWindow("The Potion Game " + version.toString(), 500, 300);
 			
 		// Cauldron Recipes Added Here
 		Cauldron.addRecipe(new CauldronRecipe(new GameObject[]{ GameObject.flyWings, GameObject.flyWings }).associatePotion(GameObject.healthPotion));
 		
 		GameLogic game = new GameLogic();
 		game.Load();
 		game.Draw();
 		game.Update();
 		
 	}
 	
 }

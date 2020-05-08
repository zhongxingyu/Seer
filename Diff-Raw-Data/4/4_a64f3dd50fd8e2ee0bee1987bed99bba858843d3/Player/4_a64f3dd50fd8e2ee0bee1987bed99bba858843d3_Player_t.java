 package com.hawksman.unnamedgame;
 
 public class Player {
 	
 	public int life = 100;
 	public int maxLife = 100;
 	public int power = 5;
 	public String id;
 	public static String playerName = Game.playerName;
 	
 	public Player(String name) {
 		
 		id = name;
 		
 	}
 	
 	public static Player player = new Player(playerName);
 	
 	public void heal(int healAmount) {
 		
 		this.life += healAmount;
 		
 		if(this.life > this.maxLife) {
 			this.life = this.maxLife;
 		}
 		
		System.out.println("");
		System.out.println("You now have " + Player.player.life + " life.");
		System.out.println("");
		
 		Game.nextStage();
 		
 	}
 	
 	public static void reset() {
 		
 		player.life = 100;
 		player.maxLife = 100;
 		player.power = 5;
 		
 	}
 
 }

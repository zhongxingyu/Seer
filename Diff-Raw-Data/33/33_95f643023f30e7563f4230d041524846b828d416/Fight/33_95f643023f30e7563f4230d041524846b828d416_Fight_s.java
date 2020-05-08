 package com.hawksman.unnamedgame;
 
 import java.util.Scanner;
 
 public class Fight {
 	
 	public static String fightingChoice;
 	static Scanner sc = new Scanner(System.in);
 	public static Monster monster = new Monster(1);
 	public static Player player = Game.player;
 	public static String monsterChoice;
 	public static String getMonsterChoice;
 	public static int monsterInt;
 	
 	public static void start() {
 		
 		System.out.println("What type of monster do you want to fight?:");
 		System.out.println("(W)arrior");
 		System.out.println("(F)ighter");
 		System.out.println("");
 		monsterChoice = sc.next();
 		
 		getMonster(monsterChoice);
 		
 		monster = new Monster(monsterInt);
 		battle();
 
 	}
 	
 	public static void battle() {
 		
 		System.out.println("What do you want to do?:");
 		System.out.println("(A)ttack");
 		System.out.println("(B)lock");
 		System.out.println("");
 		
 		fightingChoice = sc.next();
 		
         battleDamage(fightingChoice);
 		
 	}
 	
 	public static int getMonster(String monsterChoice) {
 		
 		getMonsterChoice = monsterChoice;
 		
 		if(getMonsterChoice.equalsIgnoreCase("f")) {
 			monsterInt = 2;
		}
		
		if(getMonsterChoice.equalsIgnoreCase("w")) {
 			monsterInt = 1;
 		}
 		return monsterInt;
 		
 	}
 	
 	public static void battleDamage(String choice) {
 		
 		boolean monsterAttack;
 		boolean playerDefend;
 		boolean playerAttack;
 		
 		if(choice.equalsIgnoreCase("a")) {
 			playerAttack = true;
 			monsterAttack = true;
 			playerDefend = false;
 		} else if(choice.equalsIgnoreCase("b")) {
 			playerDefend = true;
 			playerAttack = false;
 			monsterAttack = false;
 		} else {
 			playerAttack = false;
 			playerDefend = false;
 			monsterAttack = true;
 		}
 		
 		if(playerAttack) {
 			monster.life -= player.power;
 		} 
 		
 		if(monsterAttack) {
 			player.life -= monster.strength;
 		}
 		
 		if(monsterAttack) {
 			System.out.println("_______________");
 			System.out.println(monster.name + " hit " + player.name + " for " + monster.strength + " damage.");
 			System.out.println(player.name + " does now have " + player.life + " life left.");
 			System.out.println("");
 		}
 		
 		if(playerAttack) {
 			System.out.println(player.name + " hit " + monster.name + " for " + player.power + " damage.");
 			System.out.println(monster.name + " does now have " + monster.life + " life left.");
 			System.out.println("");
 		} else if(!playerAttack && monsterAttack) {
 			System.out.println(player.name + " did nothing.");
 			System.out.println("");
 		}
 		
 		if(playerDefend) {
 			player.life += (monster.life /4);
 			monster.life += (player.life /4);
 			System.out.println(player.name + " defended and gained 1/4 of the life of " + monster.name);
 			System.out.println(player.name + " does now have " + player.life + " life left.");
 			System.out.println(monster.name + " defended and gained 1/4 of the life of " + player.name);
 			System.out.println(monster.name + " does now have " + monster.life + " life left.");
 			System.out.println("");
 		}
 		
 		System.out.println("_______________");
 		
 		if(player.life <= 0) {
 			
 			System.out.println("Too bad! You lost!");
 			System.out.println("");
 
 			MainGame.over();
 			
 		}
 		
 		if(monster.life <= 0) {
 			
 			System.out.println("You beat " + monster.name + "!");
 			player.xp += monster.giveXp;
 			System.out.println("You got " + monster.giveXp + " XP!");
 			System.out.println("You now have " + player.xp + " XP!");
 			System.out.println("");
 			
 			Game.nextStage();
 		}
 		
 		battle();
 	
 	}
 
 }

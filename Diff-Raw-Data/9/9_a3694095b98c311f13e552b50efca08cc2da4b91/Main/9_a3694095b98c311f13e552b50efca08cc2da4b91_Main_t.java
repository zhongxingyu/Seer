 /**
 	EducationalMonopoly
 	Main.java
 	24.03.2012
 */
 package de.dhbw.educationalmonopoly.core;
 
 import de.dhbw.educationalmonopoly.gameRepresentation.IGameRepresentation;
 import de.dhbw.educationalmonopoly.gameRepresentation.swingUI.SwingGameRepresentation;
 import de.dhbw.educationalmonopoly.model.Game;
 import de.dhbw.educationalmonopoly.model.Game.MonopolyType;
 import de.dhbw.educationalmonopoly.model.GameFactory;
 import de.dhbw.educationalmonopoly.model.Player;
 
 /**
  * @author fkrueger
  *
  */
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Game game = GameFactory.createGameWithType(MonopolyType.CLASSIC);
 		IGameRepresentation gameRepresentation = new SwingGameRepresentation();
 		game.setGameRepresenation(gameRepresentation);
 		
 		Player player1 = new Player();
 		Player player2 = new Player();
 		player1.getToken().setFieldIndex(10);
		player2.getToken().setFieldIndex(39);
 		game.addPlayer(player1);
 		game.addPlayer(player2);
 		
 		game.start(); 
 	}
 
 }

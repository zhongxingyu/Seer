 package net.codegames.towerninja;
 import processing.core.PApplet;
 
 public class Main extends PApplet {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 	Game game;
 	
 	public void setup() {
 		size(600, 800);
 		frameRate(30);
 		
 		game = new Game();  
 	}
 	
 	public void draw() {
 		
 		background(128);
 		
 		game.update();
 	}
 	
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "Main" });
 	}
 	
 }

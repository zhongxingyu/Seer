 package com.voracious.dragons.client.screens;
 
 import java.awt.Graphics2D;
 
 import com.voracious.dragons.client.graphics.Screen;
 import com.voracious.dragons.client.graphics.Sprite;
 import com.voracious.dragons.client.graphics.ui.Button;
 import com.voracious.dragons.client.utils.InputHandler;
 
 public class MainMenuScreen extends Screen {
	public static final int WIDTH = 2160/3;
    public static final int HEIGHT = 1440/3;
     private Button playTurn,playNew,spectate,stats;
     private Sprite background;
     
     
 	public MainMenuScreen() {
		super(WIDTH, HEIGHT);
 		
 		this.playTurn=new Button("Play Existing Game",235,120);
 		this.playNew=new Button("Play New Game",410,120);
 		this.spectate=new Button("Spectate a Game",250,220);
 		this.stats=new Button("See Stats",425,220);
 		
 		background = new Sprite("/mainMenuBackground.png");
 		InputHandler.registerScreen(this);
 	}
 
 	@Override
 	public void render(Graphics2D g) {
 		background.draw(g, 0, 0);
 		this.playTurn.draw(g);
 		this.playNew.draw(g);
 		this.spectate.draw(g);
 		this.stats.draw(g);
 		
 	}
 
 	@Override
 	public void tick() {
 	}
 }

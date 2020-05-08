 package com.punchline.NinjaSpacePirate.screens;
 
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.punchline.javalib.Game;
 import com.punchline.javalib.utils.Display;
 
 /**
  * NinjaSpacePirate's main menu screen.
  * @author Natman64
  * @created  Oct 17, 2013
  */
 public class MainMenuScreen extends MenuScreen {
 	
 	private static final String TITLE_0 = "NINJA SPACE";
 	private static final String TITLE_1 = "PIRATE";
 	
	private MenuButton playButton;
	private MenuButton learnButton;
	private MenuButton reflectButton;
	private MenuButton quitButton;
	
 	/**
 	 * Constructs the screen.
 	 * @param game
 	 */
 	public MainMenuScreen(Game game) {
 		super(game);
 		
		playButton = new MenuButton(font, "Play");
		learnButton = new MenuButton(font, "Learn");
		reflectButton = new MenuButton(font, "Reflect");
		quitButton = new MenuButton(font, "Quit");
		
		buttons.add(playButton);
		buttons.add(learnButton);
		buttons.add(reflectButton);
		buttons.add(quitButton);
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		
 		spriteBatch.begin();
 		
 		float x = Display.getPreferredWidth() / 2;
 		float y = 3 * Display.getPreferredHeight() / 4;
 		
 		TextBounds bounds = font.getBounds(TITLE_0);
 		
 		font.draw(spriteBatch, TITLE_0, x - bounds.width / 2, y - bounds.height / 2);
 		
 		y -= bounds.height;
 		y -= LINE_PADDING;
 		bounds = font.getBounds(TITLE_1);
 		font.draw(spriteBatch, TITLE_1, x - bounds.width / 2, y - bounds.height / 2);
 		
 		spriteBatch.end();
 	}
 
 	@Override
 	public void dispose() {
 		font.dispose();
 	}
 
 }

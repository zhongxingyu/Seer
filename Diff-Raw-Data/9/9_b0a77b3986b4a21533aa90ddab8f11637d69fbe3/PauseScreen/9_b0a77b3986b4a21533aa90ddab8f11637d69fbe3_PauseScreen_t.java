 /**
  * 
  */
 package ui;
 
 import input.Controls;
 import input.Controls.Action;
 
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.font.effects.Effect;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.FontUtils;
 
 import states.IntroState;
 import config.Config;
 import entities.Sprite;
 
 /**
  * @author Mullings
  *
  */
 public class PauseScreen extends Sprite {
 	private static final Color color = new Color(0, 0, 0, 220);
 	private UnicodeFont smallFont;
 	private UnicodeFont bigFont;
 	
 	private boolean main = true;
 
 	@SuppressWarnings("unchecked")
 	public PauseScreen() {
 		super(0, 0, Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT);
 		smallFont = Config.MENU_FONT;
         ((List<Effect>) smallFont.getEffects()).add(new ColorEffect(java.awt.Color.WHITE));
         
 		bigFont = Config.BIG_FONT;
         ((List<Effect>) bigFont.getEffects()).add(new ColorEffect(java.awt.Color.WHITE));
 
 	}
 
 	// see below for update
 	public void render(GameContainer gc, Graphics graphics) {
 		graphics.setColor(color);
 		graphics.fillRect(0, 0, Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT);
 		
 		int offset = 20;
 
 		int pauseY = offset;// - font.getLineHeight() / 2;
 		int optionsY = Config.RESOLUTION_HEIGHT / 8 + offset;
 		int controlsY = Config.RESOLUTION_HEIGHT * 3 / 8;
 		int controlListY = Config.RESOLUTION_HEIGHT * 4 / 8;
 		int extraY = Config.RESOLUTION_HEIGHT * 7 / 8;
 		
 		FontUtils.drawCenter(bigFont, "Game paused", 0, pauseY, Config.RESOLUTION_WIDTH);
 		
 		if (main) {
 			FontUtils.drawCenter(smallFont, "Press 'J' to return to level select. Press 'Q' to quit.\nPress 'Escape' to unpause.", 0, optionsY, Config.RESOLUTION_WIDTH);
 			FontUtils.drawCenter(bigFont, "Controls:", 0, controlsY, Config.RESOLUTION_WIDTH);
 			FontUtils.drawCenter(smallFont, 
 					"'A' and 'D' - move left and right\n" +
 					"'W' or 'Space' - jump\n" +
 					"Left Click - shoot or pull hookshot\n" +
 					"Right Click - release hookshot\n" +
 					"'R' - reset level",
 					0, controlListY, Config.RESOLUTION_WIDTH);
 			FontUtils.drawCenter(smallFont,	"Press 'O' for game options.", 0, extraY, Config.RESOLUTION_WIDTH);
 		} else {
 			FontUtils.drawCenter(bigFont, "Options:", 0, controlsY, Config.RESOLUTION_WIDTH);
 			FontUtils.drawCenter(smallFont,
 					"'M' - mute/unmute sounds\n" +
 					"'U' - turn fullscreen on or off\n",
 					0, controlListY, Config.RESOLUTION_WIDTH);
 			FontUtils.drawCenter(smallFont,	"Press 'O' to return to pause screen.", 0, extraY, Config.RESOLUTION_WIDTH);
 		}
 	}
 
 	// Doesn't override sprite because we need access to param game
 	public void update(GameContainer gc, StateBasedGame game, int delta) {
 		if (main) {
 			if (Controls.isKeyPressed(Action.PAUSE)) {
 				unpause(gc);
 			} else if (Controls.isKeyPressed(Action.MENU)) {
 				unpause(gc);
 				game.enterState(IntroState.ID);
 			} else if (Controls.isKeyPressed(Action.QUIT)) {
 				gc.exit();
 			} else if (Controls.isKeyPressed(Action.OPTIONS)) {
 				main = false;
 			}
 		} else {
 			if (Controls.isKeyPressed(Action.OPTIONS)) {
 				main = true;
 			}
 		}
 	}
 
	public void unpause(GameContainer gc) {
 		gc.setPaused(false);
 		gc.setTargetFrameRate(Config.ACTIVE_FRAME_RATE);
 	}
 
 }

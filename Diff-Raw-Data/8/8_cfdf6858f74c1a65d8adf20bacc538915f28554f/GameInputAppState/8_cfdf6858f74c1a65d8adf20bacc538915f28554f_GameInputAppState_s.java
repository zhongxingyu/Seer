 /**
  * 
  */
 package de.findus.cydonia.appstates;
 
 import static de.findus.cydonia.player.InputCommand.ATTACK;
 import static de.findus.cydonia.player.InputCommand.CROSSHAIR;
 import static de.findus.cydonia.player.InputCommand.HUD;
 import static de.findus.cydonia.player.InputCommand.JUMP;
 import static de.findus.cydonia.player.InputCommand.MOVEBACK;
 import static de.findus.cydonia.player.InputCommand.MOVEFRONT;
 import static de.findus.cydonia.player.InputCommand.SCOREBOARD;
 import static de.findus.cydonia.player.InputCommand.STRAFELEFT;
 import static de.findus.cydonia.player.InputCommand.STRAFERIGHT;
 import static de.findus.cydonia.player.InputCommand.SWITCHEQUIP;
 import static de.findus.cydonia.player.InputCommand.USEPRIMARY;
 import static de.findus.cydonia.player.InputCommand.USESECONDARY;
 
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.math.Vector3f;
 
 import de.findus.cydonia.main.GameController;
 import de.findus.cydonia.player.InputCommand;
 
 /**
  * This Appstate controls user inputs and maps them to commands.
  * Should be attached when game is running.
  * 
  * @author Findus
  *
  */
 public class GameInputAppState extends AbstractAppState implements ActionListener{
 
 	private GameController gameController;
 	private InputManager inputManager;
 	private FirstPersonCamera camController;
 	private BitmapText crosshair;
 	
 	/**
 	 * Constructor.
 	 * @param app the game controller
 	 */
 	public GameInputAppState(GameController app) {
 		this.gameController = app;
 		this.inputManager = app.getInputManager();
 		camController = new FirstPersonCamera(app.getCamera(), Vector3f.UNIT_Y);
 	}
 	
 	@Override
 	public void stateAttached(AppStateManager stateManager) {
		inputManager.addMapping(CROSSHAIR.getCode(), new KeyTrigger(KeyInput.KEY_F10));
 		inputManager.addMapping(HUD.getCode(), new KeyTrigger(KeyInput.KEY_F11));
 		inputManager.addMapping(STRAFELEFT.getCode(), new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping(STRAFERIGHT.getCode(), new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping(MOVEFRONT.getCode(), new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping(MOVEBACK.getCode(), new KeyTrigger(KeyInput.KEY_S));
         inputManager.addMapping(JUMP.getCode(), new KeyTrigger(KeyInput.KEY_LSHIFT));
         inputManager.addMapping(USEPRIMARY.getCode(), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addMapping(USESECONDARY.getCode(), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
         inputManager.addMapping(SWITCHEQUIP.getCode(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
         inputManager.addListener(this, CROSSHAIR.getCode(), HUD.getCode(), STRAFELEFT.getCode(), STRAFERIGHT.getCode(), MOVEFRONT.getCode(), MOVEBACK.getCode(), JUMP.getCode(), ATTACK.getCode(), USEPRIMARY.getCode(), USESECONDARY.getCode(), SWITCHEQUIP.getCode());
         
         inputManager.addMapping(SCOREBOARD.getCode(), new KeyTrigger(KeyInput.KEY_TAB));
         inputManager.addListener(this, SCOREBOARD.getCode());
         
         camController.registerWithInput(inputManager);
         camController.setEnabled(true);
         
         if(gameController.isShowCrosshair()) {
         	crosshair = getCrosshair();
         	gameController.getGuiNode().attachChild(crosshair);
         }
     }
 
 	@Override
     public void stateDetached(AppStateManager stateManager) {
 		if(crosshair != null) {
     		crosshair.removeFromParent();
     	}
     	inputManager.removeListener(this);
     	camController.setEnabled(false);
     }
 
 	@Override
     public void onAction(String name, boolean isPressed, float tpf) {
 		InputCommand command = InputCommand.parseInputCommand(name);
 		if(command != null) {
 			gameController.handlePlayerInput(command, isPressed);
 		}
 	}
 	
 	/**
 	 * A plus sign used as crosshairs to help the player with aiming.
 	 */
     public BitmapText getCrosshair() {
       //guiNode.detachAllChildren();
       BitmapFont guiFont = gameController.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
       BitmapText ch = new BitmapText(guiFont, false);
       ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
       ch.setText("+");        // fake crosshairs :)
       ch.setLocalTranslation( // center
         gameController.getContext().getSettings().getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
         gameController.getContext().getSettings().getHeight() / 2 + ch.getLineHeight() / 2, 0);
       return ch;
     }
     
     public void crosshair(boolean visible) {
     	if(crosshair != null) {
     		crosshair.removeFromParent();
     	}
     	if(visible) {
         	crosshair = getCrosshair();
         	gameController.getGuiNode().attachChild(crosshair);
         }
     }
 
 }

 package tetrix.view;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import tetrix.sound.SoundEffects;
 import tetrix.util.Util;
 import tetrix.view.StateHandler.States;
 import tetrix.view.theme.ThemeHandler;
 
 /**
  * Class responsible for the view before the game starts where the user has to
  * choose a level.
  * 
  * @author Linus Karlsson
  * 
  */
 public class LevelsView extends BasicGameState implements IMultipleChoices {
 
 	private int stateID;
 
 	private Image background;
 	private Image hover;
 	private Image easyButton;
 	private Image hardButton;
 
 	private int xPos;
 	private int hoverYPos;
 	private int hoverValue;
 	private int nbrOfChoices;
 	private boolean hasAlreadyEntered;
 
 	private enum Choices {
 		EASY(0, 230), HARD(1, 330);
 
 		private final int id;
 		private final int yPos;
 
 		Choices(int id, int yPos) {
 			this.id = id;
 			this.yPos = yPos;
 		}
 
 		private int id() {
 			return id;
 		}
 
 		private int yPos() {
 			return yPos;
 		}
 	}
 
 	public LevelsView(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		background = ThemeHandler.get(ThemeHandler.BACKGROUND_IMG);
 		hover = ThemeHandler.get(ThemeHandler.HOVER_IMG);
 		easyButton = ThemeHandler.get(ThemeHandler.EASY_IMG);
 		hardButton = ThemeHandler.get(ThemeHandler.HARD_IMG);
 
 		xPos = Util.WINDOW_WIDTH / 2 - hover.getWidth() / 2;
 		nbrOfChoices = Choices.values().length;
 		hasAlreadyEntered = false;
 	}
 	
 	public void enter(GameContainer gc, StateBasedGame sbg) {
 		hoverValue = Choices.EASY.id();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		background.draw(0, 0);
 		hover.draw(xPos, hoverYPos);
 		easyButton.draw(xPos, Choices.EASY.yPos());
 		hardButton.draw(xPos, Choices.HARD.yPos());
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int value)
 			throws SlickException {
 		Input input = gc.getInput();
 
<<<<<<< HEAD
 		if(input.isKeyPressed(Input.KEY_DOWN)) {
=======
		if (input.isKeyPressed(Input.KEY_DOWN)) {
>>>>>>> ef43d2829a19f1994d43dee07097325065327092
 			hoverValue = (hoverValue + 1) % nbrOfChoices;
 		} else if (input.isKeyPressed(Input.KEY_UP)) {
 			hoverValue--;
 			if (hoverValue < 0) {
 				hoverValue = nbrOfChoices - 1;
 			}
 		}
 
 		moveMenuFocus();
 
 		if (input.isKeyPressed(Input.KEY_ENTER)) {
 			if (hoverValue == Choices.EASY.id()) {
 				((GameplayView) sbg.getState(States.GAMEPLAYVIEW.getID()))
 						.setLevel(Util.LEVEL_EASY);
 			} else if (hoverValue == Choices.HARD.id()) {
 				((GameplayView) sbg.getState(States.GAMEPLAYVIEW.getID()))
 						.setLevel(Util.LEVEL_HARD);
 			}
 
 			((GameplayView) sbg.getState(States.GAMEPLAYVIEW.getID()))
 					.setCannonImage(ThemeHandler.getCannon());
 			sbg.enterState(States.GAMEPLAYVIEW.getID(),
 					new FadeOutTransition(), new FadeInTransition());
 
 			if (!hasAlreadyEntered) {
 				((GameplayView) sbg.getState(States.GAMEPLAYVIEW.getID()))
 						.startTimer();
 				hasAlreadyEntered = true;
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 	@Override
 	public void moveMenuFocus() {
 		for (Choices c : Choices.values()) {
 			if (c.id() == hoverValue) {
 				hoverYPos = c.yPos();
 			}
 		}
 	}
 
 }

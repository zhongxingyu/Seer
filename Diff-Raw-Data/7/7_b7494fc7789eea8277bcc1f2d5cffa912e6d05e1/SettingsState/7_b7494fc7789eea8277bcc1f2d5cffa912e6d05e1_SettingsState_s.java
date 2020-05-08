 package game;
 
 import java.util.ArrayList;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import components.ImageRenderComponent;
 
 public class SettingsState extends BasicGameState {
 
 	public static final int ID = 3;
 	private int centerHeight = Game.app.getHeight() / 2;
 	private int centerWidth = Game.app.getWidth() / 2;
 
 	private ArrayList<Entity> buttons;
 	private ArrayList<Image> buttonImages;
 	private Image check, cross;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 		buttons = new ArrayList<Entity>();
 		buttonImages = new ArrayList<Image>();
 		check = new Image("res/buttons/check.png");
 		cross = new Image("res/buttons/cross.png");
 
 		Entity newGameButton = new Entity("Music");
 		buttonImages.add(new Image("res/buttons/music.png"));
 		newGameButton.AddComponent(new ImageRenderComponent("Music Button",
 				buttonImages.get(0)));
 		newGameButton.setPosition(new Vector2f(centerWidth - 100,
 				centerHeight - 125));
 		buttons.add(newGameButton);
 
 		Entity fullscreenButton = new Entity("Fullscreen");
 		buttonImages.add(new Image("res/buttons/fullscreen.png"));
 		fullscreenButton.AddComponent(new ImageRenderComponent(
 				"Fullscreen Button", buttonImages.get(1)));
 		fullscreenButton.setPosition(new Vector2f(centerWidth - 100,
 				centerHeight));
 		buttons.add(fullscreenButton);
 
 		Entity backButton = new Entity("Back");
 		buttonImages.add(new Image("res/buttons/back.png"));
 		backButton.AddComponent(new ImageRenderComponent("Back Button",
 				buttonImages.get(2)));
 		backButton.setPosition(new Vector2f(centerWidth - 100,
 				centerHeight + 125));
 		buttons.add(backButton);
 
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		for (Entity e : buttons) {
 			e.render(gc, sb, g);
 		}
 
 		if (Game.app.isMusicOn()) {
 			g.drawImage(check, buttons.get(0).getPosition().getX()
 					+ buttonImages.get(0).getWidth() + 50, buttons.get(0)
 					.getPosition().getY());
 		} else {
 			g.drawImage(cross, buttons.get(0).getPosition().getX()
 					+ buttonImages.get(0).getWidth() + 50, buttons.get(0)
 					.getPosition().getY());
 		}
 
 		if (Game.app.isFullscreen()) {
 			g.drawImage(check, buttons.get(1).getPosition().getX()
 					+ buttonImages.get(1).getWidth() + 50, buttons.get(1)
 					.getPosition().getY());
 		} else {
 			g.drawImage(cross, buttons.get(1).getPosition().getX()
 					+ buttonImages.get(1).getWidth() + 50, buttons.get(1)
 					.getPosition().getY());
 		}
 

 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 
 		Input input = gc.getInput();
 		int posX = Mouse.getX();
 		int posY = Math.abs(Mouse.getY() - Game.app.getHeight());
 
 		// Music
 		if (posX > buttons.get(0).getPosition().getX()
 				&& posX < buttons.get(0).getPosition().getX()
 						+ buttonImages.get(0).getWidth()
 				&& posY > buttons.get(0).getPosition().getY()
 				&& posY < buttons.get(0).getPosition().getY()
 						+ buttonImages.get(0).getHeight()) {
 			if (input.isMousePressed(0)) {
 				if (Game.app.isMusicOn()) {
 					Game.app.setMusicOn(false);
 				} else {
 					Game.app.setMusicOn(true);
 				}
 			}
 
 		}
 
 		// Fullscreen
 		if (posX > buttons.get(1).getPosition().getX()
 				&& posX < buttons.get(1).getPosition().getX()
 						+ buttonImages.get(1).getWidth()
 				&& posY > buttons.get(1).getPosition().getY()
 				&& posY < buttons.get(1).getPosition().getY()
 						+ buttonImages.get(1).getHeight()) {
 			if (input.isMousePressed(0)) {
 				if (Game.app.isFullscreen()) {
 					Game.app.setFullscreen(false);
 				} else {
 					Game.app.setFullscreen(true);
 				}
 			}
 		}
 
 		// Back
 		if (posX > buttons.get(2).getPosition().getX()
 				&& posX < buttons.get(2).getPosition().getX()
 						+ buttonImages.get(2).getWidth()
 				&& posY > buttons.get(2).getPosition().getY()
 				&& posY < buttons.get(2).getPosition().getY()
 						+ buttonImages.get(2).getHeight()) {
 			if (input.isMousePressed(0)) {
 				sb.enterState(MenuState.ID, new FadeOutTransition(Color.black,
 						200), null);
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }

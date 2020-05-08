 package chalmers.TDA367.B17.states;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.network.Network;
 import chalmers.TDA367.B17.sound.SoundHandler.MusicType;
 import chalmers.TDA367.B17.view.Label;
 import chalmers.TDA367.B17.view.MenuButton;
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.minlog.Log;
 import org.newdawn.slick.*;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.*;
 import chalmers.TDA367.B17.Tansk;
 
 import java.io.IOException;
 
 public class JoinMenu extends BasicGameState{
 
 	private Client client;
 	private TextField serverIPField;
 	private MenuButton joinButton;
 	private Label errorLabel;
 	private Label inputLabel;
 	private int state;
 	private StateBasedGame stateBasedGame;
 	private SpriteSheet background;
 	private MenuButton backButton;
 	
 	public JoinMenu(int state) {
 		this.state = state;
 	}
 
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		this.stateBasedGame = sbg;
 
 		serverIPField = new TextField(gc, gc.getDefaultFont(), 100, 175, 200, 20);
 		inputLabel = new Label("Enter host IP:", Color.black, 100, 150);
 		
 		joinButton = new MenuButton(100, 225,
 				GameController.getInstance().getImageHandler().getSprite("button_join"),
 				GameController.getInstance().getImageHandler().getSprite("button_join_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_join_hover"));
 		errorLabel = new Label("Invalid IP!", Color.red, 100, 195);
 
 		backButton = new MenuButton(100, 325, 
 				GameController.getInstance().getImageHandler().getSprite("button_back"),
 				GameController.getInstance().getImageHandler().getSprite("button_back_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_back_hover"));
 
 		background = new SpriteSheet(GameController.getInstance().getImageHandler().getSprite("background"),
 				Tansk.SCREEN_WIDTH, Tansk.SCREEN_HEIGHT);
 		GameController.getInstance().getSoundHandler().playMusic(MusicType.MENU_MUSIC);
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame stateBasedGame){
 		serverIPField.setFocus(true);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		background.draw();
 		g.drawRect(serverIPField.getX(), serverIPField.getY(), serverIPField.getWidth(), serverIPField.getHeight());
 		serverIPField.render(gc, g);
 		
 		if(client!=null && !client.isConnected())
 			errorLabel.render(g);
 		joinButton.draw();
 		
 		inputLabel.render(g);
 		backButton.draw();
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		if(joinButton.isClicked(gc.getInput()))
 			join();
 		else if(backButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.MENU);
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		super.keyReleased(key, c);
 		if(key == Input.KEY_ENTER){
 			if(serverIPField.hasFocus()){
 				join();
 			}
 		}
 	}
 
 	private void join(){
 		client = new Client();
 		Network.register(client);
 		client.start();

 		try {
 			client.connect(600000, serverIPField.getText(), Network.PORT, Network.PORT);
 		} catch (IOException e) {
 			Log.info("[CLIENT] Failed to connect!");
 			e.printStackTrace();
 			client.stop();
 			return;
 		}
 
		ClientState.getInstance().setClient(client);

 		System.out.println("Attempting to join server...");
 		stateBasedGame.enterState(Tansk.CLIENT);
 	}
 
 
 	@Override
 	public int getID() {
 		return this.state;
 	}
 
 }

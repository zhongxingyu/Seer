 package chalmers.TDA367.B17.view;
 
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.Tansk;
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.model.Player;
 
 public class Scoreboard {
 
 	public static final int RESTART_BUTTON = 1;
 	public static final int MENU_BUTTON = 2;
 	
 	private Font scoreboardFont;
 	private MenuButton toMenuButton;
 	private MenuButton restartButton;
 	private Vector2f position;
 	private int width;
 	private int height;
 	private int currentPressedButton;
 	
 	public Scoreboard(boolean isHost){
 		currentPressedButton = 0;
 		width = 350;
 		height = 350;
 		position = new Vector2f(Tansk.SCREEN_WIDTH/2-width/2, Tansk.SCREEN_HEIGHT/2-height/2);
 		
 		toMenuButton = new MenuButton((int)(position.x + 15), (int)(position.y + height-60), GameController.getInstance().getImageHandler().getSprite("button_menu"),
 				GameController.getInstance().getImageHandler().getSprite("button_menu_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_menu_hover"));
 		if(isHost){
 			restartButton = new MenuButton((int)(position.x + width - 165), (int)(position.y + height-60), GameController.getInstance().getImageHandler().getSprite("button_restart"),
 					GameController.getInstance().getImageHandler().getSprite("button_restart_pressed"),
 					GameController.getInstance().getImageHandler().getSprite("button_restart_hover"));
 			
 			scoreboardFont = new TrueTypeFont(new java.awt.Font("Verdana", java.awt.Font.PLAIN, 22), true);
 		}
 	}
 	
 	
 	public void render(Graphics g){
 		
 		
 		g.setLineWidth(15);
 		g.setColor(new Color(100, 100, 100, 255));
 		int tmpYOffset = 10;
 		g.fillRect(position.x, position.y, width, height);
 		g.setColor(Color.black);
 		g.drawRect(position.x, position.y, width, height);
 		g.setLineWidth(1);
 		
 
 		g.setFont(scoreboardFont);
		String winnerString = "Winner(s): ";
 		List<Player> winningPlayers = GameController.getInstance().getGameMode().getWinningPlayers();
 		for(int i = 0; i<winningPlayers.size(); i++){
 			Player playerAtI = winningPlayers.get(i);
 			winnerString += playerAtI.getName();
 			if(winningPlayers.indexOf(playerAtI) < winningPlayers.size()-1){
 				winnerString += " & ";
 			}
 		}
 		g.drawString(winnerString, position.x+20, position.y+tmpYOffset);
 		
 		tmpYOffset += 45;
 		for(Player p: GameController.getInstance().getGameMode().getPlayerList()){
 			g.setColor(p.getColor());
 			g.drawString(p.getName() + ": " + p.getScore(), position.x+20, position.y+tmpYOffset);
 			tmpYOffset += 30;
 		}
 		
 		toMenuButton.draw();
 		restartButton.draw();
 	}
 	
 	public void update(GameContainer gc){
 		if(toMenuButton.isClicked(gc.getInput())){
 			currentPressedButton = MENU_BUTTON;
 		}
 		
 		if(restartButton.isClicked(gc.getInput())){
 			currentPressedButton = RESTART_BUTTON;
 		}
 	}
 	
 	public int getCurrentPressedButton(){
 		return currentPressedButton;
 	}
 }

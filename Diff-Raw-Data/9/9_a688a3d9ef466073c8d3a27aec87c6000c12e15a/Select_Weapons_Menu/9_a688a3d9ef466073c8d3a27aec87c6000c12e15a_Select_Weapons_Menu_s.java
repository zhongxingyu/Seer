 package com.tiny.tank;
 
 import java.util.ArrayList;
 import java.util.Random;
 
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
 
 import com.tiny.guiComponents.Button;
 import com.tiny.guiComponents.SimpleTempButton;
 import com.tiny.weapons.Shot;
 import com.tiny.weapons.shots.Shots;
 //github.com/Zylox/GroupBeta.git
 
 public class Select_Weapons_Menu extends BasicGameState {
 	private int id;
 	private Image title = null;
 	private Image p1_title = null;
 	private Image p2_title = null;
 
 	private Button button_play = null;
 	private Button button_play_grey = null;
 	private Button button_menu = null;
 	private Image ammo_title = null;
 	private Image select = null;
 	private boolean playButtonActive = false;
 
 	private int ycord = 200;
 	private int playercount = 0;
 	private int spacing = 20;
 	private Image background = null;
 	private ArrayList<SimpleTempButton> buttons;
 	private ArrayList<SimpleTempButton> p1weapons;
 	private ArrayList<SimpleTempButton> p2weapons;
 	
 	public Select_Weapons_Menu(int id){
 		this.id = id;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game) throws SlickException {
 			
 		
 	}
 		
 	public void loadImages() throws SlickException{
		background = new Image("res/bg.jpg");
 		title = new Image("res/Weapon_Select_ title.png");
 		select = new Image("res/selectweapon.png");
 		button_play= new Button("res/play_button.png",500,520);
 		button_play_grey= new Button("res/play_button.png",500,520);
 		button_menu= new Button("res/back_button.png",80,520);
 		p1_title= new Image("res/P1.png");
 		p2_title= new Image("res/P2.png");
 		ammo_title= new Image("res/ammo.png");		
 	}
 //
 //		public void render(GameContainer container, StateBasedGame game, Graphics g)
 //		throws SlickException {
 //			
 //			background.draw(0,0);
 //			title.draw(0,0);
 //			if (playButtonActive)
 //			{
 //				button_play.drawButton(g);
 //			}else
 //				button_play_grey.drawButton(g);
 //
 //			button_menu.drawButton(g);
 //			p1_title.draw(30, 150);
 //			p2_title.draw(540,150);
 //			ammo_title.draw(240,80);
 //			
 //
 //			//Transparent boxes for each player
 //			g.setColor(new Color(300,300,300, 0.5f));
 //			g.fillRect(60, 200, 170, 300);
 //			g.fillRect(560,200, 170, 300);
 //			
 //			// ammunition select from box
 //			g.setColor(new Color(176, 176, 176, 0.8f));
 //			g.fillRect(300, 155, 200, 345);
 //			
 //			g.setColor(Color.black);
 //			for(SimpleTempButton s : buttons){
 //				s.render(container, game, g);
 //			}
 //			for(SimpleTempButton s : p1weapons){
 //				s.render(container, game, g);
 //			}
 //			for(SimpleTempButton s : p2weapons){
 //				s.render(container, game, g);
 //			}
 //			g.setColor(Color.white);
 //			
 //			
 //			if(odd || playercount == 0 && (playercount -1 <= buttons.size()))
 //			{
 //			select.draw(20, 110);
 //			}else if (even && (playercount -1 <= buttons.size()))
 //			{
 //			select.draw(530, 110);
 //			}
 //				
 //			
 //		
 //		}
 
 		
 		/**
 		 * Function to change the state of the game
 		 * You can either enter the game or leave to the Main Menu
 		 * 
 		 */
 	
 
 		/**
 		 * This function counts the number of players and allows the users to assign 
 		 * guns to each player. Based on the even/odd value of the players, the game will assign 
 		 * weapons in that order.
 		 * @param s
 		 * @param y
 		 */
 	
 		
 	private void ButtonPressed(SimpleTempButton s, int y) {
 
 		boolean even = (playercount % 2 == 0);
 
 			// even number selected weapons go to Player 1
 			if (even) {
 				int x1 = 60;
 
 				p1weapons.add(new SimpleTempButton(new Vector2f(x1, ycord), 173,
 						spacing, s.getShot()));
 				playercount += 1;
 				System.out.println("EVEN!!  selection #:" + playercount);
 
 			}
 			// odd number selected weapons go to Player2
 			else if (!even) {
 				int x2 = 560;
 
 				p2weapons.add(new SimpleTempButton(new Vector2f(x2, ycord), 173,
 						spacing, s.getShot()));
 				// increment Y value by 20 to put the next button below the previous
 				ycord += spacing;
 				playercount += 1;
 				System.out.println("ODD!!  selection #:" + playercount);
 			}
 
 			// if all weapons are chosen, enable play button
 			buttons.remove(s);
 			if (buttons.size() == 0) {
 				playButtonActive = true;
 			}
 		}
 		
 
 		/**
 		 * this is the function that takes the button input and assigns that button
 		 * to the weapons array of shots
 		 * @return
 		 */
 	
 		/**
 		 * this is the function that takes the button and returns it as a shot
 		 * @param buttons
 		 * @return
 		 */
 //		private ArrayList<Shot> convertButtonsToShots(ArrayList<SimpleTempButton> buttons){
 //			ArrayList<Shot> shots = new ArrayList<Shot>();
 //			
 //			for(SimpleTempButton s : buttons){
 //				shots.add(s.getShot());
 //			}
 //			
 //			return shots;
 //			
 //		}
 		
 
 		
 
 		
 		
 	
 
 	
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) {
 
 		int x = 300;
 		int y = 155;
 
 		int weaponsCount = 10;
 		Shots[] shots = Shots.values();
 		Shot[] shats = new Shot[weaponsCount];
 		Random ran = new Random();
 		for (int i = 0; i < weaponsCount; i++) {
 			shats[i] = shots[ran.nextInt(shots.length)].getCopyOfShot();
 		}
 
 		buttons = new ArrayList<SimpleTempButton>();
 		p1weapons = new ArrayList<SimpleTempButton>();
 		p2weapons = new ArrayList<SimpleTempButton>();
 
 		playButtonActive = false;
 
 		ycord = 200;
 		playercount = 0;
 
 		for (Shot s : shats) {
 			buttons.add(new SimpleTempButton(new Vector2f(x, y), 200, spacing,
 					s));
 			y += spacing;
 		}
 		container.getInput().clearMousePressedRecord();
 		container.getInput().clearKeyPressedRecord();
 	}
 
 	@Override
 	public void leave(GameContainer container, StateBasedGame game) {
 		TinyTank.setPreviousState(id);
 	}
 
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		background.draw(0, 0);
 		title.draw(0, 0);
 		if (playButtonActive) {
 			button_play.drawButton(g);
 		} else
 
 			button_play_grey.drawButton(g);
 
 		button_menu.drawButton(g);
 		p1_title.draw(30, 150);
 		p2_title.draw(540, 150);
 		ammo_title.draw(240, 80);
 
 		// Transparent boxes for each player
 		g.setColor(new Color(300, 300, 300, 0.5f));
 		g.fillRect(60, 200, 170, 300);
 		g.fillRect(560, 200, 170, 300);
 
 		// ammunition select from box
 		g.setColor(new Color(176, 176, 176, 0.8f));
 		g.fillRect(300, 155, 200, 345);
 
 		g.setColor(Color.black);
 		for (SimpleTempButton s : buttons) {
 			s.render(container, game, g);
 		}
 		for (SimpleTempButton s : p1weapons) {
 			s.render(container, game, g);
 		}
 		for (SimpleTempButton s : p2weapons) {
 			s.render(container, game, g);
 		}
 		g.setColor(Color.white);
 
 		if (!playButtonActive && playercount % 2 == 0) {
 			select.draw(20, 110);
 		} else if (!playButtonActive && playercount % 2 != 0) {
 			select.draw(530, 110);
 		}
 
 	}
 
 	/**
 	 * Function to change the state of the game You can either enter the game or
 	 * leave to the Main Menu
 	 * 
 	 */
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 
 		int posX = Mouse.getX();
 		int posY = Mouse.getY();
 
 		boolean clicked = container.getInput().isMousePressed(
 				Input.MOUSE_LEFT_BUTTON);
 		boolean moveList = false; 
 		for (int s = 0; s < buttons.size(); s++) {
 			if (buttons.get(s).update(container.getInput(), clicked)) {
 				System.out.println(buttons.get(s).getShot().getShotName());
 				ButtonPressed(buttons.get(s), ycord);
 				moveList = true;
 			}
 			if(moveList && !playButtonActive && s<buttons.size()){
 				buttons.get(s).moveInY(-spacing);
 			}
 		}
 
 		if (playButtonActive) {
 			// If play game button is clicked on play game, enter the play game
 			// state
 			if ((posX > 500 && posX < 720) && (posY > 20 && posY < 80)) {
 				if (Mouse.isButtonDown(0)) {
 					game.enterState(STATES.MAIN_GAMEPLAY.getId());
 				}
 
 			}
 		}
 		// If back button is clicked, change state to main menu state
 		if ((posX > 80 && posX < 300) && (posY > 20 && posY < 80)) {
 			if (Mouse.isButtonDown(0)) {
 				game.enterState(STATES.MAIN_MENU.getId());
 			}
 		}
 
 	}
 
 	/**
 	 * This function counts the number of players and allows the users to assign
 	 * guns to each player. Based on the even/odd value of the players, the game
 	 * will assign weapons in that order.
 	 * 
 	 * @param s
 	 * @param y
 	 */
 
 
 	/**
 	 * this is the function that takes the button input and assigns that button
 	 * to the weapons array of shots
 	 * 
 	 * @return
 	 */
 	public ArrayList<Tank> getTanks() {
 		ArrayList<Tank> tanks = new ArrayList<Tank>();
 		for (int i = 0; i < 2; i++) {
 			tanks.add(new Tank());
 		}
 		tanks.get(0).TankInfo(200, 10, 70, 100,
 				convertButtonsToShots(p1weapons), 1);
 		tanks.get(1).TankInfo(600, 10, 70, 100,
 				convertButtonsToShots(p2weapons), 2);
 
 		tanks.get(0).setFirstPos();
 		tanks.get(1).setFirstPos();
 
 		return tanks;
 
 	}
 
 
 	/**
 	 * this is the function that takes the button and returns it as a shot
 	 * 
 	 * @param buttons
 	 * @return
 	 */
 	private ArrayList<Shot> convertButtonsToShots(
 			ArrayList<SimpleTempButton> buttons) {
 		ArrayList<Shot> shots = new ArrayList<Shot>();
 
 		for (SimpleTempButton s : buttons) {
 			shots.add(s.getShot());
 		}
 
 		return shots;
 
 	}
 
 	public int getID() {
 		return id;
 	}
 
 }

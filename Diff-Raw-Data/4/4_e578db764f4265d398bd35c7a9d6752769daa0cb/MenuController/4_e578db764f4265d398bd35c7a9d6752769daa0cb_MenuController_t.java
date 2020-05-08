 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JPanel;
 
 import model.GameModel;
 import model.items.weapons.WeaponFactory;
 import model.sprites.Player;
 
 import view.GamePanel;
 import view.Window;
 import view.menu.LoadingScreen;
 import view.menu.MainMenu;
 import view.menu.MenuButton;
 import view.menu.PauseMenu;
 
 /**
  * @author Vidar Eriksson
  *
  */
 public class MenuController {
 	private static MenuController instance = null;
 	private static final MainMenu mainMenuPanel = new MainMenu("Main Menu", createMainMenuButtons());
 	private static final PauseMenu pauseMenuPanel = new PauseMenu("PAUSE", createPauseMenuButtons());
 	private static final Window window = Window.getInstance();
 	private static JPanel activePanel = null;
 	private static GameController gameController = null;
 	private static GamePanel gamePanel = null;
 
 
 	private MenuController() {
 	}
 
 	private static MenuButton[] createMainMenuButtons() {
 		MenuButton buttons[] = new MenuButton[5];
 		
 		buttons[0]= new MenuButton("New Game");
 		buttons[0].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				startGame();
 			}
 		});
 		
 		buttons[1]= new MenuButton("Load Game");
 		buttons[1].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				load();
 			}
 		});
 		
 		buttons[2]= new MenuButton("Highscore");
 		buttons[2].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				highscore();
 			}
 		});
 		
 		buttons[3]= new MenuButton("Settings");
 		buttons[3].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				settings();
 			}
 		});
 		
 		buttons[4]= new MenuButton("Exit Game");
 		buttons[4].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				exitGame();
 			}
 		});
 		
 		return buttons;
 	}
 	
 	private static MenuButton[] createPauseMenuButtons() {
 		MenuButton buttons[] = new MenuButton[5];
 		
 		buttons[0]= new MenuButton("Settings");
 		buttons[0].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				settings();
 			}
 		});
 		
 		buttons[1]= new MenuButton("Save / Load");
 		buttons[1].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveLoad();
 			}
 		});
 		
 		buttons[2]= new MenuButton("Main Menu");
 		buttons[2].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainMenu();
 			}
 		});
 		
 		buttons[3]= new MenuButton("Exit Game");
 		buttons[3].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				exitGame();
 			}
 		});
 		
 		buttons[4]= new MenuButton("Resume");
 		buttons[4].addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				resumeGame();
 			}
 		});
 		
 		return buttons;
 	}
 
 	public static MenuController getInstance() {
 		if (instance==null){
 			instance = new MenuController();
 		}
 		return instance;
 	}
 	
 	private static void startGame(){
 		//TODO
 		LoadingScreen l = new LoadingScreen(100);
 		changeWindowWiewTo(l);
 		for(int a=0; a < 90; a++){
 			l.increase();
 		}
		if (gameController == null){
			createGameController();
		}
 		changeWindowWiewTo(gamePanel);
 	}
 	private static void exitGame(){
 		System.out.println("Game Terminated sucsessfully");
 		System.exit(0);
 	}
 	private static void highscore(){
 		//TODO
 	}
 	private static void settings(){
 		//TODO
 	}
 	private static void saveLoad(){
 		//TODO
 	}
 	private static void load(){
 		//TODO
 	}
 	public static void mainMenu(){
 		//TODO
 		changeWindowWiewTo(mainMenuPanel);
 	}
 	public void pauseMenu(){
 		//TODO
 		try {
 			gameController.wait();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		changeWindowWiewTo(pauseMenuPanel);
 	}
 	private static void changeWindowWiewTo(JPanel p){
 		if (activePanel != null){
 			window.remove(activePanel);
 		}
 		window.add(p);
 		activePanel=p;
 		p.requestFocus();
 		window.revalidate();
 	}
 	private static void resumeGame() {
 		gameController.notify();
 		changeWindowWiewTo(gamePanel);
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private static void createGameController(){
 		Input input = new Input();
 		GameModel gameModel = createGameModel();
 		gameController = new GameController(gameModel, input);
 		gamePanel = new GamePanel(gameModel, gameController);
 		
 
 		input.setContainer(gamePanel);
 		gameModel.addListener(gamePanel);
 
 				
 		//Starts all the loops
 		gamePanel.start();
 		gameController.start();
 	}
 	private static GameModel createGameModel() {
 		GameModel model = new GameModel();
 		Player player = new Player(50,50);
 		player.setWeapon(WeaponFactory.startingWeapon());
 		model.setPlayer(player);
 		
 		
 		// TODO Auto-generated method stub
 		return model;
 	}
 
 }

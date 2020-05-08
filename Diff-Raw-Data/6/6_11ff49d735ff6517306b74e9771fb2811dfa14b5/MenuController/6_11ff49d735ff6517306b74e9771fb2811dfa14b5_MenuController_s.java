 package controller;
 
 import inputOutput.GameIO;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import model.GameModel;
 
 
 import resources.Language;
 
 
 
 import view.Window;
 import view.menu.LoadingPanel;
 import view.menu.MenuButton;
<<<<<<< HEAD
import view.menu.PauseMenuPanel;
=======
 import view.menu.MenuPanel;
 import view.menu.BooleanPopupMenu;

>>>>>>> Vidar
 import view.menu.subMenuPanels.Score;
 import view.menu.subMenuPanels.SaveLoadGame;
 import view.menu.subMenuPanels.Settings;
 
 /**
  * The controller which is responsible for managing the program window and it's menus and the sub menus.
  * 
  * @author Vidar Eriksson
  *
  */
 public class MenuController{
 	private static final Window WINDOW = new Window();
 	private static GameController gameController = new GameController();
 	
 	/**
 	 * Creates a new game window and starts the main menu for the game.
 	 */
 	public MenuController() {
 		showMainMenu();		
 	}
 	/**
 	 * Changes this controllers window to the main menu.
 	 */
 	public static void showMainMenu(){
 		WINDOW.add(new MenuPanel(Language.getMainMenuText(), MenuButtons.getMainMenuButtons()));
 	}
 	//TODO temp public
 	public static void startGame(GameModel m){
 		if (m == null){
 			startNewGame();
 		}
 		
 		WINDOW.add(new LoadingPanel());
 		
 		gameController.init(m);
 
 		WINDOW.add(gameController.getGamePanel());
 		new Thread(gameController).start();
 	}
 	private static void startNewGame(){
 		startGame(GameIO.newGame());
 	}
 	private static void exitGame(){
 		System.exit(0);
 	}
 	private static void showhighscore(){
 		WINDOW.add(new Score(MenuButtons.getMainMenuButton()));
 	}
 	private static void showSettingsFromMainMenu(){
 		WINDOW.add(new Settings(MenuButtons.getMainMenuButton()));
 	}
 	private static void showSettingsFromPauseMenu(){
 		WINDOW.add(new Settings(MenuButtons.getPauseMenuButton()));
 	}
 	private static void showSaveLoadGame(){
 		WINDOW.add(new SaveLoadGame(new MenuButton[]{MenuButtons.getLoadButton(),
 				MenuButtons.getSaveButton(), MenuButtons.getPauseMenuButton()}, true));
 	}
 	private static void showLoadSavedGame(){
 		WINDOW.add(new SaveLoadGame(new MenuButton[]{MenuButtons.getLoadButton(),
 				MenuButtons.getSaveButton(), MenuButtons.getPauseMenuButton()}, false));
 	}
 	/**
 	 * Changes this controllers window to the game over view.
 	 */
 	public static void showGameOverPanel(){
 		WINDOW.add(new Score(MenuButtons.getMainMenuButton()));
 	}
 	/**
 	 * Changes this controllers window to the paused game menu.
 	 */
 	public static void showPauseMenu(){
 		WINDOW.add(new MenuPanel(Language.getPauseText(), MenuButtons.getPauseMenuButtons()));
 	}
 	
 	private static void resumeGame() {
 		WINDOW.add(gameController.getGamePanel());
 		gameController.resumeThread();
 	}
 	
 	
 	private static class MenuButtons {
 		private static MenuButton mainMenuButtons[] = null;
 		private static MenuButton pauseMenuButtons[] = null;
 		private static MenuButton toMainMenuButton = null;
 		private static MenuButton toPauseMenuButton = null;
 		private static MenuButton saveButton = null;
 		private static MenuButton loadButton = null;
 		
 		private static MenuButton getSaveButton() {
 			if (saveButton == null){
 				saveButton = new MenuButton("Save");
 				saveButton .addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						GameIO.saveGame(gameController.getGameModel());
 //						showMainMenu();//TODO
 					}
 				});
 			}
 			return saveButton;
 		}
 		
 		private static MenuButton getLoadButton() {
 			if (loadButton == null){
 				loadButton = new MenuButton("Load");
 				loadButton .addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						startGame(GameIO.loadGame());
 //						showMainMenu();//TODO
 					}
 				});
 			}
 			return loadButton;
 		}
 		
 		
 		private static MenuButton getMainMenuButton() {
 			if (toMainMenuButton == null){
 				toMainMenuButton = new MenuButton(Language.getMainMenuText());
 				toMainMenuButton .addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showMainMenu();
 					}
 				});
 			}
 			return toMainMenuButton;
 		}
 		
 		private static MenuButton getPauseMenuButton() {
 			if (toPauseMenuButton == null){
 				toPauseMenuButton = new MenuButton(Language.getBackToMenuText());
 				toPauseMenuButton .addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showPauseMenu();
 					}
 				});
 			}
 			return toPauseMenuButton;
 		}
 		
 		private static MenuButton[] getMainMenuButtons() {
 			if (mainMenuButtons==null){
 				
 				MenuButton[] buttons = new MenuButton[5];
 				
 				buttons[0]= new MenuButton(resources.Language.getNewGameText());
 				buttons[0].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						startNewGame();
 					}
 				});
 				
 				buttons[1]= new MenuButton(resources.Language.getLoadGameText());
 				buttons[1].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showLoadSavedGame();
 					}
 				});
 				
 				buttons[2]= new MenuButton(resources.Language.getHighScoreText());
 				buttons[2].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showhighscore();
 					}
 				});
 				
 				buttons[3]= new MenuButton(resources.Language.getSettingsText());
 				buttons[3].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showSettingsFromMainMenu();
 					}
 				});
 				
 				buttons[4]= new MenuButton(resources.Language.getExitGameText());
 				buttons[4].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						exitGame();
 					}
 				});
 				mainMenuButtons=buttons;
 			}
 			
 			return mainMenuButtons;
 		}
 
 		private static MenuButton[] getPauseMenuButtons() {
 			if (pauseMenuButtons==null){
 				MenuButton buttons[] = new MenuButton[5];
 				
 				buttons[0]= new MenuButton(resources.Language.getSettingsText());
 				buttons[0].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showSettingsFromPauseMenu();
 					}
 				});
 				
 				buttons[1]= new MenuButton(resources.Language.getSaveLoadGameText());
 				buttons[1].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						showSaveLoadGame();
 					}
 				});
 				
 				buttons[2]= new MenuButton(resources.Language.getMainMenuText());
 				buttons[2].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 //						int optionButton = JOptionPane.YES_NO_OPTION;
 //						optionButton = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit the game?", 
 //								"Confirm", optionButton);
 //						if(optionButton == JOptionPane.YES_OPTION){
 //							showMainMenu();
 //						}else{
 //							showPauseMenu();
 //						}
 						showMainMenu();
 						
 //						//TODO
 ////						PopupMenu popup = new PopupMenu("test 1", "test22222");
 //						int optionButton = BooleanPopupMenu.showConfirmDialog(null, "Are you sure you want to exit the game?", 
 //								"Confirm", BooleanPopupMenu.YES_NO_OPTION);
 //						if(optionButton == BooleanPopupMenu.YES_OPTION){
 //							showMainMenu();
 //						}else{
 //							showPauseMenu();
 //						}
 						
 //						BooleanPopupMenu popup = new BooleanPopupMenu("Return to Main Menu Without saving?", "Yes", "No");
 //						popup.repaint();
 //						while (!popup.hasGotResult()){
 //							//Wait
 //						}
 //						BooleanPopupMenu popup = new BooleanPopupMenu("Return to Main Menu Without saving?", "Yes", "No").getResult());
 	
 						
 //						if (new BooleanPopupMenu("Return to Main Menu Without saving?", "Yes", "No").getResult()){
 //							showMainMenu();
 //							System.out.println("answear: yES");
 //						} else {
 //							System.out.println("Answear: no");
 //							showPauseMenu();
 //						}
 						
 						
 						
 					}
 				});
 				
 				buttons[3]= new MenuButton(resources.Language.getExitGameText());
 				buttons[3].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						
 //						int optionButton = JOptionPane.YES_NO_OPTION;
 //						optionButton = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit the game?", 
 //								"Confirm", optionButton);
 //						if(optionButton == JOptionPane.YES_OPTION){
 //							exitGame();
 //						}else{
 //							showPauseMenu();
 //						}
 						//TODO
 						exitGame();
 					}
 				});
 				
 				buttons[4]= new MenuButton(resources.Language.getResumeGameText());
 				buttons[4].addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						resumeGame();
 					}
 				});
 				pauseMenuButtons=buttons;
 			}
 			return pauseMenuButtons;
 		}
 
 	}
 
 }

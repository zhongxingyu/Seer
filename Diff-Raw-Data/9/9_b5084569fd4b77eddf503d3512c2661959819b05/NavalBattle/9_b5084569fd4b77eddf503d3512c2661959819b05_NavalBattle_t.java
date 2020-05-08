 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle;
 
 import java.io.ByteArrayOutputStream;
 
 import javax.swing.UIManager.*;
 import javax.swing.*;
 
 import com.jpii.gamekit.localization.LocalizationManager;
 import com.jpii.navalbattle.data.*;
 import com.jpii.navalbattle.debug.*;
 import com.jpii.navalbattle.game.HookStream;
 import com.jpii.navalbattle.game.SinglePlayerGame;
 import com.jpii.navalbattle.gui.Window;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.renderer.*;
 
 import com.roketgamer.RoketGamer;
 
 public class NavalBattle {
 
 	private static RoketGamer roketGamer;
 	private static DebugWindow debugWindow;
 	private static GameState gameState;
 	private static WindowHandler windowHandler;
 	private static LocalizationManager localizationManager;
 	
 	/**
 	 * <code>NavalBattle</code> main method. Ran on launch.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 		    public void run() {
 		    	onShutdown();
 		    }
 		}));
 		
 		
 		Helper.LoadStaticResources();
 		setDefaultLookAndFeel();
 		debugWindow = new DebugWindow();
 		System.setOut(new HookStream(new ByteArrayOutputStream()));
 		
 		// Debug purposes
 		MaxTests.run();
 		
 		debugWindow.setVisible(true);
 		gameState = new GameState();
 		roketGamer = new RoketGamer();
 		debugWindow.printInfo("NavalBattle " + Constants.NAVALBATTLE_VERSION + " initialized");
 		windowHandler = new WindowHandler();
 		localizationManager = new LocalizationManager(NavalBattle.class, "/com/jpii/navalbattle/res/strings");
 		
 		windowHandler.windows.get(windowHandler.windows.size()-1).setVisible(true);
 	}
 	
 	public static void onShutdown() {
 		try {
 		for (int c = 0; c < windowHandler.windows.size(); c++) {
 			Window w = windowHandler.windows.get(c);
 			if (w != null) {
 				if (w instanceof SinglePlayerGame) {
 					SinglePlayerGame spg = (SinglePlayerGame)w;
 					Game g = spg.game.getGame();
 					System.out.println("Calling game shutdown.");
 					g.onShutdown();
 				}
 			}
 		}
		windowHandler.killAll();
 		System.out.println("Game is closing.");
 		try {
 			Thread.currentThread().sleep(250);
 		}
 		catch (Throwable t) {
 			
 		}
 		}
 		catch (Throwable whoho) {
 			
 		}
 	}
 	
 	/**
 	 * Returns current instance of RoketGamer.
 	 * @return roketGamer
 	 */
 	public static RoketGamer getRoketGamer() {
 		return roketGamer;
 	}
 	
 	/**
 	 * Returns current instance of DebugWindow.
 	 * @return debugWindow
 	 */
 	public static DebugWindow getDebugWindow() {
 		return debugWindow;
 	}
 	
 	/**
 	 * Returns current instance of GameState.
 	 * @return gameState
 	 */
 	public static GameState getGameState() {
 		return gameState;
 	}
 	
 	/**
 	 * Returns current instance of WindowHandler. Used to switch Windows.
 	 * @return windowHandler
 	 */
 	public static WindowHandler getWindowHandler() {
 		return windowHandler;
 	}
 	
 	/**
 	 * Returns current instance of LocalizationManager. Used to handle localization.
 	 * @return windowHandler
 	 */
 	public static LocalizationManager getLocalizationManager() {
 		return localizationManager;
 	}
 	
 	/**
 	 * Attempt to set <code>DefaultLookAndFeel</code> to Nimbus and
 	 * alert the users if the process fails.
 	 */
 	private static void setDefaultLookAndFeel(){
 		try {
 		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 		        if ("Nimbus".equals(info.getName())) {
 		            UIManager.setLookAndFeel(info.getClassName());
 		            break;
 		        }
 		    }
 		} catch (UnsupportedLookAndFeelException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, unsuported");	
 		} catch (ClassNotFoundException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, class not found");	
 		} catch (InstantiationException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, instantiation");	
 		} catch (IllegalAccessException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, illegalaccess");	
 		} catch (Exception e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel has encountered an error, " + e.getMessage());	
 		} catch (Error e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel has encountered an error, " + e.getMessage());	
 		} catch (Throwable thr) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel has encountered an error, " + thr.getMessage());
 		}
 	}
 }

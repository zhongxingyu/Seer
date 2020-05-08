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
 
 package com.jpii.navalbattle.data;
 
 import java.util.ArrayList;
 
 import com.jpii.gamekit.GameKit;
 import com.jpii.gamekit.debug.*;
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.game.SinglePlayerGame;
 import com.jpii.navalbattle.gui.*;
 import com.jpii.navalbattle.gui.listeners.WindowCloser;
 import com.jpii.navalbattle.io.NavalBattleIO;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.boost.BoostBuilder;
 
 public class Commands {
 	
 	/**
 	 * Commands loaded on game start
 	 */
 	@SuppressWarnings("serial")
 	public static final ArrayList<Command> COMMANDS = new ArrayList<Command>() {{
 	    
 	    add(new Command("quit", "", "Quit game", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		WindowCloser.close();
 	    	}}
 	    ));
 	    
 	    add(new Command("boost", "", "Launches PavoBoost visual editor", new CommandAction()
 	    {
 	    	public void onRun(Command c, String[] args) {
 	    		BoostBuilder builder = new BoostBuilder();
 	    		builder.setVisible(true);
 	    	}
 	    }));
 	    
 	    add(new Command("version", "", "View version info", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		NavalBattle.getDebugWindow().println(Constants.NAVALBATTLE_VERSION_TITLE + " (" + Constants.VERSION_CODE + ")");
 	    	}}
 	    ));
 	    
 	    add(new Command("credits", "", "NavalBattle credits", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		NavalBattle.getDebugWindow().println("----------------- NavalBattle Credits -----------------");
 	    		NavalBattle.getDebugWindow().println("Anthony \"abauer\" Bauer - game design lead");
 	    		NavalBattle.getDebugWindow().println("Thomas \"TexasGamer\" Gaubert - SCM manager; RoketGamer lead");
 	    		NavalBattle.getDebugWindow().println("Max \"maximusvladimir\" K. - rendering lead, grid lead, Pavo lead");
 	    		NavalBattle.getDebugWindow().println("JR \"DarkWarHero\" Vetus - TBD");
 	    		NavalBattle.getDebugWindow().println("Matt \"Matthis5point0\" Waller - TBD");
 	    		NavalBattle.getDebugWindow().println("Zach \"smeagle42\" Mathewson - Ship Designer");
 	    		NavalBattle.getDebugWindow().println("");
 	    		NavalBattle.getDebugWindow().println("GitHub - source code hosting");
 	    		NavalBattle.getDebugWindow().println("RoketGamer - online social gaming");
 	    	}}
 	    ));
 	    
 	    add(new Command("setscore", "<score>", "Set game score", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		try {
 	    			MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().setscore(Integer.parseInt(args[0]));
 		    		NavalBattle.getDebugWindow().printInfo("Game score set to " + MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().getScore());
 	    		} catch (Exception ex) {
 	    			NavalBattle.getDebugWindow().printError("Missing or invalid arg: score");
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("addscore", "<score>", "Add to game score", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		try {
 	    			MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().addScore(Integer.parseInt(args[0]));
 		    		NavalBattle.getDebugWindow().printInfo("Game score set to " + MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().getScore());
 	    		} catch (Exception ex) {
 	    			NavalBattle.getDebugWindow().printError("Missing or invalid arg: score");
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("removescore", "<score>", "Subtract from game score", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		try {
 	    			MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().subtractscore(Integer.parseInt(args[0]));
 		    		NavalBattle.getDebugWindow().printInfo("Game score set to " + MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().getScore());
 	    		} catch (Exception ex) {
 	    			NavalBattle.getDebugWindow().printError("Missing or invalid arg: score");
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("save", "<gamename>", "Saves the current game.", new CommandAction() {
 	    	public void onRun(Command c, String[] args) {
 	    		args[0] = args[0].toLowerCase();
 	    		NavalBattleIO.saveGame(Game.Instance,args[0]);
 	    	}
 	    }));
 	    
 	    add(new Command("getscore", "", "Get game score", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 		    	NavalBattle.getDebugWindow().printInfo("Game score: " + MainMenuWindow.spg.getGame().getTurnManager().getTurn().getPlayer().getScore());
 	    	}}
 	    ));
 	    
 	    add(new Command("openwindow", "<windowid>", "Force a window to appear", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		args[0] = args[0].toLowerCase();
 	    		if(args[0].equals("login") || args[0].equals("0") || args[0].equals("loginwindow")) {
 	    			new LoginWindow();
 	    		}
 	    		
 	    		if(args[0].equals("main") || args[0].equals("1") || args[0].equals("mainmenu") || args[0].equals("mainmenuwindow")) {
 	    			new MainMenuWindow();
 	    		}
 	    		
 	    		if(args[0].equals("credits") || args[0].equals("2") || args[0].equals("creditswindow")) {
 	    			new CreditsWindow();
 	    		}
 	    		
 	    		if(args[0].equals("help") || args[0].equals("3") || args[0].equals("helpwindow")) {
 	    			new HelpWindow();
 	    		}
 	    		
 	    		if(args[0].equals("game") || args[0].equals("6") || args[0].equals("gamewindow") || args[0].equals("newgame")) {
 	    			new SinglePlayerGame();
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("rginfo", "", "Get RoketGamer info", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		NavalBattle.getDebugWindow().println("RoketGamer " + NavalBattle.getRoketGamer().getVersion());
 	    		NavalBattle.getDebugWindow().println("Server: " + NavalBattle.getRoketGamer().getServerLocation());
 	    		NavalBattle.getDebugWindow().println("Auth status: " + NavalBattle.getRoketGamer().getStatus());
 	    	}}
 	    ));
 	    
 	    add(new Command("gamekitinfo", "", "Get GameKit info", new CommandAction() { 
 	    	public void onRun(Command c, String[] args) {
 	    		NavalBattle.getDebugWindow().println("GameKit " + GameKit.getVersion());
 	    		NavalBattle.getDebugWindow().println("API level: " + GameKit.getApiLevel());
 	    	}}
 	    ));
 	}};
 }

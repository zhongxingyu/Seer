 package com.jpii.navalbattle.data;
 
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.debug.Command;
 import com.jpii.navalbattle.debug.CommandAction;
 
 public class Commands {
 	
 	/**
 	 * Commands loaded on game start
 	 */
 	@SuppressWarnings("serial")
 	public static final ArrayList<Command> COMMANDS = new ArrayList<Command>() {{
 	    add(new Command("help", "", "View all commands", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		NavalBattle.getDebugWindow().println("----------------- NavalBattle Debug Help -----------------");
 	    		for(Command cmd : NavalBattle.getCommandHandler().getCommands()) {
 	    			NavalBattle.getDebugWindow().println(cmd.getCommand() + cmd.getArgs() + " - " + cmd.getDescription());
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("quit", "", "Quit game", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		System.exit(0);
 	    	}}
 	    ));
 	    
 	    add(new Command("version", "", "View version info", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
	    		NavalBattle.getDebugWindow().println(Constants.NAVALBATTLE_VERSION_TITLE + " (" + Constants.NAVALBATTLE_CODENAME + ")");
 	    	}}
 	    ));
 	    
 	    add(new Command("echo", "<message>", "Print specified message", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		String[] s = enteredCommand.split(" ", 2);
 	    		NavalBattle.getDebugWindow().println(s[1]);
 	    	}}
 	    ));
 	    
 	    add(new Command("credits", "", "NavalBattle credits", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		NavalBattle.getDebugWindow().println("----------------- NavalBattle Credits -----------------");
 	    		NavalBattle.getDebugWindow().println("Anthony \"abauer\" Bauer - game design lead");
 	    		NavalBattle.getDebugWindow().println("Thomas \"TexasGamer\" Gaubert - SCM manager; RoketGamer lead");
 	    		NavalBattle.getDebugWindow().println("Max \"maximusvladimir\" Kirkby - TBD");
 	    		NavalBattle.getDebugWindow().println("JR \"DarkWarHero\" Vetus - TBD");
 	    		NavalBattle.getDebugWindow().println("Matt \"Matthis5point0\" Waller - TBD");
	    		NavalBattle.getDebugWindow().println("Zach \"smeagle42\" Mathewson - SCM manager; RoketGamer lead");
 	    		NavalBattle.getDebugWindow().println("");
 	    		NavalBattle.getDebugWindow().println("GitHub - source code hosting");
 	    		NavalBattle.getDebugWindow().println("RoketGamer - online social gaming");
 	    	}}
 	    ));
 	    
 	    add(new Command("setscore", "<score>", "Set game score", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		String[] s = enteredCommand.split(" ", 2);
 	    		try {
 	    			NavalBattle.getGameState().setScore(Integer.parseInt(s[1]));
 		    		NavalBattle.getDebugWindow().printInfo("Game score set to " + NavalBattle.getGameState().getScore());
 	    		} catch (Exception ex) {
 	    			NavalBattle.getDebugWindow().printError("Missing or invalid arg: score");
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("addscore", "<score>", "Add to game score", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		String[] s = enteredCommand.split(" ", 2);
 	    		try {
 	    			NavalBattle.getGameState().addScore(Integer.parseInt(s[1]));
 		    		NavalBattle.getDebugWindow().printInfo("Game score set to " + NavalBattle.getGameState().getScore());
 	    		} catch (Exception ex) {
 	    			NavalBattle.getDebugWindow().printError("Missing or invalid arg: score");
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("removescore", "<score>", "Subtract from game score", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		String[] s = enteredCommand.split(" ", 2);
 	    		try {
 	    			NavalBattle.getGameState().subtractScore(Integer.parseInt(s[1]));
 		    		NavalBattle.getDebugWindow().printInfo("Game score set to " + NavalBattle.getGameState().getScore());
 	    		} catch (Exception ex) {
 	    			NavalBattle.getDebugWindow().printError("Missing or invalid arg: score");
 	    		}
 	    	}}
 	    ));
 	    
 	    add(new Command("getscore", "", "Get game score", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 		    	NavalBattle.getDebugWindow().printInfo("Game score: " + NavalBattle.getGameState().getScore());
 	    	}}
 	    ));
 	    
 	    add(new Command("resetscore", "", "Set game score to 0", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    		NavalBattle.getGameState().resetScore();
 		    	NavalBattle.getDebugWindow().printInfo("Game score reset");
 	    	}}
 	    ));
 	    
 	    add(new Command("clear", "", "Clear debug window", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    			NavalBattle.getDebugWindow().printNew("");
 	    	}}
 	    
 	    ));
 	    
 	    add(new Command("cls", "", "Clear debug window", new CommandAction() { 
 	    	public void onRun(Command c, String enteredCommand) {
 	    			NavalBattle.getDebugWindow().printNew("");
 	    	}}
 	    
 	    ));
 	}};
 }

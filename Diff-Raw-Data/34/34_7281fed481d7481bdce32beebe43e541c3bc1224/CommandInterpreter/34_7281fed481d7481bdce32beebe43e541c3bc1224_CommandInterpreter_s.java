 package courseProject.controller;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.swing.JOptionPane;
 
 
 import courseProject.model.Game;
 import courseProject.model.ModelListener;
 import courseProject.model.Player;
 import courseProject.view.View;
 import courseProject.view.mapD.ViewMapD;
 import courseProject.view.textD.ViewText;
 import courseProject.view.twoD.View2D;
 
 /**
  * This class is part of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  * 
  * This parser reads user input and tries to interpret it as an "Adventure"
  * command. Every time it is called it reads a line from the terminal and
  * tries to interpret the line as a two word command. It returns the command
  * as an object of class Command.
  *
  * The parser has a set of known command words. It checks user input against
  * the known commands, and if the input is not one of the known commands, it
  * returns a command object that is marked as an unknown command.
  * 
  * @author Michael Hamon
  * @author Matthew Smith
  * @author Denis Dionne
  * @version 04/11/2012
  */
 public class CommandInterpreter implements InputListener//, Serializable
 {
     private Game game;
     private View view;
     private boolean finished;
     private double previousTime;
     public static final String fileName= "res\\game\\indirectTest.xml";
     
     /**
      * Create a parser to read from the terminal window.
      * @param view what version of the view using
      * @param game the game you are playing
      */
     public CommandInterpreter(View view, Game game) {
         this.view = view;
         this.game = game;
         view.addInputListener(this);
         previousTime = System.nanoTime();
     }
     
     /**
      * receives an input from the view and acts accordingly
      * @param e is the event coming from the view (whether it be a textview, 2Dview or 3Dview)
      */
     public void input(InputEvent e){
     	if(e.getClass().equals(InputEvent2D.class)){
     		if(e.getCommand() == null){
     			((View2D) view).moveCharacter((InputEvent2D)e);
     			game.addUndo();
     			return;
     		}
     	}
     	finished = game.processCommand(e.getCommand());
     	
     }
 
     /**
      * static method returns all the possible commands the parser can handle.
      * @return a string containing each of the possible commands
      */
     public static String getPossibleCommands(){
         return CommandWord.getPossibleCommands();
     }
     
     
     /**
      * The start of the program
      * has user select the type of view (text/2d/3d)
      * @param args
      */
     public static void main(String[] args){
     	int load;
     	Object[] options = {"Text-Based",
                 "Visual Text-Based",
                 "2D Game", 
                 "Game Editor"};
     	
     	Object viewOption = JOptionPane.showInputDialog(null,
 				"Welcome to the World of Nameless\nWhich version of the game would you like to play? ",
 				"Game Mode Selection",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				options,
 				options[2]);
     	
     	load = JOptionPane.showConfirmDialog(null, "Do you want to load the previous game?", "load", JOptionPane.YES_NO_OPTION);
     	System.out.println(load);
     	if(viewOption!=null) {
         	View view ;
 	    	
 	    	if(viewOption.equals(options[0])) { // Text-Based Option
 	    		view =  new ViewText();
 	    	}
 	    	else if (viewOption.equals(options[1])) { // 2D option
 	    		view = new ViewMapD();
 	    	}
 	    	else if (viewOption.equals(options[2])) {
 	    		view = new View2D();
 	    	}
 	    	else {
	    		//startLevelEditor();
 	    		
 	    		return;
 	    	}
	    	
 	    	Game game = null;
 	    	if(load != 0){
	    		game = new Game();
 	    	}
 	    	else{
 	    		try {
 					game = Game.load();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	    	}
 	    	
	    	LevelLoader loader=new LevelLoader();
 	    	
	    	Game game=new Game();
			try {
				Player player=loader.LoadLevel(fileName);
				game.setPlayer(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
 			
 			
 			
 	    	
 	    	CommandInterpreter c = new CommandInterpreter(view, game);
 	
 	    	game.addModelListeners((ModelListener)view);
 	    	c.play();
 	    	view.dispose();
     	}
     }
 
     
     /**
      * this is the loop used to play the game
      */
     private void play(){
     	game.printWelcome();
     	finished = false;
         while (! finished) {
         	final double delta = System.nanoTime()-previousTime;
     	    view.update(delta);
         	previousTime = System.nanoTime();
             if(game.getPlayer().isDead()){
             	//JOptionPane.showMessageDialog(null, "You died,\nThanks for playing", "You Died", JOptionPane.WARNING_MESSAGE);
             	view.dispose();
             	break;
             }
             try {
 				Thread.sleep(1);
 			} catch (InterruptedException e) {
 			}
         }
     }
    /*
     private static void startLevelEditor() {
     	GameEditor editor = new GameEditor();
     	
     	editor.show();
    
     private void writeObject(java.io.ObjectOutputStream out) throws IOException{
 		out.defaultWriteObject();
 		 
 	}
 	
 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		
 	}
 }

 package maze_objects;
 
 import game.logic.Game;
 import game.ui.ExitEvent;
 
 public class Hero extends Movable implements java.io.Serializable {
 
 	private static final long serialVersionUID = -5953402713235051361L;

 	//Hero states
 	public static final int ARMED = 1;
 	public static final int DEAD = 2;
 	public static final int EXITED_MAZE = 3;
 	public static final int IN_GAME = 0;
 
 	/*** Private Variable Area ***/
 
 	private void exitMaze(int r, int c) { //Moves the hero to the exit position and toggles his state to exited_maze
 		row = r;
 		column = c;
 		state = EXITED_MAZE;
 	}
 
 	private void makeMove(int r, int c) { //Puts the hero symbol in the correct position after taking the move
 		row = r;
 		column = c;
 	}
 
 	/*** Public methods ***/
 
 	public void armHero(int r, int c) { //Changes hero symbol to the armed hero symbol as he takes the sword
 		state = ARMED;
 		row = r;
 		column = c;
 	}

 	//Constructors
 	public Hero() {
 		row = 1;
 		column = 1;
 		state = IN_GAME;
 	}
 
 	public Hero(int r, int c) { //Create the hero with a predefined row and column
 		row = r;
 		column = c;
 		state = IN_GAME;
 	}
 
 	//Game methods
 
 	public boolean moveHero(int rowMovement, int columnMovement, Game g) { //Possibly moves the hero to a new position, analyzing if it's able to and possible outcoming events
 		Maze m = g.getMaze();
 
 		int newRow = row + rowMovement;
 		int newColumn = column + columnMovement;
 
 		if(m.checkIfEmpty(newRow, newColumn) && !g.checkIfSword(newRow, newColumn) && !g.checkIfOnAliveDragon(newRow, newColumn)) {
 			makeMove(newRow, newColumn);
 		}
 		else if(m.checkIfExit(newRow, newColumn)) {
 			if(state == ARMED) {
 				if(g.getExitState() == Game.EXIT_OPEN) {
 					exitMaze(newRow, newColumn);
 					return false;
 				}
 				else {
 					ExitEvent ee = new ExitEvent(1);
 					g.addEvent(ee);
 				}
 			}
 			else {
 				ExitEvent ee = new ExitEvent(0);
 				g.addEvent(ee);
 			}
 		}
		else if(g.checkIfSword(newRow, newColumn) && !g.getSword().isTaken() && !g.getEagle().isOnGroundWithSword()) {
 			armHero(newRow, newColumn);
 			g.getSword().takeSword();
 		}
 
 		return true;
 	}
 
 }

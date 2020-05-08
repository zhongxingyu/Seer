 package edu.berkeley.gamesman.core;
 
 
 /**
  * A Solver is responsible for solving a Game and storing the result to a Database
  * @author Steven Schlansker
  */
 public abstract class Solver {
 
 	protected Database db;
 	
 	/**
 	 * Set the Database to use for this solver
	 * @param db1 the Database
 	 */
	public void initialize(Database db1){
		this.db = db1;
 	}
 	
 	/**
 	 * Prepare to solve a game and encapsulate all relevant information into a WorkUnit for later execution by a Master
 	 * @param config The configuration used for this solve
 	 * @param game The Game to solve
 	 * @return a WorkUnit that has enough information to solve the game in its entirety.
 	 */
 	public abstract WorkUnit prepareSolve(Configuration config,Game<?> game);
 	
 }

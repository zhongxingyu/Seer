 package edu.brown.cs32.MFTG.tournament;
 
 public class BackendConstants {
 	public static final int MAX_NUM_PLAYERS=4; //games can handle up to this many players
 	public static final double CONFIRMATION_PERCENTAGE = .1; //this % of games will be checked for consistency
 	public static final int NUM_THREADS=10; //how many threads in which to run the games
 	public static final int DATA_PACKET_SIZE=1000; //when the client is running games, update GUI after this many games
 	public static final int NUM_DATA_POINTS=100; //combine game data into this many time stamps
 	public static final int MAX_NUM_TURNS=1000; //cut off a game after this many turns
	public static int DEFAULT_NUM_SETS=5; //the GUI starts out showing this many sets
 	public static int DEFAULT_GAMES_PER_SET=1000; //the GUI starts out showing this many games
	public static int DEFAULT_TIME_BEGIN=60; //the GUI starts out showing this much time to choose heuristics the first time
	public static int DEFAULT_TIME_BETWEEN=60; //the GUI starts out showing this much time to choose heuristics subsequent times
 	public static int DEFAULT_FREE_PARKING=-1; //the GUI starts out showing this as the parking option
 	public static int DEFAULT_PORT=3232; //the GUI starts out showing this as the port
 	public static final String DEFAULT_HOST="localhost"; //the GUI starts out showing this as the host
 }

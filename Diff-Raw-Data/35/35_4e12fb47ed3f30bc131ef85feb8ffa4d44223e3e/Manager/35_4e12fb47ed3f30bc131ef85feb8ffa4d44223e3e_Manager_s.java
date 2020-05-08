 package quoridor;
 
 import java.util.Scanner;
 
 /**
  * 
  * This is the class that controls the creation of new games, as well
  * as saving and loading game states. 
  * 
  * @author steve
  *
  */
 public class Manager {
 	
 	//Some constants to make the code easier to read
 	
 	static final String WELCOME_MSG = 
 		"Welcome to Quoridor!\n";
 	static final String PROMPT = "> ";
 	
 	static final short INVALID = -1;
 	static final short EXIT = 0;
 	static final short NEW_GAME = 1;
 	static final short HELP = 2;
 	
 	//this will store the current game being played
 	static Game currentGame = null;
 	
 	//the input parser that parses user's commands
 	static Parser inputParser;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//print a welcome method
 		welcome();
 		
 		/*
 		 * This is the information required by the parser to create
 		 * a syntax table and know how many arguments each command
 		 * has.
 		 */
		String [] commands = {"exit", "newgame", "help"};
		short [] tokens = {EXIT, NEW_GAME, HELP};
		short [] argCount = {0, 2, 0};
 		
 		//initialise the input parser
 		inputParser = new ParserImpl(commands, 
 								     tokens, 
 								     argCount, 
 								     INVALID);
 		//start up the input loop
 		inputLoop(inputParser);
 	}
 	
 	/**
 	 * Prints the welcome message
 	 */
 	public static void welcome() {
 		System.out.println(WELCOME_MSG);
 	}
 	
 	/**
 	 * Loops parsing input until the user invokes the exit command.
 	 * @param inputParser
 	 * 			the input parser that parses users' input
 	 */
 	public static void inputLoop(Parser inputParser) {
 		boolean exit = false; //flag that tells the loop to terminate
 		short token; //holds the token of the command just read
 		
 		while (!exit) {
 			System.out.print(PROMPT); //print the prompt
 			
 			//read the next token
 			token = inputParser.ensureCommand();
 			
 			if (token == EXIT) {
 				exit = true;
 			} else if (token == NEW_GAME) {
 				
 				Pair <Player> players;
 				
 				/*
 				 * If the user gave arguments, we can create the
 				 * players without prompting for more input
 				 */
 				if (inputParser.hasRequiredArgs()) {
 					Player _1 = new PlayerImpl(inputParser.nextArg());
 					Player _2 = new PlayerImpl(inputParser.nextArg());
 					
 					players = new PairImpl<Player>(_1, _2);
 					
 				} else {
 					
 					/*
 					 * Give the user an error if they tried to give
 					 * arguments but gave the wrong amount.
 					 */
 					if (inputParser.hasErroneousArgs()) {
 						System.out.println(
 						"Warning: Incorrect number of arguments");
 					}
 					
 					//prompt for the user to enter player names
 					players = enterPlayers();
 					
 				}
 				
 				//initialise a game
 				currentGame = new GameImpl(players);
 				
 				//tell the user the game is starting
 				System.out.println("Starting game: "
 						.concat(players._1().getName())
 						.concat(" vs ")
 						.concat(players._2().getName()));
 				
 				//play the game
 				currentGame.play();
 				
 			} else if (token == HELP) {
 				//this prints a list of valid commands
 				System.out.print(inputParser);
 			}
 			
 		}
 		
 		
 	}
 	
 	/**
 	 * Prompts the user to enter player names, then creates and 
 	 * returns a pair of players with those names.
 	 * @return A pair of players with names specified by the user
 	 */
 	public static Pair <Player> enterPlayers() {
 		
 		Player p1;
 		Player p2;
 		
 		Scanner s = new Scanner(System.in);
 		
 		//read player 1's name
 		System.out.print("Player 1 enter your name: ");
 		p1 = new PlayerImpl(s.nextLine());
 		
 		//read player 2's name
 		System.out.print("Player 2 enter your name: ");
 		p2 = new PlayerImpl(s.nextLine());		
 		
 		//create and return a pair containing those players
 		return new PairImpl <Player> (p1, p2);
 	}
 	
 
 }

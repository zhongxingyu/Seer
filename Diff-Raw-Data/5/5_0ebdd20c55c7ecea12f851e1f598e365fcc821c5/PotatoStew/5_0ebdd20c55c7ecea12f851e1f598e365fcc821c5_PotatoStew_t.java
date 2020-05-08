 package lettergame.g1;
 
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Random;
 
 import lettergame.ui.CSVReader;
 import lettergame.ui.Letter;
 import lettergame.ui.LetterGame;
 import lettergame.ui.LetterGameValues;
 import lettergame.ui.Player;
 import lettergame.ui.PlayerBids;
 import lettergame.ui.SecretState;
 import lettergame.ui.Word;
 
 
 /*
  * This player bids randomly within a certain range
  * based on the letter's value.
  * 
  * Keep in mind that the Player superclass has the following fields: 
 	Logger logger
 	ArrayList<Character> currentLetters
 	int myID
 	ArrayList<Word> wordlist
  */
 
 public class PotatoStew extends Player {
 
 	public PotatoStew() {
 		super();
 	}
 	
 	// for generating random numbers
 	private Random random = new Random();
 	private ArrayList<Integer> sevenLetterWords;
 	private boolean learnedStrat, threeStrat;
 
 	private int[][] bidstrategy;
 	private int[] bagOfLetters;
 
 	public final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	
     /*
      * This is called once at the beginning of a Game.
      * The id is what the game considers to be your unique identifier
      * The number_of_rounds is the total number of rounds to be played in this game
      * The number_of_players is, well, the number of players (including you!).
      */
 	public void newGame(int id, int number_of_rounds, int number_of_players) {
 		myID = id;
 		sevenLetterWords = new ArrayList<Integer>();
 		for( int i = 0; i < wordlist.size(); i++ )
 		{
 			Word w = wordlist.get(i);
 			if( w.getLength() >= 7 )
 			{
 				sevenLetterWords.add( i );
 			}
 		}
 		bidstrategy = new int[7][26];
 		try
 		{
             CSVReader csvreader = new CSVReader(new FileReader("strategy.txt"), ';');
             String[] nextLine;
             int hand = 0;
             while((nextLine = csvreader.readNext()) != null)
             {
             	if (nextLine.length > 1)  
             	{
             		for( int i = 0; i < 26; i ++ )
             		{
             			String[] bids = nextLine[i].split(",");
             			for( String bid : bids )
             			{
             				int b = Integer.valueOf( bid );
             				if( b > bidstrategy[hand][i] )
             					bidstrategy[hand][i] = b;
             			}
             		}
            		}
             	hand++;
             }
 
         }
         catch(Exception e)
         {
             e.printStackTrace();
             logger.error("\n Could not load strategy!");
         }
 	}
 
 
 	/*
 	 * This method is called at the beginning of a new round.
 	 * The secretState contains your current score and the letters that were secretly given to you in this round
 	 * The current_round indicates the current round number (0-based)
 	 */
 	public void newRound(SecretState secretState, int current_round) {
 		
 		double decider = random.nextDouble();
 		
 		// decides whether to use which strategy
 		if(decider > 0.5){
 			learnedStrat = true;
 			threeStrat = false;
 		}else{
 			learnedStrat = false;
 			threeStrat = true;
 		}
 		
 		// be sure to reinitialize the list at the start of the round
 		currentLetters = new ArrayList<Character>();
 		bagOfLetters = new int[LETTERS.length()];
 		for (int i = 0; i < LETTERS.length(); i++) {
 			bagOfLetters[i] = LetterGameValues.getLetterFrequency(LETTERS.charAt(i));
 		}
 		
 		// be sure to reinitialize the list at the start of the round
 		currentLetters = new ArrayList<Character>();
 
 		// add any letters from the secret state
 		for (Letter l : secretState.getSecretLetters()) {
 			// logger.trace("myID = " + myID + " and I'm adding " + l +
 			// " from the secret state");
 			currentLetters.add(l.getCharacter());
 			bagOfLetters[Integer.valueOf(l.getCharacter())
 					- Integer.valueOf('A')]--;
 		}
 	}
 	
 
 	public boolean stillAvailable( Word hand, int widx, int[] bag )
 	{
 		if( !wordlist.get(widx).contains(hand) )
 		{
 			return false;
 		}
 		for( int i = 0; i < 26; i++ )
 		{
 			if( hand.countKeep[i] + bag[i] < wordlist.get( widx ).countKeep[i] )
 				return false;
 		}
 		return true;
 	}
 	
 	/*
 	 * This method is called when there is a new letter available for bidding.
 	 * bidLetter = the Letter that is being bid on
 	 * playerBidList = the list of all previous bids from all players
 	 * playerList = the class names of the different players
 	 * secretState = your secret state (which includes the score)
 	 */
 	public int getBid(Letter bidLetter, ArrayList<PlayerBids> playerBidList, ArrayList<String> playerList, SecretState secretState) {
 		
		// bids 3 if we have 7 or more letters
		if(currentLetters.size() > 7){
			return 3;
		}// end default scraping
		
 		// checks to see which strategy to use for bidding.
 		if(learnedStrat){
 			char c[] = new char[currentLetters.size() + 1];
 			for (int i = 0; i < currentLetters.size(); i++) {
 				c[i + 1] = currentLetters.get(i);
 			}
 			c[0] = bidLetter.getCharacter();
 	
 			String s = new String(c);
 			Word hand = new Word( s );
 			
 			bagOfLetters[Integer.valueOf(c[0]) - Integer.valueOf('A')]--;
 			
 			boolean valid = false;
 			
 			for( int i : sevenLetterWords )
 			{
 				if( stillAvailable( hand, i, bagOfLetters ) )
 				{
 					valid = true;
 				}
 			}
 			
 			return valid ? bidstrategy[Math.min(6,currentLetters.size())][Integer.valueOf(c[0]) - Integer.valueOf('A')] : 0;
 		
 		}else{
 			
 			return 3;
 			
 		}// end strategy decision
 		
 	}// end getBid method
 
 	
 	/*
 	 * This method is called after a bid. It indicates whether or not the player
 	 * won the bid and what letter was being bid on, and also includes all the
 	 * other players' bids. 
 	 */
     public void bidResult(boolean won, Letter letter, PlayerBids bids) {
     	if (won) {
     		//logger.trace("My ID is " + myID + " and I won the bid for " + letter);
     		currentLetters.add(letter.getCharacter());
     	}
     	else {
     		//logger.trace("My ID is " + myID + " and I lost the bid for " + letter);
     	}
     }
 
     /*
      * This method is called after all the letters have been purchased in the round.
      * The word that you return will be scored for this round.
      */
 	public String getWord() {
 		char c[] = new char[currentLetters.size()];
 		for (int i = 0; i < c.length; i++) {
 			c[i] = currentLetters.get(i);
 		}
 		String s = new String(c);
 		//logger.trace("Player " + myID + " letters are " + s);
 		Word ourletters = new Word(s);
 		Word bestword = new Word("");
 
 		// iterate through all Words in the list
 		// and see which ones we can form
 		for (Word w : wordlist) {
 			if (ourletters.contains(w)) {
 				int score = w.getScore();
 				// don't forget the bonus!
 				if (w.getLength() >= 7) score += 50;
 				if (score > bestword.getScore()) {
 					bestword = w;
 				}
 
 			}
 		}
 		logger.trace("My ID is " + myID + " and my word is " + bestword.getWord());
 		
 		return bestword.getWord();
 	}
 
 	/*
 	 * This method is called at the end of the round
 	 * The ArrayList contains the scores of all the players, ordered by their ID
 	 */
 	public void updateScores(ArrayList<Integer> scores) {
 		
 	}
 
 
 
 
 }

 import java.util.List;
 import java.util.Iterator;
 
 /**
  * 
  * Allows other objects to take actions on the game logic.
  * 
  * When game is created, it will create a new instance of manger and retain the reference.
  * 
  * A team may have only one player or it may have many players. The game will know which 
  * team is up to bat. The manager should in-turn know which player is up to bat.
  * 
  * Constructor:
  *  Accepts the word length and the max attempts.
  * 
  * Private data summary:
  *  A list of teams (Manager classes)
  *  Guess list
  *  Total Wrong Guesses
  *  Game Word
  *  active team reference
  *  
  * 
  * @author Josh Gillham
  * @version 9-23-12
  */
 public class Logic{
     /** The default guesses. */
     static public final int DEFAULT_ATTEMPTS= 5;
     /** The least guesses allowed that make the game interesting. */
     static public final int MIN_ATTEMPTS= 2;
     
     /** The word every player in the game will try and guess. */
     private String gameWord;
     /** Holds the teams in the game. */
     private List< Manager > gameTeams;
     /** Holds the guess made. */
     private StringBuilder guesses= new StringBuilder();
     /** Holds a string the same length of gameWord but with dashes in places where no player has guessed. */
     private StringBuilder statusWord= new StringBuilder();
     /** Refers to the team with the turn to guess. */
     private int activeTeam= 0;
     /** Holds the call back for events during the game. Allows this class to send messages to the UI. */
     private GameEvent eventHandler= null;
     /** Holds the maximum guess allowed during the game. */
     private int maxGuesses= DEFAULT_ATTEMPTS;
     /** True if the player still needs to take a turn. */
     private boolean playerInTurn= true;
     
     /** Used to signal the game state. */
     static public enum Statis {
         STARTED,
         OVER,
         WINNER
     }
     /** Holds the game statis. */
     Statis gameState= Statis.STARTED;
     
     /**
      * Checks to make sure teams has no teams with empty rosters. Makes sure there is at least one player
      *  and one team. Makes sure the word length is allowed in the dictionary.
      *  
      * @arg teams the teams for the game.
      * @arg gameWordLength the length of the game word.
      * 
      * @throws NoSuchElementException when teams is empty or any Manager.getRosterSize() returns 0.
      * @throws IllegalArgumentException when gameWordLength is less than Dictionary.MIN_WORDLENGTH
      *  and greater than Dictionary.MAX_WORDLENGTH.
      */
     public Logic( java.util.List< Manager > teams, int gameWordLength ) throws IllegalArgumentException {
         this( teams, Dictionary.getWord( gameWordLength ) );
     }
     
     /**
      * Checks to make sure teams has no teams with empty rosters. Makes sure there is at least one player
      *  and one team. The gameWord must not be empty or null.
      *  
      * @arg teams the teams for the game.
      * @arg gameWordLength the length of the game word.
      * 
      * @throws NoSuchElementException when teams is empty or any Manager.getRosterSize() returns 0.
      * @throws IllegalArgumentException when gameWord is empty.
      * @throws NullPointerException when gameWord is null.
      */
     public Logic( java.util.List< Manager > teams, String gameWord ) throws IllegalArgumentException {
         if( teams.size() == 0 )
             throw new java.util.NoSuchElementException();
         if( gameWord == null )
             throw new NullPointerException();
         if( gameWord.isEmpty() )
             throw new IllegalArgumentException();
         // Make sure all teams have at least one player.
         for( Manager team: teams ) {
             if( team.getRosterSize() == 0 )
                 throw new java.util.NoSuchElementException();
         }
         
         gameTeams= teams;
         this.gameWord= gameWord;
         for( int i= 0; i < gameWord.length(); ++i )
             statusWord.append( '-' );
     }
     
     /**
      * Submits a guess. If the guess is not a letter an error is thrown. If
      *  the letter has been guessed before an error is thrown. Else, the 
      *  guess is noted and the game word is searched for the letter. For
      *  ever letter that was found, the game updates the status word 
      *  replacing the mask with the letter. If any letter is found the
      *  function returns true and eventHandler.changedStatusWord is called,
      *  otherwise the function returns false.
      * 
      * Postconditions:
      *  List of guesses grows.
      *  Status word is updated for each letter found.
      *  eventHandler.changedStatusWord is called if any letter is found.
      * 
      * @arg letter is the letter to guess
      * 
      * @return True if the guess is found in the word
      * 
      * @throws IllegalArgumentException when letter is not a letter i.e. '?' or '9'
      * @throws IllegalArgumentException when the letter is already guessed.
      */
     public boolean makeGuess( char letter ){
         if( !Character.isLetter( letter ) )
             throw new IllegalArgumentException( "Guesses should be letters only." );
         letter= Character.toLowerCase( letter );
         // Make sure this guess has never been guessed before.
         for( int i= 0; i < this.guesses.length(); ++i ) {
             char guess= this.guesses.charAt( i );
             if( guess == letter )
                 throw new IllegalArgumentException( "That letter has alread been guessed." );
         }
         // Add this guess to the list.
         this.guesses.append( letter );
         this.playerInTurn= false;
         boolean found= false;
         // Try to find the letter in the game word and update the status word as we go.
         for( int i= 0; i < this.gameWord.length(); ++i ) {
             char wordLetter= this.gameWord.charAt( i );
             if( letter == Character.toLowerCase( wordLetter ) ) {
                 found= true;
                 // Replace the dash with the letter
                 this.statusWord.setCharAt( i, letter );
             }
         }
         // Show the UI the new found letters.
         if( this.eventHandler != null && found )
             this.eventHandler.changedStatusWord( statusWord.toString() );
         return found;
     }
     
     /**
      * Removes a team from the game. The team will automatically lose. If there is no teams
      *  left in the game, there is no winner. If there is one team left, that team is the
      *  winner. If there are two or more, the game resumes.
      * 
      * Preconditions:
      *  manager's team must be in the game.
      *  
      * Postconditions:
      *  The manager's team is no longer in the game.
      * 
      * @arg manager the manager of the team to remove.
      * 
      * @throws NoSuchElementException when the manager's team is not in the game.
      * @throws NullPointerException when the manager is null
      */
     //public void resignTeam( Manager manager ) throws java.util.NoSuchElementException, java.lang.NullPointerException;
     
     /**
      * Can be called as much as the caller would like. Should be called to rotate the turns and
      *  prompt the UI to receive the next guess. If the word is completely guessed, the game is
      *  over and one team is the winner. If there are no more guesses remaining, the game
      *  is over and there is no winner. If the current player has not maid a guess, the call is
      *  ignored. Else, the players and teams are rotated and the game signals the UI that the
      *  next player can guess.
      *  
      * Postconditions:
      *  The current team's players are rotated.
      *  The teams are rotated.
      *  The play up on the active team is in-turn.
      *  
      */
     public void rotateTurn()  {
         // Check to see if all the letters are guessed.
         if( this.statusWord.indexOf( "-" ) < 0 ){
             this.gameState= Statis.WINNER;
             if( this.eventHandler != null && this.gameTeams.size() == 1 )
                 this.eventHandler.gameWinner( this.gameTeams.get( 0 ) );
             
             return;
         }
         // Check to see if there no guesses remaining
         if( this.guesses.length() >= this.maxGuesses ){
             this.gameState= Statis.OVER;
             if( this.eventHandler != null )
                 this.eventHandler.gameOver( this.gameWord );
             return;
         }
         // Player still needs to make a guess.
        if( this.playerInTurn ) {
            if( this.eventHandler != null )
                this.eventHandler.playerUp( this.gameTeams.get( this.activeTeam ).getPlayerUp() );
             return;
        }
         // Now its the next player's turn.
         this.playerInTurn= true;
         // Rotate players
         this.gameTeams.get( activeTeam ).nextPlayer();
         // Rotate the teams
         if( this.activeTeam == this.gameTeams.size() - 1 )
             this.activeTeam= 0;
         else
             ++this.activeTeam;
         // Notify UI
         if( this.eventHandler != null )
             this.eventHandler.playerUp( this.gameTeams.get( this.activeTeam ).getPlayerUp() );
     }
     
     /**
      * Accesses the number of teams in the game. Returns 0 when Game is first created.
      * 
      * @return the number of teams
      */
     //public int getNumberOfTeams();
     
     /**
      * Sets the GameEvents handler. Used to pass messages back to the UI. Note a
      *  null handler will unset the event handler.
      *  
      * @arg handler the new event handler
      */
     public void setGameEventsHandler( GameEvent handler ) {
         this.eventHandler= handler;
         // Show the initial state of the status word.
         this.eventHandler.changedStatusWord( statusWord.toString() );
         // First player to guess.
         this.eventHandler.playerUp( gameTeams.get( activeTeam ).getPlayerUp()  );
     }
     
     /**
      * Gets the guesses remaining.
      * 
      * @return the number of attempts remaining.
      */
     public int getAttempts() {
         return this.guesses.length();
     }
     
     /**
      * Accesses the game state which tells whether the game is in progress, over ect.
      * 
      * @return the game statis.
      */
     public Statis getGameState() {
         return gameState;
     }
     
     /**
      * Sets the max guesses.
      * 
      * @return the number of attempts remaining.
      */
     public void setMaxAttempts( int nMaxGuesses ) {
         this.maxGuesses= nMaxGuesses;
     }
 }

 
 
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * The test class testFormGamePlay.
  *
  * @author  (your name)
  * @version (a version number or a date)
  */
 public class testGameEvent
 {
     /**
      * Default constructor for test class testGame
      */
     public testGameEvent()
     {
     }
 
     /**
      * Sets up the test fixture.
      *
      * Called before every test case method.
      */
     @Before
     public void setUp()
     {
     }
 
     /**
      * Tears down the test fixture.
      *
      * Called after every test case method.
      */
     @After
     public void tearDown()
     {
     }
     
     public String savedGameWord= "flamingo";
     
     
     class MockLogic extends Logic {
         public MockLogic( java.util.List< Manager > teams, int gameWordLength, String word ) 
          throws IllegalArgumentException {
              super( teams, word );
         }
         public int getAttempts() { return DEFAULT_ATTEMPTS; }
     }
     
     class MockGameUI extends GameUI {
         MockLogic logic;
         List< Manager > teams;
         Player player;
         Manager man;
     
         Player playerUp= null;
         Manager teamUp= null;
         String savedGameWord= null;        
         Manager gameWinningTeam= null;        
         boolean gameOver= false;        
         int statusWordChanges= 0;
         
         public MockGameUI( String word ) {
             teams= new LinkedList< Manager >();
             man= new Manager( "Alpha" );
             teams.add( man );
             player= man.addPlayer( "Bob" );
             
             logic= new MockLogic( teams, Dictionary.MIN_WORDLENGTH, word );
             // Set events should shoot off playerUp and teamUp events as soon as it is set
             logic.setGameEventsHandler( this );
         }
         
         public void teamUp( Manager team ){
             teamUp= team;
         }
         public void playerUp( Player player ){
             playerUp= player;
         }
         public void gameWinner( Manager team ){
             gameWinningTeam= team;
         }
         public void gameOver() {
             gameOver= true;
         }
         public void changedStatusWord( String statusWord ) {
             ++statusWordChanges;
         }
     }
     
     
     
     
     
     /**
      * Test the constructor to make sure it can successfully construct
      */
     @Before
     public void testConstructor() {
         
         MockGameUI game= new MockGameUI( savedGameWord );
         
         assertEquals( game.man, game.teamUp );
         assertEquals( game.player, game.playerUp );
     }
     
     /**
      * Test loosing the game.
      */
     public void testGameOver(){
         MockGameUI game= new MockGameUI( savedGameWord );
         String partialABCs= "abcdefghijklmnopqr";
         for( int i= 0; i < partialABCs.length() && i < game.logic.getAttempts(); ++i ) {
             char c= partialABCs.charAt( i );
             game.logic.makeGuess( game.player, c );
         }
         assertTrue( game.gameOver );
         assertNull( game.gameWinningTeam );
     }
     
     /**
      * Test winning the game.
      */
     public void testGameWinner(){
         MockGameUI game= new MockGameUI( savedGameWord );
         for( int i= 0; i < savedGameWord.length(); ++i ) {
             char c= savedGameWord.charAt( i );
             game.logic.makeGuess( game.player, c );
         }
         assertNotNull( game.gameWinningTeam );
         assertEquals( game.man, game.gameWinningTeam );
     }
     
     /**
      * Test updates to the status word
      */
     public void testChangedStatusWord(){
         MockGameUI game= new MockGameUI( savedGameWord );
         for( int i= 0; i < savedGameWord.length(); ++i ) {
             char c= savedGameWord.charAt( i );
             game.logic.makeGuess( game.player, c );
         }
        assertEquals( savedGameWord.length(), game.statusWordChanges );
     }
     
     /**
      * Test updates to the status word with bad guesses.
      */
     public void testChangedStatusWord_wBadGuesses(){
         MockGameUI game= new MockGameUI( savedGameWord );
        StringBuilder wrd= new StringBuilder( game.savedGameWord );
         // Insert crazy letters in between to simulate bad guesses
         wrd.insert( 1, 'u' );
         wrd.insert( 3, 'z' );
         wrd.insert( 5, 'y' );
         for( int i= 0; i < savedGameWord.length(); ++i ) {
             char c= savedGameWord.charAt( i );
             game.logic.makeGuess( game.player, c );
         }
         // The status word changes should be the same
         // Remember it only changes for good guesses
        assertEquals( savedGameWord.length(), game.statusWordChanges );
     }
     
     
     
     /**
      * Tests getActiveTeam();
      * 
      */
     /* @Test
     public void testGetActiveTeam() {
         Logic g= new Logic( Logic.MIN_ATTEMPTS, Dictionary.MAX_WORDLENGTH );
         ManagerInterface firstTeam= null;
         for( int i= 0; i < 10; ++i ){
             if( i == 0 )
                 firstTeam= g.addTeam();
             else
                 g.addTeam();
             assertEquals( firstTeam, g.getActiveTeam() );
         }
     }*/
 }

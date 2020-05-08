 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * This test unit will check the integrity of the Logic class.
  * 
  * TODO:
  *  testGetStatusWord_InGame()
  *  testMakeGuess_InGame()
  *  testGetAttempts()
  *
  * @author  Josh Gillham
  * @version 9-23-12
  */
 public class testLogic
 {
     /**
      * Default constructor for test class testGame
      */
     public testLogic()
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
     
     /** Keeps the Test_InstrumentLogic class for other tests. */
     Test_InstrumentLogic wrapLogic;
     /** Keeps a Logic instance for other tests. */
     Logic game;
     
     
     /**
      * Test the constructor to make sure it can successfully construct
      */
     @Before
     public void testConstructor() {
         wrapLogic= new Test_InstrumentLogic();
         wrapLogic.addManager( "Alpha" );
         wrapLogic.addPlayer( "bob" );
         game= wrapLogic.getInstance();
     }
     
     @Test( expected= NullPointerException.class )
     public void testConstructor_Null(){
         Logic g= new Logic( null, Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when there are no teams.
      */
    @Test( expected= IllegalArgumentException.class )
     public void testConstructor_NoTeams(){
         Logic g= new Logic( new LinkedList< Manager >(), Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when there are no players.
      */
    @Test( expected= IllegalArgumentException.class )
     public void testConstructor_NoPlayers(){
         List< Manager > teams= new LinkedList< Manager >();
         teams.add( new Manager( "Alpha" ) );
         Logic g= new Logic( teams, Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when there are empty teams.
      */
    @Test( expected= IllegalArgumentException.class )
     public void testConstructor_NoPlayers2(){
         List< Manager > teams= new LinkedList< Manager >();
         Manager man= new Manager( "Alpha" );
         man.addPlayer( "bob" );
         teams.add( new Manager( "Bravo" ) );
         Logic g= new Logic( teams, Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when the wordLength is smaller than the dictionary limit.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_WordTooSmall(){
         Logic g= new Logic( wrapLogic.teams, Dictionary.MIN_WORDLENGTH - 1 );
     }
     
     /**
      * Tests the constructor to ensure it fails when the wordLength is larger than the dictionary limit.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_WordTooLarge(){
         Logic g= new Logic( wrapLogic.teams, Dictionary.MAX_WORDLENGTH + 1 );
     }
     
     /**
      * Tests the constructor to ensure it fails when the gameWord is null.
      */
     @Test( expected= NullPointerException.class )
     public void testConstructor_NullGameWord(){
         Logic g= new Logic( wrapLogic.teams, null );
     }
     
     /**
      * Tests the constructor to ensure it fails when the gameWord is empty.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_EmptyGameWord(){
         Logic g= new Logic( wrapLogic.teams, "" );
     }
     
     /**
      * GetStatusWord should return all underscores before the game starts.
      */
     /* @Test
     public void testGetStatusWord_BeforeGame() {
         for( int i= Dictionary.MIN_WORDLENGTH; i <= Dictionary.MAX_WORDLENGTH; ++i ) {
             Logic g= new Logic( teams, i );
             String word= g.getWordStatus();
             for( int k= 0; k <  word.length(); ++k )
                 assertEquals( '_', word.charAt( k ) );
         }
     }*/
     
     /**
      * Test makeGuess with a bad argument. Error expected.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testMakeGuess_wDigit() {
         game.makeGuess( '9' );
     }
     
     /**
      * Test makeGuess. Should not throw an error.
      */
     @Test
     public void testMakeGuess() {
         Logic newGame= wrapLogic.getInstance();
         newGame.makeGuess( 'a' );
     }
     
     /**
      * Tests makeGuess to make sure good guesses return true.
      */
     @Test
     public void testMakeGuess_GoodGuess() {
         {
             Logic newGame= wrapLogic.getInstance( "logic" );
             assertTrue( newGame.makeGuess( 'l' ) );
         }
         
         {
             Logic newGame= wrapLogic.getInstance( "logic" );
             assertTrue( newGame.makeGuess( 'g' ) );
         }
         
         {
             Logic newGame= wrapLogic.getInstance( "logic" );
             assertTrue( newGame.makeGuess( 'c' ) );
         }
     }
     
     /**
      * Tests makeGuess to make sure good guesses return true. Capital letters shouldn't matter.
      */
     @Test
     public void testMakeGuess_GoodGuess_Capital() {
         {
             Logic newGame= wrapLogic.getInstance( "logic" );
             assertTrue( newGame.makeGuess( 'L' ) );
         }
         
         {
             Logic newGame= wrapLogic.getInstance( "logic" );
             assertTrue( newGame.makeGuess( 'G' ) );
         }
         
         {
             Logic newGame= wrapLogic.getInstance( "logic" );
             assertTrue( newGame.makeGuess( 'C' ) );
         }
     }
     
     /**
      * Tests makeGuess to make sure bad guesses return false.
      */
     @Test
     public void testMakeGuess_BadGuess() {
         Logic newGame= wrapLogic.getInstance( "logic" );
         assertFalse( newGame.makeGuess( 'a' ) );
     }
     
     /**
      * Test UI event setter to make sure this doesn't throw an error.
      */
     @Test
     public void testSetGameEventsHandler() {
        game.setGameEventsHandler( new GameUI( game ) );
     }
     
     /**
      * Test UI event setter to make sure null does not throw an error.
      */
     @Test
     public void testSetGameEventsHandler_null() {
         game.setGameEventsHandler( null );
     }
     
     /**
      * Test UI event setter to make sure null does not throw an error.
      */
     @Test
     public void testGetAttempts() {
         Logic newGame= wrapLogic.getInstance( "Logic" );
         assertEquals( 0, newGame.getAttempts() );
         newGame.makeGuess( 'l' );
         assertEquals( 1, newGame.getAttempts() );
     }
 }

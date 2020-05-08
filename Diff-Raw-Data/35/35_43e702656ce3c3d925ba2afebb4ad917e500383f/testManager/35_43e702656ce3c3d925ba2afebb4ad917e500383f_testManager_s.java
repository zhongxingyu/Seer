 
 
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.Assert;
 
 /**
  * The test class testManager.
  * 
  * TODO:
  *  testGetScore_InGame()
  *
  * @author  Josh Gillham
  * @version 9-23-12
  */
 public class testManager 
 {
     /**
      * Default constructor for test class testManager
      */
     public testManager()
     {
     }
     
     /**
      * Tests the constructor behavior.
      *
      * Called before every test case method.
      */
     @Before
     public void setUp(){
         try{
             String teamName= "Test Team";
             Manager m= new Manager( "Test Team" );
         } catch( Exception e ) {
             fail( "Constructor should not throw error" );
         }
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
     
     /**
      * Test the constructor with a null. Errors are expected
      */
     @Test( expected= NullPointerException.class )
    public void testConstructor_BadTeamNames(){
         Manager m= new Manager( null );
     }
     
     /**
      * Test the constructor with an empty name. Errors are expected
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_BadTeamNames(){
         Manager m= new Manager( "" );
     }
     
     /**
      * Tests getScore() to ensure that it is initialized to 0.
      */
     @Test
     public void testGetScore() {
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         
         Assert.assertEquals( 0, m.getScore() );
     }
     
     /**
      * Tests getName() to ensure that it is initialized properly.
      */
     @Test
     public void testGetName() {
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         
         Assert.assertEquals( teamName, m.getName() );
     }
         
     /**
      * Tests add player. Should return a non-null.
      */
     @Test
     public void testAddPlayer(){
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         
         String[] morePlayerNames= { "Susan", "George", "Bob", "Lucas", "Fred" };
         
         for( String name: morePlayerNames )
             Assert.assertNotNull( m.addPlayer( name ) );
     }
     
     /**
      * Test roster size. Should return the number of players.
      */
     @Test
     public void testGetRosterSize() {
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         
         for( int i= 0; i < 10; ++i ){
             m.addPlayer( new Integer( i ).toString() );
             Assert.assertEquals( i + 1, m.getRosterSize() );
         }
     }
     
     /**
      * Test getPlayerUp() with no players. Errors are expected.
      */
    @Test( expected= NoSuchElementException.class )
    public void testGetPlayerUp() {
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         m.getPlayerUp();
     }
     
     /**
      * Test getPlayerUp() by adding players and checking that
      *  it always returns the first player.
      */
     @Test
     public void testGetPlayerUp() {
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         Player firstPlayer= null;
         for( int i= 0; i < 10; ++i ){
             if( i == 0 )
                 firstPlayer= m.addPlayer( new Integer( i ).toString() );
             else
                 m.addPlayer( new Integer( i ).toString() );
             Assert.assertEquals( firstPlayer, m.getPlayerUp() );
         }
     }
     
     /**
      * Tests nextPlayer() with no players. Errors are expected.
      */
    @Test( expected= NoSuchElementException.class )
    public void testNextPlayer(){
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
        m.nextPlayer()
     }
     
     /**
      * Tests nextPlayer() by first adding each player then
      *  calling next and testing if the next player is up.
      */
     @Test
     public void testNextPlayer(){
         String teamName= "Test Team";
         Manager m= new Manager( "Test Team" );
         
         for( int i= 0; i < 10; ++i ){
             m.addPlayer( new Integer( i ).toString() );
         }
         
         for( int i= 1; i < 10; ++i ){
             m.nextPlayer();
             Assert.assertEquals( new Integer( i ), m.getPlayerUp() );
         }
         
     }
     
     /**
      * Test resignPlayer with a null.
      */
     @Test( expected= java.lang.NullPointerException.class )
     public void testResignPlayer_Null() {
         Manager m= new Manager( "Test" );
         m.resignPlayer( null );
     }
     
     /**
      * Test resignPlayer with a bad argument.
      */
     @Test( expected= java.util.NoSuchElementException.class )
     public void testResignPlayer_BadPlayer() {
         Manager m= new Manager( "Test" );
         m.resignPlayer( new Player( "Test" ) );
     }
     
     /**
      * Test resignPlayer with good arguments
      */
     @Test
     public void testResignPlayer() {
         String teamName= "Test Team";
         Manager m= new Manager( teamName );
         
         String[] morePlayerNames= { "Susan", "George", "Bob", "Lucas", "Fred" };
         
         for( String name: morePlayerNames ) {
             Player player= m.addPlayer( name );
             m.resignPlayer( player );
         }
     }
 }

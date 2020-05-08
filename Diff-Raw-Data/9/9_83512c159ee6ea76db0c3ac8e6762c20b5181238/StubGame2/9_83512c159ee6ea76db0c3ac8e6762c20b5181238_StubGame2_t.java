 package hotciv.stub;
 
 import hotciv.framework.*;
 
 import java.util.*;
 
 /** Test stub for game for visual testing of
  * minidraw based graphics.
  *
  * dSoftArk support code.
  *
  <#if type == "code"> 
  <#include "/data/author.txt"> 
  </#if>
 */
 
 public class StubGame2 implements Game {
 
     // === Unit handling ===
     private Position pos_archer_red;
     private Position pos_legion_blue;
     private Position pos_settler_red;
 
     private Unit red_archer;
 
     public void addCityAt(Position p, City c) {}
     public GameEventController getEventController() { return null; }
     public Collection<City> getCities() { return null; }
 
     public Unit getUnitAt(Position p) {
         if ( p.equals(pos_archer_red) ) {
             return red_archer;
         }
         if ( p.equals(pos_settler_red) ) {
             return new StubUnit( GameConstants.SETTLER, Player.RED );
         }
         if ( p.equals(pos_legion_blue) ) {
             return new StubUnit( GameConstants.LEGION, Player.BLUE );
         }
         return null;
     }
     // Stub only allows moving red archer
     public boolean moveUnit( Position from, Position to ) { 
         System.out.println( "-- StubGame2 / moveUnit called: "+from+"->"+to );
        if (to.equals(new Position(0,0)))
            return false;
         if ( from.equals(pos_archer_red) ) {
             pos_archer_red = to;
         }
         // notify our observer(s) about the changes on the tiles
         gameObserver.worldChangedAt(from);
         gameObserver.worldChangedAt(to);
         return true; 
     }
 
     // === Turn handling ===
     private Player inTurn;
     public void endOfTurn() {
         System.out.println( "-- StubGame2 / endOfTurn called." );
         inTurn = (getPlayerInTurn() == Player.RED ?
                   Player.BLUE : 
                   Player.RED );
         // no age increments
         gameObserver.turnEnds(inTurn, -4000);
     }
     public Player getPlayerInTurn() { return inTurn; }
   
 
     // === Observer handling ===
     protected GameObserver gameObserver;
     // observer list is only a single one...
     public void addObserver(GameObserver observer) {
         gameObserver = observer;
     } 
     
 
 
     public StubGame2() { 
         defineWorld(1); 
         // AlphaCiv configuration
         pos_archer_red = new Position( 2, 0);
         pos_legion_blue = new Position( 3, 2);
         pos_settler_red = new Position( 4, 3);
 
         // the only one I need to store for this stub
         red_archer = new StubUnit( GameConstants.ARCHER, Player.RED );   
 
         inTurn = Player.RED;
     }
 
     // A simple implementation to draw the map of DeltaCiv
     protected Map<Position,Tile> world; 
     public Tile getTileAt( Position p ) { return world.get(p); }
 
 
     /** define the world.
      * @param worldType 1 gives one layout while all other
      * values provide a second world layout.
      */
     protected void defineWorld(int worldType) {
         world = new HashMap<Position,Tile>();
         for ( int r = 0; r < GameConstants.WORLDSIZE; r++ ) {
             for ( int c = 0; c < GameConstants.WORLDSIZE; c++ ) {
                 Position p = new Position(r,c);
                 world.put( p, new StubTile(p, GameConstants.PLAINS));
             }
         }
     }
 
     public City getCityAt( Position p ) { return null; }
     public Player getWinner() { return null; }
     public int getAge() { return 0; }  
     public void changeWorkForceFocusInCityAt( Position p, String balance ) {}
     public void changeProductionInCityAt( Position p, String unitType ) {}
     public void performUnitActionAt( Position p ) {}  
     public void setTileFocus(Position position) {}
 
 }
 
 class StubUnit implements  Unit {
     private String type;
     private Player owner;
     public StubUnit(String type, Player owner) {
         this.type = type;
         this.owner = owner;
     }
     public String getTypeString() { return type; }
     public Player getOwner() { return owner; }
     public int getMoveCount() { return 1; }
     public int getDefensiveStrength() { return 0; }
     public int getAttackingStrength() { return 0; }
     public int getLastMoved() { return 0; }
 
     public boolean isFortified() { return false; }
     public Unit withLastMoved(int moved) { return this; }
     public Unit withFortify(boolean fortify, int defStr) { return this; }
 }

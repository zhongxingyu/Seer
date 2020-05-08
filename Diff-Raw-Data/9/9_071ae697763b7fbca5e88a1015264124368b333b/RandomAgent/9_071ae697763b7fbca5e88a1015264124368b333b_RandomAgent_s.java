 package agents;
 
 import pplgg.MapManager;
 import pplgg.PPLGG.Terrain;
 
 public class RandomAgent extends AbstractAgent {
     
    public RandomAgent(MapManager newManager) {
        super(newManager);
         moveToRandomPosition();
        setTokens( 10 );
     }
 
     @Override
     public void performStep() {
         //switch terrain at position
         
         if (requestMapInformation( x, y ) == Terrain.EMPTY) {
             sendMapRequest( x, y, Terrain.SOLID );
         } else {
             sendMapRequest( x, y, Terrain.EMPTY );
         }
         
         //sendMapRequest( x, y, Terrain.SOLID );
         
         //move to a new position
         moveToRandomPosition();
         
         decreaseTokens( 1 );
         
     }
     
     private void moveToRandomPosition() {
         moveTo((int) (Math.random() * askForMapWidth()), (int)(Math.random() * askForMapHeight()));
     }
     
     
     
     
     
     
     
 }

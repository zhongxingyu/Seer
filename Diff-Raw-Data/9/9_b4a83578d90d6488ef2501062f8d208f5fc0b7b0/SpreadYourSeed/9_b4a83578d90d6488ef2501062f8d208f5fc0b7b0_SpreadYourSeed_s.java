 package spreadyourseed;
 
 import com.golden.gamedev.GameObject;
 
 public class SpreadYourSeed extends com.golden.gamedev.GameEngine {
     PlayerData myPlayerData;
     
     public SpreadYourSeed() {
         myPlayerData = new PlayerData(5,0,false,false);
     }
     
     @Override
     public GameObject getGame(int gameID) {
         switch (gameID) {
            case 0: return new LevelTwo(this, myPlayerData);
             case 1: return new LevelTwo(this, myPlayerData);
         }
         
         return null;
     }
 
 }

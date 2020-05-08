 import com.sun.jna.Library;
 import com.sun.jna.Pointer;
 import com.sun.jna.Native;
 
 public interface Client extends Library {
   Client INSTANCE = (Client)Native.loadLibrary("client", Client.class);
   Pointer createConnection();
   boolean serverConnect(Pointer connection, String host, String port);
 
   boolean serverLogin(Pointer connection, String username, String password);
   int createGame(Pointer connection);
   int joinGame(Pointer connection, int id, String playerType);
 
   void endTurn(Pointer connection);
   void getStatus(Pointer connection);
 
   int networkLoop(Pointer connection);
 
 
     //commands
   int creatureMove(Pointer object, int x, int y);
   int creatureEat(Pointer object, int x, int y);
   int creatureBreed(Pointer object, Pointer mate, int x, int y);
   int playerTalk(Pointer object, String message);
 
     //accessors
   int getTurnNumber(Pointer connection);
   int getPlayerID(Pointer connection);
  int getMapSize(Pointer connection);
 
   Pointer getCreature(Pointer connection, int num);
   int getCreatureCount(Pointer connection);
   Pointer getPlant(Pointer connection, int num);
   int getPlantCount(Pointer connection);
   Pointer getPlayer(Pointer connection, int num);
   int getPlayerCount(Pointer connection);
 
 
     //getters
   int creatureGetId(Pointer ptr);
   int creatureGetOwner(Pointer ptr);
   int creatureGetX(Pointer ptr);
   int creatureGetY(Pointer ptr);
   int creatureGetMaxEnergy(Pointer ptr);
   int creatureGetEnergyLeft(Pointer ptr);
   int creatureGetCarnivorism(Pointer ptr);
   int creatureGetHerbivorism(Pointer ptr);
   int creatureGetSpeed(Pointer ptr);
   int creatureGetMovementLeft(Pointer ptr);
   int creatureGetDefense(Pointer ptr);
 
   int plantGetId(Pointer ptr);
   int plantGetX(Pointer ptr);
   int plantGetY(Pointer ptr);
   int plantGetSize(Pointer ptr);
 
   int playerGetId(Pointer ptr);
   String playerGetPlayerName(Pointer ptr);
   float playerGetTime(Pointer ptr);
 
 
     //properties
 
 }

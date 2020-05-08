 package server;
 
 import commonlib.D2vector;
 import commonlib.GameSituationSerialized;
 import commonlib.Random_2Dcoord_generator;
 import commonlib.gameObjects.Map;
 import commonlib.gameObjects2.game_map;
 import commonlib.gameObjects2.nutrient;
 import commonlib.network.GameServerResponseGameOver;
 import server.network.GameServer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  */
 public class Game
 {
     //30 updates per 1000 millisecond
     private final int TIME_BETWEEN_UPDATES = 1000/30;
 
     private GameServer gameServer;
     private NetworkController networkController;
     private MovementController movementController;
     private GameStateController gameStateController;
 
     private Player player1;
     private Player player2;
     private String player1Name;
     private String player2Name;
     private String player1Password;
     private String player2Password;
     private List<nutrient> nutrients;
 
     private game_map mygamemap;
     //private Random_2Dcoord_generator rand_generator;
 
 
     public Game(String player1Name, String player1Password, String player2Name, String player2Password)
     {
         this.player1Name = player1Name;
         this.player1Password = player1Password;
         this.player2Name = player2Name;
         this.player2Password = player2Password;
         nutrients = new ArrayList<nutrient>();
         mygamemap = new game_map(Map.WIDTH, Map.HEIGHT);
         generateNutrients(3);
     }
 
     public NetworkController getNetworkController()
     {
         return networkController;
     }
 
     void start()
     {
         System.out.println("Starting game server");
         try
         {
             gameServer = new GameServer(this);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
        mygamemap = new game_map(Map.WIDTH,Map.HEIGHT);
         mygamemap.initialize_map(0);
         //mygamemap.generate_new_nutrient(30);
         int i =0;
 
         movementController = new MovementController();
         networkController = new NetworkController(this);
         gameStateController = new GameStateController();
         player1 = new Player(player1Name, player1Password, 50, Map.HEIGHT/2);
         player2 = new Player(player2Name, player2Password, Map.WIDTH-50, Map.HEIGHT/2);
 
         // The game starts here, looping until a player wins
         int timeout = 0;
         int nutrientCounter = 0;
         while (true)
         {
             GameSituationSerialized gameSituation = new GameSituationSerialized();
             try
             {
                 timeout++;
                 nutrientCounter++;
                 Thread.sleep(TIME_BETWEEN_UPDATES);
 
 
                 //mygamemap.generate_new_nutrient(1);
                 //mygamemap.remove_last_item();
 
                 movementController.moveSwarm(player1);
                 movementController.moveSwarm(player2);
 
                 gameSituation.setSwarm1(player1.getSwarm());
                 gameSituation.setSwarm2(player2.getSwarm());
 
                 gameStateController.calculatePowerups(player1.getSwarm(), nutrients);
                 gameStateController.calculatePowerups(player2.getSwarm(), nutrients);
                 String winner = gameStateController.calculateDamage(player1, player2);
                 gameSituation.setWinner(winner);
 
                 if(nutrientCounter % 100 == 0)
                 {
                    if(nutrients.size() < 5)
                    {
                        generateNutrients(1);
                    }
                 }
 
                 gameSituation.setNutrients(nutrients);
 
 
 
                 gameServer.sendToAll(gameSituation);
 
                 if(winner != null)
                 {
                     gameServer.sendToAll(new GameServerResponseGameOver(winner));
                     break;
                 }
 
                 //the server times out if it is taking too long to update
                 if(timeout == 10000)
                 {
                     gameServer.sendToAll(new GameServerResponseGameOver("Disconnect"));
                     timeout = 0;
                 }
 
             }
             catch (InterruptedException e)
             {
                 // Exception can be generated if signal is received by thread
                 // Just ignore it
                 // e.printStackTrace();
             }
         }
     }
 
     public Player verifyPlayer(String name, String password)
     {
         if (this.player1.getName().equals(name) && this.player1.getPassword().equals(password))
             return this.player1;
         else if (this.player2.getName().equals(name) && this.player2.getPassword().equals(password))
             return this.player2;
         else
             return null;
     }
 
     public void generateNutrients(int i)
     {
         List<D2vector> vectors = mygamemap.generate_corarray_for_newnutrient(i);
         for(D2vector vector : vectors)
         {
             nutrients.add(new nutrient(vector));
         }
     }
 
     public void disconnectPlayer(Player player)
     {
         return;
     }
 }

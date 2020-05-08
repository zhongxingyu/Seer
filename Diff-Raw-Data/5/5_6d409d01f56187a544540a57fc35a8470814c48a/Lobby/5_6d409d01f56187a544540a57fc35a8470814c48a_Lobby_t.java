 package edu.gatech.cs2340.risky.models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.servlet.http.HttpServletRequest;
 
 import edu.gatech.cs2340.risky.Database;
 import edu.gatech.cs2340.risky.Model;
 import edu.gatech.cs2340.risky.RiskyServlet;
 import edu.gatech.cs2340.risky.database.HashMapDbImpl;
 import edu.gatech.cs2340.risky.database.ModelDb;
 import edu.gatech.cs2340.risky.models.factories.MapFactory;
 
 public class Lobby extends Model {
     
     public static final int MIN_PLAYERS = 3;
     public static final int MAX_PLAYERS = 6;
     
     public String title;
     public ArrayList<Object> players;
     public TurnOrder turnOrder;
     public Object mapId;
     
     public Lobby(Object id) {
         this(id, "Default Lobby");
     }
     
     public Lobby(Object id, String title) {
         this.id = id;
         this.title = title;
         this.turnOrder = new TurnOrder(this.id);
         MapFactory.get(0);
         this.mapId = 0;
         this.players = new ArrayList<Object>();
     }
 
     public ArrayList<Player> getPlayers() {
         ArrayList<Player> players = new ArrayList<Player>();
         ModelDb<Player> playerDb = Database.getDb(Player.class);
         for (Object playerId : this.players) {
             players.add(playerDb.read(playerId));
         }
         Collections.sort(players);
         return players;
     }
     
     public void addPlayer() {
         
     }
 
     public String getTitle() {
         return title;
     }
     
     public boolean isReadyToPlay() {
         System.out.println("Player count: " + this.players.size());
         System.out.println(this.hasEnoughPlayers() ? "true" : "false");
         System.out.println(!this.hasTooManyPlayers() ? "true" : "false");
         System.out.println((this.mapId != null) ? "true" : "false");
         return this.hasEnoughPlayers() && !this.hasTooManyPlayers() && this.mapId != null;
     }
     
     public boolean hasEnoughPlayers() {
         return this.players.size() >= MIN_PLAYERS;
     }
     
     public boolean hasTooManyPlayers() {
         return this.players.size() >= MAX_PLAYERS;
     }
     
     public void allocateArmies() {
         for (Player player : this.getPlayers()) {
             player.allocateArmies(this.calculateArmies(this.players.size()));
         }
     }
     
     public void assignTerritories(){
         Map map = Map.get(this.mapId);
         int numofTerritories = map.territories.size();
         int territoriesPerPlayer = numofTerritories/players.size();
         int territoriesExtra = numofTerritories%players.size();
        
         //make sure ids will be assigned randomly
         ArrayList<Integer> terrIds=new ArrayList<Integer>();
         for(int i=0;i<numofTerritories;++i) {
             terrIds.add(i);
         }
         Collections.shuffle(terrIds);
         
         int t_index=0;
         ModelDb<Player> playerDb = Database.getDb(Player.class);
         //distribute main bulk of territories
         for(Object p : players) {
             Player player=playerDb.read(p);
             for(int i=0;i<territoriesPerPlayer;++i) {
                 TerritoryDeed deed = new TerritoryDeed(player.id);
                 deed.playerId = player.id;
                 int territory = terrIds.get(t_index++);
 
                map.deeds.put(Integer.toString(territory), deed);
                 player.territories.put(territory, deed);
             }
         }
         //distribute left over territories
         for(int i=0; i<territoriesExtra; ++i){
             Object p = players.get(i);
             Player player=playerDb.read(p);
             TerritoryDeed deed = new TerritoryDeed(player.id);
             deed.playerId = player.id;
             int territory = terrIds.get(t_index++);
 
            map.deeds.put(Integer.toString(territory), deed);
             player.territories.put(territory, deed);
         }
     }
     
     public int calculateArmies(int numPlayers) {
         switch (numPlayers) {
         case 3:
             return 35;
         case 4:
             return 30;
         case 5:
             return 25;
         case 6:
             return 20;
         }
         return 0;
     }
     
     public int getWinner() {
         ArrayList<Player> players = this.getPlayers();
         Map map = Map.get(this.mapId);
         for (int i=0 ; i< players.size() ; i++) {
             if (players.get(i).territories.values().size() == map.deeds.size()) {
                 return i;
             }
         }
         return -1;
     }
     
     public void populateValidWith(Lobby l) {
         this.title = l.title;
     }
     
     public static Lobby getInstance(Object id) {
         Lobby lobby = new Lobby(id);
         return lobby;
     }
 
     public static Lobby get(HttpServletRequest request) {
         return Lobby.get(RiskyServlet.getSessionId(request));
     }
 
     public static Lobby get(String id) {
         Lobby lobby = Database.getModel(Lobby.class, id, new HashMapDbImpl<Lobby>());
 
         if (lobby == null) {
             lobby = new Lobby(id);
             Database.setModel(lobby);
         }
         
         return lobby;
     }
     
 }

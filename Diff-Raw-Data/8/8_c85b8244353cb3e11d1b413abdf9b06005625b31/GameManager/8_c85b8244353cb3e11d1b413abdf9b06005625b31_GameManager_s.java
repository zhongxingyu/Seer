 package com.CC.Arenas;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 public class GameManager
 {
 
     private HashMap<String, String> players;
     private HashMap<String, Game> games;
 
     public GameManager()
     {
         this.games = new HashMap<String, Game>();
         this.players =  new HashMap<String, String>();
     }
     
     public Game getGame(String name)
     {
         return this.games.get(name);
     }
     
     public boolean createGame(String name)
     {
         if(this.games.containsKey(name))
         {
             return false;
         }
         Game g = new Game();
         this.games.put(name, g);
         return true;
     }
     
     public boolean removeGame(String name)
     {
         if(!this.games.containsKey(name))
         {
             return false;
         }
         this.games.remove(name);
         return true;
     }
     
    public Game getGame(Player p)
     {
        return getGame(p.getName());
     }
     
     /*
     *   Can return null!
     **/
    public Game getGame(String pname)
     {
         if(players.containsKey(pname))
         {
             return games.get(players.get(pname));
         }
         return null;
     }
     
 }

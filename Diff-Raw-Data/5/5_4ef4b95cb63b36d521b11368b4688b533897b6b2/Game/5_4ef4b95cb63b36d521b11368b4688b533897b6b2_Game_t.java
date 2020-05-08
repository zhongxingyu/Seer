 package ogo.spec.game.model;
 
 import java.util.Timer;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.TimerTask;
 
 public class Game implements Iterable<Player> {
 public static void main(String[] args)
 {
     Player p = new Player("jan");
     p.setCreatures(new Creature[]{new LandCreature(new Tile(TileType.LAND, 1, 1), null)});
     Game g = new Game(new Player[]{p},null);
     g.start();
 }
     
    public static final int TICK_TIME_IN_MS = 50;
     private Timer timer;
     private Player[] players;
     private GameMap map;
 
     public Game(Player[] players, GameMap map) {
         this.players = players;
         this.map = map;
         this.timer = new Timer();
     }
 
     public void start() {
         this.timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 tick();
             }
        }, 0, Game.TICK_TIME_IN_MS);
     }
 
     private void tick() {
         for (int i = 0; i < players.length; i++) {
             Creature[] c = players[i].getCreatures();
             for (int j = 0; j < c.length; j++) {
                 c[j].tick();//tick m op zn neus
             }
         }
     }
 
     public GameMap getMap() {
         return map;
     }
 
     @Override
     public Iterator<Player> iterator() {
         return Arrays.asList(players).iterator();
     }
 }

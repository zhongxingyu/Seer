 package ogo.spec.game.model;
 
 import java.util.Timer;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import ogo.spec.game.sound.SoundMonitor;
 
 public class Game implements Iterable<Player> {
 
     public class TickTimerTask extends TimerTask
     {
         public boolean pause = false;
 
         @Override
         public void run()
         {
             if (!pause) {
                 tick();
             }
         }
     }
 
     public static Game globalGameObject;
     /*
     public static void main(String[] args)
     {
     Player p = new Player("jan");
     p.setCreatures(new Creature[]{new LandCreature(new Tile(TileType.LAND, 1, 1), null)});
     Game g = new Game(new Player[]{p},null);
     g.start();
     }
      */
     public static final int TICK_TIME_IN_MS = 50;
     private long tick = 0;
     private Timer timer;
    public TickTimerTask tickTimerTask = new TickTimerTask();
     private Player[] players;
     private GameMap map;
     private SoundMonitor sm;
     private ConcurrentLinkedQueue<Change> changes = new ConcurrentLinkedQueue<Change>();
 
     private int myPlayerId;
 
     public Game(Player[] players, GameMap map, int myPlayerId) {
         this.myPlayerId = myPlayerId;
         this.players = players;
         this.map = map;
         this.timer = new Timer();
     }
 
     public void start() {
         this.sm = new SoundMonitor();
         this.sm.run();
         globalGameObject = this;
         this.timer.schedule(tickTimerTask, 0, Game.TICK_TIME_IN_MS);
     }
 
     private void tick() {
         tick++;
         if (tick % 50 == 0) {
             System.err.println("Tick: " + tick);
         }
         for (int i = 0; i < players.length; i++) {
             if (this.myPlayerId != players[i].getId()) {
                 continue;//only call tick for my own creatures
             }
             Creature[] c = players[i].getCreatures();
             for (int j = 0; j < c.length; j++) {
                 c[j].tick(tick);
                 //System.out.println("Player " + String.valueOf(i) + ", creature " + String.valueOf(j) + ":" + c[j].toString());
             }
         }
     }
 
     public Player getWinner()
     {
         int playersWith0 = 0;
         Player playerWithAliveCreatures = null;
         for(int i = 0;i<players.length;i++)
         {
             boolean alldead = true;
             for(int j = 0;j<players[i].getCreatures().length;j++)
             {
                 if(players[i].getCreatures()[j].isAlive())
                     alldead=false;
             }
             if(alldead)
                 playersWith0++;
             else
                 playerWithAliveCreatures = players[i];
         }
         if(playersWith0 + 1 == players.length)
             return playerWithAliveCreatures;
         return null;
     }
 
     /**
      * Add a change.
      */
     public void addChange(Change change) {
         changes.add(change);
     }
 
     /**
      * Get the next change.
      *
      * @return the next change, or null if none exists
      */
     public Change poll() {
         return changes.poll();
     }
 
     /**
      * Get the current tick.
      */
     public long getTick() {
         return tick;
     }
 
     public GameMap getMap() {
         return map;
     }
 
     public Player getPlayer(int id) {
         return players[id];
     }
 
     public Creature getCreature(int id) {
         int player = id / 3;
         int creature = id % 3;
         return players[player].getCreatures()[creature];
     }
 
     public Player[] getPlayers() {
         return players;
     }
 
     public Player getPlayer(Creature c) {
         for (int i = 0; i < this.players.length; i++) {
             for (int j = 0; j < this.players[i].getCreatures().length; j++) {
                 if (this.players[i].getCreatures()[j].equals(c)) {
                     return this.players[i];
                 }
             }
         }
         return null;
     }
 
     public int getSoundLevel() {
         return this.sm.getSoundLevel();
     }
 
     @Override
     public Iterator<Player> iterator() {
         return Arrays.asList(players).iterator();
     }
 }

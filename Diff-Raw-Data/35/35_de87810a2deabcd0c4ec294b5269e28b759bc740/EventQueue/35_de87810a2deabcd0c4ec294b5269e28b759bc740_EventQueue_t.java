 package fr.debnet.ircrpg.game.queues;
 
 import fr.debnet.ircrpg.Config;
 import fr.debnet.ircrpg.interfaces.IQueue;
 import fr.debnet.ircrpg.interfaces.INotifiable;
 import fr.debnet.ircrpg.game.Game;
 import fr.debnet.ircrpg.mock.Random;
 import fr.debnet.ircrpg.models.Event;
 import fr.debnet.ircrpg.models.Player;
 import fr.debnet.ircrpg.models.Result;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Event thread
  * @author Marc
  */
 public class EventQueue extends Thread implements IQueue {
     
     private static IQueue queue;
     
     public static IQueue getInstance(Game game) {
         if (queue == null || queue.getGame() != game)
             queue = new EventQueue(game);
         return queue;
     }
     
     private Game game;
     private Random random;
     private boolean run = true;
     private final List<INotifiable> notifiables;
     
     private Map<Player, List<Event>> events;
     
     public EventQueue(Game game) {
         this.setName(this.getClass().getSimpleName());
         this.game = game;
         this.random = new Random();
         this.notifiables = new ArrayList<>();
         this.events = new HashMap<>();
         this.start();
     }
     
     @Override
     public void run() {
         while (run) {
             try {
                 // Current date
                 Calendar now = Calendar.getInstance();
                 // Get list of players which have at least an executeEvent
                 List<Player> players = new ArrayList<>(this.events.keySet());
                 
                 if (!players.isEmpty()) {
                     // Get a random player from the list
                     int value = (int)(this.random.nextDouble() * players.size());
                     Player player = players.get(value);
 
                     if (player != null) {
                         // Get list of player's events
                         List<Event> events = this.events.get(player);
                         
                         if (!events.isEmpty()) {
                             // Get a random executeEvent from the list
                             value = (int)(this.random.nextDouble() * events.size());
                             Event event = events.get(value);
                             // Calculate the executeEvent chance
                             value = (int)(this.random.nextDouble() * 100);
                             // Check the player timespan since last executeEvent
                             Calendar date = (Calendar)player.getLastEvent().clone();
                             date.add(Calendar.MINUTE, Config.EVENT_TIME);
                             // Event execution
                             if (now.after(date) && value >= event.getChance()) {
                                 Result result = this.game.executeEvent(player, event);
                                 // Notify observers
                                 for (INotifiable notifiable : this.notifiables) {
                                     notifiable.notify(result);
                                 }
                             }
                         }
                     }
                 }
                 
                 Thread.sleep(Config.EVENT_SLEEP * 1000);
             } catch (InterruptedException ex) {
                 Logger.getLogger(EventQueue.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     @Override
     public boolean register(INotifiable notifiable) {
         if (!this.notifiables.contains(notifiable)) {
             this.notifiables.add(notifiable);
             return true;
         }
         return false;
     }
 
     @Override
     public synchronized void update(Player player) {
        if (!player.getOnline()) {
             this.events.remove(player);
             return;
         }
         List<Event> events = this.game.getEventsForPlayer(player);
         if (events.size() > 0)
             this.events.put(player, events);
         else this.events.remove(player);
     }
     
     @Override
     public void interrupt() {
         this.run = false;
     }
     
     @Override
     public Game getGame() {
         return this.game;
     }
 }

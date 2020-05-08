 package fr.debnet.ircrpg.game.queues;
 
 import fr.debnet.ircrpg.interfaces.IQueue;
 import fr.debnet.ircrpg.interfaces.INotifiable;
 import fr.debnet.ircrpg.Config;
 import fr.debnet.ircrpg.enums.Activity;
 import fr.debnet.ircrpg.enums.Status;
 import fr.debnet.ircrpg.game.Game;
 import fr.debnet.ircrpg.models.Modifiers;
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
  * Update player thread
  * @author Marc
  */
 public class UpdateQueue extends Thread implements IQueue {
 
     private static IQueue queue;
     
     public static IQueue getInstance(Game game) {
         if (queue == null || queue.getGame() != game)
             queue = new UpdateQueue(game);
         return queue;
     }
     
     private Game game;
     private boolean run = true;
     private List<INotifiable> notifiables;
     
     private Map<Player, Calendar> players;
     private Player player;
     private Calendar date;
     
     public UpdateQueue(Game game) {
         this.setName(this.getClass().getSimpleName());
         this.game = game;
         this.notifiables = new ArrayList<>();
         this.players = new HashMap<>();
         this.start();
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
     public void run() {
         while (run) {
             try {
                 if (this.player != null && this.date != null) {
                     Calendar now = Calendar.getInstance();
                     if (now.after(this.date)) {
                         // Update player
                         Result result = this.game.update(this.player, true, false);
                         // Notify observers
                         for (INotifiable notifiable : this.notifiables) {
                             notifiable.notify(result);
                         }
                         // Get the next player to update
                         this.next();
                     }
                 }
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {
                 Logger.getLogger(UpdateQueue.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
     private synchronized void next() {
         Player player = null;
         Calendar date = Config.MAX_DATE;
         for (Map.Entry<Player, Calendar> entry : this.players.entrySet()) {
             if (entry.getValue().before(date)) {
                 player = entry.getKey();
                 date = entry.getValue();
             }
         }
         this.player = player;
         this.date = date;
     }
 
     @Override
     public synchronized void update(Player player) {
         // Check if the player is online
         if (!player.getOnline()) {
            this.players.remove(player);
            if (player == this.player) this.next();
             return;
         }
         // Get player's modifiers
         Modifiers modifiers = new Modifiers(player);
         
         Calendar date = Config.MAX_DATE;
         // Check if the player's current activity will end
         if (player.getActivity() != Activity.NONE) {
             Calendar nextDate = Calendar.getInstance();
             nextDate.add(Calendar.MILLISECOND, -player.getActivityDuration().intValue());
             switch (player.getActivity()) {
                 case WAITING:
                     nextDate.add(Calendar.MINUTE, Config.ACTIVITY_PENALTY);
                     break;
                 case RESTING:
                     nextDate.add(Calendar.MINUTE, Config.RESTING_TIME_MAX);
                     break;
                 case TRAINING:
                     nextDate.add(Calendar.MINUTE, Config.TRAINING_TIME_MAX);
                     break;
                 case WORKING:
                     nextDate.add(Calendar.MINUTE, Config.WORKING_TIME_MAX);
                     break;
             }
             if (nextDate.before(date)) date = nextDate;
         }
         // Check if the player's current status will be cured by itself
         if (player.getStatus() != Status.NORMAL) {
             Calendar nextDate = Calendar.getInstance();
             nextDate.add(Calendar.MILLISECOND, player.getStatusDuration().intValue());
             if (nextDate.before(date)) date = nextDate;
         }
         // Check when the player will die from poisoning
         if (player.getStatus() == Status.POISONED) {
             double damagePerHour = player.getMaxHealth() * 
                 (Config.POISON_EFFECT + modifiers.getPoisonEffect());
             int timeDeath = (int)((player.getCurrentHealth() / damagePerHour) * Config.HOUR);
             // Check if the status duration can kill the player
             if (timeDeath < this.player.getStatusDuration()) {
                 Calendar nextDate = Calendar.getInstance();
                 nextDate.add(Calendar.MILLISECOND, timeDeath);
                 if (nextDate.before(date)) date = nextDate;
             }
         }
         // Check if the player will earn a level from training
         if (player.getActivity() == Activity.TRAINING) {
             double expRequired = player.getExperienceRequired() - player.getExperience();
             int timeRequired = (int)((expRequired / Config.RATE_EXPERIENCE) * Config.HOUR);
             Calendar nextDate = Calendar.getInstance();
             nextDate.add(Calendar.MILLISECOND, timeRequired);
             if (nextDate.before(date)) date = nextDate;
         }
         // If the current player is next to update
         if (this.date == null || date.before(this.date)) {
             this.player = player;
             this.date = date;
         }
         // Add or update player
         this.players.remove(player);
         this.players.put(player, date);
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

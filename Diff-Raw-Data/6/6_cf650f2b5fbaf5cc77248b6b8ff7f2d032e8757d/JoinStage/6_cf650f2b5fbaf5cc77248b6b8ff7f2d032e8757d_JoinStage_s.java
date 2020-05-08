 package fr.aumgn.dac2.stage.join;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 import fr.aumgn.bukkitutils.localization.PluginMessages;
 import fr.aumgn.bukkitutils.playerref.PlayerRef;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefHashMap;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefMap;
 import fr.aumgn.bukkitutils.playerref.set.PlayersRefHashSet;
 import fr.aumgn.bukkitutils.playerref.set.PlayersRefSet;
 import fr.aumgn.bukkitutils.util.Util;
 import fr.aumgn.dac2.DAC;
 import fr.aumgn.dac2.arena.Arena;
 import fr.aumgn.dac2.config.Color;
 import fr.aumgn.dac2.exceptions.TooManyPlayers;
 import fr.aumgn.dac2.game.start.GameStartData;
 import fr.aumgn.dac2.game.start.PlayerStartData;
 import fr.aumgn.dac2.game.start.SimplePlayerStartData;
 import fr.aumgn.dac2.stage.Stage;
 
 public class JoinStage implements Stage, Listener, GameStartData {
 
     private final DAC dac;
     private final Arena arena;
     private final Set<String> colorsTaken;
     private final PlayersRefMap<PlayerStartData> players;
     private final PlayersRefSet spectators;
 
     public JoinStage(DAC dac, Arena arena) {
         this.dac = dac;
         this.arena = arena;
         this.colorsTaken = new HashSet<String>();
         this.players = new PlayersRefHashMap<PlayerStartData>();
         this.spectators = new PlayersRefHashSet();
     }
 
     @Override
     public Arena getArena() {
         return arena;
     }
 
     @Override
     public void start() {
         PluginMessages messages = dac.getMessages();
        Util.broadcast("dac2.game.watch", 
                 messages.get("joinstage.start1", arena.getName()));
        Util.broadcast("dac2.game.watch", messages.get("joinstage.start2"));
     }
 
     @Override
     public void stop(boolean force) {
         if (force) {
             sendMessage(dac.getMessages().get("joinstage.stopped"));
         }
     }
 
     @Override
     public Listener[] getListeners() {
         return new Listener[0];
     }
 
     @Override
     public boolean contains(Player player) {
         return players.containsKey(player);
     }
 
     @Override
     public void sendMessage(String message) {
         for (Player player : players.playersSet()) {
             player.sendMessage(message);
         }
 
         String spectatorMessage = dac.getConfig().getSpectatorsMsg()
                 .format(new String[] { arena.getName(), message });
         for (Player spectator : spectators.players()) {
             spectator.sendMessage(spectatorMessage);
         }
     }
 
     public void addPlayer(Player player, List<Color> colors) {
         Color finalColor = null;
         for (Color color : colors) {
             if (!colorsTaken.contains(color.name)) {
                 finalColor = color;
                 break;
             }
         }
 
         addPlayer(player, finalColor);
     }
 
     public void addPlayer(Player player, Color color) {
         if (players.size() >= dac.getColors().size()) {
             throw new TooManyPlayers(dac.getMessages()
                     .get("joinstage.toomanyplayers"));
         }
 
         if (color == null || colorsTaken.contains(color.name)) {
             color = getFirstColorAvailable();
         }
 
         PluginMessages msgs = dac.getMessages();
         String playerName = color.chat + player.getDisplayName();
         sendMessage(msgs.get("joinstage.join", playerName));
 
         players.put(player, new SimplePlayerStartData(color, player));
         colorsTaken.add(color.name);
 
         list(player);
     }
 
     private Color getFirstColorAvailable() {
         List<Color> colors = dac.getColors().asList();
         for (Color color : colors) {
             if (!colorsTaken.contains(color.name)) {
                 return color;
             }
         }
 
         throw new Error("A unexpected error occured ! Please reoprt this to"
                 + " the plugin author.");
     }
 
     @Override
     public boolean isSpectator(Player player) {
         return spectators.contains(player);
     }
 
     @Override
     public void addSpectator(Player player) {
         spectators.add(player);
     }
 
     @Override
     public void removeSpectator(Player player) {
         spectators.remove(player);
     }
 
     public int size() {
         return players.size();
     }
 
     @Override
     public PlayersRefMap<PlayerStartData> getPlayersData() {
         return players;
     }
 
     @Override
     public PlayersRefSet getSpectators() {
         return spectators;
     }
 
     @Override
     public void list(CommandSender sender) {
         PluginMessages messages = dac.getMessages();
 
         sender.sendMessage(messages.get("joinstage.playerslist"));
         for (PlayerRef player : players.keySet()) {
             sender.sendMessage(messages.get("joinstage.playerentry", 
                     player.getDisplayName()));
         }
     }
 
     @Override
     public void onQuit(Player player) {
         PlayerStartData data = players.remove(player);
         colorsTaken.remove(data.getColor().name);
 
         String message = dac.getMessages().get("joinstage.quit",
                 data.getColor().chat + player.getDisplayName());
         sendMessage(message);
         player.sendMessage(message);
     }
 }

 package fr.aumgn.dac2.game.training;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.localization.PluginMessages;
 import fr.aumgn.bukkitutils.playerref.PlayerRef;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefHashMap;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefMap;
 import fr.aumgn.dac2.DAC;
 import fr.aumgn.dac2.arena.regions.Pool;
 import fr.aumgn.dac2.game.AbstractGame;
 import fr.aumgn.dac2.game.GameParty;
 import fr.aumgn.dac2.game.start.GameStartData;
 import fr.aumgn.dac2.game.start.PlayerStartData;
 import fr.aumgn.dac2.shape.column.Column;
 import fr.aumgn.dac2.shape.column.ColumnPattern;
 import fr.aumgn.dac2.shape.column.GlassyPattern;
 
 public class Training extends AbstractGame {
 
     private GameParty<TrainingPlayer> party;
     private PlayersRefMap<TrainingPlayer> playersMap;
 
     public Training(DAC dac, GameStartData data) {
         super(dac, data);
 
         Map<PlayerRef, ? extends PlayerStartData> playersData =
                 data.getPlayersData();
         List<TrainingPlayer> list =
                 new ArrayList<TrainingPlayer>(playersData.size());
         playersMap = new PlayersRefHashMap<TrainingPlayer>();
 
         for (Entry<PlayerRef, ? extends PlayerStartData> entry :
                 playersData.entrySet()) {
             PlayerRef playerId = entry.getKey();
             TrainingPlayer player =
                     new TrainingPlayer(playerId, entry.getValue());
             list.add(player);
             playersMap.put(playerId, player);
         }
         party = new GameParty<TrainingPlayer>(this, TrainingPlayer.class,
                 list);
     }
 
     @Override
     public void start() {
         resetPoolOnStart();
         send("training.start");
         nextTurn();
     }
 
     private void nextTurn() {
         TrainingPlayer player = party.nextTurn();
         tpBeforeJump(player);
         send("training.playerturn", player.getDisplayName());
     }
 
     @Override
     public void stop(boolean force) {
         resetPoolOnEnd();
         for (TrainingPlayer player : party.iterable()) {
             player.sendStats(dac.getMessages());
         }
     }
 
     @Override
     public boolean contains(Player player) {
         return playersMap.containsKey(player);
     }
 
     @Override
     public void sendMessage(String message) {
         for (TrainingPlayer player : party.iterable()) {
             player.sendMessage(message);
         }
         sendSpectators(message);
     }
 
     @Override
     public boolean isPlayerTurn(Player player) {
         TrainingPlayer trainingPlayer = playersMap.get(player);
         return trainingPlayer != null && party.isTurn(trainingPlayer);
     }
 
     @Override
     public void onJumpSuccess(Player player) {
         TrainingPlayer trainingPlayer = playersMap.get(player);
         World world = arena.getWorld();
         Pool pool = arena.getPool();
 
         Column column = pool.getColumn(player);
         ColumnPattern pattern = trainingPlayer.getColumnPattern();
 
         if (column.isADAC(world)) {
             send("training.jump.dac", trainingPlayer.getDisplayName());
             trainingPlayer.incrementDacs();
             pattern = new GlassyPattern(pattern);
         } else {
             send("training.jump.success", trainingPlayer.getDisplayName());
             trainingPlayer.incrementSuccesses();
         }
 
         column.set(world, pattern);
         if (pool.isFilled(world)) {
             pool.reset(world);
         }
         tpAfterJumpSuccess(trainingPlayer, column);
         nextTurn();
     }
 
     @Override
     public void onJumpFail(Player player) {
         TrainingPlayer trainingPlayer = playersMap.get(player);
         send("training.jump.fail", trainingPlayer.getDisplayName());
         trainingPlayer.incrementFails();
 
        tpAfterJumpFail(trainingPlayer);
         nextTurn();
     }
 
     @Override
     public void onQuit(Player player) {
         TrainingPlayer trainingPlayer = playersMap.get(player);
         party.removePlayer(trainingPlayer);
         playersMap.remove(player);
         trainingPlayer.sendStats(dac.getMessages());
     }
 
     @Override
     public void list(CommandSender sender) {
         PluginMessages messages = dac.getMessages();
 
         sender.sendMessage(messages.get("training.playerslist"));
         for (TrainingPlayer player : party.iterable()) {
            sender.sendMessage(messages.get("training.playerentry", 
                     player.getIndex(), player.getDisplayName(),
                     player.getSuccesses(), player.getDacs(),
                     player.getFails()));
         }
     }
 }

 /**
  * Listener class for CombatFlags
  * Listens for events being fired by hMod
  * @author dririan
  */
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class CombatFlagsListener extends PluginListener {
     private ArrayList<String> toggledUsers = new ArrayList<String>();
     private ArrayList<CommandDisabledPlayer> flaggedPlayers = new ArrayList<CommandDisabledPlayer>();
     private PropertiesFile propfile = new PropertiesFile("server.properties");
     private boolean allowToggle;
     private ArrayList<String> blockedCommands = new ArrayList<String>();
     private long commandBlockTime;
     private boolean defaultState;
     private ArrayList<String> exemptGroups = new ArrayList<String>();
 
     public void prepare() {
         String blockedCommandsString = new String();
         String[] blockedCommandsArray;
         blockedCommandsString = propfile.getString("pvp-disable-commands");
         blockedCommandsArray = blockedCommandsString.split(",");
         blockedCommands.addAll(Arrays.asList(blockedCommandsArray));
         commandBlockTime = propfile.getLong("pvp-disable-commands-time");
         defaultState = propfile.getBoolean("pvp-default-enabled");
         String exemptGroupsString = new String();
         String[] exemptGroupsArray;
         exemptGroupsString = propfile.getString("pvp-disable-commands-exempt-groups");
         exemptGroupsArray = exemptGroupsString.split(",");
         exemptGroups.addAll(Arrays.asList(exemptGroupsArray));
     }
 
    public void clean() {
         for(CommandDisabledPlayer player : flaggedPlayers) {
             if(player.isExpired()) {
                 flaggedPlayers.remove(player);
             }
         }
     }
 
     @Override
     public boolean onCommand(Player player, String[] split) {
         clean();
         String playerName = player.getName();
         long timeRemaining = CommandDisabledPlayer.isDisabled(playerName, flaggedPlayers);
         if(blockedCommands.contains(split[0]) && !isExempt(player) && timeRemaining != 0) {
             player.sendMessage(Colors.Rose + "You are blocked from that command from participating in PvP");
             if(timeRemaining > 60)
                 player.sendMessage(Colors.Rose + "Restrictions will be removed in " + timeRemaining / 60 + " minutes");
             else
                 player.sendMessage(Colors.Rose + "Restrictions will be removed in " + timeRemaining+ " seconds");
             return true;
         }
         if(split[0].equalsIgnoreCase("/pvp")) {
             if(player.canUseCommand("/pvp")) {
                 if(toggledUsers.contains(playerName)) {
                     toggledUsers.remove(playerName);
                     if(defaultState)
                         player.sendMessage("Your PvP flag is now " + Colors.LightGreen + "ON");
                     else
                         player.sendMessage("Your PvP flag is now " + Colors.Rose + "OFF");
                 } else {
                     toggledUsers.add(playerName);
                     if(defaultState)
                         player.sendMessage("Your PvP flag is now " + Colors.Rose + "OFF");
                     else
                         player.sendMessage("Your PvP flag is now " + Colors.LightGreen + "ON");
                 }
             } else
                 player.sendMessage("You are not allowed to change your PvP flag");
             return true;
         }
         return false;
     }
 
     @Override
     public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
        if(attacker == null || defender == null)
            return false;
         if(!attacker.isPlayer() || !defender.isPlayer())
             return false;
         Player attackerPlayer = attacker.getPlayer();
         Player defenderPlayer = defender.getPlayer();
         String attackerName = attackerPlayer.getName();
         String defenderName = defenderPlayer.getName();
         if(defaultState) {
             if(toggledUsers.contains(attackerName) || toggledUsers.contains(defenderName))
                 return true;
         } else {
             if(!toggledUsers.contains(attackerName) || !toggledUsers.contains(defenderName))
                 return true;
         }
 
         if(!CommandDisabledPlayer.resetTime(attackerName, (System.currentTimeMillis()/1000 + commandBlockTime), flaggedPlayers)) {
             CommandDisabledPlayer attackerCDP = new CommandDisabledPlayer(attackerName, (System.currentTimeMillis()/1000 + commandBlockTime));
             flaggedPlayers.add(attackerCDP);
         }
 
         if(!CommandDisabledPlayer.resetTime(defenderName, (System.currentTimeMillis()/1000 + commandBlockTime), flaggedPlayers)) {
             CommandDisabledPlayer defenderCDP = new CommandDisabledPlayer(defenderName, (System.currentTimeMillis()/1000 + commandBlockTime));
             flaggedPlayers.add(defenderCDP);
         }
 
         attackerPlayer.sendMessage("You attacked " + defenderName);
         defenderPlayer.sendMessage("You were attacked by " + attackerName);
 
         return false;
     }
 
     @Override
     public boolean onHealthChange(Player player, int oldValue, int newValue) {
         if(newValue <= 0) {
             int index = 0;
             for(CommandDisabledPlayer playerCDP : flaggedPlayers) {
                 if(playerCDP.getName().equals(player.getName())) {
                     flaggedPlayers.remove(index);
                     player.sendMessage("Flag removed due to death");
                 }
                 index++;
             }
         }
         return false;
     }
 
     private boolean isExempt(Player player) {
         for(String group : exemptGroups) {
             if(player.isInGroup(group))
                 return true;
         }
         return false;
     }
 }

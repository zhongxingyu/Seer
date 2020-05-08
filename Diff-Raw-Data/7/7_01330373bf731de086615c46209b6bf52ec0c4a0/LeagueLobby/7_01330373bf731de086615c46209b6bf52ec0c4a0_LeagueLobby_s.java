 package com.therhythmnjazz.league;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 
 public class LeagueLobby {
 
     private final String lobbyName;
     private final String matchType;
 
     private HashMap <String, Integer> activePlayers = new HashMap <String, Integer>();
     private HashMap <String, Integer> teamOnePlayers = new HashMap <String, Integer>();
     private HashMap <String, Integer> teamTwoPlayers = new HashMap <String, Integer>();
 
     private Integer teamOnePlayerCount;
     private Integer teamTwoPlayerCount;
     private League plugin;
 
     private Integer currentPlayers;
     private boolean inProgress;
 
 
     public LeagueLobby(League plugin, String lobbyName, String matchType) {
         this.plugin = plugin;
         this.lobbyName = lobbyName;
         this.matchType = matchType;
     }
 
     public void addPlayerToLobby(String playerName) {
         sendMessageToAllPlayers(playerName + " has joined the lobby.");
         currentPlayers = currentPlayers + 1;
         activePlayers.put(playerName, currentPlayers);
         if (!(inProgress)) {
             runPreGameCountCheck();
         }
     }
 
     private void sendMessageToAllPlayers(String s) {
         for (String player : activePlayers.keySet()) {
             Player p = Bukkit.getServer().getPlayer(player);
            p.sendMessage(this.plugin.getChatTemplate() + s);
         }
     }
 
     public void removePlayerFromLobby(String playerName) {
 
     }
 
     public void kickPlayerFromLobby(String playerName) {
 
     }
 
     public void banPlayerFromLobby(String playerName) {
 
     }
 
     public void runPreGameCountCheck() {
         if (matchType == "Twisted") {
             if (teamOnePlayers.size() == 3 && teamTwoPlayers.size() == 3) {
                 beginChampionSelect();
             }
         } else if (matchType == "Rift") {
             if (teamOnePlayers.size() == 5 && teamTwoPlayers.size() == 5) {
                 beginChampionSelect();
             }
         }
     }
 
     private void beginChampionSelect() {
         sendMessageToAllPlayers("Summoners, select your champions!");
 
         // TODO : Add menu for selecting champions
     }
 
     public void runInGameCountCheck() {
 
     }
 
     public void beginPreGameCountdown() {
 
     }
 
     public void teleportToArena() {
 
     }
 
     public void teleportToLobby() {
 
     }
 
     public void randomTeamSelect(String playerName) {
 
         if (!(teamOnePlayers.size() == 3)) {
 
             addTeam(playerName, 1);
 
         } else if (!(teamTwoPlayers.size() == 3)) {
 
             addTeam(playerName, 2);
 
         }
     }
 
     public void addTeam(String playerName, int team) {
 
         switch (team) {
 
             case 1:
             {
                 teamOnePlayers.put(playerName, teamOnePlayerCount);
                 break;
             }
 
             case 2:
             {
                 teamOnePlayers.put(playerName, teamTwoPlayerCount);
                 break;
             }
         }
     }
 
     public void sendMessageToPlayer(String playerName, String message) {
         Player player = Bukkit.getServer().getPlayer(playerName);
 
        player.sendMessage(this.plugin.getChatTemplate() + message);
 
         player = null;
     }
 
     public void swapTeam(String playerName, int team) {
 
         switch (team) {
 
             case 1:
             {
                 if (matchType == "Twisted") {
 
                     if (teamOnePlayers.size() == 3) {
 
                         sendMessageToPlayer(playerName, "This team is full.");
 
                     } else {
 
                         addTeam(playerName, 1);
 
                     }
 
                 } else if (matchType == "Rift") {
 
                     if (teamOnePlayers.size() == 5) {
 
                         sendMessageToPlayer(playerName, "This team is full.");
 
                     } else {
 
                         addTeam(playerName, 1);
 
                     }
 
                 }
                 break;
             }
 
             case 2:
             {
                 if (matchType == "Twisted") {
 
                     if (teamOnePlayers.size() == 3) {
 
                         sendMessageToPlayer(playerName, "This team is full.");
 
                     } else {
 
                         addTeam(playerName, 2);
 
                     }
 
                 } else if (matchType == "Rift") {
 
                     if (teamOnePlayers.size() == 5) {
 
                         sendMessageToPlayer(playerName, "This team is full.");
 
                     } else {
 
                         addTeam(playerName, 2);
 
                     }
 
                 }
                 break;
             }
 
         }
     }
 
     public void beginSpawnMinions() {
 
     }
 
     public void beginSpawnSuperMinions(String whichTeam) {
 
     }
 }

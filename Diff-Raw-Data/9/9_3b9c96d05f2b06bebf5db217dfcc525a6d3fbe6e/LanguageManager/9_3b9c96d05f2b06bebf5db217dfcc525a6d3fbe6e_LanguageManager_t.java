 /*
 * Copyright (c) 2013, LankyLord
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
 package net.lankylord.simplehomes.managers.languages;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 
 /** @author LankyLord */
 public class LanguageManager {
 
     public static String HOME_DELETED = ChatColor.YELLOW + "Home deleted.";
     public static String HOME_LIST_PREFIX = ChatColor.YELLOW + "Homes:";
     public static String HOME_NOT_FOUND = ChatColor.RED + "Home not found.";
     public static String HOME_NOT_SET = ChatColor.RED + "Home cannot be set. The maximum number of homes has been " +
             "reached";
     public static String HOME_SET = ChatColor.YELLOW + "Home set.";
     public static String NO_HOMES_FOUND = ChatColor.RED + "No homes found.";
     public static String PLAYER_COMMAND_ONLY = ChatColor.RED + "Only players may issue that command.";
     public static String TELEPORT_OTHERHOME = ChatColor.YELLOW + "Teleported to %p's home.";
     public static String TELEPORT_SUCCESS = ChatColor.YELLOW + "Teleported.";
 
     private LanguageFileManager fileManager;
     private FileConfiguration languageConfig;
 
     public LanguageManager(LanguageFileManager fileManager) {
         this.fileManager = fileManager;
        languageConfig = fileManager.getLanguageConfig();
         loadMessages();
     }
 
     private String convertColours(String string) {
         return ChatColor.translateAlternateColorCodes('&', string);
     }
 
     private void loadMessages() {
         HOME_DELETED = languageConfig.getString("home-deleted");
         HOME_LIST_PREFIX = languageConfig.getString("home-list-prefix");
         HOME_NOT_FOUND = languageConfig.getString("home-not-found");
         HOME_NOT_SET = languageConfig.getString("home-max-reached");
         HOME_SET = languageConfig.getString("home-set");
         NO_HOMES_FOUND = languageConfig.getString("no-homes-found");
         PLAYER_COMMAND_ONLY = languageConfig.getString("player-command-only");
         TELEPORT_OTHERHOME = languageConfig.getString("teleport-otherhome");
         TELEPORT_SUCCESS = languageConfig.getString("teleport-success");
     }
 }

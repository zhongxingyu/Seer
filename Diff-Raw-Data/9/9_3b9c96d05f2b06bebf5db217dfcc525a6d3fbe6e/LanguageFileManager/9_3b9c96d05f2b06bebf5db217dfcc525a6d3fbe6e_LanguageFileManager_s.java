 /*
  * Copyright (c) 2013, LankyLord
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
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
 
 import net.lankylord.simplehomes.SimpleHomes;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 /** @author LankyLord */
 public class LanguageFileManager {
 
     private final SimpleHomes instance;
     private FileConfiguration languageConfig = null;
     private File languageFile = null;
 
     public LanguageFileManager(SimpleHomes instance) {
        saveDefaultLanguages();
         this.instance = instance;
     }
 
     public FileConfiguration getLanguageConfig() {
         if (languageConfig == null) {
             this.reloadLanguages();
         }
         return languageConfig;
     }
 
     void reloadLanguages() {
         if (languageFile == null) {
             languageFile = new File(instance.getDataFolder(), "languages.yml");
         }
         languageConfig = YamlConfiguration.loadConfiguration(languageFile);
 
         InputStream defLanguageConfig = instance.getResource("languages.yml");
         if (defLanguageConfig != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defLanguageConfig);
             languageConfig.setDefaults(defConfig);
         }
     }
 
     public void saveLanguages() {
         if (languageConfig == null || languageFile == null) {
             return;
         }
         try {
             getLanguageConfig().save(languageFile);
         } catch (IOException ex) {
             instance.getLogger().log(Level.SEVERE, "Could not save languages file to " + languageFile, ex);
         }
     }
 
     public void saveDefaultLanguages() {
         if (languageFile == null) {
             languageFile = new File(instance.getDataFolder(), "languages.yml");
         }
         if (!languageFile.exists()) {
             instance.saveResource("languages.yml", false);
         }
     }
 }

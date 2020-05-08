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
 package net.lankylord.booksave;
 
 import com.pneumaticraft.commandhandler.CommandHandler;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.lankylord.booksave.commands.*;
 import net.lankylord.booksave.listeners.LoginListener;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 /**
  *
  * @author LankyLord
  */
 public class BookSave extends JavaPlugin {
     
     static final Logger logger = Logger.getLogger("Minecraft");
     private BookManager bookManager;
     private CommandHandler commandHandler;
     
     public BookSave() {
         this.bookManager = new BookManager(this);
     }
     
     @Override
     public void onEnable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         logger.log(Level.INFO, "[BookSave] BookSave v{0} Enabled.", pdfFile.getVersion());
         
         getConfig().options().copyDefaults(true);
         saveDefaultConfig();
         saveConfig();
         registerCommands();
         registerListeners();
         loadMetrics();
         bookManager.updateBookList();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         List<String> allArgs = new ArrayList<>();
         allArgs.addAll(Arrays.asList(args));
         allArgs.add(0, label);
         return commandHandler.locateAndRunCommand(sender, allArgs);
     }
     
     private void registerCommands() {
         PermissionsModule pm = new PermissionsModule();
         commandHandler = new CommandHandler(this, pm);
         commandHandler.registerCommand(new SaveCommand(this));
         commandHandler.registerCommand(new GiveCommand(this));
         commandHandler.registerCommand(new ListCommand(this));
         commandHandler.registerCommand(new RemoveCommand(this));
         commandHandler.registerCommand(new UnsignCommand(this));
     }
     
     private void registerListeners() {
         getServer().getPluginManager().registerEvents(new LoginListener(this), this);
     }
     
     private void loadMetrics() {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             logger.log(Level.WARNING, "Failed to submit stats");
         }
     }
     
     public BookManager getManager() {
         return this.bookManager;
     }
 }

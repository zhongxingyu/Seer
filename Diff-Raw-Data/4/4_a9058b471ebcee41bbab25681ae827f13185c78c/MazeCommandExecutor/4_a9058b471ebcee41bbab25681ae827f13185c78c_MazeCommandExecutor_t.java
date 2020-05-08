 /**
  * Copyright 2012 Laubi
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 package me.Laubi.MineMaze;
 
 import com.sk89q.worldedit.LocalPlayer;
 import java.lang.reflect.Method;
 import me.Laubi.MineMaze.Exceptions.ConsoleForbiddenException;
 import me.Laubi.MineMaze.Exceptions.PermissionException;
 import me.Laubi.MineMaze.Exceptions.SubCommandNotFoundException;
 import me.Laubi.MineMaze.Interfaces.SubCommand;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author Laubi
  */
 public class MazeCommandExecutor implements CommandExecutor{
     private MineMazePlugin plugin;
 
     public MazeCommandExecutor(MineMazePlugin plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
         LocalPlayer player = plugin.getWorldEditPlugin().wrapCommandSender(cs);
         try{
             CommandHandler cmdHandler = new CommandHandler(strings);
 
             Method subCommand = this.plugin.getSubCommandByAlias(cmdHandler.getSubCommand());
             
             if(subCommand == null){
                 throw new SubCommandNotFoundException(cmdHandler.getSubCommand());
             }
            SubCommand subCmd = subCommand.getAnnotation(SubCommand.class);
            
             if(!player.isPlayer() && !subCmd.console()){
                 throw new ConsoleForbiddenException();
             }
             
             if(!player.hasPermission(subCmd.permission())){
                 throw new PermissionException(subCmd.permission());
             }
             
             subCommand.invoke(null, player, cmdHandler, plugin);
         }catch(PermissionException e){
             player.printError("You are not allowed to use this command!");
         }catch(ConsoleForbiddenException e){
             player.printError("Only a player is allowed to use this command!");
         }catch(SubCommandNotFoundException e){
             player.printError("Couldn't find the subcommand '" + e.getSubCommand()+"'!");
         }catch(Throwable e){
             player.printError("Please report this error: [See console]");
             player.printRaw(e.getClass().getName() + ": " + e.getMessage());
             e.printStackTrace();
         }
         
         return true;
     }
     
 }

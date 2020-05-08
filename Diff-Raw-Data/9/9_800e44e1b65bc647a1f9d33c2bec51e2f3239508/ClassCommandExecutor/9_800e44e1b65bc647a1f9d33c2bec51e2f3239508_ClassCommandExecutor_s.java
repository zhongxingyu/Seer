 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.legendsdev.legends.bukkit.core.commands;
 
 import com.github.legendsdev.legends.library.lclass.LClass;
 import com.github.legendsdev.legends.library.lclass.LClassHandler;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import java.util.ArrayList;
 
 /**
  * @author B2OJustin
  */
 public class ClassCommandExecutor implements CommandExecutor {
     @SuppressWarnings("ConstantConditions")
     @Override
     public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
         if(!command.getName().equalsIgnoreCase("class")) return false;
         if(params.length == 0) return false;
 
         switch(params[0].toLowerCase()) {
             case "info":
                 if(params.length < 2) return false;
                 LClass lClass = LClassHandler.getInstance().load(params[1]);
                 if(lClass != null) {
                     ArrayList<String> description = lClass.getDescription();
                     commandSender.sendMessage(description.toArray(new String[description.size()]));
                 }
                 else {
                    commandSender.sendMessage("Sorry, we don't have any class named '" + params[1]+ "'");
                 }
                 break;
         }
 
         return true;
     }
 }

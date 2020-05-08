 /* cPlugin
  * Copyright (C) 2013 Norbert Kawinski (norbert.kawinski@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package castro.base.plugin;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import castro.base.GenericCommandMgr;
 
 public abstract class CBasePlugin extends JavaPlugin
 {
 	public static CPlugin baseinstance;
 	protected GenericCommandMgr commandMgr;
 	
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
		return commandMgr.onCommand(sender, command, args);
 	}
 	
 	
 	public String getPdf()
 	{
 		return "&f[&9" + getDescription().getName() + "&f] ";
 	}
 	
 	
 	public static String joinArgs(String[] array)
 	{ return joinArgs(array, 0); }
 	public static String joinArgs(String[] array, int start)
 	{
 		String ret = "";
 		if(array.length == 0)//<= start)
 			return "";
 		for(int i=start; i < array.length; ++i)
 			ret += array[i] + " ";
 		return ret.substring(0, ret.length()-1);
 	}
 }

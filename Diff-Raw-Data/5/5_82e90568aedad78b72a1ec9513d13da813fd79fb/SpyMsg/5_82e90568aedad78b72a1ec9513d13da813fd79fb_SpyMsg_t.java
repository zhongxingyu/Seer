 /************************************************************************
  * This file is part of AdminCmd.									
  *																		
  * AdminCmd is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by	
  * the Free Software Foundation, either version 3 of the License, or		
  * (at your option) any later version.									
  *																		
  * AdminCmd is distributed in the hope that it will be useful,	
  * but WITHOUT ANY WARRANTY; without even the implied warranty of		
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
  * GNU General Public License for more details.							
  *																		
  * You should have received a copy of the GNU General Public License
  * along with AdminCmd.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 package be.Balor.Manager.Commands.Player;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import be.Balor.Manager.Commands.CommandArgs;
 import be.Balor.Manager.Commands.CoreCommand;
 import be.Balor.Player.ACPlayer;
 import be.Balor.Tools.Type;
 import be.Balor.Tools.Utils;
 import be.Balor.bukkit.AdminCmd.ACHelper;
 
 /**
  * @author Balor (aka Antoine Aflalo)
  * 
  */
 public class SpyMsg extends CoreCommand {
 
 	/**
 	 * 
 	 */
 	public SpyMsg() {
 		permNode = "admincmd.player.spymsg";
 		cmdName = "bal_spymsg";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * be.Balor.Manager.ACCommands#execute(org.bukkit.command.CommandSender,
 	 * java.lang.String[])
 	 */
 	@Override
 	public void execute(CommandSender sender, CommandArgs args) {
 		if (Utils.isPlayer(sender)) {
 			ACPlayer acp = ACPlayer.getPlayer(((Player) sender).getName());
 			if (acp.hasPower(Type.SPYMSG)) {
 				acp.removePower(Type.SPYMSG);
				ACHelper.getInstance().removeSpy((Player) sender);
 				Utils.sI18n(sender, "spymsgDisabled");
 			} else {
 				acp.setPower(Type.SPYMSG);
				ACHelper.getInstance().addSpy((Player) sender);
 				Utils.sI18n(sender, "spymsgEnabled");
 			}
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see be.Balor.Manager.ACCommands#argsCheck(java.lang.String[])
 	 */
 	@Override
 	public boolean argsCheck(String... args) {
 		return true;
 	}
 
 }

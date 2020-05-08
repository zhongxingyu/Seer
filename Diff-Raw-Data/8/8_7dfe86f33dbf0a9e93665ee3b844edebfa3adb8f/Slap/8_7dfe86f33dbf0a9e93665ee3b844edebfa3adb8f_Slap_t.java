 /************************************************************************
  * This file is part of FunCommands.
  *
  * FunCommands is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with FunCommands.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.Commands;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 import de.Lathanael.FunCommands.Configuration;
 
 import be.Balor.Manager.Commands.CommandArgs;
 import be.Balor.Manager.Commands.CoreCommand;
 import be.Balor.Tools.Utils;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class Slap extends CoreCommand {
 
 	/**
 	 *
 	 */
 	public Slap() {
 		super("ac_slap", "admincmd.fun.slap", "FunCommands");
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
 		Random random = new Random();
 		random.nextInt(10);
 		Player target;
 
 		target = Utils.getUser(sender, args, permNode, 0, true);
 		if (target == null)
 			return;
 
 		float power = 0;
 		float height = 0;
 		HashMap<String, String> replace = new HashMap<String, String>();
 		replace.put("target", target.getName());
 		if (Utils.isPlayer(sender, false))
 			replace.put("sender", sender.getName());
 		else
 			replace.put("sender", "Server Admin");
 		Vector direction = target.getLocation().getDirection();
 		if (args.hasFlag('h')) {
 			power = Configuration.getInstance().getConfFloat("Slap.hPower");
 			height = Configuration.getInstance().getConfFloat("Slap.hHeight");
 			if (power > 10) {
 				power = 10;
 				Configuration.getInstance().setConfProperty("Slap.hPower", 10);
 			}
 			if (height > 10) {
 				height = 10;
 				Configuration.getInstance().setConfProperty("Slap.hHeight", 10);
 			}
 			direction = new Vector(direction.getX()*power, height, direction.getZ()*power);
 			target.setVelocity(direction);
 		}
 		else if (args.hasFlag('v')) {
 			power = Configuration.getInstance().getConfFloat("Slap.vPower");
 			height = Configuration.getInstance().getConfFloat("Slap.vHeight");
 			if (power > 10) {
 				power = 10;
 				Configuration.getInstance().setConfProperty("Slap.vPower", 10);
 			}
 			if (height > 10) {
 				height = 10;
 				Configuration.getInstance().setConfProperty("Slap.vHeight", 10);
 			}
 			direction = new Vector(direction.getX()*power, height, direction.getZ()*power);
 			target.setVelocity(direction);
 		}
 		else {
 			power = Configuration.getInstance().getConfFloat("Slap.normalPower");
 			height = Configuration.getInstance().getConfFloat("Slap.normalHeight");
 			if (power > 10) {
 				power = 10;
 				Configuration.getInstance().setConfProperty("Slap.normalPower", 10);
 			}
 			if (height > 10) {
 				height = 10;
 				Configuration.getInstance().setConfProperty("Slap.normalHeight", 10);
 			}
 			direction = new Vector(direction.getX()*power, height, direction.getZ()*power);
 			target.setVelocity(direction);
 		}
 
 		if (!target.equals(sender)) {
 			Utils.sI18n(target, "slapTarget", replace);
 			Utils.sI18n(sender, "slapSender", replace);
 		} else {
 			Utils.sI18n(sender, "slapYourself");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see be.Balor.Manager.ACCommands#argsCheck(java.lang.String[])
 	 */
 	@Override
 	public boolean argsCheck(String... args) {
 		return args != null && args.length >= 1;
 	}
 }

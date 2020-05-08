 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bukkitdev.fixstrength;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  *
  * @author daboross
  */
 public class FixStrength extends JavaPlugin implements Listener {
 
 	private static final double CONSTANT_TIMES = 10;
 	private static final double CONSTANT_DIVIDE = 13;
 	private static final double CONSTANT_PLUS = 1.5;
	private static final double CONSTANT_PLUS_2 = 0.5;
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this, this);
 	}
 
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		sender.sendMessage("FixStrength doesn't know about the command /" + cmd);
 		return true;
 	}
 
 	@EventHandler
 	public void onHit(EntityDamageByEntityEvent evt) {
 		if (!evt.isCancelled()) {
 			if (evt.getDamager() instanceof Player) {
 				Player player = (Player) evt.getDamager();
 				for (PotionEffect potionEffect : player.getActivePotionEffects()) {
 					if (potionEffect.getType() == PotionEffectType.INCREASE_DAMAGE) {
						evt.setDamage((evt.getDamage() * CONSTANT_TIMES / (CONSTANT_DIVIDE * potionEffect.getAmplifier()))
								+ CONSTANT_PLUS + (CONSTANT_PLUS_2 * potionEffect.getAmplifier()));
 					}
 				}
 			}
 		}
 	}
 }

 /*
  * This file is part of mmoParty <http://github.com/mmoMinecraftDev/mmoParty>.
  *
  * mmoParty is free software: you can redistribute it and/or modify
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
 package mmo.Party;
 
 import mmo.Core.DamageAPI.MMODamageEvent;
 
import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 public class PartyDamage implements Listener {
 	MMOParty plugin;
 
 	PartyDamage(MMOParty plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onMMOPVPDamage(MMODamageEvent event) {
		 Entity attacker = event.getAttacker();
		 Entity defender = event.getDefender();
		
		 if(!(attacker instanceof Player) || !(defender instanceof Player)) {
			 return;
		 }
			 
 		if (MMOParty.config_no_party_pvp && PartyAPI.instance.find((Player) event.getAttacker()).contains((Player) event.getDefender())) {
 			if (MMOParty.config_no_party_pvp_quiet) {
 				plugin.sendMessage((Player) event.getAttacker(), "Can't attack your own party!");
 			}
 			event.setCancelled(true);
 		}
 	}
 }

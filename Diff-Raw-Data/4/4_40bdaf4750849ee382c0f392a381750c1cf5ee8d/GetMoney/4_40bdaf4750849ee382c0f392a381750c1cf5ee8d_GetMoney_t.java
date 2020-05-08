 /******************************************************************************\
 |                                     ,,                                       |
 |                    db             `7MM                                       |
 |                   ;MM:              MM                                       |
 |                  ,V^MM.    ,pP"Ybd  MMpMMMb.  .gP"Ya `7Mb,od8                |
 |                 ,M  `MM    8I   `"  MM    MM ,M'   Yb  MM' "'                |
 |                 AbmmmqMA   `YMMMa.  MM    MM 8M""""""  MM                    |
 |                A'     VML  L.   I8  MM    MM YM.    ,  MM                    |
 |              .AMA.   .AMMA.M9mmmP'.JMML  JMML.`Mbmmd'.JMML.                  |
 |                                                                              |
 |                                                                              |
 |                                ,,    ,,                                      |
 |                     .g8"""bgd `7MM    db        `7MM                         |
 |                   .dP'     `M   MM                MM                         |
 |                   dM'       `   MM  `7MM  ,p6"bo  MM  ,MP'                   |
 |                   MM            MM    MM 6M'  OO  MM ;Y                      |
 |                   MM.    `7MMF' MM    MM 8M       MM;Mm                      |
 |                   `Mb.     MM   MM    MM YM.    , MM `Mb.                    |
 |                     `"bmmmdPY .JMML..JMML.YMbmd'.JMML. YA.                   |
 |                                                                              |
 \******************************************************************************/
 /******************************************************************************\
 | Copyright (c) 2012, Asher Glick                                              |
 | All rights reserved.                                                         |
 |                                                                              |
 | Redistribution and use in source and binary forms, with or without           |
 | modification, are permitted provided that the following conditions are met:  |
 |                                                                              |
 | * Redistributions of source code must retain the above copyright notice,     |
 |   this list of conditions and the following disclaimer.                      |
 | * Redistributions in binary form must reproduce the above copyright notice,  |
 |   this list of conditions and the following disclaimer in the documentation  |
 |   and/or other materials provided with the distribution.                     |
 |                                                                              |
 | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  |
 | AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    |
 | IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   |
 | ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    |
 | LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          |
 | CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         |
 | SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     |
 | INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      |
 | CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      |
 | ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   |
 | POSSIBILITY OF SUCH DAMAGE.                                                  |
 \******************************************************************************/
 package iggy.Economy;
 
import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class GetMoney implements Listener{
 	Economy plugin;
 	GetMoney (Economy instance) {
 		plugin = instance;
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void breakMoney (BlockBreakEvent event) {
 		plugin.giveMoney(event.getPlayer(), 1);
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void placeMoney (BlockPlaceEvent event) {
 		plugin.giveMoney(event.getPlayer(), 1);
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void killMob (EntityDeathEvent event) {
 		LivingEntity killed = null;
 		if (event.getEntity() instanceof LivingEntity) {
 			killed = (LivingEntity) event.getEntity();
 		}
 		else {
 			//dont fucking care
 			return;
 		}
 		Player killer = killed.getKiller();
 		if (killer == null) {
 			// fuck that!
 			return;
 		}
 		// TODO: get money on entity death
		long price = plugin.creatureBounties.get((Entity)killed);
 		if (price > 0){
 			plugin.info("Entity has a bounty");
 			plugin.giveMoney(killer, price);
 		}
 	}
 }

 package com.adamki11s.npcs.triggers.action;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.adamki11s.questx.QuestX;
 
 public class LightningAction implements Action {
 
 	short strikes, range, tickDelay;
 
 	private boolean isActive = true, isDamaging = false;
 
 	private int taskID;
 
 	public LightningAction(String npc, String data) {
 		// <STRIKES>#<RANGE>#<TICK_DELAY>#damage player
 		String[] components = data.split("#");
 		try {
 			strikes = Short.parseShort(components[0]);
 			range = Short.parseShort(components[1]);
 			tickDelay = Short.parseShort(components[2]);
 			isDamaging = Boolean.parseBoolean(components[3]);
 		} catch (NumberFormatException nfe) {
 			isActive = false;
			QuestX.logError("Error parsing value for 'LIGHTNING' for NPC '" + npc + "' in custom_trigger. Setting disabled");
 		} catch (Exception e) {
 			isActive = false;
			QuestX.logError("Invalid parameters for 'LIGHTNING' for NPC '" + npc + "' in custom_trigger. Setting disabled.");
 		}
 	}
 
 	@Override
 	public void implement(final Player p) {
 		if(taskID != 0){
 			return;
 		}
 			
 		final Location c = p.getLocation();
 		taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(QuestX.p, new Runnable() {
 
 			short sCount;
 			Random r = new Random();
 
 			public void run() {
 				sCount++;
 				if (sCount <= strikes) {
 					short xO = (short) (r.nextInt(range * 2) - range), yO = (short) (r.nextInt(range * 2) - range), zO = (short) (r.nextInt(range * 2) - range);
 					Location hit = new Location(c.getWorld(), c.getX() + xO, c.getY() + yO, c.getZ() + zO);
 					if (isDamaging) {
 						hit.getWorld().strikeLightning(hit);
 					} else {
 						hit.getWorld().strikeLightningEffect(hit);
 					}
 				} else {
 					Bukkit.getServer().getScheduler().cancelTask(taskID);
 					taskID = 0;
 				}
 			}
 
 		}, tickDelay, tickDelay);
 	}
 
 	@Override
 	public boolean isActive() {
 		return this.isActive;
 	}
 
 }

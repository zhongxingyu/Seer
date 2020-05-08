 package me.patrickfreed.EconomyPunga;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class EconomyPungaEventListener implements Listener{
 
 	public static HashMap<String, String> data = new HashMap<String, String>();
 
 	@EventHandler
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {    
 	    if(event.isCancelled()) return;
 		if(!(event.getEntity() instanceof Player)) return;
 		
 		Player victim = (Player)event.getEntity();
 			
 		if(event.getCause().toString().equals("ENTITY_ATTACK")){
 			if(event.getDamager() instanceof Player){
 				Player pvperPlayer = (Player) event.getDamager();
 				String pvper = pvperPlayer.getName();
 				data.put(victim.getName(), pvper);
 			}else if(event.getDamager() instanceof Monster){
 				//TODO: Other entities
 			}else{
 				data.put(victim.getName(), null);
 			}
 		}else if(event.getCause() == DamageCause.PROJECTILE){
 		    if(event.getDamager() instanceof Arrow){
		        Arrow arrow = (Arrow)event.getDamager();
		        if(arrow.getShooter() instanceof Player){
		        	Player pvperPlayer = (Player)arrow.getShooter();
		        	String pvper = pvperPlayer.getName();
		        	data.put(victim.getName(), pvper);
		        }
 		    }
 		}else{
 			data.put(victim.getName(), null);
 		}
 	}
 
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		
 		if(!(event.getEntity() instanceof Player))return;
 		
 		Player victim = (Player)event.getEntity();
 		if(data.get(victim.getName()) == null) return;
 
 		String pvper = data.get(victim.getName());
 		Player pvperPlayer = Bukkit.getServer().getPlayer(pvper);
 		            
 			if ((victim.hasPermission("economypunga.use") || victim.isOp() || victim.hasPermission("economyPunga.victim")) && (pvperPlayer.hasPermission("economypunga.use") || pvperPlayer.hasPermission("EconomyPunga.killer") || pvperPlayer.isOp())){
 
 				double victimBalance = EconomyPunga.economy.getBalance(victim.getName());
 				
 				double percentage;
 				double amount;
 
 				if (Config.getMode().compareToIgnoreCase("random percentage") == 0) {
 					percentage = getRandomPercentage();
 					amount = (percentage * victimBalance) / 100;
 				} else if (Config.getMode().compareToIgnoreCase("static percentage") == 0) {
 					percentage = Config.getStaticPercentage();
 					amount = (percentage * victimBalance) / 100;
 				} else if (Config.getMode().compareToIgnoreCase("Static Amount") == 0) {
 					amount = Config.getStaticAmount();
 				} else if (Config.getMode().compareToIgnoreCase("Random Amount") == 0) {
 					amount = getRandomAmount();
 				} else {
 					System.err.println("Invalid Mode, defaulting to static amount of 1.");
 					amount = 1;
 				}
 
 				if (victimBalance > amount) {
 					EconomyPunga.economy.withdrawPlayer(victim.getName(), amount);
 					EconomyPunga.economy.depositPlayer(pvper, amount);
 				} else if (victimBalance > 0) {
 					EconomyPunga.economy.depositPlayer(pvper, victimBalance);
 					amount = victimBalance;
 					EconomyPunga.economy.withdrawPlayer(victim.getName(), victimBalance);
 				} else {
 					amount = 0;
 				}
 
 				String Attacker = Config.getKillMsg().replace("%d", victim.getName());
 				String Dead = Config.getDeathmsg().replace("%d", victim.getName());
 
 				Attacker = Attacker.replace("%a", pvper);
 				Dead = Dead.replace("%a", pvper);
 
 				Attacker = Attacker.replace("%n", EconomyPunga.economy.format(amount));
 				Dead = Dead.replace("%n", EconomyPunga.economy.format(amount));
 
 				pvperPlayer.sendMessage(parseColors(Attacker));
 				victim.sendMessage(parseColors(Dead));
 			}
 		}
 
 	public String parseColors(String message){	
 		message = message.replace("&4", ChatColor.DARK_RED.toString());
 		message = message.replace("&c", ChatColor.RED.toString());
 		message = message.replace("&e", ChatColor.YELLOW.toString());
 		message = message.replace("&6", ChatColor.GOLD.toString());
 		message = message.replace("&2", ChatColor.DARK_GREEN.toString());
 		message = message.replace("&a", ChatColor.GREEN.toString());
 		message = message.replace("&b", ChatColor.AQUA.toString());
 		message = message.replace("&3", ChatColor.DARK_AQUA.toString());
 		message = message.replace("&1", ChatColor.DARK_BLUE.toString());
 		message = message.replace("&9", ChatColor.BLUE.toString());
 		message = message.replace("&d", ChatColor.LIGHT_PURPLE.toString());
 		message = message.replace("&5", ChatColor.DARK_PURPLE.toString());
 		message = message.replace("&f", ChatColor.WHITE.toString());
 		message = message.replace("&7", ChatColor.GRAY.toString());
 		message = message.replace("&8", ChatColor.DARK_GRAY.toString());
 		message = message.replace("&0", ChatColor.BLACK.toString());	
 		return message;	
 	}
 
 	public double getRandomAmount() {
 		Random generator = new Random();
 		double number = generator.nextInt(Config.getMaxA() + 1 - Config.getMinA()) + Config.getMinA();
 		return number;
 	}
 
 	public double getRandomPercentage() {
 		Random generator = new Random();
 		double number = generator.nextInt(Config.getMaxP() + 1- Config.getMinP())+ Config.getMinP();
 		return number;
 	}
 }

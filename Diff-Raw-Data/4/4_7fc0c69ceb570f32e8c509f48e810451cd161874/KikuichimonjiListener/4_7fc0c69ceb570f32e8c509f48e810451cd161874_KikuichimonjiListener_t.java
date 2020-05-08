 package com.cin316.minezweapons;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.cin316.minezweapons.MineZWeapons;
 
 public class KikuichimonjiListener implements Listener{
 	
 	MineZWeapons plugin;
 	
 	public KikuichimonjiListener(MineZWeapons plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event) {
 	    
 		Entity entity = event.getEntity();
 		//Check if the entity getting hit is a player
 		if (entity instanceof Player){
 			
 			//Create the variable for the player (getting hit)
 			Player hurted = (Player)entity;
 			//Check if the event is a Entity damage event (basically)
 			if (event instanceof EntityDamageByEntityEvent){
 				
 				//Get a variable for the cause of the damage
 				EntityDamageByEntityEvent damageCause = (EntityDamageByEntityEvent)event;
 				//Check if the damager is a player
 				if (damageCause.getDamager() instanceof Player){
 					
 					//Get the variable for the player who punched someone
 					Player hitter = (Player)damageCause.getDamager();
 					
 					//Check if the hitter is holding a Kikuichimonji and it is a wood sword.
 					if( hitter.getItemInHand().getType().equals(Material.WOOD_SWORD) ){
 						if( hitter.getItemInHand().getItemMeta().hasDisplayName() ){
 							if( hitter.getItemInHand().getItemMeta().getDisplayName().equals("\u00a7oKikuichimonji") ){
 							
 								//Generate a random number between 1 and 3.
 								Random rand = new Random();
 								int n = rand.nextInt(3) + 1;
 								Random rand2 = new Random();
 								int n2 = rand2.nextInt(3) + 1;
 								
 								if(n==1){
 									//Poison the hurted.
 									hurted.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1) );
 								}
 								if(n2==1){
									//Poison the hurter.
									hitter.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1) );
 								}
 							
 							}
 						}
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 		
 	}
 
 }

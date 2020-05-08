 package com.hotmail.joatin37.jprotection;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 import java.util.Vector;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
 
 public class EntityLock {
 	
 	private HashMap<UUID, EntityProtection> locks;
 	private JProtection jprotect;
 	
 	private FileConfiguration entityConfig = null;
 	private File entityConfigFile = null;
 
 	public EntityLock (JProtection jprotection){
 		jprotect=jprotection;
 		load();
 	}
 	
 	public void getInfo(Entity entity, Player player){
		EntityProtection lock = locks.get(entity.getUniqueId());
		player.sendMessage(ChatPaginator.wordWrap(jprotect.getConfig().getString("messages.entityinfo", "2Owner: 4[owner]\n2Protected: [protected]\n2Password: 4[password]\n2Friends: e[friendslist]").replace("[owner]", lock.getOwner()), 119));
		
 	}
 	
 	public boolean isLocked(Entity entity){
 		return locks.get(entity.getUniqueId())!=null;
 	}
 	
 	public boolean isOwner(Entity entity, String player){
 		return locks.get(entity.getUniqueId()).getOwner().equals(player);
 	}
 	
 	public boolean allowsInteraction(Entity entity, Player player){
 		if(locks.get(entity.getUniqueId())!=null){
 		return locks.get(entity.getUniqueId()).isAllowedUser(player.getName());
 		}else{
 			return true;
 		}
 	}
 	public void removelock(Entity entity){
 		locks.remove(entity.getUniqueId());
 	}
 	
 	public void unlock(Entity entity, Player player){
 		if(locks.get(entity.getUniqueId()).getOwner().equals(player.getName())||player.hasPermission("jprotection.admin.lockmaster")){
 			jprotect.getPlayerHandle(locks.get(entity.getUniqueId()).getOwner()).removeAmountlocks();
 			locks.remove(entity.getUniqueId());
 			player.sendMessage(jprotect.getConfig().getString("messages.yousuccesfullyromevedthislock", "eYou succesfully romeved this lock"));
 		}else{
 			player.sendMessage(jprotect.getConfig().getString("messages.youarenottheownerofthislock", "4You are not the owner of this lock!"));
 		}
 	}
 	
 	public void lock(Entity entity, Player commander, String player){
 		if(commander.hasPermission("jprotection.admin.lockmaster")){
 			if(jprotect.getConfig().getStringList("protection.entities").contains(Integer.toString(entity.getType().getTypeId()))){
 				locks.put(entity.getUniqueId(), new EntityProtection(entity, jprotect.getPlayerHandle(player)));
 				jprotect.getPlayerHandle(player).addAmountlocks();
 				jprotect.addTotalamountoflocks();
 				commander.sendMessage(jprotect.getConfig().getString("messages.successfullylockedthisentitytoplayer", "2Successfully locked this [entity] to [player]!").replace("[entity]", entity.getType().getName()).replace("[player]", player));
 			}else{
 				commander.sendMessage(jprotect.getConfig().getString("messages.thisentitytypecantbelocked", "4This entity type ([entity]) can't be locked!").replace("[entity]", entity.getType().getName()));
 			}
 		}else{
 			commander.sendMessage(jprotect.getConfig().getString("messages.youdonthavemasterlockpermission", "eYou don't have permission to create locks this way"));
 		}
 		}
 	
 	
 	public void lock(Entity entity, Player player){
 		if(jprotect.getConfig().getStringList("protection.entities").contains(Integer.toString(entity.getType().getTypeId()))){
 			if(player.hasPermission("jprotection.locker.entitylocker")){
 				if(locks.get(entity.getUniqueId())==null){
 					if(jprotect.econ!=null&&jprotect.getConfig().getDouble("price.entities."+entity.getType().getTypeId(), 0d)!=0d){						
 							EconomyResponse response = jprotect.econ.withdrawPlayer(player.getName(), jprotect.getConfig().getDouble("price.entities."+entity.getType().getTypeId()));
 							if(!response.transactionSuccess()){
 								player.sendMessage(jprotect.getConfig().getString("messages.failedtolockthisentity", "4Failed to lock this [entity]!").replace("[entity]", entity.getType().getName()));
 								player.sendMessage(response.errorMessage);
 							}else{
 								locks.put(entity.getUniqueId(), new EntityProtection(entity,jprotect.getPlayerHandle(player.getName())));
 								jprotect.getPlayerHandle(player.getName()).addAmountlocks();
 								jprotect.addTotalamountoflocks();
 								player.sendMessage(jprotect.getConfig().getString("messages.successfullylockedthisentity", "2Successfully locked this [entity]!").replace("[entity]", entity.getType().getName()));
 								player.sendMessage(jprotect.getConfig().getString("messages.amountwaswithdrawnfromyouracount", "e[amount] was withdrawn from your acount").replace("[amount]", ""+jprotect.getConfig().getDouble("price.blocks."+entity.getType().getTypeId())));
 							}
 					}else{
 						locks.put(entity.getUniqueId(), new EntityProtection(entity, jprotect.getPlayerHandle(player.getName())));
 						jprotect.addTotalamountoflocks();
 						jprotect.getPlayerHandle(player.getName()).addAmountlocks();
 						player.sendMessage(jprotect.getConfig().getString("messages.successfullylockedthisentity", "2Successfully locked this [entity]!").replace("[entity]", entity.getType().getName()));
 					}
 				}else{
 					player.sendMessage(jprotect.getConfig().getString("messages.thisentityisalreadylocked", "4This [entity] is already locked!").replace("[entity]", entity.getType().getName()));
 				}
 			}else{
 				player.sendMessage(jprotect.getConfig().getString("messages.youdonthavepermissiontomakelocks", "eYou don't have permission to create locks"));
 			}
 		}else{
 			player.sendMessage(jprotect.getConfig().getString("messages.thisentitytypecantbelocked", "4This entity type ([entity]) can't be locked!").replace("[entity]", entity.getType().getName()));
 		}
 	}
 	
 	
 	public void load() {
 	    if (entityConfigFile == null) {
 	    entityConfigFile = new File(jprotect.getDataFolder(), "entitysaves.sav");
 	    }
 	    entityConfig = YamlConfiguration.loadConfiguration(entityConfigFile);
 	    if(entityConfig.getStringList("uuids")==null)return;
 	    locks=new HashMap<UUID, EntityProtection>();
 	    Iterator<String> iterator = entityConfig.getStringList("uuids").listIterator();
 	    while(iterator.hasNext()){
 	    	EntityProtection e = new EntityProtection(iterator.next(), entityConfig);
 	    	locks.put(e.getuuid(), e);
 	    }
 	}
 	
 	public void save() {
 		if (entityConfig == null || entityConfigFile == null) {
 	    return;
 	    }    
 		Vector<String>list=new Vector<String>(100,100);
 		Iterator<EntityProtection> iterator = locks.values().iterator();
 		while(iterator.hasNext()){
 			list.add(iterator.next().save(entityConfig));
 		}
 		entityConfig.set("uuids", list);
 	    try {
 	    	entityConfig.save(entityConfigFile);
 	    	jprotect.getLogger().info("Succesfully saved entitysaves.sav");
 	    } catch (IOException ex) {
 	        jprotect.getLogger().log(Level.SEVERE, "Could not save config to " + entityConfigFile, ex);
 	    }
 	}
 	
 }

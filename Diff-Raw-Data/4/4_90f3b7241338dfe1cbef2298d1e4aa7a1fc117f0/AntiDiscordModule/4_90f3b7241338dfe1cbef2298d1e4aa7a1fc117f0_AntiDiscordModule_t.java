 package me.corriekay.pppopp3.modules;
 
 import java.util.HashSet;
 
 import me.corriekay.pppopp3.events.QuitEvent;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 import me.corriekay.pppopp3.utils.Utils;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class AntiDiscordModule extends PSCmdExe {
 	
 	private boolean lava = false;
 	private boolean water = false;
 	private int spawns = 10;
 	private HashSet<String> pvpBypass = new HashSet<String>();
 	
 	public AntiDiscordModule(){
 		super("AntiDiscordModule", "toggle");
 		try {
 			methodMap.put(EntityDamageByEntityEvent.class, this.getClass().getDeclaredMethod("pvpHandler", EntityDamageEvent.class));
 		} catch (Exception e) {}
 	}
 	public boolean isntPvp(World w){
 		w = Equestria.get().getParentWorld(w);
 		return !w.getName().equals("badlands");
 	}
 	@EventHandler
 	public void explosionNerf(EntityExplodeEvent event){
 		if(isntPvp(event.getEntity().getWorld())) event.blockList().clear();
 	}
 	@EventHandler
 	public void fireDamagePrevent(BlockBurnEvent event){
 		if(isntPvp(event.getBlock().getWorld())) event.setCancelled(true);
 	}
 	@EventHandler
 	public void fireSpreadControl(BlockSpreadEvent event){
 		if(isntPvp(event.getBlock().getWorld())&&event.getNewState().getType() == Material.FIRE) event.setCancelled(true);
 	}
 	@EventHandler
 	public void playerFireControl(PlayerInteractEvent event){
 		try{
 			if(isntPvp(event.getPlayer().getWorld())){
 				if(event.getItem().getType() == Material.FLINT_AND_STEEL){
 					if(!event.getPlayer().hasPermission("pppopp3.flintnsteel")){
 						if(event.getClickedBlock().getType() != Material.NETHERRACK){
 							event.setCancelled(true);
 						} else {
 							for(Block b : Utils.getBlocks(2, 2, 2, event.getClickedBlock().getLocation())){
 								if(b.getType() == Material.OBSIDIAN){
 									event.setCancelled(true);
 								}
 							}
 						}
 					}
 				}
 			}
 		} catch (NullPointerException e){
 			return;
 		}
 	}
	@EventHandler(priority = EventPriority.LOWEST)
 	public void liquidFlowEvent(BlockFromToEvent event){
 		if(event.getBlock().getType().equals(Material.STATIONARY_LAVA)){
 			event.setCancelled(lava);
 		} if(event.getBlock().getType().equals(Material.STATIONARY_WATER)){
 			event.setCancelled(water);
 		}
 	}
 	@EventHandler
 	public void onEggThrow(PlayerEggThrowEvent event){
 		if(isntPvp(event.getEgg().getWorld()))event.setHatching(false);
 	}
 	@EventHandler
 	public void onDispense(BlockDispenseEvent event){
 		if(isntPvp(event.getBlock().getWorld())&&event.getItem().getType() == Material.EGG) event.setCancelled(true);
 	}
 	@EventHandler
 	public void pvpHandler(EntityDamageEvent event){
 		if(event.getEntity() instanceof Creeper){
 			if(event.getCause() == DamageCause.ENTITY_EXPLOSION){
 				event.setCancelled(true);
 				return;
 			}
 		}
 		if(isntPvp(event.getEntity().getWorld())){
 			if(event instanceof EntityDamageByEntityEvent){
 				EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)event;
 				Player target, damager;
 				if(edbee.getEntity() instanceof Player){
 					target = (Player)edbee.getEntity();
 					if(edbee.getDamager() instanceof Player){
 						damager = (Player)edbee.getDamager();
 					} else if(edbee.getDamager() instanceof Projectile){
 						Projectile proj = (Projectile)edbee.getDamager();
 						if(proj.getShooter() instanceof Player){
 							damager = (Player)proj.getShooter();
 						} else return;
 					} else return;
 				} else return;
 				if(target != null && damager != null){
 					event.setCancelled(true);
 					if(pvpBypass.contains(damager.getName())){
 						target.damage(event.getDamage());
 					}
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void creatureSpawn(CreatureSpawnEvent event){
 		if (event.getLocation().getWorld().getEnvironment() == World.Environment.NETHER&&event.getSpawnReason() == SpawnReason.NATURAL) {
 			spawns--;
 			if (spawns == 0) {
 				spawns = 10;
 				event.setCancelled(true);
 				event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.BLAZE);
 			}
 		}
 	}
 	@EventHandler
 	public void quit(QuitEvent event){
 		if(event.isQuitting()){
 			pvpBypass.remove(event.getPlayer().getName());
 		}
 	}
 	@EventHandler
 	public void portalcreate(EntityCreatePortalEvent event){
 		if(!isntPvp(event.getEntity().getWorld()))return;
 		if(event.getEntity() instanceof Player){
 			Player p = (Player)event.getEntity();
 			if(!p.hasPermission("pppopp3.flintnsteel")){
 				event.setCancelled(true);
 				return;
 			}
 		} else {
 			event.setCancelled(true);
 		}
 	}
 	public boolean handleCommand(CommandSender sender, Command cmd, String label,String[] args){
 		if(cmd.getName().equals("toggle")){
 			if(args.length<1){
 				sendMessage(sender,notEnoughArgs);
 			} else {
 				if(args[0].equals("lava")){
 					toggleLavaFlow(sender);
 				} else if(args[0].equals("water")){
 					toggleWaterFlow(sender);
 				} else if(args[0].equals("pvp")){
 					Player player;
 					if(sender instanceof Player){
 						player = (Player)sender;
 					} else {
 						sendMessage(sender,notPlayer);
 						return true;
 					}
 					String name = player.getName();
 					if(pvpBypass.contains(name)){
 						pvpBypass.remove(name);
 						sendMessage(player,"Violence cancelled!");
 						return true;
 					} else {
 						pvpBypass.add(name);
 						sendMessage(player,"LET THE RIVERS FLOW WITH THE BLOOD OF THE INNOCENT");
 						return true;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	public void toggleWaterFlow(CommandSender player){
 		water = !water;
 		if(!water){
 			player.sendMessage(pinkieSays+"Let there be water!");
 		} else player.sendMessage(pinkieSays+"The water party is over!");
 	}
 	public void toggleLavaFlow(CommandSender player){
 		lava = !lava;
 		if(!lava){
 			player.sendMessage(pinkieSays+"Let there be lava!");
 		} else player.sendMessage(pinkieSays+"The lava party is over!");
 	}
 }

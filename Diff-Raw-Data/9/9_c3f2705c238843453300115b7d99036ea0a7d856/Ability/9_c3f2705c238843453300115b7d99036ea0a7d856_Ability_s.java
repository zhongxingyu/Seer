 package com.censoredsoftware.Demigods.Engine.Object.Ability;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 
 public abstract class Ability
 {
 	private AbilityInfo info;
 	private Listener listener;
 	private Runnable runnable;
 	private static final int TARGETOFFSET = 5;
 
 	public Ability(AbilityInfo info, Listener listener, Runnable runnable)
 	{
 		this.info = info;
 		this.listener = listener;
 		this.runnable = runnable;
 	}
 
 	public AbilityInfo getInfo()
 	{
 		return info;
 	}
 
 	public Listener getListener()
 	{
 		return listener;
 	}
 
 	public Runnable getRunnable()
 	{
 		return runnable;
 	}
 
 	private static boolean doAbilityPreProcess(Player player, int cost)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 		if(!ZoneUtility.canTarget(player))
 		{
 			player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 			return false;
 		}
 		else if(character.getMeta().getFavor() < cost)
 		{
 			player.sendMessage(ChatColor.YELLOW + "You do not have enough favor.");
 			return false;
 		}
 		else return true;
 	}
 
 	/**
 	 * Returns true if the ability for <code>player</code>, called <code>name</code>,
 	 * with a cost of <code>cost</code>, that is Type <code>type</code>, has
 	 * passed all pre-process tests.
 	 * 
 	 * @param player the player doing the ability
 	 * @param name the name of the ability
 	 * @param cost the cost (in favor) of the ability
 	 * @param info the AbilityInfo object
 	 * @return true/false depending on if all pre-process tests have passed
 	 */
 	public static boolean doAbilityPreProcess(Player player, String name, int cost, AbilityInfo info)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 		return doAbilityPreProcess(player, cost); // TODO callAbilityEvent(name, character, cost, info);
 	}
 
 	/**
 	 * Returns true if the ability for <code>player</code>, called <code>name</code>,
 	 * with a cost of <code>cost</code>, that is Type <code>type</code>, that
 	 * is doTargeting the LivingEntity <code>target</code>, has passed all pre-process tests.
 	 * 
 	 * @param player the Player doing the ability
 	 * @param target the LivingEntity being targeted
 	 * @param name the name of the ability
 	 * @param cost the cost (in favor) of the ability
 	 * @param info the AbilityInfo object
 	 * @return true/false depending on if all pre-process tests have passed
 	 */
 	public static boolean doAbilityPreProcess(Player player, LivingEntity target, String name, int cost, AbilityInfo info)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 		if(doAbilityPreProcess(player, cost)) // TODO callAbilityEvent(name, character, cost, info))
 		{
 			if(!(target instanceof LivingEntity))
 			{
 				player.sendMessage(ChatColor.YELLOW + "No target found.");
 				return false;
 			}
 			else if(!ZoneUtility.canTarget(target))
 			{
 				player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
 				return false;
 			}
 			else if(target instanceof Player)
 			{
 				PlayerCharacter attacked = PlayerWrapper.getPlayer(((Player) target)).getCurrent();
 				if(attacked != null && PlayerCharacter.areAllied(character, attacked)) return false;
 			}
 			else if(target instanceof Tameable)
 			{
 				TameableWrapper attacked = TameableWrapper.getTameable(target);
 				if(attacked != null && PlayerCharacter.areAllied(character, attacked.getOwner())) return false;
 			}
 			// TODO Bukkit.getServer().getPluginManager().callEvent(new AbilityTargetEvent(character, target, info));
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the callAbilityEvent <code>callAbilityEvent</code> is caused by a left click.
 	 * 
 	 * @param event the interact callAbilityEvent
 	 * @return true/false depending on if the callAbilityEvent is caused by a left click or not
 	 */
 	public static boolean isLeftClick(PlayerInteractEvent event)
 	{
 		Action action = event.getAction();
 		return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
 	}
 
 	/**
 	 * Returns the LivingEntity that <code>player</code> is doTargeting.
 	 * 
 	 * @param player the interact callAbilityEvent
 	 * @return the targeted LivingEntity
 	 */
 	public static LivingEntity autoTarget(Player player)
 	{
 		int targetRangeCap = Demigods.config.getSettingInt("caps.target_range");
 		Location targetLoc = player.getTargetBlock(null, targetRangeCap).getLocation();
 
 		for(Entity entity : player.getNearbyEntities(targetRangeCap, targetRangeCap, targetRangeCap))
 		{
 			if(entity.getLocation().distance(targetLoc) < 3 && entity instanceof LivingEntity) // TODO: Fix this.
 			{
 				if(entity instanceof Tameable && ((Tameable) entity).isTamed() && TameableWrapper.getTameable((LivingEntity) entity) != null)
 				{
 					TameableWrapper wrapper = TameableWrapper.getTameable((LivingEntity) entity);
 					if(PlayerCharacter.areAllied(PlayerWrapper.getPlayer(player).getCurrent(), wrapper.getOwner())) continue;
 				}
 
 				return (LivingEntity) entity;
 			}
 		}
 
 		return null;
 	}
 
 	public static Location directTarget(Player player)
 	{
 		return player.getTargetBlock(null, Demigods.config.getSettingInt("caps.target_range")).getLocation();
 	}
 
 	/**
 	 * Returns true if the <code>player</code> ability hits <code>target</code>.
 	 * 
 	 * @param player the player using the ability
 	 * @param target the targeted LivingEntity
 	 * @return true/false depending on if the ability hits or misses
 	 */
 	public static boolean doTargeting(Player player, Location target, boolean notify)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		Location toHit = adjustedAimLocation(character, target);
 		if(isHit(target, toHit)) return true;
 		if(notify) player.sendMessage(ChatColor.RED + "Missed..."); // TODO Better message.
 		return false;
 	}
 
 	/**
 	 * Returns the location that <code>character</code> is actually aiming
 	 * at when doTargeting <code>target</code>.
 	 * 
 	 * @param character the character triggering the ability callAbilityEvent
 	 * @param target the location the character is doTargeting at
 	 * @return the aimed at location
 	 */
 	public static Location adjustedAimLocation(PlayerCharacter character, Location target)
 	{
 		int ascensions = character.getMeta().getAscensions();
 		if(ascensions < 3) ascensions = 3;
 
 		int offset = (int) (TARGETOFFSET + character.getOfflinePlayer().getPlayer().getLocation().distance(target));
 		int adjustedOffset = offset / ascensions;
 		if(adjustedOffset < 1) adjustedOffset = 1;
 		Random random = new Random();
 		World world = target.getWorld();
 
 		int randomInt = random.nextInt(adjustedOffset);
 
 		int sampleSpace = random.nextInt(3);
 
 		double X = target.getX();
 		double Z = target.getZ();
 		double Y = target.getY();
 
 		if(sampleSpace == 0)
 		{
 			X += randomInt;
 			Z += randomInt;
 		}
 		else if(sampleSpace == 1)
 		{
 			X -= randomInt;
 			Z -= randomInt;
 		}
 		else if(sampleSpace == 2)
 		{
 			X -= randomInt;
 			Z += randomInt;
 		}
 		else if(sampleSpace == 3)
 		{
 			X += randomInt;
 			Z -= randomInt;
 		}
 
 		return new Location(world, X, Y, Z);
 	}
 
 	/**
 	 * Returns true if <code>target</code> is hit at <code>hit</code>.
 	 * 
 	 * @param target the LivingEntity being targeted
 	 * @param hit the location actually hit
 	 * @return true/false if <code>target</code> is hit
 	 */
 	public static boolean isHit(Location target, Location hit)
 	{
 		Location shouldHit = target;
 		return hit.distance(shouldHit) <= 2;
 	}
 
 	public static List<Ability> getLoadedAbilities()
 	{
 		return new ArrayList<Ability>()
 		{
 			{
 				for(Deity deity : Demigods.getLoadedDeities())
 				{
 					addAll(deity.getAbilities());
 				}
 			}
 		};
 	}
 
 	public static boolean invokeAbilityCommand(Player player, String command)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		for(Ability ability : character.getDeity().getAbilities())
 		{
 			if(ability.getInfo().getType().equals(Devotion.Type.PASSIVE)) continue;
 
 			if(ability.getInfo().getCommand() != null && ability.getInfo().getCommand().equalsIgnoreCase(command))
 			{
 				// Ensure that the deity can be used, permission allows it, etc
 				if(!Deity.canUseDeity(player, ability.getInfo().getDeity())) return true;
 				if(!player.hasPermission(ability.getInfo().getPermission())) return true;
 
 				// Handle enabling the command
 				final String identifier = MiscUtility.generateString(6);
 				final AbilityInfo abilityInfo = ability.getInfo();
 				String abilityName = abilityInfo.getName();
 
 				if(!character.getMeta().isBound(ability.getInfo().getName()))
 				{
					if(!abilityInfo.hasWeapon() && (player.getItemInHand() != null || !player.getItemInHand().getType().equals(Material.AIR)))
 					{
 						// Slot must be empty
 						player.sendMessage(ChatColor.RED + Demigods.text.getText(TextUtility.Text.ERROR_BIND_TO_SLOT));
 						return true;
 					}
 					else if(abilityInfo.hasWeapon())
 					{
 						// Weapon required
 						player.sendMessage(ChatColor.RED + Demigods.text.getText(TextUtility.Text.ERROR_BIND_WEAPON_REQUIRED).replace("{weapon}", abilityInfo.getWeapon().name().toLowerCase().replace("_", " ")).replace("{ability}", abilityName.toLowerCase()));
 						return true;
 					}
 
 					// Create a stick and set the meta
 					ItemStack item = abilityInfo.hasWeapon() ? player.getItemInHand() : new ItemStack(Material.STICK);
 					ItemMeta itemMeta = item.getItemMeta();
 					itemMeta.setDisplayName(ChatColor.RESET + abilityName);
 
 					itemMeta.setLore(new ArrayList<String>()
 					{
 						{
 							add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Consumes " + abilityInfo.getCost() + " favor per use.");
 							add("");
 							for(String detail : abilityInfo.getDetails())
 							{
 								add(ChatColor.AQUA + detail);
 							}
 							add("");
 							add(ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH + "Identifier: " + ChatColor.MAGIC + identifier);
 						}
 					});
 
 					// Set the item meta
 					item.setItemMeta(itemMeta);
 
 					// Save the bind
 					character.getMeta().setBound(abilityName, player.getInventory().getHeldItemSlot(), item);
 
 					// Let them know
 					player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.SUCCESS_ABILITY_BOUND).replace("{ability}", StringUtils.capitalize(abilityName)).replace("{slot}", "" + player.getInventory().getHeldItemSlot()));
 
 					return true;
 				}
 				else
 				{
 					// Remove the bind
 					character.getMeta().removeBind(ability.getInfo().getName());
 
 					// Let them know
 					player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.SUCCESS_ABILITY_UNBOUND).replace("{ability}", StringUtils.capitalize(abilityName)));
 
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static void dealDamage(LivingEntity source, LivingEntity target, double amount, EntityDamageEvent.DamageCause cause)
 	{
 		if(source instanceof Player)
 		{
 			PlayerWrapper owner = PlayerWrapper.getPlayer(((Player) source));
 			if(owner != null)
 			{
 				if(target instanceof Player)
 				{
 					PlayerCharacter targetChar = PlayerWrapper.getPlayer(((Player) target)).getCurrent();
 					if(targetChar != null && PlayerCharacter.areAllied(owner.getCurrent(), targetChar)) return;
 				}
 				else if(target instanceof Tameable && ((Tameable) target).isTamed())
 				{
 					TameableWrapper wrapper = TameableWrapper.getTameable(target);
 					if(wrapper != null && PlayerCharacter.areAllied(owner.getCurrent(), wrapper.getOwner())) return;
 				}
 			}
 		}
 		EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(source, target, cause, amount);
 		target.setLastDamageCause(event);
 		if(amount >= 1 && !event.isCancelled()) target.damage(amount);
 	}
 }

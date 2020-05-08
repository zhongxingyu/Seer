 package uk.thecodingbadgers.minekart.race;
 
 import java.util.Random;
 
 import net.citizensnpcs.api.CitizensAPI;
 import net.citizensnpcs.api.npc.NPC;
 import net.citizensnpcs.trait.Controllable;
 
 import org.bukkit.Color;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  * @author TheCodingBadgers
  *
  * A jockey consists of a player and his mount. A mount is any entity that can be controlled.
  * Jockeys also hold any powerups attained during a race. 
  *
  */
 public class Jockey {
 	
 	/** The player which represents this jockey */
 	private Player player = null;
 	
 	/** The type of mount the jockey will use */
 	private EntityType mountType = EntityType.UNKNOWN;
 	
 	/** The color which represents this jockey */
 	private Color jockeyColor = Color.RED;
 	
 	/** The jockeys mount */
 	private NPC mount = null;
 	
 	/** The location where the jockey should be taken too on race exit */
 	private Location exitLocaiton = null;
 	
 	/** The race that this jockey is in */
 	private Race race = null;
 
 	/**
 	 * 
 	 * @param player
 	 * @param mountType
 	 * @param race 
 	 */
 	public Jockey(Player player, EntityType mountType, Race race) {
 		this.player = player;
 		this.mountType = mountType;
 		this.race = race;
 		this.exitLocaiton = player.getLocation();
 		this.jockeyColor = getRandomColor();
 		
 		// Give the player a coloured jersey
 		ItemStack jersey = new ItemStack(Material.LEATHER_CHESTPLATE);
 		LeatherArmorMeta jerseyMeta = (LeatherArmorMeta) jersey.getItemMeta();
 		jerseyMeta.setColor(this.jockeyColor);
 		jersey.setItemMeta(jerseyMeta);
 		
 		// Give the player a coloured hat
 		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
 		LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
 		helmetMeta.setColor(this.jockeyColor);
 		helmet.setItemMeta(helmetMeta);
 		
 		// Give the jockey white leggings
 		ItemStack leggings = new ItemStack(Material.LEATHER_HELMET);
 		LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
 		leggingsMeta.setColor(Color.WHITE);
 		leggings.setItemMeta(leggingsMeta);
 		
 		// Give the jockey black boots
 		ItemStack boots = new ItemStack(Material.LEATHER_HELMET);
 		LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
 		bootsMeta.setColor(Color.BLACK);
 		boots.setItemMeta(bootsMeta);
 		
 		player.getInventory().setHelmet(helmet);
 		player.getInventory().setChestplate(jersey);
 		player.getInventory().setLeggings(leggings);
 		player.getInventory().setBoots(boots);
 	}
 
 	/**
 	 * Get a random color
 	 * @return A color
 	 */
 	private Color getRandomColor() {
 		Random random = new Random();
 		return Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
 	}
 
 	/**
 	 * Get the player which represents this jockey
 	 * @return The player
 	 */
 	public Player getPlayer() {
 		return this.player;
 	}
 
 	/**
 	 * Teleport a jockey to a spawn and put them on their mount 
 	 * @param spawn The spawn location
 	 */
 	public void teleportToSpawn(Location spawn) {
 		
 		// make their mounts
 		this.mount = CitizensAPI.getNPCRegistry().createNPC(this.mountType, getRadomMountName());
 		this.mount.setProtected(true);
 		this.mount.addTrait(new Controllable(false));
 		this.mount.spawn(spawn);
 		
 		// Make the npc controllable and mount the player
 		Controllable trait = this.mount.getTrait(Controllable.class);
 		trait.setEnabled(true);
 		trait.mount(this.player);	
 		trait.setEnabled(false); // disable it until the race has started
 		
 		// Give the player a whip
 		ItemStack whip = new ItemStack(Material.STICK);
 		ItemMeta whipMeta = whip.getItemMeta();
 		whipMeta.setDisplayName("Whip");
 		whip.setItemMeta(whipMeta);
 		whip.setAmount(4);
 		whip.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
 		player.getInventory().setItem(0, whip);
 	}
 
 	/**
 	 * Get a random name to be used by a mount
 	 * @return A string to be used as the mount name
 	 */
 	private String getRadomMountName() {
 		
 		String[] allNames = {"Mental Boy", "Nervous Sparxx", "OAP Money", "Clean Smoke",
 				"Gnashing Panic", "Near Pride", "Bringing Action", "Nefarious Dusty",
 				"Tornado Fall", "Jim's Depression", "Caramel Comedy", "Wally's Maiden",
 				"Dirty Underwater", "Romantic Apple", "Wisby's Revenge", "Rabid Ruler",
 				"Scared Sally", "Prancers Dream", "Tidy's Teen", "Losing Hope", "Adios Alex",
 				"Whisky Galore", "Who's Dr"};
 		
 		Random random = new Random();
 		return allNames[random.nextInt(allNames.length)];
 	}
 
 	/**
 	 * Called when a race starts
 	 */
 	public void onRaceStart() {
 		
 		Controllable trait = this.mount.getTrait(Controllable.class);
 		trait.setEnabled(true);
 		
 	}
 
 	/**
 	 * Call when a race has ended
 	 */
 	public void onRaceEnd() {
		if (this.mount != null) {
			this.mount.getBukkitEntity().eject();
			this.mount.destroy();
		}
 		this.player.teleport(this.exitLocaiton);
 	}
 
 	/**
 	 * Get the race this jockey is in
 	 * @return The race instance
 	 */
 	public Race getRace() {
 		return this.race;
 	}
 
 	/**
 	 * Increase the speed of the mount for a given amount of time
 	 * @param speed The new speed of the mount
 	 * @param length The amount of time the speed boost should be applied
 	 */
 	public void increaseSpeed(int speed, int length) {
 		
 		PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, length * 20, speed, false);
 		this.player.addPotionEffect(effect, true);
 		this.mount.getBukkitEntity().addPotionEffect(effect, true);
 		
 	}
 	
 }

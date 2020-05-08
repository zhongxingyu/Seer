 package net.lordsofcode.zephyrus;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import net.lordsofcode.zephyrus.commands.Bind;
 import net.lordsofcode.zephyrus.commands.Cast;
 import net.lordsofcode.zephyrus.commands.Level;
 import net.lordsofcode.zephyrus.commands.LevelUp;
 import net.lordsofcode.zephyrus.commands.LevelUpItem;
 import net.lordsofcode.zephyrus.commands.ManaCommand;
 import net.lordsofcode.zephyrus.commands.SpellTomeCmd;
 import net.lordsofcode.zephyrus.commands.UnBind;
 import net.lordsofcode.zephyrus.enchantments.BattleAxe;
 import net.lordsofcode.zephyrus.enchantments.GlowEffect;
 import net.lordsofcode.zephyrus.enchantments.InstaMine;
 import net.lordsofcode.zephyrus.enchantments.LifeSuck;
 import net.lordsofcode.zephyrus.enchantments.ToxicStrike;
 import net.lordsofcode.zephyrus.hooks.PluginHook;
 import net.lordsofcode.zephyrus.items.BlinkPearl;
 import net.lordsofcode.zephyrus.items.CustomItem;
 import net.lordsofcode.zephyrus.items.GemOfLightning;
 import net.lordsofcode.zephyrus.items.HoeOfGrowth;
 import net.lordsofcode.zephyrus.items.ManaPotion;
 import net.lordsofcode.zephyrus.items.RodOfFire;
 import net.lordsofcode.zephyrus.items.SpellTome;
 import net.lordsofcode.zephyrus.items.Wand;
 import net.lordsofcode.zephyrus.listeners.EconListener;
 import net.lordsofcode.zephyrus.listeners.ItemLevelListener;
 import net.lordsofcode.zephyrus.listeners.LevelingListener;
 import net.lordsofcode.zephyrus.listeners.PlayerListener;
 import net.lordsofcode.zephyrus.player.LevelManager;
 import net.lordsofcode.zephyrus.player.ManaRecharge;
 import net.lordsofcode.zephyrus.spells.Armour;
 import net.lordsofcode.zephyrus.spells.Arrow;
 import net.lordsofcode.zephyrus.spells.ArrowStorm;
 import net.lordsofcode.zephyrus.spells.Bang;
 import net.lordsofcode.zephyrus.spells.Blink;
 import net.lordsofcode.zephyrus.spells.Bolt;
 import net.lordsofcode.zephyrus.spells.Butcher;
 import net.lordsofcode.zephyrus.spells.Confuse;
 import net.lordsofcode.zephyrus.spells.Conjure;
 import net.lordsofcode.zephyrus.spells.Detect;
 import net.lordsofcode.zephyrus.spells.Dig;
 import net.lordsofcode.zephyrus.spells.Dispel;
 import net.lordsofcode.zephyrus.spells.Enderchest;
 import net.lordsofcode.zephyrus.spells.Explode;
 import net.lordsofcode.zephyrus.spells.Feather;
 import net.lordsofcode.zephyrus.spells.Feed;
 import net.lordsofcode.zephyrus.spells.FireRing;
 import net.lordsofcode.zephyrus.spells.FireShield;
 import net.lordsofcode.zephyrus.spells.Fireball;
 import net.lordsofcode.zephyrus.spells.FlameStep;
 import net.lordsofcode.zephyrus.spells.Flare;
 import net.lordsofcode.zephyrus.spells.Fly;
 import net.lordsofcode.zephyrus.spells.Frenzy;
 import net.lordsofcode.zephyrus.spells.Grenade;
 import net.lordsofcode.zephyrus.spells.Grow;
 import net.lordsofcode.zephyrus.spells.Heal;
 import net.lordsofcode.zephyrus.spells.Home;
 import net.lordsofcode.zephyrus.spells.Jail;
 import net.lordsofcode.zephyrus.spells.LifeSteal;
 import net.lordsofcode.zephyrus.spells.MageLight;
 import net.lordsofcode.zephyrus.spells.Mana;
 import net.lordsofcode.zephyrus.spells.MassParalyze;
 import net.lordsofcode.zephyrus.spells.Paralyze;
 import net.lordsofcode.zephyrus.spells.Phase;
 import net.lordsofcode.zephyrus.spells.Prospect;
 import net.lordsofcode.zephyrus.spells.Punch;
 import net.lordsofcode.zephyrus.spells.Repair;
 import net.lordsofcode.zephyrus.spells.Satisfy;
 import net.lordsofcode.zephyrus.spells.Shield;
 import net.lordsofcode.zephyrus.spells.Smite;
 import net.lordsofcode.zephyrus.spells.Spell;
 import net.lordsofcode.zephyrus.spells.Storm;
 import net.lordsofcode.zephyrus.spells.Summon;
 import net.lordsofcode.zephyrus.spells.SuperHeat;
 import net.lordsofcode.zephyrus.spells.Vanish;
 import net.lordsofcode.zephyrus.spells.Zap;
 import net.lordsofcode.zephyrus.spells.Zephyr;
 import net.lordsofcode.zephyrus.utils.ConfigHandler;
 import net.lordsofcode.zephyrus.utils.Lang;
 import net.lordsofcode.zephyrus.utils.Merchant;
 import net.lordsofcode.zephyrus.utils.UpdateChecker;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.craftbukkit.v1_6_R1.entity.CraftLivingEntity;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class Zephyrus extends JavaPlugin {
 
 	private static Zephyrus instance;
 
 	public ConfigHandler config = new ConfigHandler(this, "spells.yml");
 	public ConfigHandler langCfg = new ConfigHandler(this, "lang.yml");
 
 	public FileConfiguration lang;
 	public FileConfiguration spells;
 
 	public GlowEffect glow = new GlowEffect(120);
 
 	public String[] updateMsg;
 
 	public Map<String, Map<String, Integer>> itemDelay;
 	public Map<String, Merchant> invPlayers;
 
 	public static Map<String, Object> mana;
 	public static Map<String, Spell> spellMap;
 	public static Map<Set<ItemStack>, Spell> spellCraftMap;
 	public static Map<String, CustomItem> itemMap;
 	public static Map<ItemStack, Merchant> merchantMap;
 
 	private int builtInSpells = 0;
 
 	@Override
 	public void onEnable() {
 		instance = this;
 		new UpdateChecker(this);
 
 		saveDefaultConfig();
 		config.saveDefaultConfig();
 		langCfg.saveDefaultConfig();
 
 		itemMap = new HashMap<String, CustomItem>();
 		spellCraftMap = new HashMap<Set<ItemStack>, Spell>();
 		spellMap = new HashMap<String, Spell>();
 		merchantMap = new HashMap<ItemStack, Merchant>();
 		invPlayers = new HashMap<String, Merchant>();
 		itemDelay = new HashMap<String, Map<String, Integer>>();
 		mana = new HashMap<String, Object>();
 
 		Lang.add("noperm", "You do not have permission to do that!");
 		Lang.add("ingameonly",
 				"You must be an in-game player to perform this command!");
 		Lang.add("notonline", "That player is not online!");
 		Lang.add("outofdatebukkit",
 				"Sadly, the version of Craftbukkit that you are using is out of date...");
 
 		Lang.add("nomana", "Not enough mana!");
 		Lang.add("disabled", "That spell has been disabled...");
 		Lang.add("notlearned", "You do not know that spell!");
 		Lang.add("worldguard", "You do not have permission for this area!");
 
 		Lang.add("spelltome.learn",
 				"Learn this spell by left clicking this book");
 		Lang.add("spelltome.cast", "Cast this spell with $b/cast [SPELL]$0");
 		Lang.add("spelltome.nospell", "That spell was not found!");
 		Lang.add("spelltome.known", "You already know that spell!");
 		Lang.add("spelltome.success", "You have successfully learned $6[SPELL]");
 		Lang.add("spelltome.cantlearn", ChatColor.DARK_RED
 				+ "You don't have permission to learn [SPELL]");
 		Lang.add("spelltome.noperm", ChatColor.DARK_RED
 				+ "You don't have permission to use the spelltome!");
 
 		Lang.add("customitem.level", "Level");
 
 		try {
 			new CraftLivingEntity(null, null);
 		} catch (NoClassDefFoundError err) {
 			getLogger()
 					.warning(
 							"This version of Zephyrus is not fully compatible with your version of CraftBukkit."
 									+ " Some features have been disabled!");
 		}
 
 		hook();
 		addCommands();
 		addEnchants();
 		addItems();
 		getLogger().info("Loading spells...");
 		addSpells();
 		addListeners();
 
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			Zephyrus.mana.put(p.getName(), LevelManager.loadMana(p));
 			new ManaRecharge(this, p).runTaskLater(this, 30);
 		}
 
 		getLogger().info(
 				"Zephyrus v"
 						+ getDescription().getVersion()
 						+ " by "
 						+ getDescription().getAuthors().toString()
 								.replace("[", "").replace("]", "")
 						+ " enabled!");
 
 		new PostInit().runTaskAsynchronously(this);
 
 		instance = this;
 	}
 
 	@Override
 	public void onDisable() {
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			LevelManager.saveMana(p);
 			Zephyrus.mana.remove(p);
 		}
 		disableSpells();
 	}
 
 	/**
 	 * An instance of Zephyrus defined onEnable
 	 * 
 	 * @return An instance of Zephyrus
 	 */
 	public static Zephyrus getInstance() {
 		return instance;
 	}
 
 	private void hook() {
 		if (PluginHook.isWorldGuard()) {
 			getLogger().info("WorldGuard found. Protections integrated");
 		}
 		if (PluginHook.isEconomy()) {
 			getLogger().info("Vault found. Integrating economy!");
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(new EconListener(this), this);
 		}
 	}
 
 	private void addItems() {
 		if (!getConfig().getBoolean("Disable-Recipes")) {
 			new BlinkPearl(this);
 			new GemOfLightning(this);
 			new HoeOfGrowth(this);
 			new ManaPotion(this);
 			new RodOfFire(this);
 		}
 		new ManaPotion(this);
 		new Wand(this);
 	}
 
 	private void disableSpells() {
 		for (Spell spell : spellMap.values()) {
 			spell.onDisable();
 		}
 		instance = null;
 	}
 
 	private void addSpells() {
 		// A
 		new Armour(this);
 		new Arrow(this);
 		new ArrowStorm(this);
 		// B
 		new Bang(this);
 		new Blink(this);
 		new Bolt(this);
 		new Butcher(this);
 		// C
 		new Confuse(this);
 		new Conjure(this);
 		// D
 		new Detect(this);
 		new Dig(this);
 		new Dispel(this);
 		// E
 		new Enderchest(this);
 		new Explode(this);
 		// F
 		new Feather(this);
 		new Feed(this);
 		new Fireball(this);
 		new FireRing(this);
 		new FireShield(this);
 		new FlameStep(this);
 		new Flare(this);
 		new Fly(this);
 		new Frenzy(this);
 		// G
 		new Grow(this);
 		new Grenade(this);
 		// H
 		new Heal(this);
 		new Home(this);
 		// J
 		new Jail(this);
 		// L
 		new LifeSteal(this);
 		// M
 		new MageLight(this);
 		new Mana(this);
 		new MassParalyze(this);
 		// P
 		new Paralyze(this);
 		new Phase(this);
 		new Prospect(this);
 		new Punch(this);
 		// R
 		new Repair(this);
 		// S
 		new Satisfy(this);
 		new Shield(this);
 		new Smite(this);
 		// new Steal(this);
 		new Storm(this);
 		new Summon(this);
 		new SuperHeat(this);
 		// V
 		new Vanish(this);
 		// Z
 		new Zap(this);
 		new Zephyr(this);
 	}
 
 	private void addEnchants() {
 		if (getConfig().getBoolean("Enable-Enchantments")) {
 			new InstaMine(123);
 			new LifeSuck(124);
 			new ToxicStrike(125);
 			new BattleAxe(126);
 		} else if (getConfig().contains("Enable-Enchantments")) {
 			new InstaMine(123);
 			new LifeSuck(124);
 			new ToxicStrike(125);
 			new BattleAxe(126);
 		}
 		try {
 			Field f = Enchantment.class.getDeclaredField("acceptingNew");
 			f.setAccessible(true);
 			f.set(null, true);
 		} catch (Exception e) {
 		}
 		try {
 			Enchantment.registerEnchantment(glow);
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	private void addListeners() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new LevelingListener(this), this);
 		pm.registerEvents(new SpellTome(this, null, null), this);
 		pm.registerEvents(new ItemLevelListener(this), this);
 		pm.registerEvents(new PlayerListener(this), this);
 	}
 
 	private void addCommands() {
 		getCommand("levelup").setExecutor(new LevelUp(this));
 		getCommand("levelupitem").setExecutor(new LevelUpItem(this));
 		getCommand("cast").setExecutor(new Cast(this));
 		getCommand("cast").setTabCompleter(new Cast(this));
 		getCommand("mana").setExecutor(new ManaCommand(this));
 		getCommand("bind").setExecutor(new Bind(this));
 		getCommand("bind").setTabCompleter(new Bind(this));
 		getCommand("spelltome").setExecutor(new SpellTomeCmd(this));
 		getCommand("level").setExecutor(new Level(this));
 		getCommand("unbind").setExecutor(new UnBind());
 	}
 
 	/**
 	 * Adds the designated spell to Zephyrus Automatically called in the Spell
 	 * constructor
 	 * 
 	 * @param spell
 	 *            The spell to add
 	 */
 	public void addSpell(Spell spell) {
 		if ((spell.getClass().getPackage() == Spell.class.getPackage())) {
 			if (spell.isEnabled()) {
 				spellCfg(spell);
 				Zephyrus.spellMap.put(spell.getDisplayName().toLowerCase(),
 						spell);
 				if (spell.spellItems() != null) {
 					Zephyrus.spellCraftMap.put(spell.spellItems(), spell);
 				}
 
 				getServer().getPluginManager().registerEvents(spell, this);
 
 				Permission castPerm = new Permission("zephyrus.cast."
 						+ spell.name().toLowerCase(), PermissionDefault.FALSE);
 				Bukkit.getPluginManager().addPermission(castPerm);
 
 				Permission spellPerm = new Permission("zephyrus.spell."
 						+ spell.name().toLowerCase(), PermissionDefault.TRUE);
 				Bukkit.getPluginManager().addPermission(spellPerm);
 
 				builtInSpells++;
 			}
 		} else {
 			if (spell.isEnabled()) {
 				spellCfg(spell);
 				Zephyrus.spellMap.put(spell.getDisplayName().toLowerCase(),
 						spell);
 				if (spell.spellItems() != null
 						&& !Zephyrus.spellCraftMap.containsKey(spell
 								.spellItems())) {
 					Zephyrus.spellCraftMap.put(spell.spellItems(), spell);
 				}
 				Permission perm = new Permission("zephyrus.cast."
 						+ spell.name().toLowerCase(), PermissionDefault.FALSE);
 				Bukkit.getPluginManager().addPermission(perm);
 			}
 		}
 	}
 
 	private void spellCfg(final Spell spell) {
 		getServer().getScheduler().runTask(this, new BukkitRunnable() {
 
 			@Override
 			public void run() {
 				if (!config.getConfig().contains(spell.name() + ".enabled")) {
 					config.getConfig().set(spell.name() + ".enabled", true);
 				}
				if (!config.getConfig().contains(spell.name() + ".desc")) {
 					config.getConfig().set(spell.name() + ".desc",
							spell.bookText());
 				}
 				if (!config.getConfig().contains(spell.name() + ".mana")) {
 					config.getConfig().set(spell.name() + ".mana",
 							spell.manaCost());
 				}
 				if (!config.getConfig().contains(spell.name() + ".level")) {
 					config.getConfig().set(spell.name() + ".level",
 							spell.reqLevel());
 				}
 				if (!config.getConfig().contains(spell.name() + ".exp")) {
 					config.getConfig().set(spell.name() + ".exp",
 							spell.manaCost() / 3 + 1);
 				}
 				if (!config.getConfig().contains(spell.name() + ".displayname")) {
 					config.getConfig().set(spell.name() + ".displayname",
 							spell.name());
 				}
 				if (spell.failMessage() != ""
 						&& !langCfg.getConfig().contains(
 								"spells." + spell.name() + ".fail")) {
 					Lang.add(
 							"spells." + spell.name() + ".fail",
 							spell.failMessage().replace(
 									ChatColor.COLOR_CHAR + "", "$"));
 				}
 				if (spell.getConfigurations() != null) {
 					Map<String, Object> cfg = spell.getConfigurations();
 					for (String str : cfg.keySet()) {
 						if (!config.getConfig().contains(
 								spell.name() + "." + str)) {
 							config.getConfig().set(spell.name() + "." + str,
 									cfg.get(str));
 						}
 					}
 				}
 				config.saveConfig();
 			}
 
 		});
 	}
 
 	private class PostInit extends BukkitRunnable {
 		@Override
 		public void run() {
 			for (CustomItem ci : Zephyrus.itemMap.values()) {
 				if (ci.hasLevel()) {
 					for (int i = 1; i < ci.maxLevel(); i++) {
 						ItemStack item = ci.item();
 						ci.setItemLevel(item, i);
 						ItemStack item2 = ci.item();
 						int i2 = i;
 						ci.setItemLevel(item2, i2 + 1);
 						Merchant m = new Merchant();
 						m.addOffer(item, new ItemStack(Material.EMERALD, i),
 								item2);
 						Zephyrus.merchantMap.put(item, m);
 					}
 				}
 			}
 
 			lang = langCfg.getConfig();
 			spells = config.getConfig();
 
 			try {
 				for (String s : updateMsg) {
 					if (s != null) {
 						getLogger().info(s);
 					}
 				}
 			} catch (NullPointerException e) {
 				getLogger().info("Could not check for updates...");
 			}
 			String added = "";
 			int external = spellMap.size() - builtInSpells;
 			added = " " + external + " external spells registered. ";
 
 			getLogger().info("Loaded " + spellMap.size() + " spells." + added);
 		}
 	}
 
 }

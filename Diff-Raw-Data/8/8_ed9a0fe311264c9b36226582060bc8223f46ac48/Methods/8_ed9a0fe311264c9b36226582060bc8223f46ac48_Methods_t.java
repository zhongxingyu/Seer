 package org.rsbot.script.methods;
 
 import java.awt.Color;
import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import javax.imageio.ImageIO;

 import org.rsbot.script.wrappers.RSGroundItem;
 import org.rsbot.script.wrappers.RSNPC;
 import org.rsbot.script.wrappers.RSObject;
 import org.rsbot.script.wrappers.RSPlayer;
 import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;
 
 
 /**
  * Provides access to methods that can be used by RSBot scripts.
  */
 public class Methods {
 
 	/**
 	 * The logger instance
 	 */
 	protected final Logger log = Logger.getLogger(getClass().getName());
 	/**
 	 * The instance of {@link java.util.Random} for random number generation.
 	 */
 	protected static final java.util.Random random = new java.util.Random();
 	/**
 	 * The singleton of Skills
 	 */
 	protected Skills skills;
 	/**
 	 * The singleton of Settings
 	 */
 	protected Settings settings;
 	/**
 	 * The singleton of Magic
 	 */
 	protected Magic magic;
 	/**
 	 * The singleton of Bank
 	 */
 	protected Bank bank;
 	/**
 	 * The singleton of Players
 	 */
 	protected Players players;
 	/**
 	 * The singleton of Store
 	 */
 	protected Store store;
 	/**
 	 * The singleton of GrandExchange
 	 */
 	protected GrandExchange grandExchange;
 	/**
 	 * The singletion of Hiscores
 	 */
 	protected Hiscores hiscores;
 	/**
 	 * The singleton of ClanChat
 	 */
 	protected ClanChat clanChat;
 	/**
 	 * The singleton of Camera
 	 */
 	protected Camera camera;
 	/**
 	 * The singleton of NPCs
 	 */
 	protected NPCs npcs;
 	/**
 	 * The singleton of GameScreen
 	 */
 	protected Game game;
 	/**
 	 * The singleton of Combat
 	 */
 	protected Combat combat;
 	/**
 	 * The singleton of Interfaces
 	 */
 	protected Interfaces interfaces;
 	/**
 	 * The singleton of Mouse
 	 */
 	protected Mouse mouse;
 	/**
 	 * The singleton of Keyboard
 	 */
 	protected Keyboard keyboard;
 	/**
 	 * The singleton of Menu
 	 */
 	protected Menu menu;
 	/**
 	 * The singleton of Tiles
 	 */
 	protected Tiles tiles;
 	/**
 	 * The singleton of Objects
 	 */
 	protected Objects objects;
 	/**
 	 * The singleton of Walking
 	 */
 	protected Walking walking;
 	/**
 	 * The singleton of Calculations
 	 */
 	protected Calculations calc;
 	/**
 	 * The singleton of Inventory
 	 */
 	protected Inventory inventory;
 	/**
 	 * The singleton of Equipment
 	 */
 	protected Equipment equipment;
 	/**
 	 * The singleton of GroundItems
 	 */
 	protected GroundItems groundItems;
 	/**
 	 * The singleton of Account
 	 */
 	protected Account account;
 	/**
 	 * The singleton of Summoning
 	 */
 	protected Summoning summoning;
 	/**
 	 * The singleton of Environment
 	 */
 	protected Environment env;
 	/**
 	 * The singleton of Prayer
 	 */
 	protected Prayer prayer;
 	/**
 	 * The singleton of FriendsChat
 	 */
 	protected FriendChat friendChat;
 	/**
 	 * The singleton of Trade
 	 */
 	protected Trade trade;
 	/**
 	 * The singleton of Trade
 	 */
 	protected Paint paint;
 
 
 	/**
 	 * For internal use only: initializes the method providers.
 	 *
 	 * @param ctx The MethodContext.
 	 */
 	public void init(final MethodContext ctx) {
		ImageIO.setCacheDirectory(new File(GlobalConfiguration.Paths.getScriptCacheDirectory()));
 		skills = ctx.skills;
 		settings = ctx.settings;
 		magic = ctx.magic;
 		bank = ctx.bank;
 		players = ctx.players;
 		store = ctx.store;
 		grandExchange = ctx.grandExchange;
 		hiscores = ctx.hiscores;
 		clanChat = ctx.clanChat;
 		camera = ctx.camera;
 		npcs = ctx.npcs;
 		game = ctx.game;
 		combat = ctx.combat;
 		interfaces = ctx.interfaces;
 		mouse = ctx.mouse;
 		keyboard = ctx.keyboard;
 		menu = ctx.menu;
 		tiles = ctx.tiles;
 		objects = ctx.objects;
 		walking = ctx.walking;
 		calc = ctx.calc;
 		inventory = ctx.inventory;
 		equipment = ctx.equipment;
 		groundItems = ctx.groundItems;
 		account = ctx.account;
 		summoning = ctx.summoning;
 		env = ctx.env;
 		prayer = ctx.prayer;
 		friendChat = ctx.friendChat;
 		trade = ctx.trade;
 		paint = ctx.paint;
 	}
 
 	/**
 	 * Returns the current client's local player.
 	 *
 	 * @return The current client's <tt>RSPlayer</tt>.
 	 * @see Players#getMyPlayer()
 	 */
 	public RSPlayer getMyPlayer() {
 		return players.getMyPlayer();
 	}
 
 	/**
 	 * Returns a random integer with min as the inclusive lower bound and max as
 	 * the exclusive upper bound.
 	 *
 	 * @param min The inclusive lower bound.
 	 * @param max The exclusive upper bound.
 	 * @return Random integer min <= n < max.
 	 */
 	public static int random(final int min, final int max) {
 		final int n = Math.abs(max - min);
 		return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
 	}
 
 	/**
 	 * Checks for the existence of a NPC.
 	 *
 	 * @param npc The NPC to check for.
 	 * @return <tt>true</tt> if found.
 	 */
 	public boolean verify(final RSNPC npc) {
 		return npc != null;
 	}
 
 	/**
 	 * Checks for the existence of a RSObject.
 	 *
 	 * @param o The RSObject to check for.
 	 * @return <tt>true</tt> if found.
 	 */
 	public boolean verify(final RSObject o) {
 		return o != null;
 	}
 
 	/**
 	 * Checks for the existence of a RSTile.
 	 *
 	 * @param t The RSTile to check for.
 	 * @return <tt>true</tt> if found.
 	 */
 	public boolean verify(final RSTile t) {
 		return t != null;
 	}
 
 	/**
 	 * Checks for the existence of a RSGroundItem.
 	 *
 	 * @param i The RSGroundItem to check for.
 	 * @return <tt>true</tt> if found.
 	 */
 	public boolean verify(final RSGroundItem i) {
 		return i != null;
 	}
 
 	/**
 	 * Returns a random double with min as the inclusive lower bound and max as
 	 * the exclusive upper bound.
 	 *
 	 * @param min The inclusive lower bound.
 	 * @param max The exclusive upper bound.
 	 * @return Random double min <= n < max.
 	 */
 	public static double random(final double min, final double max) {
 		return Math.min(min, max) + random.nextDouble() * Math.abs(max - min);
 	}
 
 	/**
 	 * Pauses execution for a random amount of time between two values.
 	 *
 	 * @param minSleep The minimum time to sleep.
 	 * @param maxSleep The maximum time to sleep.
 	 * @see #sleep(int)
 	 * @see #random(int, int)
 	 */
 	public static void sleep(final int minSleep, final int maxSleep) {
 		sleep(random(minSleep, maxSleep));
 	}
 
 	/**
 	 * Pauses execution for a given number of milliseconds.
 	 *
 	 * @param toSleep The time to sleep in milliseconds.
 	 */
 	public static void sleep(final int toSleep) {
 		try {
 			final long start = System.currentTimeMillis();
 			Thread.sleep(toSleep);
 
 			// Guarantee minimum sleep
 			long now;
 			while (start + toSleep > (now = System.currentTimeMillis())) {
 				Thread.sleep(start + toSleep - now);
 			}
 		} catch (final InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Prints to the RSBot log.
 	 *
 	 * @param message Object to log.
 	 */
 	public void log(final Object message) {
 		log.info(message.toString());
 	}
 
 	/**
 	 * Prints to the RSBot log with a font color
 	 *
 	 * @param color   The color of the font
 	 * @param message Object to log
 	 */
 	public void log(final Color color, final Object message) {
 		final Object[] parameters = {color};
 		log.log(Level.INFO, message.toString(), parameters);
 	}
 }

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package gibstick.bukkit.discosheep;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 
 /**
  *
  * @author Georgiy
  */
 public class DiscoParty {
 
 	private DiscoSheep ds;
 	private Player player;
 	private ArrayList<Sheep> sheepList;
 	private int duration, frequency = 20, numSheep = 5;
 	private final int defaultDuration = 300; // ticks for entire party
 	private final int defaultFrequency = 10; // ticks per state change
 	private final int sheepSpawnRadius = 5;
 	private final int defaultSheepAmount = 10;
 	private DiscoUpdater updater;
 	private static final DyeColor[] discoColours = {
 		DyeColor.RED,
 		DyeColor.ORANGE,
 		DyeColor.YELLOW,
 		DyeColor.GREEN,
 		DyeColor.BLUE,
 		DyeColor.LIGHT_BLUE,
 		DyeColor.PINK,
 		DyeColor.MAGENTA,
 		DyeColor.LIME,
 		DyeColor.CYAN,
 		DyeColor.PURPLE
 	};
 
 	public DiscoParty(DiscoSheep parent, Player player) {
 		this.ds = parent;
 		this.player = player;
 	}
 
 	List<Sheep> getSheep() {
 		return sheepList;
 	}
 
 	void spawnSheep(World world, Location loc) {
 		Sheep newSheep = (Sheep) world.spawnEntity(loc, EntityType.SHEEP);
 		newSheep.setMaxHealth(10000);
 		newSheep.setHealth(10000);
 		newSheep.setColor(discoColours[(int) Math.round(Math.random() * (discoColours.length - 1))]);
 		getSheep().add(newSheep);
 	}
 
 	// Spawn some number of sheep next to given player
 	void spawnSheep(int num) {
 		Location loc;
 		World world = player.getWorld();
 
 		for (int i = 0; i < num; i++) {
 			double x, y, z;
 
 			// random x and z coordinates within a 5 block radius
 			// safe y-coordinate
 			x = -sheepSpawnRadius + (Math.random() * ((sheepSpawnRadius * 2) + 1)) + player.getLocation().getX();
 			z = -sheepSpawnRadius + (Math.random() * ((sheepSpawnRadius * 2) + 1)) + player.getLocation().getZ();
 			y = world.getHighestBlockYAt((int) x, (int) z);
 			loc = new Location(world, x, y, z);
 			spawnSheep(world, loc);
 		}
 	}
 
 	// Mark all sheep in the sheep array for removal, then clear the array
 	void removeAllSheep() {
 		for (Sheep sheep : getSheep()) {
 			sheep.setHealth(0);
 			sheep.remove();
 		}
 		getSheep().clear();
 	}
 
 	// Set a random colour for all sheep in array
 	void randomizeSheepColours() {
 		for (Sheep sheep : getSheep()) {
 			sheep.setColor(discoColours[(int) Math.round(Math.random() * (discoColours.length - 1))]);
 		}
 	}
 
 	void playSounds() {
 		player.playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 1.0f, 1.0f);
 		player.playSound(player.getLocation(), Sound.BURP, frequency, (float) Math.random() + 1);
 	}
 
 	void update() {
 		if (duration > 0) {
 			randomizeSheepColours();
 			playSounds();
 			duration -= frequency;
 			this.scheduleUpdate();
 		} else {
 			this.stopDisco();
 		}
 	}
 
 	void scheduleUpdate() {
 		updater = new DiscoUpdater(this);
 		updater.runTaskLater(ds, this.frequency);
 	}
 
 	void startDisco(int duration) {
 		if (this.duration > 0) {
 			stopDisco();
 		}
 		this.spawnSheep(this.defaultSheepAmount);
 		this.frequency = this.defaultFrequency;
 		this.duration = this.defaultDuration;
 		this.scheduleUpdate();
 		ds.getPartyMap().put(this.player.getName(), this);
 	}
 
 	void startDisco() {
 		this.startDisco(this.defaultDuration);
 	}
 
 	void stopDisco() {
 		removeAllSheep();
 		this.duration = 0;
 		if (updater != null) {
 			updater.cancel();
 		}
 		updater = null;
 		ds.getParties().remove(this.player.getName());
 	}
 }

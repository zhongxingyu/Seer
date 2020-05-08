 package rpisdd.rpgme.gamelogic.player;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.items.Inventory;
 import rpisdd.rpgme.gamelogic.quests.QuestManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.support.v4.app.Fragment;
 
 public class Player {
 	final static int EXP_PER_LEVEL = 100;
 
 	private static Player player = null;
 	private final String name;
 	private final String classs;
 	private final int avatarId;
 
 	private final QuestManager questManager;
 	private final Stats stats;
 	private final Inventory inventory;
 
 	private int gold;
 	private int energy;
 
 	public Player(CharSequence name, CharSequence classs, int avatarId) {
 		this.name = name.toString();
 		this.classs = classs.toString();
 		this.avatarId = avatarId;
 		this.questManager = new QuestManager();
 		this.inventory = new Inventory();
 		this.stats = new Stats();
 		this.gold = 100;
 		this.energy = 10;
 	}
 
 	public int getAvatar() {
 		return this.avatarId;
 	}
 
 	public QuestManager getQuestManager() {
 		return questManager;
 	}
 
 	public Stats getStats() {
 		return stats;
 	}
 
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getClasss() {
 		return classs;
 	}
 
 	public int getGold() {
 		return gold;
 	}
 
 	public void addGold(int amount) {
 		gold += amount;
 	}
 
 	public void deductGold(int amount) {
 		gold -= amount;
 		if (gold < 0) {
 			gold = 0;
 		}
 	}
 
 	/*
 	 * returns the player's current energy
 	 */
 	public int getEnergy() {
 		return stats.getBaseEnergy();
 	}
 
 	/*
 	 * Returns true if the player is unconscious and false otherwise.
 	 */
 	public boolean isConscious() {
 		return energy > 0;
 	}
 
 	/*
 	 * Increases the player's current energy
 	 */
 	public void addEnergy(int amount) {
 		energy += amount;
 		if (energy > getMaxEnergy()) {
 			energy = getMaxEnergy();
 		}
 	}
 
 	/*
 	 * Decreases the player's current energy
 	 */
 	public void deductEnergy(int amount) {
 		energy -= amount;
 		if (energy < 0) {
 			energy = 0;
 		}
 	}
 
 	/*
 	 * Returns exp to next level up
 	 */
 	public int getExpForLevel(int aLevel) {
 		return EXP_PER_LEVEL * aLevel;
 	}
 
 	public int getNextExp() {
 		return getExpForLevel(getLevel()) - getTotalExp();
 	}
 
 	/*
 	 * Returns total exp accumulated.
 	 */
 	public int getTotalExp() {
 		return stats.getExp();
 	}
 
 	/*
 	 * Increases earned exp
 	 */
 	public void addExp(int amount, Reward reward) {
 		stats.incExp(amount);
 		if (stats.getExp() >= getExpForLevel(getLevel())) {
 			levelUp(reward);
 		}
 	}
 
 	/*
 	 * Returns the player's level
 	 */
 	public int getLevel() {
 		return stats.getLevel();
 	}
 
 	/*
 	 * Levels up the player
 	 */
 	public void levelUp(Reward reward) {
 		incMaxEnergy(5);
 		stats.incrementLevel();
 		reward.setEnergyGained(5);
 		reward.setNewLevel(stats.getLevel());
 		reward.setIsLevelUp(true);
 	}
 
 	// Stat functions/////////////////////////////////////////////
 
 	/*
 	 * Returns player's max energy
 	 */
 	public int getMaxEnergy() {
 		return stats.getBaseEnergy();
 	}
 
 	/*
 	 * Increases the player's max energy
 	 */
 	public void incMaxEnergy(int amount) {
 		stats.incBaseEnergy(amount);
 	}
 
 	/*
 	 * Returns the player's strength
 	 */
 	public int getStrength() {
 		return stats.getBaseStr();
 	}
 
 	public int getStrAtk() {
 		int total = stats.getBaseStr();
 		if (inventory.getWeapon() != null) {
 			total += inventory.getWeapon().getMod().str;
 		}
 		return total;
 	}
 
 	public int getStrDef() {
 		int total = stats.getBaseStr();
 		if (inventory.getArmor() != null) {
 			total += inventory.getArmor().getMod().str;
 		}
 		return total;
 	}
 
 	/*
 	 * Increases the player's strength
 	 */
 	public void incStrength(int amount) {
 		stats.incBaseStr(amount);
 	}
 
 	/*
 	 * Returns the player's intelligence
 	 */
 	public int getInt() {
 		return stats.getBaseInt();
 	}
 
 	public int getIntAtk() {
 		int total = stats.getBaseInt();
 		if (inventory.getWeapon() != null) {
 			total += inventory.getWeapon().getMod().intel;
 		}
 		return total;
 	}
 
 	public int getIntDef() {
 		int total = stats.getBaseInt();
 		if (inventory.getArmor() != null) {
 			total += inventory.getArmor().getMod().intel;
 		}
 		return total;
 	}
 
 	/*
 	 * Increases the player's intelligence
 	 */
 	public void incInt(int amount) {
 		stats.incBaseInt(amount);
 	}
 
 	/*
 	 * Returns the player's will
 	 */
 	public int getWill() {
 		return stats.getBaseWill();
 	}
 
 	public int getWillAtk() {
 		int total = stats.getBaseWill();
 		if (inventory.getWeapon() != null) {
 			total += inventory.getWeapon().getMod().will;
 		}
 		return total;
 	}
 
 	public int getWillDef() {
 		int total = stats.getBaseWill();
 		if (inventory.getArmor() != null) {
 			total += inventory.getArmor().getMod().will;
 		}
 		return total;
 	}
 
 	/*
 	 * Increases the player's will
 	 */
 	public void incWill(int amount) {
 		stats.incBaseWill(amount);
 	}
 
 	/*
 	 * Returns the player's spirit
 	 */
 	public int getSpirit() {
 		return stats.getBaseSpr();
 	}
 
 	public int getSprAtk() {
 		int total = stats.getBaseSpr();
 		if (inventory.getWeapon() != null) {
 			total += inventory.getWeapon().getMod().spirit;
 		}
 		return total;
 	}
 
 	public int getSprDef() {
 		int total = stats.getBaseSpr();
 		if (inventory.getArmor() != null) {
 			total += inventory.getArmor().getMod().spirit;
 		}
 		return total;
 	}
 
 	/*
 	 * Increases the player's spirit
 	 */
 	public void incSpirit(int amount) {
 		stats.incBaseSpr(amount);
 	}
 
 	public int getStat(StatType type) {
 
 		switch (type) {
 		case STRENGTH:
 			return getStrength();
 		case INTELLIGENCE:
 			return getInt();
 		case WILL:
 			return getWill();
 		case SPIRIT:
 			return getSpirit();
 		default:
 			return -1;
 		}
 	}
 
 	// ///////////////////////////////////////////////
 
 	public static Player getPlayer() {
 		return player;
 	}
 
 	// Todo: Eliminate this function, and replace all occurrences of
 	// getPlayer(this) with getPlayer()
 	public static Player getPlayer(Fragment fragment) {
 		return player;
 	}
 
 	public void savePlayer(Activity activity) {
 		questManager.saveQuestsToDatabase(activity);
 		inventory.saveItemsToDatabase(activity);
 		SharedPreferences p = activity.getSharedPreferences("player",
 				Context.MODE_PRIVATE);
 		Editor e = p.edit();
 		stats.save(e);
 		e.putString("name", name);
 		e.putString("class", classs);
 		e.putInt("avatarId", avatarId);
 		e.putBoolean("playerExists", true);
 		e.putInt("gold", gold);
 		e.commit();
 	}
 
 	public static void loadPlayer(Activity activity) {
 
 		SharedPreferences pref = activity.getSharedPreferences("player",
 				Context.MODE_PRIVATE);
 		assert pref.getBoolean("playerExists", false);
 		Player p = new Player(pref.getString("name", "Missingno"),
 				pref.getString("class", "Bird, Water"), pref.getInt("avatarId",
 						R.drawable.splash_screen));
 		p.questManager.loadQuestsFromDatabase(activity);
 		p.inventory.loadItemsFromDatabase(activity);
 		p.stats.load(pref);
 		p.gold = pref.getInt("gold", 100);
 		player = p;
 	}
 
 	public static void createPlayer(CharSequence name, CharSequence classs,
 			int avatarId) {
 		assert player == null;
 		player = new Player(name, classs, avatarId);
 	}
 
 }

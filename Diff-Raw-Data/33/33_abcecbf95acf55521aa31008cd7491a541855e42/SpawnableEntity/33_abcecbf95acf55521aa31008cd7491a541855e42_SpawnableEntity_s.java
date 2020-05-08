 package com.github.thebiologist13;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Villager;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 import com.github.thebiologist13.serialization.SInventory;
 import com.github.thebiologist13.serialization.SItemStack;
 import com.github.thebiologist13.serialization.SPotionEffect;
 import com.github.thebiologist13.serialization.SVector;
 
 public class SpawnableEntity implements Serializable {
 	
 	/*
 	 * Spawnable Entities are entities that
 	 * Custom Spawners can spawn. They have lots
 	 * of data associated with them.
 	 */
 	
 	private static final long serialVersionUID = -60000847475741355L;
 	//Damage Blacklist
 	private ArrayList<String> blacklist = new ArrayList<String>();
 	private Map<String, Object> data = new HashMap<String, Object>();
 	//Drops
 	private ArrayList<SItemStack> drops = new ArrayList<SItemStack>();
 	//Basic Data
 	private ArrayList<SPotionEffect> effects = new ArrayList<SPotionEffect>();
 	//Items that do or do not inflict damage
 	private ArrayList<SItemStack> itemDamage = new ArrayList<SItemStack>(); 
 	//Damage Whitelist
 	private ArrayList<String> whitelist = new ArrayList<String>();
 	
 	//Initialize a SpawnableEntity
 	public SpawnableEntity(EntityType type, int id) {
 		this.data.put("type", type.getName());
 		this.data.put("id", id);
 		this.data.put("useWhitelist", false);
 		this.data.put("useBlacklist", true);
 	}
 
 	public void addDamageBlacklist(String damageType) {
		blacklist.add(damageType);
 	}
 	
 	public void addDamageWhitelist(String damageType) {
		whitelist.add(damageType);
 	}
 	
 	public void addDrop(ItemStack drop) {
 		drops.add(new SItemStack(drop));
 	}
 	
 	public void addInventoryItem(ItemStack stack) {
 		SInventory newInv = getInventory();
 		newInv.addItem(stack);
 		setInventory(newInv);
 	}
 
 	public void addItemDamage(ItemStack value) {
 		itemDamage.add(new SItemStack(value));
 	}
 	
 	public void addPotionEffect(SPotionEffect effect) {
 		effects.add(effect);
 	}
 	
 	public SpawnableEntity cloneWithNewId(int id) {
 		SpawnableEntity e = new SpawnableEntity(getType(), id);
 		e.setData(data);
 		e.setDamageBlacklist(blacklist);
 		e.setDamageWhitelist(whitelist);
 		e.setItemDamageList(getItemDamageList());
 		e.setEffects(effects);
 		return e;
 	}
 	
 	public int getAge() {
 		return (this.data.containsKey("age")) ? (Integer) this.data.get("age") : 0;
 	}
 	
 	public int getAir() {
 		return (this.data.containsKey("air")) ? (Integer) this.data.get("air") : 0;
 	}
 	
 	public String getCatType() {
 		return (this.data.containsKey("catType")) ? (String) this.data.get("catType") : "WILD_OCELOT";
 	}
 	
 	public String getColor() {
 		return (this.data.containsKey("color")) ? (String) this.data.get("color") : "WHITE";
 	}
 	
 	public int getDamage() {
 		return (this.data.containsKey("damage")) ? (Integer) this.data.get("damage") : 2;
 	}
 	
 	public ArrayList<String> getDamageBlacklist() {
 		return blacklist;
 	}
 	
 	public ArrayList<String> getDamageWhitelist() {
 		return whitelist;
 	}
 	
 	public int getDroppedExp() {
 		return (this.data.containsKey("exp")) ? (Integer) this.data.get("exp") : 1;
 	}
 	
 	public ArrayList<ItemStack> getDrops() {
 		
 		ArrayList<ItemStack> drops1 = new ArrayList<ItemStack>();
 		
 		for(SItemStack i : drops) {
 			drops1.add(i.toItemStack());
 		}
 		
 		return drops1;
 	}
 	
 	public ArrayList<SPotionEffect> getEffects() {
 		return effects;
 	}
 
 	public MaterialData getEndermanBlock() {
 		return (this.data.containsKey("enderBlock")) ? new MaterialData((Integer) this.data.get("enderBlock")) : new MaterialData(1);
 	}
 
 	public int getFireTicks() {
 		return (this.data.containsKey("fire")) ? (Integer) this.data.get("fire") : 0;
 	}
 
 	public int getFuseTicks() {
 		return (this.data.containsKey("fuse")) ? (Integer) this.data.get("fuse") : 80;
 	}
 
 	public int getHealth() {
 		return (this.data.containsKey("health")) ? (Integer) this.data.get("health") : 1;
 	}
 	
 	public float getHeight() {
 		return (this.data.containsKey("height")) ? (Float) this.data.get("height") : -1f;
 	}
 
 	public int getId() {
 		return (Integer) this.data.get("id");
 	}
 
 	public SInventory getInventory() {
 		return (this.data.containsKey("inv")) ? (SInventory) this.data.get("inv") : new SInventory();
 	}
 
 	public ArrayList<ItemStack> getItemDamageList() {
 		
 		ArrayList<ItemStack> damage1 = new ArrayList<ItemStack>();
 		
 		for(SItemStack i : itemDamage) {
 			damage1.add(i.toItemStack());
 		}
 		
 		return damage1;
 	}
 
 	public ItemStack getItemType() {
 		return (this.data.containsKey("itemType")) ? ((SItemStack) this.data.get("itemType")).toItemStack() : new ItemStack(1, 1, (short) 0);
 	}
 
 	public float getLength() {
 		return (this.data.containsKey("length")) ? (Float) this.data.get("length") : -1f;
 	}
 
 	public int getMaxAir() {
 		return (this.data.containsKey("maxAir")) ? (Integer) this.data.get("maxAir") : 200;
 	}
 
 	public int getMaxHealth() {
 		return (this.data.containsKey("maxHealth")) ? (Integer) this.data.get("maxHealth") : 20;
 	}
 
 	public String getName() {
 		return (this.data.containsKey("name")) ? (String) this.data.get("name") : "";
 	}
 
 	public SPotionEffect getPotionEffect() {
 		return (this.data.containsKey("potionEffect")) ? (SPotionEffect) this.data.get("potionEffect") : new SPotionEffect(PotionEffectType.REGENERATION, 1, 0);
 	}
 
 	public Villager.Profession getProfession() {
 		return (this.data.containsKey("profession")) ? Villager.Profession.valueOf((String) this.data.get("profession")) : Villager.Profession.FARMER;
 	}
 
 	public Object getProp(String key) {
 		return (this.data.containsKey(key)) ? this.data.get(key) : null;
 	}
 
 	public int getSlimeSize() {
 		return (this.data.containsKey("slimeSize")) ? (Integer) this.data.get("slimeSize") : 1;
 	}
 
 	public EntityType getType() {
 		String name = (String) this.data.get("type");
		System.out.println(name);
 		for(EntityType e1 : EntityType.values()) {
			System.out.println(e1.toString());
 			if(e1.toString().equalsIgnoreCase(name)) {
 				return e1;
 			}
 		}
 		return null;
 	}
 
 	public Vector getVelocity() {
 		return (this.data.containsKey("velocity")) ? ((SVector) this.data.get("velocity")).toVector() : new Vector(0, 0, 0);
 	}
 
 	public float getWidth() {
 		return (this.data.containsKey("width")) ? (Float) this.data.get("width") : -1f;
 	}
 
 	public double getXVelocity() {
 		return (this.data.containsKey("xVelo")) ? (Double) this.data.get("xVelo") : 0d;
 	}
 
 	public float getYield() {
 		
 		if(this.data.containsKey("yield")) {
 			double d = (Double) this.data.get("yield");
 			return (float) d;
 		} else {
 			return 4.0f;
 		}
 		
 	}
 
 	public double getYVelocity() {
 		return (this.data.containsKey("yVelo")) ? (Double) this.data.get("yVelo") : 0d;
 	}
 
 	public double getZVelocity() {
 		return (this.data.containsKey("zVelo")) ? (Double) this.data.get("zVelo") : 0d;
 	}
 
 	public boolean hasAllDimensions() {
 		return (this.data.containsKey("height") &&
 				this.data.containsKey("width") && 
 				this.data.containsKey("length")) ? true : false;
 	}
 
 	public boolean hasProp(String key) {
 		return this.data.containsKey(key);
 	}
 
 	public boolean isAngry() {
 		return (this.data.containsKey("angry")) ? (Boolean) this.data.get("angry") : false;
 	}
 
 	public boolean isCharged() {
 		return (this.data.containsKey("charged")) ? (Boolean) this.data.get("charged") : false;
 	}
 
 	public boolean isIncendiary() {
 		return (this.data.containsKey("incendiary")) ? (Boolean) this.data.get("incendiary") : false;
 	}
 
 	public boolean isInvulnerable() {
 		return (this.data.containsKey("invul")) ? (Boolean) this.data.get("invul") : false;
 	}
 
 	public boolean isJockey() {
 		return (this.data.containsKey("jockey")) ? (Boolean) this.data.get("jockey") : false;
 	}
 
 	public boolean isPassive() {
 		return (this.data.containsKey("passive")) ? (Boolean) this.data.get("passive") : false;
 	}
 
 	public boolean isSaddled() {
 		return (this.data.containsKey("saddle")) ? (Boolean) this.data.get("saddle") : false;
 	}
 
 	public boolean isSitting() {
 		return (this.data.containsKey("sit")) ? (Boolean) this.data.get("sit") : false;
 	}
 
 	public boolean isTamed() {
 		return (this.data.containsKey("tame")) ? (Boolean) this.data.get("tame") : false;
 	}
 
 	public boolean isUsingBlacklist() {
 		return (this.data.containsKey("useBlacklist")) ? (Boolean) this.data.get("useBlacklist") : true;
 	}
 
 	public boolean isUsingCustomDamage() {
 		return (this.data.containsKey("useCustomDamage")) ? (Boolean) this.data.get("useCustomDamage") : false;
 	}
 
 	public boolean isUsingCustomDrops() {
 		return (this.data.containsKey("useDrops")) ? (Boolean) this.data.get("useDrops") : false;
 	}
 
 	public boolean isUsingInventory() {
 		return (this.data.containsKey("useInventory")) ? (Boolean) this.data.get("useInventory") : false;
 	}
 
 	public boolean isUsingWhitelist() {
 		return (this.data.containsKey("useWhitelist")) ? (Boolean) this.data.get("useWhitelist") : false;
 	}
 
 	public void remove() {
 		this.data.put("id", -1);
 	}
 
 	public boolean requiresBlockBelow() {
 		return (this.data.containsKey("blockBelow")) ? (Boolean) this.data.get("blockBelow") : true;
 	}
 	
 	public void setAge(int age) {
 		this.data.put("age", age);
 	}
 	
 	public void setAir(int air) {
 		this.data.put("air", air);
 	}
 
 	public void setAngry(boolean angry) {
 		this.data.put("angry", angry);
 	}
 
 	public void setBlockBelow(boolean value) {
 		this.data.put("blockBelow", value);
 	}
 
 	public void setCatType(String catType) {
 		this.data.put("catType", catType);
 	}
 
 	public void setCharged(boolean isCharged) {
 		this.data.put("charged", isCharged);
 	}
 	
 	public void setColor(String color) {
 		this.data.put("color", color);
 	}
 
 	public void setDamage(int damage) {
 		this.data.put("damage", damage);
 	}
 
 	public void setDamageBlacklist(ArrayList<String> damageBlacklist) {
 		this.blacklist = damageBlacklist;
 	}
 	
 	public void setDamageWhitelist(ArrayList<String> damageWhitelist) {
 		this.whitelist = damageWhitelist;
 	}
 
 	public void setData(Map<String, Object> data) {
 		
 		if(data == null)
 			return;
 		
 		if(this.data.containsKey("id")) {
 			data.put("id", (Integer) this.data.get("id"));
 		}
 		
 		if(this.data.containsKey("name")) {
 			data.put("name", (String) this.data.get("name"));
 		}
 		
 		this.data = data;
 	}
 
 	public void setDimensions(float height, float width, float length) {
 		setHeight(height);
 		setWidth(width);
 		setLength(length);
 	}
 
 	public void setDroppedExp(int droppedExp) {
 		this.data.put("exp", droppedExp);
 	}
 
 	public void setDrops(ArrayList<ItemStack> drops) {
 		
 		ArrayList<SItemStack> drops1 = new ArrayList<SItemStack>();
 		
 		for(ItemStack i : drops) {
 			drops1.add(new SItemStack(i));
 		}
 		
 		this.drops = drops1;
 	}
 
 	public void setEffects(ArrayList<SPotionEffect> effects) {
 		this.effects = effects;
 	}
 
 	public void setEndermanBlock(MaterialData endermanBlock) {
 		this.data.put("enderBlock", endermanBlock.getItemTypeId());
 	}
 	
 	public void setFireTicks(int fireTicks) {
 		this.data.put("fireTicks", fireTicks);
 	}
 
 	public void setFuseTicks(int fuseTicks) {
 		this.data.put("fuse", fuseTicks);
 	}
 
 	public void setHealth(int health) {
 		this.data.put("health", health);
 	}
 
 	public void setHeight(float height) {
 		this.data.put("height", height);
 	}
 
 	public void setIncendiary(boolean incendiary) {
 		this.data.put("incendiary", incendiary);
 	}
 
 	public void setInventory(SInventory inventory) {
 		this.data.put("inv", inventory);
 	}
 
 	public void setInvulnerable(boolean invulnerable) {
 		this.data.put("invul", invulnerable);
 	}
 
 	public void setItemDamageList(ArrayList<ItemStack> itemDamage) {
 		
 		ArrayList<SItemStack> damage1 = new ArrayList<SItemStack>();
 		
 		for(ItemStack i : itemDamage) {
 			damage1.add(new SItemStack(i));
 		}
 		
 		this.itemDamage = damage1;
 		
 	}
 
 	public void setItemType(ItemStack itemType) {
 		this.data.put("itemType", new SItemStack(itemType));
 	}
 
 	public void setJockey(boolean isJockey) {
 		this.data.put("jockey", isJockey);
 	}
 
 	public void setLength(float length) {
 		this.data.put("length", length);
 	}
 
 	public void setMaxAir(int maxAir) {
 		this.data.put("maxAir", maxAir);
 	}
 
 	public void setMaxHealth(int maxHealth) {
 		this.data.put("maxHealth", maxHealth);
 	}
 
 	public void setName(String name) {
 		this.data.put("name", name);
 	}
 
 	public void setPassive(boolean passive) {
 		this.data.put("passive", passive);
 	}
 
 	public void setPotionEffect(SPotionEffect potionEffect) {
 		this.data.put("potionEffect", potionEffect);
 	}
 
 	public void setProfession(Villager.Profession villagerProfession) {
 		this.data.put("profession", villagerProfession.toString());
 	}
 	
 	public void setProp(String key, Object value) {
 		this.data.put(key, value);
 	}
 
 	public void setSaddled(boolean isSaddled) {
 		this.data.put("saddle", isSaddled);
 	}
 
 	public void setSitting(boolean isSitting) {
 		this.data.put("sit", isSitting);
 	}
 
 	public void setSlimeSize(int slimeSize) {
 		this.data.put("slimeSize", slimeSize);
 	}
 
 	public void setTamed(boolean isTamed) {
 		this.data.put("tame", isTamed);
 	}
 
 	public void setType(EntityType type) {
 		this.data.put("type", type.toString());
 	}
 
 	public void setUseBlacklist(boolean useBlacklist) {
 		
 		this.data.put("useBlacklist", useBlacklist);
 		this.data.put("useWhitelist", !useBlacklist);
 		
 	}
 
 	public void setUseWhitelist(boolean useWhitelist) {
 		
 		this.data.put("useWhitelist", useWhitelist);
 		this.data.put("useBlacklist", !useWhitelist);
 		
 	}
 
 	public void setUsingCustomDamage(boolean useCustomDamage) {
 		this.data.put("useCustomDamage", useCustomDamage);
 	}
 
 	public void setUsingCustomDrops(boolean useCustomDrops) {
 		this.data.put("useDrops", useCustomDrops);
 	}
 
 	public void setUsingInventory(boolean usingInventory) {
 		this.data.put("useInventory", usingInventory);
 	}
 
 	public void setVelocity(SVector velocity) {
 		this.data.put("velocity", velocity);
 		setXVelocity(velocity.getX());
 		setYVelocity(velocity.getY());
 		setZVelocity(velocity.getZ());
 	}
 
 	public void setWidth(float width) {
 		this.data.put("width", width);
 	}
 
 	public void setXVelocity(double xVelocity) {
 		this.data.put("xVelo", xVelocity);
 	}
 
 	public void setYield(float yield) {
 		this.data.put("yield", (double) yield);
 	}
 
 	public void setYVelocity(double yVelocity) {
 		this.data.put("yVelo", yVelocity);
 	}
 	
 	public void setZVelocity(double zVelocity) {
 		this.data.put("zVelo", zVelocity);
 	}
 
 }

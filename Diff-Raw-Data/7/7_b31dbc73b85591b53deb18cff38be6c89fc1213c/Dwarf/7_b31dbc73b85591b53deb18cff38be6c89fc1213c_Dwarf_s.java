 package com.smartaleq.bukkit.dwarfcraft;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class Dwarf {
 
 	public List <Skill> skills;
 	public boolean isElf;
 	public Player player;
 		
 	public Dwarf(Player whoami) {
 		player = whoami;
 	}
 	
 	/**
 	 * Finds a dwarf from the server's static list based on player's name
 	 * @param player
 	 * @return dwarf or null
 	 */
 	public static Dwarf find(Player player) {
 		for(Dwarf d:DataManager.getDwarves()) {
 			if (d != null)
 				if (d.player != null)
 					if (d.player.getName().equalsIgnoreCase(player.getName()))
 						return d;
 		}
 		return null;
 	}
 
 	public static Dwarf findOffline(String name) {
 		Dwarf dwarf = createDwarf(null);
 		if(DataManager.getDwarfData(dwarf, name)) return dwarf;
 		else{
 			//No dwarf or data found
 			return null;
 		}
 	}
 
 	public static Dwarf createDwarf(Player player){
 		Dwarf newDwarf = new Dwarf(player);
 		newDwarf.isElf = false;
 		newDwarf.skills = ConfigManager.getAllSkills();
 		for (Skill skill:newDwarf.skills) if(skill != null) skill.level = 0;
 		if(player!=null) DataManager.dwarves.add(newDwarf);
 		return newDwarf;
 	}
 
 	/** 
 	 * Removes a Dwarf from the database. Only used for debugging/banning.
 	 */
 	void remove(){
 		DataManager.removeDwarf(this);
 	}
 	
 	/**
 	 * Calculates the Dwarf's total Level
 	 * @return total level
 	 */
 	int level(){
 		int playerLevel = 5;
 		int highestSkill = 0;
 		for(Skill s:skills){
 			if(s.level > highestSkill) highestSkill = s.level;
 			if(s.level > 5) playerLevel += s.level - 5;;
 		}
 		if(playerLevel == 5) playerLevel = highestSkill;
 		return playerLevel;
 	}
 	
 	public List<ItemStack> calculateTrainingCost(Skill skill) {
 		int highSkills = countHighSkills();
 		int dwarfLevel = getDwarfLevel();
 		int quartileSize = Math.min(4,highSkills/4);
 		int quartileNumber = 1; //1 = top, 2 = 2nd, etc.
 		int[] levelList = new int[highSkills+1];
 		List <ItemStack> trainingStack = new ArrayList<ItemStack>();
 		int i = 0;
 		
 		//Creates an ordered list of skill levels and finds where in that list the skill is (what quartile)
 		if (DwarfCraft.debugMessagesThreshold < 2) System.out.println("Debug Message: starting skill ordering for quartiles");
 		for (Skill s:skills){
 			if(s==null)continue;
 			if (s.level > 5){
 				levelList[i] = s.level;
 				i++;}}
 		Arrays.sort(levelList);
 		if (levelList[highSkills - quartileSize]<=skill.level) quartileNumber = 1 ;
 		else if (levelList[highSkills - 2 * quartileSize]<=skill.level) quartileNumber = 2 ;
 		else if (levelList[highSkills - 3 * quartileSize]<=skill.level) quartileNumber = 3 ;
 		if (skill.level < 5) quartileNumber = 1;   //low skills train full speed
 		
 		//calculate quartile penalties for 2nd/3rd/4th quartile
 		double multiplier = skill.baseTrainingMultiplier();
 		if (quartileNumber == 2) multiplier *= (1 + 1* dwarfLevel/(100 + 3*dwarfLevel));
 		if (quartileNumber == 3) multiplier *= (1 + 2* dwarfLevel/(100 + 3*dwarfLevel));
 		if (quartileNumber == 4) multiplier *= (1 + 3* dwarfLevel/(100 + 3*dwarfLevel));
 		
 		//create output item stack of new items
 		for(ItemStack item:skill.trainingCost){
 			if (item.getAmount() != 0){
 				trainingStack.add(new ItemStack(item.getTypeId(), (int) Math.floor(item.getAmount()*multiplier)));
 				i++;
 			}
 		}
 		return trainingStack;
 	}
 
 	/**
 	 * Counts skills greater than level 5, used for training costs
 	 */
 	private int countHighSkills() {
 		int highCount = 0;
 		for (Skill s:skills) {
 			if (s!=null) if(s.level>5) highCount++;
 		}
 		return highCount;
 	}
 
 	/**
 	 * makes the Dwarf into an elf
 	 * @return
 	 */
 	public boolean makeElf(){
 		if (isElf) return false;
 		isElf = true;
 		for (Skill skill: skills) if(skill!=null) skill.level = 0;
 		DataManager.saveDwarfData(this);
 		return isElf;
 	}
 	
 	/**
 	 * Makes an elf into a dwarf
 	 */
 	public boolean makeElfIntoDwarf(){
 		isElf = false;
 		for (Skill skill: skills) if(skill!=null) skill.level = 0;
 		DataManager.saveDwarfData(this);
 		return true;
 	}
 	
 	/**
 	 * Gets a dwarf's skill by name or id number(as String)
 	 * @param skillName
 	 * @return Skill or null if none found
 	 */
 	public Skill getSkill(String skillName){
 		try{
 			return getSkill(Integer.parseInt(skillName));
 		}
 		catch (NumberFormatException n){
 			for (Skill skill: skills){
 				if (skill.displayName.equalsIgnoreCase(skillName)) return skill;
 				if (skill.toString().equalsIgnoreCase(skillName)) return skill;
 				if (skill.displayName.toLowerCase().regionMatches(0, skillName.toLowerCase(), 0, 5)) return skill;
 				if (skill.toString().toLowerCase().regionMatches(0, skillName.toLowerCase(), 0, 5)) return skill;
 			}
 			
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets a dwarf's skill by id
 	 * @param skillId
 	 * @return Skill or null if none found
 	 */
 	public Skill getSkill(int skillId){
 		for (Skill skill: skills){
 			if (skill.id == skillId) return skill;
 		}
 		return null;
 		}
 	
 	/**
 	 * Gets a dwarf's skill from an effect
 	 * @param effect (does not have to be this dwarf's effect, only used for ID#)
 	 * @return Skill or null if none found
 	 */
 	public Skill getSkill(Effect effect) {
 		for (Skill skill: skills){
 			if (skill.id == effect.id/10) return skill;
 		}
 		return null;
 	}
 	
 	/**
 	 * Counts items in a dwarf's inventory
 	 * @param itemId
 	 * @return total item count int
 	 */
 	public int countItem(int itemId) {
 		int itemCount = 0;
 		ItemStack[] items = player.getInventory().getContents();
 		for(ItemStack item: items){
 			if(item.getTypeId() == itemId){
 				itemCount += item.getAmount();
 			}
 		}
 		return itemCount;
 	}
 	
 	/**
 	 * Counts items in a dwarf's inventory
 	 * @param material
 	 * @return total item count int
 	 */
 	public int countItem(Material material) {
 		return countItem(material.getId());
 	}
 
 	/**
 	 * Removes a set number of items from an inventory
 	 * No error checking, should only be used if the items have already been counted
 	 * @param itemId
 	 * @param amount
 	 */
 	// TODO fix buggy inventory shiznit
 	public void removeInventoryItems(int itemId, int amount){
 		Inventory inventory = player.getInventory();
 		ItemStack[] items = inventory.getContents();
 		int amountLeft = amount;
 			for(int i=0; i<40; i++){
 				if(items[i].getTypeId() == itemId){
 					if(items[i].getAmount() > amountLeft){
 						items[i].setAmount(items[i].getAmount()-amountLeft);
 						break;
 						
 					}
 					else if(items[i].getAmount() == amountLeft){
						inventory.remove(items[i]);
 						break;
 					}
 					else {
 						amountLeft = amountLeft - items[i].getAmount();
						inventory.remove(items[i]);
 					}
 				}
 			}
 				//don't think this is required			
 //		inventory.setContents(items);
 	}
 
 	public boolean isInSchoolZone(School school){
 		World world = player.getWorld();
 		Vector victor = player.getLocation().toVector();
 		List <TrainingZone> schoolZones = DataManager.getSchoolZones(world);
 		for(TrainingZone zone: schoolZones){
 			if (DwarfCraft.debugMessagesThreshold < 7) System.out.println("Debug Message: zone:"+zone.name);
 			if (victor.isInAABB(zone.lowCorner, zone.highCorner) && zone.school == school && world == zone.world) return true;
 		}
 		return false;
 	}
 	
 	public List<TrainingZone> listAllZones(){
 		World world = player.getWorld();
 		Vector victor = player.getLocation().toVector();
 		if (DwarfCraft.debugMessagesThreshold < 7) System.out.println("Debug Message: player at x,y,z:"+victor.getX()+","+victor.getY()+","+victor.getZ());
 		List <TrainingZone> schoolZones = DataManager.getSchoolZones(world);
 		List <TrainingZone> zonesHere = new ArrayList <TrainingZone> ();
 		for(TrainingZone zone: schoolZones){
 			if (DwarfCraft.debugMessagesThreshold < 7) System.out.println("Debug Message: zone:"+zone.name);
 			if (victor.isInAABB(zone.lowCorner, zone.highCorner)){
 				zonesHere.add(zone);
 			}
 		}
 		return zonesHere;
 	}
 	
 	public int countSkills() {
 		int count = 0;
 		for (Skill s:skills) if(s != null) count++;
 		return count;
 	}
 
 	/**
 	 * Retrieves an effect from a player based on its effectId.
 	 * @param effectId
 	 * @return
 	 */
 	public Effect getEffect(int effectId) {
 		Skill skill = getSkill(effectId/10);
 		for(Effect effect:skill.effects){
 			if(effect.id == effectId) return effect;
 		}
 		return null;
 	}
 
 	/**
 	 * Calculates the dwarf's total level for display/e-peening. Value is the total of all skill level above 5, or
 	 * the highest skill level when none are above 5.
 	 * @return
 	 */
 	public int getDwarfLevel() {
 		if(isElf) return 0;
 		int playerLevel = 5;
 		int highestSkill = 0;
 		for(Skill s:skills){
 			if(s==null) continue;
 			if(s.level > highestSkill){highestSkill = s.level;};
 			if(s.level > 5){playerLevel = playerLevel + s.level - 5;};
 		}
 		if(playerLevel == 5){playerLevel = highestSkill;};
 		return playerLevel;
 	}
 
 	public void sendMessage(String string) {
 		player.sendMessage(string);
 	}
 
 	public boolean isElf() {
 		if (isElf) return true;
 		return false;
 	}
 
 	public int skillLevel(int i) {
 		for (Skill s: skills) if (s.id == i) return s.level;
 		return 0;
 	}
 
 	
 }

 package com.TeamNovus.Supernaturals.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 
 import com.TeamNovus.Supernaturals.Player.Class.Ability;
 import com.TeamNovus.Supernaturals.Player.Class.Power;
 import com.google.common.collect.LinkedListMultimap;
 
 public abstract class SNClass {
 	private String name;
 	private ChatColor color;
 	private Integer maxLevel;
 	
 	private HashMap<Integer, Integer> maxMana = new HashMap<Integer, Integer>();
 	private HashMap<Integer, Integer> maxHealth = new HashMap<Integer, Integer>(); 
 	private HashMap<Integer, Integer> maxFoodLevel = new HashMap<Integer, Integer>(); 
 	private HashMap<Integer, Float> speed = new HashMap<Integer, Float>();  
 	private LinkedListMultimap<Integer, Power> powers = LinkedListMultimap.create();
 	private LinkedListMultimap<Integer, Ability> abilities = LinkedListMultimap.create();
 	
 	private LinkedListMultimap<Integer, SNClass> joinableClasses = LinkedListMultimap.create();
 	
 	public SNClass(String name, ChatColor color, Integer maxLevel) {
 		this.name = name;
 		this.color = color;
 		this.maxLevel = maxLevel;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public ChatColor getColor() {
 		return color;
 	}
 	
 	public Integer getMaxLevel() {
 		return maxLevel;
 	}
 		
 	public Integer getMaxMana(Integer level) {
 		Integer max = 0;
 		
 		for(Integer i : maxMana.keySet()) {
 			if(i > level) break;
 			
 			max = maxMana.get(i);
 		}
 		
 		return max;
 	}
 	
 	public void setMaxMana(Integer level, Integer maxMana) {
 		this.maxMana.put(level, maxMana);
 	}
 	
 	public Integer getMaxHealth(Integer level) {
 		Integer max = 0;
 		
 		for(Integer i : maxHealth.keySet()) {
 			if(i > level) break;
 			
 			max = maxHealth.get(i);
 		}
 		
 		return max;
 	}
 	
 	public void setMaxHealth(Integer level, Integer maxHealth) {
 		this.maxHealth.put(level, maxHealth);
 	}
 	
 	public Integer getMaxFoodLevel(Integer level) {
 		Integer max = 0;
 		
 		for(Integer i : maxFoodLevel.keySet()) {
 			if(i > level) break;
 			
 			max = maxFoodLevel.get(i);
 		}
 		
 		return max;
 	}
 	
 	public void setMaxFoodLevel(Integer level, Integer maxFoodLevel) {
 		this.maxFoodLevel.put(level, maxFoodLevel);
 	}
 	
 	public Float getSpeed(Integer level) {
 		Float max = 0f;
 		
 		for(Integer i : speed.keySet()) {
 			if(i > level) break;
 			
 			max = speed.get(i);
 		}
 		
 		return max;
 	}
 	
 	public void setSpeed(Integer level, Float speed) {
 		this.speed.put(level, speed);
 	}
 	
 	public List<Power> getPowers(Integer level) {
 		List<Power> data = new ArrayList<Power>();
 		
 		// Iterate through all the registered powers and levels.
 		for (Integer i : abilities.keySet()) {
 			if (i > level) break;
 			
 			// Setup the iterators
 			Iterator<Power> dataIterator = data.iterator();
 			Iterator<Power> toAddIterator = powers.get(i).iterator();
 			
 			// Loop through the powers of a level.
 			while (toAddIterator.hasNext()) {
 				Power t = toAddIterator.next();
 				
 				// Make sure there isn't an existing power with the same level.
 				while (dataIterator.hasNext()) {
 					Power d = dataIterator.next();
 					
 					if (d.getClass().equals(t.getClass())) {
 						dataIterator.remove();
 					}
 				}
 				
 				data.add(t);
 			}
 		}
 		
 		return data;
 	}
 	
 	public void addPower(Integer level, Power power) {
 		powers.put(level, power);
 	}
 	
 	public List<Ability> getAbilities(Integer level) {
 		List<Ability> data = new ArrayList<Ability>();
 		
 		// Iterate through all the registered abilities and levels.
 		for (Integer i : abilities.keySet()) {
 			if (i > level) break;
 			
 			// Setup the iterators
 			Iterator<Ability> toAddIterator = abilities.get(i).iterator();
 			
 			// Loop through the abilities of a level.
 			while (toAddIterator.hasNext()) {
 				Ability t = toAddIterator.next();
 				
 				Iterator<Ability> dataIterator = data.iterator();
 				
 				// Make sure there isn't an existing ability with the same level.
 				while (dataIterator.hasNext()) {
 					Ability d = dataIterator.next();
 					
 					if (d.getClass().equals(t.getClass())) {
 						dataIterator.remove();
 					}
 				}
 
 				data.add(t);
 			}
 		}
 		
 		return data;
 	}
 	
 	public void addAbility(Integer level, Ability ability) {
 		abilities.put(level, ability);
 	}
 	
 	public List<SNClass> getJoinableClasses(Integer level) {
 		List<SNClass> data = new ArrayList<SNClass>();
 		
 		// Iterate through all the registered abilities and levels.
 		for (Integer i : joinableClasses.keySet()) {
 			if (i > level) break;
 			
 			// Setup the iterators
 			Iterator<SNClass> toAddIterator = joinableClasses.get(i).iterator();
 			
 			// Loop through the abilities of a level.
 			while (toAddIterator.hasNext()) {
 				SNClass t = toAddIterator.next();
 				
 				Iterator<SNClass> dataIterator = data.iterator();
 				
 				// Make sure there isn't an existing ability with the same level.
 				while (dataIterator.hasNext()) {
 					SNClass d = dataIterator.next();
 					
 					if (d.getClass().equals(t.getClass())) {
 						dataIterator.remove();
 					}
 				}
 
 				data.add(t);
 			}
 		}
 		
 		return data;
 	}
 	
 	public void addJoinableClass(Integer level, SNClass joinableClass) {
 		joinableClasses.put(level, joinableClass);
 	}
 	
 }

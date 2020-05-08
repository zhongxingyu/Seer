 
 /**
  * Class Room - a room in an adventure game.
  *
  * This class is part of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  *
  * A "Room" represents one location in the scenery of the game.  It is 
  * connected to other rooms via exits.  The exits are labeled north, 
  * east, south, west.  For each direction, the room stores a reference
  * to the neighboring room, or null if there is no exit in that direction.
  * 
  * @author Micheal Hamon
  * @author Matthew Smith
  * @author Denis Dionne
  * @version 18/10/12
  */
 
 package courseProject.model;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.List;
 import java.util.HashMap;
 import java.util.ArrayList;
 
 
 public class Room
 {
 	protected String description;
     protected Map<ExitDirection,Room> exits;
     protected Inventory items;
     protected List<Monster> monsters;
     protected boolean visited;
 
     /**
      * Create a room described "description". Initially, it has
      * no exits. "description" is something like "a kitchen" or
      * "an open court yard".
      * @param description The room's description.
      */
     public Room(String description) 
     {
         this.description = description;
         exits = new HashMap<ExitDirection,Room>();
         items = new Inventory();
         monsters = new ArrayList<Monster>();
         visited = false;
     }
     /**
      * sets that the player has visited here
      */
     public void visit(){
     	visited = true;
     }
     /**
      * checks if the player has visited here
      * @return if the player has visited
      */
     public boolean visited(){
     	return visited;
     }
     
     /**
      * Copy Constructor for objects of class Room
      * @param r the room to copy from
      */
     public Room(Room r){
     	
     	exits = new HashMap<ExitDirection, Room>();
     	exits.putAll(r.exits);
     	
     	description = r.description;
     	items=new Inventory(r.items);
     	
     	List<Monster> monsterList = new ArrayList<Monster>();
     	for(Monster m: r.monsters){
     		monsterList.add(new Monster(m));
     	}
     	monsters  = monsterList;
     	
     	
     }
     
     protected ExitDirection reverseMapping(ExitDirection s){
     	switch(s) {
     	case north:
     		return ExitDirection.south;
     	case south:
     		return ExitDirection.north;
     	case east:
     		return ExitDirection.west;
     	case west:
 		default:
 			return ExitDirection.east;
     	}
     }
     
     public List<Monster> getMonsters(){
     	return monsters;
     }
     
     public List<Item> getItems(){
     	return items.getAllItems();
     }
 
     /**
      * Adds an exit to this room in the passed direction.
      * @param dir The direction of the exit.
      * @param exit The room the exit connects to.
      */
     public void addExit(ExitDirection dir, Room exit) 
     {
         exits.put(dir,exit);
     }
 
     /**
      * Gets the exit accociated with the passed direction
      * @param dir the direction of the exit to get
      * @return the connecting room in the passed direction
      */
     public Room getExit (ExitDirection dir){
         return exits.get(dir);
     }
     
     /**
      * returns a Map of each of the exits in a map with their direction as the key
      * @return the Map of the exits from the room.
      */
     public Map<ExitDirection,Room> getExitMap(){
     	return exits;
     }
     
     /**
      * add a monster to the room with the passed parameters
      * @param m the monster to add
      */
     public void addMonster(Monster m){
     	monsters.add(m);
     }
     
     /**
      * returns the Monster with the passed name
      * @param name the name of the Monster
      * @return the monster with the passed name
      */
     public Monster getMonster(String name){
     	for (Monster m : monsters){
     		if(m.getName().equals(name)){
     			return m;
     		}
     	}
     	return null;
     }
     
     /**
      * returns a string with each of the names of the monsters in the room.
      * @return a string of all monster's names
      */
     public String getMonsterNames(){
     	StringBuffer monNames = new StringBuffer();
     	for(Monster m : monsters){
     		if(!m.isDead()){
     			monNames.append(m.getName());
     		}
     	}
     	return monNames.toString();
     }
     
     /**
      * iterates through each monster and performs their attack on the player.
      * @param p the player which the monsters are attacking
      * @return a string description of the battle
      */
     public String monsterAttack(Player p){
     	StringBuffer s = new StringBuffer();
     	for (Monster m : monsters){
     		if(!m.isDead()){ // if the monster is dead, it doesn't attack
 				s.append(m.getName());
 				s.append(" attacks, ");
 				s.append(m.attack(p));
     		}
     	}
     	return s.toString();
     }
     
     /**
      * revives all monsters upon player leaving a room
      */
     public void revMonster(){
     	for (Monster m : monsters){
     		m.rev();
     	}
     }
     
     /**
      * returns the possible exits from this location
      * @return a String description of the exits from this location
      */
     public String getLoc(){
         StringBuffer dec = new StringBuffer();
         dec.append("You are ");
         dec.append(this.description);
         dec.append("\nExits: ");
         
         Iterator<ExitDirection> dir = exits.keySet().iterator(); 
         while(dir.hasNext()){
         	dec.append(dir.next().toString());
             if(dir.hasNext()) {
             	dec.append(", ");
             }
         }
         
         return dec.toString();
     }
     
     /**
      * returns the description of the room.
      * @return The description of the room.
      */
     public String getDescription()
     {
         return description;
     }
   
 
      /** creates an item in the room
      * for start up
      * @param name the name of the item
      * @param desc a description of the item
      * @param weight the weight of the item
      * @param type the type of item
      * @param value the value modifier for the item
      */
     public void setItem(Item item){
         items.add(item);
     }
     
    
     
     /**
      * returns the full details for an item in the room
      * @param name name of the item to check
      * @return a full description of the item
      */
     public String getItemFull(String name){
        Item item=items.getItem(name);
        return ""+item.getName()+": "+item.getDesc()+" ("+item.getWeight()+" lbs)";
     }
     
     /**
      * check weight of an item in the room
      * @param name name of item to check
      * @return the weight of the item
      */
     public int getItemWeight(String name){
     	Item item=items.getItem(name);
     	if(item!=null)
     	{
     		return item.getWeight();
     	}
     	else
     	{
     		return 0;
     	} 
     }
     
     /**
      * takes the item specified, this will remove the item from the room
      * @param name the name of the item to pick up
      * @return the item to pick up
      */
     public Item pickup(String name){
     	Item item=items.getItem(name);
     	if(item!=null)
     	{
     			items.remove(item);
     			return item;
     	}
     	else
     	{
     		return null;
     	}
     }
     
     /**
      * drops the passed item in the room
      * @param i the item to drop
      */
     public void drop(Item i){
         items.add(i);
     }
     
     
     /**
      * returns string naming all of the items in the room
      * @return the name of each item in the room
      */
     public String getItemNames(){
     	return items.getItemNames();
     }
     
     /**
      * @return 
      * 
      */
     @Override
     public boolean equals(Object o){
     	if(!(o instanceof Room)){
     		return false;
     	}
     	Room r = (Room)o;
     	if(getDescription().equals(r.getDescription())&&getExitMap().equals(r.getExitMap())&&getMonsters().equals(r.getMonsters())&&getItems().equals(r.getItems())){
     		return true;
     	}
     	return false;
     }
 }

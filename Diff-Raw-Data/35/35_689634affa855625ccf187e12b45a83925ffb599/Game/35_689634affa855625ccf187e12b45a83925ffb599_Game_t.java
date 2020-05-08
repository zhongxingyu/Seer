 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 import java.io.*;
 
 /**
  *
  * @author Mika Oksanen
  *
  */
 
 
 @SuppressWarnings("serial")
 public class Game implements Serializable {
 
 
 	//Fields
     private Dungeon [] arrayDungeon = new Dungeon [26];
     private int posDungeon;
     private GameGUI view;
     private Dungeon d;
     private Adventurer a;
     private ArrayList <Monster> monsters;
     private Random rnd;
     private long seed;
     private String sisterName;
     private String[] monsterName = new String[]{"Ape", "Bat", "Chimera", "Doppelganger", "Entropic Reaper", "Flame Snake", "Goblin", "Harpy", "Imp", "Jackal", "Kobold", "Lion", "Mummy", "Nymph", "Orc", "Phantom", "Quell", "Rat", "Skeleton", "Treant", "Undead", "Vermin", "Wraith", "Xill", "Yeti", "Zombie"};
     private String[] bossName = new String[]{"Storm Giant", "Titan", "Fire Elemental", "Armored Troll", "Goblin Chief", "Orc Chieftain", "Wyvern", "Hydra", "Basilisk", "Rakshasa"};
     private String[] girlsName = new String[]{"Vickie", "Laurene", "Jessica", "Jessie", "Julia", "Nadia", "Jennifer", "Jane", "Carlotta", "Clara", "Laila", "Estelle", "Victoria", "Megan", "Scarlett", "Nathalie", "Amber", "Gwyneth", "Pasta", "Keira", "Emma", "Lisa", "Dora", "Helena", "Johanna", "Laura", "Melanie", "Vittorio", "Eva"};
 
 
     //Methods
 
     public Game(GameGUI view,String playername) {
     	this.seed = (long) (Math.random()*Long.MAX_VALUE);
 		System.out.println(seed);
 
     	this.rnd = new Random(this.seed);
         for(int i=0; i<=25; i++)
     		this.arrayDungeon[i] = new Dungeon(this.rnd.nextLong());
 			//this.arrayDungeon[i] = new Dungeon(0L);
         
         this.arrayDungeon[25] = Dungeon.dragoonRoom();
         this.posDungeon=0;
         this.d=arrayDungeon[this.posDungeon];
     	this.a = new Adventurer(playername, this.arrayDungeon[posDungeon]);
     	createMonsters();
     	this.view = view;
     	this.sisterName=this.girlsName[(int) (Math.random()*(girlsName.length*1.0))];
     }
     
     
     
     public Game(GameGUI view,String playername,long seed) {
     	this.seed = seed;
 		System.out.println(seed);
     	this.rnd = new Random(this.seed);
         for(int i=0; i<=25; i++)
     		this.arrayDungeon[i] = new Dungeon(this.rnd.nextLong());
         
         this.arrayDungeon[25] = Dungeon.dragoonRoom();
         this.posDungeon=0;
         this.d=arrayDungeon[this.posDungeon];
     	this.a = new Adventurer(playername, this.arrayDungeon[posDungeon]);
     	createMonsters();
     	this.view = view;
     	this.sisterName=this.girlsName[(int) (Math.random()*(girlsName.length*1.0))];
     }
     
     /**
      * Method called by the in-game menu, used to save the game into a file called [PLAYERNAME].sav
      * @throws IOException
      */
     public void save() {
     	try {
     		//Adventurer
 	    	ObjectOutputStream oosa=new ObjectOutputStream(new FileOutputStream(this.a.getName()+"-adventurer.sav"));
 	    	oosa.writeObject(this.a);
 	    	oosa.close();
 	    	
 	    	//Monsters array
 	    	ObjectOutputStream oosm=new ObjectOutputStream(new FileOutputStream(this.a.getName()+"-monsters.sav"));
 	    	oosm.writeObject(this.monsters);
 	    	oosm.close();
 	    	
 	    	//Dungeon array
 	    	ObjectOutputStream oosd=new ObjectOutputStream(new FileOutputStream(this.a.getName()+"-dungeon.sav"));
 	    	oosd.writeObject(this.arrayDungeon);
 	    	oosd.close();
 	    	
 	    	//Actual dungeon
 	    	ObjectOutputStream oosad=new ObjectOutputStream(new FileOutputStream(this.a.getName()+"-level.sav"));
 	    	oosad.writeObject(this.posDungeon);
 	    	oosad.close();
 	    	
 	    	//Random object
 	    	ObjectOutputStream oosr=new ObjectOutputStream(new FileOutputStream(this.a.getName()+"-random.sav"));
 	    	oosr.writeObject(this.rnd);
 	    	oosr.close();
 	    	
 	    	//Seed
 	    	ObjectOutputStream ooss=new ObjectOutputStream(new FileOutputStream(this.a.getName()+"-seed.sav"));
 	    	ooss.writeObject(this.seed);
 	    	ooss.close();
     	}
     	catch (IOException e) {
     		System.out.println("Saving error !");
     		e.printStackTrace();
     	}
     }
     
     /**
      * This method do loads the saved game, by using the [PLAYERNAME].sav file.
      * The playername should be asked again (in case you would like to load another game)
      * @param filename
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @SuppressWarnings("unchecked")
 	public void load(String playername) {
     	//Importing the saved game class
     	this.a.setName(playername);
     	try {
     		ObjectInputStream oisa=new ObjectInputStream(new FileInputStream(playername+"-adventurer.sav"));
 	    	this.a=(Adventurer) oisa.readObject();
 	    	oisa.close();
 	    	
 	    	ObjectInputStream oism=new ObjectInputStream(new FileInputStream(playername+"-monsters.sav"));
 	    	this.monsters = (ArrayList<Monster>) oism.readObject();
 	    	oism.close();
 	    	
 	    	ObjectInputStream oisd=new ObjectInputStream(new FileInputStream(playername+"-dungeon.sav"));
 	    	this.arrayDungeon=(Dungeon[]) oisd.readObject();
 	    	oisd.close();
 	    	
 	    	ObjectInputStream oisl=new ObjectInputStream(new FileInputStream(playername+"-level.sav"));
 	    	this.posDungeon=(Integer) oisl.readObject();
 	    	oisl.close();
 	    	
 	    	ObjectInputStream oisr=new ObjectInputStream(new FileInputStream(playername+"-random.sav"));
 	    	this.rnd=(Random) oisr.readObject();
 	    	oisr.close();
 	    	
 	    	ObjectInputStream oiss=new ObjectInputStream(new FileInputStream(playername+"-seed.sav"));
 	    	this.seed=(Long) oiss.readObject();
 	    	oiss.close();
     	}
     	catch (IOException e) {
     		System.out.println("Loading error!");
     		e.printStackTrace();
     	}
     	catch (ClassNotFoundException e) {
     		System.out.println("Class not found exception !");
     		e.printStackTrace();
     	}
     	this.d=this.arrayDungeon[this.posDungeon];
     	this.view.registerController(this);
         this.view.registerAdventurer(this.returnAdventurer());
         this.view.registerMonster(this.returnMonsters());
         a.setDungeon(arrayDungeon[this.posDungeon]);
         for (int i=0;i<monsters.size();i++) {
         	monsters.get(i).setDungeon(this.d);
         }
         this.view.refreshScreen();
     }
 
     /**
      * Returns the name of the sister of adventurer
      * @return Sister's name
      */
     public String getSisterName() {
     	if (a.getName().equals("Theo") || a.getName().equals("theo") || a.getName().equals("théo") || a.getName().equals("Théo"))
     		return "CORRIDORS";
     	return this.sisterName;
     }
     
     public ArrayList<Monster> returnMonsters() {
     	return this.monsters;
     }
     
     public static void main(String[] args) {
     	
 
         //TODO Randomize the number of monsters
 
         GameGUI view = new GameGUI();
 
         
         view.displayStartMenu();
 
         Game controller;
         if (args.length == 0){
         	controller = new Game(view,"Player");
         }
         else {
         	controller = new Game(view,"Player",Long.parseLong(args[0]));
         }
         
         view.registerController(controller);
         view.registerAdventurer(controller.returnAdventurer());
         view.registerMonster(controller.returnMonsters());
         
         //Sounds
         Sound.setCurrentMusic(Sound.back);
         
     }
 
     /**
      * Testing
      */
 
     public void printDungeon() {
         System.out.println(d);
     }
 
     /**
      * Gets the dungeon as a string and returns it to GUI
      */
 
     public String getDungeon( Adventurer a) {
         return d.toString(a);
     }
 
     /**
      * Things to do when the adventurer dies
      */
     public static void playerDied() {
     	//TODO Focus on end panel, then show it
     	//TODO Make the dungeon panel invisible
     	//TODO Stop the timer
     }
     
     /**
      * Gets the adventurer stats as string and returns it
      */
 
     public String getAdventurer() {
         return a.toString();
     }
     
     
     /**
      * Returns the adventurer of the current game
      * Actually this is only used for the loading method
      * @return
      */
     public Adventurer returnAdventurer() {
     	return a;
     }
 
     public String getMonster(Monster m) {
         return m.toString();
     }
 
     public void createMonsters() {
     	this.monsters = new ArrayList <Monster> ();
     	if (posDungeon != 25){
     		int numberOfMonsters = this.rnd.nextInt(6)+10;
 	    	if(!a.hasKey()){
 	    		for (int i=0; i < numberOfMonsters; i++) {
 	    			int j= this.rnd.nextInt(25) ;
 	    			this.monsters.add(new Monster(monsterName[j], this.posDungeon+1, this.arrayDungeon[posDungeon]));
 	    			monsters.get(i).setRepresentation(monsterName[j].toLowerCase().charAt(0));
 	    		}
 	    	} else{
 	    		for (int i=0; i < numberOfMonsters; i++) {
 	    			int j= this.rnd.nextInt(25) ;
 	    			this.monsters.add(new Monster(monsterName[j], 52-this.posDungeon+1, this.arrayDungeon[posDungeon]));
 	    			monsters.get(i).setRepresentation(monsterName[j].toLowerCase().charAt(0));
 	    		}
 	    	}
 	        for (int i=0; i < numberOfMonsters; i++) {
 	        	int j = this.rnd.nextInt(4);
 	        	if(a.hasKey())
 	        		j = this.rnd.nextInt(9);
 	        	this.monsters.get(i).setStrength(this.monsters.get(i).getStrength()+j);
 	        	j = this.rnd.nextInt(3);
 	        	if(a.hasKey())
 	        		j = this.rnd.nextInt(9);
 	        	this.monsters.get(i).setDefence(monsters.get(i).getDefence()+j);
 	        }
     	}
         
         if(posDungeon != 0 && !a.hasKey()){							
         	if(posDungeon%3 == 0 || posDungeon == 25){
         		if(posDungeon!=25){
         			int j = rnd.nextInt(10);
         			this.monsters.add((Monster)new Boss(bossName[j], this.posDungeon +1, this.arrayDungeon[posDungeon]));
         			this.monsters.get(monsters.size()-1).setRepresentation(bossName[j].charAt(0));
         		} else{
         			this.monsters.add((Monster)new Boss("DRAGON", this.posDungeon +1, this.arrayDungeon[posDungeon]));
         			this.monsters.get(monsters.size()-1).setRepresentation('D');
         		}
         	}
         }
     }
     
     public void goUpDungeon(){
     	if(posDungeon<25){
 	    	d.getBoxes()[a.getPosX()][a.getPosY()].setActor(null);
 	    	for(int i=0; i<monsters.size(); i++){
 	    		monsters.get(i).isDead();
 	    	}
 	    	posDungeon++;
 	    	d = arrayDungeon[this.posDungeon];
 	    	a.setDungeon(arrayDungeon[this.posDungeon]);
 	    	a.setPosX(d.getSpawnX());
 	    	a.setPosY(d.getSpawnY());
 	    	d.getBoxes()[a.getPosX()][a.getPosY()].setActor(a);
 	    	createMonsters();
 	    	view.registerController(view.getController());
 	        view.registerAdventurer(a);
 	        view.registerMonster(monsters);
 	        if (posDungeon == 25){
 	        	Sound.setCurrentMusic(Sound.boss);
 	        }
 	        else {
 	        	Sound.setCurrentMusic(Sound.back);
 	        }
     	}
     }
     
     public void goDownDungeon(){
     	if(a.hasKey() && posDungeon > 0){
 	    	d.getBoxes()[a.getPosX()][a.getPosY()].setActor(null);
 	    	for(int i=0; i<monsters.size(); i++){
 	    		monsters.get(i).isDead();
 	    	}
 	    	posDungeon--;
 	    	d = arrayDungeon[posDungeon];
 	    	a.setDungeon(arrayDungeon[posDungeon]);
 	    	a.setPosX(d.getSpawnX());
 	    	a.setPosY(d.getSpawnY());
 	    	d.getBoxes()[a.getPosX()][a.getPosY()].setActor(a);
 	    	createMonsters();
 	    	view.registerController(view.getController());
 	        view.registerAdventurer(a);
 	        view.registerMonster(monsters);
 	        Sound.setCurrentMusic(Sound.back);
        	}
     }
     
     public Dungeon returnDungeon(){
     	return d;
     }
 
     public int getPosDungeon() {
         return posDungeon;
     }
     
     public Dungeon[] getArrayDungeon(){
     	return arrayDungeon;
     }
     
     public void setAdventurer(Adventurer a1){
     	this.a = a1;
     }
     public ArrayList <Monster> getMonsters(){
     	return monsters;
     }
     
     //Method added to testing code
     public Long getSeed(){
     	return this.seed;
     }
     
     public Dungeon getD(){
     	return this.d;
     }
     
     @Override
     public boolean equals(Object o){
 		if(!(o instanceof Game)) return false;
 		
		//compare dungeons array
 		Game g = (Game) o;
 		Dungeon d[] = g.getArrayDungeon();
 		for(int i=0; i<d.length; i++){
			if(!d[i].equals(this.arrayDungeon[i]))
 				return false;
 		}
//	
//		
     	return this.posDungeon == g.getPosDungeon() || this.sisterName.equals(g.getSisterName())
     		   ||this.seed == g.getSeed()||this.a.equals(g.getAdventurer())||this.d.equals(g.getD())
     		   ||this.monsters.equals(g.getMonsters()); 
//    	return true;
     }
 }

 package com.me.Roguish.Model;
 import com.me.Roguish.Controller.AbilityController;
 import com.me.Roguish.Screens.GameScreen;
 import com.badlogic.gdx.utils.Array;
 import java.util.Random;
 import com.badlogic.gdx.maps.MapProperties;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 
 public class Level{
 	public Array<Entity> entities = new Array<Entity>();
 	public TurnQueue queue;
 	public AbilityController ability = new AbilityController();
     public TiledMap map;
     //public SimpleTileAtlas atlas;
     public HeroUnit hero;
     private Random Dice = new Random();
     public Entity winChest = new Entity(2,39,"Chest", new Array<Integer>());
     private MonsterUnit mon;
     
     private TiledMapTileLayer backgroundLayer;
     public int columns;
     public int rows;
     
 	public Level(ClassCard cCard){
 		hero =  new HeroUnit(2, 38, cCard.getClassName(), new Array<Integer>(), cCard);
 		create();
 		setStats();
 		populate();
 		queue = new TurnQueue(getEntities());
 		
 		backgroundLayer = (TiledMapTileLayer)map.getLayers().get(0);
 		columns = backgroundLayer.getWidth();
 		rows = backgroundLayer.getHeight();
 	}
 	
 	public Level(ClassCard cCard, Array<Integer> abilities) {
 		hero =  new HeroUnit(5, 5, cCard.getClassName(),  new Array<Integer>(), cCard);
 		for(int i = 0; i < abilities.size; i++ )
 			hero.addAbility(abilities.get(i));
 		create();
 		backgroundLayer = (TiledMapTileLayer)map.getLayers().get(0);
 		columns = backgroundLayer.getWidth();
 		rows = backgroundLayer.getHeight();
 		setStats();
 		populate();
 		queue = new TurnQueue(getEntities());
 	}
 	
 	public Level(ClassCard cCard, HeroUnit hero) {
 		this.hero = hero;
 		create();
 		backgroundLayer = (TiledMapTileLayer)map.getLayers().get(0);
 		columns = backgroundLayer.getWidth();
 		rows = backgroundLayer.getHeight();
 		setStats();
 		populate();
 		queue = new TurnQueue(getEntities());
 	}
 	
 	
 	// The below will work once the tileOpen() method is added to level.
 	private void generateEntities(){
 		// This can be scaled as the level difficulty
 		int area = columns * rows;
 		System.out.println("Area:" + area);
 		int totalMon = Math.round( area / 50);
 		System.out.println("Total Monsters:" + totalMon);
 		
 		for(int i = 0; i < totalMon; i++){
 			int[] x = findOpenXY();
 			
 			//Update as new units are created
 			int monType = Dice.nextInt(4);
 			switch(monType){
 			case MonsterUnit.RAT: mon = new MonsterUnit(x[0], x[1], "E_Rat", new Array<Integer>(), MonsterUnit.RAT); mon.setId(1 + i); break;
 			case MonsterUnit.BAT: mon = new MonsterUnit(x[0], x[1], "E_Bat", new Array<Integer>(), MonsterUnit.BAT); mon.setId(1 + i); break;
 			case MonsterUnit.SPIDER: mon = new MonsterUnit(x[0], x[1], "E_Spider", new Array<Integer>(), MonsterUnit.SPIDER);  mon.setId(1 + i); break;
 			case MonsterUnit.SHADOW: mon = new MonsterUnit(x[0], x[1], "E_Shadow", new Array<Integer>(), MonsterUnit.SHADOW); mon.setId(1 + i); break;
 			}
 			
 			
 			addEntity(mon);
 		}
 	}
 
 	private int[] findOpenXY() {
 		int x;
 		int y;
 		int[] fin = new int[2];
 		boolean done = false;
 		do{
 			x = Dice.nextInt(columns);
 			y = Dice.nextInt(rows);
 			if(tileOpen(x,y)){
 				done = true;
 				fin[0] = x;
 				fin[1] = y;
 			}
 		}while(!done);
 		
 		return fin;
 	}
 
 	private void setStats() {
 		 hero.setId(0);
 		 hero.setHP(1000);
 		
 		 /*
 		 rat1.setId(1);
 		 rat2.setId(2);
 		 rat3.setId(3);
 		 bat1.setId(4);
 		 spider1.setId(5);
 		 shadow1.setId(6);
 		 
 		 */
 		
 	}
 
 	public void create(){
 		boolean test;
 		System.out.println("In create");
 		if(GameScreen.level1){
 		map = new TmxMapLoader().load("data/level/test_FoV.tmx");
 		}
 		else if(GameScreen.level1 && GameScreen.level2){
 			map = new TmxMapLoader().load("data/level/test_FoV.tmx");
 			
 		}else{
 		map = new TmxMapLoader().load("data/level/test_FoV2.tmx");
 		}
 		System.out.println("Tiles loaded");
 	   
 	}
 
 	
 	private void addEntity(Entity ent){
 		entities.add(ent);
 	}
 	
 	private boolean removeEntity(Entity ent){
 		return entities.removeValue(ent, true);
 	}
 	
 	public HeroUnit getHero(){
 		return hero;
 	}
 		// Populates entity list
 	private void populate(){
 	
 		addEntity(hero);
 		
		
 		//Level specific monster && objective placement
 		if(GameScreen.level1 && GameScreen.level2){
 			
 		}
 		else if(GameScreen.level1){
			hero.setPosition(5, 5);
 			
 		}
 		else{
			addEntity(winChest);
 			winChest.setAlive(false);
 			winChest.setPosition(1, 48);
 			winChest.setId(1337);
 		}
 		
 		generateEntities();
 		/*
 		addEntity(rat1);
 		addEntity(winChest);
 		addEntity(rat2);
 		addEntity(rat3);
 		addEntity(bat1);
 		addEntity(spider1);
 		addEntity(shadow1);
 		*/
 		
 		
 	}
 	
 	public Array<Entity> getEntities(){
 		return this.entities;
 	}
 	
 	//Returns true if the tile at the x, y is open
 		public boolean tileOpen(int x, int y){
 			if(x < 0 || x > columns || y < 0 || y > rows) return false;
 			for (Entity ent : getEntities()) {
 				if (ent.getX() == x && ent.getY() == y && ent.getAlive()) return false;
 			}
 			System.out.println("open?: " + tilePropCheck(x,y,"wall"));
 			return tilePropCheck(x,y,"wall");
 		}
 	
 	public boolean tilePropCheck(int x, int y, String property){
 		MapProperties temp = getTile(x,y).getProperties();
 		if(temp.containsKey(property)){
 			return !Boolean.parseBoolean(temp.get(property, String.class));
 		}
 		else return true;
 	}
 
 	public boolean tilePropCheck(int x, int y, int layer, String property){
 		MapProperties temp = getTile(x,y,layer).getProperties();
 		if(temp.containsKey(property))
 			return !Boolean.parseBoolean(temp.get(property, String.class));
 		else return true;
 	}
 	
 	public boolean tileExists(int x, int y, int layer){
 		try{
 			backgroundLayer.getCell(x,y).getTile();
 			return true;
 		}catch(ArrayIndexOutOfBoundsException e){
 			return false;
 		}
 	}
 	
 	public TiledMapTile getTile(int x, int y, int layer){
 		TiledMapTileLayer temp = (TiledMapTileLayer)map.getLayers().get(layer);
 		return temp.getCell(x,y).getTile();
 	}
 	
 	public TiledMapTile getTile(int x, int y){
 		try{
 			return backgroundLayer.getCell(x,y).getTile();
 		}
 		catch(NullPointerException e){
 			return null;
 		}
 	}
 	
 }

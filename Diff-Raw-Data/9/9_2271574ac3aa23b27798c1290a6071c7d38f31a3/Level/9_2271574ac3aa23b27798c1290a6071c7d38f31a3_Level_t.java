 package com.me.Roguish.Model;
 import com.me.Roguish.Controller.AbilityController;
 import com.me.Roguish.Model.*;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.graphics.g2d.tiled.SimpleTileAtlas;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 
 public class Level{
 	public Array<Entity> entities = new Array<Entity>();
 	public TurnQueue queue;
 	public AbilityController ability = new AbilityController();
 	
     public TiledMap map;
     public SimpleTileAtlas atlas;
     public HeroUnit hero;
     public MonsterUnit rat1 = new MonsterUnit(1, 2, "E_Rat", new Array<Integer>(), MonsterUnit.RAT);
     public MonsterUnit rat2 = new MonsterUnit(2, 1, "E_Rat", new Array<Integer>(), MonsterUnit.RAT);
     public MonsterUnit rat3 = new MonsterUnit(2, 2, "E_Rat", new Array<Integer>(), MonsterUnit.RAT);
     public Entity winChest = new Entity(4,13,"D_Plus_B", new Array<Integer>());
     
 	public Level(ClassCard cCard){
 		hero =  new HeroUnit(5, 5, cCard.getClassName(), new Array<Integer>(), cCard);
 		create();
 		setStats();
 		populate();
 		queue = new TurnQueue(getEntities());
 	}
 	
 	private void setStats() {
 		 hero.setMovement(0);
 		 hero.setId(0);
		 hero.setHP(1);
 		 rat1.setMovement(1);
 		 rat1.setId(1);
 		 rat2.setMovement(2);
 		 rat2.setId(2);
 		 rat3.setMovement(3);
 		 rat3.setId(3);
 		 rat1.addAbility(AbilityController.BITE);
 		 rat2.addAbility(AbilityController.BITE);
 		 rat3.addAbility(AbilityController.BITE);
 		 winChest.setId(1337);
 		 winChest.setAlive(false);
 		
 	}
 
 	public void create(){
 		System.out.println("In create");
 		map = TiledLoader.createMap(Gdx.files.internal("data/level/test3.tmx"));
 		System.out.println("Tiles loaded");
 	    atlas = new SimpleTileAtlas(map, Gdx.files.internal("data/"));
 	    System.out.println("atlas made");  
 	   
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
 		addEntity(rat1);
 		addEntity(rat2);
 		addEntity(rat3);
 		addEntity(winChest);
 
 	}
 	
 	public Array<Entity> getEntities(){
 		return this.entities;
 	}
 	
 	public boolean tilePropCheck(int x, int y, String property){
 		return ("true".equals(map.getTileProperty(getTile(x,y), property)));
 	}
 	public int getTile(int x, int y, int layer){
 		return map.layers.get(layer).tiles[y][x];
 	}
 	
 	public int getTile(int x, int y){
 		return map.layers.get(0).tiles[y][x];
 	}
 }

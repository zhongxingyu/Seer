 package com.me.Roguish.Model;
 import com.me.Roguish.Model.Entity;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.graphics.g2d.tiled.SimpleTileAtlas;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
 
 public class Level{
 	private static int maxEntity = 16;
 	public Array<Entity> entities = new Array<Entity>();
 	public TurnQueue queue;
 	public AbilityController ability;
 	
     public TiledMap map;
     public SimpleTileAtlas atlas;
     public HeroUnit hero = new HeroUnit(5, 5, "Warrior");
    public MonsterUnit rat1 = new MonsterUnit(1, 2, "Enemy", MonsterUnit.RAT);
    public MonsterUnit rat2 = new MonsterUnit(2, 1, "Enemy", MonsterUnit.RAT);
    public MonsterUnit rat3 = new MonsterUnit(2, 2, "Enemy", MonsterUnit.RAT);
     
 	public Level(){
 		create();
 		populate();
 		queue = new TurnQueue(getEntities());
 	}
 	
 	public void create(){
 		System.out.println("In create");
 		map = TiledLoader.createMap(Gdx.files.internal("data/level/test3.tmx"));
 		System.out.println("Tiles loaded");
 	    atlas = new SimpleTileAtlas(map, Gdx.files.internal("data/"));
 	    System.out.println("atlas made");  
 	    hero.setMovement(0);
 	    hero.setId(0);
 	    rat1.setMovement(1);
 	    rat1.setId(1);
 	    rat2.setMovement(2);
 	    rat2.setId(2);
 	    rat3.setMovement(3);
 	    rat3.setId(3);
 	}
 
 	
 	private void addEntity(Entity ent){
 		entities.add(ent);
 	}
 	
 	private boolean removeEntity(Entity ent){
 		return entities.removeValue(ent, true);
 	}
 	
 	public Entity getHero(){
 		return hero;
 	}
 	
 	// Populates entity list
 	private void populate(){
 
 		addEntity(hero); 
 		addEntity(rat1);
 		addEntity(rat2);
 		addEntity(rat3);	
 
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

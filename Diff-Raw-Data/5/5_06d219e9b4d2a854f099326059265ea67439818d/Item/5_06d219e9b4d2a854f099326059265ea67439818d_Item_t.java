 package com.cannon.basegame;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class Item extends Entity{
 	
 	private int id;
 	private Image image;
 	private boolean pickedUp = false;
 	public static HashMap<Integer,String> itemList;
 	
 	public Item() {
 		this.id = 0;
 		try {
			this.image = new Image(SlimeGame.basePath + "res\\fang.png");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		width = 32;
 		height = 32;
 	}
 	
 	public Item(Recipe recipe) {
 		this.id = recipe.itemId;
 		try {
			this.image = new Image(SlimeGame.basePath + "res\\" + recipe.getImageLocation());
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		width = 32;
 		height = 32;
 	}  
 	
 	public Item(int id) {
 		this.id = id;
 		try {
 			if(Item.itemList.get(id) != null) {
 				
 				this.image = new Image(SlimeGame.basePath + "res\\" + Item.itemList.get(id) + ".png");
 			}
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		width = 32;
 		height = 32;
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		g.drawImage(image, x, y);
 	}
 	
 	
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)	throws SlickException {
 		
 	}
 
 	public static void initList() throws SlickException {
 		itemList = new HashMap<Integer,String>();
 		Type mapType = new TypeToken<HashMap<Integer,String>>() {}.getType();
 		Gson myGson = new Gson();
 		
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader("src\\" + SlimeGame.basePath + "data\\items.json"));			itemList = myGson.fromJson(reader.readLine(), mapType);
 			reader.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		for(int i = 0; i < itemList.size(); i++) {
 			System.out.print(itemList.get(i) + " ");
 		}
 		System.out.println();
 	}
 
 	@Override
 	public boolean onCollision(Entity entity) {
 		if(entity.getClass() == Player.class && !pickedUp) {
 			entity.pickUp(this);
 			Entity.entityList.remove(this);
 			pickedUp = true;
 		}
 		return true;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public String getName() {
 		return itemList.get(id);
 	}
 	
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 }

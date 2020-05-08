 package edu.virginia.cs.sgd.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 
 import edu.virginia.cs.sgd.GameOfSwords;
 
 public class TextureRegionManager {
 
 	private String sheetName;
 	
 	private int width;
 	private int height;
 	
 	private Map<String, Vector2> bounds;
 	
 	public TextureRegionManager(String sheetName, int width, int height) {
 		this.sheetName = sheetName;
 		this.width = width;
 		this.height = height;
 		
 		this.bounds = new HashMap<String, Vector2>();
 	}
 	
 	public void addRegion(String name, Vector2 p) {
 		bounds.put(name, p);
 	}
 	
 	public TextureRegion getRegion(String name) {
 		Vector2 p = bounds.get(name);
 		
 		Texture tex = GameOfSwords.getManager().get(sheetName, Texture.class);
 		
		return new TextureRegion(tex, (int)p.x, (int)p.y, (int)p.x + width, (int)p.y + height);
 	}
 }

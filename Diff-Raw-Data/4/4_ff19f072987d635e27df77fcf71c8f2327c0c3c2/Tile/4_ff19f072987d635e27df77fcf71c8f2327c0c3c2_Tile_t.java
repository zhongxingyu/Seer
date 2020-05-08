 package com.github.desmaster.Devio.realm.world;
 
 import java.io.IOException;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.tex.iTexture;
 
 public enum Tile {
 	GRASS("Grass", false, true, 0), 					// 0
 	SAND("Sand", false, true, 1), 						// 1
 	STONE("Stone", false, true, 2), 					// 2
 	COBBLE_STONE("Cobblestone", false, true, 3), 		// 3
 	MOSSY_STONE("Mossy_Cobblestone", false, true, 4), 	// 4
 	WATER("Water", false, true, 5); 					// 5
 	//LAVA("Lava", false, true, 6) 						// 6
 
 	private String name;
 	private boolean breakable;
 	private boolean solid;
 	private int TextureID;
 	
 	public int x;
 	public int y;
 
 	Tile(String name, boolean breakable, boolean solid,int TextureID) {
		Console.log("initialized enum: " + name);
 		this.name = name;
 		this.breakable = breakable;
 		this.solid = solid;
 		this.TextureID = TextureID;
 	}
 
 	public int getID() {
 		return this.ordinal();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean isBreakable() {
 		return breakable;
 	}
 
 	public boolean isSolid() {
 		return solid;
 	}
 
 	public Texture getTexture() {
		return iTexture.textures[TextureID];
 	}
 	public int getTextureID() {
 		return TextureID;
 	}
 	public void setTextureID(int TextureID) {
 		this.TextureID = TextureID;
 	}
 
 }

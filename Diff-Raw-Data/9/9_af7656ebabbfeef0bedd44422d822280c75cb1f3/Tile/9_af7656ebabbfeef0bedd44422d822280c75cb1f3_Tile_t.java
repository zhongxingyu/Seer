 package inf1.oop.turnbased.map;
 
 // A map square
 public class Tile {
 	boolean hasTexture;
 	String textureName;
	private boolean passable;
	
	public void setPassable(boolean p){
		passable = p;
	}
	
	public boolean isPassable(){
		return passable;
	}
 	
 	public String getTextureName() {
 		return textureName;
 	}
 	
 	public boolean hasTexture() {
 		return hasTexture;
 	}
 	
 	public Tile(String tex) {
 		textureName = tex;
 		hasTexture = true;
 	}
 	
 	public Tile() {
 		hasTexture = false;
 	}
 }

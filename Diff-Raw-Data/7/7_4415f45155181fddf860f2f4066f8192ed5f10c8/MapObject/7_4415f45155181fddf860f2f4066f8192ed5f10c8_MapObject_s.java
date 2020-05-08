 package net.fourbytes.shadow.map;
 
 import com.badlogic.gdx.utils.ObjectMap;
 
 import net.fourbytes.shadow.stream.Data;
 
 /**
  * A MapObject contains needed data to create a "fresh" object (as when loading an .tmx) and / or 
  * additional data (f.e. fluid height, [insert example here], ...).
  */
 public class MapObject extends Data {
 	
 	public float x;
 	public float y;
 	public String type;
 	public String subtype;
 	public int layer;
	public ObjectMap<String, Object> args;
 	
 	public MapObject() {
 	}
 	
 	public MapObject(String type, String subtype, int layer, ObjectMap<String, Object> args) {
 		this.type = type;
 		this.subtype = subtype;
 		this.layer = layer;
		this.args = args;
 	}
 
 }

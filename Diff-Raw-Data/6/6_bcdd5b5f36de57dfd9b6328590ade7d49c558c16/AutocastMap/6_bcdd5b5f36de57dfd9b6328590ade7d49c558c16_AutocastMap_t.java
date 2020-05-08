 package org.andengine.extension.rubeloader.json;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import net.minidev.json.JSONObject;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class AutocastMap extends LinkedHashMap<String, Object> {
 	private static final long serialVersionUID = -7878573948739697257L;
 
 	public boolean has(String key) {
 		return containsKey(key);
 	}
 
 	public JSONObject getJSON(String key) {
 		return (JSONObject) get(key);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Object> getList(String key) {
 		return (List<Object>) get(key);
 	}
 
 	public String getString(String key) {
 		return (String) get(key);
 	}
 
 	public String getString(String key, String pDefault) {
 		return has(key) ? getString(key) : pDefault;
 	}
 
 	public int getInt(String key) {
 		return (Integer) get(key);
 	}
 
 	public int getInt(String key, int pDefault) {
 		return has(key) ? getInt(key) : pDefault;
 	}
 
 	public boolean getBool(String key) {
 		return (Boolean) get(key);
 	}
 
 	public boolean getBool(String key, boolean pDefault) {
 		return has(key) ? getBool(key) : pDefault;
 	}
 
 	public float getFloat(String key) {
 		try {
 			return (Float) get(key);
 		} catch (java.lang.ClassCastException e) {
 			/* XXX workaround no-comma cases, i.e. 0 instead of 0.0 */
 			return (Integer) get(key);
 		}
 	}
 
 	public float getFloat(String key, float pDefault) {
 		return has(key) ? getFloat(key) : pDefault;
 	}
 
 	public Vector2 getVector2(String key) {
 		return getVector2Reuse(key, new Vector2());
 	}
 
 	public Vector2 getVector2(String key, Vector2 pDefault) {
 		return has(key) ? getVector2(key) : pDefault;
 	}
 
 	public Vector2 getVector2Reuse(String key, Vector2 pReuse) {
 		if (get(key) instanceof Integer) {
 			/* XXX workaround non-vector vectors, i.e 0 instead of {x=0,y=0}*/
 			int v = getInt(key);
 			return pReuse.set(v, v);
 		} else {
 			AutocastMap vectorMap = (AutocastMap) get(key);
 			pReuse.set(vectorMap.getFloat("x"), vectorMap.getFloat("y"));
 			return pReuse;
 		}
 	}
 
 	public Vector2[] getVector2Array(String key) {
 		AutocastMap verticesMap = (AutocastMap) get(key);
 		List<Object> listX = verticesMap.getList("x");
 		List<Object> listY = verticesMap.getList("y");
 		int size = listX.size();
 
 		Vector2[] ret = new Vector2[size];
 
 		for (int i = 0; i < size; i++) {
			Object objectX = listX.get(i);
			Object objectY = listY.get(i);
			Float x = (objectX instanceof Integer) ? (Integer)objectX : (Float)objectX;
			Float y = (objectY instanceof Integer) ? (Integer)objectY : (Float)objectY;
			ret[i] = (new Vector2(x, y));
 		}
 		return ret;
 	}
 }

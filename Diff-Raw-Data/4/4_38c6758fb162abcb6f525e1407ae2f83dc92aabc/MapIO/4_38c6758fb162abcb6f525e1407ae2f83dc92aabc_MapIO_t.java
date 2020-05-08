 package entity.util;
 
 import entity.Entity;
 import entity.EntityRegistry;
 import event.EntityEvent;
import game.Game;
 import game.World;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.lwjgl.util.vector.Vector3f;
 
 import util.Compressor;
 
 public class MapIO
 {
 	public static void loadMap(World world, String file)
 	{
 		try
 		{
 			JSONObject content = new JSONObject(Compressor.decompressFile(new File(file)));
 
 			JSONArray entities = content.getJSONArray("entities");
 
 			for (int i = 0; i < entities.length(); i++)
 			{
 				JSONObject o = entities.getJSONObject(i);
 				Entity entity = EntityRegistry.createEntity(o.getString("name"));
 
 				JSONArray groups = o.getJSONArray("groups");
 				for (int j = 0; j < groups.length(); j++)
 				{
					Game.getCurrentGame().getCurrentWorld().addEntityToGroup(entity, groups.getString(i));
 				}
 				JSONArray pos = o.getJSONArray("pos");
 				JSONArray rot = o.getJSONArray("rot");
 				entity.setPosition((float) pos.getDouble(0), (float) pos.getDouble(1), (float) pos.getDouble(2));
 				entity.setRotation((float) rot.getDouble(0), (float) rot.getDouble(1), (float) rot.getDouble(2));
 
 				HashMap<String, Object> customValues = new HashMap<>();
 				JSONObject c = o.getJSONObject("custom");
 				JSONArray cKeys = c.names();
 				for (int j = 0; j < c.length(); j++)
 				{
 					Object object = c.get(cKeys.getString(j));
 
 					customValues.put(cKeys.getString(j), object);
 				}
 
 				JSONArray events = o.getJSONArray("events");
 
 				for (int j = 0; j < events.length(); j++)
 					entity.events.get(events.getJSONObject(j).get("trigger")).add(new EntityEvent(entity, events.getJSONObject(j)));
 
 				entity.customValues = customValues;
 				entity.key = o.getString("id");
 				entity.initEntity();
 				world.spawnEntity(entity);
 			}
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static JSONArray serializeVector3f(Vector3f v)
 	{
 		try
 		{
 			JSONArray a = new JSONArray();
 			a.put(v.x);
 			a.put(v.y);
 			a.put(v.z);
 			return a;
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 }

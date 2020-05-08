 package edu.washington.cs.games.ktuite.pointcraft;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 import org.lwjgl.util.vector.Vector3f;
 
 class Scene implements org.json.JSONString {
 	float    pan, tilt;
 	Vector3f pos;
 	
 	public Scene() {
 		pan  = 0.0f;
 		tilt = 0.0f;
		pos = new Vector3f(0.0f, 0.0f, 0.0f);
 	};
 	
 	public String toJSONString() {
 		JSONStringer s = new JSONStringer();
 		try {
 			s.object();
 			s.key("pan");
 			s.value(pan);
 			s.key("tilt");
 			s.value(tilt);
 			s.key("pos_x");
 			s.value(pos.getX());
 			s.key("pos_y");
 			s.value(pos.getY());
 			s.key("pos_z");
 			s.value(pos.getZ());			
 			s.endObject();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return s.toString();
 	}
 	
 	public void fromJSONString(JSONObject obj) throws JSONException {
 		pan = obj.getLong("pan");
 		tilt = obj.getLong("tilt");
 		pos.x = obj.getLong("pos_x");
 		pos.y = obj.getLong("pos_y");
 		pos.z = obj.getLong("pos_z");
 	}
 };
 
 
 public class Cinematics  {
 	static Scene[] scenes = new Scene[10];
 
 	public static void recordScene(int n) {
 		assert (n >= 0 && n < 10);
 		scenes[n] = new Scene();
 		scenes[n].pan = Main.pan_angle;
 		scenes[n].tilt = Main.tilt_angle;
		scenes[n].pos.x = Main.pos.x;
		scenes[n].pos.y = Main.pos.y;
		scenes[n].pos.z = Main.pos.z;
 		System.out.println("recording scene "+n);
 		System.out.println("set pan: "+scenes[n].pan+ ", tilt: "+scenes[n].tilt+", pos: "+scenes[n].pos);
 	}
 	
 	public static void recallScene(int n) {
 		assert (n >= 0 && n < 10);
 		Main.pan_angle = scenes[n].pan;
 		Main.tilt_angle = scenes[n].tilt;
 		Main.pos = scenes[n].pos;
 		System.out.println("recalling scene "+n);
 		System.out.println("set pan: "+Main.pan_angle+ ", tilt_angle: "+Main.tilt_angle+", pos: "+Main.pos);
 	}
 	
 	
 	public static String toJSONString() {
 		JSONStringer s = new JSONStringer();
 		try {
 			s.object();
 			for (int i = 0; i < 10; i++) {
 				s.key("scene_" + i);
 				s.value(scenes[i]);
 			}
 			s.endObject();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return s.toString();
 	}
 
 	public static void loadFromJSON(JSONObject obj) throws JSONException {
 	
 		for (int i = 0; i < 10; i++){
 			scenes[i].fromJSONString(obj.getJSONObject("scene_" + i));
 		}
 	}
 };

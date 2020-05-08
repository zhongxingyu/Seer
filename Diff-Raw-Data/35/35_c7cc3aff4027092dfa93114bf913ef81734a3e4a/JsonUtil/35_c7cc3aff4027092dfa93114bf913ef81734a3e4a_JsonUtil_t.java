 package utils;
 
 import java.io.File;
 
 import com.golden.gamedev.util.FileUtil;
 
 /**
  * JSON loader utility for different classes.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
  * 
  * @author Kirill Klimuk
  */
 
 public class JsonUtil {
 	public static String getJSON(String url) {
 		String[] jsonPacked = FileUtil.fileRead(new File(url));
 
 		StringBuilder jsonBuilder = new StringBuilder();
 		for (String line : jsonPacked) {
 			jsonBuilder.append(line);
 		}
 		
 		return jsonBuilder.toString();
 	}
 	
 	public class JSONDirections implements Jsonable {
 		public int frames;
 		public int delay;
 		public JSONDirection[] directions;
 	}
 	
 	public class JSONDirection implements Jsonable {
 		public String direction;
 		public String image;
 	} 
 
 	public class JSONPlayerWalking implements Jsonable {
 		public int[] up;
 		public int[] down;
 		public int[] left;
 		public int[] right;
 	}
 	
 	public class JSONPlayerAttacking implements Jsonable {
 		public int[] keys;
 		public JSONDirections directions;
 	}
 
 	public class JSONLevel implements Jsonable{
 		public String nextLevel;
 		public String upperFilename;
		public String lowerFilename;
 		public JSONPlayer player;
 		//public Enemy[] enemies;
 		//public NPC[] npcs;		
 	}
 	
 	 public class JSONPlayer implements Jsonable{
 		 public int[] location;
 		 public String directionsURL;
 		 public String actionsURL;		 
 	 }
 	 
 	 public class JSONEnemy implements Jsonable{
 		 public int[] location;
 		 public String directions;
 		 public String actions;		 
 	 }
 	 
 	 public class JSONNpc implements Jsonable{
 		 public int[] location;
 	}
 	 
 	 public class JSONScenery implements Jsonable{
 		 public int[] locations;
 		 public String imageURL;
 		 public int layer;
 	 }
 
 	public class JSONPlayerActions implements Jsonable {
 		public JSONPlayerWalking walking;
 		public JSONPlayerAttacking attacking;
 	}
 }

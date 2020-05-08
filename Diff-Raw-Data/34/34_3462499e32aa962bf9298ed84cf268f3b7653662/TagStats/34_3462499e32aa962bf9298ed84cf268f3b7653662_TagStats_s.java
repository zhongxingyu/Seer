 package tumblstats;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import tumblib.Post;
 
 public class TagStats extends Stats{
 	TagStats(String tableName, String keyName, Post[] postArray){
 		super(tableName, keyName, postArray);
 	}
 
 	@Override
 	protected void setData() {
 		try{
 			HashMap<String, Integer> tagMap = new HashMap<String, Integer>();
 			for(int i = 0; i < postArray.length; i++){
 				String[] tags = postArray[i].getTags();
 				if(tags != null)
 					for(int j = 0; j < tags.length; j++){
 						if(tagMap.containsKey(tags[j]))
 							tagMap.put(tags[j], tagMap.get(tags[j]) + 1);
 						else
 							tagMap.put(tags[j], 1);

 						prep.setString(1, tags[j]);
 						prep.setInt(2, tagMap.get(tags[j]));
						prep.addBatch();
 
 					}
 			}
 
 			prep.executeBatch();
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 
 
 	}
 
 
 
 }

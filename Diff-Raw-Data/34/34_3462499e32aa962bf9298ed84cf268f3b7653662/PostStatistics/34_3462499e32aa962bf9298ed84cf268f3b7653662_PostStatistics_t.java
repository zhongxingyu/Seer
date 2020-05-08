 package tumblstats;
 
 import java.util.ArrayList;
 
 import tumblib.Post;
 
 /**
  * That Post Statistics class calls the functions that accumulate and print 
  * statistics about posts.  It passes the postList to those functions.
  * @author Yeison Rodriguez
  *
  */
 
 public class PostStatistics {
 
 
 	public PostStatistics(ArrayList<Post> postList){
 		Post[] postArray = {};
 		postArray = postList.toArray(postArray);
 
 		new TypeStats("types", "type", postArray).createTable(false);
 		System.out.println();
 		new TimeStats("hour", "hour", postArray).createTable(false);
 		System.out.println();
		new TagStats("tags", "tag", postArray).createTable(false);
		System.out.println();
		System.out.println(postArray[0].getContent());
 	}
 
 }

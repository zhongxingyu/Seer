 package im.wades;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 public class TwitterTrackStreamHandler extends TwitterStreamHandler {
 	private Set<String> keywords = new HashSet<String>(50);
 
 	public static void main(String[] args) {
 		TwitterTrackStreamHandler handler = new TwitterTrackStreamHandler();
 		
 		String keywords = GlobalConfig.getRequired("track.keywords");
 		for (String keyword : keywords.split(",")) {
 			if (handler.getKeywordCount() >= 50) {
 				break;
 			}
 			handler.addKeyword(keyword);
 		}
 		
 		handler.run();
 	}
 	
 	public void addKeyword(String keyword) {
 		keywords.add(keyword);
 	}
 	
 	public int getKeywordCount() {
 		return keywords.size();
 	}
 
 	@Override
 	protected HttpMethod getMethod() {
		PostMethod post = new PostMethod("http://stream.twitter.com/1/statuses/filter.format");
 		
 		post.addParameter("track", getTrackString());
 		// post.addParameter("count", "10");
 		
 		return post;
 	}
 	
 	private String getTrackString() {
 		StringBuilder trackString = new StringBuilder();
 
 		boolean first = true;
 		for (String keyword : keywords) {
 			if (first) {
 				first = false;
 			} else {
 				trackString.append(',');
 			}
 			trackString.append(keyword);
 		}
 		
 		return trackString.toString();
 	}
 
 	@Override
 	protected void handleEntry(Map<String, ?> entry) {
 		if (entry.containsKey("text")) {
 			// tweet
 			
 			Map<String, ?> user = (Map<String, ?>) entry.get("user");
 			
 			// TODO: kind of hacky way to ensure it is a Long
 			Long userId = ((Number) user.get("id")).longValue();
 			String screenName = (String) user.get("screen_name");
 			String tweet = (String) entry.get("text");
 			
 			Utilities.notify(screenName, tweet);
 			System.out.println("tweet: " + screenName + ": " + tweet);
 		} else {
 			// something else
 			System.out.println(entry);
 		}
 	}
 }

 package im.wades;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.lang.StringUtils;
 
 public class TwitterFollowStreamHandler extends TwitterStreamHandler {
 	private Set<Long> followIds = new HashSet<Long>(200);
 	private Set<Long> replyIds = new HashSet<Long>();
 
 	public static void main(String[] args) {
 		TwitterFollowStreamHandler handler = new TwitterFollowStreamHandler();
 		
 		String followIds = GlobalConfig.getRequired("follow.ids");
 		for (String id : followIds.split(",")) {
 			if (handler.getFollowCount() >= 200) {
 				break;
 			}
 			handler.addUser(Long.valueOf(id));
 		}
 		
 		String replyIds = GlobalConfig.get("follow.reply_ids");
 		if (StringUtils.isNotBlank(replyIds)) {
 			for (String id : replyIds.split(",")) {
 				handler.addReplyUser(Long.valueOf(id));
 			}
 		}
 		
 		handler.run();
 	}
 	
 	public void addUser(Long userId) {
 		followIds.add(userId);
 	}
 	
 	public void addReplyUser(Long replyUserId) {
 		replyIds.add(replyUserId);
 	}
 	
 	public int getFollowCount() {
 		return followIds.size();
 	}
 
 	@Override
 	protected HttpMethod getMethod() {
		PostMethod post = new PostMethod("http://stream.twitter.com/1/statuses/filter.format");
 		
 		post.addParameter("follow", getFollowString());
 		
 		return post;
 	}
 	
 	private String getFollowString() {
 		StringBuilder followString = new StringBuilder();
 
 		boolean first = true;
 		for (Long followId : followIds) {
 			if (first) {
 				first = false;
 			} else {
 				followString.append(',');
 			}
 			followString.append(followId);
 		}
 		
 		return followString.toString();
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
 			
 			if (followIds.contains(userId)) {
 				Utilities.notify(screenName, tweet);
 				System.out.println("tweet: " + screenName + ": " + tweet);
 			} else {
 				// This is an @ reply
 				Long inReplyToUserId = ((Number) entry.get("in_reply_to_user_id")).longValue();
 				
 				if (replyIds.contains(inReplyToUserId)) {
 					Utilities.notify(screenName, tweet);
 					System.out.println("@reply: " + screenName + ": " + tweet);
 				} else {
 					// System.out.println("@reply: " + screenName + ": " + tweet);
 				}
 			}
 			
 		} else {
 			// something else
 			System.out.println(entry);
 		}
 	}
 }

 package il.technion.ewolf.server.jsonDataHandlers;
 
 import static il.technion.ewolf.server.EWolfResponse.RES_BAD_REQUEST;
 import static il.technion.ewolf.server.EWolfResponse.RES_NOT_FOUND;
 import il.technion.ewolf.ewolf.SocialNetwork;
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.ewolf.WolfPackLeader;
 import il.technion.ewolf.exceptions.WallNotFound;
 import il.technion.ewolf.posts.Post;
 import il.technion.ewolf.posts.TextPost;
 import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.UserIDFactory;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.inject.Inject;
 
 public class NewsFeedFetcherWithCache implements JsonDataHandler {
 	private final SocialFS socialFS;
 	private final WolfPackLeader socialGroupsManager;
 	private final UserIDFactory userIDFactory;
 	private final SocialNetwork snet;
 
 	private  static final long cachedTime = 60000; // 60000ms = 1min
 	private Date lastModified;
 	private Map<Profile, List<Post>> newsFeed;
 
 	@Inject
 	public NewsFeedFetcherWithCache(SocialFS socialFS, WolfPackLeader socialGroupsManager, UserIDFactory userIDFactory, SocialNetwork snet) {
 		this.socialFS = socialFS;
 		this.socialGroupsManager = socialGroupsManager;
 		this.userIDFactory = userIDFactory;
 		this.snet = snet;
 
 		newsFeed = fetchAllPosts();
 		lastModified = new Date();
 	}
 
 	private static final String POST_OWNER_NOT_FOUND_MESSAGE = "Not found";
 
 	private static class JsonReqNewsFeedParams {
 		//Request type: "user" or "wolfpack"
 		String newsOf;
 		/* The wolfpackName field will be ignored if "newsOf == user",
 		 * otherwise
 		 * 		if "wolfpackName" field is present then the response will contain
 		 * 			all posts made by all the members of the given wolfpack,
 		 * 		else ("wolfpackName" field isn't present) the response will contain
 		 * 			all the posts readable for the "logged in" user (made by all
 		 * 			members from all the “logged in” user wolfpacks).
 		 */
 		String wolfpackName;
 		/* The userID field will be ignored if "newsOf == wolfpack",
 		 * otherwise
 		 * 		if "userID" field is present then the response will contain
 		 * 			all the posts of the given user,
 		 * 		else ("userID" field isn't present) the response will contain
 		 * 			all the posts made by the “logged in” user.
 		 */
 		String userID;
 		//The max amount of posts to retrieve.
 		Integer maxMessages;
 		//Time in milliseconds since 1970, to retrieve posts older than this date.
 		Long olderThan;
 		//Time in milliseconds since 1970, to retrieve posts newer than this date.
 		Long newerThan;
 	}
 
 	public static class PostData implements Comparable<PostData>{
 		String itemID;
 		String senderID;
 		String senderName;
 		Long timestamp;
 		String mail;
 
 		PostData(String postID, String senderID, String senderName, Long timestamp, String post) {
 			this.itemID = postID;
 			this.senderID = senderID;
 			this.senderName = senderName;
 			this.timestamp = timestamp;
 			this.mail = post;
 		}
 
 		@Override
 		public int compareTo(PostData o) {
 			return -Long.signum(this.timestamp - o.timestamp); //"-" for ordering from newer posts to older
 		}
 
 		public String toString() {
 			return "PostData " +
 					"( itemID: " + itemID +
 					", senderID: " + senderID +
 					", senderName: " + senderName +
 					", timestamp: " + timestamp.toString() +
 					", mail: \"" + mail + "\" )";
 		}
 	}
 
 	public static class NewsFeedResponse extends EWolfResponse {
 		public Set<PostData> mailList;
 
 		public NewsFeedResponse(Set<PostData> postList) {
 			this.mailList = postList;
 		}
 
 		public NewsFeedResponse(String result) {
 			super(result);
 		}
 
 		public NewsFeedResponse(String result, String errorMessage) {
 			super(result, errorMessage);
 		}
 	}
 
 	/**
 	 * @param	jsonReq	serialized object of JsonReqNewsFeedParams class  
 	 * @return	list of posts, each contains post ID, sender ID, sender name, timestamp and post text
 	 */
 	@Override
 	public EWolfResponse handleData(JsonElement jsonReq) {
 		Long curTime = new Date().getTime();
 		if (curTime - lastModified.getTime() > cachedTime) {
 			newsFeed = fetchAllPosts();
 			lastModified = new Date();
 		}
 		
 		Gson gson = new Gson();
 		JsonReqNewsFeedParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqNewsFeedParams.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new NewsFeedResponse(RES_BAD_REQUEST);
 		}
 		
 		if (jsonReqParams.newsOf == null) {
 			return new NewsFeedResponse(RES_BAD_REQUEST,
 					"Must specify whose news feed to fetch.");
 		}
 		
 		List<Post> posts;
 		try {
 			if (jsonReqParams.newsOf.equals("user")) {
 				posts = fetchPostsForUser(jsonReqParams.userID);
 			} else if (jsonReqParams.newsOf.equals("wolfpack")) {
 				posts = fetchPostsForWolfpack(jsonReqParams.wolfpackName);
 			} else {
 				return new NewsFeedResponse(RES_BAD_REQUEST,
 						"Request type should be either \"user\" or \"wolfpack\"");
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			return new NewsFeedResponse(RES_BAD_REQUEST, "Illegal user ID.");
 		} catch (ProfileNotFoundException e) {
 			e.printStackTrace();
 			return new NewsFeedResponse(RES_NOT_FOUND, "User with given ID wasn't found.");
 		}
		
		return new NewsFeedResponse(filterPosts(posts, jsonReqParams.maxMessages,
				jsonReqParams.newerThan, jsonReqParams.olderThan));
 	}
 
 	private Map<Profile, List<Post>> fetchAllPosts() {
 		Map<Profile, List<Post>> allPosts = new HashMap<Profile, List<Post>>();
 
 		List<WolfPack> wolfpacks = socialGroupsManager.getAllSocialGroups();
 		Set<Profile> profiles = new HashSet<Profile>();
 
 		for (WolfPack w : wolfpacks) {
 			profiles.addAll(w.getMembers());
 		}
 		//add self profile
 		profiles.add(socialFS.getCredentials().getProfile());
 
 		for (Profile profile: profiles) {
 			try {
 				List<Post> posts = snet.getWall(profile).getAllPosts();
 				allPosts.put(profile, posts);
 			} catch (WallNotFound e) {
 				Profile user = socialFS.getCredentials().getProfile();
 				System.err.println("User " + user.getName() + ": " + user.getUserId() +
 						" isn't allowed to view posts of " +
 						profile.getName() + ": " + profile.getUserId() + ".");
 				//e.printStackTrace();
 			} catch (FileNotFoundException e) {
 				Profile user = socialFS.getCredentials().getProfile();
 				System.err.println("User " + user.getName() + ": " + user.getUserId() +
 						" isn't allowed to view posts of " +
 						profile.getName() + ": " + profile.getUserId() + ".");
 				//e.printStackTrace();
 			}
 		}
 
 		return allPosts;
 	}
 	
 	private Set<PostData> filterPosts(List<Post> posts, Integer filterNumOfPosts,
 			Long filterFromDate, Long filterToDate) {
 		List<PostData> lst = new ArrayList<PostData>();
 		for (Post post: posts) {
 			String name;
 			String id;
 			try {
 				Profile postOwner = post.getOwner();
 				name = postOwner.getName();
 				id = postOwner.getUserId().toString();
 			} catch (ProfileNotFoundException e) {
 				name = POST_OWNER_NOT_FOUND_MESSAGE;
 				id = POST_OWNER_NOT_FOUND_MESSAGE;
 			}
 			Long timestamp = post.getTimestamp();
 			if (filterFromDate==null || filterFromDate<=timestamp) {
 				if (filterToDate==null || filterToDate>=timestamp) {
 					lst.add(new PostData(post.getPostId().toString(), id,
 							name, post.getTimestamp(), ((TextPost)post).getText()));
 				}
 			}
 		}
 		//sort by timestamp
 		Collections.sort(lst);
 
 		if (filterNumOfPosts != null && lst.size() > filterNumOfPosts) {
 			lst = lst.subList(0, filterNumOfPosts);
 		}
 		return new TreeSet<PostData>(lst);
 	}
 
 	private List<Post> fetchPostsForWolfpack(String socialGroupName) {
 		List<Post> posts = new ArrayList<Post>();
 
 		if (socialGroupName == null) {
 			for (Map.Entry<Profile, List<Post>> entry : newsFeed.entrySet()) {
 				posts.addAll(entry.getValue());
 			}
 		} else {
 			WolfPack wp = socialGroupsManager.findSocialGroup(socialGroupName);
 			if (wp == null) {
 				return posts;
 			}
 			List<Profile> profiles = wp.getMembers();
 			//TODO improve
 			for (Profile p : profiles) {
 				posts.addAll(newsFeed.get(p));
 			}
 		}
 
 		return posts;
 	}
 
 	private List<Post> fetchPostsForUser(String strUid) throws ProfileNotFoundException {
 		Profile profile;
 		if (strUid==null) {
 			profile = socialFS.getCredentials().getProfile();
 		} else {
 			UserID uid = userIDFactory.getFromBase64(strUid);
 			profile = socialFS.findProfile(uid);
 		}
 		return newsFeed.get(profile);
 	}
 
 }

 package il.technion.ewolf.server.jsonDataHandlers;
 
 import static il.technion.ewolf.server.EWolfResponse.RES_BAD_REQUEST;
 import static il.technion.ewolf.server.EWolfResponse.RES_INTERNAL_SERVER_ERROR;
 import static il.technion.ewolf.server.EWolfResponse.RES_NOT_FOUND;
 import il.technion.ewolf.ewolf.SocialNetwork;
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.exceptions.WallNotFound;
 import il.technion.ewolf.posts.Post;
 import il.technion.ewolf.posts.TextPost;
 import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.server.cache.ICache;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.stash.exception.GroupNotFoundException;
 
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 public class PostToNewsFeedHandler implements IJsonDataHandler {
 	private final SocialNetwork snet;
 	private final Provider<TextPost> textPostProvider;
 
 	private final ICache<Map<Profile, List<Post>>> newsFeedCache;
 	private final ICache<Map<String, WolfPack>> wolfpacksCache;
 
 	@Inject
 	public PostToNewsFeedHandler(SocialNetwork snet,
 			Provider<TextPost> textPostProvider,
 			ICache<Map<Profile, List<Post>>> newsFeedCache,
 			ICache<Map<String, WolfPack>> wolfpacksCache) {
 		this.snet = snet;
 		this.textPostProvider = textPostProvider;
 		this.newsFeedCache = newsFeedCache;
 		this.wolfpacksCache = wolfpacksCache;
 	}
 
 	private static class JsonReqPostToNewsFeedParams {
 		String wolfpackName;
 		//post text
 		String post;
 	}
 
 	static class PostToNewsFeedResponse extends EWolfResponse {
 		public PostToNewsFeedResponse(String result) {
 			super(result);
 		}
 
 		public PostToNewsFeedResponse(String result, String errorMessage) {
 			super(result, errorMessage);
 		}
 
 		public PostToNewsFeedResponse() {
 		}
 	}
 
 	/**
 	 * @param	jsonReq	serialized object of JsonReqCreateWolfpackParams class
 	 * @return	"success" or error message
 	 */
 	@Override
 	public EWolfResponse handleData(JsonElement jsonReq) {
 		Gson gson = new Gson();
 		JsonReqPostToNewsFeedParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqPostToNewsFeedParams.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_BAD_REQUEST);
 		}
		if (jsonReqParams.wolfpackName == null || jsonReqParams.post == null) {
 			return new PostToNewsFeedResponse(RES_BAD_REQUEST,
 					"Must specify both wolfpack name and post text.");
 		}
 
 		Map<String, WolfPack> wolfpacksMap = wolfpacksCache.get();
 		WolfPack wolfpack = wolfpacksMap.get(jsonReqParams.wolfpackName);
 
 		if (wolfpack == null) {
 			return new PostToNewsFeedResponse(RES_NOT_FOUND, "Given wolfpack wasn't found.");
 		}
 
 		try {
 			snet.getWall().publish(textPostProvider.get().setText(jsonReqParams.post), wolfpack);
 		} catch (GroupNotFoundException e) {
 			System.out.println("Wolfpack " + jsonReqParams.wolfpackName + " not found");
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_INTERNAL_SERVER_ERROR);
 		} catch (WallNotFound e) {
 			System.out.println("Wall not found.");
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_INTERNAL_SERVER_ERROR);
 		} catch (FileNotFoundException e) {
 			System.out.println("File /wall/posts/ not found");
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_INTERNAL_SERVER_ERROR);
 		}
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				newsFeedCache.update();
 			}
 		}).start();
 
 		return new PostToNewsFeedResponse();
 	}
 
 }

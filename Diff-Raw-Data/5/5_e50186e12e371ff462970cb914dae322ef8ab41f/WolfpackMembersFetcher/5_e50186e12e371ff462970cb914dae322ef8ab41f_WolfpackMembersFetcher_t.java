 package il.technion.ewolf.server.jsonDataHandlers;
 
 import static il.technion.ewolf.server.EWolfResponse.RES_BAD_REQUEST;
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.server.cache.ICache;
 import il.technion.ewolf.socialfs.Profile;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.inject.Inject;
 
 public class WolfpackMembersFetcher implements IJsonDataHandler {
 	private final ICache<Map<WolfPack,List<Profile>>> wolfpacksMembersCache;
 	private final ICache<List<WolfPack>> wolfpacksCache;
 
 	@Inject
 	public WolfpackMembersFetcher(
 			ICache<Map<WolfPack,List<Profile>>> wolfpacksMembersCache,
 			ICache<List<WolfPack>> wolfpacksCache) {
 		this.wolfpacksMembersCache = wolfpacksMembersCache;
 		this.wolfpacksCache = wolfpacksCache;
 	}
 
 	static class ProfileData {
 		String name;
 		String id;
 
 		ProfileData(String name, String id) {
 			this.name = name;
 			this.id = id;
 		}
 	}
 
 	static class WolfpackMembersResponse extends EWolfResponse {
 		List<ProfileData> membersList;
 		public WolfpackMembersResponse(List<ProfileData> lst) {
 			this.membersList = lst;
 		}
 
 		public WolfpackMembersResponse(String result) {
 			super(result);
 		}
 	}
 
 	private static class JsonReqWolfpackMembersParams {
 		//		If wolfpackName field wasn't sent with the request then
 		//		the response list will contain all the members of all the "logged in" user wolfpacks
 		String wolfpackName;
 	}
 
 	/**
 	 * @param	jsonReq	serialized object of JsonReqWolfpackMembersParams class
 	 * @return	list of ProfileData objects. Each object contains name and user ID.
 	 */
 	@Override
 	public EWolfResponse handleData(JsonElement jsonReq) {
 		Gson gson = new Gson();
 		JsonReqWolfpackMembersParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqWolfpackMembersParams.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new WolfpackMembersResponse(RES_BAD_REQUEST);
 		}
 
 		Map<WolfPack,List<Profile>> wolfpacksMembersMap = wolfpacksMembersCache.get();
 		List<WolfPack> wolfpacks = wolfpacksCache.get();
 		Set<Profile> profiles = new HashSet<Profile>();
 		if (jsonReqParams.wolfpackName == null) {
 			for (Map.Entry<WolfPack,List<Profile>> entry : wolfpacksMembersMap.entrySet()) {
 				profiles.addAll(entry.getValue());
 			}
 		} else {
 			for (WolfPack w : wolfpacks) {
 				if (w.getName().equals(jsonReqParams.wolfpackName)) {
 					List<Profile> wMembers = wolfpacksMembersMap.get(w);
 					if (wMembers != null)
 						profiles.addAll(wMembers);
 				}
 			}
 		}
 
 		List<ProfileData> resList = new ArrayList<ProfileData>();
 
 		for (Profile profile: profiles) {
 			resList.add(new ProfileData(profile.getName(), profile.getUserId().toString()));
 		}
 		return new WolfpackMembersResponse(resList);
 	}
 
 }

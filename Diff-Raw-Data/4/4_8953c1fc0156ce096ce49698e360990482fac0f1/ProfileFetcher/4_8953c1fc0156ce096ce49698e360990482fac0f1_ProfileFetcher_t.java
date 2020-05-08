 package il.technion.ewolf.server.fetchers;
 
import com.google.inject.Inject;

 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.UserIDFactory;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 
 public class ProfileFetcher implements JsonDataFetcher {
 	private final SocialFS socialFS;
 	private final UserIDFactory userIDFactory;
 
	@Inject
 	public ProfileFetcher(SocialFS socialFS, UserIDFactory userIDFactory) {
 		this.socialFS = socialFS;
 		this.userIDFactory = userIDFactory;
 	}
 	
 	@SuppressWarnings("unused")
 	private class ProfileData {
 		private String name;
 		private String id;
 	
 		private ProfileData(String name, String id) {
 			this.name = name;
 			this.id = id;
 		}
 	}
 
 	/**
 	 * @param	parameters	user ID in parameters[0]  
 	 * @return	ProfileData object that contains user's name and ID
 	 */
 	@Override
 	public Object fetchData(String... parameters)
 			throws ProfileNotFoundException {
 		if(parameters.length != 1) {
 			return null;
 		}
 		
 		String strUid = parameters[0];
 		Profile profile;
 		if (strUid.equals("my")) {
 			profile = socialFS.getCredentials().getProfile();
 			strUid = profile.getUserId().toString();
 		} else {
 			UserID uid = userIDFactory.getFromBase64(strUid);
 			profile = socialFS.findProfile(uid);			
 		}
 		return new ProfileData(profile.getName(), strUid);
 	}
 }

 package com.opensajux.integration;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.opensajux.common.Chosen;
 import com.opensajux.dto.FacebookStatusMessage;
 import com.opensajux.dto.SiteDetails;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.FacebookClient.AccessToken;
 import com.restfb.Parameter;
 
 @Singleton
 public class FacebookClient implements Serializable {
 	private static final long serialVersionUID = 7845735358098351478L;
	private com.restfb.FacebookClient facebook;
 
 	@Inject
 	public FacebookClient(@Chosen SiteDetails siteDetails) {
		AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(siteDetails.getFacebookAppId(),
				siteDetails.getFacebookAppSecret());
 		facebook = new DefaultFacebookClient(siteDetails.getFacebookAccessToken());
 	}
 
 	public List<FacebookStatusMessage> getUserPosts(String user) {
 		return facebook.executeQuery("select status_id, message, time from status where uid=100002229454916",
 				FacebookStatusMessage.class, Parameter.with("type", "status"));
 	}
 }

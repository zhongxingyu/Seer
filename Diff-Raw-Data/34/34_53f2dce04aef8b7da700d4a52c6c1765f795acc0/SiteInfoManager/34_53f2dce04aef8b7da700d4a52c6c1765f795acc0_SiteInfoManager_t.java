 package com.opensajux.view;
 
 import java.util.List;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import com.opensajux.common.Chosen;
 import com.opensajux.dto.SiteDetails;
 import com.opensajux.entity.SiteInfo;
 
 @Named
 @ApplicationScoped
 public class SiteInfoManager {
 	@Inject
 	private transient PersistenceManagerFactory pmf;
 
 	private SiteDetails siteDetails;
 
 	@Produces
 	@Chosen
 	@SuppressWarnings("unchecked")
 	public SiteDetails getSiteDetails() {
 		if (siteDetails == null) {
 			PersistenceManager pm = pmf.getPersistenceManager();
 			Query query = pm.newQuery("select from " + SiteInfo.class.getName());
 			List<SiteInfo> list = (List<SiteInfo>) query.execute();
 			siteDetails = new SiteDetails();
 			if (list != null && list.size() > 0) {
 				SiteInfo siteInfo = list.get(0);
 				siteDetails.setAboutMe(siteInfo.getAboutMe());
 				siteDetails.setTitle(siteInfo.getTitle());
 				siteDetails.setSubTitle(siteInfo.getSubTitle());
 				siteDetails.setGoogleApiKey(siteInfo.getGoogleApiKey());
 				siteDetails.setTwitterAccessToken(siteInfo.getTwitterAccessToken());
 				siteDetails.setTwitterAccessTokenSecret(siteInfo.getTwitterAccessTokenSecret());
 				siteDetails.setTwitterConsumerKey(siteInfo.getTwitterConsumerKey());
 				siteDetails.setTwitterConsumerSecret(siteInfo.getTwitterConsumerSecret());
 				siteDetails.setFacebookAppId(siteInfo.getFacebookAppId());
 				siteDetails.setFacebookAppSecret(siteInfo.getFacebookAppSecret());
 				siteDetails.setFacebookAccessToken(siteInfo.getFacebookAccessToken());
 			}
 			pm.close();
 
 		}
 		return siteDetails;
 	}
 
	@SuppressWarnings("unchecked")
 	public String saveSiteInfo() {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Query query = pm.newQuery("select from " + SiteInfo.class.getName());
 		List<SiteInfo> list = (List<SiteInfo>) query.execute();
 		SiteInfo siteInfo = list.get(0);
 		siteInfo.setAboutMe(siteDetails.getAboutMe());
 		siteInfo.setTitle(siteDetails.getTitle());
 		siteInfo.setSubTitle(siteDetails.getSubTitle());
 		siteInfo.setGoogleApiKey(siteDetails.getGoogleApiKey());
 		siteInfo.setTwitterAccessToken(siteDetails.getTwitterAccessToken());
 		siteInfo.setTwitterAccessTokenSecret(siteDetails.getTwitterAccessTokenSecret());
 		siteInfo.setTwitterConsumerKey(siteDetails.getTwitterConsumerKey());
 		siteInfo.setTwitterConsumerSecret(siteDetails.getTwitterConsumerSecret());
 		siteInfo.setFacebookAppId(siteDetails.getFacebookAppId());
 		siteInfo.setFacebookAppSecret(siteDetails.getFacebookAppSecret());
 		siteInfo.setFacebookAccessToken(siteDetails.getFacebookAccessToken());
 		pm.makePersistent(siteInfo);
 		pm.close();
 		return "/admin/index.jsf";
 	}
 }

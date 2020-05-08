 package com.opensajux.view;
 
 import java.util.List;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import com.google.appengine.api.datastore.Text;
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
 				siteDetails.setAboutMe(siteInfo.getAboutMe().getValue());
 				siteDetails.setTitle(siteInfo.getTitle());
 				siteDetails.setSubTitle(siteInfo.getSubTitle());
 				siteDetails.setGoogleUserId(siteInfo.getGoogleUserId());
 				siteDetails.setGoogleApiKey(siteInfo.getGoogleApiKey());
 				siteDetails.setGoogleAnalyticsId(siteInfo.getGoogleAnalyticsId());
 				siteDetails.setTwitterUsername(siteInfo.getTwitterUsername());
 				siteDetails.setTwitterAccessToken(siteInfo.getTwitterAccessToken());
 				siteDetails.setTwitterAccessTokenSecret(siteInfo.getTwitterAccessTokenSecret());
 				siteDetails.setTwitterConsumerKey(siteInfo.getTwitterConsumerKey());
 				siteDetails.setTwitterConsumerSecret(siteInfo.getTwitterConsumerSecret());
 				siteDetails.setFacebookUsername(siteInfo.getFacebookUsername());
 				siteDetails.setFacebookAppId(siteInfo.getFacebookAppId());
 				siteDetails.setFacebookAppSecret(siteInfo.getFacebookAppSecret());
 				siteDetails.setFacebookAccessToken(siteInfo.getFacebookAccessToken());
 				siteDetails.setLinkedinUsername(siteInfo.getLinkedinUsername());
 				siteDetails.setLinkedinApiKey(siteInfo.getLinkedinApiKey());
 				siteDetails.setLinkedinSecretKey(siteInfo.getLinkedinSecretKey());
 				siteDetails.setLinkedinUserSecret(siteInfo.getLinkedinUserSecret());
 				siteDetails.setLinkedinUserToken(siteInfo.getLinkedinUserToken());
 			}
 			pm.close();
 
 		}
 		return siteDetails;
 	}
 
 	@SuppressWarnings("unchecked")
 	public String saveSiteInfo() {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Query query = pm.newQuery(SiteInfo.class);
 		List<SiteInfo> list = (List<SiteInfo>) query.execute();
		SiteInfo siteInfo = list.get(0);
 		siteInfo.setAboutMe(new Text(siteDetails.getAboutMe()));
 		siteInfo.setTitle(siteDetails.getTitle());
 		siteInfo.setSubTitle(siteDetails.getSubTitle());
 		siteInfo.setGoogleUserId(siteDetails.getGoogleUserId());
 		siteInfo.setGoogleApiKey(siteDetails.getGoogleApiKey());
 		siteInfo.setGoogleAnalyticsId(siteDetails.getGoogleAnalyticsId());
 		siteInfo.setTwitterUsername(siteDetails.getTwitterUsername());
 		siteInfo.setTwitterAccessToken(siteDetails.getTwitterAccessToken());
 		siteInfo.setTwitterAccessTokenSecret(siteDetails.getTwitterAccessTokenSecret());
 		siteInfo.setTwitterConsumerKey(siteDetails.getTwitterConsumerKey());
 		siteInfo.setTwitterConsumerSecret(siteDetails.getTwitterConsumerSecret());
 		siteInfo.setFacebookUsername(siteDetails.getFacebookUsername());
 		siteInfo.setFacebookAppId(siteDetails.getFacebookAppId());
 		siteInfo.setFacebookAppSecret(siteDetails.getFacebookAppSecret());
 		siteInfo.setFacebookAccessToken(siteDetails.getFacebookAccessToken());
 		siteInfo.setLinkedinUsername(siteDetails.getLinkedinUsername());
 		siteInfo.setLinkedinApiKey(siteDetails.getLinkedinApiKey());
 		siteInfo.setLinkedinSecretKey(siteDetails.getLinkedinSecretKey());
 		siteInfo.setLinkedinUserSecret(siteDetails.getLinkedinUserSecret());
 		siteInfo.setLinkedinUserToken(siteDetails.getLinkedinUserToken());
 		pm.makePersistent(siteInfo);
 		pm.close();
 		return "/admin/index.jsf";
 	}
 }

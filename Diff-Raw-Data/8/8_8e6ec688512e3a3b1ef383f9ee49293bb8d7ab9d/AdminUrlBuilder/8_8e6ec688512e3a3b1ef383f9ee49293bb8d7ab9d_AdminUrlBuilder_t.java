 package nz.co.searchwellington.controllers.admin;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import nz.co.searchwellington.model.FrontendFeedNewsitem;
 import nz.co.searchwellington.model.SiteInformation;
 import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.frontend.FrontendFeed;
 import nz.co.searchwellington.model.frontend.FrontendResource;
 import nz.co.searchwellington.model.frontend.FrontendWebsite;
 import nz.co.searchwellington.urls.UrlBuilder;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class AdminUrlBuilder {
 	
 	private SiteInformation siteInformation;
 	private UrlBuilder urlBuilder;
 	
 	@Autowired
 	public AdminUrlBuilder(SiteInformation siteInformation, UrlBuilder urlBuilder) {		
 		this.siteInformation = siteInformation;
 		this.urlBuilder = urlBuilder;
 	}
 	
 	public String getResourceEditUrl(FrontendResource resource) {
 		return siteInformation.getUrl() + "/edit?resource=" + resource.getId();						
 	}
 	
 	public String getResourceDeleteUrl(FrontendResource resource) {
 		return siteInformation.getUrl() + "/delete?resource=" + resource.getId();		
 	}
 	
 	public String getResourceCheckUrl(FrontendResource resource) {
 		return siteInformation.getUrl() + "/admin/linkchecker/add?resource=" + resource.getId();
 	}
 	
 	public String getViewSnapshotUrl(FrontendResource resource) throws UnsupportedEncodingException {
 		final String resourceUrl = siteInformation.getUrl() + "/" + resource.getUrlWords();
 		return resourceUrl + "/viewsnapshot";	
 	}
 	
	public String getFeednewsItemAcceptUrl(FrontendFeed feed, FrontendFeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/accept?feed=" + feed.getUrlWords() + "&url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");		
 	}
 	
 	public String getFeedNewsitemSuppressUrl(FrontendFeedNewsitem feednewsitem) throws UnsupportedEncodingException {
 		return siteInformation.getUrl() + "/supress/supress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
 	}
 	
 	public String getFeedNewsitemUnsuppressUrl(FrontendFeedNewsitem feednewsitem) throws UnsupportedEncodingException {
 		return siteInformation.getUrl() + "/supress/unsupress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
 	}
 	
 	public String getPublisherAutoGatherUrl(FrontendWebsite resource) throws UnsupportedEncodingException {
 		final String resourceUrl = urlBuilder.getResourceUrl(resource);
 		if (resourceUrl != null) {
 			return resourceUrl + "/gather";
 		}
 		return null;
 	}
 	
 	public String getAddTagUrl() throws UnsupportedEncodingException {
 		return siteInformation.getUrl() + "/edit/tag/submit";
 	}
 		
 }

 package nz.co.searchwellington.urls;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Date;
 
 import nz.co.searchwellington.dates.DateFormatter;
 import nz.co.searchwellington.model.Geocode;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.SiteInformation;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.UrlWordsGenerator;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.model.frontend.FrontendFeed;
 import nz.co.searchwellington.model.frontend.FrontendNewsitem;
 import nz.co.searchwellington.twitter.TwitterService;
 
 public class UrlBuilder {
 
 	private SiteInformation siteInformation;
 	private TwitterService twitterService;
 	private DateFormatter dateFormatter;
 	
 	public UrlBuilder(SiteInformation siteInformation, TwitterService twitterService, DateFormatter dateFormatter) {		
 		this.siteInformation = siteInformation;
 		this.twitterService = twitterService;
 		this.dateFormatter = dateFormatter;
 	}
 	
 	public String getHomeUrl() {
 		return siteInformation.getUrl();
 	}
 	
 	public String getImageUrl(String filename) {
 		return siteInformation.getImageRoot() + filename;
 	}
 	
 	public String getStaticUrl(String filename) {
 		return siteInformation.getStaticRoot() + filename;
 	}
 	
 	public String getPublishersAutoCompleteUrl() {
 		return siteInformation.getUrl() + "/ajax/publishers";
 	}
 	
 	public String getTagsAutoCompleteUrl() {
 		return siteInformation.getUrl() + "/ajax/tags";
 	}
 	
 	public String getTwitterReactionsUrl() {
 		return siteInformation.getUrl() + "/twitter";
 	}
 	
 	public String getFeedUrl(FrontendFeed feed) {		
 		return siteInformation.getUrl() + "/feed/" + feed.getUrlWords();
 	}
 	
 	public String getFeedUrlFromFeedName(String feedname) {		
 		return siteInformation.getUrl() + "/feed/" + UrlWordsGenerator.makeUrlWordsFromName(feedname);
 	}
 	
 	
 	public String getFeedsInboxUrl() {
 		return siteInformation.getUrl() + "/feeds/inbox";
 	}
 	
 	public String getFeedsUrl() {
 		return siteInformation.getUrl() + "/feeds";
 	}
 	
 	public String getTagUrl(Tag tag) {
 		return siteInformation.getUrl() + "/" + tag.getName();
 	}
 	
 	
 	public String getAutoTagUrl(Tag tag) {
 		return siteInformation.getUrl() + "/autotag/" + tag.getName();
 	}
 
 	public String getTagCombinerUrl(Tag firstTag, Tag secondTag) {
 		return siteInformation.getUrl() + "/" + firstTag.getName() + "+" + secondTag.getName();
 	}
 	
 	public String getTagSearchUrl(Tag tag, String keywords) {
 		return getTagUrl(tag) + "?keywords=" + urlEncode(keywords);
 	}
 	
 	public String getLocalPageUrl(FrontendNewsitem newsitem) {
 		return siteInformation.getUrl() + UrlWordsGenerator.markUrlForNewsitem(newsitem);
 	}
 	
 	public String getPublisherUrl(String publisherName) {
		if (publisherName != null) {
			return siteInformation.getUrl() + "/" + UrlWordsGenerator.makeUrlWordsFromName(publisherName);
		}
		return null;
 	}
 
 	public String getPublisherCombinerUrl(String publisherName, Tag tag) {
 		return siteInformation.getUrl() + "/" + UrlWordsGenerator.makeUrlWordsFromName(publisherName) + "+" + tag.getName();
 	}
 
 	public String getTagCommentUrl(Tag tag) {
 		return siteInformation.getUrl() + "/" + tag.getName() + "/comment";
 	}
 	
 	public String getTagGeocodedUrl(Tag tag) {
 		return siteInformation.getUrl() + "/" + tag.getName() + "/geotagged";
 	}
 
 	public String getCommentUrl() {
 		return siteInformation.getUrl() + "/comment";
 	}
 
 	public String getJustinUrl() {
 		return siteInformation.getUrl() + "/justin";
 	}
 
 	public String getGeotaggedUrl() {
 		return siteInformation.getUrl() + "/geotagged";
 	}
 	
 	public String getPublicTaggingSubmissionUrl(Resource resource) {
 		return siteInformation.getUrl() + "/tagging/submit";
 	}
 	
 	@Deprecated // TODO Inline
 	public String getTaggingUrl(FrontendNewsitem newsitem) {
 		return this.getLocalPageUrl(newsitem);
 	}
 	
 	public String getArchiveUrl() {
 		return siteInformation.getUrl() + "/archive";
 	}
 	
 	public String getArchiveLinkUrl(Date date) {
 		return siteInformation.getUrl() + "/archive/" + dateFormatter.formatDate(date, "yyyy") + "/" + dateFormatter.formatDate(date, "MMM").toLowerCase();		
 	}
 	
 	public String getOpenIDCallbackUrl() {
 		return siteInformation.getUrl() + "/openid/callback";
 	}
 	
 	@Deprecated
 	public String getProfileUrl(User user) {
 		return siteInformation.getUrl() + "/profiles/" + user.getProfilename();
 	}
 	
 	public String getProfileUrlFromProfileName(String username) {
 		return siteInformation.getUrl() + "/profiles/" + urlEncode(username);
 	}
 
 	public String getWatchlistUrl() {
 		return siteInformation.getUrl() + "/watchlist";
 	}
 
 	public String getTwitterCallbackUrl() {
 		return siteInformation.getUrl() + "/twitter/callback";
 	}
 
 	public String getLocationUrlFor(Geocode somewhere) {		
 		return siteInformation.getUrl() + "/geotagged?location=" + urlEncode(somewhere.getAddress());
 	}
 
 	public String getSearchUrlFor(String keywords) {
 		return siteInformation.getUrl() + "/search?keywords=" + urlEncode(keywords);
 	}
 	
 	public String getTagSearchUrlFor(String keywords, Tag tag) {
 		return getTagUrl(tag) + "?keywords=" + urlEncode(keywords);
 	}
 	
 	public String getTwitterProfileImageUrlFor(String twitterUsername) {
 		return twitterService.getTwitterProfileImageUrlFor(twitterUsername);
 	}
 	
 	public String getSubmitWebsiteUrl() {
 		return siteInformation.getUrl() + "/edit/submit/website";
 	}
 	
 	public String getSubmitNewsitemUrl() {
 		return siteInformation.getUrl() + "/edit/submit/newsitem";
 	}
 	
 	public String getSubmitFeedUrl() {
 		return siteInformation.getUrl() + "/edit/submit/feed";
 	}
 	
 	private String urlEncode(String keywords) {
 		try {
 			return URLEncoder.encode(keywords, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 	
 }

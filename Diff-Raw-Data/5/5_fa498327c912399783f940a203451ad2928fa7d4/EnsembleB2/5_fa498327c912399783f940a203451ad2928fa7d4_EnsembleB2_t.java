 package edu.syr.ischool.mafudge.ensemblelib;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 
 import edu.syr.ischool.mafudge.ensemblelib.models.*;
 import edu.syr.ischool.mafudge.ensemblelib.repositories.*;
 
 
 // Main Class Which Brokers All Activity with Ensemble
 public class EnsembleB2 {
 
 	private String m_serverUrl;
 	private String m_apiKey;
 	private String m_secretKey;
 	private String m_domain;
 	private String m_secureApiBaseUrl;
 	private String m_simpleApiBaseUrl;
 	private VideoRepository m_vr;
 	private WebDestinationRepository m_wd;
 	private InstContentRepository m_ic;
 	private SimpleHttpClient m_http;
 
 	// This is the proper constructor going forward
 	public EnsembleB2(String serverUrl, String apiKey, String secretKey, String domain)
 	{
 		m_vr = new VideoRepository();
 		m_ic = new InstContentRepository();
 		m_wd = new WebDestinationRepository();
 		m_http= new SimpleHttpClient();
 		m_apiKey = apiKey;
 		m_secretKey = secretKey;
 		m_domain = domain;
 		m_serverUrl =  serverUrl.replaceAll("/$", "");
 		m_simpleApiBaseUrl = m_serverUrl + "/app/simpleAPI";
 		m_secureApiBaseUrl = m_serverUrl + "/blackBoardAPI/Service.svc/" + m_apiKey;
 	}
 	
 	
 	public String TestApiWithResult() throws Exception {
 		String result;
 		DateTime nowUtc = (new DateTime()).withZone(DateTimeZone.UTC);
 		String timeStamp = nowUtc.toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
 		String testUrl = this.m_secureApiBaseUrl + "/Test?ts=" + timeStamp + "&hmac=";
 		String hmac = HMacMD5Encoder.Encode(this.m_secretKey, testUrl.toLowerCase());
 		result = m_http.webGet(testUrl + hmac).replaceAll("\\<.*?\\>", "");
 		return result;
 	}
 	
 	public Boolean TestApi() throws Exception {
 		String Expected = "Success";
 		String result = this.TestApiWithResult();
 		return (result.indexOf(Expected) > 0);
 	}
 
 	public VideoRepository getVideoRepository()
 	{
 		return m_vr;
 	}
 	
 	public InstContentRepository getInstContentRepository()
 	{
 		return m_ic;
 	}
 	
 	public String getServerUrl() { return m_serverUrl; }
 	public String getApiKey() { return m_apiKey; }
 	public String getSecretKey() { return m_secretKey; }
 	public String getDomain() { return m_domain; }
 	
 	
 	public List<Video> getVideosByUrl(String stringUrl) throws Exception
 	{
 		String result = m_http.get(stringUrl);
 		if (result.length()>0) {
 			m_vr.fromRawXmlString(result);
 		}
 		return m_vr.getVideos();
 	}
 	
 	public List<Video> getVideosByWebDestination(String webDestinationId) throws Exception	{
 		return getVideosByUrl(getWebDestinationHref(webDestinationId));
 	}
 	
 	public String getWebDestinationHref(String webDestinationId) throws Exception {
 		return (this.m_simpleApiBaseUrl + "/video/list.xml/" + webDestinationId);
 	}
 	
 	// Pre-Ensemble 3.4
 	public String getPluginUrl() {
 		return (this.m_serverUrl + "/app/plugin/plugin.aspx");
 	}
 	
 	// Ensemble 3.4 and up
 	public String getEmbedPluginUrl() {
 		return (this.m_serverUrl + "/app/plugin/embed.aspx");
 		
 	}
 	
 	public String getContentUrl(String contentId) throws Exception {
 		String requestUrl = this.buildPreviewUrl(contentId);
 		String hmac = HMacMD5Encoder.Encode(this.m_secretKey, requestUrl.toLowerCase());
 		return (requestUrl + hmac);
 	}
 		
 	public String getWebDestinationHtml(String webDestinationID) {
 		// Ensemble 3.4 and up
 		return getWebDestinationHtmlAsIFrame(webDestinationID);
 		// return getWebDestinationHtmlAsJavascript(webDestinationID);
 	}
 
 	// Ensemble pre 3.4
 	public String getWebDestinationHtmlAsJavascript(String webDestinationID) {
 		String plugInUrl = this.getPluginUrl();  		
 		String embedHtml = "<div id=\"ensembleContentContainer_" + webDestinationID + "\" class=\"ensembleContentContainer\" style=\"width: 99%; height: 1000px;\">";
 		embedHtml += "<script type=\"text/javascript\" src=\"" + plugInUrl + "?destinationID=" + webDestinationID + "&useIFrame=true\"></script></div>";
 		return embedHtml;
 	}
 
 	// Ensemble 3.4 and higher
 	public String getWebDestinationHtmlAsIFrame(String webDestinationID) {
 		String plugInUrl = this.getEmbedPluginUrl();  		
 		String embedHtml = "<iframe id=\"ensembleFrame_" + webDestinationID + "\" src=\"" + plugInUrl + "?DestinationID=" + webDestinationID + "\" ";
 		embedHtml += "frameborder=\"0\" style=\"width: 99%; height : 1000px;\" allowfullscreen></iframe>"; 
 		return embedHtml;
 	}
 
 	public String getContentHtml(String contentID, String thumbnail){
 		// Ensemble 3.4 and up
 		return getContentHtmlAsIFrame(contentID,thumbnail);
 		//return getContentHtmlAsJavascript(contentID, thumbnail);
 	}
 	
 	// Ensemble pre 3.4
 	public String getContentHtmlAsJavascript(String contentID, String thumbnail) {
 		String plugInUrl = this.getPluginUrl();
 		String embedHtml = "<div id=\"ensembleEmbeddedContent_" + contentID + "\" class=\"ensembleEmbeddedContent\" style=\"width: 320px; height: 320px;\">";
 		embedHtml += thumbnail + "<script type=\"text/javascript\" src=\"" + plugInUrl  + "?contentID=" + contentID;
 		embedHtml += "&useIFrame=true&embed=true&displayTitle=false&startTime=0&autoPlay=false&hideControls=false&showCaptions=false&width=320&height=240\">";
 		embedHtml +="</script></div>";
 		return embedHtml;
 	}
 
 	// Ensemble 3.4 and higher
 	public String getContentHtmlAsIFrame(String contentID, String thumbnail) {
 		String plugInUrl=this.getEmbedPluginUrl();
 		String embedHtml = "<iframe id=\"ensembleEmbeddedContent_" + contentID + "\" src=\"" + plugInUrl + "?ID=" + contentID;
		embedHtml += "&displayTitle=false&startTime=0&autoPlay=false&hideControls=false&showCaptions=false&width=640&height=480\" frameborder=\"0\"";
		embedHtml += "style=\"width: 665px;height:520px;\" width=\"665\" height=\"520\"";
		embedHtml += " allowfullscreen></iframe>";
 		return embedHtml;
 	}
 
 	public List<WebDestination> getWebDestinations(String userName) throws Exception {
 		String result;
 		String requestUrl = this.buildRequestUrl("/webDestinations/user/", getUserWithDomain(userName));
 		String hmac = HMacMD5Encoder.Encode(this.m_secretKey, requestUrl.toLowerCase());
 		result = m_http.webGet(requestUrl + hmac);
 		if (result.length()>0) {
 			m_wd.fromRawXmlString(result);
 		}
 		return m_wd.getWebDestinations();
 	}
 
 	public List<Video> getMediaLibraryVideo(String searchText, String userName) throws Exception {
 		String result = "";
 		String requestUrl = this.buildRequestSearchUrl("/library/user/", getUserWithDomain(userName), searchText);
 		String hmac = HMacMD5Encoder.Encode(this.m_secretKey, requestUrl.toLowerCase());
 		result = m_http.webGet(requestUrl + hmac);
 		if (result.length()>0) {
 			m_vr.fromRawXmlString(result);
 		}
 		return m_vr.getVideos();
 	}
 	
 	public List<Video> getSharedLibraryVideo(String searchText, String userName) throws Exception {
 		String result = "";
 		String requestUrl = this.buildRequestSearchUrl("/sharedlibrary/user/", getUserWithDomain(userName), searchText);
 		String hmac = HMacMD5Encoder.Encode(this.m_secretKey, requestUrl.toLowerCase());
 		result = m_http.webGet(requestUrl + hmac);
 		if (result.length() > 0) {
 			m_vr.fromRawXmlString(result);
 		}
 		return m_vr.getVideos();
 	}
 	
 	public List<Video> getInstContentVideo(String searchText, String xmlInstContent) throws Exception {
 		List<Video> vl = new ArrayList<Video>();
 		if (xmlInstContent.length()> 0) {
 			InstContentRepository wdr = new InstContentRepository();
 			wdr.fromSerializedXmlString(xmlInstContent);
 			for (InstContentWebDestination w : wdr.getWebDestinations()) {
 				String wdUrl =  this.getWebDestinationHref(w.wdId);
 				if (searchText.length()> 0) { wdUrl += "?searchString=" +searchText; }
 				vl.addAll(this.getVideosByUrl(wdUrl));
 			}
 		}
 		return vl;
 	}
 
 	private String getUserWithDomain(String userName){
 		return this.m_domain.length() == 0 ? userName : userName + "@" + this.m_domain;
 	}
 
 	private String buildRequestSearchUrl(String command, String userNameWithDomain, String searchText) {
 		DateTime nowUtc = (new DateTime()).withZone(DateTimeZone.UTC);
 		String timeStamp = nowUtc.toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
 		searchText = searchText.length() == 0 ? "!" : searchText;
 		String requestUrl = this.m_secureApiBaseUrl + command + userNameWithDomain + "?s=" + searchText + "&ts=" + timeStamp + "&hmac=";
 		return requestUrl;
 	}
 
 	private String buildRequestUrl(String command, String userNameWithDomain) {
 		DateTime nowUtc = (new DateTime()).withZone(DateTimeZone.UTC);
 		String timeStamp = nowUtc.toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
 		String requestUrl = this.m_secureApiBaseUrl + command + userNameWithDomain + "?ts=" + timeStamp + "&hmac=";
 		return requestUrl;
 	}
 	
 	private String buildPreviewUrl(String contentId) {
 		DateTime nowUtc = (new DateTime()).withZone(DateTimeZone.UTC);
 		String timeStamp = nowUtc.toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
 		String requestUrl = this.m_secureApiBaseUrl + "/content/" +contentId + "?ts=" + timeStamp + "&hmac=";
 		return requestUrl;
 		
 	}
 
 }

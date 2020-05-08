 package toctep.skynet.backend.dal.domain;
 
 import java.util.Date;
 
 public class User extends Domain {
 
 	private long twitterId;
 	private Place place;
 	private boolean defaultProfile;
 	private int statusesCount;
 	private long profileBackgroundTile;
 	private Language language;
 	private String profileLinkColor;
 	private int following;
 	private int favouritesCount;
 	private boolean isProtected;
 	private String profileTextColor;
 	private boolean verified;
 	private boolean contributorsEnabled;
 	private String description;
 	private String name;
 	private String profileSidebarBorderColor;
 	private String profileBackgroundColor;
 	private Date createdAt;
 	private boolean defaultProfileImage;
 	private int followersCount;
 	private boolean geoEnabled;
 	private String profileBackgroundImageUrl;
 	private String profileBackgroundImageUrlHttps;
 	private boolean followRequestSent;
 	private Url url;
 	private TimeZone timeZone;
 	private long notifications;
 	private boolean profileUseBackgroundImage;
 	private int friendsCount;
 	private String profileSideBarFillColor;
 	private String screenName;
 	private String profileImageUrl;
 	private boolean showAllInlineMedia;
 	private boolean isTranslator;
 	private int listedCount;
 	
 	public long getTwitterId() {
 		return twitterId;
 	}
 
 	public void setTwitterId(long twitterId) {
 		this.twitterId = twitterId;
 	}
 
 	public Place getPlace() {
 		return place;
 	}
 
 	public void setPlace(Place place) {
 		this.place = place;
 	}
 
 	public boolean isDefaultProfile() {
 		return defaultProfile;
 	}
 
 	public void setDefaultProfile(boolean defaultProfile) {
 		this.defaultProfile = defaultProfile;
 	}
 
 	public int getStatusesCount() {
 		return statusesCount;
 	}
 
 	public void setStatusesCount(int statusesCount) {
 		this.statusesCount = statusesCount;
 	}
 
 	public long getProfileBackgroundTile() {
 		return profileBackgroundTile;
 	}
 
 	public void setProfileBackgroundTile(long profileBackgroundTile) {
 		this.profileBackgroundTile = profileBackgroundTile;
 	}
 
 	public Language getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(Language language) {
 		this.language = language;
 	}
 
 	public String getProfileLinkColor() {
 		return profileLinkColor;
 	}
 
 	public void setProfileLinkColor(String profileLinkColor) {
 		this.profileLinkColor = profileLinkColor;
 	}
 
 	public int getFollowing() {
 		return following;
 	}
 
 	public void setFollowing(int following) {
 		this.following = following;
 	}
 
 	public int getFavouritesCount() {
 		return favouritesCount;
 	}
 
 	public void setFavouritesCount(int favouritesCount) {
 		this.favouritesCount = favouritesCount;
 	}
 
 	public boolean isProtected() {
 		return isProtected;
 	}
 
 	public void setProtected(boolean isProtected) {
 		this.isProtected = isProtected;
 	}
 
 	public String getProfileTextColor() {
 		return profileTextColor;
 	}
 
 	public void setProfileTextColor(String profileTextColor) {
 		this.profileTextColor = profileTextColor;
 	}
 
 	public boolean isVerified() {
 		return verified;
 	}
 
 	public void setVerified(boolean verified) {
 		this.verified = verified;
 	}
 
 	public boolean isContributorsEnabled() {
 		return contributorsEnabled;
 	}
 
 	public void setContributorsEnabled(boolean contributorsEnabled) {
 		this.contributorsEnabled = contributorsEnabled;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getProfileSidebarBorderColor() {
 		return profileSidebarBorderColor;
 	}
 
 	public void setProfileSidebarBorderColor(String profileSidebarBorderColor) {
 		this.profileSidebarBorderColor = profileSidebarBorderColor;
 	}
 
 	public String getProfileBackgroundColor() {
 		return profileBackgroundColor;
 	}
 
 	public void setProfileBackgroundColor(String profileBackgroundColor) {
 		this.profileBackgroundColor = profileBackgroundColor;
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	public boolean isDefaultProfileImage() {
 		return defaultProfileImage;
 	}
 
 	public void setDefaultProfileImage(boolean defaultProfileImage) {
 		this.defaultProfileImage = defaultProfileImage;
 	}
 
 	public int getFollowersCount() {
 		return followersCount;
 	}
 
 	public void setFollowersCount(int followersCount) {
 		this.followersCount = followersCount;
 	}
 
 	public boolean isGeoEnabled() {
 		return geoEnabled;
 	}
 
 	public void setGeoEnabled(boolean geoEnabled) {
 		this.geoEnabled = geoEnabled;
 	}
 
 	public String getProfileBackgroundImageUrl() {
 		return profileBackgroundImageUrl;
 	}
 
 	public void setProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
 		this.profileBackgroundImageUrl = profileBackgroundImageUrl;
 	}
 
 	public String getProfileBackgroundImageUrlHttps() {
 		return profileBackgroundImageUrlHttps;
 	}
 
 	public void setProfileBackgroundImageUrlHttps(
 			String profileBackgroundImageUrlHttps) {
 		this.profileBackgroundImageUrlHttps = profileBackgroundImageUrlHttps;
 	}
 
 	public boolean isFollowRequestSent() {
 		return followRequestSent;
 	}
 
 	public void setFollowRequestSent(boolean followRequestSent) {
 		this.followRequestSent = followRequestSent;
 	}
 
 	public Url getUrl() {
 		return url;
 	}
 
 	public void setUrl(Url url) {
 		this.url = url;
 	}
 
 	public TimeZone getTimeZone() {
 		return timeZone;
 	}
 
 	public void setTimeZone(TimeZone timeZone) {
 		this.timeZone = timeZone;
 	}
 
 	public long getNotifications() {
 		return notifications;
 	}
 
 	public void setNotifications(long notifications) {
 		this.notifications = notifications;
 	}
 
 	public boolean isProfileUseBackgroundImage() {
 		return profileUseBackgroundImage;
 	}
 
 	public void setProfileUseBackgroundImage(boolean profileUseBackgroundImage) {
 		this.profileUseBackgroundImage = profileUseBackgroundImage;
 	}
 
 	public int getFriendsCount() {
 		return friendsCount;
 	}
 
 	public void setFriendsCount(int friendsCount) {
 		this.friendsCount = friendsCount;
 	}
 
 	public String getProfileSideBarFillColor() {
 		return profileSideBarFillColor;
 	}
 
 	public void setProfileSideBarFillColor(String profileSideBarFillColor) {
 		this.profileSideBarFillColor = profileSideBarFillColor;
 	}
 
 	public String getScreenName() {
 		return screenName;
 	}
 
 	public void setScreenName(String screenName) {
 		this.screenName = screenName;
 	}
 
 	public String getProfileImageUrl() {
 		return profileImageUrl;
 	}
 
 	public void setProfileImageUrl(String profileImageUrl) {
 		this.profileImageUrl = profileImageUrl;
 	}
 
 	public boolean isShowAllInlineMedia() {
 		return showAllInlineMedia;
 	}
 
 	public void setShowAllInlineMedia(boolean showAllInlineMedia) {
 		this.showAllInlineMedia = showAllInlineMedia;
 	}
 
 	public boolean isTranslator() {
 		return isTranslator;
 	}
 
 	public void setTranslator(boolean isTranslator) {
 		this.isTranslator = isTranslator;
 	}
 
 	public int getListedCount() {
 		return listedCount;
 	}
 
 	public void setListedCount(int listedCount) {
 		this.listedCount = listedCount;
 	}
	
 }

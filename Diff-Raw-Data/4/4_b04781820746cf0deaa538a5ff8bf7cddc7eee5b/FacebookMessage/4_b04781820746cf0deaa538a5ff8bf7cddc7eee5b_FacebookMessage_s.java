 /**
  * 
  */
 package net.mysocio.data.messages.facebook;
 
 import net.mysocio.data.messages.UserMessage;
 import net.mysocio.ui.data.objects.facebook.FacebookUiMessage;
 
 import com.github.jmkgreen.morphia.annotations.Entity;
 
 /**
  * @author Aladdin
  *
  */
 @Entity("messages")
 public class FacebookMessage extends UserMessage {
 	private String fbId = "";
 	private String message = "";
 	private String picture = "";
 	private String link = "";
 	private String name = "";
 	private String caption = "";
 	private String description= "";
 	private String source = "";
 	private String properties = "";
 	private String icon = "";
 	private String privacy = "";
 	private String type = "";
 	private String likes = "";
 	private String place = "";
 	private String story = "";
 	private String application = "";
 	private String updated_time = "";
 	private String linkToMessage = "";
 	private String uiObjectName = FacebookUiMessage.NAME;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5110172305702339723L;
 	
 	@Override
 	public String replacePlaceholders(String template) {
 		String message = super.replacePlaceholders(template);
 		message = message.replace("message.outer.link", getLinkToMessage());
 		message = message.replace("message.name", getName());
 		message = message.replace("message.link", getLink());
 		String picture = getPicture();
 		if (getType().equals("video")){
 			message = message.replace("message.picture", picture);
 		}else{
 			//for some reason pictures shown in FB and ones in api messages are not the same
 			message = message.replace("message.picture", picture.replace("_s.", "_n."));
 		}
 		message = message.replace("message.caption", getCaption());
 		message = message.replace("message.description", getDescription());
 		return message;
 	}
 
 	@Override
 	public String getNetworkIcon() {
 		return "images/networksIcons/fb.png";
 	}
 
 	@Override
 	public String getReadenNetworkIcon() {
 		return "images/networksIcons/fb-gray.png";
 	}
 
 	@Override
 	public String getLink() {
 		return link;
 	}
 
 	@Override
 	public String getUiCategory() {
 		return FacebookUiMessage.CATEGORY;
 	}
 
 	@Override
 	public String getUiName() {
 		return uiObjectName;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @param message the message to set
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * @return the picture
 	 */
 	public String getPicture() {
 		return picture;
 	}
 
 	/**
 	 * @param picture the picture to set
 	 */
 	public void setPicture(String picture) {
 		this.picture = picture;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the caption
 	 */
 	public String getCaption() {
 		return caption;
 	}
 
 	/**
 	 * @param caption the caption to set
 	 */
 	public void setCaption(String caption) {
 		this.caption = caption;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the source
 	 */
 	public String getSource() {
 		return source;
 	}
 
 	/**
 	 * @param source the source to set
 	 */
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	/**
 	 * @return the properties
 	 */
 	public String getProperties() {
 		return properties;
 	}
 
 	/**
 	 * @param properties the properties to set
 	 */
 	public void setProperties(String properties) {
 		this.properties = properties;
 	}
 
 	/**
 	 * @return the icon
 	 */
 	public String getIcon() {
 		return icon;
 	}
 
 	/**
 	 * @param icon the icon to set
 	 */
 	public void setIcon(String icon) {
 		this.icon = icon;
 	}
 
 	/**
 	 * @return the privacy
 	 */
 	public String getPrivacy() {
 		return privacy;
 	}
 
 	/**
 	 * @param privacy the privacy to set
 	 */
 	public void setPrivacy(String privacy) {
 		this.privacy = privacy;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the likes
 	 */
 	public String getLikes() {
 		return likes;
 	}
 
 	/**
 	 * @param likes the likes to set
 	 */
 	public void setLikes(String likes) {
 		this.likes = likes;
 	}
 
 	/**
 	 * @return the place
 	 */
 	public String getPlace() {
 		return place;
 	}
 
 	/**
 	 * @param place the place to set
 	 */
 	public void setPlace(String place) {
 		this.place = place;
 	}
 
 	/**
 	 * @return the story
 	 */
 	public String getStory() {
 		return story;
 	}
 
 	/**
 	 * @param story the story to set
 	 */
 	public void setStory(String story) {
 		this.story = story;
 	}
 
 	/**
 	 * @return the application
 	 */
 	public String getApplication() {
 		return application;
 	}
 
 	/**
 	 * @param application the application to set
 	 */
 	public void setApplication(String application) {
 		this.application = application;
 	}
 
 	/**
 	 * @return the updated_time
 	 */
 	public String getUpdated_time() {
 		return updated_time;
 	}
 
 	/**
 	 * @param updated_time the updated_time to set
 	 */
 	public void setUpdated_time(String updated_time) {
 		this.updated_time = updated_time;
 	}
 
 	/**
 	 * @param link the link to set
 	 */
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	/**
 	 * @return the uiObjectName
 	 */
 	public String getUiObjectName() {
 		return uiObjectName;
 	}
 
 	/**
 	 * @param uiObjectName the uiObjectName to set
 	 */
 	public void setUiObjectName(String uiObjectName) {
 		this.uiObjectName = uiObjectName;
 	}
 
 	/**
 	 * @return the linkToMessage
 	 */
 	public String getLinkToMessage() {
 		return linkToMessage;
 	}
 
 	/**
 	 * @param linkToMessage the linkToMessage to set
 	 */
 	public void setLinkToMessage(String linkToMessage) {
 		this.linkToMessage = linkToMessage;
 	}
 
 	@Override
 	public Object getUniqueFieldValue() {
 		return fbId;
 	}
 
 	@Override
 	public String getUniqueFieldName() {
 		return "fbId";
 	}
 
 	public String getFbId() {
 		return fbId;
 	}
 
 	public void setFbId(String fbId) {
 		this.fbId = fbId;
 	}
 }

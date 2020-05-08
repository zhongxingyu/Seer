 /**
  * 
  */
 package com.github.yuyang226.j500px.users;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.github.yuyang226.j500px.J500pxConstants;
 import com.github.yuyang226.j500px.J500pxException;
 import com.github.yuyang226.j500px.http.Parameter;
 import com.github.yuyang226.j500px.http.Response;
 import com.github.yuyang226.j500px.http.Transport;
 import com.github.yuyang226.j500px.oauth.OAuthUtils;
 
 /**
  * @author yayu
  *
  */
 public class UsersInterface {
 	private String apiKey;
     private String sharedSecret;
     private Transport transportAPI;
     
     private static final ThreadLocal<DateFormat> SHORT_DATE_FORMATS = new ThreadLocal<DateFormat>() {
         protected synchronized DateFormat initialValue() {
             return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
         }
     };
     
     /**
 	 * 
 	 */
 	public UsersInterface(String apiKey, String sharedSecret,
             Transport transportAPI) {
 		super();
 		this.apiKey = apiKey;
 		this.sharedSecret = sharedSecret;
 		this.transportAPI = transportAPI;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws J500pxException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public User getUserProfile() throws J500pxException, IOException, JSONException {
         List<Parameter> parameters = new ArrayList<Parameter>();
         parameters.add(new Parameter(J500pxConstants.PARAM_OAUTH_CONSUMER_KEY,
                 apiKey));
         OAuthUtils.addOAuthToken(parameters);
         Response response = transportAPI.getJSON(sharedSecret, J500pxConstants.PATH_USERS, parameters);
         if (response.isError()) {
             throw new J500pxException(response.getErrorMessage());
         }
 
         JSONObject userElement = response.getData().getJSONObject("user");
 		return parseUserObject(userElement);
 	}
 	
 	/**
 	 * 
 	 * @param userElement
 	 * @return
 	 * @throws JSONException 
 	 */
 	public static User parseUserObject(JSONObject userElement) throws JSONException {
 		User user = new User();
 		//short format
 		user.setId(userElement.getInt("id"));
 		user.setUserName(userElement.getString("username"));
 		user.setFirstName(userElement.optString("firstname", null));
 		user.setLastName(userElement.optString("lastname", null));
 		user.setCity(userElement.optString("city", null));
 		user.setUpgradeStatus(userElement.optInt("upgrade_status", 0));
 		
 		//full format
 		user.setSex(UserSex.valueOf(userElement.optInt("sex", 0)));
 		user.setState(userElement.optString("state", null));
 		user.setAbout(userElement.optString("about", null));
 		user.setDomain(userElement.optString("domain", null));
 		user.setLocale(userElement.optString("locale", "en"));
 		user.setShowNude(userElement.optBoolean("show_nude", Boolean.FALSE));
 		user.setFullName(userElement.optString("fullname", null));
 		user.setUserPicUrl(userElement.optString("userpic_url", null));
 		user.setEmail(userElement.optString("email", null));
 		user.setPhotosCount(userElement.optInt("photos_count", -1));
 		user.setAffection(userElement.optInt("affection", -1));
 		user.setInFavoritesCount(userElement.optInt("in_favorites_count", -1));
 		user.setFriendsCount(userElement.optInt("friends_count", -1));
 		user.setFollowersCount(userElement.optInt("followers_count", -1));
 		user.setFotomotoOn(userElement.optBoolean("fotomoto_on", false));
 		
 		if (userElement.has("auth")) {
 			JSONObject authElement = userElement.getJSONObject("auth");
 			user.setAuthedSites(new AuthedSites(
 					"1".equals(authElement.optString("facebook", "")), 
 					"1".equals(authElement.optString("twitter", ""))));
 		}
 		
 		if (userElement.has("upgrade_status_expiry")) {
 			try {
 				user.setUpgradeStatusExpiry(
 						SHORT_DATE_FORMATS.get().parse(userElement.getString("upgrade_status_expiry")));
 			} catch (ParseException e) {
 				//ignore
 			}
 		}
 		
 		if (userElement.has("birthday")) {
 			String birthday = userElement.getString("birthday");
 			if (birthday != null && !birthday.equals("null")) {
 				try {
 					user.setBirthday(
 							SHORT_DATE_FORMATS.get().parse(userElement.getString("birthday")));
 				} catch (ParseException e) {
 					//ignore
 				}
 			}
 		}
 		
 		if (userElement.has("registration_date")) {
 			try {
 				String regDate = userElement.getString("registration_date");
 				user.setRegistrationDate(
 						J500pxConstants.W3C_DATE_FORMATS.get().parse(regDate));
 			} catch (ParseException e) {
 				//ignore
 			}
 		}
 		
 		user.setUploadLimit(userElement.optInt("upload_limit", -1));
 		
 		if (userElement.has("upload_limit_expiry")) {
 			try {
 				String uploadExpiry = userElement.getString("upload_limit_expiry");
 				user.setUpgradeStatusExpiry(
 						J500pxConstants.W3C_DATE_FORMATS.get().parse(uploadExpiry));
 			} catch (ParseException e) {
 				//ignore
 			}
 		}
 		
 		if (userElement.has("contacts")) {
 			JSONObject contactsObj = userElement.getJSONObject("contacts");
			JSONArray contactsArray = contactsObj.names();
			for (int i = 0; i < contactsArray.length(); i++) {
				String name = contactsArray.getString(i);
 				user.getContacts().add(new UserContact(name, contactsObj.getString(name)));
 			}
 		}
 		
 		if (userElement.has("equipment")) {
 			JSONObject equipObj = userElement.getJSONObject("equipment");
 			if (equipObj.has("camera")) {
 				JSONArray cameraObj = equipObj.getJSONArray("camera");
 				for (int i = 0; i < cameraObj.length(); i++) {
 					user.getEquipments().add(new Camera(cameraObj.getString(i)));
 				}
 			}
 			if (equipObj.has("lens")) {
 				JSONArray lensObj = equipObj.getJSONArray("lens");
 				for (int i = 0; i < lensObj.length(); i++) {
 					user.getEquipments().add(new Lens(lensObj.getString(i)));
 				}
 			}
 			if (equipObj.has("misc")) {
 				JSONArray miscObj = equipObj.getJSONArray("misc");
 				for (int i = 0; i < miscObj.length(); i++) {
 					user.getEquipments().add(new Equipment(miscObj.getString(i)));
 				}
 			}
 		}
 		
 		return user;
 	}
 
 }

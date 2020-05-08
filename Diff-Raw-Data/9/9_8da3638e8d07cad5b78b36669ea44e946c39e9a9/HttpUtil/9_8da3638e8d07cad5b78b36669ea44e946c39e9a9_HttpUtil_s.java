 package com.coffeeandpower.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.LayeredSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SocketFactory;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.util.Log;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.RootActivity;
 import com.coffeeandpower.activity.ActivityEnterInviteCode;
 import com.coffeeandpower.activity.ActivityJobCategory;
 import com.coffeeandpower.activity.ActivitySettings;
 import com.coffeeandpower.activity.ActivityUserDetails;
 import com.coffeeandpower.activity.ActivityWallet;
 import com.coffeeandpower.cont.ChatMessage;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.Education;
 import com.coffeeandpower.cont.Listing;
 import com.coffeeandpower.cont.Review;
 import com.coffeeandpower.cont.Transaction;
 import com.coffeeandpower.cont.User;
 import com.coffeeandpower.cont.UserResume;
 import com.coffeeandpower.cont.UserShort;
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.cont.UserTransaction;
 import com.coffeeandpower.cont.Venue;
 import com.coffeeandpower.cont.VenueChatEntry;
 import com.coffeeandpower.cont.VenueSmart;
 import com.coffeeandpower.cont.VenueSmart.CheckinData;
 import com.coffeeandpower.cont.Work;
 import com.coffeeandpower.inter.OAuthService;
 import com.google.android.maps.GeoPoint;
 
 public class HttpUtil {
 
 	private AbstractHttpClient client;
 
 	public HttpUtil() {
 		this.client = getThreadSafeClient();
 	}
 
 	/**
 	 * Get Resume for user woth userId
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder getUserResume(int userIdForUrl) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "getResume"));
 			params.add(new BasicNameValuePair("user_id", URLEncoder.encode(userIdForUrl + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getResumeForUserId: " + responseString);
 			AppCAP.logInFile(responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					JSONObject payload = json.optJSONObject("payload");
 					if (payload != null) {
 
 						UserResume userResume = new UserResume();
 
 						ArrayList<Review> reviews = new ArrayList<Review>();
 						ArrayList<Education> education = new ArrayList<Education>();
 						ArrayList<Work> work = new ArrayList<Work>();
 						ArrayList<Listing> agentList = new ArrayList<Listing>();
 						ArrayList<Listing> clientList = new ArrayList<Listing>();
 						ArrayList<Venue> checkinhistoryArray = new ArrayList<Venue>();
 						ArrayList<Venue> favoritePlacesArray = new ArrayList<Venue>();
 
 						// ************* PARSE JSON
 						// ***********************
 						// ************************************************
 						userResume.setNickName(payload.optString("nickname"));
 						userResume.setMajorJob(payload.optString("major_job_category"));
 						userResume.setMinorJob(payload.optString("minor_job_category"));
 						userResume.setStatusText(payload.optString("status_text"));
 						userResume.setJoinSponsor(payload.optString("sponsorNickname"));
 
 						// Location
 						JSONObject objLocation = payload.optJSONObject("location");
 						if (objLocation != null) {
 							userResume.setLocationLat(objLocation.optDouble("lat"));
 							userResume.setLocationLng(objLocation.optDouble("lng"));
 						}
 
 						userResume.setUrlPhoto(payload.optString("urlPhoto"));
 						userResume.setUrlThumbnail(payload.optString("urlThumbnail"));
 						userResume.setJoined(payload.optString("joined"));
 						userResume.setJoinedBrief(payload.optString("joined_brief"));
 						userResume.setJoinDate(payload.optString("join_date"));
 						userResume.setEnteredInviteCode(payload.optString("entered_invite_code"));
 						userResume.setBio(payload.optString("bio"));
 
 						// Stats
 						JSONObject objStats = payload.optJSONObject("stats");
 						if (objStats != null) {
 							userResume.setTotalEarned(objStats.optInt("totalEarned"));
 							userResume.setTotalTipsEarned(objStats.optInt("totalTipsEarned"));
 							userResume.setTotalHours(objStats.optInt("totalHours"));
 							userResume.setTotalMissionCountAsRecipient(objStats.optInt("totalMissionCountAsRecipient"));
 							userResume.setDistinctTipPayers(objStats.optInt("distinctTipPayers"));
 							userResume.setTotalSpent(objStats.optInt("totalSpent"));
 							userResume.setTotalTipsSpent(objStats.optInt("totalTipsSpent"));
 							userResume.setTotalMissionCountAsPayer(objStats.optInt("totalMissionCountAsPayer"));
 							userResume.setDistinctTipRecipients(objStats.optInt("distinctTipRecipients"));
 							userResume.setTotalEarnedFromMe(objStats.optDouble("totalEarnedFromMe"));
 							userResume.setTotalMissionsFromMe(objStats.optInt("totalMissionsFromMe"));
 							userResume.setTotalMissionsAsAgent(objStats.optInt("totalMissionsAsAgent"));
 							userResume.setTotalMissionsAsClient(objStats.optInt("totalMissionsAsClient"));
 							userResume.setTotalMissions(objStats.optInt("totalMissions"));
 							userResume.setTotalFunded(objStats.optString("totalFunded"));
 						}
 
 						userResume.setSkillSet(payload.optString("skillSet"));
 						userResume.setHourlyBillingRate(payload.optString("hourly_billing_rate"));
 
 						// Verified
 						JSONObject objVerified = payload.optJSONObject("verified");
 						if (objVerified != null) {
 
 							JSONObject objLinkedIn = objVerified.optJSONObject("linkedin");
 							if (objLinkedIn != null) {
 								userResume.setVerifiedLinkedIn(objLinkedIn.optString("verified"));
 								userResume.setVerifiedLinkedInProfileLink(objLinkedIn.optString("profileLink"));
 							}
 
 							JSONObject objFacebook = objVerified.optJSONObject("facebook");
 							if (objFacebook != null) {
 								userResume.setVerifiedFacebook(objFacebook.optString("verified"));
 								userResume.setVerifiedFacebookProfileLink(objFacebook.optString("profileLink"));
 							}
 
 							JSONObject objMobile = objVerified.optJSONObject("mobile");
 							if (objMobile != null) {
 								userResume.setVerifiedMobile(objMobile.optString("verified"));
 							}
 						}
 
 						userResume.setContactsOnlyChat(payload.optString("contacts_only_chat"));
 						userResume.setUserIsContact(payload.optBoolean("user_is_contact"));
 						userResume.setLinkedInPublicProfileUrl(payload.optString("linkedin_public_profile_url"));
 						userResume.setTrusted(payload.optString("trusted"));
 						userResume.setSmartererName(payload.optString("smarterer_name"));
 						userResume.setJobTitle(payload.optString("job_title"));
 
 						// Get Work data
 						JSONArray arrayWork = payload.optJSONArray("work");
 						if (arrayWork != null) {
 							for (int x = 0; x < arrayWork.length(); x++) {
 
 								JSONObject objWork = arrayWork.optJSONObject(x);
 								if (objWork != null) {
 									work.add(new Work(objWork.optString("title"), objWork.optString("company"),
 											objWork.optString("startDate"), objWork.optString("endDate")));
 								}
 							}
 						}
 						userResume.setWork(work);
 
 						// Get Education data
 						JSONArray arrayEdu = payload.optJSONArray("education");
 						if (arrayEdu != null) {
 							for (int x = 0; x < arrayEdu.length(); x++) {
 
 								JSONObject objEdu = arrayEdu.optJSONObject(x);
 								if (objEdu != null) {
 									education.add(new Education(objEdu.optString("school"), objEdu
 											.optInt("startDate"), objEdu.optInt("endDate"), objEdu
 											.optString("concentrations"), objEdu.optString("degree")));
 								}
 							}
 						}
 						userResume.setEducation(education);
 
 						userResume.setUserHasEducation(payload.optString("userHasEducation"));
 
 						// Checkin Data
 						JSONObject objCheckInData = payload.optJSONObject("checkin_data");
 						if (objCheckInData != null) {
 							userResume.setCheckInData_id(objCheckInData.optInt("id"));
 							userResume.setCheckInData_userId(objCheckInData.optInt("userid"));
 							userResume.setCheckInData_lat(objCheckInData.optDouble("lat"));
 							userResume.setCheckInData_lng(objCheckInData.optDouble("lng"));
 							userResume.setCheckInData_Date(objCheckInData.optString("checkin_date"));
 							userResume.setCheckInData_checkIn(objCheckInData.optString("checkin"));
 							userResume.setCheckInData_checkOutDate(objCheckInData.optString("checkout_date"));
 							userResume.setCheckInData_checkOut(objCheckInData.optString("checkout"));
 							userResume.setCheckInData_checkedIn(objCheckInData.optString("checked_in"));
 							userResume.setCheckInData_venueId(objCheckInData.optString("venue_id"));
 							userResume.setCheckInData_foursquareId(objCheckInData.optString("foursquare_id"));
 							userResume.setCheckInData_Name(objCheckInData.optString("name"));
 							userResume.setCheckInData_Address(objCheckInData.optString("address"));
 							userResume.setCheckInData_city(objCheckInData.optString("city"));
 							userResume.setCheckInData_state(objCheckInData.optString("state"));
 							userResume.setCheckInData_zip(objCheckInData.optString("zip"));
 							userResume.setCheckInData_phone(objCheckInData.optString("phone"));
 							userResume.setCheckInData_icon(objCheckInData.optString("icon"));
 							userResume.setCheckInData_visible(objCheckInData.optString("visible"));
 							userResume.setCheckInData_photoUrl(objCheckInData.optString("photo_url"));
 							userResume.setCheckInData_formattedPhone(objCheckInData.optString("formatted_phone"));
 							userResume.setCheckInData_usersHere(objCheckInData.optInt("users_here"));
 							userResume.setCheckInData_usersIncludingMe(objCheckInData.optInt("users_including_me"));
 							userResume.setCheckInData_availableForHours(objCheckInData.optInt("available_for_hours"));
 							userResume.setCheckInData_availableForMinutes(objCheckInData.optInt("available_for_minutes"));
 						}
 
 						// Checkin history
 						JSONArray arrayCheckIn = payload.optJSONArray("checkin_history");
 						if (arrayCheckIn != null) {
 							for (int x = 0; x < arrayCheckIn.length(); x++) {
 
 								if (x < 3) {
 									JSONObject objFromArray = arrayCheckIn.optJSONObject(x);
 									if (objFromArray != null) {
 
 										Venue venue = new Venue();
 										venue.setCheckinsCount(objFromArray.optInt("count"));
 										venue.setFoursquareId(objFromArray.optString("foursquare_id"));
 										venue.setVenueId(objFromArray.optInt("venue_id"));
 										venue.setName(objFromArray.optString("name"));
 										venue.setAddress(objFromArray.optString("address"));
 										venue.setCity(objFromArray.optString("city"));
 										venue.setState(objFromArray.optString("state"));
 										venue.setPostalCode(objFromArray.optString("zip"));
 										venue.setPhone(objFromArray.optString("phone"));
 										venue.setIcon(objFromArray.optString("icon"));
 										venue.setPhotoUrl(objFromArray.optString("photo_url"));
 										venue.setCheckinTime(objFromArray.optString("checkin_time"));
 										checkinhistoryArray.add(venue);
 									}
 								}
 							}
 						}
 						userResume.setCheckinhistoryArray(checkinhistoryArray);
 
 						userResume.setUserHasFavoritePlaces(payload.optString("hasFavoritePlaces"));
 
 						// Favorite places
 						JSONArray arrayFavoritePlaces = payload.optJSONArray("favorite_places");
 						if (arrayFavoritePlaces != null) {
 							for (int x = 0; x < arrayFavoritePlaces.length(); x++) {
 
 								JSONObject objFromArray = arrayFavoritePlaces.optJSONObject(x);
 								if (objFromArray != null) {
 
 									Venue venue = new Venue();
 									venue.setCheckinsCount(objFromArray.optInt("count"));
 									venue.setFoursquareId(objFromArray.optString("foursquare_id"));
 									venue.setVenueId(objFromArray.optInt("venue_id"));
 									venue.setName(objFromArray.optString("name"));
 									venue.setAddress(objFromArray.optString("address"));
 									venue.setCity(objFromArray.optString("city"));
 									venue.setState(objFromArray.optString("state"));
 									venue.setPostalCode(objFromArray.optString("zip"));
 									venue.setPhone(objFromArray.optString("phone"));
 									venue.setIcon(objFromArray.optString("icon"));
 									venue.setPhotoUrl(objFromArray.optString("photo_url"));
 									venue.setCheckinTime(objFromArray.optString("checkin_time"));
 									favoritePlacesArray.add(venue);
 								}
 							}
 						}
 						userResume.setFavoritePlaces(favoritePlacesArray);
 
 						// Get listings as agent
 						JSONArray arrayAgent = payload.optJSONArray("listingsAsAgent");
 						if (arrayAgent != null) {
 							for (int x = 0; x < arrayAgent.length(); x++) {
 
 								JSONObject obj = arrayAgent.optJSONObject(x);
 								if (obj != null) {
 									agentList.add(new Listing(obj.optString("days_past"), obj
 											.optString("listing"), obj.optInt("author_id"), obj
 											.optString("price"), obj.optInt("client_id"), obj
 											.optString("client_nickname")));
 								}
 							}
 						}
 						userResume.setAgentListings(agentList);
 
 						// Get listings as client
 						JSONArray arrayClient = payload.optJSONArray("listingsAsClient");
 						if (arrayClient != null) {
 							for (int x = 0; x < arrayClient.length(); x++) {
 
 								JSONObject obj = arrayClient.optJSONObject(x);
 								if (obj != null) {
 									clientList.add(new Listing(obj.optString("days_past"), obj
 											.optString("listing"), obj.optInt("author_id"), obj
 											.optString("price"), obj.optInt("client_id"), obj
 											.optString("client_nickname")));
 								}
 							}
 						}
 						userResume.setClienListings(clientList);
 
 						// Reviews
 						JSONObject objReview = payload.optJSONObject("reviews");
 						if (objReview != null) {
 							userResume.setReviewsPage(objReview.optString("page"));
 							userResume.setReviewsLoveReceived(objReview.optString("love_received"));
 							userResume.setReviewsTotal(objReview.optInt("total"));
 							userResume.setReviewsRecords(objReview.optString("records"));
 
 							JSONArray reviewsArray = objReview.optJSONArray("rows");
 							if (reviewsArray != null) {
 								for (int x = 0; x < reviewsArray.length(); x++) {
 
 									JSONObject objReviewFromArray = reviewsArray.optJSONObject(x);
 									if (objReviewFromArray != null) {
 										reviews.add(new Review(objReviewFromArray.optInt("id"),
 												objReviewFromArray.optString("author"),
 												objReviewFromArray.optString("title"),
 												objReviewFromArray.optString("type"),
 												objReviewFromArray.optString("create_time"),
 												objReviewFromArray.optString("skill"),
 												objReviewFromArray.optString("rating"),
 												objReviewFromArray.optString("is_love"),
 												objReviewFromArray.optString("tip_amount"),
 												objReviewFromArray.optString("review"),
 												objReviewFromArray.optString("ratingImage"),
 												objReviewFromArray.optString("relativeTime")));
 									}
 								}
 							}
 						}
 						userResume.setReviews(reviews);
 
 						result.setHandlerCode(Executor.HANDLE_GET_USER_RESUME);
 						result.setObject(userResume);
 					}
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get user data with userID
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder getCheckInDataWithUserId(int userId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "getUserCheckInData"));
 			params.add(new BasicNameValuePair("user_id", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getCheckInDataWithUserId: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get Bitmap (profile photos) from URL
 	 * 
 	 * @param url
 	 * @return
 	 */
 	public static DataHolder getBitmapFromURL(String url) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		URL myFileUrl = null;
 		Bitmap bmImg = null;
 
 		try {
 			myFileUrl = new URL(url);
 		} catch (MalformedURLException e) {
 			RootActivity.log("HttpUtil_getBitmapFromURL url:" + url);
 			e.printStackTrace();
 		}
 
 		try {
 			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
 			conn.setDoInput(true);
 			conn.connect();
 			InputStream is = conn.getInputStream();
 
 			bmImg = BitmapFactory.decodeStream(is);
 
 			result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 			result.setObject(bmImg);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Upload user profile image
 	 * 
 	 * @return
 	 */
 	public DataHolder uploadUserProfilePhoto() {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
 		File file = new File(Environment.getExternalStorageDirectory() + ActivitySettings.IMAGE_FOLDER, "photo_profile.jpg");
 
 		try {
 			multipartEntity.addPart("action", new StringBody("setUserProfileData"));
 			multipartEntity.addPart("profile", new FileBody(file));
 
 			post.setEntity(multipartEntity);
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_uploadUserProfilePhoto: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 					result.setHandlerCode(Executor.HANDLE_UPLOAD_USER_PROFILE_PHOTO);
 					result.setObject(json.opt("message"));
 
 					JSONObject params = json.optJSONObject("params");
 					if (params != null) {
 						AppCAP.setLocalUserPhotoURL(params.optString("thumbnail"));
 						AppCAP.setLocalUserPhotoLargeURL(params.optString("picture"));
 					}
 
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send One on One chat message
 	 * 
 	 * @param userId
 	 * @param message
 	 * @return
 	 */
 	public DataHolder sendOneOnOneChatMessage(int userId, String message) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "oneOnOneChatFromMobile"));
 			params.add(new BasicNameValuePair("message", message + ""));
 			params.add(new BasicNameValuePair("toUserId", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendOneOnOneChatMessage: " + responseString);
 
 			if (responseString != null) {
 
 				if (responseString.equals("0")) {
 					result.setHandlerCode(Executor.HANDLE_SEND_CHAT_MESSAGE);
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send love review
 	 * 
 	 * @param user
 	 * @param review
 	 * @return
 	 */
 	public DataHolder sendReview(UserResume user, String review) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + "reviews.php");
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "makeMobileReview"));
 			params.add(new BasicNameValuePair("recipientID", URLEncoder.encode(user.getCheckInData_userId() + "", "utf-8")));
 			params.add(new BasicNameValuePair("reviewText", review + ""));
 			if (user.getLocationLat() != 0 && user.getLocationLng() != 0) {
 				params.add(new BasicNameValuePair("lat", URLEncoder.encode(user.getLocationLat() + "", "utf-8")));
 				params.add(new BasicNameValuePair("lng", URLEncoder.encode(user.getLocationLng() + "", "utf-8")));
 			}
 			if (ActivityUserDetails.isUserHereNow(user)) {
 				params.add(new BasicNameValuePair("foursquare", URLEncoder.encode(user.getCheckInData_foursquareId() + "", "utf-8")));
 			}
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendReview: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					boolean res = json.optBoolean("succeeded");
 					result.setObject(res);
 
 					String message = json.optString("message");
 					result.setResponseMessage(message);
 					result.setHandlerCode(Executor.HANDLE_SENDING_PROP);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get chat history with user
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder getOneOnOneChatHistory(int userId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "getOneOnOneChatHistory"));
 			params.add(new BasicNameValuePair("other_user", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getOneOnOneChatHistory: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					if (json.optInt("error") == 0) {
 
 						ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
 						JSONArray arrayChat = json.optJSONArray("chat");
 						if (arrayChat != null) {
 
 							for (int x = 0; x < arrayChat.length(); x++) {
 
 								JSONObject objMess = arrayChat.optJSONObject(x);
 								if (objMess != null) {
 									messages.add(new ChatMessage(objMess.optInt("id"), objMess.optInt("user_id"),
 											objMess.optString("entry_text"), objMess
 													.optString("nickname"), objMess
 													.optString("date"), objMess
 													.optString("photo_url"), objMess
 													.optInt("receiving_user_id"), objMess
 													.optInt("offer_id")));
 								}
 							}
 						}
 						result.setObject(messages);
 						result.setHandlerCode(Executor.HANDLE_ONE_ON_ONE_CHAT_HISTORY);
 						return result;
 					} else {
 						result.setHandlerCode(AppCAP.HTTP_ERROR);
 						result.setResponseMessage("Error loading chat...");
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Change user profile data
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public DataHolder setUserProfileData(User user, boolean isEmailChanged) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "setUserProfileData"));
 			params.add(new BasicNameValuePair("nickname", user.getNickName() + ""));
 			if (isEmailChanged) {
 				params.add(new BasicNameValuePair("email", URLEncoder.encode(user.getUserName() + "", "utf-8")));
 			}
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_setUserProfileData: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					boolean res = json.optBoolean("succeeded");
 					result.setObject(res);
 
 					String message = json.optString("message");
 					result.setResponseMessage(message);
 					result.setHandlerCode(Executor.HANDLE_SET_USER_PROFILE_DATA);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Set Notification Settings
 	 * 
 	 * @param distance
 	 * @param checkedInOnly
 	 * @return
 	 */
 	public DataHolder setNotificationSettings(String distance, boolean checkedInOnly) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "setNotificationSettings"));
 			params.add(new BasicNameValuePair("push_distance", URLEncoder.encode(distance + "", "utf-8")));
 			params.add(new BasicNameValuePair("checked_in_only", checkedInOnly ? "1" : "0"));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_setNotificationSettings: " + responseString);
 
 			if (responseString != null) {
 
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get users checked in around me
 	 * 
 	 * @param venue
 	 * @return
 	 */
 	/*
 	 * public DataHolder getCheckedInBoundsOverTime (MapView mapView){
 	 * 
 	 * DataHolder result = new DataHolder(AppCAP.HTTP_ERROR,
 	 * "Internet connection error", null);
 	 * 
 	 * client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
 	 * HttpVersion.HTTP_1_1);
 	 * 
 	 * GeoPoint pointCenterMap = mapView.getMapCenter(); int lngSpan =
 	 * mapView.getLongitudeSpan(); int latSpan = mapView.getLatitudeSpan();
 	 * 
 	 * GeoPoint sw = new GeoPoint(pointCenterMap.getLatitudeE6() -
 	 * latSpan/2, pointCenterMap.getLongitudeE6() - lngSpan/2); GeoPoint ne
 	 * = new GeoPoint(pointCenterMap.getLatitudeE6() + latSpan/2,
 	 * pointCenterMap.getLongitudeE6() + lngSpan/2);
 	 * 
 	 * float numberOfDays = 7.0f;
 	 * 
 	 * HttpGet get = new HttpGet(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API +
 	 * "?action=getCheckedInBoundsOverTime" + "&sw_lat=" +
 	 * (sw.getLatitudeE6() / 1E6) + "&sw_lng=" + (sw.getLongitudeE6() / 1E6)
 	 * + "&ne_lat=" + (ne.getLatitudeE6() / 1E6) + "&ne_lng=" +
 	 * (ne.getLongitudeE6() / 1E6) + "&checked_in_since=" +
 	 * (System.currentTimeMillis() /1000 - (86400 * numberOfDays)) +
 	 * "&group_users=1" + "&version=0.1");
 	 * 
 	 * try {
 	 * 
 	 * // Execute HTTP Get Request HttpResponse response =
 	 * client.execute(get); HttpEntity resEntity = response.getEntity();
 	 * 
 	 * String responseString = EntityUtils.toString(resEntity);
 	 * RootActivity.log("HttpUtil_getCheckedInBoundsOverTime: "
 	 * +responseString);
 	 * 
 	 * if (responseString!=null){
 	 * 
 	 * JSONObject json = new JSONObject(responseString); if (json!=null){
 	 * 
 	 * boolean res = json.optBoolean("error"); if (!res){
 	 * 
 	 * JSONArray payload = json.optJSONArray("payload"); if (payload!=null){
 	 * 
 	 * ArrayList<UserSmart> mapUsersArray = new ArrayList<UserSmart>();
 	 * 
 	 * for (int m=0; m<payload.length(); m++){
 	 * 
 	 * JSONObject item = payload.optJSONObject(m); if (item!=null){
 	 * 
 	 * int checkInId = item.optInt("checkin_id"); int userId=
 	 * item.optInt("id"); String nickName = item.optString("nickname");
 	 * String statusText = item.optString("status_text"); String photo =
 	 * item.optString("photo"); // ??? String majorJobCategory =
 	 * item.optString("major_job_category"); String minorJobCategory =
 	 * item.optString("minor_job_category"); String headLine =
 	 * item.optString("headline"); String fileName =
 	 * item.optString("filename"); double lat = item.optDouble("lat");
 	 * double lng = item.optDouble("lng"); int checkedIn =
 	 * item.optInt("checked_in"); String foursquareId =
 	 * item.optString("foursquare"); String venueName =
 	 * item.optString("venue_name"); int checkInCount =
 	 * item.optInt("checkin_count"); String skills =
 	 * item.optString("skills"); boolean met = item.optBoolean("met");
 	 * 
 	 * mapUsersArray.add(new UserSmart(checkInId, userId, nickName,
 	 * statusText, photo, majorJobCategory, minorJobCategory, headLine,
 	 * fileName, lat, lng, checkedIn, foursquareId, venueName, checkInCount,
 	 * skills, met)); } }
 	 * 
 	 * result.setResponseCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 	 * result.setObject(mapUsersArray); }
 	 * 
 	 * } else { // we have unknown error
 	 * result.setResponseMessage("Unknown error"); }
 	 * 
 	 * return result; } }
 	 * 
 	 * } catch (UnsupportedEncodingException e) { e.printStackTrace();
 	 * return result;
 	 * 
 	 * } catch (ClientProtocolException e) { e.printStackTrace(); return
 	 * result;
 	 * 
 	 * } catch (IOException e) { e.printStackTrace(); return result;
 	 * 
 	 * } catch (JSONException e) { e.printStackTrace(); return result; }
 	 * return result; }
 	 */
 
 	/**
 	 * Get Venues And Users With Checkins In Bounds During Interval
 	 * 
 	 * @param data
 	 * @return Object[] [0]venues [1]users
 	 */
 	public DataHolder getVenuesAndUsersWithCheckinsInBoundsDuringInterval(double data[], int numberOfDays) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpGet get = new HttpGet(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API + "?action=getVenuesAndUsersWithCheckinsInBoundsDuringInterval"
 				+ "&sw_lat=" + data[0] + "&sw_lng=" + data[1] + "&ne_lat=" + data[2] + "&ne_lng=" + data[3] + "&checked_in_since="
 				+ (System.currentTimeMillis() / 1000 - (86400 * numberOfDays)) + "&user_lat=" + data[4] + "&user_lng=" + data[5]
 				+ "&version=0.1");
 
 		try {
 
 			// Execute HTTP Get Request
 			HttpResponse response = client.execute(get);
 			HttpEntity resEntity = response.getEntity();
 			if (Constants.debugLog)
 				Log.d("LOG", "URI: " + get.getURI());
 
 			String responseString = EntityUtils.toString(resEntity);
 			AppCAP.logInFile(responseString);
 			RootActivity.log("HttpUtil_getVenuesAndUsersWithCheckinsInBoundsDuringInterval: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					JSONObject objPayload = json.optJSONObject("payload");
 					if (objPayload != null) {
 
 						// Array Venues
 						ArrayList<VenueSmart> venues = new ArrayList<VenueSmart>();
 						JSONArray arrayVenues = objPayload.optJSONArray("venues");
 						if (arrayVenues != null) {
 							for (int x = 0; x < arrayVenues.length(); x++) {
 
 								JSONObject objVenue = arrayVenues.optJSONObject(x);
 								if (objVenue != null) {
 
 									ArrayList<CheckinData> arrayCheckins = new ArrayList<VenueSmart.CheckinData>();
 									JSONObject objUsersFromVenue = objVenue.optJSONObject("users");
 									if (objUsersFromVenue != null) {
 
 										JSONArray userIds = objUsersFromVenue.names();
 										if (userIds != null) {
 
 											for (int y = 0; y < userIds.length(); y++) {
 
 												JSONObject o = objUsersFromVenue
 														.optJSONObject(userIds.getString(y));
 												if (o != null) {
 													int userId = 0;
 													try {
 														userId = Integer.parseInt(userIds
 																.getString(y));
 													} catch (NumberFormatException e) {
 													}
 
 													arrayCheckins.add(new CheckinData(userId, o
 															.optInt("checkin_count"), o
 															.optInt("checked_in")));
 												}
 											}
 										}
 									}
 
 									venues.add(new VenueSmart(objVenue.optInt("venue_id"), objVenue
 											.optString("name"), objVenue.optString("address"), objVenue
 											.optString("city"), objVenue.optString("state"), objVenue
 											.optDouble("distance"), objVenue.optString("foursquare_id"),
 											objVenue.optInt("checkins"), objVenue
 													.optInt("checkins_for_week"), objVenue
 													.optInt("checkins_for_interval"), objVenue
 													.optString("photo_url"), objVenue
 													.optString("phone"), objVenue
 													.optString("formatted_phone"), objVenue
 													.optDouble("lat"), objVenue.optDouble("lng"),
 											arrayCheckins));
 								}
 							}
 						}
 
 						// Array Users
 						ArrayList<UserSmart> users = new ArrayList<UserSmart>();
 						JSONArray arrayUsers = objPayload.optJSONArray("users");
 						if (arrayUsers != null) {
 
 							boolean isFirstInList1 = false;
 							boolean isFirstInList0 = false;
 
 							for (int x = 0; x < arrayUsers.length(); x++) {
 								JSONObject objUser = arrayUsers.optJSONObject(x);
 								if (objUser != null) {
 
 									UserSmart singleUserMap = new UserSmart(
 											objUser.optInt("checkin_id"),
 											objUser.optInt("id"),
 											objUser.optString("nickname"),
 											objUser.optString("status_text"),
 											objUser.optString("photo"),
 											objUser.optString("major_job_category"),
 											objUser.optString("minor_job_category"),
 											// smarterer_name
 											// !?!?!?!??!
 											objUser.optString("headline"), objUser.optString("filename"),
 											objUser.optDouble("lat"), objUser.optDouble("lng"),
 											objUser.optInt("checked_in"),
 											objUser.optString("foursquare"),
 											objUser.optString("venue_name"),
 											objUser.optInt("checkin_count"), objUser.optString("skills"),
 											objUser.optBoolean("met"));
 
 									if (singleUserMap.getCheckedIn() == 1) {
 										if (!isFirstInList1) {
 											singleUserMap.setFirstInList(true);
 											isFirstInList1 = !isFirstInList1;
 										}
 									} else {
 										if (!isFirstInList0) {
 											singleUserMap.setFirstInList(true);
 											isFirstInList0 = !isFirstInList0;
 										}
 									}
 
 									users.add(singleUserMap);
 								}
 							}
 						}
 
 						result.setObject(new Object[] { venues, users });
 					}
 					result.setHandlerCode(Executor.HANDLE_GET_VENUES_AND_USERS_IN_BOUNDS);
 					result.setResponseMessage("HTTP 200 OK");
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get users checked in venue
 	 * 
 	 * @param venue
 	 * @return
 	 */
 	public DataHolder getUsersCheckedInAtFoursquareID(String foursquareId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "getUsersCheckedIn"));
 			params.add(new BasicNameValuePair("foursquare", URLEncoder.encode(foursquareId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getUsersCheckedIn: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					ArrayList<UserShort> usersArray = new ArrayList<UserShort>();
 
 					JSONObject objPayload = json.optJSONObject("payload");
 					if (objPayload != null) {
 
 						int count = objPayload.optInt("count");
 						JSONArray arrayUsers = objPayload.optJSONArray("users");
 						if (arrayUsers != null) {
 
 							for (int i = 0; i < arrayUsers.length(); i++) {
 
 								JSONObject obj = arrayUsers.optJSONObject(i);
 								if (obj != null) {
 
 									int id = obj.optInt("id");
 									String nickName = obj.optString("nickname");
 									String statusText = obj.optString("status_text");
 									String about = obj.optString("about");
 									String joinDate = obj.optString("join_date");
 									String imageURL = obj.optString("imageUrl");
 									String hourlyBilingRate = obj.optString("hourly_biling_rate");
 
 									usersArray.add(new UserShort(id, nickName, statusText, about, joinDate,
 											imageURL, hourlyBilingRate));
 								}
 							}
 						}
 					}
 					result.setHandlerCode(Executor.HANDLE_GET_CHECHED_USERS_IN_FOURSQUARE);
 					result.setObject(usersArray);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send contact request to userId
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder sendContactRequestToUserId(int userId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "sendContactRequest"));
 			params.add(new BasicNameValuePair("acceptor_id", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendContactRequestToUserId: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send accept contact request from userId
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder sendAcceptContactRequestFromUserId(int userId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "acceptContactRequest"));
 			params.add(new BasicNameValuePair("initiator_id", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendAcceptContactRequestFromUserId: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send F2F invite
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder sendFriendRequest(int userId) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "f2fInvite"));
 			params.add(new BasicNameValuePair("greeted_id", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendF2FInvite: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 					if (json.optString("error") != null) {
 						result.setResponseMessage(json.optString("message"));
 						result.setResponseCode(json.optInt("error"));
 						result.setHandlerCode(Executor.HANDLE_SEND_FRIEND_REQUEST);
 						return result;
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Add place at Foursquare
 	 * 
 	 * @param name
 	 * @param coords
 	 * @return
 	 */
 	public DataHolder addPlace(String name, double[] coords) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost("https://api.foursquare.com/v2/venues/add");
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("name", name));
 			params.add(new BasicNameValuePair("ll", coords[4] + "," + coords[5]));
 			params.add(new BasicNameValuePair("oauth_token", AppCAP.FOURSQUARE_OAUTH));
 			params.add(new BasicNameValuePair("v", "20120208"));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_addPlace: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 					JSONObject meta = json.optJSONObject("meta");
 					if (meta != null) {
 						if (meta.optString("code") != null && meta.optString("code").equals("200")) {
 
 							JSONObject responseObj = json.optJSONObject("response");
 							if (responseObj != null) {
 								JSONObject venueObj = responseObj.optJSONObject("venue");
 								if (venueObj != null) {
 
 									JSONObject locationObj = venueObj.optJSONObject("location");
 									if (locationObj != null) {
 
 										int checkinsCount = 0;
 										int usersCount = 0;
 										int tipCount = 0;
 										int hereNowCount = 0;
 
 										JSONObject statsObj = venueObj.optJSONObject("stats");
 										if (statsObj != null) {
 
 											checkinsCount = statsObj.optInt("checkinsCount");
 											usersCount = statsObj.optInt("usersCount");
 											tipCount = statsObj.optInt("tipCount");
 										}
 
 										JSONObject hereNowObj = venueObj.optJSONObject("hereNow");
 										if (hereNowObj != null) {
 
 											hereNowCount = hereNowObj.optInt("count");
 
 										}
 										result.setHandlerCode(Executor.HANDLE_ADD_PLACE);
 										result.setObject(new Venue("", venueObj.optInt("id"), venueObj
 												.optString("name"), locationObj.optString("address"),
 												locationObj.optString("crossStreet"), locationObj
 														.optDouble("lat"), locationObj
 														.optDouble("lng"), locationObj
 														.optInt("distance"), locationObj
 														.optString("postalCode"), locationObj
 														.optString("city"), locationObj
 														.optString("state"), locationObj
 														.optString("country"), "", "", "",
 												checkinsCount, usersCount, tipCount, hereNowCount,
 												"", "", "", ""));
 
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send F2F accept
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder sendF2FAccept(int userId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "f2fAccept"));
 			params.add(new BasicNameValuePair("greeter_id", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendF2FAccept: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send F2F decline
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public DataHolder sendF2FDecline(int userId) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "f2fDecline"));
 			params.add(new BasicNameValuePair("greeter_id", URLEncoder.encode(userId + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendF2FDecline: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send F2F verify
 	 * 
 	 * @param userId
 	 * @param password
 	 * @return
 	 */
 	public DataHolder sendF2FVerify(int userId, String password) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "f2fVerify"));
 			params.add(new BasicNameValuePair("greeter_id", URLEncoder.encode(userId + "", "utf-8")));
 			params.add(new BasicNameValuePair("password", URLEncoder.encode(password + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_sendF2FVerify: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get contactw list
 	 * 
 	 * @return
 	 */
 	public DataHolder getContactsList() {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "getContactList"));
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getContactList: " + responseString);
 
 			if (responseString != null) {
 				JSONObject json = new JSONObject(responseString);
 				JSONArray contacts = json.optJSONArray("payload");
 				//FIXME
 				//Need to read in error field and respond appropriately
 				ArrayList<UserSmart> contactsArray = new ArrayList<UserSmart>();
 				if (contacts != null) {
 					for (int m = 0; m < contacts.length(); m++) {
 
 						JSONObject currContact = contacts.optJSONObject(m);
 						if (currContact != null) {
 							contactsArray.add(new UserSmart(currContact));				
 	
 						}
 					}
 					result.setObject(contactsArray);
 					result.setResponseMessage("HTTP 200 OK");
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get invitation code for specified location
 	 * 
 	 * @param lat
 	 * @param lng
 	 * @return
 	 */
 	public DataHolder getInvitationCodeForLocation(double lat, double lng) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "getInvitationCode"));
 			params.add(new BasicNameValuePair("lat", URLEncoder.encode(lat + "", "utf-8")));
 			params.add(new BasicNameValuePair("lng", URLEncoder.encode(lng + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getInvitationCodeForLocation: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					JSONObject payload = json.optJSONObject("payload");
 					if(payload != null)
 					{
 						String code = payload.optString("code");
 						if(code != null)
 						{
 							result.setObject(code);
 							result.setHandlerCode(Executor.HANDLE_GET_INVITATION_CODE);
 						}
 					}
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Enter Invitation code
 	 * 
 	 * @param invitationCode
 	 * @param lat
 	 * @param lng
 	 * @return
 	 */
 	public DataHolder enterInvitationCode(String invitationCode, double lat, double lng) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "enterInvitationCode"));
 			params.add(new BasicNameValuePair("lat", URLEncoder.encode(lat + "", "utf-8")));
 			params.add(new BasicNameValuePair("lng", URLEncoder.encode(lng + "", "utf-8")));
 			params.add(new BasicNameValuePair("invite_code", URLEncoder.encode(invitationCode + "", "utf-8")));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_enterInvitationCode: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					if (json.optBoolean("error")) {
 						result.setHandlerCode(AppCAP.HTTP_ERROR);
 						result.setResponseMessage(json.optString("payload"));
 					} else {
 						//Parsing through JSON and finding entered_invite_code field might also make sense
 						AppCAP.setEnteredInviteCode();
 						result.setHandlerCode(Executor.HANDLE_ENTER_INVITATION_CODE);
 					}
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get or send venue chat
 	 * 
 	 * @param venueId
 	 * @param lastChatIDString
 	 * @return
 	 */
 	public DataHolder venueChatForVenueWithID(int venueId, String lastChatIDString, String message, boolean isSend) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", isSend ? "sendVenueChat" : "getVenueChat"));
 			params.add(new BasicNameValuePair("venue_id", Integer.toString(venueId)));
 			params.add(new BasicNameValuePair("last_id", lastChatIDString));
 			params.add(new BasicNameValuePair("message", message));
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_VenueChatForVenueWithID: " + responseString);
 
 			if (responseString != null) {
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					if (json.optBoolean("error")) {
 						result.setHandlerCode(AppCAP.HTTP_ERROR);
 						result.setResponseMessage(json.optString("payload"));
 					} else {
 						JSONObject payloadObj = json.optJSONObject("payload");
 						if (payloadObj != null) {
 							ArrayList<Object> holder = new ArrayList<Object>();
 
 							holder.add(payloadObj.optString("lastId"));
 							holder.add(payloadObj.optString("firstDate"));
 							holder.add(payloadObj.optString("lastDate"));
 
 							ArrayList<VenueChatEntry> chatEntries = new ArrayList<VenueChatEntry>();
 
 							JSONArray chatArray = payloadObj.optJSONArray("entries");
 							if (chatArray != null) {
 								for (int x = 0; x < chatArray.length(); x++) {
 									JSONObject entryObj = chatArray.optJSONObject(x);
 
 									String systemData_userId = "";
 									String systemData_author = "";
 									String systemData_fineName = "";
 
 									JSONObject systemDataObj = entryObj.optJSONObject("system_data");
 									if (systemDataObj != null) {
 										JSONObject systemData_UserObj = systemDataObj.optJSONObject("user");
 										if (systemData_UserObj != null) {
 											systemData_userId = systemData_UserObj.optString("user_id");
 											systemData_author = systemData_UserObj.optString("author");
 											systemData_fineName = systemData_UserObj
 													.optString("filename");
 										}
 									}
 
 									if (entryObj != null) {
 										chatEntries.add(new VenueChatEntry(entryObj.optString("id"), entryObj
 												.optString("user_id"), entryObj.optString("entry"),
 												entryObj.optString("author"), entryObj
 														.optString("filename"), entryObj
 														.optString("ip"), entryObj
 														.optString("date"), entryObj
 														.optString("system_type"),
 												systemData_userId, systemData_author,
 												systemData_fineName));
 									}
 								}
 							}
 							holder.add(chatEntries);
 
 							result.setObject(holder);
 							result.setHandlerCode(Executor.HANDLE_VENUE_CHAT);
 							return result;
 						}
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Send Venue Chat
 	 * 
 	 * @param venueId
 	 * @param lastChatIDString
 	 * @param message
 	 * @return
 	 */
 	/*
 	 * public DataHolder sendVenueChatForVenueWithID(String venueId, String
 	 * lastChatIDString, String message) { DataHolder result = new
 	 * DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 	 * client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
 	 * HttpVersion.HTTP_1_1); HttpPost post = new
 	 * HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 	 * 
 	 * List<NameValuePair> params = new ArrayList<NameValuePair>();
 	 * 
 	 * try { params.add(new BasicNameValuePair("action", "sendVenueChat"));
 	 * params.add(new BasicNameValuePair("venue_id", venueId));
 	 * params.add(new BasicNameValuePair("last_id", lastChatIDString));
 	 * params.add(new BasicNameValuePair("message", message));
 	 * post.setEntity(new UrlEncodedFormEntity(params));
 	 * 
 	 * // Execute HTTP Post Request HttpResponse response =
 	 * client.execute(post); HttpEntity resEntity = response.getEntity();
 	 * 
 	 * String responseString = EntityUtils.toString(resEntity);
 	 * RootActivity.log("HttpUtil_sendVenueChatForVenueWithID: " +
 	 * responseString);
 	 * 
 	 * if (responseString != null) {
 	 * 
 	 * JSONObject json = new JSONObject(responseString); if (json != null) {
 	 * 
 	 * result.setHandlerCode(Executor.HANDLE_SEND_VENUE_CHAT); return
 	 * result; } }
 	 * 
 	 * } catch (UnsupportedEncodingException e) { e.printStackTrace();
 	 * return result;
 	 * 
 	 * } catch (ClientProtocolException e) { e.printStackTrace(); return
 	 * result;
 	 * 
 	 * } catch (IOException e) { e.printStackTrace(); return result;
 	 * 
 	 * } catch (JSONException e) { e.printStackTrace(); return result; }
 	 * return result; }
 	 */
 	/**
 	 * Save User Job Category
 	 * 
 	 * @param majorJobCategory
 	 * @param minorJobCategory
 	 * @return
 	 */
 	public DataHolder saveUserJobCategory(String majorJobCategory, String minorJobCategory) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "updateJobCategories"));
 			params.add(new BasicNameValuePair("major_job_category", majorJobCategory + ""));
 			params.add(new BasicNameValuePair("minor_job_category", minorJobCategory + ""));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_saveUserMajorJobCategory: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					if (json.optBoolean("error")) {
 						result.setHandlerCode(AppCAP.HTTP_ERROR);
 						result.setResponseMessage(json.optString("payload"));
 					} else {
 						result.setHandlerCode(Executor.HANDLE_SAVE_USER_JOB_CATEGORY);
 						result.setResponseMessage(json.optString("payload"));
 					}
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Save user smarterer name
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public DataHolder saveUserSmartererName(String name) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "saveUserSmartererName"));
 			params.add(new BasicNameValuePair("name", name + ""));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_saveUserSmartererName: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED); // change
 											      // this
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Check out user from location
 	 * 
 	 * @return
 	 */
 	public DataHolder checkOut() {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "checkout"));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_checkOut: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					result.setHandlerCode(UserAndTabMenu.HANDLE_CHECK_OUT);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Check in user to location
 	 * 
 	 * @return
 	 */
 	public DataHolder checkIn(VenueSmart venue, int checkInTime, int checkOutTime, String statusText) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		try {
 			params.add(new BasicNameValuePair("action", "checkin"));
 			params.add(new BasicNameValuePair("lat", Double.toString(venue.getLat())));
 			params.add(new BasicNameValuePair("lng", Double.toString(venue.getLng())));
 			params.add(new BasicNameValuePair("venue_name", venue.getName() + ""));
 			params.add(new BasicNameValuePair("checkin", checkInTime + ""));
 			params.add(new BasicNameValuePair("checkout", checkOutTime + ""));
 			params.add(new BasicNameValuePair("foursquare", URLEncoder.encode(venue.getFoursquareId() + "", "utf-8")));
 			params.add(new BasicNameValuePair("address", venue.getAddress() + ""));
 			params.add(new BasicNameValuePair("city", venue.getCity() + ""));
 			params.add(new BasicNameValuePair("state", venue.getState() + ""));
 			params.add(new BasicNameValuePair("zip", venue.zip + ""));
 			params.add(new BasicNameValuePair("phone", ""));
 			params.add(new BasicNameValuePair("status", statusText + ""));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_checkIn: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					boolean res = json.optBoolean("error");
 					if (!res) {
 						result.setHandlerCode(Executor.HANDLE_CHECK_IN);
 						return result;
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Get venues in bounds, uses C&P API
 	 * 
 	 * @param gp
 	 * @param number
 	 * @return
 	 */
 	public DataHolder getVenuesInSWCoords(double data[]) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		String userLat = "";
 		String userLng = "";
 
 		if (data[4] != 0 && data[5] != 0) {
 			userLat = "" + data[4];
 			userLng = "" + data[5];
 		}
 
 		// HttpClient client = getThreadSafeClient();
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		try {
 			params.add(new BasicNameValuePair("action", "getVenuesInBounds"));
 			params.add(new BasicNameValuePair("sw_lat", data[0] + ""));
 			params.add(new BasicNameValuePair("sw_lng", data[1] + ""));
 			params.add(new BasicNameValuePair("ne_lat", data[2] + ""));
 			params.add(new BasicNameValuePair("ne_lng", data[3] + ""));
 			params.add(new BasicNameValuePair("lat", userLat));
 			params.add(new BasicNameValuePair("lng", userLng));
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Get Request
 			HttpResponse responseClient = client.execute(post);
 			HttpEntity resEntity = responseClient.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getVenuesInSWCoords " + responseString);
 
 			if (responseString != null) {
 				JSONObject json = new JSONObject(responseString);
 
 				JSONObject meta = json.optJSONObject("meta");
 				if (meta != null) {
 
 					int code = meta.optInt("code");
 					if (code == 200) {
 
 						JSONObject response = json.optJSONObject("response");
 						if (response != null) {
 
 							JSONArray venues = response.optJSONArray("venues");
 							if (venues != null) {
 
 								ArrayList<Venue> venuesArray = new ArrayList<Venue>();
 
 								for (int m = 0; m < venues.length(); m++) {
 									
 									JSONObject venue = venues.optJSONObject(m);
 									if (venue != null) {
 										venuesArray.add(new Venue(venue));
 									}
 								}
 
 								result.setHandlerCode(code);
 								result.setObject(venuesArray);
 								return result;
 							}
 						}
 
 					} else {
 						result.setHandlerCode(code);
 						return result;
 					}
 
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get venues near my location, uses Foursquare API
 	 * 
 	 * @param gp
 	 *                GeoPoint with my coordinates
 	 * @param number
 	 *                of displayed venues
 	 * @return
 	 */
 	public DataHolder getVenuesCloseToLocation(GeoPoint gp, int number) {
 		double latFromGp = gp.getLatitudeE6() / 1E6;
 		double lngFromGp = gp.getLongitudeE6() / 1E6;
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpGet get = new HttpGet(AppCAP.URL_FOURSQUARE + "&limit=" + number + "&ll=" + latFromGp + "," + lngFromGp);
 
 		try {
 			// Execute HTTP Get Request
 			HttpResponse responseClient = client.execute(get);
 			HttpEntity resEntity = responseClient.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getVenuesCloseToLocation " + responseString);
 
 			if (responseString != null) {
 				JSONObject json = new JSONObject(responseString);
 
 				JSONObject meta = json.optJSONObject("meta");
 				if (meta != null) {
 
 					int code = meta.optInt("code");
 					if (code == 200) {
 
 						JSONObject response = json.optJSONObject("response");
 						if (response != null) {
 
 							JSONArray venues = response.optJSONArray("venues");
 							if (venues != null) {
 
 								ArrayList<VenueSmart> venuesArray = new ArrayList<VenueSmart>();
 								for (int m = 0; m < venues.length(); m++) {
 
 									JSONObject venue = venues.optJSONObject(m);
 									if (venue != null) {
 										JSONObject location = venue.optJSONObject("location");
 										String fourSquareId = venue.optString("id");
 										String name = venue.optString("name");
 										venuesArray.add(new VenueSmart(fourSquareId, name, location));
 									}
 								}
 
 								result.setHandlerCode(Executor.HANDLE_VENUES_CLOSE_TO_LOCATION);
 								result.setObject(venuesArray);
 								result.setResponseMessage("HTTP 200 OK");
 								return result;
 							}
 						}
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			result.setResponseMessage("UnsupportedEncodingException Error: " + e);
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			result.setResponseMessage("ClientProtocolException Error: " + e);
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			result.setResponseMessage("IOException Error: " + e);
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 
 		}
 		
 		result.setResponseMessage("Unhandled Exception");
 		return result;
 	}
 
 	/**
 	 * Get Nearest Venues With Checkins To Coordinate
 	 * 
 	 * @param coords
 	 * @return
 	 */
 	public DataHolder getNearestVenuesWithCheckinsToCoordinate(double[] coords) {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		int testVar = AppCAP.getLoggedInUserId();
 		HttpGet get = new HttpGet(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API + "?action=getNearestVenuesAndUsersWithCheckinsDuringInterval"
 		+ "&lat=" + coords[0] + "&lng=" + coords[1] + "&user_id=" + AppCAP.getLoggedInUserId());
 
 		//Check to see what user Id we have (was not matching up with iOS)
 		//int currentUserId = AppCAP.getLoggedInUserId();
 		try {
 			// Execute HTTP Get Request
 			HttpResponse response = client.execute(get);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getNearestVenuesWithCheckinsToCoordinate: " + responseString);
 			//Check to see if we got a response from the network
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				//Check to see if you got a valid JSON string
 				if (json != null) {
 
 					JSONObject objPayload = json.optJSONObject("payload");
 					//Check to see if the JSON has a vaild payload
 					if (objPayload != null) {
 						result.setResponseMessage("JSON PARSE ERROR");
 						result.setObject(objPayload);
 
 						// Array Venues
 						ArrayList<VenueSmart> venues = new ArrayList<VenueSmart>();
 						JSONArray arrayVenues = objPayload.optJSONArray("venues");
 						if (arrayVenues != null) {
 							for (int x = 0; x < arrayVenues.length(); x++) {
 
 								JSONObject objVenue = arrayVenues.optJSONObject(x);
 								if (objVenue != null) {
 
 									ArrayList<CheckinData> arrayCheckins = new ArrayList<VenueSmart.CheckinData>();
 									JSONObject objUsersFromVenue = objVenue.optJSONObject("users");
 									if (objUsersFromVenue != null) {
 
 										JSONArray userIds = objUsersFromVenue.names();
 										if (userIds != null) {
 
 											for (int y = 0; y < userIds.length(); y++) {
 
 												JSONObject o = objUsersFromVenue
 														.optJSONObject(userIds.getString(y));
 												if (o != null) {
 													int userId = 0;
 													try {
 														userId = Integer.parseInt(userIds
 																.getString(y));
 													} catch (NumberFormatException e) {
 													}
 
 													arrayCheckins.add(new CheckinData(userId, o
 															.optInt("checkin_count"), o
 															.optInt("checked_in")));
 												}
 											}
 										}
 									}
 
 									venues.add(new VenueSmart(objVenue,arrayCheckins));
 								}
 							}
 						}
 
 						// Array Users
 						ArrayList<UserSmart> users = new ArrayList<UserSmart>();
 						JSONArray arrayUsers = objPayload.optJSONArray("users");
 						if (arrayUsers != null) {
 
 							boolean isFirstInList1 = false;
 							boolean isFirstInList0 = false;
 
 							for (int x = 0; x < arrayUsers.length(); x++) {
 								JSONObject objUser = arrayUsers.optJSONObject(x);
								if (objUser != null) {
 
 									UserSmart singleUserMap = new UserSmart(objUser);
 
 									if (singleUserMap.getCheckedIn() == 1) {
 										if (!isFirstInList1) {
 											singleUserMap.setFirstInList(true);
 											isFirstInList1 = !isFirstInList1;
 										}
 									} else {
 										if (!isFirstInList0) {
 											singleUserMap.setFirstInList(true);
 											isFirstInList0 = !isFirstInList0;
 										}
 									}
 									users.add(singleUserMap);
 								}
 							}
 						}
 						//Array Contacts
 						ArrayList<UserSmart> contacts = new ArrayList<UserSmart>();
 						JSONArray arrayContacts = objPayload.optJSONArray("contacts");
 						if (arrayContacts != null) {
 
 							boolean isFirstInList1 = false;
 							boolean isFirstInList0 = false;
 
 							for (int x = 0; x < arrayContacts.length(); x++) {
 								JSONObject objUser = arrayContacts.optJSONObject(x);
 								if (objUser != null) {
 
 									UserSmart singleUserMap = new UserSmart(0, objUser.optInt("other_user_id"), "", "", "", "",
 											"", "", "", 0.0, 0.0, 0, "",
 											"", 0, "", false);
 									contacts.add(singleUserMap);
 								}
 							}
 						}
 						result.setObject(new Object[] { venues, users, contacts });
 					}
 					result.setHandlerCode(Executor.HANDLE_GET_VENUES_AND_USERS_IN_BOUNDS);
 					result.setResponseMessage("HTTP 200 OK");
 					return result;	
 						
 				}
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get User transaction data
 	 * 
 	 * @return
 	 */
 	public DataHolder getUserTransactionData() {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("action", "getTransactionData"));
 
 		try {
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getUserTrasactionData: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					JSONObject objPayload = json.optJSONObject("payload");
 					if (objPayload != null) {
 						ArrayList<Transaction> transactions = new ArrayList<Transaction>();
 
 						// Implement Transactions here
 
 						result.setHandlerCode(Executor.HANDLE_GET_USER_TRANSACTION_DATA);
 						result.setObject(new UserTransaction(objPayload.optInt("userid"), objPayload.optString("nickname"),
 								objPayload.optString("username"), objPayload.optString("status_text"), objPayload
 										.optString("status"), objPayload.optString("active"), objPayload
 										.optString("photo"), objPayload.optString("photo_large"), objPayload
 										.optDouble("lat"), objPayload.optDouble("lng"), objPayload
 										.optInt("favorite_enabled"), objPayload.optInt("favorite_count"),
 								objPayload.optInt("my_favorite_count"), objPayload.optInt("money_received"),
 								objPayload.optInt("offers_paid"), objPayload.optInt("balance"), transactions));
 
 						return result;
 					}
 				}
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get user data for logged user
 	 * 
 	 * @return
 	 */
 	public DataHolder getUserData() {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("action", "getUserData"));
 
 		try {
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getUserData: " + responseString);
 
 			// Save cookies to share session with WebView
 			client.getCookieStore().getCookies();
 			List<Cookie> cookies = client.getCookieStore().getCookies();
 			String cookieString = "";
 			for (int i = 0; i < cookies.size(); i++) {
 				Cookie cookie = cookies.get(i);
 				cookieString += cookie.getName() + "=" + cookie.getValue();// +"; domain="+cookie.getDomain();
 			}
 			AppCAP.setCookieString(cookieString);
 			if (Constants.debugLog)
 				Log.d("LOG", "Cookie: " + AppCAP.getCookieString());
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					int userId = json.optInt("userid");
 					String nickName = json.optString("nickname");
 					String userName = json.optString("username");
 					String statusText = json.optString("status_text");
 					String status = json.optString("status");
 					String active = json.optString("active");
 					String photo = json.optString("photo");
 					String photoLarge = json.optString("photo_large");
 					double lat = json.optDouble("lat");
 					double lng = json.optDouble("lng");
 					int favoriteEnabled = json.optInt("favorite_enabled");
 					int favoriteCount = json.optInt("favorite_count");
 					int myFavoriteCount = json.optInt("my_favorite_count");
 					int moneyReceived = json.optInt("money_received");
 					int offersPaid = json.optInt("offers_paid");
 					int balance = json.getInt("balance");
 
 					AppCAP.setLocalUserPhotoLargeURL(photoLarge);
 					AppCAP.setLocalUserPhotoURL(photo);
 
 					result.setObject(new User(userId, favoriteEnabled, favoriteCount, myFavoriteCount, moneyReceived, offersPaid,
 							balance, nickName, userName, statusText, status, active, photo, photoLarge, lat, lng));
 					result.setHandlerCode(Executor.HANDLE_GET_USER_DATA);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get Notification Settings
 	 * 
 	 * @return
 	 */
 	public DataHolder getNotificationSettings() {
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("action", "getNotificationSettings"));
 
 		try {
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_getNotificationSettings: " + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				if (json != null) {
 
 					JSONObject objPayload = json.optJSONObject("payload");
 					if (objPayload != null) {
 
 						result.setObject(new Object[] { objPayload.optString("push_distance"),
 								objPayload.optString("checked_in_only") });
 					}
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 		}
 		return result;
 	}
 
 	/**
 	 * Sign up for a new account
 	 * 
 	 * @param userName
 	 * @param password
 	 * @param confPassword
 	 * @param nickName
 	 * @return
 	 */
 	public DataHolder signup(String userName, String password, String nickName) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		// HttpClient client = getThreadSafeClient();
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_SIGNUP);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		params.add(new BasicNameValuePair("action", "signup"));
 		params.add(new BasicNameValuePair("signupUsername", userName));
 		params.add(new BasicNameValuePair("signupPassword", password));
 		params.add(new BasicNameValuePair("signupConfirm", password));
 		params.add(new BasicNameValuePair("signupNickname", nickName));
 		params.add(new BasicNameValuePair("signupAcceptTerms", "1"));
 		params.add(new BasicNameValuePair("type", "json"));
 
 		try {
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_signup: " + EntityUtils.toString(post.getEntity()) + ":" + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				Boolean succeeded = json.optBoolean("succeeded");
 				String mess = json.optString("message");
 
 				result.setResponseMessage(mess);
 
 				if (succeeded) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 					return result;
 
 				} else {
 					result.setHandlerCode(AppCAP.ERROR_SUCCEEDED_SHOW_MESS);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Sign up for a new account using a 3rd party
 	 * 
 	 * @param userId
 	 * @param token
 	 * @return
 	 */
 	public DataHolder signupViaOAuthService(OAuthService service) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		// HttpClient client = getThreadSafeClient();
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_SIGNUP);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		String serviceName = service.getServiceNameSignUp();
 		params.add(new BasicNameValuePair("action", "signup"));
 		params.add(new BasicNameValuePair("oauth_token", service.getAccessToken()));
 		params.add(new BasicNameValuePair("oauth_secret", service.getAccessTokenSecret()));
 		params.add(new BasicNameValuePair(serviceName + "_connect", "1"));
 		params.add(new BasicNameValuePair(serviceName + "_id", service.getUserId()));
 		params.add(new BasicNameValuePair("signupUsername", service.getUserName()));
 		params.add(new BasicNameValuePair("signupNickname", service.getUserNickName()));
 		params.add(new BasicNameValuePair("signupPassword", service.getUserPassword()));
 		params.add(new BasicNameValuePair("signupConfirm", service.getUserPassword()));
 		params.add(new BasicNameValuePair("type", "json"));
 
 		try {
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_signup: " + EntityUtils.toString(post.getEntity()) + ":" + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				Boolean succeeded = json.optBoolean("succeeded");
 				String mess = json.optString("message");
 
 				result.setResponseMessage(mess);
 
 				if (succeeded) {
 
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 					return result;
 
 				} else {
 					result.setHandlerCode(AppCAP.ERROR_SUCCEEDED_SHOW_MESS);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Login
 	 * 
 	 * @param userName
 	 * @param password
 	 * @return
 	 */
 	public DataHolder login(String userName, String password) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		// HttpClient client = getThreadSafeClient();
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_LOGIN);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		params.add(new BasicNameValuePair("action", "login"));
 		params.add(new BasicNameValuePair("username", userName));
 		params.add(new BasicNameValuePair("password", password));
 		params.add(new BasicNameValuePair("type", "json"));
 
 		try {
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_login: " + EntityUtils.toString(post.getEntity()) + ":" + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				Boolean succeeded = json.optBoolean("succeeded");
 				String mess = json.optString("message");
 
 				result.setResponseMessage(mess);
 
 				if (succeeded) {
 					/*
 					 * JSONObject paramsObj =
 					 * json.optJSONObject("params"); if
 					 * (paramsObj!=null){
 					 * 
 					 * JSONObject userObj =
 					 * paramsObj.optJSONObject("user"); if
 					 * (userObj!=null){
 					 * 
 					 * int userId = userObj.optInt("id");
 					 * String nickName =
 					 * userObj.optString("nickname");
 					 * 
 					 * result.setObject(new User(userId,
 					 * nickName)); } }
 					 */
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 					return result;
 
 				} else {
 					result.setHandlerCode(AppCAP.ERROR_SUCCEEDED_SHOW_MESS);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 
 		}
 
 		return result;
 	}
 
 	public DataHolder loginViaOAuthService(OAuthService service) {
 
 		DataHolder result = new DataHolder(AppCAP.HTTP_ERROR, "Internet connection error", null);
 
 		// HttpClient client = getThreadSafeClient();
 		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
 		HttpPost post = new HttpPost(AppCAP.URL_WEB_SERVICE + AppCAP.URL_API);
 
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		String serviceName = service.getServiceNameLogin();
 		String serviceSignUp = service.getServiceNameSignUp();
 		params.add(new BasicNameValuePair("signupNickname", service.getUserNickName()));
 		params.add(new BasicNameValuePair("linkedin_id", service.getUserId()));
 		params.add(new BasicNameValuePair("linkedin_connect", "1"));
 		params.add(new BasicNameValuePair("signupUsername", service.getUserName()));
 		params.add(new BasicNameValuePair("oauth_token", service.getAccessToken()));
 		params.add(new BasicNameValuePair("oauth_secret", service.getAccessTokenSecret()));
 		params.add(new BasicNameValuePair("signupPassword", service.getUserPassword()));
 		params.add(new BasicNameValuePair("signupConfirm", service.getUserPassword()));
 		params.add(new BasicNameValuePair("action", "mobileSignup"));
 		
 		//params.add(new BasicNameValuePair("type", "json"));
 
 		try {
 
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			HttpResponse response = client.execute(post);
 			HttpEntity resEntity = response.getEntity();
 
 			String responseString = EntityUtils.toString(resEntity);
 			RootActivity.log("HttpUtil_login: " + EntityUtils.toString(post.getEntity()) + ":" + responseString);
 
 			if (responseString != null) {
 
 				JSONObject json = new JSONObject(responseString);
 				Boolean succeeded = json.optBoolean("succeeded");
 				String mess = json.optString("message");
 
 				result.setResponseMessage(mess);
 
 				if (succeeded) {
 					 JSONObject paramsObj =json.optJSONObject("params"); 
 					 if(paramsObj!=null){
 						 JSONObject paramsObj2 =paramsObj.optJSONObject("params"); 
 						 if(paramsObj2!=null){
 							 String enteredInviteCode = paramsObj2.optString("entered_invite_code");
 							 if(enteredInviteCode!=null)
 							 {
 								 if(enteredInviteCode.equalsIgnoreCase("Y"))
 								 {
 									 AppCAP.setEnteredInviteCode();
 								 }
 							 }
 						 }
 	
 					 }
 					result.setHandlerCode(AppCAP.HTTP_REQUEST_SUCCEEDED);
 					return result;
 
 				} else {
 					result.setHandlerCode(AppCAP.ERROR_SUCCEEDED_SHOW_MESS);
 					return result;
 				}
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return result;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			result.setResponseMessage("JSON Parsing Error: " + e);
 			return result;
 
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns the Http client that is safe to use with threads
 	 * 
 	 * @return
 	 */
 	private Object mLock = new Object();
 	private CookieStore mCookie = null;
 
 	private DefaultHttpClient getThreadSafeClient() {
 		DefaultHttpClient client = new DefaultHttpClient();
 		synchronized (mLock) {
 			if (mCookie == null) {
 				mCookie = client.getCookieStore();
 			} else {
 				client.setCookieStore(mCookie);
 			}
 		}
 		ClientConnectionManager mgr = client.getConnectionManager();
 		HttpParams params = client.getParams();
 		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
 
 		workAroundReverseDnsBugInHoneycombAndEarlier(client);
 		return client;
 
 	}
 
 	private void workAroundReverseDnsBugInHoneycombAndEarlier(org.apache.http.client.HttpClient client) {
 		// Android had a bug where HTTPS made reverse DNS lookups (fixed
 		// in Ice
 		// Cream Sandwich)
 		// http://code.google.com/p/android/issues/detail?id=13117
 		SocketFactory socketFactory = new LayeredSocketFactory() {
 			SSLSocketFactory delegate = SSLSocketFactory.getSocketFactory();
 
 			@Override
 			public Socket createSocket() throws IOException {
 				return delegate.createSocket();
 			}
 
 			@Override
 			public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params)
 					throws IOException {
 				return delegate.connectSocket(sock, host, port, localAddress, localPort, params);
 			}
 
 			@Override
 			public boolean isSecure(Socket sock) throws IllegalArgumentException {
 				return delegate.isSecure(sock);
 			}
 
 			@Override
 			public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
 				injectHostname(socket, host);
 				return delegate.createSocket(socket, host, port, autoClose);
 			}
 
 			private void injectHostname(Socket socket, String host) {
 				try {
 					Field field = InetAddress.class.getDeclaredField("hostName");
 					field.setAccessible(true);
 					field.set(socket.getInetAddress(), host);
 				} catch (Exception ignored) {
 				}
 			}
 		};
 
 		client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", socketFactory, 443));
 	}
 
 }

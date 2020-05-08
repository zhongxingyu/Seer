 package eu.fiveminutes.cipele46.api;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Base64;
 import android.util.Log;
 
 import com.android.volley.Request.Method;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonArrayRequest;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.Volley;
 
 import eu.fiveminutes.cipele46.model.Ad;
 import eu.fiveminutes.cipele46.model.AdStatus;
 import eu.fiveminutes.cipele46.model.AdType;
 import eu.fiveminutes.cipele46.model.Category;
 import eu.fiveminutes.cipele46.model.City;
 import eu.fiveminutes.cipele46.model.District;
 import eu.fiveminutes.cipele46.model.User;
 
 public class CipeleAPI {
 
 	public enum UserAdSection {
 		ACTIVE_ADS,
 		FAVORITE_ADS,
 		CLOSED_ADS
 	}
 
 	
 	private static CipeleAPI cipele;
 	private RequestQueue reqQueue;
 	
 	// Cached categories. Live during the app session.
 	private List<Category> categories;
 	
 	private String TAG = this.getClass().getSimpleName();
 	
 	private List<District> cachedListOfDistricts;
 	
 	public void init(Context c) {
 		reqQueue = Volley.newRequestQueue(c);
 	}
 	
 	public static CipeleAPI get() {
 		if (cipele == null) {
 			cipele = new CipeleAPI();
 		}
 		return cipele;
 	}
 	
 	
 	public void registerUser(String firstName, String lastName, String email, String phone, String password, final UserRegistrationListener url) {
 		
 		
 
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.e(TAG, "Registration failed", error);
 				url.onFailure(error);
 			}
 		};
 	
 		Listener<JSONObject> userListener = new Listener<JSONObject>() {
 
 			@Override
 			public void onResponse(JSONObject response) {
 				Log.d(TAG, "Registration succeded" + response);
 				url.onSuccess();
 				
 			}
 		};
 		
 		JSONObject userObj = new JSONObject();
 		JSONObject requestObj = new JSONObject();
 		
 		try {
 			userObj.put("first_name", firstName);
 			userObj.put("last_name", lastName);
 			userObj.put("email", email);
 			userObj.put("password", password);
 			userObj.put("password_confirmation", password);
 
 			requestObj.put("user", userObj);
 			
 		} catch (JSONException e) {
 			url.onFailure(e);
 			return;
 		}
 
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put("Content-Type", "application/json");
 		
 		RegistrationRequest jsonReq = new RegistrationRequest(Method.POST, "http://cipele46.org/users.json", requestObj, headers, userListener, errorListener);
 		
 		reqQueue.add(jsonReq);
 
 		
 	}
 
 	private static String basicAuthHeaderValue(String username, String password) {
 		String x = username + ":" + password;
 		try {
 			return "Basic " + Base64.encodeToString(x.getBytes("UTF-8"), Base64.DEFAULT);
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 	
 	public void loginUser(String email, String password, final UserLoginListener url) {
 
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				
 				url.onFailure(error);
 			}
 		};
 		
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put("Authorization", basicAuthHeaderValue(email, password));
 		
 		Listener<User> userListener = new Listener<User>() {
 
 			@Override
 			public void onResponse(User user) {
 				
 				url.onSuccess(user);
 			}
 		};
 		
 		reqQueue.add(new GsonRequest<User>("http://cipele46.org/users/show.json", User.class, headers, userListener, errorListener));
 	}
 	
 	
 	/**
 	 * 
 	 * @param type Ad type. Required.
 	 * @param categoryID If null, all categories implied.
 	 * @param districtID If null, all districts implied.
 	 * @param adsListener
 	 */
 	public void getAds(final AdType type, final Long categoryID, 
 			final Long districtID, final AdsListener adsListener) {
 		
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.e(TAG, "", error);
 				adsListener.onFailure(error);
 			}
 		};
 		
 		Listener<JSONArray> arrayListener = new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 				
 				try {
 					List<Ad> adList = parseAdList(response);
 					adsListener.onSuccess(adList);
 				} catch (JSONException e) {
 					Log.e(TAG, "", e);
 					adsListener.onFailure(e);
 				}
 			}
 		};
 		
 		reqQueue.add(
 				new JsonArrayRequest("http://cipele46.org/ads.json", arrayListener, errorListener));
 	}
 	
 	/**
 	 * 
 	 * @param user ad section, required.
 	 * @param adsListener
 	 */
 	public void getUserAds(UserAdSection userAdSection, final AdsListener adsListener) {
 		
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				adsListener.onFailure(error);
 			}
 		};
 		
 		Listener<JSONArray> arrayListener = new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 				
 				try {
 					List<Ad> adList = parseAdList(response);
 					adsListener.onSuccess(adList);
 				} catch (JSONException e) {
 					adsListener.onFailure(e);
 				}
 			}
 		};
 		
 		reqQueue.add(
 				new JsonArrayRequest("http://cipele46.org/ads.json", arrayListener, errorListener));
 	}
 	
 	private static List<Ad> parseAdList(JSONArray array) throws JSONException {
 		List<Ad> adList = new ArrayList<Ad>();
 		
 		JSONObject obj;
 		for (int i = 0; i < array.length(); i++) {
 			obj = array.getJSONObject(i);
 			adList.add(parseAd(obj));
 		}
 		
 		return adList;
 	}
 	
 	private static Ad parseAd(JSONObject obj) throws JSONException {
 		
 		Ad newAd = new Ad();
 		newAd.setId(obj.getLong("id"));
 		newAd.setTitle(obj.getString("title"));
 		newAd.setDescription(obj.getString("description"));
 		newAd.setEmail(obj.getString("email"));
 		newAd.setPhone(obj.getString("phone"));
 		//newAd.setImageURLString(obj.optString("imageUrl"));
 		newAd.setCityID(obj.getLong("city_id"));
 		newAd.setCategoryID(obj.getLong("category_id"));
 		newAd.setDistrictID(obj.optLong("district_id", -1));
 		
 		int statusNumber = obj.getInt("status");
 		AdStatus status = AdStatus.ACTIVE;
 		switch (statusNumber) {
 			case 1:
 				status = AdStatus.PENDING;
 				break;
 			case 2:
 				status = AdStatus.ACTIVE;
 				break;
 			case 3:
 				status = AdStatus.CLOSED;
 				break;
 			default:
 				throw new IllegalArgumentException("Invalid ad status for ad " + newAd.getTitle());
 		}
 		newAd.setStatus(status);
 		
 		int typeNumber = obj.getInt("ad_type");
 		AdType type = AdType.SUPPLY;
 		switch (typeNumber) {
 			case 1:
 				type = AdType.SUPPLY;
 				break;
 			case 2:
 				type = AdType.DEMAND;
 				break;
 			default:
 				throw new IllegalArgumentException("Invalid ad type for ad " + newAd.getTitle());
 		}
 		newAd.setType(type);
 		
 		return newAd;
 	}
 	
 	public String getDistrictNameForID(Long districtID) {
 		
 		if (districtID == -1) {
 			return "Sve županije";
 		}
 		
 		if (cachedListOfDistricts == null) {
 			return "err0";
 		}
 		
 		for (District cat : cachedListOfDistricts) {
 			if (cat.getId().longValue() == districtID.longValue()) {
 				return cat.getName();
 			}
 		}
 		
 		return "err1";
 	}
 	
 	public String getCategoryNameForID(Long categoryID) {
 		
 		if (categoryID == -1) {
 			return "Sve kategorije";
 		}
 		
 		if (categories == null) {
 			return "Kategorije nisu učitane";
 		}
 		
 		for (Category cat : categories) {
 			//if (cat.getId().longValue() == categoryID.longValue()) {
 				//return cat.getName();
 			//}
 		}
 		
 		return "ne znam";
 	}
 	
 	public void getCategories(final CategoriesListener categoriesListener) {
 		
 		if (categories != null) {
 			categoriesListener.onSuccess(categories);
 			return;
 		}
 		
 		String url = "http://cipele46.org/categories.json";
 		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 				Log.i(TAG, response.toString());
 				
 				List<Category> listofCategory = Collections.<Category>emptyList();
 				
 				for (int i = 0; i < response.length(); i++) {
 					
 					if (listofCategory.isEmpty()) {
 						listofCategory = new ArrayList<Category>();
 					}
 					
 					JSONObject jsonObject;
 					try {
 						
 						jsonObject = response.getJSONObject(i);
 						Category category = new Category();
 						//category.setId(jsonObject.getLong("id"));
 						category.setName(jsonObject.getString("name"));
 						listofCategory.add(category);
 						
 					} catch (JSONException e) {
 						categoriesListener.onFailure(e);
 					}
 				}
 				
 				listofCategory.add(0, new Category(-1L, "Sve kategorije"));
 				
 				categories = listofCategory;
 				
 				categoriesListener.onSuccess(listofCategory);
 				
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.i(TAG, error.getMessage());
 				categoriesListener.onFailure(error);
 				
 			}
 		});
 		
 		reqQueue.add(jsonArrayRequest);
 		
 	}
 	
 	public void getDistrictWithCities(final DistrictWithCitiesListener districtWithCitiesListener) {
 		
 		if (cachedListOfDistricts != null) {
 			districtWithCitiesListener.onSuccess(cachedListOfDistricts);
 			Log.d(TAG, "Using cachedListOfDistricts");
 			return;
 		}
 		
 		String url = "http://www.cipele46.org/regions.json";
 		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 				Log.i(TAG, response.toString());
 				
 				List<District> listOfDistricts = Collections.<District>emptyList();
 				
 				try {
 				
 					for (int i = 0; i < response.length(); i++) {
 
 						if (listOfDistricts.isEmpty()) {
 							listOfDistricts = new ArrayList<District>();
 						}
 
 						JSONObject jsonObject;
 
 						jsonObject = response.getJSONObject(i);
 						District district = new District();
 						district.setId(jsonObject.getLong("id"));
 						district.setName(jsonObject.getString("name"));
 						
 						JSONArray cities = jsonObject.getJSONArray("cities");
 						List<City> listOfCities = Collections.<City>emptyList();
 						
 						for (int j = 0; j < cities.length(); j++) {
 							
 							if (listOfCities.isEmpty()) {
 								listOfCities = new ArrayList<City>();
 							}
 							
 							JSONObject jsonCity = cities.getJSONObject(j);
 							City city = new City();
 							city.setId(jsonCity.getString("id"));
 							city.setName(jsonCity.getString("name"));
 							city.setDistrictId(jsonCity.getString("region_id"));
 							listOfCities.add(city);
 						}
 						
 						district.setCities(listOfCities);
 						listOfDistricts.add(district);
 
 					}
 					
					listOfDistricts.add(0, new District(-1L, "Sve upanije"));
 					cachedListOfDistricts = listOfDistricts;
 					
 					districtWithCitiesListener.onSuccess(listOfDistricts);
 				
 				} catch (JSONException e) {
 					districtWithCitiesListener.onFailure(e);
 				}
 				
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.i(TAG, error.getMessage());
 				districtWithCitiesListener.onFailure(error);
 				
 			}
 		});
 		
 		reqQueue.add(jsonArrayRequest);
 		
 	}
 
 }

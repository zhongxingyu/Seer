 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.solr.common.SolrDocumentList;
 
 import com.orange.common.solr.SolrClient;
 import com.orange.common.utils.StringUtil;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.HotKeyword;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.KeywordManager;
 import com.orange.groupbuy.manager.ProductManager;
 import com.orange.groupbuy.manager.UserManager;
 import com.orange.groupbuy.util.UrlUtil;
 
 public class SearchService extends CommonGroupBuyService {
 
 	String deviceId;
 	String appId;									// app id, mandantory
 	String city;									// optional, city name, mandantory
 	boolean todayOnly = false;						// optional, only return today's product
 	List<Integer> categoryList;						// optional, return specify category's product
 	int startOffset = 0;							// optional
 	int maxCount = 30;								// optional
 	String keyword;									// mandantory
 	
 	Double latitude;								// optional, only for record user search history
 	Double longitude;								// optional, only for record user search history
 	boolean hasLocation = false;					// internal usage
 	Double radius;									// optional
 	int reCountStatus = 0;                          // optional
 		
 	@Override
 	public String toString() {
 		return "SearchService [deviceId=" + deviceId + ", appId=" + appId
 				+ ", city=" + city + ", todayOnly=" + todayOnly
 				+ ", categoryList=" + categoryList + ", startOffset="
 				+ startOffset + ", maxCount=" + maxCount + ", keyword="
 				+ keyword + ", latitude=" + latitude + ", longitude="
 				+ longitude + ", hasLocation=" + hasLocation + ", radius="
 				+ radius + ", reCountStatus=" + reCountStatus + "]";
 	}
 
 	@Override
 	public void handleData() {
 		//String[] keywords = keyword.trim().split(" ");
 		if (deviceId != null){
 			if (hasLocation)
 				UserManager.addSearchKeyword(mongoClient, deviceId, keyword, longitude, latitude);
 			else
 				UserManager.addSearchKeyword(mongoClient, deviceId, keyword);
 		}
 		
 		KeywordManager.upsertKeyword(mongoClient, keyword);
 		
 		HotKeyword hotKeyword = KeywordManager.findHotKeyword(mongoClient, keyword);
 		List<Product> productList = null;
 		
 		String queryStr = "";
 		if (hotKeyword == null) {	
 			queryStr = keyword;
 		} else {
 			queryStr = hotKeyword.getQueryString();
 		}
 		if (!hasLocation) {
 			latitude = null;
 			longitude = null;
 			radius = null;
 		} 
 		
 		SolrDocumentList resultList = ProductManager.getResultListBySolr(SolrClient.getInstance(), mongoClient, city, categoryList, 
 				todayOnly, queryStr, null, latitude, longitude, radius, startOffset, maxCount);
 		
 		if (reCountStatus == 0) {
 			productList = ProductManager.getResultList(resultList, mongoClient);
 			resultData = CommonServiceUtils.productListToJSONArray(productList);	
 		} else {
 			long resultCnt = ProductManager.getResultCnt(resultList);
 			productList = ProductManager.getResultList(resultList, mongoClient);
 			JSONArray productArray = CommonServiceUtils.productListToJSONArray(productList);
			JSONObject object = new JSONObject();			
 			safePut(object, "list", productArray);
 			safePut(object, "count", resultCnt);
 			resultData = object;
 		}		
 			
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		city = request.getParameter(ServiceConstant.PARA_CITY);
 		keyword = request.getParameter(ServiceConstant.PARA_KEYWORD);
 		if (!check(keyword, ErrorCode.ERROR_PARAMETER_KEYWORD_EMPTY,
 				ErrorCode.ERROR_PARAMETER_KEYWORD_NULL)) {
 			return false;
 		}
 		deviceId = request.getParameter(ServiceConstant.PARA_DEVICEID);
 		
 		String latitudeStr = request.getParameter(ServiceConstant.PARA_LATITUDE);
 		String longitudeStr = request.getParameter(ServiceConstant.PARA_LONGITUDE);
 		String radiusStr = request.getParameter(ServiceConstant.PARA_RADIUS);
 		if (!StringUtil.isEmpty(latitudeStr) && !StringUtil.isEmpty(longitudeStr) && !StringUtil.isEmpty(radiusStr)){
 			hasLocation = true;
 			latitude = Double.parseDouble(latitudeStr);
 			longitude = Double.parseDouble(longitudeStr);
 			radius = Double.parseDouble(radiusStr);
 		}
 		
 		String todayOnlyStr = request.getParameter(ServiceConstant.PARA_TODAY_ONLY);
 		if (!StringUtil.isEmpty(todayOnlyStr)){
 			todayOnly = (Integer.parseInt(todayOnlyStr) == 0 ? false : true);
 		}		
 		
 		String categoryStr = request.getParameter(ServiceConstant.PARA_CATEGORIES);
 		categoryList = UrlUtil.parserUrlIntArray(categoryStr);
 
 		String startOffsetStr = request.getParameter(ServiceConstant.PRAR_START_OFFSET);
 		if (!StringUtil.isEmpty(startOffsetStr)){
 			startOffset = Integer.parseInt(startOffsetStr);
 		}		
 
 		String maxCountStr = request.getParameter(ServiceConstant.PARA_MAX_COUNT);
 		if (!StringUtil.isEmpty(maxCountStr)){
 			maxCount = Integer.parseInt(maxCountStr);
 		}		
 
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_APPID_NULL)) {
 			return false;
 		}				
 		
 		String returnCountStr = request.getParameter(ServiceConstant.PARA_RETURN_COUNT); 
 		if (!StringUtil.isEmpty(returnCountStr)){
 			reCountStatus = Integer.parseInt(returnCountStr);
 		}
 		
 		return true;
 	}
 	
 	private static void safePut(JSONObject object, String key, Object value) {
 		if (value == null)
 			return;
 		object.put(key, value);
 	}
 
 }

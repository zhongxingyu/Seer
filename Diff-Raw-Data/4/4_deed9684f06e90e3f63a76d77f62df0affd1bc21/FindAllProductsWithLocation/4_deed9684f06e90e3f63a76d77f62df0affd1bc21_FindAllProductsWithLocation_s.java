 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class FindAllProductsWithLocation extends CommonGroupBuyService {
 	String appId;
 	String maxCount;
 	String startOffset;
 	String latitude;
 	String longitude;
 
 	@Override
 	public void handleData() {
 		List<Product> productList = ProductManager.getAllProductsWithLocation(mongoClient, latitude,
 				longitude, startOffset, maxCount);
 		resultData = CommonServiceUtils.productListToJSONArray(productList);
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "FindAllProductsWithLocation [appId=" + appId + ", latitude="
 				+ latitude + ", longitude=" + longitude + ", maxCount="
 				+ maxCount + ", startOffset=" + startOffset + "]";
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		maxCount = request.getParameter(ServiceConstant.PARA_MAX_COUNT);
 		startOffset = request.getParameter(ServiceConstant.PRAR_START_OFFSET);
 		latitude = request.getParameter(ServiceConstant.PARA_LANGUAGE);
 		longitude = request.getParameter(ServiceConstant.PARA_LONGTITUDE);
 
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_APPID_NULL)) {
 			return false;
 		}
		if (!check(latitude, ErrorCode.ERROR_PARAMETER_LANGUAGE_EMPTY,
				ErrorCode.ERROR_PARAMETER_LANGUAGE_NULL)) {
 			return false;
 		}
 		if (!check(longitude, ErrorCode.ERROR_PARAMETER_LONGITUDE_EMPTY,
 				ErrorCode.ERROR_PARAMETER_LONGITUDE_NULL)) {
 			return false;
 		}
 
 		return true;
 	}
 
 }

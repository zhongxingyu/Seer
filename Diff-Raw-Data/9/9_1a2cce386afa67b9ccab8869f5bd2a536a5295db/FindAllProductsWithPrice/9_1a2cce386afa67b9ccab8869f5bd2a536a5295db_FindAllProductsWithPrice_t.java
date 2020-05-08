 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class FindAllProductsWithPrice extends CommonGroupBuyService {
 
 	String appId;
 	String maxCount;			
 	String startOffset;
 	String city;
 	
 	
 	
 	@Override
 	public String toString() {
 		return "FindAllProductsWithPrice [appId=" + appId + ", city=" + city
 				+ ", maxCount=" + maxCount + ", startOffset=" + startOffset
 				+ "]";
 	}
 
 	@Override
 	public void handleData() {
 		List<Product> products = ProductManager.getAllProductWithPrice(mongoClient, city, true, startOffset, maxCount);
 		resultData = CommonServiceUtils.productListToJSONArray(products);
 	}
 
 	
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		maxCount = request.getParameter(ServiceConstant.PARA_MAX_COUNT);
 		startOffset = request.getParameter(ServiceConstant.PRAR_START_OFFSET);
 		city = request.getParameter(ServiceConstant.PARA_CITY);
 		
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY, ErrorCode.ERROR_PARAMETER_APPID_NULL)) {
 			return false;
 		}
 		
 		return true;
 	}
 
 }

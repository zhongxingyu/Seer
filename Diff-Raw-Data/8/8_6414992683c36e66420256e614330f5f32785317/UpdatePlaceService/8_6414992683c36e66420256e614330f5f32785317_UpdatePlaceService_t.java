 package com.orange.place.api.service;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.orange.place.constant.ErrorCode;
 import com.orange.place.constant.ServiceConstant;
 import com.orange.place.manager.PlaceManager;
 
 public class UpdatePlaceService extends CommonService {
 
 	String userId;
 	String placeId;
 
 	String longitude;
 	String latitude;
 	String name;
 	String radius;
 	String postType;
 	String desc;
 
 	@Override
 	public void handleData() {
 		// TODO Auto-generated method stub
 		// String creator = PlaceManager.getCreator(cassandraClient, placeId);
 		if (!PlaceManager.isPlaceExist(cassandraClient, placeId)) {
 			log.info("fail to update place, place name=" + name + ", userId="
 					+ userId + ".Reason:place not found!");
 			resultCode = ErrorCode.ERROR_PLACE_NOT_FOUND;
 			return;
 		}
 		PlaceManager.updatePlace(cassandraClient, placeId, longitude, latitude,
 				name, radius, postType, desc);
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void printData() {
 		// TODO Auto-generated method stub
 		log.info(String.format(
 				"userId=%s, placeId=%s, longitude=%s, latitude=%s,"
 						+ "name=%s, radisu=%s, postType=%s, desc=%s", userId,
 				placeId, longitude, latitude, name, radius, postType, desc));
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		placeId = request.getParameter(ServiceConstant.PARA_PLACEID);
 		userId = request.getParameter(ServiceConstant.PARA_USERID);
		
		longitude = request.getParameter(ServiceConstant.PARA_LONGTITUDE);
		latitude = request.getParameter(ServiceConstant.PARA_LATITUDE);
		name = request.getParameter(ServiceConstant.PARA_NAME);
		radius = request.getParameter(ServiceConstant.PARA_RADIUS);
		postType = request.getParameter(ServiceConstant.PARA_POSTTYPE);
		desc = request.getParameter(ServiceConstant.PARA_DESC);	
 		if (!check(placeId, ErrorCode.ERROR_PARAMETER_PLACEID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_PLACEID_NULL))
 			return false;
 
 		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_USERID_NULL))
 			return false;
 
 		return true;
 	}
 }

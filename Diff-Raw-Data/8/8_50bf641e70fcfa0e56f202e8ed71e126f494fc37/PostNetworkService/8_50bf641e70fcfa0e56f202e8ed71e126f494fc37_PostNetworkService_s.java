 //xumengyi@baixing.com
 package com.baixing.util.post;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Pair;
 
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.BXLocation;
 import com.baixing.entity.PostGoodsBean;
 import com.baixing.entity.UserBean;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.network.api.ApiError;
 import com.baixing.network.api.ApiParams;
 import com.baixing.network.api.BaseApiCommand;
 import com.baixing.network.api.BaseApiCommand.Callback;
 import com.baixing.util.ErrorHandler;
 import com.baixing.util.Util;
 
 public class PostNetworkService implements Callback{
 	private Handler handler;
 	private String city;
 	private String category;
 	
 	public static class PostResultData extends Object{
 		public String id;
 		public int error;
 		public String message;
 		public boolean isRegisteredUser;
 	}
 	
 	public PostNetworkService(Handler handler){
 		this.handler = handler;
 	}
 	private boolean isretreiveMeta = false; 
 	public void retreiveMetaAsync(String cityEnglishName, String categoryEnglishName){
 		String apiName = "category_meta_post";
 		city = cityEnglishName;
 		category = categoryEnglishName;
 		ApiParams param = new ApiParams();
 		param.addParam("categoryEnglishName", categoryEnglishName);
 		param.addParam("cityEnglishName", (cityEnglishName == null ? GlobalDataManager.getInstance().getCityEnglishName() : cityEnglishName));
 //		ApiClient.getInstance().remoteCall(Api.createPost(apiName), param, this);
 		BaseApiCommand.createCommand(apiName, false, param).execute(GlobalDataManager.getInstance().getApplicationContext(), this);
 		isretreiveMeta = true;
 	}
 	
 	public void postAdAsync(HashMap<String, String> params,
 			HashMap<String, String> mustParams,
 			LinkedHashMap<String, PostGoodsBean> beans,
 			List<String> bmpUrls,
 			BXLocation address,
 			boolean editMode){
 
 		if(address != null){
 			params.put(PostCommonValues.STRING_AREA, getDistrictByLocation(address, beans));
 		}
 
 		String apiName = editMode ? "ad_update" : "ad_add";
 		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
 		boolean registered = (user != null && user.getPhone() != null && !user.getPhone().equals(""));
 		ApiParams apiParam = new ApiParams();
 		if(mustParams != null){
 			apiParam.addAll(mustParams);
 		}
 
 		if(registered){
 			apiParam.appendAuthInfo(user.getPhone(), user.getPassword());//.appendUserInfo(user);
 		}
 		Pair<Double, Double> coorGoogle = PostLocationService.retreiveCoorFromGoogle(params.get(PostCommonValues.STRING_DETAIL_POSITION));
 		apiParam.addParam("lat", String.valueOf(coorGoogle.first));
 		apiParam.addParam("lng", String.valueOf(coorGoogle.second));
 
 		Set<String> keys = params.keySet();
 		if(keys != null){
 			Iterator<String> ite = keys.iterator();
 			while(ite.hasNext()){
 				String key = ite.next();
 				String value = params.get(key);
 				if (value != null && value.length() > 0 && beans.get(key) != null) {
 //					try{
 //						apiParam.addParam(URLEncoder.encode(beans.get(key).getName(), "UTF-8"),
 //								URLEncoder.encode(value, "UTF-8").replaceAll("%7E", "~"));//ugly, replace, what's that? 
 //						if(beans.get(key).getName().equals(PostCommonValues.STRING_DESCRIPTION)){//generate title from description
 //							apiParam.addParam("title", 
 //									URLEncoder.encode(value.substring(0, Math.min(25, value.length())), "UTF-8").replaceAll("%7E", "~"));
 
 						apiParam.addParam(beans.get(key).getName(), value); 
 						if(beans.get(key).getName().equals(PostCommonValues.STRING_DESCRIPTION)){//generate title from description
 							apiParam.addParam("title", value.substring(0, Math.min(25, value.length())));
 						
 						}
 //					}catch(UnsupportedEncodingException e){
 //						e.printStackTrace();
 //					}
 				}
 			}
 		}
 		
 		String images = "";
 		if(bmpUrls != null){
 			for (int i = 0; i < bmpUrls.size(); i++) {				
 				if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
 					images += "," + bmpUrls.get(i);
 				}
 			}
 			if(images != null && images.length() > 0 && images.charAt(0) == ','){
 				images = images.substring(1);
 			}
 			if(images != null && images.length() > 0){
 				apiParam.addParam("images", images);
 			}
 		}
 		
 //		ApiClient.getInstance().remoteCall(Api.createPost(apiName), apiParam, this);
 		BaseApiCommand.createCommand(apiName, false, apiParam).execute(GlobalDataManager.getInstance().getApplicationContext(), this);
 		isretreiveMeta = false;
 	}
 	
 	private void sendMessage(int what, Object obj){
 		if(handler != null){
 			Message msg = Message.obtain();
 			msg.what = what;
 			msg.obj = obj;
 			handler.sendMessage(msg);
 		}
 	}
 	
 	private String getDistrictByLocation(BXLocation location, LinkedHashMap<String, PostGoodsBean> postList){
 		if(location == null || location.subCityName == null) return null;
 		if(postList != null && postList.size() > 0){
 			Object[] postListKeySetArray = postList.keySet().toArray();
 			for(int i = 0; i < postList.size(); ++ i){
 				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
 				if(bean.getName().equals(PostCommonValues.STRING_AREA)){
 					if(bean.getLabels() != null){
 						for(int t = 0; t < bean.getLabels().size(); ++ t){
 							if(location.subCityName.contains(bean.getLabels().get(t))){
 								return bean.getValues().get(t);
 							}
 						}
 					}						
 				}
 			}
 		}
 		return null;
 	}
 	
 	private void handlePostMsgBack(String rawData){
 		if (rawData != null) {
 			try{
 				JSONObject jsonObject = new JSONObject(rawData);
 				JSONObject errorJson = jsonObject.getJSONObject("error");
 				int code = errorJson.getInt("code");
 				String message = errorJson.getString("message");
 				PostResultData data = new PostResultData();
 				data.error = code;
 				data.message = message;
 				data.id = jsonObject.getString("id");
 				data.isRegisteredUser = jsonObject.getBoolean("contactIsRegisteredUser");
 				if(code == 0){									
 					sendMessage(PostCommonValues.MSG_POST_SUCCEED, data);
 				}else{
 					sendMessage(PostCommonValues.MSG_POST_FAIL, data);
 				}
 				return;
 			}catch(JSONException e){
 				sendMessage(PostCommonValues.MSG_POST_FAIL, "发布失败");
 			}
 		}
 	}
 
 //	private String replaceTitleToDescription(String msg) {
 //		// replace title to description in message
 //		PostGoodsBean titleBean = null, descriptionBean = null;
 //		for (String key : postList.keySet()) {
 //			PostGoodsBean bean = postList.get(key);
 //			if (bean.getName().equals("title")) 
 //				titleBean = bean;
 //			if (bean.getName().equals(STRING_DESCRIPTION))
 //				descriptionBean = bean;
 //		}
 //		if (titleBean != null && descriptionBean != null)
 //			msg = msg.replaceAll(titleBean.getDisplayName(), descriptionBean.getDisplayName());
 //		return msg;
 //	}
 	
 	
 	
 	private void handleGetMetaMsgBack(String rawData){
 		if(rawData != null){
 			LinkedHashMap<String, PostGoodsBean> pl = JsonUtil.getPostGoodsBean(rawData);
 			if(pl == null || pl.size() == 0){
 				sendMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
 				return;
 			}
 			Util.saveJsonAndTimestampToLocate(GlobalDataManager.getInstance().getApplicationContext(), 
 					category + city, rawData, System.currentTimeMillis()/1000);
 			sendMessage(PostCommonValues.MSG_GET_META_SUCCEED, pl);
 		} else {
 			sendMessage(PostCommonValues.MSG_GET_META_FAIL, null);
 		}
 	}
 
 	@Override
 	public void onNetworkDone(String apiName, String responseData) {
 		if(isretreiveMeta){
 			handleGetMetaMsgBack(responseData);
 			isretreiveMeta = false;
 		}else{
 			handlePostMsgBack(responseData);
 		}
 	}
 
 	@Override
 	public void onNetworkFail(String apiName, ApiError error) {
		String msg = "网络错误";//(error == null ? "请求错误" : error.getMsg()); //TODO
 		int msgCode = isretreiveMeta ? PostCommonValues.MSG_GET_META_FAIL : PostCommonValues.MSG_POST_FAIL;
 		sendMessage(msgCode, msg);
 	}
 }

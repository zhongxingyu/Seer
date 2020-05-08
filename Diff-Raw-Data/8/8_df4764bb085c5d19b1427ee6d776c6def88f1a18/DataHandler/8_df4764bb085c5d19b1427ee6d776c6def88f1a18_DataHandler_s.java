 package nanofuntas.fbls;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.codec.binary.Base64;
 import org.json.simple.JSONObject;
 
 /**
  * This DataHandler class receives data from servlet, 
  * and handle data and finally returns data to servlet 
  */
 public class DataHandler {
 	private static boolean DEBUG = true;
 	private static String TAG = "DataHandler";
 	
 	/**
 	 * This handleData function handles data received from servlet and returns result data to servlet
 	 * 
 	 * @param jsonReq, JSONObject received from servlet
 	 * @return JSONObject result data to servlet
 	 */
 	@SuppressWarnings("unchecked")
 	public static JSONObject handleData(JSONObject jsonReq) {
 		String mReqType = (String) jsonReq.get(Config.KEY_REQ_TYPE);
 		if (DEBUG) System.out.println(TAG + ", " + mReqType);	
 		if (DEBUG) System.out.println(TAG + ", " + jsonReq.toString());	
 		
 		JSONObject jsonRsp = new JSONObject();
 		
 		if (mReqType.equals(Config.KEY_REQ_TYPE_LOGIN)) {
 			String strEmail = (String) jsonReq.get(Config.KEY_EMAIL);
 			String strPassword = (String) jsonReq.get(Config.KEY_PASSWORD);
 
 			JSONObject result = DatabaseService.login(strEmail, strPassword);
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_LOGIN);
 			jsonRsp.put(Config.KEY_RESULT, result);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_REGISTER)) {
 			String strEmail = (String) jsonReq.get(Config.KEY_EMAIL);
 			String strPassword = (String) jsonReq.get(Config.KEY_PASSWORD);
 			
 			DatabaseService.register(strEmail, strPassword);	
 			
 			//make sure user is registered and return uid for registering
 			JSONObject result = DatabaseService.login(strEmail, strPassword);
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_REGISTER);
 			jsonRsp.put(Config.KEY_RESULT, result);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_PLAYER_STATUS)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);			
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_PLAYER_STATUS);
 			
 			jsonRsp.put(Config.KEY_UID, uid);
 			for (String s: Config.PLAYER_PROFILE_ARRAY) {
 				jsonRsp.put(s, DatabaseService.getPlayerProfile(uid, s));
 			}
 			for (String s: Config.PLAYER_RATING_ALL_ARRAY) { 
 				jsonRsp.put(s, DatabaseService.getPlayerRating(uid, s));
 			}
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_PLAYER_RATING)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);			
 			JSONObject jsonRating = (JSONObject) jsonReq.get(Config.KEY_RATING);
 			long totalRated = DatabaseService.getPlayerRating(uid, Config.KEY_TOTAL_RATED);
 			
 			for (String s: Config.PLAYER_RATING_WITHOUT_OVERALL_ARRAY) {
 				setNewRating(uid, s, jsonRating, totalRated);
 			}
 
 			totalRated ++;
 					
 			DatabaseService.setPlayerRating(uid, Config.KEY_TOTAL_RATED, totalRated);
 			setOverallRating(uid);
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_PLAYER_RATING);
 			jsonRsp.put(Config.KEY_RESULT, Config.KEY_OK);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_TEAM_STATUS)) {
 			long tid = (Long) jsonReq.get(Config.KEY_TID);			
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_TEAM_STATUS);
 			
 			for (String s: Config.TEAM_PROFILE_ARRAY) {
 				jsonRsp.put(s, DatabaseService.getTeamProfile(tid, s));
 			}
 			for (String s: Config.TEAM_RATING_ALL_ARRAY) { 
 				jsonRsp.put(s, DatabaseService.getTeamRating(tid, s));
 			}
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_CREATE_TEAM)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);
 			String teamName = (String) jsonReq.get(Config.KEY_TEAM_NAME);
 
 			long result = DatabaseService.createTeam(uid, teamName);
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_CREATE_TEAM);
 			jsonRsp.put(Config.KEY_RESULT, result);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_JOIN_TEAM)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);
 			String teamName = (String) jsonReq.get(Config.KEY_TEAM_NAME);
 
 			long result = DatabaseService.joinTeam(uid, teamName);
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_JOIN_TEAM);
 			jsonRsp.put(Config.KEY_RESULT, result);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_INCRUIT_PLAYER)) {
 			long tid = (Long) jsonReq.get(Config.KEY_TID);
 			String playerName = (String) jsonReq.get(Config.KEY_NAME);
 
 			long result = DatabaseService.incruitPlayer(tid, playerName);
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_INCRUIT_PLAYER);
 			jsonRsp.put(Config.KEY_RESULT, result);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_UPDATA_PLAYER_PROFILE)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);
 			JSONObject myProfile = (JSONObject) jsonReq.get(Config.KEY_PLAYER_PROFILE);
 			
 			String value = null;
 			for (String s : Config.PLAYER_PROFILE_ARRAY) {
 				value = (String) myProfile.get(s);
 				DatabaseService.setPlayerProfile(uid, s, value);
 			}
 			
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_UPDATA_PLAYER_PROFILE);
 			jsonRsp.put(Config.KEY_RESULT, Config.KEY_OK);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_MEMBERS_PROFILE)) {
 			long tid = (Long) jsonReq.get(Config.KEY_TID);
 			
 			JSONObject jsonMembersProfile = DatabaseService.getMembersProfile(tid);
 			
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_MEMBERS_PROFILE);
 			jsonRsp.put(Config.KEY_RESULT, jsonMembersProfile);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_MEMBERS_STATUS)) {			
 			long tid = (Long) jsonReq.get(Config.KEY_TID);
 			
 			JSONObject jsonMembersStatus = DatabaseService.getMembersStatus(tid);
 			
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_MEMBERS_STATUS);
 			jsonRsp.put(Config.KEY_RESULT, jsonMembersStatus);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_IMG_UPLOAD)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);
 			
 			String strImage = (String) jsonReq.get(Config.KEY_IMAGE);
 			byte[] bytesImage = Base64.decodeBase64(strImage);
 
 			String absoluteStuffPath = FBLServlet.PATH;
 			String path = absoluteStuffPath + uid + ".png";
 			InputStream in = null;
 			try {
				in = new ByteArrayInputStream(bytesImage);
 				BufferedImage bImageFromConvert = ImageIO.read(in);
				ImageIO.write(bImageFromConvert, "png", new File(path));
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				if (in != null)
 					try {
 						in.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 			}
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_IMG_UPLOAD);
 			jsonRsp.put(Config.KEY_RESULT, Config.KEY_OK);
 			
 		} else if (mReqType.equals(Config.KEY_REQ_TYPE_IMG_DOWNLOAD)) {
 			long uid = (Long) jsonReq.get(Config.KEY_UID);
 
 			jsonRsp.put(Config.KEY_RSP_TYPE, Config.KEY_RSP_TYPE_IMG_DOWNLOAD);
 
 			String absoluteStuffPath = FBLServlet.PATH;
 			String path = absoluteStuffPath + uid + ".png";		
 			FileInputStream in = null;
 			
 			File f = new File(path);
 			if (!f.exists()) { 
 				jsonRsp.put(Config.KEY_IMAGE, null);
 				return jsonRsp;
 			} 
 			
 			try {
 				in = new FileInputStream(path);
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			}
 			
 		    ByteArrayOutputStream byteArrayOutStrm = new ByteArrayOutputStream();
 
 		    int lenth;
 		    int bufferSize = 1024;
 			byte[] buffer = new byte[bufferSize];
 			byte[] outbytes = null;
 			
 		    try {
 				while ((lenth = in.read(buffer, 0, buffer.length)) != -1) {
 					byteArrayOutStrm.write(buffer, 0, lenth);
 				}
 		    	byteArrayOutStrm.flush();
 		    	outbytes = byteArrayOutStrm.toByteArray();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (in != null)	in.close();
 					if (byteArrayOutStrm != null) byteArrayOutStrm.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}	    			
 	    	String strImage = Base64.encodeBase64String(outbytes);
 	    	jsonRsp.put(Config.KEY_IMAGE, strImage);
 			
 		} 
 		if (DEBUG) System.out.println(TAG + " jsonRsp:" + jsonRsp);
 		return jsonRsp;		
 	}
 	
 	private static void setNewRating(long uid, String key, JSONObject jsonRating, long totalRated) {
 		if (DEBUG) System.out.println(TAG + ", setNewRating()");	
 		
 		long ratingNow = DatabaseService.getPlayerRating(uid, key);
 		long newRating = (Long) jsonRating.get(key);
 
 		if (totalRated != 0) {
 			newRating = (ratingNow * totalRated + newRating) / (totalRated + 1);						
 		}
 
 		DatabaseService.setPlayerRating(uid, key, newRating);
 	}
 	
 	private static void setOverallRating(long uid) {
 		if (DEBUG) System.out.println(TAG + ", setOverallRating()");
 
 		long ratingSum = 0;
 		for (String s: Config.PLAYER_RATING_WITHOUT_OVERALL_ARRAY) {
 			ratingSum += DatabaseService.getPlayerRating(uid, s);
 		}
 				
 		long newOverall = ratingSum / Config.PLAYER_RATING_WITHOUT_OVERALL_ARRAY.length;
 		DatabaseService.setPlayerRating(uid, Config.KEY_OVERALL, newOverall);		
 	}
 	
 }

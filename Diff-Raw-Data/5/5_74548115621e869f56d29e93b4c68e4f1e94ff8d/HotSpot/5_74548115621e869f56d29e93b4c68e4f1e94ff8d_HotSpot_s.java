 package com.zarcode.data.resources;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.gson.Gson;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.BuzzDao;
 import com.zarcode.data.dao.HotSpotDao;
 import com.zarcode.data.dao.UserDao;
 import com.zarcode.data.dao.UserTokenDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.model.BuzzMsgDO;
 import com.zarcode.data.model.CommentDO;
 import com.zarcode.data.model.HotSpotDO;
 import com.zarcode.data.model.UserTokenDO;
 
@Path("/hotSpots")
 public class HotSpot extends ResourceBase {
 	
 	private Logger logger = Logger.getLogger(HotSpot.class.getName());
 	
 	@Context 
 	HttpHeaders headers = null;
 	
 	@Context 
 	SecurityContext context = null;
 	
 	@Context 
 	UriInfo uriInfo = null;
     
 	@Context 
     Request request = null;
 	
 	String container = null;
 	
 	private static final int MAXPAGE = 10;
 	
 	@POST
 	@Produces("application/json")
 	@Path("/lakes/{resourceId}/hotspot")
 	public HotSpotDO addHotSpot(@PathParam("resourceId") Long resourceId, @QueryParam("userToken") String userToken,  String hotSpot) {
 		List<BuzzMsgDO> res = null;
 		HotSpotDao dao = null;
 		WaterResourceDao waterResDao = null;
 		UserDao userDao = null;
 		int rows = 0;
 		String llId = null;
 		HotSpotDO spot = null;
 		HotSpotDO newSpot = null;
 	
 		logger.info("Process NEW hotspot: " + hotSpot);
 		
 		UserTokenDao tokenDao = new UserTokenDao();
 		UserTokenDO t = tokenDao.getTokenByTokenStr(userToken);
 		if (t == null) {
 			logger.warning("*** REJECTED --- INVALID USER TOKEN ---> " + userToken);
 			return newSpot;
 		}
 		
 		llId = t.getLlId();
 		
 		if (hotSpot != null && hotSpot.length() > 0) {
 			spot = new Gson().fromJson(hotSpot, HotSpotDO.class);
 			try {
 				if (spot != null) {
 					spot.postCreation();
 					spot.setLLId(llId);
 					spot.setResourceId(resourceId);
 					dao = new HotSpotDao();
 					newSpot = dao.addHotSpot(spot);
 					//
 					// since event was created inside lake area, update last communication
 					//
 					if (spot.getResourceId() > 0) {
 						try {
 							waterResDao = new WaterResourceDao();
 							waterResDao.updateLastUpdate(spot.getResourceId());
 							logger.info("Updated lastUpdated for resource=" + spot.getResourceId());
 						}
 						catch (JDOObjectNotFoundException ex) {
 							logger.severe("Unable to update lastUpdated timestamp for water resource");
 						}
 					}
 					logger.info("Successfully added new hotSpot -- " + spot);
 				}
 			}
 			catch (Exception e) {
 				logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 			}
 		}
 		else {
 			logger.warning("Event JSON instance is empty");
 		}
 		logger.info("Returning: " + newSpot);
 		return newSpot;
 	}
 	
 	@POST
 	@Produces("text/plain")
 	@Path("/rating/{hotSpotId}")
 	public String addRatingToHotSpot(@PathParam("hotSpotId") Long hotSpotId, @QueryParam("userToken") String userToken, int rating) {
 		int rows = 0;
 		CommentDO comm = null;
 		CommentDO newComm = null;
 		String llId = null;
 	
 		logger.info("Process incoming rating: " + rating);
 	
 		UserTokenDao tokenDao = new UserTokenDao();
 		UserTokenDO t = tokenDao.getTokenByTokenStr(userToken);
 		if (t == null) {
 			logger.warning("*** REJECTED --- INVALID USER TOKEN ---> " + userToken);
 			return "FAIL";
 		}
 		llId = t.getLlId();
 		HotSpotDao dao = new HotSpotDao();
 		HotSpotDO res = dao.getHotSpotById(hotSpotId);
 		if (res == null) {
 			logger.warning("*** REJECTED --- MISSING HOTSPOT ---> " + hotSpotId);
 			return "FAIL";
 		}
 		dao.incrementRating(res.getHotSpotId());
 		
 		return "SUCCESS";
 	}
 	
 	@GET 
 	@Path("/lakes/{resourceId}")
 	@Produces("application/json")
 	public List<HotSpotDO> getHotSpotsByResourceId(@PathParam("resourceId") Long resourceId) {
 		int i = 0;
 		List<HotSpotDO> results = null;
 		HotSpotDao dao = null;
 		boolean bFindAll = false;
 		
 		logger.info("Entered");
 		try {
 			dao = new HotSpotDao();
 			results = dao.getHotSpotsByResourceId(resourceId);
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 		}
 		logger.info("Exit");
 		
 		return results;
 	}
 	
 	@GET 
	@Path("/user/{userToken}")
 	@Produces("application/json")
 	public List<HotSpotDO> getHotSpotsByUserToken(@PathParam("userToken") String userToken) {
 		int i = 0;
 		List<HotSpotDO> results = null;
 		HotSpotDao dao = null;
 		UserTokenDao tokenDao = null;
 		boolean bFindAll = false;
 		
 		logger.info("Entered");
 		try {
 			dao = new HotSpotDao();
 			tokenDao = new UserTokenDao();
 			UserTokenDO t = tokenDao.getTokenByTokenStr(userToken);
 			if (t != null) {
 				String llId = t.getLlId();
 				results = dao.getHotSpotsByUser(llId);
 			}
 			else {
 				logger.warning("*** Unable to find a matching user token for string=" + userToken);
 			}
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 		}
 		logger.info("Exit");
 		
 		return results;
 	}
 	
 	
 } // Event

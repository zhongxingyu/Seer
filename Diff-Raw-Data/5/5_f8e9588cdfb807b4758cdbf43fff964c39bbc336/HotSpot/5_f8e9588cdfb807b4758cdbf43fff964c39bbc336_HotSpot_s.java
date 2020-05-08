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
 import com.zarcode.common.ApplicationProps;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.BuzzDao;
 import com.zarcode.data.dao.HotSpotDao;
 import com.zarcode.data.dao.UserDao;
 import com.zarcode.data.dao.UserTokenDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.exception.BadRequestAppDataException;
 import com.zarcode.data.exception.BadUserDataProvidedException;
 import com.zarcode.data.model.BuzzMsgDO;
 import com.zarcode.data.model.CommentDO;
 import com.zarcode.data.model.HotSpotDO;
 import com.zarcode.data.model.UserTokenDO;
 import com.zarcode.platform.model.AppPropDO;
 import com.zarcode.security.BlockTea;
 
 @Path("/hotspots")
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
 	@Path("/addOrUpdate")
 	public HotSpotDO addOrUpdateHotSpot(@QueryParam("userToken") String userToken,  String hotSpot) {
 		List<BuzzMsgDO> res = null;
 		HotSpotDao dao = null;
 		WaterResourceDao waterResDao = null;
 		UserDao userDao = null;
 		int rows = 0;
 		String llId = null;
 		String idClear = null;
 		HotSpotDO spot = null;
 		HotSpotDO newSpot = null;
 		boolean adding = false;
 	
 		logger.info("Process NEW hotspot: " + hotSpot);
 		
 		UserTokenDao tokenDao = new UserTokenDao();
 		UserTokenDO t = tokenDao.getTokenByTokenStr(userToken);
 		if (t == null) {
 			logger.warning("*** REJECTED --- INVALID USER TOKEN ---> " + userToken);
 			throw new BadUserDataProvidedException();
 		}
 		
 		idClear = t.getIdClear();
 		
 		if (hotSpot != null && hotSpot.length() > 0) {
 			spot = new Gson().fromJson(hotSpot, HotSpotDO.class);
 			try {
 				if (spot != null) {
 					dao = new HotSpotDao();
 					/*
 					 * new hotspot
 					 */
 					if (spot.getHotSpotId() == null) {
 						adding = true;
 						spot.postCreation();
 						
 						/*
 						 * update llId for transport back to client
 						 */
 						AppPropDO p2 = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 						BlockTea.BIG_ENDIAN = false;
 						llId = BlockTea.encrypt(idClear, p2.getStringValue());
 						spot.setLLId(llId);
 					
 						/*
 						 * add it
 						 */
 						newSpot = dao.addHotSpot(spot);
 						logger.info("Successfully added new hotSpot -- " + newSpot);
 					}
 					/*
 					 * existing hotspot
 					 */
 					else {
 						newSpot = dao.updateHotSpot(spot);
 						logger.info("Successfully updated new hotSpot -- " + newSpot);
 					}
 					//
 					// since event was created inside lake area, update last communication
 					//
 					if (adding && spot.getResourceId() > 0) {
 						try {
 							waterResDao = new WaterResourceDao();
 							waterResDao.updateLastUpdate(spot.getResourceId());
 							logger.info("Updated lastUpdated for resource=" + spot.getResourceId());
 						}
 						catch (JDOObjectNotFoundException ex) {
 							logger.severe("Unable to update lastUpdated timestamp for water resource");
 						}
 					}
 				}
 			}
 			catch (Exception e) {
 				logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 			}
 		}
 		else {
 			logger.warning("Event JSON instance is empty");
 		}
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
 		String idClear = null;
 	
 		logger.info("Process incoming rating: " + rating);
 	
 		UserTokenDao tokenDao = new UserTokenDao();
 		UserTokenDO t = tokenDao.getTokenByTokenStr(userToken);
 		if (t == null) {
 			logger.warning("*** REJECTED --- INVALID USER TOKEN ---> " + userToken);
 			throw new BadUserDataProvidedException();
 		}
 		idClear = t.getIdClear();
 		HotSpotDao dao = new HotSpotDao();
 		HotSpotDO res = dao.getHotSpotById(hotSpotId);
 		if (res == null) {
 			logger.warning("*** REJECTED --- MISSING HOTSPOT ---> " + hotSpotId);
 			throw new BadRequestAppDataException();
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
 			throw new BadRequestAppDataException();
 		}
 		logger.info("Exit");
 		
 		return results;
 	}
 	
 	@GET 
 	@Path("/users/{userToken}")
 	@Produces("application/json")
 	public List<HotSpotDO> getHotSpotsByUserToken(@PathParam("userToken") String userToken) {
 		int i = 0;
 		List<HotSpotDO> results = null;
 		HotSpotDO spot = null;
 		HotSpotDao dao = null;
 		UserTokenDao tokenDao = null;
 		boolean bFindAll = false;
 		
 		logger.info("Entered");
 		try {
 			dao = new HotSpotDao();
 			tokenDao = new UserTokenDao();
 			UserTokenDO t = tokenDao.getTokenByTokenStr(userToken);
 			if (t != null) {
 				String idClear = t.getIdClear();
 				results = dao.getHotSpotsByIdClear(idClear);
 			}
 			else {
 				logger.warning("*** Unable to find a matching user token for string=" + userToken);
 				throw new BadUserDataProvidedException();
 			}
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 			throw new BadRequestAppDataException();
 		}
 		logger.info("Exit");
 		
 		return results;
 	}
 	
 	
 } // HotSpot

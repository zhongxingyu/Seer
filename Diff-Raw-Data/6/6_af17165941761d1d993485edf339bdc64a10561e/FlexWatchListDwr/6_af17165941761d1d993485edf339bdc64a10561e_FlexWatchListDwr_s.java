 /*
     Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
     @author Matthew Lohbihler
  */
 package com.serotonin.eazytec.web.dwr;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.ObjectUtils;
 import org.directwebremoting.WebContextFactory;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.serotonin.db.pair.IntStringPair;
 import com.serotonin.eazytec.db.dao.HourPowerPointValueDao;
 import com.serotonin.eazytec.db.dao.MeterItemDao;
 import com.serotonin.eazytec.rt.dataImage.MeterItem;
 import com.serotonin.eazytec.vo.flexwatchlist.FlexWatchList;
 import com.serotonin.eazytec.vo.flexwatchlist.FlexWatchListCommon;
 import com.serotonin.eazytec.vo.flexwatchlist.FlexWatchListDao;
 import com.serotonin.eazytec.vo.flexwatchlist.FlexWatchListState;
 import com.serotonin.m2m2.Common;
 import com.serotonin.m2m2.DataTypes;
 import com.serotonin.m2m2.db.dao.DataPointDao;
 import com.serotonin.m2m2.db.dao.PointValueDao;
 import com.serotonin.m2m2.db.dao.UserDao;
 import com.serotonin.m2m2.i18n.TranslatableMessage;
 import com.serotonin.m2m2.rt.RuntimeManager;
 import com.serotonin.m2m2.rt.dataImage.DataPointRT;
 import com.serotonin.m2m2.rt.dataImage.PointValueTime;
 import com.serotonin.m2m2.rt.dataImage.types.DataValue;
 import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
 import com.serotonin.m2m2.view.ShareUser;
 import com.serotonin.m2m2.vo.DataPointExtendedNameComparator;
 import com.serotonin.m2m2.vo.DataPointSummary;
 import com.serotonin.m2m2.vo.DataPointVO;
 import com.serotonin.m2m2.vo.User;
 import com.serotonin.m2m2.vo.hierarchy.PointHierarchy;
 import com.serotonin.m2m2.vo.permission.Permissions;
 import com.serotonin.m2m2.web.dwr.ModuleDwr;
 import com.serotonin.m2m2.web.dwr.beans.DataExportDefinition;
 import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;
 import com.serotonin.m2m2.web.dwr.util.DwrPermission;
 import com.serotonin.m2m2.web.taglib.Functions;
 import com.serotonin.web.dwr.DwrResponseI18n;
 
 public class FlexWatchListDwr extends ModuleDwr {
     @DwrPermission(user = true)
     public Map<String, Object> init() {
         DataPointDao dataPointDao = new DataPointDao();
         Map<String, Object> data = new HashMap<String, Object>();
 
         PointHierarchy ph = dataPointDao.getPointHierarchy(true).copyFoldersOnly();
         User user = Common.getUser();
         List<DataPointVO> points = dataPointDao.getDataPoints(DataPointExtendedNameComparator.instance, false);
         for (DataPointVO point : points) {
             if (Permissions.hasDataPointReadPermission(user, point))
                 ph.addDataPoint(point.getPointFolderId(), new DataPointSummary(point));
         }
 
         ph.parseEmptyFolders();
 
         FlexWatchList watchList = new FlexWatchListDao().getSelectedWatchList(user.getId());
         prepareWatchList(watchList, user);
         setWatchList(user, watchList);
 
         data.put("pointFolder", ph.getRoot());
         data.put("shareUsers", getShareUsers(user));
         data.put("selectedWatchList", getWatchListData(user, watchList));
 
         return data;
     }
 
     /**
      * Retrieves point state for all points on the current watch list.
      * 
      * @param pointIds
      * @return
      */
     public List<FlexWatchListState> getPointData() {
         // Get the watch list from the user's session. It should have been set by the controller.
         return getPointDataImpl(getWatchList());
     }
 
     private List<FlexWatchListState> getPointDataImpl(FlexWatchList watchList) {
         if (watchList == null)
             return new ArrayList<FlexWatchListState>();
 
         HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
         User user = Common.getUser(request);
 
         FlexWatchListState state;
         List<FlexWatchListState> states = new ArrayList<FlexWatchListState>(watchList.getPointList().size());
         Map<String, Object> model = new HashMap<String, Object>();
         for (DataPointVO point : watchList.getPointList()) {
             // Create the watch list state.
             state = createWatchListState(request, point, Common.runtimeManager, model, user);
             states.add(state);
         }
 
         return states;
     }
 
     @DwrPermission(user = true)
     public void updateWatchListName(String name) {
         User user = Common.getUser();
         FlexWatchList watchList = getWatchList(user);
         FlexWatchListCommon.ensureWatchListEditPermission(user, watchList);
         watchList.setName(name);
         new FlexWatchListDao().saveWatchList(watchList);
     }
 
     @DwrPermission(user = true)
     public IntStringPair addNewWatchList(int copyId) {
         User user = Common.getUser();
 
         FlexWatchListDao watchListDao = new FlexWatchListDao();
         FlexWatchList watchList;
 
         if (copyId == Common.NEW_ID) {
             watchList = new FlexWatchList();
             watchList.setName(translate("common.newName"));
         }
         else {
             watchList = new FlexWatchListDao().getWatchList(getWatchList().getId());
             watchList.setId(Common.NEW_ID);
             watchList.setName(translate(new TranslatableMessage("common.copyPrefix", watchList.getName())));
         }
         watchList.setUserId(user.getId());
         watchList.setXid(watchListDao.generateUniqueXid());
 
         watchListDao.saveWatchList(watchList);
 
         setWatchList(user, watchList);
         watchListDao.saveSelectedWatchList(user.getId(), watchList.getId());
 
         return new IntStringPair(watchList.getId(), watchList.getName());
     }
 
     @DwrPermission(user = true)
     public void deleteWatchList(int watchListId) {
         User user = Common.getUser();
 
         FlexWatchListDao watchListDao = new FlexWatchListDao();
         FlexWatchList watchList = getWatchList(user);
         if (watchList == null || watchListId != watchList.getId())
             watchList = watchListDao.getWatchList(watchListId);
 
         if (watchList == null || watchListDao.getWatchLists(user.getId()).size() == 1)
             // Only one watch list left. Leave it.
             return;
 
         // Allow the delete.
         if (watchList.getUserAccess(user) == ShareUser.ACCESS_OWNER)
             watchListDao.deleteWatchList(watchListId);
         else
             watchListDao.removeUserFromWatchList(watchListId, user.getId());
     }
 
     @DwrPermission(user = true)
     public Map<String, Object> setSelectedWatchList(int watchListId) {
         User user = Common.getUser();
 
         FlexWatchListDao watchListDao = new FlexWatchListDao();
         FlexWatchList watchList = watchListDao.getWatchList(watchListId);
         FlexWatchListCommon.ensureWatchListPermission(user, watchList);
         prepareWatchList(watchList, user);
 
         watchListDao.saveSelectedWatchList(user.getId(), watchList.getId());
 
         Map<String, Object> data = getWatchListData(user, watchList);
         // Set the watchlist in the user object after getting the data since it may take a while, and the long poll
         // updates will all be missed in the meantime.
         setWatchList(user, watchList);
 
         return data;
     }
 
     @DwrPermission(user = true)
     public FlexWatchListState addToWatchList(int pointId) {
         HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
         User user = Common.getUser();
         DataPointVO point = new DataPointDao().getDataPoint(pointId);
         if (point == null)
             return null;
         FlexWatchList watchList = getWatchList(user);
 
         // Check permissions.
         Permissions.ensureDataPointReadPermission(user, point);
         FlexWatchListCommon.ensureWatchListEditPermission(user, watchList);
 
         // Add it to the watch list.
         watchList.getPointList().add(point);
         new FlexWatchListDao().saveWatchList(watchList);
         updateSetPermission(point, watchList.getUserAccess(user), new UserDao().getUser(watchList.getUserId()));
 
         // Return the watch list state for it.
         return createWatchListState(request, point, Common.runtimeManager, new HashMap<String, Object>(), user);
     }
 
     @DwrPermission(user = true)
     public void removeFromWatchList(int pointId) {
         // Remove the point from the user's list.
         User user = Common.getUser();
         FlexWatchList watchList = getWatchList(user);
         FlexWatchListCommon.ensureWatchListEditPermission(user, watchList);
         for (DataPointVO point : watchList.getPointList()) {
             if (point.getId() == pointId) {
                 watchList.getPointList().remove(point);
                 break;
             }
         }
         new FlexWatchListDao().saveWatchList(watchList);
     }
 
     @DwrPermission(user = true)
     public void moveUp(int pointId) {
         User user = Common.getUser();
         FlexWatchList watchList = getWatchList(user);
         FlexWatchListCommon.ensureWatchListEditPermission(user, watchList);
         List<DataPointVO> points = watchList.getPointList();
 
         DataPointVO point;
         for (int i = 0; i < points.size(); i++) {
             point = points.get(i);
             if (point.getId() == pointId) {
                 points.set(i, points.get(i - 1));
                 points.set(i - 1, point);
                 break;
             }
         }
 
         new FlexWatchListDao().saveWatchList(watchList);
     }
 
     @DwrPermission(user = true)
     public void moveDown(int pointId) {
         User user = Common.getUser();
         FlexWatchList watchList = getWatchList(user);
         FlexWatchListCommon.ensureWatchListEditPermission(user, watchList);
         List<DataPointVO> points = watchList.getPointList();
 
         DataPointVO point;
         for (int i = 0; i < points.size(); i++) {
             point = points.get(i);
             if (point.getId() == pointId) {
                 points.set(i, points.get(i + 1));
                 points.set(i + 1, point);
                 break;
             }
         }
 
         new FlexWatchListDao().saveWatchList(watchList);
     }
 
     /**
      * Convenience method for creating a populated view state.
      */
     private FlexWatchListState createWatchListState(HttpServletRequest request, DataPointVO pointVO, RuntimeManager rtm,
             Map<String, Object> model, User user) {
         // Get the data point status from the data image.
         DataPointRT point = rtm.getDataPoint(pointVO.getId());
 
         FlexWatchListState state = new FlexWatchListState();
         state.setId(Integer.toString(pointVO.getId()));
 
         PointValueTime pointValue = prepareBasePointState(Integer.toString(pointVO.getId()), state, pointVO, point,
                 model);
         setEvents(pointVO, user, model);
         if (pointValue != null && pointValue.getValue() instanceof ImageValue) {
             // Text renderers don't help here. Create a thumbnail.
             setImageText(request, state, pointVO, model, pointValue);
         }
         else
             setPrettyText(state, pointVO, model, pointValue);
 
         if (pointVO.isSettable())
             setChange(pointVO, state, point, request, model, user);
 
 //        if (state.getValue() != null)
 //            setChart(pointVO, state, request, model);
 //        setMessages(state, request, getModule().getWebPath() + "/web/snippet/watchListMessages.jsp", model);
 
         return state;
     }
 
     private static void setImageText(HttpServletRequest request, FlexWatchListState state, DataPointVO pointVO,
             Map<String, Object> model, PointValueTime pointValue) {
         if (!ObjectUtils.equals(pointVO.lastValue(), pointValue)) {
             state.setValue(generateContent(request, "imageValueThumbnail.jsp", model));
             if (pointValue != null)
                 state.setTime(Functions.getTime(pointValue));
             pointVO.updateLastValue(pointValue);
         }
     }
 
     /**
      * Method for creating image charts of the points on the watch list.
      */
     @DwrPermission(user = true)
     public String getImageChartData(int[] pointIds, int fromYear, int fromMonth, int fromDay, int fromHour,
             int fromMinute, int fromSecond, boolean fromNone, int toYear, int toMonth, int toDay, int toHour,
             int toMinute, int toSecond, boolean toNone, int width, int height) {
         DateTimeZone dtz = Common.getUser().getDateTimeZoneInstance();
         DateTime from = createDateTime(fromYear, fromMonth, fromDay, fromHour, fromMinute, fromSecond, fromNone, dtz);
         DateTime to = createDateTime(toYear, toMonth, toDay, toHour, toMinute, toSecond, toNone, dtz);
 
         StringBuilder htmlData = new StringBuilder();
         htmlData.append("<img src=\"achart/ft_");
         htmlData.append(System.currentTimeMillis());
         htmlData.append('_');
         htmlData.append(fromNone ? -1 : from.getMillis());
         htmlData.append('_');
         htmlData.append(toNone ? -1 : to.getMillis());
 
         boolean pointsFound = false;
         // Add the list of points that are numeric.
         List<DataPointVO> watchList = getWatchList().getPointList();
         for (DataPointVO dp : watchList) {
             int dtid = dp.getPointLocator().getDataTypeId();
             if ((dtid == DataTypes.NUMERIC || dtid == DataTypes.BINARY || dtid == DataTypes.MULTISTATE)
                     && ArrayUtils.contains(pointIds, dp.getId())) {
                 pointsFound = true;
                 htmlData.append('_');
                 htmlData.append(dp.getId());
             }
         }
 
         if (!pointsFound)
             // There are no chartable points, so abort the image creation.
             return translate("watchlist.noChartables");
 
         htmlData.append(".png?w=");
         htmlData.append(width);
         htmlData.append("&h=");
         htmlData.append(height);
         htmlData.append("\" alt=\"" + translate("common.imageChart") + "\"/>");
 
         return htmlData.toString();
     }
 
     private Map<String, Object> getWatchListData(User user, FlexWatchList watchList) {
         Map<String, Object> data = new HashMap<String, Object>();
         if (watchList == null)
             return data;
 
         List<DataPointVO> points = watchList.getPointList();
         List<Integer> pointIds = new ArrayList<Integer>(points.size());
         for (DataPointVO point : points) {
             if (Permissions.hasDataPointReadPermission(user, point))
                 pointIds.add(point.getId());
         }
 
         data.put("points", pointIds);
         data.put("users", watchList.getWatchListUsers());
         data.put("access", watchList.getUserAccess(user));
 
         return data;
     }
 
     private void prepareWatchList(FlexWatchList watchList, User user) {
         int access = watchList.getUserAccess(user);
         User owner = new UserDao().getUser(watchList.getUserId());
         for (DataPointVO point : watchList.getPointList())
             updateSetPermission(point, access, owner);
     }
 
     private void updateSetPermission(DataPointVO point, int access, User owner) {
         // Point isn't settable
         if (!point.getPointLocator().isSettable())
             return;
 
         // Read-only access
         if (access != ShareUser.ACCESS_OWNER && access != ShareUser.ACCESS_SET)
             return;
 
         // Watch list owner doesn't have set permission
         if (!Permissions.hasDataPointSetPermission(owner, point))
             return;
 
         // All good.
         point.setSettable(true);
     }
 
     private static void setPrettyText(FlexWatchListState state, DataPointVO pointVO, Map<String, Object> model,
             PointValueTime pointValue) {
         String prettyText = Functions.getHtmlText(pointVO, pointValue);
         model.put("text", prettyText);
         if (!ObjectUtils.equals(pointVO.lastValue(), pointValue)) {
             state.setValue(prettyText);
             if (pointValue != null)
                 state.setTime(Functions.getTime(pointValue));
             pointVO.updateLastValue(pointValue);
         }
     }
 
     //
     // Share users
     //
     @DwrPermission(user = true)
     public List<ShareUser> addUpdateSharedUser(int userId, int accessType) {
         FlexWatchList watchList = getWatchList();
         boolean found = false;
         for (ShareUser su : watchList.getWatchListUsers()) {
             if (su.getUserId() == userId) {
                 found = true;
                 su.setAccessType(accessType);
                 break;
             }
         }
 
         if (!found) {
             ShareUser su = new ShareUser();
             su.setUserId(userId);
             su.setAccessType(accessType);
             watchList.getWatchListUsers().add(su);
         }
 
         new FlexWatchListDao().saveWatchList(watchList);
 
         return watchList.getWatchListUsers();
     }
 
     @DwrPermission(user = true)
     public List<ShareUser> removeSharedUser(int userId) {
         FlexWatchList watchList = getWatchList();
 
         for (ShareUser su : watchList.getWatchListUsers()) {
             if (su.getUserId() == userId) {
                 watchList.getWatchListUsers().remove(su);
                 break;
             }
         }
 
         new FlexWatchListDao().saveWatchList(watchList);
 
         return watchList.getWatchListUsers();
     }
 
     private void setWatchList(User user, FlexWatchList watchList) {
         user.setAttribute("watchList", watchList);
     }
 
     private static FlexWatchList getWatchList() {
         return getWatchList(Common.getUser());
     }
 
     private static FlexWatchList getWatchList(User user) {
         return user.getAttribute("watchList", FlexWatchList.class);
     }
 
     @DwrPermission(anonymous = true)
     public void resetWatchListState(int pollSessionId) {
         LongPollData data = getLongPollData(pollSessionId, false);
 
         synchronized (data.getState()) {
             FlexWatchListCommon.getWatchListStates(data).clear();
             FlexWatchList wl = getWatchList();
             for (DataPointVO dp : wl.getPointList())
                 dp.resetLastValue();
         }
         notifyLongPollImpl(data.getRequest());
     }
 
     @DwrPermission(user = true)
     public void getChartData(int[] pointIds, int fromYear, int fromMonth, int fromDay, int fromHour, int fromMinute,
             int fromSecond, boolean fromNone, int toYear, int toMonth, int toDay, int toHour, int toMinute,
             int toSecond, boolean toNone) {
         User user = Common.getUser();
         DateTimeZone dtz = user.getDateTimeZoneInstance();
         DateTime from = createDateTime(fromYear, fromMonth, fromDay, fromHour, fromMinute, fromSecond, fromNone, dtz);
         DateTime to = createDateTime(toYear, toMonth, toDay, toHour, toMinute, toSecond, toNone, dtz);
         DataExportDefinition def = new DataExportDefinition(pointIds, from, to);
         user.setDataExportDefinition(def);
     }
     @DwrPermission(anonymous = true)
     public Map<String, Object> handleRequestInternal(String dpid) {
     	
 		HttpServletRequest request = WebContextFactory.get()
 				.getHttpServletRequest();
 		User user = Common.getUser(request);
 		
         Map<String, Object> model = new HashMap<String, Object>();
 
 
         int id;
         DataPointDao dataPointDao = new DataPointDao();
         String idStr = dpid;
         DataPointVO point = null;
 
 
             id = Integer.parseInt(idStr);
 
         // Put the point in the model.
         if (point == null)
             point = dataPointDao.getDataPoint(id);
 
         if (point != null) {
             Permissions.ensureDataPointReadPermission(user, point);
 
             model.put("point", point);
         }
 
 
         // Set the point in the session for the dwr.
         user.setEditPoint(point);
 
         // Find accessible points for the goto list
      //   ControllerUtils.addPointListDataToModel(user, id, model);
 
        // return new ModelAndView(getViewName(), model);
         
         return model;
     }	
     @DwrPermission(anonymous = true)
     public String getPorjectPath(){ 
     	String projectName="web"; 	
	    String nowpath;             //ǰtomcatbinĿ¼·  D:\java\software\apache-tomcat-6.0.14\bin   
 	    String tempdir; 
 	    nowpath=System.getProperty("user.dir");   
 	    tempdir=nowpath.replace("bin", "webapps");   
 	    tempdir+="\\"+projectName;  
 	  
 	  return tempdir;
     }   
     @DwrPermission(user = true)
 	 public  Map<String, Object> readRealTimeDataFromDB(int watchListId){
 			
 		 //System.out.println("readRealTimeDataFromDB watchListId ="+watchListId);
 		//	User user = Common.getUser();
 			
 		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
 		User user = Common.getUser(request);
 		
 			FlexWatchListDao watchListDao = new FlexWatchListDao();
 			FlexWatchList watchList = watchListDao.getWatchList(watchListId);
 		//	Permissions.ensureWatchListPermission(user, watchList);
 			prepareWatchList(watchList, user);
 
 
 			Map<String, Object> data = new HashMap<String, Object>();
 			if (watchList == null)
 				return data;
 
 			List<DataPointVO> points = watchList.getPointList();
 			//List<Integer> pointIds = new ArrayList<Integer>(points.size());
 			List<String> watchListStateValues = new ArrayList<String>(points.size());
 			for (DataPointVO point : points) {
 				if (Permissions.hasDataPointReadPermission(user, point))
 				{
 					
 
 
 	// Check permissions.
 					Permissions.ensureDataPointReadPermission(user, point);
 				//	Permissions.ensureWatchListEditPermission(user, watchList);
 	// Return the watch list state for it.
 					FlexWatchListState watchListState = createWatchListState(request, point,
 							Common.runtimeManager, new HashMap<String, Object>(),user);
 
 
 					watchListStateValues.add(watchListState.getValue());
 				}
 			}
 			
 			PointValueDao pointValueDao = new PointValueDao();
 			MeterItem meterItem = new MeterItem();
 			List<MeterItem> meterItemList = meterItem.getMeterItemRootList(pointValueDao,0);
 			HourPowerPointValueDao hourPowerPointValueDao = new HourPowerPointValueDao();
 			
 			List<String> powerTopLevelValues = new ArrayList<String>(meterItemList.size());
 			for (MeterItem tempMeterItem : meterItemList) {
 				
 				PointValueTime  pointValueTime = hourPowerPointValueDao.getLatestPointValue(tempMeterItem.getCode(),1);
 				
 				if ( pointValueTime == null){
 					powerTopLevelValues.add("0");
 				}else{
 					DataValue baseValue = pointValueTime.getValue();
 					//Double sumValue = new Double();
 					watchListStateValues.add(baseValue.numberValue().toString());
 					System.out.println("Code = "+ tempMeterItem.getCode() + " value = "+ baseValue.numberValue());
 				}
 
 			}
 			MeterItemDao meterItemDao = new MeterItemDao();
 			List<MeterItem> meterItemList2 = meterItemDao.getMeterItemListByLevel(4);
 			// List<MeterItem> meterItemList = meterItem.getMeterItemRootList(pointValueDao,0);
 		//	HourPowerPointValueDao hourPowerPointValueDao = new HourPowerPointValueDao();
 			
 		//	List<String> powerTopLevelValues = new ArrayList<String>(meterItemList2.size());
 			for (MeterItem tempMeterItem : meterItemList2) {
 				
 				PointValueTime  pointValueTime = hourPowerPointValueDao.getLatestPointValue(tempMeterItem.getCode(),1);
 				
 				if ( pointValueTime == null){
 					powerTopLevelValues.add("0");
 				}else{
 					DataValue baseValue = pointValueTime.getValue();
 					//Double sumValue = new Double();
 					watchListStateValues.add(baseValue.numberValue().toString());
 					System.out.println("second Code = "+ tempMeterItem.getCode() + " value = "+ baseValue.numberValue());
 				}
 
 			}			
 			
 			data.put("realTimeData", watchListStateValues);
 //			data.put("powerTopLevelValues", powerTopLevelValues);
 			
 			return data;		
 		}
     @DwrPermission(user = true)
 	 public  Map<String, Object> readRealTimePowerDataFromDB(int watchListId){
 			
 
 
 
 			Map<String, Object> data = new HashMap<String, Object>();
 
 		//	PointValueDao pointValueDao = new PointValueDao();
 			//MeterItem meterItem = new MeterItem();
 		/*	
 			MeterItemDao meterItemDao = new MeterItemDao();
 			List<MeterItem> meterItemList = meterItemDao.getMeterItemListByLevel(4);
 			// List<MeterItem> meterItemList = meterItem.getMeterItemRootList(pointValueDao,0);
 			HourPowerPointValueDao hourPowerPointValueDao = new HourPowerPointValueDao();
 			
 			List<String> powerTopLevelValues = new ArrayList<String>(meterItemList.size());
 			for (MeterItem tempMeterItem : meterItemList) {
 				
 				PointValueTime  pointValueTime = hourPowerPointValueDao.getLatestPointValue(tempMeterItem.getCode(),1);
 				
 				if ( pointValueTime == null){
 					powerTopLevelValues.add("0");
 				}else{
 					BaseValue baseValue = pointValueTime.getValue();
 					//Double sumValue = new Double();
 					powerTopLevelValues.add(baseValue.numberValue().toString());
 					System.out.println("second Code = "+ tempMeterItem.getCode() + " value = "+ baseValue.numberValue());
 				}
 
 			}
 			
 			
 		//	data.put("realTimeData", watchListStateValues);
 			data.put("powerTopLevelValues", powerTopLevelValues);
 			*/
 			return data;		
 		}	 
     @DwrPermission(user = true)
 		public DwrResponseI18n getHistoryChartValue(int limit,String dpid) {
 			
 			int id;
 			PointValueDao pointValueDao = new PointValueDao();
 
 	        
 	        id = Integer.parseInt(dpid);
 
 
 			
 		//	PointValueFacade facade = new PointValueFacade(Integer.parseInt(dpid));
 
 			List<PointValueTime> rawData = pointValueDao.getLatestPointValues(id, limit);
 			List<PointValueTime> rawData2 = pointValueDao.getLatestPointValues(id+1, limit);
 //			List<String> renderedData = new ArrayList<String>(
 //					rawData.size());
 			StringBuilder htmlData = new StringBuilder();
 			htmlData.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
 			htmlData.append("<root>");
 			int i = 0;
 	//		float value ;
 			 SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			for (PointValueTime pvt : rawData) {
 				PointValueTime pvt2 = rawData2.get(i);
 				String timeStr1 = sdf.format(pvt.getTime());
 				System.out.println(timeStr1);
 				
 				//renderedData.add(Functions.getHtmlText(pointVO, pvt));
 				htmlData.append("<result>");
 				htmlData.append("<value>");
 				double dbValue = pvt.getDoubleValue();
 				String  strValue = String.format("%.1f",dbValue);
 				htmlData.append(strValue);
 				htmlData.append("</value>");
 				htmlData.append("<value1>");
 			//	int value1 = 2*i;
 				double dbValue2 = pvt2.getDoubleValue();
 				String  strValue2 = String.format("%.1f",dbValue2);				
 				htmlData.append(strValue2);				
 				//htmlData.append(value1);
 				htmlData.append("</value1>");				
 				htmlData.append("<time>");
 			//	String timeStr = Functions.getTime(pvt);
 			//	System.out.println(timeStr);
 				htmlData.append(timeStr1.substring(11, 16));
 				htmlData.append("</time>");
 				htmlData.append("</result>");
 				
 				i++;
 			}
 			htmlData.append("</root>");
 
 			DwrResponseI18n response = new DwrResponseI18n();
 			response.addData("Flexchart", htmlData.toString());
 			 try {   
 				 String projectDir = getPorjectPath();
				 projectDir += "\\"+"WEB-INF\\jsp\\flexChartData.jsp";
 				 System.out.println(projectDir);
 				 BufferedWriter output = new BufferedWriter(new FileWriter(projectDir));
 				   output.write(htmlData.toString());
 				   output.close();
 				  } catch (Exception e) {
 				   e.printStackTrace();
 				  }
 //				 FileWriter fw=new FileWriter(projectDir);  
 //				 fw.write(htmlData.toString());   
 //				 fw.flush();
 //				  fw.close();  
 //				  } catch (IOException e)
 //				 {   e.printStackTrace();
 //				 }
 			return response;
 
 		}		 
 }
